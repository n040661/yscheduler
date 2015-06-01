package com.yeahmobi.yscheduler.web.controller.log;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FSDataInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.yeahmobi.yscheduler.common.Constants;
import com.yeahmobi.yscheduler.common.log.AgentLogUtils;
import com.yeahmobi.yscheduler.common.log.impl.HdfsLogServiceImpl;
import com.yeahmobi.yscheduler.model.Attempt;
import com.yeahmobi.yscheduler.model.Task;
import com.yeahmobi.yscheduler.model.service.AttemptService;
import com.yeahmobi.yscheduler.model.service.TaskService;
import com.yeahmobi.yscheduler.web.controller.AbstractController;

/**
 * @author wukezhu
 */
@Controller
@RequestMapping(value = { LogController.SCREEN_NAME })
public class LogController extends AbstractController {

    private static final String              AGENT_LOG_SPLIT_LINE = IOUtils.LINE_SEPARATOR
                                                                    + "==================== agent log below ===================="
                                                                    + IOUtils.LINE_SEPARATOR;

    public static final String               SCREEN_NAME          = "task/instance/attemptlog";

    private static final Logger              LOGGER               = LoggerFactory.getLogger(LogController.class);

    // 5 min
    private static final long                ENTRY_TIMEOUT_MILLIS = 5 * 60 * 1000;

    @Autowired
    private AttemptService                   attemptService;

    @Autowired
    private TaskService                      taskService;

    @Autowired
    private HdfsLogServiceImpl               logService;

    // key is uuid
    private ConcurrentHashMap<String, Entry> logStreamMap         = new ConcurrentHashMap<String, Entry>();

    @RequestMapping(value = "/clearCache", method = RequestMethod.GET)
    @ResponseBody
    public void clearAttemptLogCache(String uuid) {
        this.logStreamMap.remove(uuid);

        // 顺便清理其他过期的entry
        long currentTime = System.currentTimeMillis();
        for (java.util.Map.Entry<String, Entry> mapEntry : this.logStreamMap.entrySet()) {
            Entry entry = mapEntry.getValue();
            if ((currentTime - entry.lastAccessTime) > ENTRY_TIMEOUT_MILLIS) {
                entry.close();
                this.logStreamMap.remove(mapEntry.getKey());
            }
        }
    }

    @RequestMapping(value = "/tail", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object getAttemptLog(HttpSession session, String uuid, long attemptId) {
        Map<String, Object> map = new HashMap<String, Object>();
        // 获取inputstream
        Entry entry = this.logStreamMap.get(uuid);
        try {
            StringBuilder filterLog = new StringBuilder();

            if (entry == null) {
                Entry tempEntry = new Entry(attemptId);
                if ((entry = this.logStreamMap.putIfAbsent(uuid, tempEntry)) != null) {
                    // 如果put失败(因为重复)，则关闭
                    tempEntry.close();
                } else {
                    entry = tempEntry;
                }

                // 在首部，显示server端log
                String serverLog = entry.attempt.getOutput();
                filterLog.append(serverLog);
                filterLog.append(AGENT_LOG_SPLIT_LINE);
            }

            // mark access
            entry.lastAccessTime = System.currentTimeMillis();

            // 读取logStream
            String agentOrdinaryLog = tryRead(entry, filterLog.length() > 0);

            // checkEndline & filt
            boolean end = false;
            if (agentOrdinaryLog != null) {
                end = checkEndline(entry, agentOrdinaryLog, filterLog);
            }

            // clear when end
            if (end) {
                entry.close();
                this.logStreamMap.remove(uuid);
            }

            map.put("continue", !end);

            map.put("log", filterLog);
            map.put("success", true);
        } catch (IllegalArgumentException e) {
            if (entry != null) {
                entry.close();
            }
            map.put("continue", false);
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
        } catch (Exception e) {
            if (entry != null) {
                entry.close();
            }
            map.put("continue", false);
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
            LOGGER.error(e.getMessage(), e);
        }
        return JSON.toJSONString(map);

    }

    @RequestMapping(value = "/download", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void download(HttpServletRequest request, HttpServletResponse response, long attemptId) throws IOException,
                                                                                                  InterruptedException {
        Attempt attempt = this.attemptService.get(attemptId);
        Task task = this.taskService.get(attempt.getTaskId());

        response.addHeader("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader("Content-Disposition", "attachment;fileName=" + task.getName() + "-attempt-" + attemptId
                                                  + ".log");

        Entry entry = new Entry(attemptId);

        StringBuilder filterLog = new StringBuilder();
        // 在首部，显示server端log
        String serverLog = entry.attempt.getOutput();
        filterLog.append(serverLog);
        filterLog.append(AGENT_LOG_SPLIT_LINE);

        boolean end = false;
        do {

            // 读取logStream
            String agentOrdinaryLog = tryRead(entry, filterLog.length() > 0);

            // checkEndline & filter
            if (agentOrdinaryLog != null) {
                end = checkEndline(entry, agentOrdinaryLog, filterLog);
            }
            // output to client
            IOUtils.write(filterLog, response.getOutputStream());
            response.flushBuffer();
        } while (!end);

    }

    private static String tryRead(Entry entry, boolean onlyTryOnce) throws IOException, InterruptedException {
        int count = 0;

        // 如果一直没有数据，最多会等待10s
        String data = null;
        while ((count++ < 5) && ((data = entry.read()) == null)) {
            if (onlyTryOnce) {
                break;
            }
            Thread.sleep(2000);
        }

        return data;
    }

    private static boolean checkEndline(Entry entry, String log, StringBuilder filterLog) {
        for (int i = 0; i < log.length(); i++) {
            char ch = log.charAt(i);
            if (ch == AgentLogUtils.ENDLINE_SPLIT) {
                // lastLine不匹配EndLine，则将其放回log里
                filterLog.append(entry.lastLine != null ? entry.lastLine : "");
                entry.lastLine = new StringBuilder();
            } else {
                if (entry.lastLine != null) {
                    entry.lastLine.append(ch);
                } else {
                    filterLog.append(ch);
                }
            }

            if ((entry.lastLine != null) && (entry.lastLine.length() == entry.endline.length())) {
                if (entry.lastLine.toString().equals(entry.endline)) {
                    // if end
                    return true;
                } else {
                    // lastLine不匹配EndLine，则将其放回log里
                    filterLog.append(entry.lastLine != null ? entry.lastLine : "");
                    entry.lastLine = null;
                }
            }
        }
        return false;
    }

    class Entry {

        // 由于内存有限，每次最多读取2M到内存
        private static final long MAX_LENGTH_IN_MEMORY = 2 * 1024 * 1024L;
        private static final int  BUFFER_SIZE          = 4096;

        long                      offset;
        final String              endline;
        StringBuilder             lastLine;
        long                      lastAccessTime       = System.currentTimeMillis();
        private Attempt           attempt;                                           ;

        public Entry(long attemptId) throws IOException {
            this.attempt = LogController.this.attemptService.get(attemptId);
            this.endline = AgentLogUtils.getEndline(attemptId);
        }

        public void close() {
            // nothing
        }

        // 注意：hdfs 的 stream无法感知新的数据，故每次都需要新开启
        public String read() throws IOException {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            FSDataInputStream in = null;
            try {
                in = LogController.this.logService.getInputStream(this.attempt.getId());
                in.seek(this.offset);
                IOUtils.copyLarge(in, bout, -1, MAX_LENGTH_IN_MEMORY, new byte[BUFFER_SIZE]);
                this.offset = in.getPos();
                if (bout.size() > 0) {
                    return bout.toString(Constants.LOG_FILE_ENCODE);
                } else {
                    return null;
                }

            } catch (FileNotFoundException e) {
                // 不存在，就不断check
                // if (this.attempt.getStatus().isCompleted()) {
                // // 可能暂无数据，也可能因为未提交给agent(如果attempt失败，则未提交给agent的可能性比较大，故需要插入endline使浏览器停止)
                // return AgentLogUtils.ENDLINE_SPLIT + this.endline;
                // } else {
                return null;
                // }
            } catch (EOFException e) {
                // seek 遇到EOF，说明暂时无数据，则返回null
                return null;
            } finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(bout);
            }
        }

    }
}

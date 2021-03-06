package com.yeahmobi.yscheduler.web.controller.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.yeahmobi.yscheduler.model.Agent;
import com.yeahmobi.yscheduler.model.Task;
import com.yeahmobi.yscheduler.model.User;
import com.yeahmobi.yscheduler.model.common.UserContextHolder;
import com.yeahmobi.yscheduler.model.service.AgentService;
import com.yeahmobi.yscheduler.model.service.TaskService;
import com.yeahmobi.yscheduler.model.service.UserService;
import com.yeahmobi.yscheduler.model.type.TaskType;
import com.yeahmobi.yscheduler.web.controller.AbstractController;

/**
 * @author wukezhu
 */
@Controller
@RequestMapping(value = { TaskDetailController.SCREEN_NAME })
public class TaskDetailController extends AbstractController {

    public static final String SCREEN_NAME = "task/detail";

    @Autowired
    private TaskService        taskService;

    @Autowired
    private UserService        userService;

    @Autowired
    private AgentService       agentService;

    @RequestMapping(value = { "/{taskId}" })
    public ModelAndView task(@PathVariable
    long taskId) {
        Map<String, Object> map = new HashMap<String, Object>();

        Task task = this.taskService.get(taskId);
        User user = this.userService.get(task.getOwner());
        Agent agent = null;
        if (task.getAgentId() != null) {
            agent = this.agentService.get(task.getAgentId());
        }

        // 将命令和描述中的特殊字符转义
        task.setCommand(StringEscapeUtils.escapeHtml(task.getCommand()));
        task.setDescription(StringEscapeUtils.escapeHtml(task.getDescription()));

        if (task.getType() == TaskType.HTTP) {
            String[] split = StringUtils.split(task.getCommand(), ';');
            map.put("calloutUrl", split[0]);
            if (split.length > 1) {
                map.put("needCallback", split[1]);
            }
            if (split.length > 2) {
                map.put("cancelUrl", split[2]);
            }
        }
        long teamId = this.userService.get(UserContextHolder.getUserContext().getId()).getTeamId();
        List<Agent> agents = this.agentService.list(teamId, true);
        if (!agents.contains(agent)) {
            agents.add(agent);
        }

        map.put("task", task);
        map.put("owner", user);
        map.put("agent", agent);

        map.put("users", this.userService.list());
        map.put("agents", agents);

        map.put("canModify", this.taskService.canModify(taskId, UserContextHolder.getUserContext().getId()));

        return screen(map, SCREEN_NAME);
    }
}

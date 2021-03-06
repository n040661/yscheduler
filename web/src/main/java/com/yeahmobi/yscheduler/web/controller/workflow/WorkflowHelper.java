package com.yeahmobi.yscheduler.web.controller.workflow;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yeahmobi.yscheduler.common.CrontabUtils;
import com.yeahmobi.yscheduler.model.Workflow;
import com.yeahmobi.yscheduler.model.WorkflowDetail;
import com.yeahmobi.yscheduler.model.common.UserContextHolder;
import com.yeahmobi.yscheduler.model.service.TaskService;
import com.yeahmobi.yscheduler.model.service.WorkflowDetailService;
import com.yeahmobi.yscheduler.model.service.WorkflowService;
import com.yeahmobi.yscheduler.model.type.DependingStatus;
import com.yeahmobi.yscheduler.model.type.WorkflowStatus;
import com.yeahmobi.yscheduler.web.vo.WorkflowDetailVO;

/**
 * @author Ryan Sun
 */
@Service
public class WorkflowHelper {

    private static final String   TASK_ERROR_MSG = "请检查任务的填写";

    @Autowired
    private WorkflowDetailService workflowDetailService;

    @Autowired
    private WorkflowService       workflowService;

    @Autowired
    private TaskService           taskService;

    @SuppressWarnings("unchecked")
    public void saveWorkflowDetail(List<WorkflowDetailVO> detailVos, Workflow workflow) {
        List<WorkflowDetail> details = new ArrayList<WorkflowDetail>(CollectionUtils.collect(detailVos,
                                                                                             new Transformer() {

                                                                                                 public Object transform(Object input) {
                                                                                                     return ((WorkflowDetailVO) input).getWorkflowDetail();
                                                                                                 }
                                                                                             }));
        List<List<Long>> dependencyList = new ArrayList<List<Long>>(CollectionUtils.collect(detailVos,
                                                                                            new Transformer() {

                                                                                                public Object transform(Object input) {
                                                                                                    return ((WorkflowDetailVO) input).getDependencies();
                                                                                                }
                                                                                            }));
        this.workflowDetailService.save(workflow.getId(), details, dependencyList);
    }

    private Workflow buildAndSaveWorkflow(Integer timeout, String description, String crontab, boolean canSkip,
                                          Workflow workflow, boolean concurrent, String dependingStatus) {
        workflow.setCrontab(crontab);
        workflow.setDescription(description);
        workflow.setOwner(UserContextHolder.getUserContext().getId());
        workflow.setTimeout(timeout);
        if (canSkip) {
            workflow.setCanSkip(true);
            workflow.setLastStatusDependency(DependingStatus.NONE);
        } else {
            workflow.setCanSkip(false);
            if (concurrent) {
                workflow.setLastStatusDependency(DependingStatus.NONE);
            } else {
                if (DependingStatus.COMPLETED.name().equalsIgnoreCase(dependingStatus)) {
                    workflow.setLastStatusDependency(DependingStatus.COMPLETED);
                } else if (DependingStatus.SUCCESS.name().equalsIgnoreCase(dependingStatus)) {
                    workflow.setLastStatusDependency(DependingStatus.SUCCESS);
                }
            }
        }
        this.workflowService.createOrUpdate(workflow);
        return workflow;
    }

    public Workflow createWorkflow(String name, Integer timeout, String description, String crontab, boolean running,
                                   boolean canSkip, boolean concurrent, String dependingStatus) {
        Workflow workflow = new Workflow();
        workflow.setName(name);
        workflow.setCommon(false);
        if (running) {
            workflow.setStatus(WorkflowStatus.OPEN);
        } else {
            workflow.setStatus(WorkflowStatus.PAUSED);
        }
        return buildAndSaveWorkflow(timeout, description, crontab, canSkip, workflow, concurrent, dependingStatus);
    }

    public Workflow updateWorkflow(Long id, Integer timeout, String description, String crontab, boolean canSkip,
                                   boolean concurrent, String dependingStatus) {
        Workflow workflow = new Workflow();
        workflow.setId(id);
        return buildAndSaveWorkflow(timeout, description, crontab, canSkip, workflow, concurrent, dependingStatus);
    }

    public void validate(String name, Integer timeout, String crontab) {
        // name
        Validate.isTrue(StringUtils.isNotBlank(name), "工作流名称不能为空");
        Validate.isTrue(!this.workflowService.nameExist(name), "工作流名称重复");
        validate(timeout, crontab);
    }

    public void validate(Integer timeout, String crontab) {
        // timeout
        Validate.isTrue((timeout != null) && (timeout >= 0), "超时时间不合法");
        // crontab
        Validate.isTrue(StringUtils.isNotBlank(crontab), "调度表达式为空");
        crontab = CrontabUtils.normalize(crontab, false);
    }

    public List<WorkflowDetailVO> parse(HttpServletRequest request) {
        List<WorkflowDetailVO> detailVos = new ArrayList<WorkflowDetailVO>();
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            String strTaskId = request.getParameter("taskId_" + i);
            String strTaskTimeout = request.getParameter("taskTimeout_" + i);
            String strTaskRetrycount = request.getParameter("taskRetrycount_" + i);
            String strTaskDelay = request.getParameter("taskDelay_" + i);
            String strTaskCondition = request.getParameter("taskCondition_" + i);

            String[] strDependencyTaskIds = request.getParameterValues("dependencyTaskId_" + i);
            // 4项都为空，则停止
            if ((strTaskId == null) && (strTaskTimeout == null) && (strTaskRetrycount == null)
                && ((strDependencyTaskIds == null) || (strDependencyTaskIds.length == 0))) {
                break;
            }
            // 验证
            Validate.isTrue(StringUtils.isNotBlank(strTaskId), "任务必选");
            Validate.isTrue(StringUtils.isNotBlank(strTaskCondition), "任务自依赖条件必填");

            // Validate.isTrue(StringUtils.isNotBlank(strTaskTimeout),
            // "任务超时时间必填");
            // Validate.isTrue(StringUtils.isNotBlank(strTaskRetrycount),
            // "任务重试次数必填");

            WorkflowDetail detail = new WorkflowDetail();
            List<Long> dependencies = new ArrayList<Long>();
            WorkflowDetailVO detailVo = new WorkflowDetailVO();

            detailVo.setWorkflowDetail(detail);
            detailVo.setDependencies(dependencies);
            detailVos.add(detailVo);

            try {
                // Integer taskTimeout = Integer.valueOf(strTaskTimeout);
                // Integer taskRetrycount = Integer.valueOf(strTaskRetrycount);
                // 暂未使用
                detail.setRetryTimes(0);
                detail.setTimeout(60);

                Long taskId = Long.valueOf(strTaskId);
                Integer taskDelay = null;
                if (StringUtils.isNotBlank(strTaskDelay)) {
                    taskDelay = Integer.valueOf(strTaskDelay);
                }
                detail.setTaskId(taskId);
                detail.setDelay(taskDelay);
                if (DependingStatus.COMPLETED.name().equalsIgnoreCase(strTaskCondition)) {
                    detail.setLastStatusDependency(DependingStatus.COMPLETED);
                } else if (DependingStatus.SUCCESS.name().equalsIgnoreCase(strTaskCondition)) {
                    detail.setLastStatusDependency(DependingStatus.SUCCESS);
                } else {
                    detail.setLastStatusDependency(DependingStatus.NONE);
                }
                detailVo.setTaskName(this.taskService.get(taskId).getName());
            } catch (RuntimeException e) {
                throw new IllegalArgumentException(TASK_ERROR_MSG, e);
            }

            if (strDependencyTaskIds != null) {
                for (String strDependencyTaskId : strDependencyTaskIds) {
                    Long dependencyTaskId = Long.valueOf(strDependencyTaskId);
                    dependencies.add(dependencyTaskId);
                }
            }
        }
        Validate.isTrue((detailVos.size() > 0), "工作流中的任务至少要有一个");

        WorkflowDependencyValidate validator = new WorkflowDependencyValidate(detailVos);
        validator.validate();

        return detailVos;
    }
}

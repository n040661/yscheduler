package com.yeahmobi.yscheduler.model.dao.mapper;

import com.yeahmobi.yscheduler.model.TeamWorkflowInstanceStatus;
import com.yeahmobi.yscheduler.model.TeamWorkflowInstanceStatusExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface TeamWorkflowInstanceStatusMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table team_workflow_instance_status
     *
     * @mbggenerated
     */
    int countByExample(TeamWorkflowInstanceStatusExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table team_workflow_instance_status
     *
     * @mbggenerated
     */
    int deleteByExample(TeamWorkflowInstanceStatusExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table team_workflow_instance_status
     *
     * @mbggenerated
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table team_workflow_instance_status
     *
     * @mbggenerated
     */
    int insert(TeamWorkflowInstanceStatus record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table team_workflow_instance_status
     *
     * @mbggenerated
     */
    int insertSelective(TeamWorkflowInstanceStatus record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table team_workflow_instance_status
     *
     * @mbggenerated
     */
    List<TeamWorkflowInstanceStatus> selectByExampleWithRowbounds(TeamWorkflowInstanceStatusExample example, RowBounds rowBounds);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table team_workflow_instance_status
     *
     * @mbggenerated
     */
    List<TeamWorkflowInstanceStatus> selectByExample(TeamWorkflowInstanceStatusExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table team_workflow_instance_status
     *
     * @mbggenerated
     */
    TeamWorkflowInstanceStatus selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table team_workflow_instance_status
     *
     * @mbggenerated
     */
    int updateByExampleSelective(@Param("record") TeamWorkflowInstanceStatus record, @Param("example") TeamWorkflowInstanceStatusExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table team_workflow_instance_status
     *
     * @mbggenerated
     */
    int updateByExample(@Param("record") TeamWorkflowInstanceStatus record, @Param("example") TeamWorkflowInstanceStatusExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table team_workflow_instance_status
     *
     * @mbggenerated
     */
    int updateByPrimaryKeySelective(TeamWorkflowInstanceStatus record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table team_workflow_instance_status
     *
     * @mbggenerated
     */
    int updateByPrimaryKey(TeamWorkflowInstanceStatus record);
}
package com.yeahmobi.yscheduler.model.dao.mapper;

import com.yeahmobi.yscheduler.model.TaskInstance;
import com.yeahmobi.yscheduler.model.TaskInstanceExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface TaskInstanceMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table task_instance
     *
     * @mbggenerated
     */
    int countByExample(TaskInstanceExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table task_instance
     *
     * @mbggenerated
     */
    int deleteByExample(TaskInstanceExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table task_instance
     *
     * @mbggenerated
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table task_instance
     *
     * @mbggenerated
     */
    int insert(TaskInstance record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table task_instance
     *
     * @mbggenerated
     */
    int insertSelective(TaskInstance record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table task_instance
     *
     * @mbggenerated
     */
    List<TaskInstance> selectByExampleWithRowbounds(TaskInstanceExample example, RowBounds rowBounds);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table task_instance
     *
     * @mbggenerated
     */
    List<TaskInstance> selectByExample(TaskInstanceExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table task_instance
     *
     * @mbggenerated
     */
    TaskInstance selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table task_instance
     *
     * @mbggenerated
     */
    int updateByExampleSelective(@Param("record") TaskInstance record, @Param("example") TaskInstanceExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table task_instance
     *
     * @mbggenerated
     */
    int updateByExample(@Param("record") TaskInstance record, @Param("example") TaskInstanceExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table task_instance
     *
     * @mbggenerated
     */
    int updateByPrimaryKeySelective(TaskInstance record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table task_instance
     *
     * @mbggenerated
     */
    int updateByPrimaryKey(TaskInstance record);
}
package com.auth.service.system.schedule.mapper;

import com.auth.service.system.schedule.model.entity.LogJobEntity;
import com.auth.service.system.schedule.model.po.LogJobPageRowPO;
import com.auth.service.system.schedule.model.query.LogJobQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 任务日志 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface LogJobMapper extends BaseMapper<LogJobEntity> {

	/**
	 * 分页查询日志
	 * @param page 分页
	 * @param query 条件
	 * @return 分页结果
	 */
	IPage<LogJobPageRowPO> selectListByPage(@Param("page") Page<LogJobEntity> page, @Param("query") LogJobQuery query);

	/**
	 * 按任务取最近若干条（按时间倒序），用于熔断判断
	 * @param jobName 任务名
	 * @param jobGroup 分组
	 * @param limit 条数
	 * @return 日志列表
	 */
	List<LogJobEntity> selectRecentByJobNameAndGroup(@Param("jobName") String jobName,
			@Param("jobGroup") String jobGroup, @Param("limit") int limit);

	/**
	 * 批量查询各任务最近一条执行日志
	 * @param jobIds 任务主键列表
	 * @return 每个任务最近一条日志
	 */
	List<LogJobEntity> selectLatestByJobIds(@Param("jobIds") List<Long> jobIds);

}

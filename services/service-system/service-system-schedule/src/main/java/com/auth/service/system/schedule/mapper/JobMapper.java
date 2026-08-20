package com.auth.service.system.schedule.mapper;

import com.auth.service.system.schedule.model.entity.JobEntity;
import com.auth.service.system.schedule.model.po.SysJobDetailRowPO;
import com.auth.service.system.schedule.model.po.SysJobPageRowPO;
import com.auth.service.system.schedule.model.query.SysJobQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 定时任务 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface JobMapper extends BaseMapper<JobEntity> {

	/**
	 * 分页查询任务
	 * @param page 分页
	 * @param query 条件
	 * @return 分页结果
	 */
	IPage<SysJobPageRowPO> selectListByPage(@Param("page") Page<JobEntity> page, @Param("query") SysJobQuery query);

	/**
	 * 根据主键查询详情
	 * @param id 主键
	 * @return 详情行，不存在时返回 null
	 */
	SysJobDetailRowPO selectDetailById(@Param("id") Long id);

}

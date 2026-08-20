package com.auth.service.system.schedule.mapper;

import com.auth.service.system.schedule.model.entity.JobGroupEntity;
import com.auth.service.system.schedule.model.po.JobGroupPageRowPO;
import com.auth.service.system.schedule.model.query.SysJobGroupQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 任务分组 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface JobGroupMapper extends BaseMapper<JobGroupEntity> {

	/**
	 * 分页查询分组
	 * @param page 分页
	 * @param query 条件
	 * @return 分页结果
	 */
	IPage<JobGroupPageRowPO> selectListByPage(@Param("page") Page<JobGroupEntity> page,
			@Param("query") SysJobGroupQuery query);

	/**
	 * 启用分组下拉（编码/名称模糊）
	 * @param keyword 关键字
	 * @param limit 限制数量
	 * @return 分组下拉
	 */
	List<JobGroupPageRowPO> selectEnabledOptions(@Param("keyword") String keyword, @Param("limit") int limit);

}

package com.auth.service.system.admin.mapper.admin.log;

import com.auth.service.system.admin.model.entity.LogUserPasswordHistoryEntity;
import com.auth.service.system.admin.model.po.loguserpasswordhistory.LogUserPasswordHistoryDetailRowPO;
import com.auth.service.system.admin.model.po.loguserpasswordhistory.LogUserPasswordHistoryPageRowPO;
import com.auth.service.system.admin.model.query.log.LogUserPasswordHistoryQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 密码历史日志 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface LogUserPasswordHistoryMapper extends BaseMapper<LogUserPasswordHistoryEntity> {

	/**
	 * 分页查询密码历史日志
	 * @param page 分页参数
	 * @param query 查询条件
	 * @return 分页结果
	 */
	IPage<LogUserPasswordHistoryPageRowPO> selectListByPage(@Param("page") Page<LogUserPasswordHistoryEntity> page,
			@Param("query") LogUserPasswordHistoryQuery query);

	/**
	 * 根据ID查询密码历史日志详情 用户状态可被禁用、删除等
	 * @param id ID
	 * @return 密码历史日志详情
	 */
	LogUserPasswordHistoryDetailRowPO selectDetailById(@Param("id") Long id);

}

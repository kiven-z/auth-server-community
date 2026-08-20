package com.auth.service.system.admin.mapper.admin.log;

import com.auth.service.system.admin.model.entity.LogAuthorizationAuditEntity;
import com.auth.service.system.admin.model.po.logauthorizationaudit.LogAuthorizationAuditPageRowPO;
import com.auth.service.system.admin.model.query.log.LogAuthorizationAuditQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 权限决策审计日志 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface LogAuthorizationAuditMapper extends BaseMapper<LogAuthorizationAuditEntity> {

	/**
	 * 分页查询权限决策审计
	 * @param page 分页
	 * @param query 条件
	 * @return 分页数据
	 */
	IPage<LogAuthorizationAuditPageRowPO> selectListByPage(@Param("page") Page<LogAuthorizationAuditEntity> page,
			@Param("query") LogAuthorizationAuditQuery query);

}

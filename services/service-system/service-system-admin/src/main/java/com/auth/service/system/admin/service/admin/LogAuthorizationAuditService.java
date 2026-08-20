package com.auth.service.system.admin.service.admin;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.entity.LogAuthorizationAuditEntity;
import com.auth.service.system.admin.model.query.log.LogAuthorizationAuditQuery;
import com.auth.service.system.admin.model.vo.logauthorizationaudit.LogAuthorizationAuditDetailVO;
import com.auth.service.system.admin.model.vo.logauthorizationaudit.LogAuthorizationAuditPageVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 权限决策审计日志服务
 *
 * @author Bunny
 */
public interface LogAuthorizationAuditService extends IService<LogAuthorizationAuditEntity> {

	/**
	 * 分页查询权限决策审计日志
	 * @param query 条件
	 * @return 分页数据
	 */
	PageResponse<LogAuthorizationAuditPageVO> getPage(LogAuthorizationAuditQuery query);

	/**
	 * 获取权限决策审计日志详情
	 * @param id 主键
	 * @return 详情
	 */
	LogAuthorizationAuditDetailVO getDetail(Long id);

}

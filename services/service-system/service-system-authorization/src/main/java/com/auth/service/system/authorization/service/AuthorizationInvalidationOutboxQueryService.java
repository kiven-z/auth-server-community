package com.auth.service.system.authorization.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.authorization.model.query.AuthorizationInvalidationOutboxQuery;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationOutboxDetailVO;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationOutboxPageVO;

/**
 * 授权失效 Outbox 运维查询服务
 *
 * @author Bunny
 */
public interface AuthorizationInvalidationOutboxQueryService {

	/**
	 * 分页查询 Outbox
	 * @param query 查询条件
	 * @return 分页结果
	 */
	PageResponse<AuthorizationInvalidationOutboxPageVO> getPage(AuthorizationInvalidationOutboxQuery query);

	/**
	 * 查询 Outbox 详情
	 * @param id 主键
	 * @return 详情
	 */
	AuthorizationInvalidationOutboxDetailVO getDetail(Long id);

}

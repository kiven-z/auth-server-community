package com.auth.service.auth.service;

import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;

/**
 * 授权失效编排唯一入口
 *
 * @author Bunny
 */
public interface AuthorizationInvalidationService {

	/**
	 * 执行授权失效流水线
	 * @param request 统一失效请求
	 * @return 执行结果摘要
	 */
	AuthorizationInvalidateResponse invalidate(AuthorizationInvalidateRequest request);

}

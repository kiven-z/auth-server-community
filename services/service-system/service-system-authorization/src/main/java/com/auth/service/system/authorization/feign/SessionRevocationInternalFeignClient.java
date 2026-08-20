package com.auth.service.system.authorization.feign;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.authorization.feign.fallback.SessionRevocationInternalFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 会话撤销内部 Feign 客户端（IAM 批量踢下线）
 *
 * @author Bunny
 */
@FeignClient(name = "service-auth", contextId = "sessionRevocationInternalFeignClient",
		path = "/api/auth/inner/sessions", fallback = SessionRevocationInternalFeignClientFallback.class)
public interface SessionRevocationInternalFeignClient {

	/**
	 * 批量踢出用户全部会话（账户状态变更、删除等）
	 * @param userIds 用户 ID 列表
	 * @return 统一响应
	 */
	@PostMapping("/kick-all")
	Result<Void> kickAllSessions(@RequestBody(required = false) List<Long> userIds);

}

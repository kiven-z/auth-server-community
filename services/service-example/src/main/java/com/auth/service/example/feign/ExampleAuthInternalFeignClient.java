package com.auth.service.example.feign;

import com.auth.common.core.model.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 调用鉴权服务内部接口（用于验证 Feign 出站 X-Internal-JWT）
 *
 * @author Bunny
 */
@FeignClient(name = "service-auth", path = "/api/auth/")
public interface ExampleAuthInternalFeignClient {

	/**
	 * 刷新令牌（refreshToken 由 HttpOnly Cookie 读取）
	 * @return 结果
	 */
	@Operation(summary = "刷新令牌")
	@PostMapping("refresh-token")
	Result<Object> refreshToken();

}

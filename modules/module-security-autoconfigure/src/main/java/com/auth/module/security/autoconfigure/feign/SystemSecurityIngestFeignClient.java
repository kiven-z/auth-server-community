package com.auth.module.security.autoconfigure.feign;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.contract.dto.AuthorizationAuditIngestRequest;
import com.auth.module.security.contract.dto.OperationLogIngestRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 调用 service-system 内部接口：持久化授权审计与操作日志。
 *
 * @author Bunny
 */
@FeignClient(name = "service-system", contextId = "systemSecurityIngest", path = "/api/system")
public interface SystemSecurityIngestFeignClient {

	/**
	 * 追加一条授权审计记录。
	 * @param body 上报负载
	 * @return 统一响应
	 */
	@PostMapping("inner/authorization-audit/records")
	Result<Void> appendAuthorizationAudit(@RequestBody AuthorizationAuditIngestRequest body);

	/**
	 * 追加一条操作日志。
	 * @param body 上报负载
	 * @return 统一响应
	 */
	@PostMapping("inner/operation-log/records")
	Result<Void> appendOperationLog(@RequestBody OperationLogIngestRequest body);

}

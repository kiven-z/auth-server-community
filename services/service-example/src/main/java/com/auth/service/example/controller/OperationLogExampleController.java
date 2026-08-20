package com.auth.service.example.controller;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfigure.annotation.PublicApi;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.module.security.contract.context.OperationLogContext;
import com.auth.service.example.audit.ExampleAuditBizModule;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 操作日志示例控制器 用于演示操作日志的记录和查询
 *
 * @author Bunny
 */
@RequestMapping("/api/example/operation-log-example")
@RestController
public class OperationLogExampleController {

	@PublicApi
	@OperationLog(serviceDomain = AuditServiceDomain.EXAMPLE, bizModule = ExampleAuditBizModule.EXAMPLE_DEMO,
			operation = OperationLogKind.QUERY)
	@Operation(summary = "示例：操作日志设置目标ID")
	@DeleteMapping("/set-target-id/{id}")
	public Result<String> example(@PathVariable Long id) {
		OperationLogContext.setTargetId(id);
		return Result.success(null, "操作日志示例");
	}

	@PublicApi
	@OperationLog(serviceDomain = AuditServiceDomain.EXAMPLE, bizModule = ExampleAuditBizModule.EXAMPLE_DEMO,
			operation = OperationLogKind.QUERY)
	@Operation(summary = "示例：操作日志携带请求参数")
	@PostMapping("request-params")
	public Result<Map<String, Object>> requestParams(@RequestBody Map<String, Object> data) {
		return Result.success(data, "操作日志示例");
	}

}

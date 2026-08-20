package com.auth.service.example.controller.internal;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfigure.annotation.InternalApi;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 定时任务调度集成验证用 HTTP 接口
 *
 * @author Bunny
 */
@Slf4j
@RestController
public class InternalScheduleExampleController {

	private static final String INNER_PATH = "/api/example/inner/schedule";

	@InternalApi
	@Operation(summary = "联调：定时任务内部 GET", description = "FeignInvoke 推荐路径")
	@GetMapping(INNER_PATH)
	public Result<String> innerGet() {
		return Result.success("ok-schedule-inner-get");
	}

	@InternalApi
	@Operation(summary = "联调：定时任务内部 POST", description = "回显请求体")
	@PostMapping(INNER_PATH)
	public Result<Map<String, Object>> innerPost(@RequestBody(required = false) Map<String, Object> body) {
		return echoBody("schedule-inner-post", body);
	}

	@InternalApi
	@Operation(summary = "联调：定时任务内部 PUT", description = "回显请求体")
	@PutMapping(INNER_PATH)
	public Result<Map<String, Object>> innerPut(@RequestBody(required = false) Map<String, Object> body) {
		return echoBody("schedule-inner-put", body);
	}

	@InternalApi
	@Operation(summary = "联调：定时任务内部 DELETE", description = "回显请求体")
	@DeleteMapping(INNER_PATH)
	public Result<Map<String, Object>> innerDelete(@RequestBody(required = false) Map<String, Object> body) {
		return echoBody("schedule-inner-delete", body);
	}

	@InternalApi
	@Operation(summary = "联调：查询定时任务调用方身份", description = "内部 GET，回显调用方服务身份")
	@GetMapping(INNER_PATH + "/caller")
	public Result<Map<String, Object>> innerCaller() {
		AuthProfile profile = SecurityUserUtils.currentAuthProfile();
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("username", profile != null ? profile.getUsername() : "unknown");
		payload.put("roles", profile != null ? profile.getRoles() : null);
		return Result.success(payload, "ok-schedule-inner-caller");
	}

	/**
	 * 回显请求体，便于校验 FeignInvoke 的 method 与 body 透传
	 * @param tag 日志与 message 标识
	 * @param body 请求体
	 * @return 回显结果
	 */
	private Result<Map<String, Object>> echoBody(String tag, Map<String, Object> body) {
		log.debug("{} body={}", tag, body);
		return Result.success(body, "ok-" + tag);
	}

}

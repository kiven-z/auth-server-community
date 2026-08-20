package com.auth.module.security.autoconfigure.security;

import com.auth.module.security.autoconfigure.pipeline.authenticate.SecurityAuthExecutor;
import jakarta.servlet.http.HttpServletRequest;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * 按 {@link SecurityRequirement} 分派认证动作（避免在过滤器中写分支）
 *
 * @author Bunny
 */
public enum AuthAction {

	/**
	 * 允许访问
	 */
	ALLOW((executor, request) -> {
	}),

	/**
	 * 需要认证
	 */
	REQUIRE(SecurityAuthExecutor::require),

	/**
	 * 需要内部认证：仅允许 X-Internal-JWT
	 */
	REQUIRE_INTERNAL(SecurityAuthExecutor::requireInternal),

	/**
	 * 尝试认证
	 */
	TRY(SecurityAuthExecutor::tryAuthenticate);

	private final BiConsumer<SecurityAuthExecutor, HttpServletRequest> runner;

	AuthAction(BiConsumer<SecurityAuthExecutor, HttpServletRequest> runner) {
		this.runner = runner;
	}

	/**
	 * 默认分派表：注解优先 + 路径兜底
	 * @return 分派表
	 */
	public static Map<SecurityRequirement, AuthAction> defaultDispatch() {
		Map<SecurityRequirement, AuthAction> map = new EnumMap<>(SecurityRequirement.class);
		map.put(SecurityRequirement.PUBLIC, ALLOW);
		map.put(SecurityRequirement.INTERNAL, REQUIRE_INTERNAL);
		map.put(SecurityRequirement.AUTHENTICATED, REQUIRE);
		map.put(SecurityRequirement.FALLBACK_TO_PATH, TRY);
		return map;
	}

	/**
	 * 执行动作
	 * @param executor 认证执行器
	 * @param request HTTP 请求
	 */
	public void execute(SecurityAuthExecutor executor, HttpServletRequest request) {
		runner.accept(executor, request);
	}

}

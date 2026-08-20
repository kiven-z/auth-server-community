package com.auth.module.security.core.token.model;

import com.auth.common.jwt.model.JwtUserToken;
import com.auth.module.security.contract.constants.SecurityTokenKind;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 验证令牌解析结果（claims 快照）
 *
 * @author Bunny
 */
@Getter
@Setter
@Builder
public class SecurityTokenResult {

	/**
	 * 令牌类型
	 */
	private final SecurityTokenKind kind;

	/**
	 * 原始令牌
	 */
	private final String rawToken;

	/**
	 * 令牌 claims
	 */
	private final JwtUserToken userToken;

	/**
	 * 访问令牌中的权限版本快照（JWT perm_version）；非外部 Access 或未携带时为 null
	 */
	private final Long permVersion;

	/**
	 * 内部令牌主体类型（USER / SERVICE）；非内部令牌为 null
	 */
	private final String principalType;

	/**
	 * 服务身份令牌携带的服务名（一般为 spring.application.name）；非服务令牌为 null
	 */
	private final String serviceId;

}

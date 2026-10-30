package com.auth.module.security.contract.profile;

import com.auth.module.security.contract.profile.datascope.DataScopeDimension;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 用户授权画像
 *
 * @author Bunny
 */
@Getter
@Builder
@Jacksonized
public class AuthProfile implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private final Long userId;

	private final String username;

	@Builder.Default
	private final List<String> roles = Collections.emptyList();

	@Builder.Default
	private final List<String> permissions = Collections.emptyList();

	/**
	 * 按数据维索引的范围
	 * <p>
	 * 同一维内用户行覆盖角色行；该维没有配置时为 SELF。values 在登录时已经展开
	 * </p>
	 */
	@Builder.Default
	private final Map<DataScopeDimension, ScopeGrant> dataScopes = Map.of();

	/**
	 * 权限/画像版本号
	 * <p>
	 * 用于与 Redis 中的 permVersion 对比
	 * </p>
	 */
	private final Long permVersion;

}

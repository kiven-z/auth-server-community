package com.auth.module.security.contract.api.authorization;

import com.auth.module.security.contract.api.datascope.DataScopeStorageType;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 部门维数据范围
 *
 * @author Bunny
 */
@Getter
@Builder
@Jacksonized
public class ScopeGrant implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 授权范围类型
	 */
	private final DataScopeStorageType scopeType;

	/**
	 * 范围取值列表
	 */
	@Builder.Default
	private final List<Long> values = Collections.emptyList();

}

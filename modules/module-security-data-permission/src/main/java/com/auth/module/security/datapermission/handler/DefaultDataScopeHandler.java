package com.auth.module.security.datapermission.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.datascope.DataScopeStorageType;
import com.auth.module.security.contract.constants.PermissionConstant;
import com.auth.module.security.datapermission.annotation.DataScope;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 默认处理器：按部门/自身范围生成 SQL 条件
 *
 * @author Bunny
 */
public class DefaultDataScopeHandler implements DataScopeHandler {

	private static final String FAIL_CLOSE_CONDITION = "1 = 0";

	private static final Pattern SAFE_IDENTIFIER = Pattern.compile("^[A-Za-z_]\\w*$");

	@Override
	public String buildCondition(AuthProfile profile, DataScope dataScope) {
		if (profile == null || profile.getUserId() == null || dataScope == null) {
			return null;
		}
		if (PermissionConstant.isSuperAdmin(profile.getUserId())) {
			return null;
		}

		String safeAlias = safeIdentifier(dataScope.alias());
		String safeUserColumn = safeQualifiedColumn(safeAlias, dataScope.userColumn());
		String safeDimensionColumn = safeQualifiedColumn(safeAlias, dataScope.dimensionColumn());
		if (safeUserColumn == null || safeDimensionColumn == null) {
			return FAIL_CLOSE_CONDITION;
		}

		ScopeGrant grant = profile.getDeptScope();
		DataScopeStorageType scopeType = resolveEffectiveScopeType(dataScope.scope(), grant);
		return switch (scopeType) {
			case ALL -> null;
			case SELF, FROM_PROFILE -> safeUserColumn + " = " + profile.getUserId();
			case DEPT, DEPT_AND_CHILD -> {
				List<Long> grantValues = grant == null ? List.of() : grant.getValues();
				List<Long> normalizeValues = Objects.requireNonNullElse(grantValues, List.<Long>of())
					.stream()
					.filter(Objects::nonNull)
					.collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), List::copyOf));

				yield buildDimensionInCondition(safeDimensionColumn, normalizeValues);
			}
		};
	}

	/**
	 * 裁决最终生效范围：注解强制优先，其次画像，最后 SELF 兜底。
	 */
	private DataScopeStorageType resolveEffectiveScopeType(DataScopeStorageType annotatedScope, ScopeGrant grant) {
		if (annotatedScope != null && annotatedScope != DataScopeStorageType.FROM_PROFILE) {
			return annotatedScope;
		}
		if (grant == null || grant.getScopeType() == null
				|| grant.getScopeType() == DataScopeStorageType.FROM_PROFILE) {
			return DataScopeStorageType.SELF;
		}
		return grant.getScopeType();
	}

	/**
	 * 部门范围 IN 条件；无有效 ID 时 fail-close
	 */
	private String buildDimensionInCondition(String dimensionColumn, List<Long> values) {
		if (CollUtil.isEmpty(values)) {
			return FAIL_CLOSE_CONDITION;
		}
		String ids = values.stream().map(String::valueOf).collect(Collectors.joining(","));
		return dimensionColumn + " IN (" + ids + ")";
	}

	/**
	 * 校验标识符是否可作为 SQL 列名/别名
	 */
	private String safeIdentifier(String raw) {
		if (CharSequenceUtil.isBlank(raw)) {
			return null;
		}
		String normalized = CharSequenceUtil.trim(raw);
		return SAFE_IDENTIFIER.matcher(normalized).matches() ? normalized : null;
	}

	/**
	 * 拼装安全的列引用（含可选表别名）
	 */
	private String safeQualifiedColumn(String alias, String column) {
		String normalizedColumn = safeIdentifier(column);
		if (normalizedColumn == null) {
			return null;
		}
		return CharSequenceUtil.isBlank(alias) ? normalizedColumn : alias + "." + normalizedColumn;
	}

}

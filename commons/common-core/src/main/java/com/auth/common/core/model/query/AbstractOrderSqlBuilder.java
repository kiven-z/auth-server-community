package com.auth.common.core.model.query;

import cn.hutool.core.text.CharSequenceUtil;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 抽象排序 SQL 构建器
 *
 * @author Bunny
 */
public abstract class AbstractOrderSqlBuilder {

	protected AbstractOrderSqlBuilder() {
	}

	/**
	 * 将排序规则转换为 SQL 片段
	 * @param spec 排序规则
	 * @return SQL 片段
	 */
	protected static Optional<String> toSegment(SortSpec spec, Map<String, String> map) {
		if (spec == null || CharSequenceUtil.isBlank(spec.getField())) {
			return Optional.empty();
		}
		String column = map.get(spec.getField());
		if (column == null) {
			return Optional.empty();
		}
		SortDirection direction = Objects.requireNonNullElse(spec.getDirection(), SortDirection.DESC);
		return Optional.of(column + " " + direction.name());
	}

}

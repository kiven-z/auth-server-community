package com.auth.module.security.contract.dto.invalidation;

import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * 失效契约 Payload 共用校验（包内可见，不对外暴露）。
 *
 * @author Bunny
 */
@UtilityClass
class InvalidationPayloadSupport {

	/**
	 * 校验引用非 null。
	 * @param value 待校验值
	 * @param fieldName 字段名
	 * @param <T> 值类型
	 * @return 原值
	 */
	static <T> T requireNonNull(T value, String fieldName) {
		if (value == null) {
			throw new IllegalArgumentException(fieldName + " must not be null");
		}
		return value;
	}

	/**
	 * 校验列表非 null 且非空，并返回不可变拷贝。
	 * @param values 待校验列表
	 * @param fieldName 字段名
	 * @param <T> 元素类型
	 * @return 不可变列表
	 */
	static <T> List<T> copyNonEmpty(List<T> values, String fieldName) {
		if (values == null || values.isEmpty()) {
			throw new IllegalArgumentException(fieldName + " must not be null or empty");
		}
		return List.copyOf(values);
	}

}

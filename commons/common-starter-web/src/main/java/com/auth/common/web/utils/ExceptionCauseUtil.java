package com.auth.common.web.utils;

import lombok.experimental.UtilityClass;

import java.sql.SQLException;

/**
 * 异常原因工具类 用于提取异常原因链中的有用信息
 *
 * @author Bunny
 */
@UtilityClass
public final class ExceptionCauseUtil {

	/**
	 * 获取异常根原因
	 * @param throwable 异常
	 * @return 根原因
	 */
	public static Throwable rootCause(Throwable throwable) {
		Throwable cur = throwable;
		while (cur != null && cur.getCause() != null && cur.getCause() != cur) {
			cur = cur.getCause();
		}
		return cur;
	}

	/**
	 * 获取第一个 {@link SQLException} 异常，或 null 最佳实践：第一个 SQL 异常通常包含最可操作的供应商信息（SQLState,
	 * errorCode）
	 * @param throwable 异常
	 * @return 第一个 SQL 异常
	 */
	public static SQLException firstSqlException(Throwable throwable) {
		for (Throwable cur = throwable; cur != null; cur = cur.getCause()) {
			if (cur instanceof SQLException sqlException) {
				return sqlException;
			}
		}
		return null;
	}

}

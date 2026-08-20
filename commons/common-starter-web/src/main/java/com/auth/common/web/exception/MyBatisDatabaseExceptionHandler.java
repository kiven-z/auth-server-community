package com.auth.common.web.exception;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.core.model.response.Result;
import com.auth.common.web.utils.ExceptionCauseUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.exceptions.PersistenceException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MyBatis 特定持久化异常处理 此建议条件加载以避免与 MyBatis 的硬依赖
 *
 * @author Bunny
 */
@Slf4j
@ConditionalOnClass(name = "org.apache.ibatis.exceptions.PersistenceException")
@Order(5)
@RestControllerAdvice
public class MyBatisDatabaseExceptionHandler extends BaseExceptionResponseBuilder {

	/**
	 * 数据太长异常模式
	 */
	private static final Pattern DATA_TOO_LONG_PATTERN = Pattern.compile("Data too long for column (.*?) at row 1");

	/**
	 * 重复条目异常模式
	 */
	private static final Pattern DUPLICATE_ENTRY_PATTERN = Pattern.compile("Duplicate entry '(.*?)' for key .*");

	/**
	 * 处理MyBatis持久化异常
	 * @param exception 异常
	 * @return 响应实体
	 */
	@ExceptionHandler(PersistenceException.class)
	public ResponseEntity<Result<Object>> handlePersistenceException(PersistenceException exception) {
		return handleDatabaseThrowable(exception, "MyBatis PersistenceException");
	}

	/**
	 * 处理Spring DataAccessException异常
	 */
	@ExceptionHandler({ DataAccessException.class })
	public ResponseEntity<Result<Object>> handleDataAccess(DataAccessException exception) {
		return handleDatabaseThrowable(exception, "Spring DataAccessException");
	}

	/**
	 * 处理数据库操作异常
	 * @param exception 异常
	 * @param logTag 日志标签
	 * @return 响应实体
	 */
	protected ResponseEntity<Result<Object>> handleDatabaseThrowable(Throwable exception, String logTag) {
		SQLException sqlException = ExceptionCauseUtil.firstSqlException(exception);
		if (sqlException == null) {
			return error(HttpStatus.INTERNAL_SERVER_ERROR, 500, "Database operation failed.", exception, logTag);
		}
		return handleSqlException(sqlException, exception, logTag);
	}

	/**
	 * 处理SQL异常
	 * @param sqlException SQL异常
	 * @param wrapper 异常包装器
	 * @param logTag 日志标签
	 * @return 响应实体
	 */
	protected ResponseEntity<Result<Object>> handleSqlException(SQLException sqlException, Throwable wrapper,
			String logTag) {
		String message = sqlException.getMessage();
		if (CharSequenceUtil.isNotBlank(message)) {

			Matcher tooLong = DATA_TOO_LONG_PATTERN.matcher(message);
			if (tooLong.find()) {
				String column = tooLong.group(1);
				return warn(HttpStatus.PAYLOAD_TOO_LARGE, 413, "Value too long for column: " + column, wrapper, logTag);
			}

			Matcher dup = DUPLICATE_ENTRY_PATTERN.matcher(message);
			if (dup.find()) {
				String value = dup.group(1);
				return warn(HttpStatus.CONFLICT, 409, "Duplicate value: [" + value + "].", wrapper, logTag);
			}
		}

		return error(HttpStatus.INTERNAL_SERVER_ERROR, 500, "Database operation failed.", wrapper, logTag);
	}

}

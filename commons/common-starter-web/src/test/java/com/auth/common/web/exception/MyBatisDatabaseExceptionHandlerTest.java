package com.auth.common.web.exception;

import com.auth.common.core.model.response.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link MyBatisDatabaseExceptionHandler} 单元测试
 */
@DisplayName("MyBatisDatabaseExceptionHandler 数据库错误映射")
class MyBatisDatabaseExceptionHandlerTest {

	private final MyBatisDatabaseExceptionHandler handler = new MyBatisDatabaseExceptionHandler();

	@Test
	@DisplayName("无 SQLException 时返回 DATABASE_UNAVAILABLE")
	void handleDataAccess_noSqlException_returnsDatabaseUnavailable() {
		DataAccessException exception = new DataAccessException("connection refused") {
		};

		ResponseEntity<Result<Object>> response = handler.handleDataAccess(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getCode()).isEqualTo(500);
		assertThat(response.getBody().getError()).isEqualTo(CommonWebErrorCodes.DATABASE_UNAVAILABLE);
		assertThat(response.getBody().getSubCode()).isEqualTo(CommonWebErrorCodes.DATABASE_UNAVAILABLE);
		assertThat(response.getBody().getMessage()).isEqualTo("Database operation failed.");
		assertThat(response.getBody().getMessage()).doesNotContain("connection refused");
	}

	@Test
	@DisplayName("列过长返回 DATA_TOO_LONG")
	void handleDataAccess_dataTooLong_returnsDataTooLong() {
		SQLException sqlException = new SQLException("Data too long for column 'username' at row 1");
		DataAccessException exception = new DataAccessException("sql", sqlException) {
		};

		ResponseEntity<Result<Object>> response = handler.handleDataAccess(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getCode()).isEqualTo(413);
		assertThat(response.getBody().getError()).isEqualTo(CommonWebErrorCodes.DATA_TOO_LONG);
		assertThat(response.getBody().getMessage()).isEqualTo("Value too long for column: 'username'");
	}

	@Test
	@DisplayName("唯一键冲突返回 DUPLICATE_ENTRY")
	void handleDataAccess_duplicateEntry_returnsDuplicateEntry() {
		SQLException sqlException = new SQLException("Duplicate entry 'admin' for key 'uk_username'");
		DataAccessException exception = new DataAccessException("sql", sqlException) {
		};

		ResponseEntity<Result<Object>> response = handler.handleDataAccess(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getCode()).isEqualTo(409);
		assertThat(response.getBody().getError()).isEqualTo(CommonWebErrorCodes.DUPLICATE_ENTRY);
		assertThat(response.getBody().getMessage()).isEqualTo("Duplicate value: [admin].");
	}

	@Test
	@DisplayName("其它 SQLException 返回 DATABASE_UNAVAILABLE，不泄露 SQL 原文")
	void handleDataAccess_otherSql_returnsDatabaseUnavailableWithoutSqlText() {
		SQLException sqlException = new SQLException(
				"Deadlock found when trying to get lock; try restarting transaction");
		DataAccessException exception = new DataAccessException("sql", sqlException) {
		};

		ResponseEntity<Result<Object>> response = handler.handleDataAccess(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getError()).isEqualTo(CommonWebErrorCodes.DATABASE_UNAVAILABLE);
		assertThat(response.getBody().getMessage()).isEqualTo("Database operation failed.");
		assertThat(response.getBody().getMessage()).doesNotContain("Deadlock");
	}

}

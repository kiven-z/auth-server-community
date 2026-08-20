package com.auth.common.web.exception;

import com.auth.common.core.exception.RemoteServiceException;
import com.auth.common.core.model.response.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link RemoteServiceExceptionHandler} 单元测试
 */
@DisplayName("RemoteServiceExceptionHandler 远端错误映射")
class RemoteServiceExceptionHandlerTest {

	private final RemoteServiceExceptionHandler handler = new RemoteServiceExceptionHandler();

	@Test
	@DisplayName("透传远端 HTTP 状态与 Result 体")
	void handleRemoteServiceException() {
		Result<Object> remoteBody = Result.error(100312, "DATA_INVALID", "Data anomaly");
		remoteBody.setExt(Map.of("i18nKey", "data.invalid", "i18nArgs", new String[] { "querySnapshotJson" }));
		RemoteServiceException exception = new RemoteServiceException(422, remoteBody);

		ResponseEntity<Result<Object>> response = handler.handleRemoteService(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getCode()).isEqualTo(100312);
		assertThat(response.getBody().getError()).isEqualTo("DATA_INVALID");
		assertThat(response.getBody().getExt()).containsEntry("i18nKey", "data.invalid");
	}

}

package com.auth.common.web.resttemplate.feign;

import com.auth.common.core.exception.RemoteServiceException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ResultFeignErrorDecoder} 单元测试
 */
@DisplayName("ResultFeignErrorDecoder 远端 Result 解码")
class ResultFeignErrorDecoderTest {

	private final ResultFeignErrorDecoder decoder = new ResultFeignErrorDecoder();

	private static Response response(int status, String body) {
		byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
		return Response.builder()
			.status(status)
			.reason("error")
			.request(Request.create(Request.HttpMethod.POST, "/test", Collections.emptyMap(), null,
					StandardCharsets.UTF_8, null))
			.body(bytes)
			.build();
	}

	@Test
	@DisplayName("422 body 为 Result 时转为 RemoteServiceException")
	void decodeBusinessErrorAsRemoteServiceException() {
		String json = "{\"code\":100312,\"message\":\"Data anomaly\",\"error\":\"DATA_INVALID\",\"data\":null}";
		Response response = response(422, json);

		Exception exception = decoder.decode("TestClient#create", response);

		assertThat(exception).isInstanceOf(RemoteServiceException.class);
		RemoteServiceException remote = (RemoteServiceException) exception;
		assertThat(remote.getHttpStatus()).isEqualTo(422);
		assertThat(remote.getResult().getCode()).isEqualTo(100312);
		assertThat(remote.getResult().getError()).isEqualTo("DATA_INVALID");
	}

	@Test
	@DisplayName("非 Result 错误体回退默认解码")
	void decodeNonResultBodyUsesDefaultDecoder() {
		Response response = response(500, "Internal Server Error");

		Exception exception = decoder.decode("TestClient#create", response);

		assertThat(exception).isNotInstanceOf(RemoteServiceException.class);
	}

}

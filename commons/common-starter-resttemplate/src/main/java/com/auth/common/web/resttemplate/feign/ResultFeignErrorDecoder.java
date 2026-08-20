package com.auth.common.web.resttemplate.feign;

import com.auth.common.core.exception.RemoteServiceException;
import com.auth.common.core.model.response.Result;
import com.auth.common.core.utils.JsonSupport;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 将远端 {@link Result} 错误体转为 {@link RemoteServiceException}，避免 Feign 默认抛裸异常
 *
 * @author Bunny
 */
public class ResultFeignErrorDecoder implements ErrorDecoder {

	private final ErrorDecoder defaultDecoder = new Default();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Exception decode(String methodKey, Response response) {
		if (response.body() == null) {
			return defaultDecoder.decode(methodKey, response);
		}
		try {
			byte[] bodyBytes = Util.toByteArray(response.body().asInputStream());
			if (bodyBytes.length == 0) {
				return defaultDecoder.decode(methodKey, response);
			}
			String body = new String(bodyBytes, StandardCharsets.UTF_8);
			Result<?> result = JsonSupport.fromJson(body, Result.class);
			if (result != null && result.getCode() != null && result.getCode() != Result.SUCCESS_CODE) {
				return new RemoteServiceException(response.status(), result);
			}
		}
		catch (IOException | IllegalArgumentException exception) {
			return defaultDecoder.decode(methodKey, response);
		}
		return defaultDecoder.decode(methodKey, response);
	}

}

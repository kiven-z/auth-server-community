package com.auth.gateway.exception;

import com.auth.common.core.model.response.Result;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.Objects;

/**
 * 响应式全局错误处理器，用于 Spring Cloud Gateway（GlobalFilter 等链路上的错误）
 *
 * @author Bunny
 */
@Order(-2)
@Component
public class GatewayErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {

	public GatewayErrorWebExceptionHandler(ErrorAttributes errorAttributes, WebProperties resources,
			ApplicationContext applicationContext, ServerCodecConfigurer serverCodecConfigurer) {
		super(errorAttributes, resources.getResources(), applicationContext);
		this.setMessageWriters(serverCodecConfigurer.getWriters());
	}

	/**
	 * 将枚举上的 HTTP 状态解析为 {@link HttpStatus}；无法解析时回退为 500
	 * @param resultCode 结果码
	 * @return HTTP 状态
	 */
	private static HttpStatus resolveHttpStatus(GatewayResultCodeEnum resultCode) {
		Objects.requireNonNull(resultCode, "resultCode");
		HttpStatus resolved = HttpStatus.resolve(resultCode.getHttpStatus());
		if (resolved != null) {
			return resolved;
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	@Override
	protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
		return RouterFunctions.route(RequestPredicates.all(), request -> {
			Throwable ex = getError(request);
			GatewayResultCodeEnum rc = ex instanceof GatewayBusinessException gbe ? gbe.getResultCode()
					: GatewayResultCodeEnum.INTERNAL_ERROR;

			// 构建网关响应体负载
			Result<Object> body = Result.error(rc.fullBizCode(), rc.getError(), rc.getMessage());
			return ServerResponse.status(resolveHttpStatus(rc)).contentType(MediaType.APPLICATION_JSON).bodyValue(body);
		});
	}

}

package com.auth.gateway.exception;

import lombok.Getter;
import lombok.ToString;

/**
 * 网关业务异常，语义以 {@link GatewayResultCodeEnum} 为唯一来源
 *
 * @author Bunny
 */
@ToString(of = { "resultCode" })
public class GatewayBusinessException extends RuntimeException {

	/**
	 * 网关结果代码
	 */
	@Getter
	private final GatewayResultCodeEnum resultCode;

	public GatewayBusinessException(GatewayResultCodeEnum rc) {
		super(rc != null ? rc.getError() : "GATEWAY_ERROR");
		if (rc == null) {
			throw new IllegalArgumentException("rc is required");
		}
		this.resultCode = rc;
	}

	/**
	 * HTTP 状态码
	 * @return HTTP 状态码
	 */
	public int getHttpStatus() {
		return resultCode.getHttpStatus();
	}

	/**
	 * 完整业务码
	 * @return 业务码
	 */
	public int getBizCode() {
		return resultCode.fullBizCode();
	}

	/**
	 * 稳定错误标识
	 * @return 错误标识
	 */
	public String getError() {
		return resultCode.getError();
	}

}

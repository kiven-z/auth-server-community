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

	@Getter
	private final GatewayResultCodeEnum resultCode;

	public GatewayBusinessException(GatewayResultCodeEnum rc) {
		super(rc != null ? rc.getError() : "GATEWAY_ERROR");
		if (rc == null) {
			throw new IllegalArgumentException("rc is required");
		}
		this.resultCode = rc;
	}

	public int getHttpStatus() {
		return resultCode.getHttpStatus();
	}

	public int getBizCode() {
		return resultCode.fullBizCode();
	}

	public String getError() {
		return resultCode.getError();
	}

}

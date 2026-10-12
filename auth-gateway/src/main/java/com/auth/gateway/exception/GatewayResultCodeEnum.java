package com.auth.gateway.exception;

import lombok.Getter;

/**
 * 网关基础设施错误码，模块前缀为 00
 *
 * @author Bunny
 */
@Getter
public enum GatewayResultCodeEnum {

	/**
	 * 网关内部错误
	 */
	INTERNAL_ERROR(500, 199, "GATEWAY_INTERNAL_ERROR", "Gateway internal error.");

	/**
	 * HTTP状态码
	 */
	private final int httpStatus;

	/**
	 * 业务代码
	 */
	private final int code;

	/**
	 * 错误信息
	 */
	private final String error;

	/**
	 * 响应消息
	 */
	private final String message;

	GatewayResultCodeEnum(int httpStatus, int code, String error, String message) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.error = error;
		this.message = message;
	}

	/**
	 * 完整的5位业务代码：00模块前缀 + bizCode，其中bizCode是CAT*100+SEQ
	 * <p>
	 * 示例：bizCode=201 -> 00201
	 * </p>
	 * @return 完整的5位业务代码
	 */
	public int fullBizCode() {
		return 10_000 + code;
	}

}

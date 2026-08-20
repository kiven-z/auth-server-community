package com.auth.module.security.contract.exception;

import lombok.Getter;

/**
 * Token 模块根异常；携带 {@link SecurityResultCodeEnum} 供集成层映射 HTTP / Result。
 *
 * @author Bunny
 */
@Getter
public class SecurityTokenException extends RuntimeException {

	private final SecurityResultCodeEnum resultCode;

	public SecurityTokenException(SecurityResultCodeEnum resultCode, String message) {
		super(message);
		if (resultCode == null) {
			throw new IllegalArgumentException("resultCode is required");
		}
		this.resultCode = resultCode;
	}

	/**
	 * 稳定错误子码（与 {@link SecurityResultCodeEnum#getError()} 一致），供仍按字符串集成的调用方使用。
	 * @return 稳定子码
	 */
	public String getCode() {
		return resultCode.getError();
	}

}

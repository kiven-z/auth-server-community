package com.auth.common.jwt.exception;

import lombok.Getter;

/**
 * JWT 模块根异常；携带稳定错误码供集成层映射 HTTP / Result 注释中文：对外 message 建议使用英文（由调用方决定）
 *
 * @author Bunny
 */
@Getter
public class JwtException extends RuntimeException {

	private final String code;

	public JwtException(String code, String message) {
		super(message);
		this.code = code;
	}

	public JwtException(String code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}

}

package com.auth.service.auth.exception;

import com.auth.common.core.i18n.I18nMessageProvider;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 认证服务业务异常：语义以 {@link AuthResultCode} 为唯一来源，可选携带 i18n 占位参数
 * <p>
 * {@link #getMessage()} 返回 messageKey 供日志/调试；对外 API 文案由
 * {@link AuthBusinessExceptionResultBuilder} 结合
 * {@link org.springframework.context.MessageSource} 解析
 * </p>
 *
 * @author Bunny
 */
@Getter
public class AuthBusinessException extends RuntimeException implements I18nMessageProvider {

	private final AuthResultCode resultCode;

	private final transient Object[] args;

	public AuthBusinessException(AuthResultCode resultCode, Object... args) {
		super(resultCode != null ? resultCode.getMessageKey() : null);
		this.resultCode = resultCode;
		this.args = Objects.requireNonNullElse(args, new Object[0]);
	}

	@Override
	public String getMessage() {
		if (resultCode == null) {
			return super.getMessage();
		}
		String key = resultCode.getMessageKey();
		if (args.length == 0) {
			return key;
		}
		return key + " " + Arrays.toString(args);
	}

	@Override
	public String getMessageKey() {
		return resultCode != null ? resultCode.getMessageKey() : null;
	}

	@Override
	public Object[] getMessageArgs() {
		return args;
	}

}

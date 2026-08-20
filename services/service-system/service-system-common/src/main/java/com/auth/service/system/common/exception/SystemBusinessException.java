package com.auth.service.system.common.exception;

import com.auth.common.core.i18n.I18nMessageProvider;
import com.auth.service.system.common.exception.code.SystemResultCode;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 系统业务异常，语义以 {@link SystemResultCode} 为来源，可携带 i18n 占位参数
 *
 * @author Bunny
 */
public class SystemBusinessException extends RuntimeException implements I18nMessageProvider {

	@Getter
	private final transient SystemResultCode resultCode;

	private final transient Object[] args;

	public SystemBusinessException(SystemResultCode resultCode, Object... args) {
		super(resultCode != null ? resultCode.getMessageKey() : null);
		this.resultCode = Objects.requireNonNull(resultCode, "resultCode");
		this.args = Objects.requireNonNullElse(args, new Object[0]);
	}

	@Override
	public String getMessage() {
		if (resultCode == null) {
			return super.getMessage();
		}
		try {
			return resultCode.getMessage(args);
		}
		catch (Exception ignored) {
			return resultCode.getMessageKey() + (args.length > 0 ? (" " + Arrays.toString(args)) : "");
		}
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

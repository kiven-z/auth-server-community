package com.auth.service.system.schedule.exception;

import com.auth.common.core.i18n.I18nMessageProvider;
import com.auth.service.system.common.exception.code.SystemResultCode;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 定时任务模块业务异常，语义以 {@link SystemResultCode} 为来源，可携带 i18n 占位参数
 *
 * @author Bunny
 */
@Getter
public class SysJobException extends RuntimeException implements I18nMessageProvider {

	private final transient SystemResultCode resultCode;

	private final transient Object[] args;

	/**
	 * @param resultCode 结果码
	 * @param args i18n 占位参数
	 */
	public SysJobException(SystemResultCode resultCode, Object... args) {
		super(resultCode != null ? resultCode.getMessageKey() : null);
		this.resultCode = Objects.requireNonNull(resultCode, "resultCode");
		this.args = Objects.requireNonNullElse(args, new Object[0]);
	}

	/**
	 * 携带根因的结果码异常（如 Quartz 调度失败需触发事务回滚）
	 * @param resultCode 结果码
	 * @param cause 根因
	 * @param args i18n 占位参数
	 */
	public SysJobException(SystemResultCode resultCode, Throwable cause, Object... args) {
		super(resultCode != null ? resultCode.getMessageKey() : null, cause);
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

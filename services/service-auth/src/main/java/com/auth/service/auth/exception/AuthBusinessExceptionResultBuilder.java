package com.auth.service.auth.exception;

import com.auth.common.core.model.response.Result;
import lombok.experimental.UtilityClass;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * 将 {@link AuthBusinessException} 转为统一 {@link Result}
 *
 * @author Bunny
 */
@UtilityClass
public class AuthBusinessExceptionResultBuilder {

	/**
	 * 构建认证服务错误响应体
	 * @param messageSource 消息源
	 * @param exception 业务异常（须含非空 {@link AuthResultCode}）
	 * @return Result
	 */
	public static Result<Object> build(MessageSource messageSource, AuthBusinessException exception) {
		Objects.requireNonNull(messageSource, "messageSource");
		Objects.requireNonNull(exception, "exception");
		AuthResultCode resultCode = Objects.requireNonNull(exception.getResultCode(), "resultCode");

		Locale locale = LocaleContextHolder.getLocale();
		Object[] args = Objects.requireNonNullElse(exception.getMessageArgs(), new Object[0]);
		String messageKey = Objects.requireNonNull(resultCode.getMessageKey(), "messageKey");
		// 英文兜底：仅用 MessageSource，避免依赖 SpringContextHolder（便于单测与无上下文场景）
		String defaultEnglish = messageSource.getMessage(messageKey, args, messageKey, Locale.ENGLISH);
		String message = messageSource.getMessage(messageKey, args, defaultEnglish, locale);

		Result<Object> body = Result.error(resultCode.getCode(), resultCode.getError(), message);
		body.setExt(Map.of("i18nKey", messageKey, "i18nArgs", args));
		return body;
	}

	/**
	 * 由 {@link AuthResultCode#getHttpStatus()} 解析 {@link HttpStatus}；无法解析时回退 400
	 * @param resultCode 结果码
	 * @return HTTP 状态
	 */
	public static HttpStatus resolveHttpStatus(AuthResultCode resultCode) {
		Objects.requireNonNull(resultCode, "resultCode");
		HttpStatus resolved = HttpStatus.resolve(resultCode.getHttpStatus());
		if (resolved != null) {
			return resolved;
		}
		return HttpStatus.BAD_REQUEST;
	}

}

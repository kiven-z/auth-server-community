package com.auth.service.system.common.exception.code;

import com.auth.common.web.context.SpringContextHolder;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

/**
 * 系统服务结果码契约（模块 10）
 *
 * @author Bunny
 */
public interface SystemResultCode {

	/**
	 * HTTP 状态码
	 * @return HTTP 状态
	 */
	int getHttpStatus();

	/**
	 * 模块内序号（3 位 catSeq）
	 * @return catSeq
	 */
	int getCode();

	/**
	 * 稳定错误标识（写入 Result.error）
	 * @return error 字符串
	 */
	String getError();

	/**
	 * i18n 消息键
	 * @return messageKey
	 */
	String getMessageKey();

	/**
	 * 对外业务码
	 * @return 100_000 + getCode()
	 */
	default int getBizCode() {
		return 100_000 + getCode();
	}

	/**
	 * 按区域渲染消息
	 * @param locale 区域
	 * @param args 占位参数
	 * @return 翻译文案
	 */
	default String getMessage(Locale locale, Object... args) {
		return SpringContextHolder.getMessage(getMessageKey(), args, locale);
	}

	/**
	 * 按当前请求区域渲染消息
	 * @param args 占位参数
	 * @return 翻译文案
	 */
	default String getMessage(Object... args) {
		return getMessage(LocaleContextHolder.getLocale(), args);
	}

	/**
	 * 渲染当前区域消息（无占位参数）
	 * @return 翻译文案
	 */
	default String getMessage() {
		return getMessage(LocaleContextHolder.getLocale());
	}

}

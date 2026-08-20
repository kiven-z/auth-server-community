package com.auth.module.message.api.model.enums;

/**
 * 消息正文编辑格式（跨渠道共用，与发送渠道无关）
 *
 * @author Bunny
 */
public enum MessageContentType {

	/**
	 * 纯文本
	 */
	TEXT,

	/**
	 * Markdown
	 */
	MARKDOWN;

	/**
	 * 解析正文类型；空或未知时按 TEXT
	 * @param raw 原始字符串
	 * @return 正文类型
	 */
	public static MessageContentType from(String raw) {
		MessageContentType parsed = parseOrNull(raw);
		return parsed != null ? parsed : TEXT;
	}

	/**
	 * 严格解析；空或未知返回 null
	 * @param raw 原始字符串
	 * @return 正文类型；无法识别时为 null
	 */
	public static MessageContentType parseOrNull(String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		try {
			return valueOf(raw.trim().toUpperCase());
		}
		catch (IllegalArgumentException ex) {
			return null;
		}
	}

}

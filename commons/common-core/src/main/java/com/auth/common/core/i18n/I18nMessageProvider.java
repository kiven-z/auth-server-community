package com.auth.common.core.i18n;

/**
 * 提供i18n消息元数据用于API响应
 *
 * @author Bunny
 */
public interface I18nMessageProvider {

	/**
	 * i18n消息键，例如"user.not.found"
	 * @return i18n消息键
	 */
	String getMessageKey();

	/**
	 * 可选的消息参数用于格式化
	 * @return message arguments
	 */
	Object[] getMessageArgs();

}

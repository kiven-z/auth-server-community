package com.auth.service.auth.model.enums;

import cn.hutool.core.text.CharSequenceUtil;

import static com.auth.module.security.contract.redis.SecurityRedisKey.EMAIL_CODE;
import static com.auth.module.security.contract.redis.SecurityRedisKey.SMS_CODE;

/**
 * 登录凭证查询维度
 *
 * @author Bunny
 */
public enum CredentialDimension {

	/**
	 * 用户名
	 */
	USERNAME,

	/**
	 * 邮箱
	 */
	EMAIL,

	/**
	 * 手机号
	 */
	PHONE,;

	public static String resolveKey(CredentialDimension dimension, String target) {
		if (CharSequenceUtil.isBlank(target)) {
			return null;
		}
		return switch (dimension) {
			case EMAIL -> EMAIL_CODE.key(target);
			case PHONE -> SMS_CODE.key(target);
			default -> null;
		};
	}

}

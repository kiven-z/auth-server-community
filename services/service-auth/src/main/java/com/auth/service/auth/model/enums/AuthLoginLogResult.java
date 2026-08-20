package com.auth.service.auth.model.enums;

import lombok.Getter;

/**
 * 登录审计结果码，与表 log_login.login_result 一致
 *
 * @author Bunny
 */
@Getter
public enum AuthLoginLogResult {

	/**
	 * 成功
	 */
	SUCCESS(0),

	/**
	 * 密码错误或通用凭证失败（含刷新失败等非细分场景，配合 failureReason 区分）
	 */
	PASSWORD_OR_CREDENTIAL_ERROR(1),

	/**
	 * 账号锁定
	 */
	ACCOUNT_LOCKED(2),

	/**
	 * 验证码错误
	 */
	CAPTCHA_ERROR(3),

	/**
	 * 账号禁用
	 */
	ACCOUNT_DISABLED(4);

	private final int code;

	AuthLoginLogResult(int code) {
		this.code = code;
	}

}

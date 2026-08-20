package com.auth.common.core.desensitize;

/**
 * 脱敏类型，可按项目需要扩展
 *
 * @author Bunny
 */
public enum DesensitizedType {

	/**
	 * 中文姓名
	 */
	CHINESE_NAME,

	/**
	 * 身份证号
	 */
	ID_CARD,

	/**
	 * 手机号
	 */
	MOBILE_PHONE,

	/**
	 * 电子邮箱
	 */
	EMAIL,

	/**
	 * 银行卡号
	 */
	BANK_CARD,

	/**
	 * IP 地址（自动识别 IPv4 / IPv6，均使用 Hutool 脱敏规则）
	 */
	IP_ADDRESS,

	/**
	 * 中国大陆车牌（普通 / 新能源），使用 Hutool
	 * {@link cn.hutool.core.util.DesensitizedUtil#carLicense(String)}
	 */
	CAR_LICENSE,

	/**
	 * 地址（保留前缀、掩码末尾敏感长度），使用 Hutool
	 * {@link cn.hutool.core.util.DesensitizedUtil#address(String, int)}， 敏感长度由注解
	 * addressSensitiveSize 配置，默认 8 与 Hutool desensitized(..., ADDRESS) 一致
	 */
	ADDRESS,

	/**
	 * 密码：接口与日志统一显示为固定长度掩码，不随原文长度变化
	 */
	PASSWORD,

	/**
	 * 统一社会信用代码，使用 Hutool {@link cn.hutool.core.util.DesensitizedUtil#creditCode(String)}
	 */
	CREDIT_CODE,

	/**
	 * 金额展示字符串：保留整数部分（含千分位逗号），仅将小数点后的数字替换为 *
	 */
	MONEY_DECIMAL,

	/**
	 * 自定义保留前后缀规则，配合注解上的 prefixLen、suffixLen、symbol
	 */
	CUSTOM

}

package com.auth.common.web.validation;

import lombok.experimental.UtilityClass;

/**
 * 编译时正则表达式共享 用于在编译时检查正则表达式是否正确
 *
 * @author Bunny
 */
@UtilityClass
public class ValidationPatterns {

	/**
	 * 只能是字母，下划线，中划线；必须包含至少一个字母
	 * <p>
	 * 用于代码中的邮箱模板键，不允许单独使用数字
	 * </p>
	 */
	public static final String LETTER_UNDERSCORE_HYPHEN_REQUIRE_LETTER = "^(?=.*[a-zA-Z])[a-zA-Z_-]+$";

	/**
	 * 只能是大写字母，数字，下划线（整个字符串）
	 */
	public static final String UPPER_ALNUM_UNDERSCORE = "^[A-Z0-9_]+$";

	/**
	 * 8-18 位，且数字、字母、符号中至少包含两类
	 */
	public static final String TWO_OF_THREE_CHAR_CLASSES_8_18 = "^(?:(?=.*[0-9])(?=.*[a-zA-Z])|(?=.*[0-9])(?=.*[^a-zA-Z0-9])|(?=.*[a-zA-Z])(?=.*[^a-zA-Z0-9])).{8,18}$";

}

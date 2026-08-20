package com.auth.service.system.admin.excel.user;

import com.auth.common.web.validation.ValidationPatterns;
import com.auth.module.file.importer.model.ImportErrorCode;
import com.auth.module.file.importer.model.ImportRowError;
import com.auth.module.file.importer.rule.RowRule;
import com.auth.module.file.importer.rule.RowRules;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 用户导入 Validate 阶段：唯一性、长度等规则。
 *
 * @author Bunny
 */
@UtilityClass
public class UserRowValidator {

	/**
	 * 导入校验字段名：username
	 */
	private static final String FIELD_USERNAME = "username";

	/**
	 * 导入校验字段名：email
	 */
	private static final String FIELD_EMAIL = "email";

	/**
	 * 导入校验字段名：phone
	 */
	private static final String FIELD_PHONE = "phone";

	/**
	 * 导入校验字段名：employeeNo
	 */
	private static final String FIELD_EMPLOYEE_NO = "employeeNo";

	/**
	 * 导入校验字段名：initialPassword
	 */
	private static final String FIELD_INITIAL_PASSWORD = "initialPassword";

	/**
	 * 初始密码最小长度
	 */
	private static final int INITIAL_PASSWORD_MIN_LENGTH = 8;

	/**
	 * 初始密码最大长度
	 */
	private static final int INITIAL_PASSWORD_MAX_LENGTH = 18;

	private static final Pattern INITIAL_PASSWORD_COMPLEXITY_PATTERN = Pattern
		.compile(ValidationPatterns.TWO_OF_THREE_CHAR_CLASSES_8_18);

	/**
	 * 用户导入校验规则
	 */
	private static final List<RowRule<UserParsedRow, UserSheetImporter.Context>> RULES = List.of(
			// 用户名唯一性校验
			RowRules.unique(FIELD_USERNAME, UserParsedRow::username,
					UserSheetImporter.Context::duplicateUsernamesInFile, UserSheetImporter.Context::existingUsernames),
			// 邮箱唯一性校验
			RowRules.unique(FIELD_EMAIL, UserParsedRow::email, UserSheetImporter.Context::duplicateEmailsInFile,
					UserSheetImporter.Context::existingEmails),
			// 手机号唯一性校验
			RowRules.unique(FIELD_PHONE, UserParsedRow::phone, UserSheetImporter.Context::duplicatePhonesInFile,
					UserSheetImporter.Context::existingPhones),
			// 员工编号唯一性校验
			RowRules.unique(FIELD_EMPLOYEE_NO, UserParsedRow::employeeNo,
					UserSheetImporter.Context::duplicateEmployeeNosInFile,
					UserSheetImporter.Context::existingEmployeeNos),
			// 初始密码长度范围校验
			RowRules.lengthBetween(FIELD_INITIAL_PASSWORD, UserParsedRow::initialPassword, INITIAL_PASSWORD_MIN_LENGTH,
					INITIAL_PASSWORD_MAX_LENGTH),
			// 初始密码复杂度校验
			RowRules.assertThat(FIELD_INITIAL_PASSWORD, UserParsedRow::initialPassword, row -> {
				String password = row.initialPassword();
				return password != null && INITIAL_PASSWORD_COMPLEXITY_PATTERN.matcher(password).matches();
			}, ImportErrorCode.INVALID_FORMAT));

	/**
	 * 校验已解析行
	 * @param parsed 已解析行
	 * @param rowNum 行号
	 * @param context 导入上下文
	 * @return 错误列表
	 */
	public static List<ImportRowError> validate(UserParsedRow parsed, int rowNum, UserSheetImporter.Context context) {
		return RowRules.applyFailFast(RULES, parsed, rowNum, context);
	}

}

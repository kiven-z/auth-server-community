package com.auth.service.system.admin.excel.user;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.data.model.enums.UserStatus;
import com.auth.module.file.importer.model.ImportErrors;
import com.auth.module.file.importer.model.RowOutcome;
import com.auth.module.file.importer.parse.ImportTextParsers;
import com.auth.module.file.importer.parse.ImportValueParser;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * 用户导入 Extract 阶段：trim、必填与类型解析。
 *
 * @author Bunny
 */
@UtilityClass
public class UserRowExtractor {

	/**
	 * 用户状态解析器
	 */
	static final ImportValueParser<Integer> USER_STATUS_PARSER = (raw, rowNum, field) -> {
		if (CharSequenceUtil.isBlank(raw)) {
			return RowOutcome.err(ImportErrors.statusRequired(rowNum));
		}
		String trimmed = raw.trim();
		for (UserStatus status : UserStatus.values()) {
			if (status.getDesc().equals(trimmed)) {
				return RowOutcome.ok(status.getCode());
			}
		}
		return RowOutcome.err(ImportErrors.invalidValue(rowNum, field, trimmed));
	};

	private static final DateTimeFormatter BIRTHDAY_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

	/**
	 * 生日解析器（blank 时返回 null）
	 */
	static final ImportValueParser<LocalDate> BIRTHDAY_PARSER = (raw, rowNum, field) -> {
		if (CharSequenceUtil.isBlank(raw)) {
			return RowOutcome.ok(null);
		}
		try {
			return RowOutcome.ok(LocalDate.parse(raw.trim(), BIRTHDAY_FORMAT));
		}
		catch (DateTimeParseException ex) {
			return RowOutcome.err(ImportErrors.birthdayInvalid(rowNum, raw));
		}
	};

	private static final Map<String, Integer> GENDER_BY_LABEL = Map.of("男", 1, "女", 2, "未知", 0);

	/**
	 * 性别解析器（blank 时返回 null）
	 */
	static final ImportValueParser<Integer> GENDER_PARSER = (raw, rowNum, field) -> {
		if (CharSequenceUtil.isBlank(raw)) {
			return RowOutcome.ok(null);
		}
		Integer gender = GENDER_BY_LABEL.get(raw.trim());
		if (gender == null) {
			return RowOutcome.err(ImportErrors.invalidValue(rowNum, field, raw));
		}
		return RowOutcome.ok(gender);
	};

	/**
	 * 导入校验字段名：username
	 */
	private static final String FIELD_USERNAME = "username";

	/**
	 * 导入校验字段名：nickname
	 */
	private static final String FIELD_NICKNAME = "nickname";

	/**
	 * 导入校验字段名：email
	 */
	private static final String FIELD_EMAIL = "email";

	/**
	 * 导入校验字段名：phone
	 */
	private static final String FIELD_PHONE = "phone";

	/**
	 * 导入校验字段名：initialPassword
	 */
	private static final String FIELD_INITIAL_PASSWORD = "initialPassword";

	/**
	 * 导入校验字段名：status
	 */
	private static final String FIELD_STATUS = "status";

	/**
	 * 导入校验字段名：gender
	 */
	private static final String FIELD_GENDER = "gender";

	/**
	 * 导入校验字段名：birthday
	 */
	private static final String FIELD_BIRTHDAY = "birthday";

	/**
	 * 从 Excel 行提取并解析字段
	 * @param row Excel 行
	 * @param rowNum 行号
	 * @return 解析结果
	 */
	public static RowOutcome<UserParsedRow> extract(SysUserImportRow row, int rowNum) {
		// 用户名
		RowOutcome<String> username = ImportTextParsers.require(row.getUsername(), rowNum, FIELD_USERNAME);
		if (!username.ok()) {
			return RowOutcome.err(username.errors());
		}
		// 昵称
		RowOutcome<String> nickname = ImportTextParsers.require(row.getNickname(), rowNum, FIELD_NICKNAME);
		if (!nickname.ok()) {
			return RowOutcome.err(nickname.errors());
		}
		// 邮箱
		RowOutcome<String> email = ImportTextParsers.require(row.getEmail(), rowNum, FIELD_EMAIL);
		if (!email.ok()) {
			return RowOutcome.err(email.errors());
		}
		// 手机号
		RowOutcome<String> phone = ImportTextParsers.require(row.getPhone(), rowNum, FIELD_PHONE);
		if (!phone.ok()) {
			return RowOutcome.err(phone.errors());
		}
		// 初始密码
		RowOutcome<String> initialPassword = ImportTextParsers.require(row.getInitialPassword(), rowNum,
				FIELD_INITIAL_PASSWORD);
		if (!initialPassword.ok()) {
			return RowOutcome.err(initialPassword.errors());
		}

		// 状态
		RowOutcome<Integer> status = USER_STATUS_PARSER.parse(row.getStatusLabel(), rowNum, FIELD_STATUS);
		if (!status.ok()) {
			return RowOutcome.err(status.errors());
		}
		// 性别
		RowOutcome<Integer> gender = GENDER_PARSER.parse(row.getGenderLabel(), rowNum, FIELD_GENDER);
		if (!gender.ok()) {
			return RowOutcome.err(gender.errors());
		}
		// 生日
		RowOutcome<LocalDate> birthday = BIRTHDAY_PARSER.parse(row.getBirthday(), rowNum, FIELD_BIRTHDAY);
		if (!birthday.ok()) {
			return RowOutcome.err(birthday.errors());
		}

		return RowOutcome.ok(UserParsedRow.builder()
			.username(username.value())
			.nickname(nickname.value())
			.email(email.value())
			.phone(phone.value())
			.initialPassword(initialPassword.value())
			.employeeNo(ImportTextParsers.optional(row.getEmployeeNo()))
			.status(status.value())
			.gender(gender.value())
			.birthday(birthday.value())
			.remark(row.getRemark())
			.build());
	}

}

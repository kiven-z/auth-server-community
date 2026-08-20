package com.auth.service.system.admin.excel.user;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

import java.time.LocalDate;

/**
 * 用户导入行解析结果（Extract 阶段产物）。
 *
 * @author Bunny
 */
@Value
@Builder
@Accessors(fluent = true)
public class UserParsedRow {

	String username;

	String nickname;

	String email;

	String phone;

	String initialPassword;

	String employeeNo;

	Integer status;

	Integer gender;

	LocalDate birthday;

	String remark;

}

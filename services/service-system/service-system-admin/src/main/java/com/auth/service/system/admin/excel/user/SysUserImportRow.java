package com.auth.service.system.admin.excel.user;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户 Excel 导入行（仅包含批量创建所需字段）
 *
 * @author Bunny
 */
@Getter
@Setter
public class SysUserImportRow {

	@ExcelProperty(value = "用户名", index = 0)
	private String username;

	@ExcelProperty(value = "昵称", index = 1)
	private String nickname;

	@ExcelProperty(value = "邮箱", index = 2)
	private String email;

	@ExcelProperty(value = "手机号", index = 3)
	private String phone;

	@ExcelProperty(value = "工号", index = 4)
	private String employeeNo;

	@ExcelProperty(value = "状态", index = 5)
	private String statusLabel;

	@ExcelProperty(value = "初始密码", index = 6)
	private String initialPassword;

	@ExcelProperty(value = "性别", index = 7)
	private String genderLabel;

	@ExcelProperty(value = "出生日期", index = 8)
	private String birthday;

	@ExcelProperty(value = "备注", index = 9)
	private String remark;

}

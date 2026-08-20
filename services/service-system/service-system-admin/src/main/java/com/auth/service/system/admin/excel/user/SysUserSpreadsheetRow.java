package com.auth.service.system.admin.excel.user;

import com.alibaba.excel.annotation.ExcelProperty;
import com.auth.module.file.spreadsheet.LongIdExcelConverter;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户 Excel 表格行
 *
 * @author Bunny
 */
@Getter
@Setter
public class SysUserSpreadsheetRow {

	@ExcelProperty(value = "用户名", index = 0)
	private String username;

	@ExcelProperty(value = "昵称", index = 1)
	private String nickname;

	@ExcelProperty(value = "手机号", index = 2)
	private String phone;

	@ExcelProperty(value = "邮箱", index = 3)
	private String email;

	@ExcelProperty(value = "工号", index = 4)
	private String employeeNo;

	@ExcelProperty(value = "状态", index = 5)
	private String statusLabel;

	@ExcelProperty(value = "创建时间", index = 6)
	private String createdAtText;

	@ExcelProperty(value = "更新时间", index = 7)
	private String updatedAtText;

	@ExcelProperty(value = "主键ID", index = 8, converter = LongIdExcelConverter.class)
	private Long id;

}

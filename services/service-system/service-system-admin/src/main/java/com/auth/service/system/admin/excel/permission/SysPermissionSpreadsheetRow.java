package com.auth.service.system.admin.excel.permission;

import com.alibaba.excel.annotation.ExcelProperty;
import com.auth.module.file.spreadsheet.LongIdExcelConverter;
import lombok.Getter;
import lombok.Setter;

/**
 * 权限 Excel 表格行
 *
 * @author Bunny
 */
@Getter
@Setter
public class SysPermissionSpreadsheetRow {

	@ExcelProperty(value = "权限编码", index = 0)
	private String permissionCode;

	@ExcelProperty(value = "权限名称", index = 1)
	private String permissionName;

	@ExcelProperty(value = "状态", index = 2)
	private String statusLabel;

	@ExcelProperty(value = "显示顺序", index = 3)
	private Integer orderNum;

	@ExcelProperty(value = "备注", index = 4)
	private String remark;

	@ExcelProperty(value = "创建时间", index = 5)
	private String createdAtText;

	@ExcelProperty(value = "更新时间", index = 6)
	private String updatedAtText;

	@ExcelProperty(value = "主键ID", index = 7, converter = LongIdExcelConverter.class)
	private Long id;

}

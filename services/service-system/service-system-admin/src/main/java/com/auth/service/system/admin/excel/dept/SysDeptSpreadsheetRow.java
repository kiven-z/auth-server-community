package com.auth.service.system.admin.excel.dept;

import com.alibaba.excel.annotation.ExcelProperty;
import com.auth.module.file.spreadsheet.LongIdExcelConverter;
import lombok.Getter;
import lombok.Setter;

/**
 * 部门 Excel 表格行
 *
 * @author Bunny
 */
@Getter
@Setter
public class SysDeptSpreadsheetRow {

	@ExcelProperty(value = "父部门编码", index = 0)
	private String parentDeptCode;

	@ExcelProperty(value = "父部门名称", index = 1)
	private String parentDeptName;

	@ExcelProperty(value = "父部门ID", index = 2, converter = LongIdExcelConverter.class)
	private Long parentId;

	@ExcelProperty(value = "部门编码", index = 3)
	private String deptCode;

	@ExcelProperty(value = "部门名称", index = 4)
	private String deptName;

	@ExcelProperty(value = "部门路径", index = 5)
	private String deptPath;

	@ExcelProperty(value = "状态", index = 6)
	private String statusLabel;

	@ExcelProperty(value = "计算有效", index = 7)
	private String effectiveLabel;

	@ExcelProperty(value = "显示顺序", index = 8)
	private Integer orderNum;

	@ExcelProperty(value = "备注", index = 9)
	private String remark;

	@ExcelProperty(value = "创建时间", index = 10)
	private String createdAtText;

	@ExcelProperty(value = "更新时间", index = 11)
	private String updatedAtText;

	@ExcelProperty(value = "主键ID", index = 12, converter = LongIdExcelConverter.class)
	private Long id;

}

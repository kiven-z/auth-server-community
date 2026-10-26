package com.auth.service.system.admin.excel.post;

import com.alibaba.excel.annotation.ExcelProperty;
import com.auth.module.file.export.converter.LongIdExcelConverter;
import lombok.Getter;
import lombok.Setter;

/**
 * 岗位 Excel 表格行
 *
 * @author Bunny
 */
@Getter
@Setter
public class SysPostSpreadsheetRow {

	@ExcelProperty(value = "部门编码", index = 0)
	private String deptCode;

	@ExcelProperty(value = "部门名称", index = 1)
	private String deptName;

	@ExcelProperty(value = "岗位编码", index = 2)
	private String postCode;

	@ExcelProperty(value = "岗位名称", index = 3)
	private String postName;

	@ExcelProperty(value = "显示顺序", index = 4)
	private Integer orderNum;

	@ExcelProperty(value = "备注", index = 5)
	private String remark;

	@ExcelProperty(value = "创建时间", index = 6)
	private String createdAtText;

	@ExcelProperty(value = "更新时间", index = 7)
	private String updatedAtText;

	@ExcelProperty(value = "主键ID", index = 8, converter = LongIdExcelConverter.class)
	private Long id;

}

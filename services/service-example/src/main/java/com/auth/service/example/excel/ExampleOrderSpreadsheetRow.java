package com.auth.service.example.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.auth.module.file.spreadsheet.LongIdExcelConverter;
import lombok.Getter;
import lombok.Setter;

/**
 * 演示单 Excel 导出行
 *
 * @author Bunny
 */
@Getter
@Setter
public class ExampleOrderSpreadsheetRow {

	@ExcelProperty(value = "标题", index = 0)
	private String title;

	@ExcelProperty(value = "所属部门ID", index = 1, converter = LongIdExcelConverter.class)
	private Long deptId;

	@ExcelProperty(value = "主键ID", index = 2, converter = LongIdExcelConverter.class)
	private Long id;

}

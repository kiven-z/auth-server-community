package com.auth.service.system.admin.excel.dept;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 部门 Excel 导入行（仅包含写入所需字段）
 *
 * @author Bunny
 */
@Getter
@Setter
public class SysDeptImportRow {

	@ExcelProperty(value = "父部门编码", index = 0)
	private String parentDeptCode;

	@ExcelProperty(value = "部门编码", index = 1)
	private String deptCode;

	@ExcelProperty(value = "部门名称", index = 2)
	private String deptName;

	@ExcelProperty(value = "状态", index = 3)
	private String statusLabel;

	@ExcelProperty(value = "显示顺序", index = 4)
	private Integer orderNum;

	@ExcelProperty(value = "备注", index = 5)
	private String remark;

}

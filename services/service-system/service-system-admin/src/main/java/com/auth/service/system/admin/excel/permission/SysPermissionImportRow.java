package com.auth.service.system.admin.excel.permission;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 权限 Excel 导入行（仅包含写入所需字段）
 *
 * @author Bunny
 */
@Getter
@Setter
public class SysPermissionImportRow {

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

}

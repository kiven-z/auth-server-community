package com.auth.service.system.admin.excel.permission;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * 权限导入行解析结果（Extract 阶段产物）
 *
 * @author Bunny
 */
@Value
@Builder
@Accessors(fluent = true)
public class PermissionParsedRow {

	/**
	 * 权限编码
	 */
	String permissionCode;

	/**
	 * 权限名称
	 */
	String permissionName;

	/**
	 * 状态标签
	 */
	String statusLabel;

	/**
	 * 排序号
	 */
	Integer orderNum;

	/**
	 * 备注
	 */
	String remark;

}

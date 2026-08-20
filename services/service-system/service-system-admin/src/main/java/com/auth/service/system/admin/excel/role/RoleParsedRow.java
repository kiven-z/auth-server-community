package com.auth.service.system.admin.excel.role;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * 角色导入行解析结果（Extract 阶段产物）
 *
 * @author Bunny
 */
@Value
@Builder
@Accessors(fluent = true)
public class RoleParsedRow {

	/**
	 * 角色编码
	 */
	String roleCode;

	/**
	 * 角色名称
	 */
	String roleName;

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

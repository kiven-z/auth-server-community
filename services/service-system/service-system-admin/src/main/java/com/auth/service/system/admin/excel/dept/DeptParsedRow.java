package com.auth.service.system.admin.excel.dept;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * 部门导入行解析结果（Extract 阶段产物）
 *
 * @author Bunny
 */
@Value
@Builder
@Accessors(fluent = true)
public class DeptParsedRow {

	/**
	 * 父部门编码
	 */
	String parentDeptCode;

	/**
	 * 部门编码
	 */
	String deptCode;

	/**
	 * 部门名称
	 */
	String deptName;

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

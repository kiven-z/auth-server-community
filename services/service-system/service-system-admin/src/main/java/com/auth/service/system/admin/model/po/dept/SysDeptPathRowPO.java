package com.auth.service.system.admin.model.po.dept;

import lombok.Getter;
import lombok.Setter;

/**
 * 部门全路径查询投影
 *
 * @author Bunny
 */
@Getter
@Setter
public class SysDeptPathRowPO {

	/**
	 * 部门主键
	 */
	private Long id;

	/**
	 * 从根到自身的部门名称路径
	 */
	private String deptPath;

}

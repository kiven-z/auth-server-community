package com.auth.service.system.admin.model.po.dept;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 部门闭包新增节点参数
 *
 * @author Bunny
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeptClosureNodePO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 部门 ID
	 */
	private Long deptId;

	/**
	 * 父部门 ID，顶级传 0
	 */
	private Long parentId;

}

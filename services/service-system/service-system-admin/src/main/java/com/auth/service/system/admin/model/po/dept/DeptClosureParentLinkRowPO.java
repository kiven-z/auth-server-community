package com.auth.service.system.admin.model.po.dept;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * 闭包父子直连异常行（Mapper 结果）
 *
 * @author Bunny
 */
@Getter
@Setter
@ToString
public class DeptClosureParentLinkRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private Long id;

	private String deptName;

	private Long parentId;

}

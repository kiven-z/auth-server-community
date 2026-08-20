package com.auth.service.system.admin.model.po.user;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户主部门投影
 *
 * @author Bunny
 */
@Getter
@Setter
public class UserPrimaryDeptPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 部门 ID
	 */
	private Long deptId;

	/**
	 * 部门名称
	 */
	private String deptName;

}

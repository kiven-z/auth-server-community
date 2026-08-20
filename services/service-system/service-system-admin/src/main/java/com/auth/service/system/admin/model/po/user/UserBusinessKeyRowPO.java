package com.auth.service.system.admin.model.po.user;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户业务键查询行（username / email / phone / employeeNo）
 *
 * @author Bunny
 */
@Getter
@Setter
@ToString
public class UserBusinessKeyRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 用户主键
	 */
	private Long id;

	/**
	 * 用户名
	 */
	private String username;

	/**
	 * 邮箱
	 */
	private String email;

	/**
	 * 手机号
	 */
	private String phone;

	/**
	 * 工号
	 */
	private String employeeNo;

}

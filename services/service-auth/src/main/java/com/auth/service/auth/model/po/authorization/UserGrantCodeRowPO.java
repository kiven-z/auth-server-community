package com.auth.service.auth.model.po.authorization;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户授权码行投影（角色码或权限码）
 *
 * @author Bunny
 */
@Getter
@Setter
public class UserGrantCodeRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 用户 ID
	 */
	private Long userId;

	/**
	 * 角色码或权限码
	 */
	private String code;

}

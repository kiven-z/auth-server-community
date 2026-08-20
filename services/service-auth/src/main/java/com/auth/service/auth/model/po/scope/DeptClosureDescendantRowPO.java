package com.auth.service.auth.model.po.scope;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 部门闭包后代行投影（锚点 → 后代）
 *
 * @author Bunny
 */
@Getter
@Setter
public class DeptClosureDescendantRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 锚点部门 ID
	 */
	private Long ancestorId;

	/**
	 * 后代部门 ID
	 */
	private Long descendantId;

}

package com.auth.service.system.admin.model.po.dept;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * 闭包健康度统计
 *
 * @author Bunny
 */
@Getter
@Setter
@ToString
public class DeptClosureHealthStatsPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private Long onlySelf;

	private Long hasAncestors;

	private Long zeroClosure;

	private Long total;

}

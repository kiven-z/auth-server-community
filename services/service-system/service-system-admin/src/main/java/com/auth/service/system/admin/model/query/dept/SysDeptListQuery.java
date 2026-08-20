package com.auth.service.system.admin.model.query.dept;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 部门扁平列表查询
 *
 * @author Bunny
 */
@Schema(name = "SysDeptListQuery", title = "部门扁平列表查询")
@Getter
@Setter
public class SysDeptListQuery implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "关键词")
	private String keyword;

	@Schema(title = "状态（true=正常，false=禁用）")
	private Boolean status;

}

package com.auth.service.system.admin.model.vo.reference;

import com.auth.common.core.annotation.JsonStringFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * 部门关联回显基础字段
 *
 * @author Bunny
 */
@Schema(name = "DeptReferenceVO", title = "部门关联回显")
@Getter
@Setter
@ToString
public class DeptReferenceVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonStringFormat
	@Schema(title = "部门 ID")
	private Long id;

	@Schema(title = "部门名称")
	private String deptName;

	@Schema(title = "部门编码")
	private String deptCode;

	@Schema(title = "启用状态（true=正常，false=禁用）")
	private Boolean status;

}

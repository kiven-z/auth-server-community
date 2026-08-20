package com.auth.service.system.admin.model.vo.reference;

import com.auth.common.core.annotation.JsonStringFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * 岗位关联回显基础字段
 *
 * @author Bunny
 */
@Schema(name = "PostReferenceVO", title = "岗位关联回显")
@Getter
@Setter
@ToString
public class PostReferenceVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonStringFormat
	@Schema(title = "岗位 ID")
	private Long id;

	@Schema(title = "岗位编码")
	private String postCode;

	@Schema(title = "岗位名称")
	private String postName;

	@Schema(title = "启用状态（true=启用，false=停用）")
	private Boolean status;

}

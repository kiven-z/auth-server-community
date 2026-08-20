package com.auth.service.system.admin.model.po.reference;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 岗位关联查询投影
 *
 * @author Bunny
 */
@Schema(name = "PostReferencePO", title = "岗位关联查询 PO")
@Getter
@Setter
public class PostReferencePO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "岗位 ID")
	private Long id;

	@Schema(title = "岗位编码")
	private String postCode;

	@Schema(title = "岗位名称")
	private String postName;

	@Schema(title = "启用状态（true=启用，false=停用）")
	private Boolean status;

}

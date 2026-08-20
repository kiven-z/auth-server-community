package com.auth.service.system.admin.model.po.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户档案-岗位关联查询行
 *
 * @author Bunny
 */
@Getter
@Setter
public class UserPostProfilePO implements Serializable {

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

	@Schema(title = "是否主岗位")
	private Boolean isPrimary;

}

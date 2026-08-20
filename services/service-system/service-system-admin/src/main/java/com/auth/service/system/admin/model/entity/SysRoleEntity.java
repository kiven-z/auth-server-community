package com.auth.service.system.admin.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统角色主表（物理删除）
 *
 * @author Bunny
 */
@TableName("sys_role")
@Schema(name = "SysRoleEntity", title = "系统角色")
@Getter
@Setter
public class SysRoleEntity extends BaseEntity {

	@Schema(title = "角色编码")
	private String roleCode;

	@Schema(title = "角色名称")
	private String roleName;

	@Schema(title = "启用状态（true=启用，false=停用）")
	private Boolean status;

	@Schema(title = "显示顺序")
	private Integer orderNum;

}

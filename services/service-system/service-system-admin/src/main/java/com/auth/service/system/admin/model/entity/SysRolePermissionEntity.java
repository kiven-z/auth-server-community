package com.auth.service.system.admin.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * 角色与权限码关联
 *
 * @author Bunny
 */
@TableName("sys_role_permission")
@Schema(name = "SysRolePermissionEntity", title = "角色权限关联")
@Getter
@Setter
public class SysRolePermissionEntity extends BaseEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	@TableId(type = IdType.AUTO)
	private Long id;

	@Schema(title = "角色 ID")
	private Long roleId;

	@Schema(title = "权限 ID")
	private Long permissionId;

	@Schema(title = "授权人 userId")
	private Long grantorId;

}

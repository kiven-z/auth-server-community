package com.auth.service.system.admin.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 角色数据范围配置（部门维）
 *
 * @author Bunny
 */
@TableName("role_scope")
@Schema(name = "RoleScopeEntity", title = "角色数据范围")
@Getter
@Setter
public class RoleScopeEntity extends BaseEntity {

	@Schema(title = "角色 ID")
	private Long roleId;

	@Schema(title = "范围类型：ALL/SELF/DEPT/DEPT_AND_CHILD")
	private String scopeType;

	@Schema(title = "部门 ID 列表 JSON")
	private String scopeDeptIds;

}

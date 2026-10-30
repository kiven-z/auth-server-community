package com.auth.service.system.admin.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户数据范围配置
 * <p>
 * 按数据维；有行则覆盖同维 role_scope
 * </p>
 *
 * @author Bunny
 */
@TableName("user_scope")
@Schema(name = "UserScopeEntity", title = "用户数据范围")
@Getter
@Setter
public class UserScopeEntity extends BaseEntity {

	@Schema(title = "用户 ID")
	private Long userId;

	@Schema(title = "数据维度")
	private String dimension;

	@Schema(title = "范围类型", description = "ALL/SELF/IDS/IDS_AND_CHILD")
	private String scopeType;

	@Schema(title = "节点 ID 列表 JSON")
	private String scopeIds;

}

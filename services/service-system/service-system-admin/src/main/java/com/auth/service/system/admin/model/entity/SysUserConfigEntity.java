package com.auth.service.system.admin.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户级配置项（物理删除；删用户级联删除）
 *
 * @author Bunny
 */
@TableName("sys_user_config")
@Schema(name = "SysUserConfigEntity", title = "用户配置项")
@Getter
@Setter
public class SysUserConfigEntity extends BaseEntity {

	@Schema(title = "用户 ID")
	private Long userId;

	@Schema(title = "配置键")
	private String configKey;

	@Schema(title = "配置值 JSON 原文")
	private String configValue;

}

package com.auth.service.system.admin.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户岗位关联
 *
 * @author Bunny
 */
@TableName("user_post")
@Getter
@Setter
public class UserPostEntity extends BaseEntity {

	@Schema(title = "用户 ID")
	private Long userId;

	@Schema(title = "岗位 ID")
	private Long postId;

	@Schema(title = "是否主岗位")
	private Boolean isPrimary;

}

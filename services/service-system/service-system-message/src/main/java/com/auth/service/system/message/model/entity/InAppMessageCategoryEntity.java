package com.auth.service.system.message.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 站内信业务分类字典
 *
 * @author Bunny
 */
@TableName("in_app_message_category")
@Schema(name = "InAppMessageCategoryEntity", title = "站内信业务分类")
@Getter
@Setter
@Accessors(chain = true)
public class InAppMessageCategoryEntity extends BaseEntity {

	@Schema(title = "父分类 ID", description = "0=大类；非 0=小类，指向大类 id")
	private Long parentId;

	@Schema(title = "分类码")
	private String code;

	@Schema(title = "展示名")
	private String name;

	@Schema(title = "同级排序")
	private Integer sortOrder;

	@Schema(title = "启用状态")
	private Boolean status;

}

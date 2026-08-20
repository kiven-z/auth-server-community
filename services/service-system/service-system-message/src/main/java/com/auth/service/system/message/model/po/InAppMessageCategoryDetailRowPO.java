package com.auth.service.system.message.model.po;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 站内信业务分类详情行（左联父级展示字段）
 *
 * @author Bunny
 */
@Schema(name = "InAppMessageCategoryDetailRowPO", title = "站内信业务分类详情行")
@Getter
@Setter
@ToString
public class InAppMessageCategoryDetailRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "主键")
	private Long id;

	@Schema(title = "父分类 ID")
	private Long parentId;

	@Schema(title = "父分类码")
	private String parentCode;

	@Schema(title = "父分类名")
	private String parentName;

	@Schema(title = "分类码")
	private String code;

	@Schema(title = "展示名")
	private String name;

	@Schema(title = "同级排序")
	private Integer sortOrder;

	@Schema(title = "启用状态")
	private Boolean status;

	@Schema(title = "备注")
	private String remark;

	@Schema(title = "创建时间")
	private Instant createdAt;

	@Schema(title = "更新时间")
	private Instant updatedAt;

	@Schema(title = "创建用户")
	private Long createdBy;

	@Schema(title = "更新用户")
	private Long updatedBy;

}

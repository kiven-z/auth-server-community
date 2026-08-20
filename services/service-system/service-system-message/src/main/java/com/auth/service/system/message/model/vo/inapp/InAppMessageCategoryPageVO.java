package com.auth.service.system.message.model.vo.inapp;

import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 站内信业务分类分页返回对象
 *
 * @author Bunny
 */
@Schema(name = "InAppMessageCategoryPageVO", title = "站内信业务分类列表返回对象")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class InAppMessageCategoryPageVO extends BaseResponse {

	@JsonStringFormat
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

}

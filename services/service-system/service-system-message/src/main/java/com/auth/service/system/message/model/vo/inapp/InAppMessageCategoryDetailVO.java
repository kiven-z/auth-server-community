package com.auth.service.system.message.model.vo.inapp;

import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 站内信业务分类详情返回对象
 *
 * @author Bunny
 */
@Schema(name = "InAppMessageCategoryDetailVO", title = "站内信业务分类详情")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class InAppMessageCategoryDetailVO extends BaseResponse {

	@JsonStringFormat
	@Schema(title = "父分类 ID", description = "0 表示大类")
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

}

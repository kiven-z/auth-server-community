package com.auth.service.system.message.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 站内信业务分类列表筛选
 *
 * @author Bunny
 */
@Schema(name = "InAppMessageCategoryQuery", title = "站内信业务分类查询")
@Getter
@Setter
public class InAppMessageCategoryQuery {

	@Schema(title = "分类码")
	private String code;

	@Schema(title = "展示名")
	private String name;

	@Schema(title = "父分类 ID")
	private Long parentId;

	@Schema(title = "根节点筛选", description = "true=仅大类（parent_id=0）；false=仅小类(parent_id>0)；空=全部")
	private Boolean rootOnly;

	@Schema(title = "启用状态")
	private Boolean status;

}

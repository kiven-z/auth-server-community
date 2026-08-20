package com.auth.service.system.message.model.query;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户侧站内信收件箱分页查询
 *
 * @author Bunny
 */
@Schema(name = "InAppInboxQuery", title = "站内信收件箱查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class InAppInboxQuery extends PageQueryRequest {

	@Schema(name = "majorCategoryId", title = "业务大类 ID（筛该大类下全部小类）")
	private Long majorCategoryId;

	@Schema(name = "categoryId", title = "业务小类 ID")
	private Long categoryId;

	@Schema(name = "isRead", title = "是否已读")
	private Boolean isRead;

	@Schema(name = "title", title = "标题关键字")
	private String title;

}

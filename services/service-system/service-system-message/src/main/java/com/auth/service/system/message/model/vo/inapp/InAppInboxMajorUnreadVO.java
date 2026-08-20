package com.auth.service.system.message.model.vo.inapp;

import com.auth.common.core.annotation.JsonStringFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * 单个大类的未读角标
 *
 * @author Bunny
 */
@Schema(name = "InAppInboxMajorUnreadVO", title = "站内信大类未读")
@Getter
@Builder
@ToString
public class InAppInboxMajorUnreadVO {

	@JsonStringFormat
	@Schema(title = "业务大类 ID")
	private Long majorCategoryId;

	@Schema(title = "业务大类名称")
	private String majorCategoryName;

	@Schema(title = "业务大类编码")
	private String majorCategoryCode;

	@Schema(title = "该大类未读条数")
	private long unreadCount;

}

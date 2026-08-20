package com.auth.service.system.message.model.vo.inapp;

import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * 用户侧站内信收件箱分页返回对象
 *
 * @author Bunny
 */
@Schema(name = "InAppInboxPageVO", title = "站内信收件箱分页返回对象")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class InAppInboxPageVO extends BaseResponse {

	@Schema(name = "sceneCode", title = "场景编码")
	private String sceneCode;

	@Schema(name = "title", title = "标题")
	private String title;

	@Schema(name = "contentType", title = "正文类型")
	private String contentType;

	@JsonStringFormat
	@Schema(name = "categoryId", title = "业务小类 ID")
	private Long categoryId;

	@Schema(name = "categoryName", title = "业务小类名称")
	private String categoryName;

	@Schema(name = "linkUrl", title = "跳转链接")
	private String linkUrl;

	@JsonStringFormat
	@Schema(name = "senderUserId", title = "发起人用户 ID")
	private Long senderUserId;

	@Schema(name = "isRead", title = "是否已读")
	private Boolean isRead;

	@Schema(name = "readTime", title = "已读时间")
	private Instant readTime;

}

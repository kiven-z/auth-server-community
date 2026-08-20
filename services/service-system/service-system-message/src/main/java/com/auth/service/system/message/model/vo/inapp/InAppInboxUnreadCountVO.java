package com.auth.service.system.message.model.vo.inapp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * 用户侧站内信未读角标
 *
 * @author Bunny
 */
@Schema(name = "InAppInboxUnreadCountVO", title = "站内信未读角标")
@Getter
@Builder
@ToString
public class InAppInboxUnreadCountVO {

	@Schema(title = "未读总数")
	private long totalUnreadCount;

	@Schema(title = "启用大类列表")
	private List<InAppInboxMajorUnreadVO> majors;

}

package com.auth.service.system.message.model.po;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * 按大类汇总的收件箱未读行
 *
 * @author Bunny
 */
@Schema(name = "InAppInboxMajorUnreadRowPO", title = "站内信大类未读行")
@Getter
@Setter
@ToString
public class InAppInboxMajorUnreadRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "业务大类 ID")
	private Long majorCategoryId;

	@Schema(title = "业务大类名称")
	private String majorCategoryName;

	@Schema(title = "业务大类编码")
	private String majorCategoryCode;

	@Schema(title = "该大类未读条数")
	private Long unreadCount;

}

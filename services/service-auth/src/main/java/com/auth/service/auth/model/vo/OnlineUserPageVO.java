package com.auth.service.auth.model.vo;

import com.auth.common.core.annotation.JsonStringFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * 在线用户分页行（一人一行）
 *
 * @author Bunny
 */
@Schema(name = "OnlineUserPageVO", title = "在线用户分页行")
@Getter
@Setter
@ToString
public class OnlineUserPageVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonStringFormat
	@Schema(title = "用户 ID")
	private Long userId;

	@Schema(title = "用户名")
	private String username;

	@Schema(title = "昵称")
	private String nickname;

	@Schema(title = "活跃会话数")
	private Integer activeSessionCount;

	@Schema(title = "最近登录时间（毫秒）")
	private Long lastLoginAt;

}

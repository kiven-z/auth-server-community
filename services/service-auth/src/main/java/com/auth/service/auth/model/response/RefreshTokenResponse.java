package com.auth.service.auth.model.response;

import com.auth.common.core.annotation.JsonStringFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * 刷新令牌响应
 *
 * @author Bunny
 */
@Getter
@Setter
public class RefreshTokenResponse implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonStringFormat
	@Schema(title = "用户ID")
	private Long id;

	@Schema(title = "用户名")
	private String username;

	@Schema(title = "角色")
	private List<String> roles;

	@Schema(title = "权限")
	private List<String> permissions;

	@Schema(title = "访问令牌")
	private String accessToken;

	@JsonIgnore
	@Schema(title = "刷新令牌（仅服务端 Cookie 下发使用，不对前端 JSON 暴露）")
	private String refreshToken;

	@Schema(title = "阅读过期天数")
	private Long readMeDay;

	@Schema(title = "过期时间")
	private Instant expires;

}

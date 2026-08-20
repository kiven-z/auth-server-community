package com.auth.service.system.authorization.feign.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * auth 用户活跃会话远程 DTO
 *
 * @author Bunny
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class UserSessionRemoteDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "用户 ID")
	private Long userId;

	@Schema(title = "会话 ID（jti）")
	private String sessionId;

	@Schema(title = "请求 IP")
	private String ipAddress;

	@Schema(title = "IP 归属地")
	private String ipRegion;

	@Schema(title = "设备类型")
	private String deviceType;

	@Schema(title = "浏览器类型")
	private String browserType;

	@Schema(title = "操作系统类型")
	private String osType;

	@Schema(title = "是否记住我")
	private Boolean rememberMe;

	@Schema(title = "Refresh Token 过期时间戳（毫秒）")
	private Long refreshTokenExpiresAt;

	@Schema(title = "登录时间戳（毫秒）")
	private Long loginAt;

}

package com.auth.service.auth.model.vo;

import com.auth.common.core.annotation.JsonStringFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 管理端用户活跃会话
 *
 * @author Bunny
 */
@Getter
@Setter
@ToString
public class UserSessionVO {

	@JsonStringFormat
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

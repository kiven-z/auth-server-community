package com.auth.service.system.admin.model.vo.me;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 个人中心用户活跃会话
 *
 * @author Bunny
 */
@Getter
@Setter
@ToString
public class MeUserSessionVO {

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

	@Schema(title = "是否为当前请求所在会话")
	private Boolean current;

}

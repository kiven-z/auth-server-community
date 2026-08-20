package com.auth.service.system.admin.model.vo.loglogin;

import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

/**
 * 登录日志详情
 *
 * @author Bunny
 */
@Schema(name = "LogLoginLogDetailVO", title = "登录日志详情")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class LogLoginLogDetailVO extends BaseResponse {

	@JsonStringFormat
	@Schema(title = "用户ID")
	private Long userId;

	@Schema(title = "登录账号")
	private String username;

	@Schema(title = "登录结果")
	private Integer loginResult;

	@Schema(title = "失败原因详情")
	private String failureReason;

	@Schema(title = "登录时间")
	private Instant loginTime;

	@Schema(title = "登录IP")
	private String loginIp;

	@Schema(title = "登录地区")
	private String loginRegion;

	@Schema(title = "用户代理")
	private String userAgent;

	@Schema(title = "设备类型")
	private String deviceType;

	@Schema(title = "操作系统")
	private String osType;

	@Schema(title = "浏览器类型")
	private String browserType;

	@Schema(title = "登录方式")
	private String loginType;

	@Schema(title = "会话ID")
	private String sessionId;

}

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
 * 登录日志分页行
 *
 * @author Bunny
 */
@Schema(name = "LogLoginLogPageVO", title = "登录日志分页行")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class LogLoginLogPageVO extends BaseResponse {

	@JsonStringFormat
	@Schema(title = "用户ID")
	private Long userId;

	@Schema(title = "登录账号")
	private String username;

	@Schema(title = "登录结果")
	private Integer loginResult;

	@Schema(title = "登录时间")
	private Instant loginTime;

	@Schema(title = "登录地区")
	private String loginRegion;

	@Schema(title = "登录方式")
	private String loginType;

}

package com.auth.service.system.admin.model.po.loglogin;

import com.auth.common.core.annotation.JsonStringFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 登录日志分页行
 *
 * @author Bunny
 */
@Schema(name = "LogLoginLogPageRowPO", title = "登录日志分页行")
@Getter
@Setter
@ToString
public class LogLoginLogPageRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonStringFormat
	@Schema(title = "主键")
	private Long id;

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

	@Schema(title = "创建时间")
	private Instant createdAt;

	@Schema(title = "更新时间")
	private Instant updatedAt;

	@JsonStringFormat
	@Schema(title = "创建用户")
	private Long createdBy;

	@JsonStringFormat
	@Schema(title = "更新用户")
	private Long updatedBy;

}

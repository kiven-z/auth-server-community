package com.auth.service.system.admin.model.vo.me;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

/**
 * 个人中心登录日志分页行（精简字段）
 *
 * @author Bunny
 */
@Getter
@Setter
@ToString
public class MeLoginLogPageVO {

	@Schema(title = "登录时间")
	private Instant loginTime;

	@Schema(title = "登录地区")
	private String loginRegion;

	@Schema(title = "登录结果")
	private Integer loginResult;

	@Schema(title = "登录方式")
	private String loginType;

}

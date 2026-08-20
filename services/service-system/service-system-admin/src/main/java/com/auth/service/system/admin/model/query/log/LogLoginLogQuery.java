package com.auth.service.system.admin.model.query.log;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * 登录日志分页查询
 *
 * @author Bunny
 */
@Schema(name = "LogLoginLogQuery", title = "登录日志查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class LogLoginLogQuery extends PageQueryRequest {

	@Schema(title = "用户ID")
	private Long userId;

	@Schema(title = "登录结果")
	private Integer loginResult;

	@Schema(title = "登录时间（开始）")
	private Instant loginTimeStart;

	@Schema(title = "登录时间（结束）")
	private Instant loginTimeEnd;

	@Schema(title = "登录方式")
	private String loginType;

}

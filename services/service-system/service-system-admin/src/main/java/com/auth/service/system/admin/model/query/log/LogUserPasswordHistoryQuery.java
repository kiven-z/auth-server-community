package com.auth.service.system.admin.model.query.log;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * 密码历史日志分页查询
 *
 * @author Bunny
 */
@Schema(name = "LogUserPasswordHistoryQuery", title = "密码历史日志查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class LogUserPasswordHistoryQuery extends PageQueryRequest {

	@Schema(title = "用户ID")
	private Long userId;

	@Schema(title = "修改时间（开始）")
	private Instant changeTimeStart;

	@Schema(title = "修改时间（结束）")
	private Instant changeTimeEnd;

	@Schema(title = "修改IP地址")
	private String changeIp;

}

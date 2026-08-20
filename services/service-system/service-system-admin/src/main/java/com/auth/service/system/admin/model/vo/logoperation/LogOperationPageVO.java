package com.auth.service.system.admin.model.vo.logoperation;

import com.auth.common.core.annotation.Desensitized;
import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.common.core.desensitize.DesensitizedType;
import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 操作日志分页行
 *
 * @author Bunny
 */
@Schema(name = "LogOperationPageVO", title = "操作日志分页行")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class LogOperationPageVO extends BaseResponse {

	@JsonStringFormat
	@Schema(title = "操作用户 ID")
	private Long userId;

	@Schema(title = "操作用户名")
	private String username;

	@Schema(title = "操作模块")
	private String module;

	@Schema(title = "操作类型")
	private String operationType;

	@Schema(title = "目标类型")
	private String targetType;

	@JsonStringFormat
	@Schema(title = "目标主键 ID")
	private Long targetId;

	@Schema(title = "HTTP 方法")
	private String requestMethod;

	@Schema(title = "HTTP 状态码")
	private Integer responseStatus;

	@Schema(title = "执行耗时毫秒")
	private Integer executionTimeMs;

	@Desensitized(DesensitizedType.IP_ADDRESS)
	@Schema(title = "请求 IP（脱敏）")
	private String ipAddress;

}

package com.auth.service.system.admin.model.vo.logauthorizationaudit;

import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 权限决策审计分页行
 *
 * @author Bunny
 */
@Schema(name = "LogAuthorizationAuditPageVO", title = "权限决策审计分页行")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class LogAuthorizationAuditPageVO extends BaseResponse {

	@Schema(title = "事件类型")
	private String eventType;

	@Schema(title = "决策原因")
	private String decisionReason;

	@Schema(title = "Controller 类名")
	private String className;

	@Schema(title = "方法名")
	private String methodName;

}

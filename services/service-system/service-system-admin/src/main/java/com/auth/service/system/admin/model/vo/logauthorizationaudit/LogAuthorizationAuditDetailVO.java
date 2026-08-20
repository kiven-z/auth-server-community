package com.auth.service.system.admin.model.vo.logauthorizationaudit;

import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 权限决策审计详情
 *
 * @author Bunny
 */
@Schema(name = "LogAuthorizationAuditDetailVO", title = "权限决策审计详情")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class LogAuthorizationAuditDetailVO extends BaseResponse {

	@Schema(title = "事件类型")
	private String eventType;

	@Schema(title = "会话 ID")
	private String sessionId;

	@Schema(title = "所需权限码")
	private String requiredPermission;

	@Schema(title = "HTTP 方法")
	private String requestMethod;

	@Schema(title = "请求 URI")
	private String requestUri;

	@Schema(title = "请求 IP")
	private String requestIp;

	@Schema(title = "决策原因")
	private String decisionReason;

	@Schema(title = "决策详情")
	private String decisionDetail;

	@Schema(title = "用户权限摘要")
	private String userPermissionsSummary;

	@Schema(title = "策略编码")
	private String policyCode;

	@Schema(title = "策略决策结果")
	private Boolean policyDecision;

	@Schema(title = "Controller 类名")
	private String className;

	@Schema(title = "方法名")
	private String methodName;

	@Schema(title = "方法参数摘要")
	private String methodParams;

	@Schema(title = "异常消息")
	private String exceptionMessage;

	@Schema(title = "备注")
	private String remark;

}

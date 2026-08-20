package com.auth.service.system.admin.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * 权限决策审计日志
 *
 * @author Bunny
 */
@TableName("log_authorization_audit")
@Getter
@Setter
@Accessors(chain = true)
public class LogAuthorizationAuditEntity extends BaseEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "事件类型：GRANTED / DENIED")
	private String eventType;

	@Schema(title = "会话 ID")
	private String sessionId;

	@Schema(title = "所需权限码（如 sys:user:add）")
	private String requiredPermission;

	@Schema(title = "HTTP 请求方法")
	private String requestMethod;

	@Schema(title = "HTTP 请求 URI")
	private String requestUri;

	@Schema(title = "HTTP 请求 IP")
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

	@Schema(title = "类名")
	private String className;

	@Schema(title = "方法名")
	private String methodName;

	@Schema(title = "方法参数")
	private String methodParams;

	@Schema(title = "异常消息")
	private String exceptionMessage;

}

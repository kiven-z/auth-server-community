package com.auth.service.system.admin.model.po.logauthorizationaudit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 权限决策审计分页行
 *
 * @author Bunny
 */
@Schema(name = "LogAuthorizationAuditPageRowPO", title = "权限决策审计分页行")
@Getter
@Setter
@ToString
public class LogAuthorizationAuditPageRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private Long id;

	@Schema(title = "事件类型")
	private String eventType;

	@Schema(title = "决策原因")
	private String decisionReason;

	@Schema(title = "Controller 类名")
	private String className;

	@Schema(title = "方法名")
	private String methodName;

	@Schema(title = "创建时间")
	private Instant createdAt;

	@Schema(title = "更新时间")
	private Instant updatedAt;

	@Schema(title = "创建用户")
	private Long createdBy;

	@Schema(title = "更新用户")
	private Long updatedBy;

}

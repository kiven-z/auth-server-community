package com.auth.service.system.admin.model.query.log;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 操作日志分页查询条件
 *
 * @author Bunny
 */
@Schema(name = "LogOperationQuery", title = "操作日志查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class LogOperationQuery extends PageQueryRequest {

	@Schema(title = "操作用户 ID")
	private Long userId;

	@Schema(title = "操作类型：CREATE/UPDATE/DELETE/QUERY/EXPORT")
	private String operationType;

	@Schema(title = "操作模块")
	private String module;

	@Schema(title = "操作对象类型")
	private String targetType;

	@Schema(title = "目标主键 ID")
	private Long targetId;

	@Schema(title = "HTTP 方法：GET/POST/PUT/DELETE")
	private String requestMethod;

}

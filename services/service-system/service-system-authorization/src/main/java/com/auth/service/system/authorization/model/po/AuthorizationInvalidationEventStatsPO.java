package com.auth.service.system.authorization.model.po;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 授权失效幂等事件运维统计
 *
 * @author Bunny
 */
@Getter
@Setter
public class AuthorizationInvalidationEventStatsPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "总记录数")
	private Long totalCount;

	@Schema(title = "处理中占位记录数")
	private Long processingCount;

	@Schema(title = "已完成记录数")
	private Long completedCount;

}

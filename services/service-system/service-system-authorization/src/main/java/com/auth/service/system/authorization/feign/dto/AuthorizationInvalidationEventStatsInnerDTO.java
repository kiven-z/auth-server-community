package com.auth.service.system.authorization.feign.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * 授权失效幂等事件统计 Feign DTO
 *
 * @author Bunny
 */
@Getter
@Setter
@ToString
public class AuthorizationInvalidationEventStatsInnerDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "总记录数")
	private Long totalCount;

	@Schema(title = "处理中占位记录数")
	private Long processingCount;

	@Schema(title = "已完成记录数")
	private Long completedCount;

}

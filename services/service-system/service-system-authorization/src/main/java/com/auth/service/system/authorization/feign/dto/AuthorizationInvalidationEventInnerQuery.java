package com.auth.service.system.authorization.feign.dto;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 授权失效幂等事件分页查询
 *
 * @author Bunny
 */
@Schema(name = "AuthorizationInvalidationEventInnerQuery", title = "授权失效幂等事件查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class AuthorizationInvalidationEventInnerQuery extends PageQueryRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "业务事件 ID")
	private String eventId;

	@Schema(title = "变更维度（AuthorizationChangeKind）")
	private String changeKind;

	@Schema(title = "是否处理中占位（impacted_user_count = -1）")
	private Boolean processing;

	@Schema(title = "处理完成时间（开始）")
	private Instant processedAtStart;

	@Schema(title = "处理完成时间（结束）")
	private Instant processedAtEnd;

}

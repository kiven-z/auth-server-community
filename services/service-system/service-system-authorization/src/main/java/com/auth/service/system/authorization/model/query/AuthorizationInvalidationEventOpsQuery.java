package com.auth.service.system.authorization.model.query;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * 授权失效幂等事件运维查询条件
 *
 * @author Bunny
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class AuthorizationInvalidationEventOpsQuery extends PageQueryRequest {

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

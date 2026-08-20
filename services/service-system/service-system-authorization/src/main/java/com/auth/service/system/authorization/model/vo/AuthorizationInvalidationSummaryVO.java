package com.auth.service.system.authorization.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * 授权失效运维统计摘要
 *
 * @author Bunny
 */
@Schema(name = "AuthorizationInvalidationSummaryVO", title = "授权失效运维统计")
@Getter
@Setter
@ToString
public class AuthorizationInvalidationSummaryVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "Outbox 投递队列统计")
	private AuthorizationInvalidationOutboxStatsVO outbox;

	@Schema(title = "幂等事件统计")
	private AuthorizationInvalidationEventStatsVO event;

}

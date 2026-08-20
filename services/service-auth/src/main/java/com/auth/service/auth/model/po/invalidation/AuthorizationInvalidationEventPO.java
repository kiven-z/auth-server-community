package com.auth.service.auth.model.po.invalidation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * 授权失效幂等事件投影
 *
 * @author Bunny
 */
@Getter
@Setter
public class AuthorizationInvalidationEventPO {

	@Schema(title = "主键")
	private Long id;

	@Schema(title = "业务事件 ID（幂等键）")
	private String eventId;

	@Schema(title = "变更维度")
	private String changeKind;

	@Schema(title = "影响面用户数")
	private Integer impactedUserCount;

	@Schema(title = "递增版本用户数")
	private Integer versionBumpedCount;

	@Schema(title = "刷新画像用户数")
	private Integer profileRefreshedCount;

	@Schema(title = "驱逐画像用户数")
	private Integer profileEvictedCount;

	@Schema(title = "处理完成时间")
	private Instant processedAt;

}

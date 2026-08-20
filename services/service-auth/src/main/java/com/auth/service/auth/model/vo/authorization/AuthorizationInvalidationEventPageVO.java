package com.auth.service.auth.model.vo.authorization;

import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

/**
 * 授权失效幂等事件分页行
 *
 * @author Bunny
 */
@Schema(name = "AuthorizationInvalidationEventPageVO", title = "授权失效幂等事件分页行")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class AuthorizationInvalidationEventPageVO extends BaseResponse {

	@Schema(title = "业务事件 ID")
	private String eventId;

	@Schema(title = "变更维度")
	private String changeKind;

	@Schema(title = "是否处理中占位")
	private Boolean processing;

	@Schema(title = "影响面用户数")
	private Integer impactedUserCount;

	@Schema(title = "递增 perm_version 用户数")
	private Integer versionBumpedCount;

	@Schema(title = "刷新画像用户数")
	private Integer profileRefreshedCount;

	@Schema(title = "驱逐画像用户数")
	private Integer profileEvictedCount;

	@Schema(title = "处理完成时间")
	private Instant processedAt;

}

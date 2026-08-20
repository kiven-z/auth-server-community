package com.auth.service.auth.convert;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;
import com.auth.service.auth.model.po.invalidation.AuthorizationInvalidationEventPO;
import com.auth.service.auth.support.invalidation.InvalidationProcessingMarker;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.experimental.UtilityClass;
import org.mapstruct.Named;

import java.time.Instant;
import java.util.Objects;

/**
 * 授权失效幂等事件写侧投影组装与 MapStruct 辅助映射。
 *
 * @author Bunny
 */
@UtilityClass
public class AuthorizationInvalidationEventMappingSupport {

	/**
	 * 构建处理中占位投影行。
	 * <p>
	 * 复杂度否决：含 IdWorker、多计数占位与审计时间，不适合 MapStruct。
	 * </p>
	 * @param eventId 事件 ID
	 * @param kind 变更类型
	 * @return 待插入的占位投影
	 */
	public static AuthorizationInvalidationEventPO buildProcessingClaim(String eventId, AuthorizationChangeKind kind) {
		Instant now = Instant.now();
		AuthorizationInvalidationEventPO projection = new AuthorizationInvalidationEventPO();
		projection.setId(IdWorker.getId());
		projection.setEventId(eventId);
		projection.setChangeKind(kind.name());
		projection.setImpactedUserCount(InvalidationProcessingMarker.PROCESSING_COUNT);
		projection.setVersionBumpedCount(InvalidationProcessingMarker.PROCESSING_COUNT);
		projection.setProfileRefreshedCount(InvalidationProcessingMarker.PROCESSING_COUNT);
		projection.setProfileEvictedCount(InvalidationProcessingMarker.PROCESSING_COUNT);
		projection.setProcessedAt(now);
		return projection;
	}

	/**
	 * 投影行 → 失效处理结果 DTO。
	 * <p>
	 * 复杂度否决：含 null 安全默认值，字段少且读侧专用，手写更清晰。
	 * </p>
	 * @param projection 持久化投影
	 * @return 失效处理结果
	 */
	public static AuthorizationInvalidateResponse toResponse(AuthorizationInvalidationEventPO projection) {
		return new AuthorizationInvalidateResponse(Objects.requireNonNullElse(projection.getImpactedUserCount(), 0),
				Objects.requireNonNullElse(projection.getVersionBumpedCount(), 0),
				Objects.requireNonNullElse(projection.getProfileRefreshedCount(), 0),
				Objects.requireNonNullElse(projection.getProfileEvictedCount(), 0));
	}

	/**
	 * 失效处理结果 DTO → 待更新的完成态投影。
	 * <p>
	 * 复杂度否决：Response record → Projection 更新载荷，无对称 MapStruct 源类型。
	 * </p>
	 * @param eventId 事件 ID
	 * @param kind 变更类型
	 * @param response 处理结果
	 * @return 待 update 的投影
	 */
	public static AuthorizationInvalidationEventPO toProcessedOutcomeProjection(String eventId,
			AuthorizationChangeKind kind, AuthorizationInvalidateResponse response) {
		AuthorizationInvalidationEventPO projection = new AuthorizationInvalidationEventPO();
		projection.setEventId(eventId);
		projection.setChangeKind(kind.name());
		projection.setImpactedUserCount(response.impactedUserCount());
		projection.setVersionBumpedCount(response.versionBumpedCount());
		projection.setProfileRefreshedCount(response.profileRefreshedCount());
		projection.setProfileEvictedCount(response.profileEvictedCount());
		projection.setProcessedAt(Instant.now());
		return projection;
	}

	/**
	 * 影响面计数是否为处理中占位
	 * @param impactedUserCount 影响面用户数
	 * @return 处理中时为 true
	 */
	@Named("toProcessingFlag")
	public static Boolean toProcessingFlag(Integer impactedUserCount) {
		return InvalidationProcessingMarker.isProcessing(impactedUserCount);
	}

}

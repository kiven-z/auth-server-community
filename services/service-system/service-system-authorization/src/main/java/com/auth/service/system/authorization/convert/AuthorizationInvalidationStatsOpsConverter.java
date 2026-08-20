package com.auth.service.system.authorization.convert;

import com.auth.service.system.authorization.feign.dto.AuthorizationInvalidationEventStatsInnerDTO;
import com.auth.service.system.authorization.model.po.AuthorizationInvalidationEventStatsPO;
import com.auth.service.system.authorization.model.po.AuthorizationInvalidationFailureRateTrendBucketPO;
import com.auth.service.system.authorization.model.po.AuthorizationInvalidationOutboxStatsPO;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationEventStatsVO;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationFailureRateTrendPointVO;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationOutboxStatsVO;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 授权失效运维统计转换器；含 null 默认值与失败率计算，不使用 MapStruct。
 *
 * @author Bunny
 */
@UtilityClass
public class AuthorizationInvalidationStatsOpsConverter {

	/**
	 * Outbox 统计 PO 转 VO
	 * @param statsPo 统计 PO
	 * @return Outbox 统计 VO
	 */
	public static AuthorizationInvalidationOutboxStatsVO toOutboxStatsVo(
			AuthorizationInvalidationOutboxStatsPO statsPo) {
		AuthorizationInvalidationOutboxStatsPO safePo = Objects.requireNonNullElse(statsPo,
				new AuthorizationInvalidationOutboxStatsPO());
		AuthorizationInvalidationOutboxStatsVO statsVo = new AuthorizationInvalidationOutboxStatsVO();

		long total = Objects.requireNonNullElse(safePo.getTotalCount(), 0L);
		long failed = Objects.requireNonNullElse(safePo.getFailedCount(), 0L);
		long dead = Objects.requireNonNullElse(safePo.getDeadCount(), 0L);
		statsVo.setTotalCount(total);
		statsVo.setPendingCount(Objects.requireNonNullElse(safePo.getPendingCount(), 0L));
		statsVo.setProcessingCount(Objects.requireNonNullElse(safePo.getProcessingCount(), 0L));
		statsVo.setSuccessCount(Objects.requireNonNullElse(safePo.getSuccessCount(), 0L));
		statsVo.setFailedCount(failed);
		statsVo.setDeadCount(dead);
		statsVo.setFailureRatePercent(calculateFailureRatePercent(total, failed, dead));
		return statsVo;
	}

	/**
	 * Feign 统计摘要 → 统计 PO
	 * @param statsDto Feign 统计
	 * @return 统计 PO
	 */
	public static AuthorizationInvalidationEventStatsPO toEventStatsPo(
			AuthorizationInvalidationEventStatsInnerDTO statsDto) {
		AuthorizationInvalidationEventStatsPO statsPo = new AuthorizationInvalidationEventStatsPO();

		statsPo.setTotalCount(statsDto.getTotalCount());
		statsPo.setProcessingCount(statsDto.getProcessingCount());
		statsPo.setCompletedCount(statsDto.getCompletedCount());
		return statsPo;
	}

	/**
	 * 幂等事件统计 PO 转 VO
	 * @param statsPo 统计 PO
	 * @return 事件统计 VO
	 */
	public static AuthorizationInvalidationEventStatsVO toEventStatsVo(AuthorizationInvalidationEventStatsPO statsPo) {
		AuthorizationInvalidationEventStatsPO safePo = Objects.requireNonNullElse(statsPo,
				new AuthorizationInvalidationEventStatsPO());
		AuthorizationInvalidationEventStatsVO statsVo = new AuthorizationInvalidationEventStatsVO();

		statsVo.setTotalCount(Objects.requireNonNullElse(safePo.getTotalCount(), 0L));
		statsVo.setProcessingCount(Objects.requireNonNullElse(safePo.getProcessingCount(), 0L));
		statsVo.setCompletedCount(Objects.requireNonNullElse(safePo.getCompletedCount(), 0L));
		return statsVo;
	}

	/**
	 * 失败率趋势分桶 PO 转 VO
	 * @param bucketPo 分桶统计 PO
	 * @return 趋势点 VO
	 */
	public static AuthorizationInvalidationFailureRateTrendPointVO toFailureRateTrendPointVo(
			AuthorizationInvalidationFailureRateTrendBucketPO bucketPo) {
		AuthorizationInvalidationFailureRateTrendBucketPO safePo = Objects.requireNonNullElse(bucketPo,
				new AuthorizationInvalidationFailureRateTrendBucketPO());
		AuthorizationInvalidationFailureRateTrendPointVO pointVo = new AuthorizationInvalidationFailureRateTrendPointVO();

		long total = Objects.requireNonNullElse(safePo.getTotalCount(), 0L);
		long failed = Objects.requireNonNullElse(safePo.getFailedCount(), 0L);
		long dead = Objects.requireNonNullElse(safePo.getDeadCount(), 0L);

		pointVo.setBucket(safePo.getBucket());
		pointVo.setTotalCount(total);
		pointVo.setFailedCount(failed);
		pointVo.setDeadCount(dead);
		pointVo.setFailureRatePercent(calculateFailureRatePercent(total, failed, dead));
		return pointVo;
	}

	/**
	 * 计算失败率百分比
	 * @param total 总记录数
	 * @param failed 待重试记录数
	 * @param dead 死信记录数
	 * @return 失败率百分比
	 */
	static BigDecimal calculateFailureRatePercent(long total, long failed, long dead) {
		if (total <= 0L) {
			return BigDecimal.ZERO;
		}
		return BigDecimal.valueOf(failed + dead)
			.multiply(BigDecimal.valueOf(100))
			.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
	}

}

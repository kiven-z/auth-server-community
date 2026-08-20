package com.auth.service.auth.support.invalidation;

import lombok.experimental.UtilityClass;

/**
 * 幂等表中「处理中」占位标记（impacted_user_count = -1）
 *
 * @author Bunny
 */
@UtilityClass
public class InvalidationProcessingMarker {

	/**
	 * 处理中占位计数，与已完成记录的 >= 0 区分
	 */
	public static final int PROCESSING_COUNT = -1;

	/**
	 * 是否为处理中占位行
	 * @param impactedUserCount 影响面用户数
	 * @return 处理中时为 true
	 */
	public static boolean isProcessing(Integer impactedUserCount) {
		return impactedUserCount != null && impactedUserCount == PROCESSING_COUNT;
	}

}

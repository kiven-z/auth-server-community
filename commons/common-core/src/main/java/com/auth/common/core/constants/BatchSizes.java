package com.auth.common.core.constants;

import lombok.experimental.UtilityClass;

/**
 * 通用批处理批次大小（数字档）
 *
 * <p>
 * 业务批量读写 / 清理 / 扫描优先从这两档选取；需要更小抢占或更小事务时在调用侧单独定义。
 * </p>
 *
 * @author Bunny
 */
@UtilityClass
public class BatchSizes {

	/**
	 * 常规批处理档
	 */
	public static final int SIZE_500 = 500;

	/**
	 * 偏大批处理档（游标扫描、导出分页抓取等）
	 */
	public static final int SIZE_1000 = 1000;

}

package com.auth.service.system.authorization.mapper;

import com.auth.service.system.authorization.model.entity.SysAuthorizationInvalidationOutboxEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;

/**
 * 授权失效 Outbox Mapper
 *
 * @author Bunny
 */
@Mapper
public interface SysAuthorizationInvalidationOutboxMapper {

	/**
	 * 插入待投递记录（status=PENDING）
	 * @param outboxEntity Outbox 行
	 * @return 影响行数
	 */
	int insertPending(SysAuthorizationInvalidationOutboxEntity outboxEntity);

	/**
	 * 按主键查询
	 * @param id 主键
	 * @return 记录，不存在为 null
	 */
	SysAuthorizationInvalidationOutboxEntity selectById(@Param("id") Long id);

	/**
	 * 查询可抢占的待重试记录
	 * @param statuses 状态列表
	 * @param now 当前时间
	 * @param limit 批次上限
	 * @return 记录列表
	 */
	List<SysAuthorizationInvalidationOutboxEntity> selectClaimable(@Param("statuses") List<String> statuses,
			@Param("now") Instant now, @Param("limit") int limit);

	/**
	 * 抢占记录（乐观锁 version）
	 * @param id 主键
	 * @param lockedBy 实例标识
	 * @param lockedAt 抢占时间
	 * @param expectedVersion 期望版本
	 * @return 更新行数
	 */
	int claimForProcessing(@Param("id") Long id, @Param("lockedBy") String lockedBy,
			@Param("lockedAt") Instant lockedAt, @Param("expectedVersion") Long expectedVersion);

	/**
	 * 标记投递成功
	 * @param id 主键
	 * @param processedAt 处理完成时间
	 * @return 影响行数
	 */
	int markSuccess(@Param("id") Long id, @Param("processedAt") Instant processedAt);

	/**
	 * 标记投递失败并安排下次重试
	 * @param id 主键
	 * @param status FAILED 或 DEAD
	 * @param retryCount 已重试次数
	 * @param nextRetryAt 下次重试时间（DEAD 可为 null）
	 * @param lastError 失败原因
	 * @return 影响行数
	 */
	int markFailure(@Param("id") Long id, @Param("status") String status, @Param("retryCount") int retryCount,
			@Param("nextRetryAt") Instant nextRetryAt, @Param("lastError") String lastError);

	/**
	 * 同步投递失败时记录错误（保持 PENDING，供 Job 重试）
	 * @param id 主键
	 * @param lastError 失败原因
	 * @return 影响行数
	 */
	int recordSyncFailure(@Param("id") Long id, @Param("lastError") String lastError);

	/**
	 * 人工重试前重置为 PENDING（DEAD / FAILED / PROCESSING）
	 * @param id 主键
	 * @param expectedVersion 期望版本
	 * @return 影响行数
	 */
	int resetForManualRetry(@Param("id") Long id, @Param("expectedVersion") Long expectedVersion);

	/**
	 * 批量删除 SUCCESS 且已过保留期的 Outbox 记录
	 * @param cutoffTime processed_at 早于此时间可被删除
	 * @param batchSize 本批删除上限
	 * @return 实际删除行数
	 */
	int deleteSuccessBefore(@Param("cutoffTime") Instant cutoffTime, @Param("batchSize") int batchSize);

}

package com.auth.service.system.authorization.service;

import com.auth.service.system.authorization.model.dto.AuthorizationInvalidationOutboxManualRetryOutcome;
import com.auth.service.system.authorization.model.po.AuthorizationInvalidationOutboxDetailRowPO;
import com.auth.service.system.authorization.model.po.AuthorizationInvalidationOutboxPageRowPO;
import com.auth.service.system.authorization.model.po.AuthorizationInvalidationOutboxStatsPO;
import com.auth.service.system.authorization.model.query.AuthorizationInvalidationOutboxOpsQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 授权失效 Outbox 运维门面
 *
 * @author Bunny
 */
public interface AuthorizationInvalidationOutboxOpsService {

	/**
	 * 分页查询 Outbox
	 * @param query 查询条件
	 * @return 分页结果
	 */
	IPage<AuthorizationInvalidationOutboxPageRowPO> getPage(AuthorizationInvalidationOutboxOpsQuery query);

	/**
	 * 查询 Outbox 详情
	 * @param id 主键
	 * @return 详情行，不存在时抛出
	 * {@link com.auth.service.system.common.exception.SystemBusinessException}
	 */
	AuthorizationInvalidationOutboxDetailRowPO getDetail(Long id);

	/**
	 * 统计 Outbox 各状态记录数
	 * @return 状态统计
	 */
	AuthorizationInvalidationOutboxStatsPO getOutboxStats();

	/**
	 * 人工重试单条 Outbox
	 * @param outboxId Outbox 主键
	 * @param force 是否强制重试 PROCESSING 占位
	 * @return 重试结果摘要
	 */
	AuthorizationInvalidationOutboxManualRetryOutcome retryManual(Long outboxId, boolean force);

}

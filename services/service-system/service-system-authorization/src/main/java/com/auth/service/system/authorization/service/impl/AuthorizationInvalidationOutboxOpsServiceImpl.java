package com.auth.service.system.authorization.service.impl;

import com.auth.service.system.authorization.mapper.AuthorizationInvalidationOutboxOpsMapper;
import com.auth.service.system.authorization.model.dto.AuthorizationInvalidationOutboxManualRetryOutcome;
import com.auth.service.system.authorization.model.po.AuthorizationInvalidationOutboxDetailRowPO;
import com.auth.service.system.authorization.model.po.AuthorizationInvalidationOutboxPageRowPO;
import com.auth.service.system.authorization.model.po.AuthorizationInvalidationOutboxStatsPO;
import com.auth.service.system.authorization.model.query.AuthorizationInvalidationOutboxOpsQuery;
import com.auth.service.system.authorization.ops.AuthorizationInvalidationOutboxManualRetryApplicationService;
import com.auth.service.system.authorization.service.AuthorizationInvalidationOutboxOpsService;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;

/**
 * 授权失效 Outbox 运维门面实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AuthorizationInvalidationOutboxOpsServiceImpl implements AuthorizationInvalidationOutboxOpsService {

	private final AuthorizationInvalidationOutboxOpsMapper authorizationInvalidationOutboxOpsMapper;

	private final AuthorizationInvalidationOutboxManualRetryApplicationService manualRetryApplicationService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IPage<AuthorizationInvalidationOutboxPageRowPO> getPage(AuthorizationInvalidationOutboxOpsQuery query) {
		Page<AuthorizationInvalidationOutboxPageRowPO> pageParams = new Page<>(query.getPageIndex(),
				query.getPageSize());
		return authorizationInvalidationOutboxOpsMapper.selectListByPage(pageParams, query);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationInvalidationOutboxDetailRowPO getDetail(Long id) {
		AuthorizationInvalidationOutboxDetailRowPO detailRow = authorizationInvalidationOutboxOpsMapper
			.selectDetailById(id);
		if (detailRow == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}
		return detailRow;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationInvalidationOutboxStatsPO getOutboxStats() {
		return authorizationInvalidationOutboxOpsMapper.selectOutboxStats();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationInvalidationOutboxManualRetryOutcome retryManual(Long outboxId, boolean force) {
		return manualRetryApplicationService.retryManual(outboxId, force);
	}

}

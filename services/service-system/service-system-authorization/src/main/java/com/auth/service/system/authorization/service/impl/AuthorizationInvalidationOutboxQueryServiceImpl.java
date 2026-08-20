package com.auth.service.system.authorization.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.authorization.convert.AuthorizationInvalidationOutboxOpsConverter;
import com.auth.service.system.authorization.model.po.AuthorizationInvalidationOutboxDetailRowPO;
import com.auth.service.system.authorization.model.po.AuthorizationInvalidationOutboxPageRowPO;
import com.auth.service.system.authorization.model.query.AuthorizationInvalidationOutboxOpsQuery;
import com.auth.service.system.authorization.model.query.AuthorizationInvalidationOutboxQuery;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationOutboxDetailVO;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationOutboxPageVO;
import com.auth.service.system.authorization.service.AuthorizationInvalidationOutboxOpsService;
import com.auth.service.system.authorization.service.AuthorizationInvalidationOutboxQueryService;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 授权失效 Outbox 运维查询服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AuthorizationInvalidationOutboxQueryServiceImpl implements AuthorizationInvalidationOutboxQueryService {

	private final AuthorizationInvalidationOutboxOpsService authorizationInvalidationOutboxOpsService;

	private final AuditUserDisplayService auditUserDisplayService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<AuthorizationInvalidationOutboxPageVO> getPage(AuthorizationInvalidationOutboxQuery query) {
		AuthorizationInvalidationOutboxOpsQuery opsQuery = AuthorizationInvalidationOutboxOpsConverter.INSTANCE
			.toOpsQuery(query);
		IPage<AuthorizationInvalidationOutboxPageRowPO> page = authorizationInvalidationOutboxOpsService
			.getPage(opsQuery);
		IPage<AuthorizationInvalidationOutboxPageVO> voPage = page
			.convert(AuthorizationInvalidationOutboxOpsConverter.INSTANCE::toPageVO);

		auditUserDisplayService.enrichAuditUsernames(voPage, null, null);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationInvalidationOutboxDetailVO getDetail(Long id) {
		AuthorizationInvalidationOutboxDetailRowPO detailRow = authorizationInvalidationOutboxOpsService.getDetail(id);
		AuthorizationInvalidationOutboxDetailVO detailVO = AuthorizationInvalidationOutboxOpsConverter.INSTANCE
			.toDetailVo(detailRow);

		auditUserDisplayService.enrichAuditUsernames(List.of(detailVO), null, null);
		return detailVO;
	}

}

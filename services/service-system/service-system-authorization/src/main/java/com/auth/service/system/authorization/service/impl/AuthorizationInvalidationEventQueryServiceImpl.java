package com.auth.service.system.authorization.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.authorization.convert.AuthorizationInvalidationEventOpsConverter;
import com.auth.service.system.authorization.feign.dto.AuthorizationInvalidationEventDetailInnerDTO;
import com.auth.service.system.authorization.feign.dto.AuthorizationInvalidationEventPageInnerDTO;
import com.auth.service.system.authorization.model.query.AuthorizationInvalidationEventOpsQuery;
import com.auth.service.system.authorization.model.query.AuthorizationInvalidationEventQuery;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationEventDetailVO;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationEventPageVO;
import com.auth.service.system.authorization.service.AuthorizationInvalidationEventOpsService;
import com.auth.service.system.authorization.service.AuthorizationInvalidationEventQueryService;
import com.auth.service.system.common.service.AuditUserDisplayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 授权失效幂等事件运维查询服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AuthorizationInvalidationEventQueryServiceImpl implements AuthorizationInvalidationEventQueryService {

	private final AuthorizationInvalidationEventOpsService authorizationInvalidationEventOpsService;

	private final AuditUserDisplayService auditUserDisplayService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<AuthorizationInvalidationEventPageVO> getPage(AuthorizationInvalidationEventQuery query) {
		AuthorizationInvalidationEventOpsQuery opsQuery = AuthorizationInvalidationEventOpsConverter.INSTANCE
			.toOpsQuery(query);
		PageResponse<AuthorizationInvalidationEventPageInnerDTO> pageData = authorizationInvalidationEventOpsService
			.getPage(opsQuery);

		List<AuthorizationInvalidationEventPageVO> records = pageData.getList()
			.stream()
			.map(AuthorizationInvalidationEventOpsConverter.INSTANCE::toPageVO)
			.toList();
		PageResponse<AuthorizationInvalidationEventPageVO> response = PageResponse.of(pageData.getPageNo(),
				pageData.getPageSize(), pageData.getTotal(), records);

		auditUserDisplayService.enrichAuditUsernames(response.getList(), null, null);
		return response;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationInvalidationEventDetailVO getDetail(Long id) {
		AuthorizationInvalidationEventDetailInnerDTO detailDto = authorizationInvalidationEventOpsService.getDetail(id);
		AuthorizationInvalidationEventDetailVO detailVO = AuthorizationInvalidationEventOpsConverter.INSTANCE
			.toDetailVO(detailDto);

		auditUserDisplayService.enrichAuditUsernames(List.of(detailVO), null, null);
		return detailVO;
	}

}

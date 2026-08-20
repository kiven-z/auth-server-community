package com.auth.service.system.admin.service.admin.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.admin.log.LogAuthorizationAuditConverter;
import com.auth.service.system.admin.mapper.admin.log.LogAuthorizationAuditMapper;
import com.auth.service.system.admin.model.entity.LogAuthorizationAuditEntity;
import com.auth.service.system.admin.model.po.logauthorizationaudit.LogAuthorizationAuditPageRowPO;
import com.auth.service.system.admin.model.query.log.LogAuthorizationAuditQuery;
import com.auth.service.system.admin.model.vo.logauthorizationaudit.LogAuthorizationAuditDetailVO;
import com.auth.service.system.admin.model.vo.logauthorizationaudit.LogAuthorizationAuditPageVO;
import com.auth.service.system.admin.service.admin.LogAuthorizationAuditService;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;

/**
 * 权限决策审计日志服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class LogAuthorizationAuditServiceImpl extends
		ServiceImpl<LogAuthorizationAuditMapper, LogAuthorizationAuditEntity> implements LogAuthorizationAuditService {

	private final AuditUserDisplayService auditUserDisplayService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public PageResponse<LogAuthorizationAuditPageVO> getPage(LogAuthorizationAuditQuery query) {
		Page<LogAuthorizationAuditEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		IPage<LogAuthorizationAuditPageRowPO> page = baseMapper.selectListByPage(pageParams, query);
		IPage<LogAuthorizationAuditPageVO> voPage = page.convert(LogAuthorizationAuditConverter.INSTANCE::toPageVO);

		auditUserDisplayService.enrichAuditUsernames(voPage, null, null);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public LogAuthorizationAuditDetailVO getDetail(Long id) {
		LogAuthorizationAuditEntity entity = getById(id);
		if (entity == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}

		LogAuthorizationAuditDetailVO vo = LogAuthorizationAuditConverter.INSTANCE.toDetailVo(entity);
		auditUserDisplayService.enrichAuditUsernames(List.of(vo), null, null);
		return vo;
	}

}

package com.auth.service.system.admin.service.admin.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.admin.log.LogOperationConverter;
import com.auth.service.system.admin.mapper.admin.log.LogOperationMapper;
import com.auth.service.system.admin.model.entity.LogOperationEntity;
import com.auth.service.system.admin.model.po.logoperation.LogOperationPageRowPO;
import com.auth.service.system.admin.model.query.log.LogOperationQuery;
import com.auth.service.system.admin.model.vo.logoperation.LogOperationDetailVO;
import com.auth.service.system.admin.model.vo.logoperation.LogOperationPageVO;
import com.auth.service.system.admin.service.admin.LogOperationService;
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
 * 操作日志服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class LogOperationServiceImpl extends ServiceImpl<LogOperationMapper, LogOperationEntity>
		implements LogOperationService {

	private final AuditUserDisplayService auditUserDisplayService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public PageResponse<LogOperationPageVO> getPage(LogOperationQuery query) {
		Page<LogOperationEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		IPage<LogOperationPageRowPO> page = baseMapper.selectListByPage(pageParams, query);
		IPage<LogOperationPageVO> voPage = page.convert(LogOperationConverter.INSTANCE::toPageVO);

		auditUserDisplayService.enrichAuditUsernames(voPage, null, null);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public LogOperationDetailVO getDetail(Long id) {
		LogOperationEntity entity = getById(id);
		if (entity == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}

		LogOperationDetailVO vo = LogOperationConverter.INSTANCE.toDetailVo(entity);
		auditUserDisplayService.enrichAuditUsernames(List.of(vo), null, null);
		return vo;
	}

}

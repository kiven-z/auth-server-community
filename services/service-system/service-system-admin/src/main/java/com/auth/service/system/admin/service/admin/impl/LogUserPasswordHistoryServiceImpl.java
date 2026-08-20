package com.auth.service.system.admin.service.admin.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.admin.log.LogUserPasswordHistoryConverter;
import com.auth.service.system.admin.mapper.admin.log.LogUserPasswordHistoryMapper;
import com.auth.service.system.admin.model.entity.LogUserPasswordHistoryEntity;
import com.auth.service.system.admin.model.po.loguserpasswordhistory.LogUserPasswordHistoryDetailRowPO;
import com.auth.service.system.admin.model.po.loguserpasswordhistory.LogUserPasswordHistoryPageRowPO;
import com.auth.service.system.admin.model.query.log.LogUserPasswordHistoryQuery;
import com.auth.service.system.admin.model.vo.loguserpasswordhistory.LogUserPasswordHistoryDetailVO;
import com.auth.service.system.admin.model.vo.loguserpasswordhistory.LogUserPasswordHistoryPageVO;
import com.auth.service.system.admin.service.admin.LogUserPasswordHistoryService;
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
 * 密码历史日志服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class LogUserPasswordHistoryServiceImpl
		extends ServiceImpl<LogUserPasswordHistoryMapper, LogUserPasswordHistoryEntity>
		implements LogUserPasswordHistoryService {

	private final AuditUserDisplayService auditUserDisplayService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public PageResponse<LogUserPasswordHistoryPageVO> getPage(LogUserPasswordHistoryQuery query) {
		Page<LogUserPasswordHistoryEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		IPage<LogUserPasswordHistoryPageRowPO> page = baseMapper.selectListByPage(pageParams, query);
		IPage<LogUserPasswordHistoryPageVO> voPage = page.convert(LogUserPasswordHistoryConverter.INSTANCE::toPageVO);

		auditUserDisplayService.enrichAuditUsernames(voPage, null, null);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public LogUserPasswordHistoryDetailVO getDetail(Long id) {
		LogUserPasswordHistoryDetailRowPO po = baseMapper.selectDetailById(id);
		if (po == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}

		LogUserPasswordHistoryDetailVO vo = LogUserPasswordHistoryConverter.INSTANCE.toDetailVo(po);
		auditUserDisplayService.enrichAuditUsernames(List.of(vo), null, null);
		return vo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void recordChange(Long userId, String passwordHash, String changeIp) {
		LogUserPasswordHistoryEntity entity = new LogUserPasswordHistoryEntity();
		entity.setUserId(userId);
		entity.setPasswordHash(passwordHash);
		entity.setChangeIp(changeIp);
		save(entity);
	}

}

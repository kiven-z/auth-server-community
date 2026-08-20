package com.auth.service.system.schedule.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.schedule.convert.SysJobGroupConverter;
import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.mapper.JobGroupMapper;
import com.auth.service.system.schedule.mapper.JobMapper;
import com.auth.service.system.schedule.model.entity.JobEntity;
import com.auth.service.system.schedule.model.entity.JobGroupEntity;
import com.auth.service.system.schedule.model.form.SysJobGroupForm;
import com.auth.service.system.schedule.model.form.SysJobGroupUpdateForm;
import com.auth.service.system.schedule.model.po.JobGroupPageRowPO;
import com.auth.service.system.schedule.model.query.SysJobGroupQuery;
import com.auth.service.system.schedule.model.vo.SysJobGroupDetailVO;
import com.auth.service.system.schedule.model.vo.SysJobGroupPageVO;
import com.auth.service.system.schedule.service.SysJobGroupService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_CODE_DUPLICATE;
import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;
import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_GROUP_HAS_ACTIVE_JOBS;
import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_GROUP_SYSTEM_PROTECTED;

/**
 * 任务分组服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class SysJobGroupServiceImpl extends ServiceImpl<JobGroupMapper, JobGroupEntity> implements SysJobGroupService {

	private final JobMapper jobMapper;

	private final AuditUserDisplayService auditUserDisplayService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<SysJobGroupPageVO> getPage(SysJobGroupQuery query) {
		Page<JobGroupEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		IPage<JobGroupPageRowPO> page = baseMapper.selectListByPage(pageParams, query);
		IPage<SysJobGroupPageVO> voPage = page.convert(SysJobGroupConverter.INSTANCE::toPageVO);

		auditUserDisplayService.enrichAuditUsernames(voPage, null, null);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SysJobGroupPageVO> listEnabledOptions(String keyword, int limit) {
		List<JobGroupPageRowPO> list = baseMapper.selectEnabledOptions(keyword, limit);
		List<SysJobGroupPageVO> voList = list.stream().map(SysJobGroupConverter.INSTANCE::toPageVO).toList();

		auditUserDisplayService.enrichAuditUsernames(voList, null, null);
		return voList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SysJobGroupDetailVO getDetail(Long id) {
		JobGroupEntity entity = super.getById(id);
		if (entity == null) {
			throw new SysJobException(DATA_NOT_EXIST);
		}

		SysJobGroupDetailVO detail = SysJobGroupConverter.INSTANCE.toDetailVo(entity);
		auditUserDisplayService.enrichAuditUsernames(List.of(detail), null, null);
		return detail;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void create(SysJobGroupForm form) {
		long exists = count(
				Wrappers.<JobGroupEntity>lambdaQuery().eq(JobGroupEntity::getGroupCode, form.getGroupCode()));
		if (exists > 0) {
			throw new SysJobException(DATA_CODE_DUPLICATE, form.getGroupCode());
		}

		JobGroupEntity jobGroup = SysJobGroupConverter.INSTANCE.toEntity(form);
		super.save(jobGroup);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void update(SysJobGroupUpdateForm form) {
		JobGroupEntity jobGroup = super.getById(form.getId());
		if (jobGroup == null) {
			throw new SysJobException(DATA_NOT_EXIST);
		}

		SysJobGroupConverter.INSTANCE.updateEntity(form, jobGroup);
		super.updateById(jobGroup);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteById(Long id) {
		JobGroupEntity jobGroup = super.getById(id);
		if (jobGroup == null) {
			throw new SysJobException(DATA_NOT_EXIST);
		}

		// 系统内置的不能删除
		if (jobGroup.getIsSystem() != null && jobGroup.getIsSystem()) {
			throw new SysJobException(JOB_GROUP_SYSTEM_PROTECTED);
		}

		// 分组下还有绑定任务不能删
		long count = jobMapper
			.selectCount(Wrappers.<JobEntity>lambdaQuery().eq(JobEntity::getJobGroup, jobGroup.getGroupCode()));
		if (count > 0) {
			throw new SysJobException(JOB_GROUP_HAS_ACTIVE_JOBS, count);
		}

		super.removeById(id);
	}

}

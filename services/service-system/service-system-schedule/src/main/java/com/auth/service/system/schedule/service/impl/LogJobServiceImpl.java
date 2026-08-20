package com.auth.service.system.schedule.service.impl;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.schedule.convert.LogJobConverter;
import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.mapper.LogJobMapper;
import com.auth.service.system.schedule.model.entity.LogJobEntity;
import com.auth.service.system.schedule.model.po.LogJobPageRowPO;
import com.auth.service.system.schedule.model.query.LogJobQuery;
import com.auth.service.system.schedule.model.vo.LogJobDetailVO;
import com.auth.service.system.schedule.model.vo.LogJobPageVO;
import com.auth.service.system.schedule.service.LogJobService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;

/**
 * 定时任务执行日志服务实现
 *
 * @author Bunny
 */
@Slf4j
@Service
public class LogJobServiceImpl extends ServiceImpl<LogJobMapper, LogJobEntity> implements LogJobService {

	private final AuditUserDisplayService auditUserDisplayService;

	public LogJobServiceImpl(AuditUserDisplayService auditUserDisplayService) {
		this.auditUserDisplayService = auditUserDisplayService;
	}

	/**
	 * 构建基础日志实体
	 * @param jobId 任务ID
	 * @param jobName 任务名称
	 * @param jobGroup 任务组
	 * @param invokeTarget 任务执行目标
	 * @param triggerType 触发类型
	 * @param elapsedMs 执行时间
	 * @return 基础日志实体
	 */
	private static LogJobEntity buildBaseLog(Long jobId, String jobName, String jobGroup, String invokeTarget,
			String triggerType, long elapsedMs) {
		LogJobEntity entity = new LogJobEntity();
		entity.setJobId(jobId);
		entity.setJobName(jobName);
		entity.setJobGroup(jobGroup);
		entity.setInvokeTarget(invokeTarget);
		entity.setTriggerType(triggerType);
		entity.setElapsedTime(elapsedMs);
		return entity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<LogJobPageVO> getPage(LogJobQuery query) {
		Page<LogJobEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		IPage<LogJobPageRowPO> page = baseMapper.selectListByPage(pageParams, query);
		IPage<LogJobPageVO> voPage = page.convert(LogJobConverter.INSTANCE::toPageVO);

		auditUserDisplayService.enrichAuditUsernames(voPage, null, null);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LogJobDetailVO getDetail(Long id) {
		LogJobEntity jobLog = super.getById(id);
		if (jobLog == null) {
			throw new SysJobException(DATA_NOT_EXIST);
		}

		LogJobDetailVO vo = LogJobConverter.INSTANCE.toDetailVo(jobLog);
		auditUserDisplayService.enrichAuditUsernames(List.of(vo), null, null);
		return vo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void recordSuccess(Long jobId, String jobName, String jobGroup, String invokeTarget, String triggerType,
			long elapsedMs, String message) {
		LogJobEntity entity = buildBaseLog(jobId, jobName, jobGroup, invokeTarget, triggerType, elapsedMs);
		entity.setJobMessage(message);
		entity.setStatus(true);
		super.save(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void recordFailure(Long jobId, String jobName, String jobGroup, String invokeTarget, String triggerType,
			long elapsedMs, JobExecutionException exception) {
		LogJobEntity entity = buildBaseLog(jobId, jobName, jobGroup, invokeTarget, triggerType, elapsedMs);
		entity.setJobMessage("FAILED");
		entity.setStatus(false);
		if (exception != null) {
			entity.setExceptionInfo(ExceptionUtil.stacktraceToString(exception, 8000));
		}
		super.save(entity);
	}

}

package com.auth.service.system.schedule.service.impl;

import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.mapper.JobGroupMapper;
import com.auth.service.system.schedule.mapper.JobMapper;
import com.auth.service.system.schedule.mapper.LogJobMapper;
import com.auth.service.system.schedule.model.entity.JobEntity;
import com.auth.service.system.schedule.model.entity.JobGroupEntity;
import com.auth.service.system.schedule.model.entity.LogJobEntity;
import com.auth.service.system.schedule.model.enums.SysJobLastExecutionStatus;
import com.auth.service.system.schedule.model.enums.SysJobQuartzRuntimeStatus;
import com.auth.service.system.schedule.model.form.SysJobCreateForm;
import com.auth.service.system.schedule.model.form.SysJobUpdateForm;
import com.auth.service.system.schedule.model.po.SysJobDetailRowPO;
import com.auth.service.system.schedule.model.po.SysJobPageRowPO;
import com.auth.service.system.schedule.model.query.SysJobQuery;
import com.auth.service.system.schedule.model.vo.SysJobDetailVO;
import com.auth.service.system.schedule.model.vo.SysJobPageVO;
import com.auth.service.system.schedule.support.SysJobQuartzRuntimeSnapshot;
import com.auth.service.system.schedule.support.quartz.SysJobQuartzRuntimeReader;
import com.auth.service.system.schedule.support.quartz.SysJobSchedulerManager;
import com.auth.service.system.schedule.validation.task.SysJobDefinitionValidationManager;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.SchedulerException;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;
import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_BUSY;
import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_GROUP_NOT_AVAILABLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link SysJobDefinitionServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysJobDefinitionServiceImpl 定时任务定义")
@ExtendWith(MockitoExtension.class)
class SysJobDefinitionServiceImplTest {

	@Mock
	private JobMapper jobMapper;

	@Mock
	private SysJobSchedulerManager sysJobSchedulerManager;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@Mock
	private JobGroupMapper jobGroupMapper;

	@Mock
	private LogJobMapper logJobMapper;

	@Mock
	private SysJobDefinitionValidationManager validationManager;

	@Mock
	private SysJobQuartzRuntimeReader sysJobQuartzRuntimeReader;

	private SysJobDefinitionServiceImpl sysJobDefinitionService;

	@BeforeAll
	static void initMybatisPlusTableInfo() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), JobEntity.class);
	}

	private static SysJobCreateForm minimalCreateForm(String jobGroup, boolean status) {
		SysJobCreateForm form = new SysJobCreateForm();
		form.setJobName("demoJob");
		form.setJobGroup(jobGroup);
		form.setTaskType("BEAN_INVOKE");
		form.setInvokeTarget("demoTask.run()");
		form.setCronExpression("0 0 12 * * ?");
		form.setMisfirePolicy(2);
		form.setConcurrent(false);
		form.setStatus(status);
		return form;
	}

	private static JobGroupEntity enabledGroup(String groupCode) {
		JobGroupEntity group = new JobGroupEntity();
		group.setGroupCode(groupCode);
		group.setStatus(true);
		return group;
	}

	@BeforeEach
	void setUp() throws Exception {
		sysJobDefinitionService = spy(new SysJobDefinitionServiceImpl(sysJobSchedulerManager, auditUserDisplayService,
				jobGroupMapper, logJobMapper, validationManager, sysJobQuartzRuntimeReader));
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysJobDefinitionService, jobMapper);
		lenient().doReturn(true).when(sysJobDefinitionService).updateBatchById(anyList());
	}

	@Test
	@DisplayName("新增：分组不存在时抛出 DATA_NOT_EXIST")
	void createThrowsWhenJobGroupMissing() {
		when(jobGroupMapper.selectOne(any())).thenReturn(null);
		SysJobCreateForm form = minimalCreateForm("MISSING", true);

		assertThatThrownBy(() -> sysJobDefinitionService.create(form)).isInstanceOf(SysJobException.class)
			.extracting(ex -> ((SysJobException) ex).getResultCode())
			.isEqualTo(DATA_NOT_EXIST);
		verify(jobMapper, never()).insert(org.mockito.ArgumentMatchers.<JobEntity>any());
	}

	@Test
	@DisplayName("新增：分组已禁用时抛出 JOB_GROUP_NOT_AVAILABLE")
	void createThrowsWhenJobGroupDisabled() {
		JobGroupEntity disabledGroup = enabledGroup("DISABLED");
		disabledGroup.setStatus(false);
		when(jobGroupMapper.selectOne(any())).thenReturn(disabledGroup);
		SysJobCreateForm form = minimalCreateForm("DISABLED", true);

		assertThatThrownBy(() -> sysJobDefinitionService.create(form)).isInstanceOf(SysJobException.class)
			.extracting(ex -> ((SysJobException) ex).getResultCode())
			.isEqualTo(JOB_GROUP_NOT_AVAILABLE);
		verify(jobMapper, never()).insert(org.mockito.ArgumentMatchers.<JobEntity>any());
	}

	@Test
	@DisplayName("新增：启用任务落库并注册 Quartz")
	void createEnabledJobPersistsAndRegistersQuartz() throws SchedulerException {
		when(jobGroupMapper.selectOne(any())).thenReturn(enabledGroup("DEFAULT"));
		when(jobMapper.insert(any(JobEntity.class))).thenReturn(1);

		sysJobDefinitionService.create(minimalCreateForm("DEFAULT", true));

		ArgumentCaptor<JobEntity> entityCaptor = ArgumentCaptor.forClass(JobEntity.class);
		verify(jobMapper).insert(entityCaptor.capture());
		assertThat(entityCaptor.getValue().getJobName()).isEqualTo("demoJob");
		assertThat(entityCaptor.getValue().getStatus()).isTrue();
		verify(sysJobSchedulerManager).scheduleOrReplace(any(JobEntity.class));
	}

	@Test
	@DisplayName("新增：暂停任务仅落库，不注册 Quartz")
	void createPausedJobPersistsWithoutQuartzRegister() {
		when(jobGroupMapper.selectOne(any())).thenReturn(enabledGroup("DEFAULT"));
		when(jobMapper.insert(any(JobEntity.class))).thenReturn(1);

		sysJobDefinitionService.create(minimalCreateForm("DEFAULT", false));

		ArgumentCaptor<JobEntity> entityCaptor = ArgumentCaptor.forClass(JobEntity.class);
		verify(jobMapper).insert(entityCaptor.capture());
		assertThat(entityCaptor.getValue().getStatus()).isFalse();
		verifyNoInteractions(sysJobSchedulerManager);
	}

	@Test
	@DisplayName("详情：存在时补全分组名称与运行时信息")
	void getDetailReturnsVoWhenExists() {
		SysJobDetailRowPO detailRow = new SysJobDetailRowPO();
		detailRow.setId(1L);
		detailRow.setJobName("demoJob");
		detailRow.setJobGroup("DEFAULT");
		detailRow.setJobGroupName("默认分组");
		detailRow.setTaskType("BEAN_INVOKE");
		when(jobMapper.selectDetailById(1L)).thenReturn(detailRow);
		when(sysJobQuartzRuntimeReader.getFireTimes("demoJob", "DEFAULT"))
			.thenReturn(SysJobQuartzRuntimeReader.FireTimes.empty());
		when(sysJobQuartzRuntimeReader.resolveRuntimeSnapshot("demoJob", "DEFAULT"))
			.thenReturn(SysJobQuartzRuntimeSnapshot.of(SysJobQuartzRuntimeStatus.IDLE, null));
		when(logJobMapper.selectLatestByJobIds(any())).thenReturn(List.of());

		SysJobDetailVO detail = sysJobDefinitionService.getDetail(1L);

		assertThat(detail.getJobName()).isEqualTo("demoJob");
		assertThat(detail.getJobGroup()).isEqualTo("DEFAULT");
		assertThat(detail.getJobGroupName()).isEqualTo("默认分组");
		assertThat(detail.getLastExecutionStatus()).isEqualTo(SysJobLastExecutionStatus.UNKNOWN);
		verify(auditUserDisplayService).enrichAuditUsernames(any(List.class), isNull(), isNull());
	}

	@Test
	@DisplayName("详情：不存在时抛出 DATA_NOT_EXIST")
	void getDetailThrowsWhenMissing() {
		when(jobMapper.selectDetailById(99L)).thenReturn(null);

		assertThatThrownBy(() -> sysJobDefinitionService.getDetail(99L)).isInstanceOf(SysJobException.class)
			.extracting(ex -> ((SysJobException) ex).getResultCode())
			.isEqualTo(DATA_NOT_EXIST);
	}

	@Test
	@DisplayName("分页：最近执行补充失败日志")
	void getPageEnrichesLastExecutionFromLog() {
		SysJobPageRowPO rowPo = new SysJobPageRowPO();
		rowPo.setId(1L);
		rowPo.setJobName("demoJob");
		IPage<SysJobPageRowPO> page = new Page<SysJobPageRowPO>().setRecords(List.of(rowPo));
		when(jobMapper.selectListByPage(any(), any(SysJobQuery.class))).thenReturn(page);
		LogJobEntity failedLog = new LogJobEntity();
		failedLog.setJobId(1L);
		failedLog.setStatus(false);
		failedLog.setCreatedAt(LocalDateTime.of(2026, 6, 13, 9, 0).toInstant(java.time.ZoneOffset.UTC));
		when(logJobMapper.selectLatestByJobIds(any())).thenReturn(List.of(failedLog));

		SysJobQuery query = new SysJobQuery();
		query.setPageIndex(1);
		query.setPageSize(10);
		var response = sysJobDefinitionService.getPage(query);

		SysJobPageVO row = response.getList().get(0);
		assertThat(row.getLastExecutionStatus()).isEqualTo(SysJobLastExecutionStatus.FAILED);
		assertThat(row.getLastExecutionTime()).isEqualTo(failedLog.getCreatedAt());
	}

	@Test
	@DisplayName("更新：清空开始/结束时间后持久化实体对应字段为 null")
	void updateClearsScheduleWindowWhenFormTimesAreNull() throws Exception {
		LocalDateTime oldStart = LocalDateTime.of(2026, 6, 1, 8, 0);
		LocalDateTime oldEnd = LocalDateTime.of(2026, 12, 31, 18, 0);
		JobEntity dbJob = new JobEntity();
		dbJob.setId(10L);
		dbJob.setJobName("demoJob");
		dbJob.setJobGroup("DEFAULT");
		dbJob.setTaskType("BEAN_INVOKE");
		dbJob.setCronExpression("0 0 12 * * ?");
		dbJob.setMisfirePolicy(2);
		dbJob.setConcurrent(false);
		dbJob.setStatus(true);
		dbJob.setStartTime(oldStart);
		dbJob.setEndTime(oldEnd);
		when(jobMapper.selectById(10L)).thenReturn(dbJob);
		doAnswer(invocation -> {
			JobEntity entity = invocation.getArgument(0);
			entity.setVersion(1L);
			return 1;
		}).when(jobMapper).updateById(any(JobEntity.class));

		SysJobUpdateForm form = new SysJobUpdateForm();
		form.setId(10L);
		form.setCronExpression("0 0 1 * * ?");
		form.setMisfirePolicy(2);
		form.setConcurrent(true);
		form.setStatus(true);
		form.setTaskType("BEAN_INVOKE");
		form.setStartTime(null);
		form.setEndTime(null);

		sysJobDefinitionService.update(form);

		ArgumentCaptor<JobEntity> entityCaptor = ArgumentCaptor.forClass(JobEntity.class);
		verify(jobMapper).updateById(entityCaptor.capture());
		JobEntity persisted = entityCaptor.getValue();
		assertThat(persisted.getStartTime()).isNull();
		assertThat(persisted.getEndTime()).isNull();
		verify(sysJobSchedulerManager).scheduleOrReplace(persisted);
	}

	@Test
	@DisplayName("立即执行：任务不存在时抛出 DATA_NOT_EXIST")
	void runOnceThrowsWhenMissing() {
		when(jobMapper.selectById(99L)).thenReturn(null);

		assertThatThrownBy(() -> sysJobDefinitionService.runOnce(99L)).isInstanceOf(SysJobException.class)
			.extracting(ex -> ((SysJobException) ex).getResultCode())
			.isEqualTo(DATA_NOT_EXIST);
	}

	@Test
	@DisplayName("立即执行：存在时 triggerOnce")
	void runOnceTriggersWhenExists() throws SchedulerException {
		JobEntity entity = new JobEntity();
		entity.setId(5L);
		entity.setJobName("demoJob");
		entity.setJobGroup("DEFAULT");
		when(jobMapper.selectById(5L)).thenReturn(entity);
		when(sysJobQuartzRuntimeReader.resolveRuntimeSnapshot("demoJob", "DEFAULT"))
			.thenReturn(SysJobQuartzRuntimeSnapshot.of(SysJobQuartzRuntimeStatus.IDLE, null));

		sysJobDefinitionService.runOnce(5L);

		verify(sysJobSchedulerManager).triggerOnce(entity);
	}

	@Test
	@DisplayName("立即执行：任务 busy 时抛出 JOB_BUSY")
	void runOnceThrowsWhenBusy() {
		JobEntity entity = new JobEntity();
		entity.setId(5L);
		entity.setJobName("demoJob");
		entity.setJobGroup("DEFAULT");
		when(jobMapper.selectById(5L)).thenReturn(entity);
		when(sysJobQuartzRuntimeReader.resolveRuntimeSnapshot("demoJob", "DEFAULT"))
			.thenReturn(SysJobQuartzRuntimeSnapshot.of(SysJobQuartzRuntimeStatus.RUNNING, null));

		assertThatThrownBy(() -> sysJobDefinitionService.runOnce(5L)).isInstanceOf(SysJobException.class)
			.extracting(ex -> ((SysJobException) ex).getResultCode())
			.isEqualTo(JOB_BUSY);
	}

	@Test
	@DisplayName("删除：移除 Quartz 与任务定义，保留执行日志")
	void deleteByIdRemovesQuartzAndJobDefinition() throws SchedulerException {
		JobEntity dbJob = new JobEntity();
		dbJob.setId(3L);
		dbJob.setJobName("demoJob");
		dbJob.setJobGroup("DEFAULT");
		when(jobMapper.selectById(3L)).thenReturn(dbJob);
		doReturn(true).when(sysJobDefinitionService).removeById(3L);

		sysJobDefinitionService.deleteById(3L);

		verify(sysJobSchedulerManager).deleteJob("demoJob", "DEFAULT");
		verify(sysJobDefinitionService).removeById(3L);
	}

}

package com.auth.service.system.file.export.worker;

import com.auth.module.file.api.model.dto.FileUploadResultDTO;
import com.auth.module.file.exception.ExportRowLimitExceededException;
import com.auth.module.file.export.executor.AsyncPagedExportExecutor;
import com.auth.module.file.export.scope.FileExportScopeSnapshots;
import com.auth.module.file.export.task.FileExportTaskArtifact;
import com.auth.module.file.export.task.FileExportTaskContext;
import com.auth.module.security.autoconfigure.pipeline.authenticate.AuthProfileSecurityContextPopulator;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.module.security.contract.api.AuthProfile;
import com.auth.module.security.contract.api.ScopeGrant;
import com.auth.module.security.contract.api.datascope.DataScopeStorageType;
import com.auth.service.system.file.config.properties.FileExportTaskProperties;
import com.auth.service.system.file.export.notify.FileExportTaskNotifier;
import com.auth.service.system.file.export.remote.RemotePageFetcher;
import com.auth.service.system.file.mapper.FileExportAttemptMapper;
import com.auth.service.system.file.mapper.FileExportTaskMapper;
import com.auth.service.system.file.model.entity.FileExportAttemptEntity;
import com.auth.service.system.file.model.entity.FileExportTaskEntity;
import com.auth.service.system.file.service.FileUploadService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link FileExportTaskWorker} 单元测试
 *
 * @author Bunny
 */
@DisplayName("FileExportTaskWorker 任务执行")
@ExtendWith(MockitoExtension.class)
class FileExportTaskWorkerTest {

	@Mock
	private FileExportTaskMapper fileExportTaskMapper;

	@Mock
	private FileExportAttemptMapper fileExportAttemptMapper;

	@Mock
	private FileUploadService fileUploadService;

	@Mock
	private RemotePageFetcher remotePageFetcher;

	@Mock
	private AsyncPagedExportExecutor asyncPagedExportExecutor;

	@Mock
	private FileExportTaskNotifier taskNotifier;

	private ThreadPoolTaskExecutor asyncExecutor;

	private CountDownLatch releaseExports;

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
		if (releaseExports != null) {
			releaseExports.countDown();
		}
		if (asyncExecutor != null) {
			ThreadPoolExecutor executor = asyncExecutor.getThreadPoolExecutor();
			asyncExecutor.shutdown();
			try {
				if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
					executor.shutdownNow();
				}
			}
			catch (InterruptedException exception) {
				executor.shutdownNow();
				Thread.currentThread().interrupt();
			}
			asyncExecutor = null;
		}
	}

	@Test
	@DisplayName("executeRound：任务成功时写入 SUCCESS 尝试记录并恢复快照上下文")
	void executeRoundWritesSuccessAttempt() {
		FileExportTaskProperties properties = newProperties();
		FileExportTaskWorker worker = newWorker(properties);

		FileExportTaskEntity task = newTask(101L);
		when(fileExportTaskMapper.selectClaimCandidateIds(any(), anyInt())).thenReturn(List.of(101L));
		when(fileExportTaskMapper.claimTask(eq(101L), anyString(), any(), any())).thenReturn(1);
		when(fileExportTaskMapper.selectById(101L)).thenReturn(task, task);
		AtomicReference<AuthProfile> profileDuringExport = new AtomicReference<>();
		AtomicReference<Path> artifactPath = new AtomicReference<>();
		when(asyncPagedExportExecutor.export(any(FileExportTaskContext.class), any(), any())).thenAnswer(invocation -> {
			profileDuringExport.set(SecurityUserUtils.currentAuthProfile());
			Path tempPath = writeArtifactFile();
			artifactPath.set(tempPath);
			return FileExportTaskArtifact.builder()
				.fileName("file_export.xlsx")
				.contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
				.filePath(tempPath)
				.rowCount(12)
				.build();
		});
		FileUploadResultDTO uploadResult = new FileUploadResultDTO();
		uploadResult.setId(7001L);
		when(fileUploadService.upload(any())).thenReturn(uploadResult);

		worker.executeRound();

		String workerId = capturedClaimWorkerId();
		verify(fileExportTaskMapper).markSuccess(eq(101L), eq(workerId), eq(7001L), any(), any());
		verify(fileExportTaskMapper, never()).markFailed(anyLong(), anyString(), anyString(), anyString(), any());
		verify(fileExportTaskMapper, never()).markCancelled(anyLong(), anyString(), any());
		verify(fileExportTaskMapper, never()).markRetryPending(anyLong(), anyString(), anyString(), anyString(), any(),
				any());

		ArgumentCaptor<FileExportAttemptEntity> captor = ArgumentCaptor.forClass(FileExportAttemptEntity.class);
		verify(fileExportAttemptMapper).insert(captor.capture());
		assertThat(captor.getValue().getResult()).isEqualTo("SUCCESS");
		assertThat(captor.getValue().getWorkerId()).isEqualTo(workerId);
		assertThat(captor.getValue().getProcessedRows()).isEqualTo(12L);
		assertThat(profileDuringExport.get()).isNotNull();
		assertThat(profileDuringExport.get().getUserId()).isEqualTo(9001L);
		assertThat(profileDuringExport.get().getDeptScope()).isNotNull();
		assertThat(profileDuringExport.get().getDeptScope().getScopeType())
			.isEqualTo(DataScopeStorageType.DEPT_AND_CHILD);
		assertThat(SecurityUserUtils.currentAuthProfile()).isNull();
		assertThat(artifactPath.get()).isNotNull();
		assertThat(Files.exists(artifactPath.get())).isFalse();
		verify(taskNotifier).notifySuccess(task, "file_export.xlsx");
		verify(taskNotifier, never()).notifyTerminalFailure(any(), any());
	}

	@Test
	@DisplayName("executeRound：任务失败且可重试时回退为 PENDING")
	void executeRoundMovesToPendingWhenRetryableFailure() {
		FileExportTaskProperties properties = newProperties();
		FileExportTaskWorker worker = newWorker(properties);

		FileExportTaskEntity task = newTask(102L);
		task.setRetryCount(0);
		task.setMaxRetry(3);
		when(fileExportTaskMapper.selectClaimCandidateIds(any(), anyInt())).thenReturn(List.of(102L));
		when(fileExportTaskMapper.claimTask(eq(102L), anyString(), any(), any())).thenReturn(1);
		when(fileExportTaskMapper.selectById(102L)).thenReturn(task, task);
		when(asyncPagedExportExecutor.export(any(FileExportTaskContext.class), any(), any()))
			.thenThrow(new IllegalStateException("boom"));
		when(fileExportTaskMapper.markRetryPending(eq(102L), anyString(), eq("IllegalStateException"), eq("boom"),
				any(), any()))
			.thenReturn(1);

		worker.executeRound();

		verify(fileExportTaskMapper).markRetryPending(eq(102L), anyString(), eq("IllegalStateException"), eq("boom"),
				any(), any());
		verify(fileExportTaskMapper, never()).markFailed(anyLong(), anyString(), anyString(), anyString(), any());
		verify(fileExportTaskMapper, never()).markCancelled(anyLong(), anyString(), any());
		assertThat(SecurityUserUtils.currentAuthProfile()).isNull();

		ArgumentCaptor<FileExportAttemptEntity> captor = ArgumentCaptor.forClass(FileExportAttemptEntity.class);
		verify(fileExportAttemptMapper).insert(captor.capture());
		assertThat(captor.getValue().getResult()).isEqualTo("FAILED");
		verify(taskNotifier, never()).notifySuccess(any(), any());
		verify(taskNotifier, never()).notifyTerminalFailure(any(), any());
	}

	@Test
	@DisplayName("executeRound：导出校验失败时标记 FAILED 且不自动重试")
	void executeRoundMarksFailedWhenExportValidationException() {
		FileExportTaskProperties properties = newProperties();
		FileExportTaskWorker worker = newWorker(properties);

		FileExportTaskEntity runningTask = newTask(106L);
		runningTask.setRetryCount(0);
		runningTask.setMaxRetry(3);
		FileExportTaskEntity failedTask = newTask(106L);
		failedTask.setRetryCount(0);
		failedTask.setMaxRetry(3);
		failedTask.setStatus("FAILED");
		when(fileExportTaskMapper.selectClaimCandidateIds(any(), anyInt())).thenReturn(List.of(106L));
		when(fileExportTaskMapper.claimTask(eq(106L), anyString(), any(), any())).thenReturn(1);
		when(fileExportTaskMapper.selectById(106L)).thenReturn(runningTask, failedTask);
		when(asyncPagedExportExecutor.export(any(FileExportTaskContext.class), any(), any()))
			.thenThrow(new ExportRowLimitExceededException(100L, 101L));

		worker.executeRound();

		verify(fileExportTaskMapper).markFailed(eq(106L), anyString(), eq("ExportRowLimitExceededException"),
				anyString(), any());
		verify(fileExportTaskMapper, never()).markRetryPending(anyLong(), anyString(), anyString(), anyString(), any(),
				any());
		assertThat(SecurityUserUtils.currentAuthProfile()).isNull();
		verify(taskNotifier).notifyTerminalFailure(eq(failedTask), anyString());
		verify(taskNotifier, never()).notifySuccess(any(), any());
	}

	@Test
	@DisplayName("executeRound：非法权限快照且重试耗尽时标记 FAILED")
	void executeRoundMarksFailedWhenScopeSnapshotInvalid() {
		FileExportTaskProperties properties = newProperties();
		FileExportTaskWorker worker = newWorker(properties);

		FileExportTaskEntity runningTask = newTask(104L);
		runningTask.setScopeSnapshotJson("{\"permVersion\":1}");
		runningTask.setRetryCount(3);
		runningTask.setMaxRetry(3);
		FileExportTaskEntity failedTask = newTask(104L);
		failedTask.setScopeSnapshotJson("{\"permVersion\":1}");
		failedTask.setRetryCount(3);
		failedTask.setMaxRetry(3);
		failedTask.setStatus("FAILED");
		when(fileExportTaskMapper.selectClaimCandidateIds(any(), anyInt())).thenReturn(List.of(104L));
		when(fileExportTaskMapper.claimTask(eq(104L), anyString(), any(), any())).thenReturn(1);
		when(fileExportTaskMapper.selectById(104L)).thenReturn(runningTask, failedTask);

		worker.executeRound();

		verify(asyncPagedExportExecutor, never()).export(any(), any(), any());
		verify(fileExportTaskMapper).markFailed(eq(104L), anyString(), eq("IllegalArgumentException"),
				eq("scopeSnapshotJson must contain valid userId"), any());
		verify(fileExportTaskMapper, never()).markRetryPending(anyLong(), anyString(), anyString(), anyString(), any(),
				any());
		assertThat(SecurityUserUtils.currentAuthProfile()).isNull();
		verify(taskNotifier).notifyTerminalFailure(eq(failedTask), anyString());
	}

	@Test
	@DisplayName("executeRound：执行前已收到取消请求时标记 CANCELLED")
	void executeRoundMarksCancelledBeforeExportRun() {
		FileExportTaskProperties properties = newProperties();
		FileExportTaskWorker worker = newWorker(properties);

		FileExportTaskEntity runningTask = newTask(103L);
		FileExportTaskEntity cancelledTask = newTask(103L);
		cancelledTask.setStatus("CANCELLING");
		when(fileExportTaskMapper.selectClaimCandidateIds(any(), anyInt())).thenReturn(List.of(103L));
		when(fileExportTaskMapper.claimTask(eq(103L), anyString(), any(), any())).thenReturn(1);
		when(fileExportTaskMapper.selectById(103L)).thenReturn(runningTask, cancelledTask, cancelledTask);

		worker.executeRound();

		verify(fileExportTaskMapper).markCancelled(eq(103L), anyString(), any());
		verify(asyncPagedExportExecutor, never()).export(any(), any(), any());
		verify(fileUploadService, never()).upload(any());

		ArgumentCaptor<FileExportAttemptEntity> captor = ArgumentCaptor.forClass(FileExportAttemptEntity.class);
		verify(fileExportAttemptMapper).insert(captor.capture());
		assertThat(captor.getValue().getResult()).isEqualTo("CANCELLED");
		verify(taskNotifier, never()).notifySuccess(any(), any());
		verify(taskNotifier, never()).notifyTerminalFailure(any(), any());
	}

	@Test
	@DisplayName("executeRound：attemptNo 由 retryCount+1 推导")
	void executeRoundUsesRetryCountPlusOneAsAttemptNo() {
		FileExportTaskProperties properties = newProperties();
		FileExportTaskWorker worker = newWorker(properties);

		FileExportTaskEntity task = newTask(105L);
		task.setRetryCount(2);
		when(fileExportTaskMapper.selectClaimCandidateIds(any(), anyInt())).thenReturn(List.of(105L));
		when(fileExportTaskMapper.claimTask(eq(105L), anyString(), any(), any())).thenReturn(1);
		when(fileExportTaskMapper.selectById(105L)).thenReturn(task, task);
		when(asyncPagedExportExecutor.export(any(FileExportTaskContext.class), any(), any()))
			.thenReturn(FileExportTaskArtifact.builder()
				.fileName("file_export.xlsx")
				.contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
				.filePath(writeArtifactFile())
				.rowCount(9)
				.build());
		FileUploadResultDTO uploadResult = new FileUploadResultDTO();
		uploadResult.setId(7002L);
		when(fileUploadService.upload(any())).thenReturn(uploadResult);

		worker.executeRound();

		ArgumentCaptor<FileExportAttemptEntity> captor = ArgumentCaptor.forClass(FileExportAttemptEntity.class);
		verify(fileExportAttemptMapper).insert(captor.capture());
		assertThat(captor.getValue().getAttemptNo()).isEqualTo(3);
		assertThat(captor.getValue().getResult()).isEqualTo("SUCCESS");
	}

	@Test
	@DisplayName("executeRound：提交后立即返回，导出在线程池内完成")
	void executeRoundReturnsBeforeExportFinishes() throws InterruptedException {
		CountDownLatch started = new CountDownLatch(1);
		releaseExports = new CountDownLatch(1);
		stubBlockedExport(started);
		when(fileExportTaskMapper.selectClaimCandidateIds(any(), anyInt())).thenReturn(List.of(201L));
		when(fileExportTaskMapper.claimTask(eq(201L), anyString(), any(), any())).thenReturn(1);
		when(fileExportTaskMapper.selectById(201L)).thenReturn(newTask(201L));
		when(fileUploadService.upload(any())).thenReturn(uploadResult(7201L));

		asyncExecutor = newExportPool(1);
		FileExportTaskWorker worker = newWorker(newProperties(), asyncExecutor);

		worker.executeRound();

		assertThat(started.await(5, TimeUnit.SECONDS)).isTrue();
		verify(fileExportTaskMapper, never()).markSuccess(anyLong(), anyString(), anyLong(), any(), any());
		releaseExports.countDown();
		verify(fileExportTaskMapper, timeout(5_000)).markSuccess(eq(201L), anyString(), eq(7201L), any(), any());
	}

	@Test
	@DisplayName("executeRound：并发上限内两条任务可同时进入写出")
	void executeRoundRunsTwoTasksOverlap() throws InterruptedException {
		CountDownLatch entered = new CountDownLatch(2);
		releaseExports = new CountDownLatch(1);
		when(asyncPagedExportExecutor.export(any(FileExportTaskContext.class), any(), any())).thenAnswer(invocation -> {
			entered.countDown();
			awaitRelease();
			return artifactFrom(writeArtifactFile(), 3);
		});
		when(fileExportTaskMapper.selectClaimCandidateIds(any(), anyInt())).thenReturn(List.of(301L, 302L));
		when(fileExportTaskMapper.claimTask(anyLong(), anyString(), any(), any())).thenReturn(1);
		when(fileExportTaskMapper.selectById(301L)).thenReturn(newTask(301L));
		when(fileExportTaskMapper.selectById(302L)).thenReturn(newTask(302L));
		when(fileUploadService.upload(any())).thenReturn(uploadResult(7301L));

		FileExportTaskProperties properties = newProperties();
		properties.setMaxConcurrency(2);
		asyncExecutor = newExportPool(2);
		FileExportTaskWorker worker = newWorker(properties, asyncExecutor);

		worker.executeRound();

		assertThat(entered.await(5, TimeUnit.SECONDS)).isTrue();
		releaseExports.countDown();
		verify(fileExportTaskMapper, timeout(5_000).times(2)).markSuccess(anyLong(), anyString(), anyLong(), any(),
				any());
	}

	@Test
	@DisplayName("executeRound：并发槽占满时本轮不再领取")
	void executeRoundSkipsClaimWhenSlotsFull() throws InterruptedException {
		CountDownLatch started = new CountDownLatch(1);
		releaseExports = new CountDownLatch(1);
		stubBlockedExport(started);
		when(fileExportTaskMapper.selectClaimCandidateIds(any(), anyInt())).thenReturn(List.of(401L));
		when(fileExportTaskMapper.claimTask(eq(401L), anyString(), any(), any())).thenReturn(1);
		when(fileExportTaskMapper.selectById(401L)).thenReturn(newTask(401L));
		when(fileUploadService.upload(any())).thenReturn(uploadResult(7401L));

		FileExportTaskProperties properties = newProperties();
		properties.setMaxConcurrency(1);
		asyncExecutor = newExportPool(1);
		FileExportTaskWorker worker = newWorker(properties, asyncExecutor);

		worker.executeRound();
		assertThat(started.await(5, TimeUnit.SECONDS)).isTrue();
		worker.executeRound();

		verify(fileExportTaskMapper, times(1)).selectClaimCandidateIds(any(), anyInt());
		verify(fileExportTaskMapper, times(1)).claimTask(anyLong(), anyString(), any(), any());
		releaseExports.countDown();
		verify(fileExportTaskMapper, timeout(5_000)).markSuccess(eq(401L), anyString(), eq(7401L), any(), any());
	}

	private FileExportTaskWorker newWorker(FileExportTaskProperties properties) {
		return newWorker(properties, Runnable::run);
	}

	private FileExportTaskWorker newWorker(FileExportTaskProperties properties, Executor executor) {
		AuthProfileSecurityContextPopulator populator = new AuthProfileSecurityContextPopulator();
		FileExportTaskRunner runner = new FileExportTaskRunner(fileExportTaskMapper, fileUploadService,
				remotePageFetcher, asyncPagedExportExecutor, properties, populator);
		FileExportTaskFailureHandler failureHandler = new FileExportTaskFailureHandler(fileExportTaskMapper,
				properties);
		FileExportAttemptRecorder attemptRecorder = new FileExportAttemptRecorder(fileExportAttemptMapper);
		return new FileExportTaskWorker(fileExportTaskMapper, properties, runner, failureHandler, attemptRecorder,
				taskNotifier, executor);
	}

	private FileExportTaskProperties newProperties() {
		FileExportTaskProperties properties = new FileExportTaskProperties();
		properties.setClaimBatchSize(5);
		properties.setMaxConcurrency(4);
		properties.setLeaseSeconds(60);
		properties.setArtifactExpireDays(7);
		properties.setRetryBaseDelaySeconds(30);
		return properties;
	}

	private String capturedClaimWorkerId() {
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(fileExportTaskMapper).claimTask(eq((Long) 101L), captor.capture(), any(), any());
		assertThat(captor.getValue()).startsWith("fe-");
		return captor.getValue();
	}

	private void stubBlockedExport(CountDownLatch started) {
		when(asyncPagedExportExecutor.export(any(FileExportTaskContext.class), any(), any())).thenAnswer(invocation -> {
			started.countDown();
			awaitRelease();
			return artifactFrom(writeArtifactFile(), 1);
		});
	}

	private void awaitRelease() {
		try {
			assertThat(releaseExports.await(5, TimeUnit.SECONDS)).isTrue();
		}
		catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while holding export", exception);
		}
	}

	private ThreadPoolTaskExecutor newExportPool(int size) {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(size);
		executor.setMaxPoolSize(size);
		executor.setQueueCapacity(0);
		executor.setThreadNamePrefix("file-export-test-");
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
		executor.initialize();
		return executor;
	}

	private FileUploadResultDTO uploadResult(Long fileRecordId) {
		FileUploadResultDTO uploadResult = new FileUploadResultDTO();
		uploadResult.setId(fileRecordId);
		return uploadResult;
	}

	private FileExportTaskArtifact artifactFrom(Path filePath, long rowCount) {
		return FileExportTaskArtifact.builder()
			.fileName("file_export.xlsx")
			.contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
			.filePath(filePath)
			.rowCount(rowCount)
			.build();
	}

	private FileExportTaskEntity newTask(Long id) {
		FileExportTaskEntity task = new FileExportTaskEntity();
		task.setId(id);
		task.setBizType("FILE_RECORD");
		task.setFetchService("service-system");
		task.setFetchPath("/api/system/inner/export/page");
		task.setQuerySnapshotJson("{}");
		task.setColumnSnapshotJson("[{\"fieldKey\":\"id\",\"header\":\"主键ID\"}]");
		task.setCreatedBy(9001L);
		task.setCreatedAt(Instant.parse("2026-08-07T00:00:00Z"));
		task.setRetryCount(0);
		task.setMaxRetry(3);
		task.setStatus("RUNNING");
		task.setScopeSnapshotJson(FileExportScopeSnapshots.toJson(AuthProfile.builder()
			.userId(9001L)
			.deptScope(
					ScopeGrant.builder().scopeType(DataScopeStorageType.DEPT_AND_CHILD).values(List.of(110L)).build())
			.permVersion(1L)
			.build()));
		return task;
	}

	private Path writeArtifactFile() {
		try {
			Path file = Files.createTempFile("file-export-task-worker-test-", ".xlsx");
			Files.write(file, new byte[] { 1, 2, 3 });
			return file;
		}
		catch (Exception exception) {
			throw new IllegalStateException("Failed to build test artifact", exception);
		}
	}

}

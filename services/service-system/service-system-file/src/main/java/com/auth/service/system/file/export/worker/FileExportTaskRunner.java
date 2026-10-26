package com.auth.service.system.file.export.worker;

import com.auth.common.core.constants.BatchSizes;
import com.auth.module.file.api.model.dto.FileUploadResultDTO;
import com.auth.module.file.api.policy.FileBizType;
import com.auth.module.file.export.datasource.PageDataSource;
import com.auth.module.file.export.executor.AsyncPagedExportExecutor;
import com.auth.module.file.export.scope.FileExportScopeSnapshots;
import com.auth.module.file.export.task.FileExportTaskArtifact;
import com.auth.module.file.export.task.FileExportTaskContext;
import com.auth.module.file.export.task.FileExportTaskProgressListener;
import com.auth.module.security.autoconfigure.pipeline.authenticate.AuthProfileSecurityContextPopulator;
import com.auth.module.security.contract.api.AuthProfile;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.file.config.properties.FileExportTaskProperties;
import com.auth.service.system.file.export.datasource.FileRemotePageDataSource;
import com.auth.service.system.file.export.listener.DatabaseProgressListener;
import com.auth.service.system.file.export.remote.RemotePageFetcher;
import com.auth.service.system.file.mapper.FileExportTaskMapper;
import com.auth.service.system.file.model.entity.FileExportTaskEntity;
import com.auth.service.system.file.model.enums.FileExportAttemptResult;
import com.auth.service.system.file.model.form.FileUploadForm;
import com.auth.service.system.file.model.value.PathMultipartFile;
import com.auth.service.system.file.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * 导出任务执行：恢复权限快照、分页写出、上传产物
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class FileExportTaskRunner {

	private final FileExportTaskMapper fileExportTaskMapper;

	private final FileUploadService fileUploadService;

	private final RemotePageFetcher remotePageFetcher;

	private final AsyncPagedExportExecutor asyncPagedExportExecutor;

	private final FileExportTaskProperties properties;

	private final AuthProfileSecurityContextPopulator securityContextPopulator;

	/**
	 * 执行已认领任务（含取消短路与成功落库）
	 * @param task 任务实体
	 * @param workerId 本次执行的租约持有者标识
	 * @return 执行结果
	 */
	public FileExportTaskRunResult run(FileExportTaskEntity task, String workerId) {
		Long taskId = task.getId();
		DatabaseProgressListener progressListener = new DatabaseProgressListener(fileExportTaskMapper, properties,
				taskId, workerId);
		if (progressListener.isCancelRequested()) {
			fileExportTaskMapper.markCancelled(taskId, workerId, Instant.now());
			return FileExportTaskRunResult.builder().attemptResult(FileExportAttemptResult.CANCELLED).build();
		}

		AuthProfile profile = resolveProfile(task);
		return securityContextPopulator.runWithProfile(profile,
				() -> exportAndUpload(task, progressListener, workerId));
	}

	/**
	 * 解析权限快照
	 * @param task 任务实体
	 * @return 权限快照
	 */
	private AuthProfile resolveProfile(FileExportTaskEntity task) {
		AuthProfile profile = FileExportScopeSnapshots.requireAuthProfile(task.getScopeSnapshotJson());
		if (task.getCreatedBy() != null && !task.getCreatedBy().equals(profile.getUserId())) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_INVALID, "scopeSnapshotUser");
		}
		return profile;
	}

	/**
	 * 导出并上传产物
	 * @param task 任务实体
	 * @param progressListener 进度监听器
	 * @param workerId 本次执行的租约持有者标识
	 * @return 执行结果
	 */
	private FileExportTaskRunResult exportAndUpload(FileExportTaskEntity task,
			DatabaseProgressListener progressListener, String workerId) {
		Long taskId = task.getId();
		FileExportTaskContext context = buildContext(task);
		FileExportTaskArtifact artifact = exportArtifact(context, progressListener);
		long processedRows = artifact.getRowCount();
		if (progressListener.isCancelRequested()) {
			fileExportTaskMapper.markCancelled(taskId, workerId, Instant.now());
			return FileExportTaskRunResult.builder()
				.attemptResult(FileExportAttemptResult.CANCELLED)
				.processedRows(processedRows)
				.artifact(artifact)
				.build();
		}

		Long fileRecordId = uploadArtifact(task, artifact);
		Instant expireAt = Instant.now().plus(properties.getArtifactExpireDays(), ChronoUnit.DAYS);
		fileExportTaskMapper.markSuccess(taskId, workerId, fileRecordId, expireAt, Instant.now());
		return FileExportTaskRunResult.builder()
			.attemptResult(FileExportAttemptResult.SUCCESS)
			.processedRows(processedRows)
			.artifact(artifact)
			.build();
	}

	/**
	 * 组装导出上下文
	 * @param task 任务实体
	 * @return 导出上下文
	 */
	private FileExportTaskContext buildContext(FileExportTaskEntity task) {
		return FileExportTaskContext.builder()
			.taskId(task.getId())
			.bizType(task.getBizType())
			.exportFormat(task.getExportFormat())
			.querySnapshotJson(task.getQuerySnapshotJson())
			.columnSnapshotJson(task.getColumnSnapshotJson())
			.scopeSnapshotJson(task.getScopeSnapshotJson())
			.fetchService(task.getFetchService())
			.fetchPath(task.getFetchPath())
			.createdBy(task.getCreatedBy())
			.createdAt(task.getCreatedAt())
			.fetchPageSize(BatchSizes.SIZE_1000)
			.maxRows(properties.getMaxRows())
			.shardRows(properties.getShardRows())
			.build();
	}

	/**
	 * 按任务上下文分页写出产物
	 * @param context 导出上下文
	 * @param progressListener 进度监听器
	 * @return 导出产物
	 */
	private FileExportTaskArtifact exportArtifact(FileExportTaskContext context,
			FileExportTaskProgressListener progressListener) {
		PageDataSource dataSource = new FileRemotePageDataSource(remotePageFetcher, context.getFetchService(),
				context.getFetchPath(), context.getBizType(), context.getQuerySnapshotJson(),
				context.getColumnSnapshotJson(), context.getScopeSnapshotJson());
		return asyncPagedExportExecutor.export(context, dataSource, progressListener);
	}

	/**
	 * 上传产物
	 * @param task 任务实体
	 * @param artifact 产物
	 * @return 文件记录ID
	 */
	private Long uploadArtifact(FileExportTaskEntity task, FileExportTaskArtifact artifact) {
		Path tempFile = artifact.getFilePath();
		if (tempFile == null || !Files.exists(tempFile)) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_UNAVAILABLE, "exportArtifact");
		}
		PathMultipartFile file = new PathMultipartFile("file", artifact.getFileName(), artifact.getContentType(),
				tempFile);
		FileUploadForm form = new FileUploadForm();
		form.setFile(file);
		form.setBizType(FileBizType.INTERNAL_EXPORT.getCode());
		form.setBizId(String.valueOf(task.getId()));
		form.setIsPrivate(true);
		form.setRemark("async-export-task");

		FileUploadResultDTO result = fileUploadService.upload(form);
		Long fileRecordId = result.getId();
		if (fileRecordId == null) {
			throw new SystemBusinessException(SystemCommonResultCode.OPERATION_FAILED, "fileRecordId");
		}
		return fileRecordId;
	}

}

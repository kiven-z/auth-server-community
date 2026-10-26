package com.auth.service.system.file.export.port;

import com.auth.module.file.api.model.dto.ExportPreCheckResult;
import com.auth.module.file.api.model.dto.FileExportTaskCreateResult;
import com.auth.module.file.api.model.request.ExportPreCheckRequest;
import com.auth.module.file.api.model.request.FileExportTaskCreateRequest;
import com.auth.module.file.api.port.FileExportTaskOperations;
import com.auth.module.file.export.scope.FileExportScopeSnapshots;
import com.auth.module.file.export.shard.ExportShardPolicy;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.module.security.contract.api.AuthProfile;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.file.config.properties.FileExportTaskProperties;
import com.auth.service.system.file.export.remote.RemotePageFetcher;
import com.auth.service.system.file.export.task.FileExportTaskAssembler;
import com.auth.service.system.file.export.task.FileExportTaskNormalizedCreateContext;
import com.auth.service.system.file.mapper.FileExportTaskMapper;
import com.auth.service.system.file.model.entity.FileExportTaskEntity;
import com.auth.service.system.file.model.enums.FileExportTaskStatus;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 进程内导出任务 Port 实现：前置检查与任务创建
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class LocalFileExportTaskOperations implements FileExportTaskOperations {

	private final RemotePageFetcher remotePageFetcher;

	private final FileExportTaskProperties properties;

	private final FileExportTaskMapper fileExportTaskMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExportPreCheckResult preCheck(ExportPreCheckRequest request) {
		AuthProfile profile = SecurityUserUtils.currentAuthProfile();
		if (profile == null || profile.getUserId() == null) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_INVALID, "authProfile");
		}

		String scopeSnapshotJson = FileExportScopeSnapshots.toJson(profile);
		String fetchService = request.getFetchService();
		String fetchPath = request.getFetchPath();
		String bizType = request.getBizType();
		String querySnapshotJson = request.getQuerySnapshotJson();
		IPage<Map<String, Object>> page = remotePageFetcher.fetchPage(fetchService, fetchPath, bizType,
				querySnapshotJson, scopeSnapshotJson, 1, 1);

		ExportShardPolicy policy = ExportShardPolicy.of(properties.getMaxRows(), properties.getShardRows());
		long totalRows = page.getTotal();
		long maxRows = policy.maxRows();
		if (totalRows > maxRows) {
			return ExportPreCheckResult.exceeded(totalRows, maxRows);
		}
		if (policy.exceedsShardThreshold(totalRows)) {
			return ExportPreCheckResult.sharded(totalRows, maxRows, policy.shardCount(totalRows));
		}
		return ExportPreCheckResult.normal(totalRows, maxRows);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public FileExportTaskCreateResult createTask(FileExportTaskCreateRequest request) {
		FileExportTaskNormalizedCreateContext createContext = FileExportTaskNormalizedCreateContext.from(request);
		Long createdBy = request.getCreatedBy();
		String requestId = createContext.getRequestId();

		if (requestId != null) {
			// 查询幂等重复任务
			FileExportTaskEntity duplicated = new LambdaQueryChainWrapper<>(fileExportTaskMapper)
				.eq(FileExportTaskEntity::getCreatedBy, createdBy)
				.eq(FileExportTaskEntity::getRequestId, requestId)
				.last("LIMIT 1")
				.one();
			if (duplicated != null) {
				FileExportTaskCreateResult result = new FileExportTaskCreateResult();
				result.setTaskId(duplicated.getId());
				return result;
			}
		}

		long inProgressCount = new LambdaQueryChainWrapper<>(fileExportTaskMapper)
			.eq(FileExportTaskEntity::getCreatedBy, createdBy)
			.in(FileExportTaskEntity::getStatus, FileExportTaskStatus.PENDING.name(),
					FileExportTaskStatus.RUNNING.name(), FileExportTaskStatus.CANCELLING.name())
			.count();
		if (inProgressCount >= properties.getMaxInProgressPerUser()) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_INVALID, "inProgressLimit");
		}

		FileExportTaskEntity entity = FileExportTaskAssembler.fromCreateRequest(request, createContext);
		fileExportTaskMapper.insert(entity);
		FileExportTaskCreateResult result = new FileExportTaskCreateResult();
		result.setTaskId(entity.getId());
		return result;
	}

}

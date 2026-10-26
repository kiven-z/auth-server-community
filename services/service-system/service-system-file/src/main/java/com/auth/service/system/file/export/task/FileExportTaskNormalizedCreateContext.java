package com.auth.service.system.file.export.task;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.core.utils.JsonSupport;
import com.auth.module.file.api.model.enums.FileExportFormat;
import com.auth.module.file.api.model.request.FileExportTaskCreateRequest;
import com.auth.module.file.export.definition.ExportColumns;
import com.auth.module.file.export.scope.FileExportScopeSnapshots;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.module.security.contract.api.AuthProfile;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import lombok.Builder;
import lombok.Value;

import java.util.Locale;

/**
 * 创建任务时已归一化的快照与路由
 *
 * @author Bunny
 */
@Value
@Builder
public class FileExportTaskNormalizedCreateContext {

	/**
	 * 幂等请求ID
	 */
	String requestId;

	/**
	 * 归一化业务类型
	 */
	String bizType;

	/**
	 * 查询快照
	 */
	String querySnapshotJson;

	/**
	 * 列快照
	 */
	String columnSnapshotJson;

	/**
	 * 权限快照
	 */
	String scopeSnapshotJson;

	/**
	 * 取数服务名
	 */
	String fetchService;

	/**
	 * 取数路径
	 */
	String fetchPath;

	/**
	 * 由创建请求做格式校验与字段归一化
	 * @param request 创建请求
	 * @return 归一化上下文
	 */
	public static FileExportTaskNormalizedCreateContext from(FileExportTaskCreateRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("request");
		}

		try {
			String exportFormat = request.getExportFormat();
			FileExportFormat.valueOf(exportFormat.trim().toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException exception) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_INVALID, "exportFormat");
		}

		Long createdBy = request.getCreatedBy();
		AuthProfile profile = SecurityUserUtils.currentAuthProfile();
		if (profile == null || profile.getUserId() == null) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_INVALID, "authProfile");
		}
		if (!profile.getUserId().equals(createdBy)) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_INVALID, "createdBy");
		}
		return FileExportTaskNormalizedCreateContext.builder()
			.requestId(CharSequenceUtil.trimToNull(request.getRequestId()))
			.bizType(request.getBizType().trim().toUpperCase(Locale.ROOT))
			.querySnapshotJson(normalizeQuerySnapshotJson(request.getQuerySnapshotJson()))
			.columnSnapshotJson(normalizeColumnSnapshotJson(request.getColumnSnapshotJson()))
			.scopeSnapshotJson(FileExportScopeSnapshots.toJson(profile))
			.fetchService(request.getFetchService().trim())
			.fetchPath(normalizeFetchPath(request.getFetchPath()))
			.build();
	}

	private static String normalizeQuerySnapshotJson(String querySnapshotJson) {
		try {
			return JsonSupport.toJson(JsonSupport.readObjectTree(querySnapshotJson));
		}
		catch (IllegalArgumentException exception) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_INVALID, "querySnapshotJson");
		}
	}

	private static String normalizeColumnSnapshotJson(String columnSnapshotJson) {
		try {
			return ExportColumns.toSnapshotJson(ExportColumns.fromSnapshotJson(columnSnapshotJson));
		}
		catch (RuntimeException exception) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_INVALID, "columnSnapshotJson");
		}
	}

	private static String normalizeFetchPath(String fetchPath) {
		String trimmed = fetchPath.trim();
		if (!trimmed.startsWith("/")) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_INVALID, "fetchPath");
		}
		return trimmed;
	}

}

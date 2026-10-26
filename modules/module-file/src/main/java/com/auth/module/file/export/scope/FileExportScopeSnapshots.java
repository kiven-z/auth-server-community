package com.auth.module.file.export.scope;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.core.utils.JsonSupport;
import com.auth.module.security.contract.profile.AuthProfile;
import lombok.experimental.UtilityClass;

/**
 * 异步导出数据权限快照编解码
 *
 * @author Bunny
 */
@UtilityClass
public class FileExportScopeSnapshots {

	/**
	 * 由当前认证画像生成快照 JSON
	 * @param profile 认证画像
	 * @return JSON
	 */
	public static String toJson(AuthProfile profile) {
		if (profile == null || profile.getUserId() == null) {
			throw new IllegalArgumentException("authProfile is required");
		}

		FileExportScopeSnapshot snapshot = FileExportScopeSnapshot.builder()
			.userId(profile.getUserId())
			.deptScope(profile.getDeptScope())
			.permVersion(profile.getPermVersion())
			.build();
		return JsonSupport.toJson(snapshot);
	}

	/**
	 * 解析快照并还原为最小认证画像（供 Worker 恢复 SecurityContext）
	 * @param scopeSnapshotJson 快照 JSON
	 * @return 认证画像
	 */
	public static AuthProfile requireAuthProfile(String scopeSnapshotJson) {
		if (CharSequenceUtil.isBlank(scopeSnapshotJson)) {
			throw new IllegalArgumentException("scopeSnapshotJson is required");
		}

		FileExportScopeSnapshot snapshot;
		try {
			snapshot = JsonSupport.fromJson(scopeSnapshotJson, FileExportScopeSnapshot.class);
		}
		catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Invalid scopeSnapshotJson format", exception);
		}
		if (snapshot == null || snapshot.getUserId() == null) {
			throw new IllegalArgumentException("scopeSnapshotJson must contain valid userId");
		}

		return AuthProfile.builder()
			.userId(snapshot.getUserId())
			.deptScope(snapshot.getDeptScope())
			.permVersion(snapshot.getPermVersion())
			.build();
	}

}

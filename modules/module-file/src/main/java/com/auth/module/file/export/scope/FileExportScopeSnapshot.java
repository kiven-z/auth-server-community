package com.auth.module.file.export.scope;

import com.auth.module.security.contract.profile.ScopeGrant;
import com.auth.module.security.contract.profile.datascope.DataScopeDimension;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 异步导出数据权限快照（提交时冻结）
 *
 * @author Bunny
 */
@Value
@Builder
@Jacksonized
public class FileExportScopeSnapshot implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 提交用户 ID
	 */
	Long userId;

	/**
	 * 按数据维索引的范围（提交时冻结）
	 */
	@Builder.Default
	Map<DataScopeDimension, ScopeGrant> dataScopes = Map.of();

	/**
	 * 权限画像版本号
	 */
	Long permVersion;

}

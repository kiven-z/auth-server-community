package com.auth.module.file.export.scope;

import com.auth.module.security.contract.api.ScopeGrant;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.io.Serial;
import java.io.Serializable;

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
	 * 部门维数据范围
	 */
	ScopeGrant deptScope;

	/**
	 * 权限画像版本号
	 */
	Long permVersion;

}

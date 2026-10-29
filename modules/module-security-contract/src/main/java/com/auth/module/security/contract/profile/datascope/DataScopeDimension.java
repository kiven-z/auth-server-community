package com.auth.module.security.contract.profile.datascope;

/**
 * 数据权限维度
 *
 * @author Bunny
 */
public enum DataScopeDimension {

	/**
	 * 部门维（树形结构，支持 IDS_AND_CHILD 展开）
	 */
	DEPT;

	/**
	 * 是否为树形结构维度（决定 IDS_AND_CHILD 是否需要闭包展开）
	 * @return 树形维度为 true
	 */
	public boolean isTree() {
		return switch (this) {
			case DEPT -> true;
			// 示例
			// case REGION -> false;
			default -> false;
		};
	}

}

package com.auth.module.file.api.model.enums;

/**
 * 文件删除后在回收站的可见性策略
 *
 * @author Bunny
 */
public enum RecycleVisibility {

	/**
	 * 用户个人回收站可见，可恢复与彻底删除。
	 */
	USER,

	/**
	 * 仅管理端回收站可见。
	 */
	ADMIN_ONLY,

	/**
	 * 回收站不可见，仅保留审计信息。
	 */
	HIDDEN

}

package com.auth.module.security.datapermission.handler;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.datapermission.annotation.DataScope;

/**
 * 构建数据范围 SQL 条件（不包含 WHERE 关键字）
 *
 * <p>
 * 实现必须安全且可移植用于 MySQL/PostgreSQL
 * </p>
 *
 * @author Bunny
 */
public interface DataScopeHandler {

	/**
	 * 构建当前用户的 SQL 条件
	 * @param profile 当前用户快照
	 * @param dataScope 注解元数据
	 * @return SQL 条件（不包含 WHERE）或 null 当不需要过滤时
	 */
	String buildCondition(AuthProfile profile, DataScope dataScope);

}

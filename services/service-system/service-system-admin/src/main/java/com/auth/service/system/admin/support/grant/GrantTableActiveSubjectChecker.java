package com.auth.service.system.admin.support.grant;

import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;

/**
 * grant_table 授权前校验某一主体类型存在且启用。
 *
 * @author Bunny
 */
public interface GrantTableActiveSubjectChecker {

	/**
	 * 本校验器负责的主体类型。
	 * @return 主体类型
	 */
	GrantTableSubjectType subjectType();

	/**
	 * 校验主体存在且为启用状态，不存在时抛业务异常。
	 * @param subjectId 主体 ID
	 */
	void requireExistingActive(Long subjectId);

}

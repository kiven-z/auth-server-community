package com.auth.service.system.admin.support.grant;

import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;

import java.util.function.LongConsumer;

/**
 * 按主体类型委托 {@link LongConsumer} 完成 grant_table 授权前存在性校验
 *
 * @param subjectType 本校验器负责的主体类型
 * @param check 存在且启用校验逻辑
 * @author Bunny
 */
public record TypedGrantTableActiveSubjectChecker(GrantTableSubjectType subjectType,
		LongConsumer check) implements GrantTableActiveSubjectChecker {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void requireExistingActive(Long subjectId) {
		check.accept(subjectId);
	}

}

package com.auth.service.system.admin.support.grant;

import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.LongConsumer;

/**
 * grant_table 授权前校验主体存在且启用；新增主体类型时注册对应 {@link GrantTableActiveSubjectChecker} 即可
 *
 * @author Bunny
 */
@Component
public class GrantTableSubjectExistenceVerifier {

	private final Map<GrantTableSubjectType, LongConsumer> activeSubjectChecks;

	public GrantTableSubjectExistenceVerifier(List<GrantTableActiveSubjectChecker> subjectCheckers) {
		activeSubjectChecks = new EnumMap<>(GrantTableSubjectType.class);
		for (GrantTableActiveSubjectChecker checker : subjectCheckers) {
			GrantTableSubjectType subjectType = checker.subjectType();
			if (activeSubjectChecks.containsKey(subjectType)) {
				throw new IllegalStateException("Duplicate grant table subject checker for type: " + subjectType);
			}
			activeSubjectChecks.put(subjectType, checker::requireExistingActive);
		}
	}

	/**
	 * 校验授权主体存在且为启用状态
	 * @param subjectType 主体类型
	 * @param subjectId 主体 ID
	 */
	public void requireExistingActive(GrantTableSubjectType subjectType, Long subjectId) {
		LongConsumer check = activeSubjectChecks.get(subjectType);
		if (check == null) {
			throw new IllegalArgumentException("Unsupported grant subject type: " + subjectType);
		}
		check.accept(subjectId);
	}

}

package com.auth.service.system.message.model.value.recipient;

import com.auth.service.system.message.model.enums.RecipientScopeType;
import lombok.Builder;
import lombok.Value;

import java.util.Collections;
import java.util.List;

/**
 * 接收人范围
 *
 * @author Bunny
 */
@Value
@Builder
public class RecipientScope {

	/**
	 * 范围类型
	 */
	RecipientScopeType type;

	/**
	 * 范围 ID（用户/岗位/部门）
	 */
	List<Long> ids;

	/**
	 * 部门是否包含子部门
	 */
	Boolean includeChildren;

	/**
	 * 安全获取 ID 列表
	 * @return 非 null 列表
	 */
	public List<Long> safeIds() {
		return ids == null ? Collections.emptyList() : ids;
	}

	/**
	 * 部门是否展开子树
	 * @return true=含子部门
	 */
	public boolean includeChildrenOrDefault() {
		return includeChildren == null || includeChildren;
	}

}

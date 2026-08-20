package com.auth.service.system.admin.support.menu;

import com.auth.common.core.utils.TreeParentIdUtil;
import com.auth.common.core.utils.TreeRelationUtil;
import com.auth.service.system.admin.mapper.admin.menu.SysMenuMapper;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 菜单写操作前的存在性与移动目标校验。
 *
 * @author Bunny
 */
@Slf4j
@Component
public class MenuReferenceChecker {

	private final SysMenuMapper sysMenuMapper;

	public MenuReferenceChecker(SysMenuMapper sysMenuMapper) {
		this.sysMenuMapper = sysMenuMapper;
	}

	/**
	 * 读取已存在菜单，不存在时抛出业务异常。
	 * @param menuId 菜单 ID
	 * @return 菜单实体
	 */
	public SysMenuEntity getExisting(Long menuId) {
		SysMenuEntity existing = sysMenuMapper.selectById(menuId);
		if (existing == null) {
			log.warn("menu not found: id={}", menuId);
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST);
		}
		return existing;
	}

	/**
	 * 规范化父菜单 ID 并校验父菜单存在（顶级跳过校验）。
	 * @param rawParentId 原始父菜单 ID
	 * @return 规范化后的父菜单 ID
	 */
	public long normalizeAndRequireParent(Long rawParentId) {
		long parentId = TreeParentIdUtil.normalize(rawParentId);
		if (parentId > TreeParentIdUtil.ROOT_PARENT_ID && sysMenuMapper.selectById(parentId) == null) {
			log.warn("Tree parent unavailable: parentId={}", parentId);
			throw new SystemBusinessException(SystemCommonResultCode.TREE_PARENT_UNAVAILABLE);
		}
		return parentId;
	}

	/**
	 * 移动目标不能是自身或自身后代。
	 * @param menuId 被移动菜单
	 * @param newParentId 新父菜单
	 */
	public void requireMoveTargetValid(Long menuId, Long newParentId) {
		TreeRelationUtil.requireValidMoveTarget(menuId, newParentId,
				(ancestorId, nodeId) -> TreeRelationUtil.isDescendantOf(ancestorId, nodeId, id -> {
					SysMenuEntity node = sysMenuMapper.selectById(id);
					return node == null ? null : node.getParentId();
				}));
	}

}

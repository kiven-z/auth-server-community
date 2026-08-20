package com.auth.common.core.utils;

import lombok.experimental.UtilityClass;

/**
 * 树节点父子关系判定与移动目标校验。
 *
 * @author Bunny
 */
@UtilityClass
public class TreeRelationUtil {

	/**
	 * 判断 nodeId 是否位于 ancestorId 的子树内。
	 * @param ancestorId 候选祖先节点
	 * @param nodeId 待判定节点
	 * @param loader 父链加载器
	 * @return nodeId 为 ancestorId 的后代时返回 true
	 */
	public static boolean isDescendantOf(Long ancestorId, Long nodeId, ParentIdLoader loader) {
		Long cursor = nodeId;
		while (cursor != null && cursor > TreeParentIdUtil.ROOT_PARENT_ID) {
			if (cursor.equals(ancestorId)) {
				return true;
			}
			cursor = loader.loadParentId(cursor);
		}
		return false;
	}

	/**
	 * 校验移动目标合法；不合法时抛出英文 {@link IllegalArgumentException}。
	 * @param nodeId 被移动节点
	 * @param newParentId 新父节点
	 * @param descendantChecker 后代判定策略
	 */
	public static void requireValidMoveTarget(Long nodeId, Long newParentId, DescendantChecker descendantChecker) {
		if (newParentId != null && newParentId.equals(nodeId)) {
			throw new IllegalArgumentException("Cannot move tree node under itself, nodeId=" + nodeId);
		}
		if (newParentId != null && newParentId > TreeParentIdUtil.ROOT_PARENT_ID
				&& descendantChecker.isDescendantOf(nodeId, newParentId)) {
			throw new IllegalArgumentException(
					"Cannot move tree node under its descendant, nodeId=" + nodeId + ", newParentId=" + newParentId);
		}
	}

	/**
	 * 按节点 ID 加载其 parent_id。
	 */
	@FunctionalInterface
	public interface ParentIdLoader {

		/**
		 * 加载节点的 parent_id。
		 * @param nodeId 当前节点 ID
		 * @return 父节点 ID；不存在或无父级时返回 null
		 */
		Long loadParentId(Long nodeId);

	}

	/**
	 * 后代关系判定策略。
	 */
	@FunctionalInterface
	public interface DescendantChecker {

		/**
		 * 判断 newParentId 是否落在 nodeId 的子树内。
		 * @param ancestorId 被移动节点
		 * @param nodeId 候选新父节点
		 * @return nodeId 为 ancestorId 的后代时返回 true
		 */
		boolean isDescendantOf(Long ancestorId, Long nodeId);

	}

}

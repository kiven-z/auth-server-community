package com.auth.common.core.utils;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.model.entity.TreeNode;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * 树形结构 构造
 *
 * @author Bunny
 */
@UtilityClass
public class TreeNodeProcessorUtil {

	/**
	 * 处理列表数据，构建树形结构
	 * @param list 待处理的扁平数据列表
	 * @return 树形结构的根节点列表
	 */
	public static <T extends TreeNode<T>> List<T> process(List<T> list) {
		List<T> roots = findRoots(list);
		for (T root : roots) {
			buildChildren(root, list);
		}
		return roots;
	}

	/**
	 * 查找根节点（parentId 为空或 0）
	 * @param list 所有节点列表
	 * @return 根节点列表
	 */
	public static <T extends TreeNode<T>> List<T> findRoots(List<T> list) {
		return list.stream().filter(node -> node.getParentId() == null || node.getParentId().equals(0L)).toList();
	}

	private static <T extends TreeNode<T>> void buildChildren(T parent, List<T> list) {
		List<T> children = list.stream()
			.filter(node -> node.getParentId() != null && node.getParentId().equals(parent.getId()))
			.toList();

		parent.setHasLeaf(CollUtil.isNotEmpty(children));
		parent.setChildren(children.isEmpty() ? null : children);

		for (T child : children) {
			buildChildren(child, list);
		}
	}

}

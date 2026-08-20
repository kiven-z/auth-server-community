package com.auth.service.system.admin.convert.admin.menu;

import com.auth.common.core.model.entity.TreeNode;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * 建树中间节点 TreeNodeProcessorUtil 使用
 *
 * @author Bunny
 */
@Getter
@Setter
public class MenuRouteTreeNode extends TreeNode<MenuRouteTreeNode> implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private Integer menuRank;

	private SysMenuEntity menu;

	/**
	 * 由菜单实体构造树节点（id/parentId 与实体一致）
	 * @param entity 菜单行
	 * @return 树节点
	 */
	public static MenuRouteTreeNode fromEntity(SysMenuEntity entity) {
		MenuRouteTreeNode node = new MenuRouteTreeNode();

		Long parentId = entity.getParentId();
		node.setId(entity.getId());
		node.setParentId(Objects.requireNonNullElse(parentId, 0L));
		node.setMenuRank(entity.getMenuRank());
		node.setMenu(entity);
		return node;
	}

}

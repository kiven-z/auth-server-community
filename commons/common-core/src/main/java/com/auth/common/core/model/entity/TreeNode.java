package com.auth.common.core.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 树形结构节点
 *
 * @author Bunny
 */
@Getter
@Setter
public class TreeNode<T> implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	private Long id;

	/**
	 * 父菜单ID(0表示一级菜单)
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	private Long parentId;

	/**
	 * 是否有子级
	 */
	private boolean hasLeaf;

	/**
	 * 子列表
	 */
	@JsonProperty(index = 100)
	private transient List<T> children;

}
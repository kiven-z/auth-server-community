package com.auth.common.core.model.query;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 基础分页请求
 *
 * @author Bunny
 */
@Getter
@Setter
public class PageQueryRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 当前页
	 */
	private Integer pageIndex = 1;

	/**
	 * 分页大小
	 */
	private Integer pageSize = 30;

	/**
	 * 排序字段
	 */
	private List<SortSpec> sort;

}

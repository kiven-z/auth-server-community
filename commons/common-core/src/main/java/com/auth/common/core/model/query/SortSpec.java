package com.auth.common.core.model.query;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 排序规则
 *
 * @author Bunny
 */
@Getter
@Setter
public class SortSpec implements Serializable {

	/**
	 * 业务字段 key，如 createdAt
	 */
	private String field;

	/**
	 * 排序方向
	 */
	private SortDirection direction = SortDirection.DESC;

}
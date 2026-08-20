package com.auth.common.core.model.response;

import com.auth.common.core.annotation.JsonStringFormat;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 基础返回数据
 *
 * @author Bunny
 */
@Getter
@Setter
public class BaseResponse implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 唯一标识
	 */
	@JsonStringFormat
	private Long id;

	/**
	 * 创建时间
	 */
	private Instant createdAt;

	/**
	 * 更新时间
	 */
	private Instant updatedAt;

	/**
	 * 创建用户ID
	 */
	@JsonStringFormat
	private Long createdBy;

	/**
	 * 更新用户ID
	 */
	@JsonStringFormat
	private Long updatedBy;

	/**
	 * 创建用户名
	 */
	private String createdByName;

	/**
	 * 更新用户名
	 */
	private String updatedByName;

}

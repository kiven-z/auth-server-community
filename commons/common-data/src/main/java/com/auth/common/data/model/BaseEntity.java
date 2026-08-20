package com.auth.common.data.model;

import com.auth.common.core.annotation.JsonStringFormat;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 基础信息字段
 *
 * @author Bunny
 */
@Getter
@Setter
public class BaseEntity implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 唯一标识
	 */
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	private Long id;

	/**
	 * 创建时间
	 */
	@TableField(fill = FieldFill.INSERT)
	private Instant createdAt;

	/**
	 * 更新时间
	 */
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private Instant updatedAt;

	/**
	 * 创建用户
	 */
	@JsonStringFormat
	@TableField(fill = FieldFill.INSERT)
	private Long createdBy;

	/**
	 * 操作用户
	 */
	@JsonStringFormat
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private Long updatedBy;

	/**
	 * 乐观锁版本号
	 */
	@Version
	private Long version;

	/**
	 * 备注
	 */
	private String remark;

}

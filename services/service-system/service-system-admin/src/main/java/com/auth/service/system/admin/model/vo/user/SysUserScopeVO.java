package com.auth.service.system.admin.model.vo.user;

import com.auth.common.core.annotation.JsonStringFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 用户数据范围回显
 * <p>
 * 无覆盖时接口返回空列表
 * </p>
 *
 * @author Bunny
 */
@Schema(name = "SysUserScopeVO", title = "用户数据范围")
@Getter
@Setter
@ToString
public class SysUserScopeVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonStringFormat
	@Schema(title = "主键")
	private Long id;

	@JsonStringFormat
	@Schema(title = "用户 ID")
	private Long userId;

	@Schema(title = "数据维度")
	private String dimension;

	@Schema(title = "范围类型", description = "ALL/SELF/IDS/IDS_AND_CHILD")
	private String scopeType;

	@JsonSerialize(contentUsing = ToStringSerializer.class)
	@Schema(title = "节点 ID 列表", description = "ALL/SELF 时为空列表")
	private List<Long> scopeIds;

	@Schema(title = "备注")
	private String remark;

}

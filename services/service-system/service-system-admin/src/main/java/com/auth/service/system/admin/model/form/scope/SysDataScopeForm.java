package com.auth.service.system.admin.model.form.scope;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 数据范围保存表单
 *
 * @author Bunny
 */
@Schema(name = "SysDataScopeForm", title = "数据范围表单")
@Getter
@Setter
public class SysDataScopeForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "数据维度", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "数据维度不能为空")
	private String dimension;

	@Schema(title = "范围类型", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "范围类型不能为空")
	private String scopeType;

	@Schema(title = "节点 ID 列表", description = "IDS/IDS_AND_CHILD 时必填，ALL/SELF 时忽略")
	private List<Long> scopeIds;

	@Schema(title = "备注")
	@Size(max = 500, message = "备注长度不能超过500个字符")
	private String remark;

}

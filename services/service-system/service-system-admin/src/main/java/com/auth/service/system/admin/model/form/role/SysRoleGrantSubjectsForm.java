package com.auth.service.system.admin.model.form.role;

import com.auth.module.security.contract.api.GrantTableSubjectType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 角色授权对象按主体类型全量覆盖表单
 *
 * @author Bunny
 */
@Schema(name = "SysRoleGrantSubjectsForm", title = "角色授权对象按类型表单")
@Getter
@Setter
public class SysRoleGrantSubjectsForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "主体类型（USER / DEPT / POST）", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "主体类型不能为空")
	private GrantTableSubjectType subjectType;

	@Schema(title = "主体 ID 列表（全量替换；空数组表示清空该角色下该类型全部授权）", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "主体 ID 列表不能为空")
	private List<Long> subjectIds;

}

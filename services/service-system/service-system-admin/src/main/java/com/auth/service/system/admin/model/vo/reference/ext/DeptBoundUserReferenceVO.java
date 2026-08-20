package com.auth.service.system.admin.model.vo.reference.ext;

import com.auth.service.system.admin.model.vo.reference.UserReferenceVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 部门详情-关联用户回显（在用户基础字段上扩展主部门）
 *
 * @author Bunny
 */
@Schema(name = "DeptBoundUserReferenceVO", title = "部门关联用户回显")
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DeptBoundUserReferenceVO extends UserReferenceVO {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "是否主部门")
	private Boolean isPrimary;

}

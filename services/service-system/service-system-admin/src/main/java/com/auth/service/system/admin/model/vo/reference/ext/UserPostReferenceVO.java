package com.auth.service.system.admin.model.vo.reference.ext;

import com.auth.service.system.admin.model.vo.reference.PostReferenceVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 用户-岗位关联回显（在岗位基础字段上扩展主岗位）
 *
 * @author Bunny
 */
@Schema(name = "UserPostReferenceVO", title = "用户岗位关联回显")
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class UserPostReferenceVO extends PostReferenceVO {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "是否主岗位")
	private Boolean isPrimary;

}

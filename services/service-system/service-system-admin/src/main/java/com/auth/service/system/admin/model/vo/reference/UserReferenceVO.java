package com.auth.service.system.admin.model.vo.reference;

import com.auth.common.core.annotation.JsonStringFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户关联回显基础字段
 *
 * @author Bunny
 */
@Schema(name = "UserReferenceVO", title = "用户关联回显")
@Getter
@Setter
@ToString
public class UserReferenceVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonStringFormat
	@Schema(title = "用户 ID")
	private Long id;

	@Schema(title = "用户名")
	private String username;

	@Schema(title = "用户状态（0=禁用，1=正常，2=锁定）")
	private Integer status;

	@Schema(title = "昵称")
	private String nickname;

	@Schema(title = "头像")
	private String avatar;

	@Schema(title = "性别（0=未知，1=男，2=女）")
	private Integer gender;

	@Schema(title = "工号")
	private String employeeNo;

}

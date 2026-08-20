package com.auth.service.system.admin.model.vo.user;

import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户分页行
 *
 * @author Bunny
 */
@Schema(name = "SysUserPageVO", title = "用户分页行")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class SysUserPageVO extends BaseResponse {

	@Schema(title = "用户名")
	private String username;

	@Schema(title = "昵称")
	private String nickname;

	@Schema(title = "头像 URL")
	private String avatar;

	@Schema(title = "手机号")
	private String phone;

	@Schema(title = "邮箱")
	private String email;

	@Schema(title = "工号")
	private String employeeNo;

	@Schema(title = "账号状态（0=禁用，1=正常，2=锁定）")
	private Integer status;

}

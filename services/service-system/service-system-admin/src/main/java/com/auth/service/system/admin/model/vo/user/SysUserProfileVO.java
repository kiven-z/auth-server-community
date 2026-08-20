package com.auth.service.system.admin.model.vo.user;

import com.auth.common.core.model.response.BaseResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

/**
 * 用户档案（标量 + 组织关联数）
 *
 * @author Bunny
 */
@Schema(name = "SysUserProfileVO", title = "用户档案")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class SysUserProfileVO extends BaseResponse {

	@Schema(title = "登录账号")
	private String username;

	@Schema(title = "昵称")
	private String nickname;

	@Schema(title = "邮箱")
	private String email;

	@Schema(title = "手机号")
	private String phone;

	@Schema(title = "工号")
	private String employeeNo;

	@Schema(title = "头像URL")
	private String avatar;

	@Schema(title = "状态（0=禁用，1=正常，2=锁定）")
	private Integer status;

	@Schema(title = "性别（0=未知，1=男，2=女）")
	private Integer gender;

	@JsonFormat(pattern = "yyyy-MM-dd")
	@Schema(title = "生日")
	private LocalDate birthday;

	@Schema(title = "个人简介")
	private String introduction;

	@Schema(title = "备注")
	private String remark;

	@Schema(title = "有效任职部门数")
	private Long deptCount;

	@Schema(title = "有效任职岗位数")
	private Long postCount;

}

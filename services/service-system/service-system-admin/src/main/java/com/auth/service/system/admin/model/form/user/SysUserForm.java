package com.auth.service.system.admin.model.form.user;

import com.auth.common.web.valid.group.CreateGroup;
import com.auth.common.web.valid.group.UpdateGroup;
import com.auth.common.web.validation.ValidationPatterns;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 用户新增/更新表单
 *
 * @author Bunny
 */
@Schema(name = "SysUserSaveForm", title = "用户保存表单")
@Getter
@Setter
public class SysUserForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "用户主键，更新时必填")
	@NotNull(groups = UpdateGroup.class, message = "用户ID不能为空")
	private Long id;

	@Schema(title = "用户名")
	@Size(max = 64, message = "用户名长度不能超过64个字符", groups = { CreateGroup.class, UpdateGroup.class })
	@NotBlank(message = "用户名不能为空", groups = { CreateGroup.class, UpdateGroup.class })
	private String username;

	@Schema(title = "初始密码")
	@Size(min = 8, max = 18, message = "初始密码长度应为8-18位", groups = CreateGroup.class)
	@Pattern(regexp = ValidationPatterns.TWO_OF_THREE_CHAR_CLASSES_8_18, message = "初始密码格式不符合约定（8-18位，数字、字母、符号至少两种）",
			groups = CreateGroup.class)
	@NotBlank(message = "初始密码不能为空", groups = CreateGroup.class)
	private String initialPassword;

	@Schema(title = "昵称", requiredMode = Schema.RequiredMode.REQUIRED)
	@Size(max = 64, message = "昵称长度不能超过64个字符", groups = { CreateGroup.class, UpdateGroup.class })
	@NotBlank(message = "昵称不能为空", groups = { CreateGroup.class, UpdateGroup.class })
	private String nickname;

	@Schema(title = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
	@Size(max = 128, message = "邮箱长度不能超过128个字符", groups = { CreateGroup.class, UpdateGroup.class })
	@Email(message = "邮箱格式不正确", groups = { CreateGroup.class, UpdateGroup.class })
	@NotBlank(message = "邮箱不能为空", groups = { CreateGroup.class, UpdateGroup.class })
	private String email;

	@Schema(title = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
	@Size(max = 32, message = "手机号长度不能超过32个字符", groups = { CreateGroup.class, UpdateGroup.class })
	@NotBlank(message = "手机号不能为空", groups = { CreateGroup.class, UpdateGroup.class })
	private String phone;

	@Schema(title = "工号")
	@Size(max = 64, message = "工号长度不能超过64个字符", groups = { CreateGroup.class, UpdateGroup.class })
	private String employeeNo;

	@Schema(title = "账号状态（0=禁用，1=正常，2=锁定）")
	@Max(value = 2, message = "账号状态值无效", groups = { CreateGroup.class, UpdateGroup.class })
	@Min(value = 0, message = "账号状态值无效", groups = { CreateGroup.class, UpdateGroup.class })
	@NotNull(message = "账号状态不能为空", groups = { CreateGroup.class, UpdateGroup.class })
	private Integer status;

	@Schema(title = "性别（0=未知，1=男，2=女）")
	@Max(value = 2, message = "性别值无效", groups = { CreateGroup.class, UpdateGroup.class })
	@Min(value = 0, message = "性别值无效", groups = { CreateGroup.class, UpdateGroup.class })
	private Integer gender;

	@Schema(title = "出生日期")
	@PastOrPresent(message = "出生日期不能晚于今天", groups = { CreateGroup.class, UpdateGroup.class })
	private LocalDate birthday;

	@Schema(title = "个人简介")
	private String introduction;

	@Schema(title = "备注")
	@Size(max = 500, message = "备注长度不能超过500个字符", groups = { CreateGroup.class, UpdateGroup.class })
	private String remark;

}

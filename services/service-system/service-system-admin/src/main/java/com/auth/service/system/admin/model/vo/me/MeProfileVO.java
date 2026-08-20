package com.auth.service.system.admin.model.vo.me;

import com.auth.common.core.annotation.JsonStringFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 当前登录用户展示资料
 *
 * @author Bunny
 */
@Getter
@Setter
@ToString
public class MeProfileVO {

	@Schema(title = "用户名")
	private String username;

	@Schema(title = "昵称")
	private String nickname;

	@Schema(title = "头像")
	private String avatar;

	@JsonStringFormat
	@Schema(title = "主部门 ID")
	private Long primaryDeptId;

	@Schema(title = "主部门名称")
	private String primaryDeptName;

}

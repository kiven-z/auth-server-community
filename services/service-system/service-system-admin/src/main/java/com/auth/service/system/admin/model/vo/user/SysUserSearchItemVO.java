package com.auth.service.system.admin.model.vo.user;

import com.auth.common.core.annotation.JsonStringFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户关键词搜索单行结果（不含敏感字段）
 *
 * @author Bunny
 */
@Schema(name = "SysUserSearchItemVO", title = "用户搜索项")
@Getter
@Setter
@ToString
public class SysUserSearchItemVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonStringFormat
	@Schema(title = "用户ID")
	private Long id;

	@Schema(title = "登录账号")
	private String username;

	@Schema(title = "昵称")
	private String nickname;

}

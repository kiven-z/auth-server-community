package com.auth.service.system.admin.model.po.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户关键词搜索查询行
 *
 * @author Bunny
 */
@Getter
@Setter
public class UserSearchItemPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "用户ID")
	private Long id;

	@Schema(title = "用户名")
	private String username;

	@Schema(title = "昵称")
	private String nickname;

}

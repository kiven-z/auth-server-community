package com.auth.service.auth.model.po.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户失效状态查询投影
 *
 * @author Bunny
 */
@Getter
@Setter
public class UserInvalidationStatePO {

	@Schema(title = "用户ID")
	private Long userId;

	@Schema(title = "用户状态（0=禁用,1=正常,2=锁定）")
	private Integer status;

}

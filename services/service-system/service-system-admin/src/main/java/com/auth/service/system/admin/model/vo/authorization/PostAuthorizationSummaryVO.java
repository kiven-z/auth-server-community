package com.auth.service.system.admin.model.vo.authorization;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 岗位授权面摘要
 *
 * @author Bunny
 */
@Schema(name = "PostAuthorizationSummaryVO", title = "岗位授权面摘要")
@Getter
@Setter
@ToString
public class PostAuthorizationSummaryVO {

	@Schema(title = "绑定用户数", description = "含岗位已停用")
	private Long boundUserCount;

}

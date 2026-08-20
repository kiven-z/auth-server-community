package com.auth.service.system.admin.model.vo.authorization;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 部门授权面摘要
 *
 * @author Bunny
 */
@Schema(name = "DeptAuthorizationSummaryVO", title = "部门授权面摘要")
@Getter
@Setter
@ToString
public class DeptAuthorizationSummaryVO {

	@Schema(title = "关联用户数", description = "含用户已停用")
	private Long boundUserCount;

	@Schema(title = "下属岗位数")
	private Long boundPostCount;

}

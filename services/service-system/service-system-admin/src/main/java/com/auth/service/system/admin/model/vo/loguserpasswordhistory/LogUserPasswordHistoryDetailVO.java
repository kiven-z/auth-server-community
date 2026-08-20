package com.auth.service.system.admin.model.vo.loguserpasswordhistory;

import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 密码历史日志详情
 *
 * @author Bunny
 */
@Schema(name = "LogUserPasswordHistoryDetailVO", title = "密码历史日志详情")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class LogUserPasswordHistoryDetailVO extends BaseResponse {

	@JsonStringFormat
	@Schema(title = "用户ID")
	private Long userId;

	@Schema(title = "用户名")
	private String username;

	@Schema(title = "修改IP地址")
	private String changeIp;

}

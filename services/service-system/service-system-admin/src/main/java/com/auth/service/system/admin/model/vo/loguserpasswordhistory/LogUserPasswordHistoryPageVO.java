package com.auth.service.system.admin.model.vo.loguserpasswordhistory;

import com.auth.common.core.annotation.Desensitized;
import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.common.core.desensitize.DesensitizedType;
import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

/**
 * 密码历史日志分页行
 *
 * @author Bunny
 */
@Schema(name = "LogUserPasswordHistoryPageVO", title = "密码历史日志分页行")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class LogUserPasswordHistoryPageVO extends BaseResponse {

	@JsonStringFormat
	@Schema(title = "用户ID")
	private Long userId;

	@Schema(title = "用户名")
	private String username;

	@Desensitized(DesensitizedType.IP_ADDRESS)
	@Schema(title = "修改IP地址")
	private String changeIp;

	@Schema(title = "修改时间")
	private Instant changeTime;

}

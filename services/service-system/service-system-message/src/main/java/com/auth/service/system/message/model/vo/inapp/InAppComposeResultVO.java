package com.auth.service.system.message.model.vo.inapp;

import com.auth.common.core.annotation.JsonStringFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * 站内信按范围发送结果
 *
 * @author Bunny
 */
@Schema(name = "InAppComposeResultVO", title = "站内信发送结果")
@Getter
@Builder
@ToString
public class InAppComposeResultVO {

	@JsonStringFormat
	@Schema(title = "发送任务 ID")
	private Long taskId;

	@Schema(title = "展开后接收人数")
	private Integer totalCount;

	@Schema(title = "写入成功数")
	private Integer successCount;

	@Schema(title = "任务状态")
	private String status;

}

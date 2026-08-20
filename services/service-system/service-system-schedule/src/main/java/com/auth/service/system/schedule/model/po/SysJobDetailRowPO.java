package com.auth.service.system.schedule.model.po;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 定时任务详情行（含分组名称）
 *
 * @author Bunny
 */
@Schema(name = "SysJobDetailRowPO", title = "定时任务详情行")
@Getter
@Setter
@ToString(callSuper = true)
public class SysJobDetailRowPO extends SysJobPageRowPO {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "任务分组名称")
	private String jobGroupName;

}

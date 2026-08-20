package com.auth.service.system.schedule.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * 白名单任务类可选方法
 *
 * @author Bunny
 */
@Schema(name = "QuartzTaskMethodVO", title = "白名单任务可选方法")
@Getter
@Setter
@ToString
public class QuartzTaskMethodVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "方法名")
	private String methodName;

	@Schema(title = "参数签名")
	private String parameterSignature;

	@Schema(title = "返回值类型")
	private String returnType;

	@Schema(title = "invoke_target 示例")
	private String invokeTargetExample;

}

package com.auth.service.system.schedule.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 前端「执行配置」下拉：白名单任务类信息
 *
 * @author Bunny
 */
@Schema(name = "QuartzTaskClassVO", title = "可调度任务类")
@Getter
@Setter
@ToString
public class QuartzTaskClassVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "类全限定名")
	private String className;

	@Schema(title = "展示名称")
	private String name;

	@Schema(title = "说明")
	private String description;

	@Schema(title = "适用调度模式（BEAN_INVOKE/CUSTOM_CLASS）")
	private List<String> invokeModes;

	@Schema(title = "job_params 示例 JSON（CUSTOM_CLASS 可选）")
	private String jobParamsExample;

}

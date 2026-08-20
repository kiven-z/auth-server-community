package com.auth.service.system.schedule.model.jobparams;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 微服务远程调用内置任务 job_params 载荷
 *
 * @author Bunny
 */
@Schema(name = "FeignInvokeJobParams", title = "微服务远程调用任务参数")
@Getter
@Setter
public class FeignInvokeJobParams implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "目标微服务名（注册中心 serviceId）")
	@NotBlank
	private String serviceName;

	@Schema(title = "请求路径（以 / 开头）")
	@NotBlank
	private String path;

	@Schema(title = "HTTP 方法")
	@NotNull
	private BuiltinRemoteHttpMethod method;

	@Schema(title = "请求头")
	private Map<String, String> headers;

	@Schema(title = "查询参数（一层键值对）")
	private Map<String, String> query;

	@Schema(title = "请求体（JSON 对象或数组；GET/DELETE 可省略）")
	private transient JsonNode body;

}

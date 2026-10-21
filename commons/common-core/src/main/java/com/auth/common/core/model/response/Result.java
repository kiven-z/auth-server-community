package com.auth.common.core.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 通用返回结果
 *
 * @param <T> 传入数据类型
 * @author Bunny
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

	/**
	 * 全项目统一的业务成功码（与 HTTP 2xx 配合使用；失败码由各模块枚举定义，非 0）
	 */
	public static final int SUCCESS_CODE = 0;

	@Serial
	private static final long serialVersionUID = 1L;

	private Integer code;

	private String message;

	/**
	 * 稳定错误标识符
	 */
	private String error;

	private transient T data;

	/**
	 * 扩展负载，用于额外元数据（例如 i18n 消息键和参数）
	 */
	private transient Map<String, Object> ext;

	private long timestamp = System.currentTimeMillis();

	protected static <T> Result<T> build(T data) {
		Result<T> result = new Result<>();
		result.setData(data);
		return result;
	}

	/**
	 * 组装返回体
	 * @param body 数据
	 * @param code 状态码
	 * @param message 消息
	 * @return 返回体
	 */
	public static <T> Result<T> build(T body, Integer code, String message) {
		Result<T> result = build(body);
		result.setCode(code);
		result.setMessage(message);
		result.setData(body);
		return result;
	}

	/**
	 * 业务成功，携带数据
	 * @param data 数据
	 */
	public static <T> Result<T> success(T data) {
		return build(data, SUCCESS_CODE, "success");
	}

	/**
	 * 无负载的业务成功
	 */
	public static <T> Result<T> success() {
		return build(null, SUCCESS_CODE, "success");
	}

	/**
	 * 业务成功，自定义消息（例如增删改后的 i18n 文案）
	 * @param data 返回体
	 * @param message 提示信息
	 */
	public static <T> Result<T> success(T data, String message) {
		return build(data, SUCCESS_CODE, message);
	}

	/**
	 * 失败，不设置状态码和消息
	 */
	public static <T> Result<T> error() {
		return Result.build(null);
	}

	/**
	 * 失败，自定义数据、状态码和消息
	 * @param data 数据
	 * @param code 状态码
	 * @param message 消息
	 */
	public static <T> Result<T> error(T data, Integer code, String message) {
		return build(data, code, message);
	}

	/**
	 * 失败，自定义数据和消息（状态码 500）
	 * @param data 数据
	 * @param message 消息
	 */
	public static <T> Result<T> error(T data, String message) {
		return build(data, 500, message);
	}

	/**
	 * 失败，自定义状态码和消息
	 * @param code 状态码
	 * @param message 消息
	 */
	public static <T> Result<T> error(Integer code, String message) {
		return build(null, code, message);
	}

	/**
	 * 失败，携带稳定错误标识
	 * @param code 状态码
	 * @param error 稳定错误标识
	 * @param message 消息
	 */
	public static <T> Result<T> error(Integer code, String error, String message) {
		Result<T> result = build(null, code, message);
		result.setError(error);
		return result;
	}

	/**
	 * 失败，自定义消息（状态码 500）
	 * @param message 消息
	 */
	public static <T> Result<T> error(String message) {
		return build(null, 500, message);
	}

}

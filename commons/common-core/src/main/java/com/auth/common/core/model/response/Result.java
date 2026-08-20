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

	/**
	 * 状态码
	 */
	private Integer code;

	/**
	 * 返回消息
	 */
	private String message;

	/**
	 * 稳定错误标识符
	 */
	private String error;

	/**
	 * 稳定错误子代码，别名 {@link #error}
	 */
	private String subCode;

	/**
	 * 返回数据
	 */
	private transient T data;

	/**
	 * 扩展负载用于额外的元数据（例如 i18n 消息键/参数）
	 */
	private transient Map<String, Object> ext;

	/**
	 * 系统时间戳
	 */
	private long timestamp = System.currentTimeMillis();

	/**
	 * 自定义返回体
	 * @param data 返回体
	 * @return 返回体
	 */
	protected static <T> Result<T> build(T data) {
		Result<T> result = new Result<>();
		result.setData(data);
		return result;
	}

	/**
	 * 自定义返回体
	 * @param body 返回体
	 * @param code 返回状态码
	 * @param message 返回消息
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
	 * 操作失败-自定义返回数据和状态码
	 * @param data 返回体
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
	 * 操作失败
	 */
	public static <T> Result<T> error() {
		return Result.build(null);
	}

	/**
	 * 操作失败-自定义返回数据和状态码
	 * @param data 返回体
	 * @param code 状态码
	 * @param message 错误信息
	 */
	public static <T> Result<T> error(T data, Integer code, String message) {
		return build(data, code, message);
	}

	/**
	 * 操作失败-自定义返回数据和状态码
	 * @param data 返回体
	 * @param message 错误信息
	 */
	public static <T> Result<T> error(T data, String message) {
		return build(data, 500, message);
	}

	/**
	 * 操作失败-自定义返回数据和状态码
	 * @param code 状态码
	 * @param message 错误信息
	 */
	public static <T> Result<T> error(Integer code, String message) {
		return build(null, code, message);
	}

	/**
	 * Error response with stable error identifier.
	 * @param code business/http status code
	 * @param error stable error identifier
	 * @param message english message for humans
	 */
	public static <T> Result<T> error(Integer code, String error, String message) {
		Result<T> result = build(null, code, message);
		result.setError(error);
		result.setSubCode(error);
		return result;
	}

	/**
	 * 操作失败-自定义返回数据和状态码
	 * @param message 错误信息
	 */
	public static <T> Result<T> error(String message) {
		return build(null, 500, message);
	}

}

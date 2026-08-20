package com.auth.module.security.autoconfigure.audit.operationlog;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import lombok.experimental.UtilityClass;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.*;
import java.util.function.Function;

/**
 * 操作日志请求参数摘要构建
 * <p>
 * Servlet 侧：GET 使用 query string；其它方法使用 {@link HttpServletRequest#getParameterMap()}
 * Spring MVC 侧：对控制器方法实参做摘要（含 @RequestBody 解析后的对象），上传文件仅记元数据
 * </p>
 *
 * @author Bunny
 */
@UtilityClass
public class OperationLogRequestParamsBuilder {

	/**
	 * 请求文本最大长度（约 4KB）
	 */
	public static final int MAX_REQUEST_TEXT = 4096;

	private static final String KEY_QUERY_STRING = "queryString";

	private static final String KEY_PARAMETER_MAP = "parameterMap";

	private static final String KEY_METHOD_ARGS = "methodArgs";

	private static final String KEY_TYPE = "_type";

	private static final String KEY_NOTE = "_note";

	private static final String GET = "GET";

	/**
	 * 组装请求参数摘要
	 * @param request HTTP 请求
	 * @param hasRecord 是否记录
	 * @param objectMapper JSON 序列化
	 * @param methodArgs 控制器方法实参，可为 null 或空列表
	 * @return 摘要或 null
	 */
	public static String build(HttpServletRequest request, boolean hasRecord, ObjectMapper objectMapper,
			List<Object> methodArgs) {
		if (!hasRecord) {
			return null;
		}
		List<Object> methodSummaries = summarizeMethodArguments(methodArgs, objectMapper);
		if (methodSummaries.isEmpty()) {
			return buildServletDerivedOnly(request, objectMapper);
		}
		return buildCombinedWithMethodArgs(request, objectMapper, methodSummaries);
	}

	/**
	 * 仅从 Servlet 层 query / parameterMap 生成摘要
	 * @param request 请求
	 * @param objectMapper JSON 序列化
	 * @return 摘要或 null
	 */
	private static String buildServletDerivedOnly(HttpServletRequest request, ObjectMapper objectMapper) {
		if (GET.equalsIgnoreCase(request.getMethod())) {
			return legacyGetQuerySummary(request);
		}
		return serializeParameterMapOrNull(request.getParameterMap(), objectMapper);
	}

	/**
	 * 合并 Servlet 摘要与 Spring 绑定的方法实参
	 * @param request 请求
	 * @param objectMapper JSON 序列化
	 * @param methodSummaries 方法实参摘要
	 * @return 摘要或 null
	 */
	private static String buildCombinedWithMethodArgs(HttpServletRequest request, ObjectMapper objectMapper,
			List<Object> methodSummaries) {
		Map<String, Object> root = new LinkedHashMap<>();
		if (GET.equalsIgnoreCase(request.getMethod())) {
			putQueryStringIfNotBlankForCombined(root, request);
		}
		else {
			Map<String, String[]> map = request.getParameterMap();
			if (MapUtil.isNotEmpty(map)) {
				root.put(KEY_PARAMETER_MAP, map);
			}
		}
		root.put(KEY_METHOD_ARGS, methodSummaries);
		return writeJsonTruncated(objectMapper, root);
	}

	/**
	 * GET：返回截断后的 query 字符串（空白 query 也返回空串而非 null）
	 * @param request 请求
	 * @return 截断后的 query 字符串
	 */
	private static String legacyGetQuerySummary(HttpServletRequest request) {
		return CharSequenceUtil.subPre(CharSequenceUtil.nullToEmpty(request.getQueryString()), MAX_REQUEST_TEXT);
	}

	/**
	 * 合并形态下仅在 query 非空白时写入 {@link #KEY_QUERY_STRING}
	 * @param root 根
	 * @param request 请求
	 */
	private static void putQueryStringIfNotBlankForCombined(Map<String, Object> root, HttpServletRequest request) {
		String qs = CharSequenceUtil.nullToEmpty(request.getQueryString());
		if (CharSequenceUtil.isNotBlank(qs)) {
			root.put(KEY_QUERY_STRING, CharSequenceUtil.subPre(qs, MAX_REQUEST_TEXT));
		}
	}

	/**
	 * 将非空 parameterMap 序列化为 JSON 并截断；否则 null
	 * @param map 参数映射
	 * @param objectMapper JSON 序列化
	 * @return 摘要或 null
	 */
	private static String serializeParameterMapOrNull(Map<String, String[]> map, ObjectMapper objectMapper) {
		if (MapUtil.isEmpty(map)) {
			return null;
		}
		return writeJsonTruncated(objectMapper, map);
	}

	/**
	 * JSON 序列化后按 {@link #MAX_REQUEST_TEXT} 截断；失败返回 null
	 * @param objectMapper JSON 序列化
	 * @param value 值
	 * @return 摘要或 null
	 */
	private static String writeJsonTruncated(ObjectMapper objectMapper, Object value) {
		try {
			return CharSequenceUtil.subPre(objectMapper.writeValueAsString(value), MAX_REQUEST_TEXT);
		}
		catch (JsonProcessingException ex) {
			return null;
		}
	}

	/**
	 * 将方法实参转为可 JSON 化的摘要列表（跳过 Web 框架占位参数）
	 * @param methodArgs 方法实参
	 * @param objectMapper JSON 序列化
	 * @return 摘要列表
	 */
	private static List<Object> summarizeMethodArguments(List<Object> methodArgs, ObjectMapper objectMapper) {
		if (methodArgs == null || methodArgs.isEmpty()) {
			return Collections.emptyList();
		}
		List<Object> out = new ArrayList<>();
		for (Object arg : methodArgs) {
			if (arg == null || isSkippedFrameworkArgument(arg)) {
				continue;
			}

			Object summary = toLoggableArgumentSummary(arg, objectMapper);
			if (summary != null) {
				out.add(summary);
			}
		}
		return out;
	}

	/**
	 * 判断是否属于 Web 层框架注入参数（不参与业务入参摘要）
	 * @param arg 方法实参
	 * @return 是否跳过
	 */
	private static boolean isSkippedFrameworkArgument(Object arg) {
		// RedirectAttributes 为 Model 子类型，由 instanceof Model 一并跳过
		return arg instanceof ServletRequest || arg instanceof ServletResponse || arg instanceof BindingResult
				|| arg instanceof Model || arg instanceof NativeWebRequest || arg instanceof HttpSession
				|| arg instanceof Principal;
	}

	/**
	 * 单个实参的日志摘要：上传文件仅元数据；字节数组只记长度；其余尽量转为 {@link JsonNode}
	 * @param arg 方法实参
	 * @param objectMapper JSON 序列化
	 * @return 摘要或 null
	 */
	private static Object toLoggableArgumentSummary(Object arg, ObjectMapper objectMapper) {
		if (arg instanceof MultipartFile file) {
			return summarizeMultipartFile(file);
		}
		if (arg instanceof MultipartFile[] files) {
			return mapNonNullElements(files, OperationLogRequestParamsBuilder::summarizeMultipartFile);
		}
		if (arg instanceof Part part) {
			return summarizePart(part);
		}
		if (arg instanceof Part[] parts) {
			return mapNonNullElements(parts, OperationLogRequestParamsBuilder::summarizePart);
		}
		if (arg instanceof byte[] bytes) {
			return Map.of(KEY_TYPE, "byte[]", "length", bytes.length);
		}
		try {
			return objectMapper.valueToTree(arg);
		}
		catch (Exception ex) {
			return Map.of(KEY_TYPE, arg.getClass().getName(), KEY_NOTE, "not serializable for operation log");
		}
	}

	/**
	 * 将数组中非 null 元素映射为摘要列表
	 * @param items 数组
	 * @param toSummary 映射函数
	 * @return 摘要列表
	 */
	private static <T> List<Object> mapNonNullElements(T[] items, Function<T, Object> toSummary) {
		List<Object> list = new ArrayList<>();
		for (T item : items) {
			if (item != null) {
				list.add(toSummary.apply(item));
			}
		}
		return list;
	}

	/**
	 * 将 {@link MultipartFile} 转为元数据映射（不落库文件字节）
	 * @param file 文件
	 * @return 元数据映射
	 */
	private static Map<String, Object> summarizeMultipartFile(MultipartFile file) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put(KEY_TYPE, "MultipartFile");
		m.put("originalFilename", file.getOriginalFilename());
		m.put("size", file.getSize());
		m.put("contentType", file.getContentType());
		return m;
	}

	/**
	 * 将 {@link Part} 转为元数据映射
	 * @param part 部分
	 * @return 元数据映射
	 */
	private static Map<String, Object> summarizePart(Part part) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put(KEY_TYPE, "Part");
		m.put("name", part.getName());
		m.put("submittedFileName", part.getSubmittedFileName());
		m.put("size", part.getSize());
		m.put("contentType", part.getContentType());
		return m;
	}

}

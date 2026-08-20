package com.auth.service.system.schedule.task.support;

import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.model.vo.QuartzTaskMethodVO;
import lombok.experimental.UtilityClass;
import org.springframework.lang.NonNull;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_INVOKE_TARGET_INVALID;
import static com.auth.service.system.schedule.model.constants.SysJobQuartzDataKeys.METHOD_NAME;

/**
 * Bean 调用模式 invoke_target 的共享规则：可调用方法判定、签名与示例格式化。
 *
 * @author Bunny
 */
@UtilityClass
public class InvokeTargetSupport {

	/**
	 * 是否可被 invoke_target 反射调用
	 * @param method 候选方法
	 * @return 是否可调用
	 */
	public static boolean isCallableMethod(@NonNull Method method) {
		int modifiers = method.getModifiers();
		if (!Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers) || method.isSynthetic()
				|| method.isBridge()) {
			return false;
		}
		if (method.getDeclaringClass() == Object.class) {
			return false;
		}
		Class<?>[] parameterTypes = method.getParameterTypes();
		if (parameterTypes.length == 0) {
			return true;
		}
		return parameterTypes.length == 1 && parameterTypes[0] == String.class;
	}

	/**
	 * 生成 invoke_target 示例；按参数类型生成占位符供用户填写
	 * @param beanName Spring Bean 名
	 * @param method 已判定为可调用的方法
	 * @return 示例字符串
	 */
	public static @NonNull String formatInvokeTarget(@NonNull String beanName, @NonNull Method method) {
		String methodName = method.getName();
		Class<?>[] parameterTypes = method.getParameterTypes();
		if (parameterTypes.length == 0) {
			return beanName + "." + methodName + "()";
		}
		String args = Stream.of(parameterTypes)
			.map(InvokeTargetSupport::formatInvokeArgPlaceholder)
			.collect(Collectors.joining(", "));
		return beanName + "." + methodName + "(" + args + ")";
	}

	/**
	 * 组装任务目录方法项
	 * @param beanName Spring Bean 名
	 * @param method 已判定为可调用的方法
	 * @return 方法目录 VO
	 */
	public static @NonNull QuartzTaskMethodVO toMethodCatalog(@NonNull String beanName, @NonNull Method method) {
		QuartzTaskMethodVO methodVO = new QuartzTaskMethodVO();
		methodVO.setMethodName(method.getName());
		String parameterSignature = formatParameterList(method.getParameterTypes());
		methodVO.setParameterSignature(parameterSignature);
		methodVO.setReturnType(method.getReturnType().getSimpleName());
		methodVO.setInvokeTargetExample(formatInvokeTarget(beanName, method));
		return methodVO;
	}

	/**
	 * 按参数个数解析 Bean 上的 public 方法
	 * @param clazz Bean 类型
	 * @param methodName 方法名
	 * @param argCount 参数个数（0 或 1）
	 * @return 可访问的方法
	 */
	public static @NonNull Method resolveMethod(@NonNull Class<?> clazz, @NonNull String methodName, int argCount) {
		Class<?>[] paramTypes;
		if (argCount == 0) {
			paramTypes = new Class<?>[0];
		}
		else if (argCount == 1) {
			paramTypes = new Class<?>[] { String.class };
		}
		else {
			throw new SysJobException(JOB_INVOKE_TARGET_INVALID,
					"only no-arg or single string literal parameter is supported");
		}

		String signature = formatParameterList(paramTypes);
		Method method = ReflectionUtils.findMethod(clazz, methodName, paramTypes);
		if (method == null) {
			throw new SysJobException(JOB_INVOKE_TARGET_INVALID,
					"method not found for signature " + signature + ": " + methodName);
		}
		ReflectionUtils.makeAccessible(method);
		return Objects.requireNonNull(method, METHOD_NAME);
	}

	/**
	 * 将参数类型格式化为展示用括号段
	 * @param parameterTypes 参数类型数组
	 * @return 如 () 或 (String, int)
	 */
	public static @NonNull String formatParameterList(@NonNull Class<?>[] parameterTypes) {
		if (parameterTypes.length == 0) {
			return "()";
		}
		String inner = Stream.of(parameterTypes).map(Class::getSimpleName).collect(Collectors.joining(", "));
		return "(" + inner + ")";
	}

	/**
	 * 按参数类型生成 invoke_target 占位字面量
	 * @param parameterType 参数类型
	 * @return 占位字符串
	 */
	private static @NonNull String formatInvokeArgPlaceholder(@NonNull Class<?> parameterType) {
		if (parameterType == String.class) {
			return "''";
		}
		throw new IllegalArgumentException("Unsupported invoke_target parameter type: " + parameterType.getName());
	}

}

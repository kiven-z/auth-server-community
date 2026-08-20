package com.auth.service.system.schedule.task.support;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.schedule.exception.SysJobException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Objects;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.PARAM_REQUIRED;
import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_INVOKE_TARGET_INVALID;
import static com.auth.service.system.schedule.model.constants.SysJobQuartzDataKeys.BEAN_NAME;
import static com.auth.service.system.schedule.model.constants.SysJobQuartzDataKeys.METHOD_NAME;

/**
 * 解析 invoke_target（首个 . 前为 Spring Bean 名，后为方法及可选参数）并反射调用
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class QuartzBeanInvoker {

	private final ApplicationContext applicationContext;

	/**
	 * 解析调用目标字符串
	 * @param invokeTarget 如 beanInvokeDemoTask.run()
	 * @return 调用规范
	 */
	private static @NonNull InvocationSpec parseSpec(@Nullable String invokeTarget) {
		if (CharSequenceUtil.isBlank(invokeTarget)) {
			throw new SysJobException(PARAM_REQUIRED, "invoke_target");
		}

		// 验证invokeTarget是否为空
		String nonNullInvokeTarget = Objects.requireNonNull(invokeTarget, "invokeTarget");
		int dot = nonNullInvokeTarget.indexOf('.');
		if (dot <= 0 || dot >= invokeTarget.length() - 1) {
			throw new SysJobException(JOB_INVOKE_TARGET_INVALID,
					"format must be beanName.method or beanName.method(...)");
		}

		// 解析beanName和methodName
		String beanName = nonNullInvokeTarget.substring(0, dot).trim();
		String tail = nonNullInvokeTarget.substring(dot + 1).trim();
		if (CharSequenceUtil.isEmpty(tail)) {
			throw new SysJobException(JOB_INVOKE_TARGET_INVALID,
					"format must be beanName.method or beanName.method(...)");
		}

		// 验证beanName是否为空
		String nonNullBeanName = Objects.requireNonNull(beanName, BEAN_NAME);
		int lp = tail.indexOf('(');
		if (lp < 0) {
			String nonNullMethodName = Objects.requireNonNull(tail, METHOD_NAME);
			return new InvocationSpec(nonNullBeanName, nonNullMethodName, null);
		}

		String suffix = ")";
		if (!tail.endsWith(suffix)) {
			throw new SysJobException(JOB_INVOKE_TARGET_INVALID, "mismatched parentheses");
		}

		String methodName = tail.substring(0, lp).trim();
		String argsInner = tail.substring(lp + 1, tail.length() - 1).trim();
		String nonNullMethodName = Objects.requireNonNull(methodName, METHOD_NAME);
		return new InvocationSpec(nonNullBeanName, nonNullMethodName, argsInner);
	}

	/**
	 * 解析方法参数
	 * @param inner 参数字符串
	 * @return 参数数组
	 */
	private static Object[] parseArgs(@Nullable String inner) {
		// 1. 前置检查与去空格
		if (CharSequenceUtil.isBlank(inner)) {
			return new Object[0];
		}
		String t = CharSequenceUtil.trim(inner);

		// 2. 尝试移除引号（支持双引号或单引号）
		String unwrapped = CharSequenceUtil.unWrap(t, '"');
		// 如果没变，说明不是双引号包裹，尝试单引号
		if (unwrapped.equals(t)) {
			unwrapped = CharSequenceUtil.unWrap(t, '\'');
		}

		// 3. 判断是否成功移除并返回
		if (unwrapped.length() < t.length()) {
			return new Object[] { unwrapped };
		}
		throw new SysJobException(JOB_INVOKE_TARGET_INVALID,
				"only no-arg or single string literal parameter is supported");
	}

	/**
	 * 执行调用目标字符串
	 * @param invokeTarget 如 beanInvokeDemoTask.run()
	 * @return 方法返回值转字符串（无返回则为空串）
	 */
	public String invoke(@Nullable String invokeTarget) {
		InvocationSpec spec = parseSpec(invokeTarget);

		String beanName = Objects.requireNonNull(spec.beanName, BEAN_NAME);
		String methodName = Objects.requireNonNull(spec.methodName, METHOD_NAME);

		Object bean = applicationContext.getBean(beanName);
		Object[] args = parseArgs(spec.argsInner);

		// 解析方法
		Method method = InvokeTargetSupport.resolveMethod(bean.getClass(), methodName, args.length);
		// 调用方法
		Object result = ReflectionUtils.invokeMethod(method, bean, args);

		return Convert.toStr(result, "");
	}

	/**
	 * 调用规范
	 *
	 * @param beanName 类名
	 * @param methodName 方法名
	 * @param argsInner 参数
	 */
	private record InvocationSpec(@NonNull String beanName, @NonNull String methodName, @Nullable String argsInner) {
		private InvocationSpec(@NonNull String beanName, @NonNull String methodName, @Nullable String argsInner) {
			this.beanName = Objects.requireNonNull(beanName, BEAN_NAME);
			this.methodName = Objects.requireNonNull(methodName, METHOD_NAME);
			this.argsInner = argsInner;
		}
	}

}

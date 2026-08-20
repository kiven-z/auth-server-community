package com.auth.service.system.schedule.task.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link InvokeTargetSupport} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("InvokeTargetSupport Bean 调用目标")
class InvokeTargetSupportTest {

	@Test
	@DisplayName("isCallableMethod：public 无参或单 String 参数实例方法可调用")
	void isCallableMethodAllowsSupportedSignatures() throws NoSuchMethodException {
		Method run = SampleTask.class.getMethod("run");
		Method echo = SampleTask.class.getMethod("echo", String.class);
		Method twoParams = SampleTask.class.getMethod("twoParams", String.class, String.class);
		Method staticRun = SampleTask.class.getMethod("staticRun");

		assertThat(InvokeTargetSupport.isCallableMethod(run)).isTrue();
		assertThat(InvokeTargetSupport.isCallableMethod(echo)).isTrue();
		assertThat(InvokeTargetSupport.isCallableMethod(twoParams)).isFalse();
		assertThat(InvokeTargetSupport.isCallableMethod(staticRun)).isFalse();
	}

	@Test
	@DisplayName("formatParameterList：按反射参数类型生成签名")
	void formatParameterListMatchesArgCount() throws NoSuchMethodException {
		Method run = SampleTask.class.getMethod("run");
		Method echo = SampleTask.class.getMethod("echo", String.class);

		assertThat(InvokeTargetSupport.formatParameterList(run.getParameterTypes())).isEqualTo("()");
		assertThat(InvokeTargetSupport.formatParameterList(echo.getParameterTypes())).isEqualTo("(String)");
	}

	@Test
	@DisplayName("formatInvokeTarget：无参方法与 String 参数方法分别生成示例")
	void formatInvokeTargetBuildsExpectedExamples() throws NoSuchMethodException {
		Method run = SampleTask.class.getMethod("run");
		Method echo = SampleTask.class.getMethod("echo", String.class);

		assertThat(InvokeTargetSupport.formatInvokeTarget("sampleTask", run)).isEqualTo("sampleTask.run()");
		assertThat(InvokeTargetSupport.formatInvokeTarget("sampleTask", echo)).isEqualTo("sampleTask.echo('')");
	}

	@Test
	@DisplayName("toMethodCatalog：组装目录 VO 字段")
	void toMethodCatalogBuildsVo() throws NoSuchMethodException {
		Method echo = SampleTask.class.getMethod("echo", String.class);

		var vo = InvokeTargetSupport.toMethodCatalog("sampleTask", echo);

		assertThat(vo.getMethodName()).isEqualTo("echo");
		assertThat(vo.getParameterSignature()).isEqualTo("(String)");
		assertThat(vo.getReturnType()).isEqualTo("String");
		assertThat(vo.getInvokeTargetExample()).isEqualTo("sampleTask.echo('')");
	}

	/**
	 * 无参与单 String 参数的可调用方法
	 */
	public static class SampleTask {

		public static void staticRun() {
			// 测试用
		}

		public void run() {
			// 测试用
		}

		public String echo(String message) {
			return message;
		}

		public void twoParams(String first, String second) {
			// 测试用：不支持的双 String 参数
		}

	}

}

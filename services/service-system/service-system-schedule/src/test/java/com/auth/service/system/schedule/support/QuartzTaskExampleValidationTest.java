package com.auth.service.system.schedule.support;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.schedule.annotation.QuartzTask;
import com.auth.service.system.schedule.model.entity.JobEntity;
import com.auth.service.system.schedule.support.catalog.QuartzTaskRegistry;
import com.auth.service.system.schedule.task.support.BuiltinJobParamsParser;
import com.auth.service.system.schedule.validation.task.CustomClassTaskDefinitionValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

/**
 * 内置 {@link QuartzTask#example()} 应与载荷校验规则一致。
 *
 * @author Bunny
 */
@DisplayName("QuartzTask example 与 job_params 校验一致性")
class QuartzTaskExampleValidationTest {

	private static CustomClassTaskDefinitionValidator validator;

	@BeforeAll
	static void setUpValidator() {
		Validator beanValidator = Validation.buildDefaultValidatorFactory().getValidator();
		validator = new CustomClassTaskDefinitionValidator(mock(QuartzTaskRegistry.class),
				new BuiltinJobParamsParser(new ObjectMapper()), beanValidator);
	}

	static Stream<Arguments> builtinJobExamples() {
		return Stream.of(Arguments.of(com.auth.service.system.schedule.task.builtin.HttpInvokeJob.class),
				Arguments.of(com.auth.service.system.schedule.task.builtin.FeignInvokeJob.class));
	}

	@DisplayName("@QuartzTask example 应能通过 validateIfPresent")
	@ParameterizedTest(name = "{0}")
	@MethodSource("builtinJobExamples")
	void quartzTaskExample_passesValidation(Class<?> jobClass) {
		QuartzTask ann = AnnotationUtils.findAnnotation(jobClass, QuartzTask.class);
		Assertions.assertNotNull(ann);
		String example = CharSequenceUtil.trim(ann.example());

		JobEntity job = new JobEntity();
		job.setJobClass(jobClass.getName());
		job.setJobParams(example);

		assertThatCode(() -> validator.validateIfPresent(job)).doesNotThrowAnyException();
	}

}

package com.auth.service.system.message.support.inapp;

import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.mapper.InAppMessageCategoryMapper;
import com.auth.service.system.message.model.entity.InAppMessageCategoryEntity;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.PARAM_REQUIRED;
import static com.auth.service.system.message.exception.MessageResultCode.IN_APP_MESSAGE_CATEGORY_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link InAppMessageCategorySupport} 启用小类校验
 *
 * @author Bunny
 */
@DisplayName("InAppMessageCategorySupport 业务分类校验")
@ExtendWith(MockitoExtension.class)
class InAppMessageCategorySupportTest {

	@Mock
	private InAppMessageCategoryMapper categoryMapper;

	@InjectMocks
	private InAppMessageCategorySupport categorySupport;

	@Test
	@DisplayName("requireEnabledMinor：命中启用小类")
	void requireEnabledMinor_returnsEntity() {
		InAppMessageCategoryEntity minor = new InAppMessageCategoryEntity();
		minor.setId(104L);
		when(categoryMapper.selectOne(any(Wrapper.class))).thenReturn(minor);

		assertThat(categorySupport.requireEnabledMinor(104L)).isSameAs(minor);
	}

	@Test
	@DisplayName("requireEnabledMinor：空主键抛出 PARAM_REQUIRED")
	void requireEnabledMinor_null_throws() {
		assertThatThrownBy(() -> categorySupport.requireEnabledMinor(null)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(PARAM_REQUIRED);
		verify(categoryMapper, never()).selectOne(any(Wrapper.class));
	}

	@Test
	@DisplayName("requireEnabledMinor：未命中抛出 NOT_FOUND")
	void requireEnabledMinor_missing_throws() {
		when(categoryMapper.selectOne(any(Wrapper.class))).thenReturn(null);

		assertThatThrownBy(() -> categorySupport.requireEnabledMinor(104L)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_MESSAGE_CATEGORY_NOT_FOUND);
	}

	@Test
	@DisplayName("requireEnabledMinorByCode：命中启用小类")
	void requireEnabledMinorByCode_returnsEntity() {
		InAppMessageCategoryEntity minor = new InAppMessageCategoryEntity();
		minor.setId(104L);
		minor.setCode("NOTICE_FILE_EXPORT");
		when(categoryMapper.selectOne(any(Wrapper.class))).thenReturn(minor);

		assertThat(categorySupport.requireEnabledMinorByCode("NOTICE_FILE_EXPORT")).isSameAs(minor);
	}

	@Test
	@DisplayName("requireEnabledMinorByCode：空白编码抛出 PARAM_REQUIRED")
	void requireEnabledMinorByCode_blank_throws() {
		assertThatThrownBy(() -> categorySupport.requireEnabledMinorByCode(" ")).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(PARAM_REQUIRED);
		verify(categoryMapper, never()).selectOne(any(Wrapper.class));
	}

	@Test
	@DisplayName("requireEnabledMinorByCode：未命中抛出 NOT_FOUND")
	void requireEnabledMinorByCode_missing_throws() {
		when(categoryMapper.selectOne(any(Wrapper.class))).thenReturn(null);

		assertThatThrownBy(() -> categorySupport.requireEnabledMinorByCode("MISSING"))
			.isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_MESSAGE_CATEGORY_NOT_FOUND);
	}

}

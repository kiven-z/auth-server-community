package com.auth.service.system.message.service.admin.impl;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.mapper.MessageTemplateMapper;
import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.form.sms.SmsTemplateForm;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link SmsTemplateServiceImpl} 短信模板创建/更新单元测试
 */
@DisplayName("SysSmsTemplateServiceImpl 短信模板服务")
@ExtendWith(MockitoExtension.class)
class SmsTemplateServiceImplTest {

	@Mock
	private MessageTemplateMapper messageTemplateMapper;

	@InjectMocks
	private SmsTemplateServiceImpl sysSmsTemplateService;

	@BeforeAll
	static void initMybatisPlusTableInfo() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
				MessageTemplateEntity.class);
	}

	@BeforeEach
	void setUp() throws Exception {
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysSmsTemplateService, messageTemplateMapper);
	}

	@Test
	@DisplayName("创建：写入 SMS 渠道、厂商模板码，并初始化空 require_fields")
	void create_shouldPersistSmsChannelAndProviderCode() {
		when(messageTemplateMapper.insert(any(MessageTemplateEntity.class))).thenReturn(1);

		SmsTemplateForm form = new SmsTemplateForm();
		form.setTemplateCode("sms_login");
		form.setTemplateName("登录验证码短信");
		form.setProviderTemplateCode("SMS_123456");
		form.setDescription("Dypns 登录验证码");
		form.setStatus(true);
		form.setPriority(5);
		form.setContent("您的验证码是${code}");

		sysSmsTemplateService.create(form);

		ArgumentCaptor<MessageTemplateEntity> entityCaptor = ArgumentCaptor.forClass(MessageTemplateEntity.class);
		verify(messageTemplateMapper).insert(entityCaptor.capture());
		MessageTemplateEntity saved = entityCaptor.getValue();

		assertThat(saved.getChannel()).isEqualTo(MessageChannel.SMS.name());
		assertThat(saved.getSceneCode()).isEqualTo("sms_login");
		assertThat(saved.getTemplateName()).isEqualTo("登录验证码短信");
		assertThat(saved.getProviderTemplateCode()).isEqualTo("SMS_123456");
		assertThat(saved.getDescription()).isEqualTo("Dypns 登录验证码");
		assertThat(saved.getStatus()).isTrue();
		assertThat(saved.getPriority()).isEqualTo(5);
		assertThat(saved.getBodyContent()).isEqualTo("您的验证码是${code}");
		assertThat(saved.getRequireFields()).isEqualTo("[]");
		assertThat(saved.getSubject()).isNull();
	}

	@Test
	@DisplayName("创建：content 为 null 时 bodyContent 原样写入 null")
	void create_shouldPersistNullBodyWhenContentNull() {
		when(messageTemplateMapper.insert(any(MessageTemplateEntity.class))).thenReturn(1);

		SmsTemplateForm form = new SmsTemplateForm();
		form.setTemplateCode("sms_reset");
		form.setTemplateName("重置密码短信");
		form.setProviderTemplateCode("SMS_654321");
		form.setStatus(false);
		form.setPriority(1);
		form.setContent(null);

		sysSmsTemplateService.create(form);

		ArgumentCaptor<MessageTemplateEntity> entityCaptor = ArgumentCaptor.forClass(MessageTemplateEntity.class);
		verify(messageTemplateMapper).insert(entityCaptor.capture());
		assertThat(entityCaptor.getValue().getBodyContent()).isNull();
		assertThat(entityCaptor.getValue().getRequireFields()).isEqualTo("[]");
	}

	@Test
	@DisplayName("更新：覆盖元数据与厂商模板码，保留 require_fields 与 channel")
	void update_shouldOverwriteFieldsAndKeepRequireFields() {
		MessageTemplateEntity exists = new MessageTemplateEntity();
		exists.setId(10L);
		exists.setChannel(MessageChannel.SMS.name());
		exists.setSceneCode("old_code");
		exists.setTemplateName("旧名称");
		exists.setProviderTemplateCode("SMS_OLD");
		exists.setDescription("旧描述");
		exists.setStatus(false);
		exists.setPriority(1);
		exists.setBodyContent("旧正文");
		exists.setRequireFields("[{\"key\":\"code\",\"exampleValue\":\"123456\"}]");
		when(messageTemplateMapper.selectById(10L)).thenReturn(exists);
		when(messageTemplateMapper.updateById(any(MessageTemplateEntity.class))).thenReturn(1);

		SmsTemplateForm form = new SmsTemplateForm();
		form.setId(10L);
		form.setTemplateCode("sms_login");
		form.setTemplateName("登录验证码短信");
		form.setProviderTemplateCode("SMS_123456");
		form.setDescription("新描述");
		form.setStatus(true);
		form.setPriority(8);
		form.setContent("您的验证码是${code}");

		sysSmsTemplateService.update(form);

		ArgumentCaptor<MessageTemplateEntity> entityCaptor = ArgumentCaptor.forClass(MessageTemplateEntity.class);
		verify(messageTemplateMapper).updateById(entityCaptor.capture());
		MessageTemplateEntity updated = entityCaptor.getValue();

		assertThat(updated.getChannel()).isEqualTo(MessageChannel.SMS.name());
		assertThat(updated.getSceneCode()).isEqualTo("sms_login");
		assertThat(updated.getTemplateName()).isEqualTo("登录验证码短信");
		assertThat(updated.getProviderTemplateCode()).isEqualTo("SMS_123456");
		assertThat(updated.getDescription()).isEqualTo("新描述");
		assertThat(updated.getStatus()).isTrue();
		assertThat(updated.getPriority()).isEqualTo(8);
		assertThat(updated.getBodyContent()).isEqualTo("您的验证码是${code}");
		assertThat(updated.getRequireFields()).isEqualTo("[{\"key\":\"code\",\"exampleValue\":\"123456\"}]");
	}

	@Test
	@DisplayName("更新：content 为 null 时 bodyContent 覆盖为 null")
	void update_shouldOverwriteBodyWhenContentNull() {
		MessageTemplateEntity exists = new MessageTemplateEntity();
		exists.setId(11L);
		exists.setChannel(MessageChannel.SMS.name());
		exists.setBodyContent("原正文");
		exists.setRequireFields("[]");
		when(messageTemplateMapper.selectById(11L)).thenReturn(exists);
		when(messageTemplateMapper.updateById(any(MessageTemplateEntity.class))).thenReturn(1);

		SmsTemplateForm form = new SmsTemplateForm();
		form.setId(11L);
		form.setTemplateCode("sms_overwrite");
		form.setTemplateName("覆盖正文模板");
		form.setProviderTemplateCode("SMS_OVERWRITE");
		form.setStatus(true);
		form.setPriority(3);
		form.setContent(null);

		sysSmsTemplateService.update(form);

		ArgumentCaptor<MessageTemplateEntity> entityCaptor = ArgumentCaptor.forClass(MessageTemplateEntity.class);
		verify(messageTemplateMapper).updateById(entityCaptor.capture());
		assertThat(entityCaptor.getValue().getBodyContent()).isNull();
		assertThat(entityCaptor.getValue().getRequireFields()).isEqualTo("[]");
	}

	@Test
	@DisplayName("更新：记录不存在时抛出 DATA_NOT_EXIST")
	void update_shouldThrowWhenMissing() {
		when(messageTemplateMapper.selectById(99L)).thenReturn(null);

		SmsTemplateForm form = new SmsTemplateForm();
		form.setId(99L);
		form.setTemplateCode("sms_missing");
		form.setTemplateName("不存在");
		form.setProviderTemplateCode("SMS_X");
		form.setStatus(true);
		form.setPriority(1);

		assertThatThrownBy(() -> sysSmsTemplateService.update(form)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(DATA_NOT_EXIST);
		verify(messageTemplateMapper, never()).updateById(any(MessageTemplateEntity.class));
	}

	@Test
	@DisplayName("更新：不校验渠道，仅按 id 更新且不改写 channel")
	void update_shouldNotValidateChannel() {
		MessageTemplateEntity exists = new MessageTemplateEntity();
		exists.setId(12L);
		exists.setChannel(MessageChannel.EMAIL.name());
		exists.setRequireFields("[]");
		when(messageTemplateMapper.selectById(12L)).thenReturn(exists);
		when(messageTemplateMapper.updateById(any(MessageTemplateEntity.class))).thenReturn(1);

		SmsTemplateForm form = new SmsTemplateForm();
		form.setId(12L);
		form.setTemplateCode("sms_any_channel");
		form.setTemplateName("不校验渠道");
		form.setProviderTemplateCode("SMS_X");
		form.setStatus(true);
		form.setPriority(1);
		form.setContent("正文");

		sysSmsTemplateService.update(form);

		ArgumentCaptor<MessageTemplateEntity> entityCaptor = ArgumentCaptor.forClass(MessageTemplateEntity.class);
		verify(messageTemplateMapper).updateById(entityCaptor.capture());
		MessageTemplateEntity updated = entityCaptor.getValue();
		assertThat(updated.getSceneCode()).isEqualTo("sms_any_channel");
		assertThat(updated.getBodyContent()).isEqualTo("正文");
	}

}

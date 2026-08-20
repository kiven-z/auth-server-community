package com.auth.service.system.message.service.admin.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.module.message.api.channel.MessageChannel;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.mapper.MessageChannelDeliveryMapper;
import com.auth.service.system.message.model.entity.MessageChannelDeliveryEntity;
import com.auth.service.system.message.model.enums.MessageDeliveryStatus;
import com.auth.service.system.message.model.po.MessageChannelDeliveryPageRowPO;
import com.auth.service.system.message.model.query.MessageChannelDeliveryQuery;
import com.auth.service.system.message.model.vo.delivery.MessageChannelDeliveryDetailVO;
import com.auth.service.system.message.model.vo.delivery.MessageChannelDeliveryPageVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link MessageChannelDeliveryServiceImpl} 单元测试
 */
@DisplayName("MessageChannelDeliveryServiceImpl 渠道投递记录")
@ExtendWith(MockitoExtension.class)
class MessageChannelDeliveryServiceImplTest {

	@Mock
	private MessageChannelDeliveryMapper messageChannelDeliveryMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	private MessageChannelDeliveryServiceImpl messageChannelDeliveryService;

	@BeforeEach
	void setUp() {
		// ServiceImpl.baseMapper 由 Spring 字段注入；单测手动挂上
		messageChannelDeliveryService = new MessageChannelDeliveryServiceImpl(auditUserDisplayService);
		ReflectionTestUtils.setField(messageChannelDeliveryService, "baseMapper", messageChannelDeliveryMapper);
	}

	@Test
	@DisplayName("分页：映射业务字段并填充审计用户名")
	void getChannelDeliveryPage_shouldMapFieldsAndEnrichAudit() {
		// 准备带筛选条件的分页查询与 Mapper 返回行
		MessageChannelDeliveryQuery query = new MessageChannelDeliveryQuery();
		query.setChannel(MessageChannel.EMAIL.name());
		query.setStatus(MessageDeliveryStatus.SUCCESS.name());
		query.setTaskId(100L);
		query.setPageIndex(1);
		query.setPageSize(10);

		Instant sentAt = LocalDateTime.of(2026, 7, 18, 12, 0, 0).toInstant(java.time.ZoneOffset.UTC);
		Instant auditAt = sentAt;
		MessageChannelDeliveryPageRowPO row = new MessageChannelDeliveryPageRowPO();
		row.setId(1L);
		row.setTaskId(100L);
		row.setChannel(MessageChannel.EMAIL.name());
		row.setStatus(MessageDeliveryStatus.SUCCESS.name());
		row.setProviderMsgId("prov-1");
		row.setErrorCode(null);
		row.setSentAt(sentAt);
		row.setCreatedAt(auditAt);
		row.setUpdatedAt(auditAt);
		row.setCreatedBy(1L);
		row.setUpdatedBy(1L);

		Page<MessageChannelDeliveryPageRowPO> mapperPage = new Page<>(1, 10);
		mapperPage.setRecords(List.of(row));
		mapperPage.setTotal(1);
		when(messageChannelDeliveryMapper.selectChannelDeliveryPage(any(Page.class), eq(query))).thenReturn(mapperPage);

		PageResponse<MessageChannelDeliveryPageVO> result = messageChannelDeliveryService.getChannelDeliveryPage(query);

		assertThat(result.getList()).hasSize(1);
		MessageChannelDeliveryPageVO vo = result.getList().get(0);
		assertThat(vo.getId()).isEqualTo(1L);
		assertThat(vo.getTaskId()).isEqualTo(100L);
		assertThat(vo.getChannel()).isEqualTo(MessageChannel.EMAIL.name());
		assertThat(vo.getStatus()).isEqualTo(MessageDeliveryStatus.SUCCESS.name());
		assertThat(vo.getProviderMsgId()).isEqualTo("prov-1");
		assertThat(vo.getErrorCode()).isNull();
		assertThat(vo.getSentAt()).isEqualTo(sentAt);
		verify(auditUserDisplayService).enrichAuditUsernames(any(IPage.class), isNull(), isNull());

		ArgumentCaptor<MessageChannelDeliveryQuery> queryCaptor = ArgumentCaptor
			.forClass(MessageChannelDeliveryQuery.class);
		verify(messageChannelDeliveryMapper).selectChannelDeliveryPage(any(Page.class), queryCaptor.capture());
		assertThat(queryCaptor.getValue().getChannel()).isEqualTo(MessageChannel.EMAIL.name());
		assertThat(queryCaptor.getValue().getStatus()).isEqualTo(MessageDeliveryStatus.SUCCESS.name());
		assertThat(queryCaptor.getValue().getTaskId()).isEqualTo(100L);
	}

	@Test
	@DisplayName("分页：无匹配数据时返回空列表")
	void getChannelDeliveryPage_shouldReturnEmptyWhenNoMatch() {
		// 无筛选命中时交由 SQL 返回空页
		MessageChannelDeliveryQuery query = new MessageChannelDeliveryQuery();
		query.setChannel("FAX");

		Page<MessageChannelDeliveryPageRowPO> mapperPage = new Page<>(1, 30);
		mapperPage.setRecords(Collections.emptyList());
		mapperPage.setTotal(0);
		when(messageChannelDeliveryMapper.selectChannelDeliveryPage(any(Page.class), eq(query))).thenReturn(mapperPage);

		PageResponse<MessageChannelDeliveryPageVO> result = messageChannelDeliveryService.getChannelDeliveryPage(query);

		assertThat(result.getList()).isEmpty();
		assertThat(result.getTotal()).isZero();
		verify(auditUserDisplayService).enrichAuditUsernames(any(IPage.class), isNull(), isNull());
	}

	@Test
	@DisplayName("详情：按主键返回全量字段并填充审计用户名")
	void getChannelDeliveryById_shouldReturnDetailAndEnrichAudit() {
		// 准备落库实体，校验详情映射含目标与错误信息等列表未暴露字段
		Instant sentAt = LocalDateTime.of(2026, 7, 18, 12, 30, 0).toInstant(java.time.ZoneOffset.UTC);
		Instant auditAt = sentAt;
		MessageChannelDeliveryEntity entity = new MessageChannelDeliveryEntity();
		entity.setId(11L);
		entity.setTaskId(200L);
		entity.setChannel(MessageChannel.SMS.name());
		entity.setTargetValue("13800000000");
		entity.setStatus(MessageDeliveryStatus.FAILED.name());
		entity.setProviderMsgId(null);
		entity.setErrorCode("SMS_TIMEOUT");
		entity.setErrorMessage("vendor timeout");
		entity.setSentAt(sentAt);
		entity.setRetryCount(0);
		entity.setRemark("retry later");
		entity.setCreatedAt(auditAt);
		entity.setUpdatedAt(auditAt);
		entity.setCreatedBy(2L);
		entity.setUpdatedBy(2L);
		when(messageChannelDeliveryMapper.selectById(11L)).thenReturn(entity);

		MessageChannelDeliveryDetailVO vo = messageChannelDeliveryService.getChannelDeliveryById(11L);

		assertThat(vo.getId()).isEqualTo(11L);
		assertThat(vo.getTaskId()).isEqualTo(200L);
		assertThat(vo.getChannel()).isEqualTo(MessageChannel.SMS.name());
		assertThat(vo.getTargetValue()).isEqualTo("13800000000");
		assertThat(vo.getStatus()).isEqualTo(MessageDeliveryStatus.FAILED.name());
		assertThat(vo.getErrorCode()).isEqualTo("SMS_TIMEOUT");
		assertThat(vo.getErrorMessage()).isEqualTo("vendor timeout");
		assertThat(vo.getSentAt()).isEqualTo(sentAt);
		assertThat(vo.getRetryCount()).isZero();
		assertThat(vo.getRemark()).isEqualTo("retry later");
		verify(auditUserDisplayService).enrichAuditUsernames(any(List.class), isNull(), isNull());
	}

	@Test
	@DisplayName("详情：记录不存在时抛出 DATA_NOT_EXIST")
	void getChannelDeliveryById_shouldThrowWhenMissing() {
		// 主键无对应行时直接业务异常
		when(messageChannelDeliveryMapper.selectById(999L)).thenReturn(null);

		assertThatThrownBy(() -> messageChannelDeliveryService.getChannelDeliveryById(999L))
			.isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(DATA_NOT_EXIST);
	}

	@Test
	@DisplayName("批量删除：按 ids 物理删除")
	void batchDelete_shouldDeleteByIds() {
		// 前端传入主键数组，Mapper 按 ids 物理删除
		List<Long> ids = List.of(1L, 2L);
		when(messageChannelDeliveryMapper.deleteByIds(ids)).thenReturn(2);

		messageChannelDeliveryService.batchDelete(ids);

		verify(messageChannelDeliveryMapper).deleteByIds(ids);
	}

	@Test
	@DisplayName("批量删除：ids 为空时不调用删除")
	void batchDelete_shouldSkipWhenIdsEmpty() {
		// 空列表短路，避免无意义 delete
		messageChannelDeliveryService.batchDelete(Collections.emptyList());

		verify(messageChannelDeliveryMapper, never()).deleteByIds(any());
	}

}

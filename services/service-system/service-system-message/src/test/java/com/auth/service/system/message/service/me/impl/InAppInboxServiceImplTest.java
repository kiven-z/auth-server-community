package com.auth.service.system.message.service.me.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.module.message.api.model.enums.MessageContentType;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.mapper.InAppInboxMapper;
import com.auth.service.system.message.mapper.InAppMessageMapper;
import com.auth.service.system.message.mapper.InAppMessageRecipientMapper;
import com.auth.service.system.message.mapper.InAppMessageUserStatusMapper;
import com.auth.service.system.message.model.entity.InAppMessageEntity;
import com.auth.service.system.message.model.entity.InAppMessageUserStatusEntity;
import com.auth.service.system.message.model.enums.InAppMessageStatus;
import com.auth.service.system.message.model.enums.RecipientScopeType;
import com.auth.service.system.message.model.po.InAppInboxDetailRowPO;
import com.auth.service.system.message.model.po.InAppInboxMajorUnreadRowPO;
import com.auth.service.system.message.model.po.InAppInboxPageRowPO;
import com.auth.service.system.message.model.query.InAppInboxQuery;
import com.auth.service.system.message.model.vo.inapp.InAppInboxDetailVO;
import com.auth.service.system.message.model.vo.inapp.InAppInboxMajorUnreadVO;
import com.auth.service.system.message.model.vo.inapp.InAppInboxPageVO;
import com.auth.service.system.message.model.vo.inapp.InAppInboxUnreadCountVO;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link InAppInboxServiceImpl} 单元测试
 *
 * @author Bunny
 */
@DisplayName("InAppInboxServiceImpl 用户侧站内信收件箱")
@ExtendWith(MockitoExtension.class)
class InAppInboxServiceImplTest {

	@Mock
	private InAppInboxMapper inAppInboxMapper;

	@Mock
	private InAppMessageMapper inAppMessageMapper;

	@Mock
	private InAppMessageRecipientMapper inAppMessageRecipientMapper;

	@Mock
	private InAppMessageUserStatusMapper inAppMessageUserStatusMapper;

	private InAppInboxServiceImpl service;

	@BeforeEach
	void setUp() {
		// lambdaQuery 需要实体表元数据
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
				InAppMessageEntity.class);
		service = new InAppInboxServiceImpl(inAppInboxMapper, inAppMessageMapper, inAppMessageRecipientMapper,
				inAppMessageUserStatusMapper);
	}

	@Test
	@DisplayName("分页：统一列表按当前用户查询并映射字段")
	void getInboxPage_shouldPassUserIdAndMapFields() {
		// 登录用户查统一收件箱：未读优先结果映射到列表 VO
		InAppInboxQuery query = new InAppInboxQuery();
		query.setMajorCategoryId(1L);
		query.setIsRead(false);
		query.setCategoryId(104L);
		query.setTitle("公告");
		query.setPageIndex(1);
		query.setPageSize(10);

		Instant now = LocalDateTime.of(2026, 7, 19, 17, 0, 0).toInstant(java.time.ZoneOffset.UTC);
		InAppInboxPageRowPO row = new InAppInboxPageRowPO();
		row.setId(11L);
		row.setSceneCode("notice");
		row.setTitle("系统公告");
		row.setContentType(MessageContentType.MARKDOWN.name());
		row.setCategoryId(104L);
		row.setCategoryName("一般通知");
		row.setLinkUrl("/notice/11");
		row.setSenderUserId(7L);
		row.setIsRead(false);
		row.setReadTime(null);
		row.setCreatedAt(now);
		row.setUpdatedAt(now);
		row.setCreatedBy(1L);
		row.setUpdatedBy(1L);

		Page<InAppInboxPageRowPO> mapperPage = new Page<>(1, 10);
		mapperPage.setRecords(List.of(row));
		mapperPage.setTotal(1);
		when(inAppInboxMapper.selectInboxPage(any(Page.class), eq(100L), any(InAppInboxQuery.class)))
			.thenReturn(mapperPage);

		try (MockedStatic<SecurityUserUtils> security = mockStatic(SecurityUserUtils.class)) {
			security.when(SecurityUserUtils::getUserId).thenReturn(100L);

			PageResponse<InAppInboxPageVO> result = service.getInboxPage(query);

			assertThat(result.getList()).hasSize(1);
			InAppInboxPageVO vo = result.getList().get(0);
			assertThat(vo.getId()).isEqualTo(11L);
			assertThat(vo.getSceneCode()).isEqualTo("notice");
			assertThat(vo.getTitle()).isEqualTo("系统公告");
			assertThat(vo.getContentType()).isEqualTo(MessageContentType.MARKDOWN.name());
			assertThat(vo.getCategoryId()).isEqualTo(104L);
			assertThat(vo.getCategoryName()).isEqualTo("一般通知");
			assertThat(vo.getLinkUrl()).isEqualTo("/notice/11");
			assertThat(vo.getSenderUserId()).isEqualTo(7L);
			assertThat(vo.getIsRead()).isFalse();
			assertThat(vo.getReadTime()).isNull();
		}

		ArgumentCaptor<InAppInboxQuery> queryCaptor = ArgumentCaptor.forClass(InAppInboxQuery.class);
		verify(inAppInboxMapper).selectInboxPage(any(Page.class), eq(100L), queryCaptor.capture());
		assertThat(queryCaptor.getValue().getMajorCategoryId()).isEqualTo(1L);
		assertThat(queryCaptor.getValue().getCategoryId()).isEqualTo(104L);
		assertThat(queryCaptor.getValue().getIsRead()).isFalse();
		assertThat(queryCaptor.getValue().getTitle()).isEqualTo("公告");
	}

	@Test
	@DisplayName("未读角标：按大类汇总名称编码并计算总数")
	void getUnreadCount_shouldAggregateByMajor() {
		// Mapper 返回启用大类（含名称/编码），Service 映射并汇总 total
		InAppInboxMajorUnreadRowPO notice = new InAppInboxMajorUnreadRowPO();
		notice.setMajorCategoryId(1L);
		notice.setMajorCategoryName("通知消息");
		notice.setMajorCategoryCode("NOTICE");
		notice.setUnreadCount(3L);
		InAppInboxMajorUnreadRowPO securityMajor = new InAppInboxMajorUnreadRowPO();
		securityMajor.setMajorCategoryId(2L);
		securityMajor.setMajorCategoryName("安全消息");
		securityMajor.setMajorCategoryCode("SECURITY");
		securityMajor.setUnreadCount(5L);
		when(inAppInboxMapper.selectUnreadCountByMajor(100L)).thenReturn(List.of(notice, securityMajor));

		try (MockedStatic<SecurityUserUtils> security = mockStatic(SecurityUserUtils.class)) {
			security.when(SecurityUserUtils::getUserId).thenReturn(100L);

			InAppInboxUnreadCountVO result = service.getUnreadCount();

			assertThat(result.getTotalUnreadCount()).isEqualTo(8L);
			assertThat(result.getMajors()).hasSize(2);
			InAppInboxMajorUnreadVO first = result.getMajors().get(0);
			assertThat(first.getMajorCategoryId()).isEqualTo(1L);
			assertThat(first.getMajorCategoryName()).isEqualTo("通知消息");
			assertThat(first.getMajorCategoryCode()).isEqualTo("NOTICE");
			assertThat(first.getUnreadCount()).isEqualTo(3L);
			assertThat(result.getMajors().get(1).getMajorCategoryId()).isEqualTo(2L);
			assertThat(result.getMajors().get(1).getUnreadCount()).isEqualTo(5L);
		}

		verify(inAppInboxMapper).selectUnreadCountByMajor(100L);
	}

	@Test
	@DisplayName("未读角标：无启用大类时返回空列表与 0 总数")
	void getUnreadCount_shouldReturnEmptyWhenNoUnread() {
		// 无启用大类时 majors 为空、total=0
		when(inAppInboxMapper.selectUnreadCountByMajor(100L)).thenReturn(List.of());

		try (MockedStatic<SecurityUserUtils> security = mockStatic(SecurityUserUtils.class)) {
			security.when(SecurityUserUtils::getUserId).thenReturn(100L);

			InAppInboxUnreadCountVO result = service.getUnreadCount();

			assertThat(result.getTotalUnreadCount()).isZero();
			assertThat(result.getMajors()).isEmpty();
		}
	}

	@Test
	@DisplayName("标已读：按范围分流，写扩散改收件行、读扩散懒写状态")
	void markRead_shouldSplitPushUpdateAndPullUpsert() {
		// 先查主表分组：写扩散只更新 recipient，读扩散只 upsert user_status
		InAppMessageEntity pushMessage = new InAppMessageEntity();
		pushMessage.setId(11L);
		pushMessage.setRecipientScopeType(RecipientScopeType.USER.name());
		pushMessage.setStatus(InAppMessageStatus.SUCCESS.name());

		InAppMessageEntity pushPartial = new InAppMessageEntity();
		pushPartial.setId(12L);
		pushPartial.setRecipientScopeType(RecipientScopeType.USER.name());
		pushPartial.setStatus(InAppMessageStatus.PARTIAL.name());

		InAppMessageEntity pullMessage = new InAppMessageEntity();
		pullMessage.setId(13L);
		pullMessage.setRecipientScopeType(RecipientScopeType.ALL.name());
		pullMessage.setStatus(InAppMessageStatus.SUCCESS.name());

		when(inAppMessageMapper.selectList(any(Wrapper.class)))
			.thenReturn(List.of(pushMessage, pushPartial, pullMessage));

		try (MockedStatic<SecurityUserUtils> security = mockStatic(SecurityUserUtils.class)) {
			security.when(SecurityUserUtils::getUserId).thenReturn(100L);

			service.markRead(List.of(11L, 12L, 13L));
		}

		verify(inAppMessageRecipientMapper).markRead(eq(100L), eq(List.of(11L, 12L)), any(Instant.class));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<InAppMessageUserStatusEntity>> rowsCaptor = ArgumentCaptor.forClass(List.class);
		verify(inAppMessageUserStatusMapper).upsertReadBatch(rowsCaptor.capture());
		List<InAppMessageUserStatusEntity> rows = rowsCaptor.getValue();
		assertThat(rows).hasSize(1);
		InAppMessageUserStatusEntity row = rows.get(0);
		assertThat(row.getId()).isNotNull();
		assertThat(row.getMessageId()).isEqualTo(13L);
		assertThat(row.getUserId()).isEqualTo(100L);
		assertThat(row.getIsRead()).isTrue();
		assertThat(row.getReadTime()).isNotNull();
		assertThat(row.getIsDeleted()).isFalse();
		assertThat(row.getCreatedBy()).isEqualTo(100L);
		assertThat(row.getUpdatedBy()).isEqualTo(100L);
		assertThat(row.getVersion()).isZero();
	}

	@Test
	@DisplayName("标已读：去重后仅写扩散可读时只更新收件行")
	void markRead_shouldOnlyUpdateRecipientWhenNoPull() {
		// 重复 ID 去重；只有定向可读消息时不写 user_status
		InAppMessageEntity pushMessage = new InAppMessageEntity();
		pushMessage.setId(21L);
		pushMessage.setRecipientScopeType(RecipientScopeType.USER.name());
		pushMessage.setStatus(InAppMessageStatus.SUCCESS.name());

		InAppMessageEntity recalledPull = new InAppMessageEntity();
		recalledPull.setId(22L);
		recalledPull.setRecipientScopeType(RecipientScopeType.ALL.name());
		recalledPull.setStatus(InAppMessageStatus.RECALLED.name());

		when(inAppMessageMapper.selectList(any(Wrapper.class))).thenReturn(List.of(pushMessage, recalledPull));

		try (MockedStatic<SecurityUserUtils> security = mockStatic(SecurityUserUtils.class)) {
			security.when(SecurityUserUtils::getUserId).thenReturn(100L);

			service.markRead(Arrays.asList(21L, null, 21L, 22L));
		}

		verify(inAppMessageRecipientMapper).markRead(eq(100L), eq(List.of(21L)), any(Instant.class));
		verify(inAppMessageUserStatusMapper, never()).upsertReadBatch(anyList());
	}

	@Test
	@DisplayName("标已读：仅读扩散可读时不更新收件行")
	void markRead_shouldOnlyUpsertUserStatusWhenNoPush() {
		// 只有公开可读消息时跳过 recipient 更新
		InAppMessageEntity pullMessage = new InAppMessageEntity();
		pullMessage.setId(31L);
		pullMessage.setRecipientScopeType(RecipientScopeType.ALL.name());
		pullMessage.setStatus(InAppMessageStatus.SUCCESS.name());
		when(inAppMessageMapper.selectList(any(Wrapper.class))).thenReturn(List.of(pullMessage));

		try (MockedStatic<SecurityUserUtils> security = mockStatic(SecurityUserUtils.class)) {
			security.when(SecurityUserUtils::getUserId).thenReturn(100L);

			service.markRead(List.of(31L));
		}

		verify(inAppMessageRecipientMapper, never()).markRead(any(), anyList(), any(Instant.class));
		verify(inAppMessageUserStatusMapper).upsertReadBatch(anyList());
	}

	@Test
	@DisplayName("标已读：空列表直接返回")
	void markRead_shouldNoOpWhenMessageIdsEmpty() {
		// 空入参不访问写 Mapper
		try (MockedStatic<SecurityUserUtils> security = mockStatic(SecurityUserUtils.class)) {
			security.when(SecurityUserUtils::getUserId).thenReturn(100L);

			service.markRead(List.of());
		}

		verifyNoInteractions(inAppMessageRecipientMapper, inAppMessageMapper, inAppMessageUserStatusMapper);
	}

	@Test
	@DisplayName("当前大类全部已读：查出未读 ID 后按范围分流标已读")
	void markAllRead_shouldLoadUnreadIdsThenMarkRead() {
		// 大类未读 ID 由 Mapper 圈定，再走与批量已读相同的写扩散/读扩散分流
		Long majorCategoryId = 1L;
		when(inAppInboxMapper.selectUnreadMessageIdsByMajor(100L, majorCategoryId)).thenReturn(List.of(11L, 13L));

		InAppMessageEntity pushMessage = new InAppMessageEntity();
		pushMessage.setId(11L);
		pushMessage.setRecipientScopeType(RecipientScopeType.USER.name());
		pushMessage.setStatus(InAppMessageStatus.SUCCESS.name());

		InAppMessageEntity pullMessage = new InAppMessageEntity();
		pullMessage.setId(13L);
		pullMessage.setRecipientScopeType(RecipientScopeType.ALL.name());
		pullMessage.setStatus(InAppMessageStatus.SUCCESS.name());

		when(inAppMessageMapper.selectList(any(Wrapper.class))).thenReturn(List.of(pushMessage, pullMessage));

		try (MockedStatic<SecurityUserUtils> security = mockStatic(SecurityUserUtils.class)) {
			security.when(SecurityUserUtils::getUserId).thenReturn(100L);

			service.markAllRead(majorCategoryId);
		}

		verify(inAppInboxMapper).selectUnreadMessageIdsByMajor(100L, majorCategoryId);
		verify(inAppMessageRecipientMapper).markRead(eq(100L), eq(List.of(11L)), any(Instant.class));
		verify(inAppMessageUserStatusMapper).upsertReadBatch(anyList());
	}

	@Test
	@DisplayName("当前大类全部已读：无未读时不写库")
	void markAllRead_shouldNoOpWhenNoUnread() {
		// 大类下无未读时不访问主表与写 Mapper
		Long majorCategoryId = 1L;
		when(inAppInboxMapper.selectUnreadMessageIdsByMajor(100L, majorCategoryId)).thenReturn(List.of());

		try (MockedStatic<SecurityUserUtils> security = mockStatic(SecurityUserUtils.class)) {
			security.when(SecurityUserUtils::getUserId).thenReturn(100L);

			service.markAllRead(majorCategoryId);
		}

		verify(inAppInboxMapper).selectUnreadMessageIdsByMajor(100L, majorCategoryId);
		verifyNoInteractions(inAppMessageRecipientMapper, inAppMessageMapper, inAppMessageUserStatusMapper);
	}

	@Test
	@DisplayName("当前大类全部已读：未读 ID 超过批次时分片调用标已读")
	void markAllRead_shouldSplitBatchesWhenUnreadExceedsBatchSize() {
		// 501 条按 INBOX_MARK_READ=500 拆成 2 批，每批都会走 resolveActionableIds
		Long majorCategoryId = 1L;
		List<Long> unreadIds = new java.util.ArrayList<>(501);
		for (long id = 1L; id <= 501L; id++) {
			unreadIds.add(id);
		}
		when(inAppInboxMapper.selectUnreadMessageIdsByMajor(100L, majorCategoryId)).thenReturn(unreadIds);
		when(inAppMessageMapper.selectList(any(Wrapper.class))).thenReturn(List.of());

		try (MockedStatic<SecurityUserUtils> security = mockStatic(SecurityUserUtils.class)) {
			security.when(SecurityUserUtils::getUserId).thenReturn(100L);

			service.markAllRead(majorCategoryId);
		}

		verify(inAppInboxMapper).selectUnreadMessageIdsByMajor(100L, majorCategoryId);
		verify(inAppMessageMapper, times(2)).selectList(any(Wrapper.class));
		verifyNoInteractions(inAppMessageRecipientMapper, inAppMessageUserStatusMapper);
	}

	@Test
	@DisplayName("批量删除：按范围分流，写扩散软删收件行、读扩散懒写状态")
	void batchDelete_shouldSplitPushUpdateAndPullUpsert() {
		// 先查主表分组：写扩散只更新 recipient，读扩散只 upsert user_status
		InAppMessageEntity pushMessage = new InAppMessageEntity();
		pushMessage.setId(11L);
		pushMessage.setRecipientScopeType(RecipientScopeType.USER.name());
		pushMessage.setStatus(InAppMessageStatus.SUCCESS.name());

		InAppMessageEntity pushPartial = new InAppMessageEntity();
		pushPartial.setId(12L);
		pushPartial.setRecipientScopeType(RecipientScopeType.USER.name());
		pushPartial.setStatus(InAppMessageStatus.PARTIAL.name());

		InAppMessageEntity pullMessage = new InAppMessageEntity();
		pullMessage.setId(13L);
		pullMessage.setRecipientScopeType(RecipientScopeType.ALL.name());
		pullMessage.setStatus(InAppMessageStatus.SUCCESS.name());

		when(inAppMessageMapper.selectList(any(Wrapper.class)))
			.thenReturn(List.of(pushMessage, pushPartial, pullMessage));

		try (MockedStatic<SecurityUserUtils> security = mockStatic(SecurityUserUtils.class)) {
			security.when(SecurityUserUtils::getUserId).thenReturn(100L);

			service.batchDelete(List.of(11L, 12L, 13L));
		}

		verify(inAppMessageRecipientMapper).markDeleted(100L, List.of(11L, 12L));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<InAppMessageUserStatusEntity>> rowsCaptor = ArgumentCaptor.forClass(List.class);
		verify(inAppMessageUserStatusMapper).upsertDeletedBatch(rowsCaptor.capture());
		List<InAppMessageUserStatusEntity> rows = rowsCaptor.getValue();
		assertThat(rows).hasSize(1);
		InAppMessageUserStatusEntity row = rows.get(0);
		assertThat(row.getId()).isNotNull();
		assertThat(row.getMessageId()).isEqualTo(13L);
		assertThat(row.getUserId()).isEqualTo(100L);
		assertThat(row.getIsRead()).isFalse();
		assertThat(row.getReadTime()).isNull();
		assertThat(row.getIsDeleted()).isTrue();
		assertThat(row.getCreatedBy()).isEqualTo(100L);
		assertThat(row.getUpdatedBy()).isEqualTo(100L);
		assertThat(row.getVersion()).isZero();
	}

	@Test
	@DisplayName("批量删除：去重后仅写扩散可删时只更新收件行")
	void batchDelete_shouldOnlyUpdateRecipientWhenNoPull() {
		// 重复 ID 去重；只有定向可删消息时不写 user_status
		InAppMessageEntity pushMessage = new InAppMessageEntity();
		pushMessage.setId(21L);
		pushMessage.setRecipientScopeType(RecipientScopeType.USER.name());
		pushMessage.setStatus(InAppMessageStatus.SUCCESS.name());

		InAppMessageEntity recalledPull = new InAppMessageEntity();
		recalledPull.setId(22L);
		recalledPull.setRecipientScopeType(RecipientScopeType.ALL.name());
		recalledPull.setStatus(InAppMessageStatus.RECALLED.name());

		when(inAppMessageMapper.selectList(any(Wrapper.class))).thenReturn(List.of(pushMessage, recalledPull));

		try (MockedStatic<SecurityUserUtils> security = mockStatic(SecurityUserUtils.class)) {
			security.when(SecurityUserUtils::getUserId).thenReturn(100L);

			service.batchDelete(Arrays.asList(21L, null, 21L, 22L));
		}

		verify(inAppMessageRecipientMapper).markDeleted(100L, List.of(21L));
		verify(inAppMessageUserStatusMapper, never()).upsertDeletedBatch(anyList());
	}

	@Test
	@DisplayName("批量删除：仅读扩散可删时不更新收件行")
	void batchDelete_shouldOnlyUpsertUserStatusWhenNoPush() {
		// 只有公开可删消息时跳过 recipient 更新
		InAppMessageEntity pullMessage = new InAppMessageEntity();
		pullMessage.setId(31L);
		pullMessage.setRecipientScopeType(RecipientScopeType.ALL.name());
		pullMessage.setStatus(InAppMessageStatus.SUCCESS.name());
		when(inAppMessageMapper.selectList(any(Wrapper.class))).thenReturn(List.of(pullMessage));

		try (MockedStatic<SecurityUserUtils> security = mockStatic(SecurityUserUtils.class)) {
			security.when(SecurityUserUtils::getUserId).thenReturn(100L);

			service.batchDelete(List.of(31L));
		}

		verify(inAppMessageRecipientMapper, never()).markDeleted(any(), anyList());
		verify(inAppMessageUserStatusMapper).upsertDeletedBatch(anyList());
	}

	@Test
	@DisplayName("批量删除：空列表直接返回")
	void batchDelete_shouldNoOpWhenMessageIdsEmpty() {
		// 空入参不访问写 Mapper
		try (MockedStatic<SecurityUserUtils> security = mockStatic(SecurityUserUtils.class)) {
			security.when(SecurityUserUtils::getUserId).thenReturn(100L);

			service.batchDelete(List.of());
		}

		verifyNoInteractions(inAppMessageRecipientMapper, inAppMessageMapper, inAppMessageUserStatusMapper);
	}

	@Test
	@DisplayName("当前大类全部删除：查出可见 ID 后按范围分流软删")
	void deleteAll_shouldLoadVisibleIdsThenBatchDelete() {
		// 大类可见 ID 由 Mapper 圈定，再走与批量删除相同的写扩散/读扩散分流
		Long majorCategoryId = 1L;
		when(inAppInboxMapper.selectVisibleMessageIdsByMajor(100L, majorCategoryId)).thenReturn(List.of(11L, 13L));

		InAppMessageEntity pushMessage = new InAppMessageEntity();
		pushMessage.setId(11L);
		pushMessage.setRecipientScopeType(RecipientScopeType.USER.name());
		pushMessage.setStatus(InAppMessageStatus.SUCCESS.name());

		InAppMessageEntity pullMessage = new InAppMessageEntity();
		pullMessage.setId(13L);
		pullMessage.setRecipientScopeType(RecipientScopeType.ALL.name());
		pullMessage.setStatus(InAppMessageStatus.SUCCESS.name());

		when(inAppMessageMapper.selectList(any(Wrapper.class))).thenReturn(List.of(pushMessage, pullMessage));

		try (MockedStatic<SecurityUserUtils> security = mockStatic(SecurityUserUtils.class)) {
			security.when(SecurityUserUtils::getUserId).thenReturn(100L);

			service.deleteAll(majorCategoryId);
		}

		verify(inAppInboxMapper).selectVisibleMessageIdsByMajor(100L, majorCategoryId);
		verify(inAppMessageRecipientMapper).markDeleted(100L, List.of(11L));
		verify(inAppMessageUserStatusMapper).upsertDeletedBatch(anyList());
	}

	@Test
	@DisplayName("当前大类全部删除：无可见消息时不写库")
	void deleteAll_shouldNoOpWhenNoVisible() {
		// 大类下无可删消息时不访问主表与写 Mapper
		Long majorCategoryId = 1L;
		when(inAppInboxMapper.selectVisibleMessageIdsByMajor(100L, majorCategoryId)).thenReturn(List.of());

		try (MockedStatic<SecurityUserUtils> security = mockStatic(SecurityUserUtils.class)) {
			security.when(SecurityUserUtils::getUserId).thenReturn(100L);

			service.deleteAll(majorCategoryId);
		}

		verify(inAppInboxMapper).selectVisibleMessageIdsByMajor(100L, majorCategoryId);
		verifyNoInteractions(inAppMessageRecipientMapper, inAppMessageMapper, inAppMessageUserStatusMapper);
	}

	@Test
	@DisplayName("当前大类全部删除：可见 ID 超过批次时分片调用批量删除")
	void deleteAll_shouldSplitBatchesWhenVisibleExceedsBatchSize() {
		// 501 条按 INBOX_DELETE=500 拆成 2 批，每批都会走 resolveActionableIds
		Long majorCategoryId = 1L;
		List<Long> visibleIds = new java.util.ArrayList<>(501);
		for (long id = 1L; id <= 501L; id++) {
			visibleIds.add(id);
		}
		when(inAppInboxMapper.selectVisibleMessageIdsByMajor(100L, majorCategoryId)).thenReturn(visibleIds);
		when(inAppMessageMapper.selectList(any(Wrapper.class))).thenReturn(List.of());

		try (MockedStatic<SecurityUserUtils> security = mockStatic(SecurityUserUtils.class)) {
			security.when(SecurityUserUtils::getUserId).thenReturn(100L);

			service.deleteAll(majorCategoryId);
		}

		verify(inAppInboxMapper).selectVisibleMessageIdsByMajor(100L, majorCategoryId);
		verify(inAppMessageMapper, times(2)).selectList(any(Wrapper.class));
		verifyNoInteractions(inAppMessageRecipientMapper, inAppMessageUserStatusMapper);
	}

	@Test
	@DisplayName("详情：当前用户可见时返回正文并调用 markRead")
	void getInboxDetail_shouldReturnContentAndMarkRead() {
		InAppInboxDetailRowPO row = new InAppInboxDetailRowPO();
		row.setId(11L);
		row.setTitle("定向通知");
		row.setContent("push-body");
		row.setContentType(MessageContentType.MARKDOWN.name());
		row.setCategoryId(104L);
		row.setCategoryName("一般通知");
		row.setIsRead(false);

		InAppMessageEntity message = new InAppMessageEntity();
		message.setId(11L);
		message.setRecipientScopeType(RecipientScopeType.USER.name());
		message.setStatus(InAppMessageStatus.SUCCESS.name());

		when(inAppInboxMapper.selectInboxDetail(100L, 11L)).thenReturn(row);
		when(inAppMessageMapper.selectList(any(Wrapper.class))).thenReturn(List.of(message));

		try (MockedStatic<SecurityUserUtils> security = mockStatic(SecurityUserUtils.class)) {
			security.when(SecurityUserUtils::getUserId).thenReturn(100L);

			InAppInboxDetailVO vo = service.getInboxDetail(11L);

			assertThat(vo.getId()).isEqualTo(11L);
			assertThat(vo.getContent()).isEqualTo("push-body");
			assertThat(vo.getCategoryName()).isEqualTo("一般通知");
			assertThat(vo.getIsRead()).isTrue();
			assertThat(vo.getReadTime()).isNotNull();
		}

		verify(inAppMessageRecipientMapper).markRead(eq(100L), eq(List.of(11L)), any(Instant.class));
	}

	@Test
	@DisplayName("详情：读扩散消息返回正文并懒写已读")
	void getInboxDetail_shouldMarkPullRead() {
		InAppInboxDetailRowPO row = new InAppInboxDetailRowPO();
		row.setId(13L);
		row.setTitle("公开公告");
		row.setContent("pull-body");
		row.setContentType(MessageContentType.TEXT.name());
		row.setIsRead(false);

		InAppMessageEntity message = new InAppMessageEntity();
		message.setId(13L);
		message.setRecipientScopeType(RecipientScopeType.ALL.name());
		message.setStatus(InAppMessageStatus.SUCCESS.name());

		when(inAppInboxMapper.selectInboxDetail(100L, 13L)).thenReturn(row);
		when(inAppMessageMapper.selectList(any(Wrapper.class))).thenReturn(List.of(message));

		try (MockedStatic<SecurityUserUtils> security = mockStatic(SecurityUserUtils.class)) {
			security.when(SecurityUserUtils::getUserId).thenReturn(100L);

			InAppInboxDetailVO vo = service.getInboxDetail(13L);

			assertThat(vo.getContent()).isEqualTo("pull-body");
			assertThat(vo.getIsRead()).isTrue();
		}

		verify(inAppMessageUserStatusMapper).upsertReadBatch(anyList());
	}

	@Test
	@DisplayName("详情：对当前用户不可见时抛出 DATA_NOT_EXIST")
	void getInboxDetail_shouldRejectWhenNotVisible() {
		when(inAppInboxMapper.selectInboxDetail(100L, 999L)).thenReturn(null);

		try (MockedStatic<SecurityUserUtils> security = mockStatic(SecurityUserUtils.class)) {
			security.when(SecurityUserUtils::getUserId).thenReturn(100L);

			assertThatThrownBy(() -> service.getInboxDetail(999L)).isInstanceOf(MessageException.class)
				.extracting(ex -> ((MessageException) ex).getResultCode())
				.isEqualTo(DATA_NOT_EXIST);
		}

		verify(inAppMessageRecipientMapper, never()).markRead(any(), anyList(), any(Instant.class));
		verify(inAppMessageUserStatusMapper, never()).upsertReadBatch(anyList());
	}

}

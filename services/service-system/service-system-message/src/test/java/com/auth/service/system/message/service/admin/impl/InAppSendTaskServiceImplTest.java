package com.auth.service.system.message.service.admin.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.module.message.api.model.enums.MessageContentType;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.mapper.InAppMessageMapper;
import com.auth.service.system.message.mapper.InAppMessageRecipientMapper;
import com.auth.service.system.message.mapper.InAppMessageUserStatusMapper;
import com.auth.service.system.message.model.entity.InAppMessageEntity;
import com.auth.service.system.message.model.enums.InAppMessageStatus;
import com.auth.service.system.message.model.enums.MessageSendSourceType;
import com.auth.service.system.message.model.enums.RecipientScopeType;
import com.auth.service.system.message.model.form.inapp.InAppComposeForm;
import com.auth.service.system.message.model.po.InAppSendTaskPageRowPO;
import com.auth.service.system.message.model.po.InAppSendTaskRecipientPageRowPO;
import com.auth.service.system.message.model.query.InAppSendTaskQuery;
import com.auth.service.system.message.model.query.InAppSendTaskRecipientQuery;
import com.auth.service.system.message.model.vo.inapp.InAppComposeResultVO;
import com.auth.service.system.message.model.vo.inapp.InAppSendTaskDetailVO;
import com.auth.service.system.message.model.vo.inapp.InAppSendTaskPageVO;
import com.auth.service.system.message.model.vo.inapp.InAppSendTaskRecipientPageVO;
import com.auth.service.system.message.service.admin.InAppMessageCategoryService;
import com.auth.service.system.message.support.inapp.InAppComposeDispatchTrigger;
import com.auth.service.system.message.support.inapp.InAppMessageCategorySupport;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.auth.service.system.message.exception.MessageResultCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link InAppSendTaskServiceImpl} 单元测试
 *
 * @author Bunny
 */
@DisplayName("InAppSendTaskServiceImpl 管理端按范围发送站内信")
@ExtendWith(MockitoExtension.class)
class InAppSendTaskServiceImplTest {

	private static final long MINOR_CATEGORY_ID = 104L;

	@Mock
	private InAppMessageMapper inAppMessageMapper;

	@Mock
	private InAppComposeDispatchTrigger dispatchTrigger;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@Mock
	private InAppMessageCategoryService inAppMessageCategoryService;

	@Mock
	private InAppMessageCategorySupport categorySupport;

	@Mock
	private InAppMessageRecipientMapper inAppMessageRecipientMapper;

	@Mock
	private InAppMessageUserStatusMapper inAppMessageUserStatusMapper;

	private InAppSendTaskServiceImpl service;

	/**
	 * 组装可补发校验用的管理端定向写扩散任务
	 */
	private static InAppMessageEntity adminComposePushTask(Long id, InAppMessageStatus status) {
		InAppMessageEntity task = new InAppMessageEntity();
		task.setId(id);
		task.setSourceType(MessageSendSourceType.ADMIN_COMPOSE.name());
		task.setRecipientScopeType(RecipientScopeType.DEPT.name());
		task.setStatus(status.name());
		return task;
	}

	@BeforeEach
	void setUp() {
		// ServiceImpl.baseMapper 由 Spring 字段注入；单测手动挂上
		service = new InAppSendTaskServiceImpl(dispatchTrigger, auditUserDisplayService, inAppMessageCategoryService,
				categorySupport, inAppMessageRecipientMapper, inAppMessageUserStatusMapper);
		ReflectionTestUtils.setField(service, "baseMapper", inAppMessageMapper);
	}

	@Test
	@DisplayName("分页：映射业务字段并填充审计用户名")
	void getSendTaskPage_shouldMapFieldsAndEnrichAudit() {
		// 准备筛选条件与 Mapper 投影行，校验列表 VO 映射与审计填充
		InAppSendTaskQuery query = new InAppSendTaskQuery();
		query.setStatus(InAppMessageStatus.SUCCESS.name());
		query.setSourceType(MessageSendSourceType.ADMIN_COMPOSE.name());
		query.setRecipientScopeType(RecipientScopeType.DEPT.name());

		query.setCategoryId(MINOR_CATEGORY_ID);
		query.setTitle("公告");
		query.setPageIndex(1);
		query.setPageSize(10);

		Instant now = LocalDateTime.of(2026, 7, 19, 12, 0, 0).toInstant(java.time.ZoneOffset.UTC);
		InAppSendTaskPageRowPO row = new InAppSendTaskPageRowPO();
		row.setId(1L);
		row.setSourceType(MessageSendSourceType.ADMIN_COMPOSE.name());
		row.setSceneCode("notice");
		row.setTitle("系统公告");
		row.setContentType(MessageContentType.MARKDOWN.name());
		row.setCategoryId(MINOR_CATEGORY_ID);
		row.setCategoryName("一般通知");
		row.setSenderUserId(7L);
		row.setRecipientScopeType(RecipientScopeType.DEPT.name());

		row.setTotalCount(10);
		row.setSuccessCount(9);
		row.setFailCount(1);
		row.setStatus(InAppMessageStatus.PARTIAL.name());
		row.setRecalledAt(null);
		row.setCreatedAt(now);
		row.setUpdatedAt(now);
		row.setCreatedBy(1L);
		row.setUpdatedBy(1L);

		Page<InAppSendTaskPageRowPO> mapperPage = new Page<>(1, 10);
		mapperPage.setRecords(List.of(row));
		mapperPage.setTotal(1);
		when(inAppMessageMapper.selectSendTaskPage(any(Page.class), eq(query))).thenReturn(mapperPage);

		PageResponse<InAppSendTaskPageVO> result = service.getSendTaskPage(query);

		assertThat(result.getList()).hasSize(1);
		InAppSendTaskPageVO vo = result.getList().get(0);
		assertThat(vo.getId()).isEqualTo(1L);
		assertThat(vo.getSourceType()).isEqualTo(MessageSendSourceType.ADMIN_COMPOSE.name());
		assertThat(vo.getSceneCode()).isEqualTo("notice");
		assertThat(vo.getTitle()).isEqualTo("系统公告");
		assertThat(vo.getContentType()).isEqualTo(MessageContentType.MARKDOWN.name());
		assertThat(vo.getCategoryId()).isEqualTo(MINOR_CATEGORY_ID);
		assertThat(vo.getCategoryName()).isEqualTo("一般通知");
		assertThat(vo.getSenderUserId()).isEqualTo(7L);
		assertThat(vo.getRecipientScopeType()).isEqualTo(RecipientScopeType.DEPT.name());

		assertThat(vo.getTotalCount()).isEqualTo(10);
		assertThat(vo.getSuccessCount()).isEqualTo(9);
		assertThat(vo.getFailCount()).isEqualTo(1);
		assertThat(vo.getStatus()).isEqualTo(InAppMessageStatus.PARTIAL.name());
		assertThat(vo.getRecalledAt()).isNull();
		verify(auditUserDisplayService).enrichAuditUsernames(any(IPage.class), isNull(), isNull());

		ArgumentCaptor<InAppSendTaskQuery> queryCaptor = ArgumentCaptor.forClass(InAppSendTaskQuery.class);
		verify(inAppMessageMapper).selectSendTaskPage(any(Page.class), queryCaptor.capture());
		assertThat(queryCaptor.getValue().getStatus()).isEqualTo(InAppMessageStatus.SUCCESS.name());
		assertThat(queryCaptor.getValue().getTitle()).isEqualTo("公告");
	}

	@Test
	@DisplayName("详情：按主键返回正文与范围解析并填充审计用户名")
	void getSendTaskById_shouldReturnDetailAndEnrichAudit() {
		// 准备含部门范围快照的任务，校验详情含列表未暴露的正文/链接/撤回人，并解析 ids
		Instant now = LocalDateTime.of(2026, 7, 19, 12, 30, 0).toInstant(java.time.ZoneOffset.UTC);
		InAppMessageEntity task = new InAppMessageEntity();
		task.setId(11L);
		task.setSourceType(MessageSendSourceType.ADMIN_COMPOSE.name());
		task.setSceneCode("notice");
		task.setTitle("系统公告");
		task.setContentType(MessageContentType.MARKDOWN.name());
		task.setContent("## hello");
		task.setCategoryId(MINOR_CATEGORY_ID);
		task.setLinkUrl("/dashboard");
		task.setSenderUserId(7L);
		task.setRecipientScopeType(RecipientScopeType.DEPT.name());
		task.setRecipientScopeJson("{\"ids\":[3,4],\"includeChildren\":false}");
		task.setTotalCount(2);
		task.setSuccessCount(2);
		task.setFailCount(0);
		task.setStatus(InAppMessageStatus.SUCCESS.name());
		task.setRecalledAt(null);
		task.setRecallUserId(null);
		task.setRemark("detail-check");
		task.setCreatedAt(now);
		task.setUpdatedAt(now);
		task.setCreatedBy(1L);
		task.setUpdatedBy(1L);
		when(inAppMessageMapper.selectById(11L)).thenReturn(task);
		com.auth.service.system.message.model.entity.InAppMessageCategoryEntity category = new com.auth.service.system.message.model.entity.InAppMessageCategoryEntity();
		category.setId(MINOR_CATEGORY_ID);
		category.setName("一般通知");
		when(inAppMessageCategoryService.getById(MINOR_CATEGORY_ID)).thenReturn(category);

		InAppSendTaskDetailVO vo = service.getSendTaskById(11L);

		assertThat(vo.getId()).isEqualTo(11L);
		assertThat(vo.getTitle()).isEqualTo("系统公告");
		assertThat(vo.getContent()).isEqualTo("## hello");
		assertThat(vo.getCategoryId()).isEqualTo(MINOR_CATEGORY_ID);
		assertThat(vo.getCategoryName()).isEqualTo("一般通知");
		assertThat(vo.getLinkUrl()).isEqualTo("/dashboard");
		assertThat(vo.getRecipientScopeType()).isEqualTo(RecipientScopeType.DEPT.name());
		assertThat(vo.getRecipientScopeJson()).isEqualTo("{\"ids\":[3,4],\"includeChildren\":false}");
		assertThat(vo.getRecipientScopeIds()).containsExactly(3L, 4L);
		assertThat(vo.getIncludeChildren()).isFalse();
		assertThat(vo.getStatus()).isEqualTo(InAppMessageStatus.SUCCESS.name());
		assertThat(vo.getRemark()).isEqualTo("detail-check");
		verify(auditUserDisplayService).enrichAuditUsernames(any(List.class), isNull(), isNull());
	}

	@Test
	@DisplayName("详情：ALL 任务范围 ID 为空且无 includeChildren")
	void getSendTaskById_shouldParseAllScopeWithoutIds() {
		// 全员读扩散无范围快照，解析结果为空列表
		InAppMessageEntity task = new InAppMessageEntity();
		task.setId(12L);
		task.setSourceType(MessageSendSourceType.ADMIN_COMPOSE.name());
		task.setTitle("全员公告");
		task.setContentType(MessageContentType.TEXT.name());
		task.setContent("all users");
		task.setRecipientScopeType(RecipientScopeType.ALL.name());
		task.setRecipientScopeJson(null);
		task.setRecipientScopeType(RecipientScopeType.ALL.name());
		task.setStatus(InAppMessageStatus.SUCCESS.name());
		when(inAppMessageMapper.selectById(12L)).thenReturn(task);

		InAppSendTaskDetailVO vo = service.getSendTaskById(12L);

		assertThat(vo.getRecipientScopeType()).isEqualTo(RecipientScopeType.ALL.name());
		assertThat(vo.getRecipientScopeIds()).isEmpty();
		assertThat(vo.getIncludeChildren()).isNull();

	}

	@Test
	@DisplayName("详情：任务不存在时抛出 IN_APP_SEND_TASK_NOT_FOUND")
	void getSendTaskById_shouldThrowWhenMissing() {
		// 主键无对应行时抛业务异常
		when(inAppMessageMapper.selectById(999L)).thenReturn(null);

		assertThatThrownBy(() -> service.getSendTaskById(999L)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_SEND_TASK_NOT_FOUND);
	}

	@Test
	@DisplayName("收件人分页：写扩散查 recipient 并填充用户名")
	void getRecipientPage_shouldQueryPushRecipientsAndEnrichUsername() {
		InAppMessageEntity task = adminComposePushTask(50L, InAppMessageStatus.SUCCESS);
		when(inAppMessageMapper.selectById(50L)).thenReturn(task);

		InAppSendTaskRecipientPageRowPO row = new InAppSendTaskRecipientPageRowPO();
		row.setId(501L);
		row.setMessageId(50L);
		row.setUserId(88L);
		row.setIsRead(true);
		row.setReadTime(LocalDateTime.of(2026, 7, 23, 10, 0, 0).toInstant(java.time.ZoneOffset.UTC));
		row.setIsDeleted(false);
		row.setCreatedAt(LocalDateTime.of(2026, 7, 23, 9, 0, 0).toInstant(java.time.ZoneOffset.UTC));
		row.setUpdatedAt(LocalDateTime.of(2026, 7, 23, 10, 0, 0).toInstant(java.time.ZoneOffset.UTC));
		row.setCreatedBy(1L);
		row.setUpdatedBy(88L);

		Page<InAppSendTaskRecipientPageRowPO> mapperPage = new Page<>(1, 10);
		mapperPage.setRecords(List.of(row));
		mapperPage.setTotal(1);
		when(inAppMessageRecipientMapper.selectRecipientPage(any(Page.class), eq(50L),
				any(InAppSendTaskRecipientQuery.class)))
			.thenReturn(mapperPage);

		InAppSendTaskRecipientQuery query = new InAppSendTaskRecipientQuery();
		query.setPageIndex(1);
		query.setPageSize(10);
		query.setUserId(88L);
		query.setIsRead(true);

		PageResponse<InAppSendTaskRecipientPageVO> result = service.getRecipientPage(50L, query);

		assertThat(result.getList()).hasSize(1);
		InAppSendTaskRecipientPageVO vo = result.getList().get(0);
		assertThat(vo.getId()).isEqualTo(501L);
		assertThat(vo.getMessageId()).isEqualTo(50L);
		assertThat(vo.getUserId()).isEqualTo(88L);
		assertThat(vo.getIsRead()).isTrue();
		assertThat(vo.getIsDeleted()).isFalse();
		verify(inAppMessageRecipientMapper).selectRecipientPage(any(Page.class), eq(50L),
				any(InAppSendTaskRecipientQuery.class));
		verify(inAppMessageUserStatusMapper, never()).selectUserStatusPage(any(Page.class), anyLong(),
				any(InAppSendTaskRecipientQuery.class));
		verify(auditUserDisplayService).enrichAuditUsernames(any(IPage.class), any(), any());
	}

	@Test
	@DisplayName("收件人分页：读扩散查 user_status")
	void getRecipientPage_shouldQueryPullUserStatus() {
		InAppMessageEntity task = new InAppMessageEntity();
		task.setId(51L);
		task.setRecipientScopeType(RecipientScopeType.ALL.name());
		task.setStatus(InAppMessageStatus.SUCCESS.name());
		when(inAppMessageMapper.selectById(51L)).thenReturn(task);

		InAppSendTaskRecipientPageRowPO row = new InAppSendTaskRecipientPageRowPO();
		row.setId(601L);
		row.setMessageId(51L);
		row.setUserId(99L);
		row.setIsRead(true);
		row.setIsDeleted(false);

		Page<InAppSendTaskRecipientPageRowPO> mapperPage = new Page<>(1, 10);
		mapperPage.setRecords(List.of(row));
		mapperPage.setTotal(1);
		when(inAppMessageUserStatusMapper.selectUserStatusPage(any(Page.class), eq(51L),
				any(InAppSendTaskRecipientQuery.class)))
			.thenReturn(mapperPage);

		InAppSendTaskRecipientQuery query = new InAppSendTaskRecipientQuery();
		query.setPageIndex(1);
		query.setPageSize(10);

		PageResponse<InAppSendTaskRecipientPageVO> result = service.getRecipientPage(51L, query);

		assertThat(result.getList()).hasSize(1);
		assertThat(result.getList().get(0).getUserId()).isEqualTo(99L);
		verify(inAppMessageUserStatusMapper).selectUserStatusPage(any(Page.class), eq(51L),
				any(InAppSendTaskRecipientQuery.class));
		verify(inAppMessageRecipientMapper, never()).selectRecipientPage(any(Page.class), anyLong(),
				any(InAppSendTaskRecipientQuery.class));
	}

	@Test
	@DisplayName("收件人分页：任务不存在时抛出 IN_APP_SEND_TASK_NOT_FOUND")
	void getRecipientPage_shouldThrowWhenTaskMissing() {
		when(inAppMessageMapper.selectById(404L)).thenReturn(null);
		InAppSendTaskRecipientQuery query = new InAppSendTaskRecipientQuery();
		query.setPageIndex(1);
		query.setPageSize(10);

		assertThatThrownBy(() -> service.getRecipientPage(404L, query)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_SEND_TASK_NOT_FOUND);
		verifyNoInteractions(inAppMessageRecipientMapper, inAppMessageUserStatusMapper);
	}

	@Test
	@DisplayName("收件人分页：范围类型非法时抛出 IN_APP_RECIPIENT_SCOPE_INVALID")
	void getRecipientPage_shouldThrowWhenScopeInvalid() {
		InAppMessageEntity task = new InAppMessageEntity();
		task.setId(52L);
		task.setRecipientScopeType("UNKNOWN");
		when(inAppMessageMapper.selectById(52L)).thenReturn(task);
		InAppSendTaskRecipientQuery query = new InAppSendTaskRecipientQuery();
		query.setPageIndex(1);
		query.setPageSize(10);

		assertThatThrownBy(() -> service.getRecipientPage(52L, query)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_RECIPIENT_SCOPE_INVALID);
		verifyNoInteractions(inAppMessageRecipientMapper, inAppMessageUserStatusMapper);
	}

	@Test
	@DisplayName("分页：无匹配数据时返回空列表")
	void getSendTaskPage_shouldReturnEmptyWhenNoMatch() {
		// 无筛选命中时交由 SQL 返回空页
		InAppSendTaskQuery query = new InAppSendTaskQuery();
		query.setStatus("UNKNOWN");

		Page<InAppSendTaskPageRowPO> mapperPage = new Page<>(1, 30);
		mapperPage.setRecords(Collections.emptyList());
		mapperPage.setTotal(0);
		when(inAppMessageMapper.selectSendTaskPage(any(Page.class), eq(query))).thenReturn(mapperPage);

		PageResponse<InAppSendTaskPageVO> result = service.getSendTaskPage(query);

		assertThat(result.getList()).isEmpty();
		assertThat(result.getTotal()).isZero();
		verify(auditUserDisplayService).enrichAuditUsernames(any(IPage.class), isNull(), isNull());
	}

	@Test
	@DisplayName("定向写扩散：写 PENDING 任务并 afterCommit 派发")
	void send_pushScope_shouldCreatePendingTaskAndDispatch() {
		// 校验落库字段与派发时机：定向受理即 PENDING，实投由异步回写
		InAppComposeForm form = new InAppComposeForm();
		form.setRecipientScopeType(RecipientScopeType.DEPT.name());
		form.setRecipientScopeIds(List.of(3L));
		form.setIncludeChildren(true);
		form.setTemplateCode("notice");
		form.setTitle("Hello");
		form.setBody("body text");
		form.setContentType("MARKDOWN");
		form.setCategoryId(MINOR_CATEGORY_ID);
		form.setLinkUrl("/home");

		doAnswer(invocation -> {
			InAppMessageEntity task = invocation.getArgument(0);
			task.setId(9001L);
			return 1;
		}).when(inAppMessageMapper).insert(any(InAppMessageEntity.class));
		when(categorySupport.requireEnabledMinor(MINOR_CATEGORY_ID))
			.thenReturn(new com.auth.service.system.message.model.entity.InAppMessageCategoryEntity());

		try (MockedStatic<SecurityUserUtils> security = mockStatic(SecurityUserUtils.class)) {
			security.when(SecurityUserUtils::getUserId).thenReturn(7L);

			InAppComposeResultVO result = service.send(form);

			assertThat(result.getTaskId()).isEqualTo(9001L);
			assertThat(result.getTotalCount()).isZero();
			assertThat(result.getSuccessCount()).isZero();
			assertThat(result.getStatus()).isEqualTo(InAppMessageStatus.PENDING.name());
		}

		ArgumentCaptor<InAppMessageEntity> taskCaptor = ArgumentCaptor.forClass(InAppMessageEntity.class);
		verify(inAppMessageMapper).insert(taskCaptor.capture());
		InAppMessageEntity task = taskCaptor.getValue();
		assertThat(task.getSourceType()).isEqualTo(MessageSendSourceType.ADMIN_COMPOSE.name());
		assertThat(task.getSceneCode()).isEqualTo("notice");
		assertThat(task.getTitle()).isEqualTo("Hello");
		assertThat(task.getContent()).isEqualTo("body text");
		assertThat(task.getContentType()).isEqualTo(MessageContentType.MARKDOWN.name());
		assertThat(task.getRecipientScopeType()).isEqualTo(RecipientScopeType.DEPT.name());
		assertThat(task.getRecipientScopeJson()).contains("\"includeChildren\":true");
		assertThat(task.getCategoryId()).isEqualTo(MINOR_CATEGORY_ID);
		assertThat(task.getLinkUrl()).isEqualTo("/home");
		assertThat(task.getSenderUserId()).isEqualTo(7L);
		assertThat(task.getStatus()).isEqualTo(InAppMessageStatus.PENDING.name());
		assertThat(task.getTotalCount()).isZero();
		assertThat(task.getSuccessCount()).isZero();

		verify(dispatchTrigger).dispatchAfterCommit(9001L);
	}

	@Test
	@DisplayName("全员读扩散：直接 SUCCESS，不异步派发")
	void send_allScope_shouldPublishWithoutDispatch() {
		// 公开读扩散只落任务，不写收件箱、不走 dispatch
		InAppComposeForm form = new InAppComposeForm();
		form.setRecipientScopeType(RecipientScopeType.ALL.name());
		form.setTitle("Broadcast");
		form.setBody("all users");
		form.setContentType("TEXT");
		form.setCategoryId(MINOR_CATEGORY_ID);

		doAnswer(invocation -> {
			invocation.getArgument(0, InAppMessageEntity.class).setId(8001L);
			return 1;
		}).when(inAppMessageMapper).insert(any(InAppMessageEntity.class));
		when(categorySupport.requireEnabledMinor(MINOR_CATEGORY_ID))
			.thenReturn(new com.auth.service.system.message.model.entity.InAppMessageCategoryEntity());

		try (MockedStatic<SecurityUserUtils> security = mockStatic(SecurityUserUtils.class)) {
			security.when(SecurityUserUtils::getUserId).thenReturn(1L);

			InAppComposeResultVO result = service.send(form);

			assertThat(result.getTaskId()).isEqualTo(8001L);
			assertThat(result.getStatus()).isEqualTo(InAppMessageStatus.SUCCESS.name());
		}

		ArgumentCaptor<InAppMessageEntity> taskCaptor = ArgumentCaptor.forClass(InAppMessageEntity.class);
		verify(inAppMessageMapper).insert(taskCaptor.capture());
		InAppMessageEntity task = taskCaptor.getValue();
		assertThat(task.getRecipientScopeType()).isEqualTo(RecipientScopeType.ALL.name());
		assertThat(task.getStatus()).isEqualTo(InAppMessageStatus.SUCCESS.name());
		verify(dispatchTrigger, never()).dispatchAfterCommit(any());
	}

	@Test
	@DisplayName("DEPT includeChildren=false 写入范围快照")
	void send_deptWithoutChildren_shouldPersistFlag() {
		// 子部门开关需原样写入 recipient_scope_json
		InAppComposeForm form = new InAppComposeForm();
		form.setRecipientScopeType(RecipientScopeType.DEPT.name());
		form.setRecipientScopeIds(List.of(5L));
		form.setIncludeChildren(false);
		form.setTitle("t");
		form.setBody("b");
		form.setContentType("TEXT");
		form.setCategoryId(MINOR_CATEGORY_ID);

		doAnswer(invocation -> {
			invocation.getArgument(0, InAppMessageEntity.class).setId(2L);
			return 1;
		}).when(inAppMessageMapper).insert(any(InAppMessageEntity.class));
		when(categorySupport.requireEnabledMinor(MINOR_CATEGORY_ID))
			.thenReturn(new com.auth.service.system.message.model.entity.InAppMessageCategoryEntity());

		try (MockedStatic<SecurityUserUtils> security = mockStatic(SecurityUserUtils.class)) {
			security.when(SecurityUserUtils::getUserId).thenReturn(1L);
			service.send(form);
		}

		ArgumentCaptor<InAppMessageEntity> taskCaptor = ArgumentCaptor.forClass(InAppMessageEntity.class);
		verify(inAppMessageMapper).insert(taskCaptor.capture());
		assertThat(taskCaptor.getValue().getRecipientScopeJson()).contains("\"includeChildren\":false");
		verify(dispatchTrigger).dispatchAfterCommit(2L);
	}

	@Test
	@DisplayName("非法范围类型：抛范围非法且不落库")
	void send_shouldRejectInvalidScopeType() {
		// 校验失败时不得 insert / 派发
		InAppComposeForm form = new InAppComposeForm();
		form.setRecipientScopeType("UNKNOWN");
		form.setTitle("t");
		form.setBody("b");
		form.setContentType("TEXT");

		assertThatThrownBy(() -> service.send(form)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_RECIPIENT_SCOPE_INVALID);
		verify(inAppMessageMapper, never()).insert(any(InAppMessageEntity.class));
		verify(dispatchTrigger, never()).dispatchAfterCommit(any());
	}

	@Test
	@DisplayName("补发：PARTIAL 重置为 PENDING 并派发")
	void retry_shouldResetPartialAndDispatch() {
		// 可恢复状态走 resetForRetry + afterCommit
		InAppMessageEntity task = adminComposePushTask(20L, InAppMessageStatus.PARTIAL);
		when(inAppMessageMapper.selectById(20L)).thenReturn(task);
		when(inAppMessageMapper.resetForRetry(20L)).thenReturn(1);

		service.retry(20L);

		verify(inAppMessageMapper).resetForRetry(20L);
		verify(dispatchTrigger).dispatchAfterCommit(20L);
	}

	@Test
	@DisplayName("补发：NO_RECIPIENTS 可重置并派发")
	void retry_shouldAllowNoRecipients() {
		// 无人接收后可按原范围再展开
		InAppMessageEntity task = adminComposePushTask(27L, InAppMessageStatus.NO_RECIPIENTS);
		when(inAppMessageMapper.selectById(27L)).thenReturn(task);
		when(inAppMessageMapper.resetForRetry(27L)).thenReturn(1);

		service.retry(27L);

		verify(inAppMessageMapper).resetForRetry(27L);
		verify(dispatchTrigger).dispatchAfterCommit(27L);
	}

	@Test
	@DisplayName("补发：任务不存在")
	void retry_shouldRejectWhenTaskMissing() {
		when(inAppMessageMapper.selectById(99L)).thenReturn(null);

		assertThatThrownBy(() -> service.retry(99L)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_SEND_TASK_NOT_FOUND);
		verify(inAppMessageMapper, never()).resetForRetry(any());
		verify(dispatchTrigger, never()).dispatchAfterCommit(any());
	}

	@Test
	@DisplayName("补发：SUCCESS 拒绝")
	void retry_shouldRejectSuccess() {
		when(inAppMessageMapper.selectById(21L)).thenReturn(adminComposePushTask(21L, InAppMessageStatus.SUCCESS));

		assertThatThrownBy(() -> service.retry(21L)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_SEND_TASK_OPERATION_NOT_ALLOWED);
		verify(inAppMessageMapper, never()).resetForRetry(any());
		verify(dispatchTrigger, never()).dispatchAfterCommit(any());
	}

	@Test
	@DisplayName("补发：RECALLED 拒绝")
	void retry_shouldRejectRecalled() {
		when(inAppMessageMapper.selectById(23L)).thenReturn(adminComposePushTask(23L, InAppMessageStatus.RECALLED));

		assertThatThrownBy(() -> service.retry(23L)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_SEND_TASK_OPERATION_NOT_ALLOWED);
		verify(inAppMessageMapper, never()).resetForRetry(any());
	}

	@Test
	@DisplayName("补发：全员读扩散拒绝")
	void retry_shouldRejectPullDelivery() {
		// 读扩散发布即成功，补发无意义
		InAppMessageEntity task = new InAppMessageEntity();
		task.setId(26L);
		task.setSourceType(MessageSendSourceType.ADMIN_COMPOSE.name());
		task.setRecipientScopeType(RecipientScopeType.ALL.name());
		task.setStatus(InAppMessageStatus.SUCCESS.name());
		when(inAppMessageMapper.selectById(26L)).thenReturn(task);

		assertThatThrownBy(() -> service.retry(26L)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_SEND_TASK_OPERATION_NOT_ALLOWED);
		verify(inAppMessageMapper, never()).resetForRetry(any());
		verify(dispatchTrigger, never()).dispatchAfterCommit(any());
	}

	@Test
	@DisplayName("补发：非管理端群发拒绝")
	void retry_shouldRejectNonAdminCompose() {
		InAppMessageEntity task = new InAppMessageEntity();
		task.setId(22L);
		task.setSourceType("TEMPLATE");
		task.setRecipientScopeType(RecipientScopeType.USER.name());
		task.setStatus(InAppMessageStatus.FAILED.name());
		when(inAppMessageMapper.selectById(22L)).thenReturn(task);

		assertThatThrownBy(() -> service.retry(22L)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_SEND_TASK_OPERATION_NOT_ALLOWED);
		verify(inAppMessageMapper, never()).resetForRetry(any());
	}

	@Test
	@DisplayName("补发：reset 影响 0 行时拒绝（状态已变）")
	void retry_shouldRejectWhenResetAffectsZeroRows() {
		// CAS/并发下 reset 失败：直接按校验时快照拒绝，不再二次查询
		InAppMessageEntity task = adminComposePushTask(25L, InAppMessageStatus.FAILED);
		when(inAppMessageMapper.selectById(25L)).thenReturn(task);
		when(inAppMessageMapper.resetForRetry(25L)).thenReturn(0);

		assertThatThrownBy(() -> service.retry(25L)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_SEND_TASK_OPERATION_NOT_ALLOWED);
		verify(dispatchTrigger, never()).dispatchAfterCommit(any());
		verify(inAppMessageMapper, times(1)).selectById(25L);
	}

	@Test
	@DisplayName("撤回：SUCCESS 写入 RECALLED 与操作人")
	void recall_shouldMarkSuccessAsRecalled() {
		// 可撤回态走 recallTask，不触碰收件箱与派发
		when(inAppMessageMapper.selectById(30L)).thenReturn(adminComposePushTask(30L, InAppMessageStatus.SUCCESS));
		when(inAppMessageMapper.recallTask(eq(30L), any(Instant.class), eq(7L))).thenReturn(1);

		try (MockedStatic<SecurityUserUtils> security = mockStatic(SecurityUserUtils.class)) {
			security.when(SecurityUserUtils::getUserId).thenReturn(7L);
			service.recall(30L);
		}

		verify(inAppMessageMapper).recallTask(eq(30L), any(Instant.class), eq(7L));
		verify(dispatchTrigger, never()).dispatchAfterCommit(any());
	}

	@Test
	@DisplayName("撤回：PARTIAL 可撤回")
	void recall_shouldAllowPartial() {
		when(inAppMessageMapper.selectById(31L)).thenReturn(adminComposePushTask(31L, InAppMessageStatus.PARTIAL));
		when(inAppMessageMapper.recallTask(eq(31L), any(Instant.class), eq(1L))).thenReturn(1);

		try (MockedStatic<SecurityUserUtils> security = mockStatic(SecurityUserUtils.class)) {
			security.when(SecurityUserUtils::getUserId).thenReturn(1L);
			service.recall(31L);
		}

		verify(inAppMessageMapper).recallTask(eq(31L), any(Instant.class), eq(1L));
	}

	@Test
	@DisplayName("撤回：NO_RECIPIENTS 可撤回")
	void recall_shouldAllowNoRecipients() {
		// 无人接收仍可作废为 RECALLED
		when(inAppMessageMapper.selectById(37L))
			.thenReturn(adminComposePushTask(37L, InAppMessageStatus.NO_RECIPIENTS));
		when(inAppMessageMapper.recallTask(eq(37L), any(Instant.class), eq(3L))).thenReturn(1);

		try (MockedStatic<SecurityUserUtils> security = mockStatic(SecurityUserUtils.class)) {
			security.when(SecurityUserUtils::getUserId).thenReturn(3L);
			service.recall(37L);
		}

		verify(inAppMessageMapper).recallTask(eq(37L), any(Instant.class), eq(3L));
	}

	@Test
	@DisplayName("撤回：全员读扩散 SUCCESS 可撤回")
	void recall_shouldAllowPullSuccess() {
		// 读扩散无收件箱行，仍只改主表状态
		InAppMessageEntity task = new InAppMessageEntity();
		task.setId(32L);
		task.setSourceType(MessageSendSourceType.ADMIN_COMPOSE.name());
		task.setRecipientScopeType(RecipientScopeType.ALL.name());
		task.setStatus(InAppMessageStatus.SUCCESS.name());
		when(inAppMessageMapper.selectById(32L)).thenReturn(task);
		when(inAppMessageMapper.recallTask(eq(32L), any(Instant.class), eq(2L))).thenReturn(1);

		try (MockedStatic<SecurityUserUtils> security = mockStatic(SecurityUserUtils.class)) {
			security.when(SecurityUserUtils::getUserId).thenReturn(2L);
			service.recall(32L);
		}

		verify(inAppMessageMapper).recallTask(eq(32L), any(Instant.class), eq(2L));
	}

	@Test
	@DisplayName("撤回：任务不存在")
	void recall_shouldRejectWhenTaskMissing() {
		when(inAppMessageMapper.selectById(98L)).thenReturn(null);

		assertThatThrownBy(() -> service.recall(98L)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_SEND_TASK_NOT_FOUND);
		verify(inAppMessageMapper, never()).recallTask(any(), any(), any());
	}

	@Test
	@DisplayName("撤回：SENDING 拒绝")
	void recall_shouldRejectSending() {
		when(inAppMessageMapper.selectById(33L)).thenReturn(adminComposePushTask(33L, InAppMessageStatus.SENDING));

		assertThatThrownBy(() -> service.recall(33L)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_SEND_TASK_OPERATION_NOT_ALLOWED);
		verify(inAppMessageMapper, never()).recallTask(any(), any(), any());
	}

	@Test
	@DisplayName("撤回：RECALLED 拒绝")
	void recall_shouldRejectAlreadyRecalled() {
		when(inAppMessageMapper.selectById(34L)).thenReturn(adminComposePushTask(34L, InAppMessageStatus.RECALLED));

		assertThatThrownBy(() -> service.recall(34L)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_SEND_TASK_OPERATION_NOT_ALLOWED);
		verify(inAppMessageMapper, never()).recallTask(any(), any(), any());
	}

	@Test
	@DisplayName("撤回：FAILED 拒绝")
	void recall_shouldRejectFailed() {
		when(inAppMessageMapper.selectById(35L)).thenReturn(adminComposePushTask(35L, InAppMessageStatus.FAILED));

		assertThatThrownBy(() -> service.recall(35L)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_SEND_TASK_OPERATION_NOT_ALLOWED);
		verify(inAppMessageMapper, never()).recallTask(any(), any(), any());
	}

	@Test
	@DisplayName("撤回：CAS 影响 0 行时拒绝（状态已变）")
	void recall_shouldRejectWhenCasAffectsZeroRows() {
		InAppMessageEntity task = adminComposePushTask(36L, InAppMessageStatus.SUCCESS);
		when(inAppMessageMapper.selectById(36L)).thenReturn(task);
		when(inAppMessageMapper.recallTask(eq(36L), any(Instant.class), eq(9L))).thenReturn(0);

		try (MockedStatic<SecurityUserUtils> security = mockStatic(SecurityUserUtils.class)) {
			security.when(SecurityUserUtils::getUserId).thenReturn(9L);

			assertThatThrownBy(() -> service.recall(36L)).isInstanceOf(MessageException.class)
				.extracting(ex -> ((MessageException) ex).getResultCode())
				.isEqualTo(IN_APP_SEND_TASK_OPERATION_NOT_ALLOWED);
		}

		verify(inAppMessageMapper, times(1)).selectById(36L);
	}

	@Test
	@DisplayName("批量删除：终态任务按 ids 物理删除")
	void batchDelete_shouldDeleteTerminalTasks() {
		// SUCCESS / RECALLED / ALL 读扩散均可删；收件箱由外键 CASCADE
		List<Long> ids = List.of(40L, 41L);
		InAppMessageEntity pushSuccess = adminComposePushTask(40L, InAppMessageStatus.SUCCESS);
		InAppMessageEntity pullRecalled = new InAppMessageEntity();
		pullRecalled.setId(41L);
		pullRecalled.setRecipientScopeType(RecipientScopeType.ALL.name());
		pullRecalled.setStatus(InAppMessageStatus.RECALLED.name());
		when(inAppMessageMapper.selectByIds(ids)).thenReturn(List.of(pushSuccess, pullRecalled));
		when(inAppMessageMapper.deleteByIds(ids)).thenReturn(2);

		service.batchDelete(ids);

		verify(inAppMessageMapper).selectByIds(ids);
		verify(inAppMessageMapper).deleteByIds(ids);
	}

	@Test
	@DisplayName("批量删除：ids 为空时跳过")
	void batchDelete_shouldSkipWhenIdsEmpty() {
		service.batchDelete(Collections.emptyList());

		verify(inAppMessageMapper, never()).selectByIds(any());
		verify(inAppMessageMapper, never()).deleteByIds(any());
	}

	@Test
	@DisplayName("批量删除：SENDING 拒绝且不删")
	void batchDelete_shouldRejectSending() {
		List<Long> ids = List.of(42L);
		when(inAppMessageMapper.selectByIds(ids))
			.thenReturn(List.of(adminComposePushTask(42L, InAppMessageStatus.SENDING)));

		assertThatThrownBy(() -> service.batchDelete(ids)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_SEND_TASK_OPERATION_NOT_ALLOWED);
		verify(inAppMessageMapper, never()).deleteByIds(any());
	}

	@Test
	@DisplayName("批量删除：PENDING 拒绝且不删")
	void batchDelete_shouldRejectPending() {
		List<Long> ids = List.of(43L);
		when(inAppMessageMapper.selectByIds(ids))
			.thenReturn(List.of(adminComposePushTask(43L, InAppMessageStatus.PENDING)));

		assertThatThrownBy(() -> service.batchDelete(ids)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_SEND_TASK_OPERATION_NOT_ALLOWED);
		verify(inAppMessageMapper, never()).deleteByIds(any());
	}

	@Test
	@DisplayName("批量删除：存在不可删时整批中止")
	void batchDelete_shouldAbortWhenAnyNotDeletable() {
		// 混入 SENDING 时整批失败，避免部分删除
		List<Long> ids = List.of(44L, 45L);
		when(inAppMessageMapper.selectByIds(ids))
			.thenReturn(List.of(adminComposePushTask(44L, InAppMessageStatus.SUCCESS),
					adminComposePushTask(45L, InAppMessageStatus.SENDING)));

		assertThatThrownBy(() -> service.batchDelete(ids)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_SEND_TASK_OPERATION_NOT_ALLOWED);
		verify(inAppMessageMapper, never()).deleteByIds(any());
	}

}

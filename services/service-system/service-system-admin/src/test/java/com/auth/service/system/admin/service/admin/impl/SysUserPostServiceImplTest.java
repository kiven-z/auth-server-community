package com.auth.service.system.admin.service.admin.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.admin.post.SysPostMapper;
import com.auth.service.system.admin.mapper.admin.user.SysUserMapper;
import com.auth.service.system.admin.mapper.admin.user.UserPostMapper;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.entity.UserPostEntity;
import com.auth.service.system.admin.model.form.user.UserPostAssignForm;
import com.auth.service.system.admin.model.form.user.UserPostRelationUpdateForm;
import com.auth.service.system.admin.model.po.user.UserPostPageRowPO;
import com.auth.service.system.admin.model.query.user.UserPostPageQuery;
import com.auth.service.system.admin.model.vo.user.UserPostPageVO;
import com.auth.service.system.admin.support.post.PostReferenceChecker;
import com.auth.service.system.admin.support.user.UserOrgRelationBatchRemoveSupport;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.UserAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link SysUserPostServiceImpl} 单元测试
 */
@DisplayName("SysUserPostServiceImpl 用户岗位关联")
@ExtendWith(MockitoExtension.class)
class SysUserPostServiceImplTest {

	private static final Long USER_ID = 10L;

	private static final Long RELATION_ID = 100L;

	private static final Long POST_ID = 30L;

	@Mock
	private SysUserMapper sysUserMapper;

	@Mock
	private SysPostMapper sysPostMapper;

	@Mock
	private UserPostMapper userPostMapper;

	@Mock
	private UserAuthorizationInvalidationTrigger userAuthorizationInvalidationTrigger;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@Captor
	private ArgumentCaptor<UserPostEntity> entityCaptor;

	private SysUserPostServiceImpl sysUserPostService;

	private static UserEntity activeUser() {
		UserEntity entity = new UserEntity();
		entity.setId(SysUserPostServiceImplTest.USER_ID);
		entity.setUsername("tester");
		return entity;
	}

	private static SysPostEntity activePost() {
		SysPostEntity entity = new SysPostEntity();
		entity.setId(POST_ID);
		entity.setStatus(true);
		return entity;
	}

	private static UserPostAssignForm createForm(Boolean isPrimary, String remark) {
		UserPostAssignForm form = new UserPostAssignForm();
		form.setPostId(POST_ID);
		form.setIsPrimary(isPrimary);
		form.setRemark(remark);
		return form;
	}

	private static UserPostRelationUpdateForm updateForm(String remark) {
		UserPostRelationUpdateForm form = new UserPostRelationUpdateForm();
		form.setIsPrimary(false);
		form.setRemark(remark);
		return form;
	}

	@BeforeEach
	void setUp() throws Exception {
		UserReferenceChecker userReferenceChecker = new UserReferenceChecker(sysUserMapper);
		PostReferenceChecker postReferenceChecker = new PostReferenceChecker(sysPostMapper);
		UserOrgRelationBatchRemoveSupport batchRemoveSupport = new UserOrgRelationBatchRemoveSupport(
				userReferenceChecker, userAuthorizationInvalidationTrigger);
		sysUserPostService = new SysUserPostServiceImpl(userReferenceChecker, postReferenceChecker,
				userAuthorizationInvalidationTrigger, auditUserDisplayService, batchRemoveSupport);
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysUserPostService, userPostMapper);
	}

	@Test
	@DisplayName("分页查询：映射业务字段并补充审计用户名")
	void getPageMapsRowsAndEnrichesAuditUsernames() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());
		UserPostPageQuery query = new UserPostPageQuery();
		query.setPageIndex(1);
		query.setPageSize(10);
		Instant now = LocalDateTime.of(2026, 6, 11, 10, 0, 0).toInstant(java.time.ZoneOffset.UTC);
		UserPostPageRowPO row = new UserPostPageRowPO();
		row.setId(RELATION_ID);
		row.setUserId(USER_ID);
		row.setPostId(POST_ID);
		row.setPostCode("DEV");
		row.setPostName("开发工程师");
		row.setPostStatus(true);
		row.setPostEffective(false);
		row.setIsPrimary(true);
		row.setRemark("核心岗位");
		row.setCreatedAt(now);
		row.setUpdatedAt(now);
		row.setCreatedBy(1L);
		row.setUpdatedBy(1L);
		Page<UserPostPageRowPO> poPage = new Page<>(1, 10);
		poPage.setRecords(List.of(row));
		poPage.setTotal(1);
		when(userPostMapper.selectListByPage(any(Page.class), eq(USER_ID), eq(query))).thenReturn(poPage);

		PageResponse<UserPostPageVO> result = sysUserPostService.getPage(USER_ID, query);

		assertThat(result.getList()).hasSize(1);
		UserPostPageVO vo = result.getList().get(0);
		assertThat(vo.getPostCode()).isEqualTo("DEV");
		assertThat(vo.getPostStatus()).isTrue();
		assertThat(vo.getPostEffective()).isFalse();
		assertThat(vo.getIsPrimary()).isTrue();
		verify(auditUserDisplayService).enrichAuditUsernames(any(IPage.class), isNull(), isNull());
	}

	@Test
	@DisplayName("创建关联：非主岗位时不降级其他关联")
	void createNonPrimarySkipsDemote() {
		stubCreatePrerequisites();
		when(userPostMapper.countByUserIdAndPostId(USER_ID, POST_ID)).thenReturn(0);
		when(userPostMapper.insert(any(UserPostEntity.class))).thenReturn(1);

		sysUserPostService.create(USER_ID, createForm(false, "备注"));

		verify(userPostMapper, never()).demotePrimaryByUserId(USER_ID);
		verify(userPostMapper).insert(entityCaptor.capture());
		UserPostEntity saved = entityCaptor.getValue();
		assertThat(saved.getUserId()).isEqualTo(USER_ID);
		assertThat(saved.getPostId()).isEqualTo(POST_ID);
		assertThat(saved.getIsPrimary()).isFalse();
		verify(userAuthorizationInvalidationTrigger).submitByUserIds(List.of(USER_ID), "create-post");
	}

	@Test
	@DisplayName("创建关联：主岗位时降级该用户其他主岗位")
	void createPrimaryDemotesExistingPrimary() {
		stubCreatePrerequisites();
		when(userPostMapper.countByUserIdAndPostId(USER_ID, POST_ID)).thenReturn(0);
		when(userPostMapper.insert(any(UserPostEntity.class))).thenReturn(1);

		sysUserPostService.create(USER_ID, createForm(true, null));

		verify(userPostMapper).demotePrimaryByUserId(USER_ID);
		verify(userPostMapper).insert(entityCaptor.capture());
		assertThat(entityCaptor.getValue().getIsPrimary()).isTrue();
	}

	@Test
	@DisplayName("创建关联：重复岗位时拒绝保存")
	void createRejectsDuplicatePostRelation() {
		stubCreatePrerequisites();
		when(userPostMapper.countByUserIdAndPostId(USER_ID, POST_ID)).thenReturn(1);
		UserPostAssignForm form = createForm(false, null);

		assertThatThrownBy(() -> sysUserPostService.create(USER_ID, form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.USER_POST_DUPLICATE);

		verify(userPostMapper, never()).insert(any(UserPostEntity.class));
	}

	@Test
	@DisplayName("创建关联：计算无效岗位不可新增任职")
	void createRejectsIneffectivePost() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());
		when(sysPostMapper.selectById(POST_ID)).thenReturn(activePost());
		when(sysPostMapper.countEffectiveById(POST_ID)).thenReturn(0L);
		UserPostAssignForm form = createForm(false, null);

		assertThatThrownBy(() -> sysUserPostService.create(USER_ID, form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_UNAVAILABLE);

		verify(userPostMapper, never()).insert(any(UserPostEntity.class));
	}

	@Test
	@DisplayName("更新关联：按主键更新主岗与备注")
	void updateAppliesRelationFields() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());
		UserPostEntity existing = new UserPostEntity();
		existing.setId(RELATION_ID);
		existing.setUserId(USER_ID);
		existing.setPostId(POST_ID);
		when(userPostMapper.selectByIdAndUserId(RELATION_ID, USER_ID)).thenReturn(existing);
		when(userPostMapper.updateById(any(UserPostEntity.class))).thenReturn(1);

		sysUserPostService.update(USER_ID, RELATION_ID, updateForm("调整"));

		verify(userPostMapper).updateById(entityCaptor.capture());
		UserPostEntity updated = entityCaptor.getValue();
		assertThat(updated.getPostId()).isEqualTo(POST_ID);
		assertThat(updated.getRemark()).isEqualTo("调整");
		verify(userAuthorizationInvalidationTrigger).submitByUserIds(List.of(USER_ID), "update-post");
		verify(sysPostMapper, never()).countEffectiveById(any());
	}

	@Test
	@DisplayName("更新关联：计算无效岗位不可设为主岗")
	void updateRejectsPrimaryWhenPostNotEffective() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());
		UserPostEntity existing = new UserPostEntity();
		existing.setId(RELATION_ID);
		existing.setUserId(USER_ID);
		existing.setPostId(POST_ID);
		when(userPostMapper.selectByIdAndUserId(RELATION_ID, USER_ID)).thenReturn(existing);
		when(sysPostMapper.selectById(POST_ID)).thenReturn(activePost());
		when(sysPostMapper.countEffectiveById(POST_ID)).thenReturn(0L);
		UserPostRelationUpdateForm form = updateForm(null);
		form.setIsPrimary(true);

		assertThatThrownBy(() -> sysUserPostService.update(USER_ID, RELATION_ID, form))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_UNAVAILABLE);

		verify(userPostMapper, never()).demotePrimaryByUserId(USER_ID);
		verify(userPostMapper, never()).updateById(any(UserPostEntity.class));
	}

	@Test
	@DisplayName("更新关联：计算有效岗位可设为主岗")
	void updatePrimaryDemotesWhenPostEffective() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());
		UserPostEntity existing = new UserPostEntity();
		existing.setId(RELATION_ID);
		existing.setUserId(USER_ID);
		existing.setPostId(POST_ID);
		when(userPostMapper.selectByIdAndUserId(RELATION_ID, USER_ID)).thenReturn(existing);
		when(sysPostMapper.selectById(POST_ID)).thenReturn(activePost());
		when(sysPostMapper.countEffectiveById(POST_ID)).thenReturn(1L);
		when(userPostMapper.updateById(any(UserPostEntity.class))).thenReturn(1);
		UserPostRelationUpdateForm form = updateForm(null);
		form.setIsPrimary(true);

		sysUserPostService.update(USER_ID, RELATION_ID, form);

		verify(userPostMapper).demotePrimaryByUserId(USER_ID);
		verify(userPostMapper).updateById(any(UserPostEntity.class));
	}

	@Test
	@DisplayName("更新关联：关联不存在时拒绝")
	void updateRejectsMissingRelation() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());
		when(userPostMapper.selectByIdAndUserId(RELATION_ID, USER_ID)).thenReturn(null);
		UserPostRelationUpdateForm form = updateForm(null);

		assertThatThrownBy(() -> sysUserPostService.update(USER_ID, RELATION_ID, form))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);
	}

	@Test
	@DisplayName("批量删除：提交授权失效")
	void removeBatchSubmitsInvalidation() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());

		sysUserPostService.removeBatch(USER_ID, List.of(RELATION_ID));

		verify(userPostMapper).deleteByIds(List.of(RELATION_ID));
		verify(userAuthorizationInvalidationTrigger).submitByUserIds(List.of(USER_ID), "delete-post");
	}

	@Test
	@DisplayName("清空关联：有数据时提交授权失效")
	void removeAllSubmitsInvalidationWhenRowsDeleted() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());
		when(userPostMapper.deleteByUserId(USER_ID)).thenReturn(2);

		sysUserPostService.removeAll(USER_ID);

		verify(userPostMapper).deleteByUserId(USER_ID);
		verify(userAuthorizationInvalidationTrigger).submitByUserIds(List.of(USER_ID), "clear-post");
	}

	@Test
	@DisplayName("全部清空：无关联时不触发授权失效")
	void removeAllSkipsInvalidationWhenNoRelations() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());
		when(userPostMapper.deleteByUserId(USER_ID)).thenReturn(0);

		sysUserPostService.removeAll(USER_ID);

		verify(userPostMapper).deleteByUserId(USER_ID);
		verify(userAuthorizationInvalidationTrigger, never()).submitByUserIds(any(), any());
	}

	private void stubCreatePrerequisites() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());
		when(sysPostMapper.selectById(POST_ID)).thenReturn(activePost());
		when(sysPostMapper.countEffectiveById(POST_ID)).thenReturn(1L);
	}

}

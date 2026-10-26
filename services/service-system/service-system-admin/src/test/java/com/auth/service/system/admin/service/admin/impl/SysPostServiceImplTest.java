package com.auth.service.system.admin.service.admin.impl;

import com.auth.common.core.constants.BatchSizes;
import com.auth.module.security.contract.api.GrantTableSubjectType;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.admin.post.SysPostMapper;
import com.auth.service.system.admin.mapper.authorization.GrantBindingQueryMapper;
import com.auth.service.system.admin.mapper.authorization.PostRelationQueryMapper;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.form.post.SysPostForm;
import com.auth.service.system.admin.model.po.post.PostDeptCodePairPO;
import com.auth.service.system.admin.model.po.reference.DeptReferencePO;
import com.auth.service.system.admin.model.vo.post.SysPostDetailVO;
import com.auth.service.system.admin.support.post.PostReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.PostAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link SysPostServiceImpl} 单元测试
 *
 * @author Bunny
 */
@DisplayName("SysPostServiceImpl 岗位管理")
@ExtendWith(MockitoExtension.class)
class SysPostServiceImplTest {

	@Mock
	private SysPostMapper sysPostMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@Mock
	private PostAuthorizationInvalidationTrigger postInvalidationTrigger;

	@Mock
	private PostRelationQueryMapper postRelationQueryMapper;

	@Mock
	private GrantBindingQueryMapper grantBindingQueryMapper;

	private SysPostServiceImpl sysPostService;

	private static SysPostForm minimalForm(Long deptId, String postCode, String postName) {
		return minimalForm(null, deptId, postCode, postName);
	}

	private static SysPostForm minimalForm(Long id, Long deptId, String postCode, String postName) {
		SysPostForm form = new SysPostForm();
		form.setId(id);
		form.setDeptId(deptId);
		form.setPostCode(postCode);
		form.setPostName(postName);
		form.setOrderNum(0);
		return form;
	}

	private static SysPostEntity activePost(Long id, Long deptId, String postCode) {
		SysPostEntity entity = new SysPostEntity();
		entity.setId(id);
		entity.setDeptId(deptId);
		entity.setPostCode(postCode);
		entity.setPostName("原名");
		return entity;
	}

	@BeforeEach
	void setUp() throws Exception {
		PostReferenceChecker postReferenceChecker = new PostReferenceChecker(sysPostMapper);
		sysPostService = spy(new SysPostServiceImpl(auditUserDisplayService, postInvalidationTrigger,
				postReferenceChecker, postRelationQueryMapper, grantBindingQueryMapper));
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysPostService, sysPostMapper);
		lenient().doReturn(true).when(sysPostService).saveBatch(anyList(), anyInt());
	}

	@Test
	@DisplayName("搜索：limit 为 null 时默认使用 20")
	void searchUsesDefaultLimitWhenNull() {
		when(sysPostMapper.search(any(), anyInt())).thenReturn(List.of());

		sysPostService.search("dev", null);

		ArgumentCaptor<Integer> limitCap = ArgumentCaptor.forClass(Integer.class);
		verify(sysPostMapper).search(eq("dev"), limitCap.capture());
		assertThat(limitCap.getValue()).isEqualTo(20);
	}

	@Test
	@DisplayName("搜索：limit 超过 50 时封顶为 50")
	void searchCapsLimitAtFifty() {
		when(sysPostMapper.search(any(), anyInt())).thenReturn(List.of());

		sysPostService.search("研发", 200);

		ArgumentCaptor<Integer> limitCap = ArgumentCaptor.forClass(Integer.class);
		verify(sysPostMapper).search(eq("研发"), limitCap.capture());
		assertThat(limitCap.getValue()).isEqualTo(50);
	}

	@Test
	@DisplayName("搜索：limit 小于 1 时提升为 1")
	void searchRaisesTinyLimitToOne() {
		when(sysPostMapper.search(any(), anyInt())).thenReturn(List.of());

		sysPostService.search("p1", 0);

		ArgumentCaptor<Integer> limitCap = ArgumentCaptor.forClass(Integer.class);
		verify(sysPostMapper).search(eq("p1"), limitCap.capture());
		assertThat(limitCap.getValue()).isEqualTo(1);
	}

	@Test
	@DisplayName("详情：岗位不存在时 POST_NOT_FOUND")
	void getDetailThrowsWhenPostNotFound() {
		when(sysPostMapper.selectById(9L)).thenReturn(null);

		assertThatThrownBy(() -> sysPostService.getDetail(9L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);

		verifyNoInteractions(grantBindingQueryMapper);
		verify(auditUserDisplayService, never()).enrichAuditUsernames(anyList(), isNull(), isNull());
	}

	@Test
	@DisplayName("详情：组装主表、部门快照与关系计数并补全审计用户名")
	void getDetailAssemblesBoundDataAndEnrichesAudit() {
		Long postId = 30L;
		SysPostEntity entity = activePost(postId, 2L, "DEV");
		entity.setPostName("研发");
		entity.setOrderNum(1);
		entity.setRemark("备注");

		DeptReferencePO boundDept = new DeptReferencePO();
		boundDept.setId(2L);
		boundDept.setDeptCode("RD");
		boundDept.setDeptName("研发部");

		when(sysPostMapper.selectById(postId)).thenReturn(entity);
		when(sysPostMapper.selectBoundDeptByPostId(postId)).thenReturn(boundDept);
		when(postRelationQueryMapper.countUsersByPostId(postId, null)).thenReturn(1L);
		when(grantBindingQueryMapper.countBoundRolesBySubject(GrantTableSubjectType.POST.name(), postId, null))
			.thenReturn(1L);

		SysPostDetailVO detail = sysPostService.getDetail(postId);

		assertThat(detail.getPostCode()).isEqualTo("DEV");
		assertThat(detail.getPostName()).isEqualTo("研发");
		assertThat(detail.getDeptId()).isEqualTo(2L);
		assertThat(detail.getBoundDept()).isNotNull();
		assertThat(detail.getBoundDept().getDeptName()).isEqualTo("研发部");
		assertThat(detail.getBoundUserCount()).isEqualTo(1L);
		assertThat(detail.getBoundRoleCount()).isEqualTo(1L);
		verify(sysPostMapper).selectBoundDeptByPostId(postId);
		verify(postRelationQueryMapper).countUsersByPostId(postId, null);
		verify(grantBindingQueryMapper).countBoundRolesBySubject(GrantTableSubjectType.POST.name(), postId, null);
		verify(auditUserDisplayService).enrichAuditUsernames(anyList(), isNull(), isNull());
	}

	@Test
	@DisplayName("批量新增岗位：空列表直接返回")
	void createBatchFromImportSkipsWhenEmpty() {
		sysPostService.createBatchFromImport(List.of());

		verify(sysPostMapper, never()).selectAssignableDeptIds(any());
		verify(sysPostService, never()).saveBatch(anyList(), anyInt());
	}

	@Test
	@DisplayName("新增：目标部门不存在时 DATA_UNAVAILABLE")
	void createThrowsWhenDeptNotAssignable() {
		when(sysPostMapper.selectAssignableDeptIds(List.of(99L))).thenReturn(List.of());
		SysPostForm form = minimalForm(99L, "P1", "开发");

		ThrowingCallable executable = () -> sysPostService.createBatchFromImport(List.of(form));
		assertThatThrownBy(executable).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_UNAVAILABLE);
	}

	@Test
	@DisplayName("新增：部门内编码重复时 POST_CODE_DUPLICATE_IN_DEPT")
	void createThrowsWhenPostCodeDuplicateInDept() {
		when(sysPostMapper.selectAssignableDeptIds(List.of(1L))).thenReturn(List.of(1L));
		PostDeptCodePairPO existing = new PostDeptCodePairPO();
		existing.setDeptId(1L);
		existing.setPostCode("DUP");
		when(sysPostMapper.selectReferenceByDeptPostPairs(anyList())).thenReturn(List.of(existing));
		SysPostForm form = minimalForm(1L, "DUP", "重复岗");

		ThrowingCallable executable = () -> sysPostService.createBatchFromImport(List.of(form));
		assertThatThrownBy(executable).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.POST_CODE_DUPLICATE_IN_DEPT);

		verify(sysPostMapper).selectReferenceByDeptPostPairs(anyList());
		verify(sysPostMapper, never()).selectList(any());
	}

	@Test
	@DisplayName("新增：校验通过时批量落库")
	void createPersistsWhenValid() {
		when(sysPostMapper.selectAssignableDeptIds(List.of(2L))).thenReturn(List.of(2L));
		when(sysPostMapper.selectReferenceByDeptPostPairs(anyList())).thenReturn(List.of());
		SysPostForm form = minimalForm(2L, "NEW_CODE", "新岗位");

		sysPostService.createBatchFromImport(List.of(form));

		verify(sysPostMapper).selectReferenceByDeptPostPairs(anyList());
		verify(sysPostMapper, never()).selectList(any());
		verify(sysPostService).saveBatch(argThat((List<SysPostEntity> entities) -> entities.size() == 1
				&& Long.valueOf(2L).equals(entities.get(0).getDeptId())
				&& "NEW_CODE".equals(entities.get(0).getPostCode())
				&& Integer.valueOf(0).equals(entities.get(0).getOrderNum())), eq(BatchSizes.SIZE_500));
	}

	@Test
	@DisplayName("更新：岗位不存在时 POST_NOT_FOUND")
	void updateThrowsWhenPostNotFound() {
		when(sysPostMapper.selectById(9L)).thenReturn(null);
		SysPostForm form = minimalForm(9L, 1L, "C1", "岗位");

		assertThatThrownBy(() -> sysPostService.update(form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);
	}

	@Test
	@DisplayName("更新：编制部门未变时不校验可挂载性且不提交授权失效")
	void updateWithoutDeptChangeSkipsAssignableAndInvalidation() {
		Long postId = 10L;
		SysPostEntity existing = activePost(postId, 1L, "DEV");
		when(sysPostMapper.selectById(postId)).thenReturn(existing);
		when(sysPostMapper.selectCount(any())).thenReturn(0L);
		when(sysPostMapper.updateById(any(SysPostEntity.class))).thenReturn(1);

		SysPostForm form = minimalForm(postId, 1L, "DEV", "研发岗");

		sysPostService.update(form);

		verify(sysPostMapper, never()).selectAssignableDeptIds(any());
		verify(sysPostMapper).updateById(argThat((SysPostEntity e) -> "研发岗".equals(e.getPostName())));
		verify(postInvalidationTrigger, never()).submitByPostIds(any(), any());
	}

	@Test
	@DisplayName("更新：改挂编制部门时校验目标部门存在且不提交授权失效")
	void updateDeptChangeSkipsInvalidation() {
		Long postId = 12L;
		SysPostEntity existing = activePost(postId, 1L, "DEV");
		when(sysPostMapper.selectById(postId)).thenReturn(existing);
		when(sysPostMapper.selectAssignableDeptIds(List.of(5L))).thenReturn(List.of(5L));
		when(sysPostMapper.selectCount(any())).thenReturn(0L);
		when(sysPostMapper.updateById(any(SysPostEntity.class))).thenReturn(1);

		SysPostForm form = minimalForm(postId, 5L, "DEV", "原名");

		sysPostService.update(form);

		verify(sysPostMapper).selectAssignableDeptIds(List.of(5L));
		verify(sysPostMapper).updateById(argThat((SysPostEntity e) -> Long.valueOf(5L).equals(e.getDeptId())));
		verify(postInvalidationTrigger, never()).submitByPostIds(any(), any());
	}

	@Test
	@DisplayName("更新：改挂到不存在的部门时 DATA_UNAVAILABLE")
	void updateThrowsWhenNewDeptNotAssignable() {
		Long postId = 13L;
		SysPostEntity existing = activePost(postId, 1L, "DEV");
		when(sysPostMapper.selectById(postId)).thenReturn(existing);
		when(sysPostMapper.selectAssignableDeptIds(List.of(99L))).thenReturn(List.of());

		SysPostForm form = minimalForm(postId, 99L, "DEV", "原名");

		assertThatThrownBy(() -> sysPostService.update(form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_UNAVAILABLE);

		verify(sysPostMapper, never()).updateById(any(SysPostEntity.class));
		verify(postInvalidationTrigger, never()).submitByPostIds(any(), any());
	}

	@Test
	@DisplayName("删除：岗位不存在时 POST_NOT_FOUND")
	void deleteThrowsWhenPostNotFound() {
		when(sysPostMapper.selectById(9L)).thenReturn(null);

		assertThatThrownBy(() -> sysPostService.deleteById(9L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);

		verifyNoInteractions(postInvalidationTrigger);
	}

	@Test
	@DisplayName("删除：user_post 仍有关联时 POST_IN_USE")
	void deleteThrowsWhenUserPostInUse() {
		Long postId = 20L;
		when(sysPostMapper.selectById(postId)).thenReturn(activePost(postId, 1L, "U1"));
		when(sysPostMapper.countUserPostByPostId(postId)).thenReturn(1L);

		assertThatThrownBy(() -> sysPostService.deleteById(postId)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_IN_USE);

		verify(sysPostMapper, never()).deleteById(any());
		verifyNoInteractions(postInvalidationTrigger);
	}

	@Test
	@DisplayName("删除：grant_table POST 主体仍引用时 POST_IN_USE")
	void deleteThrowsWhenGrantPostSubjectInUse() {
		Long postId = 21L;
		when(sysPostMapper.selectById(postId)).thenReturn(activePost(postId, 1L, "G1"));
		when(sysPostMapper.countUserPostByPostId(postId)).thenReturn(0L);
		when(sysPostMapper.countGrantPostSubjectByPostId(postId)).thenReturn(1L);

		assertThatThrownBy(() -> sysPostService.deleteById(postId)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_IN_USE);

		verify(sysPostMapper, never()).deleteById(any());
		verifyNoInteractions(postInvalidationTrigger);
	}

	@Test
	@DisplayName("删除：无引用时物理删除并仅提交 USER_POST 授权失效")
	void deleteShouldInvalidateAndRemoveWhenDeletable() {
		Long postId = 22L;
		when(sysPostMapper.selectById(postId)).thenReturn(activePost(postId, 1L, "OK"));
		when(sysPostMapper.countUserPostByPostId(postId)).thenReturn(0L);
		when(sysPostMapper.countGrantPostSubjectByPostId(postId)).thenReturn(0L);
		when(sysPostMapper.deleteById(postId)).thenReturn(1);

		sysPostService.deleteById(postId);

		verify(postInvalidationTrigger).submitByPostIds(List.of(postId), "delete");
		verify(sysPostMapper).deleteById(postId);
	}

}

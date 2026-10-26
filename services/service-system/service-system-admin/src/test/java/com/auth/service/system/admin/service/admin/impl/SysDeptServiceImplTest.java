package com.auth.service.system.admin.service.admin.impl;

import com.auth.service.system.admin.mapper.admin.dept.SysDeptMapper;
import com.auth.service.system.admin.mapper.admin.user.UserDeptMapper;
import com.auth.service.system.admin.mapper.authorization.DeptRelationQueryMapper;
import com.auth.service.system.admin.mapper.authorization.GrantBindingQueryMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.form.dept.SysDeptForm;
import com.auth.service.system.admin.model.form.dept.SysDeptMoveForm;
import com.auth.service.system.admin.model.po.dept.SysDeptPageRowPO;
import com.auth.service.system.admin.model.query.dept.SysDeptListQuery;
import com.auth.service.system.admin.model.vo.dept.SysDeptListVO;
import com.auth.service.system.admin.support.dept.DeptClosureMaintainer;
import com.auth.service.system.admin.support.dept.DeptReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.UserAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.anyLong;

/**
 * {@link SysDeptServiceImpl} 单元测试
 *
 * @author Bunny
 */
@DisplayName("SysDeptServiceImpl 部门管理")
@ExtendWith(MockitoExtension.class)
class SysDeptServiceImplTest {

	@Mock
	private SysDeptMapper sysDeptMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@Mock
	private DeptReferenceChecker deptReferenceChecker;

	@Mock
	private DeptClosureMaintainer deptClosureMaintainer;

	@Mock
	private UserDeptMapper userDeptMapper;

	@Mock
	private UserAuthorizationInvalidationTrigger userAuthorizationInvalidationTrigger;

	@Mock
	private DeptRelationQueryMapper deptRelationQueryMapper;

	@Mock
	private GrantBindingQueryMapper grantBindingQueryMapper;

	private SysDeptServiceImpl sysDeptService;

	private SysDeptQueryServiceImpl sysDeptQueryService;

	private static SysDeptForm minimalForm(Long id, long parentId, String deptCode, String deptName) {
		SysDeptForm form = new SysDeptForm();
		form.setId(id);
		form.setParentId(parentId);
		form.setDeptCode(deptCode);
		form.setDeptName(deptName);
		form.setOrderNum(0);
		return form;
	}

	private static SysDeptEntity activeDept(Long id, String deptCode) {
		SysDeptEntity entity = new SysDeptEntity();
		entity.setId(id);
		entity.setParentId(0L);
		entity.setDeptCode(deptCode);
		entity.setDeptName("原部门");
		return entity;
	}

	@BeforeEach
	void setUp() throws Exception {
		sysDeptService = spy(new SysDeptServiceImpl(deptReferenceChecker, deptClosureMaintainer, userDeptMapper,
				userAuthorizationInvalidationTrigger));
		sysDeptQueryService = new SysDeptQueryServiceImpl(auditUserDisplayService, deptReferenceChecker,
				deptRelationQueryMapper, grantBindingQueryMapper);
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysDeptService, sysDeptMapper);
		baseMapperField.set(sysDeptQueryService, sysDeptMapper);
		lenient().doAnswer(invocation -> {
			SysDeptEntity entity = invocation.getArgument(0);
			entity.setId(1L);
			return true;
		}).when(sysDeptService).save(any(SysDeptEntity.class));
	}

	@Test
	@DisplayName("新增部门：校验通过后落库并维护闭包")
	void createPersistsEntityAndInsertsClosure() {
		SysDeptForm form = minimalForm(null, 0L, "RD", "研发部");
		when(sysDeptMapper.selectCount(any())).thenReturn(0L);

		sysDeptService.create(form);

		verify(sysDeptMapper).selectCount(any());
		verify(sysDeptMapper, never()).countById(anyLong());
		verify(sysDeptService).save(argThat((SysDeptEntity entity) -> "RD".equals(entity.getDeptCode())
				&& Long.valueOf(0L).equals(entity.getParentId())));
		verify(deptClosureMaintainer).insertNodes(argThat(
				nodes -> nodes.size() == 1 && nodes.get(0).getDeptId() == 1L && nodes.get(0).getParentId() == 0L));
	}

	@Test
	@DisplayName("新增部门：编码已存在时抛出 DATA_CODE_DUPLICATE")
	void createThrowsWhenDeptCodeExists() {
		SysDeptForm form = minimalForm(null, 0L, "RD", "研发部");
		when(sysDeptMapper.selectCount(any())).thenReturn(1L);

		ThrowingCallable executable = () -> sysDeptService.create(form);
		assertThatThrownBy(executable).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_CODE_DUPLICATE);

		verify(sysDeptService, never()).save(any(SysDeptEntity.class));
		verify(deptClosureMaintainer, never()).insertNodes(anyList());
	}

	@Test
	@DisplayName("扁平列表：无关键词时查询全量部门")
	void listFlatWithoutKeywordQueriesAllActiveDepts() {
		SysDeptPageRowPO row = new SysDeptPageRowPO();
		row.setId(1L);
		row.setDeptCode("D001");
		row.setDeptName("研发部");
		when(sysDeptMapper.selectListByQuery(any(SysDeptListQuery.class))).thenReturn(List.of(row));

		List<SysDeptListVO> result = sysDeptQueryService.listFlat(new SysDeptListQuery());

		assertEquals(1, result.size());
		assertEquals("D001", result.get(0).getDeptCode());
		verify(sysDeptMapper).selectListByQuery(argThat((SysDeptListQuery q) -> q.getKeyword() == null));
		verify(auditUserDisplayService).enrichAuditUsernames(anyList(), isNull(), isNull());
	}

	@Test
	@DisplayName("扁平列表：关键词原样传给 Mapper")
	void listFlatPassesKeywordAsIs() {
		when(sysDeptMapper.selectListByQuery(any(SysDeptListQuery.class))).thenReturn(Collections.emptyList());

		SysDeptListQuery query = new SysDeptListQuery();
		query.setKeyword("  研发  ");
		sysDeptQueryService.listFlat(query);

		verify(sysDeptMapper).selectListByQuery(argThat((SysDeptListQuery q) -> "  研发  ".equals(q.getKeyword())));
	}

	@Test
	@DisplayName("更新元数据：仅改名称时不提交授权失效")
	void updateMetaWithoutAuthImpactSkipsInvalidation() {
		Long deptId = 1L;
		SysDeptEntity existing = activeDept(deptId, "D001");
		when(deptReferenceChecker.getExistingActive(deptId)).thenReturn(existing);
		when(sysDeptMapper.updateById(any(SysDeptEntity.class))).thenReturn(1);

		SysDeptForm form = minimalForm(deptId, 0L, "D001", "新部门名");
		sysDeptService.updateMeta(form);

		verify(sysDeptMapper).updateById(argThat((SysDeptEntity e) -> "新部门名".equals(e.getDeptName())));
		verify(userDeptMapper, never()).selectUserIdsByAncestorDeptIds(any());
		verify(userAuthorizationInvalidationTrigger, never()).submitByUserIds(any(), any());
		verify(deptClosureMaintainer, never()).moveNode(anyLong(), anyLong());
	}

	@Test
	@DisplayName("更新元数据：父级变更时维护闭包并提交 move 失效")
	void updateMetaWithParentChangeSubmitsMoveInvalidation() {
		Long deptId = 3L;
		SysDeptEntity existing = activeDept(deptId, "D003");
		when(deptReferenceChecker.getExistingActive(deptId)).thenReturn(existing);
		when(sysDeptMapper.countById(10L)).thenReturn(1);
		when(sysDeptMapper.countDescendantRelation(deptId, 10L)).thenReturn(0L);
		when(sysDeptMapper.updateById(any(SysDeptEntity.class))).thenReturn(1);
		when(userDeptMapper.selectUserIdsByAncestorDeptIds(List.of(deptId))).thenReturn(List.of(21L, 22L));

		SysDeptForm form = minimalForm(deptId, 10L, "D003", "原部门");
		sysDeptService.updateMeta(form);

		verify(deptClosureMaintainer).moveNode(deptId, 10L);
		verify(userAuthorizationInvalidationTrigger).submitByUserIds(List.of(21L, 22L), "move");
	}

	@Test
	@DisplayName("移动部门：父级未变时跳过更新")
	void moveSkipsUpdateWhenParentUnchanged() {
		// 父级相同则不写库、不维护闭包
		Long deptId = 6L;
		SysDeptEntity existing = activeDept(deptId, "D006");
		when(deptReferenceChecker.getExistingActive(deptId)).thenReturn(existing);

		SysDeptMoveForm form = new SysDeptMoveForm();
		form.setId(deptId);
		form.setParentId(0L);
		sysDeptService.move(form);

		verify(sysDeptMapper, never()).updateById(any(SysDeptEntity.class));
		verify(deptClosureMaintainer, never()).moveNode(anyLong(), anyLong());
		verify(userDeptMapper, never()).selectUserIdsByAncestorDeptIds(any());
		verify(userAuthorizationInvalidationTrigger, never()).submitByUserIds(any(), any());
	}

	@Test
	@DisplayName("移动部门：父级变更且无成员时不提交失效")
	void moveUpdatesParentWhenChanged() {
		Long deptId = 7L;
		SysDeptEntity existing = activeDept(deptId, "D007");
		when(deptReferenceChecker.getExistingActive(deptId)).thenReturn(existing);
		when(sysDeptMapper.countById(12L)).thenReturn(1);
		when(sysDeptMapper.countDescendantRelation(deptId, 12L)).thenReturn(0L);
		when(sysDeptMapper.updateById(any(SysDeptEntity.class))).thenReturn(1);
		when(userDeptMapper.selectUserIdsByAncestorDeptIds(List.of(deptId))).thenReturn(List.of());

		SysDeptMoveForm form = new SysDeptMoveForm();
		form.setId(deptId);
		form.setParentId(12L);
		sysDeptService.move(form);

		verify(sysDeptMapper).updateById(argThat((SysDeptEntity e) -> Long.valueOf(12L).equals(e.getParentId())));
		verify(deptClosureMaintainer).moveNode(deptId, 12L);
		verify(userAuthorizationInvalidationTrigger, never()).submitByUserIds(any(), any());
	}

	@Test
	@DisplayName("移动部门：父级变更且存在成员时按用户提交失效")
	void moveSubmitsUserInvalidationWhenMembersExist() {
		Long deptId = 11L;
		SysDeptEntity existing = activeDept(deptId, "D011");
		when(deptReferenceChecker.getExistingActive(deptId)).thenReturn(existing);
		when(sysDeptMapper.countById(12L)).thenReturn(1);
		when(sysDeptMapper.countDescendantRelation(deptId, 12L)).thenReturn(0L);
		when(sysDeptMapper.updateById(any(SysDeptEntity.class))).thenReturn(1);
		when(userDeptMapper.selectUserIdsByAncestorDeptIds(List.of(deptId))).thenReturn(List.of(31L));

		SysDeptMoveForm form = new SysDeptMoveForm();
		form.setId(deptId);
		form.setParentId(12L);
		sysDeptService.move(form);

		verify(userAuthorizationInvalidationTrigger).submitByUserIds(List.of(31L), "move");
	}

	@Test
	@DisplayName("删除部门：无引用时先展开成员，空集不提交失效")
	void deleteQueriesMembersThenRemovesWhenUnreferenced() {
		Long deptId = 4L;
		when(deptReferenceChecker.getExistingActive(deptId)).thenReturn(activeDept(deptId, "D004"));
		when(sysDeptMapper.countActiveDirectChildren(deptId)).thenReturn(0);
		when(sysDeptMapper.existsSubtreeReference(deptId)).thenReturn(false);
		when(userDeptMapper.selectUserIdsByAncestorDeptIds(List.of(deptId))).thenReturn(List.of());
		when(sysDeptMapper.deleteById(deptId)).thenReturn(1);

		sysDeptService.deleteById(deptId);

		var inOrder = inOrder(userDeptMapper, deptClosureMaintainer, sysDeptMapper);
		inOrder.verify(userDeptMapper).selectUserIdsByAncestorDeptIds(List.of(deptId));
		inOrder.verify(deptClosureMaintainer).removeClosurePaths(deptId);
		inOrder.verify(sysDeptMapper).deleteById(deptId);
		verify(userAuthorizationInvalidationTrigger, never()).submitByUserIds(any(), any());
	}

	@Test
	@DisplayName("删除部门：子树仍被引用时不展开成员")
	void deleteRejectsWhenSubtreeReferenced() {
		Long deptId = 5L;
		when(deptReferenceChecker.getExistingActive(deptId)).thenReturn(activeDept(deptId, "D005"));
		when(sysDeptMapper.countActiveDirectChildren(deptId)).thenReturn(0);
		when(sysDeptMapper.existsSubtreeReference(deptId)).thenReturn(true);

		assertThatThrownBy(() -> sysDeptService.deleteById(deptId)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_IN_USE);

		verify(userDeptMapper, never()).selectUserIdsByAncestorDeptIds(any());
		verify(deptClosureMaintainer, never()).removeClosurePaths(anyLong());
	}

	@Test
	@DisplayName("移动部门：父部门不存在时抛出 TREE_PARENT_UNAVAILABLE")
	void moveThrowsWhenParentMissing() {
		Long deptId = 8L;
		SysDeptEntity existing = activeDept(deptId, "D008");
		when(deptReferenceChecker.getExistingActive(deptId)).thenReturn(existing);
		when(sysDeptMapper.countById(99L)).thenReturn(0);

		SysDeptMoveForm form = new SysDeptMoveForm();
		form.setId(deptId);
		form.setParentId(99L);
		assertThatThrownBy(() -> sysDeptService.move(form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.TREE_PARENT_UNAVAILABLE);

		verify(sysDeptMapper, never()).updateById(any(SysDeptEntity.class));
		verify(deptClosureMaintainer, never()).moveNode(anyLong(), anyLong());
	}

}

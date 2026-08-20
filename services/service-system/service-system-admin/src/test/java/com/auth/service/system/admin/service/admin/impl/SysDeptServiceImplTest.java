package com.auth.service.system.admin.service.admin.impl;

import com.auth.common.core.constants.BatchSizes;
import com.auth.common.core.model.form.IdsEnableStatusForm;
import com.auth.service.system.admin.mapper.admin.dept.SysDeptMapper;
import com.auth.service.system.admin.mapper.authorization.DeptRelationQueryMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.form.dept.SysDeptForm;
import com.auth.service.system.admin.model.form.dept.SysDeptMoveForm;
import com.auth.service.system.admin.model.po.dept.SysDeptPageRowPO;
import com.auth.service.system.admin.model.query.dept.SysDeptListQuery;
import com.auth.service.system.admin.model.vo.dept.SysDeptListVO;
import com.auth.service.system.admin.support.dept.DeptClosureMaintainer;
import com.auth.service.system.admin.support.dept.DeptReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.DeptAuthorizationInvalidationTrigger;
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
	private DeptAuthorizationInvalidationTrigger deptInvalidationTrigger;

	@Mock
	private DeptRelationQueryMapper deptRelationQueryMapper;

	private SysDeptServiceImpl sysDeptService;

	private SysDeptQueryServiceImpl sysDeptQueryService;

	private static SysDeptForm minimalForm(Long id, long parentId, String deptCode, String deptName, boolean status) {
		SysDeptForm form = new SysDeptForm();
		form.setId(id);
		form.setParentId(parentId);
		form.setDeptCode(deptCode);
		form.setDeptName(deptName);
		form.setStatus(status);
		form.setOrderNum(0);
		return form;
	}

	private static SysDeptEntity activeDept(Long id, String deptCode) {
		SysDeptEntity entity = new SysDeptEntity();
		entity.setId(id);
		entity.setParentId(0L);
		entity.setDeptCode(deptCode);
		entity.setDeptName("原部门");
		entity.setStatus(true);
		return entity;
	}

	@BeforeEach
	void setUp() throws Exception {
		sysDeptService = spy(
				new SysDeptServiceImpl(deptReferenceChecker, deptClosureMaintainer, deptInvalidationTrigger));
		sysDeptQueryService = new SysDeptQueryServiceImpl(auditUserDisplayService, deptReferenceChecker,
				deptRelationQueryMapper);
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysDeptService, sysDeptMapper);
		baseMapperField.set(sysDeptQueryService, sysDeptMapper);
		lenient().doAnswer(invocation -> {
			List<SysDeptEntity> entities = invocation.getArgument(0);
			long id = 1L;
			for (SysDeptEntity entity : entities) {
				entity.setId(id++);
			}
			return true;
		}).when(sysDeptService).saveBatch(anyList(), anyInt());
	}

	@Test
	@DisplayName("批量新增部门：空列表直接返回")
	void createBatchFromImportSkipsWhenEmpty() {
		sysDeptService.createBatchFromImport(List.of());

		verify(sysDeptMapper, never()).selectActiveByDeptCodes(anyList());
		verify(sysDeptService, never()).saveBatch(anyList(), anyInt());
	}

	@Test
	@DisplayName("批量新增部门：校验通过后批量落库并维护闭包")
	void createBatchFromImportPersistsEntitiesAndInsertsClosure() {
		SysDeptForm form = minimalForm(null, 0L, "RD", "研发部", true);
		when(sysDeptMapper.selectActiveByDeptCodes(List.of("RD"))).thenReturn(List.of());

		sysDeptService.createBatchFromImport(List.of(form));

		verify(sysDeptMapper).selectActiveByDeptCodes(List.of("RD"));
		verify(sysDeptMapper, never()).countById(anyLong());
		verify(sysDeptService).saveBatch(anyList(), eq(BatchSizes.SIZE_500));
		verify(deptClosureMaintainer)
			.insertNodes(argThat(nodes -> nodes.size() == 1 && nodes.get(0).getParentId() == 0L));
	}

	@Test
	@DisplayName("批量新增部门：编码已存在时抛出 DATA_CODE_DUPLICATE")
	void createBatchFromImportThrowsWhenDeptCodeExists() {
		SysDeptForm form = minimalForm(null, 0L, "RD", "研发部", true);
		SysDeptEntity existing = activeDept(1L, "RD");
		when(sysDeptMapper.selectActiveByDeptCodes(List.of("RD"))).thenReturn(List.of(existing));

		ThrowingCallable executable = () -> sysDeptService.createBatchFromImport(List.of(form));
		assertThatThrownBy(executable).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_CODE_DUPLICATE);

		verify(sysDeptService, never()).saveBatch(anyList(), anyInt());
		verify(deptClosureMaintainer, never()).insertNodes(anyList());
	}

	@Test
	@DisplayName("批量新增部门：请求内编码重复时抛出 DATA_CODE_DUPLICATE")
	void createBatchFromImportThrowsWhenDeptCodesDuplicatedInRequest() {
		SysDeptForm first = minimalForm(null, 0L, "RD", "研发部", true);
		SysDeptForm second = minimalForm(null, 0L, "RD", "另一研发", true);

		ThrowingCallable executable = () -> sysDeptService.createBatchFromImport(List.of(first, second));
		assertThatThrownBy(executable).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_CODE_DUPLICATE);

		verify(sysDeptMapper, never()).selectActiveByDeptCodes(any());
		verify(sysDeptService, never()).saveBatch(anyList(), anyInt());
	}

	@Test
	@DisplayName("扁平列表：无关键词时查询全量部门")
	void listFlatWithoutKeywordQueriesAllActiveDepts() {
		SysDeptPageRowPO row = new SysDeptPageRowPO();
		row.setId(1L);
		row.setDeptCode("D001");
		row.setDeptName("研发部");
		row.setStatus(true);
		row.setEffective(true);
		when(sysDeptMapper.selectListByQuery(any(SysDeptListQuery.class))).thenReturn(List.of(row));

		List<SysDeptListVO> result = sysDeptQueryService.listFlat(new SysDeptListQuery());

		assertEquals(1, result.size());
		assertEquals(Boolean.TRUE, result.get(0).getEffective());
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

		SysDeptForm form = minimalForm(deptId, 0L, "D001", "新部门名", true);
		sysDeptService.updateMeta(form);

		verify(sysDeptMapper).updateById(argThat((SysDeptEntity e) -> "新部门名".equals(e.getDeptName())));
		verify(deptInvalidationTrigger, never()).submitByDeptIds(any(), any());
		verify(deptClosureMaintainer, never()).moveNode(anyLong(), anyLong());
	}

	@Test
	@DisplayName("更新元数据：状态变更时提交 USER_DEPT 授权失效")
	void updateMetaWithStatusChangeSubmitsInvalidation() {
		Long deptId = 2L;
		SysDeptEntity existing = activeDept(deptId, "D002");
		when(deptReferenceChecker.getExistingActive(deptId)).thenReturn(existing);
		when(sysDeptMapper.updateById(any(SysDeptEntity.class))).thenReturn(1);

		SysDeptForm form = minimalForm(deptId, 0L, "D002", "原部门", false);
		sysDeptService.updateMeta(form);

		verify(sysDeptMapper).updateById(argThat((SysDeptEntity e) -> e.getStatus() != null && !e.getStatus()));
		verify(deptInvalidationTrigger).submitByDeptIds(List.of(deptId), "update");
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

		SysDeptForm form = minimalForm(deptId, 10L, "D003", "原部门", true);
		sysDeptService.updateMeta(form);

		verify(deptClosureMaintainer).moveNode(deptId, 10L);
		verify(deptInvalidationTrigger).submitByDeptIds(List.of(deptId), "move");
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
		verify(deptInvalidationTrigger, never()).submitByDeptIds(any(), any());
	}

	@Test
	@DisplayName("移动部门：父级变更时更新 parentId、维护闭包并提交失效")
	void moveUpdatesParentWhenChanged() {
		// 变更父级后写库并触发授权失效
		Long deptId = 7L;
		SysDeptEntity existing = activeDept(deptId, "D007");
		when(deptReferenceChecker.getExistingActive(deptId)).thenReturn(existing);
		when(sysDeptMapper.countById(12L)).thenReturn(1);
		when(sysDeptMapper.countDescendantRelation(deptId, 12L)).thenReturn(0L);
		when(sysDeptMapper.updateById(any(SysDeptEntity.class))).thenReturn(1);

		SysDeptMoveForm form = new SysDeptMoveForm();
		form.setId(deptId);
		form.setParentId(12L);
		sysDeptService.move(form);

		verify(sysDeptMapper).updateById(argThat((SysDeptEntity e) -> Long.valueOf(12L).equals(e.getParentId())));
		verify(deptClosureMaintainer).moveNode(deptId, 12L);
		verify(deptInvalidationTrigger).submitByDeptIds(List.of(deptId), "move");
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

	@Test
	@DisplayName("批量启停：仅更新入参 ids，不级联子部门")
	void batchUpdateStatusUpdatesOnlyGivenIds() {
		doReturn(true).when(sysDeptService).updateBatchById(anyList());

		IdsEnableStatusForm form = new IdsEnableStatusForm();
		form.setIds(List.of(1L, 2L));
		form.setStatus(false);
		sysDeptService.batchUpdateStatus(form);

		verify(sysDeptService).updateBatchById(argThat((List<SysDeptEntity> list) -> list.size() == 2
				&& list.stream().allMatch(e -> Boolean.FALSE.equals(e.getStatus()))));
		verify(deptInvalidationTrigger).submitByDeptIds(List.of(1L, 2L), "update");
		verify(deptClosureMaintainer, never()).moveNode(anyLong(), anyLong());
	}

	@Test
	@DisplayName("批量启停：ids 为空时不写库")
	void batchUpdateStatusSkipsWhenIdsEmpty() {
		IdsEnableStatusForm form = new IdsEnableStatusForm();
		form.setIds(List.of());
		form.setStatus(true);
		sysDeptService.batchUpdateStatus(form);

		verify(sysDeptService, never()).updateBatchById(anyList());
		verify(deptInvalidationTrigger, never()).submitByDeptIds(any(), any());
	}

}

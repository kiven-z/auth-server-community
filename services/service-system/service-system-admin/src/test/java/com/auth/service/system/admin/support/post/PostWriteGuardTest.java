package com.auth.service.system.admin.support.post;

import com.auth.service.system.admin.excel.post.SysPostImportRow;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.admin.post.SysPostMapper;
import com.auth.service.system.admin.model.form.post.SysPostForm;
import com.auth.service.system.admin.model.po.post.PostDeptCodePairPO;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * {@link PostWriteGuard} 单元测试。
 */
@DisplayName("PostWriteGuard 岗位写入复合键校验")
@ExtendWith(MockitoExtension.class)
class PostWriteGuardTest {

	@Mock
	private SysPostMapper sysPostMapper;

	private static SysPostForm form(Long deptId, String postCode) {
		SysPostForm form = new SysPostForm();
		form.setDeptId(deptId);
		form.setPostCode(postCode);
		return form;
	}

	private static SysPostImportRow importRow(String deptCode, String postCode) {
		SysPostImportRow row = new SysPostImportRow();
		row.setDeptCode(deptCode);
		row.setPostCode(postCode);
		return row;
	}

	@Test
	@DisplayName("deptIdPostCodeKey 与 deptCodePostCodeKey 使用统一分隔符")
	void compositeKeyBuildersUseSharedSeparator() {
		assertThat(PostWriteGuard.deptIdPostCodeKey(1L, "A")).isEqualTo("1" + PostWriteGuard.COMPOSITE_KEY_SEP + "A");
		assertThat(PostWriteGuard.deptCodePostCodeKey("RD", "A"))
			.isEqualTo("RD" + PostWriteGuard.COMPOSITE_KEY_SEP + "A");
	}

	@Test
	@DisplayName("requireBatchCreatable：部门不可挂载时抛出 DATA_UNAVAILABLE")
	void requireBatchCreatableThrowsWhenDeptNotAssignable() {
		when(sysPostMapper.selectAssignableDeptIds(List.of(99L))).thenReturn(List.of());

		SysPostForm postForm = form(99L, "P1");
		List<SysPostForm> forms = List.of(postForm);
		assertThatThrownBy(() -> PostWriteGuard.requireBatchCreatable(sysPostMapper, forms))
			.isInstanceOf(SystemBusinessException.class)
			.satisfies(ex -> {
				SystemBusinessException biz = (SystemBusinessException) ex;
				assertThat(biz.getResultCode()).isEqualTo(SystemCommonResultCode.DATA_UNAVAILABLE);
				assertThat(biz.getMessageArgs()).isEmpty();
			});
	}

	@Test
	@DisplayName("requireBatchCreatable：库中已存在复合键时抛出 POST_CODE_DUPLICATE_IN_DEPT")
	void requireBatchCreatableThrowsWhenDbConflict() {
		when(sysPostMapper.selectAssignableDeptIds(List.of(1L))).thenReturn(List.of(1L));
		PostDeptCodePairPO existing = new PostDeptCodePairPO();
		existing.setDeptId(1L);
		existing.setPostCode("DUP");
		when(sysPostMapper.selectReferenceByDeptPostPairs(anyList())).thenReturn(List.of(existing));

		SysPostForm duplicateForm = form(1L, "DUP");
		List<SysPostForm> duplicateForms = List.of(duplicateForm);
		assertThatThrownBy(() -> PostWriteGuard.requireBatchCreatable(sysPostMapper, duplicateForms))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.POST_CODE_DUPLICATE_IN_DEPT);
	}

	@Test
	@DisplayName("precheckForImport：汇总不可挂载部门、文件内重复与库中已存在键")
	void precheckForImportAggregatesImportViolations() {
		List<SysPostImportRow> rows = List.of(importRow("RD", "A"), importRow("RD", "A"), importRow("OPS", "B"));
		Map<String, Long> deptIdByCode = Map.of("RD", 1L, "OPS", 2L);
		when(sysPostMapper.selectAssignableDeptIds(anyList())).thenReturn(List.of(1L));
		PostDeptCodePairPO existing = new PostDeptCodePairPO();
		existing.setDeptId(2L);
		existing.setPostCode("B");
		when(sysPostMapper.selectReferenceByDeptPostPairs(anyList())).thenReturn(List.of(existing));

		PostWriteGuard.ImportPrecheck precheck = PostWriteGuard.precheckForImport(sysPostMapper, rows, deptIdByCode);

		assertThat(precheck.getUnassignableDeptCodes()).containsExactly("OPS");
		assertThat(precheck.getDuplicateCompositeKeysInFile())
			.containsExactly(PostWriteGuard.deptCodePostCodeKey("RD", "A"));
		assertThat(precheck.getExistingCompositeKeys()).containsExactly(PostWriteGuard.deptIdPostCodeKey(2L, "B"));
	}

	@Test
	@DisplayName("requireBatchCreatable：空列表直接返回")
	void requireBatchCreatableSkipsWhenEmpty() {
		PostWriteGuard.requireBatchCreatable(sysPostMapper, List.of());

		verify(sysPostMapper, never()).selectAssignableDeptIds(anyList());
	}

}

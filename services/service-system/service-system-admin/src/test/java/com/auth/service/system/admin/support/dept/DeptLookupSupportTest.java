package com.auth.service.system.admin.support.dept;

import com.auth.service.system.admin.mapper.admin.dept.SysDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.po.dept.SysDeptPathRowPO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link DeptLookupSupport} 单元测试
 */
@DisplayName("DeptLookupSupport 部门只读批量查询")
@ExtendWith(MockitoExtension.class)
class DeptLookupSupportTest {

	@Mock
	private SysDeptMapper sysDeptMapper;

	@InjectMocks
	private DeptLookupSupport deptLookupSupport;

	@Test
	@DisplayName("空 ID 列表返回空映射且不访问数据库")
	void resolvePaths_whenEmptyIds_returnsEmptyMap() {
		Map<Long, String> result = deptLookupSupport.resolvePaths(Collections.emptyList());

		assertThat(result).isEmpty();
		verifyNoInteractions(sysDeptMapper);
	}

	@Test
	@DisplayName("按 descendant_id 组装根到叶的全路径映射")
	void resolvePaths_whenIdsGiven_returnsPathById() {
		SysDeptPathRowPO root = new SysDeptPathRowPO();
		root.setId(1L);
		root.setDeptPath("总部");

		SysDeptPathRowPO child = new SysDeptPathRowPO();
		child.setId(3L);
		child.setDeptPath("总部/研发/后端");

		when(sysDeptMapper.selectDeptPathByIds(List.of(1L, 3L))).thenReturn(List.of(root, child));

		Map<Long, String> result = deptLookupSupport.resolvePaths(List.of(1L, 3L));

		assertThat(result).containsEntry(1L, "总部").containsEntry(3L, "总部/研发/后端");
	}

	@Test
	@DisplayName("空行列表解析编码时返回空映射且不访问数据库")
	void resolveIdsByCodes_whenEmptyRows_returnsEmptyMap() {
		record ImportRow(String deptCode) {
		}

		Map<String, Long> result = deptLookupSupport.resolveIdsByCodes(Collections.emptyList(), ImportRow::deptCode);

		assertThat(result).isEmpty();
		verifyNoInteractions(sysDeptMapper);
	}

	@Test
	@DisplayName("按部门编码批量解析主键映射")
	void resolveIdsByCodes_whenCodesGiven_returnsIdByCode() {
		SysDeptEntity dept = new SysDeptEntity();
		dept.setId(10L);
		dept.setDeptCode("RD");

		record ImportRow(String deptCode) {
		}

		when(sysDeptMapper.selectActiveByDeptCodes(List.of("RD"))).thenReturn(List.of(dept));

		Map<String, Long> result = deptLookupSupport.resolveIdsByCodes(List.of(new ImportRow("RD")),
				ImportRow::deptCode);

		assertThat(result).containsEntry("RD", 10L);
	}

}

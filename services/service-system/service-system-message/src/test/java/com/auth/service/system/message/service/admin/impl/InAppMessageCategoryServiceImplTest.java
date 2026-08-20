package com.auth.service.system.message.service.admin.impl;

import com.auth.common.core.model.form.IdsEnableStatusForm;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.mapper.InAppMessageCategoryMapper;
import com.auth.service.system.message.model.entity.InAppMessageCategoryEntity;
import com.auth.service.system.message.model.form.inapp.InAppMessageCategoryForm;
import com.auth.service.system.message.model.po.InAppMessageCategoryDetailRowPO;
import com.auth.service.system.message.model.po.InAppMessageCategoryPageRowPO;
import com.auth.service.system.message.model.query.InAppMessageCategoryQuery;
import com.auth.service.system.message.model.vo.inapp.InAppMessageCategoryDetailVO;
import com.auth.service.system.message.model.vo.inapp.InAppMessageCategoryPageVO;
import com.auth.service.system.message.model.vo.inapp.InAppMessageCategoryVO;
import com.auth.service.system.message.support.inapp.InAppMessageCategorySupport;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
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

import static com.auth.common.core.utils.TreeParentIdUtil.ROOT_PARENT_ID;
import static com.auth.service.system.common.exception.code.SystemCommonResultCode.*;
import static com.auth.service.system.message.exception.MessageResultCode.IN_APP_MESSAGE_CATEGORY_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link InAppMessageCategoryServiceImpl} 单元测试
 *
 * @author Bunny
 */
@DisplayName("InAppMessageCategoryServiceImpl 站内信分类")
@ExtendWith(MockitoExtension.class)
class InAppMessageCategoryServiceImplTest {

	@Mock
	private InAppMessageCategoryMapper inAppMessageCategoryMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	private InAppMessageCategoryServiceImpl service;

	@BeforeAll
	static void initMybatisPlusTableInfo() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
				InAppMessageCategoryEntity.class);
	}

	private static InAppMessageCategoryEntity major(Long id, String code, String name, int sort) {
		InAppMessageCategoryEntity entity = new InAppMessageCategoryEntity();
		entity.setId(id);
		entity.setParentId(0L);
		entity.setCode(code);
		entity.setName(name);
		entity.setSortOrder(sort);
		entity.setStatus(Boolean.TRUE);
		return entity;
	}

	private static InAppMessageCategoryEntity child() {
		InAppMessageCategoryEntity entity = major(11L, "SERVICE_INSPECTION", "巡检报告", 20);
		entity.setParentId(1L);
		return entity;
	}

	private static InAppMessageCategoryDetailRowPO detailMajor(Long id, String code, String name, int sort) {
		InAppMessageCategoryDetailRowPO row = new InAppMessageCategoryDetailRowPO();
		row.setId(id);
		row.setParentId(0L);
		row.setCode(code);
		row.setName(name);
		row.setSortOrder(sort);
		row.setStatus(Boolean.TRUE);
		return row;
	}

	private static InAppMessageCategoryDetailRowPO detailChild() {
		InAppMessageCategoryDetailRowPO row = detailMajor(11L, "SERVICE_INSPECTION", "巡检报告", 20);
		row.setParentId(1L);
		row.setParentCode("SERVICE");
		row.setParentName("服务消息");
		return row;
	}

	/**
	 * 最小合法新增表单（大类）
	 */
	private static InAppMessageCategoryForm majorForm(String code, String name) {
		InAppMessageCategoryForm form = new InAppMessageCategoryForm();
		form.setCode(code);
		form.setName(name);
		form.setStatus(Boolean.TRUE);
		form.setSortOrder(10);
		return form;
	}

	@BeforeEach
	void setUp() {
		// ServiceImpl.baseMapper 由 Spring 字段注入；单测手动挂上；Support 用同一 Mapper mock
		InAppMessageCategorySupport support = new InAppMessageCategorySupport(inAppMessageCategoryMapper);
		service = new InAppMessageCategoryServiceImpl(auditUserDisplayService, support);
		ReflectionTestUtils.setField(service, "baseMapper", inAppMessageCategoryMapper);
	}

	@Test
	@DisplayName("详情：大类返回字段并填充审计用户名")
	void getCategoryById_shouldReturnMajorDetailAndEnrichAudit() {
		// 一次 SQL 左联；大类无父级展示码/名
		InAppMessageCategoryDetailRowPO row = detailMajor(1L, "SERVICE", "服务消息", 10);
		row.setRemark("种子大类");
		when(inAppMessageCategoryMapper.selectDetailById(1L)).thenReturn(row);

		InAppMessageCategoryDetailVO vo = service.getCategoryById(1L);

		assertThat(vo.getId()).isEqualTo(1L);
		assertThat(vo.getParentId()).isZero();
		assertThat(vo.getParentCode()).isNull();
		assertThat(vo.getParentName()).isNull();
		assertThat(vo.getCode()).isEqualTo("SERVICE");
		assertThat(vo.getName()).isEqualTo("服务消息");
		assertThat(vo.getSortOrder()).isEqualTo(10);
		assertThat(vo.getStatus()).isTrue();
		assertThat(vo.getRemark()).isEqualTo("种子大类");
		verify(auditUserDisplayService).enrichAuditUsernames(any(List.class), any(), any());
	}

	@Test
	@DisplayName("详情：小类附带父级 code/name")
	void getCategoryById_shouldFillParentCodeAndNameForChild() {
		// 父级字段由 SQL 左联带出，无二次查库
		InAppMessageCategoryDetailRowPO row = detailChild();
		row.setRemark("子类备注");
		when(inAppMessageCategoryMapper.selectDetailById(11L)).thenReturn(row);

		InAppMessageCategoryDetailVO vo = service.getCategoryById(11L);

		assertThat(vo.getId()).isEqualTo(11L);
		assertThat(vo.getParentId()).isEqualTo(1L);
		assertThat(vo.getParentCode()).isEqualTo("SERVICE");
		assertThat(vo.getParentName()).isEqualTo("服务消息");
		assertThat(vo.getCode()).isEqualTo("SERVICE_INSPECTION");
		assertThat(vo.getRemark()).isEqualTo("子类备注");
	}

	@Test
	@DisplayName("详情：主键不存在时抛数据不存在")
	void getCategoryById_shouldThrowWhenMissing() {
		// 未知 id 直接失败
		when(inAppMessageCategoryMapper.selectDetailById(999L)).thenReturn(null);

		assertThatThrownBy(() -> service.getCategoryById(999L)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(DATA_NOT_EXIST);
	}

	@Test
	@DisplayName("全量列表：映射扁平行并填充审计用户名")
	void listCategories_shouldMapFieldsAndEnrichAudit() {
		// 树视图：一次拉全量扁平数据，前端 handleTree
		InAppMessageCategoryQuery query = new InAppMessageCategoryQuery();
		query.setStatus(Boolean.TRUE);

		Instant now = LocalDateTime.of(2026, 7, 20, 12, 0, 0).toInstant(java.time.ZoneOffset.UTC);
		InAppMessageCategoryPageRowPO major = new InAppMessageCategoryPageRowPO();
		major.setId(1L);
		major.setParentId(0L);
		major.setCode("SERVICE");
		major.setName("服务消息");
		major.setSortOrder(10);
		major.setStatus(Boolean.TRUE);
		major.setCreatedAt(now);
		major.setUpdatedAt(now);
		major.setCreatedBy(1L);
		major.setUpdatedBy(1L);

		InAppMessageCategoryPageRowPO child = new InAppMessageCategoryPageRowPO();
		child.setId(11L);
		child.setParentId(1L);
		child.setParentCode("SERVICE");
		child.setParentName("服务消息");
		child.setCode("SERVICE_INSPECTION");
		child.setName("巡检报告");
		child.setSortOrder(20);
		child.setStatus(Boolean.TRUE);
		child.setCreatedAt(now);
		child.setUpdatedAt(now);
		child.setCreatedBy(1L);
		child.setUpdatedBy(1L);

		when(inAppMessageCategoryMapper.selectCategoryList(query)).thenReturn(List.of(major, child));

		List<InAppMessageCategoryPageVO> result = service.listCategories(query);

		assertThat(result).hasSize(2);
		assertThat(result.get(0).getCode()).isEqualTo("SERVICE");
		assertThat(result.get(1).getParentId()).isEqualTo(1L);
		assertThat(result.get(1).getParentCode()).isEqualTo("SERVICE");
		verify(auditUserDisplayService).enrichAuditUsernames(any(List.class), any(), any());
		verify(inAppMessageCategoryMapper).selectCategoryList(query);
	}

	@Test
	@DisplayName("新增：大类校验通过则 insert")
	void create_shouldInsertMajorWhenCodeUnique() {
		// code 唯一且无父节点 → 大类落库
		when(inAppMessageCategoryMapper.selectCount(any())).thenReturn(0L);
		when(inAppMessageCategoryMapper.insert(any(InAppMessageCategoryEntity.class))).thenReturn(1);

		service.create(majorForm("NEW_MAJOR", "新大类"));

		verify(inAppMessageCategoryMapper)
			.insert(argThat((InAppMessageCategoryEntity e) -> "NEW_MAJOR".equals(e.getCode())
					&& "新大类".equals(e.getName()) && Long.valueOf(0L).equals(e.getParentId())
					&& Boolean.TRUE.equals(e.getStatus()) && Integer.valueOf(10).equals(e.getSortOrder())));
	}

	@Test
	@DisplayName("新增：小类挂在大类下")
	void create_shouldInsertChildUnderMajor() {
		// parent 为大类时允许建小类
		InAppMessageCategoryEntity parent = major(1L, "SERVICE", "服务消息", 10);
		when(inAppMessageCategoryMapper.selectCount(any())).thenReturn(0L);
		when(inAppMessageCategoryMapper.selectById(1L)).thenReturn(parent);
		when(inAppMessageCategoryMapper.insert(any(InAppMessageCategoryEntity.class))).thenReturn(1);

		InAppMessageCategoryForm form = majorForm("SERVICE_HELP", "使用帮助");
		form.setParentId(1L);
		form.setSortOrder(null);

		service.create(form);

		verify(inAppMessageCategoryMapper)
			.insert(argThat((InAppMessageCategoryEntity e) -> "SERVICE_HELP".equals(e.getCode())
					&& Long.valueOf(1L).equals(e.getParentId()) && Integer.valueOf(0).equals(e.getSortOrder())));
	}

	@Test
	@DisplayName("新增：parentId 为空时规范化为 0 并落库大类")
	void create_shouldNormalizeNullParentIdToRoot() {
		// 表单未传父级时按 TreeParentIdUtil 写成 0，避免库中出现 NULL
		when(inAppMessageCategoryMapper.selectCount(any())).thenReturn(0L);
		when(inAppMessageCategoryMapper.insert(any(InAppMessageCategoryEntity.class))).thenReturn(1);

		InAppMessageCategoryForm form = majorForm("ROOT_NULL", "空父大类");
		form.setParentId(null);

		service.create(form);

		verify(inAppMessageCategoryMapper, never()).selectById(any());
		verify(inAppMessageCategoryMapper)
			.insert(argThat((InAppMessageCategoryEntity e) -> Long.valueOf(0L).equals(e.getParentId())));
	}

	@Test
	@DisplayName("新增：分类码重复时抛出 DATA_CODE_DUPLICATE")
	void create_shouldRejectDuplicateCode() {
		// UK 冲突前置校验
		when(inAppMessageCategoryMapper.selectCount(any())).thenReturn(1L);
		InAppMessageCategoryForm form = majorForm("SERVICE", "重复");

		assertThatThrownBy(() -> service.create(form)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(DATA_CODE_DUPLICATE);
		verify(inAppMessageCategoryMapper, never()).insert(any(InAppMessageCategoryEntity.class));
	}

	@Test
	@DisplayName("新增：父节点不存在时抛出 TREE_PARENT_UNAVAILABLE")
	void create_shouldRejectMissingParent() {
		// 父 ID 无效
		when(inAppMessageCategoryMapper.selectCount(any())).thenReturn(0L);
		when(inAppMessageCategoryMapper.selectById(99L)).thenReturn(null);

		InAppMessageCategoryForm form = majorForm("ORPHAN_CHILD", "孤儿小类");
		form.setParentId(99L);

		assertThatThrownBy(() -> service.create(form)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(TREE_PARENT_UNAVAILABLE);
		verify(inAppMessageCategoryMapper, never()).insert(any(InAppMessageCategoryEntity.class));
	}

	@Test
	@DisplayName("新增：父节点是小类时抛出 TREE_PARENT_UNAVAILABLE")
	void create_shouldRejectNonMajorParent() {
		// 只允许两级：小类不能再挂子节点
		when(inAppMessageCategoryMapper.selectCount(any())).thenReturn(0L);
		when(inAppMessageCategoryMapper.selectById(11L)).thenReturn(child());

		InAppMessageCategoryForm form = majorForm("TOO_DEEP", "三级");
		form.setParentId(11L);

		assertThatThrownBy(() -> service.create(form)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(TREE_PARENT_UNAVAILABLE);
		verify(inAppMessageCategoryMapper, never()).insert(any(InAppMessageCategoryEntity.class));
	}

	@Test
	@DisplayName("更新：字段变更后 updateById")
	void update_shouldPersistWhenValid() {
		// 存在记录且 code 不冲突 → 合并字段并更新
		InAppMessageCategoryEntity existing = major(1L, "SERVICE", "服务消息", 10);
		when(inAppMessageCategoryMapper.selectById(1L)).thenReturn(existing);
		when(inAppMessageCategoryMapper.selectCount(any())).thenReturn(0L);
		when(inAppMessageCategoryMapper.updateById(any(InAppMessageCategoryEntity.class))).thenReturn(1);

		InAppMessageCategoryForm form = majorForm("SERVICE", "服务消息-改");
		form.setId(1L);
		form.setSortOrder(20);
		form.setStatus(Boolean.FALSE);

		service.update(form);

		verify(inAppMessageCategoryMapper)
			.updateById(argThat((InAppMessageCategoryEntity e) -> "服务消息-改".equals(e.getName())
					&& Integer.valueOf(20).equals(e.getSortOrder()) && Boolean.FALSE.equals(e.getStatus())
					&& Long.valueOf(0L).equals(e.getParentId())));
	}

	@Test
	@DisplayName("更新：记录不存在时抛出 DATA_NOT_EXIST")
	void update_shouldRejectWhenMissing() {
		// 主键无效
		when(inAppMessageCategoryMapper.selectById(99L)).thenReturn(null);

		InAppMessageCategoryForm form = majorForm("X", "不存在");
		form.setId(99L);

		assertThatThrownBy(() -> service.update(form)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(DATA_NOT_EXIST);
		verify(inAppMessageCategoryMapper, never()).updateById(any(InAppMessageCategoryEntity.class));
	}

	@Test
	@DisplayName("更新：改码与其它记录冲突时抛出 DATA_CODE_DUPLICATE")
	void update_shouldRejectDuplicateCode() {
		// 改 code 撞 UK
		when(inAppMessageCategoryMapper.selectById(1L)).thenReturn(major(1L, "SERVICE", "服务消息", 10));
		when(inAppMessageCategoryMapper.selectCount(any())).thenReturn(1L);

		InAppMessageCategoryForm form = majorForm("ACTIVITY", "冲突");
		form.setId(1L);

		assertThatThrownBy(() -> service.update(form)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(DATA_CODE_DUPLICATE);
		verify(inAppMessageCategoryMapper, never()).updateById(any(InAppMessageCategoryEntity.class));
	}

	@Test
	@DisplayName("更新：有子节点的大类不可降为小类")
	void update_shouldRejectDemoteMajorWithChildren() {
		// 大类仍有小类时禁止改 parentId
		when(inAppMessageCategoryMapper.selectById(1L)).thenReturn(major(1L, "SERVICE", "服务消息", 10));
		when(inAppMessageCategoryMapper.selectCount(any())).thenReturn(0L, 2L);
		when(inAppMessageCategoryMapper.selectById(2L)).thenReturn(major(2L, "ACTIVITY", "活动消息", 20));

		InAppMessageCategoryForm form = majorForm("SERVICE", "服务消息");
		form.setId(1L);
		form.setParentId(2L);

		assertThatThrownBy(() -> service.update(form)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(TREE_HAS_ACTIVE_CHILDREN);
		verify(inAppMessageCategoryMapper, never()).updateById(any(InAppMessageCategoryEntity.class));
	}

	@Test
	@DisplayName("更新：不可将父级设为自己")
	void update_shouldRejectSelfParent() {
		// 自引用父级非法
		when(inAppMessageCategoryMapper.selectById(1L)).thenReturn(major(1L, "SERVICE", "服务消息", 10));
		when(inAppMessageCategoryMapper.selectCount(any())).thenReturn(0L);

		InAppMessageCategoryForm form = majorForm("SERVICE", "服务消息");
		form.setId(1L);
		form.setParentId(1L);

		assertThatThrownBy(() -> service.update(form)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(TREE_PARENT_UNAVAILABLE);
		verify(inAppMessageCategoryMapper, never()).updateById(any(InAppMessageCategoryEntity.class));
	}

	@Test
	@DisplayName("批量启停：按 ids 更新 status")
	void batchUpdateStatus_shouldUpdateByIds() {
		// IService.update 最终走 baseMapper.update
		List<Long> ids = List.of(1L, 2L);
		when(inAppMessageCategoryMapper.update(any(), any())).thenReturn(2);

		IdsEnableStatusForm form = new IdsEnableStatusForm();
		form.setIds(ids);
		form.setStatus(Boolean.FALSE);
		service.batchUpdateStatus(form);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Wrapper<InAppMessageCategoryEntity>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
		verify(inAppMessageCategoryMapper).update(isNull(), wrapperCaptor.capture());
		assertThat(wrapperCaptor.getValue()).isNotNull();
	}

	@Test
	@DisplayName("批量启停：ids 为空时不调用更新")
	void batchUpdateStatus_shouldSkipWhenIdsEmpty() {
		// 空列表直接返回，避免无意义 UPDATE
		IdsEnableStatusForm form = new IdsEnableStatusForm();
		form.setIds(Collections.emptyList());
		form.setStatus(Boolean.TRUE);
		service.batchUpdateStatus(form);

		verify(inAppMessageCategoryMapper, never()).update(any(), any());
	}

	@Test
	@DisplayName("listMajors：status=true 时按启用过滤并映射扁平 VO")
	void listMajors_shouldReturnEnabledMajorsThenMapVo() {
		InAppMessageCategoryEntity serviceCat = major(1L, "SERVICE", "服务消息", 10);
		InAppMessageCategoryEntity activityCat = major(2L, "ACTIVITY", "活动消息", 20);
		when(inAppMessageCategoryMapper.selectByParentId(ROOT_PARENT_ID, Boolean.TRUE))
			.thenReturn(List.of(serviceCat, activityCat));

		List<InAppMessageCategoryVO> result = service.listMajors(Boolean.TRUE);

		assertThat(result).extracting(InAppMessageCategoryVO::getCode).containsExactly("SERVICE", "ACTIVITY");
		assertThat(result.get(0).getId()).isEqualTo(1L);
		assertThat(result.get(0).getName()).isEqualTo("服务消息");
		assertThat(result.get(0).getChildren()).isNull();
		verify(inAppMessageCategoryMapper).selectByParentId(ROOT_PARENT_ID, Boolean.TRUE);
	}

	@Test
	@DisplayName("listMajors：status 为 null 时不限启停")
	void listMajors_shouldPassNullStatusWhenUnfiltered() {
		when(inAppMessageCategoryMapper.selectByParentId(ROOT_PARENT_ID, null)).thenReturn(Collections.emptyList());

		service.listMajors(null);

		verify(inAppMessageCategoryMapper).selectByParentId(ROOT_PARENT_ID, null);
	}

	@Test
	@DisplayName("listChildren：按 parentId 返回小类")
	void listChildren_shouldReturnChildrenByParentId() {
		InAppMessageCategoryEntity parent = major(1L, "SERVICE", "服务消息", 10);
		InAppMessageCategoryEntity child = child();
		when(inAppMessageCategoryMapper.selectById(1L)).thenReturn(parent);
		when(inAppMessageCategoryMapper.selectByParentId(1L, Boolean.TRUE)).thenReturn(List.of(child));

		List<InAppMessageCategoryVO> result = service.listChildren(1L, Boolean.TRUE);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getId()).isEqualTo(11L);
		assertThat(result.get(0).getCode()).isEqualTo("SERVICE_INSPECTION");
		assertThat(result.get(0).getName()).isEqualTo("巡检报告");
		verify(inAppMessageCategoryMapper).selectByParentId(1L, Boolean.TRUE);
	}

	@Test
	@DisplayName("listChildren：父级不存在时抛分类不存在")
	void listChildren_shouldThrowWhenParentMissing() {
		when(inAppMessageCategoryMapper.selectById(99L)).thenReturn(null);

		assertThatThrownBy(() -> service.listChildren(99L, null)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_MESSAGE_CATEGORY_NOT_FOUND);
		verify(inAppMessageCategoryMapper, never()).selectByParentId(any(), any());
	}

	@Test
	@DisplayName("listChildren：父级非大类时抛分类不存在")
	void listChildren_shouldThrowWhenParentIsNotMajor() {
		when(inAppMessageCategoryMapper.selectById(11L)).thenReturn(child());

		assertThatThrownBy(() -> service.listChildren(11L, Boolean.TRUE)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_MESSAGE_CATEGORY_NOT_FOUND);
		verify(inAppMessageCategoryMapper, never()).selectByParentId(any(), any());
	}

	@Test
	@DisplayName("批量删除：空列表直接返回")
	void batchDelete_shouldSkipWhenIdsEmpty() {
		// 空列表不查库、不删
		service.batchDelete(Collections.emptyList());

		verify(inAppMessageCategoryMapper, never()).selectCount(any());
		verify(inAppMessageCategoryMapper, never()).deleteByIds(any());
	}

	@Test
	@DisplayName("批量删除：待删节点下仍有子节点时抛 TREE_HAS_ACTIVE_CHILDREN")
	void batchDelete_shouldRejectWhenHasChildren() {
		// 有子节点的父级不可删，须先删子节点
		when(inAppMessageCategoryMapper.selectCount(any())).thenReturn(2L);
		List<Long> ids = List.of(1L);

		assertThatThrownBy(() -> service.batchDelete(ids)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(TREE_HAS_ACTIVE_CHILDREN);
		verify(inAppMessageCategoryMapper, never()).deleteByIds(any());
	}

	@Test
	@DisplayName("批量删除：无子节点时按 ids 物理删除")
	void batchDelete_shouldRemoveByIdsWhenNoChildren() {
		// 叶子节点（或已无子节点的大类）直接删除
		when(inAppMessageCategoryMapper.selectCount(any())).thenReturn(0L);
		when(inAppMessageCategoryMapper.deleteByIds(List.of(11L))).thenReturn(1);

		service.batchDelete(List.of(11L));

		verify(inAppMessageCategoryMapper).deleteByIds(List.of(11L));
	}

}

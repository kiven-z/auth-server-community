package com.auth.service.system.admin.support.menu;

import com.auth.service.system.admin.mapper.admin.menu.SysMenuMapper;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link MenuReferenceChecker} 单元测试。
 */
@DisplayName("MenuReferenceChecker 菜单引用校验")
@ExtendWith(MockitoExtension.class)
class MenuReferenceCheckerTest {

	@Mock
	private SysMenuMapper sysMenuMapper;

	private MenuReferenceChecker menuReferenceChecker;

	private static SysMenuEntity menuEntity(Long id, Long parentId) {
		SysMenuEntity entity = new SysMenuEntity();
		entity.setId(id);
		entity.setParentId(parentId);
		return entity;
	}

	@BeforeEach
	void setUp() {
		menuReferenceChecker = new MenuReferenceChecker(sysMenuMapper);
	}

	@Test
	@DisplayName("normalizeAndRequireParent：顶级不查库")
	void normalizeAndRequireParentSkipsLookupForRoot() {
		assertThat(menuReferenceChecker.normalizeAndRequireParent(null)).isZero();
		assertThat(menuReferenceChecker.normalizeAndRequireParent(0L)).isZero();

		verifyNoInteractions(sysMenuMapper);
	}

	@Test
	@DisplayName("normalizeAndRequireParent：父菜单存在时返回规范化 ID")
	void normalizeAndRequireParentReturnsWhenParentExists() {
		when(sysMenuMapper.selectById(8L)).thenReturn(menuEntity(8L, 0L));

		assertThat(menuReferenceChecker.normalizeAndRequireParent(8L)).isEqualTo(8L);
	}

	@Test
	@DisplayName("normalizeAndRequireParent：父菜单不存在时抛出 MENU_PARENT_NOT_FOUND")
	void normalizeAndRequireParentThrowsWhenParentMissing() {
		when(sysMenuMapper.selectById(9L)).thenReturn(null);

		assertThatThrownBy(() -> menuReferenceChecker.normalizeAndRequireParent(9L))
			.isInstanceOf(SystemBusinessException.class)
			.satisfies(ex -> {
				SystemBusinessException biz = (SystemBusinessException) ex;
				assertThat(biz.getResultCode()).isEqualTo(SystemCommonResultCode.TREE_PARENT_UNAVAILABLE);
				assertThat(biz.getMessageArgs()).isEmpty();
			});
	}

	@Test
	@DisplayName("requireMoveTargetValid：不能移动到自身")
	void requireMoveTargetValidRejectsSelf() {
		assertThatThrownBy(() -> menuReferenceChecker.requireMoveTargetValid(5L, 5L))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Cannot move tree node under itself, nodeId=5");
	}

	@Test
	@DisplayName("requireMoveTargetValid：不能移动到后代节点")
	void requireMoveTargetValidRejectsDescendant() {
		when(sysMenuMapper.selectById(20L)).thenReturn(menuEntity(20L, 10L));

		assertThatThrownBy(() -> menuReferenceChecker.requireMoveTargetValid(10L, 20L))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Cannot move tree node under its descendant, nodeId=10, newParentId=20");
	}

}

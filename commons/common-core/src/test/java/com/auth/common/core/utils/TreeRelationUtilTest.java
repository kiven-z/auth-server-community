package com.auth.common.core.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * {@link TreeRelationUtil} 单元测试。
 */
@DisplayName("TreeRelationUtil 树关系判定与移动校验")
class TreeRelationUtilTest {

	@Test
	@DisplayName("isDescendantOf：沿父链命中祖先时返回 true")
	void isDescendantOfReturnsTrueWhenAncestorOnChain() {
		// 20 -> 15 -> 10
		boolean descendant = TreeRelationUtil.isDescendantOf(10L, 20L,
				id -> id.equals(20L) ? Long.valueOf(15L) : id.equals(15L) ? 10L : null);

		assertThat(descendant).isTrue();
	}

	@Test
	@DisplayName("isDescendantOf：父链未命中祖先时返回 false")
	void isDescendantOfReturnsFalseWhenAncestorNotOnChain() {
		boolean descendant = TreeRelationUtil.isDescendantOf(10L, 20L, id -> id.equals(20L) ? 99L : null);

		assertThat(descendant).isFalse();
	}

	@Test
	@DisplayName("requireValidMoveTarget：不能移动到自身")
	void requireValidMoveTargetRejectsSelf() {
		assertThatThrownBy(() -> TreeRelationUtil.requireValidMoveTarget(5L, 5L, (a, n) -> false))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Cannot move tree node under itself, nodeId=5");
	}

	@Test
	@DisplayName("requireValidMoveTarget：不能移动到后代")
	void requireValidMoveTargetRejectsDescendant() {
		assertThatThrownBy(
				() -> TreeRelationUtil.requireValidMoveTarget(10L, 20L, (a, n) -> a.equals(10L) && n.equals(20L)))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Cannot move tree node under its descendant, nodeId=10, newParentId=20");
	}

	@Test
	@DisplayName("requireValidMoveTarget：合法目标不抛异常")
	void requireValidMoveTargetAcceptsValidTarget() {
		assertThatCode(() -> TreeRelationUtil.requireValidMoveTarget(10L, 0L, (a, n) -> false))
			.doesNotThrowAnyException();
	}

}

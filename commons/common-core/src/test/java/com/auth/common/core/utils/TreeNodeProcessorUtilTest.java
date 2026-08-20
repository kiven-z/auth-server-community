package com.auth.common.core.utils;

import com.auth.common.core.model.entity.TreeNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 树形结构处理器测试
 */
class TreeNodeProcessorUtilTest {

	@Test
	void testProcessWithEmptyList() {
		// 测试空列表
		List<TreeNodeTest> emptyList = new ArrayList<>();
		List<TreeNodeTest> result = TreeNodeProcessorUtil.process(emptyList);

		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	@Test
	void testProcessWithSingleRoot() {
		// 测试只有一个根节点
		List<TreeNodeTest> list = new ArrayList<>();
		list.add(new TreeNodeTest(1L, 0L, "根节点1", "root1"));

		List<TreeNodeTest> result = TreeNodeProcessorUtil.process(list);

		assertEquals(1, result.size());
		assertNull(result.get(0).getChildren());
		assertFalse(result.get(0).isHasLeaf());
	}

	@Test
	void testProcessWithSimpleTree() {
		// 测试简单树结构
		List<TreeNodeTest> list = createSimpleTreeData();
		List<TreeNodeTest> result = TreeNodeProcessorUtil.process(list);

		assertEquals(1, result.size());
		TreeNodeTest root = result.get(0);

		// 验证根节点
		assertEquals("根节点", root.getName());
		assertEquals(1L, root.getId());
		assertTrue(root.isHasLeaf());
		assertNotNull(root.getChildren());
		assertEquals(2, root.getChildren().size());

		// 验证子节点
		List<TreeNodeTest> children = root.getChildren();
		TreeNodeTest child1 = children.get(0);
		TreeNodeTest child2 = children.get(1);

		assertEquals("子节点1", child1.getName());
		assertEquals("子节点2", child2.getName());
		assertTrue(child1.isHasLeaf());
		assertFalse(child2.isHasLeaf());

		// 验证孙子节点
		List<TreeNodeTest> grandchildren = child1.getChildren();
		assertEquals(1, grandchildren.size());
		assertEquals("孙子节点1", grandchildren.get(0).getName());
	}

	@Test
	void testProcessWithMultipleRoots() {
		// 测试多个根节点
		List<TreeNodeTest> list = new ArrayList<>();
		list.add(new TreeNodeTest(1L, 0L, "根节点1", "root1"));
		list.add(new TreeNodeTest(2L, 0L, "根节点2", "root2"));
		list.add(new TreeNodeTest(3L, 1L, "子节点1", "child1"));
		list.add(new TreeNodeTest(4L, 2L, "子节点2", "child2"));

		List<TreeNodeTest> result = TreeNodeProcessorUtil.process(list);

		assertEquals(2, result.size());

		// 验证第一个根节点
		TreeNodeTest root1 = result.get(0);
		assertEquals("根节点1", root1.getName());
		assertTrue(root1.isHasLeaf());
		assertEquals(1, root1.getChildren().size());

		// 验证第二个根节点
		TreeNodeTest root2 = result.get(1);
		assertEquals("根节点2", root2.getName());
		assertTrue(root2.isHasLeaf());
		assertEquals(1, root2.getChildren().size());
	}

	@Test
	void testProcessWithParentIdNull() {
		// 测试parentId为null的情况
		List<TreeNodeTest> list = new ArrayList<>();
		list.add(new TreeNodeTest(1L, null, "节点1", "node1"));
		list.add(new TreeNodeTest(2L, null, "节点2", "node2"));
		list.add(new TreeNodeTest(3L, 1L, "子节点", "child1"));

		List<TreeNodeTest> result = TreeNodeProcessorUtil.process(list);

		assertEquals(2, result.size()); // parentId为null的应该被视为根节点
		assertTrue(result.stream().anyMatch(node -> node.getId() == 1L));
		assertTrue(result.stream().anyMatch(node -> node.getId() == 2L));

		TreeNodeTest node1 = result.stream().filter(node -> node.getId() == 1L).findFirst().orElse(null);

		assertNotNull(node1);
		assertTrue(node1.isHasLeaf());
		assertEquals(1, node1.getChildren().size());
	}

	@Test
	void testHasLeafProperty() {
		// 测试hasLeaf属性是否正确设置
		List<TreeNodeTest> list = new ArrayList<>();
		list.add(new TreeNodeTest(1L, 0L, "根节点", "root"));
		list.add(new TreeNodeTest(2L, 1L, "子节点", "child"));

		List<TreeNodeTest> result = TreeNodeProcessorUtil.process(list);

		TreeNodeTest root = result.get(0);
		assertTrue(root.isHasLeaf()); // 有子节点，应该为true

		TreeNodeTest child = root.getChildren().get(0);
		assertFalse(child.isHasLeaf()); // 没有子节点，应该为false
	}

	@Test
	void testFindRootsMethod() {
		// 单独测试findRoots方法
		List<TreeNodeTest> list = new ArrayList<>();
		list.add(new TreeNodeTest(1L, 0L, "根节点1", "root1"));
		list.add(new TreeNodeTest(2L, null, "根节点2", "root2"));
		list.add(new TreeNodeTest(3L, 1L, "子节点", "child"));

		List<TreeNodeTest> roots = TreeNodeProcessorUtil.findRoots(list);

		assertEquals(2, roots.size());
		assertTrue(roots.stream().anyMatch(node -> node.getId() == 1L));
		assertTrue(roots.stream().anyMatch(node -> node.getId() == 2L));
	}

	@Test
	void testSerialization() {
		// 测试序列化相关功能
		TreeNodeTest node = new TreeNodeTest(1L, 0L, "测试节点", "test");
		node.setHasLeaf(true);

		// 验证@JsonSerialize注解是否生效
		assertNotNull(node.getId());
		assertNotNull(node.getParentId());
	}

	@Test
	void testComplexTreeStructure() {
		// 测试复杂树结构
		List<TreeNodeTest> list = createComplexTreeData();
		List<TreeNodeTest> result = TreeNodeProcessorUtil.process(list);

		// 验证总的根节点数量
		assertEquals(2, result.size());

		// 遍历整个树，验证所有节点都正确构建
		int totalNodes = countNodes(result);
		assertEquals(11, totalNodes); // 总共应该有11个节点

		// 验证深度
		TreeNodeTest root1 = result.get(0);
		int maxDepth = getMaxDepth(root1);
		assertEquals(4, maxDepth); // 最大深度应该是4
	}

	/**
	 * 创建简单树结构测试数据
	 */
	private List<TreeNodeTest> createSimpleTreeData() {
		List<TreeNodeTest> list = new ArrayList<>();
		list.add(new TreeNodeTest(1L, 0L, "根节点", "root"));
		list.add(new TreeNodeTest(2L, 1L, "子节点1", "child1"));
		list.add(new TreeNodeTest(3L, 1L, "子节点2", "child2"));
		list.add(new TreeNodeTest(4L, 2L, "孙子节点1", "grandchild1"));
		return list;
	}

	/**
	 * 创建复杂树结构测试数据
	 */
	private List<TreeNodeTest> createComplexTreeData() {
		List<TreeNodeTest> list = new ArrayList<>();
		// 第一个根节点树
		list.add(new TreeNodeTest(1L, 0L, "根节点1", "root1"));
		list.add(new TreeNodeTest(2L, 1L, "子节点1-1", "child1-1"));
		list.add(new TreeNodeTest(3L, 1L, "子节点1-2", "child1-2"));
		list.add(new TreeNodeTest(4L, 2L, "孙子节点1-1-1", "grandchild1-1-1"));
		list.add(new TreeNodeTest(5L, 4L, "曾孙节点1-1-1-1", "ggchild1-1-1-1"));

		// 第二个根节点树
		list.add(new TreeNodeTest(6L, 0L, "根节点2", "root2"));
		list.add(new TreeNodeTest(7L, 6L, "子节点2-1", "child2-1"));
		list.add(new TreeNodeTest(8L, 6L, "子节点2-2", "child2-2"));
		list.add(new TreeNodeTest(9L, 7L, "孙子节点2-1-1", "grandchild2-1-1"));
		list.add(new TreeNodeTest(10L, 7L, "孙子节点2-1-2", "grandchild2-1-2"));
		list.add(new TreeNodeTest(11L, 9L, "曾孙节点2-1-1-1", "ggchild2-1-1-1"));

		return list;
	}

	/**
	 * 计算树中的总节点数
	 */
	private int countNodes(List<TreeNodeTest> nodes) {
		int count = 0;
		for (TreeNodeTest node : nodes) {
			count += 1;
			if (node.getChildren() != null && !node.getChildren().isEmpty()) {
				count += countNodes(node.getChildren());
			}
		}
		return count;
	}

	/**
	 * 获取树的最大深度
	 */
	private int getMaxDepth(TreeNodeTest node) {
		if (node.getChildren() == null || node.getChildren().isEmpty()) {
			return 1;
		}

		int maxDepth = 0;
		for (TreeNodeTest child : node.getChildren()) {
			int depth = getMaxDepth(child);
			maxDepth = Math.max(maxDepth, depth);
		}

		return maxDepth + 1;
	}

	@Data
	@EqualsAndHashCode(callSuper = true)
	static class TreeNodeTest extends TreeNode<TreeNodeTest> {

		private String name;

		private String code;

		public TreeNodeTest(Long id, Long parentId, String name, String code) {
			this.setId(id);
			this.setParentId(parentId);
			this.name = name;
			this.code = code;
		}

		@Override
		public String toString() {
			return "TreeNodeTestEntity{" + "id=" + getId() + ", parentId=" + getParentId() + ", name='" + name + '\''
					+ ", code='" + code + '\'' + ", hasLeaf=" + isHasLeaf() + ", children="
					+ (getChildren() != null ? getChildren().size() : 0) + '}';
		}

	}

}

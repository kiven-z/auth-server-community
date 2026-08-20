package com.auth.common.data.model;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 分页返回结果
 *
 * @author Bunny
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 当前页
	 */
	private Long pageNo;

	/**
	 * 每页记录数
	 */
	private Long pageSize;

	/**
	 * 总分页数
	 */
	private Long pages;

	/**
	 * 总记录数
	 */
	private Long total;

	/**
	 * 当前页数据集合
	 */
	private transient List<T> list;

	/**
	 * 根据分页数据和记录列表构建 PageResult 对象
	 * @param current 当前页码
	 * @param size 每页记录数
	 * @param total 总记录数
	 * @param records 记录列表
	 * @return 返回填充了分页信息和数据的 PageResult 对象
	 */
	public static <T> PageResponse<T> of(Long current, Long size, Long total, List<T> records) {
		// 参数校验
		Objects.requireNonNull(records, "records cannot be null");
		Objects.requireNonNull(size, "size cannot be null");
		Objects.requireNonNull(total, "total cannot be null");

		if (size <= 0) {
			throw new IllegalArgumentException("size must be greater than 0");
		}
		if (total < 0) {
			throw new IllegalArgumentException("total cannot be negative");
		}

		// 计算总页数，避免除零错误，使用向上取整公式
		Long pages = (total + size - 1) / size;

		return PageResponse.<T>builder().pageNo(current).pageSize(size).pages(pages).total(total).list(records).build();
	}

	/**
	 * 根据 MyBatis-Plus 的 IPage 对象和记录列表构建 PageResponse 对象
	 * @param <T> 泛型类型
	 * @param page MyBatis-Plus 的 IPage 对象
	 * @param records 记录列表
	 * @return 返回填充了分页信息和数据的 PageResponse 对象
	 */
	public static <T> PageResponse<T> of(IPage<T> page, List<T> records) {
		return of(page.getCurrent(), page.getSize(), page.getTotal(), records);
	}

	/**
	 * 根据 MyBatis-Plus 的 IPage 对象构建 PageResponse 对象
	 * @param <T> 泛型类型
	 * @param page MyBatis-Plus 的 IPage 对象
	 * @return 返回填充了分页信息和数据的 PageResponse 对象
	 */
	public static <T> PageResponse<T> of(IPage<T> page) {
		return of(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
	}

}
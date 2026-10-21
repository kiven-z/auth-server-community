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

	private Long pageNo;

	private Long pageSize;

	private Long pages;

	private Long total;

	private transient List<T> list;

	/**
	 * 由当前页、页大小、总记录数和本页数据组装
	 * @param current 当前页码
	 * @param size 每页记录数
	 * @param total 总记录数
	 * @param records 本页数据
	 * @return 分页结果
	 */
	public static <T> PageResponse<T> of(Long current, Long size, Long total, List<T> records) {
		Objects.requireNonNull(records, "records cannot be null");
		Objects.requireNonNull(size, "size cannot be null");
		Objects.requireNonNull(total, "total cannot be null");

		if (size <= 0) {
			throw new IllegalArgumentException("size must be greater than 0");
		}
		if (total < 0) {
			throw new IllegalArgumentException("total cannot be negative");
		}

		Long pages = (total + size - 1) / size;

		return PageResponse.<T>builder().pageNo(current).pageSize(size).pages(pages).total(total).list(records).build();
	}

	/**
	 * 由 MyBatis-Plus 分页对象和本页数据组装
	 * @param page 分页对象
	 * @param records 本页数据
	 * @return 分页结果
	 */
	public static <T> PageResponse<T> of(IPage<T> page, List<T> records) {
		return of(page.getCurrent(), page.getSize(), page.getTotal(), records);
	}

	/**
	 * 由 MyBatis-Plus 分页对象组装
	 * @param page 分页对象
	 * @return 分页结果
	 */
	public static <T> PageResponse<T> of(IPage<T> page) {
		return of(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
	}

}

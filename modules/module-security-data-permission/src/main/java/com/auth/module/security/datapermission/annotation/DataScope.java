package com.auth.module.security.datapermission.annotation;

import com.auth.module.security.contract.api.datascope.DataScopeStorageType;
import com.auth.module.security.datapermission.handler.DataScopeHandler;
import com.auth.module.security.datapermission.handler.DefaultDataScopeHandler;

import java.lang.annotation.*;

/**
 * 申明一个 mapper 方法需要数据范围过滤
 *
 * @author Bunny
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

	/**
	 * 用户所有者列，用于 SELF 范围过滤，默认 created_by
	 */
	String userColumn() default "created_by";

	/**
	 * 维度列名，例如 dept_id、post_id
	 */
	String dimensionColumn() default "dept_id";

	/**
	 * 表别名
	 */
	String alias();

	/**
	 * 数据范围类型
	 * <p>
	 * 默认 FROM_PROFILE 表示按登录画像裁决
	 * </p>
	 */
	DataScopeStorageType scope() default DataScopeStorageType.FROM_PROFILE;

	/**
	 * SQL 处理器
	 */
	Class<? extends DataScopeHandler> handler() default DefaultDataScopeHandler.class;

}

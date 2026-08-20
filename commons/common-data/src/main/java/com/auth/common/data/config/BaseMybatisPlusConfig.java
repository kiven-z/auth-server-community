package com.auth.common.data.config;

import com.auth.common.core.constants.BatchSizes;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;

/**
 * MyBatisPlus 基础配置：唯一 MybatisPlusInterceptor，扩展 InnerInterceptor 由此收敛
 *
 * @author Bunny
 */
public interface BaseMybatisPlusConfig extends MetaObjectHandler {

	/**
	 * 配置并注入 MyBatis Plus 拦截器（全应用仅此一处注册外层插件）
	 * @param extraInnerInterceptors 各模块贡献的 InnerInterceptor（须排在分页之前）
	 * @return 拦截器
	 */
	@Bean
	default MybatisPlusInterceptor mybatisPlusInterceptor(ObjectProvider<InnerInterceptor> extraInnerInterceptors) {
		MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

		// 扩展先挂（数据权限等），保证 count/page SQL 都带范围条件
		extraInnerInterceptors.orderedStream().forEach(interceptor::addInnerInterceptor);

		PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
		paginationInnerInterceptor.setDbType(DbType.MYSQL);
		paginationInnerInterceptor.setOptimizeJoin(false);
		paginationInnerInterceptor.setMaxLimit((long) BatchSizes.SIZE_1000);
		paginationInnerInterceptor.setOverflow(true);
		interceptor.addInnerInterceptor(paginationInnerInterceptor);

		interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
		interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
		return interceptor;
	}

}

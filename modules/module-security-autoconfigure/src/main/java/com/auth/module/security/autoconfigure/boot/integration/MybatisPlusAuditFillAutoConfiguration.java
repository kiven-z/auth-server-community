package com.auth.module.security.autoconfigure.boot.integration;

import com.auth.common.data.config.BaseMybatisPlusConfig;
import com.auth.common.data.model.enums.DatabaseAuditMetaFieldEnum;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.Instant;

/**
 * MyBatisPlus 配置自动填充字段
 *
 * @author Bunny
 */
@ConditionalOnMissingBean(MybatisPlusAuditFillAutoConfiguration.class)
@Configuration
@EnableTransactionManagement
public class MybatisPlusAuditFillAutoConfiguration implements BaseMybatisPlusConfig {

	private static final String CREATED_AT = DatabaseAuditMetaFieldEnum.CREATED_AT.getColumnName();

	private static final String UPDATED_AT = DatabaseAuditMetaFieldEnum.UPDATED_AT.getColumnName();

	private static final String CREATED_BY = DatabaseAuditMetaFieldEnum.CREATED_BY.getColumnName();

	private static final String UPDATED_BY = DatabaseAuditMetaFieldEnum.UPDATED_BY.getColumnName();

	/**
	 * 使用mp做添加操作时候，这个方法执行
	 */
	@Override
	public void insertFill(MetaObject metaObject) {
		this.strictInsertFill(metaObject, "version", Long.class, 0L);
		Instant now = Instant.now();
		this.setFieldValByName(CREATED_AT, now, metaObject);
		this.setFieldValByName(UPDATED_AT, now, metaObject);

		Long userId = SecurityUserUtils.getUserId();
		if (userId != null) {
			this.strictInsertFill(metaObject, CREATED_BY, Long.class, userId);
			this.strictInsertFill(metaObject, UPDATED_BY, Long.class, userId);
		}
	}

	/**
	 * 使用mp做修改操作时候，这个方法执行
	 */
	@Override
	public void updateFill(MetaObject metaObject) {
		this.setFieldValByName(UPDATED_AT, Instant.now(), metaObject);

		Long userId = SecurityUserUtils.getUserId();
		if (userId != null) {
			this.setFieldValByName(UPDATED_BY, userId, metaObject);
		}
	}

}

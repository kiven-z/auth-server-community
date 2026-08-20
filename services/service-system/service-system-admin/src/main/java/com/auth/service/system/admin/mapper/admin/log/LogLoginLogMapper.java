package com.auth.service.system.admin.mapper.admin.log;

import com.auth.module.platform.persistence.model.LoginLogEntity;
import com.auth.service.system.admin.model.po.loglogin.LogLoginLogPageRowPO;
import com.auth.service.system.admin.model.query.log.LogLoginLogQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 登录日志 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface LogLoginLogMapper extends BaseMapper<LoginLogEntity> {

	/**
	 * 分页查询登录日志
	 * @param page 分页参数
	 * @param query 查询条件
	 * @return 分页结果
	 */
	IPage<LogLoginLogPageRowPO> selectListByPage(@Param("page") Page<LoginLogEntity> page,
			@Param("query") LogLoginLogQuery query);

}

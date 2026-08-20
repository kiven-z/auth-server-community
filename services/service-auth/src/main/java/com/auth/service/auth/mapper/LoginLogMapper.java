package com.auth.service.auth.mapper;

import com.auth.module.platform.persistence.model.LoginLogEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录日志 Mapper（认证专用）
 *
 * @author Bunny
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLogEntity> {

}

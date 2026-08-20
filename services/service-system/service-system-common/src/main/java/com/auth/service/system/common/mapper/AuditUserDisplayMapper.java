package com.auth.service.system.common.mapper;

import com.auth.module.platform.persistence.model.UserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审计展示名所需的用户只读访问
 *
 * @author Bunny
 */
@Mapper
public interface AuditUserDisplayMapper extends BaseMapper<UserEntity> {

}

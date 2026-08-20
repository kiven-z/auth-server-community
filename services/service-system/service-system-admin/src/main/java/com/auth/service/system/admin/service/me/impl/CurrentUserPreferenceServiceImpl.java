package com.auth.service.system.admin.service.me.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.utils.JsonSupport;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.admin.mapper.admin.user.SysUserConfigMapper;
import com.auth.service.system.admin.model.constants.UserPreferenceKeys;
import com.auth.service.system.admin.model.entity.SysUserConfigEntity;
import com.auth.service.system.admin.model.form.me.MeUserPreferenceUpsertForm;
import com.auth.service.system.admin.model.vo.me.MeUserPreferenceItemVO;
import com.auth.service.system.admin.model.vo.me.MeUserPreferenceListVO;
import com.auth.service.system.admin.service.me.CurrentUserPreferenceService;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_INVALID;

/**
 * 当前登录用户 UI 偏好配置服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CurrentUserPreferenceServiceImpl extends ServiceImpl<SysUserConfigMapper, SysUserConfigEntity>
		implements CurrentUserPreferenceService {

	private final ObjectMapper objectMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MeUserPreferenceListVO listMyPreferences() {
		Long userId = SecurityUserUtils.getUserId();

		List<SysUserConfigEntity> entities = baseMapper.selectListByUserIdAndConfigKeys(userId,
				UserPreferenceKeys.ALLOWED_KEYS);

		MeUserPreferenceListVO response = new MeUserPreferenceListVO();
		response.setItems(entities.stream().map(entity -> {
			JsonNode configValue;
			try {
				configValue = JsonSupport.readObjectTree(objectMapper, entity.getConfigValue());
			}
			catch (IllegalArgumentException ex) {
				throw new SystemBusinessException(DATA_INVALID, entity.getConfigValue());
			}

			MeUserPreferenceItemVO item = new MeUserPreferenceItemVO();
			item.setConfigKey(entity.getConfigKey());
			item.setConfigValue(configValue);
			return item;
		}).toList());

		return response;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void upsertMyPreference(MeUserPreferenceUpsertForm form) {
		Long userId = SecurityUserUtils.getUserId();
		String configKey = form.getConfigKey();
		JsonNode configValue = form.getConfigValue();

		// 1. 校验配置键在白名单内，且配置值为非空 JSON 对象
		UserPreferenceKeys.assertUpsertForm(configKey, configValue);

		// 2. 查询是否存在配置记录
		List<SysUserConfigEntity> configs = baseMapper.selectListByUserIdAndConfigKeys(userId, Set.of(configKey));
		SysUserConfigEntity existing = CollUtil.getFirst(configs);

		// 3. 存在更新不存在插入
		String configValueJson = JsonSupport.toJson(objectMapper, configValue);
		if (existing == null) {
			SysUserConfigEntity entity = new SysUserConfigEntity();
			entity.setUserId(userId);
			entity.setConfigKey(configKey);
			entity.setConfigValue(configValueJson);
			baseMapper.insert(entity);
			return;
		}

		existing.setConfigValue(configValueJson);
		baseMapper.updateById(existing);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void clearMyPreferences() {
		Long userId = SecurityUserUtils.getUserId();
		baseMapper.deleteByUserId(userId);
	}

}

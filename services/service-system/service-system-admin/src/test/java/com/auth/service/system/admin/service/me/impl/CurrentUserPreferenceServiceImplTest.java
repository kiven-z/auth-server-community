package com.auth.service.system.admin.service.me.impl;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.system.admin.mapper.admin.user.SysUserConfigMapper;
import com.auth.service.system.admin.model.constants.UserPreferenceKeys;
import com.auth.service.system.admin.model.entity.SysUserConfigEntity;
import com.auth.service.system.admin.model.form.me.MeUserPreferenceUpsertForm;
import com.auth.service.system.admin.model.vo.me.MeUserPreferenceItemVO;
import com.auth.service.system.admin.model.vo.me.MeUserPreferenceListVO;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link CurrentUserPreferenceServiceImpl} 单元测试
 */
@DisplayName("CurrentUserPreferenceServiceImpl 个人中心偏好")
@ExtendWith(MockitoExtension.class)
class CurrentUserPreferenceServiceImplTest {

	private static final Long USER_ID = 100L;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Mock
	private SysUserConfigMapper sysUserConfigMapper;

	private CurrentUserPreferenceServiceImpl currentUserPreferenceService;

	@BeforeEach
	void setUp() throws Exception {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
				SysUserConfigEntity.class);
		AuthProfile profile = AuthProfile.builder().userId(USER_ID).username("tester").build();
		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(profile, null));
		currentUserPreferenceService = new CurrentUserPreferenceServiceImpl(objectMapper);
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(currentUserPreferenceService, sysUserConfigMapper);
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("无配置记录时返回空 items")
	void listMyPreferences_returnsEmptyItemsWhenNoRows() {
		when(sysUserConfigMapper.selectListByUserIdAndConfigKeys(USER_ID, UserPreferenceKeys.ALLOWED_KEYS))
			.thenReturn(List.of());

		MeUserPreferenceListVO result = currentUserPreferenceService.listMyPreferences();

		assertThat(result.getItems()).isEmpty();
	}

	@Test
	@DisplayName("查询时调用 Mapper 拉取当前用户配置")
	void listMyPreferences_delegatesToMapper() {
		when(sysUserConfigMapper.selectListByUserIdAndConfigKeys(USER_ID, UserPreferenceKeys.ALLOWED_KEYS))
			.thenReturn(List.of());

		currentUserPreferenceService.listMyPreferences();

		verify(sysUserConfigMapper).selectListByUserIdAndConfigKeys(USER_ID, UserPreferenceKeys.ALLOWED_KEYS);
	}

	@Test
	@DisplayName("映射白名单配置项字段与 JSON 值")
	void listMyPreferences_mapsWhitelistedItems() {
		SysUserConfigEntity entity = new SysUserConfigEntity();
		entity.setUserId(USER_ID);
		entity.setConfigKey(UserPreferenceKeys.UI_LAYOUT);
		entity.setConfigValue("{\"layout\":\"mix\",\"sidebarStatus\":false}");
		when(sysUserConfigMapper.selectListByUserIdAndConfigKeys(USER_ID, UserPreferenceKeys.ALLOWED_KEYS))
			.thenReturn(List.of(entity));

		MeUserPreferenceListVO result = currentUserPreferenceService.listMyPreferences();

		assertThat(result.getItems()).hasSize(1);
		MeUserPreferenceItemVO item = result.getItems().get(0);
		assertThat(item.getConfigKey()).isEqualTo(UserPreferenceKeys.UI_LAYOUT);
		assertThat(item.getConfigValue().get("layout").asText()).isEqualTo("mix");
		assertThat(item.getConfigValue().get("sidebarStatus").asBoolean()).isFalse();
	}

	@Test
	@DisplayName("配置值 JSON 非法时抛出业务异常")
	void listMyPreferences_invalidJsonThrows() {
		SysUserConfigEntity entity = new SysUserConfigEntity();
		entity.setConfigKey(UserPreferenceKeys.UI_LAYOUT);
		entity.setConfigValue("{invalid-json");
		when(sysUserConfigMapper.selectListByUserIdAndConfigKeys(USER_ID, UserPreferenceKeys.ALLOWED_KEYS))
			.thenReturn(List.of(entity));

		assertThatThrownBy(() -> currentUserPreferenceService.listMyPreferences())
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_INVALID);
	}

	@Test
	@DisplayName("无记录时插入新偏好配置")
	void upsertMyPreference_insertsWhenNotExists() throws Exception {
		MeUserPreferenceUpsertForm form = preferenceForm(UserPreferenceKeys.UI_LAYOUT, "{\"locale\":\"zh\"}");

		when(sysUserConfigMapper.selectListByUserIdAndConfigKeys(USER_ID, Set.of(UserPreferenceKeys.UI_LAYOUT)))
			.thenReturn(List.of());
		when(sysUserConfigMapper.insert(any(SysUserConfigEntity.class))).thenReturn(1);

		currentUserPreferenceService.upsertMyPreference(form);

		verify(sysUserConfigMapper).insert(any(SysUserConfigEntity.class));
		verify(sysUserConfigMapper, never()).updateById(any(SysUserConfigEntity.class));
	}

	@Test
	@DisplayName("按域拆分的 locale / theme / display 键均在白名单内可 upsert")
	void upsertMyPreference_acceptsDomainSplitKeys() throws Exception {
		when(sysUserConfigMapper.insert(any(SysUserConfigEntity.class))).thenReturn(1);

		currentUserPreferenceService
			.upsertMyPreference(preferenceForm(UserPreferenceKeys.UI_LOCALE, "{\"locale\":\"zh\"}"));
		currentUserPreferenceService.upsertMyPreference(preferenceForm(UserPreferenceKeys.UI_THEME,
				"{\"colorScheme\":\"dark\",\"navTheme\":\"default\",\"primaryColor\":\"#1b2a47\"}"));
		currentUserPreferenceService
			.upsertMyPreference(preferenceForm(UserPreferenceKeys.UI_DISPLAY, "{\"grey\":true,\"hideTabs\":true}"));

		verify(sysUserConfigMapper, times(3)).insert(any(SysUserConfigEntity.class));
	}

	@Test
	@DisplayName("已有记录时更新配置值")
	void upsertMyPreference_updatesWhenExists() throws Exception {
		SysUserConfigEntity existing = new SysUserConfigEntity();
		existing.setId(1L);
		existing.setUserId(USER_ID);
		existing.setConfigKey(UserPreferenceKeys.UI_LAYOUT);
		existing.setConfigValue("{\"sidebar\":true}");
		when(sysUserConfigMapper.selectListByUserIdAndConfigKeys(USER_ID, Set.of(UserPreferenceKeys.UI_LAYOUT)))
			.thenReturn(List.of(existing));
		when(sysUserConfigMapper.updateById(any(SysUserConfigEntity.class))).thenReturn(1);

		MeUserPreferenceUpsertForm form = preferenceForm(UserPreferenceKeys.UI_LAYOUT, "{\"sidebar\":false}");

		currentUserPreferenceService.upsertMyPreference(form);

		verify(sysUserConfigMapper).updateById(any(SysUserConfigEntity.class));
		verify(sysUserConfigMapper, never()).insert(any(SysUserConfigEntity.class));
	}

	@Test
	@DisplayName("非白名单配置键 upsert 时抛出业务异常")
	void upsertMyPreference_rejectsUnknownKey() throws Exception {
		MeUserPreferenceUpsertForm form = preferenceForm("ui.unknown", "{\"locale\":\"zh\"}");

		assertThatThrownBy(() -> currentUserPreferenceService.upsertMyPreference(form))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_INVALID);

		verify(sysUserConfigMapper, never()).insert(any(SysUserConfigEntity.class));
		verify(sysUserConfigMapper, never()).updateById(any(SysUserConfigEntity.class));
	}

	@Test
	@DisplayName("空 JSON 对象 upsert 时抛出业务异常")
	void upsertMyPreference_rejectsEmptyObject() throws Exception {
		MeUserPreferenceUpsertForm form = preferenceForm(UserPreferenceKeys.UI_LAYOUT, "{}");

		assertThatThrownBy(() -> currentUserPreferenceService.upsertMyPreference(form))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_INVALID);
	}

	@Test
	@DisplayName("清空偏好时物理删除当前用户全部配置")
	void clearMyPreferences_deletesAllUserConfigs() {
		when(sysUserConfigMapper.deleteByUserId(USER_ID)).thenReturn(2);

		currentUserPreferenceService.clearMyPreferences();

		verify(sysUserConfigMapper).deleteByUserId(USER_ID);
	}

	@Test
	@DisplayName("无偏好记录时清空仍成功")
	void clearMyPreferences_succeedsWhenNoRows() {
		when(sysUserConfigMapper.deleteByUserId(USER_ID)).thenReturn(0);

		currentUserPreferenceService.clearMyPreferences();

		verify(sysUserConfigMapper).deleteByUserId(USER_ID);
	}

	@Test
	@DisplayName("JSON 数组 upsert 时抛出业务异常")
	void upsertMyPreference_rejectsArray() throws Exception {
		MeUserPreferenceUpsertForm form = preferenceForm(UserPreferenceKeys.UI_LAYOUT, "[\"a\"]");

		assertThatThrownBy(() -> currentUserPreferenceService.upsertMyPreference(form))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_INVALID);
	}

	private MeUserPreferenceUpsertForm preferenceForm(String configKey, String json) throws Exception {
		MeUserPreferenceUpsertForm form = new MeUserPreferenceUpsertForm();
		form.setConfigKey(configKey);
		form.setConfigValue(objectMapper.readTree(json));
		return form;
	}

}

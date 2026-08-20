package com.auth.service.system.admin.excel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link EnableStatusLabels} 单元测试
 *
 * @author Bunny
 */
@DisplayName("EnableStatusLabels 启用/禁用文案")
class EnableStatusLabelsTest {

	@Test
	@DisplayName("parseImport：仅识别 canonical 启用/禁用")
	void parseImport_recognizesCanonicalLabelsOnly() {
		assertThat(EnableStatusLabels.parseImport(EnableStatusLabels.ENABLED)).isEqualTo(EnableStatus.ENABLED);
		assertThat(EnableStatusLabels.parseImport(EnableStatusLabels.DISABLED)).isEqualTo(EnableStatus.DISABLED);
	}

	@DisplayName("parseImport：未知或空白返回 UNKNOWN")
	@ParameterizedTest
	@ValueSource(strings = { "  ", "正常", "停用", "1", "0", "任意文案" })
	@NullAndEmptySource
	void parseImport_whenUnknownOrBlank_returnsUnknown(String label) {
		assertThat(EnableStatusLabels.parseImport(label)).isEqualTo(EnableStatus.UNKNOWN);
	}

	@Test
	@DisplayName("parseImport：trim 后匹配")
	void parseImport_trimsWhitespace() {
		assertThat(EnableStatusLabels.parseImport(" 启用 ")).isEqualTo(EnableStatus.ENABLED);
		assertThat(EnableStatusLabels.parseImport(" 禁用 ")).isEqualTo(EnableStatus.DISABLED);
	}

	@Test
	@DisplayName("exportLabel：与 MapStruct 导出语义一致")
	void exportLabel_matchesBooleanStatus() {
		assertThat(EnableStatusLabels.exportLabel(true)).isEqualTo(EnableStatusLabels.ENABLED);
		assertThat(EnableStatusLabels.exportLabel(false)).isEqualTo(EnableStatusLabels.DISABLED);
		assertThat(EnableStatusLabels.exportLabel(null)).isEqualTo(EnableStatusLabels.DISABLED);
	}

}

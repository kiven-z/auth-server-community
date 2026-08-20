package com.auth.common.core.desensitize;

import cn.hutool.core.util.DesensitizedUtil;
import com.auth.common.core.annotation.Desensitized;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link DesensitizedJsonSerializer} 与 {@link Desensitized} 组合行为测试
 */
class DesensitizedJsonSerializerTest {

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	@DisplayName("MOBILE_PHONE：与 Hutool mobilePhone 一致")
	void mobilePhoneMatchesHutool() throws JsonProcessingException {
		MobileDto dto = new MobileDto();
		dto.setPhone("13812345678");
		String json = mapper.writeValueAsString(dto);
		JsonNode node = mapper.readTree(json);
		assertEquals(DesensitizedUtil.mobilePhone("13812345678"), node.get("phone").asText());
	}

	@Test
	@DisplayName("CHINESE_NAME：与 Hutool chineseName 一致")
	void chineseNameMatchesHutool() throws JsonProcessingException {
		NameDto dto = new NameDto();
		dto.setName("张三丰");
		String json = mapper.writeValueAsString(dto);
		assertEquals(DesensitizedUtil.chineseName("张三丰"), mapper.readTree(json).get("name").asText());
	}

	@Test
	@DisplayName("ID_CARD：固定保留 6+4 与 Hutool idCardNum 一致")
	void idCardMatchesHutool() throws JsonProcessingException {
		IdDto dto = new IdDto();
		dto.setIdNo("110101199003076789");
		String json = mapper.writeValueAsString(dto);
		assertEquals(DesensitizedUtil.idCardNum("110101199003076789", 6, 4),
				mapper.readTree(json).get("idNo").asText());
	}

	@Test
	@DisplayName("EMAIL：与 Hutool email 一致")
	void emailMatchesHutool() throws JsonProcessingException {
		MailDto dto = new MailDto();
		dto.setEmail("zhang.san@example.com");
		String json = mapper.writeValueAsString(dto);
		assertEquals(DesensitizedUtil.email("zhang.san@example.com"), mapper.readTree(json).get("email").asText());
	}

	@Test
	@DisplayName("BANK_CARD：与 Hutool bankCard 一致")
	void bankCardMatchesHutool() throws JsonProcessingException {
		CardDto dto = new CardDto();
		dto.setCard("6222021234567890123");
		String json = mapper.writeValueAsString(dto);
		assertEquals(DesensitizedUtil.bankCard("6222021234567890123"), mapper.readTree(json).get("card").asText());
	}

	@Test
	@DisplayName("CUSTOM：按前后缀与 symbol 掩码")
	void customMask() throws JsonProcessingException {
		CustomDto dto = new CustomDto();
		dto.setSecret("abcdefghij");
		String json = mapper.writeValueAsString(dto);
		assertEquals("ab#######j", mapper.readTree(json).get("secret").asText());
	}

	@Test
	@DisplayName("CUSTOM：自定义 symbol 为 #")
	void customSymbolHash() throws JsonProcessingException {
		CustomHashDto dto = new CustomHashDto();
		dto.setCode("1234567890");
		String json = mapper.writeValueAsString(dto);
		assertEquals("12######90", mapper.readTree(json).get("code").asText());
	}

	@Test
	@DisplayName("CUSTOM：长度不超过 prefix+suffix 时保持原文")
	void customTooShortReturnsOriginal() throws JsonProcessingException {
		CustomDto dto = new CustomDto();
		dto.setSecret("ab");
		String json = mapper.writeValueAsString(dto);
		assertEquals("ab", mapper.readTree(json).get("secret").asText());
	}

	@Test
	@DisplayName("null 字段序列化为 JSON null")
	void nullFieldWritesJsonNull() throws JsonProcessingException {
		MobileDto dto = new MobileDto();
		dto.setPhone(null);
		String json = mapper.writeValueAsString(dto);
		assertTrue(mapper.readTree(json).get("phone").isNull());
	}

	@Test
	@DisplayName("空字符串原样写出")
	void emptyStringUnchanged() throws JsonProcessingException {
		MobileDto dto = new MobileDto();
		dto.setPhone("");
		String json = mapper.writeValueAsString(dto);
		assertEquals("", mapper.readTree(json).get("phone").asText());
	}

	@Test
	@DisplayName("IP_ADDRESS：IPv4 与 Hutool ipv4 一致")
	void ipV4MatchesHutool() throws JsonProcessingException {
		IpDto dto = new IpDto();
		dto.setIp("192.168.1.100");
		String json = mapper.writeValueAsString(dto);
		// 与 Hutool 脱敏结果一致
		assertEquals(DesensitizedUtil.ipv4("192.168.1.100"), mapper.readTree(json).get("ip").asText());
	}

	@Test
	@DisplayName("IP_ADDRESS：IPv6 与 Hutool ipv6 一致")
	void ipV6MatchesHutool() throws JsonProcessingException {
		IpDto dto = new IpDto();
		dto.setIp("2001:0db8:85a3::8a2e:0370:7334");
		String json = mapper.writeValueAsString(dto);
		assertEquals(DesensitizedUtil.ipv6("2001:0db8:85a3::8a2e:0370:7334"), mapper.readTree(json).get("ip").asText());
	}

	@Test
	@DisplayName("IP_ADDRESS：非 IP 字符串输出固定占位")
	void ipUnknownUsesPlaceholder() throws JsonProcessingException {
		IpDto dto = new IpDto();
		dto.setIp("not-an-ip");
		String json = mapper.writeValueAsString(dto);
		assertEquals("******", mapper.readTree(json).get("ip").asText());
	}

	@Test
	@DisplayName("CAR_LICENSE：与 Hutool carLicense 一致")
	void carLicenseMatchesHutool() throws JsonProcessingException {
		PlateDto dto = new PlateDto();
		dto.setPlate("苏D40000");
		String json = mapper.writeValueAsString(dto);
		assertEquals(DesensitizedUtil.carLicense("苏D40000"), mapper.readTree(json).get("plate").asText());
	}

	@Test
	@DisplayName("ADDRESS：默认 sensitiveSize=8 与 Hutool ADDRESS 策略一致")
	void addressMatchesHutoolDefault() throws JsonProcessingException {
		AddrDto dto = new AddrDto();
		dto.setAddr("北京市海淀区马连洼街道289号");
		String json = mapper.writeValueAsString(dto);
		String expected = DesensitizedUtil.desensitized("北京市海淀区马连洼街道289号", DesensitizedUtil.DesensitizedType.ADDRESS);
		assertEquals(expected, mapper.readTree(json).get("addr").asText());
	}

	@Test
	@DisplayName("PASSWORD：非空统一输出六位星号")
	void passwordFixedMask() throws JsonProcessingException {
		PwdDto dto = new PwdDto();
		dto.setPwd("any-length-secret-!@#");
		String json = mapper.writeValueAsString(dto);
		assertEquals("******", mapper.readTree(json).get("pwd").asText());
	}

	@Test
	@DisplayName("CREDIT_CODE：与 Hutool creditCode 一致")
	void creditCodeMatchesHutool() throws JsonProcessingException {
		UsccDto dto = new UsccDto();
		dto.setCode("91110108MA01ABCDE7");
		String json = mapper.writeValueAsString(dto);
		assertEquals(DesensitizedUtil.creditCode("91110108MA01ABCDE7"), mapper.readTree(json).get("code").asText());
	}

	@DisplayName("MONEY_DECIMAL：金额小数掩码")
	@ParameterizedTest(name = "input={0} -> {1}")
	@CsvSource({ "'1,234.56', '1,234.**'", "'1,234.567', '1,234.***'", "1234, 1234" })
	void moneyDecimalMasksFraction(String input, String expected) throws JsonProcessingException {
		MoneyDto dto = new MoneyDto();
		dto.setAmount(input);
		String json = mapper.writeValueAsString(dto);
		assertEquals(expected, mapper.readTree(json).get("amount").asText());
	}

	@Data
	private static class MobileDto {

		@Desensitized(DesensitizedType.MOBILE_PHONE)
		private String phone;

	}

	@Data
	private static class NameDto {

		@Desensitized(DesensitizedType.CHINESE_NAME)
		private String name;

	}

	@Data
	private static class IdDto {

		@Desensitized(DesensitizedType.ID_CARD)
		private String idNo;

	}

	@Data
	private static class MailDto {

		@Desensitized(DesensitizedType.EMAIL)
		private String email;

	}

	@Data
	private static class CardDto {

		@Desensitized(DesensitizedType.BANK_CARD)
		private String card;

	}

	@Data
	private static class CustomDto {

		@Desensitized(value = DesensitizedType.CUSTOM, prefixLen = 2, suffixLen = 1, symbol = "#")
		private String secret;

	}

	@Data
	private static class CustomHashDto {

		@Desensitized(value = DesensitizedType.CUSTOM, prefixLen = 2, suffixLen = 2, symbol = "#")
		private String code;

	}

	@Data
	private static class IpDto {

		@Desensitized(DesensitizedType.IP_ADDRESS)
		private String ip;

	}

	@Data
	private static class PlateDto {

		@Desensitized(DesensitizedType.CAR_LICENSE)
		private String plate;

	}

	@Data
	private static class AddrDto {

		@Desensitized(DesensitizedType.ADDRESS)
		private String addr;

	}

	@Data
	private static class PwdDto {

		@Desensitized(DesensitizedType.PASSWORD)
		private String pwd;

	}

	@Data
	private static class UsccDto {

		@Desensitized(DesensitizedType.CREDIT_CODE)
		private String code;

	}

	@Data
	private static class MoneyDto {

		@Desensitized(DesensitizedType.MONEY_DECIMAL)
		private String amount;

	}

}

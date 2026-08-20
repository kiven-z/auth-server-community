package com.auth.common.web.utils;

import com.auth.common.core.model.response.Result;
import com.auth.common.web.exception.ResponseWriteException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.TimeZone;

/**
 * 响应工具类
 *
 * @author Bunny
 */
@UtilityClass
public class ResponseUtil {

	public static void out(HttpServletResponse response, Result<?> result) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule(new JavaTimeModule());
			mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
			mapper.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));

			// 设置成UTF-8 否则多语言会显示不出来，多语言是UTF-8格式
			response.setContentType("application/json;charset=UTF-8");

			int status = response.getStatus();
			response.setStatus(status);

			mapper.writeValue(response.getWriter(), result);
		}
		catch (IOException e) {
			throw new ResponseWriteException("Failed to write response.", e);
		}
	}

}

package com.auth.common.web.resttemplate;

import com.auth.common.core.model.response.Result;
import lombok.experimental.UtilityClass;

/**
 * 远程调用工具
 *
 * @author Bunny
 */
@UtilityClass
public class FeignUtil {

	/**
	 * 判断远程调用是否成功
	 * @param result 远程调用结果
	 * @return 是否成功
	 */
	public static boolean isSuccess(Result<?> result) {
		if (result == null) {
			return false;
		}
		return Integer.valueOf(Result.SUCCESS_CODE).equals(result.getCode());
	}

	/**
	 * 判断远程调用是否成功 且返回数据也要不为空
	 * @param result 远程调用结果
	 * @return 是否成功
	 */
	public static boolean isSuccessWithData(Result<?> result) {
		if (result == null) {
			return false;
		}
		if (!Integer.valueOf(Result.SUCCESS_CODE).equals(result.getCode())) {
			return false;
		}
		return result.getData() != null;
	}

}

package com.auth.module.file.api.model.enums;

/**
 * 文件可见性规则
 *
 * <p>
 * 该规则用于将业务策略与客户端请求合并为最终可见性，避免布尔字段组合产生歧义。
 * </p>
 *
 * @author Bunny
 */
public enum VisibilityRule {

	/**
	 * 强制公开，忽略客户端请求。
	 */
	FORCE_PUBLIC {
		@Override
		public boolean resolve(Boolean requestedIsPrivate) {
			return false;
		}
	},

	/**
	 * 强制私有，忽略客户端请求。
	 */
	FORCE_PRIVATE {
		@Override
		public boolean resolve(Boolean requestedIsPrivate) {
			return true;
		}
	},

	/**
	 * 默认公开，客户端可覆盖。
	 */
	DEFAULT_PUBLIC {
		@Override
		public boolean resolve(Boolean requestedIsPrivate) {
			return requestedIsPrivate != null && requestedIsPrivate;
		}
	},

	/**
	 * 默认私有，客户端可覆盖。
	 */
	DEFAULT_PRIVATE {
		@Override
		public boolean resolve(Boolean requestedIsPrivate) {
			return requestedIsPrivate == null || requestedIsPrivate;
		}
	};

	/**
	 * 解析最终是否私有。
	 * @param requestedIsPrivate 客户端请求值
	 * @return 最终是否私有
	 */
	public abstract boolean resolve(Boolean requestedIsPrivate);

}

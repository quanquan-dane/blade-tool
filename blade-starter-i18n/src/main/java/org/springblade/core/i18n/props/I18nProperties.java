/**
 * Copyright (c) 2018-2099, Chill Zhuang 庄骞 (bladejava@qq.com).
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springblade.core.i18n.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * I18n配置属性
 *
 * @author BladeX
 */
@Data
@ConfigurationProperties(prefix = I18nProperties.PREFIX)
public class I18nProperties {
	public static final String PREFIX = "blade.i18n";

	/**
	 * 是否启用i18n
	 */
	private boolean enabled = true;

	/**
	 * 默认locale
	 */
	private String defaultLocale = "zh_CN";

	/**
	 * 支持的locales
	 */
	private List<String> supportLocales = new ArrayList<>();

	/**
	 * HTTP头名称
	 */
	private String headerName = "Accept-Language";

	/**
	 * 请求参数名称
	 */
	private String paramName = "lang";

	/**
	 * 消息源配置
	 */
	private MessageSource messageSource = new MessageSource();

	/**
	 * 消息源配置
	 */
	@Data
	public static class MessageSource {
		/**
		 * 国际化基础名列表
		 * 示例：
		 * - i18n/errors
		 * - i18n/messages
		 */
		private List<String> baseNames = List.of("i18n/errors", "i18n/messages");

		/**
		 * 编码
		 */
		private String encoding = "UTF-8";

		/**
		 * 缓存过期时间
		 */
		private Duration cacheDuration = Duration.ofMinutes(30);

		/**
		 * 是否使用代码作为默认消息
		 */
		private boolean useCodeAsDefaultMessage = true;
	}
}

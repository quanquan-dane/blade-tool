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
package org.springblade.core.excel.util;


import cn.idev.excel.FastExcel;
import cn.idev.excel.read.builder.ExcelReaderBuilder;
import cn.idev.excel.read.listener.ReadListener;
import cn.idev.excel.util.DateUtils;
import cn.idev.excel.write.builder.ExcelWriterBuilder;
import cn.idev.excel.write.handler.WriteHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.excel.listener.DataListener;
import org.springblade.core.excel.listener.ImportListener;
import org.springblade.core.excel.support.ExcelException;
import org.springblade.core.excel.support.ExcelImporter;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * Excel 工具类
 *
 * @author Chill
 */
@Slf4j
public class ExcelUtil {

	/**
	 * 读取 Excel 的所有 sheet 数据。
	 *
	 * @param <T>   数据模型类型
	 * @param excel Excel 文件
	 * @param clazz 实体类 `Class` 对象
	 * @return 读取到的数据列表
	 */
	public static <T> List<T> read(MultipartFile excel, Class<T> clazz) {
		DataListener<T> dataListener = new DataListener<>();
		ExcelReaderBuilder builder = getReaderBuilder(excel, dataListener, clazz);
		if (builder == null) {
			return null;
		}
		builder.doReadAll();
		return dataListener.getDataList();
	}

	/**
	 * 读取 Excel 指定 sheet 的数据。
	 *
	 * @param <T>     数据模型类型
	 * @param excel   Excel 文件
	 * @param sheetNo sheet 序号（从 0 开始）
	 * @param clazz   实体类 `Class` 对象
	 * @return 读取到的数据列表
	 */
	public static <T> List<T> read(MultipartFile excel, int sheetNo, Class<T> clazz) {
		return read(excel, sheetNo, 1, clazz);
	}

	/**
	 * 读取 Excel 指定 sheet 的数据。
	 *
	 * @param <T>           数据模型类型
	 * @param excel         Excel 文件
	 * @param sheetNo       sheet 序号（从 0 开始）
	 * @param headRowNumber 表头占用的行数
	 * @param clazz         实体类 `Class` 对象
	 * @return 读取到的数据列表
	 */
	public static <T> List<T> read(MultipartFile excel, int sheetNo, int headRowNumber, Class<T> clazz) {
		DataListener<T> dataListener = new DataListener<>();
		ExcelReaderBuilder builder = getReaderBuilder(excel, dataListener, clazz);
		if (builder == null) {
			return null;
		}
		builder.sheet(sheetNo).headRowNumber(headRowNumber).doRead();
		return dataListener.getDataList();
	}

	/**
	 * 读取数据并使用指定的导入器进行数据导入。
	 *
	 * @param <T>      数据模型类型
	 * @param excel    Excel 文件
	 * @param importer 导入逻辑处理器
	 * @param clazz    实体类 `Class` 对象
	 */
	public static <T> void save(MultipartFile excel, ExcelImporter<T> importer, Class<T> clazz) {
		ImportListener<T> importListener = new ImportListener<>(importer);
		ExcelReaderBuilder builder = getReaderBuilder(excel, importListener, clazz);
		if (builder != null) {
			builder.doReadAll();
		}
	}

	/**
	 * 导出 Excel。
	 * <p>
	 * 默认使用当前时间戳作为文件名，"导出数据" 作为 sheet 名。
	 *
	 * @param <T>      数据模型类型
	 * @param response `HttpServletResponse` 对象
	 * @param dataList 要导出的数据列表
	 * @param clazz    实体类 `Class` 对象
	 */
	@SneakyThrows
	public static <T> void export(HttpServletResponse response, List<T> dataList, Class<T> clazz) {
		export(response, DateUtils.format(new Date(), DateUtils.DATE_FORMAT_14), "导出数据", dataList, clazz);
	}

	/**
	 * 导出 Excel。
	 *
	 * @param <T>       数据模型类型
	 * @param response  `HttpServletResponse` 对象
	 * @param fileName  文件名（不含扩展名）
	 * @param sheetName sheet 名称
	 * @param dataList  要导出的数据列表
	 * @param clazz     实体类 `Class` 对象
	 */
	@SneakyThrows
	public static <T> void export(HttpServletResponse response, String fileName, String sheetName, List<T> dataList, Class<T> clazz) {
		export(response, fileName, sheetName, dataList, null, clazz);
	}

	/**
	 * 导出 Excel。
	 *
	 * @param <T>          数据模型类型
	 * @param response     `HttpServletResponse` 对象
	 * @param fileName     文件名（不含扩展名）
	 * @param sheetName    sheet 名称
	 * @param dataList     要导出的数据列表
	 * @param writeHandler 自定义 `WriteHandler` 处理器
	 * @param clazz        实体类 `Class` 对象
	 */
	@SneakyThrows
	public static <T> void export(HttpServletResponse response, String fileName, String sheetName, List<T> dataList, WriteHandler writeHandler, Class<T> clazz) {
		response.setContentType("application/vnd.ms-excel");
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
		response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

		ExcelWriterBuilder write = FastExcel.write(response.getOutputStream(), clazz);
		if (writeHandler != null) {
			write.registerWriteHandler(writeHandler);
		}
		write.sheet(sheetName).doWrite(dataList);
	}

	/**
	 * 获取 Excel 读取构建器 `ExcelReaderBuilder`。
	 *
	 * @param <T>          数据模型类型
	 * @param excel        Excel 文件
	 * @param readListener Excel 读取监听器
	 * @param clazz        实体类 `Class` 对象
	 * @return `ExcelReaderBuilder` 构建器实例，若发生 `IOException` 则返回 `null`
	 * @throws ExcelException 如果文件为空或文件类型不正确
	 */
	public static <T> ExcelReaderBuilder getReaderBuilder(MultipartFile excel, ReadListener<T> readListener, Class<T> clazz) {
		String filename = excel.getOriginalFilename();
		if (!StringUtils.hasText(filename)) {
			throw new ExcelException("请上传文件!");
		}
		if ((!StringUtils.endsWithIgnoreCase(filename, ".xls") && !StringUtils.endsWithIgnoreCase(filename, ".xlsx"))) {
			throw new ExcelException("请上传正确的excel文件!");
		}
		try (InputStream inputStream = new BufferedInputStream(excel.getInputStream())) {
			return FastExcel.read(inputStream, clazz, readListener);
		} catch (IOException e) {
			log.error("读取Excel文件失败", e);
			return null;
		}
	}

}

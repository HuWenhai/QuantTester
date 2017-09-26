package data;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import data.struct.BarSeries;

public abstract class AbstractDataSource implements IDataSource {
	// 上海期货交易所                                   燃油, 线材
	protected final static String SQ[] = {"fu", "wr"};
	// 上海期货交易所 (夜盘)              铜,   铝,   锌,   铅,   镍,   锡,   金,   银,螺纹钢,热轧卷板,沥青,天然橡胶
	protected final static String SY[] = {"cu", "al", "zn", "pb", "ni", "sn", "au", "ag", "rb", "hc", "bu", "ru"};
	// 大连商品交易所                                  玉米, 玉米淀粉, 纤维板,  胶合板, 鸡蛋, 线型低密度聚乙烯, 聚氯乙烯, 聚丙烯
	protected final static String DL[] = {"c",  "cs", "fb", "bb", "jd", "l",  "v",  "pp"};
	// 大连商品交易所  (夜盘)          黄大豆1号, 黄大豆2号, 豆粕, 大豆原油, 棕榈油, 冶金焦炭, 焦煤, 铁矿石
	protected final static String DY[] = {"a",  "b",  "m",  "y",  "p",  "j",  "jm", "i"};
	// 郑州商品交易所
	protected final static String ZZ[] = {"jr", "lr", "pm", "ri", "rs", "sf", "sm", "wh"};
	// 郑州商品交易所 (夜盘)
	protected final static String ZY[] = {"cf", "cy", "fg", "ma", "oi", "rm", "sr", "ta", "zc", "tc"};	// zc原来为tc
	// 中金所
	protected final static String ZJ[] = {"ic", "if", "ih", "t",  "tf"};

	protected List<Path> listSourceFiles(Path dir, String wildcard) {
		List<Path> result = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, wildcard)) {
			for (Path entry : stream) {
				result.add(entry);
			}
		} catch (DirectoryIteratorException | IOException ex) {
			// I/O error encounted during the iteration, the cause is an
			// IOException
			ex.printStackTrace();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public Map<TIME_FRAME, BarSeries>[] multi_time_frame_bars = new Map[13];
	{
		for (int i = 0; i < 13; i++) {
			multi_time_frame_bars[i] = new HashMap<>();
		}
	}

	@Override
	public final BarSeries getBarSeries(final int month, final TIME_FRAME time_frame) {
		return multi_time_frame_bars[month].get(time_frame);
	}

}
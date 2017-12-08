package data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import data.struct.BarSeries;
import helper.DateTimeHelper;

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

	private static BarSeries composeBars(BarSeries bars, TIME_FRAME sourceTf, TIME_FRAME targetTf, boolean countNumber) {
		final int sourceUnit = sourceTf.unit;
		final int targetUnit = targetTf.unit;
		if (sourceUnit >= targetUnit || targetUnit == Integer.MAX_VALUE) {
			return null;
		}

		final int composeNumber = targetUnit / sourceUnit;

		List<List<Integer>> timeGroups = new ArrayList<>();
		List<Integer> timeGroup = new ArrayList<>();
		int baseTime = 0;
		final int size = bars.array_size;
		for (int i = 0; i < size; i++) {
			int this_bar_hour = DateTimeHelper.getHour(bars.times[i]);
			int last_bar_hour = DateTimeHelper.getHour(baseTime);
			boolean anotherDay = (last_bar_hour < 17 && (this_bar_hour > 18 || last_bar_hour > this_bar_hour));
			boolean full = timeGroup.size() == composeNumber;
			int newBaseTime = bars.times[i] / targetUnit * targetUnit;
			boolean align = newBaseTime == baseTime;

			if ((countNumber && anotherDay) || (!countNumber && !align) || (countNumber && !anotherDay && full)) {
				if (!timeGroup.isEmpty()) {
					boolean absoluteValidTime = (!countNumber) && (i > 0) && (bars.times[i] - bars.times[i - 1] < 14 * 60);
					boolean absoluteMoreThan1 = (!countNumber) && (timeGroup.size() > 1);
					if (countNumber || absoluteValidTime || absoluteMoreThan1) {
						timeGroups.add(timeGroup);
					}
				}
				timeGroup = new ArrayList<>();
			}
			timeGroup.add(i);
			if (!align) {
				baseTime = newBaseTime;
			}
		}
		if (!timeGroup.isEmpty()) {
			boolean absoluteMoreThan1 = (!countNumber) && (timeGroup.size() > 1);
			if (countNumber || absoluteMoreThan1) {
				timeGroups.add(timeGroup);
			}
		}

		final int targetSize = timeGroups.size();
		if (targetSize == 0) {
			return null;
		}
		List<Integer> targetTimeI = timeGroups.stream().map(
				idxList -> idxList.stream().min(Comparator.comparing(id -> id)).get()
				).map(id -> bars.times[id]).collect(Collectors.toList());
		List<Float> targetOpenF = timeGroups.stream().map(
				idxList -> idxList.stream().min(Comparator.comparing(id -> id)).get()
				).map(id -> bars.opens[id]).collect(Collectors.toList());
		List<Float> targetHighF = timeGroups.stream().map(
				idxList -> idxList.stream().max(Comparator.comparing(id -> bars.highs[id])).get()
				).map(id -> bars.highs[id]).collect(Collectors.toList());
		List<Float> targetLowF = timeGroups.stream().map(
				idxList -> idxList.stream().min(Comparator.comparing(id -> bars.lows[id])).get()
				).map(id -> bars.lows[id]).collect(Collectors.toList());
		List<Float> targetCloseF = timeGroups.stream().map(
				idxList -> idxList.stream().max(Comparator.comparing(id -> id)).get()
				).map(id -> bars.closes[id]).collect(Collectors.toList());
		List<Double> targetVolumeF = timeGroups.stream().map(
				idxList -> idxList.stream().mapToDouble(id -> bars.volumes[id]).summaryStatistics().getSum()
				).collect(Collectors.toList());
		List<Double> targetAmountF = timeGroups.stream().map(
				idxList -> idxList.stream().mapToDouble(id -> bars.amounts[id]).summaryStatistics().getSum()
				).collect(Collectors.toList());

		BarSeries targetBars = new BarSeries(targetSize);
		for (int i = 0; i < targetSize; i++) {
			targetBars.times[i] = targetTimeI.get(i);
			targetBars.opens[i] = targetOpenF.get(i);
			targetBars.highs[i] = targetHighF.get(i);
			targetBars.lows[i] = targetLowF.get(i);
			targetBars.closes[i] = targetCloseF.get(i);
			targetBars.volumes[i] = targetVolumeF.get(i).floatValue();
			targetBars.amounts[i] = targetAmountF.get(i).floatValue();
		}

		return targetBars;
	}

	protected void generateFromLowerLevelBars(int month, TIME_FRAME tf) {
		BarSeries lowerLevelBars = multi_time_frame_bars[month].get(tf.composedFrom);
		if (lowerLevelBars != null && lowerLevelBars.times != null && lowerLevelBars.array_size != 0) {
			BarSeries composedBars = composeBars(lowerLevelBars, tf.composedFrom, tf, tf.useCountNumber);
			multi_time_frame_bars[month].put(tf, composedBars);
		}
	}

	protected void generateAllBars() {
		for (int i = 0; i < 13; i++) {
			for (int j = TIME_FRAME.MIN1.ordinal() + 1; j < TIME_FRAME.DAY.ordinal(); j++) {
				generateFromLowerLevelBars(i, TIME_FRAME.values()[j]);
			}
		}
	}
}

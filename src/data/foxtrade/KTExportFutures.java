package data.foxtrade;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import data.ITickDataSource;
import data.TIME_FRAME;
import data.struct.Bar;
import data.struct.FutureBar;
import data.struct.FutureBarSeries;
import data.struct.Tick;
import global.Config;
import helper.FileHelper;

@SuppressWarnings("unchecked")
public class KTExportFutures extends AbstractKTExport implements ITickDataSource {
	
	protected static final String[] ContractNum = { "99", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10",
			"11", "12", "97", "98", "99" };
	{
		// TODO 只导入交易所的合约数据, 自己计算量指,仓指, 这样就可以删除SuppressWarnings("unchecked")
		this.multi_time_frame_bars = new Map[ContractNum.length];
		for (int i = 0; i < ContractNum.length; i++) {
			this.multi_time_frame_bars[i] = new HashMap<>();
		}
	}

	protected String getSuffix(String instrument) {
		for (String instr :SQ) {
			if (instrument.equalsIgnoreCase(instr)) {
				return ".SQ";
			}
		}
		for (String instr :SY) {
			if (instrument.equalsIgnoreCase(instr)) {
				return ".SY";
			}
		}
		for (String instr :DL) {
			if (instrument.equalsIgnoreCase(instr)) {
				return ".DL";
			}
		}
		for (String instr :DY) {
			if (instrument.equalsIgnoreCase(instr)) {
				return ".DY";
			}
		}
		for (String instr :ZZ) {
			if (instrument.equalsIgnoreCase(instr)) {
				return ".ZZ";
			}
		}
		for (String instr :ZY) {
			if (instrument.equalsIgnoreCase(instr)) {
				return ".ZY";
			}
		}
		for (String instr :ZJ) {
			if (instrument.equalsIgnoreCase(instr)) {
				return ".ZJ";
			}
		}
		assert false : "instr not found!";
		return ".notfound";
	}
	
	public List<List<? extends Tick>> daily_tick_ohlcv = null;

	@Override
	public final List<List<? extends Tick>> getDailyTicks() {
		return daily_tick_ohlcv;
	}

	private void loadSpecificMonthAndTimeFrame(String instrument_name, int[] months, Set<TIME_FRAME> input_tf_set) {
		Set<TIME_FRAME> load_time_frames_set = new HashSet<>();
		if (Config.UseCompondBars) {
			load_time_frames_set.add(TIME_FRAME.MIN1);
			load_time_frames_set.add(TIME_FRAME.MIN5);
			if (input_tf_set.contains(TIME_FRAME.DAY)) {
				load_time_frames_set.add(TIME_FRAME.DAY);
			}
		} else {
			load_time_frames_set.addAll(input_tf_set);
		}
		for (int month : months) {
			for (TIME_FRAME time_frame : load_time_frames_set) {
				KTExport_3p(instrument_name, month, time_frame);
			}
		}
		if (Config.UseCompondBars) {
			Set<TIME_FRAME> compose_time_frames_set = new HashSet<>(input_tf_set);
			for (TIME_FRAME tf : input_tf_set) {
				compose_time_frames_set.add(tf.composedFrom);
			}
			compose_time_frames_set.remove(null);
			compose_time_frames_set.removeIf(tf -> tf.ordinal() <= TIME_FRAME.MIN1.ordinal());
			compose_time_frames_set.remove(TIME_FRAME.MIN5);
			compose_time_frames_set.removeIf(tf -> tf.ordinal() >= TIME_FRAME.DAY.ordinal());
			List<TIME_FRAME> composed_time_frames_list = new ArrayList<>(compose_time_frames_set);
			composed_time_frames_list.sort(Comparator.comparing(tf -> tf.ordinal()));
			for (int month : months) {
				for (TIME_FRAME time_frame : composed_time_frames_list) {
					generateFromLowerLevelBars(month, time_frame);
				}
			}
		}
	}

	public KTExportFutures(String instrument_name, Set<TIME_FRAME> input_tf_set) {
		loadSpecificMonthAndTimeFrame(instrument_name, IntStream.range(0, 13).toArray(), input_tf_set);
	}

	public KTExportFutures(String instrument_name) {
		Set<TIME_FRAME> time_frame_set = new HashSet<>();
		time_frame_set.addAll(KT_TIME_FRAMES);
		time_frame_set.remove(TIME_FRAME.TICK);
		loadSpecificMonthAndTimeFrame(instrument_name, IntStream.range(0, 13).toArray(), time_frame_set);
	}

	public KTExportFutures(String instrument_name, int month) {
		Set<TIME_FRAME> time_frame_set = new HashSet<>();
		time_frame_set.addAll(KT_TIME_FRAMES);
		time_frame_set.remove(TIME_FRAME.TICK);
		loadSpecificMonthAndTimeFrame(instrument_name, new int[]{month}, time_frame_set);
	}

	public KTExportFutures(String instrument_name, int month, TIME_FRAME time_frame) {
		Set<TIME_FRAME> time_frame_set = new HashSet<>();
		time_frame_set.add(time_frame);
		loadSpecificMonthAndTimeFrame(instrument_name, new int[]{month}, time_frame_set);
	}

	protected void KTExport_3p(String instrument_name, int month, TIME_FRAME time_frame) {
		String file_path = Config.KTExportDir + time_frame.name() + File.separator + instrument_name + ContractNum[month] + getSuffix(instrument_name);
		String[] input_files = null;

		if (time_frame == TIME_FRAME.TICK) {
			List<Path> paths = FileHelper.listSourceFiles(Paths.get(file_path), "*.*");
			if (paths.size() <= 0) {
				return;
			}
			input_files = new String[paths.size()];
			for (int i = 0; i < input_files.length; i++) {
				input_files[i] = paths.get(i).toString();
			}
			daily_tick_ohlcv = new ArrayList<>();
		} else {
			input_files = new String[1];
			input_files[0] = file_path;
		}

		for (String single_file : input_files) {
			if (time_frame == TIME_FRAME.TICK) {
				List<Bar> barList = ReadSTKData(single_file);
				if (barList != null && !barList.isEmpty()) {
					List<Tick> tickList = new ArrayList<>();
					int n = 0;
					for (Bar bar : barList) {
						Tick tick = new Tick();
						tick.no = n++;
						tick.time = bar.time;
						tick.price = bar.open;
						tick.volume = bar.volume;
						tick.openInterest = bar.amount;	// 期货的STKDATA里没有成交额, 成交额字段实际表示的是持仓量
						tickList.add(tick);
					}
					daily_tick_ohlcv.add(tickList);
				}
			} else {
				List<FutureBar> futureBarList = ReadSTKDataEx(single_file);
				if (futureBarList != null && !futureBarList.isEmpty()) {
					FutureBarSeries futureBars = new FutureBarSeries(futureBarList);
					futureBars.checkAndFixSettlement();	// 检查所有结算价, 如果有问题则用收盘价代替
					multi_time_frame_bars[month].put(time_frame, futureBars);
				}
			}
		}
	}
}

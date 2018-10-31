package data.sinyee;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import data.AbstractDataSource;
import data.ITickDataSource;
import data.TIME_FRAME;
import data.struct.DetailedTick;
import data.struct.FutureBar;
import data.struct.FutureBarSeries;
import data.struct.Tick;
import global.Config;
import helper.FileHelper;
import helper.MathHelper;

public class SinYeeDataSource extends AbstractDataSource implements ITickDataSource {

	protected String getPrefix(String instrument) {
		for (String instr :SQ) {
			if (instrument.equalsIgnoreCase(instr)) {
				return "SQ_";
			}
		}
		for (String instr :SY) {
			if (instrument.equalsIgnoreCase(instr)) {
				return "SQ_";
			}
		}
		for (String instr :DL) {
			if (instrument.equalsIgnoreCase(instr)) {
				return "DL_";
			}
		}
		for (String instr :DY) {
			if (instrument.equalsIgnoreCase(instr)) {
				return "DL_";
			}
		}
		for (String instr :ZZ) {
			if (instrument.equalsIgnoreCase(instr)) {
				return "ZZ_";
			}
		}
		for (String instr :ZY) {
			if (instrument.equalsIgnoreCase(instr)) {
				return "ZZ_";
			}
		}
		for (String instr :ZJ) {
			if (instrument.equalsIgnoreCase(instr)) {
				return "ZJ_";
			}
		}

		final String[] SH = {"SH000001", "SH000016", "SH000300", "SH000905"};
		final String[] SZ = {"SZ399001", "SZ399300", "SZ399905"};

		for (String instr : SH) {
			if (instrument.equalsIgnoreCase(instr)) {
				return "SH_";
			}
		}

		for (String instr : SZ) {
			if (instrument.equalsIgnoreCase(instr)) {
				return "SZ_";
			}
		}

		assert false : "Suffix not found!";
		return ".notfound";
	}

	private final static int timeframetoid(TIME_FRAME time_frame) {
		return 7 - (time_frame.ordinal() - TIME_FRAME.MIN1.ordinal());
	}
	
	private final static TIME_FRAME idtotimeframe(int id) {
		return TIME_FRAME.values()[7 - id + TIME_FRAME.MIN1.ordinal()];
	}

	private int getContractMonth(String contract_name) {
		if (contract_name.startsWith("SH") || contract_name.startsWith("SZ")) {
			return 0;
		}
		int contract_name_len = contract_name.length();
		String contract_month_str = contract_name.substring(contract_name_len - 2, contract_name_len);
		return Integer.valueOf(contract_month_str);
	}

	public SinYeeDataSource(String instrument_name, Set<TIME_FRAME> input_tf_set, Predicate<String> contract_filter) {
		Set<TIME_FRAME> time_frames_set = new HashSet<>();
		if (Config.UseCompondBars) {
			time_frames_set.add(TIME_FRAME.MIN1);
			if (input_tf_set.contains(TIME_FRAME.DAY)) {
				time_frames_set.add(TIME_FRAME.DAY);
			}
		} else {
			time_frames_set.addAll(input_tf_set);
		}
		List<Integer> id_list = new ArrayList<>();
		for (TIME_FRAME time_frame : time_frames_set) {
			if (time_frame == TIME_FRAME.TICK) {
				loadTicks(instrument_name, contract_filter);
			} else {
				id_list.add(timeframetoid(time_frame));
			}
		}
		int tf_num = id_list.size();
		if (tf_num <= 0)
			return;
		Integer[] idarray = id_list.toArray(new Integer[0]);
		Arrays.sort(idarray);
		
		String folder = Config.SinYeeDataDir + getPrefix(instrument_name) + instrument_name.toUpperCase();
		List<Path> pathlist = FileHelper.listSourceFiles(Paths.get(folder), "*.bar");
		
		Map<String, List<FutureBar>[]> contract_data_map = new HashMap<>();
		
		for (Path path : pathlist) {
			try (SinYeeBarInputStream sydis = new SinYeeBarInputStream(new FileInputStream(path.toString()))) {
				int file_pos = 0;
				@SuppressWarnings("unused")
				int file_len = sydis.readInt();	// File length
				file_pos += 4;
				Map<String, Map.Entry<Integer, Integer>[]> available_contracts = sydis.getAvailableContracts();
				file_pos += (2 + available_contracts.size() * (30 + 8 * 8));
				Set<String> contract_names_of_the_day_set = available_contracts.keySet();
				contract_names_of_the_day_set.removeIf(contract_filter.negate());
				List<String> contract_names_of_the_day_list = new ArrayList<>(contract_names_of_the_day_set);
				Collections.sort(contract_names_of_the_day_list);
				for (String name : contract_names_of_the_day_list) {
					if (!contract_data_map.containsKey(name)) {
						@SuppressWarnings("unchecked")
						List<FutureBar>[] barlistarray = new List[tf_num];
						for (int i = 0; i < tf_num; i++) {
							barlistarray[i] = new ArrayList<>();
						}
						contract_data_map.put(name, barlistarray);
					}
				}
				
				for (int i = 0; i < tf_num; i++) {
					for (String name : contract_names_of_the_day_list) {
						Map.Entry<Integer, Integer> offset_num = available_contracts.get(name)[idarray[i]];
						int offset = offset_num.getKey();
						int barnum = offset_num.getValue();
						int diff = offset - file_pos;
						if (diff > 0) {
							file_pos += sydis.skip(diff);
							diff = offset - file_pos;
							if (diff > 0) {
								byte[] temp = new byte[diff];	// For skipping
								file_pos += sydis.read(temp, 0, diff);		// Skip "diff" bytes by reading
							}
						}
						List<FutureBar> bars = sydis.readBars(barnum);
						file_pos += barnum * 32;
						contract_data_map.get(name)[i].addAll(bars);
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@SuppressWarnings("unchecked")
		Map<TIME_FRAME, List<FutureBar>>[] multi_time_frame_bars_list = new Map[13];
		for (int i = 0; i < 13; i++) {
			multi_time_frame_bars_list[i] = new HashMap<>();
			for (TIME_FRAME time_frame : time_frames_set) {
				List<FutureBar> barlist = new ArrayList<>();
				multi_time_frame_bars_list[i].put(time_frame, barlist);
			}
		}

		Set<String> contract_name_set = contract_data_map.keySet();
		List<String> contract_name_list = new ArrayList<>(contract_name_set);
		Collections.sort(contract_name_list);
		for (String contract_name : contract_name_list) {
			int contract_month = getContractMonth(contract_name);
			if (contract_month == 99) {
				contract_month = 0;
			}
			assert contract_month < 13;
			
			for (int i = 0; i < tf_num; i++) {
				TIME_FRAME time_frame = idtotimeframe(idarray[i]);
				List<FutureBar> barlist = contract_data_map.get(contract_name)[i];
				multi_time_frame_bars_list[contract_month].get(time_frame).addAll(barlist);
			}
		}

		for (int i = 0; i < 13; i++) {
			for (TIME_FRAME time_frame : time_frames_set) {
				List<FutureBar> barlist = multi_time_frame_bars_list[i].get(time_frame);
				FutureBarSeries bar_series = new FutureBarSeries(barlist);
				multi_time_frame_bars[i].put(time_frame, bar_series);
			}
		}

		if (Config.UseCompondBars) {
			Set<TIME_FRAME> compose_time_frames_set = new HashSet<>(input_tf_set);
			for (TIME_FRAME tf : input_tf_set) {
				compose_time_frames_set.add(tf.composedFrom);
			}
			compose_time_frames_set.remove(null);
			compose_time_frames_set.removeIf(tf -> tf.ordinal() <= TIME_FRAME.MIN1.ordinal());
			compose_time_frames_set.removeIf(tf -> tf.ordinal() >= TIME_FRAME.DAY.ordinal());
			List<TIME_FRAME> composed_time_frames_list = new ArrayList<>(compose_time_frames_set);
			composed_time_frames_list.sort(Comparator.comparing(TIME_FRAME::ordinal));
			for (int i = 0; i < 13; i++) {
				for (TIME_FRAME time_frame : composed_time_frames_list) {
					generateFromLowerLevelBars(i, time_frame);
				}
			}
		}
	}

	public SinYeeDataSource(String instrument_name) {
		this(instrument_name, EnumSet.range(TIME_FRAME.MIN1, TIME_FRAME.DAY), str -> true);
	}
	
	List<Map<String, List<DetailedTick>>> all_tick_list = null;
	
	public List<Map<String, List<DetailedTick>>> getAllTicks() {
		return all_tick_list;
	}
	
	private void loadTicks(String instrument_name, Predicate<String> contract_filter) {
		String folder = Config.SinYeeDataDir + getPrefix(instrument_name) + instrument_name.toUpperCase();
		List<Path> pathlist = FileHelper.listSourceFiles(Paths.get(folder), "*.tick");

		all_tick_list = new ArrayList<>();
		
		final Predicate<String> exclude_99 = str -> !str.endsWith("99");
		final Predicate<String> combined_filter = exclude_99.and(contract_filter);

		for (Path path : pathlist) {
			try (SinYeeTickInputStream sydis = new SinYeeTickInputStream(new FileInputStream(path.toString()))) {
				sydis.readInt();sydis.readShort();	// File length
				String[] contract_names = sydis.getAvailableContracts();
				Map<String, List<DetailedTick>> one_day_ticks = new HashMap<>();
				for (String contract_name : contract_names) {
					int num = sydis.readInt();
					List<DetailedTick> ticks = sydis.readTicks(num);
					if (combined_filter.test(contract_name)) {	// 不能提前RemoveIf
						one_day_ticks.put(contract_name, ticks);
					}
				}
				all_tick_list.add(one_day_ticks);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public List<List<? extends Tick>> daily_tick_ohlcv = null;

	@Override
	public final List<List<? extends Tick>> getDailyTicks() {
		if (daily_tick_ohlcv != null) {
			return daily_tick_ohlcv;
		}
		daily_tick_ohlcv = new ArrayList<>();
		for (Map<String, List<DetailedTick>> one_day_ticks : all_tick_list) {
			if (one_day_ticks.isEmpty()) {
				continue;
			}
			Collection<List<DetailedTick>> tick_lists = one_day_ticks.values();
			@SuppressWarnings("unchecked")
			List<DetailedTick>[] tick_list_array = tick_lists.toArray(new List[0]);
			
			int num = tick_lists.size();
			int[] tick_sizes = new int[num];
			for (int i = 0; i < num; i++) {
				tick_sizes[i] = tick_list_array[i].size();
			}
			int max_index = MathHelper.MaxIndex(tick_sizes);
			
			daily_tick_ohlcv.add(tick_list_array[max_index]);
		}
		return daily_tick_ohlcv;
	}

}
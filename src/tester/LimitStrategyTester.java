package tester;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import data.TIME_FRAME;
import data.collector.LiquidityCollector;
import data.sinyee.SinYeeDataSource;
import data.struct.DetailedTick;
import helper.DateTimeHelper;
import strategy.Portfolio;
import trade.ControlledTrader;

public class LimitStrategyTester extends RealStrategyTester {
	
	private static final Logger logger = LogManager.getLogger();
	
	public static int adjust_sinyee_time(final int time) {
		LocalDateTime ldt = DateTimeHelper.Long2Ldt(time);
		int hour = ldt.getHour();
		if (hour < 8) {
			if (ldt.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
				return time - 52 * 3600;
			} else {
				return time - 4 * 3600;
			}
		} else {
			return time;
		}
	}

	@SuppressWarnings("unchecked")
	private Map<Integer, Byte>[] month_results = new Map[12];

	public LimitStrategyTester(String instrument, TIME_FRAME time_frame, float init_cash, float commission_ratio) {
		super(instrument, time_frame, init_cash, commission_ratio);
		
		SinYeeDataSource sinyee_tick_source = new SinYeeDataSource(instrument, EnumSet.of(TIME_FRAME.TICK), str -> true);
		List<Map<String, List<DetailedTick>>> all_tick_list = sinyee_tick_source.getAllTicks();
		List<Map<String, Map<Integer, Byte>>> all_tick_liq = new ArrayList<>();
		for (Map<String, List<DetailedTick>> daily_tick_map : all_tick_list) {
			Map<String, Map<Integer, Byte>> liq_result_map = new HashMap<>();
			Set<String> contract_names = daily_tick_map.keySet();
			for (String contract_name : contract_names) {
				List<DetailedTick> detailedTicks = daily_tick_map.get(contract_name);
				Map<Integer, Byte> liqResult = detailedTicks.stream()
						.collect(LiquidityCollector.groupingBy(time_frame));
				Map<Integer, Byte> adjustedLiqResult = new HashMap<>();
				// 调整00:59到5:00间的数据到实际的交易时间
				liqResult.entrySet().forEach((entry) -> adjustedLiqResult.put(adjust_sinyee_time(entry.getKey()), entry.getValue()));
				liq_result_map.put(contract_name, adjustedLiqResult);
			}
			all_tick_liq.add(liq_result_map);
		}
		
		for (int i = 0; i < 12; i++) {
			month_results[i] = new HashMap<>();
		}
		
		for (Map<String, Map<Integer, Byte>> tick_liq : all_tick_liq) {
			Set<String> contract_names = tick_liq.keySet();
			for (String contract_name : contract_names) {
				int contract_name_len = contract_name.length();
				String contract_month_str = contract_name.substring(contract_name_len - 2, contract_name_len);
				int contract_month = Integer.valueOf(contract_month_str);

				Map<Integer, Byte> result_map = tick_liq.get(contract_name);
				month_results[contract_month - 1].putAll(result_map);
			}
		}
	}

	public LimitStrategyTester(String instrument, TIME_FRAME time_frame) {
		this(instrument, time_frame, 100_0000.0f, commission_ratio_table.get(instrument.toLowerCase()));
	}
	
	@Override
	protected float[] Evaluate_p(Portfolio portfolio) {
		ControlledTrader controlled_trader = new ControlledTrader(portfolio);
		
		int current_trading_month_id = -1;
		float[] daily_balance = new float[end_index - start_index + 1];
		int force_switch_counter = 0;
		for (int i = start_index; i <= end_index; i++) {
			logger.info(DateTimeHelper.Long2Ldt(adjusted_daily_close_time[i]));
			int main_month_id = daily_main_id[i];
			if (current_trading_month_id != main_month_id) {
				if (portfolio.hasNoPosition()) {
					current_trading_month_id = main_month_id;
					if (i < c_backword_days + force_switch_counter) {
						strategies[current_trading_month_id].setIndexByTime(adjusted_daily_open_time[0]);
					} else {
						strategies[current_trading_month_id].setIndexByTime(adjusted_daily_open_time[i - c_backword_days - force_switch_counter]);
					}
					strategies[current_trading_month_id].reset();
					strategies[current_trading_month_id].calcUntil((price, position) -> {}, adjusted_daily_open_time[i]);
					controlled_trader.allow_open = true;
					force_switch_counter = 0;
				} else {
					controlled_trader.allow_open = false;
				}
			}

			while (true) {
				Integer current_trading_month_time = strategies[current_trading_month_id].getCurrentTime();
				Byte liquidity = month_results[current_trading_month_id].get(current_trading_month_time);
				if (liquidity != null) {
					controlled_trader.allow_sell = (liquidity & 0x01) == 0x01;
					controlled_trader.allow_buy  = (liquidity & 0x02) == 0x02;
				} else {
					controlled_trader.allow_sell = false;
					controlled_trader.allow_buy  = false;
					logger.warn("Can not found liquidity data: {}", DateTimeHelper.Long2Ldt(current_trading_month_time));
				}
				if (strategies[current_trading_month_id]
						.calcNextBar(controlled_trader) > adjusted_daily_close_time[i]) {
					break;
				}
			}
			
			if (current_trading_month_id != main_month_id) {
				if (!portfolio.hasNoPosition()) {
					force_switch_counter++;
					if (force_switch_counter >= c_switch_contract_buffer_days) {
						int close_time = main_daily_barseries.times[i];
						float close_price = daily_barseries[current_trading_month_id].getCloseByTime(close_time);
						assert (close_price > 0.0f) : (
							"current_trading_month_id = " + current_trading_month_id + ", " + DateTimeHelper.Long2Ldt(close_time)
						);
						controlled_trader.setPosition(close_price, 0);
					}
				}
			}
			
			int close_time = main_daily_barseries.times[i];
			float settle_price = daily_barseries[current_trading_month_id].getSettlementByTime(close_time);
			daily_balance[i - start_index] = portfolio.getBalance(settle_price);
		}

		return daily_balance;
	}
}

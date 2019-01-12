package tester;

import java.util.EnumSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import data.TIME_FRAME;
import data.foxtrade.KTExportFutures;
import data.struct.BarSeries;
import data.struct.FutureBarSeries;
import helper.DateTimeHelper;
import strategy.Portfolio;
import strategy.template.BarBasedStrategy;
import trade.ControlledTrader;

public class RealStrategyTester extends AbstractStrategyTester {

	private static final Logger logger = LogManager.getLogger();

	protected BarBasedStrategy[] strategies = null;
	protected FutureBarSeries[] daily_barseries = new FutureBarSeries[12]; 
	protected int[] daily_main_id = null;
	
	protected FutureBarSeries main_daily_barseries = null;
	protected static final int c_switch_contract_buffer_days = 8;	// 如此多天后若老主力合约仍有头寸未平仓, 则在收盘时强行平仓, 并转移至新主力合约交易
	protected static final int c_backword_days = 3;				// 主力合约换月后, 从几天前开始执行策略
	
	public RealStrategyTester(String instrument, TIME_FRAME time_frame, float init_cash, float commission_ratio) {
		super(instrument, time_frame, init_cash, commission_ratio);
		this.datasource = new KTExportFutures(instrument, EnumSet.of(time_frame, TIME_FRAME.DAY));
		adjustDailyOpenCloseTime();
		findMainContracts();
	}

	public RealStrategyTester(String instrument, TIME_FRAME time_frame) {
		this(instrument, time_frame, 100_0000.0f, commission_ratio_table.get(instrument.toLowerCase()));
	}

	@Override
	public void setStrategyParam(Class<? extends BarBasedStrategy> astcls, Object... param) {
		super.setStrategyParam(astcls, param);
		strategies = new BarBasedStrategy[12];
		strategies[0] = createStrategy(astcls, param);
		for (int i = 1; i < 12; i++) {
			try {
				strategies[i] = (BarBasedStrategy) strategies[0].clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
				return;
			}
		}
		
		for (int i = 0; i < 12; i++) {
			BarSeries bar_series = datasource.getBarSeries(i + 1, time_frame);
			if (bar_series != null) {
				strategies[i].setBarSeries(bar_series);
			}
		}
	}

	private void assignDailyData() {
		main_daily_barseries = (FutureBarSeries) datasource.getBarSeries(0, TIME_FRAME.DAY);
		for (int i = 0; i < 12; i++) {
			daily_barseries[i] = (FutureBarSeries) datasource.getBarSeries(i + 1, TIME_FRAME.DAY);
		}
	}

	private void findMainContracts() {
		assignDailyData();

		final int days = main_daily_barseries.times.length;
		daily_main_id = new int[days];
		
		for (int i = 0; i < days; i++) {
			int j = 0;
			for (; j < 12; j++) {
				if (daily_barseries[j] == null) {
					continue;
				}
				int ret = daily_barseries[j].findBarIndex(main_daily_barseries.opens[i], main_daily_barseries.highs[i], main_daily_barseries.lows[i], main_daily_barseries.closes[i], main_daily_barseries.volumes[i]);
				if (ret >= 0) {
					break;
				}
			}
			if (j < 12) {
				daily_main_id[i] = j;
			} else {
				// FIXME 由于数据质量问题, 找不到这一天的数据, 只能沿用前一天的主力合约
				// STKDATA中包含的合约月份信息也不全
				assert false : "找不到这一天的数据: " + DateTimeHelper.Long2Ldt(main_daily_barseries.times[i]);
				logger.warn("找不到这一天的数据: " + DateTimeHelper.Long2Ldt(main_daily_barseries.times[i]));
				if (i > 0)
					daily_main_id[i] = daily_main_id[i - 1];
			}
		}
	}

	@Override
	protected float[] Evaluate_p(Portfolio portfolio) {
		ControlledTrader controlled_trader = new ControlledTrader(portfolio, recordActionDetail);
		
		int current_trading_month_id = -1;
		float[] daily_balance = new float[end_index - start_index + 1];
		int force_switch_counter = 0;
		for (int i = start_index; i <= end_index; i++) {
			logger.info(DateTimeHelper.Long2Ldt(adjusted_daily_close_time[i]));
			int main_month_id = daily_main_id[i];
			if (current_trading_month_id != main_month_id) {
				if (portfolio.hasNoPosition()) {
					current_trading_month_id = main_month_id;
					controlled_trader.setMonth(current_trading_month_id);
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

			strategies[current_trading_month_id].calcUntil(controlled_trader, adjusted_daily_close_time[i]);
			
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
		actionDetail = controlled_trader.getActionDetail();

		return daily_balance;
	}
}

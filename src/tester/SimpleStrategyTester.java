package tester;

import java.util.AbstractMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import data.TIME_FRAME;
import data.foxtrade.KTExportFutures;
import helper.DateTimeHelper;
import strategy.Portfolio;
import strategy.template.BarBasedStrategy;
import trade.ITradeable;
import trade.IdealTrader;

public class SimpleStrategyTester extends AbstractStrategyTester {
	
	private static final Logger logger = LogManager.getLogger();

	private BarBasedStrategy strategy = null;
	
	public SimpleStrategyTester(String instrument, TIME_FRAME time_frame, float init_cash, float commission_ratio) {
		super(instrument, time_frame, init_cash, commission_ratio);
		this.datasource = new KTExportFutures(instrument, 0);
		adjustDailyOpenCloseTime();
	}

	public SimpleStrategyTester(String instrument, TIME_FRAME time_frame) {
		this(instrument, time_frame, 100_0000.0f, commission_ratio_table.get(instrument.toLowerCase()));
	}

	@Override
	public void setStrategyParam(Class<? extends BarBasedStrategy> astcls, Object... param) {
		super.setStrategyParam(astcls, param);
		strategy = createStrategy(astcls, param);
		strategy.setBarSeries(datasource.getBarSeries(0, time_frame));
	}

	@Override
	protected float[] Evaluate_p(Portfolio portfolio) {
		IdealTrader ideal_trader = new IdealTrader(portfolio, recordActionDetail);
		ideal_trader.setInstrumentId(instrument + "1999");

		float[] daily_balance = new float[end_index - start_index + 1];
		strategy.setIndexByTime(adjusted_daily_open_time[start_index]);
		for (int i = start_index; i <= end_index; i++) {
			logger.info(DateTimeHelper.Long2Ldt(adjusted_daily_close_time[i]));
			strategy.calcUntil(ideal_trader, adjusted_daily_close_time[i]);
			daily_balance[i - start_index] = portfolio.getBalance(settle_price[i]);
		}
		actionDetail = ideal_trader.getActionDetail();

		return daily_balance;
	}

	@Override
	protected void saveAdditionalDots() {
		additionalDot = new AdditionalDot();
		String instrumentId = instrument + "1999";
		Map<AbstractMap.SimpleEntry<Integer, Float>, Integer> dotMarks = strategy.getDotMarks();
		if (dotMarks == null) {
			return;
		}
		for (Map.Entry<AbstractMap.SimpleEntry<Integer, Float>, Integer> kv : dotMarks.entrySet()) {
			additionalDot.append(kv.getKey().getKey(), instrumentId, kv.getKey().getValue(), kv.getValue());
		}
	}
}

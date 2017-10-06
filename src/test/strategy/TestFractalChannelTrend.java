package test.strategy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import data.TIME_FRAME;
import helper.DateTimeHelper;
import strategy.FractalChannelTrend;
import test.CommonParam;
import test.ParamManager;
import tester.AbstractStrategyTester;

public class TestFractalChannelTrend {

	private static final Logger logger = LogManager.getLogger();

	public static void main(String[] args) {
		final CommonParam cp = ParamManager.getCommonParam("cu", TIME_FRAME.MIN30, "20090204 000000", "20120220 170000");
		final Object[] pp = ParamManager.getParticularParam(FractalChannelTrend.class, 15);

		AbstractStrategyTester st = new tester.SimpleStrategyTester(cp.instrument, cp.tf);
		st.setTestDateRange((int) DateTimeHelper.Ldt2Long(cp.start_date), (int) DateTimeHelper.Ldt2Long(cp.end_date));
		st.setStrategyParam(FractalChannelTrend.class, pp);
		st.evaluate();

		logger.info(st.getPerformances());
		st.drawDailyBalance(FractalChannelTrend.class.getSimpleName() + ".png");
	}
}

package test.strategy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import data.TIME_FRAME;
import helper.DateTimeHelper;
import strategy.WedgeBreak;
import test.CommonParam;
import test.ParamManager;
import tester.AbstractStrategyTester;

public class TestWedgeBreak {

	private static final Logger logger = LogManager.getLogger();

	public static void main(String[] args) {
		final CommonParam cp = ParamManager.getCommonParam("oi", TIME_FRAME.MIN60, "20080101 000000", "20170505 174000");
		final Object[] pp = ParamManager.getParticularParam(WedgeBreak.class, 1, 0.02f, 0.02f, 0.2f);

		AbstractStrategyTester st = new tester.RealStrategyTester(cp.instrument, cp.tf);
		st.setTestDateRange((int) DateTimeHelper.Ldt2Long(cp.start_date), (int) DateTimeHelper.Ldt2Long(cp.end_date));
		st.setStrategyParam(WedgeBreak.class, pp);
		st.enableActionDetailRecording();
		st.evaluate();
		st.saveActionDetail();

		logger.info(st.getPerformances());
		st.drawDailyBalance(WedgeBreak.class.getSimpleName() + ".png");
	}
}

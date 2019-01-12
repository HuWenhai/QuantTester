package test.strategy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import data.TIME_FRAME;
import helper.DateTimeHelper;
import strategy.Class3Channel;
import test.CommonParam;
import test.ParamManager;
import tester.AbstractStrategyTester;

public class TestClass3Channel {

	private static final Logger logger = LogManager.getLogger();

	public static void main(String[] args) {
		final CommonParam cp = ParamManager.getCommonParam("j", TIME_FRAME.MIN60, "20080101 000000", "20170105 174000");
		final Object[] pp = ParamManager.getParticularParam(Class3Channel.class, 1, 0.02f, 0.02f, 0.2f);

		AbstractStrategyTester st = new tester.RealStrategyTester(cp.instrument, cp.tf);
		st.setTestDateRange((int) DateTimeHelper.Ldt2Long(cp.start_date), (int) DateTimeHelper.Ldt2Long(cp.end_date));
		st.setStrategyParam(Class3Channel.class, pp);
		st.evaluate();

		logger.info(st.getPerformances());
		st.drawDailyBalance(Class3Channel.class.getSimpleName() + ".png");
	}
}

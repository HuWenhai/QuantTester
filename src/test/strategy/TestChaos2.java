package test.strategy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import data.TIME_FRAME;
import helper.DateTimeHelper;
import strategy.Chaos2;
import test.CommonParam;
import test.ParamManager;
import tester.AbstractStrategyTester;

public class TestChaos2 {

	private static final Logger logger = LogManager.getLogger();

	public static void main(String[] args) {
		final CommonParam cp = ParamManager.getCommonParam("rb", TIME_FRAME.MIN15, "20100101 000000", "20160101 170000");
		final Object[] pp = ParamManager.getParticularParam(Chaos2.class,
				6,
				2.0f,
				5f
				);

		AbstractStrategyTester st = new tester.RealStrategyTester(cp.instrument, cp.tf, 10_0000.0f, 0.000_000f);
		//AbstractStrategyTester st = new tester.RealStrategyTester(cp.instrument, cp.tf, 100_0000.0f, 0.000_065f);
		st.setTestDateRange((int) DateTimeHelper.Ldt2Long(cp.start_date), (int) DateTimeHelper.Ldt2Long(cp.end_date));
		st.setStrategyParam(Chaos2.class, pp);
		st.evaluate();

		logger.info(st.getPerformances());
		st.drawDailyBalance(Chaos2.class.getSimpleName() + ".png");
	}
}

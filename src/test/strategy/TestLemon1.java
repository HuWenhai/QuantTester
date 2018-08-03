package test.strategy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import data.TIME_FRAME;
import helper.DateTimeHelper;
import strategy.Lemon1;
import test.CommonParam;
import test.ParamManager;
import tester.AbstractStrategyTester;

public class TestLemon1 {

	private static final Logger logger = LogManager.getLogger();

	public static void main(String[] args) {
		final CommonParam cp = ParamManager.getCommonParam("rb", TIME_FRAME.MIN15, "20080101 000000", "20170105 174000");
		final Object[] pp = ParamManager.getParticularParam(Lemon1.class,
				1, 0.02f,
				5, 34,
				0.02f, 0.19999f
				);

		AbstractStrategyTester st = new tester.RealStrategyTester(cp.instrument, cp.tf, 100_0000.0f, 0.000_205f);
		//AbstractStrategyTester st = new tester.RealStrategyTester(cp.instrument, cp.tf, 100_0000.0f, 0.000_065f);
		st.setTestDateRange((int) DateTimeHelper.Ldt2Long(cp.start_date), (int) DateTimeHelper.Ldt2Long(cp.end_date));
		st.setStrategyParam(Lemon1.class, pp);
		st.evaluate();

		logger.info(st.getPerformances());
		st.drawDailyBalance(Lemon1.class.getSimpleName() + ".png");
	}
}

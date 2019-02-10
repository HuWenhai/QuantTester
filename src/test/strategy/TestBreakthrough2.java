package test.strategy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import data.TIME_FRAME;
import helper.DateTimeHelper;
import indicator.APPLIED_PRICE;
import strategy.Breakthrough2;
import strategy.BreakthroughBase;
import test.CommonParam;
import test.ParamManager;
import tester.AbstractStrategyTester;

public class TestBreakthrough2 {

	private static final Logger logger = LogManager.getLogger();

	public static void main(String[] args) {
		
		final CommonParam cp = ParamManager.getCommonParam("rb", TIME_FRAME.MIN15, "20100101 000000", "20160101 170000");
		final Object[] pp = ParamManager.getParticularParam(Breakthrough2.class,
				0.02f,		// AFstep
				0.1999f,	// AFmax
				9,			// AMAPeriod
				2,
				30,
				12,
				7,
				0.10f,
				12, 26, 9, APPLIED_PRICE.PRICE_CLOSE);

		AbstractStrategyTester st = new tester.SimpleStrategyTester(cp.instrument, cp.tf);
		st.setTestDateRange((int) DateTimeHelper.Ldt2Long(cp.start_date), (int) DateTimeHelper.Ldt2Long(cp.end_date));
		st.setStrategyParam(Breakthrough2.class, pp);
		st.evaluate();

		logger.info(st.getPerformances());
		st.drawDailyBalance(Breakthrough2.class.getSimpleName() + ".png");
	}

}

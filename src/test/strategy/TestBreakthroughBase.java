package test.strategy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import data.TIME_FRAME;
import helper.DateTimeHelper;
import strategy.BreakthroughBase;
import test.CommonParam;
import test.ParamManager;
import tester.AbstractStrategyTester;

public class TestBreakthroughBase {

	private static final Logger logger = LogManager.getLogger();

	public static void main(String[] args) {
		
		final CommonParam cp = ParamManager.getCommonParam("rb", TIME_FRAME.MIN15, "20100101 000000", "20170101 170000");
		final Object[] pp = ParamManager.getParticularParam(BreakthroughBase.class,
				0.02f,		// AFstep
				0.1999f,	// AFmax
				9,			// AMAPeriod
				2,
				30,
				12,
				7,
				0.10f);

		AbstractStrategyTester st = new tester.SimpleStrategyTester(cp.instrument, cp.tf);
		st.setTestDateRange((int) DateTimeHelper.Ldt2Long(cp.start_date), (int) DateTimeHelper.Ldt2Long(cp.end_date));
		st.setStrategyParam(BreakthroughBase.class, pp);
		st.enableActionDetailRecording();
		st.evaluate();
		st.saveActionDetail();

		logger.info(st.getPerformances());
		st.drawDailyBalance(BreakthroughBase.class.getSimpleName() + ".png");
	}

}

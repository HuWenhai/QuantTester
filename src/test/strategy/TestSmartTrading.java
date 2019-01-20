package test.strategy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import data.TIME_FRAME;
import helper.DateTimeHelper;
import strategy.SmartTrading;
import test.CommonParam;
import test.ParamManager;
import tester.AbstractStrategyTester;

public class TestSmartTrading {

	private static final Logger logger = LogManager.getLogger();

	public static void main(String[] args) {
		
		final CommonParam cp = ParamManager.getCommonParam("rb", TIME_FRAME.MIN15, "20100101 000000", "20160101 170000");
		final Object[] pp = ParamManager.getParticularParam(SmartTrading.class,
				0.02f,		// AFstep
				0.1999f,	// AFmax
				9,			// AMAPeriod
				2,
				30,
				20,
				0.1f);

		AbstractStrategyTester st = new tester.RealStrategyTester(cp.instrument, cp.tf);
		st.setTestDateRange((int) DateTimeHelper.Ldt2Long(cp.start_date), (int) DateTimeHelper.Ldt2Long(cp.end_date));
		st.setStrategyParam(SmartTrading.class, pp);
		st.enableActionDetailRecording();
		st.evaluate();
		st.saveActionDetail();

		logger.info(st.getPerformances());
		st.drawDailyBalance(SmartTrading.class.getSimpleName() + ".png");
	}

}

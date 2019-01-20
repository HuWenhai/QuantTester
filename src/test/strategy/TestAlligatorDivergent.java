package test.strategy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import data.TIME_FRAME;
import helper.DateTimeHelper;
import strategy.AlligatorDivergent;
import test.CommonParam;
import test.ParamManager;
import tester.AbstractStrategyTester;

public class TestAlligatorDivergent {

	private static final Logger logger = LogManager.getLogger();

	public static void main(String[] args) {
		final CommonParam cp = ParamManager.getCommonParam("rb", TIME_FRAME.DAY, "20090204 000000", "20160220 170000");
		final Object[] pp = ParamManager.getParticularParam(AlligatorDivergent.class,
				0.02f,		// AFstep
				0.1999f,	// AFmax
				10,			// BBPeriod
				2.0f,		// BBDeviation
				150.0f		// stdDev
				);

		AbstractStrategyTester st = new tester.RealStrategyTester(cp.instrument, cp.tf);
		st.setTestDateRange((int) DateTimeHelper.Ldt2Long(cp.start_date), (int) DateTimeHelper.Ldt2Long(cp.end_date));
		st.setStrategyParam(AlligatorDivergent.class, pp);
		st.evaluate();

		logger.info(st.getPerformances());
		st.drawDailyBalance(AlligatorDivergent.class.getSimpleName() + ".png");
	}
}

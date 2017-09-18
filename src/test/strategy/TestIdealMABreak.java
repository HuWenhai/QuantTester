package test.strategy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import data.TIME_FRAME;
import helper.DateTimeHelper;
import indicator.APPLIED_PRICE;
import indicator.MA;
import strategy.IdealMABreakStrategy;
import test.CommonParam;
import test.ParamManager;
import tester.AbstractStrategyTester;

public class TestIdealMABreak {

	private static final Logger logger = LogManager.getLogger();
	
	public static void main(String[] args) {
		final CommonParam cp = ParamManager.getCommonParam("al", TIME_FRAME.MIN15, "20100101 000000", "20160101 170000");
		final Object[] pp = ParamManager.getParticularParam(IdealMABreakStrategy.class, 345, MA.MODE_SMMA, APPLIED_PRICE.PRICE_TYPICAL);
		
		AbstractStrategyTester st = new tester.SimpleStrategyTester(cp.instrument, cp.tf, 100_0000.0f, 0.03f);
		st.setTestDateRange((int) DateTimeHelper.Ldt2Long(cp.start_date), (int) DateTimeHelper.Ldt2Long(cp.end_date));
		st.setStrategyParam(IdealMABreakStrategy.class, pp);
		st.evaluate();
		
		logger.info(st.getPerformances());
		st.drawDailyBalance(IdealMABreakStrategy.class.getSimpleName() + ".png");
	}
}

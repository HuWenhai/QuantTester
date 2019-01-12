package test.optimize;

import java.util.Map.Entry;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;

import java.util.Set;

import data.TIME_FRAME;
import helper.DateTimeHelper;
import performance.Performances;
import strategy.Class3Channel;
import test.CommonParam;
import test.ParamManager;
import tester.StrategyOptimizer;

public final class OptimizeClass3Channel {

	public static void main(String[] args) {
		final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		final Configuration config = ctx.getConfiguration();
		config.getLoggerConfig(strategy.Portfolio.class.getName()).setLevel(Level.WARN);
		ctx.updateLoggers(config);
		
		final CommonParam cp = ParamManager.getCommonParam("m", TIME_FRAME.MIN60, "20080101 000000", "20160101 170000");
		
		StrategyOptimizer so = new StrategyOptimizer(tester.RealStrategyTester.class);
		so.setInstrumentParam(cp.instrument, cp.tf);
		so.setTestDateRange((int) DateTimeHelper.Ldt2Long(cp.start_date), (int) DateTimeHelper.Ldt2Long(cp.end_date));
		int num = so.setStrategyParamRange(Class3Channel.class,
				new Integer[]{0, 1, 1},
				new Float[]{0.02f, 0.10f, 0.01f},
				new Float[]{0.01f, 0.02999f, 0.01f},	// AFstep
				new Float[]{0.1f, 0.2999f, 0.1f}		// AFmax
				);
		System.out.println(num);
		so.StartOptimization();
		Set<Entry<Object[],Performances>> entryset = so.result_db.entrySet();
		for (Entry<Object[],Performances> entry : entryset) {
			for (Object obj : entry.getKey()) {
				System.out.print(obj + ",\t");
			}
			System.out.println("ProfitRatio: " + String.format("%.5f", entry.getValue().ProfitRatio) + "\tMaxDrawDown: " + entry.getValue().MaxDrawDown);
		}
	}
}

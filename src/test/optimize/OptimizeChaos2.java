package test.optimize;

import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;

import data.TIME_FRAME;
import helper.DateTimeHelper;
import performance.Performances;
import strategy.Chaos2;
import test.CommonParam;
import test.ParamManager;
import tester.StrategyOptimizer;

public final class OptimizeChaos2 {

	public static void main(String[] args) {
		final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		final Configuration config = ctx.getConfiguration();
		config.getLoggerConfig(strategy.Portfolio.class.getName()).setLevel(Level.WARN);
		ctx.updateLoggers(config);
		
		final CommonParam cp = ParamManager.getCommonParam("ru", TIME_FRAME.MIN60, "20100101 000000", "20160101 170000");
		
		StrategyOptimizer so = new StrategyOptimizer(tester.RealStrategyTester.class);
		so.setInstrumentParam(cp.instrument, cp.tf);
		so.setTestDateRange((int) DateTimeHelper.Ldt2Long(cp.start_date), (int) DateTimeHelper.Ldt2Long(cp.end_date));
		int num = so.setStrategyParamRange(Chaos2.class,
				new Float[]{0.01f, 0.02999f, 0.01f},	// AFstep
				new Float[]{0.1f, 0.2999f, 0.1f},		// AFmax
				new Integer[]{1, 3, 1},				// Open volume
				new Integer[]{3, 6, 1},				// 1st add on volume
				new Integer[]{13, 13, 2},
				new Integer[]{8, 8, 2},
				new Integer[]{8, 8, 2},
				new Integer[]{5, 5, 1},
				new Integer[]{5, 5, 1},
				new Integer[]{3, 3, 1},
				new Integer[]{5, 10, 1},			// BB Period
				new Float[]{1.5f, 2.499f, 0.5f},	// BB Deviation
				new Float[]{5.0f, 51.0f, 5.0f},	// BB deviation threshold
				new Integer[]{5, 5, 1},			// fastMA
				new Integer[]{34, 34, 2}		// slowMA
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

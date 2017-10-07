package tester;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import data.IDataSource;
import data.TIME_FRAME;
import data.struct.FutureBarSeries;
import drawing.ResultDrawing;
import performance.DailyPerformances;
import performance.Performances;
import strategy.Portfolio;
import strategy.template.BarBasedStrategy;

public abstract class AbstractStrategyTester implements Cloneable {
	protected static final Map<String, Float> commission_ratio_table = new HashMap<String, Float>() {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Float get(Object key) {
			if (containsKey(key)) {
				return super.get(key);
			} else {
				return 0.001f;	// 手续费数据库中找不到此品种, 返回默认值
			}
		}
	};
	
	static {
		// 手续费 + 滑点
		commission_ratio_table.put("au", 0.000_055f + 0.000_20f); // 手续费固定10元
		commission_ratio_table.put("ag", 0.000_065f + 0.000_25f);
		commission_ratio_table.put("cu", 0.000_065f + 0.000_30f);
		commission_ratio_table.put("al", 0.000_080f + 0.000_50f); // 手续费固定3元
		commission_ratio_table.put("rb", 0.000_130f + 0.000_60f);
		commission_ratio_table.put("ru", 0.000_500f);

		commission_ratio_table.put("i",  0.000_400f + 0.001_00f);
		commission_ratio_table.put("c",  0.000_130f + 0.001_00f); // 手续费固定1.2元
		commission_ratio_table.put("cs", 0.000_130f + 0.001_00f); // 手续费固定1.5元
		commission_ratio_table.put("a",  0.000_080f + 0.000_30f); // 手续费固定2.0元
		commission_ratio_table.put("m",  0.000_080f + 0.000_40f); // 手续费固定1.5元
		commission_ratio_table.put("y",  0.000_060f + 0.000_35f); // 手续费固定2.5元

		commission_ratio_table.put("l",  0.000_090f + 0.001_00f); // 手续费固定2.0元
		commission_ratio_table.put("v",  0.000_100f + 0.001_00f); // 手续费固定2.0元
		commission_ratio_table.put("pp", 0.000_120f + 0.000_20f); // 手续费固定2.4元
		commission_ratio_table.put("ta", 0.000_750f);
	}

	protected IDataSource datasource;
	protected final TIME_FRAME time_frame;
	protected final float init_cash;
	protected final float commission_ratio;

	protected Performances performances = null;
	
	public Performances getPerformances() {
		return performances;
	}

	public AbstractStrategyTester(String instrument, TIME_FRAME time_frame, float init_cash, float commission_ratio) {
		this.time_frame = time_frame;
		this.init_cash = init_cash;
		this.commission_ratio = commission_ratio;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	protected static BarBasedStrategy createStrategy(Class<? extends BarBasedStrategy> astcls, Object... param) {
		final int param_num = param.length;
		Class<?>[] classes = new Class[param_num];
		for (int i = 0; i < param_num; i++) {
			Class<?> cls = param[i].getClass();
			Class<?> supercls = cls.getSuperclass();
			if (supercls != null && supercls.isEnum()) {	// 为了解决枚举类的匿名内部类问题
				classes[i] = supercls;
			} else {
				classes[i] = cls;
			}
		}
		try {
			Constructor<? extends BarBasedStrategy> c = astcls.getConstructor(classes);
			return c.newInstance(param);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}

	protected int start_index = 0, end_index = Integer.MAX_VALUE;
	public void setTestDateRange(int start, int end) {
		int[] Time = datasource.getBarSeries(0, TIME_FRAME.DAY).times;
		int len = Time.length;
		
		start_index = 0;
		for (int i = 0; i < len; i++) {
			if (Time[i] >= start) {
				start_index = i;
				break;
			}
		}
		
		end_index = len - 1;
		for (int i = len - 1; i >= 0; i--) {
			if (Time[i] <= end) {
				end_index = i;
				break;
			}
		}
	}
	
	protected int[] adjusted_daily_close_time = null;
	protected int[] adjusted_daily_open_time = null;
	protected float[] settle_price = null;
	protected void adjustDailyOpenCloseTime() {
		final FutureBarSeries daily_bar_series = (FutureBarSeries) datasource.getBarSeries(0, TIME_FRAME.DAY);
		final int days = daily_bar_series.times.length;
		adjusted_daily_close_time = new int[days];
		adjusted_daily_open_time  = new int[days];
		settle_price = new float[days];
		for (int i = 0; i < days; i++) {
			adjusted_daily_close_time[i] = (daily_bar_series.times[i] / (24 * 3600) * 24 + 17) * 3600;			// 统一以下午5点作为收盘时间
			if (i == 0) {
				adjusted_daily_open_time[i] = (daily_bar_series.times[0] / (24 * 3600) * 24 - 6) * 3600;		// TODO 处理边界值
			} else {
				adjusted_daily_open_time[i] = (daily_bar_series.times[i - 1] / (24 * 3600) * 24 + 18) * 3600;	// FIXME 获得真实的开盘时间(考虑夜盘)
			}
			settle_price[i] = daily_bar_series.settlements[i];
		}
	}
	
	public abstract void setStrategyParam(Class<? extends BarBasedStrategy> astcls, Object... param);
	
	protected abstract float[] Evaluate_p(Portfolio portfolio);
	
	private float[] daily_balance = null;
	
	public void evaluate() {
		Portfolio portfolio = new Portfolio(init_cash);
		portfolio.setCommission_ratio(commission_ratio);
		portfolio.setMargin_ratio(1.0f); // TODO 建立保证金率数据库,作为默认值

		daily_balance = Evaluate_p(portfolio);
		
		performances = new DailyPerformances(Arrays.copyOfRange(adjusted_daily_close_time, start_index, end_index + 1), daily_balance);
		performances.LongTrades = portfolio.long_trades;
		performances.ShortTrades = portfolio.short_trades;
		performances.calculateAll();
	}
	
	public void drawDailyBalance(String filename) {
		// TODO 挪到Performance类里面
		new ResultDrawing().drawScatterDiagram(filename, daily_balance);
	}
}

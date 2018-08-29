package strategy.template;

import data.struct.BarSeries;
import helper.DateTimeHelper;
import helper.ReflectHelper;
import indicator.IIndicator;
import trade.ITradeable;

public abstract class BarBasedStrategy implements IStrategy, Cloneable {

	protected int position = 0;
	protected int minimumBarsToWork = 1;

	@Override
	public final int getPosition() {
		return position;
	}

	public void reset() {
		position = 0;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	protected int[] Time;
	protected float[] Open;
	protected float[] High;
	protected float[] Low;
	protected float[] Close;
	protected float[] Volume;
	
	public void setBarSeries(BarSeries barseries) {
		Time = barseries.times;
		Open = barseries.opens;
		High = barseries.highs;
		Low = barseries.lows;
		Close = barseries.closes;
		Volume = barseries.volumes;
		calculateIndicators();
	}

	protected void calculateIndicators() {
		Object[] allFieldObjects = ReflectHelper.getAllFieldObjectsIncludingSuper(this);
		for (Object fieldObj : allFieldObjects) {
			if (fieldObj != null && fieldObj instanceof IIndicator) {
				IIndicator indicator = (IIndicator) fieldObj;
				indicator.calculate(Open, High, Low, Close);
				minimumBarsToWork = Math.max(minimumBarsToWork, indicator.minimumBarsToWork());
			}
		}
	}

	protected int current_index = 0;

	@Override
	public final int getCurrentTime() {
		return Time[current_index];
	}
	
	public boolean setIndexByTime(int new_time) {
		int i = 0;
		for (; i < Time.length; i++) {
			if (Time[i] >= new_time) {
				break;
			}
		}
		
		if (i == Time.length)
			return false;
		
		current_index = i;
		return true;
	}

	public int calcNextBar(ITradeable trader) {
		if(current_index >= Time.length) {
			return Integer.MAX_VALUE;
		} else {
			tradeOneBar(trader);
			return Time[++current_index];
		}
	}
	
	public int calcUntil(ITradeable trader, final int untilTime) {
		int untilIndex;
		if (untilTime <= 0) {
			untilIndex = Time.length - 1;
		} else {
			int i = 0;
			for (; i < Time.length; i++) {
				if (Time[i] > untilTime) {
					break;
				}
			}
			untilIndex = i - 1;
		}

		if (untilIndex < 0) {
			return 0;
		}
		
		for (; current_index <= untilIndex; current_index++) {
			tradeOneBar(trader);
		}

		return Time[untilIndex];
	}

	protected boolean isTheFirstKLineOfTradingDay(int index) {
		if (index == 0)
			return true;

		int this_bar_hour = DateTimeHelper.getHour(Time[index]);
		int last_bar_hour = DateTimeHelper.getHour(Time[index - 1]);

		// 17点和18点肯定为当日收盘之后, 次日开盘之前
		boolean condition1 = last_bar_hour < 17 && this_bar_hour > 18;	// 头一天有夜盘
		boolean condition2 = last_bar_hour > this_bar_hour && last_bar_hour < 17;	// 头一天无夜盘
		return (condition1 || condition2);
	}
}

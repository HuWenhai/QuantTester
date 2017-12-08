package data.struct;

import java.util.Iterator;
import java.util.List;

public class BarSeries implements Iterable<Bar> {
	public final int array_size;
	public final int times[];
	public final float opens[];
	public final float highs[];
	public final float lows[];
	public final float closes[];
	public final float volumes[];
	public final float amounts[];

	public BarSeries(int size) {
		this.array_size = size;
		this.times = new int[array_size];
		this.opens = new float[array_size];
		this.highs = new float[array_size];
		this.lows = new float[array_size];
		this.closes = new float[array_size];
		this.volumes = new float[array_size];
		this.amounts = new float[array_size];
	}

	public BarSeries(List<? extends Bar> bars) {
		if (bars == null || bars.isEmpty()) {
			array_size = 0;
			times = null;
			opens = null;
			highs = null;
			lows = null;
			closes = null;
			volumes = null;
			amounts = null;
			return;
		}

		array_size = bars.size();

		this.times = new int[array_size];
		this.opens = new float[array_size];
		this.highs = new float[array_size];
		this.lows  = new float[array_size];
		this.closes = new float[array_size];
		this.volumes = new float[array_size];
		this.amounts = new float[array_size];

		for (int i = 0; i < array_size; i++) {
			this.times[i] = bars.get(i).time;
			this.opens[i] = bars.get(i).open;
			this.highs[i] = bars.get(i).high;
			this.lows [i] = bars .get(i).low;
			this.closes[i] = bars.get(i).close;
			this.volumes[i] = bars.get(i).volume;
			this.amounts[i] = bars.get(i).amount;
		}
	}

	/**
	 * 寻找具有相同开,高,收,低,量值的k线的索引
	 * 不比对Time, 2个原因
	 * 1. 由于数据质量问题, 时间可能对应不上
	 * 2. KT和SinYee的日线数据具有不同格式的Time字段
	 * @param open
	 * @param high
	 * @param low
	 * @param close
	 * @param volume
	 * @return
	 */
	// FIXME 高价, 低价可能不符
	public int findBarIndex(float open, float high, float low, float close, float volume) {
		for (int j = 0; j < times.length; j++) {
			if (opens[j] == open && highs[j] == high && lows[j] == low && closes[j] == close
					&& volumes[j] == volume) {
				return j;
			}
		}

		return -1;
	}

	public int findBarIndex(Bar bar) {
		return findBarIndex(bar.open, bar.high, bar.low, bar.close, bar.volume);
	}

	@Override
	public Iterator<Bar> iterator() {
		return new Iterator<Bar>() {

			private int index = 0;

			@Override
			public boolean hasNext() {
				return times != null && index < times.length;
			}

			@Override
			public Bar next() {
				Bar bar = new Bar();
				bar.time = times[index];
				bar.open = opens[index];
				bar.high = highs[index];
				bar.low = lows[index];
				bar.close = closes[index];
				bar.volume = volumes[index];
				bar.amount = amounts[index];

				index++;
				return bar;
			}
		};
	}
}

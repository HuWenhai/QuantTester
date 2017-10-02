package strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import data.ITickDataSource;
import data.collector.FutureBarCollector;
import data.struct.BarSeries;
import data.struct.FutureBar;
import data.struct.Tick;
import indicator.APPLIED_PRICE;

public class TickBarStrategy {
	private final int tickNum;
	private final int barNum;
	
	public TickBarStrategy(int TickNum, int BarNum) {
		this.tickNum = TickNum;
		this.barNum = BarNum;
	}

	protected List<int[]> daily_signal = null;
	protected List<BarSeries> daily_tick_bar = null;

	public final List<BarSeries> get_daily_tick_bar() {
		return daily_tick_bar;
	}
	
	public final int getDays() {
		return daily_tick_bar.size();
	}
	
	public void setDataSource(ITickDataSource source) {
		daily_signal = new ArrayList<>();
		daily_tick_bar = new ArrayList<>();
		
		for (List<? extends Tick> tickdata : source.getDailyTicks()) {
			
			List<FutureBar> tick_bar_list = tickdata.stream()
					.collect(Collectors.groupingBy(tick -> tick.no / tickNum, new FutureBarCollector(null)))
					.entrySet().stream().sorted((entry1, entry2) -> entry1.getKey().compareTo(entry2.getKey()))
					.map(Entry::getValue).collect(Collectors.toList());

			int total_TickBar_num = tick_bar_list.size();
			
			BarSeries tick_bar_ohlcv = new BarSeries(tick_bar_list);
			daily_tick_bar.add(tick_bar_ohlcv);
			
			int[] Signal = new int[total_TickBar_num];
			daily_signal.add(Signal);

			int i = 0;
			for (i = 0; i < total_TickBar_num; i++) {
				Signal[i] = 0;
			}
			
			APPLIED_PRICE ap = APPLIED_PRICE.PRICE_TYPICAL;
			float[] s_price = ap.simplify(tick_bar_ohlcv);
			
			if (barNum <= total_TickBar_num) {
				for (i = (barNum - 1); i < total_TickBar_num; i++) {
					boolean up = true, down = true;
					for (int j = 0; j < (barNum - 1); j++) {
						if (s_price[i - j] >= s_price[i - j - 1]) {
							down = false;
						}
						if (s_price[i - j] <= s_price[i - j - 1]) {
							up = false;
						}
					}
					if (up) {
						Signal[i] = 1;
					} else if (down) {
						Signal[i] = -1;
					}
				}
			}
		}
	}

	public void day(Portfolio p, int index) {
		int p_state = 0;
		final float volume = 1.0f;
		
		BarSeries ohlcv = daily_tick_bar.get(index);
		float[] Open = ohlcv.opens;
		float[] Close = ohlcv.closes;
		int[] Signal = daily_signal.get(index);
		
		for (int i = 1; i < Open.length; i++ ) {
			switch (Signal[i - 1]) {
			case -1:
				if (p_state >= 0) {
					p.closeLong(Open[i], p.getLongVolume());
					// p_state = 0;
					p.openShort(Open[i], volume);
					p_state = -1;
				}
				break;
			case 1:
				if (p_state <= 0) {
					p.closeShort(Open[i], p.getShortVolume());
					// p_state = 0;
					p.openLong(Open[i], volume);
					p_state = 1;
				}
				break;
			case 0:
			default:
				break;
			}
		}
		
		if (p.getLongVolume() > 0.0f) {
			p.closeLong(Close[Close.length - 1], p.getLongVolume());
		}
		if (p.getShortVolume() > 0.0f) {
			p.closeShort(Close[Close.length - 1], p.getShortVolume());
		}
	}
}

package strategy;

import indicator.Price_Channel;
import strategy.template.BarBasedStrategy;
import strategy.template.IEveryBar;

public class ChannelBreakStrategy extends BarBasedStrategy implements IEveryBar {
	private final int period;
	private final Price_Channel priceChannel;

	protected float[] high_price_buffer = null;
	protected float[] low_price_buffer = null;

	public ChannelBreakStrategy(Integer Period) {
		this.period = Period;
		this.priceChannel = new Price_Channel(period);
		this.indicators.add(priceChannel);
	}

	@Override
	protected void calculateIndicators() {
		super.calculateIndicators();
		high_price_buffer = priceChannel.getBufferById(0);
		low_price_buffer = priceChannel.getBufferById(1);
	}

	@Override
	public float onNewBar() {
		if (current_index < period) {
			position = 0;
		} else if (Close[current_index - 1] > high_price_buffer[current_index - 2]) {
			position = 1;
		} else if (Close[current_index - 1] < low_price_buffer[current_index - 2]) {
			position = -1;
		}
		return Open[current_index];
	}
}

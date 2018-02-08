package strategy;

import indicator.APPLIED_PRICE;
import indicator.MA;
import strategy.template.BarBasedStrategy;
import strategy.template.IEveryBar;

public class MABreakStrategy extends BarBasedStrategy implements IEveryBar {
	protected final int period;
	private final String ma_method;
	private final APPLIED_PRICE applied_price;
	private final MA ma;

	protected float[] ma_buffer = null;

	public MABreakStrategy(Integer Period, String ma_method, APPLIED_PRICE applied_price) {
		this.period = Period;
		this.ma_method = ma_method;
		this.applied_price = applied_price;
		this.ma = new MA(this.period, this.ma_method, this.applied_price);
	}

	public MABreakStrategy(Integer Period, String ma_method) {
		this(Period, ma_method, APPLIED_PRICE.PRICE_CLOSE);
	}

	public MABreakStrategy(Integer Period) {
		this(Period, MA.MODE_SMA, APPLIED_PRICE.PRICE_CLOSE);
	}

	@Override
	protected void calculateIndicators() {
		super.calculateIndicators();
		ma_buffer = ma.getBufferById(0);
	}

	@Override
	public float onNewBar() {
		if (current_index < period) {
			position = 0;
		} else if (Close[current_index - 1] > ma_buffer[current_index - 1]) {
			position = 1;
		} else if (Close[current_index - 1] < ma_buffer[current_index - 1]) {
			position = -1;
		}
		return Open[current_index];
	}
}

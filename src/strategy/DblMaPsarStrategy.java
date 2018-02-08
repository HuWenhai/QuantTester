package strategy;

import data.struct.BarSeries;
import indicator.APPLIED_PRICE;
import indicator.MA;
import indicator.ParabolicSAR;
import strategy.template.BarBasedStrategy;
import strategy.template.IEveryOHLC;

public class DblMaPsarStrategy extends BarBasedStrategy implements IEveryOHLC {
	
	private final int period;
	private final MA fast_ma;
	private final MA slow_ma;
	private final ParabolicSAR psar;

	private float[] ma_fast_buffer = null;
	private float[] ma_slow_buffer = null;
	private float[] psar_buffer = null;

	public DblMaPsarStrategy(Integer fastPeriod, Integer slowPeriod, String ma_method, APPLIED_PRICE applied_price, Float SARStep, Float SARMaximum) {
		this.period = slowPeriod;
		this.fast_ma = new MA(fastPeriod, ma_method, applied_price);
		this.slow_ma = new MA(slowPeriod, ma_method, applied_price);
		this.psar = new ParabolicSAR(SARStep, SARMaximum);
	}
	
	@Override
	protected void calculateIndicators() {
		super.calculateIndicators();
		ma_fast_buffer = fast_ma.getBufferById(0);
		ma_slow_buffer = slow_ma.getBufferById(0);
		psar_buffer = psar.getBufferById(0);
	}

	private int[] ma_signal = null;
	private int[] psar_state = null;

	@Override
	public void setBarSeries(BarSeries barseries) {
		super.setBarSeries(barseries);

		final int bars = Time.length;
		ma_signal = new int[bars];
		for (int i = 0; i < period; i++) {
			ma_signal[0] = 0;
		}
		for (int i = period; i < bars; i++) {
			if (ma_fast_buffer[i - 1] > ma_slow_buffer[i - 1]) {
				ma_signal[i] = 1;
			} else if (ma_fast_buffer[i - 1] < ma_slow_buffer[i - 1]) {
				ma_signal[i] = -1;
			} else {
				ma_signal[i] = 0;
			}
		}
		
		int state = 0;
		for (int i = 0; i < bars; i++) {
			if (ma_signal[i] == 1 && state != 1) {
				ma_signal[i] = 1;
				state = 1;
			} else if (ma_signal[i] == -1 && state != -1) {
				ma_signal[i] = -1;
				state = -1;
			} else {
				ma_signal[i] = 0;
			}
		}

		psar_state = new int[bars];
		psar_state[0] = 0;
		for (int i = 1; i < bars; i++) {
			if (Low[i - 1] > psar_buffer[i - 1]) {
				psar_state[i] = 1;
			} else if (High[i - 1] < psar_buffer[i - 1]) {
				psar_state[i] = -1;
			} else {
				psar_state[i] = 0;
			}
		}
	}
	
	private float sl_price = 0.0f;
	
	@Override
	public float onOpen() {
		final float open_price = Open[current_index];

		if (ma_signal[current_index] == 1) {
			if (psar_state[current_index] == 1) {
				position = 1;
			}
		} else if (ma_signal[current_index] == -1) {
			if (psar_state[current_index] == -1) {
				position = -1;
			}
		}

		if (position == 1) {
			if (ma_signal[current_index] == -1 || psar_state[current_index] <= 0) {
				position = 0;
				sl_price = 0.0f;
			} else {
				sl_price = psar_buffer[current_index - 1];
			}
		} else if (position == -1) {
			if (ma_signal[current_index] == 1 || psar_state[current_index] >= 0) {
				position = 0;
				sl_price = 0.0f;
			} else {
				sl_price = psar_buffer[current_index - 1];
			}
		}
		
		return open_price;
	}

	@Override
	public float onHigh() {
		float high_price = High[current_index];
		if (position == -1) {
			if (high_price >= sl_price) {
				position = 0;
				return sl_price;
			}
		}
		return high_price;
	}

	@Override
	public float onLow() {
		float low_price = Low[current_index];
		if (position == 1) {
			if (low_price <= sl_price) {
				position = 0;
				return sl_price;
			}
		}
		return low_price;
	}

	@Override
	public float onClose() {
		return Close[current_index];
	}

}

package strategy;

import data.struct.BarSeries;
import indicator.APPLIED_PRICE;
import indicator.MA;
import indicator.ParabolicSAR;
import strategy.template.BarBasedStrategy;
import strategy.template.IEveryOHLC;

public class MaPsarStrategy extends BarBasedStrategy implements IEveryOHLC {
	
	private final int period;
	private final MA ma;
	private final ParabolicSAR psar;

	private float[] ma_buffer = null;
	private float[] psar_buffer = null;

	public MaPsarStrategy(Integer Period, String ma_method, APPLIED_PRICE applied_price, Float SARStep, Float SARMaximum) {
		this.period = Period;
		this.ma = new MA(Period, ma_method, applied_price);
		this.psar = new ParabolicSAR(SARStep, SARMaximum);
	}
	
	@Override
	protected void calculateIndicators() {
		super.calculateIndicators();
		ma_buffer = ma.getBufferById(0);
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
			if (Close[i - 1] > ma_buffer[i - 1]) {
				ma_signal[i] = 1;
			} else if (Close[i - 1] < ma_buffer[i - 1]) {
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
	
	private float m_sl = 0.0f;
	
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
				m_sl = 0.0f;
			} else {
				m_sl = psar_buffer[current_index - 1];
			}
		} else if (position == -1) {
			if (ma_signal[current_index] == 1 || psar_state[current_index] >= 0) {
				position = 0;
				m_sl = 0.0f;
			} else {
				m_sl = psar_buffer[current_index - 1];
			}
		}
		
		return open_price;
	}

	@Override
	public float onHigh() {
		float high_price = High[current_index];
		if (position == -1) {
			if (high_price >= m_sl) {
				position = 0;
				return m_sl;
			}
		}
		return high_price;
	}

	@Override
	public float onLow() {
		float low_price = Low[current_index];
		if (position == 1) {
			if (low_price <= m_sl) {
				position = 0;
				return m_sl;
			}
		}
		return low_price;
	}

	@Override
	public float onClose() {
		return Close[current_index];
	}

}

package indicator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import indicator.IndicatorBuffer.DrawingStyle;
import indicator.IndicatorBuffer.DrawingType;

public class CCI implements IIndicator {

	private final int ExtCCIPeriod;
	private final IPriceSimplifier applied_price;

	public CCI(int period, IPriceSimplifier applied_price) {
		if (period < 1) {
			this.ExtCCIPeriod = 14;
		} else {
			this.ExtCCIPeriod = period;
		}
		this.applied_price = applied_price;
	}

	public CCI(int period) {
		this(period, APPLIED_PRICE.PRICE_TYPICAL);
	}

	public CCI() {
		this(14, APPLIED_PRICE.PRICE_TYPICAL);
	}

	private float[] ExtCCIBuffer = null;
	private float[] ExtDBuffer = null;
	private float[] ExtMBuffer = null;
	private float[] ExtSPBuffer = null;

	@Override
	public void calculate(float[] open, float[] high, float[] low, float[] close) {
		float[] price = applied_price.simplify(open, high, low, close);
		ExtCCIBuffer = new float[price.length];
		ExtDBuffer = new float[price.length];
		ExtMBuffer = new float[price.length];
		ExtSPBuffer = new float[price.length];
		this.calculateForAppliedPrice(price);
	}

	// TODO create MA_helper
	private static float SimpleMA(final int position, final int period, final float[] price) {
		// ---
		float result = 0.0f;
		// --- check position
		if (position >= period - 1 && period > 0) {
			// --- calculate value
			for (int i = 0; i < period; i++)
				result += price[position - i];
			result /= period;
		}
		// ---
		return (result);
	}

	private void calculateForAppliedPrice(final float[] price) {
		int rates_total = price.length;
		int prev_calculated = 0;
		// --- variables
		int i, j;
		double dTmp, dMul = 0.015 / ExtCCIPeriod;
		// --- start calculation
		int StartCalcPosition = ExtCCIPeriod - 1;
		// --- check for bars count
		if (rates_total < StartCalcPosition)
			return;
		// --- calculate position
		int pos = prev_calculated - 1;
		if (pos < StartCalcPosition)
			pos = StartCalcPosition;
		// --- main cycle
		for (i = pos; i < rates_total; i++) {
			// --- SMA on price buffer
			ExtSPBuffer[i] = SimpleMA(i, ExtCCIPeriod, price);
			// --- calculate D
			dTmp = 0.0;
			for (j = 0; j < ExtCCIPeriod; j++)
				dTmp += Math.abs(price[i - j] - ExtSPBuffer[i]);
			ExtDBuffer[i] = (float) (dTmp * dMul);
			// --- calculate M
			ExtMBuffer[i] = price[i] - ExtSPBuffer[i];
			// --- calculate CCI
			if (ExtDBuffer[i] != 0.0)
				ExtCCIBuffer[i] = ExtMBuffer[i] / ExtDBuffer[i];
			else
				ExtCCIBuffer[i] = 0.0f;
			// ---
		}
	}

	@Override
	public float[] getBufferById(int id) {
		switch (id) {
		case 0:
			return ExtCCIBuffer;
		case 1:
			return ExtDBuffer;
		case 2:
			return ExtMBuffer;
		case 3:
			return ExtSPBuffer;
		default:
			return null;
		}
	}

	@Override
	public int minimumBarsToWork() {
		return ExtCCIPeriod;
	}

	@Override
	public List<IndicatorBuffer> getIndicatorBuffers() {
		List<IndicatorBuffer> buffers = new ArrayList<>();
		buffers.add(new IndicatorBuffer("RSI", DrawingType.SeparateChart, DrawingStyle.Line, Color.CYAN, ExtCCIBuffer, ExtCCIPeriod - 1));
		buffers.add(new IndicatorBuffer("D", ExtDBuffer));
		buffers.add(new IndicatorBuffer("M", ExtMBuffer));
		buffers.add(new IndicatorBuffer("SP", ExtMBuffer));
		return buffers;
	}
}

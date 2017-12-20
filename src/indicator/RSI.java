package indicator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import indicator.IndicatorBuffer.DrawingStyle;
import indicator.IndicatorBuffer.DrawingType;

public class RSI implements IIndicator {

	private final int ExtPeriodRSI;
	private final IPriceSimplifier applied_price;

	public RSI(int period, IPriceSimplifier applied_price) {
		if (period < 1) {
			this.ExtPeriodRSI = 12;
		} else {
			this.ExtPeriodRSI = period;
		}
		this.applied_price = applied_price;
	}

	public RSI(int period) {
		this(period, APPLIED_PRICE.PRICE_CLOSE);
	}

	public RSI() {
		this(12, APPLIED_PRICE.PRICE_CLOSE);
	}

	private float[] ExtRSIBuffer = null;
	private float[] ExtPosBuffer = null;
	private float[] ExtNegBuffer = null;

	@Override
	public void calculate(float[] open, float[] high, float[] low, float[] close) {
		float[] price = applied_price.simplify(open, high, low, close);
		ExtRSIBuffer = new float[price.length];
		ExtPosBuffer = new float[price.length];
		ExtNegBuffer = new float[price.length];
		this.calculateForAppliedPrice(price);
	}

	private void calculateForAppliedPrice(final float[] price) {
		int i;
		float diff;

		int rates_total = price.length;
		int prev_calculated = 0;
		// --- check for rates count
		if (rates_total <= ExtPeriodRSI)
			return;
		// --- preliminary calculations
		int pos = prev_calculated - 1;
		if (pos <= ExtPeriodRSI) {
			// --- first RSIPeriod values of the indicator are not calculated
			ExtRSIBuffer[0] = 0.0f;
			ExtPosBuffer[0] = 0.0f;
			ExtNegBuffer[0] = 0.0f;
			float SumP = 0.0f;
			float SumN = 0.0f;
			for (i = 1; i <= ExtPeriodRSI; i++) {
				ExtRSIBuffer[i] = 0.0f;
				ExtPosBuffer[i] = 0.0f;
				ExtNegBuffer[i] = 0.0f;
				diff = price[i] - price[i - 1];
				SumP += (diff > 0 ? diff : 0);
				SumN += (diff < 0 ? -diff : 0);
			}
			// --- calculate first visible value
			ExtPosBuffer[ExtPeriodRSI] = SumP / ExtPeriodRSI;
			ExtNegBuffer[ExtPeriodRSI] = SumN / ExtPeriodRSI;
			if (ExtNegBuffer[ExtPeriodRSI] != 0.0)
				ExtRSIBuffer[ExtPeriodRSI] = 100.0f
						- (100.0f / (1.0f + ExtPosBuffer[ExtPeriodRSI] / ExtNegBuffer[ExtPeriodRSI]));
			else {
				if (ExtPosBuffer[ExtPeriodRSI] != 0.0f)
					ExtRSIBuffer[ExtPeriodRSI] = 100.0f;
				else
					ExtRSIBuffer[ExtPeriodRSI] = 50.0f;
			}
			// --- prepare the position value for main calculation
			pos = ExtPeriodRSI + 1;
		}
		// --- the main loop of calculations
		for (i = pos; i < rates_total; i++) {
			diff = price[i] - price[i - 1];
			ExtPosBuffer[i] = (ExtPosBuffer[i - 1] * (ExtPeriodRSI - 1) + (diff > 0.0f ?  diff : 0.0f)) / ExtPeriodRSI;
			ExtNegBuffer[i] = (ExtNegBuffer[i - 1] * (ExtPeriodRSI - 1) + (diff < 0.0f ? -diff : 0.0f)) / ExtPeriodRSI;
			if (ExtNegBuffer[i] != 0.0f)
				ExtRSIBuffer[i] = 100.0f - 100.0f / (1 + ExtPosBuffer[i] / ExtNegBuffer[i]);
			else {
				if (ExtPosBuffer[i] != 0.0f)
					ExtRSIBuffer[i] = 100.0f;
				else
					ExtRSIBuffer[i] = 50.0f;
			}
		}
	}

	@Override
	public float[] getBufferById(int id) {
		switch (id) {
		case 0:
			return ExtRSIBuffer;
		case 1:
			return ExtPosBuffer;
		case 2:
			return ExtNegBuffer;
		default:
			return null;
		}
	}

	@Override
	public int minimumBarsToWork() {
		return ExtPeriodRSI + 1;
	}

	@Override
	public List<IndicatorBuffer> getIndicatorBuffers() {
		List<IndicatorBuffer> buffers = new ArrayList<>();
		buffers.add(new IndicatorBuffer("RSI", DrawingType.SeparateChart, DrawingStyle.Line, Color.BLUE, ExtRSIBuffer, ExtPeriodRSI));
		buffers.add(new IndicatorBuffer("Pos", ExtPosBuffer));
		buffers.add(new IndicatorBuffer("Neg", ExtNegBuffer));
		return buffers;
	}
}

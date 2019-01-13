package indicator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import helper.Mql5Compatible;
import indicator.IndicatorBuffer.DrawingStyle;
import indicator.IndicatorBuffer.DrawingType;

public class AMA implements IIndicator, Mql5Compatible {

	private final float ExtFastSC;
	private final float ExtSlowSC;
	private final int ExtPeriodAMA;
	private final int ExtSlowPeriodEMA;
	private final int ExtFastPeriodEMA;
	private final IPriceSimplifier applied_price;

	float ExtAMABuffer[];

	public AMA(int period, int fastPeriod, int slowPeriod, int shift, IPriceSimplifier appied_price) {
		if (period <= 0) {
			ExtPeriodAMA = 10;
		} else {
			ExtPeriodAMA = period;
		}
		if (slowPeriod <= 0) {
			ExtSlowPeriodEMA = 30;
		} else {
			ExtSlowPeriodEMA = slowPeriod;
		}
		if (fastPeriod <= 0) {
			ExtFastPeriodEMA = 2;
		} else {
			ExtFastPeriodEMA = fastPeriod;
		}
		this.applied_price = appied_price;
		ExtFastSC = 2.0f / (ExtFastPeriodEMA + 1.0f);
		ExtSlowSC = 2.0f / (ExtSlowPeriodEMA + 1.0f);
	}

	public AMA() {
		this(10, 2, 30, 0, APPLIED_PRICE.PRICE_CLOSE);
	}

	@Override
	public void calculate(float[] open, float[] high, float[] low, float[] close) {
		float[] price = applied_price.simplify(open, high, low, close);
		ExtAMABuffer = new float[price.length];
		this.calculateForAppliedPrice(price);
	}

	float CalculateER(int nPosition, float PriceData[]) {
		float dSignal = fabs(PriceData[nPosition] - PriceData[nPosition - ExtPeriodAMA]);
		float dNoise = 0.0f;
		for (int delta = 0; delta < ExtPeriodAMA; delta++)
			dNoise += fabs(PriceData[nPosition - delta] - PriceData[nPosition - delta - 1]);
		if (dNoise != 0.0f)
			return (dSignal / dNoise);
		return 0.0f;
	}

	private void calculateForAppliedPrice(float[] price) {
		int rates_total = price.length;
		int prev_calculated = 0;
		int begin = 0;
		int i;
		// --- check for rates count
		if (rates_total < ExtPeriodAMA + begin)
			return;
		// --- detect position
		int pos = prev_calculated - 1;
		// --- first calculations
		if (pos < ExtPeriodAMA + begin) {
			pos = ExtPeriodAMA + begin;
			for (i = 0; i < pos - 1; i++)
				ExtAMABuffer[i] = 0.0f;
			ExtAMABuffer[pos - 1] = price[pos - 1];
		}
		// --- main cycle
		for (i = pos; i < rates_total && !IsStopped(); i++) {
			// --- calculate SSC
			float dCurrentSSC = (CalculateER(i, price) * (ExtFastSC - ExtSlowSC)) + ExtSlowSC;
			// --- calculate AMA
			float dPrevAMA = ExtAMABuffer[i - 1];
			ExtAMABuffer[i] = dCurrentSSC * dCurrentSSC * (price[i] - dPrevAMA) + dPrevAMA;
		}
	}

	@Override
	public float[] getBufferById(int id) {
		switch (id) {
		case 0:
			return ExtAMABuffer;
		default:
			return null;
		}
	}

	@Override
	public int minimumBarsToWork() {
		return Math.max(ExtPeriodAMA, ExtSlowPeriodEMA);
	}

	@Override
	public List<IndicatorBuffer> getIndicatorBuffers() {
		List<IndicatorBuffer> buffers = new ArrayList<>();
		buffers.add(new IndicatorBuffer("AMA", DrawingType.MainChart, DrawingStyle.Line, Color.RED, ExtAMABuffer,
				minimumBarsToWork() - 1));
		return buffers;
	}
}

package indicator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import indicator.IndicatorBuffer.DrawingStyle;
import indicator.IndicatorBuffer.DrawingType;

public class MA implements IIndicatorOnIndicator {

	@FunctionalInterface
	private static interface MA_METHOD {
		void calculateMA(int rates_total, int prev_calculated, int begin, float price[]);
	}

	// +------------------------------------------------------------------+
	// | simple moving average |
	// +------------------------------------------------------------------+
	void CalculateSimpleMA(int rates_total, int prev_calculated, int begin, float price[]) {
		int i, limit;
		// --- first calculation or number of bars was changed
		if (prev_calculated == 0)// first calculation
		{
			limit = InpMAPeriod + begin;
			// --- set empty value for first limit bars
			for (i = 0; i < limit - 1; i++)
				ExtLineBuffer[i] = 0.0f;
			// --- calculate first visible value
			float firstValue = 0;
			for (i = begin; i < limit; i++)
				firstValue += price[i];
			firstValue /= InpMAPeriod;
			ExtLineBuffer[limit - 1] = firstValue;
		} else
			limit = prev_calculated - 1;
		// --- main loop
		for (i = limit; i < rates_total; i++)
			ExtLineBuffer[i] = ExtLineBuffer[i - 1] + (price[i] - price[i - InpMAPeriod]) / InpMAPeriod;
		// ---
	}

	// +------------------------------------------------------------------+
	// | exponential moving average |
	// +------------------------------------------------------------------+
	void CalculateEMA(int rates_total, int prev_calculated, int begin, float price[]) {
		int i, limit;
		float SmoothFactor = 2.0f / (1.0f + InpMAPeriod);
		// --- first calculation or number of bars was changed
		if (prev_calculated == 0) {
			limit = InpMAPeriod + begin;
			ExtLineBuffer[begin] = price[begin];
			for (i = begin + 1; i < limit; i++)
				ExtLineBuffer[i] = price[i] * SmoothFactor + ExtLineBuffer[i - 1] * (1.0f - SmoothFactor);
		} else
			limit = prev_calculated - 1;
		// --- main loop
		for (i = limit; i < rates_total; i++)
			ExtLineBuffer[i] = price[i] * SmoothFactor + ExtLineBuffer[i - 1] * (1.0f - SmoothFactor);
		// ---
	}

	// +------------------------------------------------------------------+
	// | linear weighted moving average |
	// +------------------------------------------------------------------+
	void CalculateLWMA(int rates_total, int prev_calculated, int begin, float price[]) {
		int i, limit;
		int weightsum = 0;
		float sum;
		// --- first calculation or number of bars was changed
		if (prev_calculated == 0) {
			weightsum = 0;
			limit = InpMAPeriod + begin;
			// --- set empty value for first limit bars
			for (i = 0; i < limit; i++)
				ExtLineBuffer[i] = 0.0f;
			// --- calculate first visible value
			float firstValue = 0;
			for (i = begin; i < limit; i++) {
				int k = i - begin + 1;
				weightsum += k;
				firstValue += k * price[i];
			}
			firstValue /= (float) weightsum;
			ExtLineBuffer[limit - 1] = firstValue;
		} else
			limit = prev_calculated - 1;
		// --- main loop
		for (i = limit; i < rates_total; i++) {
			sum = 0;
			for (int j = 0; j < InpMAPeriod; j++)
				sum += (InpMAPeriod - j) * price[i - j];
			ExtLineBuffer[i] = sum / weightsum;
		}
		// ---
	}

	// +------------------------------------------------------------------+
	// | smoothed moving average |
	// +------------------------------------------------------------------+
	void CalculateSmoothedMA(int rates_total, int prev_calculated, int begin, float price[]) {
		int i, limit;
		// --- first calculation or number of bars was changed
		if (prev_calculated == 0) {
			limit = InpMAPeriod + begin;
			// --- set empty value for first limit bars
			for (i = 0; i < limit - 1; i++)
				ExtLineBuffer[i] = 0.0f;
			// --- calculate first visible value
			float firstValue = 0;
			for (i = begin; i < limit; i++)
				firstValue += price[i];
			firstValue /= InpMAPeriod;
			ExtLineBuffer[limit - 1] = firstValue;
		} else
			limit = prev_calculated - 1;
		// --- main loop
		for (i = limit; i < rates_total; i++)
			ExtLineBuffer[i] = (ExtLineBuffer[i - 1] * (InpMAPeriod - 1) + price[i]) / InpMAPeriod;
		// ---
	}

	public final static String MODE_SMA = "SimpleMA";
	public final static String MODE_EMA = "ExponentialMA";
	public final static String MODE_SMMA = "SmoothedMA";
	public final static String MODE_LWMA = "LinearWeightedMA";

	public final static String[] MA_MODES = { MODE_SMA, MODE_EMA, MODE_SMMA, MODE_LWMA };

	private final int InpMAPeriod;
	private final MA_METHOD InpMAMethod;
	private final IPriceSimplifier applied_price;

	public MA(int period, String ma_method, IPriceSimplifier applied_price) {
		this.InpMAPeriod = period;
		switch (ma_method) {
		case MODE_SMA:
			InpMAMethod = this::CalculateSimpleMA;
			break;
		case MODE_EMA:
			InpMAMethod = this::CalculateEMA;
			break;
		case MODE_SMMA:
			InpMAMethod = this::CalculateSmoothedMA;
			break;
		case MODE_LWMA:
			InpMAMethod = this::CalculateLWMA;
			break;
		default:
			InpMAMethod = this::CalculateSimpleMA;
			break;
		}
		this.applied_price = applied_price;
	}

	public MA(int period, String ma_method) {
		this(period, ma_method, APPLIED_PRICE.PRICE_CLOSE);
	}

	public MA(int period) {
		this(period, MODE_SMA, APPLIED_PRICE.PRICE_CLOSE);
	}

	public MA() {
		this(20, MODE_SMA, APPLIED_PRICE.PRICE_CLOSE);
	}

	private float[] ExtLineBuffer = null;

	@Override
	public float[] getBufferById(int id) {
		switch (id) {
		case 0:
			return ExtLineBuffer;
		default:
			return null;
		}
	}

	@Override
	public void calculate(float[] open, float[] high, float[] low, float[] close) {
		int rates_total = close.length;
		int begin = 0;
		if (rates_total < InpMAPeriod - 1 + begin) {
			return;
		}

		final float[] price = applied_price.simplify(open, high, low, close);
		ExtLineBuffer = new float[rates_total];
		InpMAMethod.calculateMA(rates_total, 0, begin, price);
	}

	@Override
	public void calculate(float[] prev_data, int begin) {
		int rates_total = prev_data.length;
		if (rates_total < InpMAPeriod - 1 + begin) {
			return;
		}

		ExtLineBuffer = new float[rates_total];
		InpMAMethod.calculateMA(rates_total, 0, begin, prev_data);
		for (int i = 0; i < (InpMAPeriod + begin); i++) {
			ExtLineBuffer[i] = 0.0f;
		}
	}

	@Override
	public int minimumBarsToWork() {
		return InpMAPeriod;
	}

	@Override
	public List<IndicatorBuffer> getIndicatorBuffers() {
		List<IndicatorBuffer> buffers = new ArrayList<>();
		buffers.add(new IndicatorBuffer("Line", DrawingType.MainChart, DrawingStyle.Line, Color.RED, ExtLineBuffer, InpMAPeriod - 1));
		return buffers;
	}
}

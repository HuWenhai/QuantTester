package indicator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import helper.Mql5Compatible;
import indicator.IndicatorBuffer.DrawingStyle;
import indicator.IndicatorBuffer.DrawingType;

public class ParabolicSAR implements IIndicator, Mql5Compatible {

	private final float ExtSarStep;
	private final float ExtSarMaximum;

	public ParabolicSAR(float SARStep, float SARMaximum) {
		this.ExtSarStep = SARStep;
		this.ExtSarMaximum = SARMaximum;
	}

	public ParabolicSAR() {
		this(0.02f, 0.2f);
	}

	private int ExtLastRevPos;
	private boolean ExtDirectionLong;

	private float[] ExtSARBuffer = null;
	private float[] ExtEPBuffer = null;
	private float[] ExtAFBuffer = null;

	@Override
	public void calculate(float[] open, float[] high, float[] low, float[] close) {
		int rates_total = high.length;

		ExtSARBuffer = new float[rates_total];
		ExtEPBuffer = new float[rates_total];
		ExtAFBuffer = new float[rates_total];

		int prev_calculated = 0;
		// --- check for minimum rates count
		if (rates_total < 3)
			return;
		// --- detect current position
		int pos = prev_calculated - 1;
		// --- correct position
		if (pos < 1) {
			// --- first pass, set as SHORT
			pos = 1;
			ExtAFBuffer[0] = ExtSarStep;
			ExtAFBuffer[1] = ExtSarStep;
			ExtSARBuffer[0] = high[0];
			ExtLastRevPos = 0;
			ExtDirectionLong = false;
			ExtSARBuffer[1] = GetHigh(pos, ExtLastRevPos, high);
			ExtEPBuffer[0] = low[pos];
			ExtEPBuffer[1] = low[pos];
		}
		// ---main cycle
		for (int i = pos; i < rates_total - 1; i++) {
			// --- check for reverse
			if (ExtDirectionLong) {
				if (ExtSARBuffer[i] > low[i]) {
					// --- switch to SHORT
					ExtDirectionLong = false;
					ExtSARBuffer[i] = GetHigh(i, ExtLastRevPos, high);
					ExtEPBuffer[i] = low[i];
					ExtLastRevPos = i;
					ExtAFBuffer[i] = ExtSarStep;
				}
			} else {
				if (ExtSARBuffer[i] < high[i]) {
					// --- switch to LONG
					ExtDirectionLong = true;
					ExtSARBuffer[i] = GetLow(i, ExtLastRevPos, low);
					ExtEPBuffer[i] = high[i];
					ExtLastRevPos = i;
					ExtAFBuffer[i] = ExtSarStep;
				}
			}
			// --- continue calculations
			if (ExtDirectionLong) {
				// --- check for new High
				if (high[i] > ExtEPBuffer[i - 1] && i != ExtLastRevPos) {
					ExtEPBuffer[i] = high[i];
					ExtAFBuffer[i] = ExtAFBuffer[i - 1] + ExtSarStep;
					if (ExtAFBuffer[i] > ExtSarMaximum)
						ExtAFBuffer[i] = ExtSarMaximum;
				} else {
					// --- when we haven't reversed
					if (i != ExtLastRevPos) {
						ExtAFBuffer[i] = ExtAFBuffer[i - 1];
						ExtEPBuffer[i] = ExtEPBuffer[i - 1];
					}
				}
				// --- calculate SAR for tomorrow
				ExtSARBuffer[i + 1] = ExtSARBuffer[i] + ExtAFBuffer[i] * (ExtEPBuffer[i] - ExtSARBuffer[i]);
				// --- check for SAR
				if (ExtSARBuffer[i + 1] > low[i] || ExtSARBuffer[i + 1] > low[i - 1])
					ExtSARBuffer[i + 1] = MathMin(low[i], low[i - 1]);
			} else {
				// --- check for new Low
				if (low[i] < ExtEPBuffer[i - 1] && i != ExtLastRevPos) {
					ExtEPBuffer[i] = low[i];
					ExtAFBuffer[i] = ExtAFBuffer[i - 1] + ExtSarStep;
					if (ExtAFBuffer[i] > ExtSarMaximum)
						ExtAFBuffer[i] = ExtSarMaximum;
				} else {
					// --- when we haven't reversed
					if (i != ExtLastRevPos) {
						ExtAFBuffer[i] = ExtAFBuffer[i - 1];
						ExtEPBuffer[i] = ExtEPBuffer[i - 1];
					}
				}
				// --- calculate SAR for tomorrow
				ExtSARBuffer[i + 1] = ExtSARBuffer[i] + ExtAFBuffer[i] * (ExtEPBuffer[i] - ExtSARBuffer[i]);
				// --- check for SAR
				if (ExtSARBuffer[i + 1] < high[i] || ExtSARBuffer[i + 1] < high[i - 1])
					ExtSARBuffer[i + 1] = MathMax(high[i], high[i - 1]);
			}
		}
	}

	// +------------------------------------------------------------------+
	// | Find highest price from start to current position |
	// +------------------------------------------------------------------+
	static float GetHigh(int nPosition, int nStartPeriod, float HiData[]) {
		// --- calculate
		float result = HiData[nStartPeriod];
		for (int i = nStartPeriod; i <= nPosition; i++)
			if (result < HiData[i])
				result = HiData[i];
		return (result);
	}

	// +------------------------------------------------------------------+
	// | Find lowest price from start to current position |
	// +------------------------------------------------------------------+
	static float GetLow(int nPosition, int nStartPeriod, float LoData[]) {
		// --- calculate
		float result = LoData[nStartPeriod];
		for (int i = nStartPeriod; i <= nPosition; i++)
			if (result > LoData[i])
				result = LoData[i];
		return (result);
	}
	// +------------------------------------------------------------------+

	@Override
	public float[] getBufferById(int id) {
		switch (id) {
		case 0:
			return ExtSARBuffer;
		case 1:
			return ExtEPBuffer;
		case 2:
			return ExtAFBuffer;
		default:
			return null;
		}
	}

	@Override
	public int minimumBarsToWork() {
		return 1;
	}

	@Override
	public List<IndicatorBuffer> getIndicatorBuffers() {
		List<IndicatorBuffer> buffers = new ArrayList<>();
		buffers.add(new IndicatorBuffer("SAR", DrawingType.MainChart, DrawingStyle.Dot, Color.ORANGE, ExtSARBuffer, 0));
		buffers.add(new IndicatorBuffer("EP", ExtEPBuffer));
		buffers.add(new IndicatorBuffer("AF", ExtAFBuffer));
		return buffers;
	}
}

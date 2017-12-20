package indicator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import indicator.IndicatorBuffer.DrawingStyle;
import indicator.IndicatorBuffer.DrawingType;

public class Stochastic implements IIndicator {

	private final int InpKPeriod;
	private final int InpDPeriod;
	private final int InpSlowing;

	public Stochastic(int KPeriod, int DPeriod, int Slowing) {
		this.InpKPeriod = KPeriod;
		this.InpDPeriod = DPeriod;
		this.InpSlowing = Slowing;
	}

	public Stochastic() {
		this(5, 3, 3);
	}

	private float[] ExtMainBuffer = null;
	private float[] ExtSignalBuffer = null;
	private float[] ExtHighesBuffer = null;
	private float[] ExtLowesBuffer = null;

	@Override
	public void calculate(float[] open, float[] high, float[] low, float[] close) {
		int rates_total = close.length;

		ExtMainBuffer = new float[rates_total];
		ExtSignalBuffer = new float[rates_total];
		ExtHighesBuffer = new float[rates_total];
		ExtLowesBuffer = new float[rates_total];

		int prev_calculated = 0;
		int i, k, start;
		// --- check for bars count
		if (rates_total <= InpKPeriod + InpDPeriod + InpSlowing)
			return;
		// ---
		start = InpKPeriod - 1;
		if (start + 1 < prev_calculated)
			start = prev_calculated - 2;
		else {
			for (i = 0; i < start; i++) {
				ExtLowesBuffer[i] = 0.0f;
				ExtHighesBuffer[i] = 0.0f;
			}
		}
		// --- calculate HighesBuffer[] and ExtHighesBuffer[]
		for (i = start; i < rates_total; i++) {
			float dmin = 100_0000.0f;
			float dmax = -100_0000.0f;
			for (k = i - InpKPeriod + 1; k <= i; k++) {
				if (dmin > low[k])
					dmin = low[k];
				if (dmax < high[k])
					dmax = high[k];
			}
			ExtLowesBuffer[i] = dmin;
			ExtHighesBuffer[i] = dmax;
		}
		// --- %K
		start = InpKPeriod - 1 + InpSlowing - 1;
		if (start + 1 < prev_calculated)
			start = prev_calculated - 2;
		else {
			for (i = 0; i < start; i++)
				ExtMainBuffer[i] = 0.0f;
		}
		// --- main cycle
		for (i = start; i < rates_total; i++) {
			float sumlow = 0.0f;
			float sumhigh = 0.0f;
			for (k = (i - InpSlowing + 1); k <= i; k++) {
				sumlow += (close[k] - ExtLowesBuffer[k]);
				sumhigh += (ExtHighesBuffer[k] - ExtLowesBuffer[k]);
			}
			if (sumhigh == 0.0f)
				ExtMainBuffer[i] = 100.0f;
			else
				ExtMainBuffer[i] = sumlow / sumhigh * 100.0f;
		}
		// --- signal
		start = InpDPeriod - 1;
		if (start + 1 < prev_calculated)
			start = prev_calculated - 2;
		else {
			for (i = 0; i < start; i++)
				ExtSignalBuffer[i] = 0.0f;
		}
		for (i = start; i < rates_total; i++) {
			float sum = 0.0f;
			for (k = 0; k < InpDPeriod; k++)
				sum += ExtMainBuffer[i - k];
			ExtSignalBuffer[i] = sum / InpDPeriod;
		}
	}

	@Override
	public float[] getBufferById(int id) {
		switch (id) {
		case 0:
			return ExtMainBuffer;
		case 1:
			return ExtSignalBuffer;
		case 2:
			return ExtHighesBuffer;
		case 3:
			return ExtLowesBuffer;
		default:
			return null;
		}
	}

	@Override
	public int minimumBarsToWork() {
		return Math.max(InpKPeriod + InpSlowing - 2, InpKPeriod + InpDPeriod);
	}

	@Override
	public List<IndicatorBuffer> getIndicatorBuffers() {
		List<IndicatorBuffer> buffers = new ArrayList<>();
		buffers.add(new IndicatorBuffer("Main", DrawingType.SeparateChart, DrawingStyle.Line, Color.WHITE, ExtMainBuffer, InpKPeriod + InpSlowing - 2));
		buffers.add(new IndicatorBuffer("Signal", DrawingType.SeparateChart, DrawingStyle.Line, Color.YELLOW, ExtSignalBuffer, InpKPeriod + InpDPeriod));
		buffers.add(new IndicatorBuffer("Highest", ExtHighesBuffer));
		buffers.add(new IndicatorBuffer("Lowest", ExtLowesBuffer));
		return buffers;
	}
}

package indicator.chaos;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import indicator.APPLIED_PRICE;
import indicator.IIndicator;
import indicator.IndicatorBuffer;
import indicator.MA;
import indicator.IndicatorBuffer.DrawingStyle;
import indicator.IndicatorBuffer.DrawingType;

public class AwesomeOscillator implements IIndicator {

	private final int fastPeriod;
	private final int slowPeriod;

	private float[] aoBuffer = null;
	private float[] fastMABuffer = null;
	private float[] slowMABuffer = null;

	public AwesomeOscillator(int fastPeriod, int slowPeriod) {
		this.fastPeriod = fastPeriod;
		this.slowPeriod = slowPeriod;
	}

	public AwesomeOscillator() {
		this(5, 34);
	}

	@Override
	public void calculate(float[] open, float[] high, float[] low, float[] close) {
		MA fastMA = new MA(fastPeriod, MA.MODE_SMA, APPLIED_PRICE.PRICE_MEDIAN);
		MA slowMA = new MA(slowPeriod, MA.MODE_SMA, APPLIED_PRICE.PRICE_MEDIAN);

		fastMA.calculate(open, high, low, close);
		slowMA.calculate(open, high, low, close);

		final int rates_total = close.length;
		aoBuffer = new float[rates_total];
		int i = 0;
		for (; i < (Math.max(fastPeriod, slowPeriod) - 1); i++) {
			aoBuffer[i] = 0.0f;
		}
		fastMABuffer = fastMA.getBufferById(0);
		slowMABuffer = slowMA.getBufferById(0);

		for (; i < rates_total; i++) {
			aoBuffer[i] = fastMABuffer[i] - slowMABuffer[i];
		}
	}

	@Override
	public float[] getBufferById(int id) {
		switch (id) {
		case 0:
			return aoBuffer;
		case 1:
			return fastMABuffer;
		case 2:
			return slowMABuffer;
		default:
			return null;
		}
	}

	@Override
	public int minimumBarsToWork() {
		return Math.max(fastPeriod, slowPeriod);
	}

	@Override
	public List<IndicatorBuffer> getIndicatorBuffers() {
		List<IndicatorBuffer> buffers = new ArrayList<>();
		buffers.add(new IndicatorBuffer("AO", DrawingType.SeparateChart, DrawingStyle.Bar, null, aoBuffer, slowPeriod - 1) {
			@Override
			public Color getColor(int index) {
				if (index == 0 || buffer[index] > buffer[index - 1]) {
					return Color.GREEN;
				} else {
					return Color.RED;
				}
			}
		});
		buffers.add(new IndicatorBuffer("FastMA", fastMABuffer));
		buffers.add(new IndicatorBuffer("SlowMA", slowMABuffer));
		return buffers;
	}
}

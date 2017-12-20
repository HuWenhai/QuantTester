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

public class Alligator implements IIndicator {

	private final int jawsPeriod;
	private final int jawsShift;
	private final int teethPeriod;
	private final int teethShift;
	private final int lipsPeriod;
	private final int lipsShift;

	private float[] jaws = null;
	private float[] teeth = null;
	private float[] lips = null;

	public Alligator(int jawsPeriod, int jawsShift, int teethPeriod, int teethShift, int lipsPeriod, int lipsShift) {
		this.jawsPeriod = jawsPeriod;
		this.jawsShift = jawsShift;
		this.teethPeriod = teethPeriod;
		this.teethShift = teethShift;
		this.lipsPeriod = lipsPeriod;
		this.lipsShift = lipsShift;
	}

	public Alligator() {
		this(13, 8, 8, 5, 5, 3);
	}

	@Override
	public void calculate(float[] open, float[] high, float[] low, float[] close) {
		MA jawsMA = new MA(jawsPeriod, MA.MODE_SMMA, APPLIED_PRICE.PRICE_MEDIAN);
		MA teethMA = new MA(teethPeriod, MA.MODE_SMMA, APPLIED_PRICE.PRICE_MEDIAN);
		MA lipsMA = new MA(lipsPeriod, MA.MODE_SMMA, APPLIED_PRICE.PRICE_MEDIAN);

		jawsMA.calculate(open, high, low, close);
		teethMA.calculate(open, high, low, close);
		lipsMA.calculate(open, high, low, close);

		final int rates_total = close.length;
		jaws = new float[rates_total];
		teeth = new float[rates_total];
		lips = new float[rates_total];

		for (int i = 0; i < jawsShift; i++) {
			jaws[i] = 0.0f;
		}
		float[] jawTemp = jawsMA.getBufferById(0);
		for (int i = jawsShift; i < rates_total; i++) {
			jaws[i] = jawTemp[i - jawsShift];
		}

		for (int i = 0; i < teethShift; i++) {
			teeth[i] = 0.0f;
		}
		float[] teethTemp = teethMA.getBufferById(0);
		for (int i = teethShift; i < rates_total; i++) {
			teeth[i] = teethTemp[i - teethShift];
		}

		for (int i = 0; i < lipsShift; i++) {
			lips[i] = 0.0f;
		}
		float[] lipsTemp = lipsMA.getBufferById(0);
		for (int i = lipsShift; i < rates_total; i++) {
			lips[i] = lipsTemp[i - lipsShift];
		}
	}

	@Override
	public float[] getBufferById(int id) {
		switch (id) {
		case 0:
			return jaws;
		case 1:
			return teeth;
		case 2:
			return lips;
		default:
			return null;
		}
	}

	@Override
	public int minimumBarsToWork() {
		return Math.max(Math.max(jawsPeriod + jawsShift, teethPeriod + teethShift), lipsPeriod + lipsShift);
	}

	@Override
	public List<IndicatorBuffer> getIndicatorBuffers() {
		List<IndicatorBuffer> buffers = new ArrayList<>();
		buffers.add(new IndicatorBuffer("Jaws", DrawingType.MainChart, DrawingStyle.Line, Color.GREEN, jaws, jawsPeriod + jawsShift - 1));
		buffers.add(new IndicatorBuffer("Teeth", DrawingType.MainChart, DrawingStyle.Line, Color.RED, teeth, teethPeriod + teethShift - 1));
		buffers.add(new IndicatorBuffer("Lips", DrawingType.MainChart, DrawingStyle.Line, Color.BLUE, lips, lipsPeriod + lipsShift - 1));
		return buffers;
	}
}

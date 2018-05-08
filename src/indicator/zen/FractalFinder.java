package indicator.zen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import indicator.IIndicator;
import indicator.IndicatorBuffer;

public class FractalFinder implements IIndicator {

	public List<HighLowLine> adjustedKLines = null;
	public List<Fractal> fractalList = null;
	protected float[] upperBuffer = null;
	protected float[] lowerBuffer = null;

	public FractalFinder() {
	}

	protected static class HighLowLine {
		int originalOrdinal;
		float high;
		float low;
		int fractal;

		HighLowLine(int ordinal, float high, float low) {
			this.originalOrdinal = ordinal;
			this.high = high;
			this.low = low;
			this.fractal = 0;
		}

		int compareTo(HighLowLine other) {
			if (this.high > other.high && this.low > other.low) {
				return 1;
			} else if (this.high < other.high && this.low < other.low) {
				return -1;
			} else {
				return 0;
			}
		}

		public String toString() {
			return getClass().getName() + "[originalOrdinal=" + originalOrdinal + ",high=" + high + ",low=" + "low" + ",fractal=" + fractal + "]";
		}
	}

	private static HighLowLine highhigh(HighLowLine k1, HighLowLine k2) {
		int ordinal = (k1.high > k2.high || k1.low < k2.low) ? k1.originalOrdinal : k2.originalOrdinal;
		return new HighLowLine(ordinal, Math.max(k1.high, k2.high), Math.max(k1.low, k2.low));
	}

	private static HighLowLine lowlow(HighLowLine k1, HighLowLine k2) {
		int ordinal = (k1.high > k2.high || k1.low < k2.low) ? k1.originalOrdinal : k2.originalOrdinal;
		return new HighLowLine(ordinal, Math.min(k1.high, k2.high), Math.min(k1.low, k2.low));
	}

	@Override
	public void calculate(float[] open, float[] high, float[] low, float[] close) {
		final int rates_total = close.length;
		adjustedKLines = new LinkedList<>();
		fractalList = new ArrayList<>();
		upperBuffer = new float[rates_total];
		lowerBuffer = new float[rates_total];
		for (int i = 0; i < rates_total; i++) {
			upperBuffer[i] = Float.NEGATIVE_INFINITY;
			lowerBuffer[i] = Float.NEGATIVE_INFINITY;
		}
		if (rates_total < 3) {
			return;
		}

		for (int i = 0; i < rates_total; i++) {
			adjustedKLines.add(new HighLowLine(i, high[i], low[i]));
		}

		Iterator<HighLowLine> iterator = adjustedKLines.iterator();
		HighLowLine k1 = iterator.next();
		HighLowLine k2 = iterator.next();
		while(k1.compareTo(k2) == 0 && iterator.hasNext()) {
			k1 = k2;
			k2 = iterator.next();
		}
		int direction = 0;
		while(iterator.hasNext()) {
			direction = k2.compareTo(k1);
			HighLowLine k3 = iterator.next();
			while (k2.compareTo(k3) == 0 && iterator.hasNext()) {
				HighLowLine combined = (direction == 1) ? (highhigh(k2, k3)) : (lowlow(k2, k3));
				k2.originalOrdinal = combined.originalOrdinal;
				k2.high = combined.high;
				k2.low = combined.low;
				iterator.remove();
				k3 = iterator.next();
			}

			int k1k2 = k1.compareTo(k2);
			int k2k3 = k2.compareTo(k3);
			if (k1k2 == -1 && k2k3 == 1) {
				fractalList.add(new Fractal(true, k1.originalOrdinal, k2.originalOrdinal, high[k2.originalOrdinal], k3.originalOrdinal));
				k2.fractal = 1;
			} else if (k1k2 == 1 && k2k3 == -1) {
				fractalList.add(new Fractal(false, k1.originalOrdinal, k2.originalOrdinal, low[k2.originalOrdinal], k3.originalOrdinal));
				k2.fractal = -1;
			}

			k1 = k2;
			k2 = k3;
		}

		for (HighLowLine kLine : adjustedKLines) {
			if (kLine.fractal == 1) {
				upperBuffer[kLine.originalOrdinal] = high[kLine.originalOrdinal];
			} else if (kLine.fractal == -1) {
				lowerBuffer[kLine.originalOrdinal] = low[kLine.originalOrdinal];
			}
		}
	}

	@Override
	public float[] getBufferById(int id) {
		switch (id) {
		case 0:
			return upperBuffer;
		case 1:
			return lowerBuffer;
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
		buffers.add(new IndicatorBuffer("upper", upperBuffer));
		buffers.add(new IndicatorBuffer("lower", lowerBuffer));
		return buffers;
	}
}

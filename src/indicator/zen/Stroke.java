package indicator.zen;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class Stroke extends Fractal {

	private final boolean strict;
	private final float gapRatio;
	protected List<Integer> strokeList = null;
	protected float[] strokeBuffer = null;

	public Stroke(boolean strict, float gapRatio) {
		this.strict = strict;
		this.gapRatio = gapRatio;
	}

	public Stroke() {
		this(true, 1.0f);
	}

	private boolean checkGap(int endFractal, int start, int end, float[] high, float[] low) {
		final int rates_total = high.length;
		if (end + 2 >= rates_total) {
			return false;
		}

		boolean foundGap = false;
		int gapBegin = 0;
		int gapEnd = 0;
		for (int i = start; i < end; i++) {
			if (endFractal == 1) {
				if (low[i + 1] > (high[i] * (1 + gapRatio))) {
					gapBegin = i;
					gapEnd = i + 1;
				}
			} else if (endFractal == -1) {
				if (high[i + 1] < (low[i] * (1 - gapRatio))) {
					gapBegin = i;
					gapEnd = i + 1;
				}
			}
		}
		if (foundGap) {
			if (endFractal == 1) {
				if (low[gapEnd + 1] > high[gapBegin] && low[gapEnd + 2] > high[gapBegin]) {
					return true;
				}
			} else if (endFractal == -1) {
				if (high[gapEnd + 1] < low[gapBegin] && high[gapEnd + 2] < low[gapBegin]) {
					return true;
				}				
			}
		}
		return false;
	}

	@Override
	public void calculate(float[] open, float[] high, float[] low, float[] close) {
		super.calculate(open, high, low, close);
		final int rates_total = close.length;
		strokeBuffer = new float[rates_total];
		for (int i = 0; i < rates_total; i++) {
			strokeBuffer[i] = Float.NEGATIVE_INFINITY;
		}
		if (rates_total < 6) {
			return;
		}

		ListIterator<HighLowKLine> iterator = adjustedKLines.listIterator();
		HighLowKLine confirmedEP = iterator.next();
		while(confirmedEP.fractal == 0 && iterator.hasNext()) {
			confirmedEP = iterator.next();
		}
		HighLowKLine unconfirmedEP = null;
		int unconfirmedIndex = 0;
		while (iterator.hasNext()) {
			unconfirmedEP = iterator.next();
			while(unconfirmedEP.fractal == 0 && iterator.hasNext()) {
				unconfirmedEP = iterator.next();
			}
			unconfirmedIndex = iterator.nextIndex();
			if (confirmedEP.fractal == unconfirmedEP.fractal) {
				confirmedEP = unconfirmedEP;
				continue;
			} else {
				break;
			}
		}

		strokeList = new LinkedList<>();
		strokeList.add(confirmedEP.originalOrdinal);
		while (iterator.hasNext()) {
			HighLowKLine nextFractal = iterator.next();
			while (nextFractal.fractal == 0 && iterator.hasNext()) {
				nextFractal = iterator.next();
			}
			int nextFractalIndex = iterator.nextIndex();
			if (unconfirmedEP.fractal == nextFractal.fractal) {
				if (nextFractal.fractal == 1 && nextFractal.high > unconfirmedEP.high) {
					unconfirmedEP = nextFractal;
					unconfirmedIndex = nextFractalIndex;
				} else if (nextFractal.fractal == -1 && nextFractal.low < unconfirmedEP.low) {
					unconfirmedEP = nextFractal;
					unconfirmedIndex = nextFractalIndex;
				}
			} else {
				final int kLineCount = nextFractalIndex - unconfirmedIndex + 1;
				boolean strictMode = (kLineCount >= 5);
				boolean unstrictMode = (kLineCount == 4 && !strict);
				boolean gapStroke = false;
				if (!strictMode && !unstrictMode) {
					gapStroke = checkGap(nextFractal.fractal, unconfirmedEP.originalOrdinal, nextFractal.originalOrdinal, high, low);
				}
				if (strictMode || unstrictMode || gapStroke) {
					confirmedEP = unconfirmedEP;
					unconfirmedEP = nextFractal;
					unconfirmedIndex = nextFractalIndex;
					strokeList.add(confirmedEP.originalOrdinal);
				}
			}
		}

		for (Integer ordinal : strokeList) {
			strokeBuffer[ordinal] = (upperBuffer[ordinal] == Float.NEGATIVE_INFINITY) ? lowerBuffer[ordinal] : upperBuffer[ordinal];
		}
	}

	@Override
	public float[] getBufferById(int id) {
		switch (id) {
		case 0:
			return upperBuffer;
		case 1:
			return lowerBuffer;
		case 2:
			return strokeBuffer;
		default:
			return null;
		}
	}
}

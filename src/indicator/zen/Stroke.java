package indicator.zen;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

public class Stroke extends Fractal {

	private final boolean strict;
	private final float gapRatio;
	protected List<Integer> strokeList = null;
	protected List<Float> strokeBufferList = null;
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
		strokeList = new ArrayList<>();
		strokeBufferList = new ArrayList<>();

		final int adjustedKLineLen = adjustedKLines.size();
		if (adjustedKLineLen < 4) {
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

		strokeList.add(confirmedEP.originalOrdinal);
		float firstFailFractal = Float.NEGATIVE_INFINITY;
		while (iterator.hasNext()) {
			HighLowKLine nextFractal = iterator.next();
			while (nextFractal.fractal == 0 && iterator.hasNext()) {
				nextFractal = iterator.next();
			}
			int nextFractalIndex = iterator.nextIndex();
			if (unconfirmedEP.fractal == nextFractal.fractal) {
				boolean foundHigher = (nextFractal.fractal == 1 && nextFractal.high > unconfirmedEP.high);
				boolean foundLower = (nextFractal.fractal == -1 && nextFractal.low < unconfirmedEP.low);
				if (foundHigher || foundLower) {
					unconfirmedEP = nextFractal;
					unconfirmedIndex = nextFractalIndex;
					firstFailFractal = Float.NEGATIVE_INFINITY;
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
					if ((firstFailFractal == Float.NEGATIVE_INFINITY) || (firstFailFractal != Float.NEGATIVE_INFINITY && 
							((nextFractal.fractal == 1 && nextFractal.high > firstFailFractal) || 
								(nextFractal.fractal == -1 && nextFractal.low < firstFailFractal)))) {
							confirmedEP = unconfirmedEP;
							unconfirmedEP = nextFractal;
							unconfirmedIndex = nextFractalIndex;
							strokeList.add(confirmedEP.originalOrdinal);
							firstFailFractal = Float.NEGATIVE_INFINITY;
					}
				} else if (firstFailFractal == Float.NEGATIVE_INFINITY && kLineCount == 4 && strict) {
					// Do nothing
				} else if (firstFailFractal == Float.NEGATIVE_INFINITY && kLineCount < 4 && strict) {
					firstFailFractal = (nextFractal.fractal == 1) ? nextFractal.high : nextFractal.low;
				}
			}
		}

		strokeBufferList = strokeList.stream()
				.map((ordinal) -> { return ((upperBuffer[ordinal] == Float.NEGATIVE_INFINITY) ? lowerBuffer[ordinal] : upperBuffer[ordinal]);} )
				.collect(Collectors.toList());

		final int srokeBufferListSize = strokeBufferList.size();
		for (int i = 0; i < srokeBufferListSize; i++) {
			strokeBuffer[strokeList.get(i)] = strokeBufferList.get(i);
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

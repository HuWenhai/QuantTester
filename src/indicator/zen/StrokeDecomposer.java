package indicator.zen;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import indicator.zen.FractalFinder.HighLowLine;

class StrokeDecomposer {

	private final boolean strict;
	private final int minimumBarsForStrokeConfirm;
	private final float gapRatio;
	public List<Stroke> strokeList = null;
	public List<Fractal> confirmList = null;

	public StrokeDecomposer(boolean strict, float gapRatio) {
		this.strict = strict;
		this.minimumBarsForStrokeConfirm = this.strict ? 5 : 4;
		this.gapRatio = gapRatio;
	}

	public StrokeDecomposer() {
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

	private static Fractal getFractalbyIndex(List<Fractal> fractalList, int originalOrdinal) {
		Fractal ret = null;
		for (Fractal fractal : fractalList) {
			if (fractal.peakIdx == originalOrdinal) {
				ret = fractal;
				break;
			}
		}
		if (ret == null) {
			System.out.println("ERROR!!! ret = null!!!");
		}
		return ret;
	}

	public List<Stroke> calculate(List<HighLowLine> adjustedKLines, List<Fractal> fractalList, float[] high, float[] low) {
		int lastEndPoint = 0;
		strokeList = new ArrayList<>();
		confirmList = new ArrayList<>();

		final int adjustedKLineLen = adjustedKLines.size();
		if (adjustedKLineLen < 4) {
			return null;
		}

		ListIterator<HighLowLine> iterator = adjustedKLines.listIterator();
		HighLowLine confirmedEP = iterator.next();
		while (confirmedEP.fractal == 0 && iterator.hasNext()) {
			confirmedEP = iterator.next();
		}
		HighLowLine unconfirmedEP = null;
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

		lastEndPoint = confirmedEP.originalOrdinal;
		float firstFailFractal = Float.NEGATIVE_INFINITY;
		while (iterator.hasNext()) {
			HighLowLine nextFractal = iterator.next();
			while (nextFractal.fractal == 0 && iterator.hasNext()) {
				nextFractal = iterator.next();
			}
			if (nextFractal.fractal == 0) {
				// Last HighLowLine but not fractal
				Fractal lastFractal = getFractalbyIndex(fractalList, lastEndPoint);
				lastEndPoint = confirmedEP.originalOrdinal;
				Fractal thisFractal = getFractalbyIndex(fractalList, unconfirmedEP.originalOrdinal); 
				strokeList.add(new Stroke(lastFractal, thisFractal));
				confirmList.add(getFractalbyIndex(fractalList, unconfirmedEP.originalOrdinal));
				break;
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
				boolean haveEnoughKLines = (kLineCount >= minimumBarsForStrokeConfirm);
				boolean gapStroke = false;
				if (!haveEnoughKLines) {
					gapStroke = checkGap(nextFractal.fractal, unconfirmedEP.originalOrdinal, nextFractal.originalOrdinal, high, low);
				}
				if (haveEnoughKLines || gapStroke) {
					if ((firstFailFractal == Float.NEGATIVE_INFINITY) || (firstFailFractal != Float.NEGATIVE_INFINITY && 
							((nextFractal.fractal == 1 && nextFractal.high > firstFailFractal) || 
								(nextFractal.fractal == -1 && nextFractal.low < firstFailFractal)))) {
							confirmedEP = unconfirmedEP;
							unconfirmedEP = nextFractal;
							unconfirmedIndex = nextFractalIndex;
							Fractal lastFractal = getFractalbyIndex(fractalList, lastEndPoint);
							lastEndPoint = confirmedEP.originalOrdinal;
							Fractal thisFractal = getFractalbyIndex(fractalList, confirmedEP.originalOrdinal); 
							strokeList.add(new Stroke(lastFractal, thisFractal));
							confirmList.add(getFractalbyIndex(fractalList, nextFractal.originalOrdinal));
							firstFailFractal = Float.NEGATIVE_INFINITY;
					}
				} else if (firstFailFractal == Float.NEGATIVE_INFINITY && kLineCount == 4 && strict) {
					// Do nothing
				} else if (firstFailFractal == Float.NEGATIVE_INFINITY && kLineCount < 4 && strict) {
					firstFailFractal = (nextFractal.fractal == 1) ? nextFractal.high : nextFractal.low;
				}
			}
		}
		return strokeList;
	}
}

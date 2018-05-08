package indicator.zen;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.IntStream;

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

		for (int i = start; i < end; i++) {
			if (endFractal == 1) {
				if (low[i + 1] > (high[i] * (1 + gapRatio))) {
					if (low[i + 2] > high[i] && low[i + 3] > high[i]) {
						return true;
					}
				}
			} else if (endFractal == -1) {
				if (high[i + 1] < (low[i] * (1 - gapRatio))) {
					if (high[i + 2] < low[i] && high[i + 3] < low[i]) {
						return true;
					}				
				}
			}
		}
		return false;
	}

	/**
	 * Check if there was a backward > 50% before secondary high/low fractal.
	 * 
	 * @param unconfirmedEP last unconfirmed end point
	 * @param firstFailFractal first reverse direction fractal after unconfirmedEP but with only 4 HighLowLine between
	 * @param secondaryFractal next fractal to check
	 * @param high high price buffer
	 * @param low low price buffer
	 * @return true if there was no backward > 50%, false otherwise
	 */
	private static boolean checkBackward(HighLowLine unconfirmedEP, HighLowLine firstFailFractal, HighLowLine secondaryFractal, float[] high, float[] low) {
		assert (unconfirmedEP.fractal == - firstFailFractal.fractal && firstFailFractal.fractal == secondaryFractal.fractal);

		float half;
		if (unconfirmedEP.fractal == 1) {
			half = (unconfirmedEP.high + firstFailFractal.low) / 2.0f;
		} else {
			half = (unconfirmedEP.low + firstFailFractal.high) / 2.0f;
		}
		
		for (int i = firstFailFractal.originalOrdinal + 1; i <= secondaryFractal.originalOrdinal; i++) {
			if (unconfirmedEP.fractal == 1 && high[i] >= half) {
				return false;
			}
			if (unconfirmedEP.fractal == -1 && low[i] <= half) {
				return false;
			}
		}
		return true;
	}

	private static Fractal getFractalbyIndex(List<Fractal> fractalList, int originalOrdinal) {
		Fractal ret = null;
		for (Fractal fractal : fractalList) {
			if (fractal.peakIdx == originalOrdinal) {
				ret = fractal;
				break;
			}
		}
		assert ret != null : "ERROR! ret = null! Can not find fractal at index =" + originalOrdinal;
		return ret;
	}

	private static Stroke createStrokeFromFractal(Fractal startFractal, Fractal endFractal, float[] high, float[] low) {
		int startIdx = startFractal.peakIdx;
		int endIdx = endFractal.peakIdx + 1;
		double maxValue = IntStream.range(startIdx, endIdx).mapToDouble(i -> high[i]).max().getAsDouble();
		double minValue = IntStream.range(startIdx, endIdx).mapToDouble(i -> low[i]).min().getAsDouble();
		return new Stroke(startFractal, endFractal, (float)maxValue, (float)minValue);
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
		HighLowLine firstFailFractal = null;
		boolean allowSecondary = false;
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
				strokeList.add(createStrokeFromFractal(lastFractal, thisFractal, high, low));
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
					firstFailFractal = null;
					allowSecondary = false;
				}
			} else {
				final int kLineCount = nextFractalIndex - unconfirmedIndex + 1;
				boolean haveEnoughKLines = (kLineCount >= minimumBarsForStrokeConfirm);
				boolean gapStroke = false;
				if (!haveEnoughKLines) {
					gapStroke = checkGap(nextFractal.fractal, unconfirmedEP.originalOrdinal, nextFractal.originalOrdinal, high, low);
				}
				if (haveEnoughKLines || gapStroke) {
					boolean firstMatch = (firstFailFractal == null);
					boolean betterMatch = (firstFailFractal != null &&
							((nextFractal.fractal == 1 && nextFractal.high > firstFailFractal.high) ||
							 (nextFractal.fractal == -1 && nextFractal.low < firstFailFractal.low)));
					boolean secondMatch = (firstFailFractal != null && allowSecondary && checkBackward(unconfirmedEP, firstFailFractal, nextFractal, high, low));
					if (firstMatch || betterMatch || secondMatch) {
						confirmedEP = unconfirmedEP;
						unconfirmedEP = nextFractal;
						unconfirmedIndex = nextFractalIndex;
						Fractal lastFractal = getFractalbyIndex(fractalList, lastEndPoint);
						lastEndPoint = confirmedEP.originalOrdinal;
						Fractal thisFractal = getFractalbyIndex(fractalList, confirmedEP.originalOrdinal); 
						strokeList.add(createStrokeFromFractal(lastFractal, thisFractal, high, low));
						confirmList.add(getFractalbyIndex(fractalList, nextFractal.originalOrdinal));
						firstFailFractal = null;
						allowSecondary = false;
					}
				} else if (firstFailFractal == null && kLineCount == minimumBarsForStrokeConfirm - 1 && strict) {
					firstFailFractal = nextFractal;
					allowSecondary = true;
				} else if (firstFailFractal == null && kLineCount < minimumBarsForStrokeConfirm - 1 && strict) {
					firstFailFractal = nextFractal;
					allowSecondary = false;
				}
			}
		}
		return strokeList;
	}
}

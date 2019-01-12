package strategy;

import org.apache.commons.math3.geometry.euclidean.twod.Line;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import indicator.zen.Fractal;
import indicator.zen.Stroke;
import indicator.zen.Trend;
import strategy.template.ZenBasedStrategyWithTrailingStop;

public class WedgeBreak extends ZenBasedStrategyWithTrailingStop {

	private enum State {
		INIT,
		CONFIRMED_4,	// Need 4 trends to form a wedge
	}
	private State state = State.INIT;
	private boolean direction = true;

	private Trend ab, bc, cd, de;
	private float a, b, c, d, e;
	private Line upperLine, lowerLine;
	private Vector2D crossPoint;
	private Fractal breakUpBegin, breakDnBegin;

	public WedgeBreak(Integer useStrictStroke, Float gapThreshold, Float AFstep, Float AFmax) {
		super(useStrictStroke != 0, gapThreshold, AFstep, AFmax);
		reset();
	}

	public WedgeBreak() {
		this(1, 1.0f, 0.02f, 0.2f);
	}

	@Override
	public void reset() {
		super.reset();
		state = State.INIT;
		ab = bc = cd = de = null;
		upperLine = null;
		lowerLine = null;
		crossPoint = null;
		breakUpBegin = null;
		breakDnBegin = null;
	}

	@Override
	public float onOpen() {
		if (state == State.CONFIRMED_4 && position == 0 && current_index > crossPoint.getX()) {	// FIXME check center also
			reset();
		}
		return Open[current_index];
	}

	@Override
	public float onHigh() {
		switch (state) {
		case INIT:
			break;
		case CONFIRMED_4:
			break;
		default:
			break;
		}
		return super.onHigh();
	}

	@Override
	public float onLow() {
		switch (state) {
		case INIT:
			break;
		case CONFIRMED_4:
			break;
		default:
			break;
		}
		return super.onLow();
	}

	@Override
	public void onFractalFormed(int fractalIdx) {
		switch (state) {
		case INIT:
			break;
		case CONFIRMED_4:
			if (position == 0) {
				Fractal lastFractal = fractalList.get(fractalIdx);
				if (lastFractal.direction && breakDnBegin != null) {
					// check break down
					int startIdx = fractalList.indexOf(breakDnBegin);
					Fractal dnFractal = null;
					float lowestEver = Float.MAX_VALUE;
					for (int i = startIdx + 1; i < fractalIdx; i++) {
						Fractal ithFractal = fractalList.get(i);
						if (!ithFractal.direction && ithFractal.peakValue < lowestEver) {
							lowestEver = ithFractal.peakValue;
							dnFractal = ithFractal;
						}
					}
					if (dnFractal == null) {
						System.out.println("Error!!!");
					} else {
						boolean dnFractalBelow = dnFractal.peakValue < d && dnFractal.peakValue < e && dnFractal.belowLine(lowerLine);
						boolean lastFractalBelow = lastFractal.peakValue < d && lastFractal.peakValue < e && lastFractal.belowLine(lowerLine);
						if (dnFractalBelow && lastFractalBelow) {
							setPosition(-1, Math.max(d, e));
						}
					}
				} else if (!lastFractal.direction && breakUpBegin != null) {
					// check break up
					int startIdx = fractalList.indexOf(breakUpBegin);
					Fractal upFractal = null;
					float highestEver = -Float.MAX_VALUE;
					for (int i = startIdx + 1; i < fractalIdx; i++) {
						Fractal ithFractal = fractalList.get(i);
						if (ithFractal.direction && ithFractal.peakValue > highestEver) {
							highestEver = ithFractal.peakValue;
							upFractal = ithFractal;
						}
					}
					if (upFractal == null) {
						System.out.println("Error!!!");
					} else {
						boolean upFractalAbove = upFractal.peakValue > d && upFractal.peakValue > e && upFractal.aboveLine(upperLine);
						boolean lastFractalAbove = lastFractal.peakValue > d && lastFractal.peakValue > e && lastFractal.aboveLine(upperLine);
						if (upFractalAbove && lastFractalAbove) {
							setPosition(1, Math.min(d, e));
						}
					}
				}
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onStrokeConfirmed(int strokeIdx) {
		switch (state) {
		case INIT:
			if (strokeIdx > 4) {
				ab = strokeList.get(strokeIdx - 3);
				bc = strokeList.get(strokeIdx - 2);
				cd = strokeList.get(strokeIdx - 1);
				de = strokeList.get(strokeIdx - 0);
				direction = ab.direction();
				a = ab.startValue();
				b = ab.endValue();
				c = cd.startValue();
				d = cd.endValue();
				e = de.endValue();
				if (direction && (a < c) && (c <= e) && (b >= d)) {
					state = State.CONFIRMED_4;
				} else if (!direction && (a > c) && (c >= e) && (b <= d)) {
					state = State.CONFIRMED_4;
				}

				if (state == State.CONFIRMED_4) {
					if (direction) {
						upperLine = new Line(new Vector2D(ab.endIndex(), b), new Vector2D(cd.endIndex(), d), 0.0);
						lowerLine = new Line(new Vector2D(bc.endIndex(), c), new Vector2D(de.endIndex(), e), 0.0);
						breakUpBegin = ((Stroke)de).endFractal;	// FIXME
					} else {
						lowerLine = new Line(new Vector2D(ab.endIndex(), b), new Vector2D(cd.endIndex(), d), 0.0);
						upperLine = new Line(new Vector2D(bc.endIndex(), c), new Vector2D(de.endIndex(), e), 0.0);
						breakDnBegin = ((Stroke)de).endFractal;	// FIXME
					}
					crossPoint = upperLine.intersection(lowerLine);
					if (crossPoint == null) {
						reset();
					}
				}
			}
			break;
		case CONFIRMED_4:
			Stroke lastStroke = strokeList.get(strokeIdx);
			if (lastStroke.direction()) {
				breakDnBegin = lastStroke.endFractal;
			} else {
				breakUpBegin = lastStroke.endFractal;
			}
			break;
		default:
			break;
		}
	}
}

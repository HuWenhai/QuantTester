package strategy;

import indicator.zen.Center;
import indicator.zen.Fractal;
import indicator.zen.Stroke;
import indicator.zen.Trend;
import strategy.template.ZenBasedStrategyWithTrailingStop;

public class Class3Channel extends ZenBasedStrategyWithTrailingStop {

	private enum State {
		INIT,
		WAIT_CLASS_3,
		MAYBE_CLASS_3,	//subjectively
		CONFIRMED_CLASS_3,
	}
	private State state = State.INIT;
	private boolean direction = true;
	private Center recentCenter = null;

	private class ParallelChannel {
		private float a, b1, b2;	// y=ax+b
		public ParallelChannel(Trend first, Trend second) {
			float x1 = first.startIndex();
			float x2 = second.endIndex();
			float y1 = first.startValue();
			float y2 = second.endValue();
			a = (y2 - y1) / (x2 - x1);
			b1 = y1 - a * x1;
			b2 = y2 - a * x2;
		}
		public boolean aboveMiddle(int idx, float value) {
			return value > (a * idx + (b1 + b2) / 2.0f);
		}
		public boolean bellowMiddle(int idx, float value) {
			return value < (a * idx + (b1 + b2) / 2.0f);
		}
	}
	private ParallelChannel channel;

	public Class3Channel(Integer useStrictStroke, Float gapThreshold, Float AFstep, Float AFmax) {
		super(useStrictStroke != 0, gapThreshold, AFstep, AFmax);
	}

	public Class3Channel() {
		this(1, 1.0f, 0.02f, 0.2f);
	}

	@Override
	public void reset() {
		super.reset();
		state = State.INIT;
	}

	@Override
	public float onOpen() {
		return Open[current_index];
	}

	@Override
	public float onHigh() {
		switch (state) {
		case INIT:
			break;
		case WAIT_CLASS_3:
		case MAYBE_CLASS_3:
			if (!direction && High[current_index] > recentCenter.lowRange) {
				reset();
				return recentCenter.lowRange;
			}
			break;
		case CONFIRMED_CLASS_3:
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
		case WAIT_CLASS_3:
		case MAYBE_CLASS_3:
			if (direction && Low[current_index] < recentCenter.highRange) {
				reset();
				return recentCenter.highRange;
			}
			break;
		case CONFIRMED_CLASS_3:
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
		case WAIT_CLASS_3:
			Fractal fractal = fractalList.get(fractalIdx);
			boolean is_class_3_buy  = (direction && !fractal.direction && fractal.peakValue > recentCenter.highRange);
			boolean is_class_3_sell = (!direction && fractal.direction && fractal.peakValue < recentCenter.lowRange);
			if (is_class_3_buy && channel.bellowMiddle(fractal.peakIdx, fractal.peakValue)) {
				setPosition(1, (recentCenter.highRange + recentCenter.lowRange) / 2.0f);
				state = State.MAYBE_CLASS_3;
			} else if (is_class_3_sell && channel.aboveMiddle(fractal.peakIdx, fractal.peakValue)) {
				setPosition(-1, (recentCenter.highRange + recentCenter.lowRange) / 2.0f);
				state = State.MAYBE_CLASS_3;
			}
			break;
		case MAYBE_CLASS_3:
			break;
		case CONFIRMED_CLASS_3:
			break;
		default:
			break;
		}
	}

	@Override
	public void onStrokeConfirmed(int strokeIdx) {
		switch (state) {
		case INIT:
			if (strokeIdx > 3) {
				Stroke[] recentStrokes = new Stroke[4];
				recentStrokes[0] = strokeList.get(strokeIdx - 4);
				recentStrokes[1] = strokeList.get(strokeIdx - 3);
				recentStrokes[2] = strokeList.get(strokeIdx - 2);
				recentStrokes[3] = strokeList.get(strokeIdx - 1);
				Center center = Center.search(recentStrokes);
				direction = recentStrokes[0].direction();
				if (center != null) {
					if ((direction && strokeList.get(strokeIdx).endValue() > center.highRange) || (!direction && strokeList.get(strokeIdx).endValue() < center.lowRange)) {
						state = State.WAIT_CLASS_3;
						recentCenter = center;
						channel = new ParallelChannel(recentStrokes[3], strokeList.get(strokeIdx));
					}
				}
			}
			break;
		case WAIT_CLASS_3:
		case MAYBE_CLASS_3:
			float endValue = strokeList.get(strokeIdx).endValue();
			if ((direction && endValue > recentCenter.highRange) || (!direction && endValue < recentCenter.lowRange)) {
				state = State.CONFIRMED_CLASS_3;
			} else {
				reset();
			}
			break;
		case CONFIRMED_CLASS_3:
			state = State.INIT;
			break;
		default:
			break;
		}
	}
}

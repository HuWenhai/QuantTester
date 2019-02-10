package strategy;

import java.util.Arrays;
import java.util.function.BiFunction;

import helper.MathHelper;
import indicator.APPLIED_PRICE;
import indicator.MACD;
import strategy.template.EnterSignal;

class EnterSignal2 extends EnterSignal {
	public final Boot boot;
	public boolean stopAdjusted = false;

	public EnterSignal2(boolean direction, float triggerPrice, float cancelPrice, Boot boot) {
		super(direction, triggerPrice, cancelPrice);
		this.boot = boot;
	}
}

public class Breakthrough2 extends BreakthroughBase {

	private final MACD macd;
	private float[] macdWhiteBuffer = null;
	private float[] macdYellowBuffer = null;

	public Breakthrough2(Float AFstep, Float AFmax, Integer AMAPeriod, Integer AMAfastPeriod, Integer AMAslowPeriod,
			Integer maxPream, Integer minOsc, Float flatRatio,
			Integer FastEMA, Integer SlowEMA, Integer SignalSMA, APPLIED_PRICE applied_price) {
		super(AFstep, AFmax, AMAPeriod, AMAfastPeriod, AMAslowPeriod, maxPream, minOsc, flatRatio);
		macd = new MACD(FastEMA, SlowEMA, SignalSMA, applied_price);
	}

	@Override
	protected void calculateIndicators() {
		super.calculateIndicators();
		macdWhiteBuffer = macd.getBufferById(0);
		macdYellowBuffer = macd.getBufferById(1);
	}

	@Override
	public float onOpen() {
		var signal2 = ((EnterSignal2) signal);
		if (stop != null && !signal2.stopAdjusted) {
			int x1 = 0;
			float y1m = 0.0f;
			float km = 0.0f;
			BiFunction<Float, Float, Boolean> isWithin;
			if (stop.getDirection()) {
				isWithin = (y, y0) -> {
					return y0 < y + 0.0001f;
				};
			} else {
				isWithin = (y, y0) -> {
					return y0 > y - 0.0001f;
				};
			}
			Boot boot = signal2.boot;
			int x2 = current_index - 3;
			float y2m = macdYellowBuffer[x2];
			boolean foundAllWithin = false;
			for (x1 = boot.startIdx + 1; x1 < current_index - 4; x1 ++) {
				y1m = macdYellowBuffer[x1];
				km = (y2m - x2) / (y1m -x1);
				boolean allWithin = true;
				int x = boot.startIdx;
				for (; x < current_index; x++) {
					float ym = km * (x - x1) + y1m;
					if (!isWithin.apply(macdYellowBuffer[x], ym)) {
						allWithin = false;
						break;
					}
				}
				if (allWithin) {
					foundAllWithin = true;
					break;
				}
			}
			if (foundAllWithin) {
				if (stop.getDirection()) {
					if (y2m > y1m) {
						stop.speedDown();
					} else if (y2m < y1m) {
						stop.speedUp();
					}
				} else {
					if (y2m > y1m) {
						stop.speedUp();
					} else if (y2m < y1m) {
						stop.speedDown();
					}
				}
				signal2.stopAdjusted = true;
			}
		}
		return super.onOpen();
	}

	@Override
	protected EnterSignal checkEnterSignal() {
		EnterSignal signal = null;
		for (Boot boot : boots) {
			if (boot.confirmIdx <= current_index && current_index < boot.expireIdx) {
				if (boot.direction) {
					float breakPoint = MathHelper.Max(Arrays.copyOfRange(High, boot.startIdx, current_index + 1));
					if (Close[current_index] < breakPoint) {
						signal = new EnterSignal2(true, breakPoint, Math.min(Math.min(Low[current_index], Low[current_index - 1]), Low[current_index - 2]), boot);
					}
				} else {
					float breakPoint = MathHelper.Min(Arrays.copyOfRange(Low, boot.startIdx, current_index + 1));
					if (Close[current_index] > breakPoint) { 
						signal = new EnterSignal2(false, breakPoint, Math.max(Math.max(High[current_index], High[current_index - 1]), High[current_index - 2]), boot);
					}
				}
			}
		}
		return signal;
	}
}

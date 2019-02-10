package strategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import data.TIME_FRAME;
import global.Cache;
import helper.MathHelper;
import indicator.AMA;
import indicator.APPLIED_PRICE;
import strategy.template.EnterSignal;
import strategy.template.IEveryOHLC;
import strategy.template.ReverseWithTrailingStop;

class Boot {
	public boolean direction;
	public int startIdx;
	public int heelIdx;
	public int confirmIdx;
	public int expireIdx;

	public Boot(boolean direction, int startIdx, int heelIdx, int confirmIdx) {
		this.direction = direction;
		this.startIdx = startIdx;
		this.heelIdx = heelIdx;
		this.confirmIdx = confirmIdx;
		this.expireIdx = 0;
	}
}

public class BreakthroughBase extends ReverseWithTrailingStop implements IEveryOHLC {

	private final AMA ama;
	private final int AMAPeriod;
	private final int AMAfastPeriod;
	private final int AMAslowPeriod;
	private final int maxPream;
	private final int minOsc;
	private final float flatRatio;

	private float[] amaBuffer = null;
	protected List<Boot> boots = null;

	public BreakthroughBase(Float AFstep, Float AFmax, Integer AMAPeriod, Integer AMAfastPeriod, Integer AMAslowPeriod,
			Integer maxPream, Integer minOsc, Float flatRatio) {
		super(AFstep, AFmax);
		this.AMAPeriod = AMAPeriod;
		this.AMAfastPeriod = AMAfastPeriod;
		this.AMAslowPeriod = AMAslowPeriod;
		this.maxPream = maxPream;
		this.minOsc = minOsc;
		this.flatRatio = flatRatio;
		this.ama = new AMA(AMAPeriod, AMAfastPeriod, AMAslowPeriod, 0, APPLIED_PRICE.PRICE_CLOSE);
	}

	private List<Boot> findBoots() {
		ArrayList<Boot> retBoots = new ArrayList<>();
		Boot prevBoot = null;
		boolean confirmed = false;
		int len = amaBuffer.length;
		for (int i = 0; i < len; i++) {
			int searchStart = 0;
			if (prevBoot != null) {
				searchStart = confirmed ? prevBoot.startIdx : prevBoot.heelIdx;
			}
			boolean match = false;
			for (; searchStart <= (i - minOsc); searchStart ++) {
				for (int j = searchStart; j <= Math.min((i - minOsc), searchStart + maxPream - 1); j++) {
					// Assume j is heel, see if it makes a boot
					float[] searchRange = Arrays.copyOfRange(amaBuffer, searchStart, (i + 1));
					float allMax = MathHelper.Max(searchRange);
					float allMin = MathHelper.Min(searchRange);
					float all = allMax - allMin;
					float[] flatRange = Arrays.copyOfRange(amaBuffer, j, (i + 1));
					float flatMax = MathHelper.Max(flatRange);
					float flatMin = MathHelper.Min(flatRange);
					float flat = flatMax - flatMin;
					if (flat / all <= flatRatio) {
						boolean direction = true;
						if (allMax == flatMax) {
							match = true;
							direction = true;
						} else if (allMin == flatMin) {
							match = true;
							direction = false;
						}
						if (match && !confirmed) {
							confirmed = true;
							prevBoot = new Boot(direction, searchStart, j, i);
						}
						break;
					}
				}
				if (match) {
					break;
				}
			}
			if (!match && confirmed) {
				prevBoot.expireIdx = i;
				retBoots.add(prevBoot);
				confirmed = false;
			}
		}
		return retBoots;
	}

	protected void printBoots() {
		for (Boot boot : boots) {
			if (boot.direction) {
				markDot(boot.startIdx, Low[boot.startIdx], 13);
				markDot(boot.heelIdx, High[boot.heelIdx], 15);
				markDot(boot.confirmIdx, Close[boot.confirmIdx], 1);
				markDot(boot.expireIdx, Close[boot.expireIdx], 14);
			} else {
				markDot(boot.startIdx, High[boot.startIdx], 13);
				markDot(boot.heelIdx, Low[boot.heelIdx], 15);
				markDot(boot.confirmIdx, Close[boot.confirmIdx], 1);
				markDot(boot.expireIdx, Close[boot.expireIdx], 14);				
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void calculateIndicators() {
		super.calculateIndicators();
		amaBuffer = ama.getBufferById(0);
		//boots = findBoots();
		boots = (List<Boot>) Cache.getCachedSignal(this::findBoots, "", TIME_FRAME.MIN15, 0, amaBuffer.length - 1, "boots",
				AMAPeriod, AMAfastPeriod, AMAslowPeriod, maxPream, minOsc, flatRatio);
		//printBoots();
	}

	@Override
	protected EnterSignal checkEnterSignal() {
		EnterSignal signal = null;
		for (Boot boot : boots) {
			if (boot.confirmIdx <= current_index && current_index < boot.expireIdx) {
				if (boot.direction) {
					float breakPoint = MathHelper.Max(Arrays.copyOfRange(High, boot.startIdx, current_index + 1));
					if (Close[current_index] < breakPoint) {
						signal = new EnterSignal(true, breakPoint, Math.min(Math.min(Low[current_index], Low[current_index - 1]), Low[current_index - 2]));
					}
				} else {
					float breakPoint = MathHelper.Min(Arrays.copyOfRange(Low, boot.startIdx, current_index + 1));
					if (Close[current_index] > breakPoint) { 
						signal = new EnterSignal(false, breakPoint, Math.max(Math.max(High[current_index], High[current_index - 1]), High[current_index - 2]));
					}
				}
			}
		}
		return signal;
	}

}

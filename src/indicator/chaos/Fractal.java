package indicator.chaos;

import java.util.List;

import indicator.IIndicator;
import indicator.IndicatorBuffer;

public class Fractal implements IIndicator {

	public boolean[] upFractal = null;
	public int[] confirmedUpIndex = null;
	public boolean[] dnFractal = null;
	public int[] confirmedDnIndex = null;

	public Fractal() {
		//
	}

	private static enum State {
		TWO,
		THREE,
		FOUR,
	}

	@Override
	public void calculate(float[] open, float[] high, float[] low, float[] close) {
		final int rates_total = close.length;
		upFractal = new boolean[rates_total];
		confirmedUpIndex = new int[rates_total];
		dnFractal = new boolean[rates_total];
		confirmedDnIndex = new int[rates_total];
		for (int i = 0; i < rates_total; i++) {
			upFractal[i] = false;
			confirmedUpIndex[i] = -1;
			dnFractal[i] = false;
			confirmedDnIndex[i] = -1;
		}
		if (rates_total < 5) {
			return;
		}

		State upState = State.TWO;
		float max2 = Math.max(high[0], high[1]);
		float max3 = Float.NEGATIVE_INFINITY;
		int max3Idx = -1; 
		State dnState = State.TWO;
		float min2 = Math.min(low[0], low[1]);
		float min3 = Float.NEGATIVE_INFINITY;
		int min3Idx = -1;
		for (int i = 2; i < rates_total; i++) {
			switch (upState) {
			case TWO:
				if (high[i] > max2) {
					max3 = high[i];
					max3Idx = i;
					upState = State.THREE;
				} else {
					max2 = Math.max(high[i - 1], high[i]);
				}
				break;
			case THREE:
				if (high[i] < max3) {
					upState = State.FOUR;
				} else {
					max3 = high[i];
					max3Idx = i;
				}
				break;
			case FOUR:
				if (high[i] < max3) {
					upFractal[max3Idx] = true;
					confirmedUpIndex[i] = max3Idx;
					max2 = Math.max(high[i - 1], high[i]);
					upState = State.TWO;
				} else if (high[i] > max3) {
					max3 = high[i];
					max3Idx = i;
					upState = State.THREE;
				} else {
					upState = State.THREE;
				}
				break;
			default:
				break;
			}

			switch (dnState) {
			case TWO:
				if (low[i] < min2) {
					min3 = low[i];
					min3Idx = i;
					dnState = State.THREE;
				} else {
					min2 = Math.min(low[i - 1], low[i]);
				}
				break;
			case THREE:
				if (low[i] > min3) {
					dnState = State.FOUR;
				} else {
					min3 = low[i];
					min3Idx = i;
				}
				break;
			case FOUR:
				if (low[i] > min3) {
					dnFractal[min3Idx] = true;
					confirmedDnIndex[i] = min3Idx;
					min2 = Math.min(low[i - 1], low[i]);
					dnState = State.TWO;
				} else if (low[i] < min3) {
					min3 = low[i];
					min3Idx = i;
					dnState = State.THREE;
				} else {
					upState = State.THREE;
				}
				break;
			default:
				break;
			}
		}
	}

	@Override
	public float[] getBufferById(int id) {
		return null;
	}

	@Override
	public int minimumBarsToWork() {
		return 3;
	}

	@Override
	public List<IndicatorBuffer> getIndicatorBuffers() {
		return null;
	}
}

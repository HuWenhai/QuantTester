package indicator.chaos;

import indicator.IIndicator;

public class DivergentBar implements IIndicator {

	public boolean[] bullishDivergent = null;
	public boolean[] bearishDivergent = null;

	@Override
	public void calculate(float[] open, float[] high, float[] low, float[] close) {
		final int rates_total = close.length;
		bullishDivergent = new boolean[rates_total];
		bearishDivergent = new boolean[rates_total];
		for (int i = 0; i < rates_total; i++) {
			bullishDivergent[i] = false;
			bearishDivergent[i] = false;
		}
		if (rates_total < 2) {
			return;
		}
		for (int i = 1; i < rates_total; i++) {
			if ((high[i] > high[i - 1]) && (close[i] < ((high[i] + low[i]) / 2.0f))) {
				bearishDivergent[i] = true;
			} else if ((low[i] < low[i - 1]) && (close[i] > ((high[i] + low[i]) / 2.0f))) {
				bullishDivergent[i] = true;
			}
		}
	}

	@Override
	public float[] getBufferById(int id) {
		return null;
	}
}

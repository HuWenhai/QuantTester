package indicator.zen;

import indicator.IIndicator;

public class Fractal implements IIndicator {

	protected float[] vf = null;

	public Fractal() {
		//
	}

	@Override
	public void calculate(float[] open, float[] high, float[] low, float[] close) {
		final int rates_total = close.length;
		vf = new float[rates_total];
		for (int i = 0; i < rates_total; i++) {
			vf[i] = 0.0f;
		}
		if (rates_total < 3) {
			return;
		}

		for (int i = 1; i < rates_total - 1; i++) {
			if (high[i] > high[i + 1] && high[i] > high[i - 1] && low[i] > low[i + 1] && low[i] > low[i - 1]) {
				vf[i] = 1.0f;
			} else if (high[i] < high[i + 1] && high[i] < high[i - 1] && low[i] < low[i + 1] && low[i] < low[i - 1]) {
				vf[i] = - 1.0f;
			}
		}
	}

	@Override
	public float[] getBufferById(int id) {
		switch (id) {
		case 0:
			return vf;
		default:
			return null;
		}
	}
}

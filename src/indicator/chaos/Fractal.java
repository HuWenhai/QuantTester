package indicator.chaos;

import indicator.IIndicator;

public class Fractal implements IIndicator {

	private float[] upDownMark = null;
	private float[] arrowPos = null;

	public Fractal() {
		//
	}

	@Override
	public void calculate(float[] open, float[] high, float[] low, float[] close) {
		final int rates_total = close.length;
		upDownMark = new float[rates_total];
		arrowPos = new float[rates_total];
		for (int i = 0; i < rates_total; i++) {
			upDownMark[i] = 0.0f;
			arrowPos[i] = 0.0f;
		}
		if (rates_total < 5) {
			return;
		}

		for (int i = 2; i < rates_total - 2; i++) {
			if (high[i] > high[i - 2] && high[i] > high[i - 1] && high[i] > high[i + 1] && high[i] > high[i + 2]) {
				upDownMark[i] = 1.0f;
				arrowPos[i] = high[i] + 10.0f;
			} else if (low[i] < low[i - 2] && low[i] < low[i - 1] && low[i] < low[i + 1] && low[i] < low[i + 2]) {
				upDownMark[i] = - 1.0f;
				arrowPos[i] = low[i] - 10.0f;
			}
		}
	}

	@Override
	public float[] getBufferById(int id) {
		switch (id) {
		case 0:
			return upDownMark;
		case 1:
			return arrowPos;
		default:
			return null;
		}
	}
}

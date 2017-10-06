package indicator.zen;

import indicator.Price_Channel;

public class FractalChannel extends Fractal {

	int nL;

	protected float[] vH;
	protected float[] vL;

	protected float[] vU;
	protected float[] vB;

	public FractalChannel(int period) {
		this.nL = period;
	}

	public FractalChannel() {
		this(15);
	}

	@Override
	public void calculate(float[] open, float[] high, float[] low, float[] close) {
		super.calculate(open, high, low, close);

		int rates_total = close.length;
		vH = new float[rates_total];
		vL = new float[rates_total];
		vU = new float[rates_total];
		vB = new float[rates_total];

		for (int i = 0; i < rates_total; i++) {
			vH[i] = 0.0f;
			vL[i] = 0.0f;
			vU[i] = 0.0f;
			vB[i] = 0.0f;
		}

		if (rates_total < (3 + nL - 1)) {
			return;
		}

		for (int i = 2; i < rates_total; i++) {
			if (vf[i - 2] < 0.1f) {
				vH[i] = vH[i - 1];
			} else {
				vH[i] = high[i - 2];
			}

			if (vf[i - 2] > - 0.1f) {
				vL[i] = vL[i - 1];
			} else {
				vL[i] = low[i - 2];
			}
		}

		for (int i = 2 + nL - 1; i < rates_total; i++) {
			vU[i] = Price_Channel.Highest(vH, nL, i);
			vB[i] = Price_Channel.Lowest(vL, nL, i);
		}
	}

	@Override
	public float[] getBufferById(int id) {
		switch (id) {
		case 0:
			return vf;
		case 1:
			return vH;
		case 2:
			return vL;
		case 3:
			return vU;
		case 4:
			return vB;
		default:
			return null;
		}
	}
}

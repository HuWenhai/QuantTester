package indicator;

public class BollingerBand implements IIndicator {
	@SuppressWarnings("unused")
	private final int ExtBandsPeriod, ExtBandsShift;
	private final float ExtBandsDeviations;
	private final IPriceSimplifier applied_price;
	private int ExtPlotBegin = 0;

	float ExtMLBuffer[];
	float ExtTLBuffer[];
	float ExtBLBuffer[];
	float ExtStdDevBuffer[];

	public BollingerBand(int period, int shift, float deviations, IPriceSimplifier applied_price) {
		if (period >= 2) {
			this.ExtBandsPeriod = period;
			this.ExtBandsShift = shift;
			this.ExtBandsDeviations = deviations;
		} else {
			// TODO Record error
			this.ExtBandsPeriod = 20;
			this.ExtBandsShift = 0;
			this.ExtBandsDeviations = 2.0f;
		}
		this.applied_price = applied_price;
		this.ExtPlotBegin = ExtBandsPeriod - 1;
	}

	public BollingerBand() {
		this(20, 0, 2.0f, APPLIED_PRICE.PRICE_CLOSE);
	}

	@Override
	public void calculate(float[] open, float[] high, float[] low, float[] close) {
		float[] price = applied_price.simplify(open, high, low, close);
		ExtMLBuffer = new float[price.length];
		ExtTLBuffer = new float[price.length];
		ExtBLBuffer = new float[price.length];
		ExtStdDevBuffer = new float[price.length];
		this.calculateForAppliedPrice(price);
	}

	// TODO create MA_helper
	private static float SimpleMA(final int position, final int period, final float[] price) {
		// ---
		float result = 0.0f;
		// --- check position
		if (position >= period - 1 && period > 0) {
			// --- calculate value
			for (int i = 0; i < period; i++)
				result += price[position - i];
			result /= period;
		}
		// ---
		return (result);
	}

	private float StdDev_Func(int position, float price[], float MAprice[], int period) {
		// --- variables
		float StdDev_dTmp = 0.0f;
		// --- check for position
		if (position < period)
			return (StdDev_dTmp);
		// --- calcualte StdDev
		for (int i = 0; i < period; i++)
			StdDev_dTmp += Math.pow(price[position - i] - MAprice[position], 2);
		StdDev_dTmp = (float) Math.sqrt(StdDev_dTmp / period);
		// --- return calculated value
		return (StdDev_dTmp);
	}

	private void calculateForAppliedPrice(float[] price) {
		int rates_total = price.length;
		int prev_calculated = 0;
		int begin = 0;
		// --- variables
		int pos;
		// --- indexes draw begin settings, when we've recieved previous begin
		if (ExtPlotBegin != ExtBandsPeriod + begin) {
			ExtPlotBegin = ExtBandsPeriod + begin;
		}
		// --- check for bars count
		if (rates_total < ExtPlotBegin)
			return;
		// --- starting calculation
		if (prev_calculated > 1)
			pos = prev_calculated - 1;
		else
			pos = 0;
		// --- main cycle
		for (int i = pos; i < rates_total; i++) {
			// --- middle line
			ExtMLBuffer[i] = SimpleMA(i, ExtBandsPeriod, price);
			// --- calculate and write down StdDev
			ExtStdDevBuffer[i] = StdDev_Func(i, price, ExtMLBuffer, ExtBandsPeriod);
			// --- upper line
			ExtTLBuffer[i] = ExtMLBuffer[i] + ExtBandsDeviations * ExtStdDevBuffer[i];
			// --- lower line
			ExtBLBuffer[i] = ExtMLBuffer[i] - ExtBandsDeviations * ExtStdDevBuffer[i];
			// ---
		}
	}

	@Override
	public float[] getBufferById(int id) {
		switch (id) {
		case 0:
			return ExtMLBuffer;
		case 1:
			return ExtTLBuffer;
		case 2:
			return ExtBLBuffer;
		case 3:
			return ExtStdDevBuffer;
		default:
			return null;
		}
	}
}

package strategy;

import java.util.Arrays;

import helper.MathHelper;
import indicator.AMA;
import indicator.APPLIED_PRICE;
import strategy.template.EnterSignal;
import strategy.template.IEveryOHLC;
import strategy.template.ReverseWithTrailingStop;

public class SmartTrading extends ReverseWithTrailingStop implements IEveryOHLC {

	private final AMA ama;
	int stdDevPeriod;
	float stdDevThreshold;

	private float[] amaBuffer = null;
	private float[] filterBuf = null;

	public SmartTrading(Float AFstep, Float AFmax, Integer AMAPeriod, Integer AMAfastPeriod, Integer AMAslowPeriod,
			Integer stdDevPeriod, Float stdDevThreshold) {
		super(AFstep, AFmax);
		this.stdDevPeriod = stdDevPeriod;
		this.stdDevThreshold = stdDevThreshold;
		this.ama = new AMA(AMAPeriod, AMAfastPeriod, AMAslowPeriod, 0, APPLIED_PRICE.PRICE_CLOSE);
	}

	@Override
	protected void calculateIndicators() {
		super.calculateIndicators();
		amaBuffer = ama.getBufferById(0);
		float[] diffBuff = new float[amaBuffer.length];
		diffBuff[0] = 0.0f;
		for (int i = 1; i < amaBuffer.length; i++) {
			diffBuff[i] = amaBuffer[i] - amaBuffer[i - 1];
		}
		filterBuf = new float[amaBuffer.length];
		for (int i = 0; i < stdDevPeriod; i++) {
			filterBuf[i] = Float.MAX_VALUE;
		}
		for (int i = stdDevPeriod; i < amaBuffer.length; i++) {
			filterBuf[i] = stdDevThreshold
					* MathHelper.SD(Arrays.copyOfRange(diffBuff, i - stdDevPeriod + 1, i + 1));
		}
	}

	private boolean checkSellSignal() {
		return (amaBuffer[current_index - 1] - amaBuffer[current_index] > filterBuf[current_index]
				|| amaBuffer[current_index - 2] - amaBuffer[current_index] > filterBuf[current_index]
				|| amaBuffer[current_index - 3] - amaBuffer[current_index] > filterBuf[current_index]);
	}

	private boolean checkBuySignal() {
		return (amaBuffer[current_index] - amaBuffer[current_index - 1] > filterBuf[current_index]
				|| amaBuffer[current_index] - amaBuffer[current_index - 2] > filterBuf[current_index]
				|| amaBuffer[current_index] - amaBuffer[current_index - 3] > filterBuf[current_index]);
	}

	@Override
	protected EnterSignal checkEnterSignal() {
		EnterSignal signal = null;
		if (checkBuySignal()) {
			signal = new EnterSignal(true, -Float.MAX_VALUE, Math.min(Math.min(Low[current_index], Low[current_index - 1]), Low[current_index - 2]));
		} else if (checkSellSignal()) {
			signal = new EnterSignal(false, Float.MAX_VALUE, Math.max(Math.max(High[current_index], High[current_index - 1]), High[current_index - 2]));
		}
		return signal;
	}

}

package strategy;

import indicator.APPLIED_PRICE;
import indicator.BollingerBand;
import indicator.chaos.Alligator;
import indicator.chaos.DivergentBar;
import strategy.template.EnterSignal;
import strategy.template.ReverseWithTrailingStop;

public class AlligatorDivergent extends ReverseWithTrailingStop {

	private final float stdDevThreshold;

	private final Alligator alligator;
	private final DivergentBar divergentBar;
	private final BollingerBand bb;

	private float[] jaws = null;
	private float[] teeth = null;
	private float[] lips = null;
	private boolean[] bullishDivergent = null;
	private boolean[] bearishDivergent = null;
	private float[] bbTop = null;
	private float[] bbBottom = null;
	private float[] stdDev = null;

	public AlligatorDivergent(Float AFstep, Float AFmax, Integer BBPeriod, Float BBDeviations, Float stdDevThreshold) {
		super(AFstep, AFmax);
		this.stdDevThreshold = stdDevThreshold;
		this.alligator = new Alligator();
		this.divergentBar = new DivergentBar();
		this.bb = new BollingerBand(BBPeriod, 0, BBDeviations, APPLIED_PRICE.PRICE_CLOSE);
	}

	public AlligatorDivergent(Float stdDevThreshold) {
		super();
		this.stdDevThreshold = stdDevThreshold;
		this.alligator = new Alligator();
		this.divergentBar = new DivergentBar();
		this.bb = new BollingerBand();
	}

	public AlligatorDivergent() {
		this(1.0f);
	}

	@Override
	protected void calculateIndicators() {
		super.calculateIndicators();
		jaws = alligator.getBufferById(0);
		teeth = alligator.getBufferById(1);
		lips = alligator.getBufferById(2);
		bullishDivergent = divergentBar.bullishDivergent;
		bearishDivergent = divergentBar.bearishDivergent;
		bbTop = bb.getBufferById(1);
		bbBottom = bb.getBufferById(2);
		stdDev = bb.getBufferById(3);
	}

	private boolean checkBuySignal() {
		if (bullishDivergent[current_index] && Low[current_index] < bbBottom[current_index] && stdDev[current_index] > stdDevThreshold) {
			if (jaws[current_index] > teeth[current_index] && teeth[current_index] > lips[current_index] && lips[current_index] > High[current_index]) {
				return true;
			}
		}
		return false;
	}

	private boolean checkSellSignal() {
		if (bearishDivergent[current_index] && High[current_index] > bbTop[current_index] && stdDev[current_index] > stdDevThreshold) {
			if (jaws[current_index] < teeth[current_index] && teeth[current_index] < lips[current_index] && lips[current_index] < Low[current_index]) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected EnterSignal checkEnterSignal() {
		EnterSignal signal = null;
		if (checkBuySignal()) {
			signal = new EnterSignal(true, High[current_index], Low[current_index]);
		} else if (checkSellSignal()) {
			signal = new EnterSignal(false, Low[current_index], High[current_index]);
		}
		return signal;
	}
}

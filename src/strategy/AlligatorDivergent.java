package strategy;

import indicator.APPLIED_PRICE;
import indicator.BollingerBand;
import indicator.chaos.Alligator;
import indicator.chaos.DivergentBar;
import strategy.template.TrailingStop;

public class AlligatorDivergent extends TrailingStop {

	private final float stdDevThreshold;

	private final Alligator alligator;
	private final DivergentBar divergentBar;
	private final BollingerBand bb;

	private float[] jaws = null;
	private float[] teeth = null;
	private float[] lips = null;
	private boolean[] bullishDivergent = null;
	private boolean[] bearishDivergent = null;
	private float[] stdDev = null;

	public AlligatorDivergent(Float AFstep, Float AFmax, Integer BBPeriod, Integer BBShift, Float stdDevThreshold) {
		super(AFstep, AFmax);
		this.stdDevThreshold = stdDevThreshold;
		this.alligator = new Alligator();
		this.divergentBar = new DivergentBar();
		this.bb = new BollingerBand(BBPeriod, BBShift, 2.0f, APPLIED_PRICE.PRICE_CLOSE);
		this.indicators.add(alligator);
		this.indicators.add(divergentBar);
		this.indicators.add(bb);
		reset();
	}

	public AlligatorDivergent(Float stdDevThreshold) {
		super();
		this.stdDevThreshold = stdDevThreshold;
		this.alligator = new Alligator();
		this.divergentBar = new DivergentBar();
		this.bb = new BollingerBand();
		this.indicators.add(alligator);
		this.indicators.add(divergentBar);
		this.indicators.add(bb);
		reset();
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
		stdDev = bb.getBufferById(3);
	}

	@Override
	protected boolean enoughBars() {
		return (current_index >= (21 + 1));
	}

	@Override
	protected boolean checkBuySignal() {
		if (bullishDivergent[current_index] && stdDev[current_index] > stdDevThreshold) {
			if (jaws[current_index] > teeth[current_index] && teeth[current_index] > lips[current_index] && lips[current_index] > High[current_index]) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean checkSellSignal() {
		if (bearishDivergent[current_index] && stdDev[current_index] > stdDevThreshold) {
			if (jaws[current_index] < teeth[current_index] && teeth[current_index] < lips[current_index] && lips[current_index] < Low[current_index]) {
				return true;
			}
		}
		return false;
	}
}

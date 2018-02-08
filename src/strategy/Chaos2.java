package strategy;

import java.util.ArrayList;
import java.util.List;

import helper.MathHelper;
import indicator.APPLIED_PRICE;
import indicator.BollingerBand;
import indicator.chaos.Alligator;
import indicator.chaos.AwesomeOscillator;
import indicator.chaos.DivergentBar;
import indicator.chaos.Fractal;
import strategy.template.AddOnTrailingStop;

public class Chaos2 extends AddOnTrailingStop {
	private final float stdDevThreshold;

	private final Alligator alligator;
	private final DivergentBar divergentBar;
	private final BollingerBand bb;
	private final Fractal fractal;
	private final AwesomeOscillator ao;

	private float[] jaws = null;
	private float[] teeth = null;
	private float[] lips = null;
	private boolean[] bullishDivergent = null;
	private boolean[] bearishDivergent = null;
	private float[] bbTop = null;
	private float[] bbBottom = null;
	private float[] stdDev = null;
	private int[] confirmedUpIndex = null;
	private int[] confirmedDnIndex = null;
	private float[] aoBuffer = null;

	private class AOSignal {
		private boolean allowLong = false;
		private boolean allowShort = false;

		private float lowestAO = Float.MAX_VALUE;
		private float highestAO = -Float.MAX_VALUE;

		public boolean upSignal = false;
		public boolean dnSignal = false;

		public void update(float aoValue) {
			if (aoValue < lowestAO && aoValue < 0.0f) {
				lowestAO = aoValue;
				highestAO = -Float.MAX_VALUE;
				allowLong = true;
				allowShort = false;
			}
			if (aoValue > highestAO && aoValue > 0.0f) {
				lowestAO = Float.MAX_VALUE;
				highestAO = aoValue;
				allowLong = false;
				allowShort = true;
			}
			if (allowLong && checkUp()) {
				upSignal = true;
				allowLong = false;
			} else {
				upSignal = false;
			}
			if (allowShort && checkDn()) {
				dnSignal = true;
				allowShort = false;
			} else {
				dnSignal = false;
			}
		}

		private boolean checkUp() {
			return (aoBuffer[current_index]		> aoBuffer[current_index - 1] &&
					aoBuffer[current_index - 1] > aoBuffer[current_index - 2] &&
					aoBuffer[current_index - 2] > aoBuffer[current_index - 3] &&
					aoBuffer[current_index - 3] > aoBuffer[current_index - 4] &&
					aoBuffer[current_index - 4] < aoBuffer[current_index - 5]);
		}

		private boolean checkDn() {
			return (aoBuffer[current_index]		< aoBuffer[current_index - 1] &&
					aoBuffer[current_index - 1] < aoBuffer[current_index - 2] &&
					aoBuffer[current_index - 2] < aoBuffer[current_index - 3] &&
					aoBuffer[current_index - 3] < aoBuffer[current_index - 4] &&
					aoBuffer[current_index - 4] > aoBuffer[current_index - 5]);
		}
	}
	private AOSignal aoSignal = null;

	public Chaos2(Float AFstep, Float AFmax, Integer openVol, Integer addOn1Vol, Integer jawsPeriod, Integer jawsShift, Integer teethPeriod, Integer teethShift, Integer lipsPeriod, Integer lipsShift, Integer BBPeriod, Float BBDeviations, Float stdDevThreshold, Integer fastMA, Integer slowMA) {
		super(AFstep, AFmax, new int[]{0, openVol, openVol + addOn1Vol, 10});
		this.stdDevThreshold = stdDevThreshold;
		this.alligator = new Alligator(jawsPeriod, jawsShift, teethPeriod, teethShift, lipsPeriod, lipsShift);
		this.divergentBar = new DivergentBar();
		this.bb = new BollingerBand(BBPeriod, 0, BBDeviations, APPLIED_PRICE.PRICE_CLOSE);
		this.fractal = new Fractal();
		this.ao = new AwesomeOscillator(fastMA, slowMA);
	}

	public Chaos2(Integer BBPeriod, Float BBDeviations, Float stdDevThreshold) {
		super();
		this.stdDevThreshold = stdDevThreshold;
		this.alligator = new Alligator();
		this.divergentBar = new DivergentBar();
		this.bb = new BollingerBand(BBPeriod, 0, BBDeviations, APPLIED_PRICE.PRICE_CLOSE);
		this.fractal = new Fractal();
		this.ao = new AwesomeOscillator();
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
		confirmedUpIndex = fractal.confirmedUpIndex;
		confirmedDnIndex = fractal.confirmedDnIndex;
		aoBuffer = ao.getBufferById(0);
	}

	private float getWiseManTriggerPrice(boolean direction, int type) {
		float ret = Float.NEGATIVE_INFINITY;
		switch (type) {
		case 1:
		case 2:
			ret = direction ? High[current_index] : Low[current_index];
			break;
		case 3:
			if (direction) {
				int fractalIdx = confirmedUpIndex[current_index];
				ret = High[fractalIdx];
			} else {
				int fractalIdx = confirmedDnIndex[current_index];
				ret = Low[fractalIdx];
			}
			break;
		default:
			break;
		}
		return ret;
	}

	private float getWiseManCancelPrice(boolean direction, int type) {
		float ret = Float.NEGATIVE_INFINITY;
		switch (type) {
		case 1:
			ret = direction ? Low[current_index] : High[current_index];
			break;
		case 2:
			ret = direction ?
					MathHelper.Min(Low[current_index], Low[current_index - 1], Low[current_index - 2]) :
					MathHelper.Max(High[current_index], High[current_index - 1], High[current_index - 2]);
			break;
		case 3:
			ret = direction ? -Float.MAX_VALUE : Float.MAX_VALUE;
			break;
		default:
			break;
		}
		return ret;
	}

	class WiseMan extends EnterSignalNeedConfirm {
		public final int type;
		public WiseMan(boolean direction, int type) {
			super(direction, getWiseManTriggerPrice(direction, type), getWiseManCancelPrice(direction, type));
			this.type = type;
		}

		@Override
		public String toString() {
			String ret = System.lineSeparator() + "WiseMan" + type;
			ret += getDirection() ? " Up: Trigger=" : " Down: Trigger=";
			ret += triggerPrice;
			ret += ";Cancel=";
			ret += cancelPrice;
			return ret;
		}
	}

	private boolean checkWiseMan1Buy() {
		if (bullishDivergent[current_index] && Low[current_index] < bbBottom[current_index] && stdDev[current_index] > stdDevThreshold) {
			if (jaws[current_index] > teeth[current_index] && teeth[current_index] > lips[current_index] && lips[current_index] > High[current_index]) {
				return aoBuffer[current_index] < aoBuffer[current_index - 1];
			}
		}
		return false;
	}

	private boolean checkWiseMan1Sell() {
		if (bearishDivergent[current_index] && High[current_index] > bbTop[current_index] && stdDev[current_index] > stdDevThreshold) {
			if (jaws[current_index] < teeth[current_index] && teeth[current_index] < lips[current_index] && lips[current_index] < Low[current_index]) {
				return aoBuffer[current_index] > aoBuffer[current_index - 1];
			}
		}
		return false;
	}

	private boolean checkWiseMan2Buy() {
		return aoSignal.upSignal;
	}

	private boolean checkWiseMan2Sell() {
		return aoSignal.dnSignal;
	}

	private boolean checkWiseMan3Buy() {
		int upIdx = confirmedUpIndex[current_index];
		return upIdx > 0;
	}

	private boolean checkWiseMan3Sell() {
		int dnIdx = confirmedDnIndex[current_index];
		return dnIdx > 0;
	}

	protected List<EnterSignalNeedConfirm> getOpenSignals() {
		List<EnterSignalNeedConfirm> wiseMans = new ArrayList<>();
		if (checkWiseMan1Buy()) {
			wiseMans.add(new WiseMan(true, 1));
		}
		if (checkWiseMan1Sell()) {
			wiseMans.add(new WiseMan(false, 1));
		}
		if (checkWiseMan2Buy()) {
			wiseMans.add(new WiseMan(true, 2));
		}
		if (checkWiseMan2Sell()) {
			wiseMans.add(new WiseMan(false, 2));
		}
		if (checkWiseMan3Buy()) {
			int upIdx = confirmedUpIndex[current_index];
			if (High[upIdx] > teeth[upIdx]) {
				wiseMans.add(new WiseMan(true, 3) {
					public boolean confirm(float price) {
						return super.confirm(price) && price > teeth[current_index];
					}
				});
			}
		}
		if (checkWiseMan3Sell()) {
			int dnIdx = confirmedDnIndex[current_index];
			if (Low[dnIdx] < teeth[dnIdx]) {
				wiseMans.add(new WiseMan(false, 3) {
					public boolean confirm(float price) {
						return super.confirm(price) && price < teeth[current_index];
					}
				});
			}
		}
		return wiseMans;
	}

	protected List<EnterSignalNeedConfirm> getAddOnSignals() {
		List<EnterSignalNeedConfirm> wiseMans = new ArrayList<>();
		if (checkWiseMan2Buy()) {
			wiseMans.add(new WiseMan(true, 2));
		}
		if (checkWiseMan2Sell()) {
			wiseMans.add(new WiseMan(false, 2));
		}
		if (checkWiseMan3Buy()) {
			wiseMans.add(new WiseMan(true, 3) {
				public boolean confirm(float price) {
					return super.confirm(price) && price > teeth[current_index];
				}
			});
		}
		if (checkWiseMan3Sell()) {
			wiseMans.add(new WiseMan(false, 3) {
				public boolean confirm(float price) {
					return super.confirm(price) && price < teeth[current_index];
				}
			});
		}
		return wiseMans;
	}

	@Override
	public float onClose() {
		if (aoSignal == null) {
			aoSignal = this.new AOSignal();
		}
		if (current_index < minimumBarsToWork - 1) {
			return Close[current_index];
		}

		aoSignal.update(aoBuffer[current_index]);
		return super.onClose();
	}
}

package strategy;

import java.util.ArrayList;
import java.util.List;

import indicator.chaos.AwesomeOscillator;
import indicator.zen.SameLevelDecomposition;
import indicator.zen.Segment;
import strategy.template.AddOnTrailingStop;

public class Lemon1 extends AddOnTrailingStop {

	private SameLevelDecomposition decomp;
	protected List<Segment> segmentList = null;
	private Segment currentSegment = null;

	private final AwesomeOscillator ao;
	private float[] aoBuffer = null;
	private boolean[] aoUpSignals = null;
	private boolean[] aoDnSignals = null;

	private boolean checkUp(int i) {
		return (aoBuffer[i]		> aoBuffer[i - 1] &&
				aoBuffer[i - 1] > aoBuffer[i - 2] &&
				aoBuffer[i - 2] > aoBuffer[i - 3] &&
				aoBuffer[i - 3] > aoBuffer[i - 4] &&
				aoBuffer[i - 4] < aoBuffer[i - 5]);
	}

	private boolean checkDn(int i) {
		return (aoBuffer[i]		< aoBuffer[i - 1] &&
				aoBuffer[i - 1] < aoBuffer[i - 2] &&
				aoBuffer[i - 2] < aoBuffer[i - 3] &&
				aoBuffer[i - 3] < aoBuffer[i - 4] &&
				aoBuffer[i - 4] > aoBuffer[i - 5]);
	}

	public Lemon1(Integer strictStroke, Float gapThreshold, Integer fastMA, Integer slowMA, Float AFstep, Float AFmax) {
		super(AFstep, AFmax, new int[]{0, 1, 1, 1});
		this.decomp = new SameLevelDecomposition(strictStroke == 1, gapThreshold);
		this.ao = new AwesomeOscillator(fastMA, slowMA);
	}

	@Override
	public void reset() {
		super.reset();
		currentSegment = null;
	}

	@Override
	protected void calculateIndicators() {
		super.calculateIndicators();
		segmentList = decomp.segmentList;
		aoBuffer = ao.getBufferById(0);
		int len = aoBuffer.length;
		aoUpSignals = new boolean[len];
		aoDnSignals = new boolean[len];
		for (int i = 0; i < len; i++) {
			aoUpSignals[i] = false;
			aoDnSignals[i] = false;
		}
		for (int i = 5; i < len; i++) {
			if (checkUp(i)) {
				aoUpSignals[i] = true;
			}
			if (checkDn(i)) {
				aoDnSignals[i] = true;
			}
		}
	}

	@Override
	public float onClose() {
		if (current_index < minimumBarsToWork - 1) {
			return Close[current_index];
		}

		for (Segment s : segmentList) {
			if (s.startIndex() == current_index) {
				reset();
				currentSegment = s;
			}
		}

		return super.onClose();
	}

	private float getWiseManTriggerPrice(boolean direction, int type) {
		float ret = Float.NEGATIVE_INFINITY;
		switch (type) {
		case 1:
		case 2:
			ret = direction ? High[current_index] : Low[current_index];
			break;
		case 3:
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
			break;
		case 2:
			ret = currentSegment.startValue();
			break;
		case 3:
			break;
		default:
			break;
		}
		return ret;
	}

	class Class2WiseMan extends EnterSignalNeedConfirm {
		public final int type;
		public Class2WiseMan(boolean direction, int type) {
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

	@Override
	protected List<EnterSignalNeedConfirm> getOpenSignals() {
		List<EnterSignalNeedConfirm> wiseMans = new ArrayList<>();
		if (currentSegment != null) {
			boolean noPrevUp = true;
			boolean noPrevDn = true;
			for (int i = currentSegment.startIndex(); i < current_index; i++) {
				if (aoUpSignals[i]) {
					noPrevUp = false;
				}
				if (aoDnSignals[i]) {
					noPrevDn = false;
				}
			}
	
			if (aoUpSignals[current_index] && noPrevUp) {
				wiseMans.add(new Class2WiseMan(true, 2));
			}
			if (aoDnSignals[current_index] && noPrevDn) {
				wiseMans.add(new Class2WiseMan(false, 2));
			}
		}
		return wiseMans;
	}

	@Override
	protected List<EnterSignalNeedConfirm> getAddOnSignals() {
		return new ArrayList<EnterSignalNeedConfirm>();
	}
}

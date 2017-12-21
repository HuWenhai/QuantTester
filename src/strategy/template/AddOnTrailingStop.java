package strategy.template;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public abstract class AddOnTrailingStop extends BarBasedStrategy implements IEveryOHLC {

	private final float AFstep, AFmax;
	private TrailingStop stop = null;

	private final int[] addOnSequence;
	private final int maxAddOnIdx;
	private int volIdx = 0;

	private float highestAddon = Float.NEGATIVE_INFINITY;
	private float lowestAddon = Float.POSITIVE_INFINITY;

	public AddOnTrailingStop(Float AFstep, Float AFmax, int[] addOnSequence) {
		this.AFstep = AFstep;
		this.AFmax = AFmax;
		this.addOnSequence = addOnSequence;
		maxAddOnIdx = addOnSequence.length - 1;
	}

	public AddOnTrailingStop() {
		this(0.02f, 0.19999f, new int[]{0, 2, 7, 10});
	}

	protected List<EnterSignalNeedConfirm> openSignals = new LinkedList<>();
	protected List<EnterSignalNeedConfirm> addonSignals = new LinkedList<>();
	protected List<PriceTrigger> takenSignals = new ArrayList<>();

	@Override
	public void reset() {
		super.reset();
		volIdx = 0;
		stop = null;
		highestAddon = Float.NEGATIVE_INFINITY;
		lowestAddon = Float.POSITIVE_INFINITY;
		openSignals.clear();
		addonSignals.clear();
		takenSignals.clear();
	}

	protected abstract class EnterSignalNeedConfirm extends EnterSignal {

		public EnterSignalNeedConfirm(boolean direction, float triggerPrice, float cancelPrice) {
			super(direction, triggerPrice, cancelPrice);
		}

		public boolean confirm(float price) {
			if (direction) {
				return getTriggerPrice() > highestAddon;
			} else {
				return getTriggerPrice() < lowestAddon;
			}
		}
	}

	protected float checkPrice(float price, boolean stopable) {
		int targetVolIdx = volIdx;
		PriceTrigger trigger = null;

		// Check all signals for cancel price
		Predicate<EnterSignal> checkCancel = signal -> signal.direction ? signal.cancelPrice > price : signal.cancelPrice < price;
		openSignals.removeIf(checkCancel);
		addonSignals.removeIf(checkCancel);

		// check stop loss
		if (stop != null) {
			boolean touchDownStop = (position > 0 && price < stop.stopLoss);
			boolean touchUpStop = (position < 0 && price > stop.stopLoss);
			if (touchDownStop || touchUpStop) {
				targetVolIdx = 0;
				addonSignals.clear();
				trigger = stop;
				stop = null;
				highestAddon = Float.NEGATIVE_INFINITY;
				lowestAddon = Float.POSITIVE_INFINITY;
			}
		}

		Predicate<EnterSignalNeedConfirm> matchTrigger = signal -> signal.checkTrigger(price);
		Predicate<EnterSignalNeedConfirm> longConfirmed = matchTrigger.and(signal -> signal.direction).and(signal -> signal.confirm(price));
		Predicate<EnterSignalNeedConfirm> shortConfirmed = matchTrigger.and(signal -> !signal.direction).and(signal -> signal.confirm(price));
 
		// check open/reverse
		for (EnterSignalNeedConfirm signal : openSignals) {
			if (targetVolIdx >= 0 && shortConfirmed.test(signal)) {
				targetVolIdx = -1;
				stop = new TrailingStop(false, signal.cancelPrice, AFmax, AFstep);
				stop.lowestEver = Low[current_index];
				takenSignals.clear();
				trigger = signal;
				lowestAddon = trigger.getTriggerPrice();
				break;
			} else if (targetVolIdx <= 0 && longConfirmed.test(signal)) {
				targetVolIdx = 1;
				stop = new TrailingStop(true, signal.cancelPrice, AFmax, AFstep);
				stop.highestEver = High[current_index];
				takenSignals.clear();
				trigger = signal;
				highestAddon = trigger.getTriggerPrice();
				break;
			}
		}
		openSignals.removeIf(matchTrigger);

		// check add on
		boolean longlong = volIdx > 0 && targetVolIdx > 0;
		boolean shortshort = volIdx < 0 && targetVolIdx < 0;

		if (longlong) {
			Optional<EnterSignalNeedConfirm> match = addonSignals.stream().filter(longConfirmed).findFirst();
			if (match.isPresent()) {
				targetVolIdx++;
				if (targetVolIdx > maxAddOnIdx) {
					targetVolIdx = maxAddOnIdx;
				}
				trigger = match.get();
				highestAddon = trigger.getTriggerPrice();
			}
		} else if (shortshort) {
			Optional<EnterSignalNeedConfirm> match = addonSignals.stream().filter(shortConfirmed).findFirst();
			if (match.isPresent()) {
				targetVolIdx--;
				if (targetVolIdx < -maxAddOnIdx) {
					targetVolIdx = -maxAddOnIdx;
				}
				trigger = match.get();
				lowestAddon = trigger.getTriggerPrice();
			}
		}
		addonSignals.removeIf(matchTrigger);	// Remove triggered signals, whatever confirm or not

		float ret = Float.NEGATIVE_INFINITY;
		if (targetVolIdx > volIdx) {
			ret = stopable ? trigger.getTriggerPrice() : Math.max(price, trigger.getTriggerPrice());
		} else if (targetVolIdx < volIdx) {
			ret = stopable ? trigger.getTriggerPrice() : Math.min(price, trigger.getTriggerPrice());
		} else {
			return price;
		}
		volIdx = targetVolIdx;
		position = addOnSequence[Math.abs(volIdx)] * (volIdx > 0 ? 1 : -1);
		takenSignals.add(trigger);
		return ret;
	}

	@Override
	public float onOpen() {
		return checkPrice(Open[current_index], false);
	}

	@Override
	public float onHigh() {
		return checkPrice(High[current_index], true);
	}

	@Override
	public float onLow() {
		return checkPrice(Low[current_index], true);
	}

	protected abstract List<EnterSignalNeedConfirm> getOpenSignals();
	protected abstract List<EnterSignalNeedConfirm> getAddOnSignals();

	@Override
	public float onClose() {
		if (current_index < minimumBarsToWork - 1) {
			return Close[current_index];
		}

		float ret = checkPrice(Close[current_index], true);

		openSignals.addAll(getOpenSignals());
		addonSignals.addAll(getAddOnSignals());

		if (position != 0) {
			stop.updateStopLoss(High[current_index], Low[current_index]);
		}

		return ret;
	}
}

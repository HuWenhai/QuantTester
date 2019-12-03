package strategy.template;

// Trigger trailing reverse
public abstract class ReverseWithTrailingStop extends BarBasedStrategy implements IEveryOHLC {

	protected final float AFstep, AFmax;
	protected EnterSignal signal = null;
	protected TrailingStop stop = null;

	protected State state = State.EMPTY;

	public ReverseWithTrailingStop(Float AFstep, Float AFmax) {
		this.AFstep = AFstep;
		this.AFmax = AFmax;
	}

	public ReverseWithTrailingStop() {
		this(0.02f, 0.19999f);
	}

	@Override
	public void reset() {
		super.reset();
		state = State.EMPTY;
		signal = null;
		stop = null;
	}

	protected float stateMachine(float price, boolean stopable) {
		switch (state) {
		case EMPTY:
			break;
		case LONG:
			if (stop != null && stop.checkStopLoss(price)) {
				float ret = stopable ? stop.stopLoss : Math.min(price, stop.stopLoss);
				reset();
				return ret;
			}
			break;
		case SHORT:
			if (stop != null && stop.checkStopLoss(price)) {
				float ret = stopable ? stop.stopLoss : Math.max(price, stop.stopLoss);
				reset();
				return ret;
			}
			break;
		case WAIT_LONG:
			if (signal != null && price > signal.triggerPrice) {
				float ret = stopable ? signal.triggerPrice : Math.max(price, signal.triggerPrice);
				position = 1;
				stop = new TrailingStop(true, signal.cancelPrice, AFstep, AFmax);
				stop.highestEver = High[current_index];
				state = State.LONG;
				markDot(current_index, stop.stopLoss, 8);
				return ret;
			} else if (signal != null && price < signal.cancelPrice) {
				state = State.EMPTY;	// Cancel
			}
			break;
		case WAIT_SHORT:
			if (signal != null && price < signal.triggerPrice) {
				float ret = stopable ? signal.triggerPrice : Math.min(price, signal.triggerPrice);
				position = -1;
				stop = new TrailingStop(false, signal.cancelPrice, AFstep, AFmax);
				stop.lowestEver = Low[current_index];
				state = State.SHORT;
				markDot(current_index, stop.stopLoss, 9);
				return ret;
			} else if (signal != null && price > signal.cancelPrice) {
				state = State.EMPTY;	// Cancel
			}
			break;
		default:
			break;
		}
		return price;
	}

	@Override
	public float onOpen() {
		if (stop != null) {
			markDot(current_index, stop.stopLoss, stop.getDirection() ? 8 : 9);
		}
		return stateMachine(Open[current_index], false);
	}

	@Override
	public float onHigh() {
		return stateMachine(High[current_index], true);
	}

	@Override
	public float onLow() {
		return stateMachine(Low[current_index], true);
	}

	protected abstract EnterSignal checkEnterSignal();

	@Override
	public float onClose() {
		if (current_index < minimumBarsToWork - 1) {
			state = State.EMPTY;
			position = 0;
			return Close[current_index];
		}

		float ret = stateMachine(Close[current_index], true);

		EnterSignal newSignal = checkEnterSignal();

		switch (state) {
		case EMPTY:
		case WAIT_LONG:
		case WAIT_SHORT:
			if (newSignal != null) {
				signal = newSignal;
				if (newSignal.direction) {
					state = State.WAIT_LONG;
				} else {
					state = State.WAIT_SHORT;
				}
			}
			break;
		case LONG:
		case SHORT:
			stop.updateStopLoss(High[current_index], Low[current_index]);
			break;
		default:
			break;
		}

		return ret;
	}
}

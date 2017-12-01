package strategy.template;

public abstract class TrailingStop extends BarBasedStrategy implements IEveryOHLC {

	private final float AFstep, AFmax;
	private float stopLoss = Float.NEGATIVE_INFINITY;
	private float takeProfit = Float.NEGATIVE_INFINITY;	// Also as cancel signal
	private float AF = 0.0f;
	private float highestEver = 0.0f;
	private float lowestEver = 0.0f;

	public TrailingStop(Float AFstep, Float AFmax) {
		this.AFstep = AFstep;
		this.AFmax = AFmax;
	}

	public TrailingStop() {
		this(0.02f, 0.19999f);
	}

	public static enum State {
		EMPTY,
		WAIT_LONG,
		WAIT_SHORT,
		LONG,
		SHORT,
	}

	protected State state = null;
	protected void reset() {
		position = 0;
		state = State.EMPTY;
		stopLoss = Float.NEGATIVE_INFINITY;
		takeProfit = Float.NEGATIVE_INFINITY;
	}

	private float stateMachine(float price, boolean stopable) {
		switch (state) {
		case EMPTY:
			break;
		case LONG:
			if (stopLoss != Float.NEGATIVE_INFINITY && price < stopLoss) {
				float ret = stopable ? stopLoss : Math.min(price, stopLoss);
				reset();
				return ret;
			} else if (takeProfit != Float.NEGATIVE_INFINITY && price > takeProfit) {
				reset();
				return takeProfit;
			}
			break;
		case SHORT:
			if (stopLoss != Float.NEGATIVE_INFINITY && price > stopLoss) {
				float ret = stopable ? stopLoss : Math.max(price, stopLoss);
				reset();
				return ret;
			} else if (takeProfit != Float.NEGATIVE_INFINITY && price < takeProfit) {
				reset();
				return takeProfit;
			}
			break;
		case WAIT_LONG:
			if (stopLoss != Float.NEGATIVE_INFINITY && price > stopLoss) {
				float ret = stopable ? stopLoss : Math.max(price, stopLoss);
				position = 1;
				stopLoss = takeProfit;
				takeProfit = Float.NEGATIVE_INFINITY;
				AF = 0.0f;
				highestEver = High[current_index];
				state = State.LONG;
				return ret;
			} else if (takeProfit != Float.NEGATIVE_INFINITY && price < takeProfit) {
				state = State.EMPTY;	// Cancel
			}
			break;
		case WAIT_SHORT:
			if (stopLoss != Float.NEGATIVE_INFINITY && price < stopLoss) {
				float ret = stopable ? stopLoss : Math.min(price, stopLoss);
				position = -1;
				stopLoss = takeProfit;
				takeProfit = Float.NEGATIVE_INFINITY;
				AF = 0.0f;
				lowestEver = Low[current_index];
				state = State.SHORT;
				return ret;
			} else if (takeProfit != Float.NEGATIVE_INFINITY && price > takeProfit) {
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

	protected abstract boolean enoughBars();
	protected abstract boolean checkBuySignal();
	protected abstract boolean checkSellSignal();

	@Override
	public float onClose() {
		if (!enoughBars()) {
			state = State.EMPTY;
			position = 0;
			return Close[current_index];
		}

		float ret = stateMachine(Close[current_index], true);

		boolean buySignal = checkBuySignal();
		boolean sellSignal = checkSellSignal();

		switch (state) {
		case EMPTY:
		case WAIT_LONG:
		case WAIT_SHORT:
			if (buySignal) {
				stopLoss = High[current_index];
				takeProfit = Low[current_index];
				state = State.WAIT_LONG;
			} else if (sellSignal) {
				stopLoss = Low[current_index];
				takeProfit = High[current_index];
				state = State.WAIT_SHORT;
			}
			break;
		case LONG:
			if (High[current_index] > highestEver) {
				highestEver = High[current_index];
				if (AF < AFmax) {
					AF += AFstep;
				}
			}
			stopLoss = stopLoss + (highestEver - stopLoss) * AF;
			break;
		case SHORT:
			if (Low[current_index] < lowestEver) {
				lowestEver = Low[current_index];
				if (AF < AFmax) {
					AF += AFstep;
				}
			}
			stopLoss = stopLoss - (stopLoss - lowestEver) * AF;
			break;
		default:
			break;
		}

		return ret;
	}
}

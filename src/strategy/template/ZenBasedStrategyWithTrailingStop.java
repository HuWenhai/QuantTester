package strategy.template;

public abstract class ZenBasedStrategyWithTrailingStop extends ZenBasedStrategy {

	private final float AFstep, AFmax;
	private TrailingStop stop = null;

	public ZenBasedStrategyWithTrailingStop(boolean strictStroke, float gapThreshold, float AFstep, float AFmax) {
		super(strictStroke, gapThreshold);
		this.AFstep = AFstep;
		this.AFmax = AFmax;
	}

	protected void setPosition(int newPosition, float stopLoss) {
		this.position = newPosition;
		stop = new TrailingStop(newPosition > 0, stopLoss, AFstep, AFmax);
	}

	@Override
	public void reset() {
		super.reset();
		stop = null;
	}

	@Override
	public float onHigh() {
		float ret = High[current_index];
		// check stop loss
		if (position < 0 && stop != null && High[current_index] > stop.stopLoss) {
			ret = stop.stopLoss;
			reset();
		}
		return ret;
	}

	@Override
	public float onLow() {
		float ret = Low[current_index];
		// check stop loss
		if (position > 0 && stop != null && Low[current_index] < stop.stopLoss) {
			ret = stop.stopLoss;
			reset();
		}
		return ret;
	}

	@Override
	public float onClose() {
		super.onClose();
		if (stop != null) {
			stop.updateStopLoss(High[current_index], Low[current_index]);
		}
		return Close[current_index];
	}
}

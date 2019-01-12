package strategy.template;

public class TrailingStop implements PriceTrigger {
	private final boolean direction;
	public float stopLoss;
	private final float AFstep, AFmax;

	private boolean isNewCreate = true;
	private float AF = 0.0f;
	public float highestEver = Float.NEGATIVE_INFINITY;
	public float lowestEver = Float.POSITIVE_INFINITY;

	public TrailingStop(boolean direction, float stopLoss, float AFstep, float AFmax) {
		this.direction = direction;
		this.stopLoss = stopLoss;
		this.AFstep = AFstep;
		this.AFmax = AFmax;
	}

	public TrailingStop(boolean direction, float stopLoss) {
		this(direction, stopLoss, 0.02f, 0.19999f);
	}

	public boolean checkStopLoss(float price) {
		return (direction && price < stopLoss) || (!direction && price > stopLoss);
	}

	public void updateStopLoss(float highPrice, float lowPrice) {
		if (isNewCreate) {
			// First time, only record the high/low price, don't update AF and SL
			if (direction) {
				highestEver = highPrice;
			} else {
				lowestEver = lowPrice;
			}
			isNewCreate = false;
			return;
		}

		if (direction) {
			if (highPrice > highestEver) {
				highestEver = highPrice;
				if (AF < AFmax) {
					AF += AFstep;
				}
			}
			stopLoss = stopLoss + (highestEver - stopLoss) * AF;
		} else {
			if (lowPrice < lowestEver) {
				lowestEver = lowPrice;
				if (AF < AFmax) {
					AF += AFstep;
				}
			}
			stopLoss = stopLoss - (stopLoss - lowestEver) * AF;
		}
	}

	@Override
	public boolean getDirection() {
		return direction;
	}

	@Override
	public float getTriggerPrice() {
		return stopLoss;
	}

	@Override
	public String toString() {
		String ret = System.lineSeparator() + "TrailingStop ";
		ret += getDirection() ? "Up: StopLoss=" : "Down: StopLoss=";
		ret += stopLoss;
		ret += ";AFstep=" + AFstep;
		ret += ";AFmax=" + AFmax;
		ret += ";AF=" + AF;
		if (direction) {
			ret += ";highestEver=" + highestEver;
		} else {
			ret += ";lowestEver=" + lowestEver;
		}
		return ret;
	}
}

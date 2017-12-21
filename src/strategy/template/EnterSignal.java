package strategy.template;

public class EnterSignal implements PriceTrigger {
	public final boolean direction;
	public final float triggerPrice;
	public final float cancelPrice;

	public EnterSignal(boolean direction, float triggerPrice, float cancelPrice) {
		this.direction = direction;
		this.triggerPrice = triggerPrice;
		this.cancelPrice = cancelPrice;
	}

	// Construct unconditional enter signal
	public EnterSignal(boolean direction) {
		this(direction, direction ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY, direction ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY);
	}

	@Override
	public boolean getDirection() {
		return direction;
	}

	@Override
	public float getTriggerPrice() {
		return triggerPrice;
	}

	@Override
	public String toString() {
		String ret = System.lineSeparator() + "EnterSignal ";
		ret += getDirection() ? "Up: Trigger=" : "Down: Trigger=";
		ret += triggerPrice;
		ret += ";Cancel=";
		ret += cancelPrice;
		return ret;
	}
}

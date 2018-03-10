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

	public static final EnterSignal UNCONDITIONALLY_BUY = new EnterSignal(true, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
	public static final EnterSignal UNCONDITIONALLY_SELL = new EnterSignal(false, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY);

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

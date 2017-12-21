package strategy.template;

public interface PriceTrigger {
	public boolean getDirection();
	public float getTriggerPrice();
	public default boolean checkTrigger(float price) {
		return getDirection() ? getTriggerPrice() < price : getTriggerPrice() > price;
	}
}

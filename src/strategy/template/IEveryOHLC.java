package strategy.template;

import trade.ITradeable;

public interface IEveryOHLC extends IStrategy {
	default public void tradeOneBar(ITradeable trader) {
		trader.setTime(getCurrentTime());
		trader.setPosition(onOpen(), getPosition());
		if (getPosition() <= 0) {
			trader.setPosition(onHigh(), getPosition());
			trader.setPosition(onLow(), getPosition());
		} else {
			trader.setPosition(onLow(), getPosition());
			trader.setPosition(onHigh(), getPosition());
		}
		trader.setPosition(onClose(), getPosition());
	}

	public float onOpen();

	public float onHigh();

	public float onLow();

	public float onClose();

}

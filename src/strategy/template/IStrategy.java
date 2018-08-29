package strategy.template;

import trade.ITradeable;

public interface IStrategy {
	public void tradeOneBar(ITradeable trader);
	public int getPosition();
	public int getCurrentTime();
}

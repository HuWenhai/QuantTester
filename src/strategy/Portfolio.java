package strategy;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Portfolio {

	private static final Logger logger = LogManager.getLogger();

	public float margin = 0.0f;
	private String[] instruments;
    private Map<String, Float> commissionRatios = new HashMap<>();
    private Map<String, Float> marginRatios = new HashMap<>();
    private Map<String, Integer> ydLongPositions = new HashMap<>();
    private Map<String, Integer> ydShortPositions = new HashMap<>();
    private Map<String, Integer> tdLongPositions = new HashMap<>();
    private Map<String, Integer> tdShortPositions = new HashMap<>();
    private Map<String, Float> openLongAverages = new HashMap<>();
    private Map<String, Float> openShortAverages = new HashMap<>();

	public Portfolio(float initCash, String[] instruments) {
		this.margin = initCash;
		this.instruments = instruments;
		for (String instrument : instruments) {
			ydLongPositions.put(instrument, 0);
			ydShortPositions.put(instrument, 0);
			tdLongPositions.put(instrument, 0);
			tdShortPositions.put(instrument, 0);
			openLongAverages.put(instrument, 0.0f);
			openShortAverages.put(instrument, 0.0f);
		}
	}

	@Deprecated
	int getLongVolume() {
		return getLongVolume("common");
	}

	int getLongVolume(String instrument) {
		int ydLongPosition = ydLongPositions.get(instrument);
		int tdLongPosition = tdLongPositions.get(instrument);
		return ydLongPosition + tdLongPosition;
	}

	@Deprecated
	int getShortVolume() {
		return getShortVolume("common");
	}

	int getShortVolume(String instrument) {
		int ydShortPosition = ydShortPositions.get(instrument);
		int tdShortPosition = tdShortPositions.get(instrument);
		return ydShortPosition + tdShortPosition;
	}

	/**
	 * @see http://www.shfe.com.cn/bourseService/activity/basics/211231978.html
	 */
	public float settle(AbstractMap.SimpleEntry<String, Float>[] settlementPrices) {
		this.margin += getHoldProfit(settlementPrices);
		for (AbstractMap.SimpleEntry<String, Float> item : settlementPrices) {
			String instrument = item.getKey();
			int ydLongPosition = ydLongPositions.get(instrument);
			int ydShortPosition = ydShortPositions.get(instrument);
			int tdLongPosition = tdLongPositions.get(instrument);
			int tdShortPosition = tdShortPositions.get(instrument);

			int totalLongPosition = ydLongPosition + tdLongPosition;
			int totalShortPosition = ydShortPosition + tdShortPosition;
			ydLongPositions.put(instrument, totalLongPosition);
			ydShortPositions.put(instrument, totalShortPosition);
			tdLongPositions.put(instrument, 0);
			tdShortPositions.put(instrument, 0);

			float settlementPrice = item.getValue();
			openLongAverages.put(instrument, settlementPrice);
			openShortAverages.put(instrument, settlementPrice);
		}
		return this.margin;
	}

	public float getHoldProfit(AbstractMap.SimpleEntry<String, Float>[] latestMarkets) {
		float profit = 0.0f;
		for (AbstractMap.SimpleEntry<String, Float> item : latestMarkets) {
			String instrument = item.getKey();
			int ydLongPosition = ydLongPositions.get(instrument);
			int ydShortPosition = ydShortPositions.get(instrument);
			int tdLongPosition = tdLongPositions.get(instrument);
			int tdShortPosition = tdShortPositions.get(instrument);
			float openLongAverage = openLongAverages.get(instrument);
			float openShortAverage = openShortAverages.get(instrument);
			float latestPrice = item.getValue();
			float holdProfit = (ydLongPosition + tdLongPosition) * (latestPrice - openLongAverage)
							+ (ydShortPosition + tdShortPosition) * (openShortAverage - latestPrice);
			profit += holdProfit;
		}
		return profit;
	}

	@Deprecated
	public float getBalance(float price) {
		AbstractMap.SimpleEntry<String, Float> latestMarket = new AbstractMap.SimpleEntry<String, Float>("common", price);
		@SuppressWarnings("unchecked")
		AbstractMap.SimpleEntry<String, Float>[] latestMarkets = new AbstractMap.SimpleEntry[1];
		latestMarkets[0] = latestMarket;
		return getBalance(latestMarkets);
	}

	public float getBalance(AbstractMap.SimpleEntry<String, Float>[] latestMarkets) {
		return this.margin + getHoldProfit(latestMarkets);
	}

	@Deprecated
	public float getAvailable(final float price) {
		AbstractMap.SimpleEntry<String, Float> latestMarket = new AbstractMap.SimpleEntry<String, Float>("common", price);
		@SuppressWarnings("unchecked")
		AbstractMap.SimpleEntry<String, Float>[] latestMarkets = new AbstractMap.SimpleEntry[1];
		latestMarkets[0] = latestMarket;
		
		return getAvailable(latestMarkets);
	}

	public float getAvailable(AbstractMap.SimpleEntry<String, Float>[] latestMarkets) {
		// TODO 考虑单向大边
		float frozen = 0.0f;
		for (AbstractMap.SimpleEntry<String, Float> item : latestMarkets) {
			String instrument = item.getKey();
			int ydLongPosition = ydLongPositions.get(instrument);
			int ydShortPosition = ydShortPositions.get(instrument);
			int tdLongPosition = tdLongPositions.get(instrument);
			int tdShortPosition = tdShortPositions.get(instrument);
			float latestPrice = item.getValue();
			float marginRatio = getMarginRatio(instrument);
			float longFrozen = (ydLongPosition + tdLongPosition) * latestPrice * marginRatio;
			float shortFrozen = (ydShortPosition + tdShortPosition) * latestPrice * marginRatio;
			frozen += (longFrozen + shortFrozen);
		}
		return getBalance(latestMarkets) - frozen;
	}

	// 扣除交易成本, 目前只考虑手续费(双边) + 1个滑点
	private void cost(final float amount) {
		this.margin -= (amount * getCommission_ratio());
	}

	@Deprecated
	public final float getMargin_ratio() {
		return marginRatios.get("common");
	}

	public float getMarginRatio(String instrument) {
		if (marginRatios.containsKey(instrument)) {
			return marginRatios.get(instrument);
		} else {
			return 1.0f;
		}
	}

	@Deprecated
	public final void setMargin_ratio(final float margin_ratio) {
		setMarginRatio("common", margin_ratio);
	}

	public void setMarginRatio(String instrument, float marginRatio) {
		marginRatios.put(instrument, marginRatio);
	}

	@Deprecated
	public final float getCommission_ratio() {
		return commissionRatios.get("common");
	}

	public float getCommissionRatio(String instrument) {
		if (commissionRatios.containsKey(instrument)) {
			return commissionRatios.get(instrument);
		} else {
			return 0.001f;
		}
	}

	@Deprecated
	public final void setCommission_ratio(final float commission_ratio) {
		setCommissionRatio("common", commission_ratio);
	}

	public void setCommissionRatio(String intrument, float commissionRatio) {
		commissionRatios.put(intrument, commissionRatio);
	}

	public Portfolio(float init_cash) {
		this(init_cash, new String[] {"common"});
	}

	@Deprecated
	public boolean openLong(float price, float volume) {
		if (getAvailable(price) / price > volume) {
			openLong("common", price, (int) volume);
			return true;
		} else {
			return false;
		}
	}

	public void openLong(String instrument, float price, int volume) {
		int ydLongPosition = ydLongPositions.get(instrument);
		int tdLongPosition = tdLongPositions.get(instrument);
		int totalPosition = ydLongPosition + tdLongPosition;
		if (totalPosition == 0) {
			openLongAverages.put(instrument, price);
		} else {
			float openLongAverage = openLongAverages.get(instrument);
			openLongAverage = (openLongAverage * totalPosition + price * volume) / (totalPosition + volume);
			openLongAverages.put(instrument, openLongAverage);
		}
		tdLongPosition += volume;
		tdLongPositions.put(instrument, tdLongPosition);

		cost(price * volume);
		long_trades ++;
		logger.info("OpenLong:   price = {}, volume = {}", price, volume);
	}

	@Deprecated
	public boolean openShort(float price, float volume) {
		if (getAvailable(price) / price > volume) {
			openShort("common", price, (int) volume);
			return true;
		} else {
			return false;
		}
	}

	public void openShort(String instrument, float price, int volume) {
		int ydShortPosition = ydShortPositions.get(instrument);
		int tdShortPosition = tdShortPositions.get(instrument);
		int totalPosition = ydShortPosition + tdShortPosition;
		if (totalPosition == 0) {
			openShortAverages.put(instrument, price);
		} else {
			float openShortAverage = openShortAverages.get(instrument);
			openShortAverage = (openShortAverage * totalPosition + price * volume) / (totalPosition + volume);
			openShortAverages.put(instrument, openShortAverage);
		}
		tdShortPosition += volume;
		tdShortPositions.put(instrument, tdShortPosition);

		cost(price * volume);
		short_trades ++;
		logger.info("OpenShort:  price = {}, volume = {}", price, volume);
	}

	@Deprecated
	public boolean closeLong(float price, float volume) {
		closeLong("common", price, (int) volume);
		return true;
	}

	public void closeLong(String instrument, float price, int volume) {
		int ydLongPosition = ydLongPositions.get(instrument);
		int tdLongPosition = tdLongPositions.get(instrument);
		float openLongAverage = openLongAverages.get(instrument);
		int closeYd = Math.min(ydLongPosition, volume);
		int closeTd = Math.min(volume - closeYd, tdLongPosition);
		ydLongPosition -= closeYd;
		tdLongPosition -= closeTd;
		ydLongPositions.put(instrument, ydLongPosition);
		tdLongPositions.put(instrument, tdLongPosition);
		this.margin += (price - openLongAverage) * volume;

		cost(price * volume);
		logger.info("CloseLong:  price = {}, volume = {}", price, volume);
	}

	@Deprecated
	public boolean closeShort(float price, float volume) {
		closeShort("common", price, (int) volume);
		return true;
	}

	public void closeShort(String instrument, float price, int volume) {
		int ydShortPosition = ydShortPositions.get(instrument);
		int tdShortPosition = tdShortPositions.get(instrument);
		float openShortAverage = openShortAverages.get(instrument);
		int closeYd = Math.min(ydShortPosition, volume);
		int closeTd = Math.min(volume - closeYd, tdShortPosition);
		ydShortPosition -= closeYd;
		tdShortPosition -= closeTd;
		ydShortPositions.put(instrument, ydShortPosition);
		tdShortPositions.put(instrument, tdShortPosition);
		this.margin += (openShortAverage - price) * volume;

		cost(price * volume);
		logger.info("CloseShort: price = {}, volume = {}", price, volume);
	}

	public boolean hasNoPosition() {
		for (String instrument : instruments) {
			if (!hasNoPosition(instrument)) {
				return false;
			}
		}
		return true;
	}

	public boolean hasNoPosition(String instrument) {
		int ydLongPosition = ydLongPositions.get(instrument);
		int ydShortPosition = ydShortPositions.get(instrument);
		int tdLongPosition = tdLongPositions.get(instrument);
		int tdShortPosition = tdShortPositions.get(instrument);
		return (ydLongPosition == 0 && ydShortPosition == 0 && tdLongPosition == 0 && tdShortPosition == 0);
	}

	public int long_trades = 0;
	public int short_trades = 0;
}

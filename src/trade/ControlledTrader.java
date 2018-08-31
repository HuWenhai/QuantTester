package trade;

import strategy.Portfolio;

public class ControlledTrader extends IdealTrader {

	public boolean allow_open = true;
	public boolean allow_buy  = true;
	public boolean allow_sell = true;

	public ControlledTrader(Portfolio portfolio, boolean saveDetail, float vol_unit) {
		super(portfolio, saveDetail, vol_unit);
	}

	public ControlledTrader(Portfolio portfolio, boolean saveDetail) {
		super(portfolio, saveDetail);
	}

	@Override
	protected void openLong(float price, int vol_num) {
		if (allow_open && allow_buy) {
			super.openLong(price, vol_num);
		}
	}

	@Override
	protected void openShort(float price, int vol_num) {
		if (allow_open && allow_sell) {
			super.openShort(price, vol_num);
		}
	}

	@Override
	protected void closeLong(float price, int vol_num) {
		if (allow_sell) {
			super.closeLong(price, vol_num);
		}
	}

	@Override
	protected void closeShort(float price, int vol_num) {
		if (allow_buy) {
			super.closeShort(price, vol_num);
		}
	}
}

package trade;

import strategy.Portfolio;
import tester.ActionDetail;

public class IdealTrader implements ITradeable {

	protected int position = 0;
	protected final float vol_unit;
	protected final Portfolio portfolio;

	protected boolean saveDetail;
	protected ActionDetail actionDetail = null;
	protected int currentMonth = -1;
	protected int currentTime = -1;

	public IdealTrader(Portfolio portfolio, boolean saveDetail, float vol_unit) {
		this.portfolio = portfolio;
		this.vol_unit = vol_unit;
		this.saveDetail = saveDetail;
		if (saveDetail) {
			actionDetail = new ActionDetail();
		}
	}

	public IdealTrader(Portfolio portfolio, boolean saveDetail) {
		this(portfolio, saveDetail, 1.0f);
	}

	public void setMonth(int month) {
		// FIXME
		this.currentMonth = month + 1;
	}

	public void setTime(int time) {
		this.currentTime = time;
	}

	public ActionDetail getActionDetail() {
		return this.actionDetail;
	}

	@Override
	public void setPosition(float price, int new_position) {
		if (new_position == position) {
			return;
		}

		if (new_position >= 0 && position < 0) {
			closeShort(price, -position);
			if (position != 0) {
				return;
			}
		} else if (new_position <= 0 && position > 0) {
			closeLong(price, position);
			if (position != 0) {
				return;
			}
		}

		int diff = new_position - position;

		if (diff < 0 && new_position > 0) {
			closeLong(price, -diff);
		} else if (0 >= position &&  diff < 0) {
			openShort(price, -diff);
		} else if (0 <= position &&  diff > 0) {
			openLong(price, diff);
		} else if (diff > 0 && new_position < 0) {
			closeShort(price, diff);
		}
	}

	protected void openLong(float price, int vol_num) {
		if (portfolio.openLong(price, vol_num * vol_unit)) {
			position += vol_num;
			if (saveDetail) {
				actionDetail.append(currentMonth, currentTime, price, (int) (vol_num * vol_unit), true, true);
			}
		}
	}

	protected void openShort(float price, int vol_num) {
		if (portfolio.openShort(price, vol_num * vol_unit)) {
			position -= vol_num;
			if (saveDetail) {
				actionDetail.append(currentMonth, currentTime, price, (int) (vol_num * vol_unit), false, true);
			}
		}
	}

	protected void closeLong(float price, int vol_num) {
		if (portfolio.closeLong(price, vol_num * vol_unit)) {
			position -= vol_num;
			if (saveDetail) {
				actionDetail.append(currentMonth, currentTime, price, (int) (vol_num * vol_unit), false, false);
			}
		}
	}

	protected void closeShort(float price, int vol_num) {
		if (portfolio.closeShort(price, vol_num * vol_unit)) {
			position += vol_num;
			if (saveDetail) {
				actionDetail.append(currentMonth, currentTime, price, (int) (vol_num * vol_unit), true, false);
			}
		}
	}
}

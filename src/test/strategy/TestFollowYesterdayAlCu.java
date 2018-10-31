package test.strategy;

import data.IDataSource;
import data.TIME_FRAME;
import data.foxtrade.KTExportFutures;
import data.struct.BarSeries;
import drawing.ResultDrawing;
import helper.ReflectHelper;
import strategy.FollowYesterdayStrategy;
import strategy.Portfolio;
import strategy.template.BarBasedStrategy;
import trade.ITradeable;
import trade.IdealTrader;

public class TestFollowYesterdayAlCu {

	public static void main(String[] args) {
		BarBasedStrategy strategy = new FollowYesterdayStrategy(1, false);
		
		IDataSource export_al = new KTExportFutures("ag", 0, TIME_FRAME.DAY);
		BarSeries ohlcv_al = export_al.getBarSeries(0, TIME_FRAME.DAY);
		
		IDataSource export_cu = new KTExportFutures("au", 0, TIME_FRAME.DAY);
		BarSeries ohlcv_cu = export_cu.getBarSeries(0, TIME_FRAME.DAY);

		float[] cu_open = ohlcv_cu.opens;
		float[] cu_close = ohlcv_cu.closes;
		
		float[] al_open = ohlcv_al.opens;
		float[] al_close = ohlcv_al.closes;
		
		float[] cu_al_open = new float[al_open.length];
		float[] cu_al_close = new float[al_open.length];
		for (int i = 0; i < al_open.length; i++) {
			cu_al_open [al_open.length - i - 1] = cu_open [cu_open.length - i - 1] / al_open [al_open.length - i - 1];
			cu_al_close[al_open.length - i - 1] = cu_close[cu_open.length - i - 1] / al_close[al_open.length - i - 1];
		}
		
		ReflectHelper.setPrivateField(strategy, "Open", cu_al_open);
		ReflectHelper.setPrivateField(strategy, "Close", cu_al_close);
		
		Portfolio portfolio = new Portfolio(1000000.0f);
		portfolio.setCommission_ratio(0.0001f);
		portfolio.setMargin_ratio(1.0f);
		
		final int len = cu_al_close.length;
		float[] balance = new float[len];
		ITradeable trader = new IdealTrader(portfolio, false);
		for (int i = 0; i < len; i++) {
			strategy.calcNextBar(trader);
			balance[i] = portfolio.getBalance(cu_al_close[i]);
		}
		
		System.out.println(balance[balance.length - 1]);
		
		new ResultDrawing().drawScatterDiagram(strategy.getClass().getSimpleName() + ".png", balance);
	}
}

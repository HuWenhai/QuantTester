package test.indicator;

import data.IDataSource;
import data.TIME_FRAME;
import data.foxtrade.KTExportStocks;
import data.struct.BarSeries;
import drawing.ChartDrawing;
import indicator.APPLIED_PRICE;
import indicator.IIndicator;
import indicator.RSI;

public final class TestRSI {

	public static void main(String[] args) {
		IDataSource export = new KTExportStocks("000300.SH", TIME_FRAME.DAY);
		BarSeries ohlcv = export.getBarSeries(0, TIME_FRAME.DAY);
		float[] Open = ohlcv.opens;
		float[] High = ohlcv.highs;
		float[] Low = ohlcv.lows;
		float[] Close = ohlcv.closes;

		IIndicator rsi = new RSI(14, APPLIED_PRICE.PRICE_CLOSE);
		rsi.calculate(Open, High, Low, Close);
		float[] rsi_buffer = rsi.getBufferById(0);

		new ChartDrawing().drawBars(ohlcv)
		.drawBufferOnSeparate(rsi_buffer, 0, true)
		.actualDraw()
		.writeToFile("rsi.png");
	}
}

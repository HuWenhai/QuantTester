package test.indicator;

import data.IDataSource;
import data.TIME_FRAME;
import data.foxtrade.KTExportStocks;
import data.struct.BarSeries;
import drawing.ChartDrawing;
import indicator.IIndicator;
import indicator.MACD;

public final class TestMACD {

	public static void main(String[] args) {
		IDataSource export = new KTExportStocks("000300.SH", TIME_FRAME.DAY);
		BarSeries ohlcv = export.getBarSeries(0, TIME_FRAME.DAY);
		float[] Open = ohlcv.opens;
		float[] High = ohlcv.highs;
		float[] Low = ohlcv.lows;
		float[] Close = ohlcv.closes;

		IIndicator macd = new MACD();
		macd.calculate(Open, High, Low, Close);
		float[] macd_main_buffer = macd.getBufferById(0);
		float[] macd_signal_buffer = macd.getBufferById(1);

		new ChartDrawing().drawBars(ohlcv)
		.drawBufferOnSeparate(macd_main_buffer, 0, true)
		.drawBufferOnSeparate(macd_signal_buffer, 0, true)
		.actualDraw()
		.writeToFile("macd.png");
	}
}

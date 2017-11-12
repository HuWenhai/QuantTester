package test.indicator;

import data.IDataSource;
import data.TIME_FRAME;
import data.foxtrade.KTExportStocks;
import data.struct.BarSeries;
import drawing.ChartDrawing;
import indicator.APPLIED_PRICE;
import indicator.CCI;
import indicator.IIndicator;

public final class TestCCI {

	public static void main(String[] args) {
		IDataSource export = new KTExportStocks("000300.SH", TIME_FRAME.DAY);
		BarSeries ohlcv = export.getBarSeries(0, TIME_FRAME.DAY);
		float[] Open = ohlcv.opens;
		float[] High = ohlcv.highs;
		float[] Low = ohlcv.lows;
		float[] Close = ohlcv.closes;

		IIndicator cci = new CCI(14, APPLIED_PRICE.PRICE_TYPICAL);
		cci.calculate(Open, High, Low, Close);
		float[] cci_buffer = cci.getBufferById(0);

		new ChartDrawing().drawBars(ohlcv)
		.drawBufferOnSeparate(cci_buffer, 0, true)
		.actualDraw()
		.writeToFile("cci.png");
	}
}

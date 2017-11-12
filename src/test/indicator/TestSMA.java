package test.indicator;

import data.IDataSource;
import data.TIME_FRAME;
import data.foxtrade.KTExportStocks;
import data.struct.BarSeries;
import drawing.ChartDrawing;
import indicator.IIndicator;
import indicator.MA;

public final class TestSMA {

	public static void main(String[] args) {
		IDataSource export = new KTExportStocks("000300.SH", TIME_FRAME.DAY);
		BarSeries ohlcv = export.getBarSeries(0, TIME_FRAME.DAY);
		float[] Open = ohlcv.opens;
		float[] High = ohlcv.highs;
		float[] Low = ohlcv.lows;
		float[] Close = ohlcv.closes;

		int Period = 40;
		IIndicator ema = new MA(Period, MA.MODE_SMA);
		ema.calculate(Open, High, Low, Close);
		float[] ema_buffer = ema.getBufferById(0);

		new ChartDrawing().drawBars(ohlcv)
		.drawBufferOnMain(ema_buffer, Period - 1, true)
		.actualDraw()
		.writeToFile("sma.png");
	}
}

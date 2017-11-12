package test.indicator;

import data.IDataSource;
import data.TIME_FRAME;
import data.foxtrade.KTExportStocks;
import data.struct.BarSeries;
import drawing.ChartDrawing;
import indicator.IIndicator;
import indicator.Price_Channel;

public final class TestPriceChannel {

	public static void main(String[] args) {
		IDataSource export = new KTExportStocks("000300.SH", TIME_FRAME.DAY);
		BarSeries ohlcv = export.getBarSeries(0, TIME_FRAME.DAY);
		float[] Open = ohlcv.opens;
		float[] High = ohlcv.highs;
		float[] Low = ohlcv.lows;
		float[] Close = ohlcv.closes;

		int period = 20;
		IIndicator channel = new Price_Channel(period);
		channel.calculate(Open, High, Low, Close);
		float[] high_buffer = channel.getBufferById(0);
		float[] low_buffer = channel.getBufferById(1);

		new ChartDrawing().drawBars(ohlcv)
		.drawBufferOnMain(high_buffer, period - 1, true)
		.drawBufferOnMain(low_buffer, period - 1, true)
		.actualDraw()
		.writeToFile("price_channel.png");
	}
}

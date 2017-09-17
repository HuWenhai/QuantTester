package test.indicator;

import data.IDataSource;
import data.TIME_FRAME;
import data.foxtrade.KTExportStocks;
import data.struct.BarSeries;
import drawing.ChartDrawing;
import indicator.BollingerBand;
import indicator.IIndicator;

public class TestBB {

	public static void main(String[] args) {
		IDataSource export = new KTExportStocks("000300.SH", TIME_FRAME.DAY);
		BarSeries ohlcv = export.getBarSeries(0, TIME_FRAME.DAY);
		float[] Open = ohlcv.opens;
		float[] High = ohlcv.highs;
		float[] Low = ohlcv.lows;
		float[] Close = ohlcv.closes;

		IIndicator bb = new BollingerBand();
		bb.calculate(Open, High, Low, Close);
		float[] ml_buffer = bb.getBufferById(0);
		float[] tl_buffer = bb.getBufferById(1);
		float[] bl_buffer = bb.getBufferById(2);

		new ChartDrawing().drawBars(ohlcv)
		.drawBufferOnMain(ml_buffer, 0)
		.drawBufferOnMain(tl_buffer, 0)
		.drawBufferOnMain(bl_buffer, 0)
		.actualDraw()
		.writeToFile("bb.png");;
	}
}

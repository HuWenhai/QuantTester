package test.indicator;

import data.IDataSource;
import data.TIME_FRAME;
import data.foxtrade.KTExportStocks;
import data.struct.BarSeries;
import drawing.ChartDrawing;
import indicator.AMA;
import indicator.IIndicator;

public class TestAMA {

	public static void main(String[] args) {
		IDataSource export = new KTExportStocks("000300.SH", TIME_FRAME.DAY);
		BarSeries ohlcv = export.getBarSeries(0, TIME_FRAME.DAY);
		float[] Open = ohlcv.opens;
		float[] High = ohlcv.highs;
		float[] Low = ohlcv.lows;
		float[] Close = ohlcv.closes;

		IIndicator ama = new AMA();
		ama.calculate(Open, High, Low, Close);
		float[] ama_buffer = ama.getBufferById(0);

		new ChartDrawing().drawBars(ohlcv)
		.drawBufferOnMain(ama_buffer, 29, true)
		.actualDraw()
		.writeToFile("ama.png");
	}
}

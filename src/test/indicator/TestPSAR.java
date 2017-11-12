package test.indicator;

import data.IDataSource;
import data.TIME_FRAME;
import data.foxtrade.KTExportStocks;
import data.struct.BarSeries;
import drawing.ChartDrawing;
import indicator.IIndicator;
import indicator.ParabolicSAR;

public final class TestPSAR {

	public static void main(String[] args) {
		IDataSource export = new KTExportStocks("000300.SH", TIME_FRAME.DAY);
		BarSeries ohlcv = export.getBarSeries(0, TIME_FRAME.DAY);
		float[] Open = ohlcv.opens;
		float[] High = ohlcv.highs;
		float[] Low = ohlcv.lows;
		float[] Close = ohlcv.closes;

		IIndicator psar = new ParabolicSAR();
		psar.calculate(Open, High, Low, Close);
		float[] psar_buffer = psar.getBufferById(0);

		new ChartDrawing().drawBars(ohlcv)
		.drawBufferOnMain(psar_buffer, 0, false)
		.actualDraw()
		.writeToFile("psar.png");
	}
}

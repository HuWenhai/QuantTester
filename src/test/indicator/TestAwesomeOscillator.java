package test.indicator;

import java.awt.Color;

import data.IDataSource;
import data.TIME_FRAME;
import data.foxtrade.KTExportStocks;
import data.struct.BarSeries;
import drawing.ChartDrawing;
import indicator.IIndicator;
import indicator.chaos.AwesomeOscillator;

public class TestAwesomeOscillator {

	public static void main(String[] args) {
		IDataSource export = new KTExportStocks("000300.SH", TIME_FRAME.DAY);
		BarSeries ohlcv = export.getBarSeries(0, TIME_FRAME.DAY);
		float[] Open = ohlcv.opens;
		float[] High = ohlcv.highs;
		float[] Low = ohlcv.lows;
		float[] Close = ohlcv.closes;

		IIndicator awesomeOscillator = new AwesomeOscillator();
		awesomeOscillator.calculate(Open, High, Low, Close);
		float[] ao = awesomeOscillator.getBufferById(0);

		new ChartDrawing().drawBars(ohlcv)
		.drawBufferOnSeparate(ao, 0, false, Color.BLUE)
		.actualDraw()
		.writeToFile("AwesomeOscillator.png");
	}
}

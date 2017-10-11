package test.indicator;

import java.awt.Color;

import data.IDataSource;
import data.TIME_FRAME;
import data.foxtrade.KTExportStocks;
import data.struct.BarSeries;
import drawing.ChartDrawing;
import indicator.IIndicator;
import indicator.chaos.Alligator;

public class TestAlligator {

	public static void main(String[] args) {
		IDataSource export = new KTExportStocks("000300.SH", TIME_FRAME.DAY);
		BarSeries ohlcv = export.getBarSeries(0, TIME_FRAME.DAY);
		float[] Open = ohlcv.opens;
		float[] High = ohlcv.highs;
		float[] Low = ohlcv.lows;
		float[] Close = ohlcv.closes;

		IIndicator alligator = new Alligator();
		alligator.calculate(Open, High, Low, Close);
		float[] jaws = alligator.getBufferById(0);
		float[] teeth = alligator.getBufferById(1);
		float[] lips = alligator.getBufferById(2);

		new ChartDrawing().drawBars(ohlcv)
		.drawBufferOnMain(jaws, 0, Color.BLUE)
		.drawBufferOnMain(teeth, 0, Color.RED)
		.drawBufferOnMain(lips, 0, Color.GREEN)
		.actualDraw()
		.writeToFile("alligator.png");;
	}
}

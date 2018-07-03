package test.indicator;

import data.IDataSource;
import data.TIME_FRAME;
import data.sinyee.SinYeeDataSource;
import data.struct.BarSeries;
import drawing.ChartDrawing;
import indicator.ADX;
import indicator.IIndicator;

public class TestADX {

	public static void main(String[] args) {
		IDataSource export = new SinYeeDataSource("RB");
		BarSeries ohlcv = export.getBarSeries(1, TIME_FRAME.MIN15);
		float[] Open = ohlcv.opens;
		float[] High = ohlcv.highs;
		float[] Low = ohlcv.lows;
		float[] Close = ohlcv.closes;

		IIndicator adx = new ADX();
		adx.calculate(Open, High, Low, Close);
		float[] adx_buffer = adx.getBufferById(0);

		new ChartDrawing(100, 200).drawBars(ohlcv)
		.drawBufferOnSeparate(adx_buffer, 0, true)
		.actualDraw(0, 900)
		.writeToFile("adx.png");
	}

}

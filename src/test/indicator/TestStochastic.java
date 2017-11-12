package test.indicator;

import data.IDataSource;
import data.TIME_FRAME;
import data.foxtrade.KTExportStocks;
import data.struct.BarSeries;
import drawing.ChartDrawing;
import indicator.IIndicator;
import indicator.Stochastic;

public final class TestStochastic {

	public static void main(String[] args) {
		IDataSource export = new KTExportStocks("000300.SH", TIME_FRAME.DAY);
		BarSeries ohlcv = export.getBarSeries(0, TIME_FRAME.DAY);
		float[] Open = ohlcv.opens;
		float[] High = ohlcv.highs;
		float[] Low = ohlcv.lows;
		float[] Close = ohlcv.closes;

		IIndicator stochastic = new Stochastic(22,5,5);
		stochastic.calculate(Open, High, Low, Close);
		float[] stochastic_main_buffer = stochastic.getBufferById(0);
		float[] stochastic_signal_buffer = stochastic.getBufferById(1);

		new ChartDrawing().drawBars(ohlcv)
		.drawBufferOnSeparate(stochastic_main_buffer, 0, true)
		.drawBufferOnSeparate(stochastic_signal_buffer, 0, true)
		.actualDraw()
		.writeToFile("stochastic.png");
	}
}

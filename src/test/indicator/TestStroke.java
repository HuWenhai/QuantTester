package test.indicator;

import java.awt.Color;
import java.util.List;

import data.IDataSource;
import data.TIME_FRAME;
import data.foxtrade.KTExportStocks;
import data.struct.BarSeries;
import drawing.ChartDrawing;
import indicator.IIndicator;
import indicator.zen.Stroke;

public class TestStroke {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		IDataSource export = new KTExportStocks("000300.SH", TIME_FRAME.DAY);
		BarSeries ohlcv = export.getBarSeries(0, TIME_FRAME.DAY);
		float[] Open = ohlcv.opens;
		float[] High = ohlcv.highs;
		float[] Low = ohlcv.lows;
		float[] Close = ohlcv.closes;

		IIndicator stroke = new Stroke();
		stroke.calculate(Open, High, Low, Close);
		List<Integer> strokeList = (List<Integer>) helper.ReflectHelper.getPrivateField(stroke, "strokeList");
		List<Float> strokeBufferList = (List<Float>) helper.ReflectHelper.getPrivateField(stroke, "strokeBufferList");

		new ChartDrawing().drawBars(ohlcv)
		.drawLinesOnMain(strokeList, strokeBufferList, Color.YELLOW)
		.actualDraw(0, 3000)
		.writeToFile("stroke.png");
	}
}

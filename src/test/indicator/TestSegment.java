package test.indicator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import data.IDataSource;
import data.TIME_FRAME;
import data.foxtrade.KTExportStocks;
import data.struct.BarSeries;
import drawing.ChartDrawing;
import helper.ReflectHelper;
import indicator.IIndicator;
import indicator.zen.Segment;

public class TestSegment {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		IDataSource export = new KTExportStocks("000300.SH", TIME_FRAME.DAY);
		BarSeries ohlcv = export.getBarSeries(0, TIME_FRAME.DAY);
		float[] Open = ohlcv.opens;
		float[] High = ohlcv.highs;
		float[] Low = ohlcv.lows;
		float[] Close = ohlcv.closes;

		IIndicator segment = new Segment();
		segment.calculate(Open, High, Low, Close);
		List<Integer> strokeList = (List<Integer>) ReflectHelper.getPrivateField(segment, "strokeList");
		List<Float> strokeBufferList = (List<Float>) ReflectHelper.getPrivateField(segment, "strokeBufferList");
		List<Integer> segmentList = (List<Integer>) ReflectHelper.getPrivateField(segment, "segmentList");
		List<Float> segmentBufferList = (List<Float>) ReflectHelper.getPrivateField(segment, "segmentBufferList");

		List<Integer> segmentList2 = new ArrayList<>();
		final int segmentListSize = segmentList.size();
		for (int i = 0; i < segmentListSize; i++) {
			segmentList2.add(strokeList.get(segmentList.get(i)));
		}
		new ChartDrawing().drawBars(ohlcv)
		.drawLinesOnMain(strokeList, strokeBufferList, Color.YELLOW)
		.drawLinesOnMain(segmentList2, segmentBufferList, Color.WHITE)
		.actualDraw(0, 3000)
		.writeToFile("segment.png");
	}
}

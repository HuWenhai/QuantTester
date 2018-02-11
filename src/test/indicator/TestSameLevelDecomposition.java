package test.indicator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import data.IDataSource;
import data.TIME_FRAME;
import data.foxtrade.KTExportFutures;
import data.foxtrade.KTExportStocks;
import data.struct.BarSeries;
import drawing.ChartDrawing;
import indicator.zen.Center;
import indicator.zen.SameLevelDecomposition;
import indicator.zen.Segment;
import indicator.zen.Stroke;

public class TestSameLevelDecomposition {

	public static void main(String[] args) {
		IDataSource export = new KTExportStocks("000300.SH", TIME_FRAME.DAY);
		//IDataSource export = new KTExportFutures("ru");
		BarSeries ohlcv = export.getBarSeries(0, TIME_FRAME.DAY);
		float[] Open = ohlcv.opens;
		float[] High = ohlcv.highs;
		float[] Low = ohlcv.lows;
		float[] Close = ohlcv.closes;

		SameLevelDecomposition sd = new SameLevelDecomposition(true, 1.0f);
		sd.calculate(Open, High, Low, Close);
		List<Stroke> strokeList = sd.strokeList;
		List<Segment> segmentList = sd.segmentList;
		List<Center> centerList = sd.centerList;

		List<Integer> xStrokeList = new ArrayList<>();
		List<Float> yStrokeList = new ArrayList<>();
		for (Stroke stroke : strokeList) {
			xStrokeList.add(stroke.startIndex());
			yStrokeList.add(stroke.startValue());
		}
		xStrokeList.add(strokeList.get(strokeList.size() - 1).endIndex());
		yStrokeList.add(strokeList.get(strokeList.size() - 1).endValue());

		List<Integer> xSegmentList = new ArrayList<>();
		List<Float> ySegmentList = new ArrayList<>();
		for (Segment segment : segmentList) {
			xSegmentList.add(segment.startIndex());
			ySegmentList.add(segment.startValue());
		}
		xSegmentList.add(segmentList.get(segmentList.size() - 1).endIndex());
		ySegmentList.add(segmentList.get(segmentList.size() - 1).endValue());

		ChartDrawing cd = new ChartDrawing().drawBars(ohlcv)
		.drawLinesOnMain(xStrokeList, yStrokeList, Color.YELLOW)
		.drawLinesOnMain(xSegmentList, ySegmentList, Color.WHITE);

		for (Center center : centerList) {
			cd.drawRectangleOnMain(center.getStartIndex(), center.getEndIndex(), center.lowRange, center.highRange, Color.ORANGE);
		}

		cd.actualDraw(0, 30000)
		.writeToFile("sameleveldecomposition.png");
	}
}

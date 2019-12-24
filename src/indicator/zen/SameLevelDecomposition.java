package indicator.zen;

import java.util.ArrayList;
import java.util.List;

import indicator.IIndicator;
import indicator.IndicatorBuffer;

public class SameLevelDecomposition implements IIndicator {

	private final boolean strictStroke;
	private final float gapThreshold;

	public List<Fractal> fractalList = null;
	public List<Integer> fractalConfirmList = null;
	public List<Stroke> strokeList = null;
	public List<Integer> strokeConfirmList = null;
	public List<Segment> segmentList = null;
	public List<Center> centerList = null;

	public SameLevelDecomposition(boolean strictStroke, float gapThreshold) {
		this.strictStroke = strictStroke;
		this.gapThreshold = gapThreshold;
	}

	@Override
	public void calculate(float[] open, float[] high, float[] low, float[] close) {
		FractalFinder fractalFinder = new FractalFinder();
		fractalFinder.calculate(open, high, low, close);
		fractalList = fractalFinder.fractalList;
		fractalConfirmList = new ArrayList<>();
		int fractalListSize = fractalList.size();
		for (int i = 0; i < fractalListSize; i++) {
			fractalConfirmList.add(fractalList.get(i).endIdx);
		}

		StrokeDecomposer std = new StrokeDecomposer(strictStroke, gapThreshold);
		strokeList = std.calculate(fractalFinder.adjustedKLines, fractalList, open, high, low, close);
		strokeConfirmList = new ArrayList<>();
		int strokeListSize = strokeList.size();
		for (int i = 0; i < strokeListSize; i++) {
			strokeConfirmList.add(std.confirmList.get(i).endIdx);
		}

		SegmentDecomposer sed = new SegmentDecomposer();
		segmentList = sed.calculate(strokeList);

		centerList = new ArrayList<>();
		for (Segment segment : sed.segmentList) {
			List<Center> centerInSegment = CenterAnalyzer.analyzeSegment(segment);
			centerList.addAll(centerInSegment);
		}
	}

	@Override
	public float[] getBufferById(int id) {
		return null;
	}

	@Override
	public int minimumBarsToWork() {
		return 1;
	}

	@Override
	public List<IndicatorBuffer> getIndicatorBuffers() {
		return null;
	}
}

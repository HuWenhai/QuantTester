package strategy.template;

import java.util.List;

import indicator.zen.Center;
import indicator.zen.Fractal;
import indicator.zen.SameLevelDecomposition;
import indicator.zen.Segment;
import indicator.zen.Stroke;

public abstract class ZenBasedStrategy extends BarBasedStrategy implements IEveryOHLC {

	private SameLevelDecomposition decomp;
	protected List<Fractal> fractalList = null;
	protected List<Integer> fractalConfirmList = null;
	protected List<Stroke> strokeList = null;
	protected List<Integer> strokeConfirmList = null;
	protected List<Segment> segmentList = null;
	protected List<Center> centerList = null;

	public ZenBasedStrategy(boolean strictStroke, float gapThreshold) {
		decomp = new SameLevelDecomposition(strictStroke, gapThreshold);
	}

	@Override
	protected void calculateIndicators() {
		super.calculateIndicators();
		fractalList = decomp.fractalList;
		fractalConfirmList = decomp.fractalConfirmList;
		strokeList = decomp.strokeList;
		strokeConfirmList = decomp.strokeConfirmList;
		segmentList = decomp.segmentList;
		centerList = decomp.centerList;
	}

	public abstract void onFractalFormed(int fractalIdx);
	public abstract void onStrokeConfirmed(int strokeIdx);

	@Override
	public float onClose() {
		int strokeIdx = strokeConfirmList.indexOf(current_index);
		if (strokeIdx > -1) {
			onStrokeConfirmed(strokeIdx);
		}
		int fractalIdx = fractalConfirmList.indexOf(current_index);
		if (fractalIdx > -1) {
			onFractalFormed(fractalIdx);
		}
		return Close[current_index];
	}
}

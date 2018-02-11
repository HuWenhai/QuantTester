package indicator.zen;

import java.util.List;

public class Stroke implements Trend {
	public final Fractal startFractal;
	public final Fractal endFractal;

	public Stroke(Fractal startFractal, Fractal endFractal) {
		this.startFractal = startFractal;
		this.endFractal = endFractal;
	}

	@Override
	public boolean direction() {
		return endFractal.direction;
	}

	@Override
	public int startIndex() {
		return startFractal.peakIdx;
	}

	@Override
	public int endIndex() {
		return endFractal.peakIdx;
	}

	@Override
	public float startValue() {
		return startFractal.peakValue;
	}

	@Override
	public float endValue() {
		return endFractal.peakValue;
	}

	@Override
	public boolean isCompleted() {
		return true;
	}

	@Override
	public boolean isMinimumLevel() {
		return true;
	}

	@Override
	public List<? extends Trend> getSubTrends() {
		return null;
	}
}

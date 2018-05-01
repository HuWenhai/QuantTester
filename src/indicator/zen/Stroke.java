package indicator.zen;

import java.util.List;

public class Stroke implements Trend {
	public final Fractal startFractal;
	public final Fractal endFractal;
	private final float maxValue;
	private final float minValue;

	public Stroke(Fractal startFractal, Fractal endFractal, float maxValue, float minValue) {
		this.startFractal = startFractal;
		this.endFractal = endFractal;
		this.maxValue = maxValue;
		this.minValue = minValue;
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
	public float getMax() {
		return maxValue;
	}

	@Override
	public float getMin() {
		return minValue;
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

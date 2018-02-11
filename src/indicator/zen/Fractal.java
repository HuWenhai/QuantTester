package indicator.zen;

public final class Fractal {
	public final boolean direction;	// True: up fractal, False: down fractal
	public final int startIdx;
	public final int peakIdx;
	public final float peakValue;
	public final int endIdx;		// When this fractal is confirmed

	public Fractal(boolean direction, int startIdx, int peakIdx, float peakValue, int endIdx) {
		this.direction = direction;
		this.startIdx = startIdx;
		this.peakIdx = peakIdx;
		this.peakValue = peakValue;
		this.endIdx = endIdx;
	}
}

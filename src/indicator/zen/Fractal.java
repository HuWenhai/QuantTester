package indicator.zen;

import org.apache.commons.math3.geometry.euclidean.twod.Line;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

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

	private static final double DEGREE_90 = Math.PI / 2.0;
	private static final double DEGREE_270 = Math.PI * 1.5;

	public boolean aboveLine(Line line) {
		double angle = line.getAngle();
		if (angle > DEGREE_90 && angle < DEGREE_270) {
			return line.getOffset(new Vector2D(peakIdx, peakValue)) > 0.0;
		} else {
			return line.getOffset(new Vector2D(peakIdx, peakValue)) < 0.0;
		}
	}

	public boolean belowLine(Line line) {
		double angle = line.getAngle();
		if (angle > DEGREE_90 && angle < DEGREE_270) {
			return line.getOffset(new Vector2D(peakIdx, peakValue)) < 0.0;
		} else {
			return line.getOffset(new Vector2D(peakIdx, peakValue)) > 0.0;
		}
	}
}

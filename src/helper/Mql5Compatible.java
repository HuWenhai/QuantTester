package helper;

public interface Mql5Compatible {
	default float MathMin(float x, float y) {
		return Math.min(x, y);
	}
	
	default float MathMax(float x, float y) {
		return Math.max(x, y);
	}
	
	default float MathAbs(float x) {
		return Math.abs(x);
	}
	
	default boolean IsStopped() {
		return false;
	}
}

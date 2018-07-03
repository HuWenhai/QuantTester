package helper;

public final class MAHelper {
	// +------------------------------------------------------------------+
	// | Simple Moving Average |
	// +------------------------------------------------------------------+
	public static final float SimpleMA(int position, int period, float price[]) {
		// ---
		float result = 0.0f;
		// --- check position
		if (position >= period - 1 && period > 0) {
			// --- calculate value
			for (int i = 0; i < period; i++)
				result += price[position - i];
			result /= period;
		}
		// ---
		return (result);
	}

	// +------------------------------------------------------------------+
	// | Exponential Moving Average |
	// +------------------------------------------------------------------+
	public static final float ExponentialMA(int position, int period, float prev_value, float price[]) {
		// ---
		float result = 0.0f;
		// --- calculate value
		if (period > 0) {
			float pr = 2.0f / (period + 1.0f);
			result = price[position] * pr + prev_value * (1 - pr);
		}
		// ---
		return (result);
	}
}

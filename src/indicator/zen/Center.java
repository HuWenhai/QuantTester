package indicator.zen;

import java.util.ArrayList;
import java.util.List;

public class Center {
	public final float lowRange;
	public final float highRange;
	private final Trend[] trends;	// length = 3
	private List<Trend> succeedTrends = new ArrayList<>(); 

	public Center(float lowRange, float highRange, Trend[] trends) {
		this.lowRange = lowRange;
		this.highRange = highRange;
		this.trends = trends;
	}

	public static Center search(Trend ...trends) {
		if (trends.length < 4) {
			return null;
		}
		boolean direction = trends[0].direction();

		float a = trends[1].startValue();
		float b = trends[2].startValue();
		float c = trends[3].startValue();
		float d = trends[3].endValue();

		float hi = direction ? Math.min(a, c) : Math.min(b, d);
		float lo = direction ? Math.max(b, d) : Math.max(a, c);
		if (hi > lo) {
			return new Center(lo, hi, new Trend[] {trends[1], trends[2], trends[3]});
		} else {
			return null;
		}
	}

	public int getStartIndex() {
		return trends[0].startIndex();
	}

	public int getEndIndex() {
		Trend lastTrend = trends[2];
		if (!succeedTrends.isEmpty()) {
			lastTrend = succeedTrends.get(succeedTrends.size() - 1);
		}
		return lastTrend.endIndex();
	}

	public void extend(Trend ...trends) {
		assert(trends.length % 2 == 0);
		succeedTrends.add(trends[0]);
		succeedTrends.add(trends[1]);
	}
}

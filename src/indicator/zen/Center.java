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

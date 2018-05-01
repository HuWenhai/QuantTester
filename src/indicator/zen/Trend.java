package indicator.zen;

import java.util.ArrayList;
import java.util.List;

public interface Trend {
	public boolean direction();
	public int startIndex();
	public int endIndex();
	public float startValue();
	public float endValue();
	public float getMax();
	public float getMin();
	public boolean isCompleted();
	public boolean isMinimumLevel();
	public List<? extends Trend> getSubTrends();

	default public List<Trend> breakDown() {
		List<? extends Trend> subTrends = this.getSubTrends();
		List<Trend> minimumLevelTrends = new ArrayList<>();
		for (Trend subTrend : subTrends) {
			if (subTrend.isMinimumLevel()) {
				minimumLevelTrends.add(subTrend);
			} else {
				minimumLevelTrends.addAll(subTrend.breakDown());
			}
		}
		return minimumLevelTrends;
	}
}

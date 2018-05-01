package indicator.zen;

import java.util.Arrays;
import java.util.List;

public class Segment implements Trend {

	private final boolean completed;
	private final boolean direction;
	private final Trend[] subTrends;

	public Segment(boolean completed, Trend... subTrends) {
		this.completed = completed;
		this.direction = subTrends[0].direction();
		this.subTrends = subTrends;
	}

	@Override
	public boolean direction() {
		return direction;
	}

	@Override
	public int startIndex() {
		return subTrends[0].startIndex();
	}

	@Override
	public int endIndex() {
		Trend lastTrend = subTrends[subTrends.length - 1];
		return lastTrend.endIndex();
	}

	@Override
	public float startValue() {
		return subTrends[0].startValue();
	}

	@Override
	public float endValue() {
		Trend lastTrend = subTrends[subTrends.length - 1];
		return lastTrend.endValue();
	}

	@Override
	public float getMax() {
		return (float) Arrays.stream(subTrends).mapToDouble(Trend::getMax).max().getAsDouble();
	}

	@Override
	public float getMin() {
		return (float) Arrays.stream(subTrends).mapToDouble(Trend::getMin).min().getAsDouble();
	}

	@Override
	public boolean isMinimumLevel() {
		return false;
	}

	@Override
	public boolean isCompleted() {
		return completed;
	}

	@Override
	public List<? extends Trend> getSubTrends() {
		return Arrays.asList(subTrends);
	}
}

class CompletedSegment extends Segment {
	public CompletedSegment(Trend ...trends) {
		super(true, trends);
	}
}

class PartialSegment extends Segment {
	public PartialSegment(Trend ...trends) {
		super(false, trends);
	}
}

package indicator.zen;

import java.util.ArrayList;
import java.util.List;

class CenterAnalyzer {

	private CenterAnalyzer() {
	}

	private static Center checkCenter(boolean direction, Trend ...trends) {
		float a = trends[0].startValue();
		float b = trends[1].startValue();
		float c = trends[2].startValue();
		float d = trends[2].endValue();

		float hi = direction ? Math.min(a, c) : Math.min(b, d);
		float lo = direction ? Math.max(b, d) : Math.max(a, c);
		if (hi > lo) {
			return new Center(lo, hi, trends);
		} else {
			return null;
		}
	}

	private static boolean touched(Center center, Trend trend) {
		float high = Math.max(trend.startValue(), trend.endValue());
		float low = Math.min(trend.startValue(), trend.endValue());
		return (center.highRange > low) && (center.lowRange < high);
	}

	public static List<Center> analyzeSegment(Segment segment) {
		List<? extends Trend> strokes = segment.breakDown();
		int size = strokes.size();
		List<Center> ret = new ArrayList<Center>();
		Center center = null;
		for (int i = 1; i < size - 1; ) {
			if (center != null && touched(center, strokes.get(i))) {
				center.extend(strokes.get(i), strokes.get(i + 1));
				i += 2;
			} else if (i < size - 3) {
				center = checkCenter(segment.direction(), strokes.get(i), strokes.get(i + 1), strokes.get(i + 2));
				if (center == null) {
					i += 2;
				} else {
					ret.add(center);
					i += 4;
				}
			} else {
				i += 2;
			}
		}

		return ret;
	}
}

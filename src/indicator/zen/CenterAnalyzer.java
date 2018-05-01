package indicator.zen;

import java.util.ArrayList;
import java.util.List;

import helper.MathHelper;

class CenterAnalyzer {

	private CenterAnalyzer() {
	}

	private static Center checkCenter(boolean direction, Trend ...trends) {
		float hi = MathHelper.Min(trends[0].getMax(), trends[1].getMax(), trends[2].getMax());
		float lo = MathHelper.Max(trends[0].getMin(), trends[1].getMin(), trends[2].getMin());
		if (hi >= lo) {
			return new Center(lo, hi, trends);
		} else {
			return null;
		}
	}

	private static boolean touched(Center center, Trend trend) {
		return (center.highRange > trend.getMin()) && (center.lowRange < trend.getMax());
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

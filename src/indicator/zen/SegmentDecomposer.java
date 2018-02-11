package indicator.zen;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

class SegmentDecomposer {

	public List<Segment> segmentList = null;

	public SegmentDecomposer() {
	}

	private static boolean matchUp(float a, float b, float c, float d) {
		return ((a < b) && (b > c) && (c < d) && (a < c) && (b < d));
	}

	private static boolean matchDown(float a, float b, float c, float d) {
		return ((a > b) && (b < c) && (c > d) && (a > c) && (b > d));
	}

	protected static SegmentState getFirstStrokeState(boolean direction, float b, float c, float e) {
		SegmentState fsState = null;
		if ((direction && e < c) || ((!direction) && e > c)) {
			// ±ÊÆÆ»µ
			fsState = SegmentState.CRASHED;
		} else if ((direction && e < b) || ((!direction) && e > b)) {
			// ÎÞÌø¿Õ
			fsState = SegmentState.NOGAP;
		} else {
			// ÓÐÌø¿Õ
			fsState = SegmentState.GAP;
		}
		return fsState;
	}

	public List<Segment> calculate(List<Stroke> strokeList) {
		segmentList = new ArrayList<>();

		if (strokeList == null || strokeList.size() < 3) {
			return null;
		}

		boolean direction = false;
		ListIterator<Stroke> iterator = strokeList.listIterator();
		Trend ab = null;
		Trend bc = iterator.next();
		Trend cd = iterator.next();
		while (iterator.hasNext()) {
			ab = bc;
			bc = cd;
			cd = iterator.next();
			if (matchUp(ab.startValue(), bc.startValue(), cd.startValue(), cd.endValue())) {
				direction = true;
				break;
			} else if (matchDown(ab.startValue(), bc.startValue(), cd.startValue(), cd.endValue())) {
				direction = false;
				break;
			}
		}

		SegmentState fsState = SegmentState.TBD;
		Trend de = null, ef = null, fg = null, ge2 = null, e2f2 = null, f2g2 = null;
		while (iterator.hasNext()) {
			Stroke next = iterator.next();
			switch (fsState) {
			case TBD:
				de = next;
				break;
			case CRASHED:
			case NOGAP:
			case GAP:
				ef = next;
				break;
			case CRASHED_WAIT_3:
			case NOGAP_WAIT_3:
			case GAP_WAIT_3:
				fg = next;
				break;
			case STILL_CRASHED_1:
			case SECOND_SCENARIO_1:
				ge2 = next;
				break;
			case STILL_CRASHED_2:
			case SECOND_SCENARIO_2:
				e2f2 = next;
				break;
			case STILL_CRASHED_3:
			case SECOND_SCENARIO_3:
				f2g2 = next;
				break;
			default:
				break;
			}

			switch (fsState) {
			case TBD:
				fsState = getFirstStrokeState(direction, bc.startValue(), bc.endValue(), de.endValue());
				break;
			case CRASHED:
				if ((direction && (ef.endValue() > cd.endValue())) || ((!direction) && (ef.endValue() < cd.endValue()))) {
					ab = new PartialSegment(ab, bc, cd);
					bc = de;
					cd = ef;
					fsState = SegmentState.TBD;
				} else {
					fsState = SegmentState.CRASHED_WAIT_3;
				}
				break;
			case NOGAP:
				if ((direction && (ef.endValue() > cd.endValue())) || ((!direction) && (ef.endValue() < cd.endValue()))) {
					ab = new PartialSegment(ab, bc, cd);
					bc = de;
					cd = ef;
					fsState = SegmentState.TBD;
				} else {
					fsState = SegmentState.NOGAP_WAIT_3;
				}
				break;
			case GAP:
				if ((direction && (ef.endValue() > cd.endValue())) || ((!direction) && (ef.endValue() < cd.endValue()))) {
					ab = new PartialSegment(ab, bc, cd);
					bc = de;
					cd = ef;
					fsState = SegmentState.TBD;
				} else {
					fsState = SegmentState.GAP_WAIT_3;
				}
				break;
			case CRASHED_WAIT_3:
				if ((direction && (fg.endValue() < de.endValue())) || ((!direction) && (fg.endValue() > de.endValue()))) {
					segmentList.add(new CompletedSegment(ab, bc, cd));
					ab = de;
					bc = ef;
					cd = fg;
					direction = !direction;
					fsState = SegmentState.TBD;
				} else if ((direction && (fg.endValue() < bc.endValue()) && (fg.endValue() >= de.endValue())) || ((!direction) && (fg.endValue() > bc.endValue()) && (fg.endValue() <= de.endValue()))) {
					fsState = SegmentState.STILL_CRASHED_1;
				} else {
					de = new PartialSegment(de, ef, fg);
					fsState = getFirstStrokeState(direction, ab.endValue(), bc.endValue(), de.endValue());
				}
				break;
			case NOGAP_WAIT_3:
				if ((direction && (fg.endValue() < de.endValue())) || ((!direction) && (fg.endValue() > de.endValue()))) {
					segmentList.add(new CompletedSegment(ab, bc, cd));
					ab = de;
					bc = ef;
					cd = fg;
					direction = !direction;
					fsState = SegmentState.TBD;
				} else {
					de = new PartialSegment(de, ef, fg);
					fsState = getFirstStrokeState(direction, ab.endValue(), bc.endValue(), de.endValue());
				}
				break;
			case GAP_WAIT_3:
				if ((direction && (fg.endValue() > de.endValue())) || ((!direction) && (fg.endValue() < de.endValue()))) {
					de = new PartialSegment(de, ef, fg);
					fsState = SegmentState.GAP;
				} else {
					fsState = SegmentState.SECOND_SCENARIO_1;
				}
				break;
			case STILL_CRASHED_1:
				if ((direction && ge2.endValue() < ef.endValue()) || (!direction && ge2.endValue() > ef.endValue())) {
					de = new PartialSegment(de, ef, fg);
					ef = ge2;
					fsState = SegmentState.CRASHED_WAIT_3;
				} else {
					fsState = SegmentState.STILL_CRASHED_2;
				}
				break;
			case STILL_CRASHED_2:
				if ((direction && (e2f2.endValue() < fg.endValue())) || ((!direction) && (e2f2.endValue() > fg.endValue()))) {
					segmentList.add(new CompletedSegment(ab, bc, cd));
					ab = new PartialSegment(de, ef, fg);
					bc = ge2;
					cd = e2f2;
					direction = !direction;
					fsState = SegmentState.TBD;
				} else {
					fsState = SegmentState.STILL_CRASHED_3;
				}
				break;
			case STILL_CRASHED_3:
				if ((direction && (f2g2.endValue() > ge2.endValue())) || ((!direction) && (f2g2.endValue() < ge2.endValue()))) {
					segmentList.add(new CompletedSegment(ab, bc, cd));
					segmentList.add(new CompletedSegment(de, ef, fg));
					ab = ge2;
					bc = e2f2;
					cd = f2g2;
					fsState = SegmentState.TBD;
				} else {
					ge2 = new PartialSegment(ge2, e2f2, f2g2);
					fsState = SegmentState.STILL_CRASHED_2;
				}
				break;
			case SECOND_SCENARIO_1:
				if ((direction && (ge2.endValue() > cd.endValue())) || ((!direction) && (ge2.endValue() < cd.endValue()))) {
					ab = new PartialSegment(ab, bc, cd);
					bc = new PartialSegment(de, ef, fg);
					cd = ge2;
					fsState = SegmentState.TBD;
				} else if ((direction && (ge2.endValue() > ef.endValue())) || ((!direction) && (ge2.endValue() < ef.endValue()))) {
					de = new PartialSegment(de, ef, fg);
					ef = ge2;
					switch (getFirstStrokeState(direction, ab.endValue(), bc.endValue(), de.endValue())) {
					case CRASHED:
						fsState = SegmentState.CRASHED_WAIT_3;
						break;
					case NOGAP:
						fsState = SegmentState.NOGAP_WAIT_3;
						break;
					case GAP:
						fsState = SegmentState.GAP_WAIT_3;
						break;
					default:
						break;
					}
				} else {
					fsState = SegmentState.SECOND_SCENARIO_2;
				}
				break;
			case SECOND_SCENARIO_2:
				if ((direction && (e2f2.endValue() < fg.endValue()) && (fg.endValue() < ab.endValue())) || ((!direction) && (e2f2.endValue() > fg.endValue()) && (fg.endValue() > ab.endValue()))) {
					segmentList.add(new CompletedSegment(ab, bc, cd));
					ab = new PartialSegment(de, ef, fg);
					bc = ge2;
					cd = e2f2;
					direction = !direction;
					fsState = SegmentState.TBD;
				} else if ((direction && (e2f2.endValue() < fg.endValue())) || ((!direction) && (e2f2.endValue() > fg.endValue()))) {
					de = new PartialSegment(de, ef, fg, ge2, e2f2);
					fsState = getFirstStrokeState(direction, ab.endValue(), bc.endValue(), de.endValue());
				} else {
					fsState = SegmentState.SECOND_SCENARIO_3;
				}
				break;
			case SECOND_SCENARIO_3:
				if ((direction && (f2g2.endValue() > ge2.endValue())) || ((!direction) && (f2g2.endValue() < ge2.endValue()))) {
					segmentList.add(new CompletedSegment(ab, bc, cd));
					segmentList.add(new CompletedSegment(de, ef, fg));
					ab = ge2;
					bc = e2f2;
					cd = f2g2;
					fsState = SegmentState.TBD;
				} else {
					ge2 = new PartialSegment(ge2, e2f2, f2g2);
					fsState = SegmentState.SECOND_SCENARIO_2;
				}
				break;
			default:
				break;
			}
		}
		segmentList.add(new PartialSegment(ab, bc, cd));
		return segmentList;
	}
}

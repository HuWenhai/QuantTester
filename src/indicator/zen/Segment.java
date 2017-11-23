package indicator.zen;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

public class Segment extends Stroke {

	protected List<Integer> segmentList = null;
	protected List<Float> segmentBufferList = null;
	protected float[] segmentBuffer = null;

	public Segment(boolean strict, float gapRatio) {
		super(strict, gapRatio);
	}

	public Segment() {
		super();
	}

	private boolean matchUp(float a, float b, float c, float d) {
		return ((a < b) && (b > c) && (c < d) && (a < c) && (b < d));
	}

	private boolean matchDown(float a, float b, float c, float d) {
		return ((a > b) && (b < c) && (c > d) && (a > c) && (b > d));
	}

	protected static enum SegmentState {
		TBD,
		CRASHED,
		NOGAP,
		GAP,
		CRASHED_WAIT_3,
		NOGAP_WAIT_3,
		GAP_WAIT_3,
		STILL_CRASHED_1,
		STILL_CRASHED_2,
		STILL_CRASHED_3,
		SECOND_SCENARIO_1,
		SECOND_SCENARIO_2,
		SECOND_SCENARIO_3,
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

	@Override
	public void calculate(float[] open, float[] high, float[] low, float[] close) {
		super.calculate(open, high, low, close);
		final int rates_total = close.length;
		segmentBuffer = new float[rates_total];
		for (int i = 0; i < rates_total; i++) {
			segmentBuffer[i] = Float.NEGATIVE_INFINITY;
		}
		segmentList = new ArrayList<>();
		segmentBufferList = new ArrayList<>();

		final int strokeLen = strokeList.size();
		if (strokeLen < 4) {
			return;
		}

		boolean direction = false;
		ListIterator<Float> iterator = strokeBufferList.listIterator();
		int aIndex = -1;
		float a = Float.NEGATIVE_INFINITY;
		int bIndex = iterator.nextIndex();
		float b = iterator.next();
		int cIndex = iterator.nextIndex();
		float c = iterator.next();
		int dIndex = iterator.nextIndex();
		float d = iterator.next();
		while (iterator.hasNext()) {
			aIndex = bIndex;
			a = b;
			bIndex = cIndex;
			b = c;
			cIndex = dIndex;
			c = d;
			dIndex = iterator.nextIndex();
			d = iterator.next();
			if (matchUp(a, b, c, d)) {
				direction = true;
				break;
			} else if (matchDown(a, b, c, d)) {
				direction = false;
				break;
			}
		}
		segmentList.add(aIndex);

		SegmentState fsState = SegmentState.TBD;
		float e = Float.NEGATIVE_INFINITY, f = Float.NEGATIVE_INFINITY, g = Float.NEGATIVE_INFINITY;
		float e2 = Float.NEGATIVE_INFINITY, f2 = Float.NEGATIVE_INFINITY, g2 = Float.NEGATIVE_INFINITY;
		int eIndex = -1, fIndex = -1, gIndex = -1;
		int e2Index = -1, f2Index = -1, g2Index = -1;
		while (iterator.hasNext()) {
			int nextIndex = iterator.nextIndex();
			float next = iterator.next();
			//System.out.println("nextIndex = " + nextIndex + ", next = " + next + ", state = " + fsState);
			switch (fsState) {
			case TBD:
				e = next;
				eIndex = nextIndex;
				break;
			case CRASHED:
			case NOGAP:
			case GAP:
				f = next;
				fIndex = nextIndex;
				break;
			case CRASHED_WAIT_3:
			case NOGAP_WAIT_3:
			case GAP_WAIT_3:
				g = next;
				gIndex = nextIndex;
				break;
			case STILL_CRASHED_1:
			case SECOND_SCENARIO_1:
				e2 = next;
				e2Index = nextIndex;
				break;
			case STILL_CRASHED_2:
			case SECOND_SCENARIO_2:
				f2 = next;
				f2Index = nextIndex;
				break;
			case STILL_CRASHED_3:
			case SECOND_SCENARIO_3:
				g2 = next;
				g2Index = nextIndex;
				break;
			default:
				break;
			}

			switch (fsState) {
			case TBD:
				fsState = getFirstStrokeState(direction, b, c, e);
				break;
			case CRASHED:
				if ((direction && (f > d)) || ((!direction) && (f < d))) {
					bIndex = dIndex;
					b = d;
					cIndex = eIndex;
					c = e;
					dIndex = fIndex;
					d = f;
					fsState = SegmentState.TBD;
				} else {
					fsState = SegmentState.CRASHED_WAIT_3;
				}
				break;
			case NOGAP:
				if ((direction && (f > d)) || ((!direction) && (f < d))) {
					bIndex = dIndex;
					b = d;
					cIndex = eIndex;
					c = e;
					dIndex = fIndex;
					d = f;
					fsState = SegmentState.TBD;
				} else {
					fsState = SegmentState.NOGAP_WAIT_3;
				}
				break;
			case GAP:
				if ((direction && (f > d)) || ((!direction) && (f < d))) {
					bIndex = dIndex;
					b = d;
					cIndex = eIndex;
					c = e;
					dIndex = fIndex;
					d = f;
					fsState = SegmentState.TBD;
				} else {
					fsState = SegmentState.GAP_WAIT_3;
				}
				break;
			case CRASHED_WAIT_3:
				if ((direction && (g < e)) || ((!direction) && (g > e))) {
					aIndex = dIndex;
					a = d;
					bIndex = eIndex;
					b = e;
					cIndex = fIndex;
					c = f;
					dIndex = gIndex;
					d = g;
					direction = !direction;
					fsState = SegmentState.TBD;
					segmentList.add(aIndex);
				} else if ((direction && (g < c) && (g >= e)) || ((!direction) && (g > c) && (g <= e))) {
					fsState = SegmentState.STILL_CRASHED_1;
				} else {
					eIndex = gIndex;
					e = g;
					fsState = getFirstStrokeState(direction, b, c, e);
				}
				break;
			case NOGAP_WAIT_3:
				if ((direction && (g < e)) || ((!direction) && (g > e))) {
					aIndex = dIndex;
					a = d;
					bIndex = eIndex;
					b = e;
					cIndex = fIndex;
					c = f;
					dIndex = gIndex;
					d = g;
					direction = !direction;
					fsState = SegmentState.TBD;
					segmentList.add(aIndex);
				} else {
					eIndex = gIndex;
					e = g;
					fsState = getFirstStrokeState(direction, b, c, e);
				}
				break;
			case GAP_WAIT_3:
				if ((direction && (g > e)) || ((!direction) && (g < e))) {
					eIndex = gIndex;
					e = g;
					fsState = SegmentState.GAP;
				} else {
					fsState = SegmentState.SECOND_SCENARIO_1;
				}
				break;
			case STILL_CRASHED_1:
				fsState = SegmentState.STILL_CRASHED_2;
				break;
			case STILL_CRASHED_2:
				if ((direction && (f2 < g)) || ((!direction) && (f2 > g))) {
					aIndex = dIndex;
					a = d;
					bIndex = gIndex;
					b = g;
					cIndex = e2Index;
					c = e2;
					dIndex = f2Index;
					d = f2;
					direction = !direction;
					fsState = SegmentState.TBD;
					segmentList.add(aIndex);
				} else {
					fsState = SegmentState.STILL_CRASHED_3;
				}
				break;
			case STILL_CRASHED_3:
				if ((direction && (g2 > e2)) || ((!direction) && (g2 < e2))) {
					segmentList.add(dIndex);
					aIndex = gIndex;
					a = g;
					bIndex = e2Index;
					b = e2;
					cIndex = f2Index;
					c = f2;
					dIndex = g2Index;
					d = g2;
					fsState = SegmentState.TBD;
					segmentList.add(aIndex);
				} else {
					e2Index = g2Index;
					e2 = g2;
					fsState = SegmentState.STILL_CRASHED_2;
				}
				break;
			case SECOND_SCENARIO_1:
				if ((direction && (e2 > d)) || ((!direction) && (e2 < d))) {
					bIndex = dIndex;
					b = d;
					cIndex = gIndex;
					c = g;
					dIndex = e2Index;
					d = e2;
					fsState = SegmentState.TBD;
				} else if ((direction && (e2 > f)) || ((!direction) && (e2 < f))) {
					eIndex = gIndex;
					e = g;
					fIndex = e2Index;
					f = e2;
					switch (getFirstStrokeState(direction, b, c, e)) {
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
				if ((direction && (f2 < g) && (g < b)) || ((!direction) && (f2 > g) && (g > b))) {
					aIndex = dIndex;
					a = d;
					bIndex = gIndex;
					b = g;
					cIndex = e2Index;
					c = e2;
					dIndex = f2Index;
					d = f2;
					direction = !direction;
					fsState = SegmentState.TBD;
					segmentList.add(aIndex);
				} else if ((direction && (f2 < g)) || ((!direction) && (f2 > g))) {
					eIndex = f2Index;
					e = f2;
					fsState = getFirstStrokeState(direction, b, c, e);
				} else {
					fsState = SegmentState.SECOND_SCENARIO_3;
				}
				break;
			case SECOND_SCENARIO_3:
				if ((direction && (g2 > e2)) || ((!direction) && (g2 < e2))) {
					segmentList.add(dIndex);
					aIndex = gIndex;
					a = g;
					bIndex = e2Index;
					b = e2;
					cIndex = f2Index;
					c = f2;
					dIndex = g2Index;
					d = g2;
					fsState = SegmentState.TBD;
					segmentList.add(aIndex);
				} else {
					e2Index = g2Index;
					e2 = g2;
					fsState = SegmentState.SECOND_SCENARIO_2;
				}
				break;
			default:
				break;
			}
		}

		segmentBufferList = segmentList.stream()
				.map((index) -> { return strokeBuffer[strokeList.get(index)];} )
				.collect(Collectors.toList());

		final int segmentBufferListSize = segmentBufferList.size();
		for (int i = 0; i < segmentBufferListSize; i++) {
			segmentBuffer[strokeList.get(segmentList.get(i))] = segmentBufferList.get(i);
		}
	}

	@Override
	public float[] getBufferById(int id) {
		switch (id) {
		case 0:
			return upperBuffer;
		case 1:
			return lowerBuffer;
		case 2:
			return strokeBuffer;
		case 3:
			return segmentBuffer;
		default:
			return null;
		}
	}
}

package data;

public enum TIME_FRAME {
	// MIN3 and MIN10 data are provided by SinYee
	// compose bar may get wrong result in non active contracts
	TICK(null, false, 0),
	MIN1(null, false, 60),
	MIN3(MIN1, false, 3 * 60),
	MIN5(MIN1, false, 5 * 60),
	MIN10(MIN5, true, 10 * 60),
	MIN15(MIN1, false, 15 * 60),
	MIN30(MIN15, true, 30 * 60),
	MIN60(MIN15, true, 60 * 60),
	DAY(null, false, 24 * 60 * 60),
	WEEK(null, false, Integer.MAX_VALUE),
	MONTH(null, false, Integer.MAX_VALUE),
	YEAR(null, false, Integer.MAX_VALUE);

	public final TIME_FRAME composedFrom;
	public final boolean useCountNumber;
	public final int unit;

	private TIME_FRAME(TIME_FRAME composedFrom, boolean useCountNumber, int unit) {
		this.composedFrom = composedFrom;
		this.useCountNumber = useCountNumber;
		this.unit = unit;
	}
}

package tester;

import java.util.ArrayList;
import java.util.List;

public class AdditionalDot {
	public List<Long> dotTimes = new ArrayList<>();
	public List<String> instruments = new ArrayList<>();
	public List<Float> prices = new ArrayList<>();
	public List<Integer> types = new ArrayList<>();

	public AdditionalDot() {
	}

	public void append(long dotTime, String instrument, float price, int type) {
		dotTimes.add(dotTime);
		instruments.add(instrument);
		prices.add(price);
		types.add(type);
	}
}

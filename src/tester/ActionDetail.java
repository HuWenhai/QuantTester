package tester;

import java.util.ArrayList;
import java.util.List;

public class ActionDetail {
	public List<Long> actionTimes = new ArrayList<>();
	public List<String> instrumentIds = new ArrayList<>();
	public List<Float> prices = new ArrayList<>();
	public List<Integer> volumes = new ArrayList<>();
	public List<Boolean> directions = new ArrayList<>();
	public List<Boolean> openCloseFlags = new ArrayList<>();
	public List<Integer> labels = new ArrayList<>();

	public ActionDetail() {
	}

	public void append(long actionTime, String instrumentId, float price, int volume, boolean direction, boolean openCloseFlag, int label) {
		actionTimes.add(actionTime);
		instrumentIds.add(instrumentId);
		prices.add(price);
		volumes.add(volume);
		directions.add(direction);
		openCloseFlags.add(openCloseFlag);
		labels.add(label);
	}

	public void append(long actionTime, String instrumentId, float price, int volume, boolean direction, boolean openCloseFlag) {
		append(actionTime, instrumentId, price, volume, direction, openCloseFlag, 0);
	}
}

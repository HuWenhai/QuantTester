package tester;

import java.util.ArrayList;
import java.util.List;

import data.TIME_FRAME;

public class ActionDetail {
	public String strategyName;
	public TIME_FRAME timeFrame;
	public String datasource;
	public int testStartTime;
	public int testEndTime;

	public List<Long> actionTimes = new ArrayList<>();
	public List<String> actionInstruments = new ArrayList<>();
	public List<Float> prices = new ArrayList<>();
	public List<Integer> volumes = new ArrayList<>();
	public List<Boolean> directions = new ArrayList<>();
	public List<Boolean> openCloseFlags = new ArrayList<>();

	public ActionDetail() {
	}

	public void append(long actionTime, String actionInstrument, float price, int volume, boolean direction, boolean openCloseFlag) {
		actionTimes.add(actionTime);
		actionInstruments.add(actionInstrument);
		prices.add(price);
		volumes.add(volume);
		directions.add(direction);
		openCloseFlags.add(openCloseFlag);
	}
}

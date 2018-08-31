package tester;

import java.util.ArrayList;
import java.util.List;

import data.TIME_FRAME;

public class ActionDetail {
	public String strategyName;
	public String instrument;
	public TIME_FRAME timeFrame;
	public String datasource;
	public int testStartTime;
	public int testEndTime;

	public List<Integer> actionMonths = new ArrayList<>();
	public List<Integer> actionTimes = new ArrayList<>();
	public List<Float> prices = new ArrayList<>();
	public List<Integer> volumes = new ArrayList<>();
	public List<Boolean> directions = new ArrayList<>();
	public List<Boolean> openCloseFlags = new ArrayList<>();

	public ActionDetail() {
	}

	public void append(int actionMonth, int actionTime, float price, int volume, boolean direction, boolean openCloseFlag) {
		actionMonths.add(actionMonth);
		actionTimes.add(actionTime);
		prices.add(price);
		volumes.add(volume);
		directions.add(direction);
		openCloseFlags.add(openCloseFlag);
	}
}

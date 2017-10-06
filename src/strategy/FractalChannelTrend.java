package strategy;

import indicator.zen.FractalChannel;
import strategy.template.BarBasedStrategy;
import strategy.template.IEveryOHLC;

public class FractalChannelTrend extends BarBasedStrategy implements IEveryOHLC {

	private final int period;
	private final FractalChannel fractalChannel;

	protected float[] vU = null;
	protected float[] vB = null;

	private float stopLoss = -1.0f;
	private float AF = 0.0f;
	private float highestEver = 0.0f;
	private float lowestEver = 0.0f;

	public FractalChannelTrend(Integer Period) {
		this.period = Period;
		this.fractalChannel = new FractalChannel(period);
		this.indicators.add(fractalChannel);
	}

	@Override
	protected void calculateIndicators() {
		super.calculateIndicators();
		vU = fractalChannel.getBufferById(3);
		vB = fractalChannel.getBufferById(4);
	}

	@Override
	public float onOpen() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float onHigh() {
		if (position < 0 && stopLoss > 0.0f && High[current_index] > stopLoss) {
			position = 0;
			return stopLoss;
		}
		return High[current_index];
	}

	@Override
	public float onLow() {
		if (position > 0 && stopLoss > 0.0f && Low[current_index] < stopLoss) {
			position = 0;
			return stopLoss;
		}
		return Low[current_index];
	}

	@Override
	public float onClose() {
		if (current_index < (period + 1)) { // FIXME
			position = 0;
			return Close[current_index];
		} else if (Close[current_index] >= vU[current_index]) {
			if (position != 1) {
				position = 1;
				stopLoss = Low[current_index];
				AF = 0.0f;
				highestEver = High[current_index];
			}
		} else if (Close[current_index] <= vB[current_index]) {
			if (position != -1) {
				position = -1;
				stopLoss = High[current_index];
				AF = 0.0f;
				lowestEver = Low[current_index];
			}
		}

		if (position > 0) {
			if (High[current_index] > highestEver) {
				highestEver = High[current_index];
				if (AF < 0.09999f) {
					AF += 0.01f;
				}
			}
			stopLoss = stopLoss + (highestEver - stopLoss) * AF;
		} else if (position < 0) {
			if (Low[current_index] < lowestEver) {
				lowestEver = Low[current_index];
				if (AF < 0.09999f) {
					AF += 0.01f;
				}
			}
			stopLoss = stopLoss - (stopLoss - lowestEver) * AF;
		}
		return Close[current_index];
	}
}

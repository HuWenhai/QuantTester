package indicator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import indicator.IndicatorBuffer.DrawingStyle;
import indicator.IndicatorBuffer.DrawingType;

public class MACD implements IIndicator {

	private final int InpFastEMA;
	private final int InpSlowEMA;
	private final int InpSignalSMA;
	private final IPriceSimplifier applied_price;

	private IIndicator ExtFastMaHandle;
	private IIndicator ExtSlowMaHandle;

	public MACD(int FastEMA, int SlowEMA, int SignalSMA, APPLIED_PRICE applied_price) {
		this.InpFastEMA = FastEMA;
		this.InpSlowEMA = SlowEMA;
		this.InpSignalSMA = SignalSMA;
		this.applied_price = applied_price;
		ExtFastMaHandle = new MA(InpFastEMA, MA.MODE_EMA, this.applied_price);
		ExtSlowMaHandle = new MA(InpSlowEMA, MA.MODE_EMA, this.applied_price);
	}

	public MACD(int FastEMA, int SlowEMA, int SignalSMA) {
		this(FastEMA, SlowEMA, SignalSMA, APPLIED_PRICE.PRICE_CLOSE);
	}

	public MACD() {
		this(12, 26, 9, APPLIED_PRICE.PRICE_CLOSE);
	}

	private float[] ExtMacdBuffer = null;
	private float[] ExtSignalBuffer = null;
	private float[] ExtFastMaBuffer = null;
	private float[] ExtSlowMaBuffer = null;

	@Override
	public void calculate(float[] open, float[] high, float[] low, float[] close) {
		int rates_total = close.length;

		ExtMacdBuffer = new float[rates_total];
		ExtSignalBuffer = new float[rates_total];

		ExtFastMaHandle.calculate(open, high, low, close);
		ExtFastMaBuffer = ExtFastMaHandle.getBufferById(0);
		ExtSlowMaHandle.calculate(open, high, low, close);
		ExtSlowMaBuffer = ExtSlowMaHandle.getBufferById(0);

		// --- check for data
		if (rates_total < InpSignalSMA)
			return;
		// --- calculate MACD
		for (int i = 0; i < rates_total; i++)
			ExtMacdBuffer[i] = ExtFastMaBuffer[i] - ExtSlowMaBuffer[i];
		// --- calculate Signal
		IIndicatorOnIndicator SignalMA = new MA(InpSignalSMA, MA.MODE_SMA);
		SignalMA.calculate(ExtMacdBuffer, 0);
		ExtSignalBuffer = SignalMA.getBufferById(0);
	}

	@Override
	public float[] getBufferById(int id) {
		switch (id) {
		case 0:
			return ExtMacdBuffer;
		case 1:
			return ExtSignalBuffer;
		case 2:
			return ExtFastMaBuffer;
		case 3:
			return ExtSlowMaBuffer;
		default:
			return null;
		}
	}

	@Override
	public int minimumBarsToWork() {
		return InpSignalSMA - 1;
	}

	@Override
	public List<IndicatorBuffer> getIndicatorBuffers() {
		List<IndicatorBuffer> buffers = new ArrayList<>();
		buffers.add(new IndicatorBuffer("Macd", DrawingType.SeparateChart, DrawingStyle.Bar, Color.WHITE, ExtMacdBuffer, InpSignalSMA - 1));
		buffers.add(new IndicatorBuffer("Signal", DrawingType.SeparateChart, DrawingStyle.Line, Color.RED, ExtSignalBuffer, InpSignalSMA - 1));
		buffers.add(new IndicatorBuffer("FastMa", ExtFastMaBuffer));
		buffers.add(new IndicatorBuffer("SlowMa", ExtSlowMaBuffer));
		return buffers;
	}
}

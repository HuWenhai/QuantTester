package indicator;

import static helper.MAHelper.ExponentialMA;

import java.util.List;

import helper.Mql5Compatible;

public class ADX implements IIndicator, Mql5Compatible {

	private final int ExtADXPeriod;

	public ADX(int InpPeriodADX) {
		// --- check for input parameters
		if (InpPeriodADX >= 100 || InpPeriodADX <= 0) {
			ExtADXPeriod = 14;
			System.out.println("Incorrect value for input variable Period_ADX=" + InpPeriodADX);
		} else
			ExtADXPeriod = InpPeriodADX;
	}

	public ADX() {
		this(14);
	}

	private float ExtADXBuffer[] = null;
	private float ExtPDIBuffer[] = null;
	private float ExtNDIBuffer[] = null;
	private float ExtPDBuffer[] = null;
	private float ExtNDBuffer[] = null;
	private float ExtTmpBuffer[] = null;

	@Override
	public void calculate(float[] open, float[] high, float[] low, float[] close) {
		int rates_total = close.length;

		ExtADXBuffer = new float[rates_total];
		ExtPDIBuffer = new float[rates_total];
		ExtNDIBuffer = new float[rates_total];
		ExtPDBuffer = new float[rates_total];
		ExtNDBuffer = new float[rates_total];
		ExtTmpBuffer = new float[rates_total];

		// --- checking for bars count
		if (rates_total < ExtADXPeriod)
			return;
		// --- detect start position
		int start = 1;
		ExtPDIBuffer[0] = 0.0f;
		ExtNDIBuffer[0] = 0.0f;
		ExtADXBuffer[0] = 0.0f;
		// --- main cycle
		for (int i = start; i < rates_total && !IsStopped(); i++) {
			// --- get some data
			float Hi = high[i];
			float prevHi = high[i - 1];
			float Lo = low[i];
			float prevLo = low[i - 1];
			float prevCl = close[i - 1];
			// --- fill main positive and main negative buffers
			float dTmpP = Hi - prevHi;
			float dTmpN = prevLo - Lo;
			if (dTmpP < 0.0f)
				dTmpP = 0.0f;
			if (dTmpN < 0.0f)
				dTmpN = 0.0f;
			if (dTmpP > dTmpN)
				dTmpN = 0.0f;
			else {
				if (dTmpP < dTmpN)
					dTmpP = 0.0f;
				else {
					dTmpP = 0.0f;
					dTmpN = 0.0f;
				}
			}
			// --- define TR
			float tr = MathMax(MathMax(MathAbs(Hi - Lo), MathAbs(Hi - prevCl)), MathAbs(Lo - prevCl));
			// ---
			if (tr != 0.0) {
				ExtPDBuffer[i] = 100.0f * dTmpP / tr;
				ExtNDBuffer[i] = 100.0f * dTmpN / tr;
			} else {
				ExtPDBuffer[i] = 0.0f;
				ExtNDBuffer[i] = 0.0f;
			}
			// --- fill smoothed positive and negative buffers
			ExtPDIBuffer[i] = ExponentialMA(i, ExtADXPeriod, ExtPDIBuffer[i - 1], ExtPDBuffer);
			ExtNDIBuffer[i] = ExponentialMA(i, ExtADXPeriod, ExtNDIBuffer[i - 1], ExtNDBuffer);
			// --- fill ADXTmp buffer
			float dTmp = ExtPDIBuffer[i] + ExtNDIBuffer[i];
			if (dTmp != 0.0)
				dTmp = 100.0f * MathAbs((ExtPDIBuffer[i] - ExtNDIBuffer[i]) / dTmp);
			else
				dTmp = 0.0f;
			ExtTmpBuffer[i] = dTmp;
			// --- fill smoothed ADX buffer
			ExtADXBuffer[i] = ExponentialMA(i, ExtADXPeriod, ExtADXBuffer[i - 1], ExtTmpBuffer);
		}
		// ---- OnCalculate done. Return new prev_calculated.
		return;
	}

	@Override
	public float[] getBufferById(int id) {
		switch (id) {
		case 0:
			return ExtADXBuffer;
		case 1:
			return ExtPDIBuffer;
		case 2:
			return ExtNDIBuffer;
		default:
			return null;
		}
	}

	@Override
	public int minimumBarsToWork() {
		return ExtADXPeriod;
	}

	@Override
	public List<IndicatorBuffer> getIndicatorBuffers() {
		// TODO Auto-generated method stub
		return null;
	}

}

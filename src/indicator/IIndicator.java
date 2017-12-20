package indicator;

import java.util.List;

public interface IIndicator {
	public void calculate(float[] open, float[] high, float[] low, float[] close);
	public float[] getBufferById(int id);
	public int minimumBarsToWork();
	public List<IndicatorBuffer> getIndicatorBuffers();
	public default IndicatorBuffer getIndicatorBufferByName(String bufferName) {
		List<IndicatorBuffer> buffers = getIndicatorBuffers();
		for (IndicatorBuffer buffer : buffers) {
			if (buffer.name.equalsIgnoreCase(bufferName)) {
				return buffer;
			}
		}
		return null;
	}
}

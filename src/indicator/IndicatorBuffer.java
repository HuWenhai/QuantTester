package indicator;

import java.awt.Color;

public class IndicatorBuffer {
	public static enum DrawingType {
		NoDraw,
		MainChart,
		SeparateChart,
	}

	public static enum DrawingStyle {
		NoDraw,
		Line,
		Dot,
		Bar,
	}

	public final String name;
	public final DrawingType drawOn;
	public final DrawingStyle style;
	private final Color color;
	public final float[] buffer;
	public final int beginIndex;

	public IndicatorBuffer(String name, DrawingType drawOn, DrawingStyle style, Color color, float[] buffer, int beginIndex) {
		this.name = name;
		this.drawOn = drawOn;
		this.style = style;
		this.color = color;
		this.buffer = buffer;
		this.beginIndex = beginIndex;
	}

	public IndicatorBuffer(String name, float[] buffer) {
		this(name, DrawingType.NoDraw, DrawingStyle.NoDraw, null, buffer, 0);
	}

	public Color getColor(int index) {
		return color;
	}
}

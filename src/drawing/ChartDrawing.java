package drawing;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import data.struct.BarSeries;
import global.Config;
import helper.ImageHelper;
import helper.StreamHelper;

public class ChartDrawing {
	
	protected final int main_height;
	protected final int separate_height;
	
	protected Set<Color> main_unused_colors = new HashSet<>();
	protected Set<Color> separate_unused_colors = new HashSet<>();	// TODO ¶à¸öseparate window
	{
		main_unused_colors.add(Color.PINK);
		main_unused_colors.add(Color.ORANGE);
		main_unused_colors.add(Color.WHITE);
		main_unused_colors.add(Color.YELLOW);
		main_unused_colors.add(Color.BLUE);
		
		separate_unused_colors.addAll(main_unused_colors);
		separate_unused_colors.add(Color.RED);
		separate_unused_colors.add(Color.GREEN);
	}
	
	protected float[] open;
	protected float[] high;
	protected float[] low;
	protected float[] close;
	
	protected List<Color> main_color_list = new ArrayList<>();
	protected List<Color> separate_color_list = new ArrayList<>();
	
	protected List<float[]> main_buffer_list = new ArrayList<>();
	protected List<float[]> separate_buffer_list = new ArrayList<>();
	
	protected List<Integer> main_begin_list = new ArrayList<>();
	protected List<Integer> separate_begin_list = new ArrayList<>();
	
	protected List<Boolean> main_smooth_list = new ArrayList<>();
	protected List<Boolean> separate_smooth_list = new ArrayList<>();
	
	protected static class LineData {
		public List<Integer> line_x_list = new ArrayList<>();
		public List<Float> line_y_list = new ArrayList<>();
		public Color line_color = null;
	}
	protected List<LineData> main_line_list = new ArrayList<>();
	
	protected BufferedImage main_image = null;
	protected BufferedImage separate_image = null;
	
	public ChartDrawing(int main_height, int separate_height) {
		this.main_height = main_height;
		this.separate_height = separate_height;
	}
	
	public ChartDrawing() {
		this(1000, 200);
	}
	
	public ChartDrawing drawBars(BarSeries barSeries) {
		open  = barSeries.opens;
		high  = barSeries.highs;
		low   = barSeries.lows;
		close = barSeries.closes;
		return this;
	}
	
	public ChartDrawing drawBufferOnMain(float[] buffer, int begin, boolean smooth, Color color) {
		main_buffer_list.add(buffer);
		main_begin_list.add(begin);
		main_smooth_list.add(smooth);
		main_color_list.add(color);
		main_unused_colors.remove(color);
		return this;
	}

	public ChartDrawing drawBufferOnMain(float[] buffer, int begin, boolean smooth) {
		Iterator<Color> ic = main_unused_colors.iterator();
		return drawBufferOnMain(buffer, begin, smooth, ic.hasNext() ? ic.next() : Color.GRAY);
	}
	
	public ChartDrawing drawLinesOnMain(List<Integer> xList, List<Float> yList, Color color) {
		LineData data = new LineData();
		data.line_x_list.addAll(xList);
		data.line_y_list.addAll(yList);
		data.line_color = color;
		main_line_list.add(data);
		return this;
	}
	
	public ChartDrawing drawRectangleOnMain(int x1, int x2, float y1, float y2, Color color) {
		List<Integer> xList = new ArrayList<>();
		List<Float> yList = new ArrayList<>();
		// Bottom left
		xList.add(x1);
		yList.add(y1);
		// Bottom right
		xList.add(x2);
		yList.add(y1);
		// Top right
		xList.add(x2);
		yList.add(y2);
		// Top left
		xList.add(x1);
		yList.add(y2);
		// Bottom left
		xList.add(x1);
		yList.add(y1);
		return drawLinesOnMain(xList, yList, color);
	}
	
	public ChartDrawing drawBufferOnSeparate(float[] buffer, int begin, boolean smooth, Color color) {
		separate_buffer_list.add(buffer);
		separate_begin_list.add(begin);
		separate_smooth_list.add(smooth);
		separate_color_list.add(color);
		separate_unused_colors.remove(color);
		return this;
	}
	
	public ChartDrawing drawBufferOnSeparate(float[] buffer, int begin, boolean smooth) {
		Iterator<Color> ic = separate_unused_colors.iterator();
		return drawBufferOnSeparate(buffer, begin, smooth, ic.hasNext() ? ic.next() : Color.GRAY);
	}

	public ChartDrawing actualDraw(int start, int end) {
		final int chartEnd = Math.min(end, close.length);
		DoubleSummaryStatistics main_dss = new DoubleSummaryStatistics();
		DoubleSummaryStatistics high_dss = StreamHelper.getFloatSummaryStatistics(Arrays.copyOfRange(high, start, chartEnd));
		DoubleSummaryStatistics low_dss  = StreamHelper.getFloatSummaryStatistics(Arrays.copyOfRange(low , start, chartEnd));
		main_dss.combine(high_dss);
		main_dss.combine(low_dss);
		
		for (int i = 0; i < main_buffer_list.size(); i++) {
			int indicator_begin = main_begin_list.get(i);
			float[] indicator_value = Arrays.copyOfRange(main_buffer_list.get(i), Math.max(indicator_begin, start), chartEnd);
			DoubleSummaryStatistics indicator_dss = StreamHelper.getFloatSummaryStatistics(indicator_value);
			main_dss.combine(indicator_dss);
		}
		
		final float main_max = (float) main_dss.getMax();
		final float main_min = (float) main_dss.getMin();
		final float main_scale = (main_height * 0.99f) / (main_max - main_min);
		main_image = new BufferedImage((chartEnd - start) * 2, main_height, BufferedImage.TYPE_INT_RGB);
		for (int i = start; i < chartEnd; i++) {
			for (int j = (int) ((low[i] - main_min) * main_scale); j < (int) ((high[i] - main_min) * main_scale); j++)
				main_image.setRGB(i * 2, j, (close[i] > open[i]) ? Color.RED.getRGB() : Color.GREEN.getRGB());
			for (int j = 0; j < main_buffer_list.size(); j++) {
				int begin = main_begin_list.get(j);
				if (i >= begin) {
					final int clr = main_color_list.get(j).getRGB();
					main_image.setRGB(i * 2, (int) ((main_buffer_list.get(j)[i] - main_min) * main_scale), clr);
					if (i < (chartEnd - 1) && main_smooth_list.get(j)) {
						main_image.setRGB(i * 2 + 1,
								(int) (((main_buffer_list.get(j)[i] + main_buffer_list.get(j)[i + 1]) / 2 - main_min) * main_scale), clr);
					}
				}
			}
		}
		
		for (LineData data : main_line_list) {
			final int lineCount = data.line_x_list.size();
			for (int i = 0; i < (lineCount - 1); i++) {
				int x1 = data.line_x_list.get(i);
				float fy1 = data.line_y_list.get(i);
				int y1 = (int) ((fy1 - main_min) * main_scale);
				int x2 = data.line_x_list.get(i + 1);
				float fy2 = data.line_y_list.get(i + 1);
				int y2 = (int) ((fy2 - main_min) * main_scale);
				if (x1 >= chartEnd || x2 >= chartEnd) {
					continue;
				}
				x1 *= 2;
				x2 *= 2;
				int dx = x2 - x1;
				int dy = y2 - y1;
				final int clr = data.line_color.getRGB();
				if (Math.abs(dx) > Math.abs(dy)) {
					// Draw along x
					float tg = ((float)dy) / ((float)dx);
					if (x1 > x2) {
						int swap = y1;
						y1 = y2;
						y2 = swap;
						swap = x1;
						x1 = x2;
						x2 = swap;
					}
					for (int xidx = x1; xidx <= x2; xidx ++) {
						main_image.setRGB(xidx, (int) ((xidx - x1) * tg) + y1, clr);
					}
				} else {
					// Draw along y
					float ctg = ((float)dx) / ((float)dy);
					if (y1 > y2) {
						int swap = y1;
						y1 = y2;
						y2 = swap;
						swap = x1;
						x1 = x2;
						x2 = swap;
					}
					for (int yidx = y1; yidx <= y2; yidx ++) {
						main_image.setRGB((int) ((yidx - y1) * ctg) + x1, yidx, clr);
					}
				}
			}
		}
		
		if (separate_buffer_list.size() == 0) {
			return this;
		}
		
		DoubleSummaryStatistics separate_dss = new DoubleSummaryStatistics();
		for (int i = 0; i < separate_buffer_list.size(); i++) {
			int indicator_begin = separate_begin_list.get(i);
			float[] indicator_value = Arrays.copyOfRange(separate_buffer_list.get(i), Math.max(indicator_begin, start), end);
			DoubleSummaryStatistics indicator_dss = StreamHelper.getFloatSummaryStatistics(indicator_value);
			separate_dss.combine(indicator_dss);
		}
		
		final float separate_max = (float) separate_dss.getMax();
		final float separate_min = (float) separate_dss.getMin();
		final float separate_scale = (separate_height * 0.99f) / (separate_max - separate_min);
		separate_image = new BufferedImage((end - start) * 2, separate_height, BufferedImage.TYPE_INT_RGB);
		
		for (int i = start; i < chartEnd; i++) {
			for (int j = 0; j < separate_buffer_list.size(); j++) {
				int begin = separate_begin_list.get(j);
				if (i >= begin) {
					final int clr = separate_color_list.get(j).getRGB();
					separate_image.setRGB(i * 2, (int) ((separate_buffer_list.get(j)[i] - separate_min) * separate_scale), clr);
					if (i < (chartEnd - 1) && separate_smooth_list.get(j)) {
						separate_image.setRGB(i * 2 + 1,
								(int) (((separate_buffer_list.get(j)[i] + separate_buffer_list.get(j)[i + 1]) / 2 - separate_min) * separate_scale), clr);
					}
				}
			}
		}
		return this;
	}
	
	public ChartDrawing actualDraw() {
		return actualDraw(0, 1500);
	}
	
	public void writeToFile(String filename) {
		BufferedImage merged = null;
		if (separate_image == null) {
			merged = main_image;
		} else {
			merged = ImageHelper.mergeImage(separate_image, main_image, false);
		}

		String filename_with_path = Config.ResultDir + filename;
		try {
			ImageIO.write(ImageHelper.flipImage(merged), "png", new File(filename_with_path));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

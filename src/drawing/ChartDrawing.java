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
		DoubleSummaryStatistics main_dss = new DoubleSummaryStatistics();
		DoubleSummaryStatistics high_dss = StreamHelper.getFloatSummaryStatistics(Arrays.copyOfRange(high, start, end));
		DoubleSummaryStatistics low_dss  = StreamHelper.getFloatSummaryStatistics(Arrays.copyOfRange(low , start, end));
		main_dss.combine(high_dss);
		main_dss.combine(low_dss);
		
		for (int i = 0; i < main_buffer_list.size(); i++) {
			int indicator_begin = main_begin_list.get(i);
			float[] indicator_value = Arrays.copyOfRange(main_buffer_list.get(i), Math.max(indicator_begin, start), end);
			DoubleSummaryStatistics indicator_dss = StreamHelper.getFloatSummaryStatistics(indicator_value);
			main_dss.combine(indicator_dss);
		}
		
		final float main_max = (float) main_dss.getMax();
		final float main_min = (float) main_dss.getMin();
		final float main_scale = (main_height * 0.99f) / (main_max - main_min);
		main_image = new BufferedImage((end - start) * 2, main_height, BufferedImage.TYPE_INT_RGB);
		final int chartEnd = Math.min(end, close.length);
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

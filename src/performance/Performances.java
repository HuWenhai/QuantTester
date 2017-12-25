package performance;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class Performances implements Serializable {	// TODO 用序列化保存测试结果

	private static final long serialVersionUID = 1L;
	
	public int Total_Days = 0;
	public float ProfitRatio = 0.0f;
	public float MaxDrawDown = 0.0f;
	public float SharpeRatio = 0.0f;
	public float GrossProfit = 0.0f;	// 毛利
	public float GrossLoss = 0.0f;		// 毛损
	public float ProfitFactor = 0.0f;	// 毛利 / 毛损
	
	public int TotalTrades = 0;
	public int LongTrades = 0;
	public int ShortTrades = 0;

	public Performances() {
	}

	public abstract void calculateTotal_Days();
	public abstract void calculateProfitRatio();
	public abstract void calculateMaxDrawDown();
	public abstract void calculateSharpeRatio();
	public abstract void calculateGrossProfit();
	public abstract void calculateGrossLoss();
	
	public void calculateProfitFactor() {
		if (GrossLoss != 0.0f) {
			ProfitFactor = GrossProfit / GrossLoss;
		}
	}
	
	public void calculateTotalTrades() {
		TotalTrades = LongTrades + ShortTrades;
	}
	public void calculateLongTrades() {		/* Nothing to do */	}
	public void calculateShortTrades() {	/* Nothing to do */	}
	
	public void calculateAll() {
		Field[] performance_fields = this.getClass().getFields();
		for (Field performance_field : performance_fields) {
			String method_name = "calculate" + performance_field.getName();
			try {
				Method calculate_method = this.getClass().getMethod(method_name);
				calculate_method.invoke(this);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
		}
	}

	public String toString() {
		Field[] performance_fields = this.getClass().getFields();
		StringBuilder result = new StringBuilder();
		for (Field performance_field : performance_fields) {
			String field_name = performance_field.getName();
			Object field_value = null;
			try {
				field_value = performance_field.get(this);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
			result.append(field_name + ":\t" + field_value + System.lineSeparator());
		}
		return result.toString();
	}
}

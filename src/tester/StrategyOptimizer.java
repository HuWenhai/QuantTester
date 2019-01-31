package tester;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import data.TIME_FRAME;
import global.Config;
import performance.Performances;
import strategy.template.BarBasedStrategy;

// TODO 用Fork/Join　重写
public class StrategyOptimizer implements Runnable {

	private final Class<? extends AbstractStrategyTester> astcls;
	private final int thread_num;
	private AbstractStrategyTester[] astList = null;
	private int astList_id_ptr = 0;

	private Class<? extends BarBasedStrategy> strategy_class = null;
	private Object[][] optimize_param = null;

	public StrategyOptimizer(Class<? extends AbstractStrategyTester> astcls, int thread_num) {
		this.astcls = astcls;
		this.thread_num = thread_num;
		this.astList = new AbstractStrategyTester[this.thread_num];
	}

	public StrategyOptimizer(Class<? extends AbstractStrategyTester> astcls) {
		this(astcls, Config.UseThreads);
	}

	public void setInstrumentParam(String instrument, TIME_FRAME time_frame, float init_cash, float commission_ratio) {
		Class<?>[] param = new Class[4];
		param[0] = String.class;
		param[1] = TIME_FRAME.class;
		param[2] = float.class;
		param[3] = float.class;
		
		try {
			Constructor<? extends AbstractStrategyTester> c = astcls.getConstructor(param);
			astList[0] = c.newInstance(new Object[]{instrument, time_frame, init_cash, commission_ratio});
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return;
		}

		cloneStrategyTesters();
	}

	public void setInstrumentParam(String instrument, TIME_FRAME time_frame) {
		Class<?>[] param = new Class[2];
		param[0] = String.class;
		param[1] = TIME_FRAME.class;

		try {
			Constructor<? extends AbstractStrategyTester> c = astcls.getConstructor(param);
			astList[0] = c.newInstance(new Object[]{instrument, time_frame});
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return;
		}
		
		cloneStrategyTesters();
	}
	
	private void cloneStrategyTesters() {
		for (int i = 1; i < this.thread_num; i++) {
			try {
				astList[i] = (AbstractStrategyTester) astList[0].clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setTestDateRange(int start, int end) {
		for (AbstractStrategyTester ast : astList) {
			ast.setTestDateRange(start, end);
		}
	}
	
	public int setStrategyParamRange(Class<? extends BarBasedStrategy> strategy_class, Object[]... paramranges) {	// Object[参数个数][3]
		if (strategy_class == null || paramranges == null) {
			return 0;
		}
		if (paramranges.length <= 0) {
			return 0;
		}
		this.strategy_class = strategy_class;
		
		int param_num = paramranges.length;
		int[] param_lengths = new int[param_num];
		int total_num = 1;
		int[] param_index = new int[param_num];
		
		@SuppressWarnings("unchecked")
		List<Object>[] listed_params = new List[param_num];

		for (int i = 0; i < param_num; i++) {
			listed_params[i] = new ArrayList<>();
			param_lengths[i] = 0;
			if (paramranges[i] instanceof Integer[]) {
				Integer start = ((Integer[]) paramranges[i])[0];
				Integer end   = ((Integer[]) paramranges[i])[1];
				Integer step  = ((Integer[]) paramranges[i])[2];
				if (end < start) {
					return 0;
				}
				for (Integer param = start; param <= end; param += step) {
					listed_params[i].add(param);
					param_lengths[i]++;
				}
			} else if (paramranges[i] instanceof Float[]) {
				Float start = ((Float[]) paramranges[i])[0];
				Float end   = ((Float[]) paramranges[i])[1];
				Float step  = ((Float[]) paramranges[i])[2];
				if (start > end) {
					return 0;
				}
				for (Float param = start; param <= end; param += step) {
					listed_params[i].add(param);
					param_lengths[i]++;
				}
			} else {
				for (Object param : paramranges[i]) {
					listed_params[i].add(param);
					param_lengths[i]++;
				}
			}
			total_num *= param_lengths[i];
			param_index[i] = 0;
		}

		optimize_param = new Object[total_num][param_num];
		for (int i = 0; i < total_num; i++) {
			for (int j = 0; j < param_num; j++) {
				optimize_param[i][j] = listed_params[j].get(param_index[j]);
			}
			if (i == total_num - 1) {
				break;
			}
			param_index[0] ++;
			for (int j = 0; j < param_num; j++) {
				if (param_index[j] >= param_lengths[j]) {
					param_index[j] = 0;
					param_index[j + 1] ++;
				}
			}
		}

		List<Object[]> tempList = Arrays.asList(optimize_param);
		Collections.shuffle(tempList);
		return total_num;
	}

	public Map<Object[], Performances> result_db = null;
	private int optimize_param_index = 0;
	private int completed_threads = 0;

	public void StartOptimization() {
		result_db = new HashMap<>();
		optimize_param_index = 0;
		completed_threads = 0;
		astList_id_ptr = 0;
		for (int i = 0; i < thread_num; i ++) {
			new Thread(this).start();
		}

		synchronized (optimize_param) {
			while (completed_threads < thread_num) {
				try {
					optimize_param.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		// TODO Use helper
		List<Map.Entry<Object[], Performances>> list = new LinkedList<>(result_db.entrySet());
		Collections.sort(list, (arg0, arg1) -> Float.compare(arg0.getValue().ProfitRatio, arg1.getValue().ProfitRatio));
		Map<Object[], Performances> sortedresult = new LinkedHashMap<>();
		for (Map.Entry<Object[], Performances> entry : list) {
			sortedresult.put(entry.getKey(), entry.getValue());
		}
		result_db = sortedresult;
	}
	
	@Override
	public void run() {
		int thread_id = -1;
		synchronized (astList) {
			thread_id = astList_id_ptr;
			astList_id_ptr++;
		}
		while (true) {
			Object[] param = null;
			synchronized (optimize_param) {
				if (optimize_param_index >= optimize_param.length) {
					completed_threads++;
					optimize_param.notifyAll();
					return;
				}
				param = optimize_param[optimize_param_index];
				optimize_param_index++;
				System.out.println(optimize_param_index + " / " + optimize_param.length);
			}
			astList[thread_id].setStrategyParam(strategy_class, param);
			astList[thread_id].evaluate();
			synchronized (result_db) {
				result_db.put(param, astList[thread_id].performances);
			}
		}
	}
}

package global;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import data.TIME_FRAME;
import indicator.IIndicator;

public class Cache {
	private static final Map<String, IIndicator> indicators = new ConcurrentHashMap<>();
	private static final Map<String, Object> signals = new ConcurrentHashMap<>();

	public static String makeKey(final List<Object> objects) {
		int size = objects.size();
		if (size <= 0) {
			return "";
		}

		StringBuilder key = new StringBuilder(objects.get(0).toString());
		for (int i = 1; i < size; i++) {
			key.append("_");
			key.append(objects.get(i).toString());
		}
		return key.toString();
	}

	public static Object getCachedSignal(Supplier<Object> supplier, String instrument, TIME_FRAME tf, int begin, int end, String name, Object...params) {
		List<Object> keyIngredients = new ArrayList<>(params.length + 5);
		keyIngredients.add(instrument);
		keyIngredients.add(tf);
		keyIngredients.add(begin);
		keyIngredients.add(end);
		keyIngredients.add(name);

		for (Object param : params) {
			keyIngredients.add(param);
		}

		String key = makeKey(keyIngredients);
		return getSignal(key, supplier);
	}

	private static Object getSignal(String key, Supplier<Object> supplier) {
		Object obj = signals.get(key);
		if (obj == null) {
			obj = supplier.get();
			signals.put(key, obj);
		}
		return obj;
	}
}

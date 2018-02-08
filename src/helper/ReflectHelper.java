package helper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ReflectHelper {
	private final static List<Field> getAllFieldsIncludingSuper(Class<?> objCls) {
		List<Field> allFields = new ArrayList<>();
		for (Class<?> cls = objCls; cls != null; cls = cls.getSuperclass()) {
			Field[] fields = null;
			try {
				fields = cls.getDeclaredFields();
			} catch (Exception e) {
				fields = null;
			}
			if (fields != null) {
				allFields.addAll(Arrays.asList(fields));
			}
		}
		return allFields;
	}

	public final static Object[] getAllFieldObjectsIncludingSuper(Object obj) {
		List<Field> allFields = getAllFieldsIncludingSuper(obj.getClass());
		int size = allFields.size();
		Object[] allFieldObjects = new Object[size];
		for (int i = 0; i < size; i ++) {
			Field field = allFields.get(i);
			field.setAccessible(true);
			try {
				allFieldObjects[i] = field.get(obj);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return allFieldObjects;
	}

	private final static Field getFieldIncludingSuper(Class<?> objCls, final String field_name) {
		Field field = null;
		for (Class<?> cls = objCls; cls != null; cls = cls.getSuperclass()) {
			try {
				field = cls.getDeclaredField(field_name);
			} catch (Exception e) {
				field = null;
			}
			if (field != null) {
				break;
			}
		}
		return field;
	}

	public final static Object getPrivateField(final Object obj, final String field_name) {
		Field field = getFieldIncludingSuper(obj.getClass(), field_name);
		Object field_obj = null;
		if (field != null) {
			field.setAccessible(true);
			try {
				field_obj = field.get(obj);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return field_obj;
	}

	public final static void setPrivateField(final Object obj, final String field_name, final Object value) {
		Field field = getFieldIncludingSuper(obj.getClass(), field_name);
		if (field != null) {
			field.setAccessible(true);
			try {
				field.set(obj, value);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
}

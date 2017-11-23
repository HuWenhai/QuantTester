package helper;

import java.lang.reflect.Field;

public final class ReflectHelper {
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

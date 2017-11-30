package test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.Scanner;

import data.TIME_FRAME;

public class ParamManager {
	public static CommonParam getCommonParam(String default_instrument, TIME_FRAME default_tf, String default_start_date, String default_end_date) {
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		System.out.print("Instrument: ");
		String instrument = scanner.nextLine();
		if (instrument.isEmpty()) {
			instrument = default_instrument;
			System.out.println("(default): " + default_instrument);
		}
		
		System.out.print("Time frame: ");
		String tfStr = scanner.nextLine();
		
		TIME_FRAME tf = null;
		try {
			tf = TIME_FRAME.valueOf(tfStr);
		} catch (IllegalArgumentException e) {
			tf = default_tf;
			System.out.println("(default): " + default_tf);
		}
		
		LocalDateTime start_date;
		while (true) {
			System.out.print("Start Date: ");
			String dateStr = scanner.nextLine();
			if (dateStr.isEmpty()) {
				dateStr = default_start_date;
				System.out.println("(default): " + default_start_date);
			}
			start_date = parseDateTime(dateStr);
			if (start_date != null) {
				break;
			}
		}
		
		LocalDateTime end_date;
		while (true) {
			System.out.print("End Date: ");
			String dateStr = scanner.nextLine();
			if (dateStr.isEmpty()) {
				dateStr = default_end_date;
				System.out.println("(default): " + default_end_date);
			}
			end_date = parseDateTime(dateStr);
			if (end_date != null) {
				break;
			}
		}
		return new CommonParam(instrument, tf, start_date, end_date);
	}
	
	private static LocalDateTime parseDateTime(String dateStr) {
		LocalDateTime datetime = null;
		boolean ok = false;
		try {
		    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss");
		    datetime = LocalDateTime.parse(dateStr, formatter);
		    ok = true;
		} catch (DateTimeParseException ex) {
		    ok = false;
		}
		return ok ? datetime : null;
	}
	
	public static Object[] getParticularParam(Class<?> cls, Object ... default_params) {
		Constructor<?>[] ctors = cls.getConstructors();
		final int ctorCount = ctors.length;
		int matchIdx = 0;
		for (; matchIdx < ctorCount; matchIdx++) {
			if (default_params.length == ctors[matchIdx].getParameterCount()) {
				// TODO parameter types should also match
				break;
			}
		}
		if (matchIdx == ctorCount) {
			return null;
		}
		Class<?>[] paramTypes = ctors[matchIdx].getParameterTypes();
		
		final int paramCount = paramTypes.length;
		Object[] params = new Object[paramCount];
		Scanner scanner = new Scanner(System.in);
		for (int i = 0; i < paramCount; i++) {
			System.out.print("Param " + i + " :");
			params[i] = getParamByCls(scanner, paramTypes[i]);
			if (params[i] == null) {
				params[i] = default_params[i];
				System.out.println("(default) " + default_params[i]);
			} else if (paramTypes[i] == String.class && ((String)params[i]).isEmpty()) {
				params[i] = default_params[i];
				System.out.println("(default) " + default_params[i]);
			}
		}
		return params;
	}
	
	private static Object getParamByCls(Scanner scanner, Class<?> cls) {
		Object ret = null;
		if (cls == Integer.class) {
			try {
				int intParam = scanner.nextInt();
				ret = intParam;
			} catch (InputMismatchException e) {
				ret = null;
			} finally {
				scanner.nextLine();
			}
		} else if (cls == Float.class) {
			try {
				float floatParam = scanner.nextFloat();
				ret = floatParam;
			} catch (InputMismatchException e) {
				ret = null;
			} finally {
				scanner.nextLine();
			}
		} else if (cls == String.class) {
			ret = scanner.nextLine();
		} else if (cls.isEnum()) {
			String enumStr = scanner.nextLine();
			if (enumStr.isEmpty()) {
				return null;
			}
			try {
				Method method = cls.getMethod("valueOf", String.class);
				Object obj = method.invoke(null, enumStr);
				ret = obj;
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}
}

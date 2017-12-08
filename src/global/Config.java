package global;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Config {
	
	private static final Logger logger = LogManager.getLogger();
	
	private static final Properties configFile;

	public static final String KTExportDir;
	public static final String SinYeeDataDir;
	public static final boolean UseCompondBars;
	public static final String ResultDir;
	public static final int UseThreads;

	private static String addSeparatorIfNecessary(String path) {
		if (path.endsWith(File.separator)) {
			return path;
		} else {
			return path + File.separator;
		}
	}

	static {
		configFile = new Properties();

		String KTExportDir_tmp = null;
		String SinYeeDataDir_tmp = null;
		String CompondBars_str = null;
		String ResultDir_tmp = null;
		String UseThreads_str = null;

		try (FileInputStream in = new FileInputStream("config.ini")) {
			configFile.load(in);
			KTExportDir_tmp = configFile.get("KTExportDir").toString();
			SinYeeDataDir_tmp = configFile.get("SinYeeDataDir").toString();
			CompondBars_str = configFile.get("UseCompondBars").toString();
			ResultDir_tmp = configFile.get("ResultDir").toString();
			UseThreads_str = configFile.get("UseThreads").toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		KTExportDir = addSeparatorIfNecessary(KTExportDir_tmp);
		SinYeeDataDir = addSeparatorIfNecessary(SinYeeDataDir_tmp);
		ResultDir = addSeparatorIfNecessary(ResultDir_tmp);

		int num = 1;
		boolean invalid_num = false;
		try {
			num = Integer.parseInt(UseThreads_str);
		} catch (NumberFormatException e) {
			invalid_num = true;
		}
		if (invalid_num || num < 1) {
			logger.warn("Invalid \"UseThreads\" value, will use 1 thread.");
			num = 1;
		}
		UseThreads = num;
		UseCompondBars = Boolean.parseBoolean(CompondBars_str);
	}
}

package cz.zcu.jsmahy.datamining.config;

import cz.zcu.jsmahy.datamining.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;

/**
 * @author Jakub Šmrha
 * @version 1.0
 * @since 1.0
 */
public final class Config {
	private static final Logger     L                  = LogManager.getLogger(Config.class);
	private static final Properties DEFAULT_PROPERTIES = new Properties();
	private static final Properties PROPERTIES         = new Properties();
	private static final String     FILENAME           = "config.properties";
	private static final Config     INSTANCE           = new Config();

	static {
		reload();
	}

	//<editor-fold desc="Config properties">
	@Property(key = "max-depth", defaultValue = "10")
	public int maxDepth;
	//</editor-fold>

	private static void reload() {
		L.info("Reloading...");
		File f = new File(FILENAME);
		InputStream in = null;
		try {
			if (!f.exists()) {
				in = Main.class.getClassLoader()
				               .getResourceAsStream(FILENAME);
			} else {
				in = new FileInputStream(f);
			}
			DEFAULT_PROPERTIES.load(in);
			L.info("Successfully loaded properties");
		} catch (IOException e) {
			L.catching(e);
		} finally {
			PROPERTIES.putAll(DEFAULT_PROPERTIES);
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					L.catching(e);
				}
			}
			autowire();
		}
	}

	/**
	 * Maps property key/values to fields with {@link Property} annotation
	 */
	private static void autowire() {
		L.info("Autowiring fields...");
		for (Field f : Config.class.getDeclaredFields()) {
			if (!f.isAnnotationPresent(Property.class)) {
				continue;
			}
			Property prop = f.getAnnotation(Property.class);
			f.setAccessible(true);
			try {
				final String value = PROPERTIES.getProperty(prop.key(), prop.defaultValue());
				L.trace(String.format("Setting %s to %s", f.getName(), value));
				final Class<?> type = f.getType();
				if (type == int.class) {
					f.setInt(INSTANCE, Integer.parseInt(value));
				} else if (type == float.class) {
					f.setFloat(INSTANCE, Float.parseFloat(value));
				} else if (type == double.class) {
					f.setDouble(INSTANCE, Double.parseDouble(value));
				} else if (type == boolean.class) {
					f.setBoolean(INSTANCE, Boolean.parseBoolean(value));
				} else if (type == char.class) {
					f.setChar(INSTANCE, value == null || value.isEmpty() ? ' ' : value.charAt(0));
				} else {
					f.set(INSTANCE, value);
				}
			} catch (Exception e) {
				// TODO do sth better ig
				L.catching(e);
			}
		}
	}

	public static Config getInstance() {
		return INSTANCE;
	}

	public int getMaxDepth() {
		return maxDepth;
	}
}

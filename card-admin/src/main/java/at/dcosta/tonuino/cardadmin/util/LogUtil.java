package at.dcosta.tonuino.cardadmin.util;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Arrays;

public class LogUtil {
	
	public static final void debug(Logger logger, Object...message) {
		log(logger, Level.DEBUG, message);
	}
	
	public static final void error(Logger logger, Object...message) {
		log(logger, Level.ERROR, message);
	}
	
	public static final void info(Logger logger, Object...message) {
		log(logger, Level.INFO, message);
	}
	
	public static final void trace(Logger logger, Object...message) {
		log(logger, Level.TRACE, message);
	}
	
	public static final void log(Logger logger, Level level, Object...message) {
		if (logger.isLoggable(level)) {
			StringBuilder b = new StringBuilder();
			Arrays.stream(message).forEach(s -> b.append(s));
			logger.log(level, b.toString());
		}
	}

}

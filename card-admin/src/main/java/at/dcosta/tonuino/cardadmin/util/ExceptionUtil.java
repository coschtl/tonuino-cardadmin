package at.dcosta.tonuino.cardadmin.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ExceptionUtil {
	
	public static String getStacktrace(Throwable t) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try (PrintStream ps = new PrintStream(bout)) {
			t.printStackTrace(ps);
			ps.flush();
		}
		return new String (bout.toByteArray());
	}

}

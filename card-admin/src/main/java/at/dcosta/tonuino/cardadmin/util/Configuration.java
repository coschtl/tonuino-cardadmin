package at.dcosta.tonuino.cardadmin.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Configuration {

	private Properties props;

	private Configuration() {
		String cfgfilePath = System.getenv("cardAdminConfigFile");
		InputStream in = null;
		try {
			if (cfgfilePath != null && new File(cfgfilePath).exists()) {
				in = new FileInputStream(new File(cfgfilePath));
			} else {
				getClass().getClassLoader().getResourceAsStream("configuration.properties");
			}
			props = new Properties();
			props.load(in);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
				// ignore
			}
		}
	}

	private static Configuration INSTANCE;
	static {
		INSTANCE = new Configuration();
	}

	public static Configuration getInstance() {
		return INSTANCE;
	}

	public boolean hasNormalizer() {
		return getNormalizer() != null;
	}

	private String getNormalizer() {
		return props.getProperty("mp3.normalizing.commandline");
	}

	public String getCardIndexLocation() {
		return props.getProperty("card-index.location");
	}

	public List<String> getNormalizerCommand() {
		List<String> cmd = new ArrayList<>();
		cmd.add(getNormalizer());
		for (String option : getNormalizerOptions()) {
			cmd.add(option);
		}
		return cmd;
	}

	private String[] getNormalizerOptions() {
		return props.getProperty("mp3.normalizing.options").split("\\s");
	}

	public String getAlternativeCardRoot() {
		String aRoot = props.getProperty("alternative.card.root");
		return aRoot == null ? null : aRoot.trim();
	}

	public File getDefaultContentRoot() {
		String aRoot = props.getProperty("default.content.root");
		return aRoot == null ? null : new File(aRoot.trim());
	}

	public boolean isAlternativeCardRoot(File f) {
		String alternativeCardRoot = getAlternativeCardRoot();
		if (alternativeCardRoot == null) {
			return false;
		}
		Path altPath = Path.of(alternativeCardRoot);
		Path filePath = f.toPath();
		return filePath.equals(altPath);
	}
}

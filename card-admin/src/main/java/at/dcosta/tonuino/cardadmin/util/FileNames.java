package at.dcosta.tonuino.cardadmin.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FileNames {
	private static Pattern PATTERN_DIR = Pattern.compile("^\\d\\d$");
	private static Pattern PATTERN_FILE = Pattern.compile("^\\d\\d\\d.mp3$", Pattern.CASE_INSENSITIVE);

	public static String getNextFolderName(File parentFolder) throws IOException {
		return getNextName(parentFolder, 100, PATTERN_DIR);
	}

	public static String getNextFileName(File parentFolder) throws IOException {
		return getNextName(parentFolder, 1000, PATTERN_FILE);
	}

	public static String getNextName(File parentFolder, int toAdd, Pattern pattern) throws IOException {
		List<Path> files = Files.list(parentFolder.toPath())
				.filter(path -> pattern.matcher(path.getFileName().toString()).matches()).sorted()
				.collect(Collectors.toList());
		if (files.size() > 0) {
			String lastFileName = files.get(files.size() - 1).getFileName().toString();
			Integer max = Integer.parseInt(lastFileName.substring(0, Integer.toString(toAdd).length() -1));
			return Integer.toString(toAdd + 1 + max).substring(1);
		} else {
			return Integer.toString(toAdd).substring(1);
		}
	}

	public static Iterator<String> getNextFileNames(File parentFolder) throws IOException {
		final int base = Integer.parseInt(getNextFileName(parentFolder));
		return new Iterator<String>() {
			int i = 0;

			@Override
			public boolean hasNext() {
				return true;
			}

			@Override
			public String next() {
				return Integer.toString(1000 + base + i++).substring(1);
			}
		};
	}

}

package at.dcosta.tonuino.cardadmin.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class FileNames {

	public enum Type {
		FILE(255, 1000, Pattern.compile("^((00[1-9])|([1-9]\\d\\d)|(\\d[1-9]\\d)).mp3$", Pattern.CASE_INSENSITIVE)),
		FOLDER(99, 100, Pattern.compile("^((0[1-9])|([1-9]\\d))$"));

		private final int maxValue;
		private final int toAdd;
		private Pattern pattern;

		private Type(int maxValue, int toAdd, Pattern pattern) {
			this.maxValue = maxValue;
			this.toAdd = toAdd;
			this.pattern = pattern;
		}

		public int getMaxValue() {
			return maxValue;
		}

		public int getToAdd() {
			return toAdd;
		}

		public Pattern getPattern() {
			return pattern;
		}
	}

	public static final String SUFFIX_MP3 = ".mp3";

	public static final Set<String> SYSTEM_FOLDERS;

	static {
		SYSTEM_FOLDERS = new HashSet<>();
		SYSTEM_FOLDERS.add("advert");
		SYSTEM_FOLDERS.add("mp3");
	}

	public static String getNextFolderName(File parentFolder) throws IOException {
		return getNextNumber(parentFolder, Type.FOLDER);
	}

	public static String createDateFileName(String prefix, String suffix) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		StringBuilder b = new StringBuilder(prefix);
		b.append('_');
		b.append(toFixedDigitString(cal.get(Calendar.YEAR), 4));
		b.append(toFixedDigitString(1 + cal.get(Calendar.MONTH), 2));
		b.append(toFixedDigitString(cal.get(Calendar.DAY_OF_MONTH), 2));
		b.append('_');
		b.append(toFixedDigitString(cal.get(Calendar.HOUR_OF_DAY), 2));
		b.append(toFixedDigitString(cal.get(Calendar.MINUTE), 4));
		b.append(toFixedDigitString(cal.get(Calendar.SECOND), 4));
		b.append(suffix);
		return b.toString();
	}

	public static String toFixedDigitString(int val, int digits) {
		return Integer.toString(val + (int) Math.pow(10, digits)).substring(1);
	}

	public static String getNextFileNumber(File parentFolder) throws IOException {
		return getNextNumber(parentFolder, Type.FILE);
	}

	public static String getNextNumber(File parentFolder, Type type) throws IOException {
		List<Path> files = Files.list(parentFolder.toPath())
				.filter(path -> type.getPattern().matcher(path.getFileName().toString()).matches()).sorted()
				.collect(Collectors.toList());
		int highestExisting;
		if (files.size() > 0) {
			String lastFileName = files.get(files.size() - 1).getFileName().toString();
			highestExisting = Integer
					.parseInt(lastFileName.substring(0, Integer.toString(type.getToAdd()).length() - 1));
		} else {
			highestExisting = 0;
		}
		int newNumber = 1 + highestExisting;
		if (newNumber <= type.getMaxValue()) {
			return Integer.toString(type.getToAdd() + newNumber).substring(1);
		}
		throw new IllegalArgumentException("Too manny files/folders!");
	}

	public static Iterator<Path> createNewFileNameSeries(Path parent) {
		return new Iterator<Path>() {
			int i = 1;

			@Override
			public boolean hasNext() {
				return i <= Type.FILE.getMaxValue();
			}

			@Override
			public Path next() {
				if (i <= Type.FILE.getMaxValue()) {
					String fileName = Integer.toString(1000 + i++).substring(1);
					return Paths.get(parent.toString(), fileName + SUFFIX_MP3);
				}
				throw new IllegalArgumentException("Too manny files!");
			}
		};
	}

	public static Iterator<String> getNextFileNames(File parentFolder) throws IOException {
		final int base = Integer.parseInt(getNextFileNumber(parentFolder));
		return new Iterator<String>() {
			int i = base;

			@Override
			public boolean hasNext() {
				return i <= Type.FILE.getMaxValue();
			}

			@Override
			public String next() {
				if (i <= Type.FILE.getMaxValue()) {
					return Integer.toString(1000 + i++).substring(1) + SUFFIX_MP3;
				}
				throw new IllegalArgumentException("Too manny files!");
			}
		};
	}

	public static int getFileNumber(Path path) {
		String name = path.getFileName().toString();
		if (!name.toLowerCase().endsWith(SUFFIX_MP3)) {
			throw new IllegalArgumentException("FileName does not have suffix " + SUFFIX_MP3);
		}
		return Integer.parseInt(name.substring(0, name.length() - SUFFIX_MP3.length()));
	}

}

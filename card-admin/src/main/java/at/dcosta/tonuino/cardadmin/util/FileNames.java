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
	public static final String SUFFIX_MP3 = ".mp3";
	
	public static final Pattern PATTERN_DIR = Pattern.compile("^((0[1-9])|([1-9]\\d))$");
	public static final Pattern PATTERN_FILE = Pattern.compile("^((00[1-9])|([1-9]\\d\\d)|(\\d[1-9]\\d)).mp3$", Pattern.CASE_INSENSITIVE);
	public static final Set<String> SYSTEM_FOLDERS;
	
	static {
		SYSTEM_FOLDERS = new HashSet<>();
		SYSTEM_FOLDERS.add("advert");
		SYSTEM_FOLDERS.add("mp3");
	}

	public static String getNextFolderName(File parentFolder) throws IOException {
		return getNextNumber(parentFolder, 100, PATTERN_DIR);
	}
	
	public static String createDateFileName(String prefix, String suffix) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		StringBuilder b = new StringBuilder(prefix);
		b.append('_');
		b.append(toFixedDigitString(cal.get(Calendar.YEAR), 4));
		b.append(toFixedDigitString(1+ cal.get(Calendar.MONTH), 2));
		b.append(toFixedDigitString(cal.get(Calendar.DAY_OF_MONTH), 2));
		b.append('_');
		b.append(toFixedDigitString(cal.get(Calendar.HOUR_OF_DAY), 2));
		b.append(toFixedDigitString(cal.get(Calendar.MINUTE), 4));
		b.append(toFixedDigitString(cal.get(Calendar.SECOND), 4));
		b.append(suffix);
		return b.toString();
	}
	
	public static String toFixedDigitString(int val, int digits) {
		int i = val + (int) Math.pow(10,  digits);
		System.out.println(val + "(" + digits + ") -> " + i);
		return Integer.toString(i).substring(1);
	}

	public static String getNextFileNumber(File parentFolder) throws IOException {
		return getNextNumber(parentFolder, 1000, PATTERN_FILE);
	}

	public static String getNextNumber(File parentFolder, int toAdd, Pattern pattern) throws IOException {
		List<Path> files = Files.list(parentFolder.toPath())
				.filter(path -> pattern.matcher(path.getFileName().toString()).matches()).sorted()
				.collect(Collectors.toList());
		int highestExisting;
		if (files.size() > 0) {
			String lastFileName = files.get(files.size() - 1).getFileName().toString();
			highestExisting = Integer.parseInt(lastFileName.substring(0, Integer.toString(toAdd).length() -1));
		} else {
			highestExisting = 0;
		}
		return Integer.toString(toAdd + 1 + highestExisting).substring(1);
	}
	
	public static Iterator<Path> createNewFileNameSeries(Path parent)  {
		return new Iterator<Path>() {
			int i = 1;
			@Override
			public boolean hasNext() {
				return true;
			}

			@Override
			public Path next() {
				String fileName =  Integer.toString(1000 + i++).substring(1);
				return Paths.get(parent.toString(), fileName + SUFFIX_MP3);
			}
		};
	}

	public static Iterator<String> getNextFileNames(File parentFolder) throws IOException {
		int base = Integer.parseInt(getNextFileNumber(parentFolder));
		return new Iterator<String>() {
			int i = 0;

			@Override
			public boolean hasNext() {
				return true;
			}

			@Override
			public String next() {
				return Integer.toString(1000 + base + i++).substring(1) + SUFFIX_MP3;
			}
		};
	}
	
	public static int getFileNumber(Path path) {
		String name = path.getFileName().toString();
		if (!name.toLowerCase().endsWith(SUFFIX_MP3)) {
			throw new IllegalArgumentException("FileName does not have suffix " + SUFFIX_MP3);
		}
		return Integer.parseInt(name.substring(0,name.length() - SUFFIX_MP3.length()));
	}

}

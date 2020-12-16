package at.dcosta.tonuino.cardadmin.util;

import static at.dcosta.tonuino.cardadmin.util.FileNames.PATTERN_DIR;
import static at.dcosta.tonuino.cardadmin.util.FileNames.PATTERN_FILE;
import static at.dcosta.tonuino.cardadmin.util.FileNames.SYSTEM_FOLDERS;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CardFilesystemUtil {

	public static class RequiredAction {
		public enum Action {
			DELETE, ADD, RENAME;
		}

		public static RequiredAction DELETE = new RequiredAction(Action.DELETE);
		public static RequiredAction ADD = new RequiredAction(Action.ADD);

		public static RequiredAction createRenameAction(String newName) {
			return new RequiredAction(Action.RENAME, newName);
		}

		private final Action action;
		private String additionalInfo;

		private RequiredAction(Action action) {
			this.action = action;
		}

		private RequiredAction(Action action, String additionalInfo) {
			this.action = action;
			this.additionalInfo = additionalInfo;
		}

		public Action getAction() {
			return action;
		}

		public String getAdditionalInfo() {
			return additionalInfo;
		}

		@Override
		public String toString() {
			if (additionalInfo == null) {
				return action.toString();
			}
			return action.toString() + " to " + additionalInfo;
		}
	}

	private static final Logger LOGGER = System.getLogger(TrackSorter.class.getName());

	public static Map<Path, RequiredAction> analyzeRoot(Path root) throws IOException {
		Map<Path, RequiredAction> changes = new TreeMap<>();
		Files.list(root).forEach(path -> {
			String filename = path.getFileName().toString();
			if (!SYSTEM_FOLDERS.contains(filename) && !PATTERN_DIR.matcher(filename).matches()) {
				System.out.println(filename + " no match: " + PATTERN_DIR.pattern());
				changes.put(path, RequiredAction.DELETE);
			}
		});
		SYSTEM_FOLDERS.forEach(folder -> {
			Path path = Path.of(root.toString(), folder);
			if (!path.toFile().isDirectory()) {
				changes.put(path, RequiredAction.ADD);
			}
		});
		return changes;
	}

	public static Map<Path, RequiredAction> correctFolders(Path root, boolean simulate) throws IOException {
		Map<Path, RequiredAction> changes = new TreeMap<>();
		Files.list(root).forEach(folder -> {
			if (PATTERN_DIR.matcher(folder.getFileName().toString()).matches()) {
				try {
					correctFolder(folder, changes, simulate);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
		return changes;
	}
	
	public static String toGermanText(Map<Path, RequiredAction> changes) {
		StringBuilder b = new StringBuilder();
		changes.entrySet().forEach(e -> {
			b.append(e.getKey());
			RequiredAction action = e.getValue();
			switch (action.getAction()) {
			case ADD:
				b.append( " fehlt\n");
				break;
			case DELETE:
				b.append( " muss gelöscht werden\n");
				break;
			case RENAME:
				b.append( " muss");
				if (action.getAdditionalInfo() != null) {
					b.append(" nach ").append(action.getAdditionalInfo());
				}
				b.append(" umbenannt werden\n");
				break;
			}
		});
		return b.toString();
	}

	private static void correctFolder(Path folder, Map<Path, RequiredAction> changes, boolean simulate) throws IOException {
		List<Path> tracks = new ArrayList<>();
		Files.list(folder).forEach(filePath -> {
			File file = filePath.toFile();
			if (file.isDirectory()) {
				if (!simulate) {
					file.delete();
				}
				changes.put( filePath, RequiredAction.DELETE);
			} else {
				String filename = filePath.getFileName().toString();
				if (!PATTERN_FILE.matcher(filename).matches()) {
					if (!simulate) {
						file.delete();
					}
					changes.put( filePath, RequiredAction.createRenameAction(null));
				} else {
					tracks.add(filePath);
				}
			}
		});
		correctFilenames(tracks, changes, simulate);
	}

	private static void correctFilenames(List<Path> paths, Map<Path, RequiredAction> changes, boolean simulate) {
		if (paths.isEmpty()) {
			return;
		}
		Map<Path, Path> renames = new HashMap<>();
		Iterator<Path> newPaths = FileNames.createNewFileNameSeries(paths.get(0).getParent());
		paths.forEach(path -> {
			Path newPath = newPaths.next();
			LogUtil.trace(LOGGER, "path: ", path, " - should be: ", newPath);
			if (!newPath.equals(path)) {
				Path tmpPath = Paths.get(path.toString() + ".tmp");
				try {
					if (!simulate) {
						Files.move(path, tmpPath);
					}
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
				LogUtil.debug(LOGGER, "rename1: ", path, " -> ", tmpPath);
				renames.put(tmpPath, newPath);
			}
		});
		renames.entrySet().forEach(e -> {
			try {
				if (!simulate) {
					Files.move(e.getKey(), e.getValue());
				}
				String origName = e.getKey().toString();
				changes.put(  Path.of(origName.substring(0, origName.length()-4)), RequiredAction.createRenameAction(e.getValue().toString()));
				LogUtil.debug(LOGGER, "rename2: ", e.getKey(), " -> ", e.getValue());
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		});
	}

}

package at.dcosta.tonuino.cardadmin.util;

import java.io.IOException;
import java.lang.System.Logger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import at.dcosta.tonuino.cardadmin.Track;
import at.dcosta.tonuino.cardadmin.ui.ErrorDisplay;
import at.dcosta.tonuino.cardadmin.ui.ValueResolver;

public class TrackSorter {

	private static final Logger LOGGER = System.getLogger(TrackSorter.class.getName());

	public static void correctFilenames(List<Track> tracks, ErrorDisplay errorDisplay) {
		if (tracks.isEmpty()) {
			return;
		}
		Map<Path, Path> renames = new HashMap<>();
		Iterator<Path> newPaths = FileNames.createNewFileNameSeries(tracks.get(0).getPath().getParent());
		tracks.forEach(t -> {
			Path newPath = newPaths.next();
			LogUtil.trace(LOGGER, "path: ", t.getPath(), " - should be: ", newPath);
			if (!newPath.equals(t.getPath())) {
				Path tmpPath = Paths.get(t.getPath().toString() + ".tmp");
				try {
					Files.move(t.getPath(), tmpPath);
					LogUtil.debug(LOGGER, "rename1: ", t.getPath(), " -> ", tmpPath);
				} catch (IOException e) {
					errorDisplay.showError("Can not rename tracks", e);
				}
				renames.put(tmpPath, newPath);
			}
		});
		renames.entrySet().forEach(e -> {
			try {
				Files.move(e.getKey(), e.getValue());
				LogUtil.debug(LOGGER, "rename2: ", e.getKey(), " -> ", e.getValue());
			} catch (IOException ex) {
				errorDisplay.showError("Can not rename tracks", ex);
			}
		});
	}

	public static void sortByFilename(List<Track> tracks) {
		tracks.sort(new Comparator<Track>() {

			@Override
			public int compare(Track t1, Track t2) {
				return t1.getPath().toString().compareTo(t2.getPath().toString());
			}
		});
	}

	public static void sort(List<Track> tracks, ValueResolver<?> valueResolver) {
		tracks.sort(new Comparator<Track>() {

			@Override
			public int compare(Track t1, Track t2) {
				return valueResolver.getValue(t1).toString().compareTo(valueResolver.getValue(t2).toString());
			}
		});
	}

}

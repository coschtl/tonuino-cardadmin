package at.dcosta.tonuino.cardadmin;

import static at.dcosta.tonuino.cardadmin.util.FileNames.PATTERN_DIR;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.System.Logger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;

import at.dcosta.tonuino.cardadmin.util.Configuration;
import at.dcosta.tonuino.cardadmin.util.FileNames;
import at.dcosta.tonuino.cardadmin.util.LogUtil;
import at.dcosta.tonuino.cardadmin.util.TrackSorter;

public class IndexGenerator {
	
	private static final Logger LOGGER = System.getLogger(IndexGenerator.class.getName());

	public void createIndexfile(Path root) throws IOException {
		File folder = new File( Configuration.getInstance().getCardIndexLocation());
		File indexFile =new File(folder, FileNames.createDateFileName("index", ".txt"));
		PrintStream out= new  PrintStream(indexFile);
		Files.list(root).forEach(path -> {
			String filename = path.getFileName().toString();
			if (PATTERN_DIR.matcher(filename).matches()) {
				try {
					addFolderToIndex(path, out);
				} catch (IOException e) {
					LogUtil.error(LOGGER, "Can not read Track from", path,": ", e);
				}
			}
		});
		out.flush();
		out.close();
		Desktop.getDesktop().open(indexFile);
	}

	private void addFolderToIndex(Path path, PrintStream out) throws IOException {
		List<Track> tracks = Files.list(path)
				.filter(file -> file.getFileName().toString().toLowerCase().endsWith(FileNames.SUFFIX_MP3))
				.map(file -> {
					try {
						return new Track(file);
					} catch (UnsupportedTagException | InvalidDataException | IOException e) {
						LogUtil.error(LOGGER, "Can not read Tracks: ", e);
						return null;
					}
				}).filter(track -> track != null).collect(Collectors.toList());
		if (tracks.isEmpty()) {
			return;
		}
		TrackSorter.sortByFilename(tracks);
		out.println(path.getFileName().toString() + " - " + tracks.get(0).getAlbum() + ":\n");
		int i=1;
		for (Track t: tracks ) {
			if (i<10) {
				out.print(" ");
			}
			out.print(i++);
			out.print(". ");
			out.println(t.getTitle());
		};
		out.println("-------------------------------------------\n");
		out.println();
	}

}

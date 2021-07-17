package at.dcosta.tonuino.cardadmin;

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
import at.dcosta.tonuino.cardadmin.util.FileNames.Type;
import at.dcosta.tonuino.cardadmin.util.LogUtil;
import at.dcosta.tonuino.cardadmin.util.TrackSorter;

public class IndexGenerator {
	
	public enum IndexFormat {
		CSV, HUMAN_READABLE;
	}
	
	private class FolderDescription {
		private final int folderNumber;
		private final String folderName;
		
		public FolderDescription(int folderNumber, String folderName) {
			this.folderNumber=folderNumber;
			this.folderName=folderName;
		}
		public String getFolderName() {
			return folderName;
		}
		public int getFolderNumber() {
			return folderNumber;
		}
	}
	private static final Logger LOGGER = System.getLogger(IndexGenerator.class.getName());
	
	private final IndexFormat format;
	
	public IndexGenerator(IndexFormat format) {
		this.format = format;
	}

	public void createIndexfile(Path root) throws IOException {
		File folder = new File( Configuration.getInstance().getCardIndexLocation());
		String suffix =  format == IndexFormat.CSV ? ".csv" : ".txt";
		File indexFile =new File(folder, FileNames.createDateFileName("index", suffix));
		PrintStream out= new  PrintStream(indexFile);
		if (format == IndexFormat.CSV) {
			out.println("directoryNumber,fileNumber,directoryName,subdirectoryName,title,tags,runtime");
		}
		Files.list(root).forEach(path -> {
			String filename = path.getFileName().toString();
			if (Type.FOLDER.getPattern().matcher(filename).matches()) {
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
		FolderDescription folderDescription = new FolderDescription(Integer.parseInt(path.getFileName().toString()), tracks.get(0).getAlbum());
		printFoderHeader(folderDescription, out);
		int i=1;
		for (Track t: tracks ) {
			printTrack(i, t, folderDescription, out);
			i++;			
		};
		printFolderFooter(out);
	}
	
	private void printFoderHeader (FolderDescription folderDescription, PrintStream out) {
		if (format == IndexFormat.HUMAN_READABLE) {
			out.println(folderDescription.getFolderNumber() + " - " + folderDescription.getFolderName() + ":\n");
		}
	}
	
	private void printTrack (int trackNumber, Track track,  FolderDescription folderDescription, PrintStream out) {
		if (format == IndexFormat.HUMAN_READABLE) {
			if (trackNumber <10) {
				out.print(" ");
			}
			out.print(trackNumber);
			out.print(". ");
			out.println(track.getTitle());
		} else {
			out.print(folderDescription.getFolderNumber());
			out.print(",");
			out.print(trackNumber);
			addMasked(folderDescription.getFolderName(), out);
			addMasked(folderDescription.getFolderName(), out);
			addMasked(track.getTitle(), out);
			addMasked(track.getArtist(), out);
			out.print(",");
			out.println(track.getLengthInSeconds());
		}
	}
	
	private void addMasked(String s, PrintStream out) {
		if (s != null) {
			out.print(',');
			out.print('"');
			out.print(s.replaceAll("\"", "\"\""));
			out.print('"');
		} else {
			out.print(',');
		}
	}
	
	private void printFolderFooter (PrintStream out) {
		if (format == IndexFormat.HUMAN_READABLE) {
			out.println("-------------------------------------------\n");
			out.println();
		}
	}

}

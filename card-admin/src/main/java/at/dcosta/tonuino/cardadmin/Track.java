package at.dcosta.tonuino.cardadmin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v1Tag;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;

public class Track {
	private final Mp3File mp3File;
	private final Path path;

	private String album;
	private String artist;
	private String title;
	private int trackNumber;
	private ID3v1 tag;
	private boolean modified;
	private TrackListener listener;

	public Track(Path path) throws UnsupportedTagException, InvalidDataException, IOException {
		this.path = path;
		this.mp3File = new Mp3File(path);

		if (mp3File.hasId3v2Tag()) {
			tag = mp3File.getId3v2Tag();
		} else if (mp3File.hasId3v1Tag()) {
			tag = mp3File.getId3v1Tag();
		}
		if (tag != null) {
			album = tag.getAlbum();
			artist = tag.getArtist();
			title = tag.getTitle();
			if (title == null || title.isBlank()) {
				setTitle(path.getFileName().toString());
			}
			String trackNo = tag.getTrack();
			int trackNumberInt = 0;
			if (trackNo != null) {
				try {
					trackNumberInt = Integer.parseInt(trackNo);
				} catch (Exception e) {
					// ignore
				}
			}
			trackNumber = trackNumberInt;
		} else {
			tag = new ID3v1Tag();
			mp3File.setId3v1Tag(tag);
			album = null;
			artist = null;
			setTitle(path.toFile().getName());
			trackNumber = 0;
			setModified();
		}
	}

	public Path getPath() {
		return path;
	}

	public String getAlbum() {
		return album;
	}

	public String getArtist() {
		return artist;
	}

	public String getTitle() {
		return title;
	}

	public int getTrackNumber() {
		return trackNumber;
	}

	public void setAlbum(String album) {
		if (!Objects.equals(album, this.album)) {
			setModified();
		}
		this.album = album;
		tag.setAlbum(album);
	}

	public void setArtist(String artist) {
		if (!Objects.equals(artist, this.artist)) {
			setModified();
		}
		this.artist = artist;
		tag.setArtist(artist);
	}

	public void setTitle(String title) {
		if (!Objects.equals(title, this.title)) {
			setModified();
		}
		this.title = title;
		tag.setTitle(title);
	}

	public void setTrackNumber(int trackNumber) {
		if (trackNumber != this.trackNumber) {
			setModified();
		}
		this.trackNumber = trackNumber;
		tag.setTrack(Integer.toString(trackNumber));
	}

	public void writeTo(String path, boolean forceWrite)
			throws UnsupportedTagException, InvalidDataException, IOException, NotSupportedException {
		if (!isModified() && !forceWrite) {
			return;
		}
		if (tag instanceof ID3v2) {
			mp3File.setId3v2Tag((ID3v2) tag);
		} else {
			mp3File.setId3v1Tag(tag);
		}
		mp3File.save(path);
	}
	
	public void save() throws IOException, UnsupportedTagException, InvalidDataException, NotSupportedException {
		if (!isModified()) {
			return;
		}
		Path tmp = File.createTempFile(getPath().getFileName().toString(), ".tmp").toPath();
		writeTo(tmp.toString(),false);
		Files.move(tmp, getPath(), StandardCopyOption.REPLACE_EXISTING);
	}

	@Override
	public String toString() {
		return "Track [path=" + path + ", album=" + album + ", artist=" + artist + ", title=" + title + ", trackNumber="
				+ trackNumber + "]";
	}

	public Track setTrackListener(TrackListener listener) {
		this.listener = listener;
		if (modified) {
			listener.trackChanged(this);
		}
		return this;
	}
	
	public boolean isModified() {
		return modified;
	}

	private void setModified() {
		modified = true;
		if (listener != null) {
			listener.trackChanged(this);
		}
	}

}

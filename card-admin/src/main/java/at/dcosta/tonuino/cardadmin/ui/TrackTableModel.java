package at.dcosta.tonuino.cardadmin.ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;

import at.dcosta.tonuino.cardadmin.Track;
import at.dcosta.tonuino.cardadmin.TrackListener;
import at.dcosta.tonuino.cardadmin.util.FileNames;
import at.dcosta.tonuino.cardadmin.util.TrackSorter;

public class TrackTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;

	private static final int MIN_EDIT_VALUE = 2;
	private static final int MAX_EDIT_VALUE = 5;

	private static final ImageIcon ICON_PLAY = new ImageIcon(
			TrackTableModel.class.getClassLoader().getResource("play.png"));
	private static final ImageIcon ICON_UP = new ImageIcon(
			TrackTableModel.class.getClassLoader().getResource("up.png"));
	private static final ImageIcon ICON_DOWN = new ImageIcon(
			TrackTableModel.class.getClassLoader().getResource("down.png"));

	private static TableHeader EMPTY_HEADER = new TableHeader("", new ValueResolver<Void>() {
		private static final long serialVersionUID = 1L;

		@Override
		public Void getValue(Track track) {
			return null;
		}

		public void setValue(Void value, Track track) {
		};
	});

	private final List<TableHeader> header;
	private final ErrorDisplay errorDisplay;
	private List<Track> tracks;

	public TrackTableModel(ErrorDisplay errorDisplay) {
		this.header = new ArrayList<>();
		this.errorDisplay = errorDisplay;
		this.tracks = new ArrayList<Track>();
		createHeader();
	}

	public void update(File folder, TrackListener trackListener) throws IOException {
		this.tracks = Files.list(folder.toPath())
				.filter(file -> file.getFileName().toString().toLowerCase().endsWith(FileNames.SUFFIX_MP3))
				.map(file -> {
					try {
						return new Track(file).setTrackListener(trackListener);
					} catch (UnsupportedTagException | InvalidDataException | IOException e) {
						errorDisplay.showError("Can not read Tracks:", e);
						return null;
					}
				}).filter(track -> track != null).collect(Collectors.toList());
		TrackSorter.sortByFilename(this.tracks);
		fireTableDataChanged();
	}

	@SuppressWarnings("serial")
	private void createHeader() {
//		header.add(new TableHeader("", new ValueResolver<Void>() {
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			public Void getValue(Track track) {
//				return null;
//			}
//
//			public void setValue(Void value, Track track) {
//			};
//		}));
		header.add(EMPTY_HEADER);
		header.add(new TableHeader("File", new ValueResolver<Path>() {
			@Override
			public Path getValue(Track track) {
				return track.getPath().getFileName();
			}

			public void setValue(Path value, Track track) {
			};
		}));
		header.add(new TableHeader("Album", new ValueResolver<String>() {
			@Override
			public String getValue(Track track) {
				return track.getAlbum();
			}

			public void setValue(String value, Track track) {
				track.setAlbum(value);
			};
		}));
		header.add(new TableHeader("Artist", new ValueResolver<String>() {
			@Override
			public String getValue(Track track) {
				return track.getArtist();
			}

			public void setValue(String value, Track track) {
				track.setArtist(value);
			};
		}));
		header.add(new TableHeader("Titel", new ValueResolver<String>() {
			@Override
			public String getValue(Track track) {
				return track.getTitle();
			}

			public void setValue(String value, Track track) {
				track.setTitle(value);
			};
		}));
		header.add(new TableHeader("Nr.", new ValueResolver<Integer>() {
			@Override
			public Integer getValue(Track track) {
				return track.getTrackNumber();
			}

			public void setValue(Integer value, Track track) {
				track.setTrackNumber(value);
			};
		}));
		header.add(EMPTY_HEADER);
		header.add(EMPTY_HEADER);
//		header.add(new TableHeader("", new ValueResolver<Void>() {
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			public Void getValue(Track track) {
//				return null;
//			}
//
//			public void setValue(Void value, Track track) {
//			};
//		}));
//		header.add(new TableHeader("", new ValueResolver<Void>() {
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			public Void getValue(Track track) {
//				return null;
//			}
//
//			public void setValue(Void value, Track track) {
//			};
//		}));
	}

	@Override
	public String getColumnName(int column) {
		return header.get(column).getName();
	}

	@Override
	public int getRowCount() {
		return tracks.size();
	}

	@Override
	public int getColumnCount() {
		return header.size();
	}

	@Override
	public Class<?> getColumnClass(int column) {
		switch (column) {
		case 0:
		case 6:
		case 7:
			return ImageIcon.class;
		default:
			return String.class;
		}
	}

	public Track getTrackAtRow(int row) {
		return tracks.get(row);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return ICON_PLAY;
		}
		if (columnIndex == 6) {
			return ICON_UP;
		}
		if (columnIndex == 7) {
			return ICON_DOWN;
		}
		if (rowIndex < tracks.size() && columnIndex < header.size()) {
			ValueResolver<?> valueResolver = header.get(columnIndex).getValueResolver();
			return valueResolver.getValue(tracks.get(rowIndex));
		}

		return null;
	}

	public List<Track> getTracks() {
		return tracks;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex == 5) {
			ValueResolver<Integer> valueResolver = (ValueResolver<Integer>) header.get(columnIndex).getValueResolver();
			valueResolver.setValue(aValue == null ? null : Integer.valueOf((String) aValue), tracks.get(rowIndex));
		} else {
			ValueResolver<String> valueResolver = (ValueResolver<String>) header.get(columnIndex).getValueResolver();
			valueResolver.setValue((String) aValue, tracks.get(rowIndex));
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex >= MIN_EDIT_VALUE && columnIndex <= MAX_EDIT_VALUE;
	}

	public void move(int rowId, Direction direction) {
		if (direction == Direction.UP || direction == Direction.DOWN) {
			int newPos = rowId + (direction == Direction.UP ? -1 : 1);

			if (newPos < 0 || newPos >= tracks.size()) {
				return;
			}
			Track tmp = tracks.get(newPos);
			tracks.set(newPos, tracks.get(rowId));
			tracks.set(rowId, tmp);
			if (direction == Direction.UP) {
				fireTableRowsUpdated(newPos, rowId);
			} else {
				fireTableRowsUpdated(rowId, newPos);
			}
		} else {
			if (direction == Direction.FIRST) {
				Track tmp = tracks.get(rowId);
				for (int i = rowId; i > 0; i--) {
					tracks.set(i, tracks.get(i - 1));
				}
				tracks.set(0, tmp);
				fireTableRowsUpdated(0, rowId);
			} else {
				Track tmp = tracks.get(rowId);
				for (int i = rowId; i < tracks.size() - 1; i++) {
					tracks.set(i, tracks.get(i + 1));
				}
				tracks.set(tracks.size() - 1, tmp);
				fireTableRowsUpdated(rowId, tracks.size() - 1);
			}
		}
	}

}

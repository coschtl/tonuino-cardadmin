package at.dcosta.tonuino.cardadmin.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;

import at.dcosta.tonuino.cardadmin.Mp3Player;
import at.dcosta.tonuino.cardadmin.Track;
import at.dcosta.tonuino.cardadmin.TrackListener;
import at.dcosta.tonuino.cardadmin.ui.ModalDialog.Duration;
import at.dcosta.tonuino.cardadmin.util.CardFilesystemUtil;
import at.dcosta.tonuino.cardadmin.util.CardFilesystemUtil.RequiredAction;
import at.dcosta.tonuino.cardadmin.util.Configuration;
import at.dcosta.tonuino.cardadmin.util.ExceptionUtil;
import at.dcosta.tonuino.cardadmin.util.FileNames;
import at.dcosta.tonuino.cardadmin.util.StreamGobbler;
import at.dcosta.tonuino.cardadmin.util.TrackSorter;

public class FilesystemView implements DirectorySelectionListener, TrackListener, ActionListener, ErrorDisplay {

	private static final String CMD_NORMALIZE = "normalize";
	private static final String CMD_SAVE_ID_TAGS = "saveIdTags";
	private static final String CMD_PERSIST_TRACK_ORDER = "persistTrackOrder";
	private JTable trackTable;
	private JFrame frame;
	private JScrollPane fileScrollPane;
	private TrackTableModel trackTableModel;
	private Mp3Player mp3Player;
	private JButton normalize;
	private JButton writeTags;
	private JButton persistTrackOrder;
	private JLabel errorSummary;
	private JTextArea errorDetail;
	private JPanel error;
	private File addFilesBaseDir;
	private File currentPath;
	private JButton correct;

	public FilesystemView() {
		mp3Player = new Mp3Player();
		addFilesBaseDir = Configuration.getInstance().getDefaultContentRoot();
	}

	private JPanel createHeaderButtons(FolderTree folderTree) {
		JPanel headerPanel = new JPanel();
		headerPanel.setBorder(new EmptyBorder(10, 5, 5, 5));
		headerPanel.setLayout(new BorderLayout());
		JButton newFolder = new JButton("Neuer Ordner");
		newFolder.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				File parent = folderTree.getCurrentFolder();
				if (parent.getParentFile() != null && !Configuration.getInstance().isAlternativeCardRoot(parent)) {
					new ModalDialog().makeToast("Achtung",
							"An dieser Stelle kann kein neues Track-Verzeichnis erstellt werden!", frame,
							Duration.SHORT);
					return;
				}
				if (folderTree != null) {
					try {
						new File(parent, FileNames.getNextFolderName(parent)).mkdir();
						folderTree.folderAdded();
					} catch (IOException ex) {
						showError("Can not create new folder:", ex);
					}
				}
			}
		});
		JButton addFiles = new JButton("Dateien hinzufügen");
		addFiles.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				File target = folderTree.getCurrentFolder();
				if (target == null) {
					return;
				}
				final JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new FileFilter() {

					@Override
					public String getDescription() {
						return null;
					}

					@Override
					public boolean accept(File f) {
						return f.isDirectory() || f.getName().toLowerCase().endsWith(FileNames.SUFFIX_MP3);
					}
				});
				fc.setCurrentDirectory(addFilesBaseDir);
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.setMultiSelectionEnabled(true);
				int returnVal = fc.showOpenDialog(frame);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					ModalDialog wait = new ModalDialog();
					SwingWorker<Void, Void> worker = new SwingWorker<>() {
						@Override
						protected Void doInBackground() throws Exception {
							Iterator<String> targetFileNames = FileNames.getNextFileNames(target);
							for (File file : fc.getSelectedFiles()) {
								addFilesBaseDir = file.getParentFile();
								Path source = file.toPath();
								new Track(source).writeTo(new File(target, targetFileNames.next()).getAbsolutePath(),
										true);
							}
							return null;
						}

						protected void done() {
							wait.close();
							update(target);
						}

					};
					worker.execute();
					wait.showWait("Bitte warten", "Die Dateien werden importiert...", frame);
				}
			}
		});

		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.CENTER,0,0));
		correct = new JButton("Filesystem korrigieren");
		correct.setBorder(new EmptyBorder(new Insets(5,5,5,5)));
		correct.setEnabled(false);
		p.add(correct);
		correct.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentPath == null) {
					return;
				}

				ModalDialog wait = new ModalDialog();
				SwingWorker<Void, Void> worker = new SwingWorker<>() {
					@Override
					protected Void doInBackground() throws Exception {
						File f = currentPath;
						Path altRootPath = Path.of(Configuration.getInstance().getAlternativeCardRoot());
						while (f.getParentFile() != null) {
							if (f.toPath().equals(altRootPath)) {
								break;
							}
							f = f.getParentFile();
						}
						Map<Path, RequiredAction> changes = CardFilesystemUtil.analyzeRoot(f.toPath());
						changes.putAll(CardFilesystemUtil.correctFolders(f.toPath(), true));
						if (!changes.isEmpty()) {
							StringBuilder b = new StringBuilder();
							b.append("Folgende Änderungen an der SD-Karte sind nötig:\n\n");
							b.append(CardFilesystemUtil.toGermanText(changes));
							wait.close();
							new MultilineTextDialog().show("Analyse-Ergebnis", b.toString(), frame);
						}
						return null;
					}

					protected void done() {
						wait.close();
					}

				};
				worker.execute();
				wait.showWait("Bitte warten", "Das Filesystem wird korrigiert...", frame);
			}

		});

		headerPanel.add(newFolder, BorderLayout.LINE_START);
		headerPanel.add(p, BorderLayout.CENTER);
		headerPanel.add(addFiles, BorderLayout.LINE_END);
		return headerPanel;
	}

	private JPanel createFooterButtons() {
		JPanel footerPanel = new JPanel();
		footerPanel.setBorder(new EmptyBorder(10, 5, 0, 5));
		footerPanel.setLayout(new FlowLayout());

		normalize = new JButton("Tracks normalisieren");
		normalize.setActionCommand(CMD_NORMALIZE);
		normalize.setEnabled(Configuration.getInstance().hasNormalizer());
		normalize.setEnabled(false);
		normalize.addActionListener(this);

		persistTrackOrder = new JButton("Reihenfolge übernehmen");
		persistTrackOrder.setActionCommand(CMD_PERSIST_TRACK_ORDER);
		persistTrackOrder.addActionListener(this);
		persistTrackOrder.setEnabled(false);

		writeTags = new JButton("ID-Tags speichern");
		writeTags.setActionCommand(CMD_SAVE_ID_TAGS);
		writeTags.addActionListener(this);
		writeTags.setEnabled(false);

		footerPanel.add(normalize);
		footerPanel.add(persistTrackOrder);
		footerPanel.add(writeTags);
		return footerPanel;
	}

	public void show() throws IOException {
		trackTableModel = new TrackTableModel(this);

		frame = new JFrame("Tonuino Card Admin");
		frame.setLayout(new BorderLayout(5, 5));

		JPanel filesPanel = new JPanel();
		filesPanel.setLayout(new BoxLayout(filesPanel, BoxLayout.Y_AXIS));
		trackTable = new JTable(trackTableModel);
		trackTable.setGridColor(Color.LIGHT_GRAY);
		trackTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (mp3Player.isPlaying()) {
					mp3Player.stop();
				}
				Point point = e.getPoint();
				int col = trackTable.columnAtPoint(point);
				int row = trackTable.rowAtPoint(point);
				Track track = trackTableModel.getTrackAtRow(row);
				if (col == 0) {
					mp3Player.play(track);
				} else if (col == 6) {
					persistTrackOrder.setEnabled(true);
					trackTableModel.move(row, e.getButton() == MouseEvent.BUTTON1 ? Direction.UP : Direction.FIRST);
				} else if (col == 7) {
					persistTrackOrder.setEnabled(true);
					trackTableModel.move(row, e.getButton() == MouseEvent.BUTTON1 ? Direction.DOWN : Direction.LAST);
				}
			}
		});
		trackTable.setRowSelectionAllowed(false);
		fileScrollPane = new JScrollPane(trackTable);

		trackTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		filesPanel.add(fileScrollPane);
		filesPanel.add(createFooterButtons());

		final FolderTree folderTree = new FolderTree(300, (int) filesPanel.getPreferredSize().getHeight(), this);
		frame.add(folderTree, BorderLayout.LINE_START);

		frame.add(createHeaderButtons(folderTree), BorderLayout.NORTH);
		frame.add(filesPanel, BorderLayout.CENTER);
		createErrorDisplay();
		frame.add(error, BorderLayout.SOUTH);
		frame.getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);

		update(null);
	}

	private void createErrorDisplay() {
		error = new JPanel();
		error.setBorder(new LineBorder(Color.BLACK));
		error.setLayout(new BoxLayout(error, BoxLayout.Y_AXIS));
		error.setAlignmentX(Component.LEFT_ALIGNMENT);
		errorSummary = new JLabel();
		errorSummary.setForeground(Color.RED);
		error.add(errorSummary);
		error.add(new JLabel(""));
		errorDetail = new JTextArea();
		errorDetail.setFont(new Font("Tahoma", Font.PLAIN, 10));
		errorDetail.setEditable(false);
		errorDetail.setBackground(errorSummary.getBackground());
		errorDetail.setBorder(new EmptyBorder(new Insets(1, 1, 1, 1)));
		JScrollPane scrollPane = new JScrollPane(errorDetail);
		error.add(scrollPane);
		error.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() != MouseEvent.BUTTON1) {
					error.setVisible(false);
					frame.pack();
				}
			}
		});
		error.setVisible(false);
		error.setPreferredSize(new DimensionUIResource(frame.getWidth(), 100));
	}

	private void update(File path) {
		currentPath = path;
		correct.setEnabled(path != null);
		persistTrackOrder.setEnabled(false);
		writeTags.setEnabled(false);
		ModalDialog wait = new ModalDialog();
		frame.pack();
		SwingWorker<Void, Void> worker = new SwingWorker<>() {
			@Override
			protected Void doInBackground() throws Exception {
				if (path != null) {
					trackTableModel.update(path, FilesystemView.this);
				}
				int totalWidth = 0;
				final TableColumnModel columnModel = trackTable.getColumnModel();
				for (int column = 0; column < trackTable.getColumnCount(); column++) {
					int width = 30; // Min width
					for (int row = 0; row < trackTable.getRowCount(); row++) {
						TableCellRenderer renderer = trackTable.getCellRenderer(row, column);
						Component comp = trackTable.prepareRenderer(renderer, row, column);
						width = Math.max(comp.getPreferredSize().width + 5, width);
						if (width > 300) {
							break;
						}
					}
					// header
					TableColumn col = trackTable.getColumnModel().getColumn(column);
					TableCellRenderer renderer = col.getHeaderRenderer();
					if (renderer == null) {
						renderer = trackTable.getTableHeader().getDefaultRenderer();
					}
					width = Math.max(width, renderer
							.getTableCellRendererComponent(trackTable, col.getHeaderValue(), false, false, -1, column)
							.getPreferredSize().width);
					if (width > 300) {
						width = 300;
					}
					columnModel.getColumn(column).setPreferredWidth(width);
					totalWidth += width;
				}
				frame.pack();
				fileScrollPane.setPreferredSize(new DimensionUIResource(totalWidth, 400));
				error.setPreferredSize(new DimensionUIResource(totalWidth, 100));
				return null;
			}

			protected void done() {
				wait.close();
				normalize.setEnabled(!trackTableModel.getTracks().isEmpty());
			}

		};
		worker.execute();
		wait.showWait("Bitte warten", "Das Verzeichnis wird gelesen...", frame);
		frame.pack();
	}

	@Override
	public void pathSelected(File path) {
		update(path);
	}

	@Override
	public void trackChanged(Track track) {
		writeTags.setEnabled(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final List<Track> tracks = trackTableModel.getTracks();
		if (tracks.isEmpty()) {
			return;
		}
		if (e.getActionCommand() == CMD_SAVE_ID_TAGS) {
			writeTags.setEnabled(false);
			ModalDialog wait = new ModalDialog();
			SwingWorker<Void, Void> worker = new SwingWorker<>() {
				@Override
				protected Void doInBackground() throws Exception {
					tracks.forEach(t -> {
						try {
							t.save();
						} catch (UnsupportedTagException | InvalidDataException | NotSupportedException
								| IOException ex) {
							showError("Can not save ID-Tags", ex);
						}
					});
					return null;
				}

				protected void done() {
					wait.close();
				}

			};
			worker.execute();
			wait.showWait("Bitte warten", "Die ID-Tags werden gespeichert...", frame);
		} else if (e.getActionCommand() == CMD_PERSIST_TRACK_ORDER) {
			persistTrackOrder.setEnabled(false);
			ModalDialog wait = new ModalDialog();
			SwingWorker<Void, Void> worker = new SwingWorker<>() {
				@Override
				protected Void doInBackground() throws Exception {
					TrackSorter.correctFilenames(tracks, FilesystemView.this);
					return null;
				}

				protected void done() {
					wait.close();
				}
			};
			worker.execute();
			wait.showWait("Bitte warten", "Die Dateien werden umsortiert...", frame);
			update(tracks.get(0).getPath().getParent().toFile());
		} else if (e.getActionCommand() == CMD_NORMALIZE) {
			List<String> command = Configuration.getInstance().getNormalizerCommand();
			for (Track track : trackTableModel.getTracks()) {
				command.add(track.getPath().toString());
			}

			ModalDialog dialog = new ModalDialog();
			SwingWorker<Void, Void> worker = new SwingWorker<>() {
				@Override
				protected Void doInBackground() throws Exception {
					ProcessBuilder builder = new ProcessBuilder();
					builder.command(command);
					try {
						Process process = builder.start();
						StreamGobbler in = new StreamGobbler(process.getInputStream());
						StreamGobbler err = new StreamGobbler(process.getErrorStream()).setUpdateableDialog(dialog);
						SwingWorker<Void, Void> worker2 = new SwingWorker<>() {
							@Override
							protected Void doInBackground() throws Exception {
								in.run();
								return null;
							}
						};
						SwingWorker<Void, Void> worker3 = new SwingWorker<>() {
							@Override
							protected Void doInBackground() throws Exception {
								err.run();
								return null;
							}
						};
						worker2.execute();
						worker3.execute();
						try {
							process.waitFor();
						} catch (InterruptedException e) {
							// ignore
						}
					} catch (Exception ex) {
						showError("Error normalizing tracks", ex);
					}
					return null;
				}

				protected void done() {
					dialog.close();
					normalize.setEnabled(false);
				}
			};
			worker.execute();
			dialog.showWait("Die Dateien werden normalisiert", "Bitte warten...", frame);
		}
	}

	@Override
	public void showError(String summary, Throwable t) {
		showError(summary, ExceptionUtil.getStacktrace(t));
	}

	@Override
	public void showError(String summary, String detail) {
		errorSummary.setText(summary);
		errorDetail.setText(detail);
		error.setVisible(true);
		frame.pack();
	}

}

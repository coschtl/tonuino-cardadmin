package at.dcosta.tonuino.cardadmin.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import at.dcosta.tonuino.cardadmin.Mp3Player;
import at.dcosta.tonuino.cardadmin.Track;
import at.dcosta.tonuino.cardadmin.TrackListener;
import at.dcosta.tonuino.cardadmin.util.FileNames;

public class FilesystemView implements DirectorySelectionListener, TrackListener {

	private JTable trackTable;
	private JFrame frame;
	private JScrollPane fileScrollPane;
	private TrackTableModel trackTableModel;
	private Mp3Player mp3Player;
	private JButton writeTags;
	
	public FilesystemView() {
		mp3Player = new Mp3Player();
	}

	public static void main(String[] args) throws IOException {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			JFrame.setDefaultLookAndFeelDecorated(true);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		new FilesystemView().show();
	}

	private JPanel createHeaderButtons(FolderTree folderTree) {
		JPanel headerPanel = new JPanel();
		headerPanel.setBorder(new EmptyBorder(5,5,5,5));
		headerPanel.setLayout(new BorderLayout());
		JButton newFolder = new JButton("neuer Ordner");
		newFolder.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				File parent = folderTree.getCurrentFolder();
				if (parent.getParent() != null) {
					return;
				}
				if (folderTree != null) {
					try {
						new File(parent, FileNames.getNextFolderName(parent)).mkdir();
						folderTree.folderAdded();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
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
						return f.isDirectory() || f.getName().toLowerCase().endsWith(".mp3");
					}
				});
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.setMultiSelectionEnabled(true);
				int returnVal = fc.showOpenDialog(frame);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					try {
						Iterator<String> targetFileNames = FileNames.getNextFileNames(target);
						for (File file : fc.getSelectedFiles()) {
							Path source = file.toPath();
							new Track(source)
									.writeTo(new File(target, targetFileNames.next() + ".mp3").getAbsolutePath());
//
//							File tmp = new File(source.toString()+".tmp");
//							new Track(source).writeTo(tmp.getAbsolutePath());
//							Files.move(tmp.toPath(), new File(target, targetFileNames.next() + ".mp3").toPath(), StandardCopyOption.COPY_ATTRIBUTES);
						}
						update(target);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});

		headerPanel.add(newFolder, BorderLayout.LINE_START);
		headerPanel.add(addFiles, BorderLayout.LINE_END);
		return headerPanel;
	}

	private JPanel createFooterButtons() {
		JPanel footerPanel = new JPanel();
		footerPanel.setLayout(new FlowLayout());
		JButton normalize = new JButton("Tracks normalisieren");
		writeTags = new JButton("ID-Tags speichern");
		writeTags.setEnabled(false);
		
		footerPanel.add(normalize);
		footerPanel.add(writeTags);
		return footerPanel;
	}

	private void show() throws IOException {
		trackTableModel = new TrackTableModel();

		frame = new JFrame("Tonuino Card Admin");
		frame.setLayout(new BorderLayout(5,5));

		JPanel filesPanel = new JPanel();
		filesPanel.setLayout(new BoxLayout(filesPanel, BoxLayout.Y_AXIS));
		trackTable = new JTable(trackTableModel);
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
					trackTableModel.move(row, Direction.UP);
				} else if (col == 7) {
					trackTableModel.move(row, Direction.DOWN);
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
		frame.getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);

		update(null);

	}

	private void update(File path) throws IOException {
		WaitDialog wait = new WaitDialog();
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
					if (renderer == null)					{
						renderer = trackTable.getTableHeader().getDefaultRenderer();
					}
					width = Math.max(width,
							renderer.getTableCellRendererComponent(trackTable, col.getHeaderValue(), false, false, -1, column).getPreferredSize().width);
					if (width > 300) {
						width = 300;
					}
					columnModel.getColumn(column).setPreferredWidth(width);
					totalWidth += width;
				}
				frame.pack();
				fileScrollPane.setPreferredSize(new DimensionUIResource(totalWidth + 20, 400));
				return null;
			}

			protected void done() {
				wait.close();
			}

		};
		worker.execute();
		wait.makeWait("Bitte warten", "Das Verzeichnis wird gelesen...", frame);
		frame.pack();
	}

	@Override
	public void pathSelected(File path) {
		try {
			writeTags.setEnabled(false);	
			update(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void trackChanged(Track track) {
		writeTags.setEnabled(true);		
	}

}

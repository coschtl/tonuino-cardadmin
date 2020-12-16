package at.dcosta.tonuino.cardadmin.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import at.dcosta.tonuino.cardadmin.UpdateableDialog;

public class ModalDialog implements UpdateableDialog {

	public enum Duration {
		SHORT(3000), LONG(6000);

		private final long millis;

		private Duration(long millis) {
			this.millis = millis;
		}

		public long getMillis() {
			return millis;
		}
	}

	private JDialog dialog;
	private JLabel msgLabel;
	private boolean disposed;

	public void showWait(String title, String message, Component parent) {
		show(title, message, parent, true);
	}

	public void showDialog(String title, String message, Component parent) {
		show(title, message, parent, false);
	}

	public void makeToast(String title, String message, Component parent, Duration duration) {
		SwingWorker<Void, Void> worker = new SwingWorker<>() {

			@Override
			protected Void doInBackground() throws Exception {
				Thread.sleep(duration.getMillis());
				return null;
			}

			protected void done() {
				close();
			};
		};
		worker.execute();
		show(title, message, parent, false);
	}

	private void show(String title, String message, Component parent, boolean withProgressBar) {
		Window win = SwingUtilities.getWindowAncestor(parent);
		dialog = new JDialog(win, title, Dialog.ModalityType.APPLICATION_MODAL);

		JPanel panel = new JPanel(new BorderLayout());
		if (withProgressBar) {
			JProgressBar progressBar = new JProgressBar();
			progressBar.setIndeterminate(true);
			panel.add(progressBar, BorderLayout.CENTER);
		}
		msgLabel = new JLabel(message);
		msgLabel.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
		msgLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
		panel.add(msgLabel, BorderLayout.PAGE_START);
		dialog.add(panel);
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
	}

	public void close() {
		if (!disposed) {
			disposed = true;
			dialog.dispose();
		}
	}

	@Override
	public void updateText(String newText) {
		double oldWidth = msgLabel.getPreferredSize().getWidth();
		msgLabel.setText(newText);
		double diff = msgLabel.getPreferredSize().getWidth() - oldWidth;
		if (newText.trim().length() > 0 && (diff > 10 || diff < -50)) {
			dialog.pack();
		}
	}
}

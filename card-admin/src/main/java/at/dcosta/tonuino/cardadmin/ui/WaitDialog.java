package at.dcosta.tonuino.cardadmin.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

public class WaitDialog {
	private JDialog dialog;

	public void makeWait(String title, String message, Component parent) {

		Window win = SwingUtilities.getWindowAncestor(parent);
		dialog = new JDialog(win, title, Dialog.ModalityType.APPLICATION_MODAL);

		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(progressBar, BorderLayout.CENTER);
		panel.add(new JLabel(message), BorderLayout.PAGE_START);
		dialog.add(panel);
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
	}

	public void close() {
		dialog.dispose();
	}
}

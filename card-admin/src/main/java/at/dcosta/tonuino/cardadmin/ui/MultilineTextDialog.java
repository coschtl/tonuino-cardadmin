package at.dcosta.tonuino.cardadmin.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class MultilineTextDialog {

	public void show(String title, String message, Component parent) {
		Window win = SwingUtilities.getWindowAncestor(parent);
		 JDialog dialog = new JDialog(win, title, Dialog.ModalityType.APPLICATION_MODAL);

		JPanel panel = new JPanel(new BorderLayout());
		JTextArea textArea = new JTextArea();
		textArea.setText(message);
		textArea.setFont(new Font("Tahoma", Font.PLAIN, 12));
		textArea.setEditable(false);
		textArea.setBorder(new EmptyBorder(new Insets(1, 1, 1, 1)));
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
		panel.add(scrollPane, BorderLayout.PAGE_START);
		JButton button = new JButton("OK");
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
				
			}
		});
		panel.add(button, BorderLayout.LINE_START);
		dialog.add(panel);
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
	}

}

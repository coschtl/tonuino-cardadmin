package at.dcosta.tonuino.cardadmin.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import at.dcosta.tonuino.cardadmin.UpdateableDialog;

public class StreamGobbler implements Runnable {
	private InputStream inputStream;
	private StringBuilder content;
	private UpdateableDialog updateableDialog;

	public StreamGobbler(InputStream inputStream) {
		this.inputStream = inputStream;
		this.content = new StringBuilder();
	}

	public StreamGobbler setUpdateableDialog(UpdateableDialog updateableDialog) {
		this.updateableDialog = updateableDialog;
		return this;
	}

	@Override
	public void run() {
		new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(l -> {
			if (updateableDialog != null) {
				updateableDialog.updateText(l);
			}
			content.append(l);
		});
	}

	public String getContent() {
		return content.toString();
	}
}

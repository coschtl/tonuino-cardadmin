package at.dcosta.tonuino.cardadmin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import javax.swing.SwingWorker;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackListener;

public class Mp3Player {
	private static class PlayerThread extends SwingWorker<Void, Void> {

		private AdvancedPlayer player;

		public PlayerThread(Track track) {
			try {
				player = new AdvancedPlayer(Files.newInputStream(track.getPath(), StandardOpenOption.READ));
				player.setPlayBackListener(new PlaybackListener() {
				});
			} catch (JavaLayerException | IOException e) {
				System.out
						.println("Can not play " + track.getTitle() + " (" + track.getPath() + "): " + e.getMessage());
			}
		}

		@Override
		protected Void doInBackground() throws Exception {
			player.play();
			return null;
		}

		@Override
		protected void done() {
			super.done();
		}

		public void stop() {
			player.stop();
		}

	}

	private PlayerThread playerThread;

	public void play(Track track) {
		playerThread = new PlayerThread(track);
		playerThread.execute();

	}

	public boolean isPlaying() {
		return playerThread != null && !playerThread.isDone();
	}

	public void stop() {
		if (isPlaying()) {
			playerThread.stop();
		}
	}

}

import javax.sound.sampled.*;
import java.io.File;
import java.util.Properties;
import java.io.FileInputStream;

public class GameSound {
	private static Clip attackSfx = null;
	private static Properties props;

	public static void main(String[] args) {
		playAttackSFX();
		new java.util.Scanner(System.in).nextLine();
	}

	static {
		props = new Properties();

		try (FileInputStream in = new FileInputStream("config.properties")) {
			props.load(in);
		} catch (Exception error) {
			error.printStackTrace();
		}
	}

	public static void close() {
		attackSfx.close();
	}

	public static void playAttackSFX() {
		try {
			if (attackSfx == null) {
				File soundFile = new File(props.getProperty("game.attack-sfx-path"));
				AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
				
				// Explicitly define what we want: a Clip class with this audio format
				DataLine.Info info = new DataLine.Info(Clip.class, audioIn.getFormat());
				
				// Check if the system actually supports this line before trying to get it
				if (!AudioSystem.isLineSupported(info)) {
					System.out.println("Warning: Audio line not supported in this WSL session.");
					return;
				}

				attackSfx = (Clip) AudioSystem.getLine(info);
				attackSfx.open(audioIn);
			}
			
			if (attackSfx.isRunning()) { 
				attackSfx.stop();
			}
			attackSfx.setFramePosition(0); // Faster than microsecond position
			attackSfx.start();
			
		} catch (Exception error) {
			// This will prevent the game from crashing if WSL audio lags
			System.err.println("Audio Error: " + error.getMessage());
		}
	}
}

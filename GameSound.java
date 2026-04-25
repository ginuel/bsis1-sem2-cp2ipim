import javax.sound.sampled.*;
import java.io.*;
import java.util.Properties;

public class GameSound {
	// This creates a place to store the sound used when a fish is hit.
	private static Clip attackSfx = null;
	private static Properties props;


	public static void main(String[] args) {
		playAttackSFX();
		new java.util.Scanner(System.in).nextLine();
	}

	// This part runs automatically to find the settings file on the computer.
	static {
		props = new Properties();

		try (FileInputStream in = new FileInputStream("config.properties")) {
			props.load(in);
		} catch (Exception error) {
			error.printStackTrace();
		}
	}

	// This turns off the sound system to free up the computer's memory.
	public static void close() {
		attackSfx.close();
	}

	public static void playAttackSFX() {
		try {
			// This checks if the sound has been loaded yet, and if not, it prepares it.
			if (attackSfx == null) {
				// This looks at the settings to find the exact location of the sound file on the hard drive.
				File soundFile = new File(props.getProperty("game.attack-sfx-path"));
				AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
				
				// Explicitly define what we want: a Clip class with this audio format
				DataLine.Info info = new DataLine.Info(Clip.class, audioIn.getFormat());
				
				// This checks if the computer is actually able to play this type of sound right now.
				if (!AudioSystem.isLineSupported(info)) {
					System.out.println("Warning: Audio line not supported in this WSL session.");
					return;
				}

				attackSfx = (Clip) AudioSystem.getLine(info);
				attackSfx.open(audioIn);
			}
			
			// This stops the sound if it is already playing so it can start over from the beginning.
			if (attackSfx.isRunning()) { 
				attackSfx.stop();
			}
			// This rewinds the sound to the very start.
			attackSfx.setFramePosition(0); // Faster than microsecond position
			attackSfx.start();
			
		// This keeps the game from closing if the sound fails to play correctly.
		} catch (Exception error) {
			System.err.println("Audio Error: " + error.getMessage());
		}
	}
}

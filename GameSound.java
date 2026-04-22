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

	public static void playAttackSFX() {
		try {
			if (attackSfx == null) {
				File soundFile = new File(props.getProperty("game.attack-sfx-path"));
				AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
				attackSfx = AudioSystem.getClip();
				attackSfx.open(audioIn);
			}
			attackSfx.stop();
			attackSfx.setMicrosecondPosition(0);
			attackSfx.start();
		} catch (Exception error) {
			error.printStackTrace();
		}
	}
}

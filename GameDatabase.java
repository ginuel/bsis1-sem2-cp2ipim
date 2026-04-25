import java.sql.*;
import java.util.Properties;
import java.io.FileInputStream;
import java.nio.file.*;

public class GameDatabase {
	private static Connection connection = null;
	private static Properties props;
	private static String feedback = "";


	public static void main(String[] args) {
		waitForMariaDB();
		System.out.println("Connected to Database...");
	}

	// This part runs automatically when the program starts to set up important settings.
	static {
		// This creates a new list to hold the game settings.
		props = new Properties();

		// This tries to open the specific file where the settings are stored on the computer.
		try (FileInputStream in = new FileInputStream("config.properties")) {
			// This reads the information from the file and puts it into the list.
			props.load(in);
		// This prepares a message to explain what went wrong if the settings file cannot be opened.
		} catch (Exception error) {
			feedback = "Error loading config.properties: " + error.getMessage();
		}
	}

	// This safely shuts down the link to the database when the game is finished.
	public static void close() {
		try {
			connection.close();
		} catch (Exception error) {
			error.printStackTrace();
		}
	}

	// This keeps trying to connect to the database every second until it finally works.
	public static void waitForMariaDB() {
		// retry and wait 1 sec until connection is OK
		while (connection == null) {
			try {
				getConnection();
				Thread.sleep(1000); 
			} catch (Exception error) {}
		}
	}

	public static Connection getConnection() { 
		try {
			// This checks if the game is already connected so it doesn't waste time making a new link unnecessarily.
			if (connection == null || connection.isClosed()) {
				// This puts together the "address" used to find the database on the computer or network.
				String url = String.format("jdbc:mariadb://%s:%s/%s",
					props.getProperty("db.host"),
					props.getProperty("db.port"),
					props.getProperty("db.name"));

				// This uses the address, username, and password to officially log into the database.
				connection = DriverManager.getConnection(url, 
					props.getProperty("db.user"), 
					props.getProperty("db.password"));
			}
		} catch (Exception error) {}
		return connection;
	}

	// This function makes a backup copy of the entire game database and saves it as a file.
	public static boolean save() {
		try {
			String backupFilePath = props.getProperty("db.backup-path");
			// This creates a special command that tells the computer's database system to export all its information.
			String dbCommand = String.format("%s -h%s -P%s -u%s -p%s %s",
					props.getProperty("db.mysql-dump-path"),
					props.getProperty("db.host"),
					props.getProperty("db.port"),
					props.getProperty("db.user"),
					props.getProperty("db.password"),
					props.getProperty("db.name"));

			// This makes sure the folder for the backup exists on the computer; if it doesn't, it creates it.
			Files.createDirectories(Paths.get(backupFilePath).getParent());

			// Build command list from shell property + the dbCommand
			java.util.List<String> command = new java.util.ArrayList<>(java.util.Arrays.asList(props.getProperty("db.shell").split(" ")));
			command.add(dbCommand);

			ProcessBuilder pb = new ProcessBuilder(command);
			// This tells the computer to take the information coming out of the database and write it into the backup file.
			pb.redirectOutput(new java.io.File(backupFilePath));
			pb.redirectError(ProcessBuilder.Redirect.INHERIT);

			Process process = pb.start();
			// This waits for the saving process to finish and checks if there were any errors.
			boolean isSuccessful = (process.waitFor() == 0);
			if (isSuccessful) {
				feedback = "Success: Database saved to " + backupFilePath;
			} else {
				feedback = "Error: mysqldump exited with an error.";
			}

			return isSuccessful;
		} catch (Exception error) {
			error.printStackTrace();
		}

		return false;
	}

	// This function takes a previously saved file and puts that information back into the game.
	public static boolean load() {
		try {

			String backupFilePath = props.getProperty("db.backup-path");
			java.io.File backupFile = new java.io.File(backupFilePath);
			
			Files.createDirectories(Paths.get(backupFilePath).getParent());

			// This checks if the backup file is actually there before trying to use it.
			if (!backupFile.exists()) {
				feedback = "Error: Backup file not found at " + backupFilePath;
				return false;
			}

			String dbCommand = String.format("%s -h%s -P%s -u%s -p%s %s",
					props.getProperty("db.mysql-path"),
					props.getProperty("db.host"),
					props.getProperty("db.port"),
					props.getProperty("db.user"),
					props.getProperty("db.password"),
					props.getProperty("db.name"));

			// Build command list from shell property + the dbCommand
			java.util.List<String> command = new java.util.ArrayList<>(java.util.Arrays.asList(props.getProperty("db.shell").split(" ")));
			command.add(dbCommand);

			ProcessBuilder pb = new ProcessBuilder(command);
			// This feeds the information from the backup file back into the database system.
			pb.redirectInput(backupFile);
			pb.redirectError(ProcessBuilder.Redirect.INHERIT);

			Process process = pb.start();
			boolean isSuccessful = (process.waitFor() == 0);
			if (isSuccessful) {
				feedback = "Success: Database loaded from " + backupFilePath;
			} else {
				feedback = "Error: mysql import exited with an error.";
			}

			return isSuccessful;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	// This gives the game a text message to show the player whether the save or load worked.
	public static String getFeedback() {
		return feedback;
	}

}




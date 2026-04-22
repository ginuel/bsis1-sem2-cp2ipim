import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.io.FileInputStream;

public class GameDatabase {
	private static Connection connection = null;
	private static Properties props;
	private static String feedback = "";


	public static void main(String[] args) {
		waitForMariaDB();
		System.out.println("Connected to Database...");
	}

	static {
		props = new Properties();

		try (FileInputStream in = new FileInputStream("config.properties")) {
			props.load(in);
		} catch (Exception error) {
			feedback = "Error loading config.properties: " + error.getMessage();
		}
	}

	public static void close() {
		try {
			connection.close();
		} catch (Exception error) {
			error.printStackTrace();
		}
	}

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
			// make a connection only if it doesn't exist
			if (connection == null || connection.isClosed()) {
				String url = String.format("jdbc:mariadb://%s:%s/%s",
					props.getProperty("db.host"),
					props.getProperty("db.port"),
					props.getProperty("db.name"));

				connection = DriverManager.getConnection(url, 
					props.getProperty("db.user"), 
					props.getProperty("db.password"));
			}
		} catch (Exception error) {}
		return connection;
	}

	public static boolean save() {
		try {
			String backupFilePath = props.getProperty("db.backup-path");
			String dbCommand = String.format("%s -h%s -P%s -u%s -p%s %s",
					props.getProperty("db.mysql-dump-path"),
					props.getProperty("db.host"),
					props.getProperty("db.port"),
					props.getProperty("db.user"),
					props.getProperty("db.password"),
					props.getProperty("db.name"));

			// Build command list from shell property + the dbCommand
			java.util.List<String> command = new java.util.ArrayList<>(java.util.Arrays.asList(props.getProperty("db.shell").split(" ")));
			command.add(dbCommand);

			ProcessBuilder pb = new ProcessBuilder(command);
			pb.redirectOutput(new java.io.File(backupFilePath));
			pb.redirectError(ProcessBuilder.Redirect.INHERIT);

			Process process = pb.start();
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

	public static boolean load() {
		try {
			String backupFilePath = props.getProperty("db.backup-path");
			java.io.File backupFile = new java.io.File(backupFilePath);

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

	public static String getFeedback() {
		return feedback;
	}

}




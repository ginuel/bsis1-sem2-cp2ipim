import org.mindrot.jbcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;


public class GameAuth {

	private static String feedback;

	public static int login(String username, String password) {
		/*
		 * Return UserID of user if login is successful otherwise -1
		 * */

		// This starts the process by assuming the login will fail until proven otherwise.
		int userID = -1;
		feedback = "Invalid username or password! Try again.";
		if (!username.chars().allMatch(Character::isLetterOrDigit)) {
			// Username must be composed of only letters or digits 
		} else if (username.length() < 1) {
			// Username must be atleast 1 character so the next else-if block "username.charAt(0)" won't index an empty String
		} else if (!Character.isLetter(username.charAt(0))) {
			// Username must start with a letter
		} else if (username.length() < 3) {
			// Username must be atleast 3 characters
		} else if (!findUsername(username)) {
			// Username must not exist to create new one
		} else if (!password.chars().noneMatch(Character::isWhitespace)) {
			// Password must contain only printable characters
		} else if (password.length() < 8) {
			// Password must be atleast 8 characters long
		} else {
			// This tells the computer to look for the user's secret information in the saved records.
			String sql = "SELECT UserID, PasswordHash FROM UserCredentials WHERE UserName = ?";
			Connection conn = GameDatabase.getConnection();
			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
				pstmt.setString(1, username);
				ResultSet rs = pstmt.executeQuery();

				if (rs.next()) {
					String storedPasswordHash = rs.getString("PasswordHash");
					// This compares the typed password with the secret code saved in the records to see if they match.
					if (BCrypt.checkpw(password, storedPasswordHash)) {
						// This saves the user's specific identification number because the password was correct.
						userID = rs.getInt("UserID");
						feedback = String.format("Login successful as %s", username);
					}
				}
			} catch (Exception error) {}

		}

		return userID;
	}

	public static int register(String username, String password, String passwordRepeated) {

		// This starts the count at a negative number to show no user is found yet.
		int userID = -1;

		if (!username.chars().allMatch(Character::isLetterOrDigit)) {
			feedback = "Username must only contain letters and numbers";
		} else if (username.length() < 1) {
			feedback = "Username must be atleast 1 character long";
		} else if (!Character.isLetter(username.charAt(0))) {
			feedback = "Username must start with a character";
		} else if (username.length() < 3) {
			feedback = "Username must be atleast 3 characters";
		// This checks if someone else is already using the name the user picked.
		} else if (findUsername(username)) {
			feedback = "Username already exists";
		} else if (!password.chars().noneMatch(Character::isWhitespace)) {
			feedback = "Password must not contain whitespace";
		// This makes sure the two passwords typed by the user are exactly the same.
		} else if (!password.equals(passwordRepeated)) {
			feedback = "Passwords don't match";
		} else if (password.length() < 8) {
			feedback = "Password must be atleast 8 characters long";
		} else {
			Connection conn = GameDatabase.getConnection();
			try (PreparedStatement pstmtUser = conn.prepareStatement("INSERT INTO Users (Gold) VALUES (0)", Statement.RETURN_GENERATED_KEYS);
					PreparedStatement pstmtCred = conn.prepareStatement("INSERT INTO UserCredentials (UserID, UserName, PasswordHash) VALUES (?, ?, ?)");
					PreparedStatement pstmtPond = conn.prepareStatement("INSERT INTO UserPonds (UserID, PondID) VALUES (?, 1)")) {
				// This tells the computer to wait and only save the data if every step finishes without a mistake.
				conn.setAutoCommit(false);

				// Make a User to be referenced by UserCredentials table
				pstmtUser.executeUpdate();
				ResultSet rs = pstmtUser.getGeneratedKeys();
				if (rs.next()) {
					userID = rs.getInt(1);

					// Make a credentials for new user
					pstmtCred.setInt(1, userID);
					pstmtCred.setString(2, username);
					// This turns the secret password into a scrambled code so it is safe and hidden.
					String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
					pstmtCred.setString(3, passwordHash);
					pstmtCred.executeUpdate();

					// Set first pond as unlocked for the user 
					pstmtPond.setInt(1, userID);
					// This gives the new user their first combat area for free to start the game.
					pstmtPond.executeUpdate();

					// Commit the transaction
					conn.commit();
					feedback = "Registration Successful";
				}
			} catch (Exception error) {
				try {
					// Undo operations when error
					conn.rollback(); 
				} catch (Exception error2) {}
				feedback = "Registration failed";
				userID = -1;
			} finally {
				try {
					conn.setAutoCommit(true);
				} catch (Exception error2) {}
			}
		}
		
		return userID;
	}
	
	private static boolean findUsername(String username) {
		// This creates a search command to look through the list of all existing players.
		String sql = "SELECT * FROM UserCredentials WHERE UserName = ?";
		Connection conn = GameDatabase.getConnection();
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			// This puts the specific name the user typed into the search command.
			pstmt.setString(1, username);
			ResultSet rs = pstmt.executeQuery();

			// This checks if the search found a matching name in the records.
			boolean usernameExists = rs.next();
			// This sends back a "yes" or "no" answer to the part of the game that asked if the name was taken.
			return usernameExists;
		} catch (Exception error) {} // This ignores any mistakes that happen during the search and keeps the game running.

		// This sends back a "no" answer if the search failed or was interrupted.
		return false;
	}

	public static String getFeedback() {
		return feedback;
	}
}


import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameMap {
	private static int mapWidth = 7;
	private static int mapHeight = 7;
	private static int homeRow = 3;
	private static int homeCol = 3;
	private static int playerRow = homeRow; 
	private static int playerCol = homeCol;
	private static int goldCount = 0;
	private static int pondCount = 3;
	private static String feedback = "";
	private static List<Location> locations = new ArrayList<>();

	public static int getGoldCount() {
		return goldCount;
	}

	public static void handleMovement(KeyStroke key) {
		switch (key.getKeyType()) {
			case ArrowUp:
				move(0, -1);
				break;
			case ArrowDown:
				move(0, 1);
				break;
			case ArrowLeft:
				move(-1, 0);
				break;
			case ArrowRight:
				move(1, 0);
				break;
		}
	}
	//
	public static boolean isPlayerIn(int col, int row) {
		return playerCol == col && playerRow == row;
	}

	public static TerminalSize getTerminalSize() {
		return new TerminalSize(mapWidth, mapHeight);
	}

	public static int getMapWidth() {
		return mapWidth;
	}

	public static int getMapHeight() {
		return mapHeight;
	}

	public static Location getLocationAtPlayer() {
		return getLocation(playerCol, playerRow);
	}

	public static void syncFromDatabase(int userID) {
		Connection conn = GameDatabase.getConnection();

		String sql = """
			SELECT
				Gold, LastMapRow, LastMapCol
			FROM
				Users
			WHERE
				UserID = ?
			""";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userID);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				goldCount = rs.getInt("Gold");
				playerRow = rs.getInt("LastMapRow");
				if (playerRow == -1) {
					playerRow = homeRow;
				}
				playerCol = rs.getInt("LastMapCol");
				if (playerCol == -1) {
					playerCol = homeCol;
				}
			}
		} catch (Exception error) {
			error.printStackTrace();
		}

		locations.clear();
		// UI Navigation points mapped to Game.State
		locations.add(new Location(1, 5, "B", TextColor.ANSI.GREEN, true, "Bestiary", null, 0, -1, Game.State.BESTIARY));
		locations.add(new Location(5, 5, "L", TextColor.ANSI.MAGENTA, true, "Leaderboards", null, 0, -1, Game.State.LEADERBOARDS));
		locations.add(new Location(homeCol, homeRow, "H", TextColor.ANSI.WHITE, false, "Home", null, 0, -1, Game.State.MENU));

		sql = """
			SELECT 
				p.*, 
				PondType,
				UserPondID
			FROM Ponds p 
			INNER JOIN PondTypes pt ON p.PondTypeID = pt.PondTypeID
			LEFT JOIN UserPonds up ON p.PondID = up.PondID AND up.UserID = ?
			ORDER BY p.PondID;
			""";


		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userID);
			ResultSet rs = pstmt.executeQuery();

			pondCount = 0;

			while (rs.next()) {
				int col = 1 + (pondCount * 2);
				int row = 1;
				String symbol = rs.getString("PondSymbol");
				TextColor color = TextColor.ANSI.BLUE;
				int cost = rs.getInt("PondCost");
				boolean owned = (cost == 0) || (rs.getObject("UserPondID") != null);
				String pondName = rs.getString("PondName");
				String pondType = rs.getString("PondType");
				int pondID = rs.getInt("PondID");
				Game.State state = Game.State.COMBAT;

				locations.add(new Location(col, row, symbol, color, owned, pondName, pondType, cost, pondID, state));

				pondCount++;
			}

			mapWidth = Math.max(7, 2 * pondCount + 1);
			mapHeight = 7;
		} catch (Exception error) { 
			error.printStackTrace(); 
		}
	}

	public static List<Location> getLocations() {
		return locations;
	}

	public static void saveUserPositionAndGold(int userID) {
		String sql = """
			UPDATE Users 
			SET LastMapRow = ?, LastMapCol = ?, Gold = ?
			WHERE UserID = ?
			""";
		Connection conn = GameDatabase.getConnection();
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, playerRow);
			pstmt.setInt(2, playerCol);
			pstmt.setInt(3, goldCount);
			pstmt.setInt(4, userID);
			pstmt.executeUpdate();
		} catch (Exception error) { 
			error.printStackTrace();
		}
	}

	public static void unlockPond(Location location, int userID) {
		Connection conn = GameDatabase.getConnection();
		String sql = "INSERT INTO UserPonds (UserID, PondID) VALUES (?, ?)";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userID); 
			pstmt.setInt(2, location.pondID);
			pstmt.executeUpdate();

			goldCount -= location.goldCost;
			location.owned = true;

			saveUserPositionAndGold(userID);

			feedback = "You've unlocked a pond!";
		} catch (Exception error) {
			feedback = "Error unlocking pond...";
			error.printStackTrace(); 
		}
	}

	public static Location getLocation(int c, int r) {
		for (Location l : locations) {
			if (l.row == r && l.col == c) {
				return l;
			}
		}
		return null;
	}
	//
	private static void move(int col, int row) {
		if (playerRow + row >= 0 
				&& playerRow + row < mapHeight 
				&& playerCol + col >= 0 
				&& playerCol + col < mapWidth) {
			playerRow += row; 
			playerCol += col;
		}
	}

	public static class Location {
		int col, row, goldCost, pondID;
		String symbol, description, pondType;
		TextColor color;
		boolean owned;
		Game.State state; 
		Label tile;

		
		public Location(int c, int r, String s, TextColor clr, boolean o, String d, String t, int g, int id, Game.State st) {
			col = c;
			row = r;
			symbol = s;
			color = clr;
			owned = o; 
			description = d;
			pondType = t;
			goldCost = g;
			pondID = id;
			state = st;
		}
	}

	public static String getFeedback() {
		return feedback;
	}
}

import java.sql.*;
import com.googlecode.lanterna.gui2.table.*;

public class GameBestiary {
	public static void updateTable(Table<String> table, int userID) {
		// This creates a new empty chart with labels for the area, the fish name, how rare it is, and how many were caught.
		TableModel<String> model = new TableModel<>("POND", "FISH", "RARITY", "KILLS");

		// This is a set of instructions that tells the computer to gather specific fish information from several different lists in the database.
		String sql = """
			SELECT 
					PondType, 
					CONCAT(AsciiHead, ' ', FishName, ' ', AsciiTail) AS Fish,
					Rarity, 
					KillCount
			FROM DiscoveredFishes df
			JOIN Fishes f ON df.FishID = f.FishID
			JOIN PondTypes pt ON f.PondTypeID = pt.PondTypeID
			JOIN FishArts fa ON f.FishArtID = fa.FishArtID
			JOIN FishRarity fr ON f.RarityID = fr.RarityID
			WHERE df.UserID = ?
			ORDER BY PondType, FishName;
			""";

		Connection conn = GameDatabase.getConnection();
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			// This tells the search to only look for fish found by the person currently playing.
			pstmt.setInt(1, userID);
			ResultSet rs = pstmt.executeQuery();

			// This goes through every fish found in the search results one by one.
			while (rs.next()) {
				String pondType = rs.getString("PondType");
				String fish = rs.getString("Fish");
				String rarity = rs.getString("Rarity");
				String killCount = rs.getString("KillCount");

				// This takes the information for one fish and puts it into a new line on the chart.
				model.addRow(pondType, fish, rarity, killCount);
			}
		// This part handles any mistakes that happen while reading the data so the game does not stop.
		} catch (Exception error) {
			error.printStackTrace();
		}

		// This attaches the finished list to the screen so the player can finally see it.
		table.setTableModel(model);
	}
}


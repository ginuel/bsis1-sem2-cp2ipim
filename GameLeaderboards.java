import java.sql.*;
import java.util.List;
import com.googlecode.lanterna.gui2.table.*;

public class GameLeaderboards {
	
	// This is a list of the different ways the game can rank players, such as speed or money.
	private static final List<String> SCORE_CATEGORIES = List.of("WPM", "CHARS", "GOLD", "KILLS", "WAVES", "TIME");


	public static void updateTable(Table<String> table, String category) {
		// This creates a new empty chart to display the top players and their scores.
		TableModel<String> model = new TableModel<>("RANK", "PLAYER", category);
		
		// This checks if the requested ranking type is on the approved list.
		if (!SCORE_CATEGORIES.contains(category)) {
			category = "WPM";
		}

		// This long instruction tells the computer to find the best scores for every player and rank them from highest to lowest.
    String sql = String.format("""
			SELECT 
				RANK() OVER (ORDER BY %s DESC) AS Rank,
				UserName, 
				%s AS Score
			FROM (
				SELECT 
					UserName,
					-- This calculates how many words a person types every minute based on their total characters and time.
					MAX(ROUND((s.CharsTyped / 5.0) / (NULLIF(s.SurvivalTime, 0) / 60.0), 2)) AS WPM,
					MAX(s.CharsTyped) as CHARS,
					MAX(s.GoldEarned) as GOLD,
					MAX(s.KillCount) as KILLS,
					MAX(s.Waves) as WAVES,
					MAX(s.SurvivalTime) as TIME
				FROM ScorePerGame s
				JOIN UserCredentials uc ON s.UserID = uc.UserID
				GROUP BY UserName	
			) AS BaseData
			ORDER BY %s DESC
			-- This tells the computer to only show the top one hundred players.
			LIMIT 100;
			""", category, category, category);


		Connection conn = GameDatabase.getConnection();
    try (Statement stmt = conn.createStatement()) {
			ResultSet rs = stmt.executeQuery(sql);
			// This takes each player found in the search and adds them to the list one at a time.
			while (rs.next()) {
				String rank = rs.getString("Rank");
				String name = rs.getString("UserName");
				String score = rs.getString("Score");
        model.addRow(rank, name, score);
			}
		} catch (Exception error) {
			error.printStackTrace();
		}

		table.setTableModel(model);
	}

	// This provides the list of ranking types to other parts of the game that need them.
	public static List<String> getCategories() {
		return SCORE_CATEGORIES;
	}
}

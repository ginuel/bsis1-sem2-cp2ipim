import java.sql.*;
import java.util.List;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;

public class GameLeaderboards {
	
	private static final List<String> SCORE_CATEGORIES = List.of("WPM", "CHARS", "GOLD", "KILLS", "WAVES", "TIME");
	


	// public static void main(String[] args) {
	// 	updateTable("WPM");
	// }

	public static void updateTable(Table<String> table, String category) {
		TableModel<String> model = new TableModel<>("RANK", "PLAYER", category);
		
		if (!SCORE_CATEGORIES.contains(category)) {
			category = "WPM";
		}

    String sql = String.format("""
			SELECT 
				RANK() OVER (ORDER BY %s DESC) AS Rank,
				UserName, 
				%s AS Score
			FROM (
				SELECT 
					UserName,
					ROUND((s.CharsTyped / 5.0) / (NULLIF(s.SurvivalTime, 0) / 60.0), 2) AS WPM,
					s.CharsTyped as CHARS,
					s.GoldEarned as GOLD,
					s.KillCount as KILLS,
					s.Waves as WAVES,
					s.SurvivalTime as TIME
				FROM ScorePerGame s
				JOIN UserCredentials uc ON s.UserID = uc.UserID
			) AS BaseData
			ORDER BY %s DESC
			LIMIT 100;
			""", category, category, category);


		Connection conn = GameDatabase.getConnection();
    try (Statement stmt = conn.createStatement()) {
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String rank = rs.getString("Rank");
				String name = rs.getString("UserName");
				String score = rs.getString("Score");
				// System.out.printf("%s, %s, %s\n", rank, name, score);
        model.addRow(rank, name, score);
			}
		} catch (Exception error) {
			error.printStackTrace();
		}

		table.setTableModel(model);
	}

	public static List<String> getCategories() {
		return SCORE_CATEGORIES;
	}
}

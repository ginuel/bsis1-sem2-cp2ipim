import java.sql.*;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;

public class GameBestiary {
	public static void updateTable(Table<String> table, int userID) {
		TableModel<String> model = new TableModel<>("POND", "FISH", "RARITY", "KILLS");

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
			pstmt.setInt(1, userID);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				String pondType = rs.getString("PondType");
				String fish = rs.getString("Fish");
				String rarity = rs.getString("Rarity");
				String killCount = rs.getString("KillCount");

				model.addRow(pondType, fish, rarity, killCount);
			}
		} catch (Exception error) {
			error.printStackTrace();
		}

		table.setTableModel(model);
	}
}


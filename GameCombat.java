import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.*;
import com.googlecode.lanterna.screen.*;
import java.sql.*;
import java.util.*;

public class GameCombat {

	private static int LANE_COUNT = 5;
	private static int LANE_WIDTH = 40;
	private static int STICKMAN_COL = 2;
	private static int STICKMAN_WIDTH = 5;
	private static int RIGHT_MARGIN = 2;
	private static int MIN_FISH_GAP = 3;
	private static int FULLSCREEN_WIDTH = STICKMAN_COL + STICKMAN_WIDTH + LANE_WIDTH + RIGHT_MARGIN;

	private static float STARTING_FISH_SPEED = 2.0f; // secs per char
	private static float FISH_SPEED_INCREMENT = 0.25f; // secs per char
	private static int STARTING_WAVE_FISH_COUNT = 25;
	private static int WAVE_FISH_INCREMENT = 25;

	private static int screenWidth;
	private static int screenHeight;

	private static List<FishType> fishSpecies; 
	private static List<String> dictionary;
	private static List<ActiveFish> swimmingFishes;
	private static float totalSpawnRateWeight;

	private static long now;
	private static long startTime;
	private static long lastTick;
	private static int gold, kills, totalChars, wave, remainingInWave;
	private static double secPerChar;
	private static int numFishMovesBeforeNextSpawn;
	private static int currentSpawnLane;

	private static boolean isPaused;
	private static long pausedTime,  pauseStart; 

	private static long combatTime, combatStart;

	private static long sec;
	private static double wpm;

	private static ActiveFish lastFish = null;

	// to store fish data retrieved from database
	public static class FishType {
		int id, coins;
		String name, rarity, pondType, head, tail;
		TextColor color;
		float spawnRate;

		FishType(int i, int c, String n, String r, String pt, String h, String t, TextColor clr, float sr) {
			id = i;
			coins = c;
			name = n;
			rarity = r;
			pondType = pt;
			head = h;
			tail = t;
			color = clr;
			spawnRate = sr;
		}
	}

	public static class ActiveFish {
		FishType type;
		String word;
		int lane, x, lettersTyped;
		
		ActiveFish(FishType t, String w, int l, int x) { 
			this.type = t; 
			this.word = w.toUpperCase();
			this.lane = l; 
			this.x = x; 
		}

		int getFullWidth() { 
			return type.head.length() + word.length() + type.tail.length(); 
		}
	}

	public static int getNumRemainingInWave() {
		return remainingInWave;
	}

	public static boolean isGamePaused() {
		return isPaused;
	}

	public static ActiveFish getLastFishKilled() { 
		return lastFish;
	}

	public static int getCharsTyped() {
		return totalChars;
	}

	public static void togglePause(boolean mustPause) {
		if (isPaused && !mustPause) {
			isPaused = false;
			pausedTime += System.currentTimeMillis() - pauseStart;
		} else if (!isPaused && mustPause) {
			isPaused = true;
			pauseStart = System.currentTimeMillis();
		}
	}

	public static String getScores() {
		return String.format("""
			GOLD: %d
			KILLS: %d
			TIME: %d
			WAVE: %d
			WPM: %.2f
			""", gold, kills, sec, wave, wpm);
	}

	public static List<ActiveFish> getSwimmingFishes() {
		return swimmingFishes;
	}

	public static int getStickmanCol() {
		return STICKMAN_COL;
	}

	public static int getStickmanWidth() {
		return STICKMAN_WIDTH;
	}
	
	public static int getLaneCount() {
		return LANE_COUNT;
	}

	public static int getLaneWidth() {
		return getTerminalSize().getColumns() - STICKMAN_WIDTH - STICKMAN_COL - RIGHT_MARGIN;
	}

	public static void setFullscreenWidth(Screen screen) {
		FULLSCREEN_WIDTH = screen.getTerminalSize().getColumns();
	}

	public static TerminalSize getTerminalSize() {
		screenWidth = Math.min(STICKMAN_COL + STICKMAN_WIDTH + LANE_WIDTH + RIGHT_MARGIN, FULLSCREEN_WIDTH);

		screenHeight = LANE_COUNT * 2 + 1;
		return new TerminalSize(screenWidth, screenHeight);
	}

	public static boolean isTooDark(TextColor color) {
		// Standard weighted formula
		double luminance = (0.299 * color.getRed()) + (0.587 * color.getGreen()) + (0.114 * color.getBlue());
							 
		return luminance < 50; // Returns true if the color is "dark"
	}

	public static void syncFromDatabase(int pondID) {
		dictionary = new ArrayList<>();
		fishSpecies = new ArrayList<>();
		// 1. DATA LOADING: Join Fishes, Arts, Rarity, and PondTypes
		String sql = """
			SELECT 
				f.FishID,
				CoinsReward,
				FishName,
				Rarity,
				PondType,
				AsciiHead,
				AsciiTail,
				FishColorHex, 
				SpawnRate 
			FROM Fishes f 
				JOIN FishArts a ON f.FishArtID = a.FishArtID 
				JOIN FishRarity r ON f.RarityID = r.RarityID
				JOIN PondTypes pt ON f.PondTypeID = pt.PondTypeID
			WHERE f.PondTypeID = (SELECT PondTypeID FROM Ponds WHERE PondID = ?)
			""";
		Connection conn = GameDatabase.getConnection();
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, pondID);
			ResultSet fishSet = pstmt.executeQuery();
			while (fishSet.next()) {
				FishType fishType = new FishType(
					fishSet.getInt("FishID"),
					fishSet.getInt("CoinsReward"),
					fishSet.getString("FishName"),
					fishSet.getString("Rarity"), 
					fishSet.getString("PondType"), 
					fishSet.getString("AsciiHead"),
					fishSet.getString("AsciiTail"), 
					TextColor.Factory.fromString(fishSet.getString("FishColorHex")),
					fishSet.getFloat("SpawnRate")
				);
				fishSpecies.add(fishType);
				totalSpawnRateWeight += fishType.spawnRate;
			}
		} catch (Exception error) {
			error.printStackTrace();
		}

		try (Statement stmt = conn.createStatement()) {
			sql = "SELECT WordText FROM Words";
			ResultSet wordSet = stmt.executeQuery(sql);
			while (wordSet.next()) {
				dictionary.add(wordSet.getString("WordText"));
			}
		} catch (Exception error) {
			error.printStackTrace();
		}
	}

	public static void resetCombat() {
		swimmingFishes = new ArrayList<>();

		startTime = System.currentTimeMillis();
		lastTick = startTime;
		gold = 0;
		kills = 0;
		totalChars = 0;
		wave = 1;
		remainingInWave = 25;
		secPerChar = STARTING_FISH_SPEED;


		sec = 0;
		wpm = 0;

		numFishMovesBeforeNextSpawn = 0;
		currentSpawnLane = new Random().nextInt(LANE_COUNT);

		lastFish = null;

		pausedTime = 0;
		isPaused = false;

		combatTime = 0;
		combatStart = System.currentTimeMillis();
	}

	public static void updateWave() {
		if (remainingInWave <= 0 && swimmingFishes.isEmpty()) {
			// if the wave is done, move to next wave
			wave++;
			remainingInWave = STARTING_WAVE_FISH_COUNT + WAVE_FISH_INCREMENT * wave;
			secPerChar = Math.max(0.1, STARTING_FISH_SPEED - ((wave - 1) * FISH_SPEED_INCREMENT));
		}
	}

	public static boolean handleCharacterInput(int userID, char typed) {
		boolean hasKilledFish = false;

		Iterator<ActiveFish> it = swimmingFishes.iterator();
		while (it.hasNext()) {
			ActiveFish f = it.next();
			if (f.word.charAt(f.lettersTyped) == Character.toUpperCase(typed)) {
				// if you typed the next letter
				f.lettersTyped += 1;

				if (f.lettersTyped == f.word.length()) {
					// if you typed the last letter of the word
					
					// Update Stats
					lastFish = new ActiveFish(f.type, f.word, f.lane, f.x);
					
					gold += f.type.coins; 
					kills += 1;
					totalChars += f.word.length();

					// Record specific fish kill in DiscoveredFishes
					updateDiscoveredFishes(userID, f.type.id);

					remainingInWave -= 1;
					
					// remove completed fish from list
					it.remove();

					if (swimmingFishes.size() == 0) {
						combatTime += System.currentTimeMillis() - combatStart;
					}
					
					hasKilledFish = true;
				}
			}	else if (f.word.charAt(0) == Character.toUpperCase(typed)) {
				f.lettersTyped = 1;
			} else {
				// if you did not type the next letter
				f.lettersTyped = 0;
			}
		}

		return hasKilledFish;
	}

	public static boolean moveFishesAndCheckStickmanCollision(int userID, int pondID) {
		for (ActiveFish f : swimmingFishes) {
			// check stickman collision
			if (f.x < STICKMAN_COL + STICKMAN_WIDTH) { 
				// if stickman gets touch, end game
				saveResults(userID, pondID, gold, kills, totalChars, startTime, wave);
				return true; 
			}
			
			// move this fish
			f.x -= 1;	
		}
		
		// stickman was not touched
		return false;
	}

	public static FishType getRandomFishType() {
		FishType selected = null;

		float dice = new Random().nextFloat() * totalSpawnRateWeight;

		float cumulative = 0;
		for (FishType ft : fishSpecies) {
			cumulative += ft.spawnRate;
			if (dice <= cumulative) {
				selected = ft;
				break; 
			}
		}

		return selected;
	}

	public static void spawnFishIfPossible() {
		if (numFishMovesBeforeNextSpawn > 0) {
			// if must wait a number of moves before spaqn
			numFishMovesBeforeNextSpawn -= 1;
			return;
		}

		// if you have waited a number of moves, you can now spawn a fish
		if (remainingInWave > swimmingFishes.size() && !fishSpecies.isEmpty()) {
			// if the wave still has a fish to spawn
			FishType selected = getRandomFishType();

			if (selected == null) {
				return;
			}

			String word = dictionary.get(new Random().nextInt(dictionary.size()));
			int spawnX = screenWidth - RIGHT_MARGIN - (selected.head.length() + word.length() + selected.tail.length());

			// check if a fish can spawn on this lane
			boolean clear = true;
			for (ActiveFish f : swimmingFishes) {
				int spawnFishLeftX = spawnX;
				int thisFishRightX = f.x + f.getFullWidth();
				if (f.lane == currentSpawnLane && spawnFishLeftX - thisFishRightX < MIN_FISH_GAP) {
					// if a fish is blocking this lane
					clear = false;
				}
			}
			if (clear) {
				// if a fish can spawn
				if (swimmingFishes.size() == 0) {
					combatStart = System.currentTimeMillis();
				}

				swimmingFishes.add(new ActiveFish(selected, word, currentSpawnLane, spawnX));
				
				// wait again a number of moves, after spawning a fish
				numFishMovesBeforeNextSpawn = new Random().nextInt(Math.max(4, 10 - wave) + 1);
				currentSpawnLane = new Random().nextInt(LANE_COUNT);

			} 
		}
	}

	public static boolean updateCombat(int userID, int pondID) {
		boolean isDead = false;

		updateWave();
		now = System.currentTimeMillis();
		if (now - lastTick > (secPerChar * 1000)) { 
			// if the fishes must now move based on speed
			isDead = moveFishesAndCheckStickmanCollision(userID, pondID);
			spawnFishIfPossible();

			lastTick = now;
		}
		sec = Math.max(1, (now - startTime - pausedTime) / 1000);
;
		wpm = (totalChars / 5.0) / (Math.max(1, combatTime / 1000) / 60.0);

		return isDead;
	} 

	public static void start(Screen screen, int userID, int pondID) {
		setFullscreenWidth(screen);
		syncFromDatabase(pondID);
		resetCombat();

		boolean isDead = false;
		try {
			while (!isDead) {
				isDead = updateCombat(userID, pondID);
			}
		} catch (Exception error) {
			error.printStackTrace();
		}

	}

	// Persists per-fish kill counts
	private static void updateDiscoveredFishes(int userID, int fishID) {
		String sql = """
			INSERT INTO 
				DiscoveredFishes (UserID, FishID, KillCount) 
			VALUES
				(?, ?, 1)
			ON DUPLICATE KEY 
				UPDATE KillCount = KillCount + 1
			""";
		Connection conn = GameDatabase.getConnection();
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userID);
			pstmt.setInt(2, fishID);
			pstmt.executeUpdate();
		} catch (Exception error) { 
			error.printStackTrace();
		}
	}

	private static void saveResults(int uid, int pid, int g, int k, int c, long start, int w) {
		String sql = """
			INSERT INTO 
				ScorePerGame (UserID, PondID, CharsTyped, SurvivalTime, KillCount, GoldEarned, Waves) 
			VALUES
				(?, ?, ?, ?, ?, ?, ?)
			""";
		Connection conn = GameDatabase.getConnection();
		try (PreparedStatement pstmt = conn.prepareStatement(sql);
				Statement stmt = conn.createStatement()) {
			conn.setAutoCommit(false);

			pstmt.setInt(1, uid); 
			pstmt.setInt(2, pid); 
			pstmt.setInt(3, c);
			pstmt.setInt(4, (int)((System.currentTimeMillis()-start)/1000));
			pstmt.setInt(5, k);
			pstmt.setInt(6, g);
			pstmt.setInt(7, w);
			pstmt.executeUpdate();

			String sql2 = "UPDATE Users SET Gold = Gold + " + g + " WHERE UserID = " + uid;
			stmt.executeUpdate(sql2);
			
			conn.commit();
		} catch (Exception error) { 
			try {
				conn.rollback();
			} catch (Exception error2) {}
			error.printStackTrace(); 
		} finally {
			try {
				conn.setAutoCommit(true);
			} catch (Exception error2) {}
		}
	}
}


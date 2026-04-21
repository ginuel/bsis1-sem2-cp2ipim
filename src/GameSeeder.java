import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.sql.*;
import java.util.Properties;
import java.util.Random;

public class GameSeeder {
    private static final Random random = new Random();
    private static final Properties config = new Properties();
    private static Process dbProcess = null; // Track the process to kill it later

    public static void main(String[] args) {
        System.out.println("=== Starting Game Database Seeder ===");
        
        loadConfig();

        // 3. Seed Data
        seedAdmin();
        seedAdminDiscoveries(); 
        seedUsersAndScores();   
        
        System.out.println("=== Seeding Complete ===");
        
        // Optional: Shutdown DB if we were the ones who started it
        if (dbProcess != null) {
            System.out.println("Closing spawned database process...");
            dbProcess.destroy();
        }
    }

    private static void loadConfig() {
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            config.load(fis);
        } catch (Exception e) {
            System.err.println("Fatal: Could not load config.properties");
            System.exit(1);
        }
    }



    private static void seedAdmin() {
        String username = config.getProperty("game.default-username");
        String password = config.getProperty("game.default-password", "").trim();

        if (username != null && username.matches("^[A-Za-z][A-Za-z0-9]{2,}$") && password.length() >= 8) {
            try {
                GameAuth.register(username, password, password);
                System.out.println("Admin account verified/registered.");
            } catch (Exception e) {
                System.out.println("Admin account already exists or registration failed.");
            }
        }
    }

    private static void seedAdminDiscoveries() {
        String sql = "INSERT IGNORE INTO DiscoveredFishes (UserID, FishID, KillCount) " +
                     "SELECT 1, FishID, 1 FROM Fishes";
        
        try (Connection conn = GameDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int count = ps.executeUpdate();
            System.out.println("Seeded " + count + " fish discoveries for Admin.");
        } catch (Exception e) {
            System.out.println("Error seeding discoveries: " + e.getMessage());
        }
    }

    private static void seedUsersAndScores() {
        try (BufferedReader userBr = new BufferedReader(new FileReader("usernames.txt"));
             BufferedReader passBr = new BufferedReader(new FileReader("passwords.txt"));
             Connection conn = GameDatabase.getConnection()) {

            String uLine, pLine;
            int registeredCount = 0;
            while ((uLine = userBr.readLine()) != null && (pLine = passBr.readLine()) != null) {
                String username = uLine.trim();
                String password = pLine.trim();

                if (!username.matches("^[A-Za-z][A-Za-z0-9]{2,}$") || password.length() < 8) continue;

                try {
                    GameAuth.register(username, password, password);
                    registeredCount++;
                    int sessions = random.nextInt(3) + 1;
                    for (int i = 0; i < sessions; i++) {
                        insertScore(conn, username);
                    }
                } catch (Exception e) { /* Skip duplicates */ }
            }
            System.out.println("Successfully seeded " + registeredCount + " test users.");
        } catch (Exception e) {
            System.out.println("Error reading seed files: " + e.getMessage());
        }
    }

    private static void insertScore(Connection conn, String username) {
        String sql = "INSERT INTO ScorePerGame (UserID, PondID, CharsTyped, SurvivalTime, KillCount, GoldEarned, Waves) " +
                     "SELECT UserID, ?, ?, ?, ?, ?, ? FROM UserCredentials WHERE UserName = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int kills = random.nextInt(40) + 10;
            ps.setInt(1, random.nextInt(3) + 1); 
            ps.setInt(2, kills * 5);             
            ps.setInt(3, random.nextInt(800));   
            ps.setInt(4, kills);                 
            ps.setInt(5, (kills * 15));          
            ps.setInt(6, (kills / 8) + 1);       
            ps.setString(7, username);
            ps.executeUpdate();
        } catch (Exception e) { /* Handle error */ }
    }
}


import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;

public class GameMenu {
    private static Screen screen;

    public static void main(String[] args) {
        try {
            Terminal terminal = new DefaultTerminalFactory().createTerminal();
            screen = new TerminalScreen(terminal);
            screen.startScreen();
            screen.setCursorPosition(null);

            runMenu();

            screen.stopScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void runMenu() throws IOException {
        boolean isRunning = true;
        String status = "Ready";

        while (isRunning) {
            screen.clear();
            
            // Draw UI Frame
            drawBox(2, 1, 30, 10);
            
            // Draw Content
            drawText(4, 2, "   FISHDA CRACKER   ");
            drawText(4, 3, "────────────────────");
            drawText(4, 5, " 1. Play Game");
            drawText(4, 6, " 2. Bestiary");
            drawText(4, 7, " 3. Leaderboards");
            drawText(4, 8, " 4. Database Backup");
            drawText(4, 9, " 5. Exit");
            
            // Status area
            drawText(2, 12, "[ Status: " + status + " ]");
            drawText(2, 13, "Press a number to select...");

            screen.refresh();

            KeyStroke key = screen.readInput();
            if (key.getKeyType() == KeyType.Character) {
                char input = key.getCharacter();
                switch (input) {
                    case '1' -> status = "Loading Map...";
                    case '2' -> status = "Opening Bestiary...";
                    case '3' -> status = "Fetching Scores...";
                    case '4' -> status = "Backup Menu...";
                    case '5' -> isRunning = false;
                    default -> status = "Unknown Command: " + input;
                }
            } else if (key.getKeyType() == KeyType.Escape) {
                isRunning = false;
            }
        }
    }

    // Helper to draw clean boxes using standard unicode characters
    private static void drawBox(int x, int y, int width, int height) {
        // Corners
        screen.setCharacter(x, y, new TextCharacter('┌'));
        screen.setCharacter(x + width, y, new TextCharacter('┐'));
        screen.setCharacter(x, y + height, new TextCharacter('└'));
        screen.setCharacter(x + width, y + height, new TextCharacter('┘'));

        // Horizontal lines
        for (int i = 1; i < width; i++) {
            screen.setCharacter(x + i, y, new TextCharacter('─'));
            screen.setCharacter(x + i, y + height, new TextCharacter('─'));
        }

        // Vertical lines
        for (int i = 1; i < height; i++) {
            screen.setCharacter(x, y + i, new TextCharacter('│'));
            screen.setCharacter(x + width, y + i, new TextCharacter('│'));
        }
    }

    private static void drawText(int x, int y, String text) {
        for (int i = 0; i < text.length(); i++) {
            screen.setCharacter(x + i, y, new TextCharacter(text.charAt(i)));
        }
    }
}


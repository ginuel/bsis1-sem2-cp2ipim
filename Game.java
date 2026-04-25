// Lanterna - Terminal and Screen Management
import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.*;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

// Lanterna - GUI Components
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.*;
import com.googlecode.lanterna.gui2.table.Table;

// Security and Database
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

// Java Utilities
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Game {
	// This is a list of all the different screens or parts the game can show.
	public enum State {BANNER, AUTH, LOGIN, REGISTER, MENU, MAP, BESTIARY, LEADERBOARDS, BACKUP, COMBAT, END};

	// This handles the buttons and windows the user interacts with
	private static MultiWindowTextGUI gui;
	private static Screen screen;
	// This saves the identification number of the person currently playing.
	private static int userID = -1;

	// This keeps track of which screen the user is looking at right now
	private static State state;
	// This remembers the previous screen with either Menu or Map so the game can go back to it later.
	private static State lastMenuState;
	
	// This stores the ID for the specific Pond the player is visiting.
	private static int pondID = -1;

	public static void main(String[] args) {
		// This part makes sure the game saves and closes correctly when you quit.
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			GameDatabase.save();
			GameDatabase.close();
			GameSound.close();
		}));

		try {
			// This starts the display so the user can see the game.
			Terminal terminal = new DefaultTerminalFactory().createTerminal();
			screen = new TerminalScreen(terminal);
			screen.startScreen();
			gui = new MultiWindowTextGUI(screen);

			// This tells the game to show the starting image first.
			state = State.BANNER;

			// This keeps the game running until the user chooses to stop.
			while (state != State.END) {
				try {
					// This looks at which part of the game should be on the screen right now.
					switch (state) {
						case BANNER:
							showBanner();
							state = State.AUTH;
							break;
						case AUTH:
							showAuthMenu();
							break;
						case LOGIN:
							showLogin();
							break;
						case REGISTER:
							showRegister();
							break;
						case MENU:
							lastMenuState = state;
							showMainMenu();
							break;
						case MAP:
							lastMenuState = state;
							showMap();
							break;
						case BESTIARY:
							showBestiary();
							state = lastMenuState;
							break;
						case LEADERBOARDS:
							showLeaderboards();
							state = lastMenuState;
							break;
						case BACKUP:
							showBackupMenu();
							state = State.MENU;
							break;
						case COMBAT:
							showCombat();
							state = State.MAP;
							break;
					}
				} catch (Exception error3) {
					error3.printStackTrace();
				}
			} // while loop
		} catch (Exception error) {
			error.printStackTrace();
		} finally {
			// This completely turns off the program.
			System.exit(0);
		}
	}

	private static void showBanner() {
		// This creates a new rectangular box on the screen to hold information.
		BasicWindow window = new BasicWindow("FishdaTyper");
		Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
		// This makes the box appear in the middle of the screen.
		window.setHints(Arrays.asList(Window.Hint.CENTERED));
		window.setComponent(panel);

		ProgressBar progressBar = new ProgressBar(0, 100, 10);
		progressBar.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Fill));
			
		// This adds the large title art and welcome text to the box.
		panel.addComponent(new Label("""
			          <°)))><        <°)))><        <°)))><
			
			______ _     _         _     _____                     
			|  ___(_)   | |       | |   |_   _|                    
			| |_   _ ___| |__   __| | __ _| |_   _ _ __   ___ _ __ 
			|  _| | / __| '_ \\ / _` |/ _` | | | | | '_ \\ / _ \\ '__|
			| |   | \\__ \\ | | | (_| | (_| | | |_| | |_) |  __/ |   
			\\_|   |_|___/_| |_|\\__,_|\\__,_\\_/\\__, | .__/ \\___|_|   
			                                  __/ | |              
			                                 |___/|_|              
			                WELCOME TO FISHDATYPER                
			
			         <°)))><        <°)))><        <°)))><
			"""));


		Label statusLabel = new Label("Starting FishdaTyper...");	


		panel.addComponent(statusLabel);
		panel.addComponent(progressBar);

		

		// This tells the computer to perform other tasks in the background while the game stays open.
		new Thread(() -> {

			try {
				// This makes the progress bar fill up slowly from start to finish.
				for (int i = 0; i < 100; i++) {
					progressBar.setValue(i);
					gui.updateScreen();
					Thread.sleep(1);
				}
				Thread.sleep(1000);
			} catch (Exception error) {
				error.printStackTrace();
			} finally {
				statusLabel.setText("Waiting for MariaDB...");
				// This makes the game pause until the saved data is ready to be used.
				GameDatabase.waitForMariaDB();
				statusLabel.setText("Loading assets: attack sound effect...");
				GameSound.playAttackSFX();
				// This removes the welcome box from the screen so the game can start.
				window.close();
			}
			
		}).start();

		gui.addWindowAndWait(window);
	}

	private static void showAuthMenu() {
		// This creates a new box where the user can choose to log in or sign up.
		BasicWindow window = new BasicWindow("Authentication");
		Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
		window.setHints(Arrays.asList(Window.Hint.CENTERED));
		window.setComponent(panel);
		// This tells the computer to watch for when the user presses keys on the keyboard.
		window.addWindowListener(new WindowListenerAdapter() {
			@Override
			public void onInput(Window basePane, KeyStroke keyStroke, AtomicBoolean deliverEvent) {
				KeyType keyType = keyStroke.getKeyType();

				// This checks if the user pressed the Escape key to quit.
				if (keyType == KeyType.Escape) {
					// This shows a small box asking the user if they are sure they want to stop playing.
					MessageDialogButton result = MessageDialog.showMessageDialog(gui, "Confirm Exit", "Are you sure you want to exit game?", MessageDialogButton.No, MessageDialogButton.Yes);
					if (result == MessageDialogButton.Yes) {
						state = State.END;
						window.close();
					}
				}
			}
		});

		// This creates a clickable button that takes the user to the login screen.
		Button loginButton = new Button("Login", () -> {
			state = State.LOGIN;
			window.close();
		}); 

		panel.addComponent(loginButton);
		panel.addComponent(new Button("Register", () -> {
			state = State.REGISTER;
			window.close();
		}));
		panel.addComponent(new Button("Exit", () -> {
			state = State.END;
			window.close();
		}));

		// This automatically highlights the login button so the user can select it immediately.
		loginButton.takeFocus();
		
		// This puts the box on the screen and waits for the user to pick an option.
		gui.addWindowAndWait(window);
	}

	private static void showLogin() {
		// This creates a new window where the user can enter their name and secret code.
		BasicWindow window = new BasicWindow("Login");
		Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
		window.setHints(Arrays.asList(Window.Hint.CENTERED));
		window.setComponent(panel);
		window.addWindowListener(new WindowListenerAdapter() {
			@Override
			public void onInput(Window basePane, KeyStroke keyStroke, AtomicBoolean deliverEvent) {
				KeyType keyType = keyStroke.getKeyType();

				if (keyType == KeyType.Escape) {
					state = State.AUTH;
					window.close();
					return;
				}
			}
		});

		TextBox usernameBox = new TextBox();
		// This creates a text box that hides the letters you type so others cannot see your password.
		TextBox passwordBox = new TextBox().setMask('*');

		//This organizes the labels and text boxes into two neat columns.
		Panel gridPanel = new Panel(new GridLayout(2));
		gridPanel.addComponent(new Label("Username"));
		gridPanel.addComponent(usernameBox);
		gridPanel.addComponent(new Label("Password"));
		gridPanel.addComponent(passwordBox);
		gridPanel.addComponent(new Button("Login", () -> {
			String username = usernameBox.getText();
			String password = passwordBox.getText();

			// This checks the database to see if the name and password match a saved user.
			userID = GameAuth.login(username, password);
			String feedback = GameAuth.getFeedback();
			MessageDialog.showMessageDialog(gui, "Login", feedback, MessageDialogButton.OK);

			// This checks if the login was successful.
			if (userID != -1) {
				state = State.MENU;
			}
			window.close();
		}));
		gridPanel.addComponent(new Button("Back", () -> {
			state = State.AUTH;
			window.close();
		}));

		panel.addComponent(gridPanel);

		// This places the typing cursor in the username box so the user can start typing right away.
		usernameBox.takeFocus();
		
		gui.addWindowAndWait(window);

	}

	private static void showRegister() {
		// This creates a new window where a person can create a new account.	
		BasicWindow window = new BasicWindow("Register");
		Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
		window.setHints(Arrays.asList(Window.Hint.CENTERED));
		window.setComponent(panel);
		window.addWindowListener(new WindowListenerAdapter() {
			@Override
			public void onInput(Window basePane, KeyStroke keyStroke, AtomicBoolean deliverEvent) {
				KeyType keyType = keyStroke.getKeyType();

				if (keyType == KeyType.Escape) {
					state = State.AUTH;
					window.close();
					return;
				}
			}
		});

		TextBox usernameBox = new TextBox();
		TextBox passwordBox = new TextBox().setMask('*');
		// This creates a second secret box to make sure the user typed their password correctly.
		TextBox passwordRepeatedBox = new TextBox().setMask('*');

		Panel gridPanel = new Panel(new GridLayout(2));
		gridPanel.addComponent(new Label("Username"));
		gridPanel.addComponent(usernameBox);
		gridPanel.addComponent(new Label("Password"));
		gridPanel.addComponent(passwordBox);
		// This adds a text label to show the user where to re-type their password.
		gridPanel.addComponent(new Label("Comfirm Password"));
		gridPanel.addComponent(passwordRepeatedBox);

		gridPanel.addComponent(new Button("Register", () -> {
			String username = usernameBox.getText();
			String password = passwordBox.getText();
			String passwordRepeated = passwordRepeatedBox.getText();

			// This tries to save the new name and password into the system.
			userID = GameAuth.register(username, password, passwordRepeated);
			// This gets a message from the system to tell the user if the sign-up worked or failed.
			String feedback = GameAuth.getFeedback();
			MessageDialog.showMessageDialog(gui, "Register", feedback, MessageDialogButton.OK);

			// This checks if the new account was created successfully.
			if (userID != -1) {
				state = State.AUTH;
			} 
			window.close();
		}));

		gridPanel.addComponent(new Button("Back", () -> {
			state = State.AUTH;
			window.close();
		}));
		
		panel.addComponent(gridPanel);
		
		// This starts the typing cursor in the first box so the user can begin typing their name immediately.
		usernameBox.takeFocus();
		
		gui.addWindowAndWait(window);
	}
	
	private static void showMainMenu() {
		// This creates the main window that shows the different game options.
		BasicWindow window = new BasicWindow("Main Menu");
		Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
		window.setHints(Arrays.asList(Window.Hint.CENTERED));
		window.setComponent(panel);
		// This checks if the user presses the Escape key to bring up the exit menu.
		window.addWindowListener(new WindowListenerAdapter() {
			@Override
			public void onInput(Window basePane, KeyStroke keyStroke, AtomicBoolean deliverEvent) {
				KeyType keyType = keyStroke.getKeyType();

				if (keyType == KeyType.Escape) {
					MessageDialogButton result = MessageDialog.showMessageDialog(gui, "Confirm Exit", "Are you sure you want to exit game?", MessageDialogButton.No, MessageDialogButton.Yes);
					if (result == MessageDialogButton.Yes) {
						state = State.END;
						window.close();
					}
				}
			}
		});

		// This creates a button that lets the user start the game and see the map.
		Button playButton = new Button("Play", () -> {
			state = State.MAP;
			window.close();
		});

		panel.addComponent(playButton);
		// This adds a button that lets the user see a list of all the fish or creatures, that he has discovered.
		panel.addComponent(new Button("Bestiary", () -> {
			state = State.BESTIARY;
			window.close();
		}));
		panel.addComponent(new Button("Leaderboards", () -> {
			state = State.LEADERBOARDS;
			window.close();
		}));
		// This adds a button to open a menu for making copies of game data.
		panel.addComponent(new Button("Backup", () -> {
			state = State.BACKUP;
			window.close();
		}));
		panel.addComponent(new Button("Exit", () -> {
			state = State.END;
			window.close();
		}));

		// This highlights the Play button first so the user can start playing quickly.
		playButton.takeFocus();
		
		gui.addWindowAndWait(window);
	}
	

	private static void showBackupMenu() {
		// This creates a new window for the user to manage their saved game files.
		BasicWindow window = new BasicWindow("Backup Menu");
		Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
		window.setHints(Arrays.asList(Window.Hint.CENTERED));
		window.setComponent(panel);
		window.addWindowListener(new WindowListenerAdapter() {
			@Override
			public void onInput(Window basePane, KeyStroke keyStroke, AtomicBoolean deliverEvent) {
				KeyType keyType = keyStroke.getKeyType();

				// This closes the window if the user presses the Escape key on the keyboard.
				if (keyType == KeyType.Escape) {
					window.close();
					return;
				}
			}
		});

		Button backButton = new Button("Back", window::close);

		// This adds a button that writes the current game progress to a permanent file.
		panel.addComponent(new Button("Save", () -> {
			GameDatabase.save();
			MessageDialog.showMessageDialog(gui, "Save", GameDatabase.getFeedback(), MessageDialogButton.OK);
		}));
		// This adds a button that brings back game progress from a previously saved file.
		panel.addComponent(new Button("Load", () -> {
			GameDatabase.load();
			// This shows a message to tell the user if the loading process worked or failed.
			MessageDialog.showMessageDialog(gui, "Load", GameDatabase.getFeedback(), MessageDialogButton.OK);
		}));
		panel.addComponent(backButton);

		// This highlights the Back button so the user can easily select it.
		backButton.takeFocus();
		
		gui.addWindowAndWait(window);

	}

	private static void showBestiary() {
		// This creates a new window to show the list of fishes the player has found.
		BasicWindow window = new BasicWindow("Bestiary");
		Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
		window.setHints(Arrays.asList(Window.Hint.CENTERED));
		window.setComponent(panel);
		window.addWindowListener(new WindowListenerAdapter() {
			@Override
			public void onInput(Window basePane, KeyStroke keyStroke, AtomicBoolean deliverEvent) {
				KeyType keyType = keyStroke.getKeyType();

				if (keyType == KeyType.Escape) {
					window.close();
					return;
				}
			}
		});
		
		// This creates a chart with four columns to organize the fish information.
		Table<String> table = new Table<>("POND", "FISH", "RARITY", "KILLS"); 
		// This fills the chart with the specific data saved for the person playing.
		GameBestiary.updateTable(table, userID);
		// This sets the chart to show ten lines of information at a time.
		table.setVisibleRows(10);

		// This creates a button that shuts this window when clicked.
		Button backButton = new Button("Back", window::close);
		
		// This places the completed chart inside the window.
		panel.addComponent(table);
		panel.addComponent(backButton);
		
		backButton.takeFocus();
		
		// This shows the window and stops the rest of the game until the user finishes looking at it.
		gui.addWindowAndWait(window);
	}

	private static void showLeaderboards() {
		// This creates a new window that shows how players rank against each other.
		BasicWindow window = new BasicWindow("Leaderboards");
		// This sets up the window so that items are placed side by side from left to right.
		Panel panel = new Panel(new LinearLayout(Direction.HORIZONTAL));
		window.setHints(Arrays.asList(Window.Hint.CENTERED));
		window.setComponent(panel);
		window.addWindowListener(new WindowListenerAdapter() {
			@Override
			public void onInput(Window basePane, KeyStroke keyStroke, AtomicBoolean deliverEvent) {
				KeyType keyType = keyStroke.getKeyType();

				if (keyType == KeyType.Escape) {
					window.close();
					return;
				}
			}
		});
		
		// This creates a chart with columns for the player rank, their name, and their score.
		Table<String> table = new Table<>("RANK", "NAME", "STAT"); 
		GameLeaderboards.updateTable(table, "WPM");
		table.setVisibleRows(10);

		// This creates a separate section to hold buttons stacked on top of each other.
		Panel buttonsPanel = new Panel(new LinearLayout(Direction.VERTICAL));

		// This goes through every available ranking group and makes a button for each one.
		for (String category : GameLeaderboards.getCategories()) {
			// This adds a button that changes the chart to show different types of scores when clicked.
			buttonsPanel.addComponent(new Button(category, () -> GameLeaderboards.updateTable(table, category)));
		}
		Button backButton = new Button("Back", window::close);

		buttonsPanel.addComponent(backButton);

		// This places the score chart and the side buttons into the main window together.
		panel.addComponent(table);
		panel.addComponent(buttonsPanel);
		
		backButton.takeFocus();

		gui.addWindowAndWait(window);
	}

	private static void showMap() {

		// This gets the player's saved position and items from the database to start the map correctly.
		GameMap.syncFromDatabase(userID);

		Panel mapPanel = new Panel();
		Label descriptionLabel = new Label("");
		descriptionLabel.setPreferredSize(new TerminalSize(GameMap.getMapWidth() * 2 + 1, 3));
		Label goldLabel = new Label("");

		// This part of the code defines how to draw the world, the paths, and the player.
		mapPanel.setRenderer(new ComponentRenderer<Panel>() {
			@Override
			public void drawComponent(TextGUIGraphics graphics, Panel component) {
				graphics.setBackgroundColor(TextColor.ANSI.BLACK);
				graphics.fill(' ');

				// This goes through every square on the map to decide what to show there.
				for (int row = 0; row < GameMap.getMapHeight(); row++) {
					for (int col = 0; col < GameMap.getMapWidth(); col++) {
						int colUI = col * 2 + 1;

						GameMap.Location location = GameMap.getLocation(col, row);
							
						// print dot paths
						graphics.setForegroundColor(TextColor.ANSI.WHITE);
						graphics.putString(colUI, row, ".");
						
						// print location
						if (location != null) {
							if (location.pondID != -1) {
								// if it's a pond
								graphics.setForegroundColor(location.owned ? location.color : TextColor.ANSI.RED);
							} else {
								// if it's other location
								graphics.setForegroundColor(location.color);
							}
							graphics.putString(colUI, row, location.symbol);
						}  

						// print player
						if (GameMap.isPlayerIn(col, row)) {
							// displau player
							graphics.setForegroundColor(TextColor.ANSI.YELLOW);
							// This draws the "@" symbol to represent where the player is currently standing.
							graphics.putString(colUI, row, "@");
							// display info about player's location
							if (location != null) {
								// if player is inside a location
								if (location.pondID != -1) {
									// if it's a pond
									descriptionLabel.setText(location.owned ? "Enter " + location.description : "Buy " + location.description + " for " + location.goldCost + "g");
								} else if (location.owned) {
									// if it's other interactible location
									descriptionLabel.setText("Enter " + location.description);
								}
							} else {
								// if it's not a location
								descriptionLabel.setText("");
							}
						} 
					}
				}
				
				// This updates the text on the screen to show how much money the player has.
				goldLabel.setText("Balance: " + GameMap.getGoldCount() + "g");
			}

			@Override
			public TerminalSize getPreferredSize(Panel component) {
				return new TerminalSize(2 * GameMap.getMapWidth() + 1, GameMap.getMapHeight());
			}

		});


		BasicWindow window = new BasicWindow("Map");
		Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
		window.setHints(Arrays.asList(Window.Hint.CENTERED));
		window.setComponent(panel);
		window.addWindowListener(new WindowListenerAdapter() {
			@Override
			public void onInput(Window basePane, KeyStroke keyStroke, AtomicBoolean deliverEvent) {
				KeyType keyType = keyStroke.getKeyType();

				if (keyType == KeyType.Escape) {
					// if player goes back to menu
					GameMap.saveUserPositionAndGold(userID);
					state = State.MENU;
					window.close();
					return;
				// This checks if the user pressed the Enter key to try to go into a location or buy a pond.
				}	else if (keyType == KeyType.Enter) {
					// if player selected a map location
					GameMap.Location location = GameMap.getLocationAtPlayer();
					if (location == null) {
						return;
					} else if (location.pondID != -1) {
						// if it's a pond
						if (!location.owned) {
							// if the pond is locked
							if (GameMap.getGoldCount() >= location.goldCost) {
								// if player can buy the pond
								GameMap.unlockPond(location, userID);
								MessageDialog.showMessageDialog(gui, "Map", "Successfully unlocked the pond", MessageDialogButton.OK);
							} else {
								// if player has no enough gold
								MessageDialog.showMessageDialog(gui, "Map", "Insufficient gold to unlock pond!", MessageDialogButton.OK);
							}
						} else {
							// if the pond is unlocked
							MessageDialog.showMessageDialog(gui, "Map", "You've entered a pond!", MessageDialogButton.OK);
							GameMap.saveUserPositionAndGold(userID);
							pondID = location.pondID;
							// This changes the game to show the combat screen.
							state = State.COMBAT;
							window.close();
						}
					} else {
						// if it's other structures
						if (!location.owned) {
							// do nothing if it's not interactible
						} else {
							// if it's interactible
							MessageDialog.showMessageDialog(gui, "Map", "You're entering " + location.description + "!", MessageDialogButton.OK);
							GameMap.saveUserPositionAndGold(userID);
							state = location.state;
							window.close();
						} 
					}
				} else {
					// This moves the player character around the map based on which arrow keys were pressed.
					GameMap.handleMovement(keyStroke);
				}
			}
		});
		
		panel.addComponent(mapPanel);
		panel.addComponent(descriptionLabel);
		panel.addComponent(goldLabel);
		
		gui.addWindowAndWait(window);
	}

	private static void showCombat() {
		Panel combatPanel = new Panel();
		Label scoresLabel = new Label("");
		Label remainingInWaveLabel = new Label("");
		Label lastFishKilledLabel = new Label("");
		Panel lastFishKilledPanel = new Panel(new LinearLayout(Direction.VERTICAL));

		//This sets up the screen and loads the fishing information for this specific area.
		GameCombat.setFullscreenWidth(screen);
		GameCombat.syncFromDatabase(pondID);
		GameCombat.resetCombat();

		int laneCount = GameCombat.getLaneCount();
		int laneWidth = GameCombat.getLaneWidth();
		int stickmanCol = GameCombat.getStickmanCol();
		int stickmanWidth = GameCombat.getStickmanWidth();
		TerminalSize terminalSize = GameCombat.getTerminalSize();
		int screenWidth = terminalSize.getColumns();
		int middleRow = (terminalSize.getRows() - 1) / 2;
		
		lastFishKilledLabel.setPreferredSize(new TerminalSize(screenWidth, 2));

		// This part of the code draws the fishing lanes, the player character, and the swimming fish.
		combatPanel.setRenderer(new ComponentRenderer<Panel>() {
			@Override
			public void drawComponent(TextGUIGraphics graphics, Panel component) {
				// black background
				graphics.setBackgroundColor(TextColor.ANSI.BLACK);
				graphics.fill(' ');

				// draw lanes
				for (int i = 0; i < laneCount-1; i++) {
					graphics.setForegroundColor(TextColor.ANSI.WHITE);
					graphics.drawLine(stickmanCol + stickmanWidth, i*2+2, stickmanCol + stickmanWidth + laneWidth - 1, i*2+2,'-');
				}

				// Stickman
				graphics.setForegroundColor(TextColor.ANSI.GREEN);
				// This draws a simple stick figure to represent the player on the left side.
				graphics.putString(stickmanCol, middleRow - 1, " [O] "); 
				graphics.putString(stickmanCol, middleRow, "--|--"); 
				graphics.putString(stickmanCol, middleRow + 1, " / \\ "); 

				// Fishes
				synchronized (GameCombat.getSwimmingFishes()) {
					for (GameCombat.ActiveFish fish : GameCombat.getSwimmingFishes()) {
						int row = 1 + (fish.lane * 2);
						// make sure fish color is bright enough
						TextColor c = fish.type.color;
						if (GameCombat.isTooDark(c)) {
							graphics.setBackgroundColor(TextColor.ANSI.WHITE);
						}
						// print head
						graphics.setForegroundColor(c).putString(fish.x, row, fish.type.head);
						int wordX = fish.x + fish.type.head.length();
						// print word
						for (int i = 0; i < fish.word.length(); i++) {
							// This changes the color of the word to green for letters you typed correctly and red for letters you still need to type.
							graphics.setForegroundColor(i < fish.lettersTyped ? TextColor.ANSI.GREEN : TextColor.ANSI.RED);
							graphics.putString(wordX + i, row, "" + fish.word.charAt(i));
						}
						// print tail
						graphics.setForegroundColor(c).putString(wordX + fish.word.length(), row, fish.type.tail);
					}
				}

			}

			public TerminalSize getPreferredSize(Panel component) {
				return terminalSize;
			}
		});
		
		lastFishKilledPanel.setRenderer(new ComponentRenderer<Panel>() {
			@Override
			public void drawComponent(TextGUIGraphics graphics, Panel component) {
				// black background
				graphics.setBackgroundColor(TextColor.ANSI.BLACK);
				graphics.fill(' ');

				// last fish killed
				GameCombat.ActiveFish fish = GameCombat.getLastFishKilled();
				
				if (fish != null) {
					TextColor c = fish.type.color;

					if (GameCombat.isTooDark(c)) {
						graphics.setBackgroundColor(TextColor.ANSI.WHITE);
					}

					fish.x = 0;
					graphics.setForegroundColor(c).putString(fish.x, 0, fish.type.head);
					int wordX = fish.x + fish.type.head.length();
					// print word
					graphics.setForegroundColor(TextColor.ANSI.GREEN);
					graphics.putString(wordX, 0, fish.word);
					// print tail
					graphics.setForegroundColor(c).putString(wordX + fish.word.length(), 0, fish.type.tail);
					lastFishKilledPanel.setPreferredSize(new TerminalSize(fish.getFullWidth(), 1));
					lastFishKilledLabel.setText("[ " + fish.type.name + " (" + fish.type.rarity + " " + fish.type.coins + "g) of " + fish.type.pondType + " ]");
				}
				remainingInWaveLabel.setText("REMAINING IN WAVE: " + GameCombat.getNumRemainingInWave());
			}
			public TerminalSize getPreferredSize(Panel component) {
				return new TerminalSize(0, 1);
			}
		});


		BasicWindow window = new BasicWindow("Combat");
		Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
		window.setHints(Arrays.asList(Window.Hint.CENTERED));
		window.setComponent(panel);
		window.addWindowListener(new WindowListenerAdapter() {
			@Override
			public void onInput(Window basePane, KeyStroke keyStroke, AtomicBoolean deliverEvent) {
				if (GameCombat.isGamePaused()) {
					return;
				}

				KeyType keyType = keyStroke.getKeyType();

				if (keyType == KeyType.Escape) {
					GameCombat.togglePause(true);
					MessageDialogButton result = MessageDialog.showMessageDialog(gui, "Confirm Exit", """
						Return to Menu? 
						(This will remove all progress
						from this Combat session!)
						""", MessageDialogButton.No, MessageDialogButton.Yes);
					if (result == MessageDialogButton.Yes) {
						window.close();
					} else {
						GameCombat.togglePause(false);
					}
				// This checks which letter the user typed on their keyboard to see if it matches a fish word.
				} else if (keyType == KeyType.Character) {
					char typed = keyStroke.getCharacter();
					boolean hasKilledFish = GameCombat.handleCharacterInput(userID, typed);
					if (hasKilledFish) {
						GameSound.playAttackSFX();
					}
				}
			}
		});


		Thread combatThread = new Thread(() -> {
			GameSound.playAttackSFX();
			boolean isDead = false;
			while (!isDead) {
				try {
					Thread.sleep(20);

					if (GameCombat.isGamePaused()) {
						continue;
					}
					
					// This checks if the user has finished the current group of fish and is ready for the next set.
					boolean isNextWave = GameCombat.updateWave();

					if (isNextWave) {
						GameCombat.togglePause(true);
						MessageDialog.showMessageDialog(gui, "Combat", "Entering next wave...", MessageDialogButton.OK);
						GameCombat.togglePause(false);
						GameSound.playAttackSFX();
					}

					synchronized (GameCombat.getSwimmingFishes()) {
						// This moves the fish across the screen and checks if the game is over.
						isDead = GameCombat.updateCombat(userID, pondID);
					}

					scoresLabel.setText(GameCombat.getScores());
				} catch (Exception error) {}
			}

			GameSound.playAttackSFX();
			MessageDialog.showMessageDialog(gui, "Game Over!", GameCombat.getScores(), MessageDialogButton.OK);
			window.close();
		});

		// This begins the action so the fish start moving and the game timer starts.
		combatThread.start();

		panel.addComponent(lastFishKilledPanel);
		panel.addComponent(lastFishKilledLabel);
		panel.addComponent(scoresLabel);
		panel.addComponent(remainingInWaveLabel);
		panel.addComponent(combatPanel);
		
		gui.addWindowAndWait(window);

		combatThread.interrupt();
	}
}

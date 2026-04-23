import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.*;
import com.googlecode.lanterna.*;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.*;
import org.mindrot.jbcrypt.BCrypt;

import java.security.MessageDigest;
import java.sql.*;
import java.util.*;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.input.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Game {
	public enum State {BANNER, AUTH, LOGIN, REGISTER, MENU, MAP, BESTIARY, LEADERBOARDS, BACKUP, COMBAT, END};

	private static MultiWindowTextGUI gui;
	private static Screen screen;
	private static int userID = -1;

	private static State state;
	private static State lastMenuState;
	
	private static int pondID = -1;

	public static void main(String[] args) {
		// Do this upon exit
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (screen != null) {
				try {
					screen.stopScreen();
				} catch (Exception error2) {
					error2.printStackTrace();
				}
			}
			GameDatabase.save();
			GameDatabase.close();
			GameSound.close();
		}));

		try {
			Terminal terminal = new DefaultTerminalFactory().createTerminal();
			screen = new TerminalScreen(terminal);
			screen.startScreen();
			gui = new MultiWindowTextGUI(screen);

			state = State.BANNER;

			while (state != State.END) {
				try {
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
			System.exit(0);
		}
	}

	private static void showBanner() {
		BasicWindow window = new BasicWindow("FishdaTyper");
		Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
		window.setHints(Arrays.asList(Window.Hint.CENTERED));
		window.setComponent(panel);

		ProgressBar progressBar = new ProgressBar(0, 100, 10);
		progressBar.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Fill));
			
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

		

		new Thread(() -> {

			try {
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
				GameDatabase.waitForMariaDB();
				statusLabel.setText("Loading assets: attack sound effect...");
				GameSound.playAttackSFX();
				window.close();
			}
			
		}).start();

		gui.addWindowAndWait(window);
	}

	private static void showAuthMenu() {
		BasicWindow window = new BasicWindow("Authentication");
		Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
		window.setHints(Arrays.asList(Window.Hint.CENTERED));
		window.setComponent(panel);
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

		loginButton.takeFocus();
		
		gui.addWindowAndWait(window);
	}

	private static void showLogin() {
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
		TextBox passwordBox = new TextBox().setMask('*');

		Panel gridPanel = new Panel(new GridLayout(2));
		gridPanel.addComponent(new Label("Username"));
		gridPanel.addComponent(usernameBox);
		gridPanel.addComponent(new Label("Password"));
		gridPanel.addComponent(passwordBox);
		gridPanel.addComponent(new Button("Login", () -> {
			String username = usernameBox.getText();
			String password = passwordBox.getText();

			userID = GameAuth.login(username, password);
			String feedback = GameAuth.getFeedback();
			MessageDialog.showMessageDialog(gui, "Login", feedback, MessageDialogButton.OK);

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

		usernameBox.takeFocus();
		
		gui.addWindowAndWait(window);

	}

	private static void showRegister() {
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
		TextBox passwordRepeatedBox = new TextBox().setMask('*');

		Panel gridPanel = new Panel(new GridLayout(2));
		gridPanel.addComponent(new Label("Username"));
		gridPanel.addComponent(usernameBox);
		gridPanel.addComponent(new Label("Password"));
		gridPanel.addComponent(passwordBox);
		gridPanel.addComponent(new Label("Comfirm Password"));
		gridPanel.addComponent(passwordRepeatedBox);

		gridPanel.addComponent(new Button("Register", () -> {
			String username = usernameBox.getText();
			String password = passwordBox.getText();
			String passwordRepeated = passwordRepeatedBox.getText();

			userID = GameAuth.register(username, password, passwordRepeated);
			String feedback = GameAuth.getFeedback();
			MessageDialog.showMessageDialog(gui, "Register", feedback, MessageDialogButton.OK);

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
		
		usernameBox.takeFocus();
		
		gui.addWindowAndWait(window);
	}
	
	private static void showMainMenu() {

		BasicWindow window = new BasicWindow("Main Menu");
		Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
		window.setHints(Arrays.asList(Window.Hint.CENTERED));
		window.setComponent(panel);
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

		Button playButton = new Button("Play", () -> {
			state = State.MAP;
			window.close();
		});

		panel.addComponent(playButton);
		panel.addComponent(new Button("Bestiary", () -> {
			state = State.BESTIARY;
			window.close();
		}));
		panel.addComponent(new Button("Leaderboards", () -> {
			state = State.LEADERBOARDS;
			window.close();
		}));
		panel.addComponent(new Button("Backup", () -> {
			state = State.BACKUP;
			window.close();
		}));
		panel.addComponent(new Button("Exit", () -> {
			state = State.END;
			window.close();
		}));

		playButton.takeFocus();
		
		gui.addWindowAndWait(window);
	}
	

	private static void showBackupMenu() {
		BasicWindow window = new BasicWindow("Backup Menu");
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

		Button backButton = new Button("Back", window::close);

		panel.addComponent(new Button("Save", () -> {
			GameDatabase.save();
			MessageDialog.showMessageDialog(gui, "Save", GameDatabase.getFeedback(), MessageDialogButton.OK);
		}));
		panel.addComponent(new Button("Load", () -> {
			GameDatabase.load();
			MessageDialog.showMessageDialog(gui, "Load", GameDatabase.getFeedback(), MessageDialogButton.OK);
		}));
		panel.addComponent(backButton);

		backButton.takeFocus();
		
		gui.addWindowAndWait(window);

	}

	private static void showBestiary() {
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
		
		Table<String> table = new Table<>("POND", "FISH", "RARITY", "KILLS"); 
		GameBestiary.updateTable(table, userID);
		table.setVisibleRows(10);

		Button backButton = new Button("Back", window::close);
		
		panel.addComponent(table);
		panel.addComponent(backButton);
		
		backButton.takeFocus();
		
		gui.addWindowAndWait(window);
	}

	private static void showLeaderboards() {
		BasicWindow window = new BasicWindow("Leaderboards");
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
		
		Table<String> table = new Table<>("RANK", "NAME", "STAT"); 
		GameLeaderboards.updateTable(table, "WPM");
		table.setVisibleRows(10);

		Panel buttonsPanel = new Panel(new LinearLayout(Direction.VERTICAL));

		for (String category : GameLeaderboards.getCategories()) {
			buttonsPanel.addComponent(new Button(category, () -> GameLeaderboards.updateTable(table, category)));
		}
		Button backButton = new Button("Back", window::close);

		buttonsPanel.addComponent(backButton);

		panel.addComponent(table);
		panel.addComponent(buttonsPanel);
		
		backButton.takeFocus();

		gui.addWindowAndWait(window);
	}

	private static void showMap() {

		GameMap.syncFromDatabase(userID);

		Panel mapPanel = new Panel();
		Label descriptionLabel = new Label("");
		descriptionLabel.setPreferredSize(new TerminalSize(GameMap.getMapWidth() * 2 + 1, 3));
		Label goldLabel = new Label("");

		mapPanel.setRenderer(new ComponentRenderer<Panel>() {
			@Override
			public void drawComponent(TextGUIGraphics graphics, Panel component) {
				graphics.setBackgroundColor(TextColor.ANSI.BLACK);
				graphics.fill(' ');

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
					// if player moves
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
							// color the typed letters green, while untyped red
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
					
					boolean isNextWave = GameCombat.updateWave();

					if (isNextWave) {
						GameCombat.togglePause(true);
						MessageDialog.showMessageDialog(gui, "Combat", "Entering next wave...", MessageDialogButton.OK);
						GameCombat.togglePause(false);
						GameSound.playAttackSFX();
					}

					synchronized (GameCombat.getSwimmingFishes()) {
						isDead = GameCombat.updateCombat(userID, pondID);
					}

					scoresLabel.setText(GameCombat.getScores());
				} catch (Exception error) {}
			}

			GameSound.playAttackSFX();
			MessageDialog.showMessageDialog(gui, "Game Over!", GameCombat.getScores(), MessageDialogButton.OK);
			window.close();
		});

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

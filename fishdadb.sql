-- Simplified Fish Database Schema (PascalCase)
-- Compatible with MySQL 8.0+

-- =====================================================
-- 1. REFERENCE TABLES (create first due to FK constraints)
-- =====================================================

-- Fish Rarity Levels
CREATE TABLE FishRarity (
	RarityID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
	Rarity VARCHAR(15) NOT NULL,
	SpawnRate FLOAT NOT NULL
);

INSERT INTO FishRarity (Rarity, SpawnRate) VALUES 
	('Common', .9),
	('Uncommon', .75),
	('Rare', .5),
	('Epic', .25),
	('Legendary', .05);

-- Fish ASCII Art Components
CREATE TABLE FishArts (
	FishArtID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
	AsciiHead VARCHAR(15) NOT NULL,
	AsciiTail VARCHAR(15) NOT NULL
);

-- Ponds/Locations
CREATE TABLE Ponds (
	PondID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
	PondName VARCHAR(100) NOT NULL,
	PondCost INT DEFAULT 0 NOT NULL,
	PondSymbol VARCHAR(1) NOT NULL,
	PondTypeID INT NOT NULL
);

-- Pond Types
CREATE TABLE PondTypes (
	PondTypeID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
	PondType VARCHAR(20) NOT NULL
);

INSERT INTO PondTypes (PondType) VALUES
	('Freshwater'),
	('Saltwater'),
	('Brackish');

-- Word Bank for Typing Game
CREATE TABLE Words (
	WordID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
	WordText VARCHAR(12) NOT NULL
);

-- =====================================================
-- 2. CORE TABLES
-- =====================================================

-- Users
CREATE TABLE Users (
	UserID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
	UserName VARCHAR(60) UNIQUE NOT NULL,
	PasswordHash VARCHAR(255) NOT NULL,
	Gold INT DEFAULT 0 NOT NULL,
	LastMapRow INT DEFAULT NULL,
	LastMapCol INT DEFAULT NULL
);

-- Fish Species
CREATE TABLE Fishes (
	FishID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
	FishName VARCHAR(100) NOT NULL,
	PondTypeID INT NOT NULL,
	CoinsReward INT DEFAULT 1 NOT NULL,
	FishArtID INT NOT NULL,
	RarityID INT NOT NULL,
	FOREIGN KEY (PondID) REFERENCES Ponds(PondID)
		ON DELETE RESTRICT,
	FOREIGN KEY (FishArtID) REFERENCES FishArts(FishArtID)
		ON DELETE RESTRICT,
	FOREIGN KEY (RarityID) REFERENCES FishRarity(RarityID)
		ON DELETE RESTRICT
);

-- =====================================================
-- 3. JUNCTION & ACTIVITY TABLES
-- =====================================================

-- User-Pond Ownership (which ponds user has unlocked)
CREATE TABLE UserPonds (
	UserID INT NOT NULL,
	PondID INT NOT NULL,
	PRIMARY KEY (UserID, PondID),
	FOREIGN KEY (UserID) REFERENCES Users(UserID)
		ON DELETE CASCADE,
	FOREIGN KEY (PondID) REFERENCES Ponds(PondID)
		ON DELETE CASCADE
);

-- User's Discovered Fish Collection
CREATE TABLE DiscoveredFishes (
	UserID INT NOT NULL,
	FishID INT NOT NULL,
	KillCount INT DEFAULT 0 NOT NULL,
	PRIMARY KEY (UserID, FishID),
	FOREIGN KEY (UserID) REFERENCES Users(UserID) 
		ON DELETE CASCADE,
	FOREIGN KEY (FishID) REFERENCES Fishes(FishID) 
		ON DELETE CASCADE
);

-- Game Session Scores
CREATE TABLE ScorePerGame (
	ScoreID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
	UserID INT NOT NULL,
	PondID INT NOT NULL,
	WordPerMinute FLOAT DEFAULT 0 NOT NULL,
	SurvivalTime INT DEFAULT 0 NOT NULL,
	FishesCaught INT DEFAULT 0 NOT NULL,
	GoldEarned INT DEFAULT 0 NOT NULL,
	Waves INT DEFAULT 0 NOT NULL,
	FOREIGN KEY (UserID) REFERENCES Users(UserID) 
		ON DELETE CASCADE,
	FOREIGN KEY (PondID) REFERENCES Ponds(PondID) 
		ON DELETE CASCADE
);

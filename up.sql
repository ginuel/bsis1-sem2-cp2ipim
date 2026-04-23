CREATE DATABASE IF NOT EXISTS fishdadb;
USE fishdadb;

-- =====================================================
-- 1. REFERENCE TABLES (create first due to FK constraints)
-- =====================================================

--

-- Fish Rarity Levels
CREATE TABLE IF NOT EXISTS FishRarity (
	RarityID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
	Rarity VARCHAR(15) UNIQUE NOT NULL,
	SpawnRate FLOAT NOT NULL,
	CoinsReward INT DEFAULT 1 NOT NULL CHECK (CoinsReward >= 1) -- It must reward atleast 1 gold
);

-- Fish ASCII Art Components
CREATE TABLE IF NOT EXISTS FishArts (
	FishArtID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
	AsciiHead VARCHAR(15) NOT NULL,
	AsciiTail VARCHAR(15) NOT NULL
);




-- Pond Types
CREATE TABLE IF NOT EXISTS PondTypes (
	PondTypeID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
	PondType VARCHAR(20) UNIQUE NOT NULL
);


-- =====================================================
-- 1.5. ISOLATED TABLE
-- =====================================================

-- Word Bank for Typing Game
CREATE TABLE IF NOT EXISTS Words (
	WordID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
	WordText VARCHAR(30) UNIQUE NOT NULL
		CHECK (LENGTH(WordText) >= 1) -- Minimum word length
);

-- =====================================================
-- 2. CORE TABLES
-- =====================================================


-- Users
CREATE TABLE IF NOT EXISTS Users (
	UserID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
	Gold INT DEFAULT 0 NOT NULL,
	LastMapRow INT DEFAULT -1 NOT NULL,
	LastMapCol INT DEFAULT -1 NOT NULL,
	CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- UserCredentials
CREATE TABLE IF NOT EXISTS UserCredentials (
	CredentialID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
	UserID INT UNIQUE NOT NULL,
	UserName VARCHAR(60) UNIQUE NOT NULL,
	PasswordHash VARCHAR(255) NOT NULL,
	FOREIGN KEY (UserID) REFERENCES Users(UserID)
		ON DELETE CASCADE
);

-- Ponds
CREATE TABLE IF NOT EXISTS Ponds (
	PondID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
	PondName VARCHAR(100) UNIQUE NOT NULL,
	PondTypeID INT NOT NULL,
	PondCost INT DEFAULT 0 NOT NULL,
	PondSymbol CHAR(1) DEFAULT '?' NOT NULL,
	FOREIGN KEY (PondTypeID) REFERENCES PondTypes(PondTypeID)
		ON DELETE RESTRICT
);



-- Fish Species
CREATE TABLE IF NOT EXISTS Fishes (
	FishID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
	FishName VARCHAR(100) UNIQUE NOT NULL,
	FishArtID INT NOT NULL,
	RarityID INT NOT NULL,
	PondTypeID INT NOT NULL,
	FishColorHex CHAR(7) DEFAULT '#FFFFFF' NOT NULL
		CHECK (FishColorHex REGEXP '^#[a-fA-F0-9]{6}$'), -- It must only contain hex color
	FOREIGN KEY (PondTypeID) REFERENCES PondTypes(PondTypeID)
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
CREATE TABLE IF NOT EXISTS UserPonds (
	UserPondID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
	UserID INT NOT NULL,
	PondID INT NOT NULL,
	PurchasedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
	UNIQUE (UserID, PondID),
	FOREIGN KEY (UserID) REFERENCES Users(UserID)
		ON DELETE CASCADE,
	FOREIGN KEY (PondID) REFERENCES Ponds(PondID)
		ON DELETE CASCADE
);

-- User's Discovered Fish Collection
CREATE TABLE IF NOT EXISTS DiscoveredFishes (
	DiscoveredFishID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
	UserID INT NOT NULL,
	FishID INT NOT NULL,
	KillCount INT DEFAULT 1 NOT NULL
		CHECK (KillCount >=	1),
	DiscoveredAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
	UpdatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
	UNIQUE (UserID, FishID),
	FOREIGN KEY (UserID) REFERENCES Users(UserID) 
		ON DELETE CASCADE,
	FOREIGN KEY (FishID) REFERENCES Fishes(FishID) 
		ON DELETE CASCADE
);

-- Game Session Scores
CREATE TABLE IF NOT EXISTS ScorePerGame (
	ScoreID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
	UserID INT NOT NULL,
	PondID INT NOT NULL,
	CharsTyped INT DEFAULT 0 NOT NULL,
	SurvivalTime INT DEFAULT 0 NOT NULL,
	KillCount INT DEFAULT 0 NOT NULL,
	GoldEarned INT DEFAULT 0 NOT NULL,
	Waves INT DEFAULT 0 NOT NULL,
	PlayedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
	FOREIGN KEY (UserID) REFERENCES Users(UserID) 
		ON DELETE CASCADE,
	FOREIGN KEY (PondID) REFERENCES Ponds(PondID) 
		ON DELETE CASCADE
);


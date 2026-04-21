DROP DATABASE IF EXISTS fishdadb;

CREATE DATABASE fishdadb;
USE fishdadb;

-- =====================================================
-- 1. REFERENCE TABLES (create first due to FK constraints)
-- =====================================================

--

-- Fish Rarity Levels
CREATE TABLE FishRarity (
	RarityID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
	Rarity VARCHAR(15) UNIQUE NOT NULL,
	SpawnRate FLOAT NOT NULL,
	CoinsReward INT DEFAULT 1 NOT NULL CHECK (CoinsReward >= 1) -- It must reward atleast 1 gold
);

-- Fish ASCII Art Components
CREATE TABLE FishArts (
	FishArtID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
	AsciiHead VARCHAR(15) NOT NULL,
	AsciiTail VARCHAR(15) NOT NULL
);




-- Pond Types
CREATE TABLE PondTypes (
	PondTypeID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
	PondType VARCHAR(20) UNIQUE NOT NULL
);


-- =====================================================
-- 1.5. ISOLATED TABLE
-- =====================================================

-- Word Bank for Typing Game
CREATE TABLE Words (
	WordID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
	WordText VARCHAR(30) UNIQUE NOT NULL
		CHECK (LENGTH(WordText) >= 1) -- Minimum word length
);

-- =====================================================
-- 2. CORE TABLES
-- =====================================================


-- Users
CREATE TABLE Users (
	UserID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
	Gold INT DEFAULT 0 NOT NULL,
	LastMapRow INT DEFAULT -1 NOT NULL,
	LastMapCol INT DEFAULT -1 NOT NULL,
	CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- UserCredentials
CREATE TABLE UserCredentials (
	CredentialID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
	UserID INT UNIQUE NOT NULL,
	UserName VARCHAR(60) UNIQUE NOT NULL,
	PasswordHash VARCHAR(255) NOT NULL,
	FOREIGN KEY (UserID) REFERENCES Users(UserID)
		ON DELETE CASCADE
);

-- Ponds
CREATE TABLE Ponds (
	PondID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
	PondName VARCHAR(100) UNIQUE NOT NULL,
	PondTypeID INT NOT NULL,
	PondCost INT DEFAULT 0 NOT NULL,
	PondSymbol CHAR(1) DEFAULT '?' NOT NULL,
	FOREIGN KEY (PondTypeID) REFERENCES PondTypes(PondTypeID)
		ON DELETE RESTRICT
);



-- Fish Species
CREATE TABLE Fishes (
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
CREATE TABLE UserPonds (
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
CREATE TABLE DiscoveredFishes (
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
CREATE TABLE ScorePerGame (
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


-- Seed

INSERT INTO FishRarity (Rarity, SpawnRate, CoinsReward) VALUES 
('Common', 50, 1),
('Uncommon', 15, 2),
('Rare', 5, 5),
('Epic', 1.5,	9),
('Legendary', 0.3, 15);

INSERT INTO FishArts (AsciiHead, AsciiTail) VALUES
('<(', ')><|'),
('O>', '><'),
('{~', '~}'),
('C>', '>>'),
('<*', '*>');

INSERT INTO PondTypes (PondType) VALUES
('Freshwater'),
('Saltwater'),
('Brackish');

INSERT INTO Ponds (PondName, PondTypeID, PondCost, PondSymbol) VALUES
('Pampanga River', 1, 0, '1'),   -- Freshwater
('Manila Bay', 2, 100, '2'),      -- Saltwater
('Dagupan Estuary', 3, 250, '3'); -- Brackish


-- Freshwater Fishes (PondTypeID 1)
INSERT INTO Fishes (FishName, FishArtID, RarityID, PondTypeID, FishColorHex) VALUES
('Tilapia', 1, 1, 1, '#C0C0C0'), ('Catfish', 2, 1, 1, '#4B3621'), 
('Snakehead', 3, 2, 1, '#2E8B57'), ('Carp', 4, 2, 1, '#DAA520'),
('Gourami', 5, 1, 1, '#FF6347'), ('Neon Tetra', 1, 3, 1, '#0000FF'),
('Guppy', 2, 1, 1, '#FFD700'), ('Angelfish', 3, 2, 1, '#FFFFFF'),
('Betta', 4, 3, 1, '#FF0000'), ('Arowana', 5, 5, 1, '#FFD700'),
('Loach', 1, 1, 1, '#8B4513'), ('Barb', 2, 2, 1, '#FFA500'),
('Oscar', 3, 4, 1, '#FF4500'), ('Discus', 4, 4, 1, '#00CED1'),
('Killifish', 5, 1, 1, '#7FFF00'), ('Cichlid', 1, 2, 1, '#BA55D3'),
('Pike', 2, 3, 1, '#556B2F'), ('Perch', 3, 1, 1, '#F4A460'),
('Eel', 4, 3, 1, '#000000'), ('Ghost Shrimp', 5, 5, 1, '#F0F8FF');

-- Saltwater Fishes (PondTypeID 2)
INSERT INTO Fishes (FishName, FishArtID, RarityID, PondTypeID, FishColorHex) VALUES
('Tuna', 1, 1, 2, '#4682B4'), ('Mackerel', 2, 1, 2, '#708090'),
('Grouper', 3, 2, 2, '#8FBC8F'), ('Snapper', 4, 2, 2, '#CD5C5C'),
('Mahi-Mahi', 5, 3, 2, '#ADFF2F'), ('Clownfish', 1, 3, 2, '#FF8C00'),
('Blue Tang', 2, 2, 2, '#0000CD'), ('Lionfish', 3, 4, 2, '#A52A2A'),
('Moorish Idol', 4, 4, 2, '#FFFF00'), ('Barracuda', 5, 3, 2, '#A9A9A9'),
('Stingray', 1, 4, 2, '#D2B48C'), ('Swordfish', 2, 5, 2, '#778899'),
('Great White', 3, 5, 2, '#D3D3D3'), ('Hammerhead', 4, 5, 2, '#C0C0C0'),
('Seahorse', 5, 3, 2, '#EE82EE'), ('Butterflyfish', 1, 2, 2, '#FFFACD'),
('Wrasse', 2, 1, 2, '#48D1CC'), ('Parrotfish', 3, 2, 2, '#32CD32'),
('Triggerfish', 4, 3, 2, '#FF00FF'), ('Boxfish', 5, 4, 2, '#F0E68C');

-- Brackish Fishes (PondTypeID 3)
INSERT INTO Fishes (FishName, FishArtID, RarityID, PondTypeID, FishColorHex) VALUES
('Milkfish', 1, 1, 3, '#F5F5F5'), ('Barramundi', 2, 2, 3, '#BDB76B'),
('Tarpon', 3, 3, 3, '#E6E6FA'), ('Mullet', 4, 1, 3, '#696969'),
('Archerfish', 5, 3, 3, '#9ACD32'), ('Mudskipper', 1, 1, 3, '#556B2F'),
('Scat', 2, 2, 3, '#DAA520'), ('Pufferfish', 3, 4, 3, '#FFE4B5'),
('Monos', 4, 2, 3, '#C0C0C0'), ('Batfish', 5, 4, 3, '#2F4F4F'),
('Spotted Scat', 1, 2, 3, '#808000'), ('Glassfish', 2, 1, 3, '#E0FFFF'),
('Bumblebee Goby', 3, 3, 3, '#000000'), ('Pistol Shrimp', 4, 5, 3, '#FF7F50'),
('Red Mangrove Crab', 5, 4, 3, '#B22222'), ('Needlefish', 1, 3, 3, '#AFEEEE'),
('Jack', 2, 1, 3, '#708090'), ('Threadfin', 3, 2, 3, '#F5DEB3'),
('Ladyfish', 4, 2, 3, '#F0FFF0'), ('Stonefish', 5, 5, 3, '#8B4513');


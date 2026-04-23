USE fishdadb;
-- Seed

INSERT INTO FishRarity (Rarity, SpawnRate, CoinsReward) VALUES 
('Common', 50, 1),
('Uncommon', 15, 2),
('Rare', 5, 5),
('Epic', 1.5,	9),
('Legendary', 0.3, 15);

-- UPDATED: Accurate, fancy, symbol-only FishArts (No letters, no spaces)
INSERT INTO FishArts (AsciiHead, AsciiTail) VALUES
-- Predator Style (Sharp/Fast)
('⊸≡≡≡≡', '≡≡❯'), ('⊸≈≈≈≈', '≈≈❯'), ('⊸----', '--❯'), ('⊸####', '##❯'), ('⊸====', '==❯'),
('⋐(‽▓▓)', '▓▓∆'), ('⋐(‽▒▒)', '▒▒∆'), ('⋐(‽░░)', '░░∆'), ('⋐(‽■■)', '■■∆'), ('⋐(‽≡≡)', '≡≡∆'),

-- Tropical/Fancy Style (Flowing fins)
('≺(°≈≈)', '≈≈≫'), ('≺(°≡≡)', '≡≡≫'), ('≺(°▒▒)', '▒▒≫'), ('≺(°░░)', '░░≫'), ('≺(°==)', '==≫'),
('⟅(°▓▓)', '▓▓⟆'), ('⟅(°▒▒)', '▒▒⟆'), ('⟅(°░░)', '░░⟆'), ('⟅(°##)', '##⟆'), ('⟅(°≡≡)', '≡≡⟆'),

-- Round/Armored (Puffers/Boxfish)
('⊂(°‡‡)', '‡‡϶'), ('⊂(°††)', '††϶'), ('⊂(°**)', '**϶'), ('⊂(°%%)', '%%϶'), ('⊂(°##)', '##϶'),
('⟬(■■■)', '■■⟭'), ('⟬(▓▓▓)', '▓▓⟭'), ('⟬(▒▒▒)', '▒▒⟭'), ('⟬(░░░)', '░░⟭'), ('⟬(≡≡≡)', '≡≡⟭'),

-- Long/Serpentine (Eels/Needlefish)
('≈≈≈≈≈', '≈≈≈≈'), ('~~~~~', '~~~~'), ('-----', '----'), (':::::', '::::'), ('.....', '....'),
('«(···)', '··»'), ('«(---)', '--»'), ('«(===)', '==»'), ('«(___)', '__»'), ('«(~~~)', '~~»'),

-- Abyssal/Exotic (Anglers/Strange)
('⋐(°●▒)', '▒▒ミ'), ('⋐(°●▓)', '▓▓ミ'), ('⋐(°●≡)', '≡≡ミ'), ('⋐(°●≈)', '≈≈ミ'), ('⋐(°●░)', '░░ミ'),
('⋐(■■■)', '≡≡≡'), ('⋐(▓▓▓)', '≡≡≡'), ('⋐(▒▒▒)', '≡≡≡'), ('⋐(░░░)', '≡≡≡'), ('⋐(###)', '≡≡≡'),

-- Bottom/Benthic (Rays/Catfish)
('≺(≡≡≡)', '≡≡--'), ('≺(▒▒▒)', '▒▒--'), ('≺(░░░)', '░░--'), ('≺(≈≈≈)', '≈≈--'), ('≺(###)', '##--'),
('≡(°▒▒)', '▒▒❯'), ('≡(°▓▓)', '▓▓❯'), ('≡(°░░)', '░░❯'), ('≡(°==)', '==❯'), ('≡(°##)', '##❯');

INSERT INTO PondTypes (PondType) VALUES
('Freshwater'),
('Saltwater'),
('Brackish');

INSERT INTO Ponds (PondName, PondTypeID, PondCost, PondSymbol) VALUES
('Pampanga River', 1, 0, '1'),   
('Manila Bay', 2, 100, '2'),      
('Dagupan Estuary', 3, 250, '3'); 


-- Freshwater Fishes (PondTypeID 1)
-- Re-mapped to use Tropical/Fancy and Long/Serpentine ArtIDs
INSERT INTO Fishes (FishName, FishArtID, RarityID, PondTypeID, FishColorHex) VALUES
('Tilapia', 11, 1, 1, '#C0C0C0'), ('Catfish', 56, 1, 1, '#4B3621'), 
('Snakehead', 31, 2, 1, '#2E8B57'), ('Carp', 13, 2, 1, '#DAA520'),
('Gourami', 14, 1, 1, '#FF6347'), ('Neon Tetra', 15, 3, 1, '#0000FF'),
('Guppy', 12, 1, 1, '#FFD700'), ('Angelfish', 16, 2, 1, '#FFFFFF'),
('Betta', 17, 3, 1, '#FF0000'), ('Arowana', 5, 5, 1, '#FFD700'),
('Loach', 35, 1, 1, '#8B4513'), ('Barb', 18, 2, 1, '#FFA500'),
('Oscar', 26, 4, 1, '#FF4500'), ('Discus', 27, 4, 1, '#00CED1'),
('Killifish', 19, 1, 1, '#7FFF00'), ('Cichlid', 20, 2, 1, '#BA55D3'),
('Pike', 3, 3, 1, '#556B2F'), ('Perch', 28, 1, 1, '#F4A460'),
('Eel', 32, 3, 1, '#000000'), ('Ghost Shrimp', 46, 5, 1, '#F0F8FF');

-- Saltwater Fishes (PondTypeID 2)
-- Re-mapped to use Predator and Abyssal ArtIDs
INSERT INTO Fishes (FishName, FishArtID, RarityID, PondTypeID, FishColorHex) VALUES
('Tuna', 1, 1, 2, '#4682B4'), ('Mackerel', 2, 1, 2, '#708090'),
('Grouper', 29, 2, 2, '#8FBC8F'), ('Snapper', 30, 2, 2, '#CD5C5C'),
('Mahi-Mahi', 4, 3, 2, '#ADFF2F'), ('Clownfish', 12, 3, 2, '#FF8C00'),
('Blue Tang', 13, 2, 2, '#0000CD'), ('Lionfish', 41, 4, 2, '#A52A2A'),
('Moorish Idol', 16, 4, 2, '#FFFF00'), ('Barracuda', 4, 3, 2, '#A9A9A9'),
('Stingray', 51, 4, 2, '#D2B48C'), ('Swordfish', 1, 5, 2, '#778899'),
('Great White', 6, 5, 2, '#D3D3D3'), ('Hammerhead', 7, 5, 2, '#C0C0C0'),
('Seahorse', 35, 3, 2, '#EE82EE'), ('Butterflyfish', 18, 2, 2, '#FFFACD'),
('Wrasse', 19, 1, 2, '#48D1CC'), ('Parrotfish', 20, 2, 2, '#32CD32'),
('Triggerfish', 30, 3, 2, '#FF00FF'), ('Boxfish', 26, 4, 2, '#F0E68C');

-- Brackish Fishes (PondTypeID 3)
INSERT INTO Fishes (FishName, FishArtID, RarityID, PondTypeID, FishColorHex) VALUES
('Milkfish', 2, 1, 3, '#F5F5F5'), ('Barramundi', 8, 2, 3, '#BDB76B'),
('Tarpon', 9, 3, 3, '#E6E6FA'), ('Mullet', 10, 1, 3, '#696969'),
('Archerfish', 14, 3, 3, '#9ACD32'), ('Mudskipper', 56, 1, 3, '#556B2F'),
('Scat', 28, 2, 3, '#DAA520'), ('Pufferfish', 21, 4, 3, '#FFE4B5'),
('Monos', 29, 2, 3, '#C0C0C0'), ('Batfish', 16, 4, 3, '#2F4F4F'),
('Spotted Scat', 27, 2, 3, '#808000'), ('Glassfish', 15, 1, 3, '#E0FFFF'),
('Bumblebee Goby', 25, 3, 3, '#000000'), ('Pistol Shrimp', 48, 5, 3, '#FF7F50'),
('Red Mangrove Crab', 49, 4, 3, '#B22222'), ('Needlefish', 5, 3, 3, '#AFEEEE'),
('Jack', 4, 1, 3, '#708090'), ('Threadfin', 57, 2, 3, '#F5DEB3'),
('Ladyfish', 3, 2, 3, '#F0FFF0'), ('Stonefish', 60, 5, 3, '#8B4513');


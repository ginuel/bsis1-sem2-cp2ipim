-- =====================================================
-- DOWN SCRIPT: Cleanup and Database Removal
-- =====================================================

-- Switch to the database
USE fishdadb;

-- Disable foreign key checks to ensure a clean wipe
SET FOREIGN_KEY_CHECKS = 0;

-- 1. Drop Operational/Link Tables
DROP TABLE IF EXISTS DiscoveredFishes;
DROP TABLE IF EXISTS ScorePerGame;
DROP TABLE IF EXISTS UserPonds;

-- 2. Drop Core Tables
DROP TABLE IF EXISTS Fishes;
DROP TABLE IF EXISTS Users;
DROP TABLE IF EXISTS UsersCredentials;
DROP TABLE IF EXISTS Ponds;

-- 3. Drop Reference Tables
DROP TABLE IF EXISTS FishRarity;
DROP TABLE IF EXISTS FishArts;
DROP TABLE IF EXISTS PondTypes;
DROP TABLE IF EXISTS Words;

-- 4. Final Cleanup
SET FOREIGN_KEY_CHECKS = 1;

-- Optional: Drop the entire database
DROP DATABASE IF EXISTS fishdadb;


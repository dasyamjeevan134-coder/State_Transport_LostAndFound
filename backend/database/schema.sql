-- ============================================================================
-- STATE PUBLIC TRANSPORT LOST & FOUND SYSTEM
-- Database Schema Definition (MySQL 8.0+)
-- Database: lost_found_db
-- ============================================================================

CREATE DATABASE IF NOT EXISTS `lost_found_db`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `lost_found_db`;

-- Drop tables in reverse dependency order if recreating
DROP TABLE IF EXISTS `return_records`;
DROP TABLE IF EXISTS `claims`;
DROP TABLE IF EXISTS `item_matches`;
DROP TABLE IF EXISTS `found_items`;
DROP TABLE IF EXISTS `lost_items`;
DROP TABLE IF EXISTS `users`;

-- ----------------------------------------------------------------------------
-- 1. USERS TABLE
-- Stores credentials, contact details, and role assignments (PASSENGER, STAFF, ADMIN)
-- ----------------------------------------------------------------------------
CREATE TABLE `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `full_name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(120) NOT NULL,
    `password_hash` VARCHAR(255) NOT NULL,
    `phone` VARCHAR(20) NOT NULL,
    `role` ENUM('PASSENGER', 'STAFF', 'ADMIN') NOT NULL DEFAULT 'PASSENGER',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `uk_users_email` UNIQUE (`email`),
    INDEX `idx_users_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 2. LOST_ITEMS TABLE
-- Reported by passengers when an item is lost during public transit
-- ----------------------------------------------------------------------------
CREATE TABLE `lost_items` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `item_name` VARCHAR(150) NOT NULL,
    `category` VARCHAR(50) NOT NULL,
    `description` TEXT,
    `loss_date_time` DATETIME NOT NULL,
    `transport_type` VARCHAR(30) NOT NULL,
    `route_number` VARCHAR(50) NOT NULL,
    `location` VARCHAR(150) NOT NULL,
    `photo_url` VARCHAR(500),
    `passenger_id` BIGINT NOT NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_lost_items_passenger` FOREIGN KEY (`passenger_id`)
        REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX `idx_lost_items_category` (`category`),
    INDEX `idx_lost_items_status` (`status`),
    INDEX `idx_lost_items_transport` (`transport_type`),
    INDEX `idx_lost_items_passenger` (`passenger_id`),
    INDEX `idx_lost_items_date` (`loss_date_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 3. FOUND_ITEMS TABLE
-- Registered by transport staff / administrators when an item is recovered
-- ----------------------------------------------------------------------------
CREATE TABLE `found_items` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `item_name` VARCHAR(150) NOT NULL,
    `category` VARCHAR(50) NOT NULL,
    `description` TEXT,
    `found_date_time` DATETIME NOT NULL,
    `transport_type` VARCHAR(30) NOT NULL,
    `route_number` VARCHAR(50) NOT NULL,
    `location` VARCHAR(150) NOT NULL,
    `photo_url` VARCHAR(500),
    `staff_id` BIGINT NOT NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_found_items_staff` FOREIGN KEY (`staff_id`)
        REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX `idx_found_items_category` (`category`),
    INDEX `idx_found_items_status` (`status`),
    INDEX `idx_found_items_transport` (`transport_type`),
    INDEX `idx_found_items_staff` (`staff_id`),
    INDEX `idx_found_items_date` (`found_date_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 4. ITEM_MATCHES TABLE
-- Stores automated heuristic match scores between lost and found items (0 to 100)
-- ----------------------------------------------------------------------------
CREATE TABLE `item_matches` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `lost_item_id` BIGINT NOT NULL,
    `found_item_id` BIGINT NOT NULL,
    `match_score` DECIMAL(5,2) NOT NULL,
    `match_notes` VARCHAR(500),
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_matches_lost_item` FOREIGN KEY (`lost_item_id`)
        REFERENCES `lost_items` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_matches_found_item` FOREIGN KEY (`found_item_id`)
        REFERENCES `found_items` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `uk_lost_found_match` UNIQUE (`lost_item_id`, `found_item_id`),
    INDEX `idx_matches_score` (`match_score` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 5. CLAIMS TABLE
-- Submitted by passengers providing ownership proof for an available found item
-- ----------------------------------------------------------------------------
CREATE TABLE `claims` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `found_item_id` BIGINT NOT NULL,
    `passenger_id` BIGINT NOT NULL,
    `ownership_evidence` TEXT NOT NULL,
    `claim_status` VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    `reviewer_id` BIGINT NULL,
    `review_notes` TEXT NULL,
    `reviewed_at` DATETIME NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_claims_found_item` FOREIGN KEY (`found_item_id`)
        REFERENCES `found_items` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_claims_passenger` FOREIGN KEY (`passenger_id`)
        REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_claims_reviewer` FOREIGN KEY (`reviewer_id`)
        REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX `idx_claims_found_item` (`found_item_id`),
    INDEX `idx_claims_passenger` (`passenger_id`),
    INDEX `idx_claims_status` (`claim_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 6. RETURN_RECORDS TABLE
-- Formal handover record once an approved claim is completed by staff
-- ----------------------------------------------------------------------------
CREATE TABLE `return_records` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `found_item_id` BIGINT NOT NULL,
    `claim_id` BIGINT NOT NULL,
    `staff_id` BIGINT NOT NULL,
    `return_date` DATETIME NOT NULL,
    `remarks` TEXT,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_returns_found_item` FOREIGN KEY (`found_item_id`)
        REFERENCES `found_items` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_returns_claim` FOREIGN KEY (`claim_id`)
        REFERENCES `claims` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_returns_staff` FOREIGN KEY (`staff_id`)
        REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `uk_returns_found_item` UNIQUE (`found_item_id`),
    CONSTRAINT `uk_returns_claim` UNIQUE (`claim_id`),
    INDEX `idx_returns_staff` (`staff_id`),
    INDEX `idx_returns_date` (`return_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

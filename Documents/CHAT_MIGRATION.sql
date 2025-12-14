-- =====================================================
-- CHAT SYSTEM DATABASE MIGRATION
-- Created: December 7, 2025
-- Description: Real-time customer support chat system
-- =====================================================

-- Table: chat_conversations
-- Stores conversation metadata between customers and admins
CREATE TABLE IF NOT EXISTS chat_conversations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NULL,                          -- NULL for guest users
    guest_name VARCHAR(255) NULL,
    guest_email VARCHAR(255) NULL,
    guest_phone VARCHAR(50) NULL,
    assigned_admin_id BIGINT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    last_message VARCHAR(500) NULL,
    last_message_at TIMESTAMP NULL,
    has_unread_messages BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (assigned_admin_id) REFERENCES users(id) ON DELETE SET NULL,

    INDEX idx_conversation_id (conversation_id),
    INDEX idx_user_id (user_id),
    INDEX idx_assigned_admin_id (assigned_admin_id),
    INDEX idx_status (status),
    INDEX idx_has_unread (has_unread_messages),
    INDEX idx_last_message_at (last_message_at),
    INDEX idx_guest_email (guest_email),
    INDEX idx_guest_phone (guest_phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: chat_messages
-- Stores individual messages in conversations
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id VARCHAR(255) NOT NULL UNIQUE,
    conversation_id BIGINT NOT NULL,
    sender VARCHAR(50) NOT NULL,                  -- 'CUSTOMER' or 'ADMIN'
    sender_user_id BIGINT NULL,                   -- NULL for guest messages
    content TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (conversation_id) REFERENCES chat_conversations(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_user_id) REFERENCES users(id) ON DELETE SET NULL,

    INDEX idx_message_id (message_id),
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_created_at (created_at),
    INDEX idx_is_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- SAMPLE DATA (Optional - for testing)
-- =====================================================

-- Note: Uncomment below if you want to insert sample data for testing

/*
-- Sample guest conversation
INSERT INTO chat_conversations (conversation_id, guest_name, guest_email, guest_phone, status, has_unread_messages)
VALUES ('test-conv-001', 'John Guest', 'guest@example.com', '0123456789', 'PENDING', TRUE);

-- Sample message from guest
INSERT INTO chat_messages (message_id, conversation_id, sender, content, is_read)
VALUES ('msg-001', (SELECT id FROM chat_conversations WHERE conversation_id = 'test-conv-001'),
        'CUSTOMER', 'Hello, I need help with my order', FALSE);

-- Auto-reply message
INSERT INTO chat_messages (message_id, conversation_id, sender, content, is_read)
VALUES ('msg-002', (SELECT id FROM chat_conversations WHERE conversation_id = 'test-conv-001'),
        'ADMIN', 'Cảm ơn bạn đã liên hệ! Nhân viên hỗ trợ sẽ phản hồi trong giây lát.', FALSE);
*/

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================

-- Check tables created successfully
SHOW TABLES LIKE 'chat_%';

-- Check chat_conversations structure
DESCRIBE chat_conversations;

-- Check chat_messages structure
DESCRIBE chat_messages;

-- Count existing conversations
SELECT COUNT(*) as total_conversations FROM chat_conversations;

-- Count existing messages
SELECT COUNT(*) as total_messages FROM chat_messages;

-- =====================================================
-- ROLLBACK SCRIPT (Use if needed to undo migration)
-- =====================================================

/*
-- Drop tables in correct order (child tables first)
DROP TABLE IF EXISTS chat_messages;
DROP TABLE IF EXISTS chat_conversations;
*/


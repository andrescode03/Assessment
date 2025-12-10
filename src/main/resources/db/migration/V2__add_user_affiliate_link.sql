-- Add affiliate_id column to users table to link User with Affiliate
ALTER TABLE users ADD COLUMN affiliate_id BIGINT;

-- Add foreign key constraint
ALTER TABLE users 
ADD CONSTRAINT fk_user_affiliate 
FOREIGN KEY (affiliate_id) REFERENCES affiliates(id);

-- Create index for better query performance
CREATE INDEX idx_users_affiliate_id ON users(affiliate_id);

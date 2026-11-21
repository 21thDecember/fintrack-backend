-- V4__goal_types_and_progress.sql
-- Add goal_type, target_value, current_value, unit to goals table

ALTER TABLE goals
    ADD COLUMN IF NOT EXISTS goal_type VARCHAR(30) NOT NULL DEFAULT 'MILESTONE',
    ADD COLUMN IF NOT EXISTS target_value NUMERIC(15, 2) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS current_value NUMERIC(15, 2) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS unit VARCHAR(50) DEFAULT '';

CREATE INDEX IF NOT EXISTS idx_goals_user_goal_type ON goals(user_id, goal_type);

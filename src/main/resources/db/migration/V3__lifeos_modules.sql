-- V3__lifeos_modules.sql
-- LifeOS Expansion: Goals, Tasks, and Habits Schema

-- 1. Goals Table
CREATE TABLE goals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(30) DEFAULT 'PERSONAL', -- CAREER, LEARNING, FINANCE, HEALTH, PERSONAL, OTHER
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED', -- NOT_STARTED, IN_PROGRESS, COMPLETED, CANCELLED
    start_date DATE,
    target_date DATE,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_goals_user_status ON goals(user_id, status);
CREATE INDEX idx_goals_user_target_date ON goals(user_id, target_date);

-- 2. Goal Milestones Table
CREATE TABLE goal_milestones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    goal_id UUID NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    due_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'TODO', -- TODO, IN_PROGRESS, COMPLETED
    display_order INTEGER NOT NULL DEFAULT 0,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_goal_milestones_goal ON goal_milestones(goal_id);
CREATE INDEX idx_goal_milestones_user ON goal_milestones(user_id);

-- 3. Tasks Table
CREATE TABLE tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    parent_task_id UUID REFERENCES tasks(id) ON DELETE CASCADE,
    goal_id UUID REFERENCES goals(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH
    status VARCHAR(20) NOT NULL DEFAULT 'TODO', -- TODO, IN_PROGRESS, COMPLETED, CANCELLED
    start_date TIMESTAMPTZ,
    due_date TIMESTAMPTZ,
    estimated_minutes INTEGER,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tasks_user_id ON tasks(user_id);
CREATE INDEX idx_tasks_user_status ON tasks(user_id, status);
CREATE INDEX idx_tasks_user_due_date ON tasks(user_id, due_date);
CREATE INDEX idx_tasks_goal_id ON tasks(goal_id);
CREATE INDEX idx_tasks_parent_id ON tasks(parent_task_id);

-- 4. Habits Table
CREATE TABLE habits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    goal_id UUID REFERENCES goals(id) ON DELETE SET NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    frequency VARCHAR(20) NOT NULL DEFAULT 'DAILY', -- DAILY, WEEKLY
    target_count INTEGER NOT NULL DEFAULT 1,
    active_days JSONB, -- Array of active weekdays, e.g. ["MONDAY", "WEDNESDAY", "FRIDAY"]
    start_date DATE NOT NULL DEFAULT CURRENT_DATE,
    end_date DATE,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_habits_user ON habits(user_id);
CREATE INDEX idx_habits_goal ON habits(goal_id);

-- 5. Habit Check-ins Table
CREATE TABLE habit_checkins (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    habit_id UUID NOT NULL REFERENCES habits(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    completion_date DATE NOT NULL,
    count INTEGER NOT NULL DEFAULT 1,
    note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_habit_checkin_date UNIQUE (habit_id, completion_date)
);

CREATE INDEX idx_habit_checkins_user_date ON habit_checkins(user_id, completion_date);

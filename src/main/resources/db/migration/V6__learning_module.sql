-- V6__learning_module.sql
-- LifeOS Expansion: Learning Management Schema (Courses, Books, Skills, Learning Sessions)

-- 1. Courses Table
CREATE TABLE learning_courses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    goal_id UUID REFERENCES goals(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    provider VARCHAR(150),
    url VARCHAR(2048),
    category VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNED', -- PLANNED, IN_PROGRESS, COMPLETED, PAUSED, CANCELLED
    priority VARCHAR(20) DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH
    start_date DATE,
    target_date DATE,
    estimated_hours NUMERIC(8,2),
    total_lessons INTEGER,
    completed_lessons INTEGER DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_course_lessons CHECK (
        (total_lessons IS NULL OR total_lessons >= 0) AND
        (completed_lessons IS NULL OR completed_lessons >= 0) AND
        (total_lessons IS NULL OR completed_lessons IS NULL OR completed_lessons <= total_lessons)
    ),
    CONSTRAINT chk_course_estimated_hours CHECK (estimated_hours IS NULL OR estimated_hours >= 0)
);

CREATE INDEX idx_learning_courses_user_status ON learning_courses(user_id, status);
CREATE INDEX idx_learning_courses_user_category ON learning_courses(user_id, category);
CREATE INDEX idx_learning_courses_goal_id ON learning_courses(goal_id);

-- 2. Books Table
CREATE TABLE learning_books (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    goal_id UUID REFERENCES goals(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255),
    description TEXT,
    isbn VARCHAR(30),
    category VARCHAR(100),
    url VARCHAR(2048),
    status VARCHAR(20) NOT NULL DEFAULT 'WANT_TO_READ', -- WANT_TO_READ, READING, COMPLETED, PAUSED, DROPPED
    total_pages INTEGER,
    current_page INTEGER DEFAULT 0,
    start_date DATE,
    target_date DATE,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_book_pages CHECK (
        (total_pages IS NULL OR total_pages > 0) AND
        (current_page IS NULL OR current_page >= 0) AND
        (total_pages IS NULL OR current_page IS NULL OR current_page <= total_pages)
    )
);

CREATE INDEX idx_learning_books_user_status ON learning_books(user_id, status);
CREATE INDEX idx_learning_books_user_category ON learning_books(user_id, category);
CREATE INDEX idx_learning_books_goal_id ON learning_books(goal_id);

-- 3. Skills Table
CREATE TABLE learning_skills (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    goal_id UUID REFERENCES goals(id) ON DELETE SET NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(30) NOT NULL DEFAULT 'TECHNICAL', -- TECHNICAL, LANGUAGE, SOFT_SKILL, PERSONAL, OTHER
    current_level SMALLINT NOT NULL DEFAULT 1,
    target_level SMALLINT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_skill_current_level CHECK (current_level BETWEEN 1 AND 5),
    CONSTRAINT chk_skill_target_level CHECK (target_level IS NULL OR target_level BETWEEN 1 AND 5)
);

CREATE INDEX idx_learning_skills_user_category ON learning_skills(user_id, category);
CREATE INDEX idx_learning_skills_user_active ON learning_skills(user_id, is_active);
CREATE INDEX idx_learning_skills_goal_id ON learning_skills(goal_id);

-- 4. Skill Level History Table
CREATE TABLE learning_skill_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    skill_id UUID NOT NULL REFERENCES learning_skills(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    previous_level SMALLINT NOT NULL,
    new_level SMALLINT NOT NULL,
    note TEXT,
    changed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_skill_history_previous CHECK (previous_level BETWEEN 1 AND 5),
    CONSTRAINT chk_skill_history_new CHECK (new_level BETWEEN 1 AND 5)
);

CREATE INDEX idx_learning_skill_history_skill ON learning_skill_history(skill_id, changed_at);
CREATE INDEX idx_learning_skill_history_user ON learning_skill_history(user_id);

-- 5. Learning Sessions Table
CREATE TABLE learning_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    course_id UUID REFERENCES learning_courses(id) ON DELETE SET NULL,
    book_id UUID REFERENCES learning_books(id) ON DELETE SET NULL,
    skill_id UUID REFERENCES learning_skills(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    learning_date DATE NOT NULL,
    start_time TIMESTAMPTZ,
    end_time TIMESTAMPTZ,
    duration_minutes INTEGER NOT NULL,
    source VARCHAR(20) NOT NULL DEFAULT 'MANUAL', -- MANUAL, TIMER
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_session_duration CHECK (duration_minutes > 0 AND duration_minutes <= 1440),
    CONSTRAINT chk_session_times CHECK (start_time IS NULL OR end_time IS NULL OR end_time > start_time)
);

CREATE INDEX idx_learning_sessions_user_date ON learning_sessions(user_id, learning_date);
CREATE INDEX idx_learning_sessions_user_course ON learning_sessions(user_id, course_id);
CREATE INDEX idx_learning_sessions_user_book ON learning_sessions(user_id, book_id);
CREATE INDEX idx_learning_sessions_user_skill ON learning_sessions(user_id, skill_id);

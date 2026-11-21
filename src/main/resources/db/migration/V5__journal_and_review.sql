-- V5__journal_and_review.sql
-- LifeOS Expansion: Daily Journal, Mood Tracking, and Weekly Review Schema

-- 1. Daily Journals Table
CREATE TABLE daily_journals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    journal_date DATE NOT NULL,
    title VARCHAR(255),
    content TEXT,
    highlight TEXT,
    gratitude TEXT,
    improvement TEXT,
    tomorrow_plan TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_daily_journals_user_date UNIQUE (user_id, journal_date)
);

CREATE INDEX idx_daily_journals_user_date ON daily_journals(user_id, journal_date);
CREATE INDEX idx_daily_journals_user_created ON daily_journals(user_id, created_at);

-- 2. Mood Entries Table
CREATE TABLE mood_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    recorded_date DATE NOT NULL,
    mood_score SMALLINT NOT NULL CHECK (mood_score BETWEEN 1 AND 5),
    energy_score SMALLINT CHECK (energy_score IS NULL OR (energy_score BETWEEN 1 AND 5)),
    note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_mood_entries_user_date UNIQUE (user_id, recorded_date)
);

CREATE INDEX idx_mood_entries_user_date ON mood_entries(user_id, recorded_date);

-- 3. Weekly Reviews Table
CREATE TABLE weekly_reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    week_start_date DATE NOT NULL,
    week_end_date DATE NOT NULL,
    summary TEXT,
    wins TEXT,
    challenges TEXT,
    lessons_learned TEXT,
    improvements TEXT,
    next_week_priorities TEXT,
    additional_notes TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_weekly_reviews_user_week UNIQUE (user_id, week_start_date),
    CONSTRAINT chk_weekly_reviews_dates CHECK (week_end_date = week_start_date + 6)
);

CREATE INDEX idx_weekly_reviews_user_week ON weekly_reviews(user_id, week_start_date);
CREATE INDEX idx_weekly_reviews_user_status ON weekly_reviews(user_id, status);

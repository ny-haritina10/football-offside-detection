CREATE TABLE matches (
    match_id SERIAL PRIMARY KEY,
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP,
    blue_team_score INTEGER DEFAULT 0,
    red_team_score INTEGER DEFAULT 0,
    total_actions INTEGER DEFAULT 0,
    total_offsides INTEGER DEFAULT 0
);

CREATE TABLE match_actions (
    action_id SERIAL PRIMARY KEY,
    match_id INTEGER REFERENCES matches(match_id),
    action_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    receive_image_path TEXT NOT NULL,
    shoot_image_path TEXT NOT NULL,
    is_offside BOOLEAN NOT NULL,
    is_goal BOOLEAN NOT NULL,
    scoring_team TEXT,
    action_result TEXT NOT NULL,
    CONSTRAINT fk_match
        FOREIGN KEY(match_id)
        REFERENCES matches(match_id)
        ON DELETE CASCADE
);
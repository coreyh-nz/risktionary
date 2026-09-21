ALTER TABLE risktionary_game ADD COLUMN scoring_max_points INT NOT NULL DEFAULT 1000;
ALTER TABLE risktionary_game ADD COLUMN scoring_min_points INT NOT NULL DEFAULT 100;
ALTER TABLE risktionary_game ADD COLUMN scoring_untimed_window_ms BIGINT NOT NULL DEFAULT 60000;
ALTER TABLE risktionary_game_round ADD COLUMN drawer_points INT NOT NULL DEFAULT 0;
ALTER TABLE risktionary_game_round_guess ADD COLUMN points INT NULL;

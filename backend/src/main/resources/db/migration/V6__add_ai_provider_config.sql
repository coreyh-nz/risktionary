ALTER TABLE risktionary_game_round_ai_usage ADD COLUMN provider VARCHAR(64) NOT NULL DEFAULT 'unknown';

CREATE TABLE IF NOT EXISTS risktionary_game_ai_use_case (game_id uuid, usage_purpose VARCHAR(32), provider VARCHAR(64) NOT NULL, model_name VARCHAR(128) NOT NULL, temperature DOUBLE PRECISION NOT NULL, CONSTRAINT pk_risktionary_game_ai_use_case PRIMARY KEY (game_id, usage_purpose), CONSTRAINT fk_risktionary_game_ai_use_case_game_id__id FOREIGN KEY (game_id) REFERENCES risktionary_game(id) ON DELETE CASCADE ON UPDATE RESTRICT);

ALTER TABLE risktionary_game_round ADD COLUMN abandoned_ai_calls INT NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS risktionary_game_round_ai_usage (id uuid PRIMARY KEY, round_id uuid NOT NULL, seq INT NOT NULL, player_id uuid NULL, usage_purpose VARCHAR(32) NOT NULL, model_name VARCHAR(128) NOT NULL, prompt_tokens INT NOT NULL, completion_tokens INT NOT NULL, total_tokens INT NOT NULL, recorded_at TIMESTAMP NOT NULL, CONSTRAINT fk_risktionary_game_round_ai_usage_round_id__id FOREIGN KEY (round_id) REFERENCES risktionary_game_round(id) ON DELETE CASCADE ON UPDATE RESTRICT, CONSTRAINT fk_risktionary_game_round_ai_usage_player_id__id FOREIGN KEY (player_id) REFERENCES risktionary_game_player(id) ON DELETE RESTRICT ON UPDATE RESTRICT);
CREATE INDEX risktionary_game_round_ai_usage_round_id ON risktionary_game_round_ai_usage (round_id);
CREATE INDEX risktionary_game_round_ai_usage_player_id ON risktionary_game_round_ai_usage (player_id);
CREATE INDEX risktionary_game_round_ai_usage_purpose ON risktionary_game_round_ai_usage (usage_purpose);

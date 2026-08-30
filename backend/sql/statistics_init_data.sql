-- Optional statistics init data.
-- This stage does not require seed statistics records.

USE `ai_design_platform`;


SELECT * FROM survey_feedback ORDER BY id DESC LIMIT 5;
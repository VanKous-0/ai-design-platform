UPDATE `prompt_template`
SET `status` = 0
WHERE `source_type` = 'DEMO'
  AND `is_deleted` = 0;

UPDATE `review_record`
SET `status` = 0
WHERE `source_type` = 'DEMO'
  AND `is_deleted` = 0;

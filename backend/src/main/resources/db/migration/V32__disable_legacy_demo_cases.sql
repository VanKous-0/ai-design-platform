UPDATE `case_project`
SET `status` = 0
WHERE `code` LIKE 'CASE\_%'
  AND `source_desc` LIKE '开发测试数据%'
  AND `is_deleted` = 0;

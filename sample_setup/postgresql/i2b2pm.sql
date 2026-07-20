-- remove SHRINE project
DELETE FROM pm_project_data WHERE project_id = 'SHRINE';
DELETE FROM pm_project_user_roles WHERE user_id = 'shrine';
DELETE FROM pm_user_data WHERE user_id = 'shrine';

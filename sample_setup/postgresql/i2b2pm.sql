-- ontologystore
INSERT INTO pm_cell_data (cell_id,project_path,"name",method_cd,url,can_override,status_cd) VALUES ('ONTSTORE','/','OntologyStore Cell','REST','http://localhost:9090/i2b2/services/OntologyStoreService/',1,'A');

-- remove SHRINE
DELETE FROM pm_project_data WHERE project_id = 'SHRINE';
DELETE FROM pm_project_user_roles WHERE user_id = 'shrine';
DELETE FROM pm_user_data WHERE user_id = 'shrine';

-- add ontologystore-admin role to i2b2 admin
INSERT INTO pm_project_user_roles (project_id,user_id,user_role_cd,entry_date,status_cd) VALUES ('Demo','i2b2','ONTSTORE_ADMIN',current_timestamp,'A');

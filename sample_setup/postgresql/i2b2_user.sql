-- create i2b2 user
INSERT INTO pm_user_data (user_id,full_name,"password",entry_date,status_cd) VALUES ('kvb2','Kevin Bui','9117d59a69dc49807671a51f10ab7f',current_timestamp,'A');

-- add user to the Demo project
INSERT INTO pm_project_user_roles (project_id,user_id,user_role_cd,entry_date,status_cd) VALUES ('Demo','kvb2','USER',current_timestamp,'A');
INSERT INTO pm_project_user_roles (project_id,user_id,user_role_cd,entry_date,status_cd) VALUES ('Demo','kvb2','DATA_DEID',current_timestamp,'A');
INSERT INTO pm_project_user_roles (project_id,user_id,user_role_cd,entry_date,status_cd) VALUES ('Demo','kvb2','DATA_OBFSC',current_timestamp,'A');
INSERT INTO pm_project_user_roles (project_id,user_id,user_role_cd,entry_date,status_cd) VALUES ('Demo','kvb2','DATA_AGG',current_timestamp,'A');
INSERT INTO pm_project_user_roles (project_id,user_id,user_role_cd,entry_date,status_cd) VALUES ('Demo','kvb2','DATA_LDS',current_timestamp,'A');
INSERT INTO pm_project_user_roles (project_id,user_id,user_role_cd,entry_date,status_cd) VALUES ('Demo','kvb2','EDITOR',current_timestamp,'A');
INSERT INTO pm_project_user_roles (project_id,user_id,user_role_cd,entry_date,status_cd) VALUES ('Demo','kvb2','DATA_PROT',current_timestamp,'A');

-- add user to the ACT project
INSERT INTO pm_project_user_roles (project_id,user_id,user_role_cd,entry_date,status_cd) VALUES ('ACT','kvb2','USER',current_timestamp,'A');
INSERT INTO pm_project_user_roles (project_id,user_id,user_role_cd,entry_date,status_cd) VALUES ('ACT','kvb2','DATA_DEID',current_timestamp,'A');
INSERT INTO pm_project_user_roles (project_id,user_id,user_role_cd,entry_date,status_cd) VALUES ('ACT','kvb2','DATA_OBFSC',current_timestamp,'A');
INSERT INTO pm_project_user_roles (project_id,user_id,user_role_cd,entry_date,status_cd) VALUES ('ACT','kvb2','DATA_AGG',current_timestamp,'A');
INSERT INTO pm_project_user_roles (project_id,user_id,user_role_cd,entry_date,status_cd) VALUES ('ACT','kvb2','DATA_LDS',current_timestamp,'A');
INSERT INTO pm_project_user_roles (project_id,user_id,user_role_cd,entry_date,status_cd) VALUES ('ACT','kvb2','EDITOR',current_timestamp,'A');
INSERT INTO pm_project_user_roles (project_id,user_id,user_role_cd,entry_date,status_cd) VALUES ('ACT','kvb2','DATA_PROT',current_timestamp,'A');

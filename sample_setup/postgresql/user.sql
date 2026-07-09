-- create i2b2 user
INSERT INTO pm_user_data (user_id,full_name,"password",entry_date,status_cd) VALUES ('kvb2','Kevin Bui','9117d59a69dc49807671a51f10ab7f',current_timestamp,'A');

-- add user to Demo project
INSERT INTO pm_project_user_roles (project_id,user_id,user_role_cd,status_cd) VALUES ('Demo','kvb2','USER','A');
INSERT INTO pm_project_user_roles (project_id,user_id,user_role_cd,status_cd) VALUES ('Demo','kvb2','DATA_DEID','A');
INSERT INTO pm_project_user_roles (project_id,user_id,user_role_cd,status_cd) VALUES ('Demo','kvb2','DATA_OBFSC','A');
INSERT INTO pm_project_user_roles (project_id,user_id,user_role_cd,status_cd) VALUES ('Demo','kvb2','DATA_AGG','A');
INSERT INTO pm_project_user_roles (project_id,user_id,user_role_cd,status_cd) VALUES ('Demo','kvb2','DATA_LDS','A');
INSERT INTO pm_project_user_roles (project_id,user_id,user_role_cd,status_cd) VALUES ('Demo','kvb2','EDITOR','A');
INSERT INTO pm_project_user_roles (project_id,user_id,user_role_cd,status_cd) VALUES ('Demo','kvb2','DATA_PROT','A');

-- add user to ACT project
INSERT INTO pm_project_user_roles (project_id,user_id,user_role_cd,status_cd) VALUES ('ACT','kvb2','USER','A');
INSERT INTO pm_project_user_roles (project_id,user_id,user_role_cd,status_cd) VALUES ('ACT','kvb2','DATA_DEID','A');
INSERT INTO pm_project_user_roles (project_id,user_id,user_role_cd,status_cd) VALUES ('ACT','kvb2','DATA_OBFSC','A');
INSERT INTO pm_project_user_roles (project_id,user_id,user_role_cd,status_cd) VALUES ('ACT','kvb2','DATA_AGG','A');
INSERT INTO pm_project_user_roles (project_id,user_id,user_role_cd,status_cd) VALUES ('ACT','kvb2','DATA_LDS','A');
INSERT INTO pm_project_user_roles (project_id,user_id,user_role_cd,status_cd) VALUES ('ACT','kvb2','EDITOR','A');
INSERT INTO pm_project_user_roles (project_id,user_id,user_role_cd,status_cd) VALUES ('ACT','kvb2','DATA_PROT','A');

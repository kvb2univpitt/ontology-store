-- psql postgresql://postgres:demouser@localhost:5432/i2b2 -f actomp_projects.sql

-- Project
INSERT INTO public.pm_project_data (project_id,project_name,project_wiki,project_key,project_path,project_description,change_date,entry_date,changeby_char,status_cd) VALUES ('ACTOMP','i2b2 ACTOMP','http://www.i2b2.org',NULL,'/ACTOMP',NULL,NULL,NULL,NULL,'A');

-- Project Users
INSERT INTO public.pm_project_user_roles (project_id,user_id,user_role_cd,change_date,entry_date,changeby_char,status_cd) VALUES ('ACTOMP','i2b2','MANAGER',NULL,NULL,NULL,'A');
INSERT INTO public.pm_project_user_roles (project_id,user_id,user_role_cd,change_date,entry_date,changeby_char,status_cd) VALUES ('ACTOMP','i2b2','USER',NULL,NULL,NULL,'A');
INSERT INTO public.pm_project_user_roles (project_id,user_id,user_role_cd,change_date,entry_date,changeby_char,status_cd) VALUES ('ACTOMP','i2b2','DATA_OBFSC',NULL,NULL,NULL,'A');
INSERT INTO public.pm_project_user_roles (project_id,user_id,user_role_cd,change_date,entry_date,changeby_char,status_cd) VALUES ('ACTOMP','demo','USER',NULL,NULL,NULL,'A');
INSERT INTO public.pm_project_user_roles (project_id,user_id,user_role_cd,change_date,entry_date,changeby_char,status_cd) VALUES ('ACTOMP','demo','DATA_DEID',NULL,NULL,NULL,'A');
INSERT INTO public.pm_project_user_roles (project_id,user_id,user_role_cd,change_date,entry_date,changeby_char,status_cd) VALUES ('ACTOMP','demo','DATA_OBFSC',NULL,NULL,NULL,'A');
INSERT INTO public.pm_project_user_roles (project_id,user_id,user_role_cd,change_date,entry_date,changeby_char,status_cd) VALUES ('ACTOMP','demo','DATA_AGG',NULL,NULL,NULL,'A');
INSERT INTO public.pm_project_user_roles (project_id,user_id,user_role_cd,change_date,entry_date,changeby_char,status_cd) VALUES ('ACTOMP','demo','DATA_LDS',NULL,NULL,NULL,'A');
INSERT INTO public.pm_project_user_roles (project_id,user_id,user_role_cd,change_date,entry_date,changeby_char,status_cd) VALUES ('ACTOMP','demo','EDITOR',NULL,NULL,NULL,'A');
INSERT INTO public.pm_project_user_roles (project_id,user_id,user_role_cd,change_date,entry_date,changeby_char,status_cd) VALUES ('ACTOMP','demo','DATA_PROT',NULL,NULL,NULL,'A');

-- Project Datasource Lookup
INSERT INTO public.ont_db_lookup (c_domain_id,c_project_path,c_owner_id,c_db_fullschema,c_db_datasource,c_db_servertype,c_db_nicename,c_db_tooltip,c_comment,c_entry_date,c_change_date,c_status_cd) VALUES ('i2b2demo','ACTOMP/','@','i2b2actomp_ont','java:/OntologyACTOMP','POSTGRESQL','Metadata',NULL,NULL,NULL,NULL,NULL);
INSERT INTO public.crc_db_lookup (c_domain_id,c_project_path,c_owner_id,c_db_fullschema,c_db_datasource,c_db_servertype,c_db_nicename,c_db_tooltip,c_comment,c_entry_date,c_change_date,c_status_cd) VALUES ('i2b2demo','/ACTOMP/','@','i2b2actomp_crc','java:/CRCACTOMP','POSTGRESQL','Demo',NULL,NULL,NULL,NULL,NULL);
INSERT INTO public.work_db_lookup (c_domain_id,c_project_path,c_owner_id,c_db_fullschema,c_db_datasource,c_db_servertype,c_db_nicename,c_db_tooltip,c_comment,c_entry_date,c_change_date,c_status_cd) VALUES ('i2b2demo','ACTOMP/','@','public','java:/WorkplaceDemoDS','POSTGRESQL','Workplace',NULL,NULL,NULL,NULL,NULL);

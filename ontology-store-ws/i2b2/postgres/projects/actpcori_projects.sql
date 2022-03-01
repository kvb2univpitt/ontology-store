-- psql postgresql://postgres:demouser@localhost:5432/i2b2 -f actpcori_projects.sql

-- Project
INSERT INTO public.pm_project_data (project_id,project_name,project_wiki,project_key,project_path,project_description,change_date,entry_date,changeby_char,status_cd) VALUES ('ACTPCORI','i2b2 ACTPCORI','http://www.i2b2.org',NULL,'/ACTPCORI',NULL,NULL,NULL,NULL,'A');

-- Project Users
INSERT INTO public.pm_project_user_roles (project_id,user_id,user_role_cd,change_date,entry_date,changeby_char,status_cd) VALUES ('ACTPCORI','i2b2','MANAGER',NULL,NULL,NULL,'A');
INSERT INTO public.pm_project_user_roles (project_id,user_id,user_role_cd,change_date,entry_date,changeby_char,status_cd) VALUES ('ACTPCORI','i2b2','USER',NULL,NULL,NULL,'A');
INSERT INTO public.pm_project_user_roles (project_id,user_id,user_role_cd,change_date,entry_date,changeby_char,status_cd) VALUES ('ACTPCORI','i2b2','DATA_OBFSC',NULL,NULL,NULL,'A');
INSERT INTO public.pm_project_user_roles (project_id,user_id,user_role_cd,change_date,entry_date,changeby_char,status_cd) VALUES ('ACTPCORI','demo','USER',NULL,NULL,NULL,'A');
INSERT INTO public.pm_project_user_roles (project_id,user_id,user_role_cd,change_date,entry_date,changeby_char,status_cd) VALUES ('ACTPCORI','demo','DATA_DEID',NULL,NULL,NULL,'A');
INSERT INTO public.pm_project_user_roles (project_id,user_id,user_role_cd,change_date,entry_date,changeby_char,status_cd) VALUES ('ACTPCORI','demo','DATA_OBFSC',NULL,NULL,NULL,'A');
INSERT INTO public.pm_project_user_roles (project_id,user_id,user_role_cd,change_date,entry_date,changeby_char,status_cd) VALUES ('ACTPCORI','demo','DATA_AGG',NULL,NULL,NULL,'A');
INSERT INTO public.pm_project_user_roles (project_id,user_id,user_role_cd,change_date,entry_date,changeby_char,status_cd) VALUES ('ACTPCORI','demo','DATA_LDS',NULL,NULL,NULL,'A');
INSERT INTO public.pm_project_user_roles (project_id,user_id,user_role_cd,change_date,entry_date,changeby_char,status_cd) VALUES ('ACTPCORI','demo','EDITOR',NULL,NULL,NULL,'A');
INSERT INTO public.pm_project_user_roles (project_id,user_id,user_role_cd,change_date,entry_date,changeby_char,status_cd) VALUES ('ACTPCORI','demo','DATA_PROT',NULL,NULL,NULL,'A');

-- Project Datasource Lookup
INSERT INTO public.ont_db_lookup (c_domain_id,c_project_path,c_owner_id,c_db_fullschema,c_db_datasource,c_db_servertype,c_db_nicename,c_db_tooltip,c_comment,c_entry_date,c_change_date,c_status_cd) VALUES ('i2b2demo','ACTPCORI/','@','i2b2actpcori_ont','java:/OntologyACTPCORI','POSTGRESQL','Metadata',NULL,NULL,NULL,NULL,NULL);
INSERT INTO public.crc_db_lookup (c_domain_id,c_project_path,c_owner_id,c_db_fullschema,c_db_datasource,c_db_servertype,c_db_nicename,c_db_tooltip,c_comment,c_entry_date,c_change_date,c_status_cd) VALUES ('i2b2demo','/ACTPCORI/','@','i2b2actpcori_crc','java:/CRCACTPCORI','POSTGRESQL','Demo',NULL,NULL,NULL,NULL,NULL);
INSERT INTO public.work_db_lookup (c_domain_id,c_project_path,c_owner_id,c_db_fullschema,c_db_datasource,c_db_servertype,c_db_nicename,c_db_tooltip,c_comment,c_entry_date,c_change_date,c_status_cd) VALUES ('i2b2demo','ACTPCORI/','@','public','java:/WorkplaceDemoDS','POSTGRESQL','Workplace',NULL,NULL,NULL,NULL,NULL);

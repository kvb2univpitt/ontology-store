-- update ACT ontology datasource path
UPDATE ont_db_lookup SET c_db_datasource = 'java:/ACTOntologyDemoDS' WHERE c_project_path = 'ACT/' AND c_owner_id = '@';

-- OntologyStore settings
INSERT INTO hive_cell_params (id,datatype_cd,cell_id,param_name_cd,value,status_cd) VALUES ((SELECT max(id)+1  FROM hive_cell_params),'T','ONTSTORE','ontstore.product.list.url','http://localhost/~kvb2/s3/ontology-store-v2/product-list-local-all.json','A');
INSERT INTO hive_cell_params (id,datatype_cd,cell_id,param_name_cd,value,status_cd) VALUES ((SELECT max(id)+1  FROM hive_cell_params),'T','ONTSTORE','ontstore.dir.download','/home/kvb2/shared/ontology/products-v2','A');

-- ontologystore ds
INSERT INTO crc_db_lookup (c_domain_id,c_project_path,c_owner_id,c_db_fullschema,c_db_datasource,c_db_servertype,c_db_nicename) VALUES ('i2b2demo','/Demo/','ontstore','public','java:/OntstoreDataDS','POSTGRESQL','Demo');
INSERT INTO ont_db_lookup (c_domain_id,c_project_path,c_owner_id,c_db_fullschema,c_db_datasource,c_db_servertype,c_db_nicename) VALUES ('i2b2demo','Demo/','ontstore','public','java:/OntstoreMetadataDS','POSTGRESQL','Metadata');
-- ACT ontologystore ds
INSERT INTO crc_db_lookup (c_domain_id,c_project_path,c_owner_id,c_db_fullschema,c_db_datasource,c_db_servertype,c_db_nicename) VALUES ('i2b2demo','/ACT/','ontstore','public','java:/OntstoreACTDataDS','POSTGRESQL','Demo');
INSERT INTO ont_db_lookup (c_domain_id,c_project_path,c_owner_id,c_db_fullschema,c_db_datasource,c_db_servertype,c_db_nicename) VALUES ('i2b2demo','ACT/','ontstore','i2b2actdata','java:/OntstoreACTMetadataDS','POSTGRESQL','ACT Metadata');
-- ontologystore configurations
INSERT INTO hive_cell_params (id,datatype_cd,cell_id,param_name_cd,value,status_cd) VALUES ((SELECT max(id)+1  FROM hive_cell_params),'T','ONTSTORE','ontstore.product.list.url','http://localhost/~kvb2/s3/ontology-store-v2/product-list-local-all.json','A');
INSERT INTO hive_cell_params (id,datatype_cd,cell_id,param_name_cd,value,status_cd) VALUES ((SELECT max(id)+1  FROM hive_cell_params),'T','ONTSTORE','ontstore.dir.download','/home/kvb2/shared/ontology/products-v2','A');

-- ontologystore ds
INSERT INTO crc_db_lookup (c_domain_id,c_project_path,c_owner_id,c_db_fullschema,c_db_datasource,c_db_servertype,c_db_nicename) VALUES ('i2b2demo','/Demo/','ontstore','public','java:/OntologyStoreDataDS','POSTGRESQL','Demo');
INSERT INTO ont_db_lookup (c_domain_id,c_project_path,c_owner_id,c_db_fullschema,c_db_datasource,c_db_servertype,c_db_nicename) VALUES ('i2b2demo','Demo/','ontstore','public','java:/OntologyStoreMetadataDS','POSTGRESQL','Metadata');
-- ACT ontologystore ds
INSERT INTO crc_db_lookup (c_domain_id,c_project_path,c_owner_id,c_db_fullschema,c_db_datasource,c_db_servertype,c_db_nicename) VALUES ('i2b2demo','/ACT/','ontstore','public','java:/OntologyStoreACTDataDS','POSTGRESQL','Demo');
-- INSERT INTO ont_db_lookup (c_domain_id,c_project_path,c_owner_id,c_db_fullschema,c_db_datasource,c_db_servertype,c_db_nicename) VALUES ('i2b2demo','ACT/','ontstore','i2b2actdata','java:/OntologyStoreACTMetadataDS','POSTGRESQL','ACT Metadata');
INSERT INTO ont_db_lookup (c_domain_id,c_project_path,c_owner_id,c_db_fullschema,c_db_datasource,c_db_servertype,c_db_nicename) VALUES ('i2b2demo','ACT/','ontstore','kevin','java:/OntologyStoreACTMetadataDS','POSTGRESQL','ACT Metadata');

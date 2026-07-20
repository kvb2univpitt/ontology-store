-- OntologyStore settings
INSERT INTO i2b2hive.dbo.HIVE_CELL_PARAMS (ID,DATATYPE_CD,CELL_ID,PARAM_NAME_CD,VALUE,ENTRY_DATE,STATUS_CD) VALUES ((SELECT max(id)+1  FROM hive_cell_params),'T','ONTSTORE','ontstore.product.list.url','http://localhost/~kvb2/s3/ontology-store-v2/product-list-local-all.json',CURRENT_TIMESTAMP,'A');
INSERT INTO i2b2hive.dbo.HIVE_CELL_PARAMS (ID,DATATYPE_CD,CELL_ID,PARAM_NAME_CD,VALUE,ENTRY_DATE,STATUS_CD) VALUES ((SELECT max(id)+1  FROM hive_cell_params),'T','ONTSTORE','ontstore.dir.download','/home/kvb2/shared/ontology/products-v2',CURRENT_TIMESTAMP,'A');

-- ontologystore ds
INSERT INTO i2b2hive.dbo.CRC_DB_LOOKUP (C_DOMAIN_ID,C_PROJECT_PATH,C_OWNER_ID,C_DB_FULLSCHEMA,C_DB_DATASOURCE,C_DB_SERVERTYPE,C_DB_NICENAME,C_ENTRY_DATE) VALUES ('i2b2demo','/Demo/','ontstore','i2b2demodata.dbo','java:/OntologyStoreDataDS','SQLSERVER','Demo',CURRENT_TIMESTAMP);
INSERT INTO i2b2hive.dbo.ONT_DB_LOOKUP (C_DOMAIN_ID,C_PROJECT_PATH,C_OWNER_ID,C_DB_FULLSCHEMA,C_DB_DATASOURCE,C_DB_SERVERTYPE,C_DB_NICENAME,C_ENTRY_DATE) VALUES ('i2b2demo','Demo/','ontstore','i2b2metadata.dbo','java:/OntologyStoreMetadataDS','SQLSERVER','Metadata',CURRENT_TIMESTAMP);
-- ACT ontologystore ds
INSERT INTO i2b2hive.dbo.CRC_DB_LOOKUP (C_DOMAIN_ID,C_PROJECT_PATH,C_OWNER_ID,C_DB_FULLSCHEMA,C_DB_DATASOURCE,C_DB_SERVERTYPE,C_DB_NICENAME,C_ENTRY_DATE) VALUES ('i2b2demo','/ACT/','ontstore','i2b2demodata.dbo','java:/OntologyStoreACTDataDS','SQLSERVER','Demo',CURRENT_TIMESTAMP);
INSERT INTO i2b2hive.dbo.ONT_DB_LOOKUP (C_DOMAIN_ID,C_PROJECT_PATH,C_OWNER_ID,C_DB_FULLSCHEMA,C_DB_DATASOURCE,C_DB_SERVERTYPE,C_DB_NICENAME,C_ENTRY_DATE) VALUES ('i2b2demo','ACT/','ontstore','i2b2actata.dbo','java:/OntologyStoreACTMetadataDS','SQLSERVER','ACT Metadata',CURRENT_TIMESTAMP);

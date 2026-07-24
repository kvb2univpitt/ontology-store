-- update ACT ontology datasource path
UPDATE ONT_DB_LOOKUP SET C_DB_FULLSCHEMA = 'i2b2actata' WHERE C_PROJECT_PATH = 'ACT/';

-- OntologyStore configurations
INSERT INTO HIVE_CELL_PARAMS (ID,DATATYPE_CD,CELL_ID,PARAM_NAME_CD,VALUE,ENTRY_DATE,STATUS_CD) VALUES ((SELECT MAX(id)+1 FROM HIVE_CELL_PARAMS),'T','ONTSTORE','ontstore.product.list.url',TO_CLOB('http://localhost/~kvb2/s3/ontology-store-v2/product-list-local-all.json'),CURRENT_TIMESTAMP,'A');
INSERT INTO HIVE_CELL_PARAMS (ID,DATATYPE_CD,CELL_ID,PARAM_NAME_CD,VALUE,ENTRY_DATE,STATUS_CD) VALUES ((SELECT MAX(id)+1 FROM HIVE_CELL_PARAMS),'T','ONTSTORE','ontstore.dir.download',TO_CLOB('/home/kvb2/shared/ontology/products-v2'),CURRENT_TIMESTAMP,'A');

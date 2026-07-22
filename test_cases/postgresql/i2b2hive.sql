-- update ACT ontology datasource path
UPDATE ont_db_lookup SET c_db_datasource = 'java:/ACTOntologyDemoDS' WHERE c_project_path = 'ACT/' AND c_owner_id = '@';

-- ontologystore configurations
INSERT INTO hive_cell_params (id,datatype_cd,cell_id,param_name_cd,value,status_cd) VALUES ((SELECT max(id)+1  FROM hive_cell_params),'T','ONTSTORE','ontstore.product.list.url','http://localhost/~kvb2/s3/ontology-store-v2/product-list-local-all.json','A');
INSERT INTO hive_cell_params (id,datatype_cd,cell_id,param_name_cd,value,status_cd) VALUES ((SELECT max(id)+1  FROM hive_cell_params),'T','ONTSTORE','ontstore.dir.download','/home/kvb2/shared/ontology/products-v2','A');

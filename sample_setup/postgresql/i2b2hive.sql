-- update ACT ontology datasource path
UPDATE ont_db_lookup SET c_db_datasource = 'java:/ACTOntologyDemoDS' WHERE c_project_path = 'ACT/' AND c_owner_id = '@';

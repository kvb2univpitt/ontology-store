--  change hostname from Docker container name to localhost
 UPDATE PM_CELL_DATA SET url = 'http://localhost:9090/i2b2/services/QueryToolService/' WHERE CELL_ID  = 'CRC';
 UPDATE PM_CELL_DATA SET url = 'http://localhost:9090/i2b2/services/FRService/' WHERE CELL_ID  = 'FRC';
 UPDATE PM_CELL_DATA SET url = 'http://localhost:9090/i2b2/services/OntologyService/' WHERE CELL_ID  = 'ONT';
 UPDATE PM_CELL_DATA SET url = 'http://localhost:9090/i2b2/services/WorkplaceService/' WHERE CELL_ID  = 'WORK';
 UPDATE PM_CELL_DATA SET url = 'http://localhost:9090/i2b2/services/IMService/' WHERE CELL_ID  = 'IM';

-- psql postgresql://postgres:demouser@localhost:5432/i2b2 -f schemas_tables.sql

CREATE SCHEMA AUTHORIZATION i2b2actdx_ont;
CREATE SCHEMA AUTHORIZATION i2b2actpcori_ont;
CREATE SCHEMA AUTHORIZATION i2b2actomp_ont;
CREATE SCHEMA AUTHORIZATION i2b2actdx_crc;
CREATE SCHEMA AUTHORIZATION i2b2actpcori_crc;
CREATE SCHEMA AUTHORIZATION i2b2actomp_crc;

CREATE TABLE i2b2actdx_ont.TABLE_ACCESS (C_TABLE_CD VARCHAR(50) NOT NULL,C_TABLE_NAME VARCHAR(50) NOT NULL,C_PROTECTED_ACCESS CHAR(1) NULL,C_ONTOLOGY_PROTECTION TEXT NULL,C_HLEVEL INT NOT NULL,C_FULLNAME VARCHAR(700) NOT NULL,C_NAME VARCHAR(2000) NOT NULL,C_SYNONYM_CD CHAR(1) NOT NULL,C_VISUALATTRIBUTES CHAR(3) NOT NULL,C_TOTALNUM INT NULL,C_BASECODE VARCHAR(50) NULL,C_METADATAXML TEXT NULL,C_FACTTABLECOLUMN VARCHAR(50) NOT NULL,C_DIMTABLENAME VARCHAR(50) NOT NULL,C_COLUMNNAME VARCHAR(50) NOT NULL,C_COLUMNDATATYPE VARCHAR(50) NOT NULL,C_OPERATOR VARCHAR(10) NOT NULL,C_DIMCODE VARCHAR(700) NOT NULL,C_COMMENT TEXT NULL,C_TOOLTIP VARCHAR(900) NULL,C_ENTRY_DATE timestamp NULL,C_CHANGE_DATE timestamp NULL,C_STATUS_CD CHAR(1) NULL,VALUETYPE_CD VARCHAR(50) NULL);
CREATE TABLE i2b2actdx_ont.SCHEMES (C_KEY VARCHAR(50) NOT NULL,C_NAME VARCHAR(50) NOT NULL,C_DESCRIPTION VARCHAR(100) NULL,CONSTRAINT SCHEMES_PK PRIMARY KEY(C_KEY));
GRANT ALL PRIVILEGES ON table i2b2actdx_ont.TABLE_ACCESS  TO i2b2actdx_ont;
GRANT ALL PRIVILEGES ON table i2b2actdx_ont.SCHEMES TO i2b2actdx_ont;

CREATE TABLE i2b2actpcori_ont.TABLE_ACCESS (C_TABLE_CD VARCHAR(50) NOT NULL,C_TABLE_NAME VARCHAR(50) NOT NULL,C_PROTECTED_ACCESS CHAR(1) NULL,C_ONTOLOGY_PROTECTION TEXT NULL,C_HLEVEL INT NOT NULL,C_FULLNAME VARCHAR(700) NOT NULL,C_NAME VARCHAR(2000) NOT NULL,C_SYNONYM_CD CHAR(1) NOT NULL,C_VISUALATTRIBUTES CHAR(3) NOT NULL,C_TOTALNUM INT NULL,C_BASECODE VARCHAR(50) NULL,C_METADATAXML TEXT NULL,C_FACTTABLECOLUMN VARCHAR(50) NOT NULL,C_DIMTABLENAME VARCHAR(50) NOT NULL,C_COLUMNNAME VARCHAR(50) NOT NULL,C_COLUMNDATATYPE VARCHAR(50) NOT NULL,C_OPERATOR VARCHAR(10) NOT NULL,C_DIMCODE VARCHAR(700) NOT NULL,C_COMMENT TEXT NULL,C_TOOLTIP VARCHAR(900) NULL,C_ENTRY_DATE timestamp NULL,C_CHANGE_DATE timestamp NULL,C_STATUS_CD CHAR(1) NULL,VALUETYPE_CD VARCHAR(50) NULL);
CREATE TABLE i2b2actpcori_ont.SCHEMES (C_KEY VARCHAR(50) NOT NULL,C_NAME VARCHAR(50) NOT NULL,C_DESCRIPTION VARCHAR(100) NULL,CONSTRAINT SCHEMES_PK PRIMARY KEY(C_KEY));
GRANT ALL PRIVILEGES ON table i2b2actpcori_ont.TABLE_ACCESS  TO i2b2actpcori_ont;
GRANT ALL PRIVILEGES ON table i2b2actpcori_ont.SCHEMES TO i2b2actpcori_ont;

CREATE TABLE i2b2actomp_ont.TABLE_ACCESS (C_TABLE_CD VARCHAR(50) NOT NULL,C_TABLE_NAME VARCHAR(50) NOT NULL,C_PROTECTED_ACCESS CHAR(1) NULL,C_ONTOLOGY_PROTECTION TEXT NULL,C_HLEVEL INT NOT NULL,C_FULLNAME VARCHAR(700) NOT NULL,C_NAME VARCHAR(2000) NOT NULL,C_SYNONYM_CD CHAR(1) NOT NULL,C_VISUALATTRIBUTES CHAR(3) NOT NULL,C_TOTALNUM INT NULL,C_BASECODE VARCHAR(50) NULL,C_METADATAXML TEXT NULL,C_FACTTABLECOLUMN VARCHAR(50) NOT NULL,C_DIMTABLENAME VARCHAR(50) NOT NULL,C_COLUMNNAME VARCHAR(50) NOT NULL,C_COLUMNDATATYPE VARCHAR(50) NOT NULL,C_OPERATOR VARCHAR(10) NOT NULL,C_DIMCODE VARCHAR(700) NOT NULL,C_COMMENT TEXT NULL,C_TOOLTIP VARCHAR(900) NULL,C_ENTRY_DATE timestamp NULL,C_CHANGE_DATE timestamp NULL,C_STATUS_CD CHAR(1) NULL,VALUETYPE_CD VARCHAR(50) NULL);
CREATE TABLE i2b2actomp_ont.SCHEMES (C_KEY VARCHAR(50) NOT NULL,C_NAME VARCHAR(50) NOT NULL,C_DESCRIPTION VARCHAR(100) NULL,CONSTRAINT SCHEMES_PK PRIMARY KEY(C_KEY));
GRANT ALL PRIVILEGES ON table i2b2actomp_ont.TABLE_ACCESS  TO i2b2actomp_ont;
GRANT ALL PRIVILEGES ON table i2b2actomp_ont.SCHEMES TO i2b2actomp_ont;

CREATE TABLE i2b2actdx_crc.CONCEPT_DIMENSION (CONCEPT_PATH VARCHAR(700) NOT NULL,CONCEPT_CD VARCHAR(50) NULL,NAME_CHAR VARCHAR(2000) NULL,CONCEPT_BLOB TEXT NULL,UPDATE_DATE TIMESTAMP NULL,DOWNLOAD_DATE TIMESTAMP NULL,IMPORT_DATE TIMESTAMP NULL,SOURCESYSTEM_CD VARCHAR(50) NULL,UPLOAD_ID INT NULL,CONSTRAINT CONCEPT_DIMENSION_PK PRIMARY KEY(CONCEPT_PATH));
CREATE TABLE i2b2actdx_crc.QT_BREAKDOWN_PATH (NAME VARCHAR(100),VALUE VARCHAR(2000),CREATE_DATE TIMESTAMP,UPDATE_DATE TIMESTAMP,USER_ID VARCHAR(50));
GRANT ALL PRIVILEGES ON table i2b2actdx_crc.CONCEPT_DIMENSION  TO i2b2actdx_crc;
GRANT ALL PRIVILEGES ON table i2b2actdx_crc.QT_BREAKDOWN_PATH TO i2b2actdx_crc;

CREATE TABLE i2b2actpcori_crc.CONCEPT_DIMENSION (CONCEPT_PATH VARCHAR(700) NOT NULL,CONCEPT_CD VARCHAR(50) NULL,NAME_CHAR VARCHAR(2000) NULL,CONCEPT_BLOB TEXT NULL,UPDATE_DATE TIMESTAMP NULL,DOWNLOAD_DATE TIMESTAMP NULL,IMPORT_DATE TIMESTAMP NULL,SOURCESYSTEM_CD VARCHAR(50) NULL,UPLOAD_ID INT NULL,CONSTRAINT CONCEPT_DIMENSION_PK PRIMARY KEY(CONCEPT_PATH));
CREATE TABLE i2b2actpcori_crc.QT_BREAKDOWN_PATH (NAME VARCHAR(100),VALUE VARCHAR(2000),CREATE_DATE TIMESTAMP,UPDATE_DATE TIMESTAMP,USER_ID VARCHAR(50));
GRANT ALL PRIVILEGES ON table i2b2actpcori_crc.CONCEPT_DIMENSION  TO i2b2actpcori_crc;
GRANT ALL PRIVILEGES ON table i2b2actpcori_crc.QT_BREAKDOWN_PATH TO i2b2actpcori_crc;

CREATE TABLE i2b2actomp_crc.CONCEPT_DIMENSION (CONCEPT_PATH VARCHAR(700) NOT NULL,CONCEPT_CD VARCHAR(50) NULL,NAME_CHAR VARCHAR(2000) NULL,CONCEPT_BLOB TEXT NULL,UPDATE_DATE TIMESTAMP NULL,DOWNLOAD_DATE TIMESTAMP NULL,IMPORT_DATE TIMESTAMP NULL,SOURCESYSTEM_CD VARCHAR(50) NULL,UPLOAD_ID INT NULL,CONSTRAINT CONCEPT_DIMENSION_PK PRIMARY KEY(CONCEPT_PATH));
CREATE TABLE i2b2actomp_crc.QT_BREAKDOWN_PATH (NAME VARCHAR(100),VALUE VARCHAR(2000),CREATE_DATE TIMESTAMP,UPDATE_DATE TIMESTAMP,USER_ID VARCHAR(50));
GRANT ALL PRIVILEGES ON table i2b2actomp_crc.CONCEPT_DIMENSION  TO i2b2actomp_crc;
GRANT ALL PRIVILEGES ON table i2b2actomp_crc.QT_BREAKDOWN_PATH TO i2b2actomp_crc;

CREATE INDEX CD_IDX_UPLOADID ON i2b2actdx_crc.CONCEPT_DIMENSION(UPLOAD_ID);
CREATE INDEX CD_IDX_UPLOADID ON i2b2actpcori_crc.CONCEPT_DIMENSION(UPLOAD_ID);
CREATE INDEX CD_IDX_UPLOADID ON i2b2actomp_crc.CONCEPT_DIMENSION(UPLOAD_ID);

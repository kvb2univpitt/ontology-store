CREATE TABLE I2B2 (
    "C_HLEVEL" NUMBER(22,0) NOT NULL,
    "C_FULLNAME" VARCHAR2(700) NOT NULL,
    "C_NAME" VARCHAR2(2000) NOT NULL,
    "C_SYNONYM_CD" CHAR(1) NOT NULL,
    "C_VISUALATTRIBUTES" CHAR(3) NOT NULL,
    "C_TOTALNUM" NUMBER(22,0) NULL,
    "C_BASECODE" VARCHAR2(50) NULL,
    "C_METADATAXML" CLOB NULL,
    "C_FACTTABLECOLUMN" VARCHAR2(50) NOT NULL,
    "C_TABLENAME" VARCHAR2(50) NOT NULL,
    "C_COLUMNNAME" VARCHAR2(50) NOT NULL,
    "C_COLUMNDATATYPE" VARCHAR2(50) NOT NULL,
    "C_OPERATOR" VARCHAR2(10) NOT NULL,
    "C_DIMCODE" VARCHAR2(700) NOT NULL,
    "C_COMMENT" CLOB NULL,
    "C_TOOLTIP" VARCHAR2(900) NULL,
    "M_APPLIED_PATH" VARCHAR2(700) NOT NULL,
    "UPDATE_DATE" DATE NOT NULL,
    "DOWNLOAD_DATE" DATE NULL,
    "IMPORT_DATE" DATE NULL,
    "SOURCESYSTEM_CD" VARCHAR2(50) NULL,
    "VALUETYPE_CD" VARCHAR2(50) NULL,
    "M_EXCLUSION_CD" VARCHAR2(25) NULL,
    "C_PATH" VARCHAR2(700) NULL,
    "C_SYMBOL" VARCHAR2(50) NULL
)

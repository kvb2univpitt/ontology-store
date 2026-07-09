CREATE TABLE i2b2actdata.schemes (
	c_key varchar(50) NOT NULL,
	c_name varchar(50) NOT NULL,
	c_description varchar(100) NULL,
	CONSTRAINT act_schemes_pk PRIMARY KEY (c_key)
);

CREATE TABLE i2b2actdata.table_access (
	c_table_cd varchar(50) NOT NULL,
	c_table_name varchar(50) NOT NULL,
	c_protected_access bpchar(1) NULL,
	c_ontology_protection text NULL,
	c_hlevel int4 NOT NULL,
	c_fullname varchar(700) NOT NULL,
	c_name varchar(2000) NOT NULL,
	c_synonym_cd bpchar(1) NOT NULL,
	c_visualattributes bpchar(3) NOT NULL,
	c_totalnum int4 NULL,
	c_basecode varchar(50) NULL,
	c_metadataxml text NULL,
	c_facttablecolumn varchar(50) NOT NULL,
	c_dimtablename varchar(50) NOT NULL,
	c_columnname varchar(50) NOT NULL,
	c_columndatatype varchar(50) NOT NULL,
	c_operator varchar(10) NOT NULL,
	c_dimcode varchar(700) NOT NULL,
	c_comment text NULL,
	c_tooltip varchar(900) NULL,
	c_entry_date timestamp NULL,
	c_change_date timestamp NULL,
	c_status_cd bpchar(1) NULL,
	valuetype_cd varchar(50) NULL
);

CREATE TABLE i2b2actdata.qt_breakdown_path (
	name varchar(100) NULL,
	value text NULL,
	create_date timestamp NULL,
	update_date timestamp NULL,
	user_id varchar(50) NULL
);

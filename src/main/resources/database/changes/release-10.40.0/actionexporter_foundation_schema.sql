
SET SCHEMA 'actionexporter';

CREATE TABLE actionrequest (
  actionrequestPK 	bigint NOT NULL,
  actionId 		uuid   NOT NULL,
  responserequired 	boolean DEFAULT FALSE,
  actionplanname 	character varying(100),
  actiontypename	character varying(100) NOT NULL,
  questionset 		character varying(10),
  contactFK		bigint,
  sampleunitrefFK	character varying(20) NOT NULL,
  caseId 		uuid NOT NULL,
  priority              character varying(10),
  caseref               character varying(16),
  iac                   character varying (24) NOT NULL,
  dateStored            timestamp with time zone,
  dateSent              timestamp with time zone
);

CREATE TABLE address (
  sampleunitrefPK	character varying(20) NOT NULL,
  addresstype           character varying(6),
  estabtype             character varying(6),
  category 		character varying(20),
  organisation_name 	character varying(60),
  address_line1 	character varying(60),
  address_line2 	character varying(60),  
  locality 		character varying(35),
  town_name 		character varying(30),
  postcode 		character varying(8),
  lad 			character varying(9),
  latitude 		double precision,
  longitude 		double precision
);


CREATE TABLE actionexporter.filerowcount
(
 filename character varying(100) NOT NULL,
 rowcount integer NOT NULL,
 datesent timestamp with time zone NOT NULL,
 reported boolean NOT NULL
);


CREATE SEQUENCE contactPKseq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 999999999999
    CACHE 1;


CREATE SEQUENCE actionrequestPKseq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 999999999999
    CACHE 1;


CREATE TABLE templatemapping (
  actiontypenamePK	character varying(100) NOT NULL,
  templatenameFK        character varying(100) NOT NULL,
  file 			character varying(100) NOT NULL,
  datemodified 		timestamp with time zone
);


CREATE TABLE template (
  templatenamePK        character varying(100) NOT NULL,
  content 		text NOT NULL,
  datemodified 		timestamp with time zone
);

CREATE TABLE contact (
    contactPK 		integer DEFAULT nextval('contactPKseq'::regclass) NOT NULL,
    forename 		character varying(35),
    surname 		character varying(35),
    phonenumber 	character varying(20),
    emailaddress 	character varying(50),
    title 		character varying(20)
);



ALTER TABLE ONLY address         ADD CONSTRAINT sampleunitrefPK_pkey      PRIMARY KEY (sampleunitrefPK);
ALTER TABLE ONLY actionrequest   ADD CONSTRAINT actionrequestPK_pkey      PRIMARY KEY (actionrequestPK);
ALTER TABLE ONLY templatemapping ADD CONSTRAINT actiontypenamePK_pkey     PRIMARY KEY (actiontypenamePK);
ALTER TABLE ONLY template        ADD CONSTRAINT templetenamePK_pkey       PRIMARY KEY (templatenamePK);
ALTER TABLE ONLY contact         ADD CONSTRAINT contactPK_pkey            PRIMARY KEY (contactPK);


ALTER TABLE ONLY actionrequest   ADD CONSTRAINT sampleunitrefFK_fkey    FOREIGN KEY (sampleunitrefFK) REFERENCES address(sampleunitrefPK);
ALTER TABLE ONLY actionrequest   ADD CONSTRAINT contactFK_fkey          FOREIGN KEY (contactFK)       REFERENCES contact(contactPK);
ALTER TABLE ONLY templatemapping ADD CONSTRAINT templatenameFK_fkey     FOREIGN KEY (templatenameFK)  REFERENCES template(templatenamePK);

CREATE INDEX actionrequest_sampleunitrefFK_index on actionexporter.actionrequest(sampleunitrefFK);
CREATE INDEX actionrequest_contactFK_index on actionexporter.actionrequest(contactFK);




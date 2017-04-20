--- Update 1.0
--- Changes due to update of MetadataSchema entity
-- ALTER TABLE metadataschema RENAME COLUMN IF EXISTS metadataschemaid TO id;
-- ALTER TABLE investigation_metadataschema RENAME COLUMN IF EXISTS metadataschema_metadataschemaid TO metadataschema_id;
-- ALTER TABLE metadataschema RENAME COLUMN IF EXISTS metadataschema TO metadataschemaurl;
-- ALTER TABLE metadataschema ADD COLUMN IF NOT EXISTS schemaidentifier varchar(255) NOT NULL UNIQUE;

--- Update 1.0build25
--- Changes due to Staging 2.0 integration
-- Necessary since Staging 2.0
-- CREATE TABLE stagingaccessmethodconfiguration
-- (
-- id serial NOT NULL,
--  customproperties character varying(1024),
--  defaultmethod boolean,
--  description character varying(1024),
--  disabled boolean,
--  groupid character varying(255),
--  implementationclass character varying(255),
--  localbasepath character varying(255),
--  "name" character varying(255),
--  remotebaseurl character varying(255),
--  transientmethod boolean,
--  uniqueidentifier character varying(255) NOT NULL,
--  CONSTRAINT stagingaccessmethodconfiguration_pkey PRIMARY KEY (id),
--  CONSTRAINT stagingaccessmethodconfiguration_uniqueidentifier_key UNIQUE (uniqueidentifier)
--)
--WITH (
--  OIDS=FALSE
--);

-- Add default entry 
-- Feel free to use more fancy stuff, e.g. read data from old table 'stagingaccessmethod'. 
-- For legacy entries, the implementationclass 'edu.kit.dama.rest.staging.services.impl.ap.BasicStagingAccessMethod' should be used without any customproperties or groupid.
--INSERT INTO stagingaccessmethodconfiguration(
--			id, customproperties, defaultmethod, description, disabled, groupid, 
--            implementationclass, localbasepath, "name", remotebaseurl, transientmethod, 
--            uniqueidentifier)
--    VALUES (1, NULL, TRUE, 'Simple WebDAV access', FALSE, NULL, 
--            'edu.kit.dama.rest.staging.services.impl.ap.BasicStagingAccessMethod', '/opt/liferay/KDM/davdir_users/', 'DavDirUsers', 'http://dama-VirtualBox/webdav/', FALSE, 
--            'ebc5a918-0c3c-4dbe-b8fe-babf50ac3459');

-- Not longer needed if the stuff above worked
--DROP TABLE stagingaccessproviderconfiguration_stagingaccessmethod;
-- Not longer needed if the stuff above worked
--DROP TABLE stagingaccessmethod;

--- Update 1.0buildXXXX
--- Changes due to renaming of StagingAccessMethod to StagingAccessPoint
--Update StagingAccessMethodConfiguration table
ALTER TABLE stagingaccessmethodconfiguration RENAME COLUMN transientmethod TO transientaccesspoint;
ALTER TABLE stagingaccessmethodconfiguration RENAME COLUMN defaultmethod TO defaultaccesspoint;
ALTER TABLE stagingaccessmethodconfiguration RENAME TO stagingaccesspointconfiguration;
--Update existing data
UPDATE stagingaccesspointconfiguration SET implementationclass='edu.kit.dama.rest.staging.services.impl.ap.BasicStagingAccessPoint' WHERE implementationClass='edu.kit.dama.rest.staging.services.impl.ap.BasicStagingAccessMethod';
--Update Ingest- and DownloadInformationTable
ALTER TABLE ingestinformation ADD COLUMN groupuuid character varying(255);
ALTER TABLE ingestinformation RENAME COLUMN accessmethodid TO accesspointid;
ALTER TABLE downloadinformation ADD COLUMN groupuuid character varying(255);
ALTER TABLE downloadinformation RENAME COLUMN accessmethodid TO accesspointid;
--Update DataOrganizationNodeTable
ALTER TABLE dataorganizationnode ADD COLUMN viewname character varying(255);
ALTER TABLE dataorganizationnode ALTER COLUMN viewname SET DEFAULT 'default';
UPDATE dataorganizationnode SET viewname='default' WHERE nodedepth>-1;
ALTER TABLE attribute ADD COLUMN viewname character varying(255);
ALTER TABLE attribute ALTER COLUMN viewname SET DEFAULT 'default';
UPDATE attribute SET viewname='default' WHERE id>-1;


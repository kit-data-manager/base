--- Update 1.0
--- Changes due to update of MetadataSchema entity
-- ALTER TABLE metadataschema RENAME COLUMN IF EXISTS metadataschemaid TO id;
-- ALTER TABLE investigation_metadataschema RENAME COLUMN IF EXISTS metadataschema_metadataschemaid TO metadataschema_id;
-- ALTER TABLE metadataschema RENAME COLUMN IF EXISTS metadataschema TO metadataschemaurl;
-- ALTER TABLE metadataschema ADD COLUMN IF NOT EXISTS schemaidentifier varchar(255) NOT NULL UNIQUE;

--- Update 1.0build25
--- Changes due to Staging 2.0 integration
-- Necessary since Staging 2.0
 CREATE TABLE stagingaccessmethodconfiguration
 (
 id serial NOT NULL,
  customproperties character varying(1024),
  defaultmethod boolean,
  description character varying(1024),
  disabled boolean,
  groupid character varying(255),
  implementationclass character varying(255),
  localbasepath character varying(255),
  "name" character varying(255),
  remotebaseurl character varying(255),
  transientmethod boolean,
  uniqueidentifier character varying(255) NOT NULL,
  CONSTRAINT stagingaccessmethodconfiguration_pkey PRIMARY KEY (id),
  CONSTRAINT stagingaccessmethodconfiguration_uniqueidentifier_key UNIQUE (uniqueidentifier)
)
WITH (
  OIDS=FALSE
);

-- Add default entry 
-- Feel free to use more fancy stuff, e.g. read data from old table 'stagingaccessmethod'. 
-- For legacy entries, the implementationclass 'edu.kit.dama.rest.staging.services.impl.ap.BasicStagingAccessMethod' should be used without any customproperties or groupid.
INSERT INTO stagingaccessmethodconfiguration(
			id, customproperties, defaultmethod, description, disabled, groupid, 
            implementationclass, localbasepath, "name", remotebaseurl, transientmethod, 
            uniqueidentifier)
    VALUES (1, NULL, TRUE, 'Simple WebDAV access', FALSE, NULL, 
            'edu.kit.dama.rest.staging.services.impl.ap.BasicStagingAccessMethod', '/opt/liferay/KDM/davdir_users/', 'DavDirUsers', 'http://dama-VirtualBox/webdav/', FALSE, 
            'ebc5a918-0c3c-4dbe-b8fe-babf50ac3459');

-- Not longer needed if the stuff above worked
DROP TABLE stagingaccessproviderconfiguration_stagingaccessmethod;
-- Not longer needed if the stuff above worked
DROP TABLE stagingaccessmethod;

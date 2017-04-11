-- Add namespace column to metadata schema table
ALTER TABLE MetaDataSchema ADD COLUMN namespace character varying(255) NOT NULL DEFAULT '';
-- Add discriminator column to digital object including default value 'BASIC' for the default DigitalObject implementation
ALTER TABLE DigitalObject ADD COLUMN obj_type character varying(31) NOT NULL DEFAULT 'DEFAULT';
-- Add priority column to staging processor table
ALTER TABLE StagingProcessor ADD COLUMN priority smallint NOT NULL DEFAULT 0;

-- Fix type in DataWorkflowTask table
ALTER TABLE DataWorkflowTask RENAME COLUMN outputDirectoryrUrl to outputDirectoryUrl;

-- Add user WORLD to Authorization users table
INSERT INTO users VALUES (nextval('users_id_seq'), 3, 'WORLD');
-- Add group WORLD to Authorization groups table
INSERT INTO groups VALUES (nextval('groups_id_seq'), 'WORLD');

-- Add membership for user WORLD in group WORLD with role 3 (Guest)
INSERT INTO memberships VALUES (nextval('memberships_id_seq'), 3, currval('groups_id_seq'), currval('users_id_seq'));

-- Add OAI-PMH user to Authorization users table
INSERT INTO users VALUES (nextval('users_id_seq'), 3, 'OAI-PMH');
-- Add membership for user OAI-PMH in group 1 (USERS) with role 3 (Guest)
INSERT INTO memberships VALUES (nextval('memberships_id_seq'), 3, 1, currval('users_id_seq'));
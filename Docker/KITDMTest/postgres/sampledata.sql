--
-- KIT Data Manager sample data
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = public, pg_catalog;

-- Add administrator user
INSERT INTO userdata VALUES (nextval('userdata_userid_seq'), 'admin', 'dama@kit.edu', 'System', 'Administrator', NULL, NULL);

-- Add AdminUI login for administrator (Password: dama14)
INSERT INTO serviceaccesstoken VALUES (nextval('serviceaccesstoken_id_seq'), 'mainLogin', 'dama@kit.edu', 'BgJZyjabg7fGkG009+20sw==', 'admin');

-- Add Webdav login for administrator (Password: dama14)
INSERT INTO serviceaccesstoken VALUES (nextval('serviceaccesstoken_id_seq'), 'webdav', 'admin', '8a9a61c50e48d860c5a7618625a7e22f', 'admin');

-- Link admin to Authorization users table
INSERT INTO users VALUES (nextval('users_id_seq'), 12, 'admin');
-- Link user WORLD to Authorization users table
INSERT INTO users VALUES (nextval('users_id_seq'), 3, 'WORLD');
-- Link OAI-PMH user to Authorization users table
INSERT INTO users VALUES (nextval('users_id_seq'), 3, 'OAI-PMH');

-- Add default group with unique identifier 'USERS' and 'PUBLIC'
INSERT INTO groups VALUES (nextval('groups_id_seq'), 'USERS');
-- Link Authorization group to metadata group table
INSERT INTO usergroup VALUES (nextval('usergroup_id_seq'), 'Default group containing all users.', 'USERS', 'All Users');

-- Insert some sample tasks
INSERT INTO task VALUES (nextval('task_taskid_seq'), 'Management');
INSERT INTO task VALUES (nextval('task_taskid_seq'), 'Data Aquisition');
INSERT INTO task VALUES (nextval('task_taskid_seq'), 'Data Analysis');

-- Add membership for user 1 (admin) in group 1 (USERS) with role 12 (Administrator)
INSERT INTO memberships VALUES (nextval('memberships_id_seq'), 12, 1, 1);
-- Add membership for user 2 (WORLD) in group 1 (USERS) with role 3 (Guest)
INSERT INTO memberships VALUES (nextval('memberships_id_seq'), 3, 1, 2);
-- Add membership for user 2 (OAI-PMH) in group 1 (USERS) with role 3 (Guest)
INSERT INTO memberships VALUES (nextval('memberships_id_seq'), 3, 1, 2);


-- Add sample organization unit
INSERT INTO organizationunit VALUES (nextval('organizationunit_organizationunitid_seq'), 'Hermann-von-Helmholtz Platz 1', 'Eggenstein-Leopoldshafen', 'Germany', 'Karlsruhe Institute of Technology', 'http://www.kit.edu', '76344', 1);

-- Insert service access token for admin in order to enable REST service access (admin/dama14 [only valid if using default global secret in datamanager.xml])
INSERT INTO serviceaccesstoken VALUES (nextval('serviceaccesstoken_id_seq'), 'restServiceAccess', 'admin', 'BgJZyjabg7fGkG009+20sw==', 'admin');

-- Insert base metadata schemas for Dublin Core metadata and KIT Data Manager Base Metadata
INSERT INTO metadataschema VALUES (nextval('metadataschema_id_seq'), 'http://www.openarchives.org/OAI/2.0/oai_dc.xsd', 'oai_dc', 'http://www.openarchives.org/OAI/2.0/oai_dc/');
INSERT INTO metadataschema VALUES (nextval('metadataschema_id_seq'), 'http://datamanager.kit.edu/dama/basemetadata/2015-08/basemetadata.xsd', 'bmd','http://datamanager.kit.edu/dama/basemetadata/');
INSERT INTO metadataschema VALUES (nextval('metadataschema_id_seq'), 'http://www.loc.gov/standards/mets/mets.xsd', 'mets','http://www.loc.gov/METS/');


-- Insert StagingAccessPointConfiguration (used for PubRepo instance)
INSERT INTO stagingaccesspointconfiguration VALUES (nextval('stagingaccesspointconfiguration_id_seq'), NULL, TRUE, NULL, FALSE, NULL, 'edu.kit.dama.staging.ap.impl.BasicStagingAccessPoint', '/webdav/', 'WebDav', 'http://localhost:8889/webdav/', FALSE, '0000-0000-0000-0000');
--INSERT INTO stagingprocessor VALUES (nextval('stagingprocessor_id_seq'), TRUE, NULL, FALSE, NULL, 'edu.kit.dama.ui.pubrepo.staging.PublicationDataZIPProcessor', 'Publication Data Processor',NULL, 'SERVER_SIDE_ONLY', '0000-0000-0000-0000');

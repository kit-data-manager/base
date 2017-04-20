-- Add ingest- and downloadProcessingSupported columns to staging processor table
ALTER TABLE StagingProcessor ADD COLUMN ingestProcessingSupported boolean;
ALTER TABLE StagingProcessor ADD COLUMN downloadProcessingSupported boolean;

-- Set for all currently existing processors ingest supported TRUE
UPDATE StagingProcessor SET ingestProcessingSupported = TRUE, downloadProcessingSupported = FALSE WHERE TRUE; 
-- Add cascade update to data organization attribute table in order to allow easy renaming of views
ALTER TABLE ONLY attribute DROP  CONSTRAINT fk_attribute_stepnoarrived;                                                                                                                                                                                                
ALTER TABLE ONLY attribute ADD CONSTRAINT fk_attribute_stepnoarrived FOREIGN KEY (stepnoarrived, viewname, digit_obj_id) REFERENCES dataorganizationnode(stepnoarrived, viewname, digit_obj_id) ON UPDATE CASCADE;
-- Fix typo
ALTER TABLE ExecutionEnvironmentProperty RENAME COLUMN decription TO description;
-- Fix due to refactoring of LFNImpl 
UPDATE DataOrganizationNode SET fullyqualifiedtypename='edu.kit.dama.mdm.dataorganization.impl.staging.LFNImpl' WHERE fullyqualifiedtypename='edu.kit.dama.staging.entities.LFNImpl';

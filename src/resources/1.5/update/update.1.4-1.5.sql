-- Remove old constraint from attribute table
ALTER TABLE ONLY attribute DROP CONSTRAINT fk_attribute_stepnoarrived;
-- Add primary key constraint and ON UPDATE CASCADE constraint
ALTER TABLE ONLY attribute ADD CONSTRAINT attribute_pkey PRIMARY KEY (id, digit_obj_id, viewname, stepnoarrived);  
ALTER TABLE ONLY attribute ADD CONSTRAINT fk_attribute_stepnoarrived FOREIGN KEY (stepnoarrived, viewname, digit_obj_id) REFERENCES dataorganizationnode(stepnoarrived, viewname, digit_obj_id) ON UPDATE CASCADE;
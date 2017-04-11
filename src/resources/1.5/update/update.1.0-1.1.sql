-- Create sequence tables for ingest- and downloadinformation
CREATE SEQUENCE downloadinformation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

CREATE SEQUENCE ingestinformation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

-- Change ownership and change id generation type	
ALTER SEQUENCE downloadinformation_id_seq OWNED BY downloadinformation.id;
ALTER SEQUENCE ingestinformation_id_seq OWNED BY ingestinformation.id;
ALTER TABLE ingestinformation ALTER COLUMN id SET DEFAULT nextval('ingestinformation_id_seq'::regclass);
ALTER TABLE downloadinformation ALTER COLUMN id SET DEFAULT nextval('downloadinformation_id_seq'::regclass);

-- Delete all values from ingest- and downloadinformation tables.
-- This is highly recommended as generated sequence numbers will start with 1. Therefor, generated ids will 
-- clash sooner or later with ids assigned before.
DELETE FROM IngestInformation WHERE true;
DELETE FROM DownloadInformation WHERE true;


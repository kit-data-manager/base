---------------------------------------------------------------------
-- Base schema file for KIT Data Manager based repository systems.
-- This file holds the very basic tables that have to be created 
-- during installation. All other tables that are part of the 
-- repository metadata model are created automatically as soon as 
-- they are used the first time.
--
-- For schema updates please refer to the update script provided with 
-- your KIT Data Manager update package.
---------------------------------------------------------------------


------------------------------------------
-- User/Group/Memberships related tables. 
------------------------------------------

-- AAI-related users, their unique userIds and max. role
CREATE TABLE users (
    id integer NOT NULL,
    maximumrole integer,
    userid character varying(255) NOT NULL
);

CREATE SEQUENCE users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER SEQUENCE users_id_seq OWNED BY users.id;
ALTER TABLE ONLY users ADD CONSTRAINT users_pkey PRIMARY KEY (id);
ALTER TABLE ONLY users ADD CONSTRAINT users_userid_key UNIQUE (userid);
ALTER TABLE users ALTER COLUMN id SET DEFAULT nextval('users_id_seq'::regclass);

-- UserData providing additional user information, e.g. email and names.
CREATE TABLE userdata (
    userid integer NOT NULL,
    distinguishedname character varying(255),
    email character varying(255),
    firstname character varying(255),
    lastname character varying(255),
    validfrom timestamp without time zone,
    validuntil timestamp without time zone
);

CREATE SEQUENCE userdata_userid_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER SEQUENCE userdata_userid_seq OWNED BY userdata.userid;
ALTER TABLE ONLY userdata ADD CONSTRAINT userdata_pkey PRIMARY KEY (userid);
ALTER TABLE userdata ALTER COLUMN userid SET DEFAULT nextval('userdata_userid_seq'::regclass);

-- AAI-related user groups and their unique groupIds
CREATE TABLE groups (
    id integer NOT NULL,
    groupid character varying(255) NOT NULL
);

CREATE SEQUENCE groups_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER SEQUENCE groups_id_seq OWNED BY groups.id;
ALTER TABLE ONLY groups ADD CONSTRAINT groups_groupid_key UNIQUE (groupid);
ALTER TABLE ONLY groups ADD CONSTRAINT groups_pkey PRIMARY KEY (id);
ALTER TABLE groups ALTER COLUMN id SET DEFAULT nextval('groups_id_seq'::regclass);

-- UserGroup providing additional group information, e.g. name and description.
CREATE TABLE usergroup (
    id integer NOT NULL,
    description character varying(1024),
    groupid character varying(255),
    groupname character varying(256)
);

CREATE SEQUENCE usergroup_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER SEQUENCE usergroup_id_seq OWNED BY usergroup.id;
ALTER TABLE ONLY usergroup ADD CONSTRAINT usergroup_pkey PRIMARY KEY (id);
ALTER TABLE usergroup ALTER COLUMN id SET DEFAULT nextval('usergroup_id_seq'::regclass);

-- Memberships are mapping users to groups with according in-group roles.
CREATE TABLE memberships (
    id integer NOT NULL,
    membersrole integer,
    group_id bigint NOT NULL,
    user_id bigint NOT NULL
);

CREATE SEQUENCE memberships_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER SEQUENCE memberships_id_seq OWNED BY memberships.id;
ALTER TABLE ONLY memberships ADD CONSTRAINT memberships_pkey PRIMARY KEY (id);
ALTER TABLE ONLY memberships ADD CONSTRAINT fk_memberships_group_id FOREIGN KEY (group_id) REFERENCES groups(id);
ALTER TABLE ONLY memberships ADD CONSTRAINT fk_memberships_user_id FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE memberships ALTER COLUMN id SET DEFAULT nextval('memberships_id_seq'::regclass);

------------------------------------------
-- AAI/Sharing related tables. 
------------------------------------------

-- A GrantSet holds all grants to resources with according role restrictions.
CREATE TABLE grantsets (
    id integer NOT NULL,
    rolerestriction integer,
    resource_id bigint
);

CREATE SEQUENCE grantsets_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER SEQUENCE grantsets_id_seq OWNED BY grantsets.id;
ALTER TABLE ONLY grantsets ADD CONSTRAINT grantsets_pkey PRIMARY KEY (id);
ALTER TABLE grantsets ALTER COLUMN id SET DEFAULT nextval('grantsets_id_seq'::regclass);

-- Securable resources identified by domain id und domain unique id. Resources are mapped to a GrantSet.
CREATE TABLE resources (
    id integer NOT NULL,
    domainid character varying(255),
    domainuniqueid character varying(255),
    grantset_id bigint
);

CREATE SEQUENCE resources_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER SEQUENCE resources_id_seq OWNED BY resources.id;
ALTER TABLE ONLY resources ADD CONSTRAINT resources_pkey PRIMARY KEY (id);
ALTER TABLE ONLY resources ADD CONSTRAINT fk_resources_grantset_id FOREIGN KEY (grantset_id) REFERENCES grantsets(id);
ALTER TABLE ONLY grantsets ADD CONSTRAINT fk_grantsets_resource_id FOREIGN KEY (resource_id) REFERENCES resources(id);
ALTER TABLE ONLY resources ADD CONSTRAINT unq_resources_0 UNIQUE (domainuniqueid, domainid);
ALTER TABLE resources ALTER COLUMN id SET DEFAULT nextval('resources_id_seq'::regclass);

-- Grants for sharing resources with single users. Grants are referring to an associated grantsets by grants_id.
CREATE TABLE grants (
    id integer NOT NULL,
    grantedrole integer,
    grantee_id bigint,
    grants_id bigint
);

CREATE SEQUENCE grants_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER SEQUENCE grants_id_seq OWNED BY grants.id;
ALTER TABLE ONLY grants ADD CONSTRAINT grants_pkey PRIMARY KEY (id);
ALTER TABLE ONLY grants ADD CONSTRAINT fk_grants_grantee_id FOREIGN KEY (grantee_id) REFERENCES users(id);
ALTER TABLE ONLY grants ADD CONSTRAINT fk_grants_grants_id FOREIGN KEY (grants_id) REFERENCES grantsets(id);
ALTER TABLE grants ALTER COLUMN id SET DEFAULT nextval('grants_id_seq'::regclass);

-- ResourceReference provides a reference to a resource shared with another group.
CREATE TABLE resourcereferences (
    id integer NOT NULL,
    role_restriction integer,
    group_id bigint NOT NULL,
    resource_id bigint NOT NULL
);

CREATE SEQUENCE resourcereferences_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER SEQUENCE resourcereferences_id_seq OWNED BY resourcereferences.id;
ALTER TABLE ONLY resourcereferences ADD CONSTRAINT resourcereferences_pkey PRIMARY KEY (id);
ALTER TABLE ONLY resourcereferences ADD CONSTRAINT fk_resourcereferences_group_id FOREIGN KEY (group_id) REFERENCES groups(id);
ALTER TABLE ONLY resourcereferences ADD CONSTRAINT fk_resourcereferences_resource_id FOREIGN KEY (resource_id) REFERENCES resources(id);
ALTER TABLE ONLY resourcereferences ADD CONSTRAINT unq_resourcereferences_0 UNIQUE (resource_id, group_id);
ALTER TABLE resourcereferences ALTER COLUMN id SET DEFAULT nextval('resourcereferences_id_seq'::regclass);

-------------------------------------------------------------------------------------------------------------------------------------
-- Basic administrative metadata related tables. 
-- Most of the tables are generated during the first start of the repository system. 
-- The following tables are the very basic ones needed for the initial setup, e.g. to add a system user, a default group and a login.
-------------------------------------------------------------------------------------------------------------------------------------

-- Metadata schema table holding supported metadata schemas which might be used during metadata extraction.
CREATE TABLE metadataschema (
    id integer NOT NULL,
    metadataschemaurl character varying(255) NOT NULL,
    schemaidentifier character varying(255) NOT NULL,
    namespace character varying(255) DEFAULT '' NOT NULL 
);

CREATE SEQUENCE metadataschema_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER SEQUENCE metadataschema_id_seq OWNED BY metadataschema.id;
ALTER TABLE ONLY metadataschema ADD CONSTRAINT metadataschema_pkey PRIMARY KEY (id);
ALTER TABLE ONLY metadataschema ADD CONSTRAINT metadataschema_schemaidentifier_key UNIQUE (schemaidentifier);
ALTER TABLE metadataschema ALTER COLUMN id SET DEFAULT nextval('metadataschema_id_seq'::regclass);

-- OrganizationUnits are used as organizational entities owning datasets or performing data producing experiments.
CREATE TABLE organizationunit (
    organizationunitid integer NOT NULL,
    address character varying(255),
    city character varying(255),
    country character varying(255),
    ouname character varying(255),
    website character varying(255),
    zipcode character varying(255),
    manager_userid bigint
);

CREATE SEQUENCE organizationunit_organizationunitid_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER SEQUENCE organizationunit_organizationunitid_seq OWNED BY organizationunit.organizationunitid;
ALTER TABLE ONLY organizationunit ADD CONSTRAINT organizationunit_pkey PRIMARY KEY (organizationunitid);
ALTER TABLE ONLY organizationunit ADD CONSTRAINT fk_organizationunit_manager_userid FOREIGN KEY (manager_userid) REFERENCES userdata(userid);
ALTER TABLE organizationunit ALTER COLUMN organizationunitid SET DEFAULT nextval('organizationunit_organizationunitid_seq'::regclass);

-- Task information allowing to define responsibilities of organization units or single users, e.g. data scientist, project manager, or software developer.
CREATE TABLE task (
    taskid integer NOT NULL,
    task character varying(255)
);

CREATE SEQUENCE task_taskid_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER SEQUENCE task_taskid_seq OWNED BY task.taskid;
ALTER TABLE ONLY task ADD CONSTRAINT task_pkey PRIMARY KEY (taskid);
ALTER TABLE task ALTER COLUMN taskid SET DEFAULT nextval('task_taskid_seq'::regclass);

-- ServiceAccessTokens are encrypted key-secret combinations associated with a service and a user allowing basic authorization, e.g. for accessing REST-service using OAuth.
CREATE TABLE serviceaccesstoken (
    id integer NOT NULL,
    serviceid character varying(255),
    tokenkey character varying(255),
    tokensecret character varying(255),
    userid character varying(255)
);

CREATE SEQUENCE serviceaccesstoken_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER SEQUENCE serviceaccesstoken_id_seq OWNED BY serviceaccesstoken.id;
ALTER TABLE ONLY serviceaccesstoken ADD CONSTRAINT serviceaccesstoken_pkey PRIMARY KEY (id);
ALTER TABLE serviceaccesstoken ALTER COLUMN id SET DEFAULT nextval('serviceaccesstoken_id_seq'::regclass);

-- StagingAccessPointConfiguration table created as for many repository instances an initial access point might be added during installation.
 CREATE TABLE stagingaccesspointconfiguration (
    id integer NOT NULL,
    customproperties character varying(1024),
    defaultaccesspoint boolean,
    description character varying(1024),
    disabled boolean,
    groupid character varying(255),
    implementationclass character varying(255),
    localbasepath character varying(255),
    name character varying(255),
    remotebaseurl character varying(255),
    transientaccesspoint boolean,
    uniqueidentifier character varying(255) NOT NULL
);

CREATE SEQUENCE stagingaccesspointconfiguration_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER SEQUENCE stagingaccesspointconfiguration_id_seq OWNED BY stagingaccesspointconfiguration.id;
ALTER TABLE ONLY stagingaccesspointconfiguration ADD CONSTRAINT stagingaccesspointconfiguration_pkey PRIMARY KEY (id);
ALTER TABLE ONLY stagingaccesspointconfiguration ADD CONSTRAINT stagingaccesspointconfiguration_uniqueidentifier_key UNIQUE (uniqueidentifier);
ALTER TABLE stagingaccesspointconfiguration ALTER COLUMN id SET DEFAULT nextval('stagingaccesspointconfiguration_id_seq'::regclass);

--CREATE TABLE sequence (
--    seq_name character varying(50) NOT NULL,
--    seq_count numeric(38,0)
--);

--ALTER TABLE ONLY sequence ADD CONSTRAINT sequence_pkey PRIMARY KEY (seq_name);


-- DataOrganizationNode and Attribute tables needed due to the fact, that the fk_attribute_stepnoarrived constraint should cascade on update. This is not reflected by JPA, thus it must be enforced. 

CREATE TABLE dataorganizationnode (
    stepnoarrived bigint NOT NULL,
    viewname character varying(255) NOT NULL,
    digit_obj_id character varying(255) NOT NULL,
    type character varying(31),
    description character varying(255),
    idversion integer,
    name character varying(255),
    nodedepth integer,
    stepnoleaved bigint,
    fullyqualifiedtypename character varying(255),
    value character varying(255)
);

ALTER TABLE ONLY dataorganizationnode ADD CONSTRAINT dataorganizationnode_pkey PRIMARY KEY (stepnoarrived, viewname, digit_obj_id);

CREATE TABLE attribute (
    id bigint NOT NULL,
    attr_key character varying(255),
    attr_value character varying(255),
    digit_obj_id character varying(255) NOT NULL,
    viewname character varying(255) NOT NULL,
    stepnoarrived bigint NOT NULL
);

ALTER TABLE ONLY attribute ADD CONSTRAINT attribute_pkey PRIMARY KEY (id, digit_obj_id, viewname, stepnoarrived);
    
ALTER TABLE ONLY attribute ADD CONSTRAINT fk_attribute_stepnoarrived FOREIGN KEY (stepnoarrived, viewname, digit_obj_id) REFERENCES dataorganizationnode(stepnoarrived, viewname, digit_obj_id) ON UPDATE CASCADE;
---------------------------------------------------------------------------------------------------------------------
-- Quartz scheduler related tables. The Quartz scheduler is used to perform recurring tasks of the repository system.
-- These tables are holding Quartz state information.
---------------------------------------------------------------------------------------------------------------------

-- Cleanup (just for debugging)
-- DROP TABLE qrtz_fired_triggers;
-- DROP TABLE QRTZ_PAUSED_TRIGGER_GRPS;
-- DROP TABLE QRTZ_SCHEDULER_STATE;
-- DROP TABLE QRTZ_LOCKS;
-- DROP TABLE qrtz_simple_triggers;
-- DROP TABLE qrtz_cron_triggers;
-- DROP TABLE qrtz_simprop_triggers;
-- DROP TABLE QRTZ_BLOB_TRIGGERS;
-- DROP TABLE qrtz_triggers;
-- DROP TABLE qrtz_job_details;
-- DROP TABLE qrtz_calendars;

CREATE TABLE qrtz_job_details
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    JOB_NAME  VARCHAR(200) NOT NULL,
    JOB_GROUP VARCHAR(200) NOT NULL,
    DESCRIPTION VARCHAR(250) NULL,
    JOB_CLASS_NAME   VARCHAR(250) NOT NULL, 
    IS_DURABLE BOOL NOT NULL,
    IS_NONCONCURRENT BOOL NOT NULL,
    IS_UPDATE_DATA BOOL NOT NULL,
    REQUESTS_RECOVERY BOOL NOT NULL,
    JOB_DATA BYTEA NULL,
    PRIMARY KEY (SCHED_NAME,JOB_NAME,JOB_GROUP)
);

CREATE TABLE qrtz_triggers
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    JOB_NAME  VARCHAR(200) NOT NULL, 
    JOB_GROUP VARCHAR(200) NOT NULL,
    DESCRIPTION VARCHAR(250) NULL,
    NEXT_FIRE_TIME BIGINT NULL,
    PREV_FIRE_TIME BIGINT NULL,
    PRIORITY INTEGER NULL,
    TRIGGER_STATE VARCHAR(16) NOT NULL,
    TRIGGER_TYPE VARCHAR(8) NOT NULL,
    START_TIME BIGINT NOT NULL,
    END_TIME BIGINT NULL,
    CALENDAR_NAME VARCHAR(200) NULL,
    MISFIRE_INSTR SMALLINT NULL,
    JOB_DATA BYTEA NULL,
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,JOB_NAME,JOB_GROUP) 
	REFERENCES QRTZ_JOB_DETAILS(SCHED_NAME,JOB_NAME,JOB_GROUP) 
);

CREATE TABLE qrtz_simple_triggers
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    REPEAT_COUNT BIGINT NOT NULL,
    REPEAT_INTERVAL BIGINT NOT NULL,
    TIMES_TRIGGERED BIGINT NOT NULL,
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP) 
	REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE qrtz_cron_triggers
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    CRON_EXPRESSION VARCHAR(120) NOT NULL,
    TIME_ZONE_ID VARCHAR(80),
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP) 
	REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE qrtz_simprop_triggers
  (          
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    STR_PROP_1 VARCHAR(512) NULL,
    STR_PROP_2 VARCHAR(512) NULL,
    STR_PROP_3 VARCHAR(512) NULL,
    INT_PROP_1 INT NULL,
    INT_PROP_2 INT NULL,
    LONG_PROP_1 BIGINT NULL,
    LONG_PROP_2 BIGINT NULL,
    DEC_PROP_1 NUMERIC(13,4) NULL,
    DEC_PROP_2 NUMERIC(13,4) NULL,
    BOOL_PROP_1 BOOL NULL,
    BOOL_PROP_2 BOOL NULL,
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP) 
    REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE qrtz_blob_triggers
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    BLOB_DATA BYTEA NULL,
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP) 
        REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE qrtz_calendars
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    CALENDAR_NAME  VARCHAR(200) NOT NULL, 
    CALENDAR BYTEA NOT NULL,
    PRIMARY KEY (SCHED_NAME,CALENDAR_NAME)
);


CREATE TABLE qrtz_paused_trigger_grps
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_GROUP  VARCHAR(200) NOT NULL, 
    PRIMARY KEY (SCHED_NAME,TRIGGER_GROUP)
);

CREATE TABLE qrtz_fired_triggers 
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    ENTRY_ID VARCHAR(95) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    INSTANCE_NAME VARCHAR(200) NOT NULL,
    FIRED_TIME BIGINT NOT NULL,
    SCHED_TIME BIGINT NOT NULL,
    PRIORITY INTEGER NOT NULL,
    STATE VARCHAR(16) NOT NULL,
    JOB_NAME VARCHAR(200) NULL,
    JOB_GROUP VARCHAR(200) NULL,
    IS_NONCONCURRENT BOOL NULL,
    REQUESTS_RECOVERY BOOL NULL,
    PRIMARY KEY (SCHED_NAME,ENTRY_ID)
);

CREATE TABLE qrtz_scheduler_state 
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    INSTANCE_NAME VARCHAR(200) NOT NULL,
    LAST_CHECKIN_TIME BIGINT NOT NULL,
    CHECKIN_INTERVAL BIGINT NOT NULL,
    PRIMARY KEY (SCHED_NAME,INSTANCE_NAME)
);

CREATE TABLE qrtz_locks
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    LOCK_NAME  VARCHAR(40) NOT NULL, 
    PRIMARY KEY (SCHED_NAME,LOCK_NAME)
);

create index idx_qrtz_j_req_recovery on qrtz_job_details(SCHED_NAME,REQUESTS_RECOVERY);
create index idx_qrtz_j_grp on qrtz_job_details(SCHED_NAME,JOB_GROUP);

create index idx_qrtz_t_j on qrtz_triggers(SCHED_NAME,JOB_NAME,JOB_GROUP);
create index idx_qrtz_t_jg on qrtz_triggers(SCHED_NAME,JOB_GROUP);
create index idx_qrtz_t_c on qrtz_triggers(SCHED_NAME,CALENDAR_NAME);
create index idx_qrtz_t_g on qrtz_triggers(SCHED_NAME,TRIGGER_GROUP);
create index idx_qrtz_t_state on qrtz_triggers(SCHED_NAME,TRIGGER_STATE);
create index idx_qrtz_t_n_state on qrtz_triggers(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP,TRIGGER_STATE);
create index idx_qrtz_t_n_g_state on qrtz_triggers(SCHED_NAME,TRIGGER_GROUP,TRIGGER_STATE);
create index idx_qrtz_t_next_fire_time on qrtz_triggers(SCHED_NAME,NEXT_FIRE_TIME);
create index idx_qrtz_t_nft_st on qrtz_triggers(SCHED_NAME,TRIGGER_STATE,NEXT_FIRE_TIME);
create index idx_qrtz_t_nft_misfire on qrtz_triggers(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME);
create index idx_qrtz_t_nft_st_misfire on qrtz_triggers(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_STATE);
create index idx_qrtz_t_nft_st_misfire_grp on qrtz_triggers(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_GROUP,TRIGGER_STATE);

create index idx_qrtz_ft_trig_inst_name on qrtz_fired_triggers(SCHED_NAME,INSTANCE_NAME);
create index idx_qrtz_ft_inst_job_req_rcvry on qrtz_fired_triggers(SCHED_NAME,INSTANCE_NAME,REQUESTS_RECOVERY);
create index idx_qrtz_ft_j_g on qrtz_fired_triggers(SCHED_NAME,JOB_NAME,JOB_GROUP);
create index idx_qrtz_ft_jg on qrtz_fired_triggers(SCHED_NAME,JOB_GROUP);
create index idx_qrtz_ft_t_g on qrtz_fired_triggers(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);
create index idx_qrtz_ft_tg on qrtz_fired_triggers(SCHED_NAME,TRIGGER_GROUP);

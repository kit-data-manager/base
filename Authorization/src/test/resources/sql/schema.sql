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
CREATE SEQUENCE users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE;

CREATE TABLE users (
    id integer NOT NULL DEFAULT nextval('users_id_seq'),
    maximumrole integer,
    userid character varying(255) NOT NULL,
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT users_userid_key UNIQUE (userid)
);

CREATE SEQUENCE userdata_userid_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE;

-- UserData providing additional user information, e.g. email and names.
CREATE TABLE userdata (
    userid integer NOT NULL DEFAULT nextval('userdata_userid_seq'),
    distinguishedname character varying(255),
    email character varying(255),
    firstname character varying(255),
    lastname character varying(255),
    validfrom timestamp without time zone,
    validuntil timestamp without time zone,
    CONSTRAINT userdata_pkey PRIMARY KEY (userid)
);

-- AAI-related user groups and their unique groupIds

CREATE SEQUENCE groups_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE;

CREATE TABLE groups (
    id integer NOT NULL DEFAULT nextval('groups_id_seq'),
    groupid character varying(255) NOT NULL,
    CONSTRAINT groups_groupid_key UNIQUE (groupid),
    CONSTRAINT groups_pkey PRIMARY KEY (id)
);

CREATE SEQUENCE usergroup_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE;

-- UserGroup providing additional group information, e.g. name and description.
CREATE TABLE usergroup (
    id integer NOT NULL DEFAULT nextval('usergroup_id_seq'),
    description character varying(1024),
    groupid character varying(255),
    groupname character varying(256),
    CONSTRAINT usergroup_pkey PRIMARY KEY (id)
);

CREATE SEQUENCE memberships_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE;

-- Memberships are mapping users to groups with according in-group roles.
CREATE TABLE memberships (
    id integer NOT NULL DEFAULT nextval('memberships_id_seq'),
    membersrole integer,
    group_id bigint NOT NULL,
    user_id bigint NOT NULL,
CONSTRAINT memberships_pkey PRIMARY KEY (id),
CONSTRAINT fk_memberships_group_id FOREIGN KEY (group_id) REFERENCES groups(id),
CONSTRAINT fk_memberships_user_id FOREIGN KEY (user_id) REFERENCES users(id)
);

------------------------------------------
-- AAI/Sharing related tables. 
------------------------------------------

CREATE SEQUENCE grantsets_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE;

-- A GrantSet holds all grants to resources with according role restrictions.
CREATE TABLE grantsets (
    id integer NOT NULL DEFAULT nextval('grantsets_id_seq'),
    rolerestriction integer,
    resource_id bigint,
    CONSTRAINT grantsets_pkey PRIMARY KEY (id)
);

CREATE SEQUENCE resources_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE;

-- Securable resources identified by domain id und domain unique id. Resources are mapped to a GrantSet.
CREATE TABLE resources (
    id integer NOT NULL DEFAULT nextval('resources_id_seq'),
    domainid character varying(255),
    domainuniqueid character varying(255),
    grantset_id bigint,
    CONSTRAINT resources_pkey PRIMARY KEY (id),
    CONSTRAINT fk_resources_grantset_id FOREIGN KEY (grantset_id) REFERENCES grantsets(id),
    CONSTRAINT unq_resources_0 UNIQUE (domainuniqueid, domainid)
);

ALTER TABLE grantsets ADD CONSTRAINT fk_grantsets_resource_id FOREIGN KEY (resource_id) REFERENCES resources(id);


CREATE SEQUENCE grants_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE;

-- Grants for sharing resources with single users. Grants are referring to an associated grantsets by grants_id.
CREATE TABLE grants (
    id integer NOT NULL DEFAULT nextval('grants_id_seq'),
    grantedrole integer,
    grantee_id bigint,
    grants_id bigint,
    CONSTRAINT grants_pkey PRIMARY KEY (id),
    CONSTRAINT fk_grants_grantee_id FOREIGN KEY (grantee_id) REFERENCES users(id),
    CONSTRAINT fk_grants_grants_id FOREIGN KEY (grants_id) REFERENCES grantsets(id)
);

CREATE SEQUENCE resourcereferences_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE;

-- ResourceReference provides a reference to a resource shared with another group.
CREATE TABLE resourcereferences (
    id integer NOT NULL DEFAULT nextval('resourcereferences_id_seq'),
    role_restriction integer,
    group_id bigint NOT NULL,
    resource_id bigint NOT NULL,
    CONSTRAINT resourcereferences_pkey PRIMARY KEY (id),
    CONSTRAINT fk_resourcereferences_group_id FOREIGN KEY (group_id) REFERENCES groups(id),
    CONSTRAINT fk_resourcereferences_resource_id FOREIGN KEY (resource_id) REFERENCES resources(id),
    CONSTRAINT unq_resourcereferences_0 UNIQUE (resource_id, group_id)
);
 
-- Database view to link users, groups, memberships, grants, grantsets, and resourcereferences. This view is used to collect all user-group-resource mappings that arise due to sharing. 
CREATE VIEW filterhelper_hack AS SELECT ut.userid, gt.groupid, grst.resource_id AS resourceid, LEAST(ut.maximumrole, grt.grantedrole, grst.rolerestriction) AS role FROM users ut, groups gt, grants grt, grantsets grst, memberships mt WHERE ((((grt.grantee_id = ut.id) AND (grst.id = grt.grants_id)) AND (ut.id = mt.user_id)) AND (gt.id = mt.group_id)) UNION SELECT ut.userid, gt.groupid, rrt.resource_id AS resourceid, LEAST(ut.maximumrole, mt.membersrole, rrt.role_restriction) AS role FROM users ut, groups gt, memberships mt, resourcereferences rrt WHERE (((ut.id = mt.user_id) AND (gt.id = mt.group_id)) AND (gt.id = rrt.group_id));

-- Database view to obtain a list of user-group-resource mappings to make final access permission decisions. 
CREATE VIEW filterhelper AS SELECT fh.userid, fh.groupid, res.domainid, res.domainuniqueid, max(fh.role) AS possessed_role FROM filterhelper_hack fh, resources res WHERE (fh.resourceid = res.id) GROUP BY fh.userid, fh.groupid, res.domainuniqueid, res.domainid

SHUTDOWN
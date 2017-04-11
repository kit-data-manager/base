--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- TOC entry 423 (class 2612 OID 16386)
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: -; Owner: postgres
--

CREATE PROCEDURAL LANGUAGE plpgsql;

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 1663 (class 1259 OID 108383)
-- Dependencies: 6
-- Name: attribute; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE attribute (
    id bigint NOT NULL,
    attr_key character varying(255),
    attr_value character varying(255),
    digit_obj_id character varying(255) NOT NULL,
    viewname character varying(255) NOT NULL,
    stepnoarrived bigint NOT NULL
);


--
-- TOC entry 1664 (class 1259 OID 108391)
-- Dependencies: 6
-- Name: dataorganizationnode; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

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


--
-- TOC entry 1614 (class 1259 OID 107988)
-- Dependencies: 6
-- Name: digitalobject; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE digitalobject (
    baseid integer NOT NULL,
    digitalobjectidentifier character varying(255) NOT NULL,
    enddate timestamp without time zone,
    label character varying(255),
    note character varying(1024),
    startdate timestamp without time zone,
    uploaddate timestamp without time zone,
    visible boolean,
    investigation_investigationid bigint,
    uploader_userid bigint
);


--
-- TOC entry 1615 (class 1259 OID 107994)
-- Dependencies: 6 1614
-- Name: digitalobject_baseid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE digitalobject_baseid_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2109 (class 0 OID 0)
-- Dependencies: 1615
-- Name: digitalobject_baseid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE digitalobject_baseid_seq OWNED BY digitalobject.baseid;

--
-- Name: digitalobjecttype; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE digitalobjecttype (
    id integer NOT NULL,
    description character varying(255),
    identifier character varying(255) NOT NULL,
    typedomain character varying(255) NOT NULL,
    version integer NOT NULL
);


--
-- Name: digitalobjecttype_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE digitalobjecttype_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: digitalobjecttype_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE digitalobjecttype_id_seq OWNED BY digitalobjecttype.id;


--
-- TOC entry 1616 (class 1259 OID 107996)
-- Dependencies: 6
-- Name: digitalobject_userdata; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE digitalobject_userdata (
    digitalobject_baseid bigint NOT NULL,
    experimenters_userid bigint NOT NULL
);


--
-- TOC entry 1611 (class 1259 OID 107967)
-- Dependencies: 6
-- Name: downloadinformation; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE downloadinformation (
    id bigint NOT NULL,
    accesspointid character varying(255),
    accessproviderid character varying(255),
    clientaccessurl character varying(255),
    digitalobjectuuid character varying(255) NOT NULL,
    errormessage character varying(255),
    expiresat bigint,
    lastupdate bigint,
    owneruuid character varying(255),
    stagingurl character varying(255),
    status integer,
    transferid character varying(255),
    groupuuid character varying(255)
);

--
-- TOC entry 1656 (class 1259 OID 108130)
-- Dependencies: 1655 6
-- Name: downloadinformation_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE downloadinformation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2130 (class 0 OID 0)
-- Dependencies: 1656
-- Name: downloadinformation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE downloadinformation_id_seq OWNED BY downloadinformation.id;


--
-- TOC entry 1617 (class 1259 OID 107999)
-- Dependencies: 6
-- Name: grants; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE grants (
    id integer NOT NULL,
    grantedrole integer,
    grantee_id bigint,
    grants_id bigint
);


--
-- TOC entry 1618 (class 1259 OID 108002)
-- Dependencies: 6
-- Name: grantsets; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE grantsets (
    id integer NOT NULL,
    rolerestriction integer,
    resource_id bigint
);


--
-- TOC entry 1619 (class 1259 OID 108005)
-- Dependencies: 6
-- Name: groups; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE groups (
    id integer NOT NULL,
    groupid character varying(255) NOT NULL
);


--
-- TOC entry 1620 (class 1259 OID 108008)
-- Dependencies: 6
-- Name: memberships; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE memberships (
    id integer NOT NULL,
    membersrole integer,
    group_id bigint NOT NULL,
    user_id bigint NOT NULL
);


--
-- TOC entry 1621 (class 1259 OID 108011)
-- Dependencies: 6
-- Name: resourcereferences; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE resourcereferences (
    id integer NOT NULL,
    role_restriction integer,
    group_id bigint NOT NULL,
    resource_id bigint NOT NULL
);


--
-- TOC entry 1622 (class 1259 OID 108014)
-- Dependencies: 6
-- Name: users; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE users (
    id integer NOT NULL,
    maximumrole integer,
    userid character varying(255) NOT NULL
);


--
-- TOC entry 1623 (class 1259 OID 108017)
-- Dependencies: 1759 6
-- Name: filterhelper_hack; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW filterhelper_hack AS
    SELECT ut.userid, gt.groupid, grst.resource_id AS resourceid, LEAST(ut.maximumrole, grt.grantedrole, grst.rolerestriction) AS role FROM users ut, groups gt, grants grt, grantsets grst, memberships mt WHERE ((((grt.grantee_id = ut.id) AND (grst.id = grt.grants_id)) AND (ut.id = mt.user_id)) AND (gt.id = mt.group_id)) UNION SELECT ut.userid, gt.groupid, rrt.resource_id AS resourceid, LEAST(ut.maximumrole, mt.membersrole, rrt.role_restriction) AS role FROM users ut, groups gt, memberships mt, resourcereferences rrt WHERE (((ut.id = mt.user_id) AND (gt.id = mt.group_id)) AND (gt.id = rrt.group_id));


--
-- TOC entry 1624 (class 1259 OID 108022)
-- Dependencies: 6
-- Name: resources; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE resources (
    id integer NOT NULL,
    domainid character varying(255),
    domainuniqueid character varying(255),
    grantset_id bigint
);


--
-- TOC entry 1625 (class 1259 OID 108028)
-- Dependencies: 1760 6
-- Name: filterhelper; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW filterhelper AS
    SELECT fh.userid, fh.groupid, res.domainid, res.domainuniqueid, max(fh.role) AS possessed_role FROM filterhelper_hack fh, resources res WHERE (fh.resourceid = res.id) GROUP BY fh.userid, fh.groupid, res.domainuniqueid, res.domainid;


--
-- TOC entry 1626 (class 1259 OID 108032)
-- Dependencies: 6 1617
-- Name: grants_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE grants_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2110 (class 0 OID 0)
-- Dependencies: 1626
-- Name: grants_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE grants_id_seq OWNED BY grants.id;


--
-- TOC entry 1627 (class 1259 OID 108034)
-- Dependencies: 6 1618
-- Name: grantsets_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE grantsets_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2111 (class 0 OID 0)
-- Dependencies: 1627
-- Name: grantsets_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE grantsets_id_seq OWNED BY grantsets.id;


--
-- TOC entry 1628 (class 1259 OID 108036)
-- Dependencies: 1619 6
-- Name: groups_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE groups_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2112 (class 0 OID 0)
-- Dependencies: 1628
-- Name: groups_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE groups_id_seq OWNED BY groups.id;


--
-- TOC entry 1610 (class 1259 OID 107959)
-- Dependencies: 6
-- Name: ingestinformation; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE ingestinformation (
    id bigint NOT NULL,
    accesspointid character varying(255),
    accessproviderid character varying(255),
    clientaccessurl character varying(255),
    digitalobjectuuid character varying(255) NOT NULL,
    errormessage character varying(255),
    expiresat bigint,
    lastupdate bigint,
    owneruuid character varying(255),
    stagingurl character varying(255),
    status integer,
    storageurl character varying(255),
    transferid character varying(255),
    groupuuid character varying(255)
);

--
-- TOC entry 1656 (class 1259 OID 108130)
-- Dependencies: 1655 6
-- Name: ingestinformation_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE ingestinformation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2130 (class 0 OID 0)
-- Dependencies: 1656
-- Name: ingestinformation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE ingestinformation_id_seq OWNED BY ingestinformation.id;

--
-- TOC entry 1670 (class 1259 OID 108435)
-- Dependencies: 6
-- Name: ingestinformation_stagingprocessor; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE ingestinformation_stagingprocessor (
    ingestinformation_id bigint NOT NULL,
    stagingprocessors_id bigint NOT NULL
);


--
-- TOC entry 1629 (class 1259 OID 108038)
-- Dependencies: 6
-- Name: investigation; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE investigation (
    investigationid integer NOT NULL,
    description character varying(1024),
    enddate timestamp without time zone,
    note character varying(1024),
    startdate timestamp without time zone,
    topic character varying(255),
    uniqueidentifier character varying(255) NOT NULL,
    visible boolean,
    study_studyid bigint
);


--
-- TOC entry 1630 (class 1259 OID 108044)
-- Dependencies: 1629 6
-- Name: investigation_investigationid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE investigation_investigationid_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2113 (class 0 OID 0)
-- Dependencies: 1630
-- Name: investigation_investigationid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE investigation_investigationid_seq OWNED BY investigation.investigationid;


--
-- TOC entry 1631 (class 1259 OID 108046)
-- Dependencies: 6
-- Name: investigation_metadataschema; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE investigation_metadataschema (
    investigation_investigationid bigint NOT NULL,
    metadataschema_id bigint NOT NULL
);


--
-- TOC entry 1632 (class 1259 OID 108049)
-- Dependencies: 6
-- Name: investigation_participant; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE investigation_participant (
    investigation_investigationid bigint NOT NULL,
    participants_participantid bigint NOT NULL
);


--
-- TOC entry 1633 (class 1259 OID 108052)
-- Dependencies: 6
-- Name: usergroup; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE usergroup (
    id integer NOT NULL,
    description character varying(1024),
    groupid character varying(255),
    groupname character varying(256)
);


--
-- TOC entry 1634 (class 1259 OID 108058)
-- Dependencies: 1633 6
-- Name: usergroup_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE usergroup_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2114 (class 0 OID 0)
-- Dependencies: 1634
-- Name: usergroup_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE usergroup_id_seq OWNED BY usergroup.id;


--
-- TOC entry 1635 (class 1259 OID 108060)
-- Dependencies: 6 1620
-- Name: memberships_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE memberships_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2115 (class 0 OID 0)
-- Dependencies: 1635
-- Name: memberships_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE memberships_id_seq OWNED BY memberships.id;


--
-- TOC entry 1636 (class 1259 OID 108062)
-- Dependencies: 6
-- Name: metadataindexingtask; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE metadataindexingtask (
    id integer NOT NULL,
    digitalobjectid character varying(255),
    failcount integer,
    finishtimestamp bigint,
    groupid character varying(255),
    lasterrortimestamp bigint,
    metadatadocumenturl character varying(255),
    ownerid character varying(255),
    scheduletimestamp bigint,
    schemareference_id bigint
);


--
-- TOC entry 1637 (class 1259 OID 108068)
-- Dependencies: 1636 6
-- Name: metadataindexingtask_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE metadataindexingtask_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2116 (class 0 OID 0)
-- Dependencies: 1637
-- Name: metadataindexingtask_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE metadataindexingtask_id_seq OWNED BY metadataindexingtask.id;


--
-- TOC entry 1638 (class 1259 OID 108070)
-- Dependencies: 6
-- Name: metadataschema; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE metadataschema (
    id integer NOT NULL,
    metadataschemaurl character varying(255),
    schemaidentifier character varying(255) NOT NULL
);


--
-- TOC entry 1639 (class 1259 OID 108076)
-- Dependencies: 6 1638
-- Name: metadataschema_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE metadataschema_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2117 (class 0 OID 0)
-- Dependencies: 1639
-- Name: metadataschema_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE metadataschema_id_seq OWNED BY metadataschema.id;


--
-- TOC entry 1640 (class 1259 OID 108078)
-- Dependencies: 6
-- Name: organizationunit; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

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


--
-- TOC entry 1641 (class 1259 OID 108084)
-- Dependencies: 1640 6
-- Name: organizationunit_organizationunitid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE organizationunit_organizationunitid_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2118 (class 0 OID 0)
-- Dependencies: 1641
-- Name: organizationunit_organizationunitid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE organizationunit_organizationunitid_seq OWNED BY organizationunit.organizationunitid;


--
-- TOC entry 1642 (class 1259 OID 108086)
-- Dependencies: 6
-- Name: participant; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE participant (
    participantid integer NOT NULL,
    task_taskid bigint,
    user_userid bigint
);


--
-- TOC entry 1643 (class 1259 OID 108089)
-- Dependencies: 6 1642
-- Name: participant_participantid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE participant_participantid_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2119 (class 0 OID 0)
-- Dependencies: 1643
-- Name: participant_participantid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE participant_participantid_seq OWNED BY participant.participantid;


--
-- TOC entry 1672 (class 1259 OID 108452)
-- Dependencies: 6
-- Name: publicationentry; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE publicationentry (
    id integer NOT NULL,
    authors character varying(255),
    booktitle character varying(255),
    chapter character varying(255),
    doi character varying(255),
    edition character varying(255),
    identifier character varying(255) NOT NULL,
    isbn character varying(255),
    issn character varying(255),
    journal character varying(255),
    keywords character varying(255),
    number character varying(255),
    pages character varying(255),
    pubabstract character varying(1024),
    pubmonth character varying(255),
    publisher character varying(255),
    title character varying(255),
    type character varying(255),
    volume character varying(255),
    url character varying(255),
    pubyear character varying(255)
);


--
-- TOC entry 1671 (class 1259 OID 108450)
-- Dependencies: 1672 6
-- Name: publicationentry_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE publicationentry_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2120 (class 0 OID 0)
-- Dependencies: 1671
-- Name: publicationentry_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE publicationentry_id_seq OWNED BY publicationentry.id;


--
-- TOC entry 1644 (class 1259 OID 108091)
-- Dependencies: 6
-- Name: relation; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE relation (
    relationid integer NOT NULL,
    organizationunit_organizationunitid bigint,
    task_taskid bigint
);


--
-- TOC entry 1645 (class 1259 OID 108094)
-- Dependencies: 1644 6
-- Name: relation_relationid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE relation_relationid_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2121 (class 0 OID 0)
-- Dependencies: 1645
-- Name: relation_relationid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE relation_relationid_seq OWNED BY relation.relationid;


--
-- TOC entry 1646 (class 1259 OID 108096)
-- Dependencies: 6 1621
-- Name: resourcereferences_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE resourcereferences_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2122 (class 0 OID 0)
-- Dependencies: 1646
-- Name: resourcereferences_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE resourcereferences_id_seq OWNED BY resourcereferences.id;


--
-- TOC entry 1647 (class 1259 OID 108098)
-- Dependencies: 1624 6
-- Name: resources_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE resources_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2123 (class 0 OID 0)
-- Dependencies: 1647
-- Name: resources_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE resources_id_seq OWNED BY resources.id;


--
-- TOC entry 1665 (class 1259 OID 108404)
-- Dependencies: 6
-- Name: sequence; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE sequence (
    seq_name character varying(50) NOT NULL,
    seq_count numeric(38,0)
);


--
-- TOC entry 1648 (class 1259 OID 108100)
-- Dependencies: 6
-- Name: serviceaccesstoken; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE serviceaccesstoken (
    id integer NOT NULL,
    serviceid character varying(255),
    tokenkey character varying(255),
    tokensecret character varying(255),
    userid character varying(255)
);


--
-- TOC entry 1649 (class 1259 OID 108106)
-- Dependencies: 1648 6
-- Name: serviceaccesstoken_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE serviceaccesstoken_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2124 (class 0 OID 0)
-- Dependencies: 1649
-- Name: serviceaccesstoken_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE serviceaccesstoken_id_seq OWNED BY serviceaccesstoken.id;


--
-- TOC entry 1612 (class 1259 OID 107975)
-- Dependencies: 6
-- Name: stagingaccesspointconfiguration; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

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


--
-- TOC entry 1613 (class 1259 OID 107981)
-- Dependencies: 6 1612
-- Name: stagingaccesspointconfiguration_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE stagingaccesspointconfiguration_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2125 (class 0 OID 0)
-- Dependencies: 1613
-- Name: stagingaccesspointconfiguration_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE stagingaccesspointconfiguration_id_seq OWNED BY stagingaccesspointconfiguration.id;


--
-- TOC entry 1667 (class 1259 OID 108411)
-- Dependencies: 6
-- Name: stagingaccessproviderconfiguration; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE stagingaccessproviderconfiguration (
    id integer NOT NULL,
    customproperties character varying(1024),
    defaultprovider boolean,
    description character varying(1024),
    disabled boolean,
    groupid character varying(255),
    implementationclass character varying(255),
    logourl character varying(255),
    name character varying(255),
    uniqueidentifier character varying(255) NOT NULL
);


--
-- TOC entry 1666 (class 1259 OID 108409)
-- Dependencies: 6 1667
-- Name: stagingaccessproviderconfiguration_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE stagingaccessproviderconfiguration_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2126 (class 0 OID 0)
-- Dependencies: 1666
-- Name: stagingaccessproviderconfiguration_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE stagingaccessproviderconfiguration_id_seq OWNED BY stagingaccessproviderconfiguration.id;


--
-- TOC entry 1669 (class 1259 OID 108424)
-- Dependencies: 6
-- Name: stagingprocessor; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE stagingprocessor (
    id integer NOT NULL,
    defaulton boolean,
    description character varying(1024),
    disabled boolean,
    groupid character varying(255),
    implementationclass character varying(255),
    name character varying(255),
    properties character varying(1024),
    type character varying(255),
    uniqueidentifier character varying(2,55) NOT NULL
    downloadprocessingsupported boolean,
    ingestprocessingsupported boolean
);


--
-- TOC entry 1668 (class 1259 OID 108422)
-- Dependencies: 1669 6
-- Name: stagingprocessor_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE stagingprocessor_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2127 (class 0 OID 0)
-- Dependencies: 1668
-- Name: stagingprocessor_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE stagingprocessor_id_seq OWNED BY stagingprocessor.id;


--
-- TOC entry 1650 (class 1259 OID 108108)
-- Dependencies: 6
-- Name: study; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE study (
    studyid integer NOT NULL,
    enddate timestamp without time zone,
    legalnote character varying(1024),
    note character varying(1024),
    startdate timestamp without time zone,
    topic character varying(255),
    uniqueidentifier character varying(255) NOT NULL,
    visible boolean,
    manager_userid bigint
);


--
-- TOC entry 1651 (class 1259 OID 108114)
-- Dependencies: 6
-- Name: study_relation; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE study_relation (
    study_studyid bigint NOT NULL,
    organizationunits_relationid bigint NOT NULL
);


--
-- TOC entry 1652 (class 1259 OID 108117)
-- Dependencies: 6 1650
-- Name: study_studyid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE study_studyid_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2128 (class 0 OID 0)
-- Dependencies: 1652
-- Name: study_studyid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE study_studyid_seq OWNED BY study.studyid;


--
-- TOC entry 1653 (class 1259 OID 108119)
-- Dependencies: 6
-- Name: task; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE task (
    taskid integer NOT NULL,
    task character varying(255)
);


--
-- TOC entry 1654 (class 1259 OID 108122)
-- Dependencies: 6 1653
-- Name: task_taskid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE task_taskid_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2129 (class 0 OID 0)
-- Dependencies: 1654
-- Name: task_taskid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE task_taskid_seq OWNED BY task.taskid;


--
-- TOC entry 1655 (class 1259 OID 108124)
-- Dependencies: 6
-- Name: userdata; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE userdata (
    userid integer NOT NULL,
    distinguishedname character varying(255),
    email character varying(255),
    firstname character varying(255),
    lastname character varying(255),
    validfrom timestamp without time zone,
    validuntil timestamp without time zone
);


--
-- TOC entry 1656 (class 1259 OID 108130)
-- Dependencies: 1655 6
-- Name: userdata_userid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE userdata_userid_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2130 (class 0 OID 0)
-- Dependencies: 1656
-- Name: userdata_userid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE userdata_userid_seq OWNED BY userdata.userid;


--
-- TOC entry 1657 (class 1259 OID 108132)
-- Dependencies: 6
-- Name: userproperty; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE userproperty (
    id integer NOT NULL,
    propertykey character varying(255),
    propertyvalue character varying(255)
);


--
-- TOC entry 1658 (class 1259 OID 108138)
-- Dependencies: 1657 6
-- Name: userproperty_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE userproperty_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2131 (class 0 OID 0)
-- Dependencies: 1658
-- Name: userproperty_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE userproperty_id_seq OWNED BY userproperty.id;


--
-- TOC entry 1659 (class 1259 OID 108140)
-- Dependencies: 6
-- Name: userpropertycollection; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE userpropertycollection (
    id integer NOT NULL,
    collectionidentifier character varying(255),
    userid character varying(255)
);


--
-- TOC entry 1660 (class 1259 OID 108146)
-- Dependencies: 1659 6
-- Name: userpropertycollection_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE userpropertycollection_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2132 (class 0 OID 0)
-- Dependencies: 1660
-- Name: userpropertycollection_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE userpropertycollection_id_seq OWNED BY userpropertycollection.id;


--
-- TOC entry 1661 (class 1259 OID 108148)
-- Dependencies: 6
-- Name: userpropertycollection_userproperty; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE userpropertycollection_userproperty (
    userpropertycollection_id bigint NOT NULL,
    properties_id bigint NOT NULL
);


--
-- TOC entry 1662 (class 1259 OID 108151)
-- Dependencies: 1622 6
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2133 (class 0 OID 0)
-- Dependencies: 1662
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE users_id_seq OWNED BY users.id;


--
-- TOC entry 1971 (class 2604 OID 108170)
-- Dependencies: 1656 1655
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ingestinformation ALTER COLUMN id SET DEFAULT nextval('ingestinformation_id_seq'::regclass);

--
-- TOC entry 1971 (class 2604 OID 108170)
-- Dependencies: 1656 1655
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE downloadinformation ALTER COLUMN id SET DEFAULT nextval('downloadinformation_id_seq'::regclass);


--
-- TOC entry 1953 (class 2604 OID 108153)
-- Dependencies: 1615 1614
-- Name: baseid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE digitalobject ALTER COLUMN baseid SET DEFAULT nextval('digitalobject_baseid_seq'::regclass);

--
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY digitalobjecttype ALTER COLUMN id SET DEFAULT nextval('digitalobjecttype_id_seq'::regclass);

--
-- TOC entry 1954 (class 2604 OID 108154)
-- Dependencies: 1626 1617
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE grants ALTER COLUMN id SET DEFAULT nextval('grants_id_seq'::regclass);


--
-- TOC entry 1955 (class 2604 OID 108155)
-- Dependencies: 1627 1618
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE grantsets ALTER COLUMN id SET DEFAULT nextval('grantsets_id_seq'::regclass);


--
-- TOC entry 1956 (class 2604 OID 108156)
-- Dependencies: 1628 1619
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE groups ALTER COLUMN id SET DEFAULT nextval('groups_id_seq'::regclass);


--
-- TOC entry 1961 (class 2604 OID 108157)
-- Dependencies: 1630 1629
-- Name: investigationid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE investigation ALTER COLUMN investigationid SET DEFAULT nextval('investigation_investigationid_seq'::regclass);


--
-- TOC entry 1962 (class 2604 OID 108158)
-- Dependencies: 1634 1633
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE usergroup ALTER COLUMN id SET DEFAULT nextval('usergroup_id_seq'::regclass);


--
-- TOC entry 1957 (class 2604 OID 108159)
-- Dependencies: 1635 1620
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE memberships ALTER COLUMN id SET DEFAULT nextval('memberships_id_seq'::regclass);


--
-- TOC entry 1963 (class 2604 OID 108160)
-- Dependencies: 1637 1636
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE metadataindexingtask ALTER COLUMN id SET DEFAULT nextval('metadataindexingtask_id_seq'::regclass);


--
-- TOC entry 1964 (class 2604 OID 108161)
-- Dependencies: 1639 1638
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE metadataschema ALTER COLUMN id SET DEFAULT nextval('metadataschema_id_seq'::regclass);


--
-- TOC entry 1965 (class 2604 OID 108162)
-- Dependencies: 1641 1640
-- Name: organizationunitid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE organizationunit ALTER COLUMN organizationunitid SET DEFAULT nextval('organizationunit_organizationunitid_seq'::regclass);


--
-- TOC entry 1966 (class 2604 OID 108163)
-- Dependencies: 1643 1642
-- Name: participantid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE participant ALTER COLUMN participantid SET DEFAULT nextval('participant_participantid_seq'::regclass);


--
-- TOC entry 1976 (class 2604 OID 108455)
-- Dependencies: 1671 1672 1672
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE publicationentry ALTER COLUMN id SET DEFAULT nextval('publicationentry_id_seq'::regclass);


--
-- TOC entry 1967 (class 2604 OID 108164)
-- Dependencies: 1645 1644
-- Name: relationid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE relation ALTER COLUMN relationid SET DEFAULT nextval('relation_relationid_seq'::regclass);


--
-- TOC entry 1958 (class 2604 OID 108165)
-- Dependencies: 1646 1621
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE resourcereferences ALTER COLUMN id SET DEFAULT nextval('resourcereferences_id_seq'::regclass);


--
-- TOC entry 1960 (class 2604 OID 108166)
-- Dependencies: 1647 1624
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE resources ALTER COLUMN id SET DEFAULT nextval('resources_id_seq'::regclass);


--
-- TOC entry 1968 (class 2604 OID 108167)
-- Dependencies: 1649 1648
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE serviceaccesstoken ALTER COLUMN id SET DEFAULT nextval('serviceaccesstoken_id_seq'::regclass);


--
-- TOC entry 1952 (class 2604 OID 107983)
-- Dependencies: 1613 1612
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE stagingaccesspointconfiguration ALTER COLUMN id SET DEFAULT nextval('stagingaccesspointconfiguration_id_seq'::regclass);


--
-- TOC entry 1974 (class 2604 OID 108414)
-- Dependencies: 1667 1666 1667
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE stagingaccessproviderconfiguration ALTER COLUMN id SET DEFAULT nextval('stagingaccessproviderconfiguration_id_seq'::regclass);


--
-- TOC entry 1975 (class 2604 OID 108427)
-- Dependencies: 1669 1668 1669
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE stagingprocessor ALTER COLUMN id SET DEFAULT nextval('stagingprocessor_id_seq'::regclass);


--
-- TOC entry 1969 (class 2604 OID 108168)
-- Dependencies: 1652 1650
-- Name: studyid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE study ALTER COLUMN studyid SET DEFAULT nextval('study_studyid_seq'::regclass);


--
-- TOC entry 1970 (class 2604 OID 108169)
-- Dependencies: 1654 1653
-- Name: taskid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE task ALTER COLUMN taskid SET DEFAULT nextval('task_taskid_seq'::regclass);


--
-- TOC entry 1971 (class 2604 OID 108170)
-- Dependencies: 1656 1655
-- Name: userid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE userdata ALTER COLUMN userid SET DEFAULT nextval('userdata_userid_seq'::regclass);


--
-- TOC entry 1972 (class 2604 OID 108171)
-- Dependencies: 1658 1657
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE userproperty ALTER COLUMN id SET DEFAULT nextval('userproperty_id_seq'::regclass);


--
-- TOC entry 1973 (class 2604 OID 108172)
-- Dependencies: 1660 1659
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE userpropertycollection ALTER COLUMN id SET DEFAULT nextval('userpropertycollection_id_seq'::regclass);


--
-- TOC entry 1959 (class 2604 OID 108173)
-- Dependencies: 1662 1622
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE users ALTER COLUMN id SET DEFAULT nextval('users_id_seq'::regclass);


--
-- TOC entry 2054 (class 2606 OID 108390)
-- Dependencies: 1663 1663 1663 1663 1663
-- Name: attribute_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY attribute
    ADD CONSTRAINT attribute_pkey PRIMARY KEY (id, digit_obj_id, viewname, stepnoarrived);


--
-- TOC entry 2056 (class 2606 OID 108398)
-- Dependencies: 1664 1664 1664 1664
-- Name: dataorganizationnode_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY dataorganizationnode
    ADD CONSTRAINT dataorganizationnode_pkey PRIMARY KEY (stepnoarrived, viewname, digit_obj_id);


--
-- TOC entry 1986 (class 2606 OID 108175)
-- Dependencies: 1614 1614
-- Name: digitalobject_digitalobjectidentifier_key; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY digitalobject
    ADD CONSTRAINT digitalobject_digitalobjectidentifier_key UNIQUE (digitalobjectidentifier);


--
-- TOC entry 1988 (class 2606 OID 108177)
-- Dependencies: 1614 1614
-- Name: digitalobject_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY digitalobject
    ADD CONSTRAINT digitalobject_pkey PRIMARY KEY (baseid);


--
-- TOC entry 1990 (class 2606 OID 108179)
-- Dependencies: 1616 1616 1616
-- Name: digitalobject_userdata_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY digitalobject_userdata
    ADD CONSTRAINT digitalobject_userdata_pkey PRIMARY KEY (digitalobject_baseid, experimenters_userid);

--
-- Name: digitalobjecttype_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY digitalobjecttype
    ADD CONSTRAINT digitalobjecttype_pkey PRIMARY KEY (id);

--
-- TOC entry 1980 (class 2606 OID 107974)
-- Dependencies: 1611 1611
-- Name: downloadinformation_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY downloadinformation
    ADD CONSTRAINT downloadinformation_pkey PRIMARY KEY (id);


--
-- TOC entry 1992 (class 2606 OID 108181)
-- Dependencies: 1617 1617
-- Name: grants_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY grants
    ADD CONSTRAINT grants_pkey PRIMARY KEY (id);


--
-- TOC entry 1994 (class 2606 OID 108183)
-- Dependencies: 1618 1618
-- Name: grantsets_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY grantsets
    ADD CONSTRAINT grantsets_pkey PRIMARY KEY (id);


--
-- TOC entry 1996 (class 2606 OID 108185)
-- Dependencies: 1619 1619
-- Name: groups_groupid_key; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY groups
    ADD CONSTRAINT groups_groupid_key UNIQUE (groupid);


--
-- TOC entry 1998 (class 2606 OID 108187)
-- Dependencies: 1619 1619
-- Name: groups_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY groups
    ADD CONSTRAINT groups_pkey PRIMARY KEY (id);


--
-- TOC entry 1978 (class 2606 OID 107966)
-- Dependencies: 1610 1610
-- Name: ingestinformation_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ingestinformation
    ADD CONSTRAINT ingestinformation_pkey PRIMARY KEY (id);


--
-- TOC entry 2068 (class 2606 OID 108439)
-- Dependencies: 1670 1670 1670
-- Name: ingestinformation_stagingprocessor_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ingestinformation_stagingprocessor
    ADD CONSTRAINT ingestinformation_stagingprocessor_pkey PRIMARY KEY (ingestinformation_id, stagingprocessors_id);


--
-- TOC entry 2018 (class 2606 OID 108189)
-- Dependencies: 1631 1631 1631
-- Name: investigation_metadataschema_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY investigation_metadataschema
    ADD CONSTRAINT investigation_metadataschema_pkey PRIMARY KEY (investigation_investigationid, metadataschema_id);


--
-- TOC entry 2020 (class 2606 OID 108191)
-- Dependencies: 1632 1632 1632
-- Name: investigation_participant_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY investigation_participant
    ADD CONSTRAINT investigation_participant_pkey PRIMARY KEY (investigation_investigationid, participants_participantid);


--
-- TOC entry 2014 (class 2606 OID 108193)
-- Dependencies: 1629 1629
-- Name: investigation_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY investigation
    ADD CONSTRAINT investigation_pkey PRIMARY KEY (investigationid);


--
-- TOC entry 2016 (class 2606 OID 108195)
-- Dependencies: 1629 1629
-- Name: investigation_uniqueidentifier_key; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY investigation
    ADD CONSTRAINT investigation_uniqueidentifier_key UNIQUE (uniqueidentifier);


--
-- TOC entry 2022 (class 2606 OID 108197)
-- Dependencies: 1633 1633
-- Name: usergroup_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY usergroup
    ADD CONSTRAINT usergroup_pkey PRIMARY KEY (id);


--
-- TOC entry 2000 (class 2606 OID 108199)
-- Dependencies: 1620 1620
-- Name: memberships_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY memberships
    ADD CONSTRAINT memberships_pkey PRIMARY KEY (id);


--
-- TOC entry 2024 (class 2606 OID 108201)
-- Dependencies: 1636 1636
-- Name: metadataindexingtask_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY metadataindexingtask
    ADD CONSTRAINT metadataindexingtask_pkey PRIMARY KEY (id);


--
-- TOC entry 2026 (class 2606 OID 108203)
-- Dependencies: 1638 1638
-- Name: metadataschema_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY metadataschema
    ADD CONSTRAINT metadataschema_pkey PRIMARY KEY (id);


--
-- TOC entry 2028 (class 2606 OID 108205)
-- Dependencies: 1638 1638
-- Name: metadataschema_schemaidentifier_key; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY metadataschema
    ADD CONSTRAINT metadataschema_schemaidentifier_key UNIQUE (schemaidentifier);


--
-- TOC entry 2030 (class 2606 OID 108207)
-- Dependencies: 1640 1640
-- Name: organizationunit_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY organizationunit
    ADD CONSTRAINT organizationunit_pkey PRIMARY KEY (organizationunitid);


--
-- TOC entry 2032 (class 2606 OID 108209)
-- Dependencies: 1642 1642
-- Name: participant_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY participant
    ADD CONSTRAINT participant_pkey PRIMARY KEY (participantid);


--
-- TOC entry 2070 (class 2606 OID 108462)
-- Dependencies: 1672 1672
-- Name: publicationentry_identifier_key; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY publicationentry
    ADD CONSTRAINT publicationentry_identifier_key UNIQUE (identifier);


--
-- TOC entry 2072 (class 2606 OID 108460)
-- Dependencies: 1672 1672
-- Name: publicationentry_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY publicationentry
    ADD CONSTRAINT publicationentry_pkey PRIMARY KEY (id);


--
-- TOC entry 2034 (class 2606 OID 108211)
-- Dependencies: 1644 1644
-- Name: relation_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY relation
    ADD CONSTRAINT relation_pkey PRIMARY KEY (relationid);


--
-- TOC entry 2002 (class 2606 OID 108213)
-- Dependencies: 1621 1621
-- Name: resourcereferences_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY resourcereferences
    ADD CONSTRAINT resourcereferences_pkey PRIMARY KEY (id);


--
-- TOC entry 2010 (class 2606 OID 108215)
-- Dependencies: 1624 1624
-- Name: resources_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY resources
    ADD CONSTRAINT resources_pkey PRIMARY KEY (id);


--
-- TOC entry 2058 (class 2606 OID 108408)
-- Dependencies: 1665 1665
-- Name: sequence_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY sequence
    ADD CONSTRAINT sequence_pkey PRIMARY KEY (seq_name);


--
-- TOC entry 2036 (class 2606 OID 108217)
-- Dependencies: 1648 1648
-- Name: serviceaccesstoken_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY serviceaccesstoken
    ADD CONSTRAINT serviceaccesstoken_pkey PRIMARY KEY (id);


--
-- TOC entry 1982 (class 2606 OID 107985)
-- Dependencies: 1612 1612
-- Name: stagingaccesspointconfiguration_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY stagingaccesspointconfiguration
    ADD CONSTRAINT stagingaccesspointconfiguration_pkey PRIMARY KEY (id);


--
-- TOC entry 1984 (class 2606 OID 107987)
-- Dependencies: 1612 1612
-- Name: stagingaccesspointconfiguration_uniqueidentifier_key; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY stagingaccesspointconfiguration
    ADD CONSTRAINT stagingaccesspointconfiguration_uniqueidentifier_key UNIQUE (uniqueidentifier);


--
-- TOC entry 2060 (class 2606 OID 108419)
-- Dependencies: 1667 1667
-- Name: stagingaccessproviderconfiguration_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY stagingaccessproviderconfiguration
    ADD CONSTRAINT stagingaccessproviderconfiguration_pkey PRIMARY KEY (id);


--
-- TOC entry 2062 (class 2606 OID 108421)
-- Dependencies: 1667 1667
-- Name: stagingaccessproviderconfiguration_uniqueidentifier_key; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY stagingaccessproviderconfiguration
    ADD CONSTRAINT stagingaccessproviderconfiguration_uniqueidentifier_key UNIQUE (uniqueidentifier);


--
-- TOC entry 2064 (class 2606 OID 108432)
-- Dependencies: 1669 1669
-- Name: stagingprocessor_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY stagingprocessor
    ADD CONSTRAINT stagingprocessor_pkey PRIMARY KEY (id);


--
-- TOC entry 2066 (class 2606 OID 108434)
-- Dependencies: 1669 1669
-- Name: stagingprocessor_uniqueidentifier_key; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY stagingprocessor
    ADD CONSTRAINT stagingprocessor_uniqueidentifier_key UNIQUE (uniqueidentifier);


--
-- TOC entry 2038 (class 2606 OID 108219)
-- Dependencies: 1650 1650
-- Name: study_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY study
    ADD CONSTRAINT study_pkey PRIMARY KEY (studyid);


--
-- TOC entry 2042 (class 2606 OID 108221)
-- Dependencies: 1651 1651 1651
-- Name: study_relation_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY study_relation
    ADD CONSTRAINT study_relation_pkey PRIMARY KEY (study_studyid, organizationunits_relationid);


--
-- TOC entry 2040 (class 2606 OID 108223)
-- Dependencies: 1650 1650
-- Name: study_uniqueidentifier_key; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY study
    ADD CONSTRAINT study_uniqueidentifier_key UNIQUE (uniqueidentifier);


--
-- TOC entry 2044 (class 2606 OID 108225)
-- Dependencies: 1653 1653
-- Name: task_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY task
    ADD CONSTRAINT task_pkey PRIMARY KEY (taskid);


--
-- TOC entry 2004 (class 2606 OID 108227)
-- Dependencies: 1621 1621 1621
-- Name: unq_resourcereferences_0; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY resourcereferences
    ADD CONSTRAINT unq_resourcereferences_0 UNIQUE (resource_id, group_id);


--
-- TOC entry 2012 (class 2606 OID 108229)
-- Dependencies: 1624 1624 1624
-- Name: unq_resources_0; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY resources
    ADD CONSTRAINT unq_resources_0 UNIQUE (domainuniqueid, domainid);


--
-- TOC entry 2046 (class 2606 OID 108231)
-- Dependencies: 1655 1655
-- Name: userdata_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY userdata
    ADD CONSTRAINT userdata_pkey PRIMARY KEY (userid);


--
-- TOC entry 2048 (class 2606 OID 108233)
-- Dependencies: 1657 1657
-- Name: userproperty_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY userproperty
    ADD CONSTRAINT userproperty_pkey PRIMARY KEY (id);


--
-- TOC entry 2050 (class 2606 OID 108235)
-- Dependencies: 1659 1659
-- Name: userpropertycollection_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY userpropertycollection
    ADD CONSTRAINT userpropertycollection_pkey PRIMARY KEY (id);


--
-- TOC entry 2052 (class 2606 OID 108237)
-- Dependencies: 1661 1661 1661
-- Name: userpropertycollection_userproperty_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY userpropertycollection_userproperty
    ADD CONSTRAINT userpropertycollection_userproperty_pkey PRIMARY KEY (userpropertycollection_id, properties_id);


--
-- TOC entry 2006 (class 2606 OID 108239)
-- Dependencies: 1622 1622
-- Name: users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- TOC entry 2008 (class 2606 OID 108241)
-- Dependencies: 1622 1622
-- Name: users_userid_key; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_userid_key UNIQUE (userid);


--
-- TOC entry 2101 (class 2606 OID 108399)
-- Dependencies: 1664 2055 1664 1664 1663 1663 1663
-- Name: fk_attribute_stepnoarrived; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY attribute
    ADD CONSTRAINT fk_attribute_stepnoarrived FOREIGN KEY (stepnoarrived, viewname, digit_obj_id) REFERENCES dataorganizationnode(stepnoarrived, viewname, digit_obj_id);


--
-- TOC entry 2073 (class 2606 OID 108242)
-- Dependencies: 2013 1614 1629
-- Name: fk_digitalobject_investigation_investigationid; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY digitalobject
    ADD CONSTRAINT fk_digitalobject_investigation_investigationid FOREIGN KEY (investigation_investigationid) REFERENCES investigation(investigationid);


--
-- TOC entry 2074 (class 2606 OID 108247)
-- Dependencies: 2045 1655 1614
-- Name: fk_digitalobject_uploader_userid; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY digitalobject
    ADD CONSTRAINT fk_digitalobject_uploader_userid FOREIGN KEY (uploader_userid) REFERENCES userdata(userid);


--
-- TOC entry 2075 (class 2606 OID 108252)
-- Dependencies: 1987 1614 1616
-- Name: fk_digitalobject_userdata_digitalobject_baseid; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY digitalobject_userdata
    ADD CONSTRAINT fk_digitalobject_userdata_digitalobject_baseid FOREIGN KEY (digitalobject_baseid) REFERENCES digitalobject(baseid);


--
-- TOC entry 2076 (class 2606 OID 108257)
-- Dependencies: 1616 1655 2045
-- Name: fk_digitalobject_userdata_experimenters_userid; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY digitalobject_userdata
    ADD CONSTRAINT fk_digitalobject_userdata_experimenters_userid FOREIGN KEY (experimenters_userid) REFERENCES userdata(userid);


--
-- TOC entry 2077 (class 2606 OID 108262)
-- Dependencies: 1617 1622 2005
-- Name: fk_grants_grantee_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY grants
    ADD CONSTRAINT fk_grants_grantee_id FOREIGN KEY (grantee_id) REFERENCES users(id);


--
-- TOC entry 2078 (class 2606 OID 108267)
-- Dependencies: 1617 1993 1618
-- Name: fk_grants_grants_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY grants
    ADD CONSTRAINT fk_grants_grants_id FOREIGN KEY (grants_id) REFERENCES grantsets(id);


--
-- TOC entry 2079 (class 2606 OID 108272)
-- Dependencies: 1618 2009 1624
-- Name: fk_grantsets_resource_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY grantsets
    ADD CONSTRAINT fk_grantsets_resource_id FOREIGN KEY (resource_id) REFERENCES resources(id);


--
-- TOC entry 2103 (class 2606 OID 108445)
-- Dependencies: 1977 1670 1610
-- Name: fk_ingestinformation_stagingprocessor_ingestinformation_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ingestinformation_stagingprocessor
    ADD CONSTRAINT fk_ingestinformation_stagingprocessor_ingestinformation_id FOREIGN KEY (ingestinformation_id) REFERENCES ingestinformation(id);


--
-- TOC entry 2102 (class 2606 OID 108440)
-- Dependencies: 1669 1670 2063
-- Name: fk_ingestinformation_stagingprocessor_stagingprocessors_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ingestinformation_stagingprocessor
    ADD CONSTRAINT fk_ingestinformation_stagingprocessor_stagingprocessors_id FOREIGN KEY (stagingprocessors_id) REFERENCES stagingprocessor(id);


--
-- TOC entry 2086 (class 2606 OID 108277)
-- Dependencies: 1629 2013 1631
-- Name: fk_investigation_metadataschema_investigation_investigationid; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY investigation_metadataschema
    ADD CONSTRAINT fk_investigation_metadataschema_investigation_investigationid FOREIGN KEY (investigation_investigationid) REFERENCES investigation(investigationid);


--
-- TOC entry 2087 (class 2606 OID 108282)
-- Dependencies: 1631 2025 1638
-- Name: fk_investigation_metadataschema_metadataschema_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY investigation_metadataschema
    ADD CONSTRAINT fk_investigation_metadataschema_metadataschema_id FOREIGN KEY (metadataschema_id) REFERENCES metadataschema(id);


--
-- TOC entry 2088 (class 2606 OID 108287)
-- Dependencies: 2013 1629 1632
-- Name: fk_investigation_participant_investigation_investigationid; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY investigation_participant
    ADD CONSTRAINT fk_investigation_participant_investigation_investigationid FOREIGN KEY (investigation_investigationid) REFERENCES investigation(investigationid);


--
-- TOC entry 2089 (class 2606 OID 108292)
-- Dependencies: 2031 1642 1632
-- Name: fk_investigation_participant_participants_participantid; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY investigation_participant
    ADD CONSTRAINT fk_investigation_participant_participants_participantid FOREIGN KEY (participants_participantid) REFERENCES participant(participantid);


--
-- TOC entry 2085 (class 2606 OID 108297)
-- Dependencies: 1650 2037 1629
-- Name: fk_investigation_study_studyid; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY investigation
    ADD CONSTRAINT fk_investigation_study_studyid FOREIGN KEY (study_studyid) REFERENCES study(studyid);


--
-- TOC entry 2080 (class 2606 OID 108302)
-- Dependencies: 1620 1619 1997
-- Name: fk_memberships_group_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY memberships
    ADD CONSTRAINT fk_memberships_group_id FOREIGN KEY (group_id) REFERENCES groups(id);


--
-- TOC entry 2081 (class 2606 OID 108307)
-- Dependencies: 1620 2005 1622
-- Name: fk_memberships_user_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY memberships
    ADD CONSTRAINT fk_memberships_user_id FOREIGN KEY (user_id) REFERENCES users(id);


--
-- TOC entry 2090 (class 2606 OID 108312)
-- Dependencies: 1636 1638 2025
-- Name: fk_metadataindexingtask_schemareference_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY metadataindexingtask
    ADD CONSTRAINT fk_metadataindexingtask_schemareference_id FOREIGN KEY (schemareference_id) REFERENCES metadataschema(id);


--
-- TOC entry 2091 (class 2606 OID 108317)
-- Dependencies: 2045 1655 1640
-- Name: fk_organizationunit_manager_userid; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY organizationunit
    ADD CONSTRAINT fk_organizationunit_manager_userid FOREIGN KEY (manager_userid) REFERENCES userdata(userid);


--
-- TOC entry 2092 (class 2606 OID 108322)
-- Dependencies: 2043 1653 1642
-- Name: fk_participant_task_taskid; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY participant
    ADD CONSTRAINT fk_participant_task_taskid FOREIGN KEY (task_taskid) REFERENCES task(taskid);


--
-- TOC entry 2093 (class 2606 OID 108327)
-- Dependencies: 1642 1655 2045
-- Name: fk_participant_user_userid; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY participant
    ADD CONSTRAINT fk_participant_user_userid FOREIGN KEY (user_userid) REFERENCES userdata(userid);


--
-- TOC entry 2094 (class 2606 OID 108332)
-- Dependencies: 2029 1644 1640
-- Name: fk_relation_organizationunit_organizationunitid; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY relation
    ADD CONSTRAINT fk_relation_organizationunit_organizationunitid FOREIGN KEY (organizationunit_organizationunitid) REFERENCES organizationunit(organizationunitid);


--
-- TOC entry 2095 (class 2606 OID 108337)
-- Dependencies: 1644 1653 2043
-- Name: fk_relation_task_taskid; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY relation
    ADD CONSTRAINT fk_relation_task_taskid FOREIGN KEY (task_taskid) REFERENCES task(taskid);


--
-- TOC entry 2082 (class 2606 OID 108342)
-- Dependencies: 1619 1621 1997
-- Name: fk_resourcereferences_group_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY resourcereferences
    ADD CONSTRAINT fk_resourcereferences_group_id FOREIGN KEY (group_id) REFERENCES groups(id);


--
-- TOC entry 2083 (class 2606 OID 108347)
-- Dependencies: 1621 1624 2009
-- Name: fk_resourcereferences_resource_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY resourcereferences
    ADD CONSTRAINT fk_resourcereferences_resource_id FOREIGN KEY (resource_id) REFERENCES resources(id);


--
-- TOC entry 2084 (class 2606 OID 108352)
-- Dependencies: 1993 1624 1618
-- Name: fk_resources_grantset_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY resources
    ADD CONSTRAINT fk_resources_grantset_id FOREIGN KEY (grantset_id) REFERENCES grantsets(id);


--
-- TOC entry 2096 (class 2606 OID 108357)
-- Dependencies: 1655 1650 2045
-- Name: fk_study_manager_userid; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY study
    ADD CONSTRAINT fk_study_manager_userid FOREIGN KEY (manager_userid) REFERENCES userdata(userid);


--
-- TOC entry 2097 (class 2606 OID 108362)
-- Dependencies: 1644 2033 1651
-- Name: fk_study_relation_organizationunits_relationid; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY study_relation
    ADD CONSTRAINT fk_study_relation_organizationunits_relationid FOREIGN KEY (organizationunits_relationid) REFERENCES relation(relationid);


--
-- TOC entry 2098 (class 2606 OID 108367)
-- Dependencies: 1651 2037 1650
-- Name: fk_study_relation_study_studyid; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY study_relation
    ADD CONSTRAINT fk_study_relation_study_studyid FOREIGN KEY (study_studyid) REFERENCES study(studyid);


--
-- TOC entry 2099 (class 2606 OID 108372)
-- Dependencies: 1661 2047 1657
-- Name: fk_userpropertycollection_userproperty_properties_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY userpropertycollection_userproperty
    ADD CONSTRAINT fk_userpropertycollection_userproperty_properties_id FOREIGN KEY (properties_id) REFERENCES userproperty(id);


--
-- TOC entry 2100 (class 2606 OID 108377)
-- Dependencies: 1661 2049 1659
-- Name: userpropertycollection_userproperty_userpropertycollection_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY userpropertycollection_userproperty
    ADD CONSTRAINT userpropertycollection_userproperty_userpropertycollection_id FOREIGN KEY (userpropertycollection_id) REFERENCES userpropertycollection(id);


--
-- TOC entry 2108 (class 0 OID 0)
-- Dependencies: 6
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2014-11-06 13:55:11

--
-- PostgreSQL database dump complete
--


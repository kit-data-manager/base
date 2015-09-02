-- Create script for KIT Data Manager Authorization views combining.
-- These views are used to bring users, groups, resources and special grants into relation to make the results
-- accessible easily via Java entities.

CREATE VIEW FilterHelper_hack AS
(SELECT ut.USERID as USERID, gt.GROUPID as GROUPID, grst.RESOURCE_ID as RESOURCEID, LEAST(ut.MAXIMUMROLE, grt.GRANTEDROLE, grst.ROLERESTRICTION) as ROLE
		FROM Users ut, Groups gt, Grants grt, GrantSets grst, Memberships mt
		WHERE grt.GRANTEE_ID = ut.ID AND grst.ID = grt.GRANTS_ID AND ut.ID = mt.USER_ID AND gt.ID = mt.GROUP_ID)
UNION
(SELECT ut.USERID as USERID, gt.GROUPID as GROUPID, rrt.RESOURCE_ID as RESOURCEID, LEAST( ut.MAXIMUMROLE, mt.membersRole, rrt.ROLE_RESTRICTION) as ROLE
		FROM Users ut, Groups gt, Memberships mt, ResourceReferences rrt
		WHERE ut.ID = mt.USER_ID AND gt.ID = mt.GROUP_ID AND gt.ID = rrt.GROUP_ID);

CREATE VIEW FilterHelper AS
SELECT USERID, GROUPID, DOMAINID, DOMAINUNIQUEID,  max(ROLE) AS POSSESSED_ROLE
FROM FilterHelper_hack fh, Resources res WHERE fh.RESOURCEID=res.ID  GROUP BY USERID, GROUPID, DOMAINUNIQUEID, DOMAINID;

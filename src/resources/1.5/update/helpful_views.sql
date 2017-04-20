-- View for listing user-group-memberships and the effective role, which is the minimum of the user's max. role and the in-group role.
-- Attention: If there is no UserGroup row for a Group row, the group won't appear in the view.
CREATE OR REPLACE VIEW usergroupmemberships AS 
SELECT u.userid,
    g.groupid,
    ug.groupName,
    ug.description,
       LEAST(u.maximumrole, m.membersrole) AS "effectiveRole"
   FROM users u,
    groups g,
    memberships m,
    usergroup ug
  WHERE u.id = m.user_id AND g.id = m.group_id AND ug.groupId=g.groupId ORDER BY u.id, g.id;
  
-- View containing all resource references ordered by groups. The role restriction in the last column shows, which is the maximum role a group
-- is entitled to use for accessing the associated resource.
CREATE OR REPLACE VIEW groupresourcereferences AS 
 SELECT g.groupId,
 r.domainId,
 r.domainUniqueId,
 rr.role_restriction
   FROM Resources r,
    Groups g,
    ResourceReferences rr
  WHERE rr.resource_id = r.id AND rr.group_id = g.id ORDER BY g.id, r.id;
  
-- View containing all resource grants ordered by user and resource id. 
-- "grantedRole"  is the minimum of the user's max. role and the granted role for the resource.
CREATE OR REPLACE VIEW usergrants AS 
 SELECT u.userId,
 r.domainId,
 r.domainUniqueId,
  LEAST(g.grantedRole, u.maximumrole) AS "grantedRole"
   FROM Users u,
   Resources r,
    GrantSets gs,
    Grants g
  WHERE g.grants_id = gs.id AND r.id = gs.resource_id AND g.grantee_id = u.id ORDER BY u.id, r.id;
  
  
-- Listing of all DataOrganizationNodes of type FileNode including their attributes. 
-- The according DataOrganizationNode can be accessed by stepnoarrived, digit_obj_id and viewname. 
-- Attribute nodes can be identified by their id.
CREATE OR REPLACE VIEW filenodes AS 
SELECT n.stepnoarrived,
n.digit_obj_id,
n.viewName,
n.name,
n.value,
a.id,
a.attr_key,
a.attr_value
    FROM DataOrganizationNode n,
    Attribute a
 WHERE n.type='FileNode' AND n.digit_obj_id = a.digit_obj_id AND n.viewName = a.viewName AND n.stepnoarrived = a.stepnoarrived ORDER BY n.digit_obj_id, n.viewName, a.id, a.attr_key;
  
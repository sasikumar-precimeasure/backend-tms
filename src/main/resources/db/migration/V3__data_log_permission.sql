-- New "Data Log" permission menu (historical readings viewer/export) -
-- granted to the existing Super Admin role alongside every other menu, same
-- as V1's original seed. Any other role must have this explicitly granted
-- via Roles > Edit before its users can see the Data Log screen.
insert into permissions (role_id, menu, function, can_read, can_write)
select r.id, 'Data Log', 'manage', true, true
from roles r
where r.name = 'Super Admin';

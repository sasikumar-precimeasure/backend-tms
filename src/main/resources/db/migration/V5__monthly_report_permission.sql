-- New "Monthly Report" permission menu (Settings > Mail Configuration >
-- Monthly Report schedule, recipients, download and send). Granted to the
-- Super Admin role only - any other role must have it explicitly granted
-- via Roles > Edit, same pattern as V3's "Data Log".
insert into permissions (role_id, menu, function, can_read, can_write)
select r.id, 'Monthly Report', 'manage', true, true
from roles r
where r.name = 'Super Admin';

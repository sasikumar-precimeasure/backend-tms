-- When each device was last included in a readings push. Every push carries
-- the browser's full current device list (Settings), so the devices in the
-- most recent push are the CURRENT ones - older same-named leftovers from
-- earlier setups stop being refreshed. Used to limit the monthly report to
-- current devices. Null = not pushed since this column was added.
alter table devices add column last_seen_at timestamptz;

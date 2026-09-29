-- audit_events.device_id must not require the device to already exist in
-- the devices table: a real-time user action (annunciation ack, AVR mode
-- change, ...) can be audited before the frontend's 60s topology push has
-- ever run for that device, and the audit trail must never silently drop
-- an entry just because of that ordering - unlike mail_thresholds, which
-- genuinely needs the device row to exist (it's a per-device setting), an
-- audit log entry is a historical record that should survive even a
-- stale/unknown/later-deleted device reference.
alter table audit_events drop constraint audit_events_device_id_fkey;

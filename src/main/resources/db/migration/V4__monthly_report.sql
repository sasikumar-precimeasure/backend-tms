-- Monthly report (Settings > Mail Configuration > Monthly Report): one
-- app-wide schedule row plus its own recipient list, deliberately separate
-- from mail_recipients (alarm emails) so report readers and on-call alarm
-- recipients can be managed independently.
create table report_settings (
    id integer primary key default 1,
    enabled boolean not null default false,
    -- Capped at 28 so the schedule exists in every month (no Feb 30th).
    day_of_month integer not null default 1 check (day_of_month between 1 and 28),
    send_time time not null default '06:00',
    -- Report slots (00:00, 00:30, ...) and the send time are both in this
    -- zone - the plant's local time, not the server's (which runs in UTC).
    timezone varchar(64) not null default 'Asia/Kolkata',
    -- 'YYYY-MM' of the last month the scheduler sent, so a restart or a
    -- late tick never sends the same month twice.
    last_sent_period varchar(7),
    last_sent_at timestamptz
);

insert into report_settings (id) values (1);

create table report_recipients (
    id varchar(64) primary key,
    name varchar(200) not null,
    email varchar(255) not null,
    enabled boolean not null default true
);

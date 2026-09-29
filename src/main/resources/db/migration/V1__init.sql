-- Core auth/RBAC
create table roles (
    id bigserial primary key,
    name varchar(100) not null unique,
    status boolean not null default true
);

create table permissions (
    id bigserial primary key,
    role_id bigint not null references roles(id) on delete cascade,
    menu varchar(100) not null,
    function varchar(100) not null,
    can_read boolean not null default false,
    can_write boolean not null default false,
    unique (role_id, menu, function)
);

create table users (
    id bigserial primary key,
    user_name varchar(100) not null unique,
    full_name varchar(200),
    email varchar(255) not null unique,
    mobile varchar(30),
    password_hash varchar(255) not null,
    status boolean not null default true,
    created_date timestamptz not null default now(),
    last_login_at timestamptz,
    role_id bigint references roles(id) on delete set null,
    img_url varchar(500),
    timezone varchar(100),
    date_format varchar(50)
);

create table refresh_tokens (
    id bigserial primary key,
    user_id bigint not null references users(id) on delete cascade,
    token_hash varchar(255) not null unique,
    expires_at timestamptz not null,
    revoked boolean not null default false,
    created_at timestamptz not null default now()
);
create index idx_refresh_tokens_user on refresh_tokens(user_id);

create table password_reset_tokens (
    id bigserial primary key,
    user_id bigint not null references users(id) on delete cascade,
    token_hash varchar(255) not null unique,
    expires_at timestamptz not null,
    used boolean not null default false,
    created_at timestamptz not null default now()
);

-- Device topology (mirrors ConnectionSettings.ts: Transformer -> Gateway -> SubDevice)
create table transformers (
    id varchar(64) primary key,
    name varchar(200) not null
);

create table gateways (
    id varchar(64) primary key,
    transformer_id varchar(64) not null references transformers(id) on delete cascade,
    name varchar(200) not null,
    client_id integer not null,
    ip_address varchar(100) not null default '',
    port integer not null default 502
);
create index idx_gateways_transformer on gateways(transformer_id);

create table devices (
    id varchar(64) primary key,
    gateway_id varchar(64) not null references gateways(id) on delete cascade,
    name varchar(200) not null,
    slave_id integer not null,
    device_type varchar(20) not null check (device_type in ('IRTCC', 'DEVICE_2243')),
    enabled boolean not null default true
);
create index idx_devices_gateway on devices(gateway_id);

-- IRTCC readings: one row per pushed 60s snapshot per IRTCC device
create table irtcc_readings (
    id bigserial primary key,
    device_id varchar(64) not null references devices(id) on delete cascade,
    recorded_at timestamptz not null,
    oti_temperature double precision,
    oti_temperature_max double precision,
    wti_temperature double precision,
    wti_temperature_max double precision,
    mog double precision,
    tap_position double precision,
    tap_position_max double precision,
    tap_count double precision,
    pt_voltage double precision,
    actual_pt_voltage double precision,
    operation_mode varchar(20),
    lv_breaker_active boolean,
    hv_breaker_active boolean,
    oltc_local boolean,
    pt_fail_active boolean,
    hooter_active boolean,
    mute_visible boolean,
    avr_mode_is_auto boolean,
    control_fail_active boolean,
    afr_active boolean,
    raise_relay_active boolean,
    lower_relay_active boolean,
    over_volt_active boolean,
    under_volt_active boolean,
    annunciation jsonb,
    annunciation_ack jsonb,
    avr_pt_ratio double precision,
    avr_set_voltage double precision,
    avr_raise_relay_voltage double precision,
    avr_low_relay_voltage double precision,
    avr_hs_forward_voltage double precision,
    avr_hs_backward_voltage double precision,
    avr_over_voltage double precision,
    avr_under_voltage double precision,
    avr_pt_fail_setpoint double precision,
    avr_initial_time double precision,
    avr_sequential_time double precision,
    avr_high_fwd_bwd_time double precision,
    avr_control_fail_time double precision,
    avr_relay_momentary_time double precision,
    unique (device_id, recorded_at)
);
create index idx_irtcc_readings_device_time on irtcc_readings(device_id, recorded_at desc);

-- 2243 readings: one row per pushed 60s snapshot per 2243 device
create table device2243_readings (
    id bigserial primary key,
    device_id varchar(64) not null references devices(id) on delete cascade,
    recorded_at timestamptz not null,
    oti_temperature double precision,
    wti_temperature double precision,
    oti_alarm_setpoint double precision,
    oti_alarm_diff double precision,
    oti_trip_setpoint double precision,
    oti_trip_diff double precision,
    wti_alarm_setpoint double precision,
    wti_alarm_diff double precision,
    wti_trip_setpoint double precision,
    wti_trip_diff double precision,
    wti_fan1_setpoint double precision,
    wti_fan1_diff double precision,
    wti_fan2_setpoint double precision,
    wti_fan2_diff double precision,
    relay_delay double precision,
    unique (device_id, recorded_at)
);
create index idx_device2243_readings_device_time on device2243_readings(device_id, recorded_at desc);

-- Audit / event log
create table audit_events (
    id bigserial primary key,
    occurred_at timestamptz not null default now(),
    user_id bigint references users(id) on delete set null,
    device_id varchar(64) references devices(id) on delete set null,
    event_type varchar(50) not null,
    field_name varchar(100),
    old_value varchar(500),
    new_value varchar(500),
    description varchar(1000),
    raw_payload jsonb
);
create index idx_audit_events_time on audit_events(occurred_at desc);
create index idx_audit_events_user on audit_events(user_id);
create index idx_audit_events_device on audit_events(device_id);

-- Mail configuration
create table mail_sender_settings (
    id integer primary key default 1,
    sender_name varchar(200) not null default '',
    sender_email varchar(255) not null default '',
    smtp_host varchar(255) not null default '',
    smtp_port integer not null default 587,
    password_encrypted varchar(1000) not null default '',
    enable_ssl boolean not null default true,
    constraint mail_sender_settings_singleton check (id = 1)
);
insert into mail_sender_settings (id) values (1);

create table mail_recipients (
    id varchar(64) primary key,
    name varchar(200) not null,
    email varchar(255) not null,
    enabled boolean not null default true
);

create table mail_recipient_devices (
    recipient_id varchar(64) not null references mail_recipients(id) on delete cascade,
    device_id varchar(64) not null references devices(id) on delete cascade,
    primary key (recipient_id, device_id)
);

create table mail_thresholds (
    device_id varchar(64) primary key references devices(id) on delete cascade,
    oti_temp_high double precision not null default 80,
    wti_temp_high double precision not null default 85,
    avr_high double precision not null default 20,
    avr_low double precision not null default 20,
    tap_high double precision not null default 15,
    tap_low double precision not null default 1,
    mail_time_minutes integer not null default 10
);

create table mail_last_sent (
    device_id varchar(64) not null references devices(id) on delete cascade,
    condition_key varchar(50) not null,
    last_sent_at timestamptz not null,
    primary key (device_id, condition_key)
);

-- Seed a default super-admin role with full permissions over every menu
-- used by the frontend today, plus a Users menu for the (backend-only, for
-- now) admin API - "super admin" is just a role with every permission
-- checked, not a hardcoded special case.
insert into roles (name, status) values ('Super Admin', true);
insert into permissions (role_id, menu, function, can_read, can_write)
select r.id, m.menu, 'manage', true, true
from roles r
cross join (values
    ('Dashboard'), ('Connection Settings'), ('AVR Settings'),
    ('Mail Configuration'), ('Users'), ('Roles'), ('Audit Log')
) as m(menu)
where r.name = 'Super Admin';

-- The default super-admin user account itself is seeded at application
-- startup (see infrastructure.config.AdminSeeder), not here - hashing a
-- real BCrypt password requires the app's own PasswordEncoder bean, and
-- seeding in Java lets the initial password be provided per-install via an
-- environment variable instead of a fixed value baked into every client's
-- database.

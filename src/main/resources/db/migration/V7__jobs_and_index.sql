create type job_status as enum (
    'pending',
    'started',
    'done',
    'failed'
);

create table if not exists job (
    id bigserial primary key,
    file_name varchar(300),
    file_path varchar(500),
    status job_status,
    update_time timestamptz default now(),
    uploaded_by varchar(100),
    error_message varchar(500),
    attempts int not null default 0
);

create extension if not exists pg_trgm;
create index idx_purgatory_series_trgm on purgatory using gin ((meta ->> 'seriesName') gin_trgm_ops);
create index idx_purgatory_number on purgatory ((meta ->> 'number'));
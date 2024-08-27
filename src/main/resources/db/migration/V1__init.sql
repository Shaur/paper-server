create table if not exists user_data (
    id bigserial primary key,
    username varchar(255),
    password varchar(512)
);

create table if not exists purgatory(
    id bigserial primary key,
    meta jsonb
);

create index if not exists purgatory_meta_idx on purgatory using gin(meta);
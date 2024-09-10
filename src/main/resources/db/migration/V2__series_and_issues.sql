create table if not exists series
(
    id        bigserial primary key,
    is_ended  boolean not null,
    publisher varchar(255),
    title     varchar(400)
);

create table if not exists issue
(
    id               bigserial primary key,
    number           varchar(255),
    pages_count      integer not null,
    publication_date timestamp(6),
    summary          text,
    series_id        bigserial
        constraint issue_fk
            references series
);
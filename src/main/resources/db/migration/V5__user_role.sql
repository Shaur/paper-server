alter table user_data add role varchar(255);

update user_data set role='user';

create table if not exists user_role
(
    name varchar(255),
    privileges jsonb,
    primary key (name)
);

insert into user_role (name, privileges) values ('user', '[]');
insert into user_role (name, privileges) values ('admin', '["issue.delete", "series.delete"]');

create index idx_series_id on issue(series_id);
create table if not exists series_subscription
(
    user_id   bigint,
    series_id bigint,
    primary key (user_id, series_id)
);
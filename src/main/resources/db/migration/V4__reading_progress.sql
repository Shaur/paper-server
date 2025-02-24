create table if not exists reading_progress
(
    user_id      bigint,
    issue_id     bigint,
    current_page int,
    update_time  bigint,
    primary key (user_id, issue_id)
);
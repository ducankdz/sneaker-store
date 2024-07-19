alter table categories modify name varchar(50) unique;

alter table products modify thumbnail varchar(255);

alter table users alter column role_id set default 1;
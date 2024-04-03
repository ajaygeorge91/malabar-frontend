# --- !Ups

ALTER TABLE auth.user
  ADD COLUMN signed_up_at TIMESTAMPTZ DEFAULT NOW();

CREATE TABLE auth.bearer_token_authenticator_info (
  id           VARCHAR NOT NULL PRIMARY KEY,
  last_used_date_time           VARCHAR NOT NULL,
  expiration_date_time           VARCHAR NOT NULL,
  idle_timeout           BIGSERIAL,
  login_info_id           VARCHAR
);


# --- !Downs

ALTER TABLE auth.user
  DROP COLUMN signed_up_at;


DROP TABLE auth.bearer_token_authenticator_info;

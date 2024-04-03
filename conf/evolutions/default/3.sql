# --- !Ups
CREATE SCHEMA wallet;

CREATE TABLE wallet.user_wallet_info (
  id           UUID NOT NULL PRIMARY KEY,
  user_id       UUID   NOT NULL,
  wallet_id VARCHAR NOT NULL,
  name VARCHAR NOT NULL,
  public_key  VARCHAR not null,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  CONSTRAINT wallet_user_wallet_info_user_id_fk FOREIGN KEY (user_id) REFERENCES auth.user (id)
);


# --- !Downs

DROP TABLE wallet.user_wallet_info;
DROP SCHEMA wallet;

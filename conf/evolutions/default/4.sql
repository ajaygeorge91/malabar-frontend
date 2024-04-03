# --- !Ups

CREATE TABLE wallet.user_private_wallet_info (
  id           UUID NOT NULL PRIMARY KEY,
  user_id       UUID   NOT NULL,
  base_address VARCHAR NOT NULL,
  name VARCHAR NOT NULL,
  private_key  VARCHAR not null,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  CONSTRAINT wallet_user_private_wallet_info_user_id_fk FOREIGN KEY (user_id) REFERENCES auth.user (id)
);


# --- !Downs

DROP TABLE wallet.user_private_wallet_info;

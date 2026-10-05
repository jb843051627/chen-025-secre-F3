-- 仅供六则单元测试使用的 H2 建表（与 doc/schema/secre.sql 的新三表+密级栏逐列对齐）
CREATE TABLE IF NOT EXISTS t_secre_carrier (
  id bigint PRIMARY KEY,
  carrier_no varchar(64),
  book_id int,
  book_no varchar(64),
  post_no varchar(64),
  need_num int,
  got_num int,
  lack_num int,
  content varchar(255),
  level_no int,
  status int,
  del_flag int DEFAULT 0,
  create_by varchar(64),
  create_time timestamp,
  update_by varchar(64),
  update_time timestamp,
  remark varchar(500)
);

CREATE TABLE IF NOT EXISTS t_secre_countersign (
  id bigint PRIMARY KEY,
  cs_no varchar(64),
  carrier_id bigint,
  carrier_no varchar(64),
  change_kind int,
  level_from int,
  level_to int,
  node_no int,
  round_no int DEFAULT 1,
  status int,
  open_flag int,
  seal_time timestamp,
  del_flag int DEFAULT 0,
  create_by varchar(64),
  create_time timestamp,
  update_by varchar(64),
  update_time timestamp,
  remark varchar(500),
  UNIQUE (carrier_id, open_flag)
);

CREATE TABLE IF NOT EXISTS t_secre_countersign_sign (
  id bigint PRIMARY KEY,
  cs_id bigint NOT NULL,
  round_no int NOT NULL,
  node_no int NOT NULL,
  seq_no int NOT NULL,
  signer varchar(64) NOT NULL,
  verdict int NOT NULL,
  sign_time timestamp NOT NULL,
  voided int DEFAULT 0,
  create_by varchar(64),
  create_time timestamp,
  update_by varchar(64),
  update_time timestamp,
  remark varchar(500),
  UNIQUE (cs_id, round_no, node_no, seq_no)
);

CREATE TABLE IF NOT EXISTS t_secre_level_log (
  id bigint PRIMARY KEY,
  carrier_id bigint NOT NULL,
  carrier_no varchar(64),
  cs_id bigint NOT NULL,
  cs_no varchar(64),
  level_from int,
  level_to int,
  signers varchar(500),
  seal_time timestamp NOT NULL,
  create_by varchar(64),
  create_time timestamp,
  update_by varchar(64),
  update_time timestamp,
  remark varchar(500)
);

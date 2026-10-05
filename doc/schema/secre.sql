-- secre 涉密载体入册清退与密级变更保密监管 -- schema (chen-025)
-- 列名与基线实体契约（@TableName/@TableField）逐列对齐，改列必须同步实体。
-- 库：chen_025

CREATE TABLE IF NOT EXISTS t_secre_carrier (
  id bigint NOT NULL COMMENT '主键',
  carrier_no varchar(64) DEFAULT NULL COMMENT '涉密载体件码',
  book_id int DEFAULT NULL COMMENT '挂在哪一册名录名下',
  book_no varchar(64) DEFAULT NULL COMMENT '名录代号（冗余自名录）',
  post_no varchar(64) DEFAULT NULL COMMENT '使用岗或承办岗代号',
  need_num int DEFAULT NULL COMMENT '应交件数',
  got_num int DEFAULT NULL COMMENT '已收件数',
  lack_num int DEFAULT NULL COMMENT '还差几件（轧出来，不手填）',
  content varchar(255) DEFAULT NULL COMMENT '随件交来的载体题名与要件',
  level_no int DEFAULT NULL COMMENT '密级栏 1秘密 2机密 3绝密（只随齐闸会签单换，不手填）',
  status int DEFAULT NULL COMMENT '册面情形 0新入册 1已收齐 2缺项',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='涉密载体册面';

CREATE TABLE IF NOT EXISTS t_secre_disp_flow (
  id bigint NOT NULL COMMENT '主键',
  biz_no varchar(64) DEFAULT NULL COMMENT '载体处置流转单号',
  stage int DEFAULT NULL COMMENT '当前关次 0..3（核件/定级/清退/监销）',
  status int DEFAULT NULL COMMENT '单子走到哪一关 0未起 1在办 2已封住',
  content varchar(255) DEFAULT NULL COMMENT '一关一记',
  last_action varchar(64) DEFAULT NULL COMMENT '最近一次挪关动作',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='载体处置流转单';

CREATE TABLE IF NOT EXISTS t_secre_hand_row (
  id bigint NOT NULL COMMENT '主键',
  batch_no varchar(64) DEFAULT NULL COMMENT '清退移交批次码',
  row_no int DEFAULT NULL COMMENT '原报行次',
  item_code varchar(64) DEFAULT NULL COMMENT '被对上的载体件码',
  qty decimal(12,2) DEFAULT NULL COMMENT '本行应交件数',
  quali_num decimal(12,2) DEFAULT NULL COMMENT '本行核收合格件数',
  hand_kind varchar(32) DEFAULT NULL COMMENT '交回渠道（本级送交／上级调回两条来路）',
  status int DEFAULT NULL COMMENT '行落地情形 0未勾 1已收下 2已退回',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='清退移交行';

CREATE TABLE IF NOT EXISTS t_secre_level_bill (
  id bigint NOT NULL COMMENT '主键',
  bill_no varchar(64) DEFAULT NULL COMMENT '密级变更签批单',
  node_no int DEFAULT NULL COMMENT '当前所在关 0..2（承办部门/本机关保密办/上级主管部门）',
  sign_mode int DEFAULT NULL COMMENT '本关算齐办法 0一名即过 1两关须同判',
  need_count int DEFAULT NULL COMMENT '本关应签人数',
  sign_count int DEFAULT NULL COMMENT '本关已签人数',
  status int DEFAULT NULL COMMENT '签批情形 0在签 1已点齐 2已压回',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='密级变更签批单';

-- ─────────────────────────────────────────────────────────────────────────────
-- 密级变更三道闸会签（新名目 countersign，承接六则；旧 t_secre_level_bill 形状锁住不动）
--   头闸 0 承办部门经办岗（1 名即过）
--   次闸 1 本机关保密办（2 名同看，头名说法定论，后名留底）
--   末闸 2 上级主管部门（2 名同「可」才过，一可一不可挂待议）
-- 唯一算齐口径在引擎一处：段位、已签数、在办张数、回数核对全由受理簿逐笔点出，
-- 报文里写的段位人数一律不算数。另起单／改旧笔／追补签三暗门不留。
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS t_secre_countersign (
  id bigint NOT NULL COMMENT '主键',
  cs_no varchar(64) DEFAULT NULL COMMENT '密级变更会签单号（一载体一行变更只带得出这一张）',
  carrier_id bigint DEFAULT NULL COMMENT '册面载体行（同一件载体、同一回变更只带一张）',
  carrier_no varchar(64) DEFAULT NULL COMMENT '载体件码（冗余，受理簿对件用）',
  change_kind int DEFAULT NULL COMMENT '往上抬/往下压/整个解开 1抬 2压 3解密',
  level_from int DEFAULT NULL COMMENT '自哪一级挪来',
  level_to int DEFAULT NULL COMMENT '挪到哪一级（解开记 0）',
  node_no int DEFAULT NULL COMMENT '当前停在哪一闸 0头闸 1次闸 2末闸（系统点，不留手填格）',
  round_no int DEFAULT '1' COMMENT '走到第几轮（每压回一次加一，本轮与上轮各放各格）',
  status int DEFAULT NULL COMMENT '情形 0在签 1待议(末闸一可一不可挂住) 2齐闸封住 3已压回重走',
  open_flag int DEFAULT '1' COMMENT '未齐闸挂账记1，齐闸置NULL（唯一索引借NULL可重，挡一件载体两张在办单）',
  seal_time datetime DEFAULT NULL COMMENT '齐闸封单那一刻',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除（会签单不许删，仅留列对齐基线）',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_carrier_open (carrier_id, open_flag),
  KEY idx_cs_no (cs_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='密级变更三道闸会签单';

CREATE TABLE IF NOT EXISTS t_secre_countersign_sign (
  id bigint NOT NULL COMMENT '主键',
  cs_id bigint NOT NULL COMMENT '所属会签单',
  round_no int NOT NULL COMMENT '第几轮落下（本轮与上轮各放各格，哪天补的各有凭据）',
  node_no int NOT NULL COMMENT '落在哪一闸 0头闸 1次闸 2末闸',
  seq_no int NOT NULL COMMENT '本闸本轮第几笔（同一瞬两笔也各按先后记，不并成一句）',
  signer varchar(64) NOT NULL COMMENT '落字人（一支笔只认本人那一刻）',
  verdict int NOT NULL COMMENT '落字 1可 0不可',
  sign_time datetime NOT NULL COMMENT '落字那一刻（日后复核只认这一刻）',
  voided int DEFAULT '0' COMMENT '本笔是否随本闸压回作账内抹除 0留底 1本轮抹（旧轮旧字一个不动）',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注（落字原话留底，两行都摆着）',
  PRIMARY KEY (id),
  UNIQUE KEY uk_cs_round_node_seq (cs_id, round_no, node_no, seq_no),
  KEY idx_cs_signer (cs_id, signer)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会签逐闸受理簿（一笔一行，回数与台账都对它）';

CREATE TABLE IF NOT EXISTS t_secre_level_log (
  id bigint NOT NULL COMMENT '主键',
  carrier_id bigint NOT NULL COMMENT '载体行（与册面那一行改动出自同一回计算）',
  carrier_no varchar(64) DEFAULT NULL COMMENT '载体件码',
  cs_id bigint NOT NULL COMMENT '凭的是哪一张会签单',
  cs_no varchar(64) DEFAULT NULL COMMENT '会签单号',
  level_from int DEFAULT NULL COMMENT '从哪一级',
  level_to int DEFAULT NULL COMMENT '到哪一级（解开记 0）',
  signers varchar(500) DEFAULT NULL COMMENT '三闸都有谁落的字（逐闸点出来串成）',
  seal_time datetime NOT NULL COMMENT '齐闸落定那一刻',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_level_log_carrier (carrier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='载体密级变更履历（一段一段倒着捋）';

CREATE TABLE IF NOT EXISTS t_secre_org_book (
  id bigint NOT NULL COMMENT '主键',
  book_no varchar(64) DEFAULT NULL COMMENT '机构名录代号',
  book_name varchar(128) DEFAULT NULL COMMENT '单位全称',
  book_kind varchar(32) DEFAULT NULL COMMENT '单位类别',
  road_name varchar(128) DEFAULT NULL COMMENT '属地一直写到街道（浔州省—谷城市—区—街道）',
  sync_kind varchar(32) DEFAULT NULL COMMENT '来路（上级名录下发／本机关自录）',
  status int DEFAULT NULL COMMENT '名录情形 0在册 1已撤出',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机构名录底册';

CREATE TABLE IF NOT EXISTS t_secre_urge_item (
  id bigint NOT NULL COMMENT '主键',
  item_no varchar(64) DEFAULT NULL COMMENT '届期催收与巡查条目代号',
  due_at datetime DEFAULT NULL COMMENT '该交回那一日的止点时刻',
  amount decimal(12,2) DEFAULT NULL COMMENT '最多许提前几日',
  content varchar(255) DEFAULT NULL COMMENT '事由与所对载体的记要',
  status int DEFAULT NULL COMMENT '条目情形 0搁着 1已派出 2派不成',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='届期催收与巡查条目';

CREATE TABLE IF NOT EXISTS t_secre_warn_line (
  id bigint NOT NULL COMMENT '主键',
  rule_code varchar(64) DEFAULT NULL COMMENT '届期预警分级线代号',
  rule_name varchar(128) DEFAULT NULL COMMENT '线名',
  obj_kind varchar(32) DEFAULT NULL COMMENT '这条线管载体册面还是签批卷面',
  th1_max decimal(12,3) DEFAULT NULL COMMENT '起算日数',
  th2_max decimal(12,3) DEFAULT NULL COMMENT '够线日数',
  th3_max decimal(12,3) DEFAULT NULL COMMENT '封顶日数',
  eff_start datetime DEFAULT NULL COMMENT '起用那一日',
  eff_end datetime DEFAULT NULL COMMENT '交班那一日(不含)',
  priority int DEFAULT NULL COMMENT '交班次序(数值大的先说话)',
  status int DEFAULT NULL COMMENT '线的情形 0在位 1已撤下',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='届期预警分级线';

-- 初始档案数据（id=1 启用 / id=2 停用）
-- t_secre_org_book 两条种子名录：book_id=0 在册、book_id=1 已撤出（供后续挂接与挡新入册
-- 判定项取用）。名录代号与类别按「三类各排各的序」的说法给值。
INSERT IGNORE INTO t_secre_org_book (id, book_no, book_name, book_kind, road_name, status, del_flag, create_by, create_time)
VALUES (0, 'ML00', '浔州区谷城街道·中共谷城市委机关（机关类，在册）', '机关', '浔州省—谷城市—浔州区—谷城街道', 0, 0, 'seed', NOW()),
       (1, 'ML01', '北塔区岭南区·市测绘院下属资料室（已并走撤出）', '事业单位', '浔州省—谷城市—北塔区—岭南街道', 1, 0, 'seed', NOW());


-- ─────────────────────────────────────────────────────────────────────────────
-- 已按旧版建过库的，执行下面这段迁库；新库不必。
-- 旧 t_secre_level_bill 及其 approve/reject/rollback 仍原样保留（形状锁住），
-- 密级变更一律改走 t_secre_countersign 三道闸会签；两者不互改样子。
-- ALTER TABLE t_secre_carrier ADD COLUMN level_no int DEFAULT NULL
--   COMMENT '密级栏 1秘密 2机密 3绝密（只随齐闸会签单换，不手填）' AFTER content;

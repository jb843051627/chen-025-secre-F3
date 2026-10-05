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

-- 三道闸密级变更（新名目 level_gate；旧 t_secre_level_bill 的 approval-chain 形状锁住不动）
-- 口径：已签人数不设列、不留格子给人填——一律由流水逐笔点出来；屏上所见只是回显。
-- 表 1：三闸密级变更单（一张单只由册面上那一行带出：同一载体、同一回变更只带得出一张）
CREATE TABLE IF NOT EXISTS t_secre_level_gate_bill (
  id bigint NOT NULL COMMENT '主键',
  bill_no varchar(64) NOT NULL COMMENT '三闸密级变更单号（一支笔只此一处入口）',
  carrier_id bigint NOT NULL COMMENT '带出这张单的册面行（t_secre_carrier.id）',
  carrier_no varchar(64) DEFAULT NULL COMMENT '涉密载体件码（冗余，对册面）',
  change_kind int NOT NULL COMMENT '这一回的走法 0往上抬 1往下压 2整个解开',
  from_level int DEFAULT NULL COMMENT '从哪一级（起单时册面原值快照）',
  to_level int DEFAULT NULL COMMENT '挪到哪一级（齐闸后才写得进去）',
  gate_no int NOT NULL COMMENT '整张单停在哪一截 0承办部门岗 1本机关保密办 2上级主管部门 3齐闸',
  gate_status int NOT NULL COMMENT '本截情形 0候签 2挂待议 3已封住（1为点满当口的瞬态，不落库；停截由 gate_no 点出）',
  round_no int NOT NULL DEFAULT 1 COMMENT '重头往上走的回数（压回几回就加几；各截各认自己落字那一回）',
  seal_time datetime DEFAULT NULL COMMENT '齐闸封住那一刻',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除（齐闸后一笔进不来、一字改不了、整张也抽不走）',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_carrier_gate (carrier_id, gate_no),
  KEY idx_status (gate_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='三闸密级变更单';

-- 各闸该几名落字、按什么算放过——配置钉死在代码枚举里，报文里写了第几段、几个人，
-- 进到库里一概不算数；段位与人数以引擎点出来的为凭。
--   闸0 承办部门那一名办事的岗：候 1 名，一名落字即放过（MODE_ONE）
--   闸1 本机关保密办：候 2 名，头一个落字那方的说法是定论（MODE_FIRST_WINS）
--   闸2 上级主管部门对口两名：候 2 名，两句都写「可」才算过（MODE_BOTH_AGREE）

-- 表 2：逐笔落字流水（顺序由 DB 时间戳定；同瞬两名各按各的先后记，不并成一句）
CREATE TABLE IF NOT EXISTS t_secre_level_gate_sign (
  id bigint NOT NULL COMMENT '主键',
  bill_id bigint NOT NULL COMMENT '对哪张三闸单',
  gate_no int NOT NULL COMMENT '落在哪一闸 0/1/2',
  round_no int NOT NULL COMMENT '落在哪一回（被压回那一闸重头走，本轮与上轮各放各格）',
  sign_seq int NOT NULL COMMENT '本闸本回里第几笔（顺着谁先谁后一笔一笔点出来的序）',
  signer_no varchar(64) NOT NULL COMMENT '落字岗位/人员代号',
  verdict int NOT NULL COMMENT '写下的字 1可 0不可',
  word varchar(500) DEFAULT NULL COMMENT '写下的原话（两方的字都留底，屏上两行都摆着）',
  signed_at datetime(3) NOT NULL COMMENT '各自落字那一刻（日后复核只认这一刻）',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_bill_gate_round_signer (bill_id, gate_no, round_no, signer_no),
  KEY idx_bill_gate_round (bill_id, gate_no, round_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='三闸单逐笔落字流水';

-- 表 3：载体密级变更履历（齐闸后叠在册面那一行上，可一段一段倒着捋）
CREATE TABLE IF NOT EXISTS t_secre_carrier_level_log (
  id bigint NOT NULL COMMENT '主键',
  carrier_id bigint NOT NULL COMMENT '叠在哪一行（t_secre_carrier.id）',
  bill_id bigint NOT NULL COMMENT '凭的是哪一张单',
  bill_no varchar(64) NOT NULL COMMENT '单号（留底）',
  change_kind int NOT NULL COMMENT '走法 0往上抬 1往下压 2整个解开',
  from_level int DEFAULT NULL COMMENT '从哪一级',
  to_level int NOT NULL COMMENT '挪到哪一级',
  write_ups text COMMENT '各闸各笔的字：闸次、回次、序、谁、可不可、原话、时刻（一条收口的账）',
  sealed_at datetime(3) NOT NULL COMMENT '齐闸那一刻',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_carrier_seq (carrier_id, sealed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='载体密级变更履历';

-- 册面那一行加密级栏；履历与册面改动出自同一回计算（同一事务），口径一致。
ALTER TABLE t_secre_carrier
  ADD COLUMN level_no int DEFAULT NULL COMMENT '当前密级 1秘密 2机密 3绝密 0已解密（整个解开）' AFTER status;

-- 初始档案数据（id=1 启用 / id=2 停用）
-- t_secre_org_book 两条种子名录：book_id=0 在册、book_id=1 已撤出（供后续挂接与挡新入册
-- 判定项取用）。名录代号与类别按「三类各排各的序」的说法给值。
INSERT IGNORE INTO t_secre_org_book (id, book_no, book_name, book_kind, road_name, status, del_flag, create_by, create_time)
VALUES (0, 'ML00', '浔州区谷城街道·中共谷城市委机关（机关类，在册）', '机关', '浔州省—谷城市—浔州区—谷城街道', 0, 0, 'seed', NOW()),
       (1, 'ML01', '北塔区岭南区·市测绘院下属资料室（已并走撤出）', '事业单位', '浔州省—谷城市—北塔区—岭南街道', 1, 0, 'seed', NOW());


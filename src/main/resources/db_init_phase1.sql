-- ============================================================
-- SW Manager — DB 초기화 Phase 1 (기초 DDL)
-- ============================================================
-- 본 스크립트는 신규 환경에서 가장 먼저 실행되는 기초 DDL 입니다.
-- 실행 순서: phase1.sql → [phase1_sigungu.sql] → phase2.sql → V*.sql
--
-- 대상 PostgreSQL: 14+ (운영DB 실측 16.11)
-- 멱등성: CREATE TABLE/INDEX IF NOT EXISTS + ADD CONSTRAINT DO 블록 + ON CONFLICT
--
-- 라이선스 모듈은 영구 보류 — license_registry, license_upload_history,
-- qr_license, geonuris_license 는 본 파일에 정의되지 않음 (재개 시 별도 스프린트)
--
-- 19 테이블 (phase1):
--   1. sigungu_code  2. sys_mst        3. prj_types      4. maint_tp_mst
--   5. cont_stat_mst 6. cont_frm_mst   7. users          8. ps_info
--   9. sw_pjt        10. tb_infra_master  11. tb_infra_server  12. tb_infra_software
--   13. tb_infra_link_upis  14. tb_infra_link_api  15. tb_infra_memo
--   16. access_logs  17. tb_pjt_target  18. tb_pjt_manpower_plan  19. tb_pjt_schedule
--
-- 출처: 운영DB schema dump (docs/references/snapshots/2026-04-27-prod-schema.sql, SHA256 f3b2b51a...)
-- ============================================================

BEGIN;

-- =========================================================
-- Section 1: 테이블 + 시퀀스 + PK/UNIQUE 정의 (부모→자식 순서)
-- =========================================================

-- ----- sigungu_code -----
CREATE TABLE IF NOT EXISTS sigungu_code (
    instt_c character varying(20),
    adm_sect_c character varying(20) NOT NULL,
    full_name character varying(100),
    sido_name character varying(50),
    sgg_name character varying(50)
);

DO $$ BEGIN
    ALTER TABLE ONLY sigungu_code
    ADD CONSTRAINT sigungu_code_pkey PRIMARY KEY (adm_sect_c);
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

-- ----- sys_mst -----
CREATE TABLE IF NOT EXISTS sys_mst (
    cd character varying(10) NOT NULL,
    nm character varying(100) NOT NULL
);

DO $$ BEGIN
    ALTER TABLE ONLY sys_mst
    ADD CONSTRAINT sys_mst_pkey PRIMARY KEY (cd);
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

-- ----- prj_types -----
CREATE TABLE IF NOT EXISTS prj_types (
    cd character varying(10) NOT NULL,
    nm character varying(50) NOT NULL
);

DO $$ BEGIN
    ALTER TABLE ONLY prj_types
    ADD CONSTRAINT prj_types_pkey PRIMARY KEY (cd);
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

-- ----- maint_tp_mst -----
CREATE TABLE IF NOT EXISTS maint_tp_mst (
    cd character varying(10) NOT NULL,
    nm character varying(50) NOT NULL
);

DO $$ BEGIN
    ALTER TABLE ONLY maint_tp_mst
    ADD CONSTRAINT maint_tp_mst_pkey PRIMARY KEY (cd);
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

-- ----- cont_stat_mst -----
CREATE TABLE IF NOT EXISTS cont_stat_mst (
    cd character varying(10) NOT NULL,
    nm character varying(20) NOT NULL
);

DO $$ BEGIN
    ALTER TABLE ONLY cont_stat_mst
    ADD CONSTRAINT cont_stat_mst_pkey PRIMARY KEY (cd);
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

-- ----- cont_frm_mst -----
CREATE TABLE IF NOT EXISTS cont_frm_mst (
    cd character varying(10) NOT NULL,
    nm character varying(20) NOT NULL
);

DO $$ BEGIN
    ALTER TABLE ONLY cont_frm_mst
    ADD CONSTRAINT cont_frm_mst_pkey PRIMARY KEY (cd);
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

-- ----- users -----
CREATE TABLE IF NOT EXISTS users (
    user_id bigint NOT NULL,
    userid character varying(50) NOT NULL,
    password character varying(100) NOT NULL,
    org_nm character varying(100),
    dept_nm character varying(100),
    team_nm character varying(100),
    tel character varying(20),
    email character varying(100),
    user_role character varying(20) DEFAULT 'ROLE_USER'::character varying,
    enabled boolean DEFAULT false,
    auth_dashboard character varying(10) DEFAULT 'NONE'::character varying,
    auth_project character varying(10) DEFAULT 'NONE'::character varying,
    auth_person character varying(10) DEFAULT 'NONE'::character varying,
    reg_dt timestamp without time zone DEFAULT now(),
    username character varying(50),
    auth_infra character varying(10) DEFAULT 'NONE'::character varying,
    auth_license character varying(10) DEFAULT 'NONE'::character varying,
    auth_quotation character varying(10) DEFAULT 'NONE'::character varying,
    failed_attempts integer DEFAULT 0,
    lock_time timestamp without time zone,
    auth_work_plan character varying(20) DEFAULT 'NONE'::character varying,
    auth_document character varying(20) DEFAULT 'NONE'::character varying,
    auth_contract character varying(20) DEFAULT 'NONE'::character varying,
    auth_performance character varying(20) DEFAULT 'NONE'::character varying,
    "position" character varying(50),
    tech_grade character varying(20),
    address character varying(300),
    certificate character varying(500),
    mobile character varying(20),
    position_title character varying(50),
    ssn character varying(14),
    tasks character varying(1000),
    field_role character varying(50),
    career_years character varying(20)
);

CREATE SEQUENCE IF NOT EXISTS users_user_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE users_user_id_seq OWNED BY users.user_id;

ALTER TABLE ONLY users ALTER COLUMN user_id SET DEFAULT nextval('users_user_id_seq'::regclass);

DO $$ BEGIN
    ALTER TABLE ONLY users
    ADD CONSTRAINT unique_userid UNIQUE (userid);
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

DO $$ BEGIN
    ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

DO $$ BEGIN
    ALTER TABLE ONLY users
    ADD CONSTRAINT users_username_key UNIQUE (userid);
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

-- ----- ps_info -----
CREATE TABLE IF NOT EXISTS ps_info (
    id bigint NOT NULL,
    sys_nm_en character varying(10),
    org_nm character varying(100),
    dept_nm character varying(100),
    team_nm character varying(100),
    pos character varying(50),
    user_nm character varying(50),
    email character varying(100),
    reg_dt timestamp without time zone DEFAULT now(),
    tel character varying(20),
    city_nm character varying(50),
    dist_nm character varying(200),
    org_cd character varying(50),
    dist_cd character varying(20),
    sys_nm character varying(200)
);

CREATE SEQUENCE IF NOT EXISTS ps_info_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE ps_info_id_seq OWNED BY ps_info.id;

ALTER TABLE ONLY ps_info ALTER COLUMN id SET DEFAULT nextval('ps_info_id_seq'::regclass);

DO $$ BEGIN
    ALTER TABLE ONLY ps_info
    ADD CONSTRAINT ps_info_pkey PRIMARY KEY (id);
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

-- ----- sw_pjt -----
CREATE TABLE IF NOT EXISTS sw_pjt (
    proj_id bigint NOT NULL,
    year integer,
    pms_cd character varying(50),
    city_nm character varying(50),
    dist_nm character varying(200),
    org_nm character varying(50),
    org_cd character varying(50),
    dist_cd character varying(20),
    biz_type character varying(100),
    biz_cat character varying(100),
    biz_cat_en character varying(10),
    sys_nm character varying(200),
    sys_nm_en character varying(10),
    proj_nm character varying(200),
    client character varying(200),
    cont_ent character varying(200),
    cont_dept character varying(200),
    cont_type character varying(10),
    cont_dt date,
    start_dt date,
    end_dt date,
    inst_dt date,
    budg_amt bigint,
    sw_rt double precision,
    cont_amt bigint,
    cont_rt double precision,
    sw_amt bigint,
    cnslt_amt bigint,
    db_impl_amt bigint,
    pkg_sw_amt bigint,
    sys_dev_amt bigint,
    hw_amt bigint,
    etc_amt bigint,
    outscr_amt bigint,
    stat character varying(10),
    pre_pay bigint,
    pay_prog bigint,
    pay_comp bigint,
    maint_type character varying(10),
    reg_dt timestamp without time zone DEFAULT now(),
    pay_prog_yn character varying(10),
    pay_prog_fr character varying(10),
    person_id bigint,
    org_lgh_nm character varying(50) DEFAULT '기본값'::character varying,
    comp_yn character varying(1) DEFAULT 'N'::character varying,
    interim_yn character varying(1) DEFAULT 'N'::character varying,
    completion_yn character varying(1) DEFAULT 'N'::character varying,
    proj_purpose text,
    support_type character varying(50),
    scope_text text,
    inspect_method character varying(200)
);

CREATE SEQUENCE IF NOT EXISTS sw_pjt_proj_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE sw_pjt_proj_id_seq OWNED BY sw_pjt.proj_id;

ALTER TABLE ONLY sw_pjt ALTER COLUMN proj_id SET DEFAULT nextval('sw_pjt_proj_id_seq'::regclass);

DO $$ BEGIN
    ALTER TABLE ONLY sw_pjt
    ADD CONSTRAINT sw_pjt_pkey PRIMARY KEY (proj_id);
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

CREATE INDEX IF NOT EXISTS idx_sw_pjt_person_id ON sw_pjt USING btree (person_id);

-- ----- tb_infra_master -----
CREATE TABLE IF NOT EXISTS tb_infra_master (
    infra_id bigint NOT NULL,
    city_nm character varying(50),
    dist_nm character varying(50),
    sys_nm character varying(100),
    sys_nm_en character varying(100),
    org_cd character varying(20),
    dist_cd character varying(20),
    reg_dt timestamp without time zone DEFAULT now(),
    mod_dt timestamp without time zone,
    infra_type character varying(20) DEFAULT 'PROD'::character varying
);

CREATE SEQUENCE IF NOT EXISTS tb_infra_master_infra_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE tb_infra_master_infra_id_seq OWNED BY tb_infra_master.infra_id;

ALTER TABLE ONLY tb_infra_master ALTER COLUMN infra_id SET DEFAULT nextval('tb_infra_master_infra_id_seq'::regclass);

DO $$ BEGIN
    ALTER TABLE ONLY tb_infra_master
    ADD CONSTRAINT tb_infra_master_pkey PRIMARY KEY (infra_id);
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

-- ----- tb_infra_server -----
CREATE TABLE IF NOT EXISTS tb_infra_server (
    server_id bigint NOT NULL,
    infra_id bigint NOT NULL,
    server_type character varying(10) NOT NULL,
    ip_addr character varying(200),
    acc_id character varying(200),
    acc_pw character varying(200),
    os_nm character varying(200),
    mac_addr character varying(200),
    server_model character varying(200),
    serial_no character varying(100),
    cpu_spec character varying(200),
    memory_spec character varying(100),
    disk_spec character varying(300),
    network_spec character varying(200),
    power_spec character varying(100),
    os_detail character varying(300),
    rack_location character varying(100),
    note text
);

CREATE SEQUENCE IF NOT EXISTS tb_infra_server_server_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE tb_infra_server_server_id_seq OWNED BY tb_infra_server.server_id;

ALTER TABLE ONLY tb_infra_server ALTER COLUMN server_id SET DEFAULT nextval('tb_infra_server_server_id_seq'::regclass);

DO $$ BEGIN
    ALTER TABLE ONLY tb_infra_server
    ADD CONSTRAINT tb_infra_server_pkey PRIMARY KEY (server_id);
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

-- ----- tb_infra_software -----
CREATE TABLE IF NOT EXISTS tb_infra_software (
    sw_id bigint NOT NULL,
    server_id bigint NOT NULL,
    sw_category character varying(20),
    sw_nm character varying(100),
    sw_ver character varying(50),
    port character varying(200),
    sw_acc_id character varying(100),
    sw_acc_pw character varying(200),
    sid character varying(100),
    install_path character varying(500)
);

CREATE SEQUENCE IF NOT EXISTS tb_infra_software_sw_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE tb_infra_software_sw_id_seq OWNED BY tb_infra_software.sw_id;

ALTER TABLE ONLY tb_infra_software ALTER COLUMN sw_id SET DEFAULT nextval('tb_infra_software_sw_id_seq'::regclass);

DO $$ BEGIN
    ALTER TABLE ONLY tb_infra_software
    ADD CONSTRAINT tb_infra_software_pkey PRIMARY KEY (sw_id);
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

-- ----- tb_infra_link_upis -----
CREATE TABLE IF NOT EXISTS tb_infra_link_upis (
    link_id bigint NOT NULL,
    infra_id bigint NOT NULL,
    kras_ip character varying(50),
    kras_cd character varying(50),
    gpki_id character varying(50),
    gpki_pw character varying(200),
    minwon_ip character varying(200),
    minwon_link_cd character varying(50),
    minwon_key character varying(200),
    doc_ip character varying(200),
    doc_adm_id character varying(50),
    doc_id character varying(200)
);

CREATE SEQUENCE IF NOT EXISTS tb_infra_link_upis_link_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE tb_infra_link_upis_link_id_seq OWNED BY tb_infra_link_upis.link_id;

ALTER TABLE ONLY tb_infra_link_upis ALTER COLUMN link_id SET DEFAULT nextval('tb_infra_link_upis_link_id_seq'::regclass);

DO $$ BEGIN
    ALTER TABLE ONLY tb_infra_link_upis
    ADD CONSTRAINT tb_infra_link_upis_pkey PRIMARY KEY (link_id);
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

-- ----- tb_infra_link_api -----
CREATE TABLE IF NOT EXISTS tb_infra_link_api (
    api_id bigint NOT NULL,
    infra_id bigint NOT NULL,
    naver_news_key character varying(300),
    naver_news_req_dt date,
    naver_news_exp_dt date,
    naver_news_user character varying(50),
    naver_secret_key character varying(300),
    naver_secret_req_dt date,
    naver_secret_exp_dt date,
    naver_secret_user character varying(50),
    public_data_key character varying(200),
    public_data_req_dt date,
    public_data_exp_dt date,
    public_data_user character varying(50),
    kras_key character varying(200),
    kras_req_dt date,
    kras_exp_dt date,
    kras_user character varying(50),
    kgeo_key character varying(300),
    kgeo_req_dt date,
    kgeo_exp_dt date,
    kgeo_user character varying(50),
    vworld_key character varying(200),
    vworld_req_dt date,
    vworld_exp_dt date,
    vworld_user character varying(50),
    kakao_key character varying(200),
    kakao_req_dt date,
    kakao_exp_dt date,
    kakao_user character varying(50)
);

CREATE SEQUENCE IF NOT EXISTS tb_infra_link_api_api_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE tb_infra_link_api_api_id_seq OWNED BY tb_infra_link_api.api_id;

ALTER TABLE ONLY tb_infra_link_api ALTER COLUMN api_id SET DEFAULT nextval('tb_infra_link_api_api_id_seq'::regclass);

DO $$ BEGIN
    ALTER TABLE ONLY tb_infra_link_api
    ADD CONSTRAINT tb_infra_link_api_pkey PRIMARY KEY (api_id);
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

-- ----- tb_infra_memo -----
CREATE TABLE IF NOT EXISTS tb_infra_memo (
    memo_id bigint NOT NULL,
    infra_id bigint NOT NULL,
    memo_content text,
    memo_writer character varying(50),
    memo_date date
);

CREATE SEQUENCE IF NOT EXISTS tb_infra_memo_memo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE tb_infra_memo_memo_id_seq OWNED BY tb_infra_memo.memo_id;

ALTER TABLE ONLY tb_infra_memo ALTER COLUMN memo_id SET DEFAULT nextval('tb_infra_memo_memo_id_seq'::regclass);

DO $$ BEGIN
    ALTER TABLE ONLY tb_infra_memo
    ADD CONSTRAINT tb_infra_memo_pkey PRIMARY KEY (memo_id);
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

-- ----- access_logs -----
CREATE TABLE IF NOT EXISTS access_logs (
    log_id bigint NOT NULL,
    userid character varying(50),
    username character varying(50),
    ip_addr character varying(45),
    access_time timestamp without time zone DEFAULT now(),
    menu_nm character varying(100),
    action_type character varying(50),
    action_detail text
);

CREATE SEQUENCE IF NOT EXISTS access_logs_log_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE access_logs_log_id_seq OWNED BY access_logs.log_id;

ALTER TABLE ONLY access_logs ALTER COLUMN log_id SET DEFAULT nextval('access_logs_log_id_seq'::regclass);

DO $$ BEGIN
    ALTER TABLE ONLY access_logs
    ADD CONSTRAINT access_logs_pkey PRIMARY KEY (log_id);
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

-- ----- tb_pjt_target -----
CREATE TABLE IF NOT EXISTS tb_pjt_target (
    id bigint NOT NULL,
    proj_id bigint NOT NULL,
    product_name character varying(200) NOT NULL,
    qty integer DEFAULT 1 NOT NULL,
    sort_order integer DEFAULT 0
);

CREATE SEQUENCE IF NOT EXISTS tb_pjt_target_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE tb_pjt_target_id_seq OWNED BY tb_pjt_target.id;

ALTER TABLE ONLY tb_pjt_target ALTER COLUMN id SET DEFAULT nextval('tb_pjt_target_id_seq'::regclass);

DO $$ BEGIN
    ALTER TABLE ONLY tb_pjt_target
    ADD CONSTRAINT tb_pjt_target_pkey PRIMARY KEY (id);
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

CREATE INDEX IF NOT EXISTS idx_tb_pjt_target_proj ON tb_pjt_target USING btree (proj_id);

-- ----- tb_pjt_manpower_plan -----
CREATE TABLE IF NOT EXISTS tb_pjt_manpower_plan (
    id bigint NOT NULL,
    proj_id bigint NOT NULL,
    step_name character varying(200) NOT NULL,
    start_dt date,
    end_dt date,
    grade_special integer DEFAULT 0,
    grade_high integer DEFAULT 0,
    grade_mid integer DEFAULT 0,
    grade_low integer DEFAULT 0,
    func_high integer DEFAULT 0,
    func_mid integer DEFAULT 0,
    func_low integer DEFAULT 0,
    remark character varying(300),
    sort_order integer DEFAULT 0
);

CREATE SEQUENCE IF NOT EXISTS tb_pjt_manpower_plan_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE tb_pjt_manpower_plan_id_seq OWNED BY tb_pjt_manpower_plan.id;

ALTER TABLE ONLY tb_pjt_manpower_plan ALTER COLUMN id SET DEFAULT nextval('tb_pjt_manpower_plan_id_seq'::regclass);

DO $$ BEGIN
    ALTER TABLE ONLY tb_pjt_manpower_plan
    ADD CONSTRAINT tb_pjt_manpower_plan_pkey PRIMARY KEY (id);
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

CREATE INDEX IF NOT EXISTS idx_tb_pjt_manpower_proj ON tb_pjt_manpower_plan USING btree (proj_id);

-- ----- tb_pjt_schedule -----
CREATE TABLE IF NOT EXISTS tb_pjt_schedule (
    id bigint NOT NULL,
    proj_id bigint NOT NULL,
    process_name character varying(200) NOT NULL,
    m01 boolean DEFAULT false,
    m02 boolean DEFAULT false,
    m03 boolean DEFAULT false,
    m04 boolean DEFAULT false,
    m05 boolean DEFAULT false,
    m06 boolean DEFAULT false,
    m07 boolean DEFAULT false,
    m08 boolean DEFAULT false,
    m09 boolean DEFAULT false,
    m10 boolean DEFAULT false,
    m11 boolean DEFAULT false,
    m12 boolean DEFAULT false,
    remark character varying(300),
    sort_order integer DEFAULT 0
);

CREATE SEQUENCE IF NOT EXISTS tb_pjt_schedule_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE tb_pjt_schedule_id_seq OWNED BY tb_pjt_schedule.id;

ALTER TABLE ONLY tb_pjt_schedule ALTER COLUMN id SET DEFAULT nextval('tb_pjt_schedule_id_seq'::regclass);

DO $$ BEGIN
    ALTER TABLE ONLY tb_pjt_schedule
    ADD CONSTRAINT tb_pjt_schedule_pkey PRIMARY KEY (id);
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

CREATE INDEX IF NOT EXISTS idx_tb_pjt_schedule_proj ON tb_pjt_schedule USING btree (proj_id);

-- =========================================================
-- Section 2: FK 제약 일괄 적용 (모든 부모 테이블 생성 후, 멱등화)
-- =========================================================

DO $$ BEGIN
    ALTER TABLE ONLY ps_info
    ADD CONSTRAINT fk_ps_sys_cd FOREIGN KEY (sys_nm_en) REFERENCES public.sys_mst(cd);
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

DO $$ BEGIN
    ALTER TABLE ONLY sw_pjt
    ADD CONSTRAINT fk_biz_cat FOREIGN KEY (biz_cat_en) REFERENCES public.prj_types(cd);
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

DO $$ BEGIN
    ALTER TABLE ONLY sw_pjt
    ADD CONSTRAINT fk_dist_cd FOREIGN KEY (dist_cd) REFERENCES public.sigungu_code(adm_sect_c);
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

DO $$ BEGIN
    ALTER TABLE ONLY sw_pjt
    ADD CONSTRAINT fk_maint_type FOREIGN KEY (maint_type) REFERENCES public.maint_tp_mst(cd);
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

DO $$ BEGIN
    ALTER TABLE ONLY sw_pjt
    ADD CONSTRAINT fk_stat FOREIGN KEY (stat) REFERENCES public.cont_stat_mst(cd);
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

DO $$ BEGIN
    ALTER TABLE ONLY sw_pjt
    ADD CONSTRAINT fk_sys_cd FOREIGN KEY (sys_nm_en) REFERENCES public.sys_mst(cd);
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

DO $$ BEGIN
    ALTER TABLE ONLY tb_infra_server
    ADD CONSTRAINT fk_server_master FOREIGN KEY (infra_id) REFERENCES public.tb_infra_master(infra_id) ON DELETE CASCADE;
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

DO $$ BEGIN
    ALTER TABLE ONLY tb_infra_software
    ADD CONSTRAINT fk_sw_server FOREIGN KEY (server_id) REFERENCES public.tb_infra_server(server_id) ON DELETE CASCADE;
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

DO $$ BEGIN
    ALTER TABLE ONLY tb_infra_link_upis
    ADD CONSTRAINT fk_upis_master FOREIGN KEY (infra_id) REFERENCES public.tb_infra_master(infra_id) ON DELETE CASCADE;
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

DO $$ BEGIN
    ALTER TABLE ONLY tb_infra_link_api
    ADD CONSTRAINT fk_api_master FOREIGN KEY (infra_id) REFERENCES public.tb_infra_master(infra_id) ON DELETE CASCADE;
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

DO $$ BEGIN
    ALTER TABLE ONLY tb_infra_memo
    ADD CONSTRAINT fk_infra_memo FOREIGN KEY (infra_id) REFERENCES public.tb_infra_master(infra_id) ON DELETE CASCADE;
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

DO $$ BEGIN
    ALTER TABLE ONLY tb_pjt_target
    ADD CONSTRAINT tb_pjt_target_proj_id_fkey FOREIGN KEY (proj_id) REFERENCES public.sw_pjt(proj_id) ON DELETE CASCADE;
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

DO $$ BEGIN
    ALTER TABLE ONLY tb_pjt_manpower_plan
    ADD CONSTRAINT tb_pjt_manpower_plan_proj_id_fkey FOREIGN KEY (proj_id) REFERENCES public.sw_pjt(proj_id) ON DELETE CASCADE;
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

DO $$ BEGIN
    ALTER TABLE ONLY tb_pjt_schedule
    ADD CONSTRAINT tb_pjt_schedule_proj_id_fkey FOREIGN KEY (proj_id) REFERENCES public.sw_pjt(proj_id) ON DELETE CASCADE;
EXCEPTION
    WHEN duplicate_object THEN NULL;
    WHEN duplicate_table THEN NULL;
    WHEN invalid_table_definition THEN NULL;
END $$;

-- =========================================================
-- Section 3: 마스터 시드 (운영DB 실데이터 기반, ~54건, ON CONFLICT DO NOTHING)
-- =========================================================
-- 운영DB 데이터와 충돌 없이 신규 환경에서만 적재됨.
-- 출처: 운영DB pg_dump --data-only (2026-04-27)

-- sys_mst (37건)
INSERT INTO sys_mst (cd, nm) VALUES ('112', '112시스템') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('ANCISS', '공항소음대책정보화 및 지원시스템') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('APIMS', '농업생산기반시설관리플랫폼') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('ARPMS', '공항활주로 포장관리시스템') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('BIS', '버스정보시스템') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('BMIS', '전장관리정보체계') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('CDMS', '상권관리시스템') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('CGM', 'CLX GPS MAP') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('CPS', '지적포털 시스템') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('CREIS', '종합부동산정보시스템') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('DEIMS', '배전설비정보관리시스템') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('DFGIS', '배전설비 지리정보시스템') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('GCRM', 'GCRM') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('GMPSS', '성장관리시스템') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('HIS', '택지정보시스템') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('JUSO', '국가주소정보시스템') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('KRAS', '부동산종합공부시스템') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('LSA', '토지적성평가 업무 프로그램') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('LSMS', '토지현황관리시스템') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('LTCS', '위치추적관제시스템') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('MPMIS', '국유재산관리조사시스템') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('MPMS', '국유재산관리시스템') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('NDTIS', '국방수송정보체계') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('NPFMS', '국립공원 시설관리시스템') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('NSDI', '국가공간정보체계') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('PGMS', '스마트공원녹지플랫폼') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('RAISE', '농어촌개발관리정보시스템') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('SC', '스마트도시 통합플랫폼') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('SMMS', 'aT센터 학교급식 모바일 시스템') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('SUFM', '스마트도시 시설물관제') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('UPIS', '도시계획정보체계') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('URTIS', '도시재생종합관리시스템') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('VLICMS', '베트남토지정보 종합관리시스템') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('IPSS', '통합인허가관리시스템') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('UPBSS', '기초조사정보시스템 v1.0') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('BSIS', '기초조사정보시스템 v1.5') ON CONFLICT (cd) DO NOTHING;
INSERT INTO sys_mst (cd, nm) VALUES ('BSISP', '기초조사정보시스템 v2.0') ON CONFLICT (cd) DO NOTHING;

-- prj_types (3건)
INSERT INTO prj_types (cd, nm) VALUES ('GISSW', '원천소프트웨어') ON CONFLICT (cd) DO NOTHING;
INSERT INTO prj_types (cd, nm) VALUES ('PKSW', '패키지소프트웨어') ON CONFLICT (cd) DO NOTHING;
INSERT INTO prj_types (cd, nm) VALUES ('TS', '기술지원') ON CONFLICT (cd) DO NOTHING;

-- cont_stat_mst (2건)
INSERT INTO cont_stat_mst (cd, nm) VALUES ('1', '진행중') ON CONFLICT (cd) DO NOTHING;
INSERT INTO cont_stat_mst (cd, nm) VALUES ('2', '완료') ON CONFLICT (cd) DO NOTHING;

-- cont_frm_mst (5건)
INSERT INTO cont_frm_mst (cd, nm) VALUES ('1', '도입') ON CONFLICT (cd) DO NOTHING;
INSERT INTO cont_frm_mst (cd, nm) VALUES ('2', '교체') ON CONFLICT (cd) DO NOTHING;
INSERT INTO cont_frm_mst (cd, nm) VALUES ('3', '무상') ON CONFLICT (cd) DO NOTHING;
INSERT INTO cont_frm_mst (cd, nm) VALUES ('4', '유상') ON CONFLICT (cd) DO NOTHING;
INSERT INTO cont_frm_mst (cd, nm) VALUES ('5', '종료') ON CONFLICT (cd) DO NOTHING;

-- maint_tp_mst (7건)
INSERT INTO maint_tp_mst (cd, nm) VALUES ('DU', '데이터 업로드') ON CONFLICT (cd) DO NOTHING;
INSERT INTO maint_tp_mst (cd, nm) VALUES ('SW', 'SW') ON CONFLICT (cd) DO NOTHING;
INSERT INTO maint_tp_mst (cd, nm) VALUES ('HS', 'HW/SW') ON CONFLICT (cd) DO NOTHING;
INSERT INTO maint_tp_mst (cd, nm) VALUES ('DS', 'DB/SW') ON CONFLICT (cd) DO NOTHING;
INSERT INTO maint_tp_mst (cd, nm) VALUES ('DHS', 'DB/HW/SW') ON CONFLICT (cd) DO NOTHING;
INSERT INTO maint_tp_mst (cd, nm) VALUES ('BASIC', '토지적성평가 베이직') ON CONFLICT (cd) DO NOTHING;
INSERT INTO maint_tp_mst (cd, nm) VALUES ('Pro', '토지적성평가 프로') ON CONFLICT (cd) DO NOTHING;

COMMIT;

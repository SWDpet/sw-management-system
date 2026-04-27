--
-- PostgreSQL database dump
--

\restrict D6RcMa6ZL7nqIrNHsTP1mn5aqASVJViVdUzOAMvnNsjaZRXkrtKVgmaDhn55ee2

-- Dumped from database version 16.11
-- Dumped by pg_dump version 16.11

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: fn_update_timestamp(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_update_timestamp() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$;


SET default_table_access_method = heap;

--
-- Name: access_logs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.access_logs (
    log_id bigint NOT NULL,
    userid character varying(50),
    username character varying(50),
    ip_addr character varying(45),
    access_time timestamp without time zone DEFAULT now(),
    menu_nm character varying(100),
    action_type character varying(50),
    action_detail text
);


--
-- Name: access_logs_cleanup_backup_20260420_200859; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.access_logs_cleanup_backup_20260420_200859 (
    log_id bigint,
    userid character varying(50),
    username character varying(50),
    ip_addr character varying(45),
    access_time timestamp without time zone,
    menu_nm character varying(100),
    action_type character varying(50),
    action_detail text
);


--
-- Name: access_logs_cleanup_backup_20260420_200917; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.access_logs_cleanup_backup_20260420_200917 (
    log_id bigint,
    userid character varying(50),
    username character varying(50),
    ip_addr character varying(45),
    access_time timestamp without time zone,
    menu_nm character varying(100),
    action_type character varying(50),
    action_detail text
);


--
-- Name: access_logs_log_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.access_logs_log_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: access_logs_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.access_logs_log_id_seq OWNED BY public.access_logs.log_id;


--
-- Name: check_category_mst; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.check_category_mst (
    section_code character varying(20) NOT NULL,
    category_code character varying(50) NOT NULL,
    category_label character varying(100) NOT NULL,
    display_order integer DEFAULT 0 NOT NULL
);


--
-- Name: check_section_mst; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.check_section_mst (
    section_code character varying(20) NOT NULL,
    section_label character varying(100) NOT NULL,
    section_group character varying(20) DEFAULT 'CORE'::character varying NOT NULL,
    display_order integer DEFAULT 0 NOT NULL
);


--
-- Name: cont_frm_mst; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cont_frm_mst (
    cd character varying(10) NOT NULL,
    nm character varying(20) NOT NULL
);


--
-- Name: cont_stat_mst; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cont_stat_mst (
    cd character varying(10) NOT NULL,
    nm character varying(20) NOT NULL
);


--
-- Name: geonuris_license; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.geonuris_license (
    id bigint NOT NULL,
    license_type character varying(20) NOT NULL,
    file_name character varying(100),
    user_name character varying(100),
    organization character varying(200),
    phone character varying(50),
    email character varying(150),
    issuer character varying(100),
    mac_address character varying(50) NOT NULL,
    permission character varying(1),
    start_date timestamp without time zone NOT NULL,
    expiry_date timestamp without time zone NOT NULL,
    dbms_type character varying(20),
    setl_count integer DEFAULT 0,
    plugin_edit boolean DEFAULT false,
    plugin_gdm boolean DEFAULT false,
    plugin_tmbuilder boolean DEFAULT false,
    plugin_setl boolean DEFAULT false,
    license_data text,
    created_at timestamp without time zone DEFAULT now(),
    created_by character varying(100)
);


--
-- Name: geonuris_license_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.geonuris_license_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: geonuris_license_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.geonuris_license_id_seq OWNED BY public.geonuris_license.id;


--
-- Name: inspect_check_result; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.inspect_check_result (
    id bigint NOT NULL,
    report_id bigint NOT NULL,
    section character varying(20) NOT NULL,
    category character varying(50),
    item_name character varying(200),
    item_method character varying(300),
    remarks character varying(300),
    sort_order integer DEFAULT 0,
    created_at timestamp without time zone DEFAULT now(),
    result_code character varying(20),
    result_text character varying(500)
);


--
-- Name: inspect_check_result_backup_20260421_235813; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.inspect_check_result_backup_20260421_235813 (
    id bigint,
    report_id bigint,
    section character varying(20),
    category character varying(50),
    item_name character varying(200),
    item_method character varying(300),
    result character varying(500),
    remarks character varying(300),
    sort_order integer,
    created_at timestamp without time zone
);


--
-- Name: inspect_check_result_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.inspect_check_result_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: inspect_check_result_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.inspect_check_result_id_seq OWNED BY public.inspect_check_result.id;


--
-- Name: inspect_report; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.inspect_report (
    id bigint NOT NULL,
    pjt_id bigint NOT NULL,
    inspect_month character varying(7),
    sys_type character varying(20),
    doc_title character varying(300),
    insp_dbms character varying(200),
    insp_gis character varying(200),
    dbms_ip character varying(50),
    status character varying(20) DEFAULT 'DRAFT'::character varying,
    created_by character varying(50),
    updated_by character varying(50),
    created_at timestamp without time zone DEFAULT now(),
    updated_at timestamp without time zone DEFAULT now(),
    insp_sign text,
    conf_sign text,
    insp_user_id bigint,
    conf_ps_info_id bigint
);


--
-- Name: inspect_report_backup_20260421_235813; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.inspect_report_backup_20260421_235813 (
    id bigint,
    pjt_id bigint,
    inspect_month character varying(7),
    sys_type character varying(20),
    doc_title character varying(300),
    insp_company character varying(100),
    insp_name character varying(50),
    conf_org character varying(100),
    conf_name character varying(50),
    insp_dbms character varying(200),
    insp_gis character varying(200),
    dbms_ip character varying(50),
    status character varying(20),
    created_by character varying(50),
    updated_by character varying(50),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    insp_sign text,
    conf_sign text
);


--
-- Name: inspect_report_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.inspect_report_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: inspect_report_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.inspect_report_id_seq OWNED BY public.inspect_report.id;


--
-- Name: inspect_template; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.inspect_template (
    id bigint NOT NULL,
    template_type character varying(20) NOT NULL,
    section character varying(20) NOT NULL,
    category character varying(50) NOT NULL,
    item_name character varying(200) NOT NULL,
    item_method character varying(300),
    sort_order integer DEFAULT 0,
    use_yn character varying(1) DEFAULT 'Y'::character varying,
    created_at timestamp without time zone DEFAULT now()
);


--
-- Name: inspect_template_backup_20260421_235813; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.inspect_template_backup_20260421_235813 (
    id bigint,
    template_type character varying(20),
    section character varying(20),
    category character varying(50),
    item_name character varying(200),
    item_method character varying(300),
    sort_order integer,
    use_yn character varying(1),
    created_at timestamp without time zone
);


--
-- Name: inspect_template_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.inspect_template_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: inspect_template_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.inspect_template_id_seq OWNED BY public.inspect_template.id;


--
-- Name: inspect_visit_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.inspect_visit_log (
    id bigint NOT NULL,
    report_id bigint NOT NULL,
    visit_year character varying(4),
    visit_month character varying(2),
    visit_day character varying(2),
    task character varying(200),
    symptom character varying(500),
    action character varying(500),
    sort_order integer DEFAULT 0,
    created_at timestamp without time zone DEFAULT now()
);


--
-- Name: inspect_visit_log_backup_20260421_235813; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.inspect_visit_log_backup_20260421_235813 (
    id bigint,
    report_id bigint,
    visit_year character varying(4),
    visit_month character varying(2),
    visit_day character varying(2),
    task character varying(200),
    symptom character varying(500),
    action character varying(500),
    sort_order integer,
    created_at timestamp without time zone
);


--
-- Name: inspect_visit_log_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.inspect_visit_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: inspect_visit_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.inspect_visit_log_id_seq OWNED BY public.inspect_visit_log.id;


--
-- Name: license_registry; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.license_registry (
    id bigint NOT NULL,
    license_id character varying(50) NOT NULL,
    product_id character varying(100) NOT NULL,
    license_type character varying(50),
    valid_product_edition character varying(100),
    valid_product_version character varying(50),
    validity_period integer,
    maintenance_period integer,
    generation_source character varying(50),
    generation_date_time timestamp without time zone,
    quantity integer,
    allowed_use_count integer,
    hardware_id text,
    registered_to character varying(200),
    full_name character varying(200),
    email character varying(200),
    company character varying(200),
    telephone character varying(50),
    fax character varying(50),
    street character varying(200),
    city character varying(100),
    zip_code character varying(20),
    country character varying(100),
    activation_required boolean,
    auto_activations_disabled boolean,
    manual_activations_disabled boolean,
    online_key_lease_disabled boolean,
    deactivations_disabled boolean,
    activation_period integer,
    allowed_activation_count integer,
    allowed_deactivation_count integer,
    dont_keep_deactivation_records boolean,
    ip_blocks text,
    ip_blocks_allow boolean,
    activation_return character varying(50),
    reject_modification_key_usage boolean,
    set_generation_time_to_activation_time boolean,
    generation_time_offset_from_activation_time integer,
    hardware_id_selection character varying(500),
    internal_hidden_string text,
    use_customer_name_in_validation boolean,
    use_company_name_in_validation boolean,
    floating_license_check_period integer,
    floating_license_server_connection_check_period integer,
    floating_allow_multiple_instances boolean,
    superseded_license_ids text,
    max_inactive_period integer,
    maximum_re_checks_before_drop integer,
    dont_keep_released_license_usage boolean,
    current_use_count integer,
    allowed_use_count_limit integer,
    current_use_time integer,
    allowed_use_time_limit integer,
    date_time_check character varying(50),
    ntp_server_check boolean,
    ntp_server character varying(200),
    ntp_server_timeout integer,
    web_server_check boolean,
    web_server character varying(500),
    web_server_timeout integer,
    query_local_ad_server boolean,
    unsigned_custom_features text,
    signed_custom_features text,
    license_string text NOT NULL,
    upload_date timestamp without time zone NOT NULL,
    uploaded_by character varying(100),
    created_date timestamp without time zone NOT NULL
);


--
-- Name: license_registry_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.license_registry_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: license_registry_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.license_registry_id_seq OWNED BY public.license_registry.id;


--
-- Name: license_upload_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.license_upload_history (
    id bigint NOT NULL,
    upload_date timestamp without time zone NOT NULL,
    file_name character varying(500),
    total_count integer NOT NULL,
    success_count integer NOT NULL,
    fail_count integer DEFAULT 0,
    uploaded_by character varying(100),
    created_date timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: license_upload_history_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.license_upload_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: license_upload_history_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.license_upload_history_id_seq OWNED BY public.license_upload_history.id;


--
-- Name: maint_tp_mst; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.maint_tp_mst (
    cd character varying(10) NOT NULL,
    nm character varying(50) NOT NULL
);


--
-- Name: prj_types; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.prj_types (
    cd character varying(10) NOT NULL,
    nm character varying(50) NOT NULL
);


--
-- Name: ps_info; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ps_info (
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


--
-- Name: ps_info_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.ps_info_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ps_info_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.ps_info_id_seq OWNED BY public.ps_info.id;


--
-- Name: qr_license; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.qr_license (
    qr_id bigint NOT NULL,
    end_user_name character varying(200) NOT NULL,
    address character varying(500),
    tel character varying(50),
    fax character varying(50),
    products character varying(500) NOT NULL,
    user_units character varying(100),
    version character varying(50),
    license_type character varying(100),
    serial_number character varying(200),
    application_name character varying(200),
    end_user_name_ko character varying(200),
    address_ko character varying(500),
    tel_ko character varying(50),
    fax_ko character varying(50),
    products_ko character varying(500),
    user_units_ko character varying(100),
    version_ko character varying(50),
    license_type_ko character varying(100),
    serial_number_ko character varying(200),
    application_name_ko character varying(200),
    qr_image_data text,
    issued_by character varying(50),
    issued_at timestamp without time zone DEFAULT now(),
    remarks character varying(1000)
);


--
-- Name: qr_license_qr_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.qr_license_qr_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: qr_license_qr_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.qr_license_qr_id_seq OWNED BY public.qr_license.qr_id;


--
-- Name: qt_category_mst; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.qt_category_mst (
    category_code character varying(10) NOT NULL,
    category_label character varying(50) NOT NULL,
    display_order integer DEFAULT 0 NOT NULL
);


--
-- Name: qt_product_pattern; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.qt_product_pattern (
    pattern_id bigint NOT NULL,
    category character varying(10) NOT NULL,
    pattern_group character varying(100) NOT NULL,
    product_name character varying(500) NOT NULL,
    default_unit character varying(10) DEFAULT '식'::character varying,
    default_unit_price bigint DEFAULT 0,
    description character varying(500),
    sub_items text,
    usage_count integer DEFAULT 0,
    calc_type character varying(10) DEFAULT 'NORMAL'::character varying,
    overhead_rate double precision DEFAULT 110.0,
    tech_fee_rate double precision DEFAULT 20.0
);


--
-- Name: qt_product_pattern_pattern_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.qt_product_pattern_pattern_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: qt_product_pattern_pattern_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.qt_product_pattern_pattern_id_seq OWNED BY public.qt_product_pattern.pattern_id;


--
-- Name: qt_quotation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.qt_quotation (
    quote_id bigint NOT NULL,
    quote_number character varying(30) NOT NULL,
    quote_date date NOT NULL,
    category character varying(10) NOT NULL,
    project_name character varying(500) NOT NULL,
    recipient character varying(200),
    reference_to character varying(200),
    total_amount bigint DEFAULT 0,
    total_amount_text character varying(200),
    vat_included boolean DEFAULT true,
    status character varying(10) DEFAULT '작성중'::character varying,
    created_by character varying(50),
    created_at timestamp without time zone DEFAULT now(),
    updated_at timestamp without time zone DEFAULT now(),
    rounddown_unit integer DEFAULT 1,
    show_seal boolean DEFAULT true,
    template_type integer DEFAULT 1,
    remarks character varying(2000),
    grand_total bigint,
    bid_rate double precision
);


--
-- Name: qt_quotation_item; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.qt_quotation_item (
    item_id bigint NOT NULL,
    quote_id bigint NOT NULL,
    item_no integer NOT NULL,
    product_name character varying(500) NOT NULL,
    specification text,
    quantity integer DEFAULT 1,
    unit character varying(10) DEFAULT '식'::character varying,
    unit_price bigint DEFAULT 0,
    amount bigint DEFAULT 0,
    remarks character varying(500)
);


--
-- Name: qt_quotation_item_item_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.qt_quotation_item_item_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: qt_quotation_item_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.qt_quotation_item_item_id_seq OWNED BY public.qt_quotation_item.item_id;


--
-- Name: qt_quotation_ledger; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.qt_quotation_ledger (
    ledger_id bigint NOT NULL,
    quote_id bigint NOT NULL,
    ledger_no integer,
    year integer NOT NULL,
    category character varying(10) NOT NULL,
    quote_number character varying(30) NOT NULL,
    quote_date date NOT NULL,
    project_name character varying(500) NOT NULL,
    total_amount bigint DEFAULT 0,
    recipient character varying(200),
    reference_to character varying(200),
    created_by character varying(50),
    registered_at timestamp without time zone DEFAULT now(),
    grand_total bigint
);


--
-- Name: qt_quotation_ledger_ledger_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.qt_quotation_ledger_ledger_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: qt_quotation_ledger_ledger_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.qt_quotation_ledger_ledger_id_seq OWNED BY public.qt_quotation_ledger.ledger_id;


--
-- Name: qt_quotation_quote_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.qt_quotation_quote_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: qt_quotation_quote_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.qt_quotation_quote_id_seq OWNED BY public.qt_quotation.quote_id;


--
-- Name: qt_quote_number_seq; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.qt_quote_number_seq (
    seq_id bigint NOT NULL,
    year integer NOT NULL,
    category character varying(10) NOT NULL,
    last_seq integer DEFAULT 0
);


--
-- Name: qt_quote_number_seq_seq_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.qt_quote_number_seq_seq_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: qt_quote_number_seq_seq_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.qt_quote_number_seq_seq_id_seq OWNED BY public.qt_quote_number_seq.seq_id;


--
-- Name: qt_remarks_pattern; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.qt_remarks_pattern (
    pattern_id bigint NOT NULL,
    pattern_name character varying(100) NOT NULL,
    content text NOT NULL,
    sort_order integer DEFAULT 0,
    user_id bigint
);


--
-- Name: qt_remarks_pattern_pattern_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.qt_remarks_pattern_pattern_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: qt_remarks_pattern_pattern_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.qt_remarks_pattern_pattern_id_seq OWNED BY public.qt_remarks_pattern.pattern_id;


--
-- Name: qt_remarks_pattern_v020_backup_20260420_211550; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.qt_remarks_pattern_v020_backup_20260420_211550 (
    pattern_id bigint,
    pattern_name character varying(100),
    content text,
    sort_order integer,
    user_id bigint
);


--
-- Name: qt_remarks_pattern_v020_backup_20260420_211552; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.qt_remarks_pattern_v020_backup_20260420_211552 (
    pattern_id bigint,
    pattern_name character varying(100),
    content text,
    sort_order integer,
    user_id bigint
);


--
-- Name: qt_wage_rate; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.qt_wage_rate (
    wage_id bigint NOT NULL,
    year integer NOT NULL,
    grade_name character varying(50) NOT NULL,
    daily_rate bigint DEFAULT 0 NOT NULL,
    monthly_rate bigint DEFAULT 0,
    hourly_rate bigint DEFAULT 0,
    description character varying(200)
);


--
-- Name: qt_wage_rate_wage_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.qt_wage_rate_wage_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: qt_wage_rate_wage_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.qt_wage_rate_wage_id_seq OWNED BY public.qt_wage_rate.wage_id;


--
-- Name: seq_doc_no; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.seq_doc_no
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sigungu_code; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sigungu_code (
    instt_c character varying(20),
    adm_sect_c character varying(20) NOT NULL,
    full_name character varying(100),
    sido_name character varying(50),
    sgg_name character varying(50)
);


--
-- Name: sw_pjt; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sw_pjt (
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


--
-- Name: sw_pjt_proj_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sw_pjt_proj_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sw_pjt_proj_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.sw_pjt_proj_id_seq OWNED BY public.sw_pjt.proj_id;


--
-- Name: sys_mst; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sys_mst (
    cd character varying(10) NOT NULL,
    nm character varying(100) NOT NULL
);


--
-- Name: tb_contract_participant; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tb_contract_participant (
    participant_id integer NOT NULL,
    user_id bigint,
    role_type character varying(20) DEFAULT 'PARTICIPANT'::character varying NOT NULL,
    tech_grade character varying(20),
    task_desc character varying(500),
    is_site_rep boolean DEFAULT false NOT NULL,
    sort_order integer DEFAULT 0,
    proj_id bigint
);


--
-- Name: tb_contract_participant_participant_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tb_contract_participant_participant_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tb_contract_participant_participant_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tb_contract_participant_participant_id_seq OWNED BY public.tb_contract_participant.participant_id;


--
-- Name: tb_document; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tb_document (
    doc_id integer NOT NULL,
    doc_no character varying(50),
    doc_type character varying(30) NOT NULL,
    sys_type character varying(20),
    infra_id bigint,
    plan_id integer,
    title character varying(500) NOT NULL,
    status character varying(20) DEFAULT 'DRAFT'::character varying NOT NULL,
    author_id bigint NOT NULL,
    approver_id bigint,
    approved_at timestamp without time zone,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    proj_id bigint,
    support_target_type character varying(20),
    org_unit_id bigint,
    environment character varying(20),
    region_code character varying(10)
);


--
-- Name: tb_document_attachment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tb_document_attachment (
    attach_id integer NOT NULL,
    doc_id integer NOT NULL,
    file_name character varying(300) NOT NULL,
    file_path character varying(500) NOT NULL,
    file_size bigint,
    mime_type character varying(100),
    uploaded_at timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: tb_document_attachment_attach_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tb_document_attachment_attach_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tb_document_attachment_attach_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tb_document_attachment_attach_id_seq OWNED BY public.tb_document_attachment.attach_id;


--
-- Name: tb_document_detail; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tb_document_detail (
    detail_id integer NOT NULL,
    doc_id integer NOT NULL,
    section_key character varying(50) NOT NULL,
    section_data jsonb DEFAULT '{}'::jsonb NOT NULL,
    sort_order integer DEFAULT 0
);


--
-- Name: tb_document_detail_detail_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tb_document_detail_detail_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tb_document_detail_detail_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tb_document_detail_detail_id_seq OWNED BY public.tb_document_detail.detail_id;


--
-- Name: tb_document_doc_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tb_document_doc_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tb_document_doc_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tb_document_doc_id_seq OWNED BY public.tb_document.doc_id;


--
-- Name: tb_document_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tb_document_history (
    history_id integer NOT NULL,
    doc_id integer NOT NULL,
    action character varying(30) NOT NULL,
    changed_field character varying(100),
    old_value text,
    new_value text,
    actor_id bigint NOT NULL,
    comment text,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: tb_document_history_history_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tb_document_history_history_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tb_document_history_history_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tb_document_history_history_id_seq OWNED BY public.tb_document_history.history_id;


--
-- Name: tb_document_signature; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tb_document_signature (
    sign_id integer NOT NULL,
    doc_id integer NOT NULL,
    signer_type character varying(30) NOT NULL,
    signer_name character varying(50) NOT NULL,
    signer_org character varying(200),
    signature_image character varying(500),
    signed_at timestamp without time zone,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: tb_document_signature_sign_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tb_document_signature_sign_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tb_document_signature_sign_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tb_document_signature_sign_id_seq OWNED BY public.tb_document_signature.sign_id;


--
-- Name: tb_infra_link_api; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tb_infra_link_api (
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


--
-- Name: tb_infra_link_api_api_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tb_infra_link_api_api_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tb_infra_link_api_api_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tb_infra_link_api_api_id_seq OWNED BY public.tb_infra_link_api.api_id;


--
-- Name: tb_infra_link_upis; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tb_infra_link_upis (
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


--
-- Name: tb_infra_link_upis_link_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tb_infra_link_upis_link_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tb_infra_link_upis_link_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tb_infra_link_upis_link_id_seq OWNED BY public.tb_infra_link_upis.link_id;


--
-- Name: tb_infra_master; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tb_infra_master (
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


--
-- Name: tb_infra_master_infra_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tb_infra_master_infra_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tb_infra_master_infra_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tb_infra_master_infra_id_seq OWNED BY public.tb_infra_master.infra_id;


--
-- Name: tb_infra_memo; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tb_infra_memo (
    memo_id bigint NOT NULL,
    infra_id bigint NOT NULL,
    memo_content text,
    memo_writer character varying(50),
    memo_date date
);


--
-- Name: tb_infra_memo_memo_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tb_infra_memo_memo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tb_infra_memo_memo_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tb_infra_memo_memo_id_seq OWNED BY public.tb_infra_memo.memo_id;


--
-- Name: tb_infra_server; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tb_infra_server (
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


--
-- Name: tb_infra_server_server_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tb_infra_server_server_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tb_infra_server_server_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tb_infra_server_server_id_seq OWNED BY public.tb_infra_server.server_id;


--
-- Name: tb_infra_software; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tb_infra_software (
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


--
-- Name: tb_infra_software_sw_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tb_infra_software_sw_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tb_infra_software_sw_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tb_infra_software_sw_id_seq OWNED BY public.tb_infra_software.sw_id;


--
-- Name: tb_inspect_checklist; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tb_inspect_checklist (
    check_id bigint NOT NULL,
    doc_id bigint NOT NULL,
    inspect_month character varying(7),
    target_sw character varying(50),
    check_item character varying(300) NOT NULL,
    check_method character varying(500),
    check_result character varying(20),
    sort_order integer DEFAULT 0,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: tb_inspect_checklist_check_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tb_inspect_checklist_check_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tb_inspect_checklist_check_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tb_inspect_checklist_check_id_seq OWNED BY public.tb_inspect_checklist.check_id;


--
-- Name: tb_inspect_issue; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tb_inspect_issue (
    issue_id bigint NOT NULL,
    doc_id bigint NOT NULL,
    issue_year character varying(4),
    issue_month character varying(2),
    issue_day character varying(2),
    task_type character varying(50),
    symptom text,
    action_taken text,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: tb_inspect_issue_issue_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tb_inspect_issue_issue_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tb_inspect_issue_issue_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tb_inspect_issue_issue_id_seq OWNED BY public.tb_inspect_issue.issue_id;


--
-- Name: tb_org_unit; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tb_org_unit (
    unit_id bigint NOT NULL,
    parent_id bigint,
    unit_type character varying(20) NOT NULL,
    name character varying(100) NOT NULL,
    sort_order integer DEFAULT 0,
    use_yn character varying(1) DEFAULT 'Y'::character varying,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    CONSTRAINT tb_org_unit_unit_type_check CHECK (((unit_type)::text = ANY ((ARRAY['DIVISION'::character varying, 'DEPARTMENT'::character varying, 'TEAM'::character varying])::text[])))
);


--
-- Name: tb_org_unit_unit_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tb_org_unit_unit_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tb_org_unit_unit_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tb_org_unit_unit_id_seq OWNED BY public.tb_org_unit.unit_id;


--
-- Name: tb_performance_summary; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tb_performance_summary (
    summary_id integer NOT NULL,
    user_id bigint NOT NULL,
    period_year integer NOT NULL,
    period_month integer NOT NULL,
    install_count integer DEFAULT 0,
    patch_count integer DEFAULT 0,
    fault_count integer DEFAULT 0,
    fault_avg_hours numeric(8,2),
    support_count integer DEFAULT 0,
    inspect_plan_count integer DEFAULT 0,
    inspect_done_count integer DEFAULT 0,
    interim_count integer DEFAULT 0,
    completion_count integer DEFAULT 0,
    infra_count integer DEFAULT 0,
    plan_total_count integer DEFAULT 0,
    plan_ontime_count integer DEFAULT 0,
    calculated_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: tb_performance_summary_summary_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tb_performance_summary_summary_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tb_performance_summary_summary_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tb_performance_summary_summary_id_seq OWNED BY public.tb_performance_summary.summary_id;


--
-- Name: tb_pjt_manpower_plan; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tb_pjt_manpower_plan (
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


--
-- Name: tb_pjt_manpower_plan_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tb_pjt_manpower_plan_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tb_pjt_manpower_plan_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tb_pjt_manpower_plan_id_seq OWNED BY public.tb_pjt_manpower_plan.id;


--
-- Name: tb_pjt_schedule; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tb_pjt_schedule (
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


--
-- Name: tb_pjt_schedule_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tb_pjt_schedule_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tb_pjt_schedule_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tb_pjt_schedule_id_seq OWNED BY public.tb_pjt_schedule.id;


--
-- Name: tb_pjt_target; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tb_pjt_target (
    id bigint NOT NULL,
    proj_id bigint NOT NULL,
    product_name character varying(200) NOT NULL,
    qty integer DEFAULT 1 NOT NULL,
    sort_order integer DEFAULT 0
);


--
-- Name: tb_pjt_target_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tb_pjt_target_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tb_pjt_target_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tb_pjt_target_id_seq OWNED BY public.tb_pjt_target.id;


--
-- Name: tb_process_master; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tb_process_master (
    process_id integer NOT NULL,
    sys_nm_en character varying(30) NOT NULL,
    process_name character varying(200) NOT NULL,
    sort_order integer DEFAULT 0,
    use_yn character varying(1) DEFAULT 'Y'::character varying
);


--
-- Name: tb_process_master_process_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tb_process_master_process_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tb_process_master_process_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tb_process_master_process_id_seq OWNED BY public.tb_process_master.process_id;


--
-- Name: tb_service_purpose; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tb_service_purpose (
    purpose_id integer NOT NULL,
    sys_nm_en character varying(30) NOT NULL,
    purpose_type character varying(20) NOT NULL,
    purpose_text text NOT NULL,
    sort_order integer DEFAULT 0,
    use_yn character varying(1) DEFAULT 'Y'::character varying
);


--
-- Name: tb_service_purpose_purpose_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tb_service_purpose_purpose_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tb_service_purpose_purpose_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tb_service_purpose_purpose_id_seq OWNED BY public.tb_service_purpose.purpose_id;


--
-- Name: tb_work_plan; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tb_work_plan (
    plan_id integer NOT NULL,
    infra_id bigint,
    plan_type character varying(30) NOT NULL,
    process_step integer,
    title character varying(300) NOT NULL,
    description text,
    assignee_id bigint,
    start_date date NOT NULL,
    end_date date,
    location character varying(300),
    repeat_type character varying(20) DEFAULT 'NONE'::character varying NOT NULL,
    parent_plan_id integer,
    status character varying(20) DEFAULT 'PLANNED'::character varying NOT NULL,
    status_reason character varying(500),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    created_by bigint,
    CONSTRAINT tb_work_plan_process_step_check CHECK (((process_step >= 1) AND (process_step <= 7)))
);


--
-- Name: tb_work_plan_plan_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tb_work_plan_plan_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tb_work_plan_plan_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tb_work_plan_plan_id_seq OWNED BY public.tb_work_plan.plan_id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
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


--
-- Name: users_user_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.users_user_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: users_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.users_user_id_seq OWNED BY public.users.user_id;


--
-- Name: users_v021_backup_20260420_220143; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users_v021_backup_20260420_220143 (
    user_id bigint,
    userid character varying(50),
    tel character varying(20),
    mobile character varying(20),
    email character varying(100)
);


--
-- Name: users_v021_backup_20260420_220144; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users_v021_backup_20260420_220144 (
    user_id bigint,
    userid character varying(50),
    tel character varying(20),
    mobile character varying(20),
    email character varying(100)
);


--
-- Name: work_plan_status_mst; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.work_plan_status_mst (
    status_code character varying(20) NOT NULL,
    status_label character varying(50) NOT NULL,
    display_order integer DEFAULT 0 NOT NULL
);


--
-- Name: work_plan_type_mst; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.work_plan_type_mst (
    type_code character varying(20) NOT NULL,
    type_label character varying(50) NOT NULL,
    color character varying(10) NOT NULL,
    display_order integer DEFAULT 0 NOT NULL
);


--
-- Name: access_logs log_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.access_logs ALTER COLUMN log_id SET DEFAULT nextval('public.access_logs_log_id_seq'::regclass);


--
-- Name: geonuris_license id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.geonuris_license ALTER COLUMN id SET DEFAULT nextval('public.geonuris_license_id_seq'::regclass);


--
-- Name: inspect_check_result id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inspect_check_result ALTER COLUMN id SET DEFAULT nextval('public.inspect_check_result_id_seq'::regclass);


--
-- Name: inspect_report id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inspect_report ALTER COLUMN id SET DEFAULT nextval('public.inspect_report_id_seq'::regclass);


--
-- Name: inspect_template id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inspect_template ALTER COLUMN id SET DEFAULT nextval('public.inspect_template_id_seq'::regclass);


--
-- Name: inspect_visit_log id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inspect_visit_log ALTER COLUMN id SET DEFAULT nextval('public.inspect_visit_log_id_seq'::regclass);


--
-- Name: license_registry id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.license_registry ALTER COLUMN id SET DEFAULT nextval('public.license_registry_id_seq'::regclass);


--
-- Name: license_upload_history id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.license_upload_history ALTER COLUMN id SET DEFAULT nextval('public.license_upload_history_id_seq'::regclass);


--
-- Name: ps_info id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ps_info ALTER COLUMN id SET DEFAULT nextval('public.ps_info_id_seq'::regclass);


--
-- Name: qr_license qr_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qr_license ALTER COLUMN qr_id SET DEFAULT nextval('public.qr_license_qr_id_seq'::regclass);


--
-- Name: qt_product_pattern pattern_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qt_product_pattern ALTER COLUMN pattern_id SET DEFAULT nextval('public.qt_product_pattern_pattern_id_seq'::regclass);


--
-- Name: qt_quotation quote_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qt_quotation ALTER COLUMN quote_id SET DEFAULT nextval('public.qt_quotation_quote_id_seq'::regclass);


--
-- Name: qt_quotation_item item_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qt_quotation_item ALTER COLUMN item_id SET DEFAULT nextval('public.qt_quotation_item_item_id_seq'::regclass);


--
-- Name: qt_quotation_ledger ledger_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qt_quotation_ledger ALTER COLUMN ledger_id SET DEFAULT nextval('public.qt_quotation_ledger_ledger_id_seq'::regclass);


--
-- Name: qt_quote_number_seq seq_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qt_quote_number_seq ALTER COLUMN seq_id SET DEFAULT nextval('public.qt_quote_number_seq_seq_id_seq'::regclass);


--
-- Name: qt_remarks_pattern pattern_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qt_remarks_pattern ALTER COLUMN pattern_id SET DEFAULT nextval('public.qt_remarks_pattern_pattern_id_seq'::regclass);


--
-- Name: qt_wage_rate wage_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qt_wage_rate ALTER COLUMN wage_id SET DEFAULT nextval('public.qt_wage_rate_wage_id_seq'::regclass);


--
-- Name: sw_pjt proj_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sw_pjt ALTER COLUMN proj_id SET DEFAULT nextval('public.sw_pjt_proj_id_seq'::regclass);


--
-- Name: tb_contract_participant participant_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_contract_participant ALTER COLUMN participant_id SET DEFAULT nextval('public.tb_contract_participant_participant_id_seq'::regclass);


--
-- Name: tb_document doc_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_document ALTER COLUMN doc_id SET DEFAULT nextval('public.tb_document_doc_id_seq'::regclass);


--
-- Name: tb_document_attachment attach_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_document_attachment ALTER COLUMN attach_id SET DEFAULT nextval('public.tb_document_attachment_attach_id_seq'::regclass);


--
-- Name: tb_document_detail detail_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_document_detail ALTER COLUMN detail_id SET DEFAULT nextval('public.tb_document_detail_detail_id_seq'::regclass);


--
-- Name: tb_document_history history_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_document_history ALTER COLUMN history_id SET DEFAULT nextval('public.tb_document_history_history_id_seq'::regclass);


--
-- Name: tb_document_signature sign_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_document_signature ALTER COLUMN sign_id SET DEFAULT nextval('public.tb_document_signature_sign_id_seq'::regclass);


--
-- Name: tb_infra_link_api api_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_infra_link_api ALTER COLUMN api_id SET DEFAULT nextval('public.tb_infra_link_api_api_id_seq'::regclass);


--
-- Name: tb_infra_link_upis link_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_infra_link_upis ALTER COLUMN link_id SET DEFAULT nextval('public.tb_infra_link_upis_link_id_seq'::regclass);


--
-- Name: tb_infra_master infra_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_infra_master ALTER COLUMN infra_id SET DEFAULT nextval('public.tb_infra_master_infra_id_seq'::regclass);


--
-- Name: tb_infra_memo memo_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_infra_memo ALTER COLUMN memo_id SET DEFAULT nextval('public.tb_infra_memo_memo_id_seq'::regclass);


--
-- Name: tb_infra_server server_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_infra_server ALTER COLUMN server_id SET DEFAULT nextval('public.tb_infra_server_server_id_seq'::regclass);


--
-- Name: tb_infra_software sw_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_infra_software ALTER COLUMN sw_id SET DEFAULT nextval('public.tb_infra_software_sw_id_seq'::regclass);


--
-- Name: tb_inspect_checklist check_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_inspect_checklist ALTER COLUMN check_id SET DEFAULT nextval('public.tb_inspect_checklist_check_id_seq'::regclass);


--
-- Name: tb_inspect_issue issue_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_inspect_issue ALTER COLUMN issue_id SET DEFAULT nextval('public.tb_inspect_issue_issue_id_seq'::regclass);


--
-- Name: tb_org_unit unit_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_org_unit ALTER COLUMN unit_id SET DEFAULT nextval('public.tb_org_unit_unit_id_seq'::regclass);


--
-- Name: tb_performance_summary summary_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_performance_summary ALTER COLUMN summary_id SET DEFAULT nextval('public.tb_performance_summary_summary_id_seq'::regclass);


--
-- Name: tb_pjt_manpower_plan id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_pjt_manpower_plan ALTER COLUMN id SET DEFAULT nextval('public.tb_pjt_manpower_plan_id_seq'::regclass);


--
-- Name: tb_pjt_schedule id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_pjt_schedule ALTER COLUMN id SET DEFAULT nextval('public.tb_pjt_schedule_id_seq'::regclass);


--
-- Name: tb_pjt_target id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_pjt_target ALTER COLUMN id SET DEFAULT nextval('public.tb_pjt_target_id_seq'::regclass);


--
-- Name: tb_process_master process_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_process_master ALTER COLUMN process_id SET DEFAULT nextval('public.tb_process_master_process_id_seq'::regclass);


--
-- Name: tb_service_purpose purpose_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_service_purpose ALTER COLUMN purpose_id SET DEFAULT nextval('public.tb_service_purpose_purpose_id_seq'::regclass);


--
-- Name: tb_work_plan plan_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_work_plan ALTER COLUMN plan_id SET DEFAULT nextval('public.tb_work_plan_plan_id_seq'::regclass);


--
-- Name: users user_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users ALTER COLUMN user_id SET DEFAULT nextval('public.users_user_id_seq'::regclass);


--
-- Name: access_logs access_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.access_logs
    ADD CONSTRAINT access_logs_pkey PRIMARY KEY (log_id);


--
-- Name: check_category_mst check_category_mst_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.check_category_mst
    ADD CONSTRAINT check_category_mst_pkey PRIMARY KEY (section_code, category_code);


--
-- Name: check_section_mst check_section_mst_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.check_section_mst
    ADD CONSTRAINT check_section_mst_pkey PRIMARY KEY (section_code);


--
-- Name: cont_frm_mst cont_frm_mst_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cont_frm_mst
    ADD CONSTRAINT cont_frm_mst_pkey PRIMARY KEY (cd);


--
-- Name: cont_stat_mst cont_stat_mst_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cont_stat_mst
    ADD CONSTRAINT cont_stat_mst_pkey PRIMARY KEY (cd);


--
-- Name: geonuris_license geonuris_license_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.geonuris_license
    ADD CONSTRAINT geonuris_license_pkey PRIMARY KEY (id);


--
-- Name: inspect_check_result inspect_check_result_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inspect_check_result
    ADD CONSTRAINT inspect_check_result_pkey PRIMARY KEY (id);


--
-- Name: inspect_report inspect_report_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inspect_report
    ADD CONSTRAINT inspect_report_pkey PRIMARY KEY (id);


--
-- Name: inspect_template inspect_template_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inspect_template
    ADD CONSTRAINT inspect_template_pkey PRIMARY KEY (id);


--
-- Name: inspect_visit_log inspect_visit_log_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inspect_visit_log
    ADD CONSTRAINT inspect_visit_log_pkey PRIMARY KEY (id);


--
-- Name: license_registry license_registry_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.license_registry
    ADD CONSTRAINT license_registry_pkey PRIMARY KEY (id);


--
-- Name: license_upload_history license_upload_history_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.license_upload_history
    ADD CONSTRAINT license_upload_history_pkey PRIMARY KEY (id);


--
-- Name: maint_tp_mst maint_tp_mst_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.maint_tp_mst
    ADD CONSTRAINT maint_tp_mst_pkey PRIMARY KEY (cd);


--
-- Name: prj_types prj_types_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.prj_types
    ADD CONSTRAINT prj_types_pkey PRIMARY KEY (cd);


--
-- Name: ps_info ps_info_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ps_info
    ADD CONSTRAINT ps_info_pkey PRIMARY KEY (id);


--
-- Name: qr_license qr_license_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qr_license
    ADD CONSTRAINT qr_license_pkey PRIMARY KEY (qr_id);


--
-- Name: qt_category_mst qt_category_mst_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qt_category_mst
    ADD CONSTRAINT qt_category_mst_pkey PRIMARY KEY (category_code);


--
-- Name: qt_product_pattern qt_product_pattern_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qt_product_pattern
    ADD CONSTRAINT qt_product_pattern_pkey PRIMARY KEY (pattern_id);


--
-- Name: qt_quotation_item qt_quotation_item_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qt_quotation_item
    ADD CONSTRAINT qt_quotation_item_pkey PRIMARY KEY (item_id);


--
-- Name: qt_quotation_ledger qt_quotation_ledger_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qt_quotation_ledger
    ADD CONSTRAINT qt_quotation_ledger_pkey PRIMARY KEY (ledger_id);


--
-- Name: qt_quotation qt_quotation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qt_quotation
    ADD CONSTRAINT qt_quotation_pkey PRIMARY KEY (quote_id);


--
-- Name: qt_quotation qt_quotation_quote_number_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qt_quotation
    ADD CONSTRAINT qt_quotation_quote_number_key UNIQUE (quote_number);


--
-- Name: qt_quote_number_seq qt_quote_number_seq_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qt_quote_number_seq
    ADD CONSTRAINT qt_quote_number_seq_pkey PRIMARY KEY (seq_id);


--
-- Name: qt_quote_number_seq qt_quote_number_seq_year_category_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qt_quote_number_seq
    ADD CONSTRAINT qt_quote_number_seq_year_category_key UNIQUE (year, category);


--
-- Name: qt_remarks_pattern qt_remarks_pattern_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qt_remarks_pattern
    ADD CONSTRAINT qt_remarks_pattern_pkey PRIMARY KEY (pattern_id);


--
-- Name: qt_wage_rate qt_wage_rate_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qt_wage_rate
    ADD CONSTRAINT qt_wage_rate_pkey PRIMARY KEY (wage_id);


--
-- Name: qt_wage_rate qt_wage_rate_year_grade_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qt_wage_rate
    ADD CONSTRAINT qt_wage_rate_year_grade_name_key UNIQUE (year, grade_name);


--
-- Name: sigungu_code sigungu_code_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sigungu_code
    ADD CONSTRAINT sigungu_code_pkey PRIMARY KEY (adm_sect_c);


--
-- Name: sw_pjt sw_pjt_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sw_pjt
    ADD CONSTRAINT sw_pjt_pkey PRIMARY KEY (proj_id);


--
-- Name: sys_mst sys_mst_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_mst
    ADD CONSTRAINT sys_mst_pkey PRIMARY KEY (cd);


--
-- Name: tb_contract_participant tb_contract_participant_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_contract_participant
    ADD CONSTRAINT tb_contract_participant_pkey PRIMARY KEY (participant_id);


--
-- Name: tb_document_attachment tb_document_attachment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_document_attachment
    ADD CONSTRAINT tb_document_attachment_pkey PRIMARY KEY (attach_id);


--
-- Name: tb_document_detail tb_document_detail_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_document_detail
    ADD CONSTRAINT tb_document_detail_pkey PRIMARY KEY (detail_id);


--
-- Name: tb_document_history tb_document_history_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_document_history
    ADD CONSTRAINT tb_document_history_pkey PRIMARY KEY (history_id);


--
-- Name: tb_document tb_document_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_document
    ADD CONSTRAINT tb_document_pkey PRIMARY KEY (doc_id);


--
-- Name: tb_document_signature tb_document_signature_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_document_signature
    ADD CONSTRAINT tb_document_signature_pkey PRIMARY KEY (sign_id);


--
-- Name: tb_infra_link_api tb_infra_link_api_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_infra_link_api
    ADD CONSTRAINT tb_infra_link_api_pkey PRIMARY KEY (api_id);


--
-- Name: tb_infra_link_upis tb_infra_link_upis_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_infra_link_upis
    ADD CONSTRAINT tb_infra_link_upis_pkey PRIMARY KEY (link_id);


--
-- Name: tb_infra_master tb_infra_master_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_infra_master
    ADD CONSTRAINT tb_infra_master_pkey PRIMARY KEY (infra_id);


--
-- Name: tb_infra_memo tb_infra_memo_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_infra_memo
    ADD CONSTRAINT tb_infra_memo_pkey PRIMARY KEY (memo_id);


--
-- Name: tb_infra_server tb_infra_server_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_infra_server
    ADD CONSTRAINT tb_infra_server_pkey PRIMARY KEY (server_id);


--
-- Name: tb_infra_software tb_infra_software_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_infra_software
    ADD CONSTRAINT tb_infra_software_pkey PRIMARY KEY (sw_id);


--
-- Name: tb_inspect_checklist tb_inspect_checklist_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_inspect_checklist
    ADD CONSTRAINT tb_inspect_checklist_pkey PRIMARY KEY (check_id);


--
-- Name: tb_inspect_issue tb_inspect_issue_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_inspect_issue
    ADD CONSTRAINT tb_inspect_issue_pkey PRIMARY KEY (issue_id);


--
-- Name: tb_org_unit tb_org_unit_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_org_unit
    ADD CONSTRAINT tb_org_unit_pkey PRIMARY KEY (unit_id);


--
-- Name: tb_performance_summary tb_performance_summary_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_performance_summary
    ADD CONSTRAINT tb_performance_summary_pkey PRIMARY KEY (summary_id);


--
-- Name: tb_performance_summary tb_performance_summary_user_id_period_year_period_month_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_performance_summary
    ADD CONSTRAINT tb_performance_summary_user_id_period_year_period_month_key UNIQUE (user_id, period_year, period_month);


--
-- Name: tb_pjt_manpower_plan tb_pjt_manpower_plan_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_pjt_manpower_plan
    ADD CONSTRAINT tb_pjt_manpower_plan_pkey PRIMARY KEY (id);


--
-- Name: tb_pjt_schedule tb_pjt_schedule_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_pjt_schedule
    ADD CONSTRAINT tb_pjt_schedule_pkey PRIMARY KEY (id);


--
-- Name: tb_pjt_target tb_pjt_target_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_pjt_target
    ADD CONSTRAINT tb_pjt_target_pkey PRIMARY KEY (id);


--
-- Name: tb_process_master tb_process_master_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_process_master
    ADD CONSTRAINT tb_process_master_pkey PRIMARY KEY (process_id);


--
-- Name: tb_service_purpose tb_service_purpose_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_service_purpose
    ADD CONSTRAINT tb_service_purpose_pkey PRIMARY KEY (purpose_id);


--
-- Name: tb_work_plan tb_work_plan_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_work_plan
    ADD CONSTRAINT tb_work_plan_pkey PRIMARY KEY (plan_id);


--
-- Name: qt_quote_number_seq ukjr58vo0b4b249r8gwheedruyh; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qt_quote_number_seq
    ADD CONSTRAINT ukjr58vo0b4b249r8gwheedruyh UNIQUE (year, category);


--
-- Name: qt_wage_rate ukruhep6rvknv2gvfwnj86scjfk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qt_wage_rate
    ADD CONSTRAINT ukruhep6rvknv2gvfwnj86scjfk UNIQUE (year, grade_name);


--
-- Name: tb_performance_summary uks4e97m3fdgq761y3voq2qb8w1; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_performance_summary
    ADD CONSTRAINT uks4e97m3fdgq761y3voq2qb8w1 UNIQUE (user_id, period_year, period_month);


--
-- Name: users unique_userid; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT unique_userid UNIQUE (userid);


--
-- Name: license_registry uq_license_product; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.license_registry
    ADD CONSTRAINT uq_license_product UNIQUE (license_id, product_id);


--
-- Name: tb_process_master uq_process_master_sys_name; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_process_master
    ADD CONSTRAINT uq_process_master_sys_name UNIQUE (sys_nm_en, process_name);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (userid);


--
-- Name: work_plan_status_mst work_plan_status_mst_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.work_plan_status_mst
    ADD CONSTRAINT work_plan_status_mst_pkey PRIMARY KEY (status_code);


--
-- Name: work_plan_type_mst work_plan_type_mst_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.work_plan_type_mst
    ADD CONSTRAINT work_plan_type_mst_pkey PRIMARY KEY (type_code);


--
-- Name: idx_attachment_doc; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_attachment_doc ON public.tb_document_attachment USING btree (doc_id);


--
-- Name: idx_check_result_report; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_check_result_report ON public.inspect_check_result USING btree (report_id);


--
-- Name: idx_check_result_section; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_check_result_section ON public.inspect_check_result USING btree (report_id, section);


--
-- Name: idx_detail_doc; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_detail_doc ON public.tb_document_detail USING btree (doc_id);


--
-- Name: idx_detail_section; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_detail_section ON public.tb_document_detail USING btree (doc_id, section_key);


--
-- Name: idx_doc_author; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_doc_author ON public.tb_document USING btree (author_id);


--
-- Name: idx_doc_created; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_doc_created ON public.tb_document USING btree (created_at);


--
-- Name: idx_doc_infra; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_doc_infra ON public.tb_document USING btree (infra_id);


--
-- Name: idx_doc_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_doc_status ON public.tb_document USING btree (status);


--
-- Name: idx_doc_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_doc_type ON public.tb_document USING btree (doc_type);


--
-- Name: idx_history_actor; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_history_actor ON public.tb_document_history USING btree (actor_id);


--
-- Name: idx_history_doc; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_history_doc ON public.tb_document_history USING btree (doc_id);


--
-- Name: idx_inspect_report_month; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_inspect_report_month ON public.inspect_report USING btree (inspect_month);


--
-- Name: idx_inspect_report_pjt; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_inspect_report_pjt ON public.inspect_report USING btree (pjt_id);


--
-- Name: idx_inspect_template_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_inspect_template_type ON public.inspect_template USING btree (template_type, section);


--
-- Name: idx_lr_company; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_lr_company ON public.license_registry USING btree (company);


--
-- Name: idx_lr_full_name; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_lr_full_name ON public.license_registry USING btree (full_name);


--
-- Name: idx_lr_generation_date_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_lr_generation_date_time ON public.license_registry USING btree (generation_date_time);


--
-- Name: idx_lr_hardware_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_lr_hardware_id ON public.license_registry USING btree (hardware_id);


--
-- Name: idx_lr_license_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_lr_license_id ON public.license_registry USING btree (license_id);


--
-- Name: idx_lr_product_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_lr_product_id ON public.license_registry USING btree (product_id);


--
-- Name: idx_lr_registered_to; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_lr_registered_to ON public.license_registry USING btree (registered_to);


--
-- Name: idx_lr_upload_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_lr_upload_date ON public.license_registry USING btree (upload_date);


--
-- Name: idx_luh_upload_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_luh_upload_date ON public.license_upload_history USING btree (upload_date);


--
-- Name: idx_org_unit_parent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_org_unit_parent ON public.tb_org_unit USING btree (parent_id);


--
-- Name: idx_org_unit_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_org_unit_type ON public.tb_org_unit USING btree (unit_type);


--
-- Name: idx_org_unit_use; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_org_unit_use ON public.tb_org_unit USING btree (use_yn);


--
-- Name: idx_performance_period; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_performance_period ON public.tb_performance_summary USING btree (period_year, period_month);


--
-- Name: idx_performance_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_performance_user ON public.tb_performance_summary USING btree (user_id);


--
-- Name: idx_plan_assignee; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_plan_assignee ON public.tb_work_plan USING btree (assignee_id);


--
-- Name: idx_plan_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_plan_date ON public.tb_work_plan USING btree (start_date, end_date);


--
-- Name: idx_plan_infra; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_plan_infra ON public.tb_work_plan USING btree (infra_id);


--
-- Name: idx_plan_parent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_plan_parent ON public.tb_work_plan USING btree (parent_plan_id);


--
-- Name: idx_plan_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_plan_status ON public.tb_work_plan USING btree (status);


--
-- Name: idx_plan_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_plan_type ON public.tb_work_plan USING btree (plan_type);


--
-- Name: idx_qr_license_end_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_qr_license_end_user ON public.qr_license USING btree (end_user_name);


--
-- Name: idx_qr_license_issued_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_qr_license_issued_at ON public.qr_license USING btree (issued_at DESC);


--
-- Name: idx_qr_license_products; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_qr_license_products ON public.qr_license USING btree (products);


--
-- Name: idx_qt_product_pattern_category; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_qt_product_pattern_category ON public.qt_product_pattern USING btree (category);


--
-- Name: idx_qt_quotation_category_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_qt_quotation_category_date ON public.qt_quotation USING btree (category, quote_date);


--
-- Name: idx_qt_quotation_item_quote_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_qt_quotation_item_quote_id ON public.qt_quotation_item USING btree (quote_id);


--
-- Name: idx_qt_quotation_ledger_quote_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_qt_quotation_ledger_quote_id ON public.qt_quotation_ledger USING btree (quote_id);


--
-- Name: idx_qt_quotation_ledger_year_category; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_qt_quotation_ledger_year_category ON public.qt_quotation_ledger USING btree (year, category);


--
-- Name: idx_qt_quotation_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_qt_quotation_status ON public.qt_quotation USING btree (status);


--
-- Name: idx_qt_wage_rate_year; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_qt_wage_rate_year ON public.qt_wage_rate USING btree (year);


--
-- Name: idx_signature_doc; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_signature_doc ON public.tb_document_signature USING btree (doc_id);


--
-- Name: idx_sw_pjt_person_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sw_pjt_person_id ON public.sw_pjt USING btree (person_id);


--
-- Name: idx_tb_document_attachment_doc; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tb_document_attachment_doc ON public.tb_document_attachment USING btree (doc_id);


--
-- Name: idx_tb_document_detail_doc; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tb_document_detail_doc ON public.tb_document_detail USING btree (doc_id);


--
-- Name: idx_tb_document_environment; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tb_document_environment ON public.tb_document USING btree (environment);


--
-- Name: idx_tb_document_history_doc; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tb_document_history_doc ON public.tb_document_history USING btree (doc_id);


--
-- Name: idx_tb_document_infra; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tb_document_infra ON public.tb_document USING btree (infra_id);


--
-- Name: idx_tb_document_org_unit; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tb_document_org_unit ON public.tb_document USING btree (org_unit_id);


--
-- Name: idx_tb_document_proj; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tb_document_proj ON public.tb_document USING btree (proj_id);


--
-- Name: idx_tb_document_region; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tb_document_region ON public.tb_document USING btree (region_code);


--
-- Name: idx_tb_document_signature_doc; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tb_document_signature_doc ON public.tb_document_signature USING btree (doc_id);


--
-- Name: idx_tb_document_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tb_document_status ON public.tb_document USING btree (status);


--
-- Name: idx_tb_document_support_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tb_document_support_type ON public.tb_document USING btree (support_target_type);


--
-- Name: idx_tb_document_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tb_document_type ON public.tb_document USING btree (doc_type);


--
-- Name: idx_tb_inspect_checklist_doc; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tb_inspect_checklist_doc ON public.tb_inspect_checklist USING btree (doc_id);


--
-- Name: idx_tb_inspect_issue_doc; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tb_inspect_issue_doc ON public.tb_inspect_issue USING btree (doc_id);


--
-- Name: idx_tb_pjt_manpower_proj; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tb_pjt_manpower_proj ON public.tb_pjt_manpower_plan USING btree (proj_id);


--
-- Name: idx_tb_pjt_schedule_proj; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tb_pjt_schedule_proj ON public.tb_pjt_schedule USING btree (proj_id);


--
-- Name: idx_tb_pjt_target_proj; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tb_pjt_target_proj ON public.tb_pjt_target USING btree (proj_id);


--
-- Name: idx_tb_work_plan_assignee; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tb_work_plan_assignee ON public.tb_work_plan USING btree (assignee_id);


--
-- Name: idx_tb_work_plan_infra; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tb_work_plan_infra ON public.tb_work_plan USING btree (infra_id);


--
-- Name: idx_tb_work_plan_parent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tb_work_plan_parent ON public.tb_work_plan USING btree (parent_plan_id);


--
-- Name: idx_tb_work_plan_start; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tb_work_plan_start ON public.tb_work_plan USING btree (start_date);


--
-- Name: idx_tb_work_plan_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tb_work_plan_status ON public.tb_work_plan USING btree (status);


--
-- Name: idx_visit_log_report; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_visit_log_report ON public.inspect_visit_log USING btree (report_id);


--
-- Name: uq_service_purpose_sys_type_md5; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_service_purpose_sys_type_md5 ON public.tb_service_purpose USING btree (sys_nm_en, purpose_type, md5(purpose_text));


--
-- Name: tb_document trg_document_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_document_updated BEFORE UPDATE ON public.tb_document FOR EACH ROW EXECUTE FUNCTION public.fn_update_timestamp();


--
-- Name: tb_performance_summary trg_performance_summary_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_performance_summary_updated BEFORE UPDATE ON public.tb_performance_summary FOR EACH ROW EXECUTE FUNCTION public.fn_update_timestamp();


--
-- Name: tb_work_plan trg_work_plan_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_work_plan_updated BEFORE UPDATE ON public.tb_work_plan FOR EACH ROW EXECUTE FUNCTION public.fn_update_timestamp();


--
-- Name: check_category_mst check_category_mst_section_code_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.check_category_mst
    ADD CONSTRAINT check_category_mst_section_code_fkey FOREIGN KEY (section_code) REFERENCES public.check_section_mst(section_code);


--
-- Name: tb_infra_link_api fk_api_master; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_infra_link_api
    ADD CONSTRAINT fk_api_master FOREIGN KEY (infra_id) REFERENCES public.tb_infra_master(infra_id) ON DELETE CASCADE;


--
-- Name: sw_pjt fk_biz_cat; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sw_pjt
    ADD CONSTRAINT fk_biz_cat FOREIGN KEY (biz_cat_en) REFERENCES public.prj_types(cd);


--
-- Name: sw_pjt fk_dist_cd; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sw_pjt
    ADD CONSTRAINT fk_dist_cd FOREIGN KEY (dist_cd) REFERENCES public.sigungu_code(adm_sect_c);


--
-- Name: inspect_check_result fk_icr_category; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inspect_check_result
    ADD CONSTRAINT fk_icr_category FOREIGN KEY (section, category) REFERENCES public.check_category_mst(section_code, category_code);


--
-- Name: inspect_check_result fk_icr_section; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inspect_check_result
    ADD CONSTRAINT fk_icr_section FOREIGN KEY (section) REFERENCES public.check_section_mst(section_code);


--
-- Name: tb_infra_memo fk_infra_memo; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_infra_memo
    ADD CONSTRAINT fk_infra_memo FOREIGN KEY (infra_id) REFERENCES public.tb_infra_master(infra_id) ON DELETE CASCADE;


--
-- Name: inspect_report fk_ir_conf_ps_info; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inspect_report
    ADD CONSTRAINT fk_ir_conf_ps_info FOREIGN KEY (conf_ps_info_id) REFERENCES public.ps_info(id);


--
-- Name: inspect_report fk_ir_insp_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inspect_report
    ADD CONSTRAINT fk_ir_insp_user FOREIGN KEY (insp_user_id) REFERENCES public.users(user_id);


--
-- Name: inspect_template fk_it_category; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inspect_template
    ADD CONSTRAINT fk_it_category FOREIGN KEY (section, category) REFERENCES public.check_category_mst(section_code, category_code);


--
-- Name: inspect_template fk_it_section; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inspect_template
    ADD CONSTRAINT fk_it_section FOREIGN KEY (section) REFERENCES public.check_section_mst(section_code);


--
-- Name: sw_pjt fk_maint_type; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sw_pjt
    ADD CONSTRAINT fk_maint_type FOREIGN KEY (maint_type) REFERENCES public.maint_tp_mst(cd);


--
-- Name: ps_info fk_ps_sys_cd; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ps_info
    ADD CONSTRAINT fk_ps_sys_cd FOREIGN KEY (sys_nm_en) REFERENCES public.sys_mst(cd);


--
-- Name: qt_quotation fk_qt_category; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qt_quotation
    ADD CONSTRAINT fk_qt_category FOREIGN KEY (category) REFERENCES public.qt_category_mst(category_code);


--
-- Name: qt_remarks_pattern fk_qt_remarks_pattern_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qt_remarks_pattern
    ADD CONSTRAINT fk_qt_remarks_pattern_user FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON DELETE SET NULL;


--
-- Name: tb_infra_server fk_server_master; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_infra_server
    ADD CONSTRAINT fk_server_master FOREIGN KEY (infra_id) REFERENCES public.tb_infra_master(infra_id) ON DELETE CASCADE;


--
-- Name: sw_pjt fk_stat; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sw_pjt
    ADD CONSTRAINT fk_stat FOREIGN KEY (stat) REFERENCES public.cont_stat_mst(cd);


--
-- Name: tb_infra_software fk_sw_server; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_infra_software
    ADD CONSTRAINT fk_sw_server FOREIGN KEY (server_id) REFERENCES public.tb_infra_server(server_id) ON DELETE CASCADE;


--
-- Name: sw_pjt fk_sys_cd; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sw_pjt
    ADD CONSTRAINT fk_sys_cd FOREIGN KEY (sys_nm_en) REFERENCES public.sys_mst(cd);


--
-- Name: tb_work_plan fk_twp_status; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_work_plan
    ADD CONSTRAINT fk_twp_status FOREIGN KEY (status) REFERENCES public.work_plan_status_mst(status_code);


--
-- Name: tb_work_plan fk_twp_type; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_work_plan
    ADD CONSTRAINT fk_twp_type FOREIGN KEY (plan_type) REFERENCES public.work_plan_type_mst(type_code);


--
-- Name: tb_infra_link_upis fk_upis_master; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_infra_link_upis
    ADD CONSTRAINT fk_upis_master FOREIGN KEY (infra_id) REFERENCES public.tb_infra_master(infra_id) ON DELETE CASCADE;


--
-- Name: inspect_check_result inspect_check_result_report_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inspect_check_result
    ADD CONSTRAINT inspect_check_result_report_id_fkey FOREIGN KEY (report_id) REFERENCES public.inspect_report(id) ON DELETE CASCADE;


--
-- Name: inspect_report inspect_report_pjt_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inspect_report
    ADD CONSTRAINT inspect_report_pjt_id_fkey FOREIGN KEY (pjt_id) REFERENCES public.sw_pjt(proj_id) ON DELETE CASCADE;


--
-- Name: inspect_visit_log inspect_visit_log_report_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inspect_visit_log
    ADD CONSTRAINT inspect_visit_log_report_id_fkey FOREIGN KEY (report_id) REFERENCES public.inspect_report(id) ON DELETE CASCADE;


--
-- Name: qt_quotation_item qt_quotation_item_quote_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.qt_quotation_item
    ADD CONSTRAINT qt_quotation_item_quote_id_fkey FOREIGN KEY (quote_id) REFERENCES public.qt_quotation(quote_id) ON DELETE CASCADE;


--
-- Name: tb_contract_participant tb_contract_participant_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_contract_participant
    ADD CONSTRAINT tb_contract_participant_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- Name: tb_document tb_document_approver_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_document
    ADD CONSTRAINT tb_document_approver_id_fkey FOREIGN KEY (approver_id) REFERENCES public.users(user_id);


--
-- Name: tb_document_attachment tb_document_attachment_doc_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_document_attachment
    ADD CONSTRAINT tb_document_attachment_doc_id_fkey FOREIGN KEY (doc_id) REFERENCES public.tb_document(doc_id) ON DELETE CASCADE;


--
-- Name: tb_document tb_document_author_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_document
    ADD CONSTRAINT tb_document_author_id_fkey FOREIGN KEY (author_id) REFERENCES public.users(user_id);


--
-- Name: tb_document_detail tb_document_detail_doc_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_document_detail
    ADD CONSTRAINT tb_document_detail_doc_id_fkey FOREIGN KEY (doc_id) REFERENCES public.tb_document(doc_id) ON DELETE CASCADE;


--
-- Name: tb_document_history tb_document_history_actor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_document_history
    ADD CONSTRAINT tb_document_history_actor_id_fkey FOREIGN KEY (actor_id) REFERENCES public.users(user_id);


--
-- Name: tb_document_history tb_document_history_doc_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_document_history
    ADD CONSTRAINT tb_document_history_doc_id_fkey FOREIGN KEY (doc_id) REFERENCES public.tb_document(doc_id) ON DELETE CASCADE;


--
-- Name: tb_document tb_document_infra_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_document
    ADD CONSTRAINT tb_document_infra_id_fkey FOREIGN KEY (infra_id) REFERENCES public.tb_infra_master(infra_id);


--
-- Name: tb_document tb_document_plan_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_document
    ADD CONSTRAINT tb_document_plan_id_fkey FOREIGN KEY (plan_id) REFERENCES public.tb_work_plan(plan_id);


--
-- Name: tb_document tb_document_proj_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_document
    ADD CONSTRAINT tb_document_proj_id_fkey FOREIGN KEY (proj_id) REFERENCES public.sw_pjt(proj_id);


--
-- Name: tb_document_signature tb_document_signature_doc_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_document_signature
    ADD CONSTRAINT tb_document_signature_doc_id_fkey FOREIGN KEY (doc_id) REFERENCES public.tb_document(doc_id) ON DELETE CASCADE;


--
-- Name: tb_inspect_checklist tb_inspect_checklist_doc_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_inspect_checklist
    ADD CONSTRAINT tb_inspect_checklist_doc_id_fkey FOREIGN KEY (doc_id) REFERENCES public.tb_document(doc_id) ON DELETE CASCADE;


--
-- Name: tb_inspect_issue tb_inspect_issue_doc_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_inspect_issue
    ADD CONSTRAINT tb_inspect_issue_doc_id_fkey FOREIGN KEY (doc_id) REFERENCES public.tb_document(doc_id) ON DELETE CASCADE;


--
-- Name: tb_org_unit tb_org_unit_parent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_org_unit
    ADD CONSTRAINT tb_org_unit_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES public.tb_org_unit(unit_id) ON DELETE RESTRICT;


--
-- Name: tb_performance_summary tb_performance_summary_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_performance_summary
    ADD CONSTRAINT tb_performance_summary_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- Name: tb_pjt_manpower_plan tb_pjt_manpower_plan_proj_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_pjt_manpower_plan
    ADD CONSTRAINT tb_pjt_manpower_plan_proj_id_fkey FOREIGN KEY (proj_id) REFERENCES public.sw_pjt(proj_id) ON DELETE CASCADE;


--
-- Name: tb_pjt_schedule tb_pjt_schedule_proj_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_pjt_schedule
    ADD CONSTRAINT tb_pjt_schedule_proj_id_fkey FOREIGN KEY (proj_id) REFERENCES public.sw_pjt(proj_id) ON DELETE CASCADE;


--
-- Name: tb_pjt_target tb_pjt_target_proj_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_pjt_target
    ADD CONSTRAINT tb_pjt_target_proj_id_fkey FOREIGN KEY (proj_id) REFERENCES public.sw_pjt(proj_id) ON DELETE CASCADE;


--
-- Name: tb_work_plan tb_work_plan_assignee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_work_plan
    ADD CONSTRAINT tb_work_plan_assignee_id_fkey FOREIGN KEY (assignee_id) REFERENCES public.users(user_id);


--
-- Name: tb_work_plan tb_work_plan_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_work_plan
    ADD CONSTRAINT tb_work_plan_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(user_id);


--
-- Name: tb_work_plan tb_work_plan_infra_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_work_plan
    ADD CONSTRAINT tb_work_plan_infra_id_fkey FOREIGN KEY (infra_id) REFERENCES public.tb_infra_master(infra_id);


--
-- Name: tb_work_plan tb_work_plan_parent_plan_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tb_work_plan
    ADD CONSTRAINT tb_work_plan_parent_plan_id_fkey FOREIGN KEY (parent_plan_id) REFERENCES public.tb_work_plan(plan_id);


--
-- PostgreSQL database dump complete
--

\unrestrict D6RcMa6ZL7nqIrNHsTP1mn5aqASVJViVdUzOAMvnNsjaZRXkrtKVgmaDhn55ee2


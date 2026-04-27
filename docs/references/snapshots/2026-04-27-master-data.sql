--
-- PostgreSQL database dump
--

\restrict O1kqYtBXi2Hmef5gMl9nko3zC9Q0N8he6hKy1K0YWgx9bINaIzDN6qvxXqNNR2r

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
-- Data for Name: cont_frm_mst; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.cont_frm_mst (cd, nm) VALUES ('1', '도입');
INSERT INTO public.cont_frm_mst (cd, nm) VALUES ('2', '교체');
INSERT INTO public.cont_frm_mst (cd, nm) VALUES ('3', '무상');
INSERT INTO public.cont_frm_mst (cd, nm) VALUES ('4', '유상');
INSERT INTO public.cont_frm_mst (cd, nm) VALUES ('5', '종료');


--
-- Data for Name: cont_stat_mst; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.cont_stat_mst (cd, nm) VALUES ('1', '진행중');
INSERT INTO public.cont_stat_mst (cd, nm) VALUES ('2', '완료');


--
-- Data for Name: maint_tp_mst; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.maint_tp_mst (cd, nm) VALUES ('DU', '데이터 업로드');
INSERT INTO public.maint_tp_mst (cd, nm) VALUES ('SW', 'SW');
INSERT INTO public.maint_tp_mst (cd, nm) VALUES ('HS', 'HW/SW');
INSERT INTO public.maint_tp_mst (cd, nm) VALUES ('DS', 'DB/SW');
INSERT INTO public.maint_tp_mst (cd, nm) VALUES ('DHS', 'DB/HW/SW');
INSERT INTO public.maint_tp_mst (cd, nm) VALUES ('BASIC', '토지적성평가 베이직');
INSERT INTO public.maint_tp_mst (cd, nm) VALUES ('Pro', '토지적성평가 프로');


--
-- Data for Name: prj_types; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.prj_types (cd, nm) VALUES ('GISSW', '원천소프트웨어');
INSERT INTO public.prj_types (cd, nm) VALUES ('PKSW', '패키지소프트웨어');
INSERT INTO public.prj_types (cd, nm) VALUES ('TS', '기술지원');


--
-- Data for Name: sys_mst; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.sys_mst (cd, nm) VALUES ('112', '112시스템');
INSERT INTO public.sys_mst (cd, nm) VALUES ('ANCISS', '공항소음대책정보화 및 지원시스템');
INSERT INTO public.sys_mst (cd, nm) VALUES ('APIMS', '농업생산기반시설관리플랫폼');
INSERT INTO public.sys_mst (cd, nm) VALUES ('ARPMS', '공항활주로 포장관리시스템');
INSERT INTO public.sys_mst (cd, nm) VALUES ('BIS', '버스정보시스템');
INSERT INTO public.sys_mst (cd, nm) VALUES ('BMIS', '전장관리정보체계');
INSERT INTO public.sys_mst (cd, nm) VALUES ('CDMS', '상권관리시스템');
INSERT INTO public.sys_mst (cd, nm) VALUES ('CGM', 'CLX GPS MAP');
INSERT INTO public.sys_mst (cd, nm) VALUES ('CPS', '지적포털 시스템');
INSERT INTO public.sys_mst (cd, nm) VALUES ('CREIS', '종합부동산정보시스템');
INSERT INTO public.sys_mst (cd, nm) VALUES ('DEIMS', '배전설비정보관리시스템');
INSERT INTO public.sys_mst (cd, nm) VALUES ('DFGIS', '배전설비 지리정보시스템');
INSERT INTO public.sys_mst (cd, nm) VALUES ('GCRM', 'GCRM');
INSERT INTO public.sys_mst (cd, nm) VALUES ('GMPSS', '성장관리시스템');
INSERT INTO public.sys_mst (cd, nm) VALUES ('HIS', '택지정보시스템');
INSERT INTO public.sys_mst (cd, nm) VALUES ('JUSO', '국가주소정보시스템');
INSERT INTO public.sys_mst (cd, nm) VALUES ('KRAS', '부동산종합공부시스템');
INSERT INTO public.sys_mst (cd, nm) VALUES ('LSA', '토지적성평가 업무 프로그램');
INSERT INTO public.sys_mst (cd, nm) VALUES ('LSMS', '토지현황관리시스템');
INSERT INTO public.sys_mst (cd, nm) VALUES ('LTCS', '위치추적관제시스템');
INSERT INTO public.sys_mst (cd, nm) VALUES ('MPMIS', '국유재산관리조사시스템');
INSERT INTO public.sys_mst (cd, nm) VALUES ('MPMS', '국유재산관리시스템');
INSERT INTO public.sys_mst (cd, nm) VALUES ('NDTIS', '국방수송정보체계');
INSERT INTO public.sys_mst (cd, nm) VALUES ('NPFMS', '국립공원 시설관리시스템');
INSERT INTO public.sys_mst (cd, nm) VALUES ('NSDI', '국가공간정보체계');
INSERT INTO public.sys_mst (cd, nm) VALUES ('PGMS', '스마트공원녹지플랫폼');
INSERT INTO public.sys_mst (cd, nm) VALUES ('RAISE', '농어촌개발관리정보시스템');
INSERT INTO public.sys_mst (cd, nm) VALUES ('SC', '스마트도시 통합플랫폼');
INSERT INTO public.sys_mst (cd, nm) VALUES ('SMMS', 'aT센터 학교급식 모바일 시스템');
INSERT INTO public.sys_mst (cd, nm) VALUES ('SUFM', '스마트도시 시설물관제');
INSERT INTO public.sys_mst (cd, nm) VALUES ('UPIS', '도시계획정보체계');
INSERT INTO public.sys_mst (cd, nm) VALUES ('URTIS', '도시재생종합관리시스템');
INSERT INTO public.sys_mst (cd, nm) VALUES ('VLICMS', '베트남토지정보 종합관리시스템');
INSERT INTO public.sys_mst (cd, nm) VALUES ('IPSS', '통합인허가관리시스템');
INSERT INTO public.sys_mst (cd, nm) VALUES ('UPBSS', '기초조사정보시스템 v1.0');
INSERT INTO public.sys_mst (cd, nm) VALUES ('BSIS', '기초조사정보시스템 v1.5');
INSERT INTO public.sys_mst (cd, nm) VALUES ('BSISP', '기초조사정보시스템 v2.0');


--
-- PostgreSQL database dump complete
--

\unrestrict O1kqYtBXi2Hmef5gMl9nko3zC9Q0N8he6hKy1K0YWgx9bINaIzDN6qvxXqNNR2r


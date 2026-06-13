-- [ops-fault-support M3] tb_ops_kb 시드 (자동생성 prep_kb_seed.py). 개인정보 제외.
-- 멱등: ON CONFLICT (kb_code) DO NOTHING.
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-15-05', '장애', 'UPIS', '표준시스템', '표준시스템 구동 오류', '표준시스템 tomcat 오류', 'UPIS 8.0 Tomcat 메모리 부족(permsize 512m 초과)으로 지도 미표시 장애가 발생하여 Tomcat 재설치 및 메모리 설정 변경으로 해결함.', 'UPIS 표준시스템 8.0에서 지도가 화면에 표시되지 않으며, Tomcat 로그에 ClientAbortException: java.io.IOException / OutputBuffer.realWriteBytes 오류가 기록된다.', '초기 구축 시 사업단 지침에 따라 Tomcat JVM 메모리(PermSize)가 512m로 설정되어 있어, 운영 중 메모리 부족이 발생하고 Tomcat이 정상 응답을 반환하지 못함.', '1. UPIS 8.0 표준시스템 접속 및 지도 미표시 현상 재현 확인
2. Tomcat 로그에서 ClientAbortException(OutputBuffer.realWriteBytes) 오류 확인
3. 기존 UPIS 8.0 Tomcat 서비스 중지 및 삭제
4. 사업단에 메모리 변경 허용 여부 문의 후 승인 취득
5. JVM 옵션에서 PermSize(min/max) 값을 적정 크기로 재설정하여 UPIS 8.0 Tomcat 재설치
6. UPIS 8.0 재시작 후 지도 정상 표시 여부 확인', 'UPIS 표준시스템 초기 구축 시 사업단 권고 메모리 기준값을 서버 사양에 맞게 검토하고, PermSize(min/max)를 운영 환경에 적합한 값으로 설정한다.', 'UPIS, 표준시스템, Tomcat, 톰캣, GWS, ClientAbortException, OutputBuffer.realWriteBytes, PermSize, 메모리부족, 지도미표시, 장애', 55, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-26-02', '장애', 'UPIS', '표준시스템', '표준시스템 지도 오류', 'GSS 오류', 'GSS 엔진 비정상 종료 또는 Oracle UPIS 계정 잠금으로 인해 UPIS 표준시스템 지도가 표출되지 않는 장애를 GSS 재기동 및 계정 잠금 해제로 복구함.', 'UPIS 표준시스템에서 용도지역 등 지도 레이어 버튼 클릭 시 GSS 오류 메시지가 표출되거나 지도 자체가 화면에 표출되지 않는 현상이 발생함.', 'GSS(GIS Spatial Server) 엔진이 서버 전원 작업, 콘솔 창 실수 종료 등의 사유로 비정상 종료되었거나, GSS가 Oracle DB에 접속 시 UPIS 계정(User)이 반복 잠금(lock) 상태가 되어 DB 연결에 실패한 것이 원인임. 일부 사례는 정확한 원인 불명확(기록상 미상).', '1. Putty 또는 telnet으로 DB 서버(또는 GIS 서버)에 SSH 접속
2. GIS 엔진 로그 및 시스템 로그를 확인하여 장애 원인(GSS 미기동 또는 DB 연결 오류) 분류
3. GSS 엔진 정지 명령 실행: ./GSS -shutdown 또는 sh GSS -shutdown
4. GSS 엔진 재기동 후 프로세스 확인: ps -ef | grep GSS
5. Oracle UPIS 계정 잠금 여부 확인 후 잠금 해제: ALTER USER upis ACCOUNT UNLOCK;
6. 표준시스템에서 용도지역 등 지도 레이어 정상 표출 여부 최종 확인
7. SUN OS 등 특수 OS 환경에서는 GSS를 백그라운드 모드로 실행하여 콘솔 종료 시에도 엔진이 유지되도록 설정', 'GSS 엔진은 nohup 또는 백그라운드 실행(& 옵션)으로 기동하여 콘솔 세션 종료 시 자동 종료되지 않도록 설정하고, Oracle UPIS 계정의 PASSWORD_LOCK_TIME 및 FAILED_LOGIN_ATTEMPTS 정책을 점검하여 반복 잠금을 예방함.', 'UPIS, 표준시스템, GSS, GIS엔진, 지도오류, 지도미표출, GSS재기동, Oracle, 계정잠금, ACCOUNT UNLOCK, 용도지역, GWS, Desktop, Putty, SSH', 18, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-15-01', '지원', 'UPIS', '표준시스템', '표준시스템 기타', '기타', 'UPIS 표준시스템의 위치 좌표 오차·배경지도 불일치·교육용 계정 일괄 생성 등 기타 운영 지원 요청을 처리하였다.', '표준시스템에서 지적도 위치가 실제와 틀어져 보이거나, 배경지도(vWorld·다음·네이버)와 철도 라인이 불일치하는 현상이 발생하였다. 또한 사용자 교육 등 일시적 운영 필요로 인해 테스트 계정 대량 생성 요청이 접수되었다.', '경북 일부 지자체(고령군·울릉군 등)는 K데이터 자체는 정상이나 좌표계 특성상 Vworld 보정좌표 미적용으로 위치가 틀어져 표시된다. 철도 라인 불일치는 표준시스템과 배경지도 간 기초 데이터 출처 차이로 추정된다. 테스트 계정 문제는 원인 불명확(기록상 기타).', '1. 위치 오차 문의 접수 시 K데이터 정합성과 좌표계 보정 여부를 먼저 확인한다.
2. 경북 고령·울릉 등 좌표계 특이 지자체는 Vworld 보정좌표 미적용 문제임을 사용자에게 설명하고, 시스템 수정은 사업단 문의가 필요함을 안내한다.
3. 배경지도 철도 라인 불일치는 vWorld·다음지도·네이버 지도와 표준시스템 라인을 교차 비교하여 기초 데이터 출처 차이를 확인한다.
4. 비교 결과를 업무요청자 및 DB업체 담당자에게 보고한다.
5. 사용자 교육용 테스트 계정이 필요한 경우 TN_USER_NEW 테이블에 test1~30 계정을 생성한다.
6. TN_AUTHOR_USER_NEW 테이블에서 도시계획 조회·업무 관련 권한(UG00002, UG00003)을 각 계정에 부여한다.
7. 생성된 테스트 계정의 정상 작동 여부를 확인한다.', '좌표계 특이 지자체 목록을 사전에 문서화하고, Vworld 보정좌표 미적용 지역에 대한 안내문을 배포하여 반복 문의를 예방한다.', 'UPIS, 표준시스템, 좌표계, 위치오차, Vworld, 보정좌표, 배경지도, 철도라인, TN_USER_NEW, TN_AUTHOR_USER_NEW, 테스트계정, 권한, GSS, GPKI, 고령군, 울릉군, K데이터', 18, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-19-03', '지원', 'UPIS', '표준시스템', '표준시스템 민원행정 연계', '서버인증서 갱신', 'UPIS 표준시스템의 새올행정 민원 연계가 서버 인증서 만료로 중단되어 GPKI 인증서를 갱신·적용함으로써 정상화하였다.', '표준시스템과 새올행정(민원행정) 간 연계가 동작하지 않아 표준시스템 화면에서 새올 데이터가 정상적으로 표출되지 않는다.', 'GPKI 서버 인증서가 만료되어 새올행정 연계 인증에 실패하였다. 일부 기관에서는 인증서 갱신 절차 중 보안모듈 설치 오류 또는 Windows 폴더 쓰기 권한 부족이 추가로 발생하였다.', '1. 해당 기관 담당자로부터 갱신된 GPKI 서버 인증서 파일을 수령한다.
2. 서버의 GPKI 폴더 및 gpkisecureweb 폴더에서 기존 인증서 파일을 확인하고 백업한다.
3. 보안모듈 설치 오류가 발생하는 경우, 정상 설치 가능한 타 지자체 환경에서 인증서를 발급한 뒤 대상 서버에 복사·적용한다.
4. Windows 폴더 쓰기 권한 오류 발생 시, GPKI 인증서 대상 폴더의 Windows 접근 권한을 최소 수준으로 조정한 후 갱신을 재시도한다.
5. 새로 발급된 인증서 파일을 GPKI 폴더 및 gpkisecureweb 폴더에 덮어쓰기하여 갱신한다.
6. 표준시스템 재기동 후 새올행정 연계 화면이 정상적으로 표출되는지 확인한다.', 'GPKI 서버 인증서 만료 전(권고: 만료 30일 전) 기관 담당자와 갱신 일정을 사전 조율하고, 인증서 경로(GPKI 폴더·gpkisecureweb 폴더)와 Windows 쓰기 권한을 점검하여 갱신 작업이 즉시 진행될 수 있도록 준비한다.', 'UPIS, 표준시스템, 새올행정, 민원행정연계, GPKI, 서버인증서, 인증서갱신, gpkisecureweb, 보안모듈, 쓰기권한, PBK', 14, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-19-01', '장애', 'UPIS', '표준시스템', '표준시스템 민원행정 연계 오류', 'PBK 오류', 'UPIS 표준시스템의 민원행정 연계 시 PBK 인증서 값 오류 또는 포트 차단으로 연계가 실패하며, PBK 재생성·갱신 및 서버별 일괄 적용으로 정상화.', 'UPIS 표준시스템에서 민원행정(새올) 연계 시 GPKI 인증 오류(900, 1507 등) 또는 connect refused가 발생하여 민원 조회·처리가 불가능해진다. AP 서버가 이중화된 경우 한쪽(AP2)만 오류가 지속되는 현상도 나타난다.', 'PBK(공개키 블록) 값이 누락되거나 구버전 생성 프로그램으로 생성되어 GPKI 인증 검증 실패(CN·O·OU 값 불일치 포함)가 발생하는 것이 주요 원인이며, 일부 사례에서는 AP2 서버의 민원행정 포트(예: 100.5.1.120:3100)가 방화벽에 의해 차단되어 connect refused 오류가 동반된다.', '1. GPKI 오류 코드(900, 1507 등) 또는 connect refused 메시지를 확인하여 인증서 문제·포트 차단 여부를 1차 분류한다.
2. 인증서 문제인 경우 GPKI사업단에 LDAP 서버의 새올공통기반 인증서 탑재 여부 및 CN·O·OU 값을 확인한다.
3. PBK 생성 프로그램(최신 차수 권장, 오류 시 이전 차수로 재시도)을 이용해 해당 기관의 새올인증서에서 PBK 값을 도출한다.
4. 도출된 PBK(.cer) 파일을 GPKI 및 GPKISecureWEB 디렉터리에 복사·붙여넣기 한다.
5. AP1·AP2 등 모든 WAS 서버에 동일하게 PBK를 적용한다.
6. 포트 차단이 의심되는 경우(connect refused) 민원행정 연계 포트의 방화벽 허용을 인프라 담당자에게 요청한다.
7. UPIS 표준시스템(톰캣)을 재기동한 후 민원행정 연계 서비스 정상 동작을 AP 서버별로 각각 확인한다.', '이중화 AP 서버 환경에서는 PBK 갱신·포트 허용 작업 후 반드시 AP1·AP2 서버 양쪽에 동일한 설정이 적용되었는지 점검하고, 인증서 갱신 주기에 맞춰 사전 PBK 재생성 일정을 수립한다.', 'UPIS, 표준시스템, 민원행정 연계, PBK, GPKI, 새올, 공개키, 인증서, connect refused, 포트 차단, 톰캣, AP서버, LDAP, GPKISecureWEB, 900오류, 1507오류', 13, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-18-04', '지원', 'UPIS', '표준시스템', '표준시스템 문서 작성', '공공데이터 보유 현황', 'UPIS AP/DB 서버의 공공데이터 보유현황 스크립트를 실행하고 데이터베이스 정의서·컬럼 정의서 등 문서를 작성하여 담당 주무관에게 송부하였다.', '지자체 UPIS 운영기관으로부터 공공데이터 보유현황 조사 요청이 접수되어 AP/DB 서버 대상 스크립트 실행 및 문서 작성 지원이 필요한 상황이다.', '공공데이터 보유현황 조사 의무에 따라 각 기관의 UPIS 데이터베이스 현황을 정기적으로 수집·문서화해야 하나, 기관 자체적으로 스크립트 실행 및 정의서 작성이 어려워 기술지원이 요청된 것으로 추정된다.', '1. 대상 기관의 UPIS AP/DB 서버 접속 환경을 확인한다.
2. 공공데이터 보유현황 수집 스크립트를 서버에서 실행한다.
3. 스크립트 실행 결과를 바탕으로 데이터베이스 정의서 및 컬럼 정의서 등 공공데이터 보유현황 문서를 작성한다.
4. 작성 완료된 문서를 해당 기관 담당 주무관에게 이메일로 첨부하여 송부한다.', '공공데이터 보유현황 조사 주기(연간 등)에 맞춰 스크립트 및 문서 작성 절차를 표준화하고 기관별 일정을 사전 공지하여 지원 요청을 분산시킨다.', 'UPIS, 공공데이터 보유현황, 표준시스템, 데이터베이스 정의서, 컬럼 정의서, 스크립트 실행, AP서버, DB서버, 문서 작성, 기술지원', 12, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-26-03', '장애', 'UPIS', '표준시스템', '표준시스템 지도 오류', 'GWS 오류', '표준시스템 용도지역 지도 미표출 장애를 GWS 재기동 또는 레지스트리 JvmMx 메모리 증설로 해결하고, 동반 발생한 새올민원 연계 오류는 GPKI PBK 공개키 갱신으로 조치하였음.', '표준시스템 로그인 후 용도지역 레이어가 지도에 표출되지 않으며, 일부 기관에서는 개발행위허가 새올민원 연계 시 기간만료 오류가 함께 발생함.', 'GWS(지도 웹서버) 프로세스 이상 또는 레지스트리 JvmMx 힙 메모리 미설정으로 인한 웹서버 자원 부족이 주원인이며, 새올민원 연계 오류는 GPKI PBK 공개키 인증서 만료가 원인임.', '1. 웹서버(GWS) 서비스 재기동 후 용도지역 표출 여부 확인
2. 재기동 후에도 오류가 지속될 경우 regedit를 실행하여 GWS 관련 JvmMx 값을 6144(MB)로 설정
3. JvmMx 수정 후 GWS 서비스를 재기동하고 용도지역 정상 표출 확인
4. 새올민원 기간만료 오류가 동반될 경우 PBK.exe를 설치·실행
5. C:\GPKI\Certificate\class1 폴더 내 공개키 인증서(예: SVR5340000004PBK.cer)를 실행하여 만료일자 확인
6. PBK.exe에서 공개키 입력 후 생성된 신규 공개키 파일을 C:\gpkiapi에 복사
7. C:\GPKI\Certificate\class1 및 C:\gpkisecureweb\certs 폴더에 신규 공개키 파일 덮어쓰기(갱신)
8. 표준시스템에서 새올행정 연계 및 전체 정상 동작 최종 확인', 'GWS 설치 시 레지스트리 JvmMx 값을 6144 이상으로 사전 설정하고, GPKI PBK 공개키 인증서 만료일을 주기적으로 모니터링하여 만료 전 갱신 절차를 수행할 것.', 'UPIS, 표준시스템, GWS, 용도지역, 지도미표출, JvmMx, 레지스트리, GPKI, PBK, 공개키갱신, 새올민원, 기간만료, 웹서버재기동', 11, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-26-06', '장애', 'UPIS', '표준시스템', '표준시스템 지도 오류', '데이터 상이', 'UPIS 표준시스템에서 레이어명 오류 및 지자체코드 불일치로 지도 데이터가 비정상 표출되는 장애를 데이터 수정·재탑재로 해결하였다.', '표준시스템 지도 화면에서 특정 레이어(도시계획시설집행시설, 개발행위허가 등)가 오류 표출되거나, 도로 등 공간데이터가 아예 표출되지 않는 현상이 발생하였다. 복수 지자체(경상남도 하위, 당진시 등)에서 용도지역·연속지적도 유실 및 좌표계 불일치도 동반 확인되었다.', '원본 공간데이터의 지자체코드(SIGNGU_SE) 값이 실제 지자체코드와 상이하게 입력되어 자료교환 시 필터링 단계에서 데이터가 전달되지 않거나, 레이어명이 잘못 설정되어 스타일 구분이 불가한 상태였다. 또한 도시계획이력도·계획도 내 도시계획시설집행시설 레이어가 다수 지자체에서 동일한 형식 오류를 보유하고 있었다.', '1. 도시계획시설집행시설(P_UQ151_1~P_UQ159_1), 개발행위허가 미분류, 기타(H_UQ174_1, H_UQ174_2) 등 오류 레이어 현황 파악
2. 개발행위허가 레이어명이 잘못 구성(스타일 구분 필요 항목이 레이어 분리로 처리)되어 있음을 확인하고 수정 대상 목록 작성
3. 도시계획이력도·계획도 내 전 지자체 대상 도시계획시설집행시설 레이어명 일괄 수정 완료
4. 경상남도 하위 지자체별 용도지역 및 연속지적도 표출 여부 확인 후 유실 지자체 목록 문서화·보고
5. 용도지역(도시지역·관리지역 등) 분리 및 좌표계 불일치 발견 시 담당 부서(사업단)에 연락하여 해결방안 협의
6. UPIS_C_UQ151 레이어의 SIGNGU_SE 컬럼 값이 당진시 코드(44270) 대신 오입력(42270)된 것을 확인하여 데이터 수정 후 재탑재', '공간데이터 탑재 전 SIGNGU_SE 등 지자체코드 컬럼의 값이 행정안전부 코드 기준과 일치하는지 자동 검증하고, 레이어명 명명 규칙 준수 여부를 배포 전 체크리스트로 점검한다.', 'UPIS, 표준시스템, 지도 오류, 레이어명 오류, SIGNGU_SE, 지자체코드, 자료교환, 용도지역, 연속지적도, 좌표계, 도시계획시설집행시설, 개발행위허가, 데이터 상이', 11, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-15-04', '장애', 'UPIS', '표준시스템', '표준시스템 구동 오류', '표준시스템 Tomcat 오류', 'UPIS 표준시스템이 서버 재기동 후 Tomcat 7.0이 자동으로 구동되지 않아 시스템 접속 불가 상태가 발생하였으며, Tomcat 서비스를 수동 기동(또는 강제 종료 후 재기동)하여 정상화함.', 'UPIS 표준시스템에 접속하거나 서비스가 응답하지 않는 구동 오류가 발생하였다. 서버 재부팅 또는 업데이트 이후 표준시스템이 정상적으로 시작되지 않아 운영 중단 상태가 지속되었다.', 'Tomcat 7.0 서비스가 Windows 로컬 서비스에서 ''''수동 시작''''으로 설정되어 있어, 서버 재기동 시 자동으로 구동되지 않은 것이 주요 원인이다. 일부 사례에서는 Tomcat 종료 원인이 기록상 불명확하며, 프로세스가 비정상 상태로 잔류하여 정상 중지가 되지 않는 경우도 발생하였다.', '1. 표준시스템 미구동 에러 발생 여부 및 Tomcat 7.0 구동 상태 확인
2. Tomcat 7.0이 shutdown(중지) 상태인지 Windows 서비스 관리자 또는 프로세스 목록에서 확인
3. Tomcat이 정상 중지되지 않은 경우 작업 관리자(프로세스)에서 Tomcat 프로세스를 강제 종료
4. Tomcat 7.0 서비스를 수동으로 기동(startup.bat 실행 또는 서비스 시작)
5. 표준시스템 접속 구동 테스트 진행하여 정상 응답 확인
6. 재발 방지를 위해 Tomcat 서비스 시작 유형을 ''''수동''''에서 ''''자동''''으로 변경', 'Windows 서비스 관리자에서 Tomcat 7.0 서비스의 시작 유형을 ''''자동''''으로 설정하여 서버 재기동 시 자동으로 구동되도록 구성한다.', 'UPIS, 표준시스템, Tomcat, Tomcat7.0, 서비스 미구동, 자동시작, Windows 서비스, 프로세스 강제종료, 도시계획정보체계, 서버 재기동', 10, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-23-02', '장애', 'UPIS', '표준시스템', '표준시스템 재해취약성 오류', '데이터 오류', 'UPIS 표준시스템 재해취약성 데이터 업로드 시 도형·속성 데이터 미입력 오류를 데이터 타입 및 필드값 재검수·재생성으로 해결', 'UPIS 표준시스템에서 재해취약성 데이터 업로드 시 도형 데이터 또는 속성 데이터가 정상적으로 입력되지 않거나, 재입력 과정에서 무한루프 현상이 발생한다. 일부 기관에서는 TN_EVAL_DR UT 등 평가 테이블의 SMAR_CD / SAMR_NM 컬럼 필드값이 비어있는 현상도 동반된다.', '재해취약성 도형 데이터의 등급 필드가 시스템이 요구하는 string 타입이 아닌 double 타입으로 구성되어 있거나, SIGNGU_SE 컬럼에 시군구코드가 누락되어 있는 등 데이터 구조·필드값 오류가 원인이다. 속성 데이터의 경우 엑셀 파일 플랫폼 이상으로 해당 컬럼에 값이 삽입되지 않는 경우도 확인되었다.', '1. 재해취약성 업로드 오류 발생 시 도형·속성 데이터 구조 및 필드값을 우선 검수
2. 도형 데이터의 등급 관련 모든 필드가 string 타입인지 확인(double 타입이면 업로드 불가)
3. double 타입 컬럼을 삭제 또는 string 으로 변환한 뒤 업로드 재시도
4. SIGNGU_SE 컬럼에 시군구코드가 존재하는지 확인하고 누락 시 보완
5. 속성 데이터(엑셀) 파일의 TN_EVAL_DRUT·TN_EVAL_HEAT·TN_EVAL_RAIN·TN_EVAL_SNOW·TN_EVAL_SSEA·TN_EVAL_WIND 테이블 대상 SMAR_CD / SAMR_NM 컬럼 필드값 입력 여부 확인
6. 엑셀 파일 플랫폼 이상 의심 시 속성 데이터를 재생성하여 재업로드
7. 무한루프 등 원격 조치 불가 현상 발생 시 현장 방문 지원 요청 및 방문 전 데이터 정시성 검수 완료 확인', '재해취약성 데이터 생성 단계에서 도형 필드 타입을 전부 string으로 통일하고, SIGNGU_SE 등 필수 코드 컬럼 누락 여부를 업로드 전에 사전 검수하는 절차를 표준화한다.', 'UPIS, 표준시스템, 재해취약성, 데이터 업로드 오류, 도형 데이터, 속성 데이터, TN_EVAL_DRUT, TN_EVAL_HEAT, TN_EVAL_RAIN, TN_EVAL_SNOW, TN_EVAL_SSEA, TN_EVAL_WIND, SMAR_CD, SAMR_NM, SIGNGU_SE, 데이터 타입, string, double, 무한루프, 시군구코드', 10, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-16-01', '지원', 'UPIS', '표준시스템', '표준시스템 데이터', '데이터 백업', 'UPIS 데이터 다운로드 시 한글 속성 깨짐 현상 발생으로 서버·오라클 로케일 점검 후 정상 MDB 파일을 담당자에게 전달하여 해결.', 'UPIS 표준시스템에서 속성 및 도형 데이터를 다운로드할 때 한글 속성값이 깨지는 현상이 발생하며, 실제 서버에 탑재된 데이터 자체에는 이상이 없다.', '서버 또는 오라클의 로케일(character set) 설정이 한글 인코딩(ko_KR)과 불일치하거나, 다운로드 경로의 인코딩 변환 과정에서 오류가 발생하는 것으로 추정되며, 로케일 변경 후에도 증상이 지속된 경우 원인이 완전히 규명되지 않음.', '1. 서버 로케일 및 오라클 로케일 설정을 확인한다.
2. 로케일이 ko_KR로 설정되어 있지 않으면 ko_KR로 변경한다.
3. 로케일 변경 후에도 한글 깨짐이 지속되면 기존 엔진 교체 시점에 수령한 정상 속성 MDB 파일을 확보한다.
4. 도형 데이터 전체를 백업한다(업로드 도형 데이터 일체).
5. 속성 데이터를 SQL 및 Excel(연계 테이블·구축 데이터) 형태로 백업한다(참조 파일: upis 탑재용_속성데이터.xls).
6. 원격 접속이 불가한 기관은 현장 방문하여 백업을 수행한다.
7. 정상 확인된 속성 MDB 파일을 담당자(관리자)에게 전달하고 완료 처리한다.', 'UPIS 서버 설치·업그레이드 시 서버 OS 로케일과 오라클 NLS_CHARACTERSET 설정이 한글(ko_KR / AL32UTF8)로 일치하는지 사전에 확인하고, 초기 속성 MDB 파일을 별도 보관해 둔다.', 'UPIS, 표준시스템, 한글 깨짐, 로케일, 오라클, NLS_CHARACTERSET, ko_KR, 속성 데이터, 도형 데이터, MDB, 데이터 백업', 8, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-13-01', '지원', 'UPIS', '표준시스템', '표준시스템 LURIS 연계', 'LURIS 테스트 연계', 'UPIS 표준시스템과 LURIS 간 연계 동작을 확인하기 위해 7개 기관에서 LURIS 테스트 연계 지원을 수행하였다.', 'UPIS 표준시스템과 LURIS(토지이용규제정보서비스) 간 연계가 정상적으로 동작하는지 확인이 필요한 상태였다. 7개 기관(고창군·담양군·신안군·예천군·청도군·칠곡군·포항시 UPIS)에서 동일한 연계 테스트 지원 요청이 접수되었다.', '원인 불명확(기록상 미상) — 업무일지에 구체적인 장애 원인이 기재되지 않고 ''''LURIS 테스트 연계''''로만 기록되어 있어 정확한 원인 특정이 불가능하다.', '1. 대상 기관의 UPIS 표준시스템 환경에서 LURIS 연계 설정 항목(연계 URL, 인증 정보 등)을 확인한다.
2. LURIS 테스트 연계를 실행하여 연계 요청 및 응답 정상 여부를 점검한다.
3. 테스트 결과를 확인하고 기관 담당자에게 연계 동작 상태를 안내한다.', 'UPIS 표준시스템 신규 구축 또는 업데이트 후에는 LURIS 연계 테스트를 사전에 수행하여 연계 정상 여부를 운영 전 확인한다.', 'UPIS, LURIS, 토지이용규제정보서비스, 표준시스템, 연계 테스트, 도시계획정보체계', 7, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-19-01', '지원', 'UPIS', '표준시스템', '표준시스템 민원행정 연계', 'PBK 적용', 'GPKI 인증서 만료 또는 PBK 미적용으로 민원행정 연계가 중단된 경우, 인증서 갱신 및 PBK 공개키 재생성·배포를 통해 정상화함.', 'UPIS 표준시스템에서 민원행정(새올행정) 연계가 동작하지 않거나 ''''1301 인증서 패스워드 오류'''' 등이 발생하여 개발행위허가 등 민원 처리가 불가능한 상태가 됨.', 'GPKI 공개키 인증서(PBK)의 만료 또는 PBK 값 미적용으로 인해 민원행정 연계 인증 단계에서 오류가 발생한 것으로 추정됨.', '1. PBK.exe 설치(미설치 환경의 경우)
2. C:\GPKI\Certificate\class1 폴더 내 공개키 인증서(예: SVR5340000004PBK.cer) 실행 후 만료일자 확인
3. PBK.exe 실행 후 공개키 입력, 생성된 공개키 파일을 C:\gpkiapi 에 복사
4. C:\GPKI\Certificate\class1 및 C:\gpkisecureweb\certs 경로에 새 공개키 파일 덮어쓰기(갱신)
5. 인증서 패스워드 오류(1301) 발생 시 담당자에게 GPKI 패스워드 요청 후 DB의 TC_SAEALL_INFO.GPKIPASS 컬럼에 입력
6. 복수 지자체 환경의 경우 PBK 모듈로 시군구별 PBK 값 추출·적용 후 test4 계정 등으로 각 지자체코드별 연계 확인
7. 표준시스템 새올행정 연계 정상 작동 여부 최종 확인', 'GPKI 공개키 인증서의 만료일을 주기적으로 점검하고, 만료 전 PBK 재생성 및 배포 절차를 사전에 수행하여 연계 중단을 예방한다.', 'UPIS, 표준시스템, 민원행정, 새올행정, GPKI, PBK, 공개키인증서, 인증서만료, 1301오류, GPKIPASS, TC_SAEALL_INFO, GSS', 7, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-26-01', '장애', 'UPIS', '표준시스템', '표준시스템 지도 오류', 'GSS 메모리 오류', 'GSS 메모리 부족으로 표준시스템 지도 및 용도지역 조회가 실패하는 장애를 GSS 메모리 설정 확대 및 재기동으로 해결.', '표준시스템에서 지도 조회 또는 용도지역 표출 시 GSS 연결 실패 오류 메시지가 출력되며 지도가 정상적으로 표시되지 않는다. 필지정보 조회 창에도 동일한 GSS 오류 문구가 나타난다.', 'GSS(지오서버 서비스) 프로세스가 메모리 부족으로 비정상 종료되어 표준시스템의 지도 요청이 실패한다. GSS.lax 설정의 JVM 최솟값/최댓값이 실제 운영 부하에 비해 낮게 설정되어 있었던 것이 근본 원인이다.', '1. 표준시스템 접속 후 지도 조회 오류 및 GSS 연결 실패 오류 메시지 확인
2. GSS 로그(GSS.log 또는 GSS.log.날짜) 검토하여 메모리 오류(OutOfMemory) 발생 여부 확인
3. GSS.lax 파일에서 JVM 메모리 설정 변경: min 256 → 512(MB), max 512 → 1024(MB)
4. GSS 프로세스 재기동 (필요 시 백그라운드 모드로 실행)
5. GWS 재기동
6. UPIS 8.0 재기동
7. 표준시스템에서 지도 조회 정상 여부 확인 후 담당자에게 완료 통보', 'GSS.lax 의 JVM 메모리 설정을 min 512MB / max 1024MB 이상으로 유지하고, 정기적으로 GSS 로그를 점검하여 메모리 경고 징후를 사전에 감지한다.', 'UPIS, GSS, GWS, 표준시스템, 지도 오류, 메모리 부족, GSS.lax, JVM 메모리, 용도지역, 필지정보조회, GSS 연결 실패', 6, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-06-01', '지원', 'UPIS', '표준시스템', 'UPIS.xml 교체', '미상(구양식)', 'UPIS.xml 레이어 연결 오류로 도시계획이력도·계획도 레이어가 잘못된 이름으로 참조되어 UPIS.xml을 수정·재배포하여 해결하였다.', 'UPIS 표준시스템에서 도시계획이력도(도시계획집행시설, UPIS_P_RBEP) 및 도시계획계획도(도시계획시설집행시설) 레이어가 정상적으로 표시되지 않거나 잘못된 레이어로 연결되는 현상이 발생하였다.', 'UPIS.xml 내 해당 레이어 항목이 정상 레이어명 대신 언더바(_)가 추가된 잘못된 레이어명으로 연결되어 있어 레이어 참조 오류가 발생하였다.', '1. UPIS.xml 파일을 열어 도시계획이력도 > 도시계획집행시설(UPIS_P_RBEP) 및 도시계획계획도 > 도시계획시설집행시설 레이어 항목을 확인한다.
2. 잘못 연결된 레이어명(언더바(_) 오기재)을 정상 레이어명으로 수정한다.
3. 수정된 UPIS.xml을 저장하고 서버에 재배포한다.
4. 재배포 후 해당 레이어가 정상적으로 표시되는지 검증한다.', 'UPIS.xml 레이어 연결 명칭은 언더바 등 특수문자 오기재 여부를 포함하여 배포 전 레이어명 일치 여부를 검수한다.', 'UPIS, UPIS.xml, 표준시스템, 레이어 연결 오류, 도시계획이력도, 도시계획계획도, UPIS_P_RBEP, 도시계획집행시설, 도시계획시설집행시설, XML 수정, 재배포', 6, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-25-01', '장애', 'UPIS', '표준시스템', '표준시스템 조회 오류', '데이터 오류', 'UPIS 표준시스템 조회 화면에서 용도지역·고시정보 등이 미표출되는 데이터 오류를 테이블 값 정정 및 누락 테이블 생성으로 해결', 'UPIS 표준시스템에서 상세 필지 조회 시 용도지역이 조서에 표출되지 않거나, 고시정보 리스트가 무한루프 상태로 미표출되는 등 특정 조회 화면이 정상 동작하지 않는 현상이 발생한다. 시스템 에러 로그는 남지 않아 데이터 문제로 오인하기 쉽다.', '조회 쿼리에서 참조하는 코드값(예: PMC0002)과 DB에 실제 입력된 값(예: PMC0003)이 불일치하거나, TN_DRW_IMAGE·TN_DRW_IMAGE_MAPNG 테이블 간 RECORD_CODE 중복으로 X/Y 좌표가 복수 도출되는 데이터 오류가 원인이다. 또한 TN_DSTPLAN_ATCHFILE 등 필수 테이블이 누락된 경우에도 동일 증상이 발생한다.', '1. 시스템 에러 로그를 확인하여 애플리케이션 오류인지 데이터 오류인지 1차 구분
2. TN_SPFC_WTNNC 테이블의 현황코드 컬럼 값과 조회 쿼리의 요청 코드값(PMC0002/PMC0003 등)을 대조하여 불일치 여부 확인
3. 불일치 데이터 확인 후 해당 테이블의 코드값을 쿼리 기준값으로 정정
4. 고시정보 위치 미표출 시 TN_DRW_IMAGE, TN_DRW_IMAGE_MAPNG 테이블의 RECORD_CODE 중복 여부 검토 후 데이터 검수 요청 및 정리
5. 고시정보 리스트 무한루프 발생 시 TN_DSTPLAN_ATCHFILE 테이블 존재 여부 확인 후 누락된 경우 스크립트를 이용하여 테이블 생성
6. 조치 후 해당 조회 화면에서 정상 표출 여부 재확인', 'UPIS 표준시스템 구축·업그레이드 시 필수 테이블(TN_DSTPLAN_ATCHFILE 등) 목록 체크리스트로 누락 여부를 사전 검증하고, 코드 참조 테이블의 코드값이 쿼리 기준값과 일치하는지 데이터 정합성 검사를 정기적으로 수행한다.', 'UPIS, 표준시스템, 용도지역 미표출, 고시정보 미표출, 무한루프, TN_SPFC_WTNNC, PMC0002, PMC0003, TN_DRW_IMAGE, TN_DRW_IMAGE_MAPNG, RECORD_CODE 중복, TN_DSTPLAN_ATCHFILE, 테이블 누락, 데이터 오류, 도시계획정보체계', 6, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-25-05', '장애', 'UPIS', '표준시스템', '표준시스템 조회 오류', '표준시스템 패치 오류', 'UPIS 표준시스템에서 새주소 API 공개키 오류로 자연어 검색 및 조회 기능이 동작하지 않아 upis.properties 키값 교체 후 Tomcat 재기동으로 복구함.', '표준시스템 첫 화면에서 자연어 검색 시 검색 목록이 표출되지 않으며, 필지 상세 조회도 정상 동작하지 않음. 일부 기관에서는 집행도 범례가 미표출되는 현상도 병행 발생함.', 'upis.properties 파일의 개발행위허가 관련 새주소 API 공개키 값이 구버전으로 설정되어 있어 API 호출 실패가 발생. 패치 적용 과정에서 키값이 최신 버전으로 갱신되지 않은 것이 원인.', '1. D:\UPIS8\egov-upis-web-cm\WEB-INF\classes\egovframework\config\upis.properties 파일 열기
2. 개발행위허가 관련 새주소 API 공개키 항목을 최신 키값(예: U01TX0FVVEgyMDE2MDcwNDE1MjMxOTEzNTQw)으로 교체
3. 파일 저장 후 Tomcat 서비스 재기동
4. 표준시스템 접속하여 자연어 검색 목록 정상 표출 여부 확인
5. 집행도 범례 미표출 등 추가 이상 증상은 사업단(담당 주임)에게 처리 요청 및 결과 확인', 'UPIS 패치 배포 시 upis.properties의 새주소 API 공개키 버전을 배포 체크리스트에 포함하여 구버전 키가 잔존하지 않도록 사전 점검한다.', 'UPIS, 표준시스템, 자연어검색, 새주소 API, 공개키, upis.properties, Tomcat, 집행도 범례, 조회 오류, 패치 오류', 6, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-23-03', '장애', 'UPIS', '표준시스템', '표준시스템 재해취약성 오류', '표준시스템 패치 오류', 'UPIS 표준시스템 패치 미적용으로 재해취약성 데이터 업로드가 실패하고 시스템이 중지되었으며, UPIS 8.0 패치 후 톰캣 재기동으로 해결되었다.', '재해취약성(폭염·강풍 등) 데이터를 업로드하는 과정에서 시스템이 중지되거나 업로드가 반복 실패하며, 일부 기관에서는 업로드 후 표준시스템 접속 자체가 불가능해지는 현상이 발생하였다.', 'UPIS 표준시스템 패치가 정상 적용되지 않은 상태에서 재해취약성 데이터 업로드를 시도하여 업로드 처리 오류 및 시스템 비정상 종료가 발생하였다.', '1. 재해취약성 원본 데이터(폭염·강풍 등) 이상 여부를 확인한다.
2. GWS 및 UPIS 톰캣(Tomcat)을 재기동한 후 업로드를 재시도한다.
3. GSS를 재기동한 후 업로드를 재시도한다.
4. 반복 실패 시 UPIS 8.0 최신 패치를 적용한다.
5. 패치 적용 후 톰캣(Tomcat)을 재기동하고 재해취약성 데이터를 다시 업로드하여 정상 처리를 확인한다.
6. 아산시 등 기관별 재해취약성 상세 필지 쿼리가 표준 호출 방식과 다를 경우 사업단에 문의하여 쿼리 정합성을 확인한다.', '재해취약성 데이터 업로드 전 UPIS 표준시스템 패치 버전을 사전 점검하고, 신규 패치 배포 시 즉시 적용한다.', 'UPIS, 표준시스템, 재해취약성, 업로드 오류, GSS, GWS, Tomcat, 톰캣 재기동, 패치 오류, 시스템 중지, 도시계획정보체계', 5, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-15-06', '장애', 'UPIS', '표준시스템', '표준시스템 구동 오류', '표준시스템 패치 오류', '표준시스템(UPIS) 기동 불가 및 패치 오류를 디렉토리 생성·업데이터 배치 재실행·upis.properties 포트 수정으로 해결', '표준시스템이 기동되지 않거나 실행 중 오류 화면이 표출된다. 구동에 성공하더라도 용도지역 필지 검색·클릭이 동작하지 않는 등 부분 기능 장애가 나타나기도 한다.', '패치 적용 과정의 결함으로 인해 필수 디렉토리 누락(D:\projects\z), class 파일 누락, 뷰·메타뷰 데이터 미생성, upis.properties 포트 설정 불완전(WFS 서비스 포트 누락) 등 여러 구성 요소가 불완전한 상태로 남아 장애가 발생한다.', '1. 표준시스템 기동 실패 시 서버 로그를 확인하여 오류 유형(디렉토리 없음 / class 파일 없음 / 포트 누락)을 분류한다
2. 로그에 디렉토리를 찾을 수 없다는 오류가 있으면 누락된 디렉토리(예: D:\projects\z)를 수동 생성한 뒤 재기동한다
3. 로그에 class 파일을 찾을 수 없다는 오류가 있으면 D:\UPIS_DX\UpisDxClient\Bin\UpdaterUpis.bat 파일의 날짜를 패치 일자 순서(예: 18.05.24 → 18.05.29 → 18.06.12)로 순차 변경하며 실행하여 패치를 완료한다
4. 기동 후 용도지역 조회·필지 클릭이 동작하지 않으면 뷰·메타뷰 데이터 누락으로 판단하고 해당 스크립트를 실행하여 데이터를 생성한다
5. upis.properties(UPIS8) 파일에서 WMS 포트(8880) 외 WFS 서비스 및 해당 서버 포트가 누락되어 있는지 확인하고, 누락된 포트 정보를 추가 작성하여 저장한다
6. 설정 파일 수정 후 표준시스템을 재기동하고 용도지역 검색·필지 클릭 정상 동작 여부를 확인한다', '패치 배포 시 필수 디렉토리 목록과 upis.properties 포트 항목(WMS·WFS 모두)을 체크리스트로 관리하여 적용 누락 여부를 사전 검증한다.', 'UPIS, 표준시스템, 패치오류, 구동오류, UpdaterUpis.bat, upis.properties, WMS, WFS, 포트누락, 디렉토리누락, class파일누락, 뷰데이터, 메타뷰, 도시계획정보체계', 5, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-15-02', '장애', 'UPIS', '표준시스템', '표준시스템 구동 오류', 'oracle 접속 오류', 'Oracle DB 계정 잠금 또는 리스너 오류·Archive 로그 용량 부족으로 UPIS 표준시스템이 구동되지 않아 계정 해제·Oracle 재기동·로그 정리로 복구.', 'UPIS 표준시스템이 실행되지 않거나 로그인이 불가하며, GSS/GWS 연결 오류가 발생한다. 일부 기관에서는 재해취약성 데이터 업로드 오류 등 부가 기능 이상도 동반된다.', 'Oracle DB UPIS 계정 잠금(account lock), Oracle 리스너 비정상 종료(release 현상), 또는 Archive 로그 디스크 용량 부족 중 하나 이상이 복합적으로 발생하여 DB 접속이 차단됨.', '1. 표준시스템 및 GSS/GWS 서비스 기동 상태를 확인한다.
2. Oracle DB 서버에 접속하여 UPIS 계정 잠금 여부를 확인한다.
3. 계정이 잠긴 경우: SQL*Plus 또는 동등 클라이언트에서 `alter user upis account unlock;` 실행 후 잠금을 해제한다.
4. 수직자료연계 모듈의 UPIS DB 접속 패스워드가 변경된 경우 해당 모듈 설정 파일의 비밀번호를 수정한다.
5. Oracle 리스너 이상(release 현상)이 확인된 경우: 리스너를 종료(lsnrctl stop)하고 Oracle 인스턴스를 재기동한 뒤 리스너를 재시작(lsnrctl start)한다.
6. Archive 로그 디스크 용량 부족이 원인인 경우: `df -k` 또는 `df -m` 명령으로 Archive 로그 폴더 사용량을 확인하고, FTP로 접속하여 오래된 Archive 로그 파일 일부를 삭제한다.
7. GWS·GSS를 순서대로 재기동하고 표준시스템 정상 작동을 확인·보고한다.', 'Oracle Archive 로그 디스크 사용률을 주기적으로 모니터링하고, UPIS 계정 비밀번호 정책(만료·잠금) 변경 시 수직자료연계 모듈 설정과 동기화하는 절차를 수립한다.', 'UPIS, 표준시스템, Oracle, account unlock, GSS, GWS, 리스너, Archive 로그, 용량 부족, DB 접속 오류, 계정 잠금', 4, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-26-04', '장애', 'UPIS', '표준시스템', '표준시스템 지도 오류', 'UPIS.xml 패치', 'UPIS 표준시스템에서 지도 레이어 오류 및 이중 표출이 발생하여 UPIS.xml·GSS·GWS 최신 패치 적용 및 중복제거 설정으로 해결.', 'UPIS 표준시스템 지도 화면에서 연혁도(지구단위계획) 레이어 표출 시 UPIS_C_RBEP_* 레이어 없음 오류가 발생하거나, 공공체육시설(고등학교) 라벨이 동일 위치에 이중으로 표출되는 현상이 나타난다.', 'TOC에 최신 패치가 적용되지 않은 UPIS.xml이 남아 있고, GSS 라이브러리 및 GWS 엔진(재해취약성 포함)도 구버전 상태여서 신규 레이어 정의(UPIS_C_RBEP_*)가 누락된 것이 주원인이다. 라벨 이중 표출은 Drawing_no 값이 없는 데이터에 대해 중복제거 필터가 미설정된 것이 원인이다.', '1. 연혁도 표출 오류 발생 시 TOC 레이어 목록에서 적용된 UPIS.xml 버전을 확인하여 구버전 여부를 점검한다.
2. GSS 라이브러리를 최신 패치로 교체한 후 GSS 서비스를 재기동한다.
3. GWS를 최신 패치(uwes 포함)로 업데이트하고 upis.properties를 적용한 뒤 GWS 서비스를 재기동한다.
4. UPIS.xml 최신 패치를 적용하고 연혁도 레이어 정의를 추가한 후 GWS를 재기동한다.
5. 누락된 도형 레이어(UPIS_C_RBEP_*)가 구축되지 않은 경우 해당 레이어 도형을 직접 구축하여 적용한다.
6. 라벨 이중 표출 오류 발생 시 Desktop(UPIS_C_UQ155)으로 해당 레이어를 열어 Drawing_no 값 누락 여부를 확인한다.
7. 스타일 설정에서 Drawing_no가 없는 데이터를 처리하는 필터에 중복제거(Distinct) 옵션을 활성화하여 이중 표출을 제거한다.', 'UPIS 패치 배포 시 UPIS.xml·GSS 라이브러리·GWS 엔진을 함께 점검하는 패치 체크리스트를 운영하여 부분 패치로 인한 레이어 누락을 예방한다.', 'UPIS, 표준시스템, UPIS.xml, GSS, GWS, upis.properties, UPIS_C_RBEP, UPIS_C_UQ155, 레이어 오류, 이중표출, 중복제거, 연혁도, 지구단위계획, 라벨 중복, TOC, Desktop, uwes, 재해취약성', 4, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-05-05-01', '지원', 'LSA', '토지적성평가', '토지적성평가 기타', '기타', '토지적성평가 구역계 파일의 형식 오류(선형식) 및 좌표계 불일치로 인해 프로그램 적용이 안 되는 문제를 데이터 생성 업체의 수정을 통해 해결하였다.', '토지적성평가 프로그램에 구역계 파일을 적용하려 하였으나 정상 동작하지 않았다. 구역계 데이터가 면 형식이 아닌 선 형식으로 표현되어 있거나, 좌표계 설정이 맞지 않아 위치가 틀리게 표시되는 현상이 발생하였다.', '구역계 파일을 생성한 업체에서 데이터 형식을 면(Polygon)이 아닌 선(Line)으로 잘못 작성하였고, 좌표계 설정 또한 올바르지 않게 지정되어 있었던 것으로 파악된다.', '1. 토지적성평가(토적) 프로그램 또는 Desktop을 사용하여 구역계 파일을 불러와 문제 여부를 확인한다.
2. 구역계 데이터의 형식(선/면 여부)과 좌표계 설정이 올바른지 점검한다.
3. 이상 내역(선 형식 오류, 좌표계 불일치 등)을 정리하여 업무 요청자 및 데이터 생성 업체에 메일로 발송한다.
4. 데이터 생성 업체가 수정한 파일을 수령한 후 토적 프로그램에 재적용하여 정상 동작 여부를 재확인한다.
5. 최종 확인 완료 후 업무 요청자 및 관련 담당자에게 처리 결과를 보고한다.', '구역계 파일 납품 시 데이터 형식(면형식 필수)과 좌표계 설정값을 사전 검수 체크리스트에 포함하여 적용 전 오류를 차단한다.', '토지적성평가, 구역계 파일, 좌표계 오류, 선형식, 면형식, Desktop, 토적 프로그램, 데이터 검수', 4, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-27-02', '지원', 'UPIS', '표준시스템', '표준시스템 지도 오류', '데이터 상이', 'UPIS 표준시스템에서 좌표계 불일치 및 데이터 취합 오류로 인해 지도 레이어 어긋남 및 용도지역 미표출 현상이 발생하여, ArcGIS Desktop·QGIS로 원점 좌표계를 통일하고 데이터 구축 오류를 수정하여 해결하였다.', '표준시스템 지도 화면에서 레이어가 어긋나게 표출되거나, 도시계획 필지의 용도지역·시설 정보가 정상적으로 표출되지 않는다. 수직적 자료연계(광역↔기초) 과정에서 특정 지자체 데이터가 누락되거나 오류 상태로 연계된다.', '지자체별 공간 데이터의 원점 좌표계가 서로 다르게 구축되어 있거나, 광역(충청북도) 수직적 자료연계 시 기초 지자체(단양군·증평군 등) 데이터의 좌표계 설정 오류 및 데이터 취합 오류가 발생하였다. 엔진 자체 결함이 아닌 데이터 구축·취합 단계의 문제로 확인되었다.', '1. 현상 재현 확인: 표준시스템에서 해당 지자체 용도지역 레이어 표출 상태를 화면으로 확인한다.
2. 데이터 진단: ArcGIS Desktop 또는 QGIS로 문제 레이어를 열고 좌표계(투영·원점) 설정을 점검한다.
3. 좌표계 통일: 어긋난 레이어의 원점 좌표계를 기준 레이어와 동일하게 재설정(재투영)한다.
4. 수직적 자료연계 오류 지자체 식별: 데이터 타입 및 취합 방식이 상이한 지자체 목록을 파악한다.
5. 데이터 재구축: 좌표계 오류가 확인된 지자체(예: 단양군, 증평군) 데이터를 올바른 좌표계로 재구축한다.
6. 전체 연계 현황 점검: 수직적 자료교환체계 대상 지자체 전체에 대해 연계 오류 발생 여부를 일괄 확인한다.
7. 결과 공유: 처리 결과를 담당자(엔진사 또는 내부 담당)에게 메일 등으로 통보한다.', '수직적 자료연계 전 기초 지자체별 공간 데이터의 좌표계(투영·원점) 일치 여부를 사전 검수 단계에서 일괄 확인한다.', 'UPIS, 표준시스템, 지도 오류, 레이어 어긋남, 좌표계, 원점 좌표계, 수직적 자료연계, 용도지역 미표출, ArcGIS Desktop, QGIS, 데이터 구축 오류, 충청북도, 광명시, 산청군', 4, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-17-01', '장애', 'UPIS', '표준시스템', '표준시스템 로그인 오류', '데이터 오류', '표준시스템 로그인 불가 시 TN_USER_NEW 테이블에서 계정 잠금 해제 및 비밀번호 초기화로 복구', '표준시스템 접속 시 로그인이 되지 않거나 사용자 계정이 잠겨 있어 업무 사용이 불가능한 상태가 발생한다. 관리자 계정 삭제로 인한 사용자 승인 권한 부재 등 계정 구조 이상도 동일 증상으로 나타난다.', 'DB 테이블(TN_USER_NEW)의 계정 데이터 이상으로 인한 로그인 오류로, 비밀번호 암호화 값 불일치, 계정 잠금(LOCK) 상태 설정, 또는 관리자 계정 삭제 등이 원인으로 확인된다.', '1. 표준시스템 DB에 접속하여 TN_USER_NEW 테이블에서 해당 사용자 ID를 조회한다.
2. 계정 잠금 여부(락온오프 컬럼)를 확인하고 잠금 상태이면 해제 처리한다.
3. 암호화된 패스워드 컬럼 값을 기본값(2222)으로 변경한다.
4. 표준시스템에 로그인하여 정상 접속 여부를 확인한다.
5. 로그인 후 사용자가 원하는 비밀번호로 변경하도록 안내한다.
6. (관리자 계정 삭제 사례) sysadmin1 관리자 계정을 신규 생성하고 담당 주무관 권한에 표준시스템 관리자 전체 권한을 부여하여 복구한다.', '정기적으로 관리자 계정(sysadmin1 등) 존재 여부 및 계정 잠금 상태를 점검하고, 계정 삭제 전 반드시 백업 관리자 계정을 확보해 둔다.', 'UPIS, 표준시스템, 로그인 오류, 계정 잠금, TN_USER_NEW, 비밀번호 초기화, 사용자 계정, sysadmin1, 데이터 오류, 도시계획정보체계', 4, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-16-03', '지원', 'UPIS', '표준시스템', '표준시스템 데이터', '데이터 업로드', 'UPIS 표준시스템 구동 오류 발생 시 로그 확인 및 재기동으로 복구하고, 테이블 데이터 업로드(TN_DSTPLAN_DEPT, TN_SERVER_INFO 등)와 PDF·고시도면·입안자료 업로드를 완료하여 정상화.', 'UPIS 표준시스템 접속 또는 구동 시 오류가 발생하며 서비스가 정상적으로 동작하지 않는다. 또한 표준시스템에 필요한 데이터(테이블 정보, 문서 파일 등)가 누락되어 업로드가 필요한 상태이다.', '표준시스템 자체의 구동 오류(원인 불명확) 또는 데이터 미업로드로 인해 시스템이 정상 동작하지 않는 상태. 로그 확인 결과 표준시스템 자체에는 결함이 없었으며 재기동으로 해소된 일시적 오류로 추정.', '1. 표준시스템에 접속하여 오류 내용 확인
2. 표준시스템 로그(log) 파일 확인
3. 표준시스템 재기동 수행
4. TN_DSTPLAN_DEPT 테이블 데이터 업데이트 처리
5. TN_SERVER_INFO 테이블에 지자체 정보 입력
6. PDF, 고시도면, 입안자료 파일 업로드
7. 정상 동작 여부 테스트 진행
8. 처리 완료 후 업무 요청자에게 결과 보고', '표준시스템 기동 후 로그를 주기적으로 점검하고, 데이터 업로드 항목(테이블 정보, 고시도면 등)은 도입 초기 체크리스트를 통해 누락 없이 일괄 처리한다.', 'UPIS, 표준시스템, 구동 오류, 데이터 업로드, TN_DSTPLAN_DEPT, TN_SERVER_INFO, 고시도면, 입안자료, 재기동, 도시계획정보체계', 3, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-15-07', '장애', 'UPIS', '표준시스템', '표준시스템 구동 오류', '하드디스크 용량 부족', '서버 C드라이브 용량 부족으로 UPIS 표준시스템이 구동되지 않아 TEMP 및 uwes/store 폴더 정리를 통해 용량을 확보하여 정상화함.', 'UPIS 표준시스템이 정상적으로 기동되지 않거나 화면이 표출되지 않는 장애가 발생하였다. AP 서버의 C드라이브 여유 공간 부족이 원인으로 확인되었다.', '서버 C드라이브의 디스크 여유 공간이 부족하여 시스템 기동에 필요한 임시 파일 생성 및 서비스 프로세스 운용이 불가능해졌다.', '1. 서버에 접속하여 C드라이브 전체 사용량을 확인한다.
2. C:\Windows\TEMP 폴더 내 임시 파일을 정리하여 용량을 확보한다.
3. GWS/uwes/store 폴더 내 입안결정 등 누적 파일을 정리하여 추가 용량을 확보한다.
4. 용량 확보 후 UPIS 표준시스템 서비스를 재기동하여 정상 구동 여부를 확인한다.', '서버 C드라이브 잔여 용량을 주기적으로 모니터링하고, TEMP 및 uwes/store 폴더에 누적 파일이 쌓이지 않도록 정기 정리 정책을 수립한다.', 'UPIS, 표준시스템, GWS, uwes/store, 디스크 용량 부족, C드라이브, TEMP 폴더, 서버 장애, 시스템 기동 오류', 3, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-27-01', '지원', 'UPIS', '표준시스템', '표준시스템 지도 오류', 'UPIS.xml 패치', 'UPIS.xml 변경·추가로 인해 지자체 적용 XML 교체가 필요하여 원격 가능 여부에 따라 23개 지자체에 순차 배포 조치함.', 'UPIS.xml 변경 및 추가 이후 표준시스템 지도가 정상 표시되지 않는 오류가 발생하였다. 도시계획이력도(UPIS_P_RBEP) 등 일부 레이어가 잘못 연결되어 지도 데이터가 올바르게 출력되지 않는 증상이 나타났다.', 'UPIS.xml 파일 변경·추가 과정에서 도시계획이력도(도시계획시설집행시설) 레이어 참조명에 불필요한 언더바(_)가 부착된 잘못된 레이어명으로 연결되어 있었던 것이 원인이다.', '1. 원격 접속 가능 지자체(17개)와 원격 불가 지자체(6개)를 사전 구분하여 작업 계획 수립
2. 기존 UPIS.xml 내 도시계획이력도(UPIS_P_RBEP) 및 도시계획계획도-도시계획시설집행시설 레이어 연결 오류 확인 — 레이어명 언더바(_) 오기재 수정
3. 수정된 UPIS.xml을 원격 가능 지자체(17개)에 원격 접속하여 교체 배포
4. 원격 불가 지자체(6개)는 별도 방문 또는 개별 배포 절차로 교체 작업 수행
5. 연혁도 XML 조각을 중부·동부 권역별로 분리 제작하여 해당 지자체(단양·옥천·양양 등)에 복사·적용 후 재배포 완료
6. 배포 후 지도 레이어 정상 표시 여부 확인', 'UPIS.xml 변경 시 레이어명 정확성을 사전 검증하고, 배포 전 테스트 환경에서 레이어 연결 상태를 확인하는 절차를 수립한다.', 'UPIS.xml, 표준시스템, UPIS, 도시계획정보체계, 레이어 오류, 도시계획이력도, UPIS_P_RBEP, 도시계획시설집행시설, XML 패치, 지도 오류, 레이어명, 재배포', 3, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-03-01-01', '장애', 'UPIS', '연계', 'UPIS 수직자료 연계 확인요청', '미상(구양식)', 'UPIS 수직자료 연계 장애 발생 시 PBK 인증값 재도출 및 AP 서버 간 설정 동기화로 민원행정 연계를 복구함.', 'UPIS 수직자료 연계가 정상 작동하지 않아 하위 지자체와의 포트(80, 7070, 8080) 연계가 끊기거나 AP 서버 일부(AP2)에서 민원행정 오류가 발생함.', '원인 불명확(기록상 미상). 대표조치 기준으로 볼 때 새올 인증서 기반 PBK 값 이상, 하위 지자체 AP 서버 종료 또는 Oracle 연결 오류, AP1·AP2 서버 간 설정 불일치 등이 복합적으로 관여한 것으로 추정됨.', '1. 사업단으로부터 PBK.zip 파일을 수령하여 담당자에게 전달
2. PBK 프로그램으로 대상 시군(새올) 인증서의 PBK 값 도출 시도 — 오류 발생 시 인접 정상 시군(예: 밀양시)에서 대상 지자체의 PBK 값 대리 도출
3. 도출된 PBK 값을 담당자에게 전달하고 AP 서버에 적용
4. AP1·AP2 서버 각각에서 민원행정 연계 정상 여부 확인
5. AP2 서버에서 오류 지속 시 AP1 서버 설정 내용을 복사하여 AP2에 적용 시도
6. 적용 실패 시 시스템 로그 파일 위치를 확인하고 로그를 수집하여 원인 분석
7. 하위 지자체 포트 연계 상태 점검: 7070, 80, 8080 포트 개방 여부 일괄 확인 및 미개방 지자체 조치 요청
8. AP 서버가 종료된 지자체(의령군 등)는 서버 기동 후 Oracle 연결 상태 재확인', '수직자료 연계 운영 전 하위 지자체 전체의 포트(80·7070·8080) 개방 여부와 AP 서버 기동 상태를 주기적으로 점검하고, AP1·AP2 서버 간 설정 파일을 동기화 상태로 유지하여 단일 서버 장애 시 빠른 전환이 가능하도록 관리.', 'UPIS, 수직자료연계, PBK, 새올, 인증서, AP서버, Oracle, 포트연계, 민원행정, 경상남도, 대구광역시', 3, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-18-02', '장애', 'UPIS', '표준시스템', '표준시스템 민원행정 연계', '서버인증서 갱신', 'GPKI 서버인증서 만료로 UPIS 표준시스템과 민원행정 시스템 간 연계가 단절되어 인증서를 교체하여 복구하였다.', 'UPIS 표준시스템에서 민원행정 시스템과의 연계가 정상적으로 동작하지 않는다. 민원행정 연계 호출이 실패하거나 응답 오류가 발생한다.', 'GPKI 서버인증서 유효기간 만료 또는 인증서 갱신 누락으로 인해 연계 구간 SSL/인증 검증이 실패한 것으로 추정된다.', '1. GPKI 인증서 만료 여부 및 현재 인증서 유효기간을 확인한다.
2. 발급기관(GPKI)으로부터 갱신된 서버인증서 파일을 발급받는다.
3. UPIS 표준시스템 서버의 GPKI 인증서를 신규 인증서로 교체한다.
4. 교체 후 민원행정 연계 기능을 재테스트하여 정상 동작을 확인한다.', 'GPKI 서버인증서 만료일을 주기적으로 점검하고, 만료 30일 전에 갱신 절차를 진행하여 연계 단절을 사전에 방지한다.', 'UPIS, 표준시스템, 민원행정연계, GPKI, 서버인증서, 인증서갱신, SSL, 인증서만료, 연계장애', 3, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-25-02', '장애', 'UPIS', '표준시스템', '표준시스템 조회 오류', '데이터 유실', 'UPIS 표준시스템에서 행정경계 코드 불일치 또는 공간데이터 유실로 인해 조회·필지검색·섬네일 표출이 되지 않는 장애를 데이터 수정 및 백업 재탑재로 해결함.', '표준시스템에서 특정 읍면동(EMD) 구역 조회가 되지 않거나, 필지검색이 동작하지 않으며, 도면보기 섬네일이 표출되지 않는 현상이 발생함.', '행정경계 코드(emd_cd) 값이 연속지적도의 실제 코드와 불일치하거나, 연속지적도(LP_PA_CBND) 및 섬네일 파일이 유실되어 조회 연계가 끊어진 것이 원인임.', '1. 조회 불가 행정구역의 emd_cd 값을 연속지적도(LP_PA_CBND) 기준 코드와 대조하여 불일치 여부 확인
2. lp_aa_emd 또는 LA_PA_EMD 테이블에서 해당 행의 emd_cd 값을 올바른 코드로 수정 요청
3. 수정 후에도 오류가 지속되면 LA_PA_EMD 등 연관 테이블에 중복·오류 데이터가 있는지 재확인 후 재수정
4. 연속지적도(LP_PA_CBND) 유실이 확인된 경우 최신 백업 데이터(예: 180119일자 백업)에서 연속지적도 레이어를 추출하여 재탑재
5. 섬네일 미표출 문제의 경우 섬네일 폴더에서 해당 파일 존재 여부를 확인하고, 파일이 없으면 섬네일을 재생성하여 지정 경로에 저장', '정기적으로 연속지적도 및 행정경계 테이블의 코드 정합성을 점검하고, 공간데이터 백업을 최신 상태로 유지하여 유실 시 신속히 복구할 수 있도록 관리함.', 'UPIS, 표준시스템, emd_cd, lp_aa_emd, LA_PA_EMD, LP_PA_CBND, 연속지적도, 행정경계코드, 데이터유실, 섬네일, 필지검색, 조회오류, 백업재탑재', 3, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-26-05', '장애', 'UPIS', '표준시스템', '표준시스템 지도 오류', 'uwes 패치', 'UPIS 표준시스템에서 지도가 표시되지 않는 장애가 발생하여 uwes.war 패치 및 GWS 재설치·환경설정 복원으로 정상화하였다.', 'UPIS 표준시스템에서 지도가 전혀 출력되지 않거나 레이어가 표시되지 않는 장애가 발생하였다. GWS(GeoNURIS WebServer) 확인 시 uwes 모듈을 찾을 수 없거나, uwes 및 upis.xml 패치가 미적용된 상태임이 확인된다.', 'uwes(웹 엔진 서비스) 패치 작업 과정에서 기존 uwes 모듈이 삭제되거나 패치가 정상 적용되지 않아 GWS가 uwes를 인식하지 못하게 된 것으로 추정된다. 일부 사례에서는 GWS 32비트/64비트 불일치 문제도 복합적으로 작용한 것으로 확인된다.', '1. GeoNURIS WebServer(GWS) 서비스 및 표준시스템 톰캣 중지
2. uwes 폴더 내 store 디렉터리의 export~ 캐시 데이터 삭제(백업 시간 단축 목적)
3. backup_날짜 폴더를 생성하여 기존 uwes 폴더 및 uwes.war 백업
4. 패치 대상 uwes.war를 지정 경로에 배치
5. GWS 콘솔 창으로 기동하여 uwes deploy 진행 여부 확인
6. uwes deploy 완료 후 GWS 서비스 재중지
7. 기존 uwes/WEB-INF 내 upis.properties 및 web.xml 환경설정 값 복원
8. GWS 및 표준시스템 톰캣 재기동 후 지도 정상 출력 확인
9. [GWS 재설치 필요 시] 기존 32비트 GWS 삭제 후 64비트 GWS 재설치, gdx 적용 및 uwes 재설치
10. [엔진 패치 미적용 시] gss.jar 패치 적용 및 gss 재기동, 용도지역 LOD 작업 및 연속지적도 공간인덱싱 실행, uwes 파일 패치 및 GWS 재기동, UPIS.xml 신규 패치 적용 후 확인', 'uwes 패치 전 반드시 GWS 비트(32/64) 버전 일치 여부를 사전 확인하고, 패치 후 uwes deploy 상태 및 upis.properties·web.xml 환경설정 보존 여부를 콘솔에서 검증한다.', 'UPIS, 표준시스템, 지도오류, uwes, uwes.war, GWS, GeoNURIS WebServer, GSS, gss.jar, upis.properties, web.xml, UPIS.xml, 톰캣, LOD, 공간인덱싱, gdx, uwes패치, GWS재설치', 3, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-15-01', '장애', 'UPIS', '표준시스템', '표준시스템 구동 오류', 'H/W OFF', 'AP 서버 전원 OFF로 UPIS 표준시스템이 구동되지 않아 서버 재기동 및 Spatial Server 재설치로 복구함.', '표준시스템이 구동되지 않아 사용자 접속이 불가능한 상태가 발생하였음. PING 테스트 결과 AP 서버가 응답하지 않는 것으로 확인됨.', 'AP 서버(Application Server)의 전원이 꺼진 상태(H/W OFF)로 인해 표준시스템이 실행되지 못함.', '1. PING TEST로 AP 서버 전원 OFF 상태 확인
2. 담당 주무관에게 유선 연락하여 AP 서버 전원 ON 요청
3. 서버 원격 접속하여 AP_Server 기동 상태 확인 및 재구동
4. DB 서버 접속권한 문제로 Spatial Server를 AP_Server에 신규 설치
5. 표준시스템 재구동 후 정상 표출 여부 테스트', 'AP 서버 전원 상태를 주기적으로 모니터링하고, 비정상 종료 시 담당자에게 즉시 알림이 가도록 서버 감시 체계를 구성함.', 'UPIS, 표준시스템, AP서버, AP_Server, Spatial Server, 서버 전원 OFF, 서버 재기동, 장애복구', 3, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-20-02', '지원', 'UPIS', '표준시스템', '표준시스템 민원행정 연계 오류', '서버인증서 오류', 'GPKI 서버인증서 만료로 표준시스템(UPIS)과 새올행정 간 민원행정 연계가 중단되어 담당 사업부에 인증서 재발급을 요청함.', '표준시스템(UPIS)에서 새올행정과의 민원행정 연계 기능이 오류로 작동하지 않는다. 여러 지자체(보령시, 영월군, 평창군)에서 동일 증상이 발생하였다.', '새올행정 연계에 사용하는 GPKI 서버인증서의 유효기간이 만료되어 인증 처리에 실패한 것으로 확인됨.', '1. 새올행정 민원행정 연계 오류 발생 시 GPKI 서버인증서 만료 여부를 확인한다.
2. 인증서 만료가 확인되면 GPKI 서버인증서 재발급을 담당 사업부에 요청(전달)한다.
3. 재발급 완료 후 새올행정 연계 정상 동작 여부를 확인한다.', 'GPKI 서버인증서 만료일을 주기적으로 점검하여 만료 전에 사전 재발급을 진행한다.', 'UPIS, 표준시스템, 새올행정, GPKI, 서버인증서, 인증서만료, 민원행정연계, 연계오류', 3, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-02-02-01', '장애', 'UPIS', '엔진', '표준시스템 민원행정 연계 오류', '라이선스 오류', '표준시스템 민원행정 연계 시 임시 라이선스 만료로 오류가 발생하여 라이선스 갱신 요청으로 해소.', '표준시스템(UPIS)에서 민원행정 연계 기능을 확인하는 과정에서 라이선스 오류가 발생하여 연계가 정상 동작하지 않았다.', '연계에 사용된 라이선스가 임시 라이선스였으며, 해당 라이선스의 유효기간이 만료되어 라이선스 오류가 발생한 것으로 확인되었다.', '1. 민원행정 연계 기능 동작 여부를 확인하고 라이선스 오류 발생 사실을 파악한다.
2. 서울시 담당자에게 메일로 라이선스 확인을 요청한다.
3. 임시 라이선스 여부 및 만료 여부를 확인한다.
4. 라이선스 갱신 필요 사실을 담당 부장(정영훈) 및 담당 대리(담당자)에게 전달하여 갱신 조치를 요청한다.', '임시 라이선스 사용 시 만료일을 사전에 등록하고, 만료 전 갱신 절차를 미리 진행한다.', 'UPIS, 표준시스템, 민원행정연계, 라이선스 오류, 임시라이선스, 라이선스 갱신, 엔진', 2, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-10-01', '지원', 'UPIS', '표준시스템', '지도 미표출', '미상(구양식)', '용도지역 등 공간 데이터 업데이트 시 좌표계 미설정으로 지도가 표출되지 않으며, 공간정의 및 좌표계 설정 후 LOD 재실행으로 해결된다.', '표준시스템에서 지도가 정상적으로 표출되지 않는 현상이 발생한다. 용도지역 등 레이어 데이터를 업데이트한 이후 해당 레이어가 지도 화면에 나타나지 않는다.', 'DB 업데이트(용도지역 등 공간 데이터 갱신) 시 데이터에 공간정의(좌표계) 가 설정되지 않아 시스템이 지도 데이터를 인식하지 못한 것으로 추정된다.', '1. 연혁도(배경 지도 이력) 업데이트를 수행한다.
2. 지도 미표출 레이어(예: 용도지역)의 공간 데이터에 좌표계가 설정되어 있는지 확인한다.
3. 좌표계가 누락된 경우 해당 데이터에 적절한 공간정의(좌표계)를 설정한다.
4. 설정 완료 후 LOD(Level of Detail)를 실행하여 지도 데이터를 재구성한다.
5. 지도 표출 여부를 재확인하여 정상 출력됨을 확인한다.', '공간 데이터(용도지역 등) DB 업데이트 후 반드시 좌표계(공간정의) 설정 여부를 검수 항목에 포함하여 누락을 사전에 방지한다.', '표준시스템, UPIS, 지도 미표출, 좌표계, 공간정의, 용도지역, LOD, 연혁도, DB 업데이트, 레이어', 2, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-05-06-01', '지원', 'LSA', '토지적성평가', '토지적성평가 라이센스 발급', '미상(구양식)', '토지적성평가 내부용 라이센스를 1년 단위로 갱신 발급하여 사용 가능 상태로 복원하였다.', '토지적성평가 시스템의 라이센스가 만료되거나 신규 발급이 필요한 상황이 발생하였다. 해당 기능의 정상 사용을 위해 라이센스 발급 처리가 요청되었다.', '원인 불명확(기록상 미상) — 라이센스 만료 주기(1년) 도래에 따른 갱신 필요로 추정된다.', '1. 토지적성평가 라이센스 발급 요청 접수(내부용 여부 확인)
2. 1년 단위 갱신 기준으로 라이센스 생성
3. 발급된 라이센스를 해당 기관(경산 본사)에 전달 및 적용 완료 확인', '라이센스 만료 30일 전 사전 알림 체계를 마련하여 갱신 지연으로 인한 서비스 중단을 예방한다.', '토지적성평가, 라이센스, 라이센스 발급, 라이센스 갱신, 내부용 라이센스, 1년 단위 갱신', 2, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-19-02', '지원', 'UPIS', '표준시스템', '표준시스템 민원행정 연계', 'TC_SAEALL_INFO 설정', 'TC_SAEALL_INFO 테이블의 부서코드 또는 GPKI 인증서 패스워드 불일치로 UPIS 표준시스템 민원행정 연계가 실패하는 문제를 해당 값 재입력으로 해결.', 'UPIS 표준시스템에서 민원행정 연계가 정상적으로 동작하지 않아 민원 처리 흐름이 중단된다.', 'TC_SAEALL_INFO 테이블에 설정된 민원처리부서 부서코드 또는 GPKI 인증서 패스워드가 실제 운영 값과 불일치하여 연계 인증 및 부서 매핑이 실패함.', '1. 민원처리부서(원스톱민원허가과 등) 현행 부서코드를 시스템 또는 담당 부서에서 확인한다.
2. GPKI 인증서 변경 여부를 확인하고, 변경된 경우 신규 패스워드를 수령한다.
3. TC_SAEALL_INFO 테이블의 부서코드 및 GPKI 인증서 패스워드 컬럼을 최신 값으로 재입력(UPDATE)한다.
4. 표준시스템에서 민원행정 연계 상태를 재확인하여 정상 연계 여부를 검증한다.', '부서 조직 개편 또는 GPKI 인증서 갱신 시 TC_SAEALL_INFO 테이블 설정 값을 즉시 동기화하는 절차를 운영 매뉴얼에 포함한다.', 'UPIS, 표준시스템, 민원행정연계, TC_SAEALL_INFO, 부서코드, GPKI, 인증서패스워드, 연계오류', 2, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-14-02', '지원', 'UPIS', '표준시스템', '표준시스템 구동 오류', '표준시스템 tomcat 오류', 'UPIS 표준시스템 Apache Tomcat 엔진 다운으로 지도 미표출 및 페이지 장애가 발생하여 Tomcat 재기동으로 복구하였다.', 'UPIS 표준시스템에서 지도가 표출되지 않거나 페이지 장애가 발생한다. 웹서버 및 표준시스템 엔진(Apache Tomcat 8.0 UPIS)이 다운된 상태로 확인된다.', 'Apache Tomcat 8.0 UPIS 엔진 프로세스가 비정상 종료되어 서비스가 중단된 것이 원인으로, 구체적인 다운 원인은 기록상 미상이다.', '1. 표준시스템 지도 미표출 및 페이지 오류 증상 확인
2. Apache Tomcat 8.0 UPIS 서비스 프로세스 다운 여부 확인
3. Apache Tomcat 8.0 UPIS 엔진 기동(재시작) 실시
4. Tomcat 로그 확인하여 기동 오류 여부 점검
5. UPIS 표준시스템 접속 후 지도 표출 및 주요 기능 정상 동작 확인', 'Apache Tomcat 8.0 UPIS 서비스에 자동 재시작 정책(윈도우 서비스 복구 옵션 또는 모니터링 스크립트)을 적용하여 엔진 다운 시 신속히 복구되도록 설정한다.', 'UPIS, 표준시스템, Apache Tomcat, Tomcat 8.0, 지도 미표출, 엔진 다운, 페이지 장애, 남양주시, 제주특별자치도, 도시계획정보체계', 2, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-18-08', '지원', 'UPIS', '표준시스템', '표준시스템 문서 작성', '정보연계신청서', 'UPIS 표준시스템과 새올 시스템 간 정보연계신청서를 기관 요청에 따라 대리 작성·송부하여 연계 절차를 완료하였다.', '기관(제주특별자치도)이 새올 시스템과의 정보연계를 위한 정보연계신청서를 자체 작성하지 못하여 지원팀에 대리 작성을 요청하였다.', '기관 담당자가 정보연계신청서 양식 및 작성 절차에 익숙하지 않아 직접 작성이 어려운 상황으로, 지원팀이 대리 작성하여 처리하였다.', '1. 기관으로부터 접수된 공문 또는 요청 내용을 확인한다.
2. 새올 연계에 필요한 정보연계신청서 양식을 준비한다.
3. 필요 시 담당 업체(예: 지오투) 담당자와 유선통화하여 기재 사항을 조율한다.
4. 정보연계신청서를 대리 작성한다.
5. 작성 완료된 신청서를 기관 또는 관련 기관에 송부한다.', '기관 담당자에게 정보연계신청서 작성 절차와 양식을 사전 안내하여 자체 작성 역량을 확보하도록 한다.', 'UPIS, 표준시스템, 새올, 정보연계신청서, 연계신청, 대리작성, 제주특별자치도, 지오투', 2, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-04-05-01', '장애', 'LSA', '토지적성평가', '토지적성평가 로그인 오류', '인터넷 접속 오류', '토지적성평가 로그인 실패 장애는 인터넷 연결 불량이 원인이었으며, 네트워크 연결 복구 후 정상 로그인이 확인되었다.', '토지적성평가 프로그램 실행은 정상이나 로그인 시도 시 오류가 발생하여 진입이 불가능하다.', '사용자 단의 인터넷 연결 불량으로 인해 인증 서버와의 통신이 차단되어 로그인 처리가 실패하였다.', '1. 사용자에게 토지적성평가 프로그램 실행 여부와 로그인 단계에서의 오류 발생 시점을 확인한다.
2. 사용자 PC의 인터넷 연결 상태(외부망 접속 가능 여부)를 직접 확인하도록 요청한다.
3. 인터넷 연결 이상 발생 시 네트워크 복구(케이블 재연결, 공유기 재기동 등)를 수행한다.
4. 인터넷 연결 복구 후 토지적성평가에 재로그인하여 정상 진입을 확인한다.', '토지적성평가 로그인 오류 발생 시 프로그램 오류 전에 사용자 단 인터넷 연결 상태를 먼저 점검하는 절차를 안내 매뉴얼에 포함한다.', '토지적성평가, 로그인 오류, 인터넷 연결 불량, 네트워크 장애, UPIS, LSA', 2, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-25-03', '장애', 'UPIS', '표준시스템', '표준시스템 조회 오류', '인터넷 옵션 설정', '표준시스템 조회 화면 미표출 및 팝업 차단 문제를 캐시 삭제, 크롬 팝업 허용 설정, Tomcat 재기동 및 메모리 증설로 해결하였다.', '고시정보·조서정보 검색란이 화면에 표출되지 않거나, 필지정보조회 팝업창이 열리지 않는 현상이 발생한다. 경우에 따라 KRAS 연계 필지정보조회가 비정상적으로 느려지기도 한다.', '브라우저 캐시 또는 팝업 차단 설정, 담당자 PC의 일시적 장애, Tomcat 메모리 부족(1024MB) 등 복합적 원인으로 발생하며, 사례에 따라 단일 원인을 특정하기 어렵다.', '1. 브라우저 캐시를 삭제하고 재시도하도록 사용자에게 안내한다.
2. 크롬 브라우저의 팝업 차단 해제 설정을 확인하고, 표준시스템 도메인을 팝업 허용 목록에 추가한다.
3. 서버(Tomcat) 및 필지정보 연계 서비스 정상 여부를 점검한다.
4. 조회 속도 저하 또는 응답 불량 시 Tomcat 8.0을 재기동한다.
5. Tomcat JVM 힙 메모리를 1024MB에서 4096MB로 증설하여 성능을 개선한다.
6. 위 조치 후에도 증상이 지속될 경우 담당 주무관 PC의 개별 장애 여부를 확인하고 현장 지원 또는 원격 지원으로 연결한다.', 'Tomcat 메모리 설정을 운영 초기에 충분히 확보(권장 4096MB 이상)하고, 브라우저 팝업 차단 해제 설정을 사용자 매뉴얼에 필수 항목으로 안내하여 반복 문의를 예방한다.', 'UPIS, 표준시스템, 고시정보, 조서정보, 필지정보조회, KRAS, Tomcat, 팝업차단, 캐시삭제, 메모리증설, 조회오류', 2, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-25-04', '장애', 'UPIS', '표준시스템', '표준시스템 조회 오류', '표준시스템 tomcat 설정 오류', 'Tomcat server.xml의 context docBase 대소문자 불일치로 인해 UPIS 표준시스템에서 고시문·고시도면이 미표출되던 장애를 경로명 수정으로 해결함.', 'UPIS 표준시스템에서 고시문 및 고시도면이 화면에 표출되지 않는 조회 오류가 발생하였다.', 'Tomcat server.xml의 Context 엘리먼트 docBase 값이 ''''UpisArchive''''(소문자 p)로 설정되어 있으나, 실제 고시문 데이터 경로는 ''''UPISArchive''''(대문자)로 되어 있어 경로 불일치가 발생하였다.', '1. 고시문·고시도면 미표출 현상을 재현하여 오류를 확인한다.
2. Tomcat 설치 경로의 conf/server.xml 파일을 열어 해당 Context 엘리먼트의 docBase 값을 확인한다.
3. docBase 값 ''''UpisArchive''''를 실제 데이터 경로와 일치하는 ''''UPISArchive''''(전체 대문자)로 수정한다.
4. Tomcat 서비스를 재시작하여 변경 사항을 적용한다.
5. 표준시스템에서 고시문·고시도면이 정상 표출되는지 확인한다.', 'Tomcat server.xml의 docBase 경로는 운영체제의 실제 디렉터리명(대소문자)과 정확히 일치하도록 초기 설치 시 검수 항목에 포함한다.', 'UPIS, 표준시스템, Tomcat, server.xml, docBase, 대소문자, 고시문, 고시도면, Context, 조회오류', 2, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-20-01', '장애', 'UPIS', '표준시스템', '표준시스템 수직적자료교환 오류', 'uwes 패치', 'uwes 패치 적용 시 수직적자료교환 오류가 발생하여 GeoNURIS WebServer 및 톰캣 서비스를 중지한 후 uwes.war를 교체·설정 복원하고 재기동함으로써 정상화.', '표준시스템에서 수직적자료교환 기능이 오류를 일으켜 정상적으로 동작하지 않음. uwes 패치 적용 후 해당 현상이 확인됨.', 'uwes 패치 적용 과정에서 기존 upis.properties 및 web.xml 설정이 새 배포 파일에 반영되지 않아 수직적자료교환 기능이 정상 작동하지 않은 것으로 추정.', '1. 서비스 관리자에서 GeoNURIS WebServer 및 표준시스템 톰캣 서비스 중지
2. 기존 uwes 폴더 내 store 디렉터리의 export~ 데이터 삭제 (백업 시간 단축 목적)
3. backup_날짜 폴더를 생성하여 기존 uwes 폴더 전체 및 uwes.war 백업
4. 패치 버전의 uwes.war를 해당 경로에 복사
5. GWS 콘솔창에서 기동하여 uwes deploy 진행 여부 확인
6. deploy 완료 후 GWS 서비스 재중지
7. 기존 백업에서 WEB-INF/upis.properties 및 web.xml 설정 정보를 새 uwes 배포 경로에 복원
8. 웹서버 및 톰캣 재기동 후 수직적자료교환 정상 동작 확인', 'uwes.war 패치 전 WEB-INF/upis.properties 및 web.xml을 별도로 보관하고, 패치 절차 체크리스트에 설정 파일 복원 단계를 필수 항목으로 포함.', 'UPIS, 표준시스템, uwes, uwes.war, 수직적자료교환, GWS, GeoNURIS WebServer, 톰캣, upis.properties, web.xml, 패치, deploy', 2, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-14-01', '장애', 'UPIS', '표준시스템', '표준시스템 SSO 오류', '설정 오류', '표준시스템 SSO 연계 오류가 TN_USER_NEW 테이블의 새올 아이디 미등록으로 발생하였으며, 해당 테이블에 담당자 새올 아이디를 입력하여 해결하였다.', '표준시스템에서 SSO(Single Sign-On) 연계 오류가 발생하여 사용자가 정상적으로 로그인하지 못한다.', 'TN_USER_NEW 테이블에 담당 주무관의 새올 아이디가 등록되어 있지 않아 SSO 인증 연계에 실패하였다.', '1. TN_USER_NEW 테이블 설정 오류 여부 확인
2. TN_USER_NEW 테이블에 담당 주무관의 새올 아이디가 등록되어 있는지 조회
3. 미등록 시 TN_USER_NEW 테이블에 해당 주무관의 새올 아이디를 입력·등록
4. SSO 로그인 재시도 및 정상 연계 여부 확인', '신규 사용자 등록 또는 담당자 변경 시 TN_USER_NEW 테이블에 새올 아이디를 즉시 갱신하는 절차를 운영 매뉴얼에 포함시킨다.', 'SSO, 표준시스템, UPIS, TN_USER_NEW, 새올, SSO 연계 오류, 인증 오류, 사용자 테이블', 2, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-26-07', '장애', 'UPIS', '표준시스템', '표준시스템 지도 오류', '표준시스템 패치 오류', '표준시스템 패치 오류로 지도 초기 레이어가 중복 표시되는 장애를 맵 레이어 세팅값 전체 삭제 및 정식 버전 패치로 해결하였고, Tomcat 서비스 등록 불가 시 모듈 활성화 방식으로 우회 기동하였다.', '표준시스템(UPIS) 지도 화면에서 맵 초기 레이어들이 중복·겹치는 현상이 발생하였다. 일부 사례에서는 Tomcat 엔진의 서비스 등록 자체가 불가하여 표준시스템이 정상 기동되지 않았다.', '서버에 적용된 표준시스템 버전의 패치 오류로 인해 맵 초기 레이어 세팅이 비정상 상태가 된 것으로 추정된다. Tomcat 서비스 등록 장애는 구조적 문제로 인해 서비스 등록 방식이 지원되지 않는 환경에서 발생하였다.', '1. 표준시스템 지도 화면에서 맵 초기 레이어 겹침 현상 및 엔진 기동 상태를 확인한다.
2. 서버의 표준시스템 버전을 확인하여 패치 오류 여부를 점검한다.
3. 맵 초기 레이어 세팅값을 전체 삭제하여 중복 레이어 문제를 해소한다.
4. 테스트 서버 디스크 교체 후 정식 버전으로 패치를 적용하여 완료한다.
5. Tomcat 서비스 등록이 불가한 경우, 서비스 등록 방식 대신 모듈 활성화(모듈활성) 방식으로 엔진을 기동한다.', '표준시스템 버전 패치 전 테스트 서버에서 맵 레이어 세팅 및 Tomcat 기동 방식을 사전 검증한 후 운영 서버에 적용한다.', 'UPIS, 표준시스템, 지도 오류, 맵 레이어, 레이어 겹침, Tomcat, 서비스 등록, 모듈활성화, 버전 패치, 도시계획정보체계', 2, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-03-04-01', '지원', 'UPIS', '엔진', '엔진 기타', '기타', '지적도 캐시 14레벨 초과 확대 시 파일 과다 생성에 따른 부하 발생 및 지도 서비스 미표출 문의를 확인하고, 캐시 구성 및 외부 배경지도 API 문제로 안내 처리함.', '시스템에서 지도 서비스가 표출되지 않거나, 지적도 캐시를 14레벨 이상으로 확대 생성할 때 파일 수 급증으로 인한 부하 문제가 발생함.', '지적도 캐시가 nnp_gis_base.xml 참조 기준으로 14레벨까지만 생성되도록 설정되어 있으나, 시스템상 추가 확대(14레벨 초과) 시에도 캐시가 생성되어 파일 수 과다로 인한 부하가 유발됨. 지도 미표출의 경우 외부 배경지도 API(다음 지도) 연동 문제로 추정되나 최종 원인은 담당자 확인이 필요한 상태로 이관됨.', '1. 캐시 생성 참조 XML(nnp_gis_base.xml) 파일 위치 및 설정 내용 확인
2. 캐시 저장 경로(/gisdata/cache/nnp_gis_base) 및 생성 레벨(14레벨 기준) 확인
3. 14레벨 초과 확대 구간에서도 캐시가 추가 생성됨을 확인하고, 해당 구간의 파일 수 증가에 따른 부하 가능성을 담당자에게 전달
4. GSS 및 GWS 서비스 구동 상태 확인(정상 구동 여부)
5. GWS 관리자 페이지에서 지도 표출 여부 확인
6. 외부 배경지도 API(다음 지도) 연동 상태를 확인하여 배경지도 미수신 여부 파악 후 담당자에게 자체 확인 및 처리 요청', '지적도 캐시 레벨 범위를 운영 환경에 맞게 제한하고, 14레벨 초과 확대 기능 사용 시 스케일 변수 및 파일 수 증가에 따른 부하를 사전에 검토하여 캐시 정책을 수립한다.', 'UPIS, GSS, GWS, 지적도 캐시, nnp_gis_base, 캐시 레벨, 지도 미표출, 다음 지도 API, 배경지도, /gisdata/cache, 엔진', 2, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-26-08', '장애', 'UPIS', '표준시스템', '표준시스템 지도 오류', '하드디스크 용량 부족', 'C드라이브 용량 부족으로 GWS store 백업파일이 디스크를 꽉 채워 표준시스템 지도가 표출되지 않았으며, 백업파일 삭제 후 서비스 재기동으로 해결', '표준시스템에서 지도가 정상적으로 표출되지 않으며, 로그 파일조차 기록되지 않는 현상이 발생하였다.', 'GWS store 디렉터리에 누적된 대용량 백업파일(약 40GB)이 C드라이브 여유 공간을 모두 소진하여 GWS 및 표준시스템 정상 동작이 불가능해진 것이 원인이다.', '1. 표준시스템 지도 미표출 및 로그 미기록 현상 확인
2. 서버 C드라이브 잔여 용량 점검 (디스크 풀(full) 상태 확인)
3. GWS store 디렉터리 내 백업파일 확인 및 불필요한 백업파일 삭제하여 디스크 공간 확보
4. GWS 서비스 재기동
5. 표준시스템 서비스 재기동 후 지도 표출 정상 여부 확인', 'GWS store 백업파일 보관 정책을 수립하고 주기적으로 오래된 백업파일을 정리하여 C드라이브 여유 공간을 일정 수준 이상 유지한다.', 'UPIS, 표준시스템, GWS, 지도 오류, 디스크 용량 부족, store 백업파일, C드라이브 풀, 서비스 재기동', 2, TRUE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-01-04-01', '장애', 'UPIS', '기타', '토지적성평가 라이센스 발급', '미상(구양식)', NULL, NULL, NULL, '* 토드(TOAD)에 속성데이터 Import 하였을 시에 한글데이터 깨짐 현상 - 김준혁 과장 해당 본인 PC에서만 한글이 깨져보이며, 실데이터는 깨지지 않음.', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-03-03-01', '장애', 'UPIS', '연계', '민원행정 연계', '미상(구양식)', NULL, NULL, NULL, '* 당진시 UPIS 표준시스템 민원행정 연계 1. 당진시 교체된 민원행정공통기반2단계 인증서값 수령 2. 당진시의 금번 교체된 민원행정공통기반2단계 인증서가 민원행정사업단에 등록되어있는지 확인 3. 당진시 UPIS서버에서 공개키값 발급 확인(PBK값) 4. 타지자체에서 당진시 공개키값 발급 확인 -', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-03-04-01', '장애', 'UPIS', '연계', '새올행정 연계 오류', '미상(구양식)', NULL, NULL, NULL, '1. GPKI error code = 1507 오류가 발생하여 새올민원 연계가 되지않음 2. 에러코드 인증서 확인 요청 3. 해당 서버 새올행정 인증서가 만료되어 PBK 모듈 사용하여 재발급 4. 새올행정 연계확인 및 메시지 보고 완료', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-01-02-01', '장애', 'UPIS', '기타', '시스템상에서 한글이 깨지는 현상', '미상(구양식)', NULL, NULL, NULL, '◯ 문제점 상세 법무부 테스트 시스템의 지도에서 한글이 깨지는 현상을 발견하고 연락옴 ◯ 원인분석 1. DB에서 한글의 깨짐 유무를 확인(정상표출) 2. Desktop의 속성테이블 조회기능을 이용하여 공간데이터 서버에서 불러오는 데이터의 한글의 깨짐 유무를 확인(정상표출) 3. WebServer에서 지도를 표출하여 한글이 깨짐 여부를 확인(비정상표출) ◯ 분석결과 WebServer상에서 한글이 깨지는 것을 확인하였으며 이는 지도에 설정된 라벨에 문제가 있는 것 임(보통은 OS폰트가 설정되지 않아서 발생하는 문제임) ◯ 조치사항 Linux한글 폰트를 다시 설정하고 해결이 되지 않으면 연락을 달라고 하였음', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-01-01-01', '장애', 'UPIS', '기타', '레이어 오류', '미상(구양식)', NULL, NULL, NULL, '완전 빈도형 작업 및 레이어 오류 발견 - 18.01.11 (목) 오전에 완전 빈도형 작업을 옥천군청에서 하던 도중 도시계획시설집행시설 (P_UQ151_1 ~ P_UQ159_1), 개발행위허가 미분류, 기타(H_UQ174_1, H_UQ174_2) 발견 - 개발행위허가는 레이어는 잘못된 레이어로 판명 (스타일이 구분되어있어야 하는데 레이어가 구분되어있으므로) - 도시계획이력도와 계획도안의 도시계획시설집행시설은 거의 모든 지자체가 _형식으로 위와 같이 되어있는 것으로 확인 - 18.01.11 (목) 오후에 잘못된 레이어명 수정 완료 - 현재 Xml 오류 계속해서 찾는 중', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-01-03-01', '장애', 'UPIS', '기타', '울릉도의 주소가 표시되지 않는 문제', '미상(구양식)', NULL, NULL, NULL, '◯ 조치사항 지도가 맞지 않는 것을 법무부 담당에게 알려주었으며 담당은 유볼트 장현길이사님을 통해 틀어진 지도를 맞춘다고 함(정도에서는 더 이상 관여할 일이 없음)', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-03-02-01', '장애', 'UPIS', '연계', 'UPIS표준시스템 오류', '미상(구양식)', NULL, NULL, NULL, '1. UPIS8.0 톰캣 구동상태에서 재기동을 한 에러가 발생하여 정상종료 후 재기동 2. GWS서버도 종료시켜두었다고 하여 재기동 3. AP1, AP2번 서버 확인 정상 확인 1. AP1서버 톰캣이 서비스에서 실행되지 않음 2. 이벤트 로그를 확인하라는 에러메시지가 뜨면서 서비스 실행 중지 3. UPIS8w 샘플 파일을 붙여넣었으나 동일 오류발생 4. 톰캣을 새로 설치 후 정상작동 완료 5. AP2번 서버는 GWS 기동이 되어 있지 않아 오류가 발생, 재기동하여 문제 해결', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-02-01', '장애', 'UPIS', '표준시스템', 'UPIS 표준시스템 및 엔진 분기 점검', '미상(구양식)', NULL, NULL, NULL, '1. GSS 꺼져있음 (구동화면에서 타이핑이 되어있어 GSS 중지되어 재기동) 2. 웹서버 접속하여 비어있는 레이어 추가', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-01-01', '장애', 'UPIS', '표준시스템', 'GIS 엔진 정기점검', '미상(구양식)', NULL, NULL, NULL, '1. GIS 엔진 점검 실시 - 재해취약성 구축되어 있음', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-04-04-01', '장애', 'LSA', '토지적성평가', '토지적성평가 기타', '기타', NULL, NULL, NULL, '· 구역계 파일을 검토 등록하면 Error가 발생

가. Error 문구를 확인
나. 확인결과 토적시스템은 파일명이 19자를 초과하면 등록 할 수 없도록 처리되어있음', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-04-03-02', '장애', 'LSA', '토지적성평가', '토지적성평가 기능 오류', '실행 권한 부여', NULL, NULL, NULL, '1. 일부 기능에 오류가 발견됨
2. window 버전이 업데이트 됨에 따라 관리자 권한으로 변경하여 실행하여 완료', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-04-02-01', '장애', 'LSA', '토지적성평가', '토지적성평가 구동 오류', '메모리 오류', NULL, NULL, NULL, '· 토지적성평가 미 실행으로 인한 문의

가. 인터넷 연결확인
나. 라이선스 확인
다. 메모리 설정 확인
라. 확인결과 메모리 설정이 잘못되어 있는것을 확인하고 수정
마. 테스트를 진행', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-04-03-01', '장애', 'LSA', '토지적성평가', '토지적성평가 기능 오류', '데이터 처리 방식', NULL, NULL, NULL, '· 지도상에서 확인했을 때 평가대상지역 전체가 별도보전지역으로 판단되었으나 입안검토가 진행되는 현상이 발생함

가. 오류 원인 확인
· 문제가 발생하는 평가대상지역의 토지적성평가 확인서를 확인해본 결과 공적규제지역을 제외하고 평가된 구역면적이 0.00m2로 표시됨
· 확인서에 표시된 구역면적 데이터가 정확하다면 이 지역은 전체가 공적규제지역으로 평가가 진행되지 않아야 하는데 그럼에도 확인서가 발급된다는 것은 평가대상지역 필지에 문제가 있다는 것으로 판단할 수 있음
· 문제 필지를 클립을 통해 분리한 뒤 정보를 확인해 보면 1개의 필지에 여러 개의 필지정보가 포함되어 있는 것을 확인할 수 있는데 이것은 현 필지에 인접필지 일부가 중첩되어 있다는 것을 의미함
나. 오류 도출
· 위 분석을 통해 평가대상지역 일부 필지가 인접필지와 겹쳐져 있는 것을 확인하였으며 이러한 이유로 입안검토 대상지역이 아님에도 불구하고 입안검토가 진행되는 문제가 발생하는것을 확인함
다. 업무요청자에게 상황을 설명 드리고 상세 보고서를 작성함', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-04-01-01', '장애', 'LSA', '토지적성평가', '구역계 파일을 검토등록하면 Error 발생', '미상(구양식)', NULL, NULL, NULL, '구역계 파일명 수정 화면', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-03-05-01', '장애', 'UPIS', '연계', '울주군 새올민원 연계에러', '미상(구양식)', NULL, NULL, NULL, '1. UPIS 표준시스템에서 울주군 GPKI 900, 1507오류 발생 2. 인증서 확인 & 갱신유무 확인 3. SVR3730068001PBK.cer 갱신하여 GPKI, GPKISecureWEB 붙여넣기 4. UPIS 시스템 재기동 5. AP1 서버 확인 및 AP2서버에 동일하게 적용하여 완료', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-23-01', '장애', 'UPIS', '표준시스템', '표준시스템 재해취약성 오류', 'UPIS.xml 패치', NULL, NULL, NULL, '*
· 업무내용
1. 재해취약성 업로드는 완료되었으나 미표출
2. 확인 결과 UPIS.xml 세팅이 되어 있지 않으면 공간데이터도 없음
3. UPIS.xml 세팅 및 공간데이터 업로드 완료
4. 기존의 재해취약성 데이터 삭제
5. UPIS 8.0 재기동 및 재해취약성 업로드 완료
6. 표출 확인', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-19-03', '장애', 'UPIS', '표준시스템', '표준시스템 민원행정 연계 오류', 'TC_SAEALL_INFO 설정 오류', NULL, NULL, NULL, '* 내용 : UPIS 표준시스템 민원행정 연계 장애 처리
* 절차 :
1. 확인 : 원스톱민원허가과 부서 개편으로 인하여 부서코드 변경
2. 처리 :
· 1) 변경된 부서코드 수령받아 TC_SAEALL_INFO 테이블 코드값 변경하여 해결', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-19-04', '장애', 'UPIS', '표준시스템', '표준시스템 민원행정 연계 오류', 'TN_USER_NEW 설정 오류', NULL, NULL, NULL, '1. 울산시 주무관 아이디 접속시 민원행정연계에 에러가 발생됨
2. 해당 주무관의 유저테이블 확인결과 울산시로 세팅이 되어 있어 에러 발생
3. 울산시는 하위구의 시군구 코드를 입력해야함
4. 아이디 생성시 울산시로 생성했기때문에 해당 아이디에서 오류가 발생
5. 해당 주무관의 유저의 시군구 코드를 변경하여 문제해결', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-19-02', '장애', 'UPIS', '표준시스템', '표준시스템 민원행정 연계 오류', 'PBK 적용', NULL, NULL, NULL, '작업인원 - 담당자
1. 민원행정연계 오류
2. GPKI PBK값 도출 및 민원행정 표출 완료', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-18-01', '장애', 'UPIS', '표준시스템', '표준시스템 민원행정 연계', 'PBK오류', NULL, NULL, NULL, '11. 13 19:35 ~ 18.11.13 19:45

1. 김천시UPIS 새올민원 900에러
2. PBK.exe 설치
3. C:\GPKI\Certificate\class1 폴더안에 있는 공개키 인증서
(예시>SVR5340000004PBK.cer)을 실행하여 만료일자 확인
4. PBK.exe 실행 후 공개키 입력 후 C:\gpkiapi에 새로 나온 공개키파일 복사
5. C:\GPKI\Certificate\class1 , C:\gpkisecureweb\certs에 덮어씌우기(갱신)
6. 표준시스템 새올행정 확인
7. 정상작동 확인', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-16-01', '장애', 'UPIS', '표준시스템', '표준시스템 기타', '기타', NULL, NULL, NULL, '· AP서버에 비번이 맞지않아서 접속이 되지 않는 오류 발생

가. 담당주무관과 장비업체에 문의하여 비번 변경이 있었는지를 확인
나. 장비업체에 비번 초기화를 문의
· 장비관리 업체(튠시스템)에게 비번을 초기화 해달라고 하였음
· 9월 20일에 방문하여 초기화 해 주기로 하였음
다. 확인
· 장비관리 업체에서 확인해본결과 비번은 변경된 적이 없고 아이디가 변경된것을 확인하였음
· 아이디가 administrator -> ypupisap로 변경된겻을 확인함', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-03-11-01', '지원', 'UPIS', '엔진', '표준시스템 민원행정 연계 오류', '라이선스 오류', NULL, NULL, NULL, '· GPKI인증서 기간이 얼마 남지않아서 갱신함

가. GPKI.go.kr사이트에 접속후 GPKI인증서를 갱신
나. 공개키 인증서를 다시 갱신함
다. 표준시스템 재구동
라. 테스트 진행', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-03-10-01', '지원', 'UPIS', '엔진', '표준시스템 민원행정 연계', '라이선스 적용', NULL, NULL, NULL, '10. 10 16:00 ~ 18.10.10 17:00

1. 박정수차장님께 받은 밀양시 GPKI 라이선스 밀양시 서버에 적용
2. tc_saeall_info 테이블에서 암호 변경
3. 표준시스템 정상작동 확인', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-03-09-01', '지원', 'UPIS', '엔진', '전 지자체 엔진버전 조사', '미상(구양식)', NULL, NULL, NULL, '01. 02 ~ 18.01.05) - 전 지자체 Desktop CCI회사로 설치 되어있는 곳 확인 - GWS 설치파일 버전 확인 및 설치경로 조사 - GSS 설치 최신화 조사 설치파일 전수조사.xls 파일에 정리하여 기록 후 팀원 전체에게 메일로 발송', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-03-08-01', '지원', 'UPIS', '엔진', '완전 빈도형 작업', '미상(구양식)', NULL, NULL, NULL, '01. 02 ~ 18.01.05) - 완전 빈도형 모든 레이어 수집', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-02-02-01', '지원', 'UPIS', '기타', '도로명사업단 과업 요청 답변서 작성', '미상(구양식)', NULL, NULL, NULL, '답변서(ver1)', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-02-03-01', '지원', 'UPIS', '기타', '영월군 착수계', '미상(구양식)', NULL, NULL, NULL, '* 영월군 착수계 투입 인력 검토 및 서명.', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-01-02-01', '지원', 'PGMS', '공원녹지', '공원녹지 데이터', '데이터 백업', NULL, NULL, NULL, '· 평택 공원녹지 데이터를 다운로드 받아 달라고함

가. 평택 UPIS 원격에 접속
나. DB Tool을 이용하여 도형을 내려받음
· 공원녹지시스템과 표준시스템은 같은 DB서버를 사용하고 있음
다. 업무요청자에게 메일을 통해 데이터를 전달함', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-02-01-01', '지원', 'UPIS', '기타', 'DB 업데이트', '미상(구양식)', NULL, NULL, NULL, '1. Oracle Client 설치 2. DB 업데이트', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-01-01-01', '지원', 'PGMS', '공원녹지', '공원녹지', '공원녹지 데이터', NULL, NULL, NULL, '데이터 백업', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-06-01-01', '장애', 'UPIS', '환경', '환경 Jeus', '한글깨짐', NULL, NULL, NULL, '1. 관리자 페이지 & 시스템페이지에서 한글데이터 깨짐
2. 해당 OS 폰트를 확인하여 돋움, 바탕체 지도생성하여 업로드 후 재기동하였으나
동일한 에러가 발생됨
3. OS 엔지니어에게 해당 폰트가 OS에 적용이 안되는 경우도 있는지에 대해 문의
> root, jeus 계정에서 폰트캐시를 삭제하였으나 동일한 에러 발생
4. Jeus 엔진의 JEUSMain.xml 확인결과 실서버의 환경설정과 개발환경의 환경설정
부분이 틀려있음 실서버는 -Dfile.encoding=eucKR 적용되어있으나, 개발서버는
인코딩혁식이 지정되어 있지 않음. 그래서 인코딩 형식을 개발서버에 적용하였
으나 동일한 에러가 발생
5. JEUS 설정 부분의 추가적인 확인이 필요하다고 판단되어 엔지니어 방문시
해결예정
6. 18.02.01에 JEUS 엔지니어와 함께 방문하여 설정에 문제가 되는 부분(JEUS 옵션설정에 문제가 있었음)을 확인하였으며 수정을 통해 문제를 해결하였음(담당자 방문)', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-24-01', '장애', 'UPIS', '표준시스템', '표준시스템 전자결재 오류', '모듈 확인', NULL, NULL, NULL, '12. 13 13:50 ~ 18.12.13 14:20

1. 홍성군 전자결재 오류 요청사항 접수
2. 전자결재 연계 확인 매뉴얼을 통하여 test실시
3. 정상적으로 작동하지 않아 EBMS모듈 재기동 실시
4. 재기동하여도 여전히 해결되지 않음
5. EBMS모듈 삭제 후 재설치
6. 정상적으로 작동완료', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-03-01', '장애', 'UPIS', '표준시스템', 'UPIS 표준시스템 이력 미표출', '미상(구양식)', NULL, NULL, NULL, '* 울산광역시 해당 필지 이력 미표출하는 현상 - 확인 결과 특정 필지뿐만 아니라 모든 필지의 이력이 미표출되고있었으며, GSS / GWS 엔진이 정상적으로 실행되지 않고있었음. - 엔진 재기동 후 확인하여 해결함.', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-05-01', '장애', 'UPIS', '표준시스템', 'UPIS.xml 복구 요청', '미상(구양식)', NULL, NULL, NULL, '1. LOD 실행 중에 UPIS.xml 내용이 삭제됨 (다른 지자체 xml 파일 복사하여 내용 수정 후 복구)', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-04-01', '장애', 'UPIS', '표준시스템', 'UPIS.xml 교체', '미상(구양식)', NULL, NULL, NULL, '* UPIS.xml 변경 및 추가로 인하여, 지자체에 현재 적용되어 있는 xml의 교체 작업이 필요함. 원격가능지자체와 원격불가지자체를 구분하여 작업 착수하였음. 총 23개 지자체 중 원격불가지자체는 6군데이며, 그 외 17개 지자체는 원격 가능.', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-15-03', '장애', 'UPIS', '표준시스템', '표준시스템 구동 오류', '포트 허용', NULL, NULL, NULL, '작업인원 - 담당자
1. UPIS 표준시스템 정상작동 확인
2. 담당 주무관 자리에서 표준시스템 미표출 확인
· 전산계 UPIS AP서버에 대한 정책변경 확인 후 변경요청
3. 담당 주무관 자리에서 정상작동 확인', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-11-01', '장애', 'UPIS', '표준시스템', '진주시 UPIS 8.0 시스템 안켜짐', '미상(구양식)', NULL, NULL, NULL, '12. 29 11:00 - 17.12.29 11:30 시스템 이상으로 서비스에서 GWS, UPIS 8.0 내렸다고 하는데 들어가 보니 이상없고 재기동 및 중지 정상작동 확인', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-12-01', '장애', 'UPIS', '표준시스템', '표준시스템 LURIS 연계', 'LURIS 정식 연계', NULL, NULL, NULL, '패치진행(LURIS 테스트)', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-13-01', '장애', 'UPIS', '표준시스템', '표준시스템 LURIS 연계 오류', '키값 오류', NULL, NULL, NULL, '12. 13 17:00 ~ 18.12.13 17:40

1. 울산광역시 Luris 연계 오류 접수
2. Luris 연계 확인 매뉴얼로 test 진행
3. Luris 사업단에서 키값 발급
4. 적용 후 완료', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-07-01', '장애', 'UPIS', '표준시스템', '장수군 UPIS 8.0 시스템 안켜짐', '미상(구양식)', NULL, NULL, NULL, '12. 27 09:10 - 17.12.27 09:30 The web application [] registered the JDBC driver [oracle.jdbc.OracleDriver] but failed to unregister it when the web application was stopped. To prevent a memory leak, the JDBC Driver has been forcibly unregistered. UPIS 8.0 시스템 톰캣에 메모리 할당량이 적어 메모리 누수 현상이 발생하여 톰캣이 자동 중지 되었으며, 메모리 할당을 기존 1G', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-08-01', '장애', 'UPIS', '표준시스템', '재해취약성', '미상(구양식)', NULL, NULL, NULL, '1. 재해취약성 데이터 업로드시에 도형데이터가 올라가지 않는 에러가 나타난다고함 2. 도형 및 속성 업로드에 업로드 요청하였으나 에러가 발생 3. 재해취약성 데이터 구조에 대하여 확인 결과 모든 필드가 string 구조일때만 업로드 된다고 명시됨을 확인 4. 해당데이터 구조 확인시 등급을 확정하는 모든 필드가 double 구조로 되어있음 5. 해당 컬럼을 삭제한뒤 업로드시 정상업로드를 확인 6. 김정환 대리 & 이동수 차장에게 해당 문제에 대하여 보고 및 처리요청', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-09-01', '장애', 'UPIS', '표준시스템', '재해취약성 업로드 에러', '미상(구양식)', NULL, NULL, NULL, '1. 재해취약성 데이터 업로드시에 도형데이터가 올라가지 않는 에러가 나타난다고함 2. 도형 및 속성 업로드에 업로드 요청하였으나 에러가 발생 3. 재해취약성 데이터 구조에 대하여 확인 결과 모든 필드가 string 구조일때만 업로드 된다고 명시됨을 확인 4. 해당데이터 구조 확인시 등급을 확정하는 모든 필드가 double 구조로 되어있음 5. 해당 컬럼을 삭제한뒤 업로드시 정상업로드를 확인 6. 김정환 대리 & 이동수 차장에게 해당 문제에 대하여 보고 및 처리요청', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('1-05-10-01', '장애', 'UPIS', '표준시스템', '지구단위 계획 미표출 데스크탑 확인', '미상(구양식)', NULL, NULL, NULL, '1. UPIS 시스템에서 지구단위계획 구역 미표출 확인 2. 데스크탑 라이센스가 다르다는 에러가 확인 3. Mac Address 확인하였는데 변경됨을 확인 4. 데스크탑 재발급 및 적용하여 문제 해결 5. UPIS 시스템 상에서 신규XML 업로드하여 XML 변경 6. 서버 재기동 및 표준시스템 표출 확인', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-04-01', '지원', 'UPIS', '표준시스템', 'UPIS 표준시스템 및 GIS 엔진 점검', '미상(구양식)', NULL, NULL, NULL, '1. 표고도, 경사도 미표출(해결)', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-03-01', '지원', 'UPIS', '표준시스템', 'UPIS 표준시스템 Vworld 미표출', '미상(구양식)', NULL, NULL, NULL, '1. Vworld 미표출 (xml 수정하여 해결) 2. 유지보수 계약관계가 되어있지 않아 uwes 패치 진행', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-01-01', '지원', 'UPIS', '표준시스템', 'DB 업데이트', '미상(구양식)', NULL, NULL, NULL, '1. 집행도, 현황도, 이력도(나주, 담양제외) DB 업데이트 2. 신안군 속성데이터, 영암군 업데이트 3. LOD 실행', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-05-07-01', '지원', 'LSA', '토지적성평가', '토지적성평가 사용자 교육', '미상(구양식)', NULL, NULL, NULL, '1. 토지적성평가 사용자 교육 실시', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-05-03-01', '지원', 'LSA', '토지적성평가', '토적 납품용 제품 제작', '미상(구양식)', NULL, NULL, NULL, '* 담양 토지적성평가 납품용 CD 및 케이스 제작', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-04-01-01', '지원', 'UPIS', '연계', '전자결재 및 GPKI 세팅', '미상(구양식)', NULL, NULL, NULL, '1. 전자결재 세팅 완료 - 지자체 전자결재 서버 모듈 재기동 해야함', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-05-01-01', '지원', 'LSA', '토지적성평가', '구역계 Mapping 테스트 진행', '미상(구양식)', NULL, NULL, NULL, '파일 확인', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-17-01', '지원', 'UPIS', '표준시스템', '표준시스템 로그인 오류', 'Oracle 오류', NULL, NULL, NULL, '1. 업무일지
· 임석호계장님이 비밀번호를 잊어버려 초기화를 요청함

가. DB Tool을 통해 DB에 접속
나. User Table에서 비밀번호를 변경
· 기본 비번으로 변경함
다. 업무요청자 및 임석호 계장님에게 보고함', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-16-02', '지원', 'UPIS', '표준시스템', '표준시스템 데이터', '데이터 삭제', NULL, NULL, NULL, '1. 사용자 교육시 사용했던 test 계정 삭제 요청 (1~30)
2. sysadmin1 계정 접속하여 관리자 탭에서 사용자 삭제', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-14-01', '지원', 'UPIS', '표준시스템', '표준시스템 구동 오류', '표준시스템 Tomcat 오류', NULL, NULL, NULL, '· 표준시스템이 미 구동되는 에러가 발생되어 확인을 요청함

가. Tomcat7.0을 중지함
나. 정상적으로 중지되지 않아서 프로세스에서 강제 종료함
다. 다시 실행함
라. Tomcat7.0이 다운되어 발생한 문제로 확인하고 업무요청자에게 보고함', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-05-01', '지원', 'UPIS', '표준시스템', 'UPIS 표준시스템 및 엔진 점검', '미상(구양식)', NULL, NULL, NULL, '1. 정기점검 실시 (완료) 2. GSS로그 예전 데이터 삭제 3. 재해취약성 데이터 구축 되어있음', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-08-01', '지원', 'UPIS', '표준시스템', '연혁도 수정 요청', '미상(구양식)', NULL, NULL, NULL, '1. 데이터 업로드 - 연혁도 추가 (xml 수정) 2. LOD 실행', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-09-01', '지원', 'UPIS', '표준시스템', '작업시간 (09:30 ~ 10:10) 1. 계약 2.', '미상(구양식)', NULL, NULL, NULL, '1. 계약 2. 정기점검 실시 - store 정리 - log파일 17년 12월만 남겨놓고 삭제', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-11-01', '지원', 'UPIS', '표준시스템', '지도상에서 Layer가 어긋나는 문제', '미상(구양식)', NULL, NULL, NULL, '두 Layer 맵핑 확인', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-12-01', '지원', 'UPIS', '표준시스템', '착수계 작성', '미상(구양식)', NULL, NULL, NULL, '대외공문신청서 상신', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-07-01', '지원', 'UPIS', '표준시스템', '사업관련 서류 준비 및 우편발송', '미상(구양식)', NULL, NULL, NULL, '신원진술서(약식)작성', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-05-04-01', '지원', 'LSA', '토지적성평가', '토지적성평가 고흥군 Test', '미상(구양식)', NULL, NULL, NULL, '토지적성평가 고흥군 Test - 18.01.08 14:30분경에 토지적성평가 활용매뉴얼을 참고하여 Test 시작 - 18.01.08 17:00분경에 데이터 이상으로인하여 고흥군 데이터로 변경 - 18.01.09 08:10분경에 내 데스크탑에 토지적성평가 라이센스 발급 후 설치 - 18.01.09 14:00분경에 분석 마침, 노트북으로 옮기는 과정에서 데이터 손실 발생 - 18.01.10 14:00분경에 다시 노트북에서 분석 마친 후 컨버터 실행까지 완료', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-05-02-01', '지원', 'LSA', '토지적성평가', '보전지역에서 입안검토가 진행되는 문제', '미상(구양식)', NULL, NULL, NULL, '토지적성평가 확인서 확인', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-18-07', '지원', 'UPIS', '표준시스템', '표준시스템 문서 작성', '영월군 착수계', NULL, NULL, NULL, '1. 영월군 착수계 투입 인력 검토 및 서명', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-18-06', '지원', 'UPIS', '표준시스템', '표준시스템 문서 작성', '수직자료 연계 확인', NULL, NULL, NULL, '· 수직자료 연계 폴더에 생성이 되지 않아 모듈 구동확인 요청
1. 해당 스크린샷 첨부하여 사업단 문의

18. 10.30
1. 사업단 문제해결', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-18-02', '지원', 'UPIS', '표준시스템', '표준시스템 문서 작성', 'UPIS 8.0 톰캣 재구동 매뉴얼', NULL, NULL, NULL, '1. UPIS 8.0 톰캣 매뉴얼 재구동 절차서 작성 (문서작성)
2. 담당자에게 전달', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-17-02', '지원', 'UPIS', '표준시스템', '표준시스템 로그인 오류', '데이터 오류', NULL, NULL, NULL, '* 내용 : 논산시 UPIS AP/DB서버 공공데이터 보유현황 스크립트 실행 및 문서 작성
* 절차 :
1. 확인 : TN_USER_NEW 테이블 해당 아이디 락온오프 여부 확인
2. 처리 :
· 1) 스크립트 실행
· 2) 공공데이터 보유현황 문서 작성(데이터베이스 정의서, 컬럼 정의서 등)', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-20-01', '지원', 'UPIS', '표준시스템', '표준시스템 민원행정 연계 오류', 'PBK 오류', NULL, NULL, NULL, '· 주무관이 GPKI 인증서 갱신을 요청함

가. 전산과 김현경 주무관님에게 갱신을 요청해서 해결함
나. 완료후 업무요청자에게 내용을 보고함', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-18-10', '지원', 'UPIS', '표준시스템', '표준시스템 문서 작성', '표준시스템 기능 오류', NULL, NULL, NULL, '*
· 표준시스템 기능구현 관련하여 사업단 문의 요청
1. 표준시스템 관리자 로그인 후 레이어 저장시에 관리자로 로그인 하라고 뜸
2. 관리자에서 자료관리(공간데이터 업/다운로드 시 오류)
3. DB백업관리 에러 확인
4. 사업단에 해당내용 문의 상세내용 및 스크린첨부 문서 전달

18. 11.01
1. 해당내용 문제 해결 문서 전달 받음
2. 담당자에게 해결요청 함 (문제가 있음)', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-18-09', '지원', 'UPIS', '표준시스템', '표준시스템 문서 작성', '클라우드 컴퓨터 수요조사서', NULL, NULL, NULL, '· 클라우드 컴퓨터 수요조사서를 작성 요청

가. 문서작성
· 서버 장비의 사양을 정리함
· 화성 UPIS표준시스템 장비에는 특이사항이 있어 수요조사서 아래 따로 정리함(백업장비, DB서버는 통합관리 대상 등)
나. 문서작성 완료후 업무요청자에게 메일로 발송', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-18-01', '지원', 'UPIS', '표준시스템', '표준시스템 문서 작성', 'LURIS 연계 키 확인', NULL, NULL, NULL, '문서 작성', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-18-03', '지원', 'UPIS', '표준시스템', '표준시스템 문서 작성', 'UPIS 포트허용 신청서', NULL, NULL, NULL, '* 내용 : 태백시 UPIS 포트허용 문서 작성
* 절차 :
1. 확인 : 포트허용 문서 작성
2. 처리 :
· 1) 기존 UPIS 도입시 문서를 바탕으로 포트허용 문서 작성하여 전달', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-18-05', '지원', 'UPIS', '표준시스템', '표준시스템 문서 작성', '데이터베이스 정의서', NULL, NULL, NULL, '* 내용 : 문서 대리 작성
* 절차 :
1. 확인 : 라이선스 발급 내용 확인
2. 처리 :
· 1) 데이터베이스 정의서 엔진 및 서버 부문 작성하여 리턴', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-21-01', '지원', 'UPIS', '표준시스템', '표준시스템 수직적자료교환 연계', '모듈 확인', NULL, NULL, NULL, '* 내용 : 충북UPIS도시스템 수직자료연계에 따른 지자체 및 도청 데이터 취합 현황 파악
* 절차 :
1. 확인 : 사업단 문의 유도
2. 처리 :
· 1) 수직모듈 미구동 지자체 사업단에 문의토록 주무관님에게 유도', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-21-02', '지원', 'UPIS', '표준시스템', '표준시스템 수직적자료교환 연계', '포트 확인', NULL, NULL, NULL, '사업단에서 전달 받은 내용
1. 합천, 창년은 국토부에서 원격이 가능하지만 의령은 원격이 되지 않고 있음
2. 창녕 AP서버에서 oracle에 연결을 할수 없다고 함
3. 합천군 -> 도청으로는 연결이 되지 않아 Port를 확인해서 조치를 요청 (80,7070,8080)

1. 의령군은 AP 서버 재기동을 요청함 (박정수 차장>유지보수 업체 연락)
2. 창녕군은 민대연 사업단 담당자가 직접 연락을 취해본다고 함 (목요일)
3. 합천군은 공문요청에 UPIS IP, KRAS IP가 잘못기입되어 있어 박정수 차장에게 수정 후 요청', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-22-03', '지원', 'UPIS', '표준시스템', '표준시스템 수직적자료교환 오류', '모듈 확인', NULL, NULL, NULL, '* 내용 : UPIS 도시스템 수직자료교환모듈 및 데이터 정시성 검증
* 절차 :
1. 확인 : 데이터 검증
2. 처리 :
· 1) UPIS 도시스템 수직자료교환모듈 및 데이터 정시성 검증
· 2) 자료에 대한 피드백 정리 및 수정', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-22-01', '지원', 'UPIS', '표준시스템', '표준시스템 수직적자료교환 오류', 'uwes 패치', NULL, NULL, NULL, '* 내용 : uwes 패치 요청
* 절차 :
1. 확인 : uwes 패치
2. 처리 :
· 1) uwes 패치 완료', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-22-02', '지원', 'UPIS', '표준시스템', '표준시스템 수직적자료교환 오류', '데이터 오류', NULL, NULL, NULL, '· 업무요청사항(세부)

· 충북도시스템 수직적자료연계에 따른 시스템 장애 및 데이터 현황 파악

· 업무내용

1. 청주시 용도지역 미표출
2. 단양군 좌표계 에러(데이터 구축)
3. 증평군 좌표계 에러(데이터 구축)
4. 기타 지자체 데이터 타입에 따른 수직적자료교환체계 시 에러 발생
5. 현황 파악 완료 및 문서 작성하여 최승환 주무관에게 메일 송부', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-26-01', '지원', 'UPIS', '표준시스템', '표준시스템 조회 오류', '데이터 오류', NULL, NULL, NULL, '1. 고시정보의 지도 하이라이팅 기능이 되지 않음
2. TN_UBPLFC_WTNNC 테이블의 결정조사가 PMC0002 일 경우에
FEATURE_EXIST_YN 컬럼값이 누락이 되면 미표출됨
3. 해당값을 입력 하여 문제해결
4. 표준시스템에서 고시정보 지도 하이라이팅 기능 확인결과 정상표출 확인', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-26-02', '지원', 'UPIS', '표준시스템', '표준시스템 조회 오류', '데이터 유실', NULL, NULL, NULL, '· 고시문이 미표출되는 문제 발생

가. 표준시스템에 접속하여 고시문 미출력 문제를 확인함
나. 고시문 출력은 문제가 없음
다. 확인후 업무요청자에게 보고함', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;
INSERT INTO tb_ops_kb (kb_code, gubun, sys_type, category, symptom, cause, summary, symptom_desc, cause_desc, action, prevention, keywords, case_count, rewritten) VALUES ('2-06-27-03', '지원', 'UPIS', '표준시스템', '표준시스템 지도 오류', '표준시스템 tomcat 오류', NULL, NULL, NULL, '* 내용 : UPIS표준시스템 사용 에러
* 절차 :
1. 확인 : 봉화군 UPIS표준시스템(tomcat엔진) 기동 장애로 인한 에러
2. 처리 :
1. Apache Tomcat 8.0 UPIS 기동 및 로그 확인
2. Apache Tomcat 8.0 UPIS 기동 실시
3. 표준시스템 기능 전반적 검토 후 해결', NULL, NULL, 1, FALSE) ON CONFLICT (kb_code) DO UPDATE SET sys_type=EXCLUDED.sys_type;

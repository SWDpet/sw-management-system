#!/bin/sh
# ── JSON 빌더 (jq 없이) ─────────────────────────────────────────────
# POSIX sh 호환. 임시 변수에 누적해 build → 마지막에 echo.
# 모든 함수가 stdout 으로 fragment 를 출력하므로 호출자에서 합성.

if [ -n "${__UPIS_JSON_SH__:-}" ]; then return 0 2>/dev/null; fi
__UPIS_JSON_SH__=1

# 문자열을 JSON 문자열 리터럴로 escape. (")(\)/제어문자.
# usage: json_str "hello \"world\""
json_str() {
    # printf %s 로 입력 받고, sed 로 escape. NUL/제어문자는 단순 제거.
    printf '%s' "$1" | awk '
        BEGIN { ORS="" }
        {
            s = $0
            gsub(/\\/, "\\\\", s)
            gsub(/"/, "\\\"", s)
            gsub(/\t/, "\\t", s)
            gsub(/\r/, "\\r", s)
            # 8비트는 그대로 (UTF-8 통과). 0x00-0x1F 중 위에서 처리 안한 것만 제거.
            print s
            if (! eof) printf("\\n")
            eof = 1
        }
        END { }
    ' | sed 's/\\n$//'
    # 마지막 \n 제거 (printf 가 newline 안 넣었으므로 awk 가 한 줄만 처리하면 OK).
    # multi-line 입력이면 위 awk 의 \\n 가 정상.
}

# 단일 라인 escape. perl 이 있으면 invalid UTF-8 시퀀스 제거 (Windows CP949 등 방어).
# perl 은 AIX/HP-UX/Linux/Solaris 전부 표준 탑재.
json_str_oneline() {
    if [ -n "${UPIS_HAS_PERL:-}" ]; then
        printf '%s' "$1" | perl -e '
            use Encode qw(decode encode FB_DEFAULT);
            binmode STDIN; binmode STDOUT;
            local $/; my $raw = <STDIN>;
            my $clean = decode("UTF-8", $raw, FB_DEFAULT);
            $clean =~ s/\x{FFFD}//g;
            print encode("UTF-8", $clean);
        ' 2>/dev/null | sed -e 's/\\/\\\\/g' -e 's/"/\\"/g' -e 's/	/\\t/g' -e 's/\r/\\r/g' | tr -d '\n'
    else
        printf '%s' "$1" | sed -e 's/\\/\\\\/g' -e 's/"/\\"/g' -e 's/	/\\t/g' -e 's/\r/\\r/g' | tr -d '\n'
    fi
}

# 따옴표로 감싼 문자열 리터럴.
json_quote() {
    printf '"%s"' "$(json_str_oneline "$1")"
}

# null 또는 빈 문자열 → "null", 그 외 → quoted string
json_quote_or_null() {
    if [ -z "$1" ]; then
        printf 'null'
    else
        json_quote "$1"
    fi
}

# 숫자 검증. 아니면 null 출력.
json_num_or_null() {
    case "$1" in
        ''|*[!0-9.\-]*) printf 'null' ;;
        *)              printf '%s' "$1" ;;
    esac
}

# Boolean. true/false/그외 → null
json_bool_or_null() {
    case "$1" in
        true|1|yes|on)  printf 'true' ;;
        false|0|no|off) printf 'false' ;;
        *)              printf 'null' ;;
    esac
}

# ── 객체/배열 빌더 ─────────────────────────────────────────────────
# 사용 패턴:
#   obj=$(json_obj_start)
#   obj=$(json_obj_add "$obj" "key" "$(json_quote val)")
#   obj=$(json_obj_add "$obj" "n"   "$(json_num_or_null 42)")
#   obj=$(json_obj_end "$obj")
#
# 내부 표현: 시작 "{", 끝 "}". 빈 객체는 "{}" 그대로.
# 키 추가 시 직전 문자가 '{' 가 아니면 ',' 를 먼저 붙임.
json_obj_start() { printf '{'; }
json_obj_end()   { printf '%s}' "$1"; }

# stdin 으로 누적 문자열 받고, key/value 한 쌍 추가한 새 문자열을 stdout 으로.
# usage: NEW=$(json_obj_add "$ACC" "$KEY" "$VALUE_LITERAL")
#   $VALUE_LITERAL 은 이미 JSON 으로 인코딩된 fragment 여야 함 (json_quote / json_num 등).
json_obj_add() {
    _acc=$1; _k=$2; _v=$3
    _last=$(printf '%s' "$_acc" | tail -c 1)
    if [ "$_last" = "{" ]; then
        printf '%s"%s":%s' "$_acc" "$_k" "$_v"
    else
        printf '%s,"%s":%s' "$_acc" "$_k" "$_v"
    fi
}

# 배열 빌더
json_arr_start() { printf '['; }
json_arr_end()   { printf '%s]' "$1"; }
json_arr_add() {
    _acc=$1; _v=$2
    _last=$(printf '%s' "$_acc" | tail -c 1)
    if [ "$_last" = "[" ]; then
        printf '%s%s' "$_acc" "$_v"
    else
        printf '%s,%s' "$_acc" "$_v"
    fi
}

# ── JSON 파일 쓰기 ─────────────────────────────────────────────────
json_write() {
    _path=$1; _content=$2
    _dir=$(dirname "$_path")
    [ -d "$_dir" ] || mkdir -p "$_dir"
    printf '%s' "$_content" > "$_path"
}

# ── 단순 JSON 읽기 (flat key) ──────────────────────────────────────
# 평탄한 "key":"value" 구조에서 값 추출. 중첩 객체는 미지원.
# usage: json_get_str CONFIG.json key
json_get_str() {
    _file=$1; _key=$2
    sed -n 's/.*"'"$_key"'"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' "$_file" | head -1
}

# 평탄한 "key":number 구조에서 숫자값 추출.
json_get_num() {
    _file=$1; _key=$2
    sed -n 's/.*"'"$_key"'"[[:space:]]*:[[:space:]]*\(-\{0,1\}[0-9][0-9.]*\).*/\1/p' "$_file" | head -1
}

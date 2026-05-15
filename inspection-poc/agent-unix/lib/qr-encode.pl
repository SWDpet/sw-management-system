#!/usr/bin/env perl
# ─────────────────────────────────────────────────────────────────────
# qr-encode.pl — snapshot.json → frames.json
#   - 축약 qr-payload (id, status, value tuple)
#   - gzip + base45 (RFC 9285) + 1800 char chunk + CRC32 + SHA-1 prefix6
#   - PWA decoder.mjs / decoder/decode.js 와 동일 contract
#
# 사용법:
#   perl qr-encode.pl <snapshot.json> <frames.json>
# ─────────────────────────────────────────────────────────────────────
use strict;
use warnings;
use IO::Compress::Gzip qw(gzip $GzipError);
use Digest::SHA qw(sha1_hex);
use Compress::Zlib qw(crc32);

my $snap_path   = $ARGV[0] or die "usage: $0 <snapshot.json> <frames.json>\n";
my $frames_path = $ARGV[1] or die "usage: $0 <snapshot.json> <frames.json>\n";

# ── snapshot.json 로드 (정규식 기반 평탄 파싱) ──
open my $fh, '<:raw', $snap_path or die "snapshot open: $!";
local $/;
my $raw = <$fh>;
close $fh;

my %f = ($raw =~ /"(site|round|tier|inspector)"\s*:\s*"([^"]*)"/g);
my ($host) = ($raw =~ /"host"\s*:\s*\{[^}]*?"hostname"\s*:\s*"([^"]*)"/);

my @items;
while ($raw =~ /\{"id":"([^"]+)"[^}]*"status":"([^"]+)"[^}]*"value":([^,]+|null|\{[^}]*\}|\[[^\]]*\])/g) {
    my ($id, $st, $val) = ($1, $2, $3);
    my $vnum = 'null';
    if ($val =~ /"used_pct"\s*:\s*([0-9.\-]+)/) {
        $vnum = $1;
    } elsif ($val =~ /"count"\s*:\s*([0-9\-]+)/) {
        $vnum = $1;
    } elsif ($val =~ /^([0-9.\-]+)$/) {
        $vnum = $1;
    }
    push @items, [$id, $st, $vnum];
}

my $id_field = ($f{site} // 'unk') . '-' . ($f{round} // '00') . '-' . ($f{tier} // 'x');

# JSON string escape
sub jstr {
    my $s = $_[0];
    $s =~ s/\\/\\\\/g;
    $s =~ s/"/\\"/g;
    return '"' . $s . '"';
}

my $arr = '[' . join(',', map {
    '[' . jstr($_->[0]) . ',' . jstr($_->[1]) . ',' . $_->[2] . ']'
} @items) . ']';

my $ts = time;
my $payload_json =
      '{"s":"snapshot/qr1","id":' . jstr($id_field)
    . ',"site":'      . jstr($f{site}      // '')
    . ',"round":'     . jstr($f{round}     // '')
    . ',"tier":'      . jstr($f{tier}      // '')
    . ',"host":'      . jstr($host         // '')
    . ',"ts":'        . $ts
    . ',"inspector":' . jstr($f{inspector} // '')
    . ',"i":'         . $arr . '}';

my $raw_bytes = length($payload_json);

# ── gzip ──
my $gz_data;
gzip \$payload_json => \$gz_data, -Level => 9
    or die "gzip: $GzipError";
my $gz_bytes = length($gz_data);

# ── base45 (RFC 9285) ──
my $b45_charset = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:';
sub b45_encode {
    my @b = unpack 'C*', $_[0];
    my $out = '';
    my $i = 0;
    while ($i < @b) {
        if ($i + 1 < @b) {
            my $n = ($b[$i] << 8) | $b[$i + 1];
            my $e = $n % 45; $n = int($n / 45);
            my $d = $n % 45; $n = int($n / 45);
            my $c = $n % 45;
            $out .= substr($b45_charset, $e, 1)
                  . substr($b45_charset, $d, 1)
                  . substr($b45_charset, $c, 1);
            $i += 2;
        } else {
            my $n  = $b[$i];
            my $bb = $n % 45;
            my $aa = int($n / 45);
            $out .= substr($b45_charset, $bb, 1)
                  . substr($b45_charset, $aa, 1);
            $i += 1;
        }
    }
    return $out;
}
my $b45 = b45_encode($gz_data);
my $b45_chars = length($b45);
my $sha = substr(sha1_hex($gz_data), 0, 6);

# ── chunk (max 1800 chars) ──
my $max = 1800;
my @chunks;
for (my $i = 0; $i < length($b45); $i += $max) {
    push @chunks, substr($b45, $i, $max);
}
my $total = scalar(@chunks);

# ── header + data frames ──
my @frames;
push @frames,
      '{"v":1,"id":'      . jstr($id_field)
    . ',"seq":0,"total":' . $total
    . ',"site":'          . jstr($f{site}  // '')
    . ',"round":'         . jstr($f{round} // '')
    . ',"tier":'          . jstr($f{tier}  // '')
    . ',"host":'          . jstr($host     // '')
    . ',"ts":'            . $ts
    . ',"hash":'          . jstr($sha)
    . ',"raw_bytes":'     . $raw_bytes
    . ',"gz_bytes":'      . $gz_bytes
    . ',"b45_chars":'     . $b45_chars . '}';

my $idx = 1;
for my $c (@chunks) {
    my $crc = crc32($c) & 0xFFFF;
    my $chk = sprintf('%04x', $crc);
    push @frames,
          '{"v":1,"id":'      . jstr($id_field)
        . ',"seq":'           . $idx
        . ',"total":'         . $total
        . ',"chk":'           . jstr($chk)
        . ',"d":'             . jstr($c) . '}';
    $idx++;
}

open my $out, '>:raw', $frames_path or die "frames open: $!";
print $out '[' . join(',', @frames) . ']';
close $out;

printf STDERR "[encoder] payload=%d B  gz=%d B  b45=%d chars  frames=%d  sha=%s\n",
    $raw_bytes, $gz_bytes, $b45_chars, scalar(@frames), $sha;

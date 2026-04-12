import olefile, zlib, struct, sys, os

def extract_hwp_text(filepath):
    try:
        ole = olefile.OleFileIO(filepath)
        header = ole.openstream('FileHeader').read()
        is_compressed = header[36] & 1
        texts = []
        for i in range(256):
            name = f'BodyText/Section{i}'
            if not ole.exists(name):
                break
            data = ole.openstream(name).read()
            if is_compressed:
                try:
                    data = zlib.decompress(data, -15)
                except:
                    pass
            text = ''
            pos = 0
            while pos < len(data):
                if pos + 4 > len(data):
                    break
                header_val = struct.unpack_from('<I', data, pos)[0]
                tag_id = header_val & 0x3FF
                size = (header_val >> 20) & 0xFFF
                if size == 0xFFF:
                    if pos + 8 > len(data):
                        break
                    size = struct.unpack_from('<I', data, pos + 4)[0]
                    pos += 8
                else:
                    pos += 4
                if tag_id == 67:
                    j = 0
                    para_text = ''
                    while j < size:
                        if pos + j + 2 > len(data):
                            break
                        ch = struct.unpack_from('<H', data, pos + j)[0]
                        j += 2
                        if ch < 32:
                            if ch in (1, 2, 3, 11, 12, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23):
                                j += 14
                            elif ch == 9:
                                para_text += '\t'
                            elif ch == 24:
                                j += 14
                            elif ch == 30:
                                para_text += '\n'
                            elif ch == 31:
                                para_text += '-'
                        else:
                            para_text += chr(ch)
                    if para_text.strip():
                        text += para_text + '\n'
                pos += size
            texts.append(text)
        ole.close()
        return '\n'.join(texts)
    except Exception as e:
        return f'Error: {str(e)}'

if __name__ == '__main__':
    sys.stdout.reconfigure(encoding='utf-8')
    filepath = sys.argv[1]
    output_path = sys.argv[2] if len(sys.argv) > 2 else None
    result = extract_hwp_text(filepath)
    if output_path:
        with open(output_path, 'w', encoding='utf-8') as f:
            f.write(result)
        print(f"Saved to {output_path}")
    else:
        print(result)

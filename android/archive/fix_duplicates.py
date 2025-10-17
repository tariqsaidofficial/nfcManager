#!/usr/bin/env python3
import re
import sys
from pathlib import Path

def remove_duplicates(file_path):
    """Remove duplicate string entries, keeping the first occurrence"""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Find all string entries
    pattern = r'<string name="([^"]+)"[^>]*>.*?</string>'
    matches = list(re.finditer(pattern, content, re.DOTALL))
    
    seen = {}
    duplicates = []
    
    for match in matches:
        name = match.group(1)
        if name in seen:
            # Mark as duplicate
            duplicates.append((match.start(), match.end(), name))
        else:
            seen[name] = match.start()
    
    # Remove duplicates from end to start to preserve positions
    if duplicates:
        print(f"Found {len(duplicates)} duplicates in {file_path.name}:")
        for _, _, name in duplicates:
            print(f"  - {name}")
        
        # Remove duplicates
        new_content = content
        for start, end, name in reversed(duplicates):
            # Find the line containing this duplicate
            line_start = content.rfind('\n', 0, start) + 1
            line_end = content.find('\n', end) + 1
            if line_end == 0:
                line_end = len(content)
            new_content = new_content[:line_start] + new_content[line_end:]
        
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(new_content)
        
        return len(duplicates)
    return 0

# Process all language files
res_dir = Path('app/src/main/res')
total_removed = 0

for lang_dir in ['values-es', 'values-de', 'values-ru', 'values-hi', 'values-fil', 'values-zh-rCN']:
    strings_file = res_dir / lang_dir / 'strings.xml'
    if strings_file.exists():
        removed = remove_duplicates(strings_file)
        total_removed += removed
        if removed > 0:
            print(f"✅ Fixed {strings_file}")
        print()

print(f"\n🎉 Total duplicates removed: {total_removed}")

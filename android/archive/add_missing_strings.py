#!/usr/bin/env python3
import re

# Filipino missing strings
fil_missing = {
    "language_chinese_mandarin": "Chinese (Mandarin)",
    "language_filipino": "Filipino",
    "language_hindi": "Hindi",
    "language_russian": "Russian"
}

# Chinese missing strings
zh_missing = {
    "interval_60_seconds": "60 秒",
    "interval_seconds_format": "%1$d 秒",
    "settings_cd_navigate": "導航",
    "settings_item_reminder_interval_current_label": "當前：%1$ds",
    "settings_item_reminder_interval_subtitle": "多久提醒一次 NFC 隱私",
    "settings_item_reminder_interval_title": "提醒間隔",
    "settings_section_language_region_title": "語言和地區"
}

def add_strings_before_closing(file_path, strings_dict):
    """Add missing strings before </resources>"""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Find </resources>
    closing_tag = '</resources>'
    pos = content.rfind(closing_tag)
    
    if pos == -1:
        print(f"Error: No closing tag found in {file_path}")
        return
    
    # Build new strings
    new_strings = "\n    <!-- Missing Strings -->\n"
    for key, value in strings_dict.items():
        new_strings += f'    <string name="{key}">{value}</string>\n'
    new_strings += "\n"
    
    # Insert before </resources>
    new_content = content[:pos] + new_strings + content[pos:]
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(new_content)
    
    print(f"✅ Added {len(strings_dict)} strings to {file_path}")

# Add to Filipino
add_strings_before_closing('app/src/main/res/values-fil/strings.xml', fil_missing)

# Add to Chinese
add_strings_before_closing('app/src/main/res/values-zh-rCN/strings.xml', zh_missing)

print("\n🎉 All missing strings added!")

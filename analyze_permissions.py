#!/usr/bin/env python3
"""
Permission Usage Analyzer for PDCMS Backend
Scans all controllers to identify:
1. Which permissions are USED vs UNUSED
2. Permission usage frequency
3. Controllers using each permission
"""

import re
import os
from collections import defaultdict
from pathlib import Path

# Extract all permissions from seed data
def extract_seed_permissions(seed_file):
    """Extract all permission_id from seed data SQL"""
    permissions = set()
    with open(seed_file, 'r', encoding='utf-8') as f:
        content = f.read()
        # Match pattern: ('PERMISSION_NAME', '...
        pattern = r"\('([A-Z_]+)',\s*'[A-Z_]+',\s*'[A-Z_]+"
        matches = re.findall(pattern, content)
        permissions.update(matches)
    return permissions

# Extract permissions used in controllers
def extract_controller_permissions(src_dir):
    """Scan all *Controller.java files for @PreAuthorize permissions"""
    usage_map = defaultdict(list)  # permission -> list of (controller, line_number)

    controller_files = list(Path(src_dir).rglob('*Controller.java'))

    for controller_file in controller_files:
        controller_name = controller_file.stem
        with open(controller_file, 'r', encoding='utf-8') as f:
            lines = f.readlines()
            for line_num, line in enumerate(lines, 1):
                if '@PreAuthorize' in line:
                    # Extract permissions from hasAuthority('X') or hasAnyAuthority('X', 'Y')
                    # Match patterns like: hasAuthority('PERMISSION') or hasAnyAuthority('PERM1', 'PERM2')
                    auth_patterns = [
                        r"hasAuthority\('([A-Z_]+)'\)",
                        r"hasAnyAuthority\('([A-Z_]+)'(?:,\s*'[A-Z_]+')*\)",
                        r"hasRole\('(ROLE_[A-Z_]+)'\)",
                    ]

                    for pattern in auth_patterns:
                        matches = re.findall(pattern, line)
                        for perm in matches:
                            usage_map[perm].append((controller_name, line_num))

                    # Also extract all quoted uppercase words (permissions)
                    all_perms = re.findall(r"'([A-Z_]+)'", line)
                    for perm in all_perms:
                        if perm not in ['OR', 'AND']:  # Skip SQL keywords
                            usage_map[perm].append((controller_name, line_num))

    return usage_map

def main():
    # Paths
    seed_file = 'src/main/resources/db/dental-clinic-seed-data.sql'
    src_dir = 'src/main/java'

    print("=" * 80)
    print("PERMISSION USAGE ANALYSIS")
    print("=" * 80)

    # 1. Get all permissions from seed data
    seed_permissions = extract_seed_permissions(seed_file)
    print(f"\n📊 Total permissions in seed data: {len(seed_permissions)}")

    # 2. Get all permissions used in controllers
    usage_map = extract_controller_permissions(src_dir)
    used_permissions = set(usage_map.keys())

    # Filter out roles (ROLE_*, ADMIN, MANAGER, etc.) to focus on permissions
    permission_usage = {k: v for k, v in usage_map.items()
                        if not k.startswith('ROLE_') and k not in ['ADMIN', 'MANAGER', 'RECEPTIONIST', 'PATIENT']}

    print(f"📊 Total permissions used in controllers: {len(permission_usage)}")

    # 3. Find UNUSED permissions
    unused_permissions = seed_permissions - set(permission_usage.keys())

    print(f"\n❌ UNUSED Permissions ({len(unused_permissions)}):")
    print("=" * 80)
    for perm in sorted(unused_permissions):
        print(f"  • {perm}")

    # 4. Top 20 most used permissions
    print(f"\n🔥 TOP 20 Most Used Permissions:")
    print("=" * 80)
    sorted_perms = sorted(permission_usage.items(), key=lambda x: len(x[1]), reverse=True)
    for perm, usages in sorted_perms[:20]:
        print(f"  {perm:40} → Used {len(usages):3} times")

    # 5. Permissions used only ONCE (candidates for removal/merge)
    print(f"\n⚠️  Permissions Used Only ONCE (Candidates for Review):")
    print("=" * 80)
    single_use = [(perm, usages) for perm, usages in sorted_perms if len(usages) == 1]
    for perm, usages in single_use:
        controller, line = usages[0]
        print(f"  • {perm:40} in {controller}.java:{line}")

    # 6. Summary statistics
    print(f"\n📈 SUMMARY:")
    print("=" * 80)
    print(f"  Total permissions defined:     {len(seed_permissions):4}")
    print(f"  Permissions actually used:     {len(permission_usage):4}")
    print(f"  Unused permissions:            {len(unused_permissions):4} ({len(unused_permissions)*100//len(seed_permissions)}%)")
    print(f"  Permissions used only once:    {len(single_use):4}")
    print("=" * 80)

    # 7. Write detailed report to file
    with open('docs/PERMISSION_USAGE_REPORT.md', 'w', encoding='utf-8') as f:
        f.write("# Permission Usage Report\n\n")
        f.write(f"**Generated**: {os.popen('date').read().strip()}\n\n")

        f.write("## Summary\n\n")
        f.write(f"- **Total Permissions Defined**: {len(seed_permissions)}\n")
        f.write(f"- **Permissions Used**: {len(permission_usage)}\n")
        f.write(f"- **Unused Permissions**: {len(unused_permissions)} ({len(unused_permissions)*100//len(seed_permissions)}%)\n\n")

        f.write("## Unused Permissions\n\n")
        for perm in sorted(unused_permissions):
            f.write(f"- `{perm}`\n")

        f.write("\n## Permission Usage Details\n\n")
        for perm, usages in sorted(permission_usage.items()):
            f.write(f"### `{perm}` ({len(usages)} usages)\n\n")
            for controller, line in usages:
                f.write(f"- {controller}.java:{line}\n")
            f.write("\n")

    print("\n✅ Detailed report saved to: docs/PERMISSION_USAGE_REPORT.md")

if __name__ == '__main__':
    main()

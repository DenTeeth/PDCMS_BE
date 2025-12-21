#!/bin/bash

echo "# Permission Usage Report" > docs/PERMISSION_USAGE_REPORT.md
echo "" >> docs/PERMISSION_USAGE_REPORT.md
echo "**Generated**: $(date)" >> docs/PERMISSION_USAGE_REPORT.md
echo "" >> docs/PERMISSION_USAGE_REPORT.md

echo "## Permissions Used in Controllers" >> docs/PERMISSION_USAGE_REPORT.md
echo "" >> docs/PERMISSION_USAGE_REPORT.md

# Extract all unique permissions from @PreAuthorize
grep -rh "@PreAuthorize" src/main/java --include="*Controller.java" | \
    grep -oE "'[A-Z_]+'" | \
    tr -d "'" | \
    grep -v "^ROLE_" | \
    grep -v "^ADMIN$" | \
    grep -v "^MANAGER$" | \
    grep -v "^RECEPTIONIST$" | \
    grep -v "^PATIENT$" | \
    sort | uniq -c | sort -rn | \
    while read count perm; do
        echo "- **$perm**: $count usages" >> docs/PERMISSION_USAGE_REPORT.md
    done

echo "" >> docs/PERMISSION_USAGE_REPORT.md
echo "## Total Statistics" >> docs/PERMISSION_USAGE_REPORT.md
echo "" >> docs/PERMISSION_USAGE_REPORT.md
echo "- Total @PreAuthorize annotations: $(grep -rh '@PreAuthorize' src/main/java --include='*Controller.java' | wc -l)" >> docs/PERMISSION_USAGE_REPORT.md
echo "- Total controllers: $(find src/main/java -name '*Controller.java' | wc -l)" >> docs/PERMISSION_USAGE_REPORT.md

echo "✅ Report generated: docs/PERMISSION_USAGE_REPORT.md"

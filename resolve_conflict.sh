#!/bin/bash
cd /Users/gebbygeovany/Documents/be-orientasi
OUTPUT_FILE="/Users/gebbygeovany/Documents/be-orientasi/merge_output.txt"

{
echo "=== Current Branch ==="
git branch --show-current

echo "=== Fetching main ==="
git fetch origin main 2>&1

echo "=== Git Log main ==="
git --no-pager log --oneline origin/main -5

echo "=== Merging main ==="
git merge origin/main --no-edit 2>&1

echo "=== Status after merge ==="
git status 2>&1

echo "=== Done ==="
} > "$OUTPUT_FILE" 2>&1

cat "$OUTPUT_FILE"

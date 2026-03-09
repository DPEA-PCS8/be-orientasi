#!/bin/bash
cd /Users/gebbygeovany/Documents/be-orientasi
OUTPUT_FILE="/Users/gebbygeovany/Documents/be-orientasi/rebase_output.txt"

{
echo "=== Current status ==="
git status

echo "=== Continuing rebase ==="
git rebase --continue 2>&1

echo "=== Status after rebase ==="
git status

echo "=== Current branch ==="
git branch --show-current

echo "=== Log ==="
git --no-pager log --oneline -5

} > "$OUTPUT_FILE" 2>&1

cat "$OUTPUT_FILE"

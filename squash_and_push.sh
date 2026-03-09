#!/bin/bash
set -e
cd /Users/gebbygeovany/Documents/be-orientasi

echo "=== Step 1: Abort rebase ==="
git rebase --abort 2>&1 || true

echo "=== Step 2: Fetch origin main ==="
git fetch origin main

echo "=== Step 3: Reset soft to origin/main ==="
git reset --soft origin/main

echo "=== Step 4: Status ==="
git status

echo "=== Step 5: Add all files ==="
git add .

echo "=== Step 6: Commit ==="
git commit -m "feat: add PKSI dashboard integration with MapStruct mapper"

echo "=== Step 7: Push with force-with-lease ==="
git push --force-with-lease

echo "=== Done! ==="
git log --oneline -3

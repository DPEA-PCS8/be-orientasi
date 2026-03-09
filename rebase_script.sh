#!/bin/bash
cd /Users/gebbygeovany/Documents/be-orientasi

# Continue rebase (using GIT_EDITOR to avoid interactive mode)
GIT_EDITOR=true git rebase --continue 2>&1

echo "=== Status ===" 
git status 2>&1

echo "=== Branch ==="
git branch --show-current 2>&1

echo "=== Log ==="
git --no-pager log --oneline -3 2>&1

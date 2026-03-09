#!/bin/bash
cd /Users/gebbygeovany/Documents/be-orientasi

# Continue rebase with auto-commit
export GIT_EDITOR=cat
export EDITOR=cat 

git -c core.editor=cat rebase --continue

echo "RETURN CODE: $?"

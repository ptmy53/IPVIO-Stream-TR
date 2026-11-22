#!/usr/bin/env bash
# Checks a release notes file against CLAUDE.md section 18 and the rules in release.yml.
# Read only. Exit 0 if no error, 1 if an error was found.
#
# Usage: .claude/scripts/check-release-notes.sh <version>      e.g. 1.4.8
#        .claude/scripts/check-release-notes.sh <path-to-file>

set -u
cd "$(git rev-parse --show-toplevel)" || exit 2

ARG="${1:-}"
if [ -z "$ARG" ]; then echo "usage: $0 <version|file>" >&2; exit 2; fi

if [ -f "$ARG" ]; then
  FILE="$ARG"; VERSION=$(basename "$ARG" .md | sed 's/^v//')
else
  VERSION="$ARG"; FILE="docs/release-notes/v$VERSION.md"
fi

errors=0
err()  { echo "ERROR: $*"; errors=$((errors + 1)); }
warn() { echo "WARN:  $*"; }

if [ ! -f "$FILE" ]; then
  err "$FILE does not exist (release.yml falls back to the PR list without it)"
  exit 1
fi
echo "Checking $FILE for version $VERSION"

# 1. Highlights: the first bullet lines of the file, as release.yml reads them (first four, skipping headings).
#    The first section must be "## Highlights" and it must have four bullets.
first_heading=$(grep -m1 '^## ' "$FILE")
[ "$first_heading" = "## Highlights" ] || err "first section is '$first_heading', must be '## Highlights'"

NOTE=$(awk 'NF && !/^[[:space:]]*#/ { sub(/^[[:space:]]*[-*][[:space:]]+/, ""); print; if (++n == 4) exit }' "$FILE" | paste -sd'\t' | sed 's/\t/ · /g')
count=$(printf '%s' "$NOTE" | LC_ALL=C.UTF-8 awk -F' · ' '{print NF}')
chars=$(printf '%s' "$NOTE" | LC_ALL=C.UTF-8 wc -m)
[ "$count" -eq 4 ] || err "the first four bullet lines must be the four highlights (found $count bullets)"
[ "$chars" -le 300 ] || err "highlights joined are $chars characters, limit is 300 (release.yml cuts at 300)"
echo "       highlights joined: $chars of 300 characters"

# 2. Users, not developers: no task IDs, no file paths, no class names with a Kotlin suffix.
if grep -nE '\bT-[0-9]{3}\b' "$FILE" >/dev/null; then
  err "task ID in the notes: $(grep -noE '\bT-[0-9]{3}\b' "$FILE" | head -3 | tr '\n' ' ')"
fi
if grep -nE '(app/|docs/|src/main|\.kt\b|\.xml\b|\.kts\b)' "$FILE" >/dev/null; then
  err "file path or code file name in the notes (users do not need these): $(grep -nE '(app/|docs/|src/main|\.kt\b|\.xml\b|\.kts\b)' "$FILE" | head -2 | tr '\n' ' ')"
fi

# 3. Install and data: the notes must say whether user data is kept and whether a manual install is needed.
grep -qiE 'install over|keep|kept' "$FILE" || err "no statement that user data is kept (CLAUDE.md section 18)"
grep -qiE 'install|manual' "$FILE" || warn "no statement about installing (install over the old version, or by hand)"

# 4. CHANGELOG section for this version must exist, and Unreleased must be empty above it.
if [ -f CHANGELOG.md ] && [ "$VERSION" != "" ]; then
  grep -qE "^## $VERSION( |\$|\()" CHANGELOG.md || err "CHANGELOG.md has no section '## $VERSION' (section 18, step 1)"
  unreleased_lines=$(awk '/^## Unreleased/{f=1;next} /^## /{f=0} f && NF' CHANGELOG.md | wc -l)
  [ "$unreleased_lines" -eq 0 ] || warn "CHANGELOG '## Unreleased' is not empty ($unreleased_lines lines); they belong to the next release"
fi

# 5. Known limits stated? Warn only, the check cannot judge this.
grep -qiE 'not (yet )?(device )?tested|not measured|known|limit' "$FILE" || warn "no known limits named; if none are known, say so explicitly"

echo
if [ "$errors" -eq 0 ]; then echo "OK: no errors."; exit 0; fi
echo "$errors error(s). Fix them before the release is prepared."
exit 1

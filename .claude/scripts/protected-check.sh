#!/usr/bin/env bash
# Compares the protected identifiers of CLAUDE.md section 5 between a base ref and the
# working tree. Read only. Exit 1 if an identifier was removed or renamed, 0 otherwise.
#
# Usage: .claude/scripts/protected-check.sh [base-ref]   (default: origin/main)
#
# Covered: SharedPreferences names and key prefixes, backup file names (Backup.kt NAMES),
# manifest components, phone-remote HTTP routes (PhoneImport.kt). Not covered: stored key
# names used only in other files, and anything not matched by the patterns below.

set -u
cd "$(git rev-parse --show-toplevel)" || exit 2

BASE="${1:-origin/main}"
if ! git rev-parse --verify --quiet "$BASE^{commit}" >/dev/null; then
  echo "protected-check: base ref '$BASE' not found" >&2
  exit 2
fi

# Name -> pattern pairs. Each pattern yields one identifier per match (-o).
SRC='app/src/main'
PREF_RE='"(ipvio[a-z_0-9]*|sublist_[a-z_]*|xt_backup_|live_|sync_)"'
ROUTE_RE='"(GET|POST) /[A-Za-z0-9/_-]*"'
MANIFEST_RE='android:name="\.?[A-Za-z][A-Za-z0-9_.]*"'

# Extract identifiers from a git ref (ref given) or from the working tree (ref empty).
extract() {
  local ref="$1" out=""
  if [ -n "$ref" ]; then
    out+=$(git grep -ohE "$PREF_RE" "$ref" -- "$SRC" 2>/dev/null | sed "s/^/pref /")$'\n'
    out+=$(git grep -ohE "$ROUTE_RE" "$ref" -- "$SRC/java" 2>/dev/null | sed "s/^/route /")$'\n'
    out+=$(git show "$ref:app/src/main/AndroidManifest.xml" 2>/dev/null | grep -ohE "$MANIFEST_RE" | sed "s/^/manifest /")$'\n'
  else
    out+=$(grep -rohE "$PREF_RE" $SRC 2>/dev/null | sed "s/^/pref /")$'\n'
    out+=$(grep -rohE "$ROUTE_RE" $SRC/java 2>/dev/null | sed "s/^/route /")$'\n'
    out+=$(grep -ohE "$MANIFEST_RE" $SRC/AndroidManifest.xml 2>/dev/null | sed "s/^/manifest /")$'\n'
  fi
  # git grep prefixes "ref:path:" only without -h; with -h the match is printed alone.
  printf '%s\n' "$out" | sed 's/^\(pref\|route\|manifest\) \(ref\|HEAD\)://' | grep -v '^$' | sort -u
}

BEFORE=$(extract "$BASE")
AFTER=$(extract "")

REMOVED=$(comm -23 <(printf '%s\n' "$BEFORE") <(printf '%s\n' "$AFTER") | grep -v '^$')
ADDED=$(comm -13 <(printf '%s\n' "$BEFORE") <(printf '%s\n' "$AFTER") | grep -v '^$')

echo "Base: $BASE ($(git rev-parse --short "$BASE"))  Working tree: $(git rev-parse --short HEAD)+changes"
echo

if [ -n "$REMOVED" ]; then
  echo "REMOVED or renamed (needs maintainer approval, CLAUDE.md sections 5 and 6):"
  while IFS= read -r line; do echo "  - $line"; done <<< "$REMOVED"
  echo
fi

if [ -n "$ADDED" ]; then
  echo "ADDED (check: a new SharedPreferences name or route must have a reason):"
  while IFS= read -r line; do echo "  + $line"; done <<< "$ADDED"
  echo
fi

if [ -z "$REMOVED" ] && [ -z "$ADDED" ]; then
  echo "No change in protected identifiers."
fi

[ -n "$REMOVED" ] && exit 1
exit 0

#!/usr/bin/env bash
# Finds German words in lines added since a base ref (CLAUDE.md section 4). Report only, read only.
# Exit 0 always; the report lists findings to be judged by a reader. Not a blocking gate.
#
# Usage: .claude/scripts/english-check.sh [base-ref]   (default: origin/main)
#
# Classes:
#   COMMENT German in a code comment: must be English (section 4).
#   KEY   German inside a t("German", "English") or tr(li, "German", "English") call: the German is the translation key (section 5), allowed.
#   UI    German label of a UI call (Text, buttons, toggles, hints) outside Dict*.kt: needs a translation key.
#   MSG   German error, status or return text outside Dict*.kt: needs a translation key or English text.
#   CODE  any other German word in code outside Dict*.kt (often a false positive, e.g. a language code).
#   TEXT  German in docs, CHANGELOG, README, release notes, commit messages: an error on GitHub.
# Skipped: Dict*.kt (the German there is the translation key).
# Limits: word list only; words that are also English are left out; identifiers in camelCase are not split.

set -u
cd "$(git rev-parse --show-toplevel)" || exit 2

BASE="${1:-origin/main}"
if ! git rev-parse --verify --quiet "$BASE^{object}" >/dev/null; then
  echo "english-check: base ref '$BASE' not found" >&2; exit 2
fi

export LC_ALL=C.UTF-8

# Distinctive German words (no English homographs). Matched as whole words, case-insensitive.
WORDS='nicht|und|oder|ist|sind|wird|werden|wurde|bitte|keine|keinen|keiner|für|über|zurück|weiter|abbrechen|speichern|schließen|Einstellungen|Sendung|Sendungen|Verbindung|Fehler|Sprache|Datei|hinzufügen|entfernen|Zurücksetzen|Wiedergabe|gefunden|erreichbar|bereits|Kategorie|Kategorien|Lade|laden|Wähle|wählen|Tonspur|Untertitel|Mediathek|Nutzer|Ende|Sender|Suche|Liste'
RE="(^|[^[:alpha:]])($WORDS)([^[:alpha:]]|\$)"

# Lines added since the base (tracked files, incl. working tree) plus all lines of new untracked files.
added=$(
  { git diff -U0 "$BASE" --no-color -- . ':!*.lock' | awk '
      /^\+\+\+ /{ file=substr($0,7); next }
      /^\+[^+]/ || /^\+$/ { sub(/^\+/, ""); print file "\t" $0 }' ;
    for f in $(git ls-files --others --exclude-standard); do
      awk -v f="$f" '{ print f "\t" $0 }' "$f" 2>/dev/null
    done; }
)

n_key=0; n_code=0; n_text=0
report=""

# Prefilter in one pass: only lines with a German word reach the loop.
hits=$(printf '%s\n' "$added" | grep -iE "$RE" || true)

while IFS=$'\t' read -r file line; do
  [ -z "${file:-}" ] && continue
  case "$file" in
    */Dict*.kt|Dict*.kt) continue ;;
  esac
  case "$file" in
    *.md|CHANGELOG.md|README.md|*/docs/*|docs/*)
      cls=TEXT; n_text=$((n_text + 1)) ;;
    *.kt|*.kts|*.xml)
      trimmed=$(printf '%s' "$line" | sed 's/^[[:space:]]*//')
      if printf '%s' "$trimmed" | grep -qE "^(//|\*|/\*)"; then
        cls=COMMENT; n_code=$((n_code + 1))
      elif printf '%s' "$line" | grep -qiE "(t|tr)\(([a-zA-Z.]+, *)?\"[^\"]*($WORDS)"; then
        cls=KEY; n_key=$((n_key + 1))
      elif printf '%s' "$line" | grep -qE "(Text|PillButton|FocusButton|FilterPill|ToggleRow|SectionTitle|EmptyState|Hint|label *=)\("; then
        cls=UI; n_code=$((n_code + 1))
      elif printf '%s' "$line" | grep -qE "(error|Error|status|throw|return|PlaylistError|incomplete|onStatus)"; then
        cls=MSG; n_code=$((n_code + 1))
      else
        cls=CODE; n_code=$((n_code + 1))
      fi ;;
    *)
      continue ;;
  esac
  snippet=$(printf '%s' "$line" | sed 's/^[[:space:]]*//' | cut -c1-140)
  report+="$cls  $file  $snippet"$'\n'
  # FULL_OUT=<path>: write every finding (class, file) without the 80-line cap, for counting.
  [ -n "${FULL_OUT:-}" ] && printf '%s\t%s\n' "$cls" "$file" >> "$FULL_OUT"
done <<< "$hits"

echo "English check against $BASE ($(git rev-parse --short "$BASE")), working tree included."
count() { grep -a -c "^$1 " <<< "$report" || true; }
echo "Findings: TEXT $(count TEXT), COMMENT $(count COMMENT), UI $(count UI), MSG $(count MSG), CODE $(count CODE), KEY $(count KEY) (KEY is allowed: translation key)."
echo
if [ -n "$report" ]; then
  # TEXT first (errors), then the review classes, KEY last (allowed). Full list: use a file via redirect.
  for c in TEXT COMMENT UI MSG CODE KEY; do
    grep -a "^$c " <<< "$report" | head -80
  done
  echo "(at most 80 lines per class are shown)"
else
  echo "No German word found in added lines."
fi
exit 0

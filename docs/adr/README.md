# Architecture decision records

| ADR | Title | Status |
|---|---|---|
| [0001](0001-no-r8-for-now.md) | No R8 for now | Superseded by 0005 |
| [0002](0002-german-text-as-i18n-key.md) | German text is the translation key | Accepted |
| [0003](0003-shared-prefs-no-database.md) | SharedPreferences and files, no database | Accepted |
| [0005](0005-r8-whole-app.md) | R8 over the whole app, own code kept | Accepted (released in 1.4) |
| [0006](0006-branch-model-and-release-pipeline.md) | Branches main and dev, release pipeline, tests per change | Accepted |

Number 0004 was never merged into `main`. It existed only on the old branch `perf/T-007`. Numbers are not reused.

One record per architectural decision, not per small change. Format: Status, Context, Decision, Consequences.

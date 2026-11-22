# T-011 Sort categories (Live TV, movies, series)

- **Goal:** categories can be shown in provider order, A-Z, or an own order.
- **Change:** new `CatOrderLogic.kt` (pure, tested) and `CatOrder.kt` (store `ipvio_catorder` per profile: `m_AREA` mode, `o_AREA` own order; areas `live`, `movie`, `series`). Added to backup `NAMES`.
  - Live TV: menu key / long OK on a provider category → "Move up", "Move down", "Move to top", "Sort categories …" (Provider order / A–Z / Own order). Moving switches to the own order. Category rows now have stable keys so focus moves with the row.
  - Movies/Series → Categories: sorting dropdown "Default / A–Z / Own order" (saved) for provider categories; menu key / long OK moves a category. Automatic genres keep the old Default/A–Z switch.
  - Favorite categories stay on top; new provider categories appear after the placed ones in provider order.
- **Files:** `CatOrderLogic.kt`, `CatOrder.kt`, `LiveTab.kt`, `CategoriesScreen.kt`, `HomeScreen.kt`, `Backup.kt`, `DictK.kt` (6 new UI strings, 8 languages + nl).
- **Checks run:** `CatOrderLogicTest` (7 tests) and all pure tests (169) green locally; CI build.
- **Device test:** Live TV → long OK on a category → move up/down/top; restart app → order kept; "Sort categories …" → A–Z; Movies → Categories → Sort "Own order", long OK on a category → move; backup export/import keeps the order.
- **Status:** on branch `fix/round28-major`, device test open.

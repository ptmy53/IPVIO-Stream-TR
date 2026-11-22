package com.ipvio.stream

// T-041, stage 2a: English rows for German text patterns with placeholders that no row covered.
// Placeholders {0}, {1} match any text; the German key must equal the runtime text around them.
// Other languages fall back to English (I18nCore.fill), as for DictEn1.
val DictEn2 = """
OpenSubtitles: Fehler {0}¦OpenSubtitles: error {0}¦OpenSubtitles: hata {0}¦OpenSubtitles: greška {0}¦OpenSubtitles: pogreška {0}¦OpenSubtitles: greška {0}¦OpenSubtitles: error {0}¦OpenSubtitles : erreur {0}¦OpenSubtitles: errore {0}
Server-Fehler {0}¦Server error {0}¦Sunucu hatası {0}¦Greška servera {0}¦Pogreška poslužitelja {0}¦Greška servera {0}¦Error del servidor {0}¦Erreur serveur {0}¦Errore del server {0}
Serien · {0}¦Series · {0}¦Diziler · {0}¦Serije · {0}¦Serije · {0}¦Serije · {0}¦Series · {0}¦Séries · {0}¦Serie · {0}
Filme · {0}¦Movies · {0}¦Filmler · {0}¦Filmovi · {0}¦Filmovi · {0}¦Filmovi · {0}¦Películas · {0}¦Films · {0}¦Film · {0}
Sender werden geladen ({0}) …¦Loading channels ({0}) …¦Kanallar yükleniyor ({0}) …¦Učitavanje kanala ({0}) …¦Učitavanje kanala ({0}) …¦Učitavanje kanala ({0}) …¦Cargando canales ({0}) …¦Chargement des chaînes ({0}) …¦Caricamento canali ({0}) …
Kategorien werden geladen ({0}/{1}) …¦Loading categories ({0}/{1}) …¦Kategoriler yükleniyor ({0}/{1}) …¦Učitavanje kategorija ({0}/{1}) …¦Učitavanje kategorija ({0}/{1}) …¦Učitavanje kategorija ({0}/{1}) …¦Cargando categorías ({0}/{1}) …¦Chargement des catégories ({0}/{1}) …¦Caricamento categorie ({0}/{1}) …
Suche „{0}“ · Filme¦Search „{0}“ · Movies¦Arama „{0}“ · Filmler¦Pretraga „{0}“ · Filmovi¦Pretraga „{0}“ · Filmovi¦Pretraga „{0}“ · Filmovi¦Buscar „{0}“ · Películas¦Recherche „{0}“ · Films¦Cerca „{0}“ · Film
Suche „{0}“ · Serien¦Search „{0}“ · Series¦Arama „{0}“ · Diziler¦Pretraga „{0}“ · Serije¦Pretraga „{0}“ · Serije¦Pretraga „{0}“ · Serije¦Buscar „{0}“ · Series¦Recherche „{0}“ · Séries¦Cerca „{0}“ · Serie
Suche „{0}“ · Live TV¦Search „{0}“ · Live TV¦Arama „{0}“ · Canlı TV¦Pretraga „{0}“ · Live TV¦Pretraga „{0}“ · Live TV¦Pretraga „{0}“ · Live TV¦Buscar „{0}“ · TV en directo¦Recherche „{0}“ · TV en direct¦Cerca „{0}“ · TV in diretta
"""

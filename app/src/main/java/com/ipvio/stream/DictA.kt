package com.ipvio.stream

// Translations part A: add playlist, browse, detail page, home screen
val DictA = """
Playlist hinzufügen¦Add playlist¦Playlist ekle¦Dodaj playlistu¦Dodaj playlistu¦Dodaj plejlistu¦Añadir lista¦Ajouter une playlist¦Aggiungi playlist
Dauert nur ein paar Sekunden.¦Only takes a few seconds.¦Yalnızca birkaç saniye sürer.¦Traje samo nekoliko sekundi.¦Traje samo nekoliko sekundi.¦Traje samo nekoliko sekundi.¦Solo tarda unos segundos.¦Ne prend que quelques secondes.¦Ci vogliono solo pochi secondi.
Aus Zwischenablage einfügen¦Paste from clipboard¦Panodan yapıştır¦Zalijepi iz međuspremnika¦Zalijepi iz međuspremnika¦Nalepi iz privremene memorije¦Pegar del portapapeles¦Coller depuis le presse-papiers¦Incolla dagli appunti
Server (z. B. http://server.tld:8080)¦Server (e.g. http://server.tld:8080)¦Sunucu (ör. http://server.tld:8080)¦Server (npr. http://server.tld:8080)¦Server (npr. http://server.tld:8080)¦Server (npr. http://server.tld:8080)¦Servidor (p. ej. http://server.tld:8080)¦Serveur (p. ex. http://server.tld:8080)¦Server (es. http://server.tld:8080)
Benutzername¦Username¦Kullanıcı adı¦Korisničko ime¦Korisničko ime¦Korisničko ime¦Nombre de usuario¦Nom d'utilisateur¦Nome utente
Passwort¦Password¦Parola¦Lozinka¦Lozinka¦Lozinka¦Contraseña¦Mot de passe¦Password
Passwort verbergen¦Hide password¦Parolayı gizle¦Sakrij lozinku¦Sakrij lozinku¦Sakrij lozinku¦Ocultar contraseña¦Masquer le mot de passe¦Nascondi password
Passwort anzeigen¦Show password¦Parolayı göster¦Prikaži lozinku¦Prikaži lozinku¦Prikaži lozinku¦Mostrar contraseña¦Afficher le mot de passe¦Mostra password
Backup-Server {#0} (optional)¦Backup server {#0} (optional)¦Yedek sunucu {#0} (isteğe bağlı)¦Rezervni server {#0} (opcionalno)¦Rezervni poslužitelj {#0} (neobavezno)¦Rezervni server {#0} (opciono)¦Servidor de respaldo {#0} (opcional)¦Serveur de secours {#0} (facultatif)¦Server di riserva {#0} (facoltativo)
+ Backup-Server¦+ Backup server¦+ Yedek sunucu¦+ Rezervni server¦+ Rezervni poslužitelj¦+ Rezervni server¦+ Servidor de respaldo¦+ Serveur de secours¦+ Server di riserva
Fällt der Server aus, wechselt die App von selbst zu einem Backup-Server. Anbieter nennen sie oft als weitere Server- oder DNS-Adressen; Benutzername und Passwort bleiben gleich.¦If the server goes down, the app switches to a backup server by itself. Providers often list these as extra server or DNS addresses; the username and password stay the same.¦Sunucu çökerse uygulama kendiliğinden bir yedek sunucuya geçer. Sağlayıcılar bunları genellikle ek sunucu veya DNS adresleri olarak verir; kullanıcı adı ve parola aynı kalır.¦Ako server prestane raditi, aplikacija sama prelazi na rezervni server. Pružaoci ih često navode kao dodatne adrese servera ili DNS-a; korisničko ime i lozinka ostaju isti.¦Ako poslužitelj prestane raditi, aplikacija sama prelazi na rezervni poslužitelj. Pružatelji ih često navode kao dodatne adrese poslužitelja ili DNS-a; korisničko ime i lozinka ostaju isti.¦Ako server prestane da radi, aplikacija sama prelazi na rezervni server. Provajderi ih često navode kao dodatne adrese servera ili DNS-a; korisničko ime i lozinka ostaju isti.¦Si el servidor se cae, la app cambia sola a un servidor de respaldo. Los proveedores suelen darlos como direcciones de servidor o DNS adicionales; el usuario y la contraseña no cambian.¦Si le serveur tombe en panne, l'app passe d'elle-même à un serveur de secours. Les fournisseurs les indiquent souvent comme adresses de serveur ou DNS supplémentaires ; le nom d'utilisateur et le mot de passe restent les mêmes.¦Se il server non risponde, l'app passa da sola a un server di riserva. I fornitori li indicano spesso come indirizzi server o DNS aggiuntivi; nome utente e password restano gli stessi.
Backup-Server ungültig: Adresse wie http://server.tld:8080 eingeben.¦Invalid backup server: enter an address like http://server.tld:8080.¦Geçersiz yedek sunucu: http://server.tld:8080 gibi bir adres girin.¦Neispravan rezervni server: unesi adresu kao http://server.tld:8080.¦Neispravan rezervni poslužitelj: unesi adresu poput http://server.tld:8080.¦Neispravan rezervni server: unesi adresu kao http://server.tld:8080.¦Servidor de respaldo no válido: introduce una dirección como http://server.tld:8080.¦Serveur de secours non valide : saisis une adresse comme http://server.tld:8080.¦Server di riserva non valido: inserisci un indirizzo come http://server.tld:8080.
Backup-Server¦Backup servers¦Yedek sunucular¦Rezervni serveri¦Rezervni poslužitelji¦Rezervni serveri¦Servidores de respaldo¦Serveurs de secours¦Server di riserva
Backup-Server: {#0}¦Backup servers: {#0}¦Yedek sunucular: {#0}¦Rezervni serveri: {#0}¦Rezervni poslužitelji: {#0}¦Rezervni serveri: {#0}¦Servidores de respaldo: {#0}¦Serveurs de secours : {#0}¦Server di riserva: {#0}
Backup-Server speichern¦Save backup servers¦Yedek sunucuları kaydet¦Sačuvaj rezervne servere¦Spremi rezervne poslužitelje¦Sačuvaj rezervne servere¦Guardar servidores de respaldo¦Enregistrer les serveurs de secours¦Salva server di riserva
Erreichbar¦Reachable¦Erişilebilir¦Dostupan¦Dostupan¦Dostupan¦Accesible¦Joignable¦Raggiungibile
Nicht erreichbar seit {0}¦Not reachable since {0}¦{0} tarihinden beri erişilemiyor¦Nedostupan od {0}¦Nedostupan od {0}¦Nedostupan od {0}¦No accesible desde {0}¦Injoignable depuis {0}¦Non raggiungibile dal {0}
Nicht erreichbar: {0}¦Not reachable: {0}¦Erişilemiyor: {0}¦Nedostupno: {0}¦Nedostupno: {0}¦Nedostupno: {0}¦No accesible: {0}¦Injoignable : {0}¦Non raggiungibile: {0}
Hauptserver: {0}¦Main server: {0}¦Ana sunucu: {0}¦Glavni server: {0}¦Glavni poslužitelj: {0}¦Glavni server: {0}¦Servidor principal: {0}¦Serveur principal : {0}¦Server principale: {0}
Hinzufügen¦Add¦Ekle¦Dodaj¦Dodaj¦Dodaj¦Añadir¦Ajouter¦Aggiungi
Abbrechen¦Cancel¦İptal¦Otkaži¦Otkaži¦Otkaži¦Cancelar¦Annuler¦Annulla
Teilliste (nur bestimmte Sender laden)¦Sublist (only load certain channels)¦Sublist (sadece bazı kanalları yükle)¦Sublist (učitaj samo određene kanale)¦Sublist (učitaj samo određene kanale)¦Sublist (učitaj samo određene kanale)¦Sublista (solo cargar ciertos canales)¦Sous-liste (charger seulement certaines chaînes)¦Sublista (carica solo alcuni canali)
Teilliste verwenden¦Use sublist¦Sublisti kullan¦Koristi sublist¦Koristi sublist¦Koristi sublist¦Usar sublista¦Utiliser la sous-liste¦Usa sublista
Nur Einträge laden, die zum Filter passen. Spart Speicher und lädt große Playlists schneller.¦Only load entries matching the filter. Saves memory and loads large playlists faster.¦Sadece filtreye uygun girişleri yükler. Bellek tasarrufu sağlar ve büyük playlistleri daha hızlı yükler.¦Učitaj samo unerte koji odgovaraju filteru. Štedzi memoriju i učitavaj velike playliste brže.¦Učitaj samo unerte koji odgovaraju filteru. Štedzi memoriju i učitavaj velike playliste brže.¦Učitaj samo stavke koje odgovaraju filteru. Uštedi memoriju i učitavaj velike playliste brže.¦Carga solo las entradas que coincidan con el filtro. Ahorra memoria y carga listas grandes más rápido.¦Chargez seulement les entrées correspondant au filtre. Économise la mémoire et charge les grandes playlists plus rapidement.¦Carica solo le voci che corrispondono al filtro. Risparmia memoria e carica playlist grandi più velocemente.
Name der Teilliste (z. B. „UK Sender“ oder „4K-Filme“)¦Name of the sublist (e.g. „UK channels“ or „4K movies“)¦Sublistin ismi (ör. „UK kanalları“ veya „4K filmler“)¦Ime sublista (npr. „UK kanali“ ili „4K filmovi“)¦Ime sublista (npr. „UK kanali“ ili „4K filmovi“)¦Ime sublista (npr. „UK kanali“ ili „4K filmovi“)¦Nombre de la sublista (p. ej. „canales UK“ o „películas 4K“)¦Nom de la sous-liste (p. ex. „chaînes UK“ ou „films 4K“)¦Nome della sublista (es. „canali UK“ o „film 4K“)
Kategorie enthält¦Category contains¦Kategori içeriyor¦Kategorija sadrži¦Kategorija sadrži¦Kategorija sadrži¦La categoría contiene¦La catégorie contient¦La categoria contiene
z. B. UK, Sport, News¦e.g. UK, Sport, News¦ör. UK, Spor, Haber¦npr. UK, Sport, Vijesti¦npr. UK, Sport, Vijesti¦npr. UK, Sport, Vijesti¦p. ej. UK, Deporte, Noticias¦p. ex. UK, Sport, Actualités¦es. UK, Sport, Notizie
Typ¦Type¦Tür¦Tip¦Tip¦Tip¦Tipo¦Type¦Tipo
Beliebig¦Any¦Herhangi biri¦Bilo koji¦Bilo koji¦Bilo koji¦Cualquiera¦N'importe quel¦Qualsiasi
Live TV¦Live TV¦Canlı TV¦Live TV¦Live TV¦Live TV¦TV en directo¦TV en direct¦TV in diretta
Filme¦Movies¦Filmler¦Filmovi¦Filmovi¦Filmovi¦Películas¦Films¦Film
Serien¦Series¦Diziler¦Serije¦Serije¦Serije¦Series¦Séries¦Serie
Titel enthält¦Title contains¦Başlık içeriyor¦Naslov sadrži¦Naslov sadrži¦Naslov sadrži¦El título contiene¦Le titre contient¦Il titolo contiene
z. B. CNN, 4K, Bundesliga¦e.g. CNN, 4K, Bundesliga¦ör. CNN, 4K, Bundesliga¦npr. CNN, 4K, Bundesliga¦npr. CNN, 4K, Bundesliga¦npr. CNN, 4K, Bundesliga¦p. ej. CNN, 4K, Bundesliga¦p. ex. CNN, 4K, Bundesliga¦es. CNN, 4K, Bundesliga
Sprache¦Language¦Dil¦Jezik¦Jezik¦Jezik¦Idioma¦Langue¦Lingua
z. B. de, en, tr¦e.g. de, en, tr¦ör. de, en, tr¦npr. de, en, tr¦npr. de, en, tr¦npr. de, en, tr¦p. ej. de, en, tr¦p. ex. de, en, tr¦es. de, en, tr
Qualität¦Quality¦Kalite¦Kvaliteta¦Kvaliteta¦Kvaliteta¦Calidad¦Qualité¦Qualità
HD¦HD¦HD¦HD¦HD¦HD¦HD¦HD¦HD
FHD¦FHD¦FHD¦FHD¦FHD¦FHD¦FHD¦FHD¦FHD
4K¦4K¦4K¦4K¦4K¦4K¦4K¦4K¦4K
Logo¦Logo¦Logo¦Logo¦Logo¦Logo¦Logo¦Logo¦Logo
Egal¦Any¦Fark etmez¦Bilo koje¦Bilo koje¦Bilo koje¦Cualquiera¦N'importe lequel¦Qualsiasi
Mit Logo¦With logo¦Logo ile¦Sa logom¦Sa logom¦Sa logom¦Con logo¦Avec logo¦Con logo
Ohne Logo¦Without logo¦Logo olmadan¦Bez logoa¦Bez logoa¦Bez logoa¦Sin logo¦Sans logo¦Senza logo
tvg-id¦tvg-id¦tvg-id¦tvg-id¦tvg-id¦tvg-id¦tvg-id¦tvg-id¦tvg-id
Mit tvg-id¦With tvg-id¦tvg-id ile¦Sa tvg-id¦Sa tvg-id¦Sa tvg-id¦Con tvg-id¦Avec tvg-id¦Con tvg-id
Ohne tvg-id¦Without tvg-id¦tvg-id olmadan¦Bez tvg-id¦Bez tvg-id¦Bez tvg-id¦Sin tvg-id¦Sans tvg-id¦Senza tvg-id
Erwachsenen-Inhalte einschließen¦Include adult content¦Yetişkin içeriği dahil et¦Uključi odrasli sadržaj¦Uključi odrasli sadržaj¦Uključi odrasli sadržaj¦Incluir contenido para adultos¦Inclure le contenu pour adultes¦Includi contenuti per adulti
Aus: Titel mit Erwachsenen-Kennzeichen werden nicht geladen.¦Off: titles with adult markers are not loaded.¦Kapalı: yetişkin işaretli başlıklar yüklenmez.¦Isključeno: naslovi s odraslim oznakama se ne učitavaju.¦Isključeno: naslovi s odraslim oznakama se ne učitavaju.¦Isključeno: naslovi s odraslim oznakama se ne učitavaju.¦Apagado: los títulos con marcadores para adultos no se cargan.¦Désactivé : les titres avec marqueurs pour adultes ne sont pas chargés.¦Spento: i titoli con marcatori per adulti non vengono caricati.
Sublist / Teilliste (optional)¦Sublist / Teilliste (optional)¦Sublist / Teilliste (isteğe bağlı)¦Sublist / Teilliste (opcionalno)¦Sublist / Teilliste (opcionalno)¦Sublist / Teilliste (opcionalno)¦Sublist / Teilliste (opcional)¦Sous-liste / Teilliste (en option)¦Sublista / Teilliste (opzionale)
Nur bestimmte Sender dieser Playlist auf den Fernseher laden.¦Load only certain channels of this playlist to the TV.¦Bu oynatma listesindeki yalnızca belirli kanalları TV'ye yükle.¦Učitaj samo određene kanale ovog popisa za reprodukciju na TV.¦Učitaj samo određene kanale ovog popisa za reprodukciju na TV.¦Učitaj samo određene stavke ove plejliste na TV.¦Carga solo ciertos canales de esta lista al televisor.¦Chargez seulement certaines chaînes de cette playlist sur la TV.¦Carica solo alcuni canali di questa playlist sulla TV.
Name (z. B. „UK-Sender“)¦Name (e.g. „UK channels“)¦İsim (ör. „UK kanalları“)¦Ime (npr. „UK kanali“)¦Ime (npr. „UK kanali“)¦Ime (npr. „UK kanali“)¦Nombre (p. ej. „canales UK“)¦Nom (p. ex. „chaînes UK“)¦Nome (es. „canali UK“)
Name der Teilliste¦Sublist name¦Sublistin ismi¦Ime sublista¦Ime sublista¦Ime sublista¦Nombre de la sublista¦Nom de la sous-liste¦Nome della sublista
Teillisten¦Sublists¦Sublistler¦Sublist¦Sublist¦Sublist¦Sublistas¦Sous-listes¦Subliste
Nur bestimmte Sender pro Playlist laden¦Load only certain channels per playlist¦Sadece belirli kanalları playlist başına yükle¦Učitaj samo određene kanale po playlista¦Učitaj samo određene kanale po playlista¦Učitaj samo određene stavke po plejlisti¦Cargar solo ciertos canales por lista¦Charger seulement certaines chaînes par playlist¦Carica solo alcuni canali per playlist
Aktive Teilliste:¦Active sublist:¦Etkin sublist:¦Aktivna sublista:¦Aktivna sublista:¦Aktivna sublista:¦Sublista activa:¦Sous-liste active:¦Sublista attiva:
Keine Teilliste (vollständige Playlist)¦No sublist (full playlist)¦Sublist yok (tüm oynatma listesi)¦Nema subliste (cijela playlista)¦Nema subliste (cijela popis za reprodukciju)¦Nema subliste (cela plejlista)¦Sin sublista (lista completa)¦Pas de sous-liste (playlist complète)¦Nessuna sublista (playlist completa)
Keine Teillisten¦No sublists¦Sublist yok¦Nema sublista¦Nema sublista¦Nema sublista¦Sin sublistas¦Pas de sous-listes¦Nessuna sublista
Neue Teillisten legst du an, indem du bei „Playlist hinzufügen“ die Teilliste einschaltest, Felder ausfüllst und „Hinzufügen“ drückst. Sie werden dann hier aufgelistet und können aktiviert oder gelöscht werden.¦Create new sublists by enabling sublist at “Add playlist”, filling in the fields, and pressing “Add”. They are then listed here and can be activated or deleted.¦Yeni sublistleri “Playlist ekle” ekranında sublisti etkinleştirip, alanları doldurarak ve “Ekle”ye basarak oluşturursun. Ardından burada listelenir ve etkinleştirilebilir veya silinebilir.¦Nove subliste stvaraš tako da na ekranu “Dodaj playlistu” omogućiš sublistu, popunješ polja i pritisneš “Dodaj”. Tada se popisuju ovdje i mogu se aktivirati ili obrisati.¦Nove subliste stvaraš tako da na ekranu “Dodaj popis za reprodukciju” omogućiš sublistu, popunješ polja i pritisneš “Dodaj”. Tada se popisuju ovdje i mogu se aktivirati ili obrisati.¦Nove subliste kreiraš tako da na ekranu “Dodaj plejlistu” omogućiš sublistu, popunješ polja i pritisneš “Dodaj”. Tada se navode ovde i mogu se aktivirati ili obrisati.¦Crea nuevas sublistas activando sublista en “Añadir lista”, rellenando los campos y pulsando “Añadir”. Luego se listan aquí y se pueden activar o eliminar.¦Créez de nouvelles sous-listes en activant la sous-liste dans “Ajouter une playlist”, en remplissant les champs, puis en appuyant sur “Ajouter”. Elles apparaissent ensuite ici et peuvent être activées ou supprimées.¦Crea nuove subliste attivando sublista in “Aggiungi playlist”, compilando i campi e premendo “Aggiungi”. Verranno poi elencate qui e potranno essere attivate o eliminate.
Alle¦All¦Tümü¦Sve¦Sve¦Sve¦Todos¦Tous¦Tutti
Alle anzeigen¦Show all¦Tümünü göster¦Prikaži sve¦Prikaži sve¦Prikaži sve¦Ver todo¦Tout afficher¦Mostra tutto
Neu hinzugefügt¦Recently added¦Yeni eklenenler¦Nedavno dodano¦Nedavno dodano¦Nedavno dodano¦Añadido recientemente¦Ajouts récents¦Aggiunti di recente
Beliebtheit¦Popularity¦Popülerlik¦Popularnost¦Popularnost¦Popularnost¦Popularidad¦Popularité¦Popolarità
Playlist¦Playlist¦Playlist¦Playlista¦Playlista¦Plejlista¦Lista¦Playlist¦Playlist
vor 1990¦before 1990¦1990 öncesi¦prije 1990.¦prije 1990.¦prije 1990.¦antes de 1990¦avant 1990¦prima del 1990
{#0} Titel¦{#0} titles¦{#0} yapım¦{#0} naslova¦{#0} naslova¦{#0} naslova¦{#0} títulos¦{#0} titres¦{#0} titoli
{0} werden geladen … {#1} / {#2}¦Loading {0} … {#1} / {#2}¦{0} yükleniyor … {#1} / {#2}¦Učitavanje: {0} … {#1} / {#2}¦Učitavanje: {0} … {#1} / {#2}¦Učitavanje: {0} … {#1} / {#2}¦Cargando {0} … {#1} / {#2}¦Chargement : {0} … {#1} / {#2}¦Caricamento: {0} … {#1} / {#2}
{0} laden … {#1}/{#2}¦Loading {0} … {#1}/{#2}¦{0} yükleniyor … {#1}/{#2}¦Učitavanje: {0} … {#1}/{#2}¦Učitavanje: {0} … {#1}/{#2}¦Učitavanje: {0} … {#1}/{#2}¦Cargando {0} … {#1}/{#2}¦Chargement : {0} … {#1}/{#2}¦Caricamento: {0} … {#1}/{#2}
Metadaten¦Metadata¦Meta veriler¦Metapodaci¦Metapodaci¦Metapodaci¦Metadatos¦Métadonnées¦Metadati
Länder¦Countries¦Ülkeler¦Države¦Države¦Države¦Países¦Pays¦Paesi
Zurück = Filter¦Back = Filters¦Geri = Filtreler¦Nazad = Filteri¦Natrag = Filtri¦Nazad = Filteri¦Atrás = Filtros¦Retour = Filtres¦Indietro = Filtri
Sortierung¦Sort¦Sıralama¦Sortiranje¦Sortiranje¦Sortiranje¦Orden¦Tri¦Ordinamento
Rating¦Rating¦Puan¦Ocjena¦Ocjena¦Ocena¦Valoración¦Note¦Voto
Jahr¦Year¦Yıl¦Godina¦Godina¦Godina¦Año¦Année¦Anno
Genre¦Genre¦Tür¦Žanr¦Žanr¦Žanr¦Género¦Genre¦Genere
Land¦Country¦Ülke¦Država¦Država¦Država¦País¦Pays¦Paese
Zurücksetzen¦Reset¦Sıfırla¦Resetiraj¦Resetiraj¦Resetuj¦Restablecer¦Réinitialiser¦Ripristina
Raster¦Grid¦Izgara¦Mreža¦Mreža¦Mreža¦Cuadrícula¦Grille¦Griglia
Kino¦Cinema¦Sinema¦Kino¦Kino¦Kino¦Cine¦Cinéma¦Cinema
Ansicht¦View¦Görünüm¦Prikaz¦Prikaz¦Prikaz¦Vista¦Affichage¦Vista
Keine Treffer – Filter zurücksetzen.¦No results – reset the filters.¦Sonuç yok – filtreleri sıfırlayın.¦Nema rezultata – resetirajte filtere.¦Nema rezultata – resetirajte filtre.¦Nema rezultata – resetujte filtere.¦Sin resultados: restablece los filtros.¦Aucun résultat – réinitialisez les filtres.¦Nessun risultato – ripristina i filtri.
Liste¦List¦Liste¦Lista¦Lista¦Lista¦Lista¦Liste¦Elenco
Besetzung¦Cast¦Oyuncular¦Glumci¦Glumci¦Glumci¦Reparto¦Distribution¦Cast
Regie¦Director¦Yönetmen¦Režija¦Režija¦Režija¦Dirección¦Réalisation¦Regia
Erstellt von¦Created by¦Yaratıcı¦Kreator¦Autor ideje¦Kreator¦Creado por¦Créé par¦Ideato da
Schauspiel¦Acting¦Oyunculuk¦Gluma¦Gluma¦Gluma¦Interpretación¦Interprétation¦Recitazione
Drehbuch¦Writing¦Senaryo¦Scenarij¦Scenarij¦Scenarij¦Guion¦Scénario¦Sceneggiatura
geboren {0}¦born {0}¦doğum {0}¦rođen/a {0}¦rođen/a {0}¦rođen/a {0}¦nacimiento {0}¦né(e) le {0}¦nato/a il {0}
Weniger¦Less¦Daha az¦Manje¦Manje¦Manje¦Menos¦Moins¦Meno
Mehr lesen¦Read more¦Devamını oku¦Pročitaj više¦Pročitaj više¦Pročitaj više¦Leer más¦Lire la suite¦Leggi tutto
Infos konnten nicht geladen werden.¦Could not load info.¦Bilgiler yüklenemedi.¦Informacije nisu učitane.¦Informacije nisu učitane.¦Informacije nisu učitane.¦No se pudo cargar la información.¦Impossible de charger les infos.¦Impossibile caricare le informazioni.
Keine Titel in deiner Playlist¦No titles in your playlist¦Playlistinizde yapım yok¦Nema naslova u tvojoj playlisti¦Nema naslova u tvojoj playlisti¦Nema naslova u tvojoj plejlisti¦No hay títulos en tu lista¦Aucun titre dans ta playlist¦Nessun titolo nella tua playlist
In deiner Playlist¦In your playlist¦Playlistinizde¦U tvojoj playlisti¦U tvojoj playlisti¦U tvojoj plejlisti¦En tu lista¦Dans ta playlist¦Nella tua playlist
{#0} weitere Titel dieser Person sind nicht in deiner Playlist.¦{#0} more titles by this person are not in your playlist.¦Bu kişinin {#0} yapımı daha playlistinizde yok.¦Još {#0} naslova ove osobe nije u tvojoj playlisti.¦Još {#0} naslova ove osobe nije u tvojoj playlisti.¦Još {#0} naslova ove osobe nije u tvojoj plejlisti.¦{#0} títulos más de esta persona no están en tu lista.¦{#0} autres titres de cette personne ne sont pas dans ta playlist.¦Altri {#0} titoli di questa persona non sono nella tua playlist.
Lade …¦Loading …¦Yükleniyor …¦Učitavanje …¦Učitavanje …¦Učitavanje …¦Cargando …¦Chargement …¦Caricamento …
Zuletzt geschaut¦Recently watched¦Son izlenenler¦Nedavno gledano¦Nedavno gledano¦Nedavno gledano¦Visto recientemente¦Vus récemment¦Visti di recente
Meine Liste¦My list¦Listem¦Moja lista¦Moja lista¦Moja lista¦Mi lista¦Ma liste¦La mia lista
In Meiner Liste¦In my list¦Listemde¦U mojoj listi¦U mojoj listi¦U mojoj listi¦En mi lista¦Dans ma liste¦Nella mia lista
Filter¦Filters¦Filtreler¦Filteri¦Filtri¦Filteri¦Filtros¦Filtres¦Filtri
Standard¦Default¦Varsayılan¦Standardno¦Standardno¦Standardno¦Predeterminado¦Par défaut¦Predefinito
Kacheln¦Tiles¦Kutucuklar¦Pločice¦Pločice¦Pločice¦Mosaicos¦Tuiles¦Riquadri
{#0} Kategorien¦{#0} categories¦{#0} kategori¦{#0} kategorija¦{#0} kategorija¦{#0} kategorija¦{#0} categorías¦{#0} catégories¦{#0} categorie
Kategorien¦Categories¦Kategoriler¦Kategorije¦Kategorije¦Kategorije¦Categorías¦Catégories¦Categorie
Sonstige¦Other¦Diğer¦Ostalo¦Ostalo¦Ostalo¦Otros¦Autres¦Altro
Anime¦Anime¦Anime¦Anime¦Anime¦Anime¦Anime¦Anime¦Anime
Animation¦Animation¦Animasyon¦Animacija¦Animacija¦Animacija¦Animación¦Animation¦Animazione
Film¦Movie¦Film¦Film¦Film¦Film¦Película¦Film¦Film
{#0} Staffel(n)¦{#0} season(s)¦{#0} sezon¦{#0} sezona¦{#0} sezona¦{#0} sezona¦{#0} temporada(s)¦{#0} saison(s)¦{#0} stagione/i
{#0} Min¦{#0} min¦{#0} dk¦{#0} min¦{#0} min¦{#0} min¦{#0} min¦{#0} min¦{#0} min
bei {#0} Min¦at {#0} min¦{#0}. dakikada¦na {#0} min¦na {#0} min¦na {#0} min¦en el min {#0}¦à {#0} min¦al minuto {#0}
TMDB antwortet mit Fehler {#0}¦TMDB returned error {#0}¦TMDB {#0} hatası döndürdü¦TMDB je vratio grešku {#0}¦TMDB je vratio grešku {#0}¦TMDB je vratio grešku {#0}¦TMDB respondió con error {#0}¦TMDB a répondu avec l'erreur {#0}¦TMDB ha risposto con errore {#0}
Keine Verbindung zu TMDB ({0})¦No connection to TMDB ({0})¦TMDB bağlantısı yok ({0})¦Nema veze s TMDB-om ({0})¦Nema veze s TMDB-om ({0})¦Nema veze s TMDB-om ({0})¦Sin conexión con TMDB ({0})¦Pas de connexion à TMDB ({0})¦Nessuna connessione a TMDB ({0})
Besetzung nicht lesbar ({0})¦Cast could not be read ({0})¦Oyuncu bilgisi okunamadı ({0})¦Glumci se ne mogu pročitati ({0})¦Glumci se ne mogu pročitati ({0})¦Glumci se ne mogu pročitati ({0})¦No se pudo leer el reparto ({0})¦Distribution illisible ({0})¦Cast non leggibile ({0})
Besetzung nicht verfügbar: {0}¦Cast unavailable: {0}¦Oyuncu bilgisi yok: {0}¦Glumci nisu dostupni: {0}¦Glumci nisu dostupni: {0}¦Glumci nisu dostupni: {0}¦Reparto no disponible: {0}¦Distribution indisponible : {0}¦Cast non disponibile: {0}
Keine App zum Öffnen des Trailers gefunden¦No app found to open the trailer¦Fragmanı açacak uygulama bulunamadı¦Nije pronađena aplikacija za otvaranje trailera¦Nije pronađena aplikacija za otvaranje trailera¦Nije pronađena aplikacija za otvaranje trejlera¦No se encontró ninguna app para abrir el tráiler¦Aucune application pour ouvrir la bande-annonce¦Nessuna app trovata per aprire il trailer
Externer Player¦External player¦Harici oynatıcı¦Vanjski player¦Vanjski player¦Spoljni plejer¦Reproductor externo¦Lecteur externe¦Player esterno
Kein externer Player installiert (z. B. VLC)¦No external player installed (e.g. VLC)¦Harici oynatıcı kurulu değil (ör. VLC)¦Nije instaliran vanjski player (npr. VLC)¦Nije instaliran vanjski player (npr. VLC)¦Nije instaliran spoljni plejer (npr. VLC)¦No hay reproductor externo instalado (p. ej. VLC)¦Aucun lecteur externe installé (p. ex. VLC)¦Nessun player esterno installato (es. VLC)
Keine Beschreibung gefunden.¦No description found.¦Açıklama bulunamadı.¦Opis nije pronađen.¦Opis nije pronađen.¦Opis nije pronađen.¦No se encontró descripción.¦Aucune description trouvée.¦Nessuna descrizione trovata.
Tipp: TMDB-Schlüssel in secrets.properties eintragen, dann erscheinen hier Beschreibung, Cover und Bewertungen.¦Tip: add your TMDB key in secrets.properties to see descriptions, covers and ratings here.¦İpucu: Açıklama, kapak ve puanlar için secrets.properties dosyasına TMDB anahtarını girin.¦Savjet: unesite TMDB ključ u secrets.properties da biste ovdje vidjeli opis, naslovnice i ocjene.¦Savjet: unesite TMDB ključ u secrets.properties da biste ovdje vidjeli opis, naslovnice i ocjene.¦Savjet: unesite TMDB ključ u secrets.properties da biste ovdje vidjeli opis, naslovnice i ocene.¦Consejo: añade tu clave de TMDB en secrets.properties para ver aquí descripciones, carátulas y valoraciones.¦Astuce : ajoutez votre clé TMDB dans secrets.properties pour afficher descriptions, jaquettes et notes.¦Suggerimento: inserisci la chiave TMDB in secrets.properties per vedere descrizioni, copertine e voti.
Fortsetzen {0}¦Resume {0}¦Devam et {0}¦Nastavi {0}¦Nastavi {0}¦Nastavi {0}¦Continuar {0}¦Reprendre {0}¦Riprendi {0}
Von vorn¦From start¦Baştan¦Od početka¦Od početka¦Od početka¦Desde el inicio¦Depuis le début¦Dall'inizio
Abspielen¦Play¦Oynat¦Pusti¦Reproduciraj¦Pusti¦Reproducir¦Lire¦Riproduci
Trailer¦Trailer¦Fragman¦Trailer¦Trailer¦Trailer¦Tráiler¦Bande-annonce¦Trailer
Extern¦External¦Harici¦Vanjski¦Vanjski¦Spoljni¦Externo¦Externe¦Esterno
Automatisch (beste)¦Automatic (best)¦Otomatik (en iyi)¦Automatski (najbolja)¦Automatski (najbolja)¦Automatski (najbolja)¦Automático (mejor)¦Automatique (meilleure)¦Automatico (migliore)
Qualität¦Quality¦Kalite¦Kvalitet¦Kvaliteta¦Kvalitet¦Calidad¦Qualité¦Qualità
Automatisch¦Automatic¦Otomatik¦Automatski¦Automatski¦Automatski¦Automático¦Automatique¦Automatico
Sprache¦Language¦Dil¦Jezik¦Jezik¦Jezik¦Idioma¦Langue¦Lingua
Quelle¦Source¦Kaynak¦Izvor¦Izvor¦Izvor¦Fuente¦Source¦Fonte
gilt für alle Folgen¦applies to all episodes¦tüm bölümler için geçerli¦vrijedi za sve epizode¦vrijedi za sve epizode¦vrijedi za sve epizode¦se aplica a todos los episodios¦valable pour tous les épisodes¦vale per tutti gli episodi
Staffeln¦Seasons¦Sezonlar¦Sezone¦Sezone¦Sezone¦Temporadas¦Saisons¦Stagioni
Staffel {#0}¦Season {#0}¦Sezon {#0}¦Sezona {#0}¦Sezona {#0}¦Sezona {#0}¦Temporada {#0}¦Saison {#0}¦Stagione {#0}
Episoden¦Episodes¦Bölümler¦Epizode¦Epizode¦Epizode¦Episodios¦Épisodes¦Episodi
lade Infos …¦loading info …¦bilgiler yükleniyor …¦učitavanje informacija …¦učitavanje informacija …¦učitavanje informacija …¦cargando información …¦chargement des infos …¦caricamento informazioni …
Links/Rechts = Staffel wechseln¦Left/Right = change season¦Sol/Sağ = sezon değiştir¦Lijevo/Desno = promijeni sezonu¦Lijevo/Desno = promijeni sezonu¦Levo/Desno = promijeni sezonu¦Izquierda/Derecha = cambiar temporada¦Gauche/Droite = changer de saison¦Sinistra/Destra = cambia stagione
Ähnliche Titel in deiner Playlist¦Similar titles in your playlist¦Playlistinizdeki benzer yapımlar¦Slični naslovi u tvojoj playlisti¦Slični naslovi u tvojoj playlisti¦Slični naslovi u tvojoj plejlisti¦Títulos similares en tu lista¦Titres similaires dans ta playlist¦Titoli simili nella tua playlist
Dieses Produkt verwendet die TMDB-API, wird aber nicht von TMDB unterstützt oder zertifiziert.¦This product uses the TMDB API but is not endorsed or certified by TMDB.¦Bu ürün TMDB API'sini kullanır, ancak TMDB tarafından onaylanmamış veya sertifikalandırılmamıştır.¦Ovaj proizvod koristi TMDB API, ali ga TMDB ne podržava niti certificira.¦Ovaj proizvod koristi TMDB API, ali ga TMDB ne podržava niti certificira.¦Ovaj proizvod koristi TMDB API, ali ga TMDB ne podržava niti certificira.¦Este producto utiliza la API de TMDB, pero TMDB no lo respalda ni certifica.¦Ce produit utilise l'API TMDB mais n'est ni approuvé ni certifié par TMDB.¦Questo prodotto utilizza l'API di TMDB ma non è approvato né certificato da TMDB.
Raster anpassen¦Adjust grid¦Izgarayı ayarla¦Prilagodi mrežu¦Prilagodi mrežu¦Prilagodi mrežu¦Ajustar cuadrícula¦Ajuster la grille¦Regola griglia
Spalten¦Columns¦Sütunlar¦Stupci¦Stupci¦Kolone¦Columnas¦Colonnes¦Colonne
Zeilen¦Rows¦Satırlar¦Redovi¦Redovi¦Redovi¦Filas¦Lignes¦Righe
Die Ansicht dahinter ändert sich sofort. Zurück schließt.¦The view behind changes immediately. Back closes.¦Arkadaki görünüm hemen değişir. Geri kapatır.¦Prikaz iza mijenja se odmah. Nazad zatvara.¦Prikaz iza mijenja se odmah. Natrag zatvara.¦Prikaz iza menja se odmah. Nazad zatvara.¦La vista de fondo cambia al instante. Atrás cierra.¦L'affichage derrière change immédiatement. Retour ferme.¦La vista dietro cambia subito. Indietro chiude.
Keine EPG-Adresse gefunden¦No EPG address found¦EPG adresi bulunamadı¦EPG adresa nije pronađena¦EPG adresa nije pronađena¦EPG adresa nije pronađena¦No se encontró dirección EPG¦Aucune adresse EPG trouvée¦Nessun indirizzo EPG trovato
Playlist enthält keine Sender-IDs (tvg-id)¦Playlist contains no channel IDs (tvg-id)¦Playlist kanal kimliği içermiyor (tvg-id)¦Playlista ne sadrži ID-ove kanala (tvg-id)¦Playlista ne sadrži ID-ove kanala (tvg-id)¦Plejlista ne sadrži ID-ove kanala (tvg-id)¦La lista no contiene ID de canal (tvg-id)¦La playlist ne contient pas d'ID de chaîne (tvg-id)¦La playlist non contiene ID dei canali (tvg-id)
Keine Programmdaten erhalten ({0})¦No guide data received ({0})¦Program verisi alınamadı ({0})¦Podaci programa nisu primljeni ({0})¦Podaci programa nisu primljeni ({0})¦Podaci programa nisu primljeni ({0})¦No se recibieron datos de programación ({0})¦Aucune donnée de programme reçue ({0})¦Nessun dato di programmazione ricevuto ({0})
Geladen: {#0} Sender mit Programm¦Loaded: {#0} channels with guide data¦Yüklendi: programı olan {#0} kanal¦Učitano: {#0} kanala s programom¦Učitano: {#0} kanala s programom¦Učitano: {#0} kanala s programom¦Cargado: {#0} canales con programación¦Chargé : {#0} chaînes avec programme¦Caricati: {#0} canali con programma
Noch nicht geladen¦Not loaded yet¦Henüz yüklenmedi¦Još nije učitano¦Još nije učitano¦Još nije učitano¦Aún sin cargar¦Pas encore chargé¦Non ancora caricato
Wird geladen …¦Loading …¦Yükleniyor …¦Učitavanje …¦Učitavanje …¦Učitavanje …¦Cargando …¦Chargement …¦Caricamento …
Intro überspringen¦Skip intro¦Girişi atla¦Preskoči uvod¦Preskoči uvod¦Preskoči uvod¦Saltar intro¦Passer l'intro¦Salta intro
Rückblick überspringen¦Skip recap¦Önceki bölümleri atla¦Preskoči sažetak¦Preskoči sažetak¦Preskoči rezime¦Saltar resumen¦Passer le résumé¦Salta riepilogo
Abspann überspringen¦Skip credits¦Jeneriği atla¦Preskoči odjavnu špicu¦Preskoči odjavnu špicu¦Preskoči odjavnu špicu¦Saltar créditos¦Passer le générique¦Salta titoli di coda
Titel-Infos¦Title info¦Yapım bilgileri¦Informacije o naslovima¦Informacije o naslovima¦Informacije o naslovima¦Información de títulos¦Infos sur les titres¦Info sui titoli
Cover, Beschreibungen, Bewertungen¦Covers, descriptions, ratings¦Kapaklar, açıklamalar, puanlar¦Naslovnice, opisi, ocjene¦Naslovnice, opisi, ocjene¦Naslovnice, opisi, ocene¦Carátulas, descripciones, valoraciones¦Jaquettes, descriptions, notes¦Copertine, descrizioni, voti
Schauspieler, Regie, ähnliche Titel¦Actors, directors, similar titles¦Oyuncular, yönetmenler, benzer yapımlar¦Glumci, redatelji, slični naslovi¦Glumci, redatelji, slični naslovi¦Glumci, reditelji, slični naslovi¦Actores, directores, títulos similares¦Acteurs, réalisateurs, titres similaires¦Attori, registi, titoli simili
Playlist-Zwischenspeicher¦Playlist cache¦Playlist önbelleği¦Priručna memorija playliste¦Priručna memorija playliste¦Priručna memorija plejliste¦Caché de listas¦Cache des playlists¦Cache delle playlist
Schneller Start ohne Wartezeit¦Fast start without waiting¦Beklemeden hızlı başlangıç¦Brzi start bez čekanja¦Brzi start bez čekanja¦Brzi start bez čekanja¦Inicio rápido sin esperas¦Démarrage rapide sans attente¦Avvio rapido senza attese
Bilder¦Images¦Görseller¦Slike¦Slike¦Slike¦Imágenes¦Images¦Immagini
Zwischengespeicherte Cover und Hintergründe¦Cached covers and backgrounds¦Önbelleğe alınmış kapaklar ve arka planlar¦Spremljene naslovnice i pozadine¦Spremljene naslovnice i pozadine¦Sačuvane naslovnice i pozadine¦Carátulas y fondos en caché¦Jaquettes et arrière-plans en cache¦Copertine e sfondi in cache
"""

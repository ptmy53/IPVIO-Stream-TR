package com.ipvio.stream

// Polish translations (part 1/6): German-Polish rows, in addition to the languages in DictA–O
val DictPl1 = """
Playlist hinzufügen¦Dodaj playlistę
Dauert nur ein paar Sekunden.¦To potrwa tylko kilka sekund.
Aus Zwischenablage einfügen¦Wklej ze schowka
Server (z. B. http://server.tld:8080)¦Serwer (np. http://server.tld:8080)
Benutzername¦Nazwa użytkownika
Passwort¦Hasło
Passwort verbergen¦Ukryj hasło
Passwort anzeigen¦Pokaż hasło
Backup-Server {#0} (optional)¦Serwer zapasowy {#0} (opcjonalnie)
+ Backup-Server¦+ Serwer zapasowy
Fällt der Server aus, wechselt die App von selbst zu einem Backup-Server. Anbieter nennen sie oft als weitere Server- oder DNS-Adressen; Benutzername und Passwort bleiben gleich.¦Jeśli serwer przestanie działać, aplikacja sama przełączy się na serwer zapasowy. Dostawcy często podają je jako dodatkowe adresy serwerów lub DNS; nazwa użytkownika i hasło pozostają takie same.
Backup-Server ungültig: Adresse wie http://server.tld:8080 eingeben.¦Nieprawidłowy serwer zapasowy: wpisz adres w formacie http://server.tld:8080.
Backup-Server¦Serwery zapasowe
Backup-Server: {#0}¦Serwery zapasowe: {#0}
Backup-Server speichern¦Zapisz serwery zapasowe
Erreichbar¦Osiągalny
Nicht erreichbar seit {0}¦Nieosiągalny od {0}
Nicht erreichbar: {0}¦Nieosiągalny: {0}
Hauptserver: {0}¦Serwer główny: {0}
Hinzufügen¦Dodaj
Abbrechen¦Anuluj
Alle¦Wszystko
Alle anzeigen¦Pokaż wszystko
Neu hinzugefügt¦Ostatnio dodane
Beliebtheit¦Popularność
Playlist¦Playlista
vor 1990¦przed 1990
{#0} Titel¦Tytuły: {#0}
{0} werden geladen … {#1} / {#2}¦Wczytywanie {0} … {#1} / {#2}
{0} laden … {#1}/{#2}¦Wczytywanie {0} … {#1}/{#2}
Metadaten¦Metadane
Länder¦Kraje
Zurück = Filter¦Wstecz = filtry
Sortierung¦Sortuj
Rating¦Ocena
Jahr¦Rok
Genre¦Gatunek
Land¦Kraj
Zurücksetzen¦Resetuj
Raster¦Siatka
Kino¦Kino
Ansicht¦Widok
Keine Treffer – Filter zurücksetzen.¦Brak wyników – zresetuj filtry.
Liste¦Lista
Besetzung¦Obsada
Regie¦Reżyseria
Erstellt von¦Twórcy
Schauspiel¦Aktorstwo
Drehbuch¦Scenariusz
geboren {0}¦ur. {0}
Weniger¦Mniej
Mehr lesen¦Czytaj więcej
Infos konnten nicht geladen werden.¦Nie udało się wczytać informacji.
Keine Titel in deiner Playlist¦Brak tytułów w Twojej playliście
In deiner Playlist¦W Twojej playliście
{#0} weitere Titel dieser Person sind nicht in deiner Playlist.¦Kolejne tytuły tej osoby spoza Twojej playlisty: {#0}
Lade …¦Wczytywanie …
Zuletzt geschaut¦Ostatnio oglądane
Meine Liste¦Moja lista
In Meiner Liste¦Na mojej liście
Filter¦Filtry
Standard¦Domyślnie
Kacheln¦Kafelki
{#0} Kategorien¦Kategorie: {#0}
Kategorien¦Kategorie
Sonstige¦Inne
Anime¦Anime
Animation¦Animacja
Film¦Film
{#0} Staffel(n)¦Sezony: {#0}
{#0} Min¦{#0} min
bei {#0} Min¦w {#0}. min
TMDB antwortet mit Fehler {#0}¦TMDB zwróciło błąd {#0}
Keine Verbindung zu TMDB ({0})¦Brak połączenia z TMDB ({0})
Besetzung nicht lesbar ({0})¦Nie udało się odczytać obsady ({0})
Besetzung nicht verfügbar: {0}¦Obsada niedostępna: {0}
Keine App zum Öffnen des Trailers gefunden¦Nie znaleziono aplikacji do otwarcia zwiastuna
Externer Player¦Zewnętrzny odtwarzacz
Kein externer Player installiert (z. B. VLC)¦Brak zainstalowanego zewnętrznego odtwarzacza (np. VLC)
Keine Beschreibung gefunden.¦Nie znaleziono opisu.
Tipp: TMDB-Schlüssel in secrets.properties eintragen, dann erscheinen hier Beschreibung, Cover und Bewertungen.¦Wskazówka: dodaj swój klucz TMDB w secrets.properties, aby widzieć tu opisy, okładki i oceny.
Fortsetzen {0}¦Wznów {0}
Von vorn¦Od początku
Abspielen¦Odtwórz
Trailer¦Zwiastun
Extern¦Zewnętrznie
Automatisch (beste)¦Automatycznie (najlepsza)
Qualität¦Jakość
Automatisch¦Automatycznie
Sprache¦Język
Quelle¦Źródło
gilt für alle Folgen¦dotyczy wszystkich odcinków
Staffeln¦Sezony
Staffel {#0}¦Sezon {#0}
Episoden¦Odcinki
lade Infos …¦wczytywanie informacji …
Links/Rechts = Staffel wechseln¦Lewo/Prawo = zmiana sezonu
Ähnliche Titel in deiner Playlist¦Podobne tytuły w Twojej playliście
Dieses Produkt verwendet die TMDB-API, wird aber nicht von TMDB unterstützt oder zertifiziert.¦Ten produkt korzysta z API TMDB, ale nie jest wspierany ani certyfikowany przez TMDB.
Raster anpassen¦Dostosuj siatkę
Spalten¦Kolumny
Zeilen¦Wiersze
Die Ansicht dahinter ändert sich sofort. Zurück schließt.¦Widok w tle zmienia się od razu. Wstecz zamyka.
Keine EPG-Adresse gefunden¦Nie znaleziono adresu EPG
Playlist enthält keine Sender-IDs (tvg-id)¦Playlista nie zawiera identyfikatorów kanałów (tvg-id)
Keine Programmdaten erhalten ({0})¦Nie otrzymano danych programu TV ({0})
Geladen: {#0} Sender mit Programm¦Wczytano: kanały z programem TV: {#0}
Noch nicht geladen¦Jeszcze nie wczytano
Wird geladen …¦Wczytywanie …
Intro überspringen¦Pomiń czołówkę
Rückblick überspringen¦Pomiń streszczenie
Abspann überspringen¦Pomiń napisy końcowe
Titel-Infos¦Informacje o tytułach
Cover, Beschreibungen, Bewertungen¦Okładki, opisy, oceny
Schauspieler, Regie, ähnliche Titel¦Aktorzy, reżyserzy, podobne tytuły
Playlist-Zwischenspeicher¦Pamięć podręczna playlisty
Schneller Start ohne Wartezeit¦Szybki start bez czekania
Bilder¦Obrazy
Zwischengespeicherte Cover und Hintergründe¦Zapisane okładki i tła
Noch einmal Zurück drücken zum Beenden¦Naciśnij Wstecz ponownie, aby wyjść
Die Playlist konnte nicht geladen werden¦Nie udało się wczytać playlisty
Noch keine Sender¦Jeszcze brak kanałów
Prüfe die Internetverbindung, die Adresse und die Zugangsdaten.¦Sprawdź połączenie z internetem, adres i dane logowania.
Erneut versuchen¦Spróbuj ponownie
Im Trend¦Na czasie
Top 10¦Top 10
Top bewertet¦Najwyżej oceniane
Diese Playlist enthält keine Filme oder Serien. Live TV findest du links.¦Ta playlista nie zawiera filmów ani seriali. Telewizję na żywo znajdziesz po lewej.
Filme¦Filmy
Serien¦Seriale
Alle {#0}¦Wszystkie ({#0})
Alle {0}¦Wszystkie {0}
Keine {0} in dieser Playlist¦{0} – brak w tej playliście
Weitere Kategorien findest du unter „Kategorien“ oben.¦Więcej kategorii znajdziesz powyżej w „Kategorie”.
Filme, Serien oder Sender suchen…¦Szukaj filmów, seriali lub kanałów…
Letzte Suchanfragen¦Ostatnie wyszukiwania
Verlauf löschen¦Wyczyść historię
Keine Treffer für „{0}“¦Brak wyników dla „{0}”
Suche „{0}“¦Szukaj „{0}”
Live TV¦TV na żywo
Weiterschauen¦Oglądaj dalej
Mehr Infos¦Więcej informacji
Favoriten¦Ulubione
Alle Sender¦Wszystkie kanały
Noch keine Favoriten. Sender mit OK (gedrückt halten) oder der Menü-Taste als Favorit markieren.¦Brak ulubionych. Oznacz kanały jako ulubione, przytrzymując OK lub naciskając klawisz Menu.
Keine Sender¦Brak kanałów
Violett & Türkis¦Fiolet i turkus
Ozean¦Ocean
Smaragd¦Szmaragd
Sonnenuntergang¦Zachód słońca
Rosé¦Róż
Gold¦Złoto
Orange¦Pomarańcz
Eisblau¦Lodowy błękit
Limette¦Limonka
Purpur¦Purpura
Schiefer¦Łupek
Mitternacht¦Północ
OLED-Schwarz¦Czerń OLED
Graphit¦Grafit
Tiefblau¦Głęboki błękit
Aus¦Wył.
An¦Wł.
Dezent¦Subtelne
Normal¦Normalne
Endet um {0}¦Koniec o {0}
Klein¦Mały
Mittel¦Średni
Groß¦Duży
Kompakt¦Kompaktowy
Jetzt¦Teraz
Jetzt {0}¦Teraz {0}
TV-Guide – Ansicht¦Program TV – widok
Zoom¦Powiększenie
Zeilenhöhe¦Wysokość wiersza
Vergangene Sendungen¦Minione audycje
Zurück = schließen¦Wstecz = zamknij
Menü-Taste im TV-Guide = Zoom¦Klawisz Menu w programie TV = powiększenie
Zeitzone {0} (änderbar unter Einstellungen > Sprache & Region)¦Strefa czasowa {0} (zmień w Ustawienia > Język i region)
Sendungen¦Audycje
Sender¦Kanały
Menü¦Menu
Zurück¦Wstecz
in {#0} Min¦za {#0} min
noch {#0} Min¦zostało {#0} min
Keine Beschreibung vorhanden.¦Brak opisu.
TV-Guide¦Program TV
Wähle eine Sendung mit den Pfeiltasten, OK schaltet auf den Sender.¦Wybierz audycję strzałkami, OK przełącza na kanał.
Zurück bringt dich zur Senderspalte, nochmal Zurück schließt.¦Wstecz przenosi do kolumny kanałów, naciśnij Wstecz ponownie, aby zamknąć.
lädt …¦wczytywanie …
Kein Programm verfügbar¦Brak danych programu TV
Kein Programm für diesen Sender¦Brak programu TV dla tego kanału
Danach {0}¦Następnie: {0}
OK halten = Kategorie als Favorit¦Przytrzymaj OK = ulubiona kategoria
OK = ansehen¦OK = oglądaj
halten = Favorit¦przytrzymaj = ulubiony
Mono¦Mono
Stereo¦Stereo
{#0} Kanäle¦Kanały: {#0}
Spur {#0}¦Ścieżka {#0}
1× (Normal)¦1× (normalnie)
Zugang abgelehnt ({#0}). Benutzername, Passwort oder Abo beim Anbieter prüfen.¦Odmowa dostępu ({#0}). Sprawdź u dostawcy nazwę użytkownika, hasło lub subskrypcję.
Dieser Sender ist beim Anbieter nicht mehr vorhanden.¦Ten kanał nie istnieje już u dostawcy.
Dieser Titel ist beim Anbieter nicht mehr vorhanden.¦Ten tytuł nie istnieje już u dostawcy.
Maximale Verbindungen des Anbieters erreicht. Anderes Gerät oder Profil beenden und erneut versuchen.¦Osiągnięto maksymalną liczbę połączeń u dostawcy. Zatrzymaj inne urządzenie lub profil i spróbuj ponownie.
Zu viele gleichzeitige Verbindungen. Anderes Gerät beenden und erneut versuchen.¦Zbyt wiele jednoczesnych połączeń. Zatrzymaj inne urządzenie i spróbuj ponownie.
Der Anbieter hat gerade ein Problem ({#0}). Später erneut versuchen.¦Dostawca ma teraz problem ({#0}). Spróbuj później.
Der Anbieter lehnt die Wiedergabe ab ({#0}).¦Dostawca odmawia odtwarzania ({#0}).
Keine Verbindung zum Anbieter. Internet und Netzwerk prüfen.¦Brak połączenia z dostawcą. Sprawdź internet i sieć.
Dieses Videoformat kann das Gerät nicht abspielen.¦To urządzenie nie obsługuje tego formatu wideo.
Der Stream ist fehlerhaft oder wird nicht unterstützt.¦Strumień jest uszkodzony lub nieobsługiwany.
Wiedergabe nicht möglich.¦Odtwarzanie niemożliwe.
Audio & Untertitel¦Audio i napisy
Links schließt das Menü¦Lewo zamyka menu
Rechts wechselt zu den Untertiteln¦Prawo przełącza na napisy
Audio¦Audio
Keine Auswahl verfügbar¦Brak opcji do wyboru
Untertitel¦Napisy
Dieser Stream enthält keine Untertitel.¦Ten strumień nie ma napisów.
Aussehen der Untertitel¦Wygląd napisów
Folgen¦Odcinki
Folge {#0}¦Odcinek {#0}
Einpassen¦Dopasuj
Zoom (Rand abschneiden)¦Powiększ (przytnij krawędzie)
Strecken¦Rozciągnij
Verbindung unterbrochen – neuer Versuch ({#0}/3)¦Utracono połączenie – ponawianie ({#0}/3)
Intro¦Czołówka
So sehen deine Untertitel aus¦Tak wyglądają Twoje napisy
Senderliste¦Lista kanałów
Aus Favoriten entfernen¦Usuń z ulubionych
Zu Favoriten hinzufügen¦Dodaj do ulubionych
Bildformat¦Proporcje obrazu
Extern öffnen¦Otwórz zewnętrznie
Hoch und Runter wechseln den Sender¦Góra i Dół zmieniają kanał
Zeitleiste¦Oś czasu
= {#0} s springen¦= przeskok o {#0} s
Halten = weiter springen¦Przytrzymaj = przewijaj dalej
Vorherige Folge: Staffel {#0}, Folge {#1}¦Poprzedni odcinek: sezon {#0}, odcinek {#1}
Vorherige Folge¦Poprzedni odcinek
Nächste Folge: Staffel {#0}, Folge {#1}¦Następny odcinek: sezon {#0}, odcinek {#1}
Nächste Folge¦Następny odcinek
{#0} Sekunden zurück¦{#0} s do tyłu
Pause¦Pauza
{#0} Sekunden vor¦{#0} s do przodu
Einstellungen¦Ustawienia
Links = zurück¦Lewo = wstecz
Größe¦Rozmiar
Farbe¦Kolor
Hintergrund¦Tło
Rand¦Obramowanie
OK = nächster Wert. Die Vorschau zeigt das Ergebnis.¦OK = następna wartość. Podgląd pokazuje efekt.
Videoqualität¦Jakość wideo
Keine Auswahl verfügbar (feste Qualität).¦Brak opcji do wyboru (stała jakość).
Sprungweite¦Długość przeskoku
Gilt für Links/Rechts, Halten und die Zeitleiste.¦Dotyczy Lewo/Prawo, przytrzymania i osi czasu.
Nächste Folge automatisch¦Następny odcinek automatycznie
Endzeit anzeigen¦Pokaż godzinę zakończenia
Startet in {0}¦Start za {0}
Server antwortet mit Fehler {#0}¦Serwer zwrócił błąd {#0}
Der Server hat keine Daten geliefert¦Serwer nie zwrócił danych
Keine Sender in der Playlist gefunden¦Nie znaleziono kanałów w playliście
Unbekannter Fehler¦Nieznany błąd
Allgemein¦Ogólne
Start, Beenden, Autostart¦Start, wyjście, autostart
Sprache & Region¦Język i region
Sprache, Uhrzeit, Zeitzone¦Język, godzina, strefa czasowa
Kartengröße, Raster¦Rozmiar kart, siatka
Design¦Wygląd
Farben, Schrift, Animationen¦Kolory, czcionka, animacje
Reihenfolge und Einträge¦Kolejność i pozycje
Player¦Odtwarzacz
Spulen, Intro, Bildformat¦Przewijanie, czołówka, proporcje obrazu
Ton & Untertitel¦Audio i napisy
Sprachen, Untertitel-Stil¦Języki, styl napisów
Genres & Kategorien¦Gatunki i kategorie
Automatisch oder vom Anbieter¦Automatycznie lub od dostawcy
Live-TV & Guide¦TV na żywo i program TV
TV-Programm, Favoriten¦Program TV, ulubione
Playlists¦Playlisty
Verwalten, Aktualisieren¦Zarządzanie, aktualizacja
Jugendschutz¦Kontrola rodzicielska
PIN, Inhalte ab 18¦PIN, treści dla dorosłych
Speicher¦Pamięć
Cache, Verlauf, Fortsetzen¦Pamięć podręczna, historia, wznawianie
Diagnose¦Diagnostyka
Status und Fehlersuche¦Stan i rozwiązywanie problemów
Über IPVIO Stream¦O IPVIO Stream
Version, Quellen, Zurücksetzen¦Wersja, źródła, reset
Wirklich? OK zum Bestätigen¦Na pewno? OK, aby potwierdzić
‹ Zurück¦‹ Wstecz
Automatisch (Gerät)¦Automatycznie (urządzenie)
Sprache der App¦Język aplikacji
Alle Menüs und Hinweise erscheinen in dieser Sprache. Beschreibungen, Besetzung und Titel-Infos kommen von TMDB und werden in derselben Sprache geladen, sobald du einen Titel öffnest. Namen von Sendern, Filmen und Kategorien stammen von deinem Anbieter und bleiben unverändert.¦Wszystkie menu i podpowiedzi będą w tym języku. Opisy, obsada i informacje o tytułach pochodzą z TMDB i są wczytywane w tym samym języku po otwarciu tytułu. Nazwy kanałów, filmów i kategorii pochodzą od dostawcy i pozostają bez zmian.
Uhrzeit¦Godzina
Zeitformat¦Format godziny
24 Stunden (14:30)¦24-godzinny (14:30)
12 Stunden (2:30 PM)¦12-godzinny (2:30 PM)
Zeitzone¦Strefa czasowa
Systemzeit (Gerät: {0})¦Czas systemowy (urządzenie: {0})
Uhrzeit jetzt¦Aktualna godzina
Gilt für alle Uhrzeiten, zum Beispiel im TV-Guide und in der Live-TV-Infoleiste. „Systemzeit“ nutzt die Einstellung deines Geräts. Zeigt die App Uhrzeiten falsch an, steht dein Fernseher vermutlich auf UTC; wähle dann hier deine Zeitzone.¦Dotyczy wszystkich godzin, np. w programie TV i na pasku informacji TV na żywo. „Czas systemowy” używa ustawienia urządzenia. Jeśli aplikacja pokazuje złe godziny, telewizor jest prawdopodobnie ustawiony na UTC; wtedy wybierz tutaj swoją strefę czasową.
PIN eingeben¦Wpisz PIN
Die Jugendschutz-Einstellungen sind mit einer PIN geschützt.¦Ustawienia kontroli rodzicielskiej są chronione kodem PIN.
Falsche PIN¦Nieprawidłowy PIN
Neue PIN festlegen¦Ustaw nowy PIN
Wähle eine 4-stellige PIN.¦Wybierz 4-cyfrowy PIN.
PIN bestätigen¦Potwierdź PIN
Gib die PIN noch einmal ein.¦Wpisz PIN ponownie.
Die PINs stimmen nicht überein¦Kody PIN nie są zgodne
Inhalte für Erwachsene ausblenden¦Ukryj treści dla dorosłych
Blendet Kategorien und Titel mit Begriffen wie XXX, Adult oder 18+ in der ganzen App aus: Startseite, Suche, Listen und Live-TV.¦Ukrywa w całej aplikacji kategorie i tytuły zawierające słowa takie jak XXX, Adult czy 18+: na stronie głównej, w wyszukiwaniu, na listach i w TV na żywo.
Der Schutz ist aktiv.¦Ochrona jest aktywna.
{#0} Einträge in deiner Playlist würden ausgeblendet.¦Pozycje w Twojej playliście, które zostałyby ukryte: {#0}
Kein Erwachsenen-Inhalt erkannt.¦Nie wykryto treści dla dorosłych.
PIN¦PIN
PIN ist gesetzt¦PIN jest ustawiony
Keine PIN gesetzt¦Nie ustawiono PIN-u
PIN ändern¦Zmień PIN
PIN festlegen¦Ustaw PIN
PIN entfernen¦Usuń PIN
Mit einer PIN lässt sich der Schutz nur nach Eingabe der PIN ändern. Die Erkennung arbeitet mit Stichwörtern in Kategorie- und Titelnamen und ist kein vollständiger Filter. Beim Zurücksetzen aller Einstellungen bleibt die PIN erhalten.¦Z kodem PIN ochronę można zmienić dopiero po jego wpisaniu. Wykrywanie opiera się na słowach kluczowych w nazwach kategorii i tytułów i nie jest pełnym filtrem. PIN zostaje zachowany przy resecie wszystkich ustawień.
Start¦Start
Beim Einschalten des Geräts öffnen¦Otwieraj przy starcie urządzenia
IPVIO Stream startet automatisch, sobald der Fernseher oder die Box hochgefahren ist.¦IPVIO Stream uruchamia się automatycznie, gdy tylko telewizor lub przystawka się włączy.
Berechtigung erteilt¦Uprawnienie przyznane
Berechtigung „Über anderen Apps einblenden“ fehlt¦Brak uprawnienia „Wyświetlanie nad innymi aplikacjami”
Einstellung öffnen¦Otwórz ustawienie
Prüfen¦Sprawdź
Ab Android 10 erlaubt das System den Autostart nur mit dieser Berechtigung. Manche Geräte (z. B. einzelne Fire-TV-Modelle) blockieren den Autostart trotzdem; das kann die App nicht ändern.¦Od Androida 10 system pozwala na autostart tylko z tym uprawnieniem. Niektóre urządzenia (np. część modeli Fire TV) i tak blokują autostart; aplikacja nie może tego zmienić.
Suche¦Szukaj
Startseite¦Strona startowa
Diese Seite öffnet sich beim Start der App. Versteckte Menüpunkte sind nicht wählbar.¦Ta strona otwiera się przy starcie aplikacji. Ukrytych pozycji menu nie można wybrać.
Letzten Sender beim Start abspielen¦Odtwarzaj ostatni kanał przy starcie
Wenn du zuletzt Live TV gesehen hast, läuft dieser Sender direkt nach dem Start.¦Jeśli ostatnio oglądałeś TV na żywo, ten kanał zacznie grać zaraz po starcie.
Bedienung¦Sterowanie
Beenden bestätigen¦Potwierdzaj wyjście
Auf der Startseite muss „Zurück“ zweimal gedrückt werden, damit die App nicht aus Versehen schließt.¦Na stronie głównej trzeba dwa razy nacisnąć „Wstecz”, żeby aplikacja nie zamknęła się przypadkiem.
Dieses Gerät bietet die Einstellung nicht an. Autostart funktioniert evtl. trotzdem.¦To urządzenie nie oferuje tego ustawienia. Autostart może mimo to działać.
Zwischenspeicher¦Pamięć podręczna
Leeren¦Wyczyść
Gesamt¦Razem
Geleerte Daten lädt die App bei Bedarf neu. Titel-Infos und Bilder erscheinen danach kurz später. Der Playlist-Zwischenspeicher macht den Start schnell; ohne ihn wartet die App auf den Download.¦Aplikacja w razie potrzeby wczyta wyczyszczone dane ponownie. Informacje o tytułach i obrazy pojawią się potem nieco później. Pamięć podręczna playlisty przyspiesza start; bez niej aplikacja czeka na pobranie.
Verlauf¦Historia
Suchverlauf löschen¦Wyczyść historię wyszukiwania
Zuletzt geschaut löschen¦Wyczyść ostatnio oglądane
Weiterschauen löschen¦Wyczyść „Oglądaj dalej”
Meine Liste und Favoriten bleiben immer erhalten.¦Moja lista i ulubione są zawsze zachowywane.
Filme, Serien und Sender¦Filmy, seriale i kanały
Raster zeigt viele Titel auf einmal. Kino zeigt oben die Details zum markierten Titel und unten eine Reihe zum Durchblättern.¦Siatka pokazuje wiele tytułów naraz. Kino pokazuje u góry szczegóły wybranego tytułu, a pod spodem wiersz do przeglądania.
Spalten (Raster)¦Kolumny (siatka)
Zeilen (Raster)¦Wiersze (siatka)
Banner wechselt¦Zmiana banera
nie¦nigdy
alle {#0} s¦co {#0} s
Kartengröße (Reihen)¦Rozmiar kart (wiersze)
Kategorien-Ansicht¦Widok kategorii
Liste mit Vorschau¦Lista z podglądem
Das Raster kannst du zusätzlich direkt in der Rasteransicht mit dem Raster-Knopf ändern.¦Siatkę możesz też zmienić bezpośrednio w widoku siatki przyciskiem siatki.
Verschiebe Einträge mit den Pfeil-Knöpfen oder blende sie aus. Start und Einstellungen bleiben immer sichtbar.¦Przesuwaj pozycje przyciskami strzałek albo je ukryj. Start i Ustawienia są zawsze widoczne.
versteckt¦ukryte
sichtbar¦widoczne
Standard-Reihenfolge wiederherstellen¦Przywróć domyślną kolejność
Sprung links/rechts¦Przeskok w lewo/prawo
Standard-Bildformat¦Domyślne proporcje obrazu
Intro überspringen anbieten¦Proponuj pominięcie czołówki
Zeigt bei Serien einen Knopf „Intro überspringen“, wenn Intro-Zeiten bekannt sind.¦Pokazuje w serialach przycisk „Pomiń czołówkę”, gdy znane są czasy czołówki.
Stream-Info am Start¦Informacje o strumieniu na starcie
Zeigt kurz Auflösung, Ton und Format beim Abspielen von Filmen und Serien.¦Przy odtwarzaniu filmów i seriali na chwilę pokazuje rozdzielczość, audio i format.
Steuerleiste ausblenden nach¦Ukryj pasek sterowania po
Startet die nächste Folge nach dem Abspann von selbst.¦Po napisach końcowych sam uruchamia następny odcinek.
Zeigt, um wie viel Uhr der Film voraussichtlich endet.¦Pokazuje, o której film powinien się skończyć.
Intro automatisch überspringen¦Pomijaj czołówkę automatycznie
Überspringt bekannte Intros ohne Rückfrage.¦Pomija znane czołówki bez pytania.
Bedienung: OK = Pause, Links/Rechts = Sprung (Halten = weiter springen), mehrere Sprünge werden gesammelt, Zurück beim Spulen = abbrechen, Hoch = Zeitleiste, Runter = Steuerleiste. Intro-Zeiten stammen aus der Community-Datenbank TheIntroDB und gibt es nicht für jede Serie. Die Stream-Info erscheint nur bei Filmen und Serien.¦Sterowanie: OK = pauza, Lewo/Prawo = przeskok (przytrzymaj = przewijaj dalej), kolejne przeskoki się sumują, Wstecz podczas przewijania = anuluj, Góra = oś czasu, Dół = pasek sterowania. Czasy czołówek pochodzą ze społecznościowej bazy TheIntroDB i nie są dostępne dla każdego serialu. Informacje o strumieniu pojawiają się tylko dla filmów i seriali.
Farbthema¦Motyw kolorystyczny
Eigene Farbe¦Własny kolor
Übernehmen¦Zastosuj
Ungültig¦Nieprawidłowy
Wähle ein Farbfeld oder gib einen Hex-Code ein, z. B. #FF6A00. Die App passt die Farbe automatisch so an, dass Schrift immer gut lesbar bleibt.¦Wybierz próbkę lub wpisz kod hex, np. #FF6A00. Aplikacja automatycznie dopasuje kolor, aby tekst zawsze był czytelny.
Darstellung¦Wygląd
Animationen¦Animacje
Anzeigegröße¦Rozmiar wyświetlania
Oberfläche aktuell ×¦Interfejs obecnie ×
(Bildschirm¦(ekran
dp breit). Standard ist 85 %: mehr Inhalt, Elemente bleiben gut lesbar. „Automatisch“ vergrößert die App auf sehr breiten Bildschirmen.¦dp szerokości). Domyślnie 85%: więcej treści, elementy pozostają czytelne. „Automatycznie” powiększa aplikację na bardzo szerokich ekranach.
Schrift¦Czcionka
Schriftart¦Krój czcionki
Schriftgröße¦Rozmiar czcionki
Aa – So sieht deine Schrift aus¦Aa – tak wygląda Twoja czcionka
Fünf klare Systemschriften, die auf jedem Gerät vorhanden sind. Die Schriftgröße wirkt zusätzlich zur Anzeigegröße.¦Pięć przejrzystych czcionek systemowych dostępnych na każdym urządzeniu. Rozmiar czcionki działa dodatkowo do rozmiaru wyświetlania.
Vorschau¦Podgląd
Aktiv¦Aktywne
Hervorhebung¦Wyróżnienie
Animationen „Aus“ schaltet die Vergrößerung beim Fokus ab, „Dezent“ macht sie schwächer. Das schont schwache Geräte.¦Animacje „Wył.” wyłączają powiększenie przy fokusie, „Subtelne” je osłabiają. To odciąża słabsze urządzenia.
Design zurücksetzen¦Resetuj wygląd
keiner seit dem Start¦brak od startu
Version¦Wersja
Bildschirm¦Ekran
{0} × {1} dp · Dichte¦{0} × {1} dp · gęstość
Inhalte¦Zawartość
{0} Sender · {1} Filme · {2} Serien¦Kanały: {0} · filmy: {1} · seriale: {2}
Ladezustand¦Stan wczytywania
Programm (EPG)¦Program TV (EPG)
{0} · {1} Sender¦{0} · kanały: {1}
lädt {0}/{1}¦wczytywanie {0}/{1}
kein TMDB-Schlüssel¦brak klucza TMDB
Letzter Fehler¦Ostatni błąd
Fehler seit Start¦Błędy od startu
Playlist neu laden¦Wczytaj playlistę ponownie
Bericht kopieren¦Kopiuj raport
IPVIO Diagnose¦Diagnostyka IPVIO
Der Bericht enthält keine Adressen, Benutzernamen oder Passwörter. Er wird nur in die Zwischenablage kopiert und nirgendwohin gesendet.¦Raport nie zawiera adresów, nazw użytkowników ani haseł. Jest tylko kopiowany do schowka i nigdzie nie jest wysyłany.
Tonspur¦Ścieżka audio
Automatische Genres¦Automatyczne gatunki
Aus = Kategorien deines Anbieters¦Wył. = kategorie dostawcy
Automatische Genres sortieren Titel nach Genre (Anime = Animation aus Japan). Beim ersten Mal dauert das einige Minuten, danach ist alles gespeichert. Aus = die Kategorien deines Anbieters in der Reihenfolge deines Anbieters. In der Ansicht „Kategorien“ kannst du jederzeit zwischen beiden wechseln.¦Automatyczne gatunki sortują tytuły według gatunku (Anime = animacja z Japonii). Za pierwszym razem trwa to kilka minut, potem wszystko jest zapisane. Wył. = kategorie dostawcy w jego kolejności. W widoku „Kategorie” możesz w każdej chwili przełączać się między nimi.
Automatische Genres brauchen den TMDB-Schlüssel in Config.kt.¦Automatyczne gatunki wymagają klucza TMDB w Config.kt.
TV-Programm (EPG)¦Program TV (EPG)
Programm bei {0} von {1} Sendern möglich¦Program TV dostępny dla {0} z {1} kanałów
XMLTV: {0}¦XMLTV: {0}
Die App sucht die Programmdaten automatisch (Xtream-Schnittstelle oder XMLTV-Adresse aus der Playlist). Wenn dein Anbieter eine eigene XMLTV-Adresse nennt, trage sie hier ein.¦Aplikacja automatycznie znajduje dane programu TV (interfejs Xtream lub adres XMLTV z playlisty). Jeśli dostawca podaje własny adres XMLTV, wpisz go tutaj.
Speichern & laden¦Zapisz i wczytaj
Neu laden¦Wczytaj ponownie
Sender oder Kategorien als Favorit markieren: OK gedrückt halten oder Menü-Taste.¦Oznacz kanały lub kategorie jako ulubione: przytrzymaj OK lub naciśnij klawisz Menu.
Meine Playlists ({0})¦Moje playlisty ({0})
Aktivieren¦Aktywuj
Umbenennen¦Zmień nazwę
Löschen¦Usuń
Name der Playlist¦Nazwa playlisty
Name speichern¦Zapisz nazwę
+ Weitere Playlist hinzufügen¦+ Dodaj kolejną playlistę
Aktive Playlist neu laden¦Wczytaj ponownie aktywną playlistę
Automatisch aktualisieren¦Aktualizuj automatycznie
Bei jedem Start¦Przy każdym starcie
Täglich¦Codziennie
Wöchentlich¦Co tydzień
Nur von Hand¦Tylko ręcznie
Playlist aktualisieren¦Aktualizuj playlistę
Abstand¦Interwał
alle {0} Stunden¦co {0} godz.
Wochentag¦Dzień tygodnia
Stunde¦Godzina
Minute¦Minuta
Zuletzt aktualisiert:¦Ostatnia aktualizacja:
noch nie¦nigdy
Nächste Aktualisierung:¦Następna aktualizacja:
„Bei jedem Start“ lädt die Playlist bei jedem Öffnen der App. Die geplanten Varianten laufen auch im Hintergrund, solange das Gerät eingeschaltet und online ist, und holen verpasste Termine beim nächsten Start nach. „Nur von Hand“ nutzt immer den gespeicherten Stand; Neu laden geht jederzeit mit dem Knopf oben. Die Uhrzeit richtet sich nach deiner Zeitzone unter „Sprache & Region“.¦„Przy każdym starcie” wczytuje playlistę przy każdym otwarciu aplikacji. Opcje harmonogramu działają też w tle, gdy urządzenie jest włączone i w sieci, a pominięte terminy nadrabiają przy następnym starcie. „Tylko ręcznie” zawsze używa zapisanej kopii; możesz ją w każdej chwili wczytać ponownie przyciskiem powyżej. Godzina jest zgodna ze strefą czasową z „Język i region”.
Zugangsdaten werden nur auf diesem Gerät gespeichert und hier nicht angezeigt. Es ist immer eine Playlist aktiv. Mit „Aktivieren“ wechselst du die Liste.¦Dane logowania są przechowywane tylko na tym urządzeniu i nie są tu pokazywane. Jedna playlista jest zawsze aktywna. Użyj „Aktywuj”, aby przełączyć listę.
IPVIO Stream · Version¦IPVIO Stream · wersja
IPVIO Stream ist ein Player und liefert selbst keine Inhalte. Nutze nur Quellen, für die du eine Berechtigung hast.¦IPVIO Stream to odtwarzacz i sam nie dostarcza żadnych treści. Korzystaj tylko ze źródeł, do których masz prawo.
Dieses Produkt verwendet die TMDB-API, wird aber nicht von TMDB unterstützt oder zertifiziert. Intro-Zeiten: TheIntroDB.¦Ten produkt korzysta z API TMDB, ale nie jest wspierany ani certyfikowany przez TMDB. Czasy czołówek: TheIntroDB.
Systemeinstellungen der App¦Ustawienia systemowe aplikacji
"""

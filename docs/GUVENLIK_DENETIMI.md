# Güvenlik Denetimi — IPVIO Stream

**Tarih:** 7 Kasım 2026  
**İncelenen kaynak:** `Emrodev/IPVIO-Stream`, commit `66f837d` (sonradan bu fork için küçük belge/yerelleştirme ve otomasyon ekleri yapıldı)  
**Kapsam:** Kaynak kod, Gradle derleme zinciri, GitHub Actions, Android izinleri, commit geçmişi ve yerelde izlenebilen ağ davranışı. APK çalıştırılmadı.

## Özet / hüküm

**Hüküm: Kullanım için makul derecede güvenli; ancak önemli kullanım/güven modeli notları var (SAFE WITH CAVEATS).**

İncelenen kaynakta doğrudan kötü amaçlı davranış göstergesi bulunmadı: dinamik DEX kod yükleme, kabuk komutu çalıştırma, gizli WebView/JavaScript köprüsü, reklam/telemetri SDK’sı, kişi/SMS/konum erişimi veya kaynak dışı veri sızdırma tespit edilmedi. Derleme yalnızca `google()`, `mavenCentral()` ve Gradle Plugin Portal kullanıyor. Unit testler bu forkta başarıyla çalıştırıldı.

Bu, uygulamanın **her türlü IPTV sağlayıcısına güvenli olduğu** anlamına gelmez: IPTV URL’nize kullanıcı adı/parola gömülü olabilir ve kaynak HTTP ise bu trafik şifrelenmez. Ayrıca isteğe bağlı Google Drive yedeği ve telefon aktarımı hassas verileri bilerek işler. Uygulama içi güncelleme mekanizması, Android imza doğrulamasına dayanır; indirilen dosya için ayrıca SHA-256 kontrolü yoktur.

## Kapsam ve yöntem

- `git log --all`, mevcut ağaç ve kaynak/izin/URL taraması yapıldı.
- Riskli Android/Kotlin API’leri tarandı: `Runtime.exec`, `ProcessBuilder`, `DexClassLoader`, `loadDex`, `System.load`, WebView JS köprüleri ve reflection.
- Gradle wrapper SHA-256’sı, `gradle-wrapper.properties` içindeki Gradle dağıtım SHA-256’sı ile resmi `services.gradle.org` değeri karşılaştırıldı.
- GitHub workflow YAML’ı ayrıştırıldı; yalnızca build artifact yükleyen workflow ve bu forkta eklenen upstream senkron workflow’u incelendi.
- Tüm kaynak dosyalarında sabit URL’ler ve dış ağ hizmetleri listelendi.

## Derleme bütünlüğü ve bağımlılıklar

| Kontrol | Sonuç |
|---|---|
| Gradle wrapper dağıtımı | `gradle-9.6.0-bin.zip`, yalnızca `https://services.gradle.org` (`gradle/wrapper/gradle-wrapper.properties:6`) |
| Dağıtım SHA-256 | Tanımlı `bbaeb2...529a01`; resmi Gradle SHA-256 ile eşleşiyor |
| Wrapper JAR SHA-256 | `76805e32c009c0cf0dd5d206bddc9fb22ea42e84db904b764f3047de095493f3` |
| Depolar | `google()`, `mavenCentral()`, Gradle Plugin Portal; özel Maven deposu yok (`settings.gradle.kts:1-22`) |
| Eklentiler | Android Gradle Plugin 9.4.1, Kotlin Compose 2.2.10 (`gradle/libs.versions.toml:3-8`) |
| Başlıca uygulama bağımlılıkları | AndroidX Compose/Media3/WorkManager, OkHttp 4.12.0, Coil 2.7.0, Jellyfin Media3 FFmpeg decoder (`app/build.gradle.kts:80-99`) |

`org.jellyfin.media3:media3-ffmpeg-decoder` GPL-3.0 bileşenidir ve proje lisansının GPL-3.0 olma nedenidir; beklenen bağımlılıktır. Derlemede ortam değişkenlerinden alınabilecek TMDB/OMDb/Google anahtarları yalnızca açıkça `secrets.properties` veya ortamdan sağlanır (`app/build.gradle.kts:8-18,42-45`); kaynakta yerleşik anahtar bulunmadı.

## Android izinleri

| İzin / ayar | Gerekçe | Not |
|---|---|---|
| `INTERNET`, `ACCESS_NETWORK_STATE` | Oynatma listeleri, yayınlar, meta veri (`AndroidManifest.xml:4-6`) | Beklenen |
| `WAKE_LOCK` | Oynatma sırasında uykuya geçmeme (`:8-9`) | Beklenen |
| `RECEIVE_BOOT_COMPLETED`, `SYSTEM_ALERT_WINDOW` | Başlatma/yeniden bağlanma davranışları (`:7,10-11`) | Güçlü izin; yalnızca ihtiyaç varsa verin |
| `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM` | Program hatırlatıcıları (`:12-15`) | İsteğe bağlı işlev |
| `FOREGROUND_SERVICE[_DATA_SYNC]` | Kayıt/aktarım hizmeti (`:16-18,62-65`) | Beklenen |
| `REQUEST_INSTALL_PACKAGES` | Kullanıcı onaylı uygulama içi APK güncellemesi (`:14`, `AppUpdate.kt:159-173`) | En güçlü izindir; aşağıdaki güncelleme notuna bakın |
| `usesCleartextTraffic=true` | HTTP IPTV sağlayıcıları (`AndroidManifest.xml:36`) | Hassas akış/kimlik bilgileri HTTP’de şifrelenmez |

Uygulama kişi, SMS, çağrı kaydı, hassas konum, kamera veya mikrofon izni istemez. Mikrofon yalnızca isteğe bağlı özellik olarak tanımlanmıştır ve zorunlu değildir (`AndroidManifest.xml:21-27`).

## Ağ trafiği ve uç noktalar

Tüm sabit üçüncü taraf uç noktaları, özellikleriyle birlikte aşağıdadır:

| Uç nokta | Amaç | Veri / koşul |
|---|---|---|
| Sağlayıcının Xtream/M3U/Stalker URL’si | Kullanıcının eklediği içerik | URL kullanıcı adı/parola taşıyabilir; HTTP olabilir |
| `api.github.com/repos/.../releases/latest` | Güncelleme denetimi | Sürüm bilgisi (`AppUpdate.kt:56-96`) |
| GitHub release asset | Kullanıcı dokununca APK indirme | APK, önbelleğe indirilir (`AppUpdate.kt:130-155`) |
| `api.themoviedb.org`, `image.tmdb.org` | TMDB meta veri/poster | Kullanıcının TMDB anahtarı varsa (`Meta.kt`, `Credits.kt`) |
| `www.omdbapi.com` | IMDb/Rotten Tomatoes puanları | Kullanıcının OMDb anahtarı varsa (`Meta.kt`) |
| `api.theintrodb.org` | Giriş/jenerik zamanları | TMDB kimliği ve süre (`Epg.kt:264`) |
| `youtube.com` | Fragman açma | Kullanıcı seçimiyle (`DetailScreen.kt:51-52`) |
| `oauth2.googleapis.com`, `www.googleapis.com` | İsteğe bağlı Drive yedeği | Sadece kullanıcı Google ile bağlanırsa; `drive.appdata` kapsamı (`DriveSync.kt:28,70-156`) |
| `paypal.com` | İsteğe bağlı bağış | Kullanıcı seçimiyle (`Donate.kt:14`) |

Telemetri, reklam analitiği veya otomatik crash-report upload uç noktası görülmedi. `Config.kt:72-85` içindeki `eval` adı, JavaScript değerlendirmesi değildir; HTTP durum kodunu hata metnine çeviren Kotlin lambda parametresidir.

## Güncelleme mekanizması

`AppUpdate.kt:56-96` GitHub Release JSON’unu veya kullanıcının Ayarlar’da tanımladığı JSON adresini okur. `AppUpdate.kt:130-173` APK’yı indirir ve Android sistem yükleyicisini açar.

- Android, yeni APK’nın paket imzasını kurulu APK ile karşılaştırır; farklı anahtarla imzalanan APK mevcut uygulamanın üstüne kurulamaz. Bu, ana güncelleme korumasıdır.
- Ancak uygulama indirme sonrasında SHA-256/asset digest kontrolü yapmaz. HTTPS ve Android paket imzasına dayanır.
- Ayarlar’daki “kendi güncelleme adresi” özelliğini yalnızca güvendiğiniz HTTPS adresiyle kullanın (`SettingsHub.kt:1189`, `AppUpdate.kt:41-42`).
- Bu forkta varsayılan adres `ptmy53/IPVIO-Stream-TR` olarak değiştirildi (`AppUpdate.kt:39`). Repo private iken GitHub API, uygulama token taşımadığı için release kontrolü/indirmesi çalışmaz. Kişisel release dağıtımı için repo public olmalı veya uygulama güncellemesini kapalı tutmalısınız.

## Yerel ağ aktarımı

Telefon aktarımı, ekran açıkken 8765–8775 arasında boş bir TCP portunu dinler (`PhoneImport.kt:150-189`). Sunucuya aynı yerel ağdaki cihazlar ulaşabilir. Her işlem dört haneli rastgele kod ister ve beş yanlış denemeden sonra kilit uygular (`PhoneImport.kt:162,238-242`). Başlık boyutu 8 KiB, küçük form gövdesi 8 KiB ile sınırlandırılmıştır (`PhoneImport.kt:195-221`); yüklenen playlist doğrudan geçici dosyaya yazılır (`:224-235`).

**Risk / önlem:** Kodu ekranda görebilen aynı Wi-Fi ağındaki biri aktarım denemesi yapabilir. Aktarımı yalnızca ev ağında kullanın, açık Wi-Fi’da kullanmayın ve işiniz biter bitmez aktarım ekranından çıkın.

## Google Drive yedeği

Google bağlantısı isteğe bağlıdır. Kod yalnızca `drive.appdata` OAuth kapsamını ister (`DriveSync.kt:28`), yani uygulamaya ait gizli Drive alanını hedefler. Ancak “oynatma listelerini erişim bilgileriyle yedekle” seçeneği açıksa sağlayıcı giriş bilgileriniz Drive yedeğine dahil edilir (`TransferUi.kt:111-117`). Bu seçeneği yalnızca hesabınız güçlü bir parolayla ve iki aşamalı doğrulamayla korunuyorsa kullanın.

## Git geçmişi ve ikili içerik

- Geçmişte `IPVIO.apk` bir kez eklendi (`bc95392`) ve sonraki commit’te silindi (`d52c2f7`). Boyutu 22,819,812 bayt; mevcut çalışma ağacında APK yoktur.
- Mevcut kaynakta tek JAR Gradle wrapper’dır (`gradle/wrapper/gradle-wrapper.jar`); DEX, `.so`, EXE veya APK izlenmiyor.
- Font ve resimler normal Android kaynak dosyalarıdır. Yalnızca kaynak denetimi yapıldığı için önceden yayımlanmış APK’yı kurmak yerine bu kaynaktan kendi APK’nızı derlemek tercih edilir.

## Bulgular ve öneriler

1. **HTTP IPTV riski (orta):** `usesCleartextTraffic=true` zorunlu olabilir ama kullanıcı adı/parola ve yayın ağda okunabilir. HTTPS kaynak seçin.
2. **Güncelleme modeli (orta):** Android imzası koruma sağlar, ek hash doğrulaması yoktur. Sadece güvenilir release kullanın; “özel güncelleme adresi”ni boş bırakmak daha güvenlidir.
3. **Yerel aktarım (düşük/orta):** PIN korumalı ama LAN erişimine açıktır. Sadece güvenilir ağlarda açın.
4. **Drive yedeği (kullanıcı tercihi):** Oynatma listesi kimlik bilgileri yedeğe girebilir. İhtiyaç yoksa kimlik bilgileriyle yedekleme seçeneğini kapalı bırakın.
5. **Gelecek upstream güncellemeleri:** Günlük otomatik senkron, yeni kodu da alır. Her sürümden sonra özellikle izinler, `AndroidManifest.xml`, `app/build.gradle.kts`, `AppUpdate.kt`, `PhoneImport.kt` ve yeni ağ uç noktalarını yeniden inceleyin.

## Sonuç

İncelenen kaynakta kötü amaçlı yazılım davranışı bulmadım. Kendi cihazınızda, yasal ve tercihen HTTPS IPTV kaynağı kullanarak; Drive/telefon aktarımı/güncelleme seçeneklerini ihtiyacınız yoksa kapalı tutarak kullanmanız makuldür. Yine de bu bir kaynak kod denetimidir; üçüncü taraf IPTV sağlayıcınızın içerik, gizlilik veya ödeme davranışını garanti etmez.

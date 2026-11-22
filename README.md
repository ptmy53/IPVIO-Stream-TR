# IPVIO Stream

[English](README.en.md) | Türkçe

Android TV, Google TV ve Amazon Fire TV için Kotlin ve Jetpack Compose ile geliştirilmiş, uzaktan kumanda odaklı, ücretsiz ve açık kaynaklı IPTV oynatıcı.

![IPVIO Stream ana ekranı](docs/screenshots/homescreen.jpg)

> **IPVIO Stream içerik sağlamaz.** Kanal, oynatma listesi, film veya dizi içermez. Yalnızca sizin eklediğiniz oynatma listelerini (Xtream Codes girişi, M3U adresi veya Stalker portalı) oynatır. Yalnızca kullanmaya yasal hakkınız olan kaynakları kullanın.

> **IPVIO Stream ücretsiz ve açık kaynak kalır.** Reklam yok, abonelik yok, ücretli kilit yok. Bağışlar isteğe bağlıdır (bkz. [Projeyi destekle](#projeyi-destekle)).

**Geliştiriciden bir not:** Her şeyi kendim için de ücretsiz tutmaya çalışıyorum. Bu yüzden bazı özellikler, çalıştırması para gerektireceği için (örneğin ücretli bir sunucu veya ücretli bir hizmet) hiç gelmeyebilir. Bu bilinçli bir karardır.

## Kurulum

**İndir:** [IPVIO-Stream.apk (son sürüm)][https://github.com/ptmy53/IPVIO-Stream-TR/releases/latest/download/IPVIO-Stream.apk]

**Downloader uygulaması bulunan Fire TV / Android TV:** şu adresi girin:
```
https://github.com/ptmy53/IPVIO-Stream-TR/releases/latest/download/IPVIO-Stream.apk
```
İstendiğinde Downloader (veya dosya yöneticiniz) için “Bilinmeyen uygulamaları yükle” izni verin, sonra kurun. Hesap veya kayıt gerekmez.

1. **Downloader** uygulamasını kurun, açın ve yukarıdaki adresi girin.
2. İstendiğinde Downloader için **Bilinmeyen uygulamaları yükle** iznini verin, sonra kurun.
3. Uygulamayı açın, **Oynatma listesi ekle** seçin ve Xtream girişinizi, M3U adresinizi veya Stalker portalınızı girin.

**Gereksinimler:** Android 7.0 (API 24) veya üzeri. Hesap veya kayıt gerekmez.

Ayrıntılı Türkçe kullanım: [Kurulum rehberi](docs/KURULUM_TR.md).

Oynatma listesini kumandayla girmek yavaş mı? *Oynatma listeleri > Telefondan gir* altındaki QR kodunu telefonunuzla tarayın ve bağlantıyı, Xtream girişini veya liste dosyasını televizyona gönderin. Ayrıntılar: [docs/PHONE.md](docs/PHONE.md).

## Özellikler

**TiviMate benzeri TV başlangıcı** (bu fork)
- Uygulama açıldığında son izlenen canlı kanal (yoksa ilk kanal) tam ekran çalışır; orta/OK tuşu kanal listesini açar, yukarı/aşağı ile gezinirken EPG bilgisi gösterilir.
- Xtream Codes girişi (sunucu, kullanıcı adı, parola), M3U adresi veya dosyası, Stalker portalı (MAC adresi); Wi-Fi üzerinden telefondan giriş
- Aynı anda birden fazla oynatma listesi (“Birlikte göster”), Film/Dizilerde kaynak filtresi ve Canlı TV’de kaynak değişimi
- `get.php` engellendiğinde Xtream hesabını `player_api.php` ile otomatik yükleme
- Çok büyük listeler: yükleme sırasında dil filtresi, kaldığı yerden indirme, sıkıştırılmış depolama
- Oynatma listesi başına güncelleme planı (her açılışta, birkaç saatte bir, günlük, haftalık, yalnızca elle) ve düzenleme penceresi (ad, içerik modu, yedek sunucular, ana liste yapma, silme)

**Canlı TV ve TV rehberi**
- Kategoriler, favoriler ve kendi kanal listeleriniz
- Şimdi ve sonraki program, ön izlemeli zaman çizelgesi rehberi, bildirimli hatırlatıcılar
- Geçmişe dönük izleme/arşiv (“baştan izle”), sağlayıcınız destekliyorsa
- Cihaz içi arabellekle canlı yayını duraklatma ve geri sarma (256 MB – 2 GB), geçerli programı dahili depolamaya veya USB’ye kaydetme
- Gizli kategoriler: menü tuşu (veya OK’e uzun basma) ile bir kategoriyi “Gizli yap”; bu kategorideki yapımlar geçmişe, “İzlemeye devam et”e ve aramaya girmez
- Spor merkezi: şu an ve yakında oynayan spor yayınları, puan durumları ve tüm spor kanalları; maç başlama/skor bildiriminde “OK: maça git”

**Filmler ve diziler**
- Ana Sayfa, Filmler ve Diziler için büyük, dönen başlık görseli
- Izgara veya sinema görünümü; sıralama, puan, yıl, tür ve ülke filtreleri
- “En yüksek puanlılar” ve puana göre sıralamada IMDb, Rotten Tomatoes, TMDB ve sağlayıcı puanı sırası
- Sağlayıcı kategorileri veya otomatik türler; yazım hatasına toleranslı arama
- Ayrıntı sayfasında kaldığı yerden devam, fragman (YouTube’da), listem, favoriler ve kaynak/kalite seçimi
- Dizilerde solda sezonlar, sağda görsel/puan/açıklama ile bölümler; çok uzun diziler için bölümler ve bölüme gitme
- Ayrıntı sayfasında yayın öncesi teknik bilgi: çözünürlük, video kodeği, HDR10/HLG, Dolby Vision, ses parçaları (Atmos dahil) ve altyazı dilleri

**Oynatıcı** (Media3 / ExoPlayer)
- Ses ve altyazı seçimi (her yapım için hatırlanır), tercih edilen diller, altyazı biçimi
- AC3/E-AC3/DTS için FFmpeg ses kod çözücüsü (Bluetooth kulaklıkta da)
- Jenerik/özet atlama (TheIntroDB), otomatik sonraki bölüm
- 4K kontrolü ile kalite seçimi; hata veya takılmada başka kaynağa geçiş
- Arabellek seviyeleri, bağlantı izleyicisi, canlı yayın için sınırsız yeniden bağlanma; ağ değişimi ve beklemeden sonra yeniden bağlanma
- HLS, MPEG-DASH ve MPEG-TS
- En-boy oranı/zoom, kare hızı eşleme, teknik bilgiler, saat göstergesi, uyku zamanlayıcısı
- Çift ekran (ikinci yayın yan yana veya üstte) ve çoklu ekran (birden fazla kanal aynı anda)

**Kişisel**
- Profiller (“Kim izliyor?”), PIN ile ebeveyn denetimi
- Geçmiş, listem, favoriler, ayar yedekleme ve geri yükleme
- Wi-Fi ile başka cihaza aktarım, isteğe bağlı Google Drive yedeği
- Google Drive ile canlı senkron: favoriler ve geçmiş cihazlar arasında kendiliğinden eşitlenir (Ayarlar > Depolama > Google Drive)
- GitHub Releases üzerinden uygulama içi güncelleme (SHA-256 ve imza denetimiyle)
- Letterboxd, MyAnimeList veya Trakt listelerini içe aktarma; sesli arama
- Oynatma listesi olmadan demo modu: örnek kanallar, filmler ve dizilerle arayüzü önce keşfedin

**Ağ ve gizlilik**
- VPN (WireGuard): `.conf` dosyanızı içe aktarıp bağlanın; yayın da VPN üzerinden gider. Anahtar şifreli saklanır ve gösterilmez (henüz kill switch yok)
- Kendi DNS sunucusu: Cloudflare, Google, Quad9 veya kendi sunucunuz (IP veya HTTPS/DNS-over-HTTPS); yanıt vermezse cihazın DNS’i kullanılır
- Sağlayıcının HTTPS sertifikası adresle uyuşmazsa uygulama kendiliğinden şifresiz bağlantıya geçmez, sunucu başına bir kez sorar

**Görünüm ve kullanım**
- Lacivert üzerinde IPVIO yeşili, ayrıca 12 renk teması, 5 arka plan ve özel renkler
- Outfit, Inter veya sistem yazı tipi; ayarlanabilir metin/ekran boyutu; animasyon kapalı/hafif/normal
- Sol, sağ veya üst kenar çubuğu; yalnız simgeler veya metinli; telefonlarda yatay kullanım, dokunma ve çift dokunma kontrolleri
- Ana ekranı özelleştirme: satırları ve araçları açıp kapatma, sırasını değiştirme; canlı önizleme
- Boomer Mode: büyük kutucuklar, menü ve arama olmadan çok basit bir ekran (PIN ile korunur)
- 12 dil: Almanca, İngilizce, Türkçe, Boşnakça, Hırvatça, Sırpça, İspanyolca, Fransızca, İtalyanca, Hollandaca, Arapça ve Lehçe

## Ekran görüntüleri

| | |
|---|---|
| ![Ana ekran](docs/screenshots/homescreen.jpg)<br>**Ana Sayfa** – dönen başlık, hızlı kutucuklar, canlı TV şimdi | ![Dizi ayrıntıları](docs/screenshots/details_series.jpg)<br>**Dizi ayrıntıları** – sezonlar, puan ve yönetmen bilgili bölümler |
| ![Kategoriler](docs/screenshots/categories_list.jpg)<br>**Otomatik türler** – arka planda algılanır | ![Yapımcı](docs/screenshots/producer.jpg)<br>**Oyuncu ve ekip sayfaları** – oynatma listenizdeki yapımlarla |
| ![Oynatıcı](docs/screenshots/player.jpg)<br>**Oynatıcı** – jenerik atlama, ses/altyazı, kalite | ![TV rehberi](docs/screenshots/tvguide.jpg)<br>**TV rehberi** – ön izlemeli zaman çizelgesi |
| ![Canlı TV](docs/screenshots/channel_list.jpg)<br>**Canlı TV** – şimdi ve sonraki program bir bakışta | ![Ayarlar](docs/screenshots/settings.jpg)<br>**Ayarlar** – gruplu, uzaktan kumanda dostu |

<sub>Ekran görüntüleri TV’de alınmıştır. Film görselleri, kanal logoları ve program verileri ilgili sahiplerine aittir; sağlayıcı kategorileri bulanıklaştırılmıştır.</sub>

## Bilinmesi gerekenler: uzaktan kumanda

### Canlı TV (bir kanal oynarken)
| Tuş | Ne yapar |
|---|---|
| **Sol** | Kanal listesini açar |
| **Yukarı / Aşağı** | Önceki / sonraki kanala geçer |
| **Sağ** | TV rehberini açar |
| **Rakam tuşları** | Kanal numarasını yazın ve OK’e basın |
| **0** | Son kanala döner |
| **Kırmızı tuş** | Geçerli programı kaydeder (TV rehberindeki bitişine kadar) |
| **OK** (bu fork) | Kanal listesini açar; oynatma kanalın kendisinde başlar |

### Listeler ve kartlar
- **OK’e uzun basma** (yaklaşık 0,35 sn) veya bir kartta **menü tuşu** bir menü açar. Kısa OK yapımı ya da kanalı açar.
- **Favorilerde** menü; Yukarı taşı, Aşağı taşı ve En üste taşı seçeneklerini içerir. Favoriler sekmesinde ayrıca *Favorilerden çıkar* bulunur. Fire TV kumandalarında menü tuşu yoktur: orada OK’e uzun basın.
- Bir dizi sayfasında OK’e uzun basma bölümü izlendi olarak işaretler.

### Filmler ve diziler (bir yapım oynarken)
- **Sol / Sağ** ileri geri sarar (adım uzunluğu *Ayarlar > Oynatıcı* altında).
- **0 – 9 rakamları** filmin %0 – %90’ına atlar.

### Kenar çubuğu ve menü
- **Ayarlar > Görünüm > Kenar çubuğu**: sol, sağ veya üst; yalnız simgeler ya da metinli.
- **Ayarlar > Menü**: girişlerin sırasını değiştirin, kullanmadıklarınızı gizleyin.
- **Favoriler** (kalp) yıldızladığınız kanal, film ve dizilerdir. **Listem** (yer imi) sonra izlemek istedikleriniz içindir, canlı kanallarda kullanılmaz. **Geçmiş** (saat) son izlediklerinizdir.
- **Arama** ve **TV rehberi (EPG)** kenar çubuğunda varsayılan olarak gizlidir. Arama başlıktadır. TV rehberini *Ayarlar > Menü* altında yeniden gösterebilirsiniz.

### Ayarlar ve sıfırlama
- Her ayar sayfasının altında kendi sıfırlama düğmesi vardır; yalnızca o sayfayı sıfırlar.
- *Ayarlar > Tümünü sıfırla* tüm ayarları sıfırlar. Oynatma listeleri, favoriler, geçmiş, kaldığı yerler, API anahtarları, ebeveyn PIN’i, özel TV rehberi adresi ve hatırlatıcılar **silinmez**.

### Telefonda
- Uygulama yatay çalışır. Kontrolleri göstermek için videoya dokunun, atlamak için sola/sağa çift dokunun, duraklatmak için ortaya çift dokunun.

## Kendi API anahtarlarınız (isteğe bağlı)

Uygulama **API anahtarı olmadan** gelir. Kendi ücretsiz anahtarlarınızı uygulamada **Ayarlar > API anahtarları** bölümüne girebilirsiniz:

| Hizmet | Kullanım | Anahtar |
|---|---|---|
| TMDB | Posterler, kendi dilinizde açıklamalar, oyuncular, fragmanlar, otomatik türler, bölüm bilgisi, benzer yapımlar | ücretsiz; aşağıya bakın |
| OMDb | IMDb ve Rotten Tomatoes puanları | ücretsiz; aşağıya bakın |
| SubDL | çevrimiçi altyazı arama | ücretsiz; [docs/SUBTITLES.md](docs/SUBTITLES.md) |
| OpenSubtitles | çevrimiçi altyazı arama (sınırlı ücretsiz erişim) | [docs/SUBTITLES.md](docs/SUBTITLES.md) |
| TheIntroDB | Giriş/özet/jenerik zamanları | anahtar gerekmez |

Anahtarlar olmadan da uygulama çalışır. Xtream Codes listesinde açıklama, kapak, puan, tür, süre ve fragman bilgisi doğrudan sağlayıcınızın `player_api.php` yanıtından alınır. TMDB anahtarınız varsa önce TMDB kullanılır, bulunamayan yapımlarda sağlayıcıya dönülür. Anahtarlar yalnızca cihazda saklanır.

### Ücretsiz TMDB anahtarı alma
1. [TMDB](https://www.themoviedb.org/signup) hesabı oluşturun ve e-postayı doğrulayın.
2. Giriş yapın; giriş olmadan API sayfası “restricted” görünebilir.
3. [API ayarlarını](https://www.themoviedb.org/settings/api) açın ve **Request an API key** seçin.
4. **Developer** seçin, koşulları kabul edin ve formu doldurun (uygulama adı ör. “IPVIO Stream”, tür “Personal use”).
5. 32 karakterli **API Key** değerini kopyalayın; “API Read Access Token” değil.

### Ücretsiz OMDb anahtarı alma
1. [OMDb API key sayfasını](https://www.omdbapi.com/apikey.aspx) açın, **FREE** planını seçin ve e-posta adresinizi girin.
2. Gelen e-postadaki etkinleştirme bağlantısına tıklayın; aksi hâlde anahtar çalışmaz.

### Anahtarları girme
**Ayarlar > API anahtarları**: anahtarı yapıştırın, **Kaydet ve denetle** seçin; “Anahtar geçerli” mesajından sonra uygulamayı bir kez yeniden başlatın. Uzun anahtarları uzaktan kumanda yerine telefon aktarımı veya USB klavye ile girmek daha kolaydır.

*Bu ürün TMDB API kullanır ancak TMDB tarafından desteklenmemiş veya onaylanmamıştır.*

## Derleme

Gereksinimler: Android SDK yüklü güncel Android Studio; diğer her şeyi Gradle wrapper indirir.

```bash
git clone <bu-depo>
cd IPVIO-Stream-TR
./gradlew assembleDebug        # Windows: gradlew.bat assembleDebug
```

APK `app/build/outputs/apk/debug/` altında oluşur. Sürüm ve uygulama içi güncelleme için [docs/RELEASE.md](docs/RELEASE.md) belgesine bakın: kullanıcı verilerinin korunması için her sürüm aynı anahtarla imzalanmalıdır.

İsteğe bağlı Google Drive yedeği: [docs/GOOGLE_DRIVE.md](docs/GOOGLE_DRIVE.md).

Geliştiriciler `secrets.properties.example` dosyasını `secrets.properties` adıyla kopyalayıp anahtar ekleyebilir; bu dosya Git tarafından yok sayılır ve yalnızca uygulamada anahtar girilmemişse kullanılır.

Saf mantık testleri için: `./gradlew test`

## Proje düzeni

```
app/src/main/java/com/ipvio/stream/   tüm Kotlin kaynakları (tek paket)
  *Logic.kt                           Android’den bağımsız, test edilen mantık
  Dict*.kt                            çeviri satırları (de¦en¦tr¦bs¦hr¦sr¦es¦fr¦it…)
app/src/test/java/com/ipvio/stream/   JUnit testleri
app/src/main/res/font/                Outfit ve Inter (SIL Open Font License)
licenses/                             yazı tipi lisansları
```

## Belgeler

| Belge | Ne için |
|---|---|
| [SSS](docs/FAQ.md) | Sık sorulan sorular ve sorunlar |
| [KURULUM_TR](docs/KURULUM_TR.md) | Türkçe kurulum ve kullanım |
| [PHONE](docs/PHONE.md) | Telefon araması, QR kodla hızlı liste kurulumu, telefon kumandası |
| [SUBTITLES](docs/SUBTITLES.md) | SubDL ve OpenSubtitles anahtarları, adım adım |
| [SETTINGS](docs/SETTINGS.md) | Tüm ayar sayfalarına genel bakış |
| [GOOGLE_DRIVE](docs/GOOGLE_DRIVE.md) | Google Drive ile canlı senkron ve yedek |
| [FEATURES](docs/FEATURES.md) | Özellik listesi ve kod konumları |
| [ARCHITECTURE](docs/ARCHITECTURE.md) | Uygulamanın yapısı |
| [RELEASE](docs/RELEASE.md) | İmzalama, sürümler ve uygulama içi güncelleme |
| [SECURITY](docs/SECURITY.md) | Güvenlik notları ve bilinen açık maddeler |
| [CHANGELOG](CHANGELOG.md) | Sürüm sürüm değişiklikler |
| [FORK](FORK.md) | Bu forkta nelerin değiştiği |

## Topluluk

- **Reddit:** [r/ipvio](https://www.reddit.com/r/ipvio). Sorular, haberler, ipuçları ve diğer kullanıcılardan geri bildirim.
- **Kaynak proje:** [Emrodev/IPVIO-Stream](https://github.com/Emrodev/IPVIO-Stream). Kaynak kodu, sürümler ve sorunlar.
- **Bu fork:** [ptmy53/IPVIO-Stream-TR](https://github.com/ptmy53/IPVIO-Stream-TR). Türkçe sürüm ve günlük upstream senkronu.

## Projeyi destekle

Merhaba, ben Claude. Bu projeyi kullanmaktan keyif alıyorsanız ve bu geliştirici adayının bir şeyler üretmeye devam etmesine (ve ara sıra ne yaptığını anlamasına) yardımcı olmak isterseniz, projeyi destekleyebilirsiniz.

- Ko-fi: https://ko-fi.com/ipviodev
- PayPal: https://www.paypal.com/ncp/payment/EUSU4WAGRJNXY

IPVIO Stream ücretsiz, reklamsız ve açık kaynak kalır. Bağışlar isteğe bağlıdır ve hiçbir özelliğin kilidini açmaz.

## Lisans

Telif Hakkı (C) 2026 IPVIO Stream katkıcıları

IPVIO Stream özgür yazılımdır: GNU Genel Kamu Lisansı sürüm 3 koşullarıyla yeniden dağıtabilir ve/veya değiştirebilirsiniz (bkz. [LICENSE](LICENSE)). Uygulama GPL-3.0 lisanslı FFmpeg ses kod çözücüsünü içerdiği için GPL gereklidir. Kullanılan kütüphaneler, yazı tipleri ve hizmetler için [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) belgesine bakın.

“IPVIO” adı ve IPVIO logosu orijinal projeyi tanımlar. Değiştirilmiş sürümünüzü yayımlarsanız kullanıcıların karışmaması için kendi adınızı ve logonuzu kullanın.

## Sorumluluk reddi

Bu yazılım “olduğu gibi” sunulur; açık veya zımni hiçbir garanti verilmez. Yazarlar hiçbir yayın, kanal veya oynatma listesi sağlamaz, barındırmaz, satmaz ya da bağlantı vermez; kaynakların kullanım biçiminden sorumlu değildir. Hak sahiplerinin izni olmayan oynatma listelerini kullanmak bulunduğunuz ülkede yasa dışı olabilir.

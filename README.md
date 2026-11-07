# IPVIO Stream

[English](README.en.md) | Türkçe

Android TV, Google TV ve Amazon Fire TV için Kotlin ve Jetpack Compose ile geliştirilmiş, uzaktan kumanda odaklı, ücretsiz ve açık kaynaklı IPTV oynatıcı.

![IPVIO Stream ana ekranı](docs/screenshots/homescreen.jpg)

> **IPVIO Stream içerik sağlamaz.** Kanal, oynatma listesi, film veya dizi içermez. Yalnızca sizin eklediğiniz oynatma listelerini (Xtream Codes girişi veya M3U adresi) oynatır. Yalnızca kullanmaya yasal hakkınız olan kaynakları kullanın.

## Kurulum

**İndir:** [IPVIO-Stream.apk (son sürüm)]([https://github.com/ptmy53/IPVIO-Stream-TR/releases/download/v1.1/IPVIO-Stream.apk](https://github.com/ptmy53/IPVIO-Stream-TR/releases/download/v1.1/IPVIO-Stream.apk)

**Downloader uygulaması bulunan Fire TV / Android TV:** şu adresi girin:
```
https://github.com/ptmy53/IPVIO-Stream-TR/releases/latest/download/IPVIO-Stream.apk
```
İstendiğinde Downloader (veya dosya yöneticiniz) için “Bilinmeyen uygulamaları yükle” izni verin, sonra kurun. Hesap veya kayıt gerekmez.

Ayrıntılı Türkçe kullanım: [Kurulum rehberi](docs/KURULUM_TR.md).

## Özellikler

**Oynatma listeleri**
- Xtream Codes girişi (sunucu, kullanıcı adı, parola), M3U adresi veya dosyası, Stalker portalı (MAC adresi); Wi-Fi üzerinden telefondan giriş
- Aynı anda birden fazla oynatma listesi (“Birlikte göster”), Film/Dizilerde kaynak filtresi ve Canlı TV’de kaynak değişimi
- `get.php` engellendiğinde Xtream hesabını `player_api.php` ile otomatik yükleme
- Çok büyük listeler: yükleme sırasında dil filtresi, kaldığı yerden indirme, sıkıştırılmış depolama
- Otomatik arka plan yenilemesi (aralık veya sabit saat; oynatma sırasında bekler)

**Ana ekran**
- Ana Sayfa, Filmler ve Diziler için büyük, dönen başlık görseli
- Seçilen yapımı takip eden arka plan; başlık, puan ve açıklama
- İzlemeye devam et, Şimdi canlı, Bugünün ilk 10’u, İzlediklerinize göre, Listem, Yeni, En yüksek puanlılar ve tür satırları

**Gezinme ve ayrıntılar**
- Izgara veya sinema görünümü; sıralama, puan, yıl, tür ve ülke filtreleri
- “En yüksek puanlılar” ve puana göre sıralamada IMDb, Rotten Tomatoes, TMDB ve sağlayıcı puanı sırası
- Sağlayıcı kategorileri veya otomatik türler; yazım hatasına toleranslı arama
- Ayrıntı sayfasında kaldığı yerden devam, fragman, listem, favoriler ve kaynak/kalite seçimi
- Dizilerde solda sezonlar, sağda görsel/puan/açıklama ile bölümler; çok uzun diziler için bölümler ve bölüme gitme

**Canlı TV**
- Kategoriler, favoriler ve kendi kanal listeleriniz
- TV rehberi, geçmişe dönük izleme/arşiv (“baştan izle”), bildirimli hatırlatıcılar
- Cihaz içi arabellekle canlı yayını geri sarma, geçerli programı dahili depolamaya veya USB’ye kaydetme
- Spor bölümü: şu an ve yakında oynayan spor yayınları, tüm spor kanalları

**Oynatıcı** (Media3 / ExoPlayer)
- Ses ve altyazı seçimi (her yapım için hatırlanır), tercih edilen diller, altyazı biçimi
- AC3/E-AC3/DTS için FFmpeg ses kod çözücüsü (Bluetooth kulaklıkta da)
- Jenerik/özet atlama (TheIntroDB), otomatik sonraki bölüm
- 4K kontrolü ile kalite seçimi; hata veya takılmada başka kaynağa geçiş
- Arabellek seviyeleri, bağlantı izleyicisi, canlı yayın için sınırsız yeniden bağlanma; ağ değişimi ve beklemeden sonra yeniden bağlanma
- HLS, MPEG-DASH ve MPEG-TS
- En-boy oranı/zoom, kare hızı eşleme, teknik bilgiler, saat göstergesi, uyku zamanlayıcısı

**Kişisel**
- Profiller (“Kim izliyor?”), PIN ile ebeveyn denetimi
- Geçmiş, listem, favoriler, ayar yedekleme ve geri yükleme
- Wi-Fi ile başka cihaza aktarım, isteğe bağlı Google Drive yedeği
- GitHub Releases üzerinden uygulama içi güncelleme

**Görünüm ve kullanım**
- Lacivert üzerinde IPVIO yeşili, ayrıca 12 renk teması, 5 arka plan ve özel renkler
- Outfit, Inter veya sistem yazı tipi; ayarlanabilir metin/ekran boyutu; animasyon kapalı/hafif/normal
- Sol, sağ veya üst kenar çubuğu; yalnız simgeler veya metinli; telefonlarda yatay kullanım, dokunma ve çift dokunma kontrolleri
- 10 dil: Almanca, İngilizce, Türkçe, Boşnakça, Hırvatça, Sırpça, İspanyolca, Fransızca, İtalyanca ve Hollandaca

## Ekran görüntüleri

| | |
|---|---|
| ![Ana ekran](docs/screenshots/homescreen.jpg)<br>**Ana Sayfa** – dönen başlık, hızlı kutucuklar, canlı TV şimdi | ![Dizi ayrıntıları](docs/screenshots/details_series.jpg)<br>**Dizi ayrıntıları** – sezonlar, puan ve yönetmen bilgili bölümler |
| ![Kategoriler](docs/screenshots/categories_list.jpg)<br>**Otomatik türler** – arka planda algılanır | ![Yapımcı](docs/screenshots/producer.jpg)<br>**Oyuncu ve ekip sayfaları** – oynatma listenizdeki yapımlarla |
| ![Oynatıcı](docs/screenshots/player.jpg)<br>**Oynatıcı** – jenerik atlama, ses/altyazı, kalite | ![TV rehberi](docs/screenshots/tvguide.jpg)<br>**TV rehberi** – ön izlemeli zaman çizelgesi |
| ![Canlı TV](docs/screenshots/channel_list.jpg)<br>**Canlı TV** – şimdi ve sonraki program bir bakışta | ![Ayarlar](docs/screenshots/settings.jpg)<br>**Ayarlar** – gruplu, uzaktan kumanda dostu |

<sub>Ekran görüntüleri TV’de alınmıştır. Film görselleri, kanal logoları ve program verileri ilgili sahiplerine aittir; sağlayıcı kategorileri bulanıklaştırılmıştır.</sub>

## Kendi API anahtarlarınız (isteğe bağlı)

Uygulama **API anahtarı olmadan** gelir. Kendi ücretsiz anahtarlarınızı uygulamada **Ayarlar > API anahtarları** bölümüne girebilirsiniz:

| Hizmet | Kullanım | Anahtar |
|---|---|---|
| TMDB | Posterler, kendi dilinizde açıklamalar, oyuncular, fragmanlar, otomatik türler, bölüm bilgisi, benzer yapımlar | ücretsiz; aşağıya bakın |
| OMDb | IMDb ve Rotten Tomatoes puanları | ücretsiz; aşağıya bakın |
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
  Dict*.kt                            çeviri satırları (de¦en¦tr¦bs¦hr¦sr¦es¦fr¦it)
app/src/test/java/com/ipvio/stream/   JUnit testleri
app/src/main/res/font/                Outfit ve Inter (SIL Open Font License)
licenses/                             yazı tipi lisansları
```

## Lisans

Telif Hakkı (C) 2026 IPVIO Stream katkıcıları

IPVIO Stream özgür yazılımdır: GNU Genel Kamu Lisansı sürüm 3 koşullarıyla yeniden dağıtabilir ve/veya değiştirebilirsiniz (bkz. [LICENSE](LICENSE)). Uygulama GPL-3.0 lisanslı FFmpeg ses kod çözücüsünü içerdiği için GPL gereklidir. Kullanılan kütüphaneler, yazı tipleri ve hizmetler için [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) belgesine bakın.

“IPVIO” adı ve IPVIO logosu orijinal projeyi tanımlar. Değiştirilmiş sürümünüzü yayımlarsanız kullanıcıların karışmaması için kendi adınızı ve logonuzu kullanın.

## Sorumluluk reddi

Bu yazılım “olduğu gibi” sunulur; açık veya zımni hiçbir garanti verilmez. Yazarlar hiçbir yayın, kanal veya oynatma listesi sağlamaz, barındırmaz, satmaz ya da bağlantı vermez; kaynakların kullanım biçiminden sorumlu değildir. Hak sahiplerinin izni olmayan oynatma listelerini kullanmak bulunduğunuz ülkede yasa dışı olabilir.

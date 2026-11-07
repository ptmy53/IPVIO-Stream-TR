# Türkçe Kurulum ve Kullanım Rehberi

Bu rehber Android TV, Google TV, Amazon Fire TV ve Android telefon/tablet için IPVIO Stream kurulumu ve ilk kullanımı içindir.

> Uygulama yayın veya oynatma listesi sağlamaz. Yalnızca kullanmaya yasal hakkınız olan Xtream Codes, M3U veya Stalker portal kaynaklarını ekleyin.

## 1. Uygulamayı kurma

### Android TV / Google TV / Fire TV: Downloader ile

1. Cihazın uygulama mağazasından **Downloader** uygulamasını yükleyin.
2. Ayarlar → Uygulamalar → Özel uygulama erişimi → **Bilinmeyen uygulamaları yükle** bölümünden Downloader’a izin verin. Fire TV’de yol, Ayarlar → My Fire TV → Developer options altında olabilir.
3. Downloader’da şu adresi açın:
   ```
   https://github.com/ptmy53/IPVIO-Stream-TR/releases/latest/download/IPVIO-Stream.apk
   ```
4. İndirme tamamlanınca **Install / Yükle** seçeneğini seçin.
5. Uygulamayı açın. Hesap oluşturmanız gerekmez.

### Android telefon/tablet

APK dosyasını güvenilir bir kaynaktan telefona indirin. İndirdiğiniz uygulamanın tarayıcısı veya dosya yöneticisi için “bu kaynaktan uygulama yükle” iznini Android sorduğunda verin; ardından APK’yı açıp kurun. TV arayüzü için tasarlandığından telefonda yatay kullanım önerilir.

## 2. İlk oynatma listesini ekleme

Uygulamayı açınca **Oynatma listesi ekle** ekranı görünür. Aşağıdaki seçeneklerden yalnızca size ait olanı kullanın.

### Xtream Codes

Sağlayıcınızın verdiği bilgileri girin:

- **Sunucu:** ör. `http://sunucu-adresi:8080`
- **Kullanıcı adı**
- **Parola**

Sonra **Ekle** seçeneğini seçin. Uygulama önce listeyi indirir; `get.php` kullanılabilir değilse destekleyen sağlayıcılarda `player_api.php` ile otomatik olarak devam eder.

### M3U internet adresi

M3U seçeneğini açın, sağlayıcının verdiği tam adresi yapıştırın ve **Ekle** seçeneğini seçin. URL’nin `http://` olması mümkündür; bu durumda yayın trafiği ağda şifreli değildir. Mümkünse sağlayıcınızdan `https://` adresi isteyin.

### M3U dosyası

M3U dosyası cihazdaysa dosya seçiciden açın. TV’de uzun URL yazmak yerine telefon aktarım ekranını kullanabilirsiniz: listede gösterilen yerel ağ adresini telefonda açın, ekrandaki dört haneli kodu girin ve M3U dosyasını yükleyin.

### Stalker / Ministra portalı

**Stalker portalı** türünü seçin; sağlayıcının portal adresini (genelde `/c/` ile biter) ve size verdiği MAC adresini girin. Bu özellik canlı kanalları ve filmleri yükler; dizi desteği sağlayıcının portalına bağlıdır.

## 3. Temel kullanım

- **Canlı TV:** kategori, favori veya kendi kanal listenizi seçin. TV rehberi yüklendiyse mevcut ve sonraki program görünür.
- **Filmler / Diziler:** tür, yıl, puan ve kaynak filtrelerini kullanın. Birden fazla liste açıksa kaynak filtresi yararlıdır.
- **Oynatıcı:** OK tuşu kontrolleri açar. Ses parçası, altyazı, kalite, en-boy oranı ve teknik bilgi kontrollerden seçilir.
- **Favoriler:** kanalda OK tuşuna basılı tutun; film ve dizilerde ayrıntı sayfasından favorilere ekleyin.
- **Birden fazla liste:** Ayarlar → Oynatma listeleri bölümündeki “Birlikte göster” seçeneği film ve dizileri birleştirir.

## 4. TMDB ve OMDb anahtarları (isteğe bağlı)

Anahtarsız kullanım mümkündür. Kendi anahtarlarınız poster, açıklama, oyuncular, fragman ve puanlar için daha zengin bilgi sağlar.

1. Uygulamada **Ayarlar → API anahtarları** bölümünü açın.
2. [TMDB](https://www.themoviedb.org/settings/api) API Key ve/veya [OMDb](https://www.omdbapi.com/apikey.aspx) anahtarınızı girin.
3. **Kaydet ve denetle** seçeneğine basın.
4. Uygulamayı bir kez yeniden başlatın.

Anahtarlar cihazda saklanır. Google Drive yedeğine oynatma listesi kimlik bilgilerini de dahil etmeyi seçerseniz bu bilgiler Google Drive’daki uygulamaya ait gizli klasöre yüklenir.

## 5. Güncelleme

Uygulama, güncellemeleri günde en fazla bir kez denetler ve bildirim simgesinde gösterir. Güncelleme bulunduğunda indirip kurma işlemini Android onay ekranından tamamlamanız gerekir.

- Aynı uygulamanın üzerine güncelleme yapılabilmesi için tüm APK’lar **aynı imzalama anahtarıyla** imzalanmalıdır.
- Uygulama içinden APK yüklemek için Android, uygulamaya bir kez “bilinmeyen kaynaklardan yükleme” izni sorar.
- Bu kişisel fork henüz imzalı bir yayın oluşturmadıysa “release” bağlantısı çalışmaz; GitHub Actions’tan üretilen debug APK yalnızca test içindir. Kalıcı güncelleme için önce kişisel release anahtarınızı oluşturup `docs/RELEASE.md` yönergelerine göre release yayınlayın.

Ana proje yenilikleri için günlük senkron: [GUNLUK_GUNCELLEME.md](GUNLUK_GUNCELLEME.md).

## 6. Sorun giderme

| Sorun | Kontrol |
|---|---|
| Liste yüklenmiyor | Sunucu/hesap bilgisini kontrol edin; sağlayıcıya erişim ve aboneliği doğrulayın. |
| Yayın açılmıyor | İnterneti deneyin, başka bir kalite/kaynak seçin; sağlayıcı eşzamanlı bağlantı sınırını kontrol edin. |
| TV rehberi boş | Sağlayıcının XMLTV/EPG bilgisi vermesi gerekir; ilk yüklemeden sonra biraz bekleyin. |
| Güncelleme kurulamadı | Android’in uygulama yükleme iznini verin. APK başka anahtarla imzalıysa mevcut uygulamanın üstüne kurulamaz. |
| Telefon aktarımı açılmıyor | Telefon ve TV aynı Wi-Fi ağında olmalı; aktarım ekranını açık bırakın ve ekrandaki kodu kullanın. |

Daha fazla ayar açıklaması: [SETTINGS.md](SETTINGS.md) ve [FAQ.md](FAQ.md).

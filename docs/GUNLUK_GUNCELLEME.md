# Günlük Güncelleme (Upstream Senkron)

Bu depo, **Emrodev/IPVIO-Stream** deposunun Türkçe kişisel sürümüdür.
Amaç: ana projedeki yeni özellikleri ve hata düzeltmelerini kaybetmeden,
Türkçe dosyaları koruyarak her gün otomatik güncellemek.

## Otomatik akış (kurulu)

| | |
|---|---|
| Workflow | `.github/workflows/sync-upstream.yml` |
| Zamanlama | Her gün **04:00 (Türkiye saati)** / 01:00 UTC |
| Kaynak | `https://github.com/Emrodev/IPVIO-Stream.git` → `main` |
| Hedef | Bu deponun `main` dalı |

Davranış:

1. Upstream'de yeni commit yoksa hiçbir şey yapmaz.
2. Yeni commit varsa `upstream-sync/YYYY-MM-DD` dalını oluşturur ve **her zaman
   bir Pull Request (PR) açar**. Böylece kaynak değişiklikleri önce gözden
   geçirilir; workflow doğrudan `main` dalına yazmaz.
3. Çakışma varsa PR sayfası bunu gösterir; PR'ı GitHub arayüzünde
   "Resolve conflicts" ile çözersin.
4. Sonuç her çalıştırmada **Actions → Upstream Senkron → Summary** ekranında
   tablo hâlinde görünür.

### Elle çalıştırma

GitHub → **Actions** → **Upstream Senkron** → **Run workflow**.
`dry_run` seçeneğini işaretlersen sadece "yeni commit var mı" kontrolü yapılır,
hiçbir şey değiştirilmez.

### Çakışma çıkarsa ne yapılır?

Çakışma olduğunda en sık etkilenen yerler şunlardır:

- `README.md` (upstream her sürümde değiştiriyor)
- `Dict*.kt` (upstream yeni satır ekliyor, biz Türkçe sütununu iyileştiriyoruz)

Kural: **upstream'in yeni satırlarını koru, Türkçe metinleri bizim taraftan al.**
Türkçe metinler `DictA.kt`–`DictJ.kt` içindeki üçüncü sütundadır; çakışma
çözümünden sonra `testDebugUnitTest` komutunu çalıştırarak satır/yer tutucu
kontrolünü yeniden yap.

## Türkçe çeviriyle senkron sonrası kontrol

Upstream `DictA.kt … DictJ.kt` dosyalarını değiştirebilir. PR’ı merge etmeden
önce bu dosyaların Türkçe (üçüncü) sütununu gözden geçir. Ardından:

```powershell
.\gradlew.bat testDebugUnitTest
```

`DictConsistencyTest` yer tutucuların bütün dillerde eşleştiğini; eklenen
Türkçe testi ise Türkçe hücrelerin boş olmadığını ve İngilizce kalmadığını
kontrol eder.

## Manuel yöntem (Actions olmadan)

`scripts/update-from-upstream.ps1` dosyasını kullan:

```powershell
# Sadece kontrol
.\scripts\update-from-upstream.ps1 -CheckOnly

# Güncelle (çakışma yoksa merge, varsa dal + rapor)
.\scripts\update-from-upstream.ps1
```

## İlk kurulumda yapılması gerekenler

1. Depo **Settings → Actions → General → Workflow permissions**:
   *Read and write permissions* seçili olmalı (workflow `permissions:` bloğuyla
   da talep ediliyor, ama organizasyon ayarı bunu kısıtlayabilir).
2. `upstream-sync` etiketi yoksa workflow etiketsiz PR açar; sorun değil.
3. İlk senkronu elle bir kez çalıştırıp sonucu görmek en sağlıklısıdır.

## Sınırlar (dürüst notlar)

- GitHub Actions zamanlaması **garantili değildir**; yoğun saatlerde birkaç saat
  gecikebilir. "Her gün" beklentisi için yeterlidir.
- Upstream yeni bir **izin**, **bağımlılık** veya **ağ adresi** eklerse bu
  otomatik olarak senin APK'na girer. Bu yüzden `docs/GUVENLIK_DENETIMI.md`
  raporundaki "her senkrondan sonra kontrol" listesine bak.
- Otomatik senkron yalnızca **kodu** günceller; APK'yı yeniden derlemek gerekir
  (Actions → Build APK artifact, ya da yerelde `gradlew.bat assembleDebug`).

# Bu Depo Hakkında (Fork)

| | |
|---|---|
| Bu depo | `ptmy53/IPVIO-Stream-TR` |
| Kaynak proje | [`Emrodev/IPVIO-Stream`](https://github.com/Emrodev/IPVIO-Stream) |
| Lisans | **GNU GPL v3** (değişmedi — bkz. [LICENSE](LICENSE)) |
| Amaç | Kişisel kullanım + Türkçe yerelleştirme + günlük otomatik upstream senkronu |

IPVIO Stream'in tüm hakları orijinal yazarlara aittir. Bu depo, GPL-3.0'ın izin
verdiği şekilde türetilmiş bir sürümdür. Uygulama **hiçbir yayın/kanal/playlist
içermez**; yalnızca kullanıcının kendi eklediği kaynakları oynatır.

## Bu forkta değişenler

| Alan | Değişiklik |
|---|---|
| Türkçe | Uygulamadaki Türkçe sütun denetlendi/düzeltildi; `README.tr.md`, `docs/KURULUM_TR.md`, `app/src/main/res/values-tr/` eklendi |
| Güncelleme | Uygulama içi güncelleme adresi bu depoya çevrildi (`AppUpdate.DEFAULT_URL`) |
| Otomasyon | `.github/workflows/sync-upstream.yml` – her gün 04:00'te upstream senkronu |
| Araçlar | `scripts/update-from-upstream.ps1` – Actions olmadan manuel senkron |
| Belgeler | `docs/GUVENLIK_DENETIMI.md`, `docs/GUNLUK_GUNCELLEME.md`, `docs/CEVIRI_RAPORU_TR.md` |

Upstream ile ortak olan her şey (oynatıcı, Xtream/M3U/Stalker desteği, EPG,
kayıt, arayüz) orijinal projeden gelir ve günlük senkronla güncel kalır.

## Uygulama kimliği

- Paket adı: `com.ipvio.stream` (değiştirilmedi — mevcut kurulumun üzerine
  güncelleme yapılabilmesi ve ayarların/geçmişin korunması için).
- Uygulama adı: **IPVIO Stream**.
- Bir sürümü başkasına dağıtacaksan: GPL-3.0 gereği kaynağı paylaş ve orijinal
  projeyle karışmaması için kendi adını/logonu kullan (upstream README'deki rica).

## Kurulum ve günlük kullanım

- Kurulum: [docs/KURULUM_TR.md](docs/KURULUM_TR.md)
- Günlük akış: [docs/GUNLUK_GUNCELLEME.md](docs/GUNLUK_GUNCELLEME.md)
- Güvenlik incelemesi: [docs/GUVENLIK_DENETIMI.md](docs/GUVENLIK_DENETIMI.md)

## Yasal uyarı

Bu yazılım "olduğu gibi" sunulur, hiçbir garanti verilmez. Yazarlar hiçbir
yayın, kanal veya playlist sağlamaz, barındırmaz ve satmaz. Hak sahiplerinin
izni olmayan kaynakları kullanmak bulunduğun ülkede yasa dışı olabilir.

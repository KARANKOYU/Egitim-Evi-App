# app/src/main/res/values/KLASOR.md

Açık temanın kaynakları: sitenin renk değişkenlerinin Android karşılığı `renkler.xml` (35 renk), uygulamanın pencere teması
`temalar.xml` (`Tema`) ve tek metin kaynağı `strings.xml` (uygulamanın adı "Eğitim Evi").

## Bu dosya ne yapar?

Android, `values/` klasöründeki kaynakları varsayılan olarak, telefonun durumuna uyan özel bir klasör varsa (ör. koyu tema
açıkken `values-night/`) onu kullanır. Bu klasör açık temayı ve iki temada ortak olanı (uygulamanın adı) taşır; koyu tema
[../values-night/KLASOR.md](../values-night/KLASOR.md)'de, renk adları iki klasörde birebir aynı.

Renklerin kaynağı sitedir: site deposunda `public/css/parcalar/00-temel.css` (`--ana`, `--zemin` …). Adlar sitedeki
değişkenlerin baştaki `--` atılıp tireleri alt çizgiye çevrilmiş hâlidir (`--ana-acik` → `ana_acik`). Kod renkleri
[Tema](../../java/org/egitimevi/aile/Tema.md)'nın `Tema.renk(c, R.color.<ad>)` işleviyle okur; sabit renk kodu yazılmaz
(yazılırsa koyu tema bozulur). İki istisna var: eski "Çocuğun telefonu" ekranı
([AileEkrani](../../java/org/egitimevi/aile/AileEkrani.md)) kendi sabit renklerini kullanır, bu dosyalara bakmaz; `Tema`'nın
`avatarRengi`'si de baş harf avatarının zeminini kodda yazılı sekiz sabit renkten seçer (sitedeki `avatar()` gibi, iki temada
aynı; [Arayuz](../../java/org/egitimevi/aile/Arayuz.md)'un `avatar`'ı üstündeki harfleri sabit beyaz yazar).

## İçinde neler var?

### `renkler.xml`

Başındaki yorum: "Sitenin renk değişkenleri (… 00-temel.css), açık tema. Koyu tema: values-night/renkler.xml. Adlar
sitedekiyle aynı." `tools:ignore="UnusedResources"`: henüz kullanılmayan renkler lint uyarısı vermesin. Değerlerin hepsi
sitenin bugünkü açık temasıyla aynı (bu belge yazılırken tek tek karşılaştırıldı).

| Ad | Değer | Uygulamada ne için |
|---|---|---|
| `ana` | `#D62839` | Marka kırmızısı: birincil düğme zemini, seçili sekme, simgeler, bağlantı yazıları, okunmamış bildirim noktası, zil rozeti; temada `colorPrimary`, `colorAccent`, `colorControlActivated` |
| `ana_koyu` | `#A51D2C` | İkincil düğmenin yazısı (varsayılan etiketin yazısı da; o etiket bugün kullanılmıyor) |
| `ana_acik` | `#FDECEE` | Seçili sekmenin hapı, ikincil düğmenin zemini, kişi kodu kutusu, boş durum ve "+ Ekle" seçeneklerinin simge dairesi; temada `colorControlHighlight` |
| `ana_cizgi` | `#F6C3C9` | Dokunma dalgası (`Tema.dalgali`, simge düğmeleri), kişi kodu kutusunun çerçevesi |
| `ustune_yazi` | `#FFFFFF` | Kırmızı zemin üstündeki yazı (birincil düğme, zil rozeti) |
| `ikinci` | `#0FA3B1` | kullanılmıyor |
| `ikinci_acik` | `#E2F6F8` | kullanılmıyor |
| `vurgu` | `#F7B32B` | kullanılmıyor |
| `vurgu_acik` | `#FFF4D9` | kullanılmıyor |
| `zemin` | `#FFF9F5` | Sayfa zemini (`AnaEkran` kökü, temada `windowBackground`, `statusBarColor`, `colorPrimaryDark`), alttaki kısa mesajın yazısı, rozetin çerçevesi |
| `kart` | `#FFFFFF` | Kartlar, alt sekme çubuğu, portal penceresi; temada `navigationBarColor` |
| `kart_ust` | `#FFFFFF` | Form kutularının zemini |
| `yazi` | `#1D1F24` | Ana yazı rengi, üst çubuk simgeleri, alttaki kısa mesajın zemini; temada `textColorPrimary` |
| `soluk` | `#6B6566` | İkincil yazılar, seçili olmayan sekme; temada `textColorSecondary` |
| `cizgi` | `#F0E6E1` | Kart çerçevesi, satır ayırıcı |
| `cizgi_koyu` | `#E2D5CF` | Form kutusunun çerçevesi |
| `yesil`, `yesil_zemin`, `yesil_yazi` | `#15803D`, `#DCFCE7`, `#14532D` | Yeşil etiket ("Buradasın"); `yesil` tek başına kullanılmıyor |
| `kirmizi`, `kirmizi_zemin`, `kirmizi_yazi` | `#DC2626`, `#FEE2E2`, `#991B1B` | Form hatası (çerçeve ve yazı), Ayarlar'daki "Çıkış yap" (tehlikeli düğme) |
| `turuncu`, `turuncu_zemin`, `turuncu_yazi` | `#C2410C`, `#FFEDD5`, `#9A3412` | Turuncu etiket ("Onay bekliyor"); `turuncu` tek başına kullanılmıyor |
| `mavi`, `mavi_zemin`, `mavi_yazi` | `#1D4ED8`, `#DBEAFE`, `#1E40AF` | Mavi etiket ve şerit (seçilen okul, ana sayfadaki bilgi şeridi); `mavi` tek başına kullanılmıyor |
| `gri_zemin` | `#ECEAF3` | Gri etiketin zemini (etiket tabloda var, bugün hiçbir ekran kullanmıyor) |
| `bordo` | `#7F1D1D` | Bordo etiketin zemini (aynı durum) |
| `harita_okul`, `harita_ev`, `harita_servis`, `harita_ben` | `#1D4ED8`, `#15803D`, `#D62839`, `#0D9488` | kullanılmıyor (harita ekranları için hazır) |
| `perde` | `#731F1B2E` | kullanılmıyor (açılır pencerenin arkasındaki perde; sitede `rgba(31, 27, 46, .45)`) |

Etiket renkleri [Arayuz](../../java/org/egitimevi/aile/Arayuz.md)'un `etiketRenkleri` tablosundan gelir (`YESIL`, `KIRMIZI`,
`TURUNCU`, `MAVI`, `GRI`, `BORDO`, varsayılan); bugün ekranlar yalnız `YESIL`, `TURUNCU` ve `MAVI`'yi kullanıyor. 35 rengin
12'si kodda hiç geçmiyor: `ikinci`, `ikinci_acik`, `vurgu`, `vurgu_acik`, `yesil`, `turuncu`, `mavi`, dört harita rengi ve
`perde`.

### `temalar.xml`

```
<style name="Tema" parent="@android:style/Theme.Material.Light.NoActionBar">
```

Android'in kendi Material açık teması, eylem çubuğu olmadan (üst çubuğu `AnaEkran` kendisi çizer). Yorum: "Açık tema;
koyusu values-night/temalar.xml. Çubuklar zemin renginde, simgeleri koyu."

| Öğe | Değer | Ne |
|---|---|---|
| `android:colorPrimary` | `@color/ana` | Sistemin kullandığı ana renk |
| `android:colorPrimaryDark` | `@color/zemin` | Eski sürümlerde durum çubuğu |
| `android:colorAccent` | `@color/ana` | Seçili onay kutusu, imleç gibi vurgular |
| `android:colorControlActivated` | `@color/ana` | Etkin denetimler |
| `android:colorControlHighlight` | `@color/ana_acik` | Sistem denetimlerinin dokunma vurgusu |
| `android:windowBackground` | `@color/zemin` | Pencerenin zemini (açılırken görünen ilk renk) |
| `android:statusBarColor` | `@color/zemin` | Durum çubuğu |
| `android:navigationBarColor` | `@color/kart` | Gezinme çubuğu |
| `android:windowLightStatusBar` | `true` | Durum çubuğundaki simgeler koyu (açık zemin üstünde okunsun) |
| `android:textColorPrimary` | `@color/yazi` | Sistem bileşenlerinin ana yazısı |
| `android:textColorSecondary` | `@color/soluk` | İkincil yazı |

Manifest bu temayı bütün uygulamaya verir (`android:theme="@style/Tema"`, [../../KLASOR.md](../../KLASOR.md)).

### `strings.xml`

Tek kaynak: `uygulama_adi` = "Eğitim Evi". Manifestte `android:label`: başlatıcıda, uygulama listesinde ve izin
pencerelerinde görünen ad. Ekranlardaki bütün metinler Java kodunda yazılıdır (uygulama yalnız Türkçe; lint'in
`SetTextI18n` uyarısı bu yüzden kapalı, [../../../../KLASOR.md](../../../../KLASOR.md)).

## Kimle konuşur?

- [Tema](../../java/org/egitimevi/aile/Tema.md) — `Tema.renk(c, id)` (`Context.getColor`), `Tema.zemin`, `Tema.dalgali`.
- [Arayuz](../../java/org/egitimevi/aile/Arayuz.md) — renklerin çoğunu kullanan parça takımı (düğme, kart, form alanı,
  etiket, boş durum).
- Doğrudan kullananlar: [AnaEkran](../../java/org/egitimevi/aile/AnaEkran.md) (zemin, üst ve alt çubuk, rozet, kısa mesaj),
  [BildirimlerSayfasi](../../java/org/egitimevi/aile/BildirimlerSayfasi.md) (okunmamış noktası),
  [EkleSayfasi](../../java/org/egitimevi/aile/EkleSayfasi.md), [GirisSayfasi](../../java/org/egitimevi/aile/GirisSayfasi.md),
  [KayitSayfasi](../../java/org/egitimevi/aile/KayitSayfasi.md), [KvkkSayfasi](../../java/org/egitimevi/aile/KvkkSayfasi.md),
  [PortalSecici](../../java/org/egitimevi/aile/PortalSecici.md), [KisiKodu](../../java/org/egitimevi/aile/KisiKodu.md),
  [AyarlarSayfasi](../../java/org/egitimevi/aile/AyarlarSayfasi.md), [AnaSayfa](../../java/org/egitimevi/aile/AnaSayfa.md),
  [DogrulamaSorusu](../../java/org/egitimevi/aile/DogrulamaSorusu.md), [KodSayfasi](../../java/org/egitimevi/aile/KodSayfasi.md),
  [SifreSayfasi](../../java/org/egitimevi/aile/SifreSayfasi.md),
  [SifremiUnuttumSayfasi](../../java/org/egitimevi/aile/SifremiUnuttumSayfasi.md).
- Manifest — `@style/Tema`, `@string/uygulama_adi`.
- Kaynak: site deposunda `public/css/parcalar/00-temel.css` (değerler elle kopyalandı, araç yok).

## Nasıl çalışır (adım adım)?

```
telefon açık temada ─► Android values/renkler.xml'i seçer
Arayuz.dugme(..., BIRINCIL) ─► Tema.renk(c, R.color.ana) ─► #D62839 zemin, R.color.ustune_yazi ─► beyaz yazı
telefon koyu temada ─► aynı kod, aynı ad ─► values-night/renkler.xml ─► #FF5C6C zemin, #15181D yazı
```

Renk, görünüm kurulurken bir kez okunur; uygulama açıkken tema değişirse var olan görünümler eski renkte kalır (manifestte
`uiMode` değişimini ekran kendisi karşılıyor ama yeniden çizmiyor; [Arayuz](../../java/org/egitimevi/aile/Arayuz.md)).

## Dikkat!

- **Sitedeki rengi değiştirirsen burada da değiştir.** İki depo arasında araç yok; site deposunda `00-temel.css`'teki bir
  değer değişince bu dosyayı ve koyu karşılığını elle güncelle.
- **Bir rengi eklerken iki dosyaya birden ekle.** `values-night/renkler.xml`'de olmayan bir ad koyu temada açık temanın
  değerini alır (yanlış görünür ama hata vermez).
- **Android 15 ve üstünde** (uygulama `targetSdk 36`) pencere kenardan kenaradır: `statusBarColor` ve `navigationBarColor`
  yok sayılır, çubukların arkasında sayfanın zemini görünür; `windowLightStatusBar` yine geçerlidir. `AnaEkran` çubukların
  kapladığı boşluğu kendisi bırakır.
- **Kullanılmayan renkleri silme**, rol ekranları (harita, servis, açılır pencere) için hazırlandılar; `tools:ignore` lint'i
  susturuyor.
- **Metinleri `strings.xml`'e taşımak** çok dil işinin parçası olacak; bugün yalnız uygulamanın adı burada.

## Testleri

- Otomatik test yok; lint (`./gradlew --offline lintDebug`) bozuk renk ya da eksik kaynak adını derlemede yakalar.
- Elle: telefonu açık temaya al, uygulamada birincil düğmenin kırmızı (`#D62839`), sayfa zemininin kırık beyaz (`#FFF9F5`)
  olduğunu gör; site ile yan yana koy, aynı renkler olmalı.
- Bu belge yazılırken değerler sitedeki `00-temel.css` ile karşılaştırıldı (açık temanın bütün değerleri aynı); uygulama
  çalıştırılmadı.

## Son durum

- Klasörün geçmişi: `34b45f1 commit 1` (2026-09-26: `strings.xml` — "Eğitim Evi Aile"), `f70aeca commit 5` (`strings.xml`:
  ad "Eğitim Evi" oldu), `e96c5f2 commit 6` (2026-09-26: `renkler.xml` ve `temalar.xml` eklendi; manifest aynı commit'te
  Android'in hazır temasından `@style/Tema`'ya geçti).
- Açık iş yok.
- Planlı işlerden bu klasörü etkileyecekler: "Çok dil" (uygulama dili seçici; metinler bir katalogdan gelecek), "Android
  yerel uygulama (bütün roller)" (harita, servis ve açılır pencere renkleri kullanılmaya başlayacak).

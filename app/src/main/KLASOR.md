# app/src/main/KLASOR.md

Bütün paketlerin (deneme ve yayın) ortak kaynağı: kod dışı tek dosyası `AndroidManifest.xml` — uygulamanın istediği her
izin, iki ekranı, üç servisi, bir yayın alıcısı ve uygulama düzeyindeki ayarlar (simge, ad, tema, ağ güvenliği, yedek
kuralları); yanında `java/` (kod) ve `res/` (kaynaklar).

## Bu dosya ne yapar?

Android bir uygulamayı kurarken ve çalıştırırken önce manifestine bakar: hangi izinleri isteyebilir, telefonun başlatıcısında
hangi ekranla açılır, hangi servisleri ve alıcıları var, hangi tema ve simgeyle görünür. Bu belge o dosyayı satır satır
anlatır; her izin ve bileşen için kodun neresinin kullandığını söyler.

Klasörün dizilişi:

| Yol | Ne | Belgesi |
|---|---|---|
| `AndroidManifest.xml` | İzinler, bileşenler, uygulama ayarları | bu belge |
| `java/org/egitimevi/aile/` | 32 Java sınıfı | her sınıfın `.md`'si; liste [../../../TANITIM.md](../../../TANITIM.md)'de |
| `res/drawable/` | Simgeler | [res/drawable/KLASOR.md](res/drawable/KLASOR.md) |
| `res/values/` | Açık tema renkleri, tema, uygulama adı | [res/values/KLASOR.md](res/values/KLASOR.md) |
| `res/values-night/` | Koyu tema | [res/values-night/KLASOR.md](res/values-night/KLASOR.md) |
| `res/xml/` | Ağ güvenliği ve yedek kuralları | [res/xml/KLASOR.md](res/xml/KLASOR.md) |
| `res/mipmap-anydpi/` | Başlatıcı simgesi | [res/mipmap-anydpi/KLASOR.md](res/mipmap-anydpi/KLASOR.md) |

Deneme paketine özel kaynak bu klasörde değil, kardeşinde: [../debug/res/xml/KLASOR.md](../debug/res/xml/KLASOR.md).

## İçinde neler var?

### `AndroidManifest.xml`

**Baştaki yorum** "Ana ekran site (WebView)" der: `commit 5`'te yazıldı, `commit 6`'da uygulama yerel ekranlara geçtiği hâlde
güncellenmedi. Bugün ana ekran siteyi açmaz; geri kalan cümleler (bildirim yoklaması, servisçinin sefer konumu, çocuğun
telefonunda uygulama kapatma ya da kilitleme olmaması) doğru.

#### İzinler (`<uses-permission>`)

| İzin | Ne için | Kim kullanır |
|---|---|---|
| `INTERNET` | Sunucuyla bütün konuşma | [Api](java/org/egitimevi/aile/Api.md) |
| `ACCESS_NETWORK_STATE` | Wi-Fi mi, mobil veri mi, bağlantı var mı; ağ şartlı iş kurmak | [IzlemeServisi](java/org/egitimevi/aile/IzlemeServisi.md) (`agTuru`); [Bildirimler](java/org/egitimevi/aile/Bildirimler.md) (yoklama işlerine "ağ olsun" şartı, `setRequiredNetworkType`; Android belgelerine göre Android 14'ü hedefleyen uygulama bu izin olmadan ağ şartlı iş kuramaz) |
| `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` | Konum | [IzlemeServisi](java/org/egitimevi/aile/IzlemeServisi.md), [SeferServisi](java/org/egitimevi/aile/SeferServisi.md); izni [AileEkrani](java/org/egitimevi/aile/AileEkrani.md) ister |
| `ACCESS_BACKGROUND_LOCATION` | Uygulama kapalıyken de konum ("Her zaman izin ver"; Android 10+) | [AileEkrani](java/org/egitimevi/aile/AileEkrani.md) ister |
| `FOREGROUND_SERVICE` | Bildirim çubuğunda görünen sürekli servis | iki konum servisi (`startForeground`) |
| `FOREGROUND_SERVICE_LOCATION` | Android 14+ konum türündeki ön plan servisi | iki konum servisi |
| `POST_NOTIFICATIONS` | Bildirim göstermek (Android 13+ çalışma anında istenir) | [AnaEkran](java/org/egitimevi/aile/AnaEkran.md) (anahtar alınınca), [AyarlarSayfasi](java/org/egitimevi/aile/AyarlarSayfasi.md) ("Telefon bildirimleri"), [AileEkrani](java/org/egitimevi/aile/AileEkrani.md) |
| `RECEIVE_BOOT_COMPLETED` | Telefon açılınca haber almak; yeniden açılışta silinmeyen iş | [BaslatmaAlici](java/org/egitimevi/aile/BaslatmaAlici.md); [Bildirimler](java/org/egitimevi/aile/Bildirimler.md) (15 dakikalık düzenli iş `setPersisted(true)` ile kurulur; Android bunu yalnız bu izni olan uygulamaya izin verir) |
| `PACKAGE_USAGE_STATS` | Ekran süresi (hangi uygulama ne kadar önde) | [Kullanim](java/org/egitimevi/aile/Kullanim.md); kişi Ayarlar > Kullanım erişimi'nden açar |

`PACKAGE_USAGE_STATS` "korumalı" bir izindir: manifestte yazmak yetmez, telefonun ayarlarından elle verilir; lint uyarısı
`tools:ignore="ProtectedPermissions"` ile susturulmuş. Bugün manifestte **olmayan** izin: pil kısıtlamasından muaf olmayı
isteyen izin `commit 4`'te çıkarıldı (Play Store kuralına takılıyor); onun yerine `AileEkrani` uygulamanın ayar sayfasını
açar, öğrenci Pil > Kısıtlamasız'ı seçer.

#### `<queries>`

`MAIN` + `LAUNCHER` niyeti: Android 11 ve üstünde uygulama yalnız "görebildiği" paketlerin bilgisini alır. Bu satır,
başlatıcıda simgesi olan uygulamaları görünür yapar; `Kullanim` ekran süresi listesinde paket adı yerine uygulamanın adını
yazabilsin diye.

#### `<application>`

| Nitelik | Değer | Anlamı |
|---|---|---|
| `allowBackup` | `false` | Uygulamanın verisi Android yedeğine girmez |
| `dataExtractionRules` | `@xml/veri_aktarimi` | Android 12+: buluta yedek ve telefondan telefona aktarım yok ([res/xml/KLASOR.md](res/xml/KLASOR.md)) |
| `fullBackupContent` | `@xml/yedek_yok` | Android 11 ve altı: yedek yok |
| `icon` | `@mipmap/ic_launcher` | Başlatıcı simgesi |
| `label` | `@string/uygulama_adi` | "Eğitim Evi" |
| `networkSecurityConfig` | `@xml/ag_guvenligi` | Yayın paketinde yalnız https; deneme paketinde kendi dosyası http'ye izin verir |
| `supportsRtl` | `true` | Sağdan sola yazılan dillere izin (bugün yalnız Türkçe) |
| `theme` | `@style/Tema` | Açık/koyu tema ([res/values/KLASOR.md](res/values/KLASOR.md)) |
| `enableOnBackInvokedCallback` | `true` (`tools:targetApi="33"`) | Android 13+ yeni geri hareketi; `AnaEkran` kendi geri çağrısını kaydeder |

#### Bileşenler

- **`.AnaEkran` (activity)** — `exported="true"`, `MAIN`/`LAUNCHER`: başlatıcıdaki "Eğitim Evi" bu ekranı açar.
  `launchMode="singleTask"`: tek örnek çalışır; bildirime dokunmak yeni ekran açmaz, var olana `onNewIntent` ile gelir.
  `configChanges="orientation|screenSize|screenLayout|keyboardHidden|uiMode"`: döndürmede, klavyede ve koyu/açık tema
  değişiminde ekran yeniden kurulmaz (sayfa yığını kaybolmasın diye). `windowSoftInputMode="adjustResize"`: klavye açılınca
  içerik küçülür, kutular klavyenin altında kalmaz.
- **`.AileEkrani` (activity)** — `exported="false"`, `adjustResize`. "Çocuğun telefonu" ekranı. Uygulamanın içinden açılır:
  öğrencinin Ayarlar'ındaki "Bu telefonu velimle paylaş" ve izleme servisinin bildirimi.
- **`.SeferServisi` (service)** — `exported="false"`, `foregroundServiceType="location"`. Servisçinin seferi sürerken
  konum gönderir; bugün onu başlatan ekran yok ([SeferServisi](java/org/egitimevi/aile/SeferServisi.md)).
- **`.BildirimIsi` (service)** — `exported="false"`, `permission="android.permission.BIND_JOB_SERVICE"`: yalnız Android'in
  iş zamanlayıcısı (`JobScheduler`) çalıştırabilir. Bildirim yoklaması ([BildirimIsi](java/org/egitimevi/aile/BildirimIsi.md)).
- **`.IzlemeServisi` (service)** — `exported="false"`, `foregroundServiceType="location"`. Çocuğun telefonunun konumu ve
  ekran süresi ([IzlemeServisi](java/org/egitimevi/aile/IzlemeServisi.md)).
- **`.BaslatmaAlici` (receiver)** — `exported="true"`, `BOOT_COMPLETED` ve `MY_PACKAGE_REPLACED`: telefon açılınca ve
  uygulama güncellenince izleme servisini ve bildirim yoklamasını yeniden kurar
  ([BaslatmaAlici](java/org/egitimevi/aile/BaslatmaAlici.md)).

## Kimle konuşur?

- **Android sistemi:** paket yöneticisi manifesti kurulumda okur; başlatıcı `AnaEkran`'ı, iş zamanlayıcısı `BildirimIsi`'ni,
  açılış ve güncelleme yayınları `BaslatmaAlici`'yı çalıştırır.
- **Kod:** `java/org/egitimevi/aile/` altındaki sınıflar (yukarıdaki tablolar). Bileşen adları `.` ile başlar, yani
  `namespace`'e (`org.egitimevi.aile`, [../../KLASOR.md](../../KLASOR.md)) göre çözülür.
- **Kaynaklar:** `@mipmap/ic_launcher`, `@string/uygulama_adi`, `@style/Tema`, `@xml/ag_guvenligi`, `@xml/veri_aktarimi`,
  `@xml/yedek_yok` — bu klasörün `res/`'inde. Deneme paketinde `@xml/ag_guvenligi` [../debug/res/xml/KLASOR.md](../debug/res/xml/KLASOR.md)'deki
  dosyadan gelir.

## Nasıl çalışır (adım adım)?

```
başlatıcıda "Eğitim Evi" ─► AnaEkran (singleTask) ─► oturum yoksa giriş, varsa sekmeler
                                ├─ girişten sonra cihaz anahtarı ─► POST_NOTIFICATIONS iste ─► JobScheduler'a BildirimIsi
                                └─ öğrenci: Ayarlar ─► "Bu telefonu velimle paylaş" ─► AileEkrani (exported=false)
                                                        ─► konum izinleri ─► IzlemeServisi (ön plan, location)
iş zamanlayıcısı ─► BildirimIsi (BIND_JOB_SERVICE) ─► yeni bildirim ─► bildirim çubuğu ─► dokun ─► AnaEkran.onNewIntent
telefon açıldı / uygulama güncellendi ─► BaslatmaAlici ─► IzlemeServisi.baslat + bildirim yoklamasını kur
```

## Dikkat!

- **Baştaki yorum eskidi** ("site (WebView)"); kod değil, yalnız yorum.
- **`<queries>`'te ana ekran niyeti yok.** `Kullanim` ana ekran uygulamasını (başlatıcıyı) ekran süresinden ayıklamak için
  `CATEGORY_HOME` sorgular; manifest yalnız `LAUNCHER`'ı açtığı için Android 11+'da simgesi olmayan bir başlatıcı listeye
  "uygulama" olarak girebilir ([Kullanim](java/org/egitimevi/aile/Kullanim.md); telefonda denenmedi). Öneri: `<queries>`'e
  `MAIN` + `HOME` niyeti.
- **`uiMode` ekranın kendisine bırakılmış ama karşılanmıyor.** `AnaEkran`'da `onConfigurationChanged` yok: uygulama
  açıkken telefon koyu temaya geçerse var olan görünümler eski renklerinde kalır ([Arayuz](java/org/egitimevi/aile/Arayuz.md)).
- **`AileEkrani`'nde `configChanges` yok:** döndürünce ekran baştan kurulur, yazılanlar gider
  ([AileEkrani](java/org/egitimevi/aile/AileEkrani.md)).
- **`BaslatmaAlici` dışa açık (`exported="true"`)** çünkü sistem yayınlarını alması gerekiyor; dinlediği iki yayını
  yalnız sistem gönderebilir, sınıf da başka eylemleri yok sayar.
- **Konum servisi kuralları:** Android 14+ konum türünde ön plan servisi için hem `FOREGROUND_SERVICE_LOCATION` izni hem
  verilmiş bir konum izni gerekir; izin yokken `startForeground` hata fırlatır. `IzlemeServisi` konum iznine
  `startForeground`'dan önce bakar; `SeferServisi` önce `startForeground` der, izne sonra bakar (bugün onu başlatan
  olmadığı için etkisi yok; başlatan bir ekran yazılırken sıra düzeltilmeli). Yeni bir ön plan servisi eklersen türünü
  (`foregroundServiceType`) ve türün iznini birlikte yaz.
- **İzin silerken kullananların hepsine bak.** `RECEIVE_BOOT_COMPLETED` ve `ACCESS_NETWORK_STATE`'i yalnız alıcı ve izleme
  servisi kullanmıyor; `Bildirimler`'in iş zamanlaması da onlara dayanır. Biri kaldırılırsa `Bildirimler.zamanla` iş
  kurarken hata fırlatabilir (Android belgelerine göre; telefonda denenmedi).
- **Arka planda konum** Android 11+'da uygulamadan doğrudan verilemez; kişi ayar sayfasında "Her zaman izin ver"i seçer
  (`AileEkrani`'deki düğme yazısı bunu söyler).
- **Yeni izin eklemek KVKK işidir.** Kişisel veri toplayan her yeni izin sitenin aydınlatma metnine de yazılmalı (site
  deposunun kuralı).

## Testleri

- Otomatik test yok. Manifest hataları derlemede ve `./gradlew --offline lintDebug`'da yakalanır (ör. eksik
  `foregroundServiceType`, korumalı izin).
- Elle: deneme paketini kur; telefonun Ayarlar > Uygulamalar > Eğitim Evi > İzinler sayfasında bildirim ve konum izinlerini
  gör; öğrenciyle "Bu telefonu velimle paylaş"ı aç, konum izni ver, bildirim çubuğunda "Konumun ve ekran süren velinle
  paylaşılıyor" görünmeli; telefonu yeniden başlat, `BaslatmaAlici` servisi yeniden başlatmalı (beklenen davranış; bu
  belge için denenmedi).
- Bu belge yazılırken derleme ya da öykünücü çalıştırılmadı.

## Son durum

- `AndroidManifest.xml`'in geçmişi: `34b45f1 commit 1` (2026-09-26, Eğitim Evi Aile'nin manifesti), `3875db7 commit 4`,
  `f70aeca commit 5`, `e96c5f2 commit 6` (2026-09-26).
- Son üç değişiklik, farklarına göre:
  - `e96c5f2 commit 6`: tema `@android:style/Theme.Material.Light.NoActionBar`'dan uygulamanın kendi `@style/Tema`'sına
    geçti (koyu tema `values-night`'tan); `enableOnBackInvokedCallback="true"` ve `tools:targetApi="33"` eklendi.
  - `f70aeca commit 5`: baştaki yorum tek uygulamayı anlatır oldu; `AnaEkran`'a `singleTask` ve `configChanges`; yeni
    bileşenler `AileEkrani`, `SeferServisi`, `BildirimIsi`.
  - `3875db7 commit 4`: pil kısıtlamasından muafiyet isteyen izin çıktı; `dataExtractionRules` ve `fullBackupContent`
    eklendi.
- Bilinen açıklar (kod değiştirilmedi): eski yorum, `<queries>`'te `HOME` olmaması, `uiMode`'un karşılanmaması.
- Planlı işlerden bu dosyayı etkileyecekler: "Android yerel uygulama (bütün roller) + doğrulayıcı" — doğrulayıcının
  `otpauth://` bağlantılarını açabilmesi için yeni bir niyet süzgeci; "uygulama kendini güncellesin" eki — indirilen
  APK'yı yükleyiciye vermek için bir dosya sağlayıcı (ve yalnız GitHub paketinde paket yükleme izni); "Android geri tuşu"
  (geri hareketi `AnaEkran`'da, manifest ayarı yerinde); "Çok dil" (`supportsRtl` zaten açık).

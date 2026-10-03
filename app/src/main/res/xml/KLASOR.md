# app/src/main/res/xml/KLASOR.md

Uygulamanın güvenlik ayarları, üç küçük XML: yayın paketinin yalnız https ile bağlanmasını şart koşan `ag_guvenligi.xml`,
Android 12 ve üstünde buluta yedeği ve telefondan telefona aktarımı kapatan `veri_aktarimi.xml`, Android 11 ve altında
yedeği kapatan `yedek_yok.xml`.

## Bu dosya ne yapar?

Bu üç dosyayı kod okumaz; manifest onları `<application>` niteliklerinde gösterir, Android kendisi uygular
([../../KLASOR.md](../../KLASOR.md)):

```
android:networkSecurityConfig="@xml/ag_guvenligi"
android:dataExtractionRules="@xml/veri_aktarimi"
android:fullBackupContent="@xml/yedek_yok"
```

İki kaygıyı karşılarlar:

1. **Şifresiz bağlantı olmasın.** Telefonla sunucu arasında giden oturum anahtarı, şifre, konum ve bildirimler ağda açık
   okunmasın. Yayın paketi yalnız https ile bağlanır. Evdeki deneme sunucusu için http yalnız deneme paketinde açılır; onun
   dosyası ayrı: [../../../debug/res/xml/KLASOR.md](../../../debug/res/xml/KLASOR.md).
2. **Anahtarlar telefondan çıkmasın.** Telefonda oturum anahtarı, bildirim yoklayan uygulama anahtarı ve çocuğun
   telefonunun anahtarı durur (üç ayar dosyası: `oturum`, `uygulama`, `aile`; ayrıca gönderilmeyi bekleyen konumlar
   `kuyruk.json`). Bunlar buluta yedeklenip yeni bir telefona geri yüklenirse o telefon, örneğin çocuğun yerine konum
   gönderebilirdi. Yedek ve aktarım bu yüzden tamamen kapalı; yeni telefonda kişi yeniden giriş yapar, çocuğun telefonu
   yeniden bağlanır.

## İçinde neler var?

### `ag_guvenligi.xml`

```
<network-security-config>
    <base-config cleartextTrafficPermitted="false" />
</network-security-config>
```

Bütün adreslere şifresiz (http) trafik kapalı. Yorumu: "Yayın paketi yalnızca HTTPS ile bağlanır. Evdeki deneme sunucusu
için (ör. `http://192.168.1.20:3000`) şifresiz bağlantı yalnızca deneme (debug) paketinde açıktır:
`app/src/debug/res/xml/ag_guvenligi.xml`." Deneme paketinde aynı adlı o dosya bunun yerine geçer.

Bu kural Android'in ağ katmanında işler: kod bir http adresine bağlanmaya kalksa bile yayın paketinde bağlantı kurulmaz.
Kodun kendi denetimi ayrıca var: [Api](../../java/org/egitimevi/aile/Api.md)'nin `adresSorunu`'su https dışını yalnız yerel
ağ adreslerinde kabul eder.

### `veri_aktarimi.xml` (Android 12 ve üstü)

`<data-extraction-rules>` içinde iki bölüm:

- `<cloud-backup>` — buluta (Google hesabına) yedek;
- `<device-transfer>` — eski telefondan yenisine doğrudan aktarım.

İkisinde de beş alanın tamamı (`root`, `file`, `database`, `sharedpref`, `external`; her biri `path="."`) dışarıda
bırakılır. Yorumu: "Android 12 ve üstü: hiçbir şey buluta yedeklenmez ve yeni telefona taşınmaz. Cihaz anahtarı bu telefona
aittir; başka telefona kopyalanırsa o telefon çocuğun yerine konum gönderebilirdi. Yeni telefonda uygulama yeniden bağlanır."

Neden ayrıca gerekiyor: Android 12 ve üstünde `allowBackup="false"` buluta yedeği kapatır ama telefondan telefona
aktarımı kendi başına kapatmaz; aktarım ancak bu kurallarla kapanır.

### `yedek_yok.xml` (Android 11 ve altı)

`<full-backup-content>` içinde aynı beş alanın hepsi `exclude`. Yorumu: "Android 11 ve altı: hiçbir şey yedeklenmez (bkz.
veri_aktarimi.xml)." Uygulama Android 8.0'dan (`minSdk 26`) başladığı için 8–11 arası telefonlar bu dosyaya bakar.

## Kimle konuşur?

- **Manifest** üçünü de `<application>`'da gösterir ([../../KLASOR.md](../../KLASOR.md)); `allowBackup="false"` ile birlikte
  çalışırlar.
- **Android sistemi** uygular: ağ katmanı `ag_guvenligi.xml`'i, yedek ve aktarım servisi öteki ikisini.
- **Korudukları veri:** [Oturum](../../java/org/egitimevi/aile/Oturum.md) ("oturum" ayar dosyası),
  [UygulamaAyar](../../java/org/egitimevi/aile/UygulamaAyar.md) ("uygulama"), [Ayarlar](../../java/org/egitimevi/aile/Ayarlar.md)
  ("aile"), [Kuyruk](../../java/org/egitimevi/aile/Kuyruk.md) (`kuyruk.json`).
- **Kodda paralel denetim:** [Api](../../java/org/egitimevi/aile/Api.md) (`adresSorunu`).
- **Deneme paketindeki karşılığı:** [../../../debug/res/xml/KLASOR.md](../../../debug/res/xml/KLASOR.md).

## Nasıl çalışır (adım adım)?

```
yayın paketi, adres http://ornek.com
  Api.adresSorunu ─► "İnternetteki sunucuya yalnızca https:// ile bağlanılır."  (kod durdurur)
yayın paketi, adres http://192.168.1.20:3000 (yerel ağ)
  Api.adresSorunu ─► geçer ─► HttpURLConnection ─► ag_guvenligi: cleartextTrafficPermitted=false
                   ─► Android bağlantıyı reddeder (yayında yerel http de yok)
deneme paketi, aynı adres ─► debug/res/xml/ag_guvenligi.xml (true) ─► bağlanır

Android 12+ telefon yenisine aktarılıyor ─► veri_aktarimi: device-transfer hepsi hariç ─► uygulama boş gelir
Android 9 telefon yedek alıyor          ─► allowBackup=false (+ yedek_yok) ─► uygulama yedeğe girmez
```

## Dikkat!

- **Bu dosyaya `cleartextTrafficPermitted="true"` yazma**, belirli bir alan adı için bile. Deneme ihtiyacı deneme paketinin
  kendi dosyasıyla karşılanıyor; ana dosya yayın paketine girer.
- **Yeni bir veri dosyası eklersen** (ör. doğrulayıcının şifreli anahtarları) bu kurallar zaten her şeyi dışarıda bırakır;
  ama `allowBackup`'ı açan ya da buradan bir alanı çıkaran bir değişiklik bütün anahtarları yedeğe sokar.
- **Yedek kapalı olmanın bedeli:** telefon değişince ya da uygulama silinip kurulunca kişi yeniden giriş yapar, çocuğun
  telefonu yeniden bağlanır, gönderilmemiş konumlar gider. Bu bilerek seçildi.
- Bir yerel ağ sunucusuna yayın paketiyle bağlanamazsın; denemeler her zaman deneme paketiyle yapılır.

## Testleri

- Otomatik test yok. Lint, `dataExtractionRules`'un ve `fullBackupContent`'in eksik ya da bozuk olmasını uyarır.
- Elle (deneme paketinde değil, yayın paketinde): http bir adrese bağlanmayı dene, bağlanmamalı. Yedek için Android'in
  kendi yedek komutlarıyla (`adb shell bmgr`) uygulamanın yedeğe girmediği görülebilir.
- Bu belge yazılırken hiçbiri denenmedi; yazılanlar dosyalara, manifeste ve koda dayanır.

## Son durum

- Klasörün geçmişi: `34b45f1 commit 1` (2026-09-26: `ag_guvenligi.xml` ilk hâli), `3875db7 commit 4` (2026-09-26).
- `3875db7 commit 4`, farkına göre: `ag_guvenligi.xml`'de `cleartextTrafficPermitted` `true`'dan `false`'a döndü ve yorum
  "şifresiz bağlantı yalnız deneme paketinde" oldu (o güne kadar yayın paketi de http'ye açıktı, yerel ağ sınırını yalnız
  `Api` koyuyordu); http izni `app/src/debug/res/xml/ag_guvenligi.xml`'e taşındı. Aynı commit'te `veri_aktarimi.xml` ve
  `yedek_yok.xml` eklendi.
- Açık iş yok.
- Planlı işlerden bu klasörü etkileyecekler: "uygulama kendini güncellesin" eki indirilen APK'yı paket yükleyiciye vermek
  için bir dosya sağlayıcı istiyor; onun paylaşılacak klasörleri tanımlayan XML'i büyük olasılıkla buraya gelecek.
  "Doğrulayıcı" işi gizli anahtarları telefonda şifreli saklayacak; tanım "yedeğe girmez (allowBackup zaten kapalı;
  dataExtractionRules)" diyerek bu dosyalara dayanıyor.

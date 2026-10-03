# app/KLASOR.md

Uygulamanın tek modülü `:app`: kod dışı tek dosyası `build.gradle` (paket adı, Android sürümleri, uygulama sürümü, yayın
imzası, lint ve Java ayarı); kod ve kaynaklar `src/main`'de, yalnız deneme paketine giren kaynak `src/debug`'da.

## Bu dosya ne yapar?

Gradle projesinde her modül kendi klasöründe kendi `build.gradle`'ıyla durur. Bu depoda tek modül var: `app`. Kök
`settings.gradle` onu `include ':app'` ile projeye katar ([../KLASOR.md](../KLASOR.md)). Bu belge `app/build.gradle`'ı satır
satır anlatır ve modülün içindeki klasörlere yol gösterir.

Modülün dizilişi:

| Yol | Ne | Belgesi |
|---|---|---|
| `build.gradle` | Modülün derleme betiği (aşağıda) | bu belge |
| `src/main/AndroidManifest.xml` | İzinler, ekranlar, servisler, alıcı | [src/main/KLASOR.md](src/main/KLASOR.md) |
| `src/main/java/org/egitimevi/aile/*.java` | Uygulamanın bütün kodu (32 sınıf), her birinin yanında `.md` | [../TANITIM.md](../TANITIM.md) belge haritası |
| `src/main/res/drawable/` | Simgeler | [src/main/res/drawable/KLASOR.md](src/main/res/drawable/KLASOR.md) |
| `src/main/res/values/` | Açık tema renkleri, tema, uygulama adı | [src/main/res/values/KLASOR.md](src/main/res/values/KLASOR.md) |
| `src/main/res/values-night/` | Koyu tema renkleri ve teması | [src/main/res/values-night/KLASOR.md](src/main/res/values-night/KLASOR.md) |
| `src/main/res/xml/` | Ağ güvenliği, yedek ve veri aktarım kuralları | [src/main/res/xml/KLASOR.md](src/main/res/xml/KLASOR.md) |
| `src/main/res/mipmap-anydpi/` | Başlatıcı simgesi | [src/main/res/mipmap-anydpi/KLASOR.md](src/main/res/mipmap-anydpi/KLASOR.md) |
| `src/debug/res/xml/` | Deneme paketinin ağ ayarı (http'ye izin) | [src/debug/res/xml/KLASOR.md](src/debug/res/xml/KLASOR.md) |
| `build/` | Derleme çıktıları (APK, lint raporu, ara dosyalar) | depoya girmez |

## İçinde neler var?

### `build.gradle`

```
apply plugin: 'com.android.application'
```

Bu modül bir uygulamadır (kütüphane değil). Eklentinin sürümü kök `build.gradle`'da (AGP 8.13.0).

**Yayın imzası.** Dosyanın başı deponun dışındaki yerel imza dosyasını arar (yerel imza dosyaları, depoya girmez). Dosya
varsa içindeki dört değer (anahtar deposu dosyası, onun şifresi, anahtarın takma adı ve şifresi) `signingConfigs.yayin`'e
okunur ve `release` yapı türü onunla imzalanır. Dosya yoksa betik hata vermez: deneme (`debug`) paketi her zaman derlenir,
yayın paketi imzasız çıkar. Şifreler hiçbir zaman depodaki bir dosyada durmaz.

**`android { … }`**

| Ayar | Değer | Anlamı |
|---|---|---|
| `namespace` | `org.egitimevi.aile` | Java paketinin ve üretilen `R` sınıfının paketi |
| `compileSdk` | 36 | Hangi Android arayüzleriyle derlendiği |
| `defaultConfig.applicationId` | `org.egitimevi.aile` | Uygulamanın telefondaki ve mağazadaki kimliği |
| `defaultConfig.minSdk` | 26 | En düşük Android 8.0 |
| `defaultConfig.targetSdk` | 36 | Android'in davranış kurallarını hangi sürüme göre uygulayacağı |
| `defaultConfig.versionCode` | 3 | Tam sayı sürüm; her yayında büyümeli |
| `defaultConfig.versionName` | `'2.0.0'` | İnsanın okuduğu sürüm |
| `buildTypes.release.minifyEnabled` | `false` | Yayın paketinde kod küçültme/karartma (R8) yok |
| `lint.disable` | `'SetTextI18n'` | "Metni kaynak dosyasına taşı" uyarısı kapalı |
| `compileOptions` | Java 17 kaynak ve hedef | Java 17 diliyle yazılıp derlenir |

`dependencies` bloğu **yok**: uygulama hiçbir dış kütüphane kullanmaz (AndroidX dahil), yalnız Android'in kendi
arayüzleri ve Java'nın standart kütüphanesi.

Neden böyle:

- **Paket adı `org.egitimevi.aile`.** Ad, uygulamanın ilk hâli "Eğitim Evi Aile"den (çocuğun telefonu) kaldı. Aynı adla ve
  aynı imzayla derlenen 2.0.0 (`versionCode 3`), telefonda kurulu 1.0.x Aile uygulamasının üstüne güncelleme olarak
  kurulur; çocuğun telefonunun bağlantısı ("aile" ayar dosyası) yerinde kalır ve
  [BaslatmaAlici](src/main/java/org/egitimevi/aile/BaslatmaAlici.md) güncellemeden sonra izleme servisini yeniden başlatır.
- **`minSdk 26`.** Uyarlanabilir başlatıcı simgesi (`mipmap-anydpi`) ve `java.time` (`Zaman.java`) Android 8.0'dan beri
  var; ek kitaplık ya da PNG yedek simge gerekmez. Kodda daha yeni özellikler için sürüm denetimi var (29, 30, 33, 35).
- **`SetTextI18n` kapalı.** Uygulama yalnız Türkçe; ekran metinleri Java kodunda yazılı, `res/values/strings.xml`'de yalnız
  uygulamanın adı var. Lint bu yüzden her `setText("…")` için uyarmasın diye kapatıldı (`commit 4`).
- **`versionName` uygulamada görünür.** `AyarlarSayfasi` en altta "Eğitim Evi 2.0.0" yazar; `AnaEkran` cihaz anahtarı
  alırken sunucuya `surum` olarak gönderir.

### Yapı türleri (variant)

- **`debug` (deneme paketi):** Android'in kendi deneme anahtarıyla kendiliğinden imzalanır; `src/main` ile `src/debug`
  birleşir, `src/debug/res/xml/ag_guvenligi.xml` ana kaynaktaki aynı adlı dosyanın yerine geçer (http'ye izin). Çıktı
  `build/outputs/apk/debug/app-debug.apk`.
- **`release` (yayın paketi):** yalnız `src/main`; yerel imza dosyası varsa `yayin` ile imzalanır. `assembleRelease` APK,
  `bundleRelease` Play Store için AAB çıkarır (`build/outputs/apk/release/`, `build/outputs/bundle/release/`).

## Kimle konuşur?

- **Gradle ve AGP:** kök [../KLASOR.md](../KLASOR.md)'deki `settings.gradle` bu modülü katar, kök `build.gradle` eklentiyi
  verir. Eklenti `src/main` ve `src/debug`'ı birleştirir, `res/`'ten `R` sınıfını üretir, manifesti işler.
- **Kod:** Java sınıfları `R.drawable.*`, `R.color.*` ile kaynaklara ulaşır; `versionName`'i
  `getPackageManager().getPackageInfo(...)` ile okurlar ([AnaEkran](src/main/java/org/egitimevi/aile/AnaEkran.md),
  [AyarlarSayfasi](src/main/java/org/egitimevi/aile/AyarlarSayfasi.md)).
- **Site:** indirme sayfasının sürüm tablosu bu deponun GitHub Releases'inden okunur (site deposunda
  `sunucu/uygulama-surum.js`). Site etiketi `v2.0.0` ya da `2.0.0` biçiminde bekler (iki ile dört sayı); etiketi
  `versionName` ile aynı vermek karışıklığı önler.
- **Yerel imza dosyaları:** yalnız `release` derlenirken okunur (yerel imza dosyaları, depoya girmez).

## Nasıl çalışır (adım adım)?

```
./gradlew assembleDebug
  app/build.gradle okunur ─► yerel imza dosyası var mı? (debug için önemsiz)
  src/main + src/debug birleşir ─► res derlenir, R üretilir ─► Java 17 derlenir ─► dex
  debug anahtarıyla imzalanır ─► build/outputs/apk/debug/app-debug.apk

./gradlew assembleRelease
  yerel imza dosyası var ─► signingConfigs.yayin doldurulur ─► release onunla imzalanır
                  yok  ─► imzasız APK (telefona kurulamaz)
```

## Dikkat!

- **`versionCode` her yayında artmalı.** Android, telefondakinden küçük ya da eşit `versionCode`'lu paketi güncelleme
  olarak kurmaz; Play Store da aynı sayıyı ikinci kez kabul etmez.
- **Paket adı ve imza değişmemeli.** `applicationId` değişirse telefon onu ayrı bir uygulama sayar (1.0.x Aile kullanan
  telefonlar güncelleme almaz). İmza anahtarı değişir ya da kaybolursa kurulu uygulama güncellenemez; anahtarın yedeği
  deponun dışında tutulmalı.
- **`minifyEnabled false`:** yayın paketi küçültülmez; dış kütüphane olmadığı için boyut zaten küçük. Açılırsa yayın paketi
  bütün ekranlarıyla baştan denenmeli.
- **`targetSdk 36`'nın getirdikleri:** Android 15 ve üstünde pencere kenardan kenaradır (çubuk renkleri yok sayılır),
  `AnaEkran` boşlukları kendisi verir; Android 14 ve üstünde konum servisi başlatmak için `FOREGROUND_SERVICE_LOCATION`
  izni ve `foregroundServiceType="location"` gerekir (manifestte var). `targetSdk`'yı yükseltirken Android'in davranış
  değişikliği listesine bak.
- **Çok dil gelirse** `SetTextI18n` kararı yeniden ele alınmalı (planlı işte metinler bir katalogdan gelecek).
- **Şifreler bu dosyaya yazılmaz.** İmza değerleri yalnız yerel imza dosyasından okunur; `build.gradle`'a sabit şifre
  koyma (depo herkese açık).

## Testleri

- Modülde otomatik test yok (`src/test`, `src/androidTest` yok). Denetim: `./gradlew --offline lintDebug` (hedef 0 hata,
  0 uyarı); lint ayarı bu dosyadaki `lint { }` bloğu.
- Elle: deneme paketini kur, Ayarlar sayfasının en altında sürümün "Eğitim Evi 2.0.0" yazdığını gör.
- Bu belge yazılırken derleme çalıştırılmadı.

## Son durum

- `app/build.gradle`'ın geçmişi: `34b45f1 commit 1` (2026-09-26, ilk hâli: `versionCode 1`, `'1.0'`), `51da3d6 commit 3`
  (yayın imzası bloğu), `3875db7 commit 4`, `f70aeca commit 5`.
- Son üç değişiklik, farklarına göre:
  - `f70aeca commit 5` (2026-09-26): `versionCode 2 → 3`, `versionName '1.0.2' → '2.0.0'` (tek uygulamaya geçiş).
  - `3875db7 commit 4`: `versionCode 1 → 2`, `'1.0' → '1.0.2'`; `lint { disable 'SetTextI18n' }` eklendi.
  - `51da3d6 commit 3`: deponun dışındaki yerel imza dosyasını okuyan blok, `signingConfigs.yayin` ve `release`'in onunla
    imzalanması eklendi.
- Açık iş: yok. 2.0.0 henüz yayımlanmadı (sitenin kılavuzuna göre yayımda olan 1.0.x Aile).
- Planlı işlerden bu dosyayı etkileyecekler: "Android yerel uygulama (bütün roller) + doğrulayıcı + apk/aab + sürüm" —
  sürüm adımında `versionCode`/`versionName` artar; "uygulama kendini güncellesin" eki GitHub'dan dağıtılan paketle Play
  Store paketini ayıran bir yapı bayrağı ister; T.C. kimlik no kuralı ve doğrulayıcı için birim testleri planlandı (gelirse
  bu modüle bir `src/test` klasörü eklenir).

# KLASOR.md

Android deposunun kök klasörü: Gradle'ın proje tanımı (`build.gradle`, `settings.gradle`, `gradle.properties`), Gradle
sarmalayıcısının başlatıcıları (`gradlew`, `gradlew.bat`), GitHub ön sayfası `README.md` ve depoya girmeyecekleri sayan
`.gitignore`.

## Bu dosya ne yapar?

Bu belge deponun kökündeki dosyaları tek tek anlatır ve alt klasörlerin belgelerine yol gösterir. Uygulamanın baştan sona
turu (ne olduğu, ekranlar, sunucuyla konuşma, derleme, sürüm) [TANITIM.md](TANITIM.md)'de; burada yalnız kökte duranlar var.

Kökte kod yok. Burada duranlar Gradle'a "bu proje ne, hangi modülü var, hangi eklentiyle derlenir" diye söyleyen birkaç
küçük dosya, Gradle'ı bilgisayarına kurmadan çalıştırmanı sağlayan iki betik, bir README ve bir `.gitignore`. Kod ve kaynaklar
`app/` modülünde.

Klasörün dizilişi:

| Yol | Ne | Belgesi |
|---|---|---|
| `app/` | Tek modül (`:app`): `build.gradle`, kod (`src/main/java`), kaynaklar (`src/main/res`), deneme paketine özel kaynak (`src/debug`) | [app/KLASOR.md](app/KLASOR.md) |
| `app/src/main/` | `AndroidManifest.xml`: izinler ve bileşenler | [app/src/main/KLASOR.md](app/src/main/KLASOR.md) |
| `app/src/main/java/org/egitimevi/aile/` | 32 Java sınıfı, her birinin yanında aynı adlı `.md` | [TANITIM.md](TANITIM.md) belge haritası |
| `app/src/main/res/drawable/` | Simgeler (vektör çizimler) | [app/src/main/res/drawable/KLASOR.md](app/src/main/res/drawable/KLASOR.md) |
| `app/src/main/res/values/` | Açık temanın renkleri, tema, uygulama adı | [app/src/main/res/values/KLASOR.md](app/src/main/res/values/KLASOR.md) |
| `app/src/main/res/values-night/` | Koyu temanın renkleri ve teması | [app/src/main/res/values-night/KLASOR.md](app/src/main/res/values-night/KLASOR.md) |
| `app/src/main/res/xml/` | Ağ güvenliği ve yedek kuralları | [app/src/main/res/xml/KLASOR.md](app/src/main/res/xml/KLASOR.md) |
| `app/src/main/res/mipmap-anydpi/` | Başlatıcı simgesi | [app/src/main/res/mipmap-anydpi/KLASOR.md](app/src/main/res/mipmap-anydpi/KLASOR.md) |
| `app/src/debug/res/xml/` | Yalnız deneme paketinin ağ ayarı | [app/src/debug/res/xml/KLASOR.md](app/src/debug/res/xml/KLASOR.md) |
| `gradle/wrapper/` | Gradle sarmalayıcısının jar'ı ve ayarı | [gradle/wrapper/KLASOR.md](gradle/wrapper/KLASOR.md) |
| `araclar/` | Tek araç: sitenin simgelerini Android çizimine çeviren `simgeleri-uret.js` | [araclar/simgeleri-uret.md](araclar/simgeleri-uret.md) |

Depoda izlenmeyen ama bilgisayarda oluşan şeyler: derleme çıktıları (`.gradle/`, `build/`, `app/build/`), Android Studio
dosyaları, bilgisayara özel SDK yolu dosyası ve yerel imza dosyaları. Bunlar yereldir, depoya girmez (`.gitignore`).

## İçinde neler var?

### `build.gradle` (kök)

Bütün projenin derleme betiği. Tek işi Gradle'a Android eklentisini (Android Gradle Plugin, kısaca AGP) bulmayı söylemek:

- `buildscript.repositories`: `google()` ve `mavenCentral()` — eklenti ve onun araçları (derleyici, lint, kaynak
  paketleyici) buradan iner.
- `buildscript.dependencies`: `classpath 'com.android.tools.build:gradle:8.13.0'` — AGP 8.13.0.

Başındaki yorum "Eğitim Evi Aile: çocuğun telefonunda çalışan küçük uygulama. Dış kütüphane yok" der. İkinci cümle bugün de
doğru (uygulamanın hiçbir dış bağımlılığı yok, AndroidX bile); birinci cümle eskidi: uygulama `commit 5`'ten beri bütün
rollerin tek uygulaması "Eğitim Evi"dir, çocuğun telefonu onun bir ekranıdır.

### `settings.gradle`

- `dependencyResolutionManagement`: modüllerin bağımlılıklarını nereden arayacağı (`google()`, `mavenCentral()`);
  `repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)` — bir modül kendi `repositories` bloğunu yazarsa derleme
  hata verir, depo listesi tek yerde kalır.
- `rootProject.name = "Egitim-Evi-App"` ve `include ':app'` — projenin tek modülü `app/`.

`app/build.gradle`'da `dependencies` bloğu olmadığı için bu depo listesi bugün yalnız Android eklentisinin kendi
ihtiyaçları için kullanılır.

### `gradle.properties`

- `org.gradle.jvmargs=-Xmx1536m -Dfile.encoding=UTF-8` — Gradle'ın Java süreci en çok 1,5 GB bellek kullanır; kaynak
  dosyalar UTF-8 okunur (Java kodundaki Türkçe metinler bozulmasın).
- `android.nonTransitiveRClass=true` — her modülün `R` sınıfı yalnız kendi kaynaklarını taşır. AGP 8'den beri zaten
  varsayılan bu; tek modül ve dış kütüphane olmadığı için bugün pratikte bir şey değiştirmez.

### `gradlew`

Gradle'ın kendi ürettiği POSIX kabuk betiği (Apache 2.0 lisans başlığıyla, elle yazılmadı). Java'yı bulur (`JAVA_HOME` ya da
`PATH`'teki `java`), `gradle/wrapper/gradle-wrapper.jar`'ı `-Xmx64m -Xms64m` ile çalıştırır ve verdiğin görevleri (ör.
`assembleDebug`) ona geçirir. Jar, `gradle/wrapper/gradle-wrapper.properties`'te yazan Gradle sürümünü (8.14.3) gerekiyorsa
indirir ve onu çalıştırır. Böylece Gradle'ı bilgisayarına ayrıca kurman gerekmez. Dosyanın çalıştırılabilir izni (`100755`)
depoda kayıtlı (`commit 2`); Linux'ta ve macOS'te `./gradlew` doğrudan çalışsın diye. Windows'ta Git Bash'ten de `./gradlew`
yazılır.

### `gradlew.bat`

Aynı işin Windows komut istemi (`cmd`) karşılığı; Gradle üretir. `JAVA_HOME`'u ya da `PATH`'teki `java.exe`'yi bulur ve
aynı jar'ı çalıştırır. PowerShell'den `.\gradlew.bat assembleDebug` diye kullanılır. Depoda LF satır sonlarıyla duruyor; bu
bilgisayarda git'in `core.autocrlf=true` ayarı çalışma kopyasında onu CRLF'ye çevirir (depoda `.gitattributes` yok).

### `README.md`

GitHub'daki depo sayfasının ön yüzü. 3 Ekim 2026'da bugünkü yerel (native) uygulamaya göre yeniden yazıldı: kısaca ne olduğu
ve kimin kullandığı, ekranlar tablosu, telefona özgü işler (bildirim yoklaması, sefer servisi, çocuğun telefonu), oturum ve
anahtarlar, kullanılan sunucu uçları (site belgelerine adla), her Java dosyasının belgesine bağlantılı dosya tablosu, izinler,
derleme (JDK 21) ve en sonda "Telif ve kullanım" (tüm hakları saklıdır). Ayrıntılar için [TANITIM.md](TANITIM.md).

## Dikkat!

- **`--offline` ilk derlemede çalışmaz.** Gradle dağıtımı, Android eklentisi ve araçları bir kez internetle inmiş olmalı;
  sonra `--offline` hiç ağa çıkmadan derler.
- **Java sürümü:** Gradle'ı JDK 21 ile çalıştır (`JAVA_HOME`). Bilgisayardaki varsayılan Java daha yeniyse Gradle açılmaz.
  Kodun hedefi ayrıca Java 17'dir (`app/build.gradle` → `compileOptions`).
- **AGP ile Gradle birlikte yükselir.** Kök `build.gradle`'daki eklenti sürümünü artırırsan Android'in uyumluluk tablosuna
  bak; gerekiyorsa `gradle/wrapper/gradle-wrapper.properties`'teki Gradle sürümünü de artır.
- **Paketler depoya girmez.** APK ve AAB GitHub Releases'e yüklenir; `.gitignore` onları dışarıda tutar. Site indirme
  sayfasını oradan doldurur.
- **İmza deponun dışında.** Yayın imzası yerel imza dosyalarından okunur (yerel imza dosyaları, depoya girmez). Dosya yoksa
  `assembleRelease` imzasız bir paket çıkarır; imzasız paket telefona kurulamaz. Anahtar kaybolursa telefondaki uygulama bir
  daha güncellenemez: anahtarın yedeği deponun dışında saklanmalı.
- **`gradlew`'nun çalıştırma izni** Windows'ta düzenlenip geri yazılırken kaybolabilir; `git --no-pager diff --summary`'de
  `mode change 100755 => 100644` görürsen geri al.
- **Satır sonları tek bir ayara bağlı.** `.gitattributes` olmadığı için `gradlew.bat`'ın CRLF'si yalnız bilgisayardaki
  `core.autocrlf` ayarından gelir; o ayar kapalı bir kopyada `cmd` LF satır sonlu toplu iş dosyasını çoğu zaman yine çalıştırır
  ama etiketli (`goto`) satırlarda nadiren şaşırabilir. Gradle'ın kendi `gradle init` şablonu bunun için `.gitattributes`'a
  `*.bat text eol=crlf` ve `/gradlew text eol=lf` yazar (öneri; bugün yok).

## Testleri

- Depoda otomatik test yok (`app/src/test` ya da `app/src/androidTest` klasörü yok). Kodun denetimi Android lint'tir:
  `./gradlew --offline lintDebug` (hedef 0 hata, 0 uyarı).
- Elle: öykünücüde ya da telefonda deneme paketini kur, [TANITIM.md](TANITIM.md)'deki "Derleme ve deneme" adımlarını izle.
- Uygulamanın dayandığı sunucu sözleşmesini site deposundaki testler korur: `testler/test-giris-kayit.js` (giriş, kayıt),
  `testler/test-servis-yoklama.js` (cihaz anahtarı, bildirim yoklama, servis konumu, 30 günlük uygulama oturumu),
  `testler/test-aile.js` (çocuğun telefonu uçları), `testler/test-kisi-kodu.js`, `testler/test-bildirim.js`,
  `testler/test-uygulama-surum.js` (indirme sayfasının GitHub sürüm listesi).
- Bu belgeyi yazarken hiçbir derleme ya da test çalıştırılmadı; yazılanlar dosyaların ve git geçmişinin okunmasına dayanır.

## Son durum

- Kök dosyaların geçmişi (Android deposu): `34b45f1 commit 1` (2026-09-26, hepsinin ilk hâli), `9e9d5af commit 2`
  (`gradlew` çalıştırılabilir yapıldı), `51da3d6 commit 3` (README yeniden yazıldı: başlık "Eğitim Evi telefon uygulaması",
  yayın APK'sı ve Play paketi komutları, yayın imzasının deponun dışından okunması), `3875db7 commit 4` (README),
  `f70aeca commit 5` (README), `822f10d commit 9` (2026-09-27, `.gitignore`). `build.gradle`, `settings.gradle`,
  `gradle.properties` ve `gradlew.bat` ilk günden beri değişmedi.
- Son üç değişiklik, farklarına göre:
  - `822f10d commit 9`: `.gitignore` 11 satırlık düz listeden beş gruplu, yorumlu 38 satıra çıktı. Yeni eklenenler:
    `.externalNativeBuild/`, `.cxx/`, `.vscode/`, imza klasörü ve imza ayar dosyası, sertifika ve özel anahtar dosyası
    türleri, `.env` dosyaları, günlük ve geçici dosyalar. `captures/`, bilgisayara özel SDK yolu dosyası, paketler (`*.apk`,
    `*.aab`) ve iki anahtar deposu türü ilk günden (`commit 1`) beri listedeydi; yalnız kendi gruplarına taşındı.
  - `f70aeca commit 5`: README çocuğun telefonu uygulamasını anlatmaktan çıkıp tek uygulama "Eğitim Evi"yi (o günkü hâliyle
    siteyi içinde açan WebView) anlatır oldu; bildirimler, servis seferi, çocuğun telefonu maddeleri, yeni dosya tablosu,
    izinler ve deneme paketinin sunucu adresi sorması yazıldı.
  - `3875db7 commit 4`: README'de `Api.java` satırı "yalnızca https (http yalnızca deneme paketinde, yerel ağdaki sunucuya)"
    oldu; izinlerde pil için "Kısıtlamasız" seçimi ve verinin buluta yedeklenmemesi yazıldı.
- Bilinen açıklar: kök `build.gradle` yorumunun eskimesi (yukarıda). README 3 Ekim 2026'da yeniden yazıldı. Kod değiştirilmedi.
- Planlı işlerden bu klasörü etkileyecekler: "Android yerel uygulama (bütün roller) + doğrulayıcı + apk/aab + sürüm" işinin
  sürüm adımı README'yi yeniden yazar ve `assembleRelease` + `bundleRelease` ile paket çıkarır; aynı işin "uygulama kendini
  güncellesin" eki GitHub ve Play Store paketleri için ayrı bir yapı bayrağı getirecek.

# app/src/main/java/org/egitimevi/aile/Tema.java

Uygulamanın görünüş ölçüleri tek yerde: sitenin köşe yarıçapları, yazı tipleri, dp çevirisi, renk okuma, yuvarlak köşeli ve
dokununca dalgalanan zeminler, avatarın rengi ve baş harfleri.

## Bu dosya ne yapar?

Uygulama ekranlarını kendisi çizer ve sitenin görünüşüne benzemesi istenir: aynı kırmızı ana renk, aynı yumuşak köşeler,
başlıkta serif yazı, kişilerin baş harfli renkli avatarları. Bu dosya o ortak ölçüleri taşır; parçaları kuran
[Arayuz.md](Arayuz.md) ve ekranlar ölçüyü buradan alır. Böylece "kart köşesi 22 dp" gibi bir karar tek yerde durur.

Renklerin kendisi burada değil, kaynak dosyalarındadır: `res/values/renkler.xml` (açık tema) ve `res/values-night/renkler.xml`
(koyu tema); adları sitenin `public/css/parcalar/00-temel.css` değişkenleriyle aynıdır (`ana`, `zemin`, `kart`, `yazi`,
`soluk`, `cizgi` …). Telefon koyu temadaysa Android `values-night`'takileri verir; bu dosyadaki `renk` yalnız o an geçerli
olanı okur. Uygulama teması `res/values/temalar.xml` ve `res/values-night/temalar.xml`'deki `Tema` stilidir (aynı ad, ayrı
şey: o, manifestteki pencere teması).

Sınıf bir araç sınıfıdır: `final`, kurucusu gizli, her şey `static`.

## İçinde neler var?

### Köşe yarıçapları (dp)

| Sabit | Değer | Sitedeki değişken | Kullanan (grep) |
|---|---|---|---|
| `R_BUYUK` | 28 | `--r-buyuk` (ana sayfa kutucukları, açılır pencere, giriş kartı) | [PortalSecici.md](PortalSecici.md) penceresi |
| `R_ORTA` | 22 | `--r` (kartlar) | [Arayuz.md](Arayuz.md) `kart` |
| `R_KUCUK` | 14 | `--r-kucuk` (düğmeler, form alanları, menü satırları) | `Arayuz` düğme, form alanı, dokunulan satırın dalgası, şerit; [KisiKodu.md](KisiKodu.md) kod kutusu; [AnaEkran.md](AnaEkran.md) kısa mesaj kutusu |
| `R_MINI` | 9 | `--r-mini` (küçük rozetler, sekmeler) | kullanılmıyor |

Hap biçimli parçalar (rozet, etiket, seçili sekme) yarıçapı `99` vererek yapılıyor (sabit değil, çağıranda yazılı).

### Yazı tipleri (`Typeface`)

| Sabit | Değer | Nerede |
|---|---|---|
| `GOVDE` | `sans-serif`, normal | bütün düz yazılar (`Arayuz.yazi`) |
| `ORTA` | `sans-serif-medium` | form etiketleri, satır başlıkları, seçili olmayan sekme adı, "Şifreyi göster" bağlantısı ([GirisSayfasi.md](GirisSayfasi.md)), doğrulama sorusunun metni ([DogrulamaSorusu.md](DogrulamaSorusu.md)) |
| `KALIN` | `sans-serif`, kalın | düğmeler, alt başlıklar, bölüm etiketleri, renkli etiketler, avatar harfleri, zil rozeti, seçili sekme adı, okunmamış bildirim metni ([BildirimlerSayfasi.md](BildirimlerSayfasi.md)) |
| `BASLIK` | `SERIF`, kalın | büyük sayfa başlıkları ve üst çubuktaki başlık |
| `KOD` | `MONOSPACE` | kişi kodu ve iki adımlı kod kutuları ([KisiKodu.md](KisiKodu.md), [KodSayfasi.md](KodSayfasi.md), [EkleSayfasi.md](EkleSayfasi.md)) |

Yorum sitedekilerin karşılığını söyler: gövde sans (sitede IBM Plex Sans), başlık serif (sitede Newsreader). Uygulama bu yazı
tiplerini taşımaz; telefonun kendi sans-serif ve serif yazı tipleri kullanılır.

### İşlevler

- `renk(c, id)` — `c.getColor(id)`: kaynak rengini o anki temaya (açık/koyu) göre döner.
- `dp(c, d)` — `d` dp'yi piksele çevirir (`d × ekran yoğunluğu`, en yakın tam sayıya yuvarlanmış).
- `zemin(c, renk, yaricapDp, cizgiRenk, cizgiDp)` — düz renkli, yuvarlak köşeli bir `GradientDrawable`; `cizgiDp > 0` ise
  kenarında o kalınlıkta çizgi (çizgi rengi yalnız o zaman kullanılır). Kartlar, düğme zeminleri, kutular, etiketler,
  pencereler hep bununla.
- `dalgali(c, icerik, yaricapDp)` — `icerik`'i dokununca dalgalanan bir `RippleDrawable` içine sarar; dalga rengi
  `R.color.ana_cizgi`, dalga aynı yarıçaplı beyaz bir maskeyle sınırlanır (köşelerden taşmaz). Düğmeler ve dokunulan
  satırlar ([Arayuz.md](Arayuz.md) `dugme`, `tiklananSatir`).
- `avatarRengi(anahtar)` — kişiye özgü avatar rengi: metnin her karakteri için `h = (h × 31 + karakter) & 0x7fffffff`, sonra
  sekiz renkten `h % 8`'inci. Renkler: `#D62839`, `#0FA3B1`, `#7C3AED`, `#C2410C`, `#15803D`, `#1D4ED8`, `#B45309`,
  `#0D9488` (temadan bağımsız, sabit). `null` → boş metin gibi (ilk renk).
- `basHarfler(ad)` — "Zeynep Ak Şahin" → "ZŞ": ilk ve son kelimenin ilk harfi, Türkçe büyük harfle (`tr-TR`: "i" → "İ");
  tek kelimede tek harf; `null` ya da boş adda "?".

## Kimle konuşur?

- Android: `Context.getColor`, `DisplayMetrics.density`, `Typeface.create`, `GradientDrawable`, `RippleDrawable`,
  `ColorStateList`; `java.util.Locale`. Kaynak: `R.color.ana_cizgi`.
- Onu kullananlar (grep; hemen her ekran):
  - [Arayuz.md](Arayuz.md) — neredeyse bütün işlevler ve sabitler (yazı, kart, düğme, alan, satır, avatar, etiket, şerit,
    yükleniyor, boş durum).
  - [AnaEkran.md](AnaEkran.md) — üst/alt çubuk, rozet, kısa mesaj kutusu, sayfa geçişindeki kayma (`dp`, `zemin`, `renk`,
    `BASLIK`, `KALIN`, `ORTA`, `R_KUCUK`).
  - Sayfalar: [AnaSayfa.md](AnaSayfa.md), [AyarlarSayfasi.md](AyarlarSayfasi.md), [BildirimlerSayfasi.md](BildirimlerSayfasi.md),
    [EkleSayfasi.md](EkleSayfasi.md), [GirisSayfasi.md](GirisSayfasi.md), [KayitSayfasi.md](KayitSayfasi.md),
    [KodSayfasi.md](KodSayfasi.md), [KvkkSayfasi.md](KvkkSayfasi.md), [SifreSayfasi.md](SifreSayfasi.md),
    [SifremiUnuttumSayfasi.md](SifremiUnuttumSayfasi.md); parçalar [DogrulamaSorusu.md](DogrulamaSorusu.md),
    [KisiKodu.md](KisiKodu.md), [PortalSecici.md](PortalSecici.md).
  - Kullanmayanlar: çocuğun telefonu ekranı [AileEkrani.md](AileEkrani.md) (renklerini kendi içinde sabit kodla, açık
    temaya göre veriyor) ve arka plan servisleri.
- Sitedeki karşılıkları (site deposu): yarıçaplar ve yazı tipleri `public/css/parcalar/00-temel.css`; avatar ve baş harfler
  `public/js/parcalar/02-ikonlar.js` (`avatar`, `basHarfler`, `AVATAR_RENKLERI`).
- Sunucuyla konuşmaz.

## Nasıl çalışır (adım adım)?

```
Arayuz.kart(c)
   ├─ renk(c, R.color.kart)         ─► açık temada #FFFFFF, koyu temada #1D2127 (values / values-night)
   ├─ renk(c, R.color.cizgi)
   └─ zemin(c, kart, R_ORTA=22, cizgi, 1) ─► GradientDrawable: köşe dp(22) px, 1 dp çizgi
Arayuz.dugme(c, "Kaydet", BIRINCIL)
   └─ dalgali(c, zemin(c, ana, R_KUCUK, 0, 0), R_KUCUK) ─► dokununca ana_cizgi renginde dalga
Arayuz.avatar(c, "Elif Kaya", "u_123", 36)
   ├─ basHarfler("Elif Kaya")  ─► "EK"
   └─ avatarRengi("u_123")     ─► h = Σ(h·31 + kar) & 0x7fffffff ; renkler[h % 8]
```

## Dikkat!

- **Avatar renkleri siteyle aynı değil.** Yorum "aynı kişi her yerde aynı renkte (site: avatar())" diyor. Karıştırma aynı
  sonucu veriyor (sitede `>>> 0` ile 32 bit, burada `& 0x7fffffff` ile 31 bit; 8'e bölümden kalan ikisinde de aynı çıkar),
  yani aynı kişi aynı SIRADAKİ rengi alır. Ama listeler farklı: sitede `['#d62839', '#0a8f9c', '#d9820b', '#0a6f79',
  '#7b4ecf', '#2f855a', '#c2410c', '#3563c9']`, burada `#D62839, #0FA3B1, #7C3AED, #C2410C, #15803D, #1D4ED8, #B45309,
  #0D9488`. Yalnız ilk renk ortak; bir kişi sitede turuncu, uygulamada mor görünebilir. Uygulama içinde tutarlı. Aynı olsun
  isteniyorsa buradaki listeyi sitedekiyle değiştirmek yeter (öneri; kod değiştirilmedi).
- **Boş anahtar ilk rengi verir.** Sitede `avatar(ad, anahtar)` anahtar boşsa adı kullanır; [Arayuz.md](Arayuz.md)'deki
  `avatar` yalnız `null`'da adı kullanır, boş metni (`optString` boş dönerse) olduğu gibi verir ve renk hep `#D62839` olur.
  Bugünkü çağrılarda kimlik hep dolu.
- **Renkler parça kurulurken bir kez okunur.** `renk` o anki temanın rengini döner; telefon uygulama açıkken koyu temaya
  geçerse var olan parçalar eski renklerinde kalır ([AnaEkran.md](AnaEkran.md) `configChanges` `uiMode`'u kendisi karşılıyor
  ama `onConfigurationChanged` yok; [Arayuz.md](Arayuz.md)). Sabit renk kodu yazma: koyu tema bozulur (uygulama tanımının
  kuralı); renk için hep `R.color.*` ve `renk`.
- **Yazı tipleri siteyle birebir değil.** Telefonun sistem yazı tipleri kullanılıyor (çoğu telefonda Roboto ve Noto Serif);
  site IBM Plex Sans ve Newsreader taşıyor. Uygulamaya yazı tipi dosyası eklemek boyutu büyütür; bugünkü seçim bilinçli
  görünüyor ama kodda gerekçesi yazmıyor.
- **`dp` her çağrıda yoğunluğu okur ve yuvarlar.** Çok küçük değerlerde (ör. 1 dp) düşük yoğunluklu ekranda 1 piksel çıkar;
  [Arayuz.md](Arayuz.md)'deki ayırıcı en az 1 piksel olsun diye ayrıca `Math.max(1, …)` kullanıyor.
- `basHarfler` her kelimenin ilk UTF-16 birimini alır; ad emoji ya da birleşik karakterle başlıyorsa yarım bir karakter
  görünebilir (sitede de aynı).
- `R_MINI` tanımlı ama hiçbir yerde kullanılmıyor.

## Testleri

- Android deposunda otomatik test yok; derleme ve lint (`./gradlew --offline assembleDebug lintDebug`) ana oturumda. Lint,
  `renkler.xml`'deki kullanılmayan renkleri bilerek susturuyor (`tools:ignore="UnusedResources"`).
- Elle (öykünücü, deneme paketi): uygulamayı açık ve koyu temada aç (`adb shell cmd uimode night yes` / `no`, sonra
  uygulamayı kapatıp aç) → kartlar, düğmeler, yazılar iki temada da okunur olmalı. Aynı kişinin avatarını sitede ve
  uygulamada karşılaştır → harfler aynı, renk (ilk renk değilse) farklı (bugünkü davranış). Bir düğmeye bas → köşeleri
  taşmayan dalga.

## Son durum

- `git log` (Android deposu): 1 commit. Dosya `e96c5f2 commit 6` (2026-09-26) ile geldi: yerel uygulamanın çekirdeği yazılırken
  sitenin görünüşünü uygulamaya taşımak için eklendi; aynı commit'te renk kaynakları (`res/values/renkler.xml`,
  `res/values-night/renkler.xml`) ve uygulama teması (`temalar.xml`) da geldi. O günden beri değişmedi.
- Bilinen açıklar (kod değiştirilmedi): avatar renk listesinin siteden farklı olması, canlı tema değişiminin yansımaması,
  kullanılmayan `R_MINI`.
- Planlı işlerden bu dosyaya dokunması beklenenler:
  - "Android yerel uygulama" (iş 10) inceleme aşaması: bütün ekranlar açık ve koyu temada, telefon ve tablette denenecek.
  - "Arayüz önizlemesi" (iş 31): kullanıcı sitenin tasarım dilini seçince "tasarım dili" tanımı yazılacak; seçilen tasarım
    renk, köşe ve yazı kararlarını değiştirirse uygulamanın ölçüleri de buradan güncellenecek.
  - "Çok dil" (iş 22): sağdan sola diller (Arapça) gelecek; sistem yazı tipleri bu yazıları zaten çizer, yazı tipi dosyası
    eklenirse bu da düşünülmeli (öneri).

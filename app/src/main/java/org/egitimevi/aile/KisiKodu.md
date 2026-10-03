# app/src/main/java/org/egitimevi/aile/KisiKodu.java

Kişi kodu (öğrencide veli kodu) yardımcıları: 16 karakterlik kodu 4'erli tireli gösterir, ayırıcıları atar, yapıştırılan
metinden tam kodu ayıklar, kod yazılan kutuda tireyi kendiliğinden koyar, kodu "Kopyala" düğmeli bir kutuda gösterir.

## Bu dosya ne yapar?

Eğitim Evi'nde kişiler birbirine **kişi kodu**yla bağlanır: yetişkin (veli, öğretmen, müdür) hesabının kodunu okulun
müdürüne veren öğretmen olarak eklenir, sistem yöneticisine veren okulunu açtırıp müdür olur; öğrencinin kodu **veli
kodu**dur ve veli onu girince çocuğuna bağlanır. Kod 16 karakterdir: büyük harf, küçük harf, rakam ve `! ? # * + =`
işaretleri (her birinden en az biri), ilk karakter harf; karışabilen `I L O l o 0 1` ve Türkçe harf yok; büyük/küçük harf
önemlidir. Ekranda, kâğıtta ve Excel'de 4'erli dört grup tireyle ayrılır: `Ab3#-kQx9-+mPt-7?zR`. **Tire kodun karakteri
değildir**, yalnız ayırıcıdır: girişte boşluklar ve tireler silinir; "Kopyala" da tireli biçimi verir.

Bu kurallar üç yerde aynı yazılıdır: sunucuda (`sunucu/ortak.js` — `KISI_KODU_DESENI`, `KISI_KODU_AYIRICI`,
`kisiKoduBicim`), sitenin ön yüzünde (`public/js/parcalar/05-giris.js` — `kisiKoduAyikla`, kod kutusu dinleyicileri) ve
burada. Bu dosya uygulamanın o üçüncü kopyasıdır: kodu göstermek (öğrencinin Ayarlar'ı, öğretmenin/müdürün "Kişi kodun"
sayfası) ve kod yazdırmak (velinin "Çocuğunu ekle" sayfası) bu sınıfı kullanır. Asıl denetim sunucudadır; buradaki
`GECERLI` yalnız yapıştırmada "bu tam bir kod mu" sorusuna cevap verir.

## İçinde neler var?

### Sabitler

- `UZUNLUK` — 16 (tiresiz kod karakteri sayısı).
- `ORNEK` — `"Ab3#-kQx9-+mPt-7?zR"`: kutulardaki ipucu metni.
- `KARAKTER` (iç) — kodun karakter sınıfı: `A-HJKMNP-Za-km-np-z2-9!?#*+=`.
- `GECERLI` (iç) — geçerli kod deseni, sunucudaki `KISI_KODU_DESENI`'nin aynısı: ileriye bakışlarla her sınıftan (büyük,
  küçük, rakam, işaret) en az biri, ilk karakter harf, sonra 15 kod karakteri.
- `ARAMA` (iç) — yapıştırılan metinde kod aramanın sırası (sitedeki `kisiKoduAyikla` ile aynı): (1) ekrandaki tireli
  biçim `XXXX-XXXX-XXXX-XXXX`; (2) başka ayırıcılarla 4'erli gruplar (arada bir ya da daha çok boşluk/tire); (3) 16 bitişik
  kod karakteri. Her desende adayın iki yanında kod karakteri OLMAMALI (daha uzun bir dizinin parçası seçilmesin).

### Metin işlevleri

- `bicim(kod)` — önce `sade`, sonra her 4 karakterde bir tire: `"Ab3#kQx9+mPt7?zR"` → `"Ab3#-kQx9-+mPt-7?zR"`. `null` → `""`.
  Uzunluğa bakmaz (sunucudaki `kisiKoduBicim` yalnız 16 karakterlik kodu biçimler; bu her uzunlukta tire koyar).
- `sade(kod)` — ayırıcıları atar, harf durumuna dokunmaz. `null` → `""`.
- `ayiriciMi(ch)` (iç) — ayırıcı kümesi: boşluklar (Java'nın `isWhitespace`'i, ama U+001C..U+001F denetim karakterleri
  hariç — sitedeki `\s` onları saymaz, küme aynı kalsın; ve `isSpaceChar`: bölünmez boşluk, dar boşluk vb.), tireler
  (`tireMi`) ve görünmez karakterler (`gorunmezMi`).
- `tireMi(ch)` (iç) — `-` ve kopyalanınca tire kılığına giren benzerleri: U+2010..U+2015 kısa/uzun çizgiler, U+2212 eksi,
  U+FE63 küçük tire, U+FF0D tam genişlikli tire.
- `gorunmezMi(ch)` (iç) — U+00AD yumuşak tire, U+200B..U+200D sıfır genişlikli karakterler, U+2060 sözcük birleştirici,
  U+FEFF.
- `ayikla(metin)` — yapıştırılan metindeki tam kodu döner (`"Veli kodu: Ab3#-kQx9-+mPt-7?zR"` → `"Ab3#kQx9+mPt7?zR"`),
  yoksa `null`:
  1. `sade`'lenmiş hâli 16'dan kısaysa `null`; tam 16 ise `GECERLI`'ye uyuyorsa o, uymuyorsa `null`.
  2. Daha uzunsa metin aranabilir hâle getirilir: görünmez karakterler atılır, tire benzerleri `-`, öbür ayırıcılar
     boşluk olur.
  3. `ARAMA`'daki desenler sırayla denenir; her eşleşmede gruplar birleştirilip `GECERLI`'ye bakılır; uymazsa bir karakter
     ileriden aramaya devam edilir.
- `imlecYeri(n)` (iç) — biçimli metinde `n` kod karakterinin ardındaki konum (aradaki tireler sayılarak): `n + (n-1)/4`.

### Kod yazılan kutu

- `kutuyaBagla(kutu)` — bir `EditText`'i kod kutusuna çevirir: ipucunu `ORNEK` yapar, kutunun süzgeçlerini YALNIZ bir
  `Bicimleyici` ile değiştirir (`setFilters`) ve aynı nesneyi metin izleyicisi olarak ekler. Davranış:
  - Yazarken her 4 karakterden sonra tire kendiliğinden gelir (bir sonraki karakter yazılınca; sonda tire kalmaz);
    silerken tire de gider; imleç yazılan ya da silinen yerde kalır.
  - Yalnız bir tire silindiyse (geri tuşu tirenin hemen ardında, Delete hemen önünde) tirenin yanındaki karakter de
    silinir; yoksa tire hemen geri gelir, silme işe yaramazdı.
  - Tireli, tiresiz ya da boşluklu yapıştırılan kod biçime girer; 16 karakterden fazlası alınmaz (yapıştırılanın sığan
    baş kısmı alınır).
  - Yapıştırılan metin (16 ya da daha uzun) içinde tam bir kod varsa kutuda YALNIZ o kalır.
- `Bicimleyici` (iç sınıf; `InputFilter` + `TextWatcher`):
  - `yaziyor` — kutuya kendisi yazarken kendi olaylarını yok saysın diye.
  - `tireSilme` — silinen tek karakter ayırıcıysa: `0` geri tuşu, `1` Delete, `-1` değil.
  - `tamKod` — yapıştırmada bulunan tam kod.
  - `filter(...)` — gelen metin 16 karakter ya da daha uzunsa `ayikla`'ya bakar; tam kod varsa onu döner ve `tamKod`'a
    yazar. Değilse gelenin `sade` hâli kutudaki öbür kod karakterleriyle 16'ya sığıyorsa dokunmaz; sığmıyorsa sığan baş
    kısmını (ayırıcısız) döner.
  - `beforeTextChanged(...)` — tek karakter siliniyorsa ve o karakter ayırıcıysa `tireSilme`'yi kurar: imleç silinen
    yerdeyse Delete (`1`), değilse geri tuşu (`0`).
  - `afterTextChanged(e)` — kod karakterlerini ve imleçten önceki kod karakteri sayısını (`n`) bulur; `tamKod` varsa kutu
    yalnız o olur; tire silindiyse yanındaki karakteri de siler; 16'ya kırpar; `bicim` ile yeniden yazar (değiştiyse) ve
    imleci `imlecYeri(n)`'ye koyar.

### Kodu gösteren kutu

- `kutu(c, kod, kopyalandi)` → `LinearLayout` (dikey, ortalı):
  - Kod yazısı: `bicim(kod)`, 26 sp, eş aralıklı yazı tipi (`Tema.KOD`), seçilebilir, ortalı, harf aralığı 0.04, TEK
    satır ve otomatik küçültme (10–26 sp, 1 sp adım). Zemin açık ana renk, ince çerçeve, küçük köşe yarıçapı; iç boşluk
    12/16 dp.
  - Ekran okuyucu: `"Kod: "` + `harfHarf` (aşağıda).
  - Sığmazsa: düzen dinleyicisi, yazı en küçük boya indiği hâlde tek satıra sığmadığını görürse bir kez
    `ikiSatiraGec`'i çağırır ve kendini kaldırır.
  - "Kopyala" (ikincil düğme, 10 dp üstte): panoya `ClipData.newPlainText("Kişi kodu", bicim(kod))` — tireli biçim;
    ardından `kopyalandi` (çağıranlar "Kod kopyalandı." diye alt mesaj gösterir).
- `ikiSatiraGec(t, kod)` (iç) — ikinci gruptan sonra satır başı: `"Ab3#-kQx9-"` ve `"+mPt-7?zR"`. Yazı boyu ilk satır
  genişliğe sığacak kadar büyütülür (en çok 26 sp; hiçbir zaman o anki boydan küçük değil); otomatik küçültme kapanır,
  satır sınırı kalkar — çok dar ekranda kod daha çok satıra bölünse de hiçbir yeri gizlenmez. Telefonun satır kırıcısına
  bırakılınca kod üçe bölünüp sonu gizlenebildiği için bu yol seçilmiş (yorumda).
- `harfHarf(k)` (iç) — ekran okuyucu için kod harf harf: büyük harf "büyük A", küçük harf ve rakam olduğu gibi, `!`
  "ünlem", `?` "soru işareti", `#` "diyez", `*` "yıldız", `+` "artı", `=` "eşittir"; virgülle ayrılır
  ("Kod: büyük A, b, 3, diyez, …").

## Kimle konuşur?

- Çağırdıkları: [Arayuz.md](Arayuz.md) (`dikey`, `yazi`, `dugme` `IKINCIL`, `ekle`), `Tema.java` (`KOD` eş aralıklı yazı
  tipi, `zemin`, `renk`, `R_KUCUK`, `dp`), renkler `R.color.yazi`, `R.color.ana_acik`, `R.color.ana_cizgi`; Android
  `ClipboardManager`, `InputFilter`, `TextWatcher`, `Selection`, `TextView` otomatik boyutlandırma; `java.util.regex`.
- Onu kullananlar (grep):
  - [EkleSayfasi.md](EkleSayfasi.md) — velinin "Çocuğunu ekle" sayfası: kutu "Veli kodu (16 karakter)", ipucu `ORNEK`,
    tür görünür parola + öneri yok, `kutuyaBagla`; gönderirken `sade(hamDeger())` ve `UZUNLUK` denetimi ("Veli kodu 16
    karakterdir.") → `POST /api/kisilik/cocuk { code }`. Öğretmen/müdür sayfası ("Kişi kodun"): `kutu(e, kod, …)`.
  - [AyarlarSayfasi.md](AyarlarSayfasi.md) — öğrencinin "Veli kodun" kartı: `kutu(e, user.code, …)`.
- Sunucu (dolaylı; site deposunda `sunucu/bolumler/kisilik.md`): kodu gösteren uçlar `GET /api/me` (`user.code`,
  öğrencide veli kodu) ve `GET /api/kisilikler` (`kisiKodu`, ham ve tiresiz; ekran tireli biçimi kendisi kurar); kod
  giren uç `POST /api/kisilik/cocuk { code }`. Asıl işi `sunucu/bolumler/veli.js`'teki `cocukBagla` yapar: hesap başına
  dakikada 5 deneme, aynı bağlantıdan saatte en çok 30 yanlış kod (429); boşluk ve tireleri silip harf duyarlı karşılaştırır,
  eski 10 ve 15 karakterli kod geçmez.
- Kuralın öbür kopyaları: `sunucu/ortak.js` ve `public/js/parcalar/05-giris.js` (site deposu). Aydınlatma metninin "Kişi
  kodu (öğrencide veli kodu)" satırı kodun ne işe yaradığını ve kimin gördüğünü anlatır.

## Nasıl çalışır (adım adım)?

```
Veli "Çocuğunu ekle" kutusuna yazar:  A b 3 # k
  filter: sığıyor → olduğu gibi
  afterTextChanged: sade "Ab3#k", imleç 5 → bicim "Ab3#-k", imleç 6
Geri tuşu tirenin ardında ("Ab3#-|k"):
  beforeTextChanged: silinen tek karakter "-", imleç silinen yerde değil → tireSilme = 0
  afterTextChanged: "Ab3#" + "k" → yanındaki "#" de silinir → "Ab3k"
Velinin yapıştırdığı: "Merhaba, veli kodu: Ab3#-kQx9-+mPt-7?zR. Görüşürüz"
  filter: uzunluk ≥ 16 → ayikla → (1) tireli desen bulur → GECERLI → "Ab3#kQx9+mPt7?zR" (tamKod)
  afterTextChanged: kutu yalnız kod → "Ab3#-kQx9-+mPt-7?zR"
"Çocuğumu ekle" → sade → 16 mı? → POST /api/kisilik/cocuk { code: "Ab3#kQx9+mPt7?zR" }

Öğrencinin Ayarlar'ı: kutu(e, "Ab3#kQx9+mPt7?zR") → [ Ab3#-kQx9-+mPt-7?zR ] [Kopyala]
  çok büyük yazı boyu → en küçük boyda da sığmıyor → [ Ab3#-kQx9- ]
                                                    [ +mPt-7?zR  ]
```

## Dikkat!

- **Kural üç yerde elle aynı tutulur.** Desen, ayırıcı kümesi, ayıklama sırası ve biçim sunucuda, sitede ve burada ayrı
  ayrı yazılı. Site deposundaki `testler/test-kisi-kodu.js` sunucu ile siteyi karşılaştırır ("ayırıcı kümesi iki dosyada
  ayrı yazılı… her UTF-16 biriminde aynı karar"), ama bu Java dosyasını DENEMEZ. Kuralı değiştiren iş buraya da aynı
  değişikliği yapmalı (16 karakterli kod işi öyle yaptı: Android tarafı `commit 10`).
- **Kutu görünür parola türünde olmalı.** Bicimleyici kutuya kendisi yazıyor; telefon klavyesi harfleri birleştirerek
  yazarsa (altı çizili sözcük) bu yazma klavyenin birleştirdiği yeri bozabilir. [EkleSayfasi.md](EkleSayfasi.md) kutuyu
  `TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | TYPE_TEXT_FLAG_NO_SUGGESTIONS` yapıyor: klavye birleştirme yapmaz. Yeni bir kod
  kutusu eklenirse aynı tür verilmeli. (Bu türün `Arayuz.alan`'da noktalarla gizlenmemesi aynı commit'te düzeltildi;
  [Arayuz.md](Arayuz.md).)
- **`kutuyaBagla` öbür süzgeçleri siler.** `setFilters` diziyi bütünüyle değiştirir; kutuya önceden konmuş bir uzunluk
  süzgeci varsa gider. 16 sınırını zaten Bicimleyici koyuyor.
- **Tam kod yapıştırmak kutudakini siler.** Yapıştırılan metinde geçerli bir kod bulunursa kutuda yalnız o kalır — kutuda
  yarım yazılmış bir şey varsa da. Bilerek böyle (sitedeki kutuyla aynı).
- **Yapıştırma en az 16 karakterse ayıklanır.** Daha kısa yapıştırmalar (ör. kodun yarısı) olduğu gibi eklenir, kutuda
  kalan yere göre kırpılır.
- **İki satır kararı bir kez verilir.** Düzen dinleyicisi iki satıra geçince kendini kaldırır; sonra ekran genişliği
  değişirse (telefon döndürülür; ana ekran döndürmede yeniden kurulmaz) yazı boyu yeniden hesaplanmaz. Satır sınırı
  kalktığı için kod yine gizlenmez, yalnız boyu yeni genişliğe göre ideal olmayabilir.
- **Seçip kopyalamada satır başı gelir.** İki satır kipindeyken yazıyı seçip kopyalayan kişi `"Ab3#-kQx9-\n+mPt-7?zR"`
  alır; hem bu sınıfın `sade`'si hem sitedeki `\s` satır başını ayırıcı saydığı için kod yine çalışır. "Kopyala" düğmesi
  her zaman tek satırlık tireli biçimi verir.
- **Pano hassas işaretlenmiyor.** Veli kodu, bir çocuğa veli olarak bağlanmaya yarar; aydınlatma metnine göre veli kodu
  girince çocuğa bağlanır (öğrenciye bildirim gider). "Kopyala" kodu düz metin olarak panoya koyar; Android 13 ve üstünde
  panoya kopyalanan metin ekranın altında önizlenir ve bazı klavyelerin pano geçmişinde kalır. Öneri (kod değiştirilmedi):
  Android 13+'da `ClipDescription.EXTRA_IS_SENSITIVE` ile hassas işaretlemek; doğrulayıcı tanımında TOTP kodları için
  planlanan "panodan 30 sn sonra silme" burada da düşünülebilir.
- `bicim` 16'dan farklı uzunlukta da tire koyar; sunucu yalnız 16 karakterlik kodu biçimler. Sunucudan eski (15
  karakterli) bir kod gelirse burada `XXXX-XXXX-XXXX-XXX` görünür. Bugün sunucu eski kodları geçersiz sayıyor.

## Testleri

- Android deposunda otomatik test yok; derleme ve lint (`./gradlew --offline assembleDebug lintDebug`) ana oturumda.
- Kuralın sunucu ve site kopyalarını site deposundaki `testler/test-kisi-kodu.js` korur: biçim (16 karakter, ilk harf, dört
  sınıf, karışan karakter ve Türkçe harf yok, 4'erli tireli gösterim), harf duyarlılığı, boşluk/tire silme (tireli,
  tiresiz, boşluklu yapıştırma), eski 10 ve 15 karakterli kodun geçmemesi, kod kutusunun tireyi kendiliğinden koyması ve
  yapıştırmada tam kodu ayıklaması (05-giris.js, tarayıcısız), Kopyala'nın tireli biçimi vermesi, iki dosyadaki ayırıcı
  kümesinin her UTF-16 biriminde aynı karar vermesi. Bu Java sınıfını çalıştırmaz.
- Elle (öykünücü, deneme paketi):
  - Öğrenciyle gir → Ayarlar → "Veli kodun": kod `XXXX-XXXX-XXXX-XXXX` biçiminde tek satırda; "Kopyala" → "Kod
    kopyalandı."; telefonun yazı boyunu en büyüğe al → kod iki satıra geçmeli, hiçbir yeri gizlenmemeli.
  - TalkBack açıkken koda dokun → "Kod: büyük …, …" diye harf harf okunmalı.
  - Veliyle gir → Ekle → Çocuğunu ekle → kodu harf harf yaz (tire kendiliğinden gelmeli), tirenin ardında geri tuşuna bas
    (tireyle önceki karakter birlikte gitmeli), "Veli kodu: Ab3#-kQx9-+mPt-7?zR" gibi bir cümleyi yapıştır (kutuda yalnız
    kod kalmalı), boşluklu/tiresiz yapıştır → hepsi aynı biçime girmeli → "Çocuğumu ekle".

## Son durum

- `git log` (Android deposu): 2 commit.
  - `cad4bc3 commit 10` (2026-09-27, 16 karakterli kişi kodu): kod 15 karakterden 16'ya çıktı ve gösterim 5'erli
    boşluklu gruplardan 4'erli tireli gruplara geçti; `UZUNLUK`, `ORNEK`, `GECERLI` ve `ARAMA` desenleri, genişletilmiş
    ayırıcı kümesi (`ayiriciMi`, `tireMi`, `gorunmezMi`; eskiden yalnız boşluk silinirdi), `ayikla`, `kutuyaBagla` ve
    `Bicimleyici` eklendi; `kutu`'da tek satır + otomatik küçültme + `ikiSatiraGec`, harf aralığı 0.06 → 0.04, iç boşluk
    14 → 12 dp; "Kopyala" ham kod yerine tireli biçimi vermeye başladı; `harfHarf`'te `-` ("tire") yerine `=` ("eşittir").
    Aynı commit'te `Arayuz.java` (görünür parola türünün gizlenmemesi) ve `EkleSayfasi.java` (kutunun bağlanması) değişti.
  - `e96c5f2 commit 6` (2026-09-26, yerel uygulamanın çekirdeği): ilk hâli — 15 karakter, 5'erli boşluklu gösterim
    (`"Ab3#k Qx9+m Pt7?z"`), Kopyala ham kodu, `sade` yalnız boşlukları silerdi.
- Bilinen açıklar (kod değiştirilmedi): Java kopyasının hiçbir testle korunmaması; panoya hassas işaretsiz kopyalama; iki
  satır kararının ekran genişliği değişince yenilenmemesi.
- Planlı işlerden bu dosyaya dokunması beklenenler: "Android yerel uygulama" (rol ekranları geldikçe kod giren ya da
  gösteren her yeni ekran bu sınıfı kullanmalı; doğrulayıcı eki pano kuralları getiriyor); "Tek kişi tek hesap + portallar
  öğrencide de" (öğrencinin veli kodunun nerede görüneceği ve velinin çocuğa bağlanma yolu bu işle değişebilir);
  "Çok dil" (`harfHarf`'in Türkçe okunuşları, "Kopyala", "Kod: ").

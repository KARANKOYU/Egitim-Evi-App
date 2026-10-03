# araclar/simgeleri-uret.js

Sitenin çizgi simgelerini (site deposunda `public/js/parcalar/02-ikonlar.js`, `IKONLAR`) Android vektör çizimlerine çeviren
Node betiği: her simge için `app/src/main/res/drawable/ik_<ad>.xml` yazar, böylece uygulamadaki simgeler sitedekilerle birebir
aynı olur.

## Bu dosya ne yapar?

Eğitim Evi sitesinin arayüzünde emoji yok; her yerde aynı biçimde çizilmiş 24×24'lük çizgi simgeler var (ev, takvim, ödev,
zil…). Site bunları SVG metni olarak tek bir JavaScript nesnesinde tutar. Android uygulaması aynı simgeleri kullanmak ister
ama Android SVG okumaz; kendi vektör çizim biçimi (`<vector>` + `<path android:pathData>`) vardır.

Bu betik o köprüdür. Sitenin deposunu gösterirsin, o da `IKONLAR`'daki her simgenin SVG öğelerini (yol, daire, dikdörtgen,
çizgi…) Android'in anladığı yollara çevirip `drawable/` klasörüne birer XML yazar. Simgeyi elle yeniden çizmek yok; sitede bir
simge değişince betiği bir kez çalıştırırsın, uygulama da aynı simgeye kavuşur.

Kim kullanır: uygulamayı geliştiren kişi, elle, ihtiyaç oldukça (sitede simge eklendi ya da değişti). Derlemenin bir adımı
değildir; Gradle onu çağırmaz. Ürettiği dosyalar depoya girer.

Bugün `drawable/`'daki 50 `ik_*.xml` bu betiğin çıktısıdır: [../app/src/main/res/drawable/KLASOR.md](../app/src/main/res/drawable/KLASOR.md).

## İçinde neler var?

Betik tek dosya, dışa açılan bir işlevi yok (bir modül değil, komut satırından çalışır). Node'un yalnız `fs` ve `path`
modüllerini kullanır; hiçbir npm paketi gerekmez.

### Girdi ve çıktı

- **Komut:** `node araclar/simgeleri-uret.js <site deposunun klasörü>`. Dosyanın yorumundaki örnek `../Egitim-Evi`
  (GitHub'dan klonlanmış site deposu). Bu bilgisayarda site deposu Android deposunun yanındaki `eğitim evi` klasörüdür;
  adında boşluk olduğu için tırnakla verilir: `node araclar/simgeleri-uret.js "../eğitim evi"`.
- **Okuduğu:** `<site>/public/js/parcalar/02-ikonlar.js`.
- **Yazdığı:** `app/src/main/res/drawable/ik_<ad>.xml` — yol betiğin kendi yerine göre kurulur (`__dirname/../app/src/main/res/drawable`),
  komutu hangi klasörden çalıştırdığın fark etmez.
- **Konsol:** sonunda `50 simge yazıldı: <klasör>` gibi bir satır.

### Hata durumları

- Klasör verilmezse: `Kullanım: node araclar/simgeleri-uret.js <Eğitim Evi deposu>`, çıkış kodu 1.
- Dosyada `var IKONLAR = {` … `\n};` kalıbı bulunamazsa: `IKONLAR bulunamadı`, çıkış kodu 1.
- `02-ikonlar.js` yoksa `fs.readFileSync` hata fırlatır (Node'un kendi hata iletisi).
- Tanımadığı SVG öğeleri (ör. `<g>`, `<text>`, `<use>`) hata vermez, **sessizce atlanır**: öğeleri bulan kalıp yalnız yedi
  öğe adını (`path`, `circle`, `rect`, `line`, `polyline`, `polygon`, `ellipse`) arar. `yol()`'un sonundaki
  `bilinmeyen öğe: <ad>` hatası bu yüzden bugünkü kodla hiç tetiklenemez; simge eksik çizilir ama konsolda uyarı çıkmaz.

### İç işlevler ve adımlar

- **`IKONLAR`'ı almak.** Dosyanın metninde `var IKONLAR = \{([\s\S]*?)\n\};` aranır; süslü parantezlerin içi
  `new Function('return {' + … + '};')()` ile bir nesneye çevrilir (ad → SVG metni). Yani sitenin o kod parçası
  **çalıştırılır**; bu yüzden betik yalnız kendi site deponla kullanılmalı.
- **`sayi(v)`** — `Number(v)`.
- **`nitelikler(s)`** — bir öğenin `ad="değer"` niteliklerini nesneye çevirir.
- **`yol(oge, a)`** — bir SVG öğesini Android yol verisine (`pathData`) çevirir:
  - `path` → `d` olduğu gibi;
  - `circle`, `ellipse` → iki yay (`M cx-rx,cy a rx,ry 0 1,0 2rx,0 a rx,ry 0 1,0 -2rx,0`);
  - `rect` → köşesizse `M x,y h w v h h -w z`; `rx` varsa dört köşesi yuvarlatılmış yol (yalnız `rx` kullanılır, `ry` okunmaz);
  - `line` → `M x1,y1 L x2,y2`;
  - `polyline`, `polygon` → noktalar `M … L …`; `polygon`'da sonuna `z`;
  - başka öğe → `bilinmeyen öğe` hatası (ana döngü `yol()`'a yalnız yukarıdaki yedi öğeyi verdiği için bu satıra ulaşılmaz).
- **Ana döngü** — her simge için SVG'deki `path|circle|rect|line|polyline|polygon|ellipse` öğelerini sırayla bulur. Öğenin
  `fill` niteliği varsa ve `none` değilse dolu yol (`android:fillColor="#FF000000"`), yoksa çizgi yol
  (`android:strokeColor="#FF000000" android:strokeWidth="1.8" android:strokeLineCap="round" android:strokeLineJoin="round"`)
  yazar. Hiç öğe bulunmayan simge atlanır.
- **Dosyanın biçimi** — her dosya:
  - `<?xml …?>`, ardından yorum: `Sitenin "<ad>" simgesi (02-ikonlar.js); araclar/simgeleri-uret.js üretir, elle değiştirme`;
  - `<vector>`: `tools:ignore="UnusedResources"` (henüz kullanılmayan simgeler lint uyarısı vermesin), `24dp` × `24dp`,
    `viewportWidth`/`viewportHeight` 24;
  - her öğe için bir `<path>`.
- **Dosya adı** — `ik_` + sitedeki ad; büyük harflerin önüne alt çizgi konup küçük harfe çevrilir, `a-z0-9_` dışındaki her
  karakter `_` olur: `gozKapali` → `ik_goz_kapali.xml`, `ev` → `ik_ev.xml`.
- Dosya varsa üstüne yazılır; satır sonları `\n`.

Renk kasıtlı olarak siyahtır: asıl rengi uygulamanın kodu verir (`Arayuz.ikon` → `ImageView.setImageTintList`), böylece
aynı simge seçili sekmede kırmızı, değilse gri, koyu temada açık renkli çizilir.

## Kimle konuşur?

- **Okuduğu:** site deposunun `public/js/parcalar/02-ikonlar.js`'i (orada `IKONLAR`, 50 simge; o dosyanın belgesi site
  deposunda `public/js/parcalar/02-ikonlar.md`).
- **Yazdığı:** [../app/src/main/res/drawable/KLASOR.md](../app/src/main/res/drawable/KLASOR.md)'deki `ik_*.xml` dosyaları.
- **Çıktısını kullananlar:** [../app/src/main/java/org/egitimevi/aile/Arayuz.md](../app/src/main/java/org/egitimevi/aile/Arayuz.md)
  (`ikon`, `simgeDugme`, `tiklananSatir`, `bosDurum`, `hataKutusu`),
  [../app/src/main/java/org/egitimevi/aile/Sekmeler.md](../app/src/main/java/org/egitimevi/aile/Sekmeler.md) (sekme
  simgeleri), [../app/src/main/java/org/egitimevi/aile/AnaEkran.md](../app/src/main/java/org/egitimevi/aile/AnaEkran.md)
  (geri, zil) ve ayar, ekle, portal, giriş sayfaları.
- **Çağıran:** yok; elle çalıştırılır. Gradle, testler ya da başka bir betik çağırmaz.
- **Karıştırılmasın:** site deposundaki `araclar/simge-uret.js` başka bir araçtır — sitenin logosunu (ev, PNG ve SVG) üretir.
  Android'deki marka simgeleri (`simge_on`, `simge_tek`, `simge_marka`, `bildirim_simge`) onun ölçüleriyle elle yazılmıştır;
  bu betik onlara dokunmaz.

## Nasıl çalışır (adım adım)?

```
node araclar/simgeleri-uret.js "../eğitim evi"
  1. siteKoku = argv[2]                         (yoksa kullanım iletisi, çıkış 1)
  2. 02-ikonlar.js oku ─► "var IKONLAR = { … \n};" bul ─► new Function ile nesne
  3. her (ad, svg) için:
       svg'deki öğeler ─► nitelikler ─► yol(oge, a) ─► { d, dolu }
       öğe yoksa atla
       XML kur: yorum + <vector 24×24> + her öğe için <path> (dolu: fillColor; değilse 1.8 kalın yuvarlak çizgi)
       app/src/main/res/drawable/ik_<snake_ad>.xml'e yaz (üstüne)
  4. "<adet> simge yazıldı: <klasör>"
```

Sonra yapılacaklar (betik yapmaz): `git --no-pager diff app/src/main/res/drawable` ile neyin değiştiğine bak, yeni simgeyi
kodda kullan, `./gradlew --offline lintDebug`.

## Dikkat!

- **Dosya silmez.** Sitede bir simge kaldırılırsa eski `ik_<ad>.xml` burada kalır; elle silinmeli. Planlı "kulüpler
  kaldırılacak" işinde sitedeki `kulup` simgesi giderse `ik_kulup.xml` için bu geçerli.
- **Elle düzenlenen `ik_*.xml` kaybolur.** Betik her çalışmada bütün `ik_` dosyalarının üstüne yazar; dosyaların başındaki
  "elle değiştirme" yorumu bu yüzden var. Elle yazılması gereken bir simge `ik_` ile başlamayan bir adla konmalı (bugün
  `ust_yenile.xml` öyle).
- **Sitenin kodunu çalıştırır.** `IKONLAR` metni `new Function` ile değerlendirilir; betiğe yalnız kendi, güvendiğin site
  deponu ver.
- **Kalıba bağlı.** `IKONLAR` tanımı `var IKONLAR = {` ile başlamalı ve satır başında `};` ile bitmeli; sitede bu biçim
  değişirse (ör. `const`'a geçilirse) betik "IKONLAR bulunamadı" der.
- **Desteklemedikleri:** `<g>` grupları ve `transform` (grubun içindeki öğeler yine bulunur ama dönüşümleri kaybolur), yedi
  öğe dışındaki her öğe (sessizce atlanır, yukarıda), öğe başına `stroke-width` ya da `stroke` rengi (hepsi 1,8 kalınlık ve
  siyah olur), `rect`'in `ry`'si. `nitelikler` yalnız küçük harfli ve çift tırnaklı nitelikleri okur (`fill='currentColor'`
  gibi tek tırnaklı bir nitelik görülmez, öğe dolu yerine çizgi olarak yazılır). Bugünkü simgeler yalnız `path` (94 öğe), `circle` (20) ve `rect` (7) kullanıyor, hiçbirinde
  dolu öğe yok; bunların hepsi düzgün çevriliyor (aşağıda).
- **Çalışırken eklenen simgeler gelmez.** Sitede quiz ekranı `IKONLAR`'a çalışırken `yukari` ve `asagi` oklarını ekliyor; bunlar
  dosyadaki `IKONLAR` tanımında olmadığı için betik onları üretmez.
- **Çıktı yolu betiğe göre.** Betiği başka bir klasöre kopyalarsan yazdığı yer de değişir.

## Testleri

- Otomatik testi yok.
- Bu belge yazılırken betik **çalıştırılmadı**; yerine aynı dönüşüm ayrı bir komutla, hiçbir dosyaya yazmadan yeniden
  hesaplandı: sitenin bugünkü `IKONLAR`'ındaki 50 simgenin her biri için üretilecek XML, depodaki `ik_*.xml` ile bayt bayt aynı
  çıktı (sitede 50 ad, burada 50 dosya, eksik ya da fazla yok). Yani depodaki simgeler sitenin bugünkü hâliyle eşit.
- Elle deneme: betiği çalıştır, `git --no-pager status`'ta `drawable/` altında değişiklik olmamalı (site değişmediyse);
  sitede bir simgeyi değiştirip yeniden çalıştırınca yalnız o dosyanın değiştiğini gör; sonra `./gradlew --offline lintDebug`.

## Son durum

- Betik `e96c5f2 commit 6` (2026-09-26, yerel uygulama çekirdeği) ile eklendi ve o günden beri değişmedi; aynı commit'te
  ürettiği 50 `ik_*.xml` de depoya girdi.
- Açık iş yok. Sitenin `02-ikonlar.js`'i en son 26 Eylül'de değişti (site deposunda `commit 421`), betiğin çıktısı bugünkü
  liste ile eşit (yukarıda).
- Planlı işlerden bu betiği ilgilendirenler: "Android yerel uygulama (bütün roller)" — rol ekranları yeni simge isterse önce
  sitedeki listeye eklenip bu betikle üretilir (işin kuralı: yeni simge gerekirse önce sitedekini kullan); "Kulüpler
  kaldırılacak" — sitede `kulup` simgesi kalkarsa `ik_kulup.xml` elle silinmeli.

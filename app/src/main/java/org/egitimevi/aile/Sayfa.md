# app/src/main/java/org/egitimevi/aile/Sayfa.java

Uygulamadaki her ekranın atası: başlık, görünümün bir kez kurulması, geri dönülünce `gorundu`, geri tuşunu kendisi
karşılama, çubuksuz ekran ve "Yenile"; ayrıca sunucudan veri isteyip "yükleniyor → içerik ya da hata + Yeniden dene"
düzenini hazır veren `Sayfa.Veri`.

## Bu dosya ne yapar?

Uygulama tek bir Android ekranından (`Activity`) oluşur: [AnaEkran.md](AnaEkran.md). Giriş, Ana sayfa, Bildirimler, Ayarlar,
Şifre değiştir… bunların hiçbiri ayrı bir `Activity` ya da `Fragment` değildir (AndroidX kullanılmıyor); hepsi bu soyut sınıftan
türeyen sade Java nesneleridir. `AnaEkran` sayfaları yığınlarda tutar, üstteki sayfanın görünümünü ortadaki alana koyar, geri
tuşunda çıkarır.

Bu dosya ikisi arasındaki sözleşmedir. Bir sayfa `AnaEkran`'a şunları söyler: başlığım ne (`baslik`), görünümüm ne
(`olustur`), geri tuşunu ben mi karşılarım (`geriBas`), üst ve alt çubuk gizlensin mi (`cubuksuz`), üst çubukta "Yenile"
olsun mu (`yenilenir`), yenilenince ne yapayım (`yenile`), üstümdeki sayfa kapanıp yeniden göründüğümde ne yapayım
(`gorundu`). `AnaEkran` de sayfaya kendisini (`e`) verir; sayfa onunla gezinir (`e.git`, `e.kapat`), kısa mesaj gösterir
(`e.bildir`) ve istek atar ([Ag.md](Ag.md) `Ag.get(e, …)`).

Sunucudan bir liste çekip gösteren ekranlar hep aynı üç durumu yaşar: yükleniyor, geldi, hata. `Sayfa.Veri` bunu bir kez yazar;
alt sınıf yalnız adresi (`adres`) ve gelen cevaptan görünümü (`ciz`) verir.

## İçinde neler var?

### Alanlar

- `e` (`protected AnaEkran`) — sayfanın bağlı olduğu ekran. `AnaEkran.goster` her gösterişte doldurur; sayfa nesnesi
  kurulurken (yapıcıda) henüz `null`'dır.
- `gorunum` (paket içi `View`) — `olustur`'un döndürdüğü görünüm; `AnaEkran` saklar, sayfa yeniden üste gelince aynısını
  yeniden kullanır. `sayfayiYenile` onu `null` yapar.

### Sayfanın `AnaEkran`'a söyledikleri

| İşlev | Varsayılan | `AnaEkran` ne zaman sorar / çağırır | Bugün geçersiz kılanlar (grep) |
|---|---|---|---|
| `baslik()` | soyut | her gösterişte: üst çubuktaki başlık ve pencere başlığı (`setTitle`) | her sayfa |
| `olustur()` | soyut (`protected`) | sayfanın görünümü yoksa (ilk gösteriş ya da yenileme sonrası) | her `Sayfa`; `Veri` kendisi yazar |
| `gorundu()` | boş | `kapat()`: üstteki sayfa kapanıp bu sayfa yeniden görününce | `AyarlarSayfasi` (`yenile()`) |
| `geriBas()` | `false` | `geri()`'nin ilk adımı; `true` dönerse geri orada biter | `KodSayfasi` (sayaçları durdurur, `false` döner) |
| `cubuksuz()` | `false` | her gösterişte; `true` ise üst ve alt çubuk gizli | `GirisSayfasi`, `KayitSayfasi`, `KodSayfasi`, `KvkkSayfasi`, `SifremiUnuttumSayfasi` (`true`); `SifreSayfasi` (yalnız zorunluyken) |
| `yenilenir()` | `false` | her gösterişte; `true` ise üst çubukta "Yenile" | `Veri` (`true`); `EkleSayfasi.Kod` (`false`) |
| `yenile()` | `e.sayfayiYenile(this)` | üst çubuktaki "Yenile" | `Veri` (`yukle()`) |

Varsayılan `yenile()` görünümü tamamen atıp `olustur()`'u yeniden çalıştırır ([AnaEkran.md](AnaEkran.md) `sayfayiYenile`:
sayfa üstteyse hemen, değilse bir sonraki gösterişte).

### `Sayfa.Veri` — sunucudan veri isteyip çizen sayfa

- `kap` (`protected FrameLayout`) — sayfanın görünümü; içine sırayla "yükleniyor", içerik ya da hata kutusu konur.
- `adres()` (soyut) — istenecek API yolu, ör. `"/api/notifications"`.
- `ciz(j)` (soyut) — gelen JSON cevaptan içerik görünümünü kurar.
- `olustur()` — boş `kap`'ı kurar, `yukle()`'yi çağırır, `kap`'ı döner.
- `yenilenir()` → `true`; `yenile()` → `yukle()` (görünüm yeniden kurulmaz, yalnız veri yeniden istenir).
- `yukle()` (`protected`) — `kap` boşsa ortasına yükleniyor göstergesi (`Arayuz.yukleniyor`). `Ag.get(e, adres(), …)`:
  - başarı → `kap` boşaltılır, `ciz(j)` tam boy konur;
  - hata ([AnaEkran.md](AnaEkran.md) `genelHata` karşılamadıysa) → `kap` boşaltılır, `Arayuz.hataKutusu`: "Bir sorun oldu",
    hatanın iletisi (ör. "İnternet bağlantısı yok ya da Eğitim Evi'ne ulaşılamadı.") ve "Yeniden dene" (yine `yukle`).

### Bugünkü sayfalar (grep: `extends Sayfa`)

| Sayfa | Türü | Nerede görünür |
|---|---|---|
| [AnaSayfa.md](AnaSayfa.md) | `Sayfa` | "Ana sayfa" sekmesinin kökü |
| [BildirimlerSayfasi.md](BildirimlerSayfasi.md) | `Sayfa.Veri` (`/api/notifications`) | "Bildirimler" sekmesinin kökü; üst çubuktaki zil |
| [AyarlarSayfasi.md](AyarlarSayfasi.md) | `Sayfa` | "Ayarlar" sekmesinin kökü; avatar (öğrenci, servisçi, yönetici); portallar penceresi; `#/profil` bildirimi |
| [EkleSayfasi.md](EkleSayfasi.md) (+ iç sınıf `Veli`) | `Sayfa` | "+ Ekle" |
| `EkleSayfasi.Kod` | `Sayfa.Veri` (`/api/kisilikler`) | "Ekle" sayfasında "Öğretmen" ya da "Müdür" seçeneği (kişi kodunu gösterir) |
| [GirisSayfasi.md](GirisSayfasi.md), [KayitSayfasi.md](KayitSayfasi.md), [KodSayfasi.md](KodSayfasi.md), [SifremiUnuttumSayfasi.md](SifremiUnuttumSayfasi.md) | `Sayfa` | giriş kipi (oturum yokken) |
| [KvkkSayfasi.md](KvkkSayfasi.md) | `Sayfa` | giriş kipi (onay eskiyse) |
| [SifreSayfasi.md](SifreSayfasi.md) | `Sayfa` | zorunluyken giriş kipi; değilse Ayarlar → "Şifre değiştir" |

## Kimle konuşur?

- Çağırdıkları: [AnaEkran.md](AnaEkran.md) — `sayfayiYenile` (varsayılan `yenile`); [Ag.md](Ag.md) — `get` (`Veri.yukle`);
  [Arayuz.md](Arayuz.md) — `yukleniyor`, `hataKutusu`. Android: `View`, `FrameLayout`; `org.json.JSONObject`.
- Onu kullananlar:
  - [AnaEkran.md](AnaEkran.md) — sayfaların tek sahibi: `goster` (`e`'yi ve `gorunum`'u doldurur, `olustur`, `cubuksuz`,
    `yenilenir`, `baslik`), `git`, `kapat` (`gorundu`), `geri` (`geriBas`), `sekmeSec`, `sayfayiYenile`, üst çubuktaki "Yenile"
    (`yenile`); `AnaEkran.Sekme`'nin kök sayfası bir `Supplier<Sayfa>`'dır.
  - [Sekmeler.md](Sekmeler.md) — sekme köklerini `AnaSayfa::new`, `BildirimlerSayfasi::new`, `AyarlarSayfasi::new` olarak verir.
  - Yukarıdaki tablodaki bütün sayfalar (alt sınıflar).
- Sunucuya kendisi gitmez; `Veri` alt sınıfının verdiği adresi `Ag` ile ister (bugün `GET /api/notifications` →
  `sunucu/bolumler/kayit.md`, `GET /api/kisilikler` → `sunucu/bolumler/kisilik.md`, site deposu).

## Nasıl çalışır (adım adım)?

### Bir sayfanın hayatı

```
new AyarlarSayfasi()                       ← e henüz null; yapıcıda e kullanma
AnaEkran.git(s)  ─► yığına koy ─► goster(s, ileri):
                     s.e = AnaEkran
                     s.gorunum == null ? s.gorunum = s.olustur()
                     içerik alanına koy (220 ms kayarak belirir)
                     çubuklar: cubuksuz()? baslik()  yenilenir()? → "Yenile"
üstüne başka sayfa açılır  ─► s'nin görünümü alandan sökülür, s.gorunum saklı kalır
üstteki kapanır (AnaEkran.kapat) ─► goster(s, geri): aynı görünüm, olustur YOK ─► s.gorundu()
"Yenile" ─► s.yenile():  Sayfa → sayfayiYenile: gorunum = null → olustur() baştan
                         Veri  → yukle(): yalnız veri yeniden
geri tuşu ─► s.geriBas()? true → dur | false → AnaEkran'ın kuralı (yığından çıkar, ilk sekmeye dön, finish)
```

### `Sayfa.Veri`

```
olustur ─► kap (boş) ─► yukle ─► kap boş? → yükleniyor göstergesi
                         Ag.get(adres())
                           200  ─► kap.removeAllViews ─► kap.addView(ciz(j))
                           hata ─► genelHata? (401 / kvkkGerek / sifreDegismeli → AnaEkran ekranı değiştirir)
                                   değilse ─► kap.removeAllViews ─► hataKutusu(ileti, "Yeniden dene" → yukle)
"Yenile" ─► yukle: kap dolu → gösterge konmaz, eski içerik cevap gelene kadar durur
```

## Dikkat!

- **`e` yapıcıda `null`'dır.** `AnaEkran` onu ancak `goster`'de verir. Görünümle ya da ağla ilgili her şeyi `olustur`'da
  (ya da sonrasında) yap; yapıcıda `e` kullanan sayfa çöker.
- **`olustur` bir kez çalışır.** Sayfa yığında beklerken görünümü (aynı nesne) saklanır; geri dönüldüğünde form alanlarına
  yazılanlar olduğu gibi durur. Ama varsayılan `yenile()` görünümü baştan kurar: içine yazılmış metinler gider. Formlu bir
  sayfaya "Yenile" açacaksan `yenile`'yi kendin yaz.
- **`gorundu` yalnız `kapat`'ta çağrılır.** İlk gösterişte (`git`), sekme değiştirip geri gelince (`sekmeSec`) çağrılmaz.
  "Her göründüğümde tazelen" isteyen sayfa bunu bilmeli ([AnaEkran.md](AnaEkran.md)).
- **"Kapandım" ya da "gizlendim" kancası yok.** Sayfa yığından çıkarken, üstü örtülürken ya da sekmeler baştan kurulurken
  (`oturumAc`, `yenidenKur`) haber almaz. Zamanlayıcı ya da tekrarlayan iş başlatan sayfa onu kendisi durdurmalı:
  [KodSayfasi.md](KodSayfasi.md) sayaçlarını `geriBas`'ta ve başarılı doğrulamada durduruyor. Ama `geriBas` yalnız geri
  tuşunda çalışır: aynı sayfanın ekrandaki "← Geri" düğmesi `e.kapat()`'ı doğrudan çağırdığı için sayaç durmaz, sayfa
  kapandıktan sonra en çok 60 saniye boşta işler (zararsız; kapanma kancası olsaydı tek yerde durdurulurdu). Ağdan
  sonradan gelen cevaplar kapanmış bir sayfanın görünümüne çizilir; görünmediği için zararsızdır ([Ag.md](Ag.md) yalnız
  Activity'nin kapanıp kapanmadığına bakar).
- **`Veri`'de yenileme sırasında gösterge yok.** "Yenile"ye basınca eski içerik cevap gelene kadar aynen durur; kişi bir şey
  olduğunu anlamayabilir. Yenileme hata verirse (ör. internet koptu) eski, doğru içerik silinir, yerine hata kutusu gelir.
- **Üst üste iki `yukle` birbirini beklemez.** "Yenile"ye iki kez hızlı basılırsa iki istek gider, iki cevap da çizilir; son
  gelen kalır. Bugünkü sayfalar için zararsız, ama `ciz` yan etki yapıyorsa (ör. [BildirimlerSayfasi.md](BildirimlerSayfasi.md)
  okunmamış varsa "okundu" isteği gönderir) iki kez yapar.
- **`ciz`'deki bir hata yakalanmaz.** `ciz` ana iş parçacığında, `Ag`'ın başarı geri çağrısında çalışır; içinde fırlayan bir
  istisna uygulamayı kapatır. Cevaptaki alanları `opt…` ile oku (bugünkü sayfalar öyle yapıyor).
- **Genel hatalarda `kap` olduğu gibi kalır.** 401, `kvkkGerek`, `sifreDegismeli`'yi `AnaEkran` karşıladığında `Veri`'nin hata
  dalı çalışmaz (gösterge dönmeye devam eder); ama ekran zaten giriş, onay ya da şifre ekranına geçtiği için görünmez.
- **`cubuksuz` yalnız uygulama kipinde fark yaratır.** Giriş kipinde (`tekSayfa` ile başlayan akış) `AnaEkran` çubukları
  zaten gizler. Bugün `cubuksuz() = true` dönen sayfaların hepsi yalnız giriş kipinde açılıyor (grep: `tekSayfa` ve
  `GirisSayfasi`'ndan `git`), yani bu değer bugün etkisiz; sayfa ileride bir sekme yığınına itilirse işe yarar.
- Görünüm bir `ScrollView` ise ([Arayuz.md](Arayuz.md) `sayfaGovdesi`) sayfa kaydırması da onunla gelir; `Veri`'nin `kap`'ı
  tam boy `FrameLayout`'tur, kaydırmayı `ciz`'in döndürdüğü görünüm sağlamalıdır.

## Testleri

- Android deposunda otomatik test yok; derleme ve lint (`./gradlew --offline assembleDebug lintDebug`) ana oturumda.
- `Veri`'nin kullandığı uçları site deposundaki testler korur: `testler/test-bildirim.js` (`GET /api/notifications`),
  `testler/test-yetiskin.js` ve `testler/test-kisi-kodu.js` (`GET /api/kisilikler`).
- Elle (öykünücü, deneme paketi):
  - "Bildirimler" sekmesi → üst çubukta "Yenile" var; "Ana sayfa"da ve "Ayarlar"da yok.
  - Uçak kipinde Bildirimler'de "Yenile" → "Bir sorun oldu", ileti ve "Yeniden dene"; uçak kipini kapat, "Yeniden dene" →
    liste.
  - Ayarlar → "Şifre değiştir" → geri → Ayarlar yeniden kurulur (`gorundu`); Ana sayfa sekmesine geç, Ayarlar'a dön →
    yeniden kurulmaz (aynı görünüm).
  - Giriş ekranında üst ve alt çubuk görünmez; "Şifremi unuttum"a git, geri → giriş formunda yazdıkların duruyor.

## Son durum

- `git log` (Android deposu): 1 commit. Dosya `e96c5f2 commit 6` (2026-09-26) ile geldi: uygulama siteyi içinde açan
  WebView'den kendi ekranlarını çizen yerel uygulamaya geçerken "tek Activity + sayfa yığını" düzeninin sözleşmesi olarak
  yazıldı (`Sayfa` ve `Sayfa.Veri`). O günden beri değişmedi.
- Bilinen açıklar (kod değiştirilmedi): kapanma kancasının olmaması, `Veri`'de yenileme göstergesi ve eşzamanlı yükleme
  denetimi olmaması.
- Planlı işlerden bu dosyaya dokunması beklenenler:
  - "Android geri tuşu" (iş 33, Linux'ta): her geri basışta tek adım — önce açık pencere/ayrıntı kapanır, kaydedilmemiş yazı
    sorulur, sonra önceki sayfa, önceki sekme; ana sekmenin kökünde hiçbir şey yapılmaz. Tanım her sayfanın kendi açılır
    parçaları için `geriBas()` yazmasını ya da ortak bir "açık katmanlar yığını" önerir; ikisi de bu sözleşmeyi genişletir.
  - "Android yerel uygulama" (iş 10): rol ekranları bu sınıftan türeyecek (çoğu `Sayfa.Veri`); sol menü ve "Yeni sürüm var"
    şeridi de sayfalarla birlikte çizilecek.
  - "Üst şerit sadeleştirme" (iş 29): uygulamada ← → ⌂ düğmeleri; "ileri" için kapanan sayfaların da tutulması gerekecek.

# app/src/main/java/org/egitimevi/aile/Ayarlar.java

Telefondaki "aile" adlı ayar dosyasının kapısı: çocuğun telefonu (Eğitim Evi Aile) bağlantısının anahtarı, öğrencinin adı,
velinin seçtiği gönderme aralıkları ve son gönderim durumu — ve bugün uygulamanın BÜTÜN ekranlarının kullandığı sunucu adresi.

## Bu dosya ne yapar?

Uygulama telefonda üç ayrı ayar dosyası (Android `SharedPreferences`) tutar ve her birinin kendi sınıfı var:

| Dosya | Sınıf | İçinde |
|---|---|---|
| `aile` | bu dosya | sunucu adresi, Aile anahtarı (`X-Aile-Cihaz`), öğrencinin adı, gönderme aralıkları, son gönderim/konum/hata |
| `uygulama` | `UygulamaAyar.java` | uygulama anahtarı (`X-Cihaz`), bildirim imleci, okulun servis saatleri, açık sefer |
| `oturum` | `Oturum.java` | giriş yapan kişinin oturum anahtarı ve hesap bilgisi |

`Ayarlar` Eğitim Evi Aile 1.0.x'ten kalmadır: o zaman uygulama yalnız "çocuğun telefonu"ydu ve tek ayar dosyası buydu.
Bugün de öncelikle o işe hizmet eder: öğrenci telefonunu velisiyle paylaşmak için [AileEkrani.md](AileEkrani.md)'de
bağlanınca sunucunun verdiği **Aile anahtarı** ve öğrencinin adı buraya yazılır; arka plandaki `IzlemeServisi.java`
konumu ne sıklıkla alacağını, konum ve ekran süresi paylaşımının açık olup olmadığını buradan okur, son gönderimi ve
son sorunu buraya yazar. Sınıf yorumunun dediği gibi Aile anahtarı yalnız konum ve kullanım göndermeye yarar; öğrencinin
hesabına giriş VERMEZ.

Ama dosyanın bir alanı çok daha geniş bir iş görür: **sunucu adresi**. Uygulamanın her ekran isteği ([Ag.md](Ag.md)),
bildirim yoklaması ([Bildirimler.md](Bildirimler.md)), servisçinin sefer konumu, çıkış ve tarayıcıda açılan site
bağlantıları adresi `Ayarlar.sunucu(...)`'dan alır. Dosyada adres yoksa `https://egitimevi.org` kullanılır.

## İçinde neler var?

### Sabitler

- `DOSYA` (iç) = `"aile"` — ayar dosyasının adı. Uygulamaya özel (`Context.MODE_PRIVATE`); her zaman uygulama bağlamıyla
  (`getApplicationContext()`) açılır, yani hangi ekran ya da servisten çağrılırsa çağrılsın aynı dosyadır.
- `VARSAYILAN_SUNUCU` = `"https://egitimevi.org"` — dosyada `sunucu` yoksa dönen adres. Dışa açık ama bugün yalnız bu
  sınıfın içinde kullanılıyor (grep).
- Yapıcı gizlidir (`private Ayarlar()`): sınıf yalnız statik işlevlerden oluşur.

### Dosyadaki anahtarlar

| Anahtar | Okuyan işlev | Yazan işlev | Varsayılan |
|---|---|---|---|
| `sunucu` | `sunucu(c)` | `baglan`, `sunucuYaz`; `cik` korur | `VARSAYILAN_SUNUCU` |
| `cihaz` | `cihazAnahtari(c)`, `bagli(c)` | `baglan` | `""` |
| `ogrenci` | `ogrenciAdi(c)` | `baglan` | `""` |
| `wifiDk` | `wifiAraligi(c)` | `ayariYaz` | 5 |
| `mobilDk` | `mobilAraligi(c)` | `ayariYaz` | 15 |
| `konumAcik` | `konumAcik(c)` | `ayariYaz` | `true` |
| `kullanimAcik` | `kullanimAcik(c)` | `ayariYaz` | `true` |
| `sonGonderim` | `sonGonderim(c)` | `gonderildi` | 0 |
| `sonKonum` | `sonKonum(c)` | `konumAlindi` | 0 |
| `sonHata` | `sonHata(c)` | `hata`; `gonderildi` siler | `""` |

### Okuyan işlevler

- `sunucu(c)` — sunucu adresi (ör. `https://egitimevi.org`; deneme paketinde `http://10.0.2.2:3200` gibi bir yerel adres
  olabilir).
- `cihazAnahtari(c)` — Aile anahtarı (sunucunun verdiği 64 onaltılık hane) ya da boş metin.
- `bagli(c)` — Aile anahtarı var mı (`!cihazAnahtari(c).isEmpty()`). "Bu telefon velisine bağlı mı?" sorusunun tek cevabı.
- `ogrenciAdi(c)` — bağlı öğrencinin adı ("Bağlı hesap: …" satırı için).
- `wifiAraligi(c)`, `mobilAraligi(c)` — velinin seçtiği konum alma aralıkları, dakika (yorum: "Wi-Fi'de sık, mobil veride
  seyrek").
- `konumAcik(c)`, `kullanimAcik(c)` — veli konum ve ekran süresi paylaşımını açık mı bırakmış.
- `sonGonderim(c)`, `sonKonum(c)` — son başarılı gönderimin ve son alınan konumun zamanı (milisaniye; 0 = hiç).
- `sonHata(c)` — servisin yakaladığı son sorunun iletisi (boş = sorun yok).

### Yazan işlevler (hepsi `apply()` ile)

- `baglan(c, sunucu, anahtar, ogrenci)` — bağlanma başarılı olunca sunucu adresi, Aile anahtarı ve öğrencinin adı birlikte
  yazılır.
- `ayariYaz(c, wifiDk, mobilDk, konum, kullanim)` — velinin ayarları; aralıklar en az 1 dakikaya yuvarlanır
  (`Math.max(1, …)`), üst sınır yoktur.
- `gonderildi(c, zaman)` — son gönderim zamanını yazar ve `sonHata`'yı temizler (gönderim başardıysa eski sorun gösterilmez).
- `konumAlindi(c, zaman)` — son konum zamanı.
- `hata(c, mesaj)` — son sorunu yazar (`null` → boş metin).
- `cik(c)` — bağlantıyı unutur: dosyanın HER şeyini siler, yalnız sunucu adresini geri yazar.
- `sunucuYaz(c, sunucu)` — yalnız adresi yazar. Yorumu "Yalnızca deneme paketinde sorulan sunucu adresi (yayın paketi
  egitimevi.org)" diyor ama bugün çağıranı YOK (aşağıda "Dikkat!" ve "Son durum").

## Kimle konuşur?

- Android: `SharedPreferences`, `Context.MODE_PRIVATE`. Başka hiçbir şey çağırmaz; sunucuyla konuşmaz.
- Onu çağıranlar (grep):
  - [AileEkrani.md](AileEkrani.md) — `sunucu` (kutunun ilk değeri), `bagli` (hangi ekran çizilecek), `baglan` ve
    `ayariYaz` (bağlanınca), `ogrenciAdi`, `wifiAraligi`, `mobilAraligi`, `sonKonum`, `sonGonderim`, `sonHata` (bağlı
    ekranın metinleri), `cihazAnahtari` ve `cik` ("Bu telefonun bağlantısını kaldır").
  - `IzlemeServisi.java` — `bagli` (başlasın mı), `konumAcik`, `kullanimAcik`, `wifiAraligi`, `mobilAraligi` (ağ türüne
    göre aralık), `sonKonum` ve `konumAlindi`, `sunucu` ve `cihazAnahtari` (gönderim), `gonderildi`, `hata`, `ayariYaz`
    (30 dakikada bir sunucudan tazelenen ayar), `cik` (sunucu 401/403 derse).
  - [AyarlarSayfasi.md](AyarlarSayfasi.md) — `bagli` ("Bu telefonu velimle paylaş" satırının alt yazısı).
  - Yalnız `sunucu`'yu okuyanlar: [Ag.md](Ag.md) (bütün ekran istekleri), [AnaEkran.md](AnaEkran.md) (`cikis`),
    [Bildirimler.md](Bildirimler.md) (yoklama), `SeferServisi.java` (sefer konumu), `KayitSayfasi.java` (`siteAc`:
    tarayıcıda açılan site, aydınlatma metni ve SSS bağlantıları).
- Değerlerin kaynağı sunucudur (site deposunda `sunucu/bolumler/aile.md`): Aile anahtarı, öğrencinin adı ve ilk ayar
  `POST /api/aile/cihaz` cevabından (`cihazAnahtari`, `ogrenci.ad`, `ayar: { wifiDk, mobilDk, konumAcik, kullanimAcik }`),
  güncel ayar `GET /api/aile/cihaz/ayar`'dan gelir.
- Yedek: manifestte `allowBackup="false"`; `res/xml/veri_aktarimi.xml` (Android 12+) ve `res/xml/yedek_yok.xml` (Android 11
  ve altı) ayar dosyalarını da buluta yedeklemeden ve yeni telefona taşımadan dışarıda bırakır. Oradaki yorumun nedeni:
  anahtar başka telefona kopyalanırsa o telefon çocuğun yerine konum gönderebilirdi.

## Nasıl çalışır (adım adım)?

```
Bağlanma (AileEkrani, öğrenci kendi hesabıyla):
   POST /api/aile/cihaz ─► Ayarlar.baglan(adres, cihazAnahtari, öğrencinin adı)
                         ─► Ayarlar.ayariYaz(wifiDk, mobilDk, konumAcik, kullanimAcik)   (gelmeyenler 5, 15, açık, açık)

Arka plan (IzlemeServisi, dakikada bir tur):
   bagli? değilse dur
   konumAcik? → aralık = Wi-Fi'de wifiAraligi, mobil veride (ya da ağ yokken) mobilAraligi
   konum geldi       → konumAlindi(zaman)
   gönderim başardı  → gonderildi(şimdi)          (sonHata silinir)
   bir şey ters gitti→ hata(ileti)
   30 dk'da bir      → GET /api/aile/cihaz/ayar → ayariYaz(...)
   sunucu 401/403    → cik()                       (anahtar gider, adres kalır)

Kaldırma (AileEkrani "Bu telefonun bağlantısını kaldır") → cik()

Uygulamanın her isteği: Ag / Bildirimler / SeferServisi → Ayarlar.sunucu(c)
   dosyada "sunucu" var mı? ── evet ─► o adres
                             └ hayır ─► https://egitimevi.org
```

## Dikkat!

- **Aile bağlantısının adresi bütün uygulamanın adresi olur.** Öğrenci "Çocuğun telefonu" ekranında sunucu kutusuna başka
  bir adres yazıp bağlanırsa `baglan` onu yazar ve uygulamanın bütün ekranları da oraya gitmeye başlar; `cik` adresi
  koruduğu için bağlantı kaldırılsa da adres kalır ([Ag.md](Ag.md), [AileEkrani.md](AileEkrani.md)). Uygulamada adresi
  değiştiren başka bir yol yok: deneme paketinin ilk açılışta adres soran penceresi `e96c5f2 commit 6`'da kalktı, o
  pencerenin kullandığı `sunucuYaz`'ın (ve `UygulamaAyar.sunucuSecildi(c, true)`'nun) bugün çağıranı yok. Sınıf yorumu
  adresin uygulamanın tamamını etkilediğini söylemiyor; adresi okuyan yeni bir yer yazarken bunu bil.
- **Adres burada denetlenmez.** `baglan` ve `sunucuYaz` ne verilirse onu yazar. `AileEkrani` bağlanmadan önce
  `Api.adresSorunu` ile denetliyor ([Api.md](Api.md)); `Api` de her istekte yeniden denetler ve sondaki `/`'ları atar.
- **İki anahtar, iki dosya.** Aile anahtarı (`aile` dosyası, `X-Aile-Cihaz`) ile uygulama anahtarı (`uygulama` dosyası,
  `X-Cihaz`) ayrıdır. Uygulamadan çıkış ([AnaEkran.md](AnaEkran.md) `cikis`) bu dosyaya dokunmaz: öğrenci çıksa da konum ve
  ekran süresi paylaşımı sürer; durdurmanın yolu "Bu telefonun bağlantısını kaldır" ya da velinin sitede kaldırması.
- **`cik` her şeyi siler.** Aralıklar, açık/kapalı ayarları, son gönderim ve son sorun da gider; yeniden bağlanınca sunucunun
  cevabıyla baştan yazılır.
- **Varsayılanlar üç yerde aynı tutuluyor.** 5 dk (Wi-Fi), 15 dk (mobil), konum ve ekran süresi açık: bu dosyanın
  okuyucularında, `AileEkrani`'nin bağlanma cevabını okurken (`optInt("wifiDk", 5)` …) ve `IzlemeServisi`'nin ayar
  tazelerken. Birini değiştirirsen ötekileri (ve sunucunun varsayılanını) da değiştir.
- **Dosya ve anahtar adları 1.0.x'teki gibi.** Paket adı da aynı (`org.egitimevi.aile`); bu yüzden Eğitim Evi Aile 1.0.x'ten
  güncellenen telefon bağlantısını kaybetmez, güncellemeden sonra [BaslatmaAlici.md](BaslatmaAlici.md) servisi yeniden
  başlatır (kod okumasına göre; telefonda denenmedi). `"aile"`, `"cihaz"`, `"sunucu"` gibi adları değiştirmek kurulu
  telefonlarda bağlantıyı koparır.
- **`sonHata` ham iletiyi saklar.** `IzlemeServisi` yakaladığı hatanın iletisini olduğu gibi yazar; ağ hatalarında bu çoğu
  zaman Android'in İngilizce cümlesidir ve bağlı ekranda "Son sorun: …" diye görünür ([AileEkrani.md](AileEkrani.md)).

## Testleri

- Android deposunda otomatik test yok (`app/src/test` ya da `androidTest` yok); derleme ve lint
  (`./gradlew --offline assembleDebug lintDebug`) ana oturumda yapılır.
- Bu dosyaya yazılan değerlerin geldiği sunucu sözleşmesini site deposundaki `testler/test-aile.js` korur: öğrenci onayla
  bağlar, 64 haneli anahtar ve ayar gelir; ayar ucu; kaldırılan anahtar 401 (telefon o zaman `cik` ile bağlantıyı unutur).
- Elle (öykünücü, deneme paketi, deneme sunucusu `http://10.0.2.2:3200`):
  - Öğrenci hesabıyla uygulamaya gir → Ayarlar → "Bu telefonu velimle paylaş" → sunucu adresini yaz, bağlan →
    "Bağlı hesap: <öğrencinin adı>" ve "Wi-Fi'deyken 5 dakikada bir, mobil veride 15 dakikada bir".
  - Velinin sitedeki "Çocuğumun telefonu" sayfasında sıklığı değiştir → en geç yarım saat sonra ekranı yeniden açınca yeni
    aralıklar görünür.
  - "Bu telefonun bağlantısını kaldır" → giriş formu gelir; "Sunucu adresi" kutusunda son adres durur (`cik` adresi korur).

## Son durum

- `git log`: 2 commit (Android deposu). Son değişiklik `f70aeca commit 5` (2026-09-26, tek uygulamaya geçiş): yalnız
  `sunucuYaz` eklendi. O commit'te ana ekran siteyi açan bir WebView'di ve deneme paketinde ilk açılışta sunucu adresini
  sorup bununla yazıyordu (`UygulamaAyar.sunucuSecildi` ile bir kez). Hemen ardından gelen `e96c5f2 commit 6` ana ekranı
  yerel yaptı ve o pencereyi kaldırdı; `sunucuYaz` o günden beri çağrılmıyor.
- Dosyanın ilk hâli `34b45f1 commit 1` (2026-09-26, Eğitim Evi Aile 1.0.x): bugünkü bütün alanlar ve öteki işlevler.
- Bilinen açıklar (kod değiştirilmedi): `sunucuYaz`'ın çağıranı yok; uygulamada sunucu adresini seçmenin tek yolu Aile
  bağlantısı.
- Planlı işler: "Android yerel uygulama" tanımı çocuğun telefonu kısmının ESKİ 1.0.x kurulumlarla uyumlu kalmasını istiyor;
  bu dosyanın dosya ve anahtar adları bu yüzden değişmemeli. Velinin "Çocuğumun telefonu" ekranı ayrı yazılacak; bu dosyaya
  dokunması beklenmiyor. Sıradaki işlerin hiçbiri bu dosyayı adıyla anmıyor.

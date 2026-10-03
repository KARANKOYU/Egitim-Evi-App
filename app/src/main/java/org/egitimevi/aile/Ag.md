# app/src/main/java/org/egitimevi/aile/Ag.java

Ekranların sunucuya istek kapısı: isteği üç iş parçacıklı bir havuzda oturum anahtarıyla gönderir, cevabı ana iş parçacığına
getirir, oturumla ilgili genel durumları ekrandan önce [AnaEkran.md](AnaEkran.md)'ye karşılatır, öbür hataları ekrana ya da
alttaki kısa mesaja bırakır.

## Bu dosya ne yapar?

Android'de ağ isteği ana iş parçacığında yapılamaz, ama cevap geldiğinde ekrana dokunmak YALNIZ ana iş parçacığında olur.
Her ekran bu ikisini tek tek düşünmesin diye `Ag` var: ekran "şu yola şu gövdeyi gönder; olursa şunu yap, olmazsa bunu"
der, gerisini `Ag` halleder.

Ayrıca bütün ekranlarda aynı olan durumları ekran düşünmez:

- Oturumun süresi doldu (401) → giriş ekranı ve "Oturumunun süresi doldu. Yeniden giriş yap.";
- Aydınlatma metni güncellendi (403 `kvkkGerek`) → onay ekranı;
- Şifresini okul vermiş, kendi şifresini koymalı (403 `sifreDegismeli`) → zorunlu şifre ekranı.

Bunları [AnaEkran.md](AnaEkran.md)'deki `genelHata` karşılar. Geri kalan her hatayı ekran isterse kendisi gösterir (ör. giriş
formu "Şifre yanlış."ı şifre kutusunun altına yazar); göstermek istemezse ekranın altında birkaç saniyelik kısa mesaj çıkar.

Bu dosya ana ekranın ([AnaEkran.md](AnaEkran.md)) sayfalarının istekleri içindir: oturum varsa her isteğe
`Authorization: Bearer <anahtar>` eklenir; oturum yoksa (giriş, iki adımlı kod, hesap açma, şifremi unuttum, doğrulama
sorusu, okul arama) anahtar boştur ve başlık hiç konmaz — bu istekler de buradan geçer. Anahtarla çalışan arka plan işleri
(bildirim yoklama, servis konumu, çocuğun telefonu) ve ayrı Activity olan [AileEkrani.md](AileEkrani.md) ise `Api`'yi
kendi iş parçacıklarından doğrudan çağırır ([Api.md](Api.md)).

## İçinde neler var?

### Sabit ve arayüzler

- `ARKA` (iç) — `Executors.newFixedThreadPool(3)`: bütün uygulamanın ortak, üç iş parçacıklı havuzu. Süreç yaşadıkça durur,
  kapatılmaz.
- `Tamam` — `ok(JSONObject j)`: başarılı cevap (ana iş parçacığında).
- `Olmadi` — `olmadi(Api.Hata h)`: hata (ana iş parçacığında). `h.durum`, `h.getMessage()` ve sunucunun hata gövdesi
  `h.govde` ([Api.md](Api.md)) elindedir.

### İşlevler

- `get(e, yol, tamam, olmadi)` — `GET`. `e` uygulamanın tek ekranı [AnaEkran.md](AnaEkran.md); `yol` `"/api/..."` (sorgu
  parametreleri dahil, ör. `"/api/okul-adres/ara?q=" + URLEncoder.encode(...)`).
- `post(e, yol, govde, tamam, olmadi)` — `POST`; `govde` `null` ise boş JSON (`{}`) gönderilir.
- `istek(...)` (iç) — asıl iş (aşağıda adım adım). `tamam` ya da `olmadi` `null` olabilir.
- `arkada(is)` — verilen `Runnable`'ı aynı havuzda çalıştırır; o kadar. Yorumunda "sonuç ana iş parçacığında" yazsa da
  hiçbir şeyi ana iş parçacığına taşımaz: işin içinden ekrana dokunacaksan `runOnUiThread`'i kendin çağırmalısın. Tek
  kullanıcısı [AnaEkran.md](AnaEkran.md)'deki `cikis` (sunucuya "anahtarı sil" ve "çıkış" der, cevabı beklemez).

## Kimle konuşur?

- Çağırdıkları:
  - [Api.md](Api.md) — `Api.oturumla(sunucu, yol, yontem, govde, anahtar)` (Bearer) ve `Api.Hata`.
  - `Ayarlar.sunucu(e)` — sunucu adresi (varsayılan `https://egitimevi.org`; aşağıda "Dikkat!").
  - `Oturum.anahtar(e)` — oturum anahtarı (girişte sunucunun verdiği `token`); oturum yoksa boş metin, o zaman `Api`
    `Authorization` başlığını koymaz.
  - [AnaEkran.md](AnaEkran.md) — `runOnUiThread`, `isFinishing()`, `isDestroyed()`, `genelHata(h)`, `bildir(ileti)`.
- Onu çağıranlar (grep) ve gittikleri uçlar (uçların belgeleri site deposunda):

  | Çağıran | Uç | Sunucu belgesi |
  |---|---|---|
  | [AnaEkran.md](AnaEkran.md) | `POST /api/cihaz` (uygulama anahtarı al), `GET /api/notifications` (zil rozeti); `arkada` (çıkış) | `sunucu/bolumler/cihaz.md`, `sunucu/bolumler/kayit.md` |
  | `Sayfa.java` (`Sayfa.Veri.yukle`) | alt sınıfın `adres()`'i: `BildirimlerSayfasi` → `GET /api/notifications`, `EkleSayfasi.Kod` → `GET /api/kisilikler` | `kayit.md`, `kisilik.md` |
  | `BildirimlerSayfasi.java` | `POST /api/notifications/read` | `kayit.md` |
  | `GirisSayfasi.java` | `GET /api/okul-adres/ara?q=`, `POST /api/login` | `kayit.md` |
  | `KodSayfasi.java` | `POST /api/login/dogrula`, `POST /api/login/tekrar` | `kayit.md` |
  | `KayitSayfasi.java` | `POST /api/register` | `kayit.md` |
  | `SifremiUnuttumSayfasi.java` | `POST /api/sifre-unuttum` | `kayit.md` |
  | `DogrulamaSorusu.java` | `GET /api/challenge` | `kayit.md` |
  | `KvkkSayfasi.java` | `POST /api/kvkk-onay` | `kayit.md` |
  | `SifreSayfasi.java` | `POST /api/password` | `kayit.md` |
  | `EkleSayfasi.java` | `POST /api/kisilik/cocuk`, `GET /api/me`, `POST /api/kisilik/kod`, `GET /api/site` | `kisilik.md`, `kayit.md`, `sunucu/site.md` |
  | `PortalSecici.java` | `GET /api/kisilikler`, `POST /api/kisilik/gec` | `sunucu/bolumler/kisilik.md` |

- Sunucu kapıları: `sunucu/api.md` — hangi uçta 403 `kvkkGerek` / `sifreDegismeli` / `rolsuz` döndüğü (`KVKK_SERBEST`,
  `SIFRE_SERBEST`, `ROLSUZ_SERBEST` listeleri).

## Nasıl çalışır (adım adım)?

```
ekran (ana iş parçacığı): Ag.post(e, "/api/password", govde, j -> {...}, h -> {...})
  istek
   1. sunucu = Ayarlar.sunucu(e), anahtar = Oturum.anahtar(e)   ← çağrı ANINDA okunur
   2. ARKA havuzunda:
        Api.oturumla(sunucu, yol, "POST", govde, anahtar)
          başarı            → cevap
          Api.Hata          → hata (sunucu 4xx/5xx ya da adres kuralı)
          başka IOException → hata = Hata(0, "İnternet bağlantısı yok ya da Eğitim Evi'ne ulaşılamadı.")
   3. e.runOnUiThread:
        ekran kapanıyor/yok edildi mi? → hiçbir şey yapma
        hata yok        → tamam.ok(cevap)                 (tamam null ise hiçbir şey)
        e.genelHata(h)  → true ise BURADA BİTER (401 oturumluyken, kvkkGerek, sifreDegismeli)
        olmadi varsa    → olmadi.olmadi(h)
        yoksa           → e.bildir(h.getMessage())        (altta 3,8 sn görünen kısa mesaj)
```

## Dikkat!

- **Sunucu adresi Aile ayar dosyasından gelir.** `Ayarlar.sunucu` "aile" adlı ayar dosyasını okur; yoksa
  `https://egitimevi.org`. Uygulamada bu adresi değiştiren bir ekran YOK: deneme paketinin ilk açılışta adres soran penceresi
  site-WebView dönemindeydi ve `e96c5f2 commit 6`'da kaldırıldı; bugün `Ayarlar.sunucuYaz` ve
  `UygulamaAyar.sunucuSecildi(c, true)`'nun hiç çağıranı yok. Adresi değiştiren tek yol [AileEkrani.md](AileEkrani.md):
  çocuğun telefonu başka bir adrese başarıyla bağlanınca `Ayarlar.baglan` o adresi yazar ve uygulamanın BÜTÜN ekranları da
  oraya gitmeye başlar; bağlantı kaldırılınca (`Ayarlar.cik`) adres yine kalır. Depodaki `README.md` hâlâ "deneme paketi ilk
  açılışta sunucu adresini sorar" diyor; eskidi. (Kod okumasına göre; telefonda denenmedi.)
- **Yorumdaki "rolsüz → portal ekle" yapılmıyor.** Sınıfın başındaki açıklama "okula bağlı değil (rolsüz) → portal ekle"
  durumunu da AnaEkran'ın karşıladığını söylüyor; oysa `genelHata` yalnız 401, `kvkkGerek` ve `sifreDegismeli`'ye bakar. 403
  `rolsuz` ("Hesabın henüz bir okula bağlı değil. Okul yönetimi seni ekleyince bu bölüm açılır.") ekranın `olmadi`'sine ya da
  alt mesaja düşer. Bugünkü ekranlar yalnız rolsüz kişiye açık uçları çağırdığı için görünür bir sorun yok; rol ekranları
  eklenince önem kazanır.
- **Genel durumları ekran ezemez.** `genelHata` `olmadi`'den ÖNCE çalışır: oturumluyken 401, `kvkkGerek` ve
  `sifreDegismeli` ekranın elinden alınır. 401 yalnız oturum açıkken (`Oturum.acik`) "oturum düştü" sayılır; bu yüzden giriş
  formunun 401'i ("Şifre yanlış.") ve iki adımlı kod ekranının 401'i (yanlış kod) ekranın kendisine ulaşır.
- **Değerler çağrı anında alınır.** Bir istek yoldayken çıkış yapılırsa istek eski anahtarla gider; cevabı çıkıştan sonra
  gelir. Başarıysa `tamam` yine çalışır (artık görünmeyen bir sayfaya çizer, zararsız); 401 gelirse oturum artık kapalı
  olduğu için "genel" sayılmaz, ekranın `olmadi`'sine ya da alt mesaja gider.
- **Sayfadan çıkınca istek iptal edilmez.** Geri tuşu ya da sekme değiştirmek isteği durdurmaz; cevap yalnız bütün ekran (`AnaEkran`) kapanıyorsa
  ya da yok edildiyse atılır. Sayfa yığında arkadaysa geri çağrı yine çalışır — sayfalar buna dayanıklı yazılmalı.
- **Havuz üç iş parçacıklı.** Aynı anda en çok üç istek yürür, fazlası sıraya girer. Bir istek bağlanmada 15, okumada 20
  saniyeye kadar bekleyebildiği için yavaş bir sunucuda kısa istekler (ör. zil rozeti) gecikebilir.
- **Yalnız `IOException` yakalanır.** Arka plandaki işte başka bir hata (`RuntimeException`) çıkarsa ne `tamam` ne `olmadi`
  çalışır ve Android'de yakalanmamış hata uygulamayı kapatır. Bugün `Api` yalnız `IOException` türleri fırlatıyor ve JSON
  ayrıştırma hatalarını kendi yakalıyor; yani bu yalnız yeni kod eklenirken akılda tutulacak bir kural.
- **Ağ hatası `durum = 0`'dır.** Ekran `h.durum == 0`'a bakarak "sunucuya ulaşılamadı"yı sunucu hatasından ayırabilir.
  Dikkat: sunucu adresi `Api.adresSorunu` kuralına takılan istek de `durum = 0` ile gelir (iletisi "İnternetteki sunucuya
  yalnızca https:// ile bağlanılır." gibi); yani 0 her zaman "internet yok" demek değildir.

## Testleri

- Android deposunda otomatik test yok; derleme ve lint (`./gradlew --offline assembleDebug lintDebug`) ana oturumda.
- Sunucunun bu dosyanın dayandığı cevapları site deposunda korunur: `testler/test-yonetim.js` (onayı eski kişiye 403 +
  `kvkkGerek`; şifresini değiştirmemiş kişiye 403 + `sifreDegismeli`), `testler/test-servis-yoklama.js` (şifre değişince ve
  çıkışta uygulama oturumu kapanır → telefondaki sonraki istek 401), `testler/test-admin-gizli.js` (rolsüz yetişkinin okul
  ucu 403 `rolsuz`), `testler/test-giris-kayit.js` (giriş hatalarındaki `alan` bayrakları: `kimlik`, `sifre`, `bot`) ve
  `testler/guvenlik-test.js` (doğrulama sorusu; hatalı denemelerden sonra 429 kilit).
- Elle (öykünücü ya da telefon):
  - Uçak kipini aç, "Bildirimler" sekmesine geç ve üst çubuktaki "Yenile"ye bas → "Bir sorun oldu", "İnternet bağlantısı
    yok ya da Eğitim Evi'ne ulaşılamadı." ve "Yeniden dene" (bu kutu `Sayfa.Veri`'nin `olmadi`'si).
  - Siteden aynı hesabın şifresini değiştir (öbür oturumlar kapanır), uygulamada Bildirimler'i yenile → "Oturumunun süresi
    doldu. Yeniden giriş yap." ve giriş ekranı.
  - Deneme sunucusunda aydınlatma metni sürümünü (`KVKK_SURUM`, site deposunda `sunucu/bolumler/kayit.md`) artır,
    uygulamada Bildirimler'i yenile → onay ekranı açılmalı.

## Son durum

- `git log`: 1 commit (Android deposu). Dosya `e96c5f2 commit 6` (2026-09-26) ile geldi: yerel uygulamanın çekirdeği
  (`AnaEkran`'ın WebView'den kendi çizdiği sayfalara geçmesi) yazılırken ekranların ortak istek kapısı olarak eklendi;
  o günden beri değişmedi.
- Bilinen açıklar (kod değiştirilmedi): sınıf yorumundaki "rolsüz" maddesinin karşılığı yok; `arkada`'nın yorumu yanıltıcı;
  uygulamada sunucu adresini seçmenin bir yolu yok (deneme paketi dahil).
- Planlı işlerden bu dosyaya dokunması beklenenler: "Android yerel uygulama" (bütün roller — rol ekranları rolsüz kapısına
  takılabilen uçları çağıracak, `rolsuz`'un genel karşılanması o zaman gerekecek; dosya yükleme/indirme bu kapıdan değil
  ayrı bir yoldan gidecek); "Sistem" işindeki bakım modu (sunucu bakımdayken her isteğin ortak bir cevabı olacaksa onu da
  büyük olasılıkla `genelHata` karşılayacak); "Çok dil" (isteklerin dil bilgisi taşıması).

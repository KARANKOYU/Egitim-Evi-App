# Eğitim Evi telefon uygulaması

[Eğitim Evi](https://github.com/KARANKOYU/Egitim-Evi) okul portalının müdür, öğretmen,
veli, öğrenci ve servisçi için yerel (native) Android uygulaması.

## Kısaca

- **Yereldir, WebView değildir.** Siteyi içinde açmaz; her ekranı Java koduyla kendisi kurar (XML ekran düzeni bile yok) ve
  sunucunun JSON uçlarıyla konuşur. `commit 5`'teki ilk tek uygulama siteyi içinde açan bir WebView'du; `commit 6`'da tamamen
  kaldırıldı.
- **Kimler kullanır?** Veli, öğretmen ve müdür aynı yetişkin hesabıyla girer ve portalları ("Öğretmen · okul", "Müdür ·
  okul", "Veli · çocuk") arasında geçer. Öğrencinin ve servisçinin hesabını okul açar; girişte okulunu seçer. Yöneticiye
  (`admin`) özel ekran yok.
- **Dış kütüphane yok:** AndroidX, Firebase, ağ ya da JSON kütüphanesi yok; yalnız Android'in kendi arayüzleri,
  `HttpURLConnection` ve `org.json` (`app/build.gradle`'da `dependencies` bloğu hiç yok). En düşük Android 8.0 (`minSdk 26`),
  hedef `targetSdk 36`. Yalnız Türkçe; açık ve koyu tema telefonun ayarını izler, renkler sitenin renkleridir.
- **Paket `org.egitimevi.aile`, sürüm 2.0.0 (`versionCode 3`), başlatıcıdaki adı "Eğitim Evi".** Uygulama önce yalnız
  çocuğun telefonu için "Eğitim Evi Aile" (1.0.x) olarak başladı. Sitenin kılavuzuna göre yayımda olan hâlâ 1.0.x; bu depodaki
  2.0.0 hazırlanıyor. Paket adı aynı kaldığı için 2.0.0, aynı imzayla telefondaki 1.0.x'in üstüne güncelleme olarak kurulur.

Uygulamanın baştan sona anlatımı (ekranlar, roller, sunucuyla konuşma, telefonda saklananlar, derleme, sürüm yayınlama,
bilinen açıklar, sözlük, kurallar, belge haritası) [TANITIM.md](TANITIM.md)'de. Bu sayfa onun özeti.

## Ne işe yarar?

### Ekranlar (bugün hazır olanlar)

| Ekran | Kim görür | Ne yapar |
|---|---|---|
| Giriş | herkes | E-posta ya da kullanıcı adı ve şifre. Öğrenci ve servisçi "okulunu seç" ile okulunu arar (en az 2 harf, en çok 6 sonuç); aynı kullanıcı adı başka okulda da olabilir. Hatalı denemeden sonra sunucu isterse doğrulama sorusu ("Doğrulama: 4 + 7 = ?") çıkar. |
| Giriş kodu | e-postası olan hesap (sunucuya göre öğrenci hariç) | İkinci adımda e-postaya gelen 6 haneli kod; altı hane yazılınca kendiliğinden gider, yeni kod 60 saniye sonra istenebilir. |
| Hesap aç | veli, öğretmen, müdür | Yetişkin hesabı: ad, kullanıcı adı, e-posta, telefon, şifre, isteğe bağlı T.C. no, aydınlatma onayı, doğrulama sorusu. Hesap e-postadaki bağlantıya 24 saat içinde tıklanınca açılır. |
| Şifremi unuttum ("Şifreni yenile") | yetişkin hesabı | E-postaya yenileme bağlantısı gider; bağlantı sitede açılır. Öğrenci ve servisçinin şifresini okul yeniler. |
| Aydınlatma metni | herkes, metin güncellenince | Beş maddelik özet, metnin tamamı tarayıcıda (`/kvkk/kvkk.html`); onaylamadan uygulama açılmaz, tek öbür yol çıkış. |
| Kendi şifreni belirle / Şifre değiştir | herkes | Okulun ya da yöneticinin verdiği şifreyle girene zorunlu hâli (çıkış dışında yol yok); Ayarlar'dan sıradan hâli. |
| Ana sayfa (sekme) | herkes | Tarih, günün saatine göre selam ("Günaydın, Ayşe"), rol ve okul. Portalı olmayan yetişkine "Henüz bir portalın yok" ve "+ Ekle"; birden çok portalı olan yetişkine portal kartları; öbür herkese "rolüne özel ekranlar hazırlanıyor" şeridi. |
| Bildirimler (sekme ve üstteki zil) | herkes | Gün gün liste ("Bugün", "Dün", "12 Eylül, Cuma"); okunmamışlar noktalı ve kalın. Sayfa açılınca hepsi okundu sayılır, zildeki sayı söner. |
| Ayarlar (sekme) | herkes | Profil; öğrencide kopyalanabilir "Veli kodun"; yetişkinde "Portallarım" ve "Ekle"; "Şifre değiştir"; "Telefon bildirimleri" (izin durumu); öğrencide "Bu telefonu velimle paylaş"; tarayıcıda açılan "Siteyi aç", "Aydınlatma metni", "Sık sorulan sorular"; onay soran "Çıkış yap"; en altta sürüm. |
| Portalların (sol üstteki avatar) | yetişkin hesabı (okul rolündeyken de) | Portal satırları; bulunulan portalda "Buradasın", onay bekleyende "Onay bekliyor". Dokunulan portala sunucu yeni oturum açar. Öğrencide ve servisçide avatar doğrudan Ayarlar'ı açar. |
| + Ekle | yetişkin hesabı | **Veli:** çocuğun 16 karakterlik veli koduyla çocuğunu ekler. **Öğretmen:** kişi kodunu okulunun müdürüne verir. **Müdür:** kişi kodunu yöneticiye verip okulunu açtırır (yöneticinin e-postası ve telefonu sitenin ayarından gelir). |
| Çocuğun telefonu | öğrenci (Ayarlar'dan) | Aşağıda, "Telefona özgü işler". |

**Henüz olmayanlar:** rollerin kendi ekranları (ödev, not, sınav, devamsızlık, servis yoklaması, mesajlar…). Ana sayfa bunu
açıkça söyler ("Bu sürümde rolüne özel ekranlar hazırlanıyor"); Ayarlar'daki "Siteyi aç" uygulamada olmayan işler için siteyi
tarayıcıda açar. Bildirimlerin bağlantıları da henüz kullanılmıyor: bildirim çubuğundan dokununca Bildirimler sayfası açılır,
listedeki bir bildirime dokununca bugün yalnız `#/profil` bağlantısı bir ekran (Ayarlar) açar.

### Telefona özgü işler

- **Telefon bildirimleri.** Uygulama Firebase kullanmaz; sunucu telefona bildirim itemez, telefon kendisi sorar. Android'in
  iş zamanlayıcısıyla 15 dakikada bir; okulun servis saatlerinde (Türkiye saatiyle sabah ve akşam aralığının 10 dakika
  öncesinden 60 dakika sonrasına kadar) yaklaşık dakikada bir, "servise bindi" gibi bildirimler gecikmesin diye. Yalnız son
  görülenden sonrakiler gelir (ilk yoklamada eskiler gösterilmez); uygulama öndeyken bildirim çubuğuna yazılmaz; dokununca
  uygulama Bildirimler sayfasıyla açılır. Telefon yeniden açılınca ya da uygulama güncellenince yoklama yeniden kurulur.
- **Servis seferi konumu.** Servisçinin seferi sürerken aracın konumunu velilere gönderen ön plan servisi hazır: araç son
  gönderilen noktadan 30 metreden fazla uzaklaşınca ya da 20 saniyede bir gönderir (iki gönderim arasında en az 5 saniye),
  bildirim çubuğunda "Sefer sürüyor" yazar, sunucu seferi kapatınca kendini durdurur. **Bugün onu başlatan bir ekran yok**
  (başlatan, WebView dönemindeki site köprüsüydü); servisçinin ekranları yazılınca seferi başlatan düğme onu çağıracak.
- **Çocuğun telefonu.** Öğrenci isterse telefonunun konumunu ve hangi uygulamayı ne kadar kullandığını (ekran süresi)
  velisiyle paylaşır: Ayarlar → "Bu telefonu velimle paylaş" → öğrenci kendi hesabıyla girer ve paylaşımı kendisi onaylar.
  Konum velinin seçtiği aralıkla alınır (varsayılan Wi-Fi'de 5, mobil veride 15 dakikada bir); internet yokken telefonda
  bekler, bağlanınca gider. Ekran süresi (dün ve bugün) 15 dakikada bir gider. Veli bunları sitede "Çocuğumun telefonu"
  sayfasında görür; okul görmez; sunucu verileri 7 gün sonra siler. Uygulama hiçbir uygulamayı kapatmaz ya da kilitlemez.

## Nasıl çalışır?

### Oturum

- Girişi uygulama kendisi yapar: `POST /api/login` gövdesinde `uygulama: true` gider, sunucu bu oturumu **30 gün** geçerli
  açar (tarayıcıdaki oturum 7 gün; ikisi de kullandıkça uzamaz). Cevap (`token`, `user`, `children`, `portallar`,
  `kapaliOzellikler`, `kvkkGuncel`…) telefonun "oturum" ayar dosyasına yazılır. Portal değiştirmek (`/api/kisilik/gec`) yeni
  bir oturum cevabıdır; aynı yoldan yazılır ve sekmeler baştan kurulur.
- Ekranlar isteklerini arka planda gönderir, cevap ana iş parçacığına döner. Oturumla ilgili genel durumları ekranlar tek tek
  düşünmez, ana ekran karşılar: 401 → oturum telefondan silinir, giriş ekranı gelir ("Oturumunun süresi doldu. Yeniden giriş
  yap."); `kvkkGerek` → aydınlatma metni onayı; `sifreDegismeli` → zorunlu şifre ekranı. İnternet yoksa ileti: "İnternet
  bağlantısı yok ya da Eğitim Evi'ne ulaşılamadı."
- **Çıkış:** önce uygulama anahtarı (`POST /api/cihaz/sil`), sonra oturum (`POST /api/logout`) sunucuda kapatılır; telefonda
  sefer durur, bildirim yoklaması iptal edilir, anahtar ve oturum silinir, giriş ekranı gelir. Çocuğun telefonunun bağlantısı
  çıkıştan etkilenmez (ayrı anahtar, ayrı dosya).

### Üç kimlik, üç başlık

| Kimlik | Başlık | Kim kullanır | Ne verir |
|---|---|---|---|
| Oturum anahtarı | `Authorization: Bearer <anahtar>` | Bütün ekranlar | Hesaba giriş; uygulamada 30 gün |
| Uygulama (cihaz) anahtarı | `X-Cihaz: <64 hex>` | Bildirim yoklaması, servis seferi, çıkış | Yalnız bildirim yoklama ve sefer konumu; hesaba giriş vermez |
| Aile anahtarı | `X-Aile-Cihaz: <anahtar>` | Çocuğun telefonu (ekranı ve servisi) | Yalnız çocuğun telefonunun konumu ve ekran süresi |

- **Uygulama anahtarı:** sekmeler kurulunca telefonda anahtar yoksa uygulama oturumla `POST /api/cihaz { ad: "<üretici>
  <model>", platform: "android", surum }` ister. 64 küçük onaltılık haneden oluşmayan cevap alınmaz; doğruysa saklanır,
  bildirim yoklaması kurulur ve Android 13 ve üstünde bildirim izni istenir. Sunucu anahtarın yalnız SHA-256 özetini tutar,
  hesap başına en çok 5 anahtar bırakır. Anahtar sunucuda silinirse (ör. şifre değişince) uygulama bunu ilk yoklamadaki
  401/403'ten öğrenir ve unutur; yenisini sekmeler yeniden kurulunca alır. Sunucu bu ucu bilmiyorsa uygulama sessizce
  bildirimsiz çalışır.
- **Aile anahtarı:** çocuğun telefonu ekranı, kendi "Sunucu adresi" kutusundaki sunucuya öğrencinin kullanıcı adı, şifresi,
  doğrulama cevabı ve isteğe bağlı okul adresiyle (`okul`) `POST /api/login` yapar (sunucu iki adımlı kod isterse ya da rol
  öğrenci değilse reddeder), gelen oturumla `POST /api/aile/cihaz { ad, platform, surum, onay: true }` ister, anahtarı,
  öğrencinin adını, velinin ayarlarını ve sunucu adresini "aile" ayar dosyasına yazar ve öğrencinin oturumunu hemen kapatır
  (`/api/logout`): telefonda yalnız konum ve süre gönderebilen anahtar kalır. Sunucu 401/403 derse (veli ya da öğrenci
  bağlantıyı kaldırdı) bağlantı ve bekleyen konumlar silinir.

### Sunucu adresi ve https

Sunucu adresi telefonda ("aile" ayar dosyasında) saklanır ve bütün uygulama için tektir: ekranlar, bildirim yoklaması, servis
seferi ve çocuğun telefonu aynı adrese gider. Varsayılan `https://egitimevi.org` (deneme paketinde de); onu değiştiren tek yer
çocuğun telefonunu bağlamaktır (aşağıda "Derleme"). İstek katmanı https dışını yalnız yerel ağ adreslerinde (`localhost`,
`10.`, `192.168.`, `172.16–31.`) kabul eder; yayın paketinde Android ayrıca her şifresiz bağlantıyı kapatır, deneme paketinde
açar (iki `ag_guvenligi.xml`: `app/src/main/res/xml/` ve `app/src/debug/res/xml/`). Bağlanmak için 15 saniye, cevap için 20
saniye beklenir.

### Kullanılan sunucu uçları

Hepsinin ayrıntısı site deposundaki ([KARANKOYU/Egitim-Evi](https://github.com/KARANKOYU/Egitim-Evi)) belgelerde; burada
belgelerine göre gruplanmış adları var:

- `sunucu/bolumler/kayit.md`: `POST /api/login`, `POST /api/login/dogrula`, `POST /api/login/tekrar`, `GET /api/challenge`,
  `GET /api/okul-adres/ara?q=`, `POST /api/register`, `POST /api/sifre-unuttum`, `POST /api/kvkk-onay`, `POST /api/password`,
  `POST /api/logout`, `GET /api/me`, `GET /api/notifications`, `POST /api/notifications/read`.
- `sunucu/bolumler/kisilik.md`: `GET /api/kisilikler`, `POST /api/kisilik/gec`, `POST /api/kisilik/cocuk`,
  `POST /api/kisilik/kod`.
- `sunucu/site.md`: `GET /api/site` ("+ Ekle > Müdür"de yöneticinin e-postası ve telefonu).
- `sunucu/bolumler/cihaz.md` (uygulama anahtarı): `POST /api/cihaz` (oturumla), `GET /api/cihaz/bildirimler[?son=<imleç>]`,
  `POST /api/cihaz/servis-konum`, `POST /api/cihaz/sil`.
- `sunucu/bolumler/aile.md` (çocuğun telefonu): `POST /api/aile/cihaz` (öğrencinin oturumuyla), `GET /api/aile/cihaz/ayar`,
  `POST /api/aile/cihaz/konum` (500'erli), `POST /api/aile/cihaz/kullanim`, `POST /api/aile/cihaz/sil`.

Uygulamadan tarayıcıda açılan site sayfaları: `/` ("Siteyi aç"), `/kvkk/kvkk.html` (aydınlatma metni), `/sss/sss.html` (sık
sorulan sorular); "+ Ekle > Müdür"de yöneticiye `mailto:` ve `tel:` bağlantıları. Hangi ekranın hangi ucu çağırdığı
[TANITIM.md](TANITIM.md)'nin 4. bölümündeki tabloda.

### Telefonda saklananlar

| Yer | Sınıf | İçinde |
|---|---|---|
| "oturum" ayar dosyası | `Oturum` | Oturum anahtarı, hesap bilgisi, çocuklar, portallar, kapalı bölümler, aydınlatma onayı, velinin seçtiği çocuk |
| "uygulama" ayar dosyası | `UygulamaAyar` | Uygulama anahtarı, bildirim imleci, servis saatleri, süren sefer |
| "aile" ayar dosyası | `Ayarlar` | Sunucu adresi, Aile anahtarı, öğrencinin adı, velinin aralıkları ve konum/ekran süresi açık mı, son konum/gönderim/sorun |
| `kuyruk.json` (uygulamanın özel klasörü) | `Kuyruk` | Gönderilmeyi bekleyen konumlar (en çok 5000; 7 günden eskisi atılır) |

Hepsi uygulamaya özeldir, başka uygulama okuyamaz; hiçbiri buluta yedeklenmez ve yeni telefona taşınmaz (manifestte
`allowBackup="false"`, `app/src/main/res/xml/` altındaki iki kural dosyası). Şifreler telefonda tutulmaz; öğrencinin oturumu
çocuğun telefonunda kalmaz.

## Dosyalar

Bütün kod tek pakette: `app/src/main/java/org/egitimevi/aile/` (32 Java sınıfı). Her `.java` dosyasının yanında aynı adlı bir
`.md` belgesi var; hepsi aynı yedi bölümle yazılır (ne yapar, içinde neler var, kimle konuşur, adım adım, dikkat, testleri,
son durum).

| Kat | Dosya | Görevi | Belgesi |
|---|---|---|---|
| İskelet | `AnaEkran.java` | Başlatıcıdan açılan tek ana ekran: üst çubuk, sayfa, alt sekmeler; sekme başına sayfa yığını, geri tuşu, oturum akışı (giriş → onay → şifre → sekmeler), genel hatalar, kısa mesaj, çıkış, uygulama anahtarı alma, zildeki sayı | [AnaEkran.md](app/src/main/java/org/egitimevi/aile/AnaEkran.md) |
| İskelet | `Sayfa.java` | Her ekranın atası; `Sayfa.Veri`: sunucudan veri isteyen sayfa (yükleniyor → içerik ya da hata ve "Yeniden dene") | [Sayfa.md](app/src/main/java/org/egitimevi/aile/Sayfa.md) |
| İskelet | `Sekmeler.java` | Alt çubuğun sekmeleri (bugün herkese Ana sayfa, Bildirimler, Ayarlar) ve bildirim bağlantısının açacağı ekran | [Sekmeler.md](app/src/main/java/org/egitimevi/aile/Sekmeler.md) |
| Görünüş | `Arayuz.java` | Arayüz parçaları: yazı, başlık, kart, düğme, form alanı, liste satırı, avatar, etiket, boş durum, yükleniyor, hata kutusu, şerit | [Arayuz.md](app/src/main/java/org/egitimevi/aile/Arayuz.md) |
| Görünüş | `Tema.java` | Köşe yarıçapları, yazı tipleri, dp, renk, zeminler, avatar rengi ve baş harfler | [Tema.md](app/src/main/java/org/egitimevi/aile/Tema.md) |
| Görünüş | `Zaman.java` | Türkiye saatiyle tarih ve saat yazımı ("5 dk önce", "Dün 09:10"), gün başlıkları, selam | [Zaman.md](app/src/main/java/org/egitimevi/aile/Zaman.md) |
| Sunucu | `Api.java` | HTTP + JSON (`HttpURLConnection`), üç kimlik başlığı, https kuralı (`adresSorunu`), hata türü `Api.Hata` | [Api.md](app/src/main/java/org/egitimevi/aile/Api.md) |
| Sunucu | `Ag.java` | Ekranların istek kapısı: üç iş parçacıklı arka plan havuzu, cevap ana iş parçacığında, genel hatalar ana ekrana | [Ag.md](app/src/main/java/org/egitimevi/aile/Ag.md) |
| Saklananlar | `Oturum.java` | Oturum anahtarı ve hesap bilgisi ("oturum" ayar dosyası), velinin seçtiği çocuk | [Oturum.md](app/src/main/java/org/egitimevi/aile/Oturum.md) |
| Saklananlar | `UygulamaAyar.java` | Uygulama anahtarı, bildirim imleci, servis saatleri, süren sefer ("uygulama" ayar dosyası) | [UygulamaAyar.md](app/src/main/java/org/egitimevi/aile/UygulamaAyar.md) |
| Saklananlar | `Ayarlar.java` | Sunucu adresi ve çocuğun telefonu bağlantısı: Aile anahtarı, öğrencinin adı, velinin aralıkları, son durum ("aile" ayar dosyası) | [Ayarlar.md](app/src/main/java/org/egitimevi/aile/Ayarlar.md) |
| Saklananlar | `Kuyruk.java` | Gönderilmeyi bekleyen konumlar (`kuyruk.json`) | [Kuyruk.md](app/src/main/java/org/egitimevi/aile/Kuyruk.md) |
| Giriş ve hesap | `GirisSayfasi.java` | Giriş: kimlik ve şifre, okul arama, doğrulama sorusu, iki adımlıya geçiş | [GirisSayfasi.md](app/src/main/java/org/egitimevi/aile/GirisSayfasi.md) |
| Giriş ve hesap | `KodSayfasi.java` | İki adımlı girişin 6 haneli kodu ve "Yeni kod gönder" | [KodSayfasi.md](app/src/main/java/org/egitimevi/aile/KodSayfasi.md) |
| Giriş ve hesap | `KayitSayfasi.java` | Hesap aç (yetişkin hesabı); site sayfalarını tarayıcıda açan `siteAc` | [KayitSayfasi.md](app/src/main/java/org/egitimevi/aile/KayitSayfasi.md) |
| Giriş ve hesap | `SifremiUnuttumSayfasi.java` | Şifre yenileme bağlantısı isteme | [SifremiUnuttumSayfasi.md](app/src/main/java/org/egitimevi/aile/SifremiUnuttumSayfasi.md) |
| Giriş ve hesap | `DogrulamaSorusu.java` | Formlara gömülen doğrulama sorusu | [DogrulamaSorusu.md](app/src/main/java/org/egitimevi/aile/DogrulamaSorusu.md) |
| Giriş ve hesap | `KvkkSayfasi.java` | Aydınlatma metni onayı; metni tarayıcıda açan `metniAc` | [KvkkSayfasi.md](app/src/main/java/org/egitimevi/aile/KvkkSayfasi.md) |
| Giriş ve hesap | `SifreSayfasi.java` | Şifre değiştir (sıradan ve zorunlu) | [SifreSayfasi.md](app/src/main/java/org/egitimevi/aile/SifreSayfasi.md) |
| Sekmelerdeki sayfalar | `AnaSayfa.java` | "Ana sayfa" sekmesi; rol kodunun Türkçe adı (`rolAdi`) | [AnaSayfa.md](app/src/main/java/org/egitimevi/aile/AnaSayfa.md) |
| Sekmelerdeki sayfalar | `BildirimlerSayfasi.java` | "Bildirimler" sayfası; okundu yapma ve zildeki sayı | [BildirimlerSayfasi.md](app/src/main/java/org/egitimevi/aile/BildirimlerSayfasi.md) |
| Sekmelerdeki sayfalar | `AyarlarSayfasi.java` | "Ayarlar" sayfası | [AyarlarSayfasi.md](app/src/main/java/org/egitimevi/aile/AyarlarSayfasi.md) |
| Sekmelerdeki sayfalar | `PortalSecici.java` | "Portalların" penceresi ve portala geçiş | [PortalSecici.md](app/src/main/java/org/egitimevi/aile/PortalSecici.md) |
| Sekmelerdeki sayfalar | `EkleSayfasi.java` | "+ Ekle": Veli (veli kodu), Öğretmen ve Müdür (kişi kodu) | [EkleSayfasi.md](app/src/main/java/org/egitimevi/aile/EkleSayfasi.md) |
| Sekmelerdeki sayfalar | `KisiKodu.java` | 16 karakterlik kişi/veli kodu: 4'erli tireli biçim, yapıştırılan metinden ayıklama, tireyi kendiliğinden koyan kutu, kopyalanabilir gösterim | [KisiKodu.md](app/src/main/java/org/egitimevi/aile/KisiKodu.md) |
| Arka plan | `Bildirimler.java` | Bildirim yoklamasının zamanlanması (15 dakika; servis saatinde yaklaşık dakikada bir), yoklama ve bildirim çubuğu | [Bildirimler.md](app/src/main/java/org/egitimevi/aile/Bildirimler.md) |
| Arka plan | `BildirimIsi.java` | Yoklamayı ayrı iş parçacığında çalıştıran `JobService` | [BildirimIsi.md](app/src/main/java/org/egitimevi/aile/BildirimIsi.md) |
| Arka plan | `BaslatmaAlici.java` | Telefon açılınca ve uygulama güncellenince çocuğun telefonu servisini ve bildirim yoklamasını yeniden kurar, yarım kalan seferi unutur | [BaslatmaAlici.md](app/src/main/java/org/egitimevi/aile/BaslatmaAlici.md) |
| Arka plan | `SeferServisi.java` | Servisçinin sefer konumunu gönderen ön plan servisi (bugün başlatan ekran yok) | [SeferServisi.md](app/src/main/java/org/egitimevi/aile/SeferServisi.md) |
| Çocuğun telefonu | `AileEkrani.java` | "Çocuğun telefonu" ekranı (ayrı Activity): bağlama, izinler, durum, bağlantıyı kaldırma | [AileEkrani.md](app/src/main/java/org/egitimevi/aile/AileEkrani.md) |
| Çocuğun telefonu | `IzlemeServisi.java` | Çocuğun telefonunun ön plan servisi: dakikalık tur, konum, ekran süresi, velinin ayarlarını tazeleme | [IzlemeServisi.md](app/src/main/java/org/egitimevi/aile/IzlemeServisi.md) |
| Çocuğun telefonu | `Kullanim.java` | Ekran süresinin hesabı: hangi uygulama önde kaç dakika (Android'in kullanım istatistikleri) | [Kullanim.md](app/src/main/java/org/egitimevi/aile/Kullanim.md) |

Kod dışındaki dosyalar (manifest, simgeler, renkler, ağ ve yedek kuralları, Gradle dosyaları) her klasörün `KLASOR.md`'sinde
anlatılır; kökteki için [KLASOR.md](KLASOR.md). Depodaki tek araç `araclar/simgeleri-uret.js` sitenin çizgi simgelerini
`drawable/ik_*.xml` çizimlerine çevirir ([araclar/simgeleri-uret.md](araclar/simgeleri-uret.md)). Bütün belgelerin listesi
[TANITIM.md](TANITIM.md)'nin 15. bölümünde (belge haritası).

## İzinler

Manifestteki (`app/src/main/AndroidManifest.xml`) izinlerin hepsi:

| İzin | Neden |
|---|---|
| `INTERNET` | Sunucuyla konuşmak. |
| `ACCESS_NETWORK_STATE` | Çocuğun telefonunda ağ türü (Wi-Fi, mobil veri, bağlantı yok): konum aralığı ve gönderim buna göre. Bildirim yoklama işleri de "ağ varken çalış" koşuluyla (`NETWORK_TYPE_ANY`) kurulur; Android 14'ü ve üstünü hedefleyen uygulamada iş zamanlayıcısı bu koşul için de bu izni ister. |
| `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` | Çocuğun telefonunun konumu (o ekran ister) ve servis seferi (izin yoksa sefer servisi başlamaz; bugün servisçiden bu izni isteyen bir ekran da yok). |
| `ACCESS_BACKGROUND_LOCATION` | Çocuğun telefonunda uygulama kapalıyken de konum ("Her zaman izin ver"; Android 10 ve üstü). |
| `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_LOCATION` | İki konum türünde ön plan servisi (servis seferi, çocuğun telefonu); çalışırken bildirim çubuğunda görünürler. |
| `POST_NOTIFICATIONS` | Android 13 ve üstünde telefon bildirimleri. Uygulama anahtarı alınınca istenir; Ayarlar'daki "Telefon bildirimleri"nden ve çocuğun telefonu ekranından da verilebilir. |
| `RECEIVE_BOOT_COMPLETED` | Telefon açılınca bildirim yoklamasını ve çocuğun telefonu servisini yeniden kurmak. |
| `PACKAGE_USAGE_STATS` | Ekran süresi. Uygulama bu izni isteyemez; öğrenci telefonun Ayarlar > Kullanım erişimi'nden açar (çocuğun telefonu ekranı o sayfayı açar). |

Ayrıca:

- **Pil:** izin değildir. Çocuğun telefonu ekranı uygulamanın ayar sayfasını açar, öğrenci Pil > "Kısıtlamasız"ı seçer
  (doğrudan "muaf tut" penceresi Play Store kuralına takıldığı için kullanılmaz).
- **`<queries>`:** başlatıcıda görünen uygulamaların adlarını okuyabilmek için (ekran süresi listesinde uygulamanın adı görünsün).
- Kamera, mikrofon, rehber ya da dosya izni yok. Kişisel veri işleyen yeni bir izin ya da özellik sitenin aydınlatma metnine de
  yazılır.

## Derleme

Android SDK (platform 36) gerekir. Gradle'ı **JDK 21** ile çalıştır (`JAVA_HOME`): kod Java 17 hedefiyle derlenir
(`app/build.gradle` → `compileOptions`), ama Gradle 8.14.3 çok yeni Java sürümlerinde (ör. 25) açılmaz. İlk derlemede
internet gerekir (Gradle 8.14.3 dağıtımı, Android eklentisi 8.13.0 ve araçları bir kez iner); sonra `--offline` ile ağa
çıkmadan derlenir. Windows'ta Git Bash'te `./gradlew`, PowerShell'de `.\gradlew.bat`.

```
./gradlew assembleDebug      # deneme paketi: app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease    # imzalı paket:  app/build/outputs/apk/release/app-release.apk
./gradlew bundleRelease      # Play Store:    app/build/outputs/bundle/release/app-release.aab
./gradlew lintDebug          # lint denetimi: hedef 0 hata, 0 uyarı (rapor app/build/reports/ altında)
./gradlew --stop             # bitince Gradle'ın arka plan sürecini kapat
```

**Deneme paketi ile yayın paketi.** İkisi de varsayılan olarak `https://egitimevi.org`'a bağlanır. Deneme paketi Android'in
deneme anahtarıyla kendiliğinden imzalanır ve yerel ağdaki sunucuya şifresiz (http) bağlantıya izin verir. Öykünücüden
bakınca bilgisayardaki deneme sunucusunun adresi `http://10.0.2.2:3200`'dür (öykünücüde `10.0.2.2` bilgisayarın kendisidir;
site deposunun testleri sunucuyu `3200`'de açar). **Dikkat:** bugün uygulamada sunucu adresini seçen bir ekran yok
(WebView dönemindeki "ilk açılışta sunucu adresini sor" penceresi `commit 6`'da kalktı). Adresi değiştiren tek yer çocuğun
telefonu ekranındaki "Sunucu adresi" kutusudur (bağlama başarılı olunca yazılır); o ekrana da ancak varsayılan sunucuda
öğrenci olarak girilmişken Ayarlar'dan ulaşılır. Yani yeni kurulmuş bir deneme paketi kendi ekranlarıyla deneme sunucusuna
bugün bağlanamaz (ayrıntı [TANITIM.md](TANITIM.md)'nin 9. bölümünde).

**Yayın imzası** deponun dışındaki `../Egitim-Evi-App-imza/imza.properties` dosyasından okunur (`app/build.gradle`); dosyadaki
alanlar `storeFile`, `storePassword`, `keyAlias`, `keyPassword`. Dosya yoksa yayın paketi imzasız çıkar
(`app-release-unsigned.apk`) ve telefona kurulamaz; deneme paketi her zaman derlenir. Hep aynı anahtarla imzala: anahtar
değişirse kurulu uygulamalar güncellenemez. İmza dosyaları ve paketler (`*.apk`, `*.aab`) depoya girmez (`.gitignore`);
paketler GitHub Releases'e yüklenir, sitenin indirme sayfası (`/indir/indir.html`) listeyi oradan alır. Sürüm çıkarmanın
adımları (`versionCode`, `versionName`, sürüm etiketi, APK adı kuralları) [TANITIM.md](TANITIM.md)'nin 10. bölümünde.

**Test:** depoda otomatik test yok; kodun denetimi Android lint'tir. Uygulamanın dayandığı sunucu sözleşmesini site
deposundaki testler korur: `testler/test-giris-kayit.js`, `testler/test-servis-yoklama.js` (cihaz anahtarı, bildirim yoklama,
servis konumu, 30 günlük uygulama oturumu), `testler/test-aile.js` (çocuğun telefonu), `testler/test-kisi-kodu.js`,
`testler/test-bildirim.js`, `testler/test-uygulama-surum.js` (indirme sayfasının sürüm listesi). Elle deneme turu
[TANITIM.md](TANITIM.md)'nin 9. bölümünde.

## Telif ve kullanım

© 2026 Eğitim Evi yapımcıları ([katkıda bulunanlar](https://github.com/KARANKOYU/Egitim-Evi/blob/main/CONTRIBUTING.md)). **Tüm hakları saklıdır.**

Bu depodaki kod, tasarım ve belgeler yalnız incelenebilsin diye herkese açıktır; bu bir açık kaynak lisansı DEĞİLDİR. Yazılı izin
olmadan kopyalanamaz, değiştirilemez, dağıtılamaz, başka bir sunucuda çalıştırılamaz ve bu kodla başka bir site ya da uygulama
yayınlanamaz. "Eğitim Evi" adı ve logosu da izinsiz kullanılamaz. GitHub'ın kuralları gereği herkese açık bir depo GitHub içinde
görüntülenebilir ve çatallanabilir (fork); bu, kodu kullanma izni vermez. İzin için proje sahibine GitHub'dan yaz
([@KARANKOYU](https://github.com/KARANKOYU)).

Copyright © 2026 the Eğitim Evi authors. All rights reserved. This repository is public for viewing only and is not open source:
no permission is granted to copy, modify, distribute, host or deploy this code, in whole or in part, without written permission.

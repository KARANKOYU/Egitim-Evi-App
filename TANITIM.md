# Eğitim Evi Android uygulaması — tanıtım

Merhaba. Bu dosya Eğitim Evi'nin Android uygulamasına ilk kez bakan birine, yani sana, uygulamayı baştan sona anlatır: ne
olduğunu, hangi ekranların kimlere göründüğünü, sunucuyla nasıl konuştuğunu, telefonda neyi sakladığını, bildirimleri
nasıl getirdiğini, konumu ne zaman gönderdiğini, nasıl derlenip yayımlandığını ve her dosyanın açıklamasının nerede
olduğunu.

Uygulama tek başına bir şey değildir; Eğitim Evi okul portalının telefon yüzüdür. Sunucu, veritabanı ve site ayrı depoda:
[KARANKOYU/Egitim-Evi](https://github.com/KARANKOYU/Egitim-Evi). Uygulamanın kullandığı her `/api/...` ucunun ayrıntısı o
deponun belgelerindedir (ör. site deposunda `sunucu/bolumler/kayit.md`); bu dosya onları adıyla anar. Kullanıcının gözünden
anlatım site deposundaki `belge/KILAVUZ.md`'nin "Eğitim Evi telefon uygulaması" bölümünde.

Her Java dosyasının yanında aynı adlı bir `.md` durur (`AnaEkran.java` → `AnaEkran.md`); hepsi aynı yedi bölümle yazılır (ne
yapar, içinde neler var, kimle konuşur, adım adım, dikkat, testleri, son durum). Kod dışı dosyaları olan her klasörde bir
`KLASOR.md` var. En sondaki **Belge haritası** hepsini listeler.

## İçindekiler

1. [Bu uygulama nedir?](#1-bu-uygulama-nedir)
2. [Depo nasıl dizilmiş?](#2-depo-nasıl-dizilmiş)
3. [Ekranlar ve roller](#3-ekranlar-ve-roller)
4. [Sunucuyla konuşma](#4-sunucuyla-konuşma)
5. [Oturum ve anahtarlar](#5-oturum-ve-anahtarlar)
6. [Bildirim yoklaması](#6-bildirim-yoklaması)
7. [Konum: servis seferi ve çocuğun telefonu](#7-konum-servis-seferi-ve-çocuğun-telefonu)
8. [Telefonda ne saklanır?](#8-telefonda-ne-saklanır)
9. [Derleme ve deneme](#9-derleme-ve-deneme)
10. [Sürüm yayınlama](#10-sürüm-yayınlama)
11. [Bilinen açıklar](#11-bilinen-açıklar)
12. [Sözlük](#12-sözlük)
13. [Kurallar](#13-kurallar)
14. [Nereden devam?](#14-nereden-devam)
15. [Belge haritası](#15-belge-haritası)

---

## 1. Bu uygulama nedir?

Eğitim Evi'nin müdür, öğretmen, veli, öğrenci ve servisçi için tek Android uygulaması: başlatıcıda adı **Eğitim Evi**.

- **Yereldir (native), WebView değildir.** Siteyi içinde açmaz; her ekranı Java koduyla kendisi kurar (XML ekran düzeni bile
  yok) ve sunucunun JSON uçlarıyla konuşur. İlk sürümlerden birinde (`commit 5`) siteyi içinde açan bir WebView vardı;
  `commit 6`'da tamamen kaldırıldı.
- **Dış kütüphane yok.** AndroidX yok, Firebase yok, ağ ya da JSON kütüphanesi yok: yalnız Android'in kendi arayüzleri,
  `HttpURLConnection` ve `org.json`. `app/build.gradle`'da `dependencies` bloğu hiç yok.
- **Android 8.0 ve üstü** (`minSdk 26`), hedef `targetSdk 36`. Yalnız Türkçe. Açık ve koyu tema telefonun ayarını izler;
  renkler sitenin renkleridir.
- **Paket adı `org.egitimevi.aile`, sürüm 2.0.0 (`versionCode 3`).** Uygulama önce yalnız çocuğun telefonu için "Eğitim Evi
  Aile" olarak başladı (1.0.x). Sitenin kılavuzuna göre yayımda olan hâlâ 1.0.x; bu depodaki 2.0.0 hazırlanıyor. Paket adı
  aynı kaldığı için 2.0.0, telefondaki 1.0.x'in üstüne güncelleme olarak kurulur ve çocuğun telefonunun bağlantısı korunur.
- **Bugün hazır olanlar:** giriş (iki adımlı kod dahil), hesap açma, şifremi unuttum, aydınlatma metni onayı, zorunlu şifre
  değişikliği, portallar ve portal değiştirme, "+ Ekle" (çocuk ekle, öğretmen olarak katıl, okulunu açtır), bildirimler
  (uygulama içinde ve telefonun bildirim çubuğunda), ayarlar, çocuğun telefonu (konum ve ekran süresini veliyle paylaşma).
- **Henüz olmayanlar:** rollerin kendi ekranları (ödev, not, sınav, devamsızlık, servis yoklaması, mesajlar…). Ana sayfa
  bunu açıkça söyler ("Bu sürümde rolüne özel ekranlar hazırlanıyor"); Ayarlar'daki "Siteyi aç" uygulamada olmayan işler için
  siteyi tarayıcıda açar.

Kodun tamamı 32 Java sınıfı, yaklaşık 4.200 satır.

## 2. Depo nasıl dizilmiş?

```
Egitim-Evi-App/
├─ build.gradle, settings.gradle, gradle.properties   Gradle proje tanımı              → KLASOR.md
├─ gradlew, gradlew.bat, gradle/wrapper/              Gradle sarmalayıcısı (8.14.3)    → gradle/wrapper/KLASOR.md
├─ README.md (eskidi, bkz. 11), .gitignore
├─ TANITIM.md                                         bu dosya
├─ araclar/simgeleri-uret.js                          sitenin simgelerini çevirir      → araclar/simgeleri-uret.md
└─ app/                                               tek modül; build.gradle          → app/KLASOR.md
   └─ src/
      ├─ main/AndroidManifest.xml                     izinler, ekranlar, servisler     → app/src/main/KLASOR.md
      ├─ main/java/org/egitimevi/aile/*.java (+.md)   bütün kod                        → 15. Belge haritası
      ├─ main/res/drawable/                           56 vektör simge                  → .../drawable/KLASOR.md
      ├─ main/res/values/, values-night/              renkler (açık/koyu), tema, ad    → .../values/KLASOR.md, .../values-night/KLASOR.md
      ├─ main/res/xml/                                ağ güvenliği, yedek kuralları    → .../xml/KLASOR.md
      ├─ main/res/mipmap-anydpi/                      başlatıcı simgesi                → .../mipmap-anydpi/KLASOR.md
      └─ debug/res/xml/                               deneme paketinde http izni       → app/src/debug/res/xml/KLASOR.md
```

Kod kat kat düşünülebilir (hepsi tek pakette, `org.egitimevi.aile`):

| Kat | Sınıflar | İşi |
|---|---|---|
| İskelet | `AnaEkran`, `Sayfa`, `Sekmeler` | Tek ekran, sayfa yığınları, sekmeler, geri tuşu, oturum akışı |
| Görünüş | `Arayuz`, `Tema`, `Zaman` | Hazır arayüz parçaları, renk ve ölçüler, Türkçe tarih/saat |
| Sunucu | `Api`, `Ag` | HTTP + JSON, kimlik başlıkları, arka plan iş parçacığı, genel hatalar |
| Telefonda saklananlar | `Oturum`, `UygulamaAyar`, `Ayarlar`, `Kuyruk` | Oturum, uygulama anahtarı, çocuğun telefonu bağlantısı, bekleyen konumlar |
| Giriş ve hesap | `GirisSayfasi`, `KodSayfasi`, `KayitSayfasi`, `SifremiUnuttumSayfasi`, `DogrulamaSorusu`, `KvkkSayfasi`, `SifreSayfasi` | Girişten sekmelere kadar her şey |
| Sekmelerdeki sayfalar | `AnaSayfa`, `BildirimlerSayfasi`, `AyarlarSayfasi`, `PortalSecici`, `EkleSayfasi`, `KisiKodu` | Ana sayfa, bildirimler, ayarlar, portallar, "+ Ekle" |
| Arka plan | `Bildirimler`, `BildirimIsi`, `BaslatmaAlici`, `SeferServisi` | Bildirim yoklaması, açılışta yeniden kurma, servis seferi konumu |
| Çocuğun telefonu | `AileEkrani`, `IzlemeServisi`, `Kullanim` (+ `Ayarlar`, `Kuyruk`) | Bağlama, izinler, konum ve ekran süresi gönderimi |

## 3. Ekranlar ve roller

### Tek ekran, sayfa yığınları

Uygulamanın asıl tek ekranı (Activity) [AnaEkran](app/src/main/java/org/egitimevi/aile/AnaEkran.md)'dır. Üstte başlık
çubuğu (sol üstte geri düğmesi ya da kişinin avatarı, başlık, "Yenile", bildirim zili ve okunmamış sayısı), ortada o anki
sayfa, altta sekmeler. Her ekran bir [Sayfa](app/src/main/java/org/egitimevi/aile/Sayfa.md)'dır; her sekmenin kendi
sayfa yığını var. `git(sayfa)` yenisini üste koyar, geri tuşu sırayla: sayfa kendisi karşılarsa onu (`geriBas`), yığında
önceki sayfa varsa ona, ana sekmede değilsek ana sekmeye, yoksa uygulamadan çıkar. Seçili sekmeye yeniden dokunmak o
sekmenin başına döner.

Giriş, aydınlatma onayı ve zorunlu şifre "giriş kipi"nde açılır: üst ve alt çubuk gizli, kendi yığınları var.

```
uygulama açıldı ─► oturum var mı? ── hayır ─► Giriş ──(e-postalı hesap)──► Giriş kodu
                       │                       ├─ "Şifremi unuttum" ─► Şifreni yenile
                       │                       └─ "Hesap aç" ────────► Hesap aç
                       evet
                       ├─ aydınlatma onayı güncel değil ─► Aydınlatma metni (onay ya da çıkış)
                       ├─ şifresini değiştirmeli        ─► Kendi şifreni belirle (ya da çıkış)
                       └─► sekmeler: Ana sayfa | Bildirimler | Ayarlar
```

### Sekmeler ve sayfalar

Bugün herkesin alt çubuğunda aynı üç sekme var ([Sekmeler](app/src/main/java/org/egitimevi/aile/Sekmeler.md)); rol ekranları
yazıldıkça role göre genişleyecek (sitedeki menünün karşılığı).

- **Ana sayfa** ([AnaSayfa](app/src/main/java/org/egitimevi/aile/AnaSayfa.md)): bugünün tarihi, günün saatine göre selam
  ("Günaydın, Ayşe"), rol ve okul. Portalı olmayan yetişkin hesabına "Henüz bir portalın yok" ve "+ Ekle"; birden çok portalı
  olan yetişkin hesabına portal kartları; öbür herkese "rolüne özel ekranlar hazırlanıyor" şeridi.
- **Bildirimler** ([BildirimlerSayfasi](app/src/main/java/org/egitimevi/aile/BildirimlerSayfasi.md)): bildirimler gün gün
  ("Bugün", "Dün", "12 Eylül, Cuma"), okunmamışlar solda noktalı ve kalın; sayfa açılınca hepsi okundu sayılır, zil rozeti
  söner. Aynı sayfa üst çubuktaki zile dokununca da açılır.
- **Ayarlar** ([AyarlarSayfasi](app/src/main/java/org/egitimevi/aile/AyarlarSayfasi.md)): profil kartı; öğrencide "Veli
  kodun" (kopyalanabilir, [KisiKodu](app/src/main/java/org/egitimevi/aile/KisiKodu.md)); yetişkinde "Portallarım" ve
  "Ekle"; herkese "Şifre değiştir", "Telefon bildirimleri" (izin durumu), öğrencide "Bu telefonu velimle paylaş"; "Siteyi
  aç", "Aydınlatma metni", "Sık sorulan sorular" (tarayıcıda); "Çıkış yap" (onaylı) ve en altta sürüm ("Eğitim Evi 2.0.0").

Sol üstteki avatar: yetişkin hesabında (ya da onun bir okul rolündeyken) "Portalların" penceresini açar
([PortalSecici](app/src/main/java/org/egitimevi/aile/PortalSecici.md)): "Öğretmen · okul", "Müdür · okul", "Veli · çocuk"
satırları, bulunduğun portalda "Buradasın", onay bekleyende "Onay bekliyor"; dokunulan portala sunucu yeni oturum açar.
Altta "+ Ekle" ([EkleSayfasi](app/src/main/java/org/egitimevi/aile/EkleSayfasi.md)): **Veli** (çocuğun 16 karakterlik veli
koduyla çocuğunu ekler), **Öğretmen** (kişi kodunu okulunun müdürüne verir), **Müdür** (kişi kodunu yöneticiye verip okulunu
açtırır; yöneticinin e-postası ve telefonu sitenin ayarından gelir). Öğrenci ve servisçide avatar doğrudan Ayarlar'ı açar.

### Giriş ve hesap ekranları

- **Giriş** ([GirisSayfasi](app/src/main/java/org/egitimevi/aile/GirisSayfasi.md)): e-posta ya da kullanıcı adı ve şifre.
  Öğrenci ve servisçi "okulunu seç" ile okulunu arar (en az 2 harf, yazmayı bırakınca 350 ms sonra, en çok 6 sonuç), çünkü
  aynı kullanıcı adı başka okulda da olabilir. Hatalı denemeden sonra sunucu isterse doğrulama sorusu çıkar
  ([DogrulamaSorusu](app/src/main/java/org/egitimevi/aile/DogrulamaSorusu.md), "Doğrulama: 4 + 7 = ?").
- **Giriş kodu** ([KodSayfasi](app/src/main/java/org/egitimevi/aile/KodSayfasi.md)): e-postası olan hesaba ikinci adımda 6
  haneli kod gider (sunucuya göre öğrenci hariç); altı hane yazılınca kendiliğinden gönderilir, yeni kod 60 saniye sonra
  istenebilir.
- **Hesap aç** ([KayitSayfasi](app/src/main/java/org/egitimevi/aile/KayitSayfasi.md)): veli, öğretmen ve müdür aynı
  yetişkin hesabını açar (ad, kullanıcı adı, e-posta, telefon, şifre, isteğe bağlı T.C. no, aydınlatma onayı, doğrulama
  sorusu); hesap e-postadaki bağlantıya 24 saat içinde tıklanınca açılır. Öğrenci ve servisçi hesabını okul açar.
- **Şifreni yenile** ([SifremiUnuttumSayfasi](app/src/main/java/org/egitimevi/aile/SifremiUnuttumSayfasi.md)): yetişkin
  hesabının e-postasına yenileme bağlantısı; bağlantı sitede açılır.
- **Aydınlatma metni** ([KvkkSayfasi](app/src/main/java/org/egitimevi/aile/KvkkSayfasi.md)): metin güncellenince herkesten
  yeniden onay istenir; beş maddelik özet, metnin tamamı sitede (`/kvkk/kvkk.html`), onay ya da çıkış.
- **Şifre değiştir** ([SifreSayfasi](app/src/main/java/org/egitimevi/aile/SifreSayfasi.md)): Ayarlar'dan sıradan hâli; okulun
  ya da yöneticinin verdiği şifreyle girene zorunlu hâli ("Kendi şifreni belirle", çıkış dışında yol yok).

### Çocuğun telefonu

[AileEkrani](app/src/main/java/org/egitimevi/aile/AileEkrani.md) ayrı bir Activity'dir (1.0.x'ten kalma, kendi sabit renkleriyle
çizilir). Öğrenci Ayarlar'da "Bu telefonu velimle paylaş"a dokununca açılır. Bağlı değilken öğrenci kendi hesabıyla girer
ve "Konumumun ve ekran süremin velimle paylaşılacağını okudum, kabul ediyorum" kutusunu işaretler; bağlıyken izinleri
(konum — her zaman, kullanım erişimi, bildirim, pil kısıtlaması yok) ve son gönderim durumunu gösterir, bağlantıyı kaldırır.
Veli bunları sitede "Çocuğumun telefonu" sayfasında görür; okul görmez; sunucu verileri 7 gün sonra siler. Uygulama hiçbir
uygulamayı kapatmaz ya da kilitlemez.

### Roller

Kişinin rolü sunucunun verdiği `user.role`'dür ([AnaSayfa](app/src/main/java/org/egitimevi/aile/AnaSayfa.md)'daki `rolAdi`
Türkçe adını verir).

| Rol | Nasıl girer | Bugün uygulamada ne görür |
|---|---|---|
| Öğrenci (`student`) | Okulunu seçer, okulun verdiği kullanıcı adı ve şifreyle; iki adımlı kod yok | Üç sekme; Ayarlar'da veli kodu ve "Bu telefonu velimle paylaş" |
| Servisçi (`servisci`) | Okulunu seçer, kullanıcı adıyla | Üç sekme. Sefer konumu gönderen servis hazır ama onu başlatan ekran henüz yok (7. bölüm) |
| Veli (`parent`), Öğretmen (`teacher`), Müdür (`principal`) — yetişkin hesabı | E-posta ya da kullanıcı adıyla; e-postası varsa iki adımlı kod | Üç sekme; avatar → portallar; "+ Ekle"; portal değiştirince yeni oturum |
| Yönetici (`admin`) | — | `rolAdi` "Yönetici" diye tanır; uygulamada ona özel ekran yok |

Rol satırı (`user.rolSatiri`) ve yetişkin (`user.yetiskin`) bayrakları sunucudan gelir: yetişkin hesabının bir okuldaki
öğretmen/müdür rolüne geçmiş hâlinde de portallar ve "+ Ekle" görünür.

## 4. Sunucuyla konuşma

Bütün istekler en alttaki [Api](app/src/main/java/org/egitimevi/aile/Api.md)'den geçer: `HttpURLConnection`, JSON gövde
(`Content-Type: application/json; charset=utf-8`), `Accept: application/json`, bağlanmak için 15 sn, cevap için 20 sn.
4xx/5xx cevaplar `Api.Hata`'ya çevrilir: `durum` (HTTP kodu; adres kuralına uymayan adres için 0; bağlantı kopması
`Api`'den düz `IOException` olarak çıkar, ekranlarda `Ag` onu da `durum` 0'lı bir `Api.Hata`'ya çevirir), ileti (sunucunun
`error`'u ya da "Sunucu hatası (kod)") ve cevabın tamamı (`govde`: `alan`, `kvkkGerek`, `sifreDegismeli`…).

**Adres kuralı:** `Api.adresSorunu` https dışını yalnız yerel ağ adreslerinde (`localhost`, `10.`, `192.168.`,
`172.16–31.`) kabul eder; yayın paketinde Android ayrıca her şifresiz bağlantıyı kapatır
([app/src/main/res/xml/KLASOR.md](app/src/main/res/xml/KLASOR.md)). Sunucu adresi [Ayarlar](app/src/main/java/org/egitimevi/aile/Ayarlar.md)'dan gelir;
varsayılan `https://egitimevi.org`.

**Üç kimlik, üç başlık:**

| Kimlik | Başlık | Kim kullanır | Ne verir |
|---|---|---|---|
| Oturum anahtarı | `Authorization: Bearer <anahtar>` | Bütün ekranlar ([Ag](app/src/main/java/org/egitimevi/aile/Ag.md) üzerinden) | Hesaba giriş |
| Uygulama anahtarı | `X-Cihaz: <64 hex>` | [Bildirimler](app/src/main/java/org/egitimevi/aile/Bildirimler.md), [SeferServisi](app/src/main/java/org/egitimevi/aile/SeferServisi.md), çıkış | Yalnız bildirim yoklama ve sefer konumu |
| Aile anahtarı | `X-Aile-Cihaz: <anahtar>` | [IzlemeServisi](app/src/main/java/org/egitimevi/aile/IzlemeServisi.md), [AileEkrani](app/src/main/java/org/egitimevi/aile/AileEkrani.md) | Yalnız çocuğun telefonunun konum ve ekran süresi |

**Ekranlar** isteği doğrudan `Api`'ye değil [Ag](app/src/main/java/org/egitimevi/aile/Ag.md)'ye verir: istek üç iş parçacıklı
bir havuzda gider, cevap ana iş parçacığına döner. Oturumla ilgili genel durumları ekran düşünmez, `AnaEkran.genelHata`
karşılar: 401 → oturum biter, "Oturumunun süresi doldu. Yeniden giriş yap."; `kvkkGerek` → Aydınlatma metni sayfası;
`sifreDegismeli` → zorunlu şifre sayfası. Öbür hataları ekran ister kendisi gösterir (ör. kutunun altında kırmızı), vermezse
altta birkaç saniye görünen kısa mesaj çıkar. İnternet yoksa ileti: "İnternet bağlantısı yok ya da Eğitim Evi'ne
ulaşılamadı."

### Kullanılan uçlar

Hepsi site deposunda belgelidir; "Belgesi" sütunu o depodaki dosyayı söyler.

| Uç | Kimlik | Uygulamada kim | Belgesi (site deposu) |
|---|---|---|---|
| `POST /api/login` | — | Giriş (`kimlik`, `password`, `uygulama: true`, varsa `okul` ve doğrulama); çocuğun telefonunu bağlarken öğrenci girişi | `sunucu/bolumler/kayit.md` |
| `POST /api/login/dogrula` | — | Giriş kodu (`challengeId`, `code`, `uygulama: true`) | `sunucu/bolumler/kayit.md` |
| `POST /api/login/tekrar` | — | Giriş kodu: "Yeni kod gönder" | `sunucu/bolumler/kayit.md` |
| `GET /api/challenge` | — | Doğrulama sorusu; çocuğun telefonu ekranı | `sunucu/bolumler/kayit.md` |
| `GET /api/okul-adres/ara?q=` | — | Giriş: okul arama | `sunucu/bolumler/kayit.md` |
| `POST /api/register` | — | Hesap aç | `sunucu/bolumler/kayit.md` |
| `POST /api/sifre-unuttum` | — | Şifreni yenile | `sunucu/bolumler/kayit.md` |
| `POST /api/kvkk-onay` | Bearer | Aydınlatma metni onayı | `sunucu/bolumler/kayit.md` |
| `POST /api/password` | Bearer | Şifre değiştir (sıradan ve zorunlu) | `sunucu/bolumler/kayit.md` |
| `POST /api/logout` | Bearer | Çıkış; çocuğun telefonunu bağladıktan hemen sonra öğrencinin oturumunu kapatmak | `sunucu/bolumler/kayit.md` |
| `GET /api/me` | Bearer | "+ Ekle > Veli": çocuk eklenince hesap bilgisini tazelemek | `sunucu/bolumler/kayit.md` |
| `GET /api/notifications` | Bearer | Bildirimler sayfası; zil rozeti (`unread`) | `sunucu/bolumler/kayit.md` |
| `POST /api/notifications/read` | Bearer | Bildirimler sayfası açılınca hepsini okundu yapmak | `sunucu/bolumler/kayit.md` |
| `GET /api/kisilikler` | Bearer | Portal listesi (oturumda yoksa); "+ Ekle > Öğretmen/Müdür": kişi kodu | `sunucu/bolumler/kisilik.md` |
| `POST /api/kisilik/gec` | Bearer | Portala geçiş (yeni oturum) | `sunucu/bolumler/kisilik.md` |
| `POST /api/kisilik/cocuk` | Bearer | "+ Ekle > Veli": veli koduyla çocuk ekleme | `sunucu/bolumler/kisilik.md` |
| `POST /api/kisilik/kod` | Bearer | "Yeni kod üret" | `sunucu/bolumler/kisilik.md` |
| `GET /api/site` | Bearer | "+ Ekle > Müdür": yöneticinin e-postası ve telefonu | `sunucu/site.md` |
| `POST /api/cihaz` | Bearer | Girişten sonra uygulama anahtarı almak | `sunucu/bolumler/cihaz.md` |
| `GET /api/cihaz/bildirimler[?son=<imleç>]` | `X-Cihaz` | Bildirim yoklaması | `sunucu/bolumler/cihaz.md` |
| `POST /api/cihaz/servis-konum` | `X-Cihaz` | Servisçinin sefer konumu | `sunucu/bolumler/cihaz.md` |
| `POST /api/cihaz/sil` | `X-Cihaz` | Çıkışta uygulama anahtarını iptal | `sunucu/bolumler/cihaz.md` |
| `POST /api/aile/cihaz` | Bearer (öğrenci) | Çocuğun telefonunu bağlamak (`onay: true`) | `sunucu/bolumler/aile.md` |
| `GET /api/aile/cihaz/ayar` | `X-Aile-Cihaz` | Velinin seçtiği aralıklar ve açık/kapalı ayarlar | `sunucu/bolumler/aile.md` |
| `POST /api/aile/cihaz/konum` | `X-Aile-Cihaz` | Bekleyen konumlar (500'erli) | `sunucu/bolumler/aile.md` |
| `POST /api/aile/cihaz/kullanim` | `X-Aile-Cihaz` | Dünün ve bugünün ekran süresi | `sunucu/bolumler/aile.md` |
| `POST /api/aile/cihaz/sil` | `X-Aile-Cihaz` | "Bu telefonun bağlantısını kaldır" | `sunucu/bolumler/aile.md` |

"—" olan uçlar oturum açılmadan çağrılır. Sunucu tarafında `X-Cihaz` ve `X-Aile-Cihaz` uçları oturum kapılarından önce
yönlendirilir (site deposunda `sunucu/api.js`). Sunucunun `GET /api/cihaz/ayar` ucu (servisçinin açık seferi) var ama uygulama
bugün onu çağırmıyor.

Uygulamadan tarayıcıda açılan site sayfaları: `/` ("Siteyi aç"), `/kvkk/kvkk.html` (aydınlatma metni), `/sss/sss.html`
(sık sorulan sorular); "+ Ekle > Müdür"de yöneticiye `mailto:` ve `tel:` bağlantıları.

## 5. Oturum ve anahtarlar

### Oturum

Girişte uygulama gövdeye `uygulama: true` koyar; sunucu bu oturumu **30 gün** geçerli açar (tarayıcıdaki oturum 7 gün; ikisi
de kullandıkça uzamaz). Gelen cevap (`token`, `user`, `children`, `portallar`, `kapaliOzellikler`, `kvkkGuncel`, `kvkkSurum`,
`kisilikSec`, `cocuk`) [Oturum](app/src/main/java/org/egitimevi/aile/Oturum.md) ile telefonun "oturum" ayar dosyasına yazılır;
velinin seçtiği çocuk da orada. Portal değiştirmek (`/api/kisilik/gec`) yeni bir oturum cevabıdır, aynı yoldan yazılır ve
sekmeler baştan kurulur. Oturum açıkken ekranların herhangi bir isteğine sunucu 401 derse oturum telefondan silinir, giriş
ekranı gelir.

### Uygulama anahtarı (`X-Cihaz`)

Sekmeler kurulunca `AnaEkran.cihazKaydet`, telefonda anahtar yoksa `POST /api/cihaz { ad: "<üretici> <model>", platform:
"android", surum: "<versionName>" }` ister. Gelen `cihazAnahtari` 64 küçük onaltılık hane değilse alınmaz; doğruysa
[UygulamaAyar](app/src/main/java/org/egitimevi/aile/UygulamaAyar.md) ile "uygulama" ayar dosyasına yazılır, bildirim
yoklaması kurulur ve Android 13+'da bildirim izni istenir. Sunucu anahtarın yalnız SHA-256 özetini tutar, hesap başına en
çok 5 anahtar bırakır; anahtar **hesaba giriş vermez**, yalnız bildirim yoklamaya ve sefer konumu göndermeye yarar. Sunucu
ucu bilmiyorsa (eski sunucu) uygulama sessizce bildirimsiz çalışır.

Anahtar sunucuda silinirse (şifre değişince hesabın bütün anahtarları silinir; aydınlatma onayı güncel değilse anahtarla
gelen istek anahtarı siler) uygulama bunu ilk yoklamada 401/403 ile öğrenir ve unutur; yenisini sekmeler yeniden kurulunca
alır ([AnaEkran](app/src/main/java/org/egitimevi/aile/AnaEkran.md)).

### Aile anahtarı (`X-Aile-Cihaz`)

Çocuğun telefonu ayrı bir anahtarla konuşur, uygulamanın girişinden bağımsızdır. [AileEkrani](app/src/main/java/org/egitimevi/aile/AileEkrani.md)
öğrencinin kullanıcı adı, şifresi ve doğrulama cevabıyla `POST /api/login` yapar (rol öğrenci değilse reddeder), gelen
oturumla `POST /api/aile/cihaz { ad, platform, surum, onay: true }` ister, cevaptaki anahtarı, öğrencinin adını ve
ayarları [Ayarlar](app/src/main/java/org/egitimevi/aile/Ayarlar.md) ile "aile" ayar dosyasına yazar ve öğrencinin oturumunu
**hemen kapatır** (`/api/logout`): telefonda yalnız konum ve süre gönderebilen anahtar kalır. Sunucu öğrenci başına en çok 3
telefon bağlar.

### Çıkış

"Çıkış yap" (`AnaEkran.cikis`): arka planda önce `POST /api/cihaz/sil` (uygulama anahtarıyla), sonra `POST /api/logout`
(oturumla); telefonda servis seferi durur, bildirim yoklaması iptal edilir, uygulama anahtarı ve oturum silinir, giriş
ekranı gelir. Çocuğun telefonunun bağlantısı çıkıştan etkilenmez (ayrı anahtar, ayrı dosya); onu yalnız "Bu telefonun
bağlantısını kaldır" ya da velinin siteden kaldırması bitirir.

## 6. Bildirim yoklaması

Uygulama Firebase kullanmaz; sunucu telefona bildirim itemez. Onun yerine telefon sunucuya kendisi sorar
([Bildirimler](app/src/main/java/org/egitimevi/aile/Bildirimler.md), [BildirimIsi](app/src/main/java/org/egitimevi/aile/BildirimIsi.md)):

- **Düzenli iş (no 101):** Android'in iş zamanlayıcısıyla 15 dakikada bir (Android'in izin verdiği en sık düzenli iş),
  herhangi bir ağ varken, telefon yeniden açılsa da süren (`setPersisted`).
- **Hızlı iş (no 102):** okulun servis saatlerindeysek bir dakika sonra (en geç üç dakikada) tek seferlik yoklama; her
  yoklama bittiğinde yenisi kurulur, böylece servis saatlerinde yaklaşık dakikada bir sorulur ("servise bindi" gibi
  bildirimler gecikmesin). Servis saatleri sunucudan gelir (sabah ve akşam aralığı); Türkiye saatiyle aralığın 10 dakika
  öncesinden 60 dakika sonrasına kadar "servis saati" sayılır.
- **Yoklama:** `GET /api/cihaz/bildirimler?son=<imleç>` (uygulama anahtarıyla). Sunucu yalnız son görülenden sonrakileri
  verir (bir seferde en çok 20). İlk yoklamada (imleç yok) eski bildirimler gösterilmez, yalnız imleç alınır. Uygulama
  öndeyken bildirim çubuğuna yazılmaz. Yeni imleç ve servis saatleri saklanır. 401/403 gelirse anahtar unutulur, işler
  iptal edilir.
- **Gösterme:** "Bildirimler" kanalı, başlık "Eğitim Evi", metin bildirimin metni; dokununca uygulama açılır ve Bildirimler
  sayfası gelir.

```
JobScheduler ─► BildirimIsi (ayrı iş parçacığı) ─► Bildirimler.yokla ─► GET /api/cihaz/bildirimler?son=…
     ▲                                                   ├─ yeni bildirimler ─► bildirim çubuğu (uygulama öndeyse değil)
     └──── Bildirimler.zamanla(zincir) ◄─────────────────┴─ imleç + servis saatleri ─► "uygulama" ayar dosyası
```

Telefon yeniden açılınca ya da uygulama güncellenince [BaslatmaAlici](app/src/main/java/org/egitimevi/aile/BaslatmaAlici.md)
yoklamayı yeniden kurar. Uygulamanın içinde ise zil rozeti `GET /api/notifications`'ın `unread` sayısını gösterir (ekran
öne gelince tazelenir).

## 7. Konum: servis seferi ve çocuğun telefonu

İki ayrı ön plan servisi var; ikisi de Android'in kuralı gereği çalışırken bildirim çubuğunda görünür ve konum türündedir.

### Servis seferi ([SeferServisi](app/src/main/java/org/egitimevi/aile/SeferServisi.md))

Servisçinin seferi sürerken aracın konumunu velilere gitsin diye gönderir: GPS ve ağ konumunu en az 5 saniye ve 10 metre
arayla dinler; araç son gönderilen noktadan 30 metreden fazla uzaklaştıysa ya da son gönderimden 20 saniye geçtiyse (iki
gönderim arasında en az 5 saniye) `POST /api/cihaz/servis-konum { seferId, enlem, boylam, dogruluk }` (uygulama
anahtarıyla). Araç dururken de 20 saniyede bir son konum yeniden gider. Sunucu seferi kapatınca (409),
sefer yoksa (404) ya da anahtar geçersizse (401/403) kendini durdurur. Bildirimi "Sefer sürüyor", kanalı "Servis seferi".

**Bugün onu başlatan bir ekran yok.** `SeferServisi.baslat`'ı WebView dönemindeki site köprüsü çağırıyordu; köprü
`commit 6`'da kalktı. Servisçinin ekranları yazılınca (planlı) seferi başlatan düğme bunu çağıracak. Çıkış ve telefonun
yeniden açılması yarım kalmış seferi unutturur.

### Çocuğun telefonu ([IzlemeServisi](app/src/main/java/org/egitimevi/aile/IzlemeServisi.md))

Telefon bağlıysa ve konum izni varsa sürekli çalışır (sistem kapatırsa yeniden başlar). Dakikada bir tur atar:

- Konum aralığını ağ türüne göre ayarlar: Wi-Fi'deyken velinin seçtiği `wifiDk` (varsayılan 5), mobil veride ya da
  bağlantısızken `mobilDk` (varsayılan 15) dakikada bir. Gelen konum (enlem, boylam, doğruluk, zaman, ağ türü, pil yüzdesi)
  önce kuyruğa girer ([Kuyruk](app/src/main/java/org/egitimevi/aile/Kuyruk.md): `kuyruk.json`, en çok 5000 konum, 7 günden
  eskisi atılır).
- İnternet varsa kuyruktaki konumları 500'erli gönderir (`/api/aile/cihaz/konum`), gönderileni siler.
- 15 dakikada bir dünün ve bugünün ekran süresini gönderir (`/api/aile/cihaz/kullanim`;
  [Kullanim](app/src/main/java/org/egitimevi/aile/Kullanim.md): hangi uygulama önde kaç dakika; ana ekran, sistem arayüzü ve
  Eğitim Evi sayılmaz).
- 30 dakikada bir velinin ayarlarını tazeler (`/api/aile/cihaz/ayar`).
- Sunucu 401/403 derse (veli ya da öğrenci bağlantıyı kaldırdı) bağlantıyı ve kuyruğu siler, kendini durdurur.

Bildirimi "Eğitim Evi Aile — Konumun ve ekran süren velinle paylaşılıyor", kanalı "Aile paylaşımı"; dokununca çocuğun
telefonu ekranı açılır.

## 8. Telefonda ne saklanır?

| Yer | Sınıf | İçinde |
|---|---|---|
| "oturum" ayar dosyası | [Oturum](app/src/main/java/org/egitimevi/aile/Oturum.md) | Oturum anahtarı, hesap bilgisi, çocuklar, portallar, kapalı bölümler, aydınlatma onayı, seçili çocuk |
| "uygulama" ayar dosyası | [UygulamaAyar](app/src/main/java/org/egitimevi/aile/UygulamaAyar.md) | Uygulama anahtarı, bildirim imleci, servis saatleri, süren sefer |
| "aile" ayar dosyası | [Ayarlar](app/src/main/java/org/egitimevi/aile/Ayarlar.md) | Sunucu adresi, Aile anahtarı, öğrencinin adı, velinin aralıkları, son konum/gönderim/sorun |
| `kuyruk.json` (uygulamanın özel klasörü) | [Kuyruk](app/src/main/java/org/egitimevi/aile/Kuyruk.md) | Gönderilmeyi bekleyen konumlar |

Hepsi uygulamaya özeldir, başka uygulama okuyamaz. **Hiçbiri yedeklenmez ve yeni telefona taşınmaz**: manifestte
`allowBackup="false"` ve iki kural dosyası ([app/src/main/res/xml/KLASOR.md](app/src/main/res/xml/KLASOR.md)). Neden: bir
anahtar başka telefona kopyalanırsa o telefon, örneğin çocuğun yerine konum gönderebilirdi. Yeni telefonda kişi yeniden
girer, çocuğun telefonu yeniden bağlanır.

Ayrıca: şifreler telefonda tutulmaz; öğrencinin oturumu çocuğun telefonunda kalmaz; yayın paketi yalnız https ile konuşur;
uygulama reklam ya da analiz için hiçbir yere veri göndermez (dış kütüphane yok).

## 9. Derleme ve deneme

Bu bölümdeki komutlar bu belge yazılırken **çalıştırılmadı**; projenin derleme düzenini anlatır.

**Gerekenler:** Android SDK (platform 36), Gradle'ı çalıştırmak için **JDK 21** (kod Java 17 hedefiyle derlenir; Gradle
8.14.3 çok yeni Java sürümlerinde, ör. 25'te, açılmaz), internet ilk derlemede (Gradle 8.14.3 dağıtımı, Android eklentisi
8.13.0 ve araçları bir kez iner). Ayrıntı: [KLASOR.md](KLASOR.md), [app/KLASOR.md](app/KLASOR.md),
[gradle/wrapper/KLASOR.md](gradle/wrapper/KLASOR.md).

```
JAVA_HOME=<JDK 21 klasörü> ./gradlew --offline assembleDebug lintDebug
   ─► app/build/outputs/apk/debug/app-debug.apk   (deneme paketi)
   ─► lint raporu app/build/reports/ altında       (hedef: 0 hata, 0 uyarı)
./gradlew --stop                                   (bitince Gradle'ın arka plan sürecini kapat)
```

`--offline` her şey bir kez indikten sonra ağa hiç çıkmadan derler. Windows'ta Git Bash'te `./gradlew`, PowerShell'de
`.\gradlew.bat`.

**Deneme paketi ile yayın paketi:** deneme paketi Android'in deneme anahtarıyla kendiliğinden imzalanır ve http'ye izin
verir ([app/src/debug/res/xml/KLASOR.md](app/src/debug/res/xml/KLASOR.md)); öykünücüden bilgisayardaki deneme sunucusuna
`http://10.0.2.2:3200` ile gidilir (öykünücüde `10.0.2.2` bilgisayarın kendisidir; site deposunun testleri sunucuyu `3200`'de
açar). **Dikkat:** bugün uygulamada sunucu adresini seçen bir ekran yok; uygulama varsayılan olarak `https://egitimevi.org`'a
gider. Adresi değiştiren tek yol çocuğun telefonu ekranındaki sunucu kutusudur ([Ayarlar](app/src/main/java/org/egitimevi/aile/Ayarlar.md)),
o ekrana da ancak varsayılan sunucuda öğrenci olarak girilmişken Ayarlar'dan ulaşılır. Yani yeni kurulmuş bir deneme paketi
uygulamanın kendi ekranlarıyla deneme sunucusuna bugün bağlanamaz
([app/src/debug/res/xml/KLASOR.md](app/src/debug/res/xml/KLASOR.md)).

**Elle deneme turu** (her Java dosyasının `.md`'sindeki "Testleri" bölümü kendi adımlarını yazar):

1. İlk açılış → giriş ekranı, çubuklar gizli. Yanlış şifreyle bir kez dene → sunucu `soruGerekli` der, doğrulama sorusu
   çıkmalı.
2. E-postalı bir yetişkin hesabıyla gir → giriş kodu ekranı; kod gelince sekmeler. Avatar → portallar; "+ Ekle".
3. Okulun açtığı bir öğrenci hesabıyla gir (okulunu seçerek) → Ayarlar'da veli kodu, "Bu telefonu velimle paylaş".
4. Sekmeler arasında gez, geri tuşunu dene (iç sayfa → kök → ana sekme → uygulama kapanır).
5. Uygulama arkadayken siteden bir bildirim üret, bir sonraki yoklamayı bekle (15 dk; servis saatinde ~1 dk), bildirime
   dokun → Bildirimler sayfası.
6. Telefonu koyu temaya al, uygulamayı yeniden aç; renkler koyu olmalı.
7. Ayarlar → "Çıkış yap" → giriş ekranı; telefon bildirimleri durmalı.

**Otomatik test:** depoda yok. Uygulamanın dayandığı sunucu sözleşmesini site deposundaki testler korur:
`testler/test-giris-kayit.js`, `testler/test-servis-yoklama.js` (cihaz anahtarı, bildirim yoklama, servis konumu, 30 günlük
uygulama oturumu), `testler/test-aile.js` (çocuğun telefonu), `testler/test-kisi-kodu.js`, `testler/test-bildirim.js`,
`testler/test-uygulama-surum.js` (indirme sayfasının sürüm listesi).

## 10. Sürüm yayınlama

Paketler depoya girmez (`.gitignore`: `*.apk`, `*.aab`); GitHub Releases'e yüklenir ve site indirme sayfasını oradan
doldurur.

1. `app/build.gradle`'da `versionCode`'u bir artır (tam sayı; telefon küçük ya da eşit sayıyı güncelleme olarak kurmaz) ve
   `versionName`'i yaz (ör. `2.0.1`).
2. Yayın paketini imzalı derle: `./gradlew assembleRelease` (APK, `app/build/outputs/apk/release/`) ve Play Store için
   `./gradlew bundleRelease` (AAB, `app/build/outputs/bundle/release/`). İmza deponun dışındaki yerel imza dosyalarından
   okunur (yerel imza dosyaları, depoya girmez); yoksa paket imzasız çıkar ve telefona kurulamaz. Hep aynı anahtarla imzala:
   anahtar değişirse kurulu uygulamalar güncellenemez.
3. Bu depoda GitHub'da yeni bir sürüm (Release) aç ve APK'yı ekle. Sitenin okuyabilmesi için (site deposunda
   `sunucu/uygulama-surum.js`):
   - etiket `v2.0.1` ya da `2.0.1` biçiminde (iki ile dört sayı, `v` isteğe bağlı) olmalı;
   - taslak ya da ön sürüm (pre-release) olmamalı;
   - APK'nın dosya adı yalnız harf, rakam, `.`, `_`, `-` içermeli (boşluk ve Türkçe harf yok), uzantısı `.apk`, boyutu
     500 MB'tan küçük;
   - sürüm notu düz metne çevrilip 400 karaktere kısaltılır; sürüm adı 90 karaktere.
   Site listeyi 15 dakika saklar, en çok 30 sürüm gösterir (sürüm numarasına göre en yeni üstte), GitHub verdiyse APK'nın
   SHA-256 özetini de yazar. İndirme sayfası `/indir/indir.html`.
4. README'yi ve gerekiyorsa bu dosyayı güncelle; commit mesajı yalnız `commit N` (Android deposu kendi numarasını sayar).

Play Store bağlantısı sitenin ayarlarındandır (site deposunda `sunucu/site.js`); uygulamanın kendisi sürüm denetimi
yapmaz (planlı, 14. bölüm).

## 11. Bilinen açıklar

Belgeleme sırasında koda bakılarak bulunanlar; hiçbiri için kod değiştirilmedi. Ayrıntıları ilgili `.md`'lerde.

- **Eski yazılar:** `README.md` ve manifestin baş yorumu WebView dönemini anlatıyor ([KLASOR.md](KLASOR.md),
  [app/src/main/KLASOR.md](app/src/main/KLASOR.md)); kök `build.gradle`'ın yorumu uygulamayı hâlâ "çocuğun telefonunda
  çalışan küçük uygulama" diye anlatıyor; `UygulamaAyar` yorumu oturumun "WebView'de" kaldığını, `Oturum` yorumu anahtarın 7
  gün geçerli olduğunu söylüyor (uygulama oturumu 30 gün). WebView döneminden kalan başka sınıf yorumları:
  [SeferServisi](app/src/main/java/org/egitimevi/aile/SeferServisi.md) ("site köprüyle (`Kopru.seferBasladi`) bunu açar"),
  [AileEkrani](app/src/main/java/org/egitimevi/aile/AileEkrani.md) ("uygulamanın ana ekranı site"),
  [Bildirimler](app/src/main/java/org/egitimevi/aile/Bildirimler.md) ("site kendi bildirimlerini gösterir");
  [Ag](app/src/main/java/org/egitimevi/aile/Ag.md)'nin yorumundaki "rolsüz → portal ekle" ise `AnaEkran.genelHata`'da yok.
- **Servis seferini başlatan ekran yok** (7. bölüm).
- **Deneme paketinde sunucu adresi seçilemiyor** (9. bölüm; [Ayarlar](app/src/main/java/org/egitimevi/aile/Ayarlar.md),
  [app/src/debug/res/xml/KLASOR.md](app/src/debug/res/xml/KLASOR.md)): deneme sunucusuna uygulamanın ekranlarıyla
  bağlanmanın yolu yok.
- **"Hesap aç" metni "+ Ekle"yi "sağ üstte" diye anlatıyor**; uygulamada "+ Ekle" sol üstteki avatarın penceresinde, Ayarlar'da
  ve portalsız ana sayfada ([KayitSayfasi](app/src/main/java/org/egitimevi/aile/KayitSayfasi.md)).
- **Bildirim bağlantıları kullanılmıyor:** bildirim çubuğundan dokununca yalnız Bildirimler sayfası açılır; listede bir
  bildirime dokununca yalnız `#/profil` bir ekran açar ([Sekmeler](app/src/main/java/org/egitimevi/aile/Sekmeler.md)).
- **Kök sayfada geri uygulamadan çıkar** (planlı "Android geri tuşu" işi değiştirecek).
- **Uygulama açıkken tema değişimi** var olan görünümleri yeniden boyamıyor ([Arayuz](app/src/main/java/org/egitimevi/aile/Arayuz.md)).
- **Çocuğun telefonu ekranı** koyu temaya uymuyor ve döndürünce yazılanlar gidiyor ([AileEkrani](app/src/main/java/org/egitimevi/aile/AileEkrani.md)).
- **Ekran süresi** yalnız dün ve bugün için gönderiliyor (sınıf yorumu 7 gün diyor); manifestteki `<queries>`'te ana ekran
  niyeti olmadığı için Android 11+'da başlatıcı listede görünebilir ([Kullanim](app/src/main/java/org/egitimevi/aile/Kullanim.md)).
- **Gradle dağıtımının özeti denetlenmiyor** (`distributionSha256Sum` yok; [gradle/wrapper/KLASOR.md](gradle/wrapper/KLASOR.md)).
- **Koyu renklerde sitenin koyu temasından dört fark** (`gri_zemin`, `bordo`, harita renkleri, `perde`; hepsi bugün hiçbir
  ekranda görünmeyen renkler: harita ve perde hiç kullanılmıyor, gri ve bordo yalnız henüz kullanılmayan etiket türlerinde;
  [app/src/main/res/values-night/KLASOR.md](app/src/main/res/values-night/KLASOR.md)).

## 12. Sözlük

| Sözcük | Anlamı |
|---|---|
| **yerel (native) uygulama** | Ekranlarını kendisi çizen uygulama; siteyi içinde açan (WebView) uygulamanın tersi |
| **sayfa** | Uygulamadaki bir ekran (`Sayfa` sınıfı); `AnaEkran` sayfaları yığında tutar |
| **yığın** | Her sekmenin açılmış sayfaları; geri tuşu üsttekini kapatır |
| **sekme** | Alt çubuktaki "Ana sayfa", "Bildirimler", "Ayarlar" |
| **giriş kipi** | Üst ve alt çubuğun gizli olduğu giriş, onay ve zorunlu şifre ekranları |
| **portal** | Yetişkinin girebildiği her kişilik: "Öğretmen · okul", "Müdür · okul", "Veli · çocuk" |
| **yetişkin hesabı** | Kendi kendine açılan hesap (veli, öğretmen, müdür); e-posta, şifre ve kişi kodu burada |
| **rol satırı** | Yetişkinin bir okuldaki öğretmen ya da müdür rolü; portal değiştirince oturum ona açılır |
| **kişi kodu / veli kodu** | 16 karakterlik kod, ekranda 4'erli tireli (`Ab3#-kQx9-+mPt-7?zR`); yetişkin müdüre ya da yöneticiye verir, öğrencinin veli kodu veliye |
| **oturum anahtarı** | Girişte sunucunun verdiği anahtar (`Bearer`); uygulamada 30 gün geçerli |
| **uygulama anahtarı** | `X-Cihaz`; 64 hane; yalnız bildirim yoklama ve sefer konumu; hesaba giriş vermez |
| **Aile anahtarı** | `X-Aile-Cihaz`; çocuğun telefonunun yalnız konum ve ekran süresi göndermeye yarayan anahtarı |
| **imleç** | Son görülen bildirimin yeri; yoklama yalnız ondan sonrakileri ister |
| **servis saatleri** | Okulun sabah ve akşam servis aralığı; o saatlerde bildirim daha sık yoklanır |
| **sefer** | Servisçinin bir servis saatindeki yolculuğu; sürerken konumu velilere gider |
| **ön plan servisi** | Bildirim çubuğunda görünerek arka planda sürekli çalışan Android servisi |
| **iş zamanlayıcısı** | Android'in `JobScheduler`'ı; bildirim yoklamasını zamanında çalıştırır |
| **kuyruk** | Çocuğun telefonunda internet gelene kadar bekleyen konumlar (`kuyruk.json`) |
| **aydınlatma onayı (KVKK)** | Kişinin aydınlatma metninin güncel sürümünü onaylaması; onaylamadan uygulama açılmaz |
| **doğrulama sorusu** | "4 + 7 = ?" gibi soru; kayıtta, şifre yenilemede ve hatalı girişten sonra |
| **iki adımlı giriş** | E-postası olan hesaba girişte e-postayla gelen 6 haneli kod |
| **deneme paketi / yayın paketi** | `debug` (http'ye izinli, deneme anahtarıyla) / `release` (yalnız https, yayın imzasıyla) |
| **APK / AAB** | Telefona doğrudan kurulan paket / Play Store'a yüklenen paket |
| **`versionCode` / `versionName`** | Her yayında artan tam sayı / insanın okuduğu sürüm ("2.0.0") |
| **uyarlanabilir simge** | Telefonun kendi biçimiyle kırptığı katmanlı başlatıcı simgesi |
| **boyama (tint)** | Siyah çizilmiş simgenin ekranda istenen renge boyanması |
| **`values-night`** | Telefon koyu temadayken Android'in seçtiği kaynak klasörü |
| **dp** | Ekran yoğunluğundan bağımsız uzunluk birimi |

## 13. Kurallar

- **Dış kütüphane eklenmez** (AndroidX dahil). Gereken şey Android'in kendi arayüzleriyle yazılır.
- **Arayüz yalnız `Arayuz` ve `Tema` parçalarıyla**; sabit renk kodu yazılmaz (koyu tema bozulur), renkler
  `res/values(-night)/renkler.xml`'den. Simgeler sitenin simgeleri (`araclar/simgeleri-uret.js`); yeni simge önce sitenin
  listesine eklenir.
- **Emoji yok, metinler Türkçe** ve sitedeki kelimelerle; simge düğmelerinde ekran okuyucu açıklaması
  (`setContentDescription`).
- **İstekler ekranlardan `Ag` ile**, servislerden kendi iş parçacıklarında `Api` ile; ana iş parçacığında ağ yok. (1.0.x'ten
  kalan `AileEkrani` istisnadır: kendi tek iş parçacıklı havuzunda `Api`'yi doğrudan çağırır.)
- **Gizli bilgi depoya girmez:** imza dosyaları, şifreler, anahtarlar, paketler (`.gitignore`). Kişisel veri işleyen yeni
  bir izin ya da özellik sitenin aydınlatma metnine de yazılır.
- **Lint temiz kalır:** `./gradlew --offline lintDebug` 0 hata, 0 uyarı.
- **Commit:** yalnız `KARANKOYU` adına, mesaj yalnız `commit N` (Android deposu kendi sırasını sayar), ek satır yok; her özellik
  ayrı commit.
- **Belgeler kodla birlikte yaşar:** bir Java dosyasını değiştiren yanındaki `.md`'nin ilgili bölümlerini ve **Son durum**'unu
  aynı işte günceller; yeni dosya yazan `.md`'sini ve bu dosyadaki haritaya satırını ekler. Kod dışı bir dosya eklenen
  klasörün `KLASOR.md`'si de güncellenir. Belgeler kodla çelişmez.

## 14. Nereden devam?

- **Önce yerel devir belgesi:** bu bilgisayarda site deposunun içinde `.claude/gelistirme/DEVAM.md` varsa (git dışında, iki
  depoda da yok) önce onu oku: son durum, sıradaki işler ve yaşanmış tuzaklar orada.
- Yoksa: `git --no-pager log --oneline -10` ve `git status`; her `.md`'nin **Son durum** bölümü o dosyada en son neyin
  değiştiğini ve bilinen açık işi yazar.
- **Git geçmişi kısaca:** `commit 1` (Eğitim Evi Aile, çocuğun telefonu), `commit 2–4` (ekran ve güvenlik düzeltmeleri, yayın
  imzası, yedek kuralları), `commit 5` (tek uygulamaya geçiş, o gün WebView), `commit 6` (yerel uygulama çekirdeği: bugünkü
  ekranlar, simgeler, renkler), `commit 7` (girişte `uygulama: true`, 30 günlük oturum), `commit 8` (aydınlatma metni ve SSS
  adresleri), `commit 9` (`.gitignore`), `commit 10` (16 karakterlik kişi kodu), `commit 11` (logo), `commit 12` (aydınlatma
  özetinde veri sorumlusu maddesi), `commit 13–15` (Java belgeleri).
- **Planlı işler** (adlarıyla):
  - "Android yerel uygulama (bütün roller) + doğrulayıcı + apk/aab + sürüm": ortak altyapı (harita, dosya yükleme/indirme,
    mesajlar, takvim, hatırlatıcılar, anketler, yemek, bildirim bağlantıları), öğrenci ve veli ekranları, öğretmen ve müdür
    ekranları, servisçi yoklaması ve servis (seferi başlatan düğme burada), quiz, inceleme, sürüm. Aynı işin ekleri:
    doğrulayıcı (telefonda iki adımlı kod üreten bölüm), uygulamanın kendini güncellemesi (GitHub paketinde; Play
    paketinde mağaza bağlantısı), giriş yapmadan da açılan sol menü (nasıl yorumlanacağı kullanıcıya soruldu).
  - "Android geri tuşu": önce açık olanı kapat, sonra önceki sayfa, kökte hiçbir şey yapma (uygulamadan çıkma).
  - "T.C. kimlik no bütün hesaplarda zorunlu": hesap açma ve ayarlarda sunucuyla aynı geçerlilik kuralı.
  - "Çok dil": uygulama dili seçici, sitenin çeviri kataloğu.
  - "Ekran turu": sitenin ekran görüntüsü albümünde uygulamanın görüntüleri de var (site deposunda
    `ekran-goruntuleri/aile-uygulamasi/`); özellikler bitince baştan alınacak.
  - "Kulüpler kaldırılacak": sitedeki `kulup` simgesi giderse `ik_kulup.xml` elle silinmeli.

## 15. Belge haritası

### Kök ve klasör belgeleri

| Dosya ya da klasör | Ne | Belgesi |
|---|---|---|
| Kök: `build.gradle`, `settings.gradle`, `gradle.properties`, `gradlew`, `gradlew.bat`, `README.md`, `.gitignore` | Gradle proje tanımı, sarmalayıcı betikleri, README, `.gitignore` | [KLASOR.md](KLASOR.md) |
| `app/` (`build.gradle`) | Tek modül: paket adı, sürümler, imza, lint, Java | [app/KLASOR.md](app/KLASOR.md) |
| `app/src/main/` (`AndroidManifest.xml`) | İzinler, ekranlar, servisler, alıcı | [app/src/main/KLASOR.md](app/src/main/KLASOR.md) |
| `app/src/main/res/drawable/` | 56 vektör simge | [app/src/main/res/drawable/KLASOR.md](app/src/main/res/drawable/KLASOR.md) |
| `app/src/main/res/values/` | Açık tema renkleri, tema, uygulama adı | [app/src/main/res/values/KLASOR.md](app/src/main/res/values/KLASOR.md) |
| `app/src/main/res/values-night/` | Koyu tema renkleri ve teması | [app/src/main/res/values-night/KLASOR.md](app/src/main/res/values-night/KLASOR.md) |
| `app/src/main/res/xml/` | Ağ güvenliği, yedek ve aktarım kuralları | [app/src/main/res/xml/KLASOR.md](app/src/main/res/xml/KLASOR.md) |
| `app/src/main/res/mipmap-anydpi/` | Başlatıcı simgesi | [app/src/main/res/mipmap-anydpi/KLASOR.md](app/src/main/res/mipmap-anydpi/KLASOR.md) |
| `app/src/debug/res/xml/` | Deneme paketinin ağ ayarı | [app/src/debug/res/xml/KLASOR.md](app/src/debug/res/xml/KLASOR.md) |
| `gradle/wrapper/` | Gradle sarmalayıcısının jar'ı ve ayarı | [gradle/wrapper/KLASOR.md](gradle/wrapper/KLASOR.md) |

### Araç

| Dosya | Ne | Belgesi |
|---|---|---|
| `araclar/simgeleri-uret.js` | Sitenin çizgi simgelerini `drawable/ik_*.xml`'e çevirir | [simgeleri-uret.md](araclar/simgeleri-uret.md) |

### `app/src/main/java/org/egitimevi/aile/`

| Dosya | Ne | Belgesi |
|---|---|---|
| `app/src/main/java/org/egitimevi/aile/AnaEkran.java` | Tek ekran: üst çubuk, sayfa, sekmeler; sekme başına yığın, geri, oturum akışı, genel hatalar, kısa mesaj, çıkış, uygulama anahtarı, zil rozeti | [AnaEkran.md](app/src/main/java/org/egitimevi/aile/AnaEkran.md) |
| `app/src/main/java/org/egitimevi/aile/Sayfa.java` | Her ekranın atası; `Sayfa.Veri`: yükleniyor → içerik ya da hata + "Yeniden dene" | [Sayfa.md](app/src/main/java/org/egitimevi/aile/Sayfa.md) |
| `app/src/main/java/org/egitimevi/aile/Sekmeler.java` | Alt çubuğun sekmeleri ve bildirim bağlantısının açacağı ekran | [Sekmeler.md](app/src/main/java/org/egitimevi/aile/Sekmeler.md) |
| `app/src/main/java/org/egitimevi/aile/Arayuz.java` | Arayüz parçaları: yazı, kart, düğme, form alanı, satır, avatar, etiket, boş durum, hata kutusu | [Arayuz.md](app/src/main/java/org/egitimevi/aile/Arayuz.md) |
| `app/src/main/java/org/egitimevi/aile/Tema.java` | Köşe yarıçapları, yazı tipleri, dp, renk, zeminler, avatar rengi | [Tema.md](app/src/main/java/org/egitimevi/aile/Tema.md) |
| `app/src/main/java/org/egitimevi/aile/Zaman.java` | Türkiye saatiyle tarih ve saat yazımı, selam | [Zaman.md](app/src/main/java/org/egitimevi/aile/Zaman.md) |
| `app/src/main/java/org/egitimevi/aile/Api.java` | HTTP + JSON, üç kimlik başlığı, https kuralı, `Api.Hata` | [Api.md](app/src/main/java/org/egitimevi/aile/Api.md) |
| `app/src/main/java/org/egitimevi/aile/Ag.java` | Ekranların istek kapısı: arka plan havuzu, ana iş parçacığına cevap, genel hatalar | [Ag.md](app/src/main/java/org/egitimevi/aile/Ag.md) |
| `app/src/main/java/org/egitimevi/aile/Oturum.java` | Oturum anahtarı ve hesap bilgisi ("oturum" ayar dosyası) | [Oturum.md](app/src/main/java/org/egitimevi/aile/Oturum.md) |
| `app/src/main/java/org/egitimevi/aile/UygulamaAyar.java` | Uygulama anahtarı, bildirim imleci, servis saatleri, süren sefer ("uygulama") | [UygulamaAyar.md](app/src/main/java/org/egitimevi/aile/UygulamaAyar.md) |
| `app/src/main/java/org/egitimevi/aile/Ayarlar.java` | Çocuğun telefonu bağlantısı, gönderme aralıkları ve sunucu adresi ("aile") | [Ayarlar.md](app/src/main/java/org/egitimevi/aile/Ayarlar.md) |
| `app/src/main/java/org/egitimevi/aile/GirisSayfasi.java` | Giriş: kimlik ve şifre, okul arama, doğrulama sorusu, iki adımlıya geçiş | [GirisSayfasi.md](app/src/main/java/org/egitimevi/aile/GirisSayfasi.md) |
| `app/src/main/java/org/egitimevi/aile/KodSayfasi.java` | İki adımlı girişin 6 haneli kodu, yeni kod | [KodSayfasi.md](app/src/main/java/org/egitimevi/aile/KodSayfasi.md) |
| `app/src/main/java/org/egitimevi/aile/KayitSayfasi.java` | Hesap aç (yetişkin hesabı); site sayfasını tarayıcıda açan `siteAc` | [KayitSayfasi.md](app/src/main/java/org/egitimevi/aile/KayitSayfasi.md) |
| `app/src/main/java/org/egitimevi/aile/SifremiUnuttumSayfasi.java` | Şifre yenileme bağlantısı isteme | [SifremiUnuttumSayfasi.md](app/src/main/java/org/egitimevi/aile/SifremiUnuttumSayfasi.md) |
| `app/src/main/java/org/egitimevi/aile/DogrulamaSorusu.java` | Formlara gömülen doğrulama sorusu | [DogrulamaSorusu.md](app/src/main/java/org/egitimevi/aile/DogrulamaSorusu.md) |
| `app/src/main/java/org/egitimevi/aile/KvkkSayfasi.java` | Aydınlatma metni onayı; metni tarayıcıda açan `metniAc` | [KvkkSayfasi.md](app/src/main/java/org/egitimevi/aile/KvkkSayfasi.md) |
| `app/src/main/java/org/egitimevi/aile/SifreSayfasi.java` | Şifre değiştir (sıradan ve zorunlu) | [SifreSayfasi.md](app/src/main/java/org/egitimevi/aile/SifreSayfasi.md) |
| `app/src/main/java/org/egitimevi/aile/AnaSayfa.java` | "Ana sayfa" sekmesi; rol kodunun Türkçe adı (`rolAdi`) | [AnaSayfa.md](app/src/main/java/org/egitimevi/aile/AnaSayfa.md) |
| `app/src/main/java/org/egitimevi/aile/PortalSecici.java` | "Portalların" penceresi ve portala geçiş | [PortalSecici.md](app/src/main/java/org/egitimevi/aile/PortalSecici.md) |
| `app/src/main/java/org/egitimevi/aile/EkleSayfasi.java` | "+ Ekle": Veli (veli kodu), Öğretmen ve Müdür (kişi kodu) | [EkleSayfasi.md](app/src/main/java/org/egitimevi/aile/EkleSayfasi.md) |
| `app/src/main/java/org/egitimevi/aile/KisiKodu.java` | 16 karakterlik kişi/veli kodunun biçimi, ayıklanması ve kopyalanabilir kutusu | [KisiKodu.md](app/src/main/java/org/egitimevi/aile/KisiKodu.md) |
| `app/src/main/java/org/egitimevi/aile/AyarlarSayfasi.java` | "Ayarlar" sayfası | [AyarlarSayfasi.md](app/src/main/java/org/egitimevi/aile/AyarlarSayfasi.md) |
| `app/src/main/java/org/egitimevi/aile/BildirimlerSayfasi.java` | "Bildirimler" sayfası; okundu yapma ve zil rozeti | [BildirimlerSayfasi.md](app/src/main/java/org/egitimevi/aile/BildirimlerSayfasi.md) |
| `app/src/main/java/org/egitimevi/aile/Bildirimler.java` | Bildirim yoklamasının zamanlanması, yoklama ve bildirim çubuğu | [Bildirimler.md](app/src/main/java/org/egitimevi/aile/Bildirimler.md) |
| `app/src/main/java/org/egitimevi/aile/BildirimIsi.java` | Yoklamayı çalıştıran `JobService` | [BildirimIsi.md](app/src/main/java/org/egitimevi/aile/BildirimIsi.md) |
| `app/src/main/java/org/egitimevi/aile/BaslatmaAlici.java` | Telefon açılınca ve uygulama güncellenince servisleri yeniden kurar | [BaslatmaAlici.md](app/src/main/java/org/egitimevi/aile/BaslatmaAlici.md) |
| `app/src/main/java/org/egitimevi/aile/SeferServisi.java` | Servisçinin sefer konumu (ön plan servisi; bugün başlatan yok) | [SeferServisi.md](app/src/main/java/org/egitimevi/aile/SeferServisi.md) |
| `app/src/main/java/org/egitimevi/aile/AileEkrani.java` | "Çocuğun telefonu" ekranı: bağlama, izinler, durum | [AileEkrani.md](app/src/main/java/org/egitimevi/aile/AileEkrani.md) |
| `app/src/main/java/org/egitimevi/aile/IzlemeServisi.java` | Çocuğun telefonunun arka plan servisi: konum, ekran süresi, ayar | [IzlemeServisi.md](app/src/main/java/org/egitimevi/aile/IzlemeServisi.md) |
| `app/src/main/java/org/egitimevi/aile/Kullanim.java` | Ekran süresinin hesabı (hangi uygulama önde kaç dakika) | [Kullanim.md](app/src/main/java/org/egitimevi/aile/Kullanim.md) |
| `app/src/main/java/org/egitimevi/aile/Kuyruk.java` | Gönderilmeyi bekleyen konumlar (`kuyruk.json`) | [Kuyruk.md](app/src/main/java/org/egitimevi/aile/Kuyruk.md) |

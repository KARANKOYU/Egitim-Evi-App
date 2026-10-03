# app/src/main/java/org/egitimevi/aile/AyarlarSayfasi.java

Uygulamanın "Ayarlar" sayfası: profil kartı, öğrencide veli kodu, Portallarım / Ekle / Şifre değiştir, telefon bildirimleri
izni, öğrencinin "Bu telefonu velimle paylaş"ı, siteye, aydınlatma metnine ve SSS'ye bağlantılar, çıkış ve sürüm.

## Bu dosya ne yapar?

Giriş yapan herkesin hesabıyla ilgili küçük işleri tek yerde toplar. Sitedeki "Ayarlar"ın tamamı değildir: kişisel bilgileri
değiştirme, tema, hesabı silme gibi işler bugün uygulamada yok; onlar için "Siteyi aç" satırı var. Sayfa sunucuya hiç istek
atmaz: kişinin bilgilerini telefonda saklı oturumdan (`Oturum.kisi`: sunucunun son verdiği `user`; ne zaman tazelendiği
"Kimle konuşur?"ta) okur.

Dört yerden açılır:

1. Alt çubuktaki üçüncü sekme "Ayarlar" (kök sayfası; `Sekmeler.icin`).
2. Hesabında `yetiskin` ya da `rolSatiri` işareti olmayan kişi (bugün öğrenci, servisçi, yönetici) sol üstteki avatarına
   dokununca ([AnaEkran.md](AnaEkran.md) `hesapMenusu`; işaretlilerde avatar portallar penceresini açar).
3. Portallar penceresindeki "Ayarlar" düğmesi (`PortalSecici.java`).
4. Bildirimler listesinde bağlantısı `#/profil` olan bir bildirime dokununca (`Sekmeler.baglantiAc`).

Kim ne görür: herkes profil kartını, "Şifre değiştir"i, "Telefon bildirimleri"ni, site bağlantılarını ve "Çıkış yap"ı
görür. Öğrenci ayrıca "Veli kodun" kartını ve "Bu telefonu velimle paylaş" satırını; yetişkin hesabı (ya da ona bağlı
öğretmen/müdür rolü) ayrıca "Portallarım" ve "Ekle" satırlarını görür.

## İçinde neler var?

### Sayfa sözleşmesi (`Sayfa`)

- `baslik()` → "Ayarlar" (üst çubukta).
- `olustur()` → sayfanın bütün görünümü (aşağıdaki bölümler); kaydırılan gövde (`Arayuz.sayfaGovdesi`) döner.
- `gorundu()` → `yenile()`: üstündeki bir sayfa kapanıp bu sayfa yeniden göründüğünde ([AnaEkran.md](AnaEkran.md) `kapat`)
  sayfa baştan kurulur (ör. "Şifre değiştir"den dönünce).
- `yenilenir()` ezilmez (`false`): üst çubukta "Yenile" düğmesi yok. `cubuksuz()` da `false`: üst ve alt çubuk görünür.

### Bölümler (yukarıdan aşağı)

1. **Profil kartı** — 56 dp avatar (adın baş harfleri, rengi kişinin kimliğinden; `Arayuz.avatar`), 19 sp ad, altında
   "Rol · Okul" (rol adı `AnaSayfa.rolAdi`: Öğrenci, Veli, Öğretmen, Müdür, Servisçi, Yönetici; rolsüzse "Hesabım"; okul
   adı yoksa yalnız rol) ve e-posta (yoksa kullanıcı adı).
2. **VELİ KODUN** (yalnız rolü `student` olan ve `code`'u dolu kişide) — "Velin bu kodla seni Eğitim Evi'nde ekler. Kodu
   yalnızca velinle paylaş." ve `KisiKodu.kutu`: 16 karakterlik kod 4'erli tireli gösterilir, "Kopyala" tireli biçimi panoya
   koyar ve altta "Kod kopyalandı." çıkar. Veli bu kodu kendi uygulamasında [EkleSayfasi.md](EkleSayfasi.md) → "Veli"ye
   yazar.
3. **HESAP**
   - "Portallarım" (simge `ik_grup`; "Veli, öğretmen ve müdür portalların arasında geç") → `PortalSecici.goster` — yalnız
     `yetiskin` ya da `rolSatiri` işaretli kişide.
   - "Ekle" (`ik_ekle`; "Çocuğunu ekle, öğretmen olarak katıl ya da okulunu açtır") → [EkleSayfasi.md](EkleSayfasi.md) —
     aynı koşulla.
   - "Şifre değiştir" (`ik_kilit`) → `SifreSayfasi(false)` (zorunlu olmayan şifre değiştirme) — herkese.
4. **TELEFON**
   - "Telefon bildirimleri" (`ik_bildirim`). Alt yazısı:
     - izin yoksa (yalnız Android 13 ve üstünde olabilir): "Kapalı: dokun ve izin ver";
     - izin var ve telefonda uygulama anahtarı var (`UygulamaAyar.anahtarVar`): "Açık: 15 dakikada bir, servis saatlerinde
       dakikada bir bakılır";
     - izin var ama anahtar yok: "Açık".
     Dokununca `bildirimAyari()`.
   - "Bu telefonu velimle paylaş" (`ik_konum`, yalnız öğrencide). Alt yazısı `Ayarlar.bagli` ise "Bağlı: konum ve ekran süresi
     velinle paylaşılıyor", değilse "Konumunu ve ekran süreni velin görsün". Dokununca ayrı Activity olan
     [AileEkrani.md](AileEkrani.md) açılır (`new Intent(e, AileEkrani.class)`).
5. **EĞİTİM EVİ** — hepsi telefonun tarayıcısında açılır (`KayitSayfasi.siteAc`: `Ayarlar.sunucu` + yol):
   - "Siteyi aç" (`ik_okul`; "Uygulamada olmayan işler için (Excel aktarımı, roller, ders programı)") → `/`;
   - "Aydınlatma metni" (`ik_belge`) → `KvkkSayfasi.metniAc` → `/kvkk/kvkk.html`;
   - "Sık sorulan sorular" (`ik_soru`) → `/sss/sss.html` (sitede ayrı dosya değil, sunucunun tek sayfalık uygulamaya
     yönlendirdiği yol: site deposunda `sunucu/http.js` `UYGULAMA_YOLLARI`).
6. **"Çıkış yap"** (kırmızı `TEHLIKE` düğmesi) → onay penceresi: başlık "Çıkış yapılsın mı?", metin "Bu telefonda bildirimler
   de durur.", "Çıkış yap" → `e.cikis()`, "Vazgeç" → hiçbir şey.
7. En altta ortalı soluk yazı: "Eğitim Evi " + paket sürümü (`versionName`; bugün `app/build.gradle`'da `2.0.0`).

Bölüm etiketleri `Arayuz.bolumEtiketi` ile Türkçe büyük harfe çevrilir ("Veli kodun" → "VELİ KODUN").

### İç işlevler

- `satir(simge, ad, alt, tik)` — `Arayuz.tiklananSatir`: solda ana renkli 22 dp simge, ad, (varsa) alt yazı, sağda ok.
- `bildirimAyari()` — Android 13 (API 33) ve üstünde bildirim izni yoksa izin penceresini açar
  (`requestPermissions(POST_NOTIFICATIONS, 13)`); izin varsa (ya da Android 12 ve altıysa) telefonun bu uygulamaya ait bildirim
  ayarlarını açar (`Settings.ACTION_APP_NOTIFICATION_SETTINGS` + `EXTRA_APP_PACKAGE`); o açılamazsa uygulamanın ayrıntı
  sayfası (`ACTION_APPLICATION_DETAILS_SETTINGS`, `package:org.egitimevi.aile`).

## Kimle konuşur?

- Aynı paketten çağırdıkları:
  - [Arayuz.md](Arayuz.md) — `sayfaGovdesi`, `kart`, `yatay`, `dikey`, `avatar`, `altBaslik`, `soluk`, `yazi`,
    `bolumEtiketi`, `ekle`, `ayirici`, `tiklananSatir`, `ikon`, `dugme` (`TEHLIKE`); `Tema.java` — `dp`.
  - `Oturum.java` — `kisi` (`fullName`, `id`, `role`, `schoolName`, `email`, `username`, `code`, `yetiskin`,
    `rolSatiri`).
  - [AnaSayfa.md](AnaSayfa.md) — `rolAdi`; `KisiKodu.java` — `kutu`; `PortalSecici.java` — `goster`;
    [EkleSayfasi.md](EkleSayfasi.md); `SifreSayfasi.java`; `KayitSayfasi.java` — `siteAc`; `KvkkSayfasi.java` — `metniAc`.
  - `UygulamaAyar.java` — `anahtarVar`; [Ayarlar.md](Ayarlar.md) — `bagli`; [AileEkrani.md](AileEkrani.md) (Intent).
  - [AnaEkran.md](AnaEkran.md) — `git`, `bildir`, `cikis`; Activity olarak `startActivity`, `checkSelfPermission`,
    `requestPermissions`, `getPackageManager`.
- Sunucuyla doğrudan konuşmaz. Dolaylı olarak:
  - "Çıkış yap" → [AnaEkran.md](AnaEkran.md) `cikis`: arka planda `POST /api/cihaz/sil` (`X-Cihaz`; site deposunda
    `sunucu/bolumler/cihaz.md`) ve `POST /api/logout` (`sunucu/bolumler/kayit.md`), cevap beklenmeden giriş ekranı.
  - Gösterilen bilgiler girişte, kod doğrulamada, portal değişiminde, aydınlatma onayında (`POST /api/kvkk-onay`), şifre
    değişiminde (`POST /api/password`) ya da çocuk eklendikten sonraki `GET /api/me`'de gelen `user` nesnesidir
    (`sunucu/bolumler/kayit.md` `benimGorunum`: `yetiskin`, `rolSatiri`; ortak alanlar `sunucu/yetki.md` `pub`: `code`,
    `schoolName` …).
- Android: `AlertDialog`, `Intent`, `Settings` (`ACTION_APP_NOTIFICATION_SETTINGS`, `EXTRA_APP_PACKAGE`,
  `ACTION_APPLICATION_DETAILS_SETTINGS`), `Uri`, `Build.VERSION.SDK_INT`, `PackageManager` (`PERMISSION_GRANTED`,
  `getPackageInfo(...).versionName`). İzin: `POST_NOTIFICATIONS` (manifestte bildirilmiş, Android 13+'te çalışma anında
  istenir).
- Onu açanlar (grep): `Sekmeler.java` (`AyarlarSayfasi::new`, üçüncü sekme; ayrıca `baglantiAc`'ta `profil`),
  [AnaEkran.md](AnaEkran.md) (`hesapMenusu`), `PortalSecici.java` ("Ayarlar" düğmesi).

## Nasıl çalışır (adım adım)?

```
"Ayarlar" sekmesi ─► AnaEkran.sekmeSec(2) ─► (ilk kez) olustur()
   k = Oturum.kisi(e)                          ← telefondaki kopya, istek yok
   [avatar  Ad Soyad / Rol · Okul / e-posta]
   öğrenci ve code dolu?        → VELİ KODUN  [Ab3#-kQx9-+mPt-7?zR] [Kopyala]
   HESAP:   yetişkin ya da rol satırı? → Portallarım, Ekle ;  herkes → Şifre değiştir
   TELEFON: Telefon bildirimleri (Kapalı / Açık / Açık: 15 dakikada bir…) ; öğrenci → Bu telefonu velimle paylaş
   EĞİTİM EVİ: Siteyi aç · Aydınlatma metni · Sık sorulan sorular
   [Çıkış yap]   Eğitim Evi 2.0.0

"Şifre değiştir" ─► git(SifreSayfasi(false)) ─► kaydet ─► kapat() ─► AyarlarSayfasi.gorundu() ─► yenile() ─► baştan kur
"Telefon bildirimleri" ─► Android 13+ ve izin yok ─► izin penceresi (istek kodu 13; sonucu dinlenmez)
                       └► öbür durumlar          ─► telefonun bildirim ayarları
"Çıkış yap" ─► "Çıkış yapılsın mı?" ─► Çıkış yap ─► AnaEkran.cikis() ─► giriş ekranı
```

## Dikkat!

- **Sayfa kendini yalnız üstündeki bir sayfa kapanınca tazeler.** `gorundu()` yalnız [AnaEkran.md](AnaEkran.md)'nin
  `kapat()`'ında çağrılır. İzin penceresinden, telefonun bildirim ayarlarından ya da [AileEkrani.md](AileEkrani.md)'den
  (ayrı Activity) dönünce çağrılmaz; sekme değiştirip dönmek de yetmez. Bu yüzden "Telefon bildirimleri" ve "Bu telefonu
  velimle paylaş" satırlarının alt yazıları, sayfa yeniden kurulana kadar (üstüne açılan bir sayfa kapanınca ya da sekmeler
  baştan kurulunca) eski hâlinde kalır: ör. öğrenci telefonu bağlayıp geri dönse de satır "Konumunu ve ekran süreni velin
  görsün" der (kod okumasına göre; telefonda denenmedi).
- **İki kez reddedilen izin.** Android 11'den beri kullanıcı bir izni iki kez reddederse sistem izin penceresini artık
  göstermez. `bildirimAyari` izin yokken HER ZAMAN pencereyi istediği için bu durumda satıra dokunmak görünür hiçbir şey
  yapmaz; kişi bildirimi telefonun ayarlarından kendisi açmalı. Öneri: izin reddedilmişse doğrudan bildirim ayarlarına
  gitmek (kod değiştirilmedi; telefonda denenmedi).
- **"Açık" yalnız izne bakar.** Android 12 ve altında izin hep var sayılır; kişi bildirimleri telefonun ayarlarından
  (uygulamanın ya da "Bildirimler" kanalının) kapatmışsa satır yine "Açık" der (`NotificationManager.areNotificationsEnabled`
  ya da kanalın önemi denetlenmiyor).
- **"servis saatlerinde dakikada bir" herkese yazıyor.** Hızlı yoklama yalnız sunucu `servisSaatleri` gönderirse (servisçi,
  servisteki öğrenci ya da velisi) çalışır; öteki hesaplarda hep 15 dakikadır. "Dakikada bir" de pratikte 1–3 dakikadır
  ([Bildirimler.md](Bildirimler.md)).
- **Bilgiler telefondaki kopyadan.** Uygulama açılışta `/api/me` istemez; ad, okul, e-posta ve öğrencinin veli kodu girişte,
  portal değişiminde, aydınlatma onayında, şifre değişince ya da çocuk eklenince tazelenir. Sitede ad değişirse ya da okul
  öğrencinin veli kodunu yenilerse (`testler/test-kisi-kodu.js` "Okul yeniler…") burada eski bilgi görünür; eski veli kodu
  artık çalışmaz.
- **Rol adı kısa liste.** `AnaSayfa.rolAdi` yalnız altı rolü tanır; özel rol adı (`customRoleName`, ör. bir öğretmenin
  "Müdür Yardımcısı" rolü) gösterilmez, öğretmen "Öğretmen" görünür. Bilinmeyen rol "Hesabım" olur.
- **Çıkış Aile paylaşımını durdurmaz.** Onay penceresi yalnız "bildirimler de durur" der; ama [AnaEkran.md](AnaEkran.md)
  `cikis` Aile anahtarına ([Ayarlar.md](Ayarlar.md)) dokunmaz, öğrenci çıksa da konum ve ekran süresi paylaşımı sürer.
- **Site bağlantıları uygulamanın oturumunu taşımaz.** Tarayıcı ayrı bir yerdir; "Siteyi aç"tan sonra kişi sitede yeniden
  giriş yapmalı. Adres `Ayarlar.sunucu`'dan gelir; Aile bağlantısıyla değişmiş olabilir ([Ayarlar.md](Ayarlar.md)). Telefonda
  tarayıcı yoksa "Bağlantıyı açacak tarayıcı yok." çıkar.
- **İzin isteği kodu 13** [AnaEkran.md](AnaEkran.md)'nin girişten sonraki izin isteğiyle aynı. İzin penceresinin sonucu
  dinlenmez: bu sayfanın Activity'si `AnaEkran`'da `onRequestPermissionsResult` yok (uygulamada bu işlev yalnız ayrı Activity
  olan [AileEkrani.md](AileEkrani.md)'de, kendi konum ve bildirim izinleri için yazılmış).

## Testleri

- Android deposunda otomatik test yok; derleme ve lint (`./gradlew --offline assembleDebug lintDebug`) ana oturumda.
- Gösterilen bilgilerin sunucu sözleşmesini site deposundaki testler korur: `testler/test-yetiskin.js` (`user.yetiskin`,
  `user.rolSatiri` bayrakları), `testler/test-kisi-kodu.js` (öğrenci kendi veli kodunu `/api/me`'de görüyor; okul yenileyince
  eski kod çalışmıyor), `testler/test-servis-yoklama.js` (çıkışta `/api/cihaz/sil` ve `/api/logout` ile uygulama oturumunun ve
  anahtarın kapanması).
- Elle (öykünücü ya da telefon):
  - Öğrenci hesabıyla gir → Ayarlar: "VELİ KODUN" kartı, "Kopyala" → "Kod kopyalandı."; "Bu telefonu velimle paylaş" satırı var;
    "Portallarım" ve "Ekle" yok.
  - Yetişkin hesabıyla gir → "Portallarım" portallar penceresini, "Ekle" "Ne eklemek istiyorsun?" sayfasını açar; veli kodu
    kartı yok.
  - Android 13+'te bildirim iznini reddet → satır "Kapalı: dokun ve izin ver"; dokun → izin penceresi; izin ver, "Şifre
    değiştir"e girip geri dön → satır "Açık: …" olur (geri dönmeden değişmez).
  - "Sık sorulan sorular" → tarayıcıda sitenin SSS sayfası; "Çıkış yap" → onay → giriş ekranı.

## Son durum

- `git log`: 2 commit (Android deposu). Son değişiklik `d0fc203 commit 8` (2026-09-27): "Sık sorulan sorular" bağlantısı
  `/sss` yerine `/sss/sss.html` oldu (aynı commit `KvkkSayfasi.metniAc`'ı da `/kvkk.html`'den `/kvkk/kvkk.html`'e çevirdi;
  "Aydınlatma metni" satırı o işlevi kullanır).
- Dosyanın ilk hâli `e96c5f2 commit 6` (2026-09-26): yerel uygulamanın çekirdeğiyle birlikte bütün sayfa (profil, veli kodu,
  hesap, telefon, site bağlantıları, çıkış, sürüm).
- Bilinen açıklar (kod değiştirilmedi): dışarıdan dönünce alt yazıların tazelenmemesi, iki kez reddedilen izinde satırın
  bir şey yapmaması, "Açık"ın yalnız izne bakması, "servis saatlerinde" yazısının herkese gösterilmesi.
- Planlı işlerden bu sayfaya dokunması beklenenler:
  - "Android yerel uygulama" — doğrulayıcı tanımı "Ayarlar > Güvenlik > 'Bu telefonu doğrulayıcı yap'" satırını, çok dil eki
    Ayarlar'da dil seçiciyi istiyor; rol ekranlarında Ayarlar her rolün "Diğer" sekmesinin altına taşınacak. Kendini güncelleme
    eki "Yeni sürüm var: 2.1.0 — Güncelle" şeridini ekranın üstüne koyuyor (Ayarlar'ı anmıyor; buradaki sürüm satırının da
    güncel sürümü söylemesi önerilir).
  - "Üst şerit sadeleştirme" — sitede Ayarlar, Portallarım ve Çıkış profil menüsüne taşınıyor; uygulamada ← → ⌂ düğmeleri.
  - "Sistem" (açık oturumlar listesi, "Diğer bütün cihazlardan çık"; telefon uygulaması oturumları da listede) ve
    "Kullanıcı arama … Verilerimi indir" işleri Ayarlar'a yeni satırlar getirebilir (tanımlar siteden söz ediyor).
  - "Çalışan olarak ekleme": tanım "+ Ekle"deki "Öğretmen" seçeneğini "Çalışan" yapıyor ve Android'de `EkleSayfasi`'nı
    anıyor; bu sayfadaki "Ekle" satırının "öğretmen olarak katıl" alt yazısı da aynı işte değişmeli (tanımda ayrıca yazmıyor).

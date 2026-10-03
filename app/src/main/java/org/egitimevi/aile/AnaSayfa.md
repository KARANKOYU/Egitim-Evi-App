# app/src/main/java/org/egitimevi/aile/AnaSayfa.java

"Ana sayfa" sekmesinin kök sayfası: tarih, günün saatine göre selam, rol ve okul; portalı olmayan yetişkin hesabına boş
durum ve "+ Ekle", birden çok portalı olan yetişkin hesabına portal kartları, öbür herkese "rolüne özel ekranlar
hazırlanıyor" şeridi — ve rol kodunu Türkçe ada çeviren `rolAdi`.

## Bu dosya ne yapar?

Uygulamaya giren herkesin ilk gördüğü sayfa budur: alt çubuktaki ilk sekme ("Ana sayfa", ev simgesi) bu sayfayla açılır
(`Sekmeler.java`: `new AnaEkran.Sekme("Ana sayfa", R.drawable.ik_ev, AnaSayfa::new)`).

Bugün "genel" bir karşılama sayfasıdır; rollere özel ana sayfalar (öğrencinin bugünkü dersleri, velinin çocuğunun özeti,
öğretmenin "şu anki ders"i…) henüz yazılmadı. Sınıfın yorumu da bunu söyler: rol ekranları eklendikçe `Sekmeler`'de bu
sayfanın yerini alacaklar. O güne kadar sayfa üç durumdan birini gösterir:

1. **Portalı olmayan yetişkin hesabı** (yeni hesap açmış, henüz ne çocuğu ne okul rolü olan kişi): "Henüz bir portalın
   yok" boş durumu ve "+ Ekle" düğmesi — çocuğunu ekle, okuluna öğretmen olarak katıl ya da okulunu açtır.
2. **Birden çok portalı olan ve oturumu yetişkin hesabında duran kişi** (hiçbir portala girmemiş ya da bir veli portalına
   girmiş — veli portalı da yetişkin hesabında açılır): "PORTALLARIN" başlığı altında portal kartları ("Öğretmen · Deneme
   Ortaokulu", "Veli · Can Kaya"; bulunulan veli portalında "Buradasın"); dokununca o portala geçilir.
3. **Öbür herkes** (öğrenci, servisçi, yönetici, bir okul rolüne — öğretmen ya da müdür — girmiş yetişkin, tek portallı
   yetişkin): mavi şerit — "Bu sürümde rolüne özel ekranlar hazırlanıyor. Bildirimlerin ve ayarların hazır."

Sayfa sunucuya kendisi istek atmaz: her şeyi girişte (ya da portal değişiminde) saklanan oturum bilgisinden okur.

## İçinde neler var?

- `baslik()` → `"Eğitim Evi"` (üst çubukta görünen başlık).
- `olustur()` — sayfayı kurar (`Arayuz.sayfaGovdesi` içinde), yukarıdan aşağı:
  1. Bölüm etiketi: bugünün tarihi Türkiye saatiyle (`Zaman.bugun()`, ör. "3 Ekim, Cumartesi"; etiket büyük harfle
     yazdığı için ekranda "3 EKİM, CUMARTESİ").
  2. Büyük başlık: `Zaman.selam()` + ad: 06:00'dan önce "İyi geceler", 12:00'den önce "Günaydın", 18:00'den önce "İyi
     günler", sonra "İyi akşamlar" (hepsi Türkiye saati); kişinin `fullName`'inin ilk kelimesi eklenir ("Günaydın,
     Elif"). Ad yoksa yalnız selam.
  3. Rol ve okul (15 sp, soluk): `rolAdi(role)` + " · " + `schoolName` (ör. "Öğrenci · Deneme Ortaokulu"); biri boşsa
     yalnız öteki, ikisi de boşsa satır yok.
  4. Durum (yukarıdaki üç durumdan biri):
     - "Yetişkin hesabındayız" = `user.yetiskin` doğru VE `user.rolSatiri` yanlış (oturum bir okul rolü satırında değil).
     - Durum 1: yetişkin hesabındayız VE `role` boş VE oturumda portal yok VE çocuk yok → `Arayuz.bosDurum` (grup simgesi,
       "Henüz bir portalın yok", "Başla: çocuğunu ekle, okuluna öğretmen olarak katıl ya da okulunu açtır.", "+ Ekle"
       birincil düğmesi → `EkleSayfasi`).
     - Durum 2: yetişkin hesabındayız VE portal sayısı 1'den fazla → "Portalların" etiketi ve içi `PortalSecici.satirlar`
       ile doldurulan liste kartı (kapatılacak pencere yok: `null`).
     - Durum 3: öteki her şey → `Arayuz.serit(…, Etiket.MAVI)`.
- `rolAdi(rol)` (paket içi, statik) — sunucunun rol kodunu Türkçe ada çevirir: `student` → "Öğrenci", `parent` → "Veli",
  `teacher` → "Öğretmen", `principal` → "Müdür", `servisci` → "Servisçi", `admin` → "Yönetici"; başka her şey `""`.
  Sitedeki `ROL_AD` tablosunun (`public/js/parcalar/02-ikonlar.js`) birebir aynısı.

## Kimle konuşur?

- Çağırdıkları:
  - `Sayfa.java` — bu sınıfın atası (`e` alanı ile [AnaEkran.md](AnaEkran.md)'ye ulaşır).
  - [Arayuz.md](Arayuz.md) — `sayfaGovdesi`, `bolumEtiketi`, `baslik`, `yazi`, `ekle`, `dugme` (`BIRINCIL`), `bosDurum`,
    `kart`, `serit` (`Etiket.MAVI`); `Tema.dp`.
  - `Oturum.java` — `kisi` (`fullName`, `schoolName`, `role`, `yetiskin`, `rolSatiri`), `portallar`, `cocuklar`.
  - `Zaman.java` — `bugun`, `selam`.
  - `PortalSecici.java` — `satirlar` (portal kartları; dokunulan satır `POST /api/kisilik/gec` ile o portala geçer —
    site deposunda `sunucu/bolumler/kisilik.md`).
  - `EkleSayfasi` — "+ Ekle" düğmesinin açtığı sayfa ([AnaEkran.md](AnaEkran.md) `git`).
  - Kaynaklar: `R.drawable.ik_grup`, `R.color.soluk`.
- Onu çağıranlar: `Sekmeler.java` (ilk sekmenin kökü), `AyarlarSayfasi.java` (`AnaSayfa.rolAdi(rol)` — profil kartındaki
  "Öğrenci · Okul adı" satırı).
- Sunucu: doğrudan hiçbir uca gitmez. Okuduğu bilgiler `Oturum`'a yazılan cevaplardan gelir: giriş (`POST /api/login`
  ya da `/api/login/dogrula`) ve portal değişimi (`POST /api/kisilik/gec`) bütün oturum cevabını yazar; çocuk ekleyince
  `GET /api/me`, aydınlatma onayında `POST /api/kvkk-onay` ve şifre değişince `POST /api/password` cevabı `user`'ı
  tazeler. Kullanılan alanlar: `user` (kişinin kendine görünümü — site deposunda `sunucu/bolumler/kayit.md`,
  `benimGorunum`: `yetiskin`, `rolSatiri` ve öbür alanlar), `children`, `portallar`.
- Gören: uygulamaya giren herkes (bütün roller ve rolsüz yetişkin hesabı).

## Nasıl çalışır (adım adım)?

```
uygulamayiKur → Sekmeler.icin → "Ana sayfa" kökü = new AnaSayfa() → olustur()
  k = Oturum.kisi(e)
  [3 EKİM, CUMARTESİ]
  Günaydın, Elif
  Öğrenci · Deneme Ortaokulu
  yetiskinHesabi = k.yetiskin && !k.rolSatiri
    ├─ yetiskinHesabi && role == "" && portallar == [] && children == []
    │     → (grup simgesi) Henüz bir portalın yok … [+ Ekle] ─► EkleSayfasi
    ├─ yetiskinHesabi && portallar.length > 1
    │     → PORTALLARIN  [Öğretmen · Deneme Ortaokulu   Buradasın]
    │                    [Veli · Can Kaya               >] ─► POST /api/kisilik/gec ─► oturumAc ─► sekmeler baştan
    └─ değilse
          → [Bu sürümde rolüne özel ekranlar hazırlanıyor. Bildirimlerin ve ayarların hazır.]
```

## Dikkat!

- **Sayfa kendini tazelemez.** Bilgiler oturumdaki son kopyadan okunur; "Yenile" düğmesi yok (`yenilenir()` varsayılan
  `false`) ve `gorundu()` yazılmamış. Bu arada bir müdür kişiyi okula eklese ya da bir portal onaylansa bile sayfa ancak
  oturum bilgisi değişip sekmeler yeniden kurulunca (giriş, portal değişimi, çocuk ekleme → `yenidenKur`) güncellenir.
- **Tek portallı yetişkin portal kartı görmez.** Kartlar için portal sayısı 1'den FAZLA olmalı; tek portalı olan (ör. tek
  çocuklu veli, oturumu yetişkin hesabında açılmış) mavi şeridi görür. Portallar penceresine sol üstteki avatardan
  ulaşılır.
- **Portal kartları ağsız çizilir.** Durum 2'ye ancak oturumda portal listesi varken girildiği için `PortalSecici.satirlar`
  sunucuya sormadan çizer; "Yükleniyor..." yolu burada hiç çalışmaz.
- **`rolAdi` yalnız altı rolü bilir.** Okulun özel rolü (sunucuda ayrı alan) burada görünmez, özel rollü öğretmen
  "Öğretmen" diye yazılır (özel rolün adı sunucudan `customRoleName` olarak gelir ama burada okunmaz); planlı yeni roller
  (çalışan, eğitmen, çevirmen, destek…) eklenince boş ad döner ve yalnız okul adı görünür — okul adı da yoksa satır hiç
  çıkmaz. Sitedeki `ROL_AD` ile elle eşit tutulmalı.
- **Ad ilk kelimeden alınır.** "Elif Nur Kaya" → "Günaydın, Elif". Ad boşluklarla bölünür; boş adda selam tek başına
  kalır.
- **Saat Türkiye'ye göredir**, telefonun saat dilimine değil (`Zaman.TURKIYE`): yurt dışındaki bir telefonda da selam ve
  tarih Türkiye saatini izler.
- Mavi şeridin metni geçici bir durum bildirir; rol ekranları gelince kaldırılmalı.

## Testleri

- Android deposunda otomatik test yok; derleme ve lint (`./gradlew --offline assembleDebug lintDebug`) ana oturumda.
- Sunucu tarafında `yetiskin`, `rolSatiri`, `portallar` ve `kisilikSec` alanlarını site deposundaki
  `testler/test-yetiskin.js` ve `testler/test-kisi-kodu.js` korur (yetişkin hesabı, portallar, portal geçişi); bu sayfa
  doğrudan test edilmez.
- Elle (öykünücü, deneme sunucusu ve tohum verisindeki hesaplar — `testler/seed.js`):
  - Öğrenci hesabıyla gir → tarih, "Günaydın/İyi günler…, <ad>", "Öğrenci · <okul>", mavi şerit.
  - Yeni bir yetişkin hesabı aç ve gir → "Henüz bir portalın yok" ve "+ Ekle"; "+ Ekle" → "Ne eklemek istiyorsun?".
  - Hem öğretmen hem veli olan yetişkin hesabıyla gir (oturum yetişkin hesabında açılır) → "PORTALLARIN" kartı. Öğretmen
    satırına dokun → o portala geçilir, sekmeler baştan kurulur, ana sayfada artık "Öğretmen · <okul>" ve mavi şerit.
    Avatardan portallara dönüp veli satırına dokun → oturum yine yetişkin hesabında açılır: kartlar kalır, "Buradasın" veli
    satırına geçer.

## Son durum

- `git log`: 1 commit (Android deposu). Dosya `e96c5f2 commit 6` (2026-09-26) ile geldi (yerel uygulamanın çekirdeği,
  "şimdilik genel" ana sayfa); o günden beri değişmedi.
- Bilinen açıklar (kod değiştirilmedi): sayfanın kendini tazelememesi, `rolAdi`'nin yalnız altı rolü bilmesi.
- Planlı işlerden bu dosyaya dokunması beklenenler: "Android yerel uygulama" (bütün roller — her rolün kendi ana sayfası
  bu sayfanın yerini alacak: öğrencide bugünün dersleri, yaklaşan ödevler, ödev serisi şeridi ve servis durumu; velide
  çocuk seçici ve çocuğun özeti; öğretmende bugünkü dersler ve "Şu an — yoklama al"; müdürde özet kutucukları); "Tek kişi
  tek hesap + portallar öğrencide de" (öğrencinin de portalları olacak; bugünkü "yalnız yetişkin hesabında portal kartı"
  koşulu değişecek); "Çalışan olarak ekleme", "Eğitim içerikleri" (eğitmen rolü), "Çok dil" (çevirmen rolü) ve "Özel
  roller" (`rolAdi` yeni rollerin adlarını öğrenmeli).

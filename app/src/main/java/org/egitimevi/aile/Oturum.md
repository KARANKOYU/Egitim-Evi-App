# app/src/main/java/org/egitimevi/aile/Oturum.java

Giriş yapan kişinin telefonda duran oturumu: sunucunun verdiği oturum anahtarı ve son bilinen hesap bilgisi (`user`,
çocuklar, portallar, okulun kapattığı bölümler, aydınlatma onayı) ile velinin seçtiği çocuk; hepsi uygulamaya özel
"oturum" ayar dosyasında.

## Bu dosya ne yapar?

Giriş yapınca sunucu sana tek bir JSON cevabı verir: içinde oturum anahtarı (`token`) ve hesabın bilgileri var. Uygulamanın
bundan sonraki her isteği o anahtarla gider ([Ag.md](Ag.md) her istekte `Oturum.anahtar`'ı okuyup `Authorization: Bearer`
başlığına koyar). Ekranların çoğu da kişiyi tanımak için sunucuya yeniden sormaz; telefondaki bu kopyaya bakar: ana sayfadaki
"Günaydın, Elif" ([AnaSayfa.md](AnaSayfa.md)), Ayarlar'daki profil kartı ([AyarlarSayfasi.md](AyarlarSayfasi.md)), sol üstteki
avatar ve portallar penceresi ([AnaEkran.md](AnaEkran.md), [PortalSecici.md](PortalSecici.md)). Bu sınıf o anahtarı ve
bilgileri saklar, geri verir, çıkışta siler.

Saklama yeri Android'in `SharedPreferences`'ıdır: uygulamaya özel (`MODE_PRIVATE`) "oturum" adlı bir ayar dosyası. Başka
uygulama okuyamaz; manifestteki yedek kuralları (`allowBackup="false"`, `res/xml/veri_aktarimi.xml` ve `res/xml/yedek_yok.xml`
ayar dosyalarını dışarıda bırakır) yüzünden buluta yedeklenmez, yeni telefona da taşınmaz. Sınıfın başındaki yorum bunu
söyler.

Telefonda üç ayrı ayar dosyası var; karıştırma:

| Dosya | Sınıf | İçinde |
|---|---|---|
| "oturum" | bu dosya | oturum anahtarı (hesaba giriş verir), `user`, çocuklar, portallar… |
| "uygulama" | [UygulamaAyar.md](UygulamaAyar.md) | uygulama anahtarı (yalnız bildirim yoklar ve sefer konumu gönderir), imleç, servis saatleri, sefer |
| "aile" | [Ayarlar.md](Ayarlar.md) | sunucu adresi, çocuğun telefonunun (Aile) anahtarı ve ayarları |

Sınıf bir araç sınıfıdır: `final`, kurucusu gizli, her işlev `static`; dışa açık her işlevin ilk argümanı bir `Context`
(içeride hep `getApplicationContext()` kullanılır, yani hangi ekrandan çağrıldığı fark etmez).

## İçinde neler var?

### Ayar dosyasındaki alanlar

| Alan | İçerik | Kim yazar |
|---|---|---|
| `anahtar` | oturum anahtarı: sunucunun `token`'ı (48 küçük onaltılık karakter) | `yaz` — cevapta `token` boş değilse |
| `user` | sunucunun `user` nesnesi, JSON metni olarak | `bilgiYaz` — cevapta `user` varsa |
| `children` | `children` dizisi (velinin çocukları) | `bilgiYaz` — cevapta varsa |
| `portallar` | `portallar` dizisi | `bilgiYaz` — cevapta varsa |
| `kapali` | `kapaliOzellikler` dizisi (okulun kapattığı bölümler) | `bilgiYaz` — cevapta varsa |
| `kvkkGuncel` | onay yürürlükteki aydınlatma metnine mi | `bilgiYaz` — cevapta varsa |
| `kvkkSurum` | yürürlükteki metnin sürümü | `bilgiYaz` — cevapta varsa (okuyan yok) |
| `kisilikSec` | yetişkin hesabı portal seçmeli mi | `bilgiYaz` — HER yazmada (cevapta yoksa `false`) |
| `cocuk` | sunucunun oturumu açtığı çocuk | `bilgiYaz` — HER yazmada (cevapta yoksa boş) |
| `seciliCocuk` | velinin uygulamada seçtiği çocuk | `cocukSec` |

### Yazan işlevler

- `yaz(c, cevap)` — sunucunun TAM oturum cevabını saklar: girişte (`POST /api/login`, iki adımlıda `POST /api/login/dogrula`)
  ve portal değişiminde (`POST /api/kisilik/gec`). Cevapta `token` varsa ve boş değilse `anahtar`'a yazılır (yoksa eski
  anahtar kalır), sonra `bilgiYaz`. Bu cevabı sunucuda `oturumCevabi` kurar (`sunucu/bolumler/kayit.md`, site deposu):
  `{ token, user, children, kapaliOzellikler, kvkkGuncel, kvkkSurum, bildirimAralikDk, portallar?, hesapAktif?, kisilikSec?,
  cocuk? }`. Uygulama `bildirimAralikDk`, `hesapAktif`, yöneticinin `yonetimAdresi`'ni saklamaz.
- `tazele(c, cevap)` — anahtarsız cevaplarla bilgiyi tazeler (yalnız `bilgiYaz`): `GET /api/me`, `POST /api/password`
  (cevap yalnız `{ user }`), `POST /api/kvkk-onay` (`{ user, kvkkGuncel: true, kvkkSurum }`); [AnaEkran.md](AnaEkran.md)'deki
  `genelHata` da 403 `kvkkGerek` gelince elle `{ kvkkGuncel: false }` verir.
- `bilgiYaz(e, j)` (iç) — tablodaki alanları yazar. `user`, `children`, `portallar`, `kapaliOzellikler`, `kvkkGuncel`,
  `kvkkSurum` yalnız cevapta varsa değişir; `kisilikSec` ve `cocuk` ise her çağrıda koşulsuz yazılır (aşağıda "Dikkat!").
  Nesne ve diziler `String.valueOf(...)` ile metne çevrilerek saklanır.
- `cocukSec(c, id)` — `seciliCocuk`'u yazar. Yalnız [PortalSecici.md](PortalSecici.md) veli portalına geçerken çağırır.
- `kapat(c)` — ayar dosyasını tamamen boşaltır (anahtar, bilgi, seçili çocuk). Yalnız [AnaEkran.md](AnaEkran.md)
  `oturumuBitir` çağırır (çıkış ve 401).

Yazmalar `apply()` ile yapılır: diske arka planda gider ama aynı süreçteki okumalar yeni değeri hemen görür.

### Okuyan işlevler

- `anahtar(c)` — oturum anahtarı; yoksa `""`. `acik(c)` — anahtar boş değil mi ("giriş yapılmış mı").
- `kisi(c)` — saklı `user` (`JSONObject`); yoksa ya da bozuksa boş nesne. `rol(c)` — `kisi(c).optString("role")`.
- `cocuklar(c)`, `portallar(c)`, `kapaliOzellikler(c)` — saklı diziler; yoksa ya da bozuksa boş dizi.
- `kvkkGuncel(c)` — saklı değer, hiç yazılmamışsa `true`. `kisilikSec(c)` — saklı değer, yoksa `false`.
- `seciliCocuk(c)` — `seciliCocuk`; o boşsa sunucunun verdiği `cocuk` (velinin "şu an baktığı" çocuk).
- `ozellikAcik(c, ozellik)` — `ozellik` (ör. `"servis"`, `"odev"`) kapalı listesinde YOKSA `true`. Sitedeki
  `ozellikAcik`'ın karşılığı (`public/js/parcalar/06-menu.js`, site deposu).
- `nesne(s)`, `dizi(s)` (iç) — metni JSON'a çevirir; `null`, `"null"` ya da bozuk metin boş nesne/dizi olur, hata fırlamaz.

### `user` ve `portallar` içinde neler gelir

`user`, sunucunun `benimGorunum`'udur (`sunucu/bolumler/kayit.md`; temeli `sunucu/yetki.js`'teki `pub`): `id`, `username`,
`email`, `fullName`, `role`, `status`, `schoolId`, `schoolName`, `schoolSlug`, `code` (öğrencinin veli kodu), `branch`, `grade`,
`city`, `district`, `address`, `phone`, `dogum`, `tc`, `tema`, `yetkiler`, `customRoleId`, `customRoleName`, `kvkkSurum`,
`createdAt` ve yalnız kişinin kendisine giden `sifreDegismeli`, `okulActi`, `yetiskin` (rolsüz ya da veli olan ana hesap),
`rolSatiri` (yetişkin hesabına bağlı öğretmen/müdür satırı). Uygulama bugün bunlardan `id`, `fullName`, `email`, `username`,
`role`, `schoolName`, `code`, `yetiskin`, `rolSatiri` ve `sifreDegismeli`'yi kullanıyor.

`portallar` yalnız yetişkin hesabında ve ona bağlı rol satırında gelir (öğrenci, servisçi, yöneticide hiç gelmez):
`[{ tur: 'rol'|'veli', id, rol, ad, alt, okulAdi, girilebilir, aktif }]` — rol satırında `id` rol satırının, veli satırında
çocuğun kimliğidir; `aktif` "oturum şu an bu portalda mı" demektir (`sunucu/bolumler/kayit.md` `portalBilgisi`).

## Kimle konuşur?

- Android: `Context.getSharedPreferences(..., MODE_PRIVATE)`, `SharedPreferences.Editor.apply()`; `org.json`
  (`JSONObject`, `JSONArray`). Başka sınıfa bağımlılığı yok.
- Yazanlar (grep):
  - [AnaEkran.md](AnaEkran.md) — `oturumAc` → `yaz` (giriş: [GirisSayfasi.md](GirisSayfasi.md), [KodSayfasi.md](KodSayfasi.md);
    portal değişimi: [PortalSecici.md](PortalSecici.md)); `genelHata` → `tazele`; `oturumuBitir` → `kapat`.
  - [EkleSayfasi.md](EkleSayfasi.md) (`Veli`: çocuk eklenince `GET /api/me` → `tazele`), [KvkkSayfasi.md](KvkkSayfasi.md)
    (`tazele`), [SifreSayfasi.md](SifreSayfasi.md) (`tazele`), [PortalSecici.md](PortalSecici.md) (`cocukSec`).
- Okuyanlar (grep):
  - [Ag.md](Ag.md) — `anahtar` (her istek); [AnaEkran.md](AnaEkran.md) — `acik`, `kvkkGuncel`, `kisi` (`sifreDegismeli`,
    avatar, `yetiskin`/`rolSatiri`), `anahtar` (çıkış).
  - [AnaSayfa.md](AnaSayfa.md) — `kisi`, `portallar`, `cocuklar`; [AyarlarSayfasi.md](AyarlarSayfasi.md) — `kisi`;
    [PortalSecici.md](PortalSecici.md) — `kisi`, `portallar`, `seciliCocuk`; [SifreSayfasi.md](SifreSayfasi.md) — `kisi`
    (`yetiskin`, `rolSatiri`).
  - Hiç çağrılmayanlar: `rol`, `kisilikSec`, `ozellikAcik` (`kapaliOzellikler` de yalnız onun içinden); `kvkkSurum` yazılır
    ama okuyan işlevi bile yok. Bunlar rol ekranları için hazır duruyor.
- Sunucu cevapları (belgeleri site deposunda): `POST /api/login`, `POST /api/login/dogrula`, `GET /api/me`, `POST /api/password`,
  `POST /api/kvkk-onay` → `sunucu/bolumler/kayit.md`; `POST /api/kisilik/gec` → `sunucu/bolumler/kisilik.md`.
- Sitedeki karşılığı: `public/js/parcalar/00-durum.js`'teki `S.token`, `S.user`, `S.children`, `S.portallar`, `S.veliCocuk`
  ve `S.kapali` (site deposu). Fark: sitede seçili çocuk sayfa yenilenince unutulur, burada ayar dosyasında kalır.

## Nasıl çalışır (adım adım)?

### Giriş ve sonrası

```
GirisSayfasi / KodSayfasi ── 200 { token, user, children, portallar, kvkkGuncel, ... }
   └─► AnaEkran.oturumAc(cevap) ─► Oturum.yaz: anahtar + bilgiler ayar dosyasına
          └─► akisiBaslat:  acik()? ─ hayır → giriş ekranı
                            kvkkGuncel()? ─ hayır → KvkkSayfasi
                            kisi().sifreDegismeli? ─ evet → SifreSayfasi(true)
                            hepsi tamam → sekmeler (AnaSayfa kisi()'den çizer)
her istek:  Ag.istek ─► Oturum.anahtar(e) ─► "Authorization: Bearer <anahtar>"
```

### Tazeleme ve çıkış

```
KvkkSayfasi onay   ─► POST /api/kvkk-onay ─► tazele({ user, kvkkGuncel: true, kvkkSurum })
SifreSayfasi       ─► POST /api/password  ─► tazele({ user })            (sifreDegismeli artık false)
EkleSayfasi.Veli   ─► GET /api/me         ─► tazele({ user, children, portallar, ... })
403 kvkkGerek      ─► AnaEkran.genelHata  ─► tazele({ kvkkGuncel: false }) ─► KvkkSayfasi
portal değişimi    ─► POST /api/kisilik/gec ─► (veli ise cocukSec(id)) ─► yaz(yeni token + bilgiler)
"Çıkış yap" / 401  ─► AnaEkran.oturumuBitir ─► kapat()  (ayar dosyası bomboş)
```

## Dikkat!

- **Sınıf yorumundaki "Anahtar sunucuda 7 gün geçerlidir" eskidi.** Uygulama girişte `uygulama: true` gönderiyor
  ([GirisSayfasi.md](GirisSayfasi.md), [KodSayfasi.md](KodSayfasi.md)); sunucu bu oturumu 30 gün geçerli açar (tarayıcıdaki 7
  gün). Süre mutlaktır, kullandıkça uzamaz; portal değiştirmek de uzatmaz (yeni oturum eskisinin açılış anını devralır,
  `sunucu/bolumler/kisilik.md`). Süre dolunca ilk istek 401 alır, [AnaEkran.md](AnaEkran.md) giriş ekranına döner.
- **`tazele` iki alanı koşulsuz sıfırlar.** `bilgiYaz` `kisilikSec`'i ve `cocuk`'u cevapta olmasa da yazar; `/api/password`,
  `/api/kvkk-onay` ve `/api/me` cevaplarında ikisi de olmadığı için her tazelemede `false` ve `""` olurlar. `kisilikSec`'i
  okuyan yok; `cocuk` ise `seciliCocuk`'un yedeği. Velinin tek çocukla doğrudan girdiği durumda (sunucu `cocuk` verir,
  `seciliCocuk` boştur) bir tazelemeden sonra "velinin baktığı çocuk" bilgisi boşalır. Bugün bunun tek görünür izi portallar
  penceresindeki "Buradasın" etiketi olabilir: `/api/me` cevabının `portallar`'ı sunucuda `cocuk` bilinmeden kurulduğu için
  veli satırlarında `aktif: false` gelir; yani velinin ilk çocuğu varken "+ Ekle" ile ikinci çocuğu eklemesinden sonra
  hiçbir veli satırında "Buradasın" görünmez (kod okumasına göre; telefonda denenmedi). Velinin çocuğa göre çalışacak rol
  ekranları `seciliCocuk`'a dayanacaksa bunu düzelt: `cocuk`'u yalnız cevapta varsa yaz.
- **Saklanan bilgi bir önbellektir, canlı değildir.** `user`, `portallar`, `children` yalnız yukarıdaki anlarda yenilenir.
  Okul seni öğretmen olarak eklese ya da okulun müdürü kaldırılıp okul kapansa uygulama bunu bir sonraki girişe, portal
  değişimine ya da çocuk eklemeye kadar görmez (`portallar` yalnız bu üç anda gelir); adın değişse `user` bu üç anda ya
  da onay ve şifre değişiminde tazelenir. Sunucu yine her istekte doğru yetkiyle davranır; yalnız ekrandaki bilgi eski
  kalır.
- **Telefonda kişisel veri durur.** `user` nesnesi T.C. kimlik no (`tc`), telefon, adres ve doğum tarihi gibi alanları da
  taşıyor ve olduğu gibi ayar dosyasına yazılıyor; uygulama bunların çoğunu kullanmıyor. Dosya uygulamaya özel ve
  yedeklenmiyor; ama şifreli değil (Android Keystore kullanılmıyor). Rootlu bir telefonda ya da deneme paketinde
  `adb shell run-as` ile okunabilir. KVKK denetiminde (iş 18) "telefonda ne saklanıyor" sorusunun cevabı bu dosya; gerekirse
  yalnız kullanılan alanlar saklanabilir.
- **Oturum anahtarı düz metin.** Anahtar hesaba tam giriş verir (uygulama anahtarından farklı olarak); kopyalanırsa 30 gün
  kullanılabilir. Bugünkü koruma yalnız Android'in uygulama yalıtımı ve yedek dışı tutulması.
- **`kvkkGuncel` hiç yazılmamışsa `true` sayılır.** Eski bir kurulumdan kalan oturumda alan yoksa uygulama onay ekranını
  göstermeden açılır; sunucu ilk istekte 403 `kvkkGerek` der ve [AnaEkran.md](AnaEkran.md) `genelHata` onay ekranını açar.
  Yani güvenlik sunucudadır, burası yalnız ilk ekranı seçer.
- **`yaz` boş `token`'da eski anahtarı korur.** Bir cevapta `token` yoksa anahtar değişmez, yalnız bilgiler değişir. Bugün
  `yaz`'a hep `token`'lı cevap gelir; `token`'sız bir cevapla `yaz` çağırma, `tazele` kullan.
- `ozellikAcik` hazır ama kullanılmıyor: okulun kapattığı bölüm (ör. Servis) için bugün hiçbir ekran gizlenmiyor, çünkü rol
  ekranları henüz yok. Rol ekranları yazılırken sekmeler ve kutucuklar bununla süzülmeli (sitede `06-menu.js` `sayfaAcik`).

## Testleri

- Android deposunda otomatik test yok; derleme ve lint (`./gradlew --offline assembleDebug lintDebug`) ana oturumda.
- Bu dosyanın sakladığı cevapların biçimini site deposundaki testler korur:
  - `testler/test-yetiskin.js` — girişte, `/api/me`'de ve `POST /api/kisilik/gec` cevabında `portallar` (`tur`, `ad`, `alt`,
    `okulAdi`, `girilebilir`, `aktif`), rol satırına geçince `hesapAktif: false` ve rol portalının `aktif` olması, veliye
    geçince `cocuk`'un gelmesi, geçişte eski oturumun kapanması (401).
  - `testler/test-yonetim.js` — okulun T.C. no ile açtığı hesapta `user.sifreDegismeli: true`, şifre değişmeden 403
    `sifreDegismeli`, değişince `false`; aydınlatma onayı eski kişiye 403 `kvkkGerek`.
  - `testler/test-servis-yoklama.js` — `uygulama: true` ile açılan oturumun 30 gün sürmesi, çıkışta kapanması.
- Elle (öykünücü, deneme paketi): giriş yap, uygulamayı kapatıp aç → giriş ekranı gelmemeli, ana sayfa adınla açılmalı.
  Ayarlar → "Çıkış yap" → uygulamayı kapatıp aç → giriş ekranı. Ayar dosyasının varlığına
  `adb shell run-as org.egitimevi.aile ls shared_prefs` ile bakabilirsin ("oturum.xml"); içinde oturum anahtarı ve kişisel
  bilgiler olduğu için içeriğini bir yere yapıştırma.

## Son durum

- `git log` (Android deposu): 1 commit. Dosya `e96c5f2 commit 6` (2026-09-26) ile geldi: uygulama siteyi içinde açan
  WebView'den kendi ekranlarını çizen yerel uygulamaya dönerken oturum artık sitenin değil uygulamanın elinde olduğu için
  yazıldı (anahtar, `user`, `children`, `portallar`, kapalı bölümler, onay bilgisi, seçili çocuk). O günden beri değişmedi;
  sonraki commit'ler (`commit 7`–`12`) başka dosyalara, `commit 13`–`15` (2026-10-03) komşu `.md` belgelerine aitti.
- Bilinen açıklar (kod değiştirilmedi): yorumdaki 7 günün eskimesi, `tazele`'nin `cocuk`'u sıfırlaması, kullanılmayan
  okuyucular, `user`'ın bütün kişisel alanlarıyla saklanması.
- Planlı işlerden bu dosyaya dokunması beklenenler:
  - "Android yerel uygulama" (rol ekranları): sekmelerin `ozellikAcik` ile süzülmesi, velinin üstteki çocuk seçicisinin
    `seciliCocuk`/`cocukSec`'i kullanması; sol menüdeki "hesap adı + portallar + Çıkış" da buradan okuyacak.
  - "Tek kişi tek hesap + portallar öğrencide de" (iş 19): portallar öğrenci ve servisçide de gelecek; `portallar`'ın
    yalnız yetişkinde dolu olduğu varsayımı (ve `yetiskin`/`rolSatiri` denetimleri) değişecek.
  - "T.C. kimlik no bütün hesaplarda zorunlu" (iş 32): T.C.'si olmayan eski hesaba girişten sonra atlanamaz bir adım
    gelecek; `akisiBaslat`'taki onay/şifre adımları gibi bir bilgi `user`'da ya da cevapta taşınacak.
  - "Güvenlik denetimi" (iş 3): okulun verdiği her şifrede ilk girişte değiştirme → `sifreDegismeli` daha çok hesapta
    gelecek; "Sistem" (iş 4): yöneticiye zorunlu iki adımlı doğrulama, yeni cihaz uyarısı ve açık oturumlar.

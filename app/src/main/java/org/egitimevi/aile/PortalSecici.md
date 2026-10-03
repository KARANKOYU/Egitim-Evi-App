# app/src/main/java/org/egitimevi/aile/PortalSecici.java

"Portalların" penceresi ve satırları: yetişkin hesabının okul rolleri ("Öğretmen · Çınar Ortaokulu", "Müdür · …") ve
çocukları ("Veli · Elif Kaya") alt alta; dokunulan portala sunucu yeni oturum açar; altta "+ Ekle" ve "Ayarlar".

## Bu dosya ne yapar?

Eğitim Evi'nde bir yetişkin hesabı aynı anda birkaç yerde olabilir: A okulunda öğretmen, B okulunda müdür, iki çocuğun
velisi. Her biri ayrı bir **portal**dır ve kişi bir anda yalnız birinin içindedir. Sitede bu liste sol menünün en üstündeki
"Portallarım"dır (`public/js/parcalar/08c-kisilikler.js`, site deposu); uygulamada bu dosya.

Dosya üç iş yapar:

1. **Pencere** (`goster`): üstte kişinin avatarı, adı ve e-postası (yoksa kullanıcı adı), "PORTALLARIN" etiketi, portal
   satırları, altta "+ Ekle" ve "Ayarlar" düğmeleri.
2. **Satırlar** (`satirlar`): listeyi telefondaki oturumdan ([Oturum.md](Oturum.md) `portallar`) çizer; oturumda liste boşsa
   sunucudan (`GET /api/kisilikler`) kurar. Aynı satırlar [AnaSayfa.md](AnaSayfa.md)'da da kullanılır.
3. **Geçiş** (`gec`): `POST /api/kisilik/gec` ile o portala geçer. Sunucu eski oturum anahtarını kapatıp yenisini verir;
   uygulama yeni cevabı saklar ve sekmeleri baştan kurar.

Kim görür: yetişkin hesabıyla (`user.yetiskin`) ya da ona bağlı bir okul rolüyle (`user.rolSatiri`) giren herkes — veli,
öğretmen, müdür ve henüz portalı olmayan yetişkin. Öğrenci, servisçi ve yönetici görmez: sol üstteki avatar onları doğrudan
Ayarlar'a götürür ([AnaEkran.md](AnaEkran.md) `hesapMenusu`).

## İçinde neler var?

Sınıf paket içidir (`final class`, gizli kurucu, üç `static` işlev ve bir iç işlev).

### `goster(e)` — portallar penceresi

- Bir `ScrollView` içinde dikey gövde (kenarlardan 18 dp, altta 12 dp).
- Üst satır: 44 dp avatar (`Arayuz.avatar`, kişinin `fullName`'i ve rengi için `id`'si), yanında ad (`altBaslik`) ve altında
  soluk yazıyla e-posta; e-posta boşsa kullanıcı adı.
- "Portalların" bölüm etiketi (20 dp üst boşluk; `bolumEtiketi` büyük harfe çevirir), altında satırların kutusu.
- Pencere bir `AlertDialog` (başlıksız, yalnız bu görünüm). Satırlar `satirlar(e, liste, d::dismiss)` ile doldurulur: bir
  satıra dokununca önce pencere kapanır.
- "+ Ekle" (`Dugme.IKINCIL`) → pencere kapanır, [EkleSayfasi.md](EkleSayfasi.md) açılır (çocuk ekle, öğretmen olarak katıl,
  okulunu açtır).
- "Ayarlar" (`Dugme.HAYALET`) → pencere kapanır, [AyarlarSayfasi.md](AyarlarSayfasi.md) açılır.
- Pencere gösterildikten sonra zemini kart renginde, 28 dp yuvarlak köşeli yapılır (`Tema.zemin(..., Tema.R_BUYUK, ...)`).

### `satirlar(e, kap, kapat)` — portal satırlarını bir kutuya çizer

- `Oturum.portallar(e)` doluysa doğrudan `ciz`.
- Boşsa kutuya "Yükleniyor..." yazar ve `GET /api/kisilikler` ister. Cevaptan aynı biçimde bir liste kurar:
  - her rol (`roller[]`): `{ tur: "rol", id, ad: "Müdür" (rol "principal" ise) ya da "Öğretmen", alt: okulAdi, girilebilir,
    aktif: id === cevabın "aktif"i }`;
  - her çocuk (`cocuklar[]`): `{ tur: "veli", id, ad: "Veli", alt: çocuğun adı, okulAdi, girilebilir: true }`.
  Hata olursa kutuda yalnız hatanın iletisi kalır.
- `kapat` bir satıra dokununca (geçişten önce) çalışan iş: pencerede `dismiss`, ana sayfada `null`.

### `ciz(e, kap, l, kapat)` (iç) — satır kuralları

Liste boşsa: "Henüz bir portalın yok. + Ekle ile başla." Değilse her portal için bir satır:

| | Rol portalı | Veli portalı |
|---|---|---|
| Sol | simge: `ad` "Müdür" ise `ik_mudur`, değilse `ik_ogretmen` (26 dp, ana renk) | çocuğun 36 dp avatarı (adı, kimliği) |
| Başlık | "Öğretmen · Çınar Ortaokulu" (`ad · alt`) | "Veli · Elif Kaya" |
| Alt yazı | yok | çocuğun okulu (`okulAdi` boş değilse) |

- **Buradasın** (yeşil etiket): satırın `aktif`'i `true` ise; ya da veli satırıysa, kişi rol satırında DEĞİLSE ve bu çocuk
  `Oturum.seciliCocuk` ise. Bu satıra dokunulamaz.
- **Onay bekliyor** (turuncu etiket): `girilebilir` `false` ise. Bu satıra da dokunulamaz.
- Öteki satırlar dokunulabilir (`Arayuz.tiklananSatir`, sağda ok): dokununca `kapat.run()` (varsa), sonra `gec`.

### `gec(e, tur, id)` — portala geçiş

`POST /api/kisilik/gec { tur, id }`. Başarıda: veli portalıysa `Oturum.cocukSec(id)`; sonra `e.oturumAc(cevap)` —
[AnaEkran.md](AnaEkran.md) yeni anahtarı ve bilgileri saklar ([Oturum.md](Oturum.md) `yaz`), `akisiBaslat` ile sekmeleri baştan
kurar (her sekmenin yığını sıfırlanır, "Ana sayfa" açılır). Hata: alttaki kısa mesaj (`e.bildir`). Gövde JSON'u kurulamazsa
sessizce hiçbir şey yapmaz.

## Kimle konuşur?

- Çağırdıkları (aynı paket):
  - [Arayuz.md](Arayuz.md) — `dikey`, `yatay`, `avatar`, `altBaslik`, `soluk`, `bolumEtiketi`, `ekle`, `dugme` (`IKINCIL`,
    `HAYALET`), `ikon`, `etiket` (`YESIL`, `TURUNCU`), `satir`, `tiklananSatir`.
  - [Tema.md](Tema.md) — `dp`, `zemin`, `renk`, `R_BUYUK`; kaynaklar `R.color.kart`, `R.color.ana`, `R.drawable.ik_mudur`,
    `R.drawable.ik_ogretmen`.
  - [Oturum.md](Oturum.md) — `kisi` (`fullName`, `id`, `email`, `username`, `rolSatiri`), `portallar`, `seciliCocuk`, `cocukSec`.
  - [Ag.md](Ag.md) — `get` (`/api/kisilikler`), `post` (`/api/kisilik/gec`); [AnaEkran.md](AnaEkran.md) — `git`, `oturumAc`,
    `bildir`.
  - Sayfalar: [EkleSayfasi.md](EkleSayfasi.md), [AyarlarSayfasi.md](AyarlarSayfasi.md).
- Android: `AlertDialog`, `ScrollView`, `LinearLayout`, `TextView`, `View`; pencere zemini için `Window.setBackgroundDrawable`.
- Onu çağıranlar (grep):
  - [AnaEkran.md](AnaEkran.md) `hesapMenusu` — sol üstteki avatara dokununca `goster` (yalnız `yetiskin` ya da `rolSatiri`).
  - [AyarlarSayfasi.md](AyarlarSayfasi.md) — "Portallarım" satırı → `goster` (yine yalnız onlarda görünür).
  - [AnaSayfa.md](AnaSayfa.md) — yetişkin hesabının kendisindeyken ve birden çok portal varken "Portalların" kartı:
    `satirlar(e, kart, null)`.
- Sunucu uçları (belgesi site deposunda `sunucu/bolumler/kisilik.md`):
  - `GET /api/kisilikler` → `{ roller: [{ id, rol, okulAdi, okulKisaAd, durum, girilebilir }], cocuklar: [{ id, ad,
    okulAdi }], hesap, aktif: <rol satırının kimliği> | "hesap", kisiKodu }`. Yan etkisizdir. Yetişkin hesabı ya da rol
    satırı değilse 403 ("Bu işlem yetişkin hesabıyla yapılır. Hesabını okul yönetimi düzenler."; yöneticide "Yönetici
    hesabında portal seçimi yok.").
  - `POST /api/kisilik/gec { tur: "rol" | "veli" | "hesap", id }` → yeni oturum cevabı (`token`, `user`, `children`,
    `portallar` …; veli geçişinde `cocuk`). Hesap başına dakikada 60 (429 "Çok hızlı. Biraz bekle."). Başkasının rolü 404 "Bu
    rol hesabında yok."; onaysız rol ya da kapalı okul 403 ("Bu okul şu an kapalı; sistem yöneticisi yeni müdürünü atayınca
    açılır." / "Bu role şu an girilemez."); bağlı olmayan çocuk 404 "Bu öğrenci hesabına bağlı değil."; başka `tur` 400
    "Geçersiz seçim". Eski anahtar hemen kapanır; yeni oturum eskisinin türünü (uygulama: 30 gün) ve açılış anını devralır.
    `tur: "hesap"`'ı uygulama bugün göndermiyor.
  - Satırların `girilebilir`/`aktif` kuralı sunucuda `kisilikListesi` ve `portalBilgisi`'dedir (`sunucu/bolumler/kayit.md`):
    `girilebilir` = rol satırı onaylı VE okulu açık.

## Nasıl çalışır (adım adım)?

```
avatar / Ayarlar "Portallarım"
   └─► goster(e): AlertDialog [avatar · ad · e-posta]  PORTALLARIN  [satırlar]  [+ Ekle] [Ayarlar]
          satirlar(e, liste, dismiss)
             Oturum.portallar dolu? ── evet ─► ciz
                                    └ hayır ─► "Yükleniyor..." ─► GET /api/kisilikler ─► listeyi kur ─► ciz
satıra dokun ("Müdür · Deniz Lisesi")
   └─► pencere kapanır ─► gec("rol", id) ─► POST /api/kisilik/gec
          200 ─► (veli ise Oturum.cocukSec) ─► AnaEkran.oturumAc ─► Oturum.yaz ─► akisiBaslat ─► sekmeler baştan
          hata ─► alttaki kısa mesaj
```

Ana sayfadaki kart aynı `satirlar`'ı pencere olmadan kullanır; oraya ancak oturumda iki ya da daha çok portal varken
girildiği için ağa hiç gitmez.

## Dikkat!

- **Liste çoğu zaman telefondaki kopyadır.** `satirlar` sunucuya yalnız oturumda hiç portal yoksa sorar. Portallar girişte,
  portal değişiminde ve çocuk eklenince (`/api/me`) yenilenir; arada müdür seni okula eklese, okul kapansa ya da bir rol
  onaylansa pencere bunu göstermez (yeni portal görünmez, eski etiket kalır). Dokunulan satır yine sunucuda denetlenir;
  yanlış bir geçiş olmaz, en çok bir hata iletisi çıkar. (Kod okumasına göre.)
- **"Onay bekliyor" etiketi yanıltıcı olabilir.** `girilebilir: false`, rol satırı onaylı değilken de okulun müdürü
  kaldırılıp okul kapandığında da gelir. Site aynı durumda "okul kapalı" diyor (`08c-kisilikler.js`). Sıradaki işlerdeki
  "Müdürü yok" düzeltmesi (iş 5) bu metni de kapsamalı.
- **Veli portalları arasında geçmek her seferinde yeni oturumdur.** Sitede veli portalı yetişkin hesabının kendisidir; çocuk
  değiştirmek sunucuya gitmez, yalnız seçili çocuk değişir. Uygulama ise her veli satırında `POST /api/kisilik/gec` yapar;
  sunucu bunu kabul eder (eski anahtarı kapatıp yenisini verir), sonuç doğrudur ama her dokunuşta sekmeler baştan kurulur.
- **"Buradasın" iki kaynağa bakar:** sunucunun `aktif` bayrağı ve telefondaki `seciliCocuk`. [Oturum.md](Oturum.md)'deki
  `tazele` `cocuk`'u sıfırladığı için tek çocukla doğrudan giren velide, bir tazelemeden sonra hiçbir satır "Buradasın"
  demeyebilir; satırlar dokunulabilir kalır, dokunmak yalnız yeni bir oturum açar.
- **Geçiş anında yoldaki istek oturumu düşürebilir.** Sunucu eski anahtarı hemen kapatır. Geçiş cevabı gelmeden önce eski
  anahtarla gönderilmiş bir istek (ör. o an yüklenen bir sayfa) 401 dönerse ve cevabı yeni anahtar saklandıktan SONRA
  gelirse, [Ag.md](Ag.md) → `genelHata` bunu "oturumun süresi doldu" sayıp kişiyi çıkarır. Pencere açıkken arkada istek
  nadirdir; olasılık düşük ama sıfır değil (kod okumasına göre).
- **Yedek listenin farkları:** `/api/kisilikler`'den kurulan listede müdür dışındaki her rol "Öğretmen" adını ve simgesini alır
  (sunucunun `portallar`'ında bilinmeyen rol "Okul" adını alır); rol satırlarında okulun kısa adı (`okulKisaAd`) kullanılmaz.
- **"Ayarlar" ve "+ Ekle" o anki sekmenin yığınına iter.** Ayarlar sekmesindeyken pencereden "Ayarlar"a basmak ikinci bir
  Ayarlar sayfası açar (geri ile kapanır).
- **Hata iletisi pencere kapandıktan sonra çıkar.** Satıra dokununca pencere hemen kapanır; geçiş başarısızsa (ör. 429)
  kişi yalnız alttaki kısa mesajı görür, pencere yeniden açılmaz.
- Pencere, dışına dokununca ya da geri tuşuyla kapanır (`AlertDialog`'un varsayılanı). Yedek liste yüklenirken pencere
  kapanırsa cevap görünmeyen kutuya çizilir; zararsızdır.
- Satırdaki ve pencere başındaki adlar oturumdan gelir; kişi rol satırındayken `kisi` rol satırının bilgisidir. Rol
  satırının e-postası yoktur (şemada rol satırı e-postasız tutulur, `012-yetiskin-hesap.sql`), bu yüzden öğretmen ya da müdür
  portalındayken pencerenin başında yetişkin hesabının e-postası yerine rol satırının okuldaki kullanıcı adı görünür.

## Testleri

- Android deposunda otomatik test yok; derleme ve lint (`./gradlew --offline assembleDebug lintDebug`) ana oturumda.
- Sunucu tarafını site deposundaki testler korur:
  - `testler/test-yetiskin.js` — girişte ve `/api/me`'de `portallar` (`tur`, `ad`, `alt`, `okulAdi`, `girilebilir`,
    `aktif`), öğretmen rolüne geçiş (yeni `token`, rol portalı `aktif`, `hesapAktif: false`), eski anahtarın 401 olması,
    veli portalına geçiş (`cocuk`, o çocuğun portalı `aktif`), başkasının rolüne / bağlı olmayan çocuğa / bozuk kimliğe
    geçilememesi (404), iki okulda iki rol (`/api/kisilikler`'de iki girilebilir rol).
  - `testler/test-servis-yoklama.js` — portal değişince uygulama oturumunun 30 günlük süresinin uzamaması.
  - `testler/test-kisi-kodu.js`, `testler/test-veli-coklu.js`, `testler/yetki-denetimi.js` — `/api/kisilikler`'i de çağırır.
- Elle (öykünücü, deneme paketi): bir yetişkin hesabını bir okula öğretmen olarak ekle ve bir çocuğa veli yap; uygulamada
  gir → avatar → pencerede iki satır; öğretmen satırına dokun → sekmeler baştan kurulur, ana sayfada "Öğretmen · <okul>";
  avatar → öğretmen satırında "Buradasın". Okulun müdürünü sistem yöneticisi kaldırınca (okul kapanınca) satırda turuncu
  etiket ve dokunulamaz olmalı (yeni girişten sonra).

## Son durum

- `git log` (Android deposu): 1 commit. Dosya `e96c5f2 commit 6` (2026-09-26) ile geldi: yerel uygulamanın çekirdeği yazılırken
  sitenin "Portallarım" listesinin karşılığı olarak eklendi (pencere, satırlar, `/api/kisilikler` yedeği, geçiş). O günden
  beri değişmedi.
- Bilinen açıklar (kod değiştirilmedi): listenin bayatlaması, "Onay bekliyor" metni, veli geçişinin yeni oturum açması,
  geçiş anındaki olası 401 yarışı.
- Planlı işlerden bu dosyaya dokunması beklenenler:
  - "Paneller + okul gezgini …" (iş 5): tanımı sitedeki "Müdür bekliyor" / "Giremiyor" kalıntılarını "Müdürü yok" +
    "Müdür ata"ya çeviriyor; buradaki "Onay bekliyor" da aynı düzeltmeye alınmalı (öneri).
  - "Tek kişi tek hesap + portallar öğrencide de" (iş 19): öğrenci (okul + dershane) ve servisçi de portal sahibi olacak;
    bu pencere yalnız yetişkinde açıldığı ve satırlar yalnız "rol"/"veli" türünü tanıdığı için genişleyecek; bildirimde kurum
    adı.
  - "Android yerel uygulama" (iş 10): sol menüde "hesap adı + portallar + Çıkış"; velide çocuk seçici (portal değiştirmeden
    çocuk değiştirme sitedeki gibi olabilir).
  - "Üst şerit sadeleştirme" (iş 29): sitede Portallarım profil menüsüne taşınıyor; uygulamadaki avatar düzeni de buna
    göre gözden geçirilecek.

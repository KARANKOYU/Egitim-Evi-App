# app/src/main/java/org/egitimevi/aile/EkleSayfasi.java

Yetişkin hesabının "+ Ekle" sayfası: Veli (çocuğun veli koduyla çocuğunu ekler), Öğretmen (kişi kodunu okulunun müdürüne
verir) ve Müdür (kişi kodunu yöneticiye verip okulunu açtırır); iki alt sayfası `EkleSayfasi.Veli` ve `EkleSayfasi.Kod`.

## Bu dosya ne yapar?

Eğitim Evi'nde veli, öğretmen ve müdür aynı **yetişkin hesabını** açar; hesabın başta hiçbir rolü (portalı) yoktur. Rol sonradan
"+ Ekle" ile gelir (site deposunda `sunucu/bolumler/kayit.md` başındaki açıklama, `sunucu/bolumler/kisilik.md`):

- **Veli** — çocuğun **veli kodunu** (öğrencinin hesabında yazan 16 karakterlik kod) girer, çocuğu hesabına eklenir ve veli
  portalı açılır. Sunucu bunu hemen yapar.
- **Öğretmen** — kendi **kişi kodunu** okulunun müdürüne verir; müdür kodu sitede girince kişi o okula öğretmen olarak eklenir.
  Uygulamadaki iş yalnız kodu göstermek, kopyalatmak ve gerekirse yenisini üretmektir.
- **Müdür** — aynı kişi kodunu sistem yöneticisine verir; yönetici okulu açıp kişiyi müdür yapar. Sayfa ayrıca yöneticinin
  iletişim bilgilerini (e-posta, telefon) gösterir.

Bu sınıf "Ne eklemek istiyorsun?" seçim sayfasıdır; asıl işi iki iç sınıfı yapar: `Veli` (kod girme formu) ve `Kod` (kişi kodunu
gösteren sayfa; öğretmen ve müdür için aynı sayfa, metinleri farklı).

Kim görür: yalnız yetişkin hesabı ya da ona bağlı okul rolü (öğretmen/müdür satırı). Üç yerden açılır: Ayarlar'daki "Ekle"
satırı ([AyarlarSayfasi.md](AyarlarSayfasi.md); yalnız `yetiskin`/`rolSatiri`), portallar penceresindeki "+ Ekle"
(`PortalSecici.java`) ve rolü olmayan yetişkinin ana sayfasındaki "+ Ekle" ([AnaSayfa.md](AnaSayfa.md)).

## İçinde neler var?

### `EkleSayfasi` (seçim sayfası)

- `baslik()` → "Ekle".
- `olustur()` → büyük başlık "Ne eklemek istiyorsun?" ve üç seçenek kartı:
  - "Veli" (simge `ik_veli`) — "Çocuğunun veli kodunu gir; ödevini, notunu, servisini gör." → `e.git(new Veli())`;
  - "Öğretmen" (`ik_ogretmen`) — "Kişi kodunu okulunun müdürüne ver; seni okula ekler." → `e.git(new Kod(false))`;
  - "Müdür" (`ik_okul`) — "Okulunu Eğitim Evi'ne açtır; yönetici okulunu açıp seni müdür yapar." → `e.git(new Kod(true))`.
- `secenek(simge, ad, aciklama, tik)` (iç) — kart: solda 52 dp açık ana renkli yuvarlak köşeli kutuda 26 dp simge, başlık 18 sp,
  açıklama, sağda ok; hem kart hem içindeki satır dokununca aynı işi yapar.

### `EkleSayfasi.Veli` (çocuğunu ekle)

- `baslik()` → "Çocuğunu ekle".
- `olustur()` → başlık "Veli kodunu gir", açıklama "Kodu çocuğunun okulundan alırsın (giriş bilgisi kâğıdında ya da öğrencinin
  Ayarlar'ında). Birden çok çocuğun varsa her birini ayrı ekle.", kartta:
  - "Veli kodu (16 karakter)" kutusu: ipucu `KisiKodu.ORNEK` ("Ab3#-kQx9-+mPt-7?zR"), eşaralıklı yazı tipi (`Tema.KOD`),
    klavye türü görünür parola + öneri yok (harfler noktalanmaz, klavye düzeltmeye kalkmaz). `KisiKodu.kutuyaBagla` kutuya
    biçimleyici bağlar: her 4 karakterden sonra tire kendiliğinden gelir, 16 karakterden fazlası yazılmaz, "Veli kodu: …"
    gibi bir metin yapıştırılırsa içindeki kod ayıklanır.
  - altında "Büyük/küçük harfe dikkat et; tireler kendiliğinden gelir.";
  - "Çocuğumu ekle" (birincil düğme).
- `ekle()` (iç):
  1. Kutudaki değerden ayırıcılar atılır (`KisiKodu.sade`: boşluk, tire ve benzerleri). Sonuç tam 16 karakter değilse
     "Veli kodu 16 karakterdir." (kutunun altında) ve dur.
  2. Düğme meşgul; `POST /api/kisilik/cocuk { code: <16 karakter> }`.
  3. Başarıda hemen `GET /api/me` → `Oturum.tazele` (hesap bilgisi, çocuklar, portallar güncellenir), alt mesaj sunucunun
     iletisi (ör. "<öğrencinin adı> hesabına eklendi."; gelmezse "Çocuğun eklendi."), sonra `e.yenidenKur()`: bütün sekmeler
     baştan kurulur, kişi Ana sayfa'ya döner. `/api/me` hata verirse ileti gösterilmeden yalnız `yenidenKur()`.
  4. Hatada düğme serbest, sunucunun iletisi kutunun altında (ör. "Bu koda sahip bir öğrenci bulunamadı. Kodu öğrencinin
     Ayarlar sayfasından kontrol et.").

### `EkleSayfasi.Kod` (kişi kodu; `Sayfa.Veri`)

- Yapıcı `Kod(boolean mudur)` — `mudur` müdür (okul açtırma) kipi mi.
- `baslik()` → "Okulunu açtır" (müdür) ya da "Öğretmen olarak katıl".
- `adres()` → `"/api/kisilikler"`; `yenilenir()` → `false` (üst çubukta "Yenile" yok).
- `ciz(JSONObject j)` — cevaptaki `kisiKodu` (yoksa `ogretmenKodu`, o da yoksa boş):
  - başlık "Kişi kodun"; açıklama müdürde "Yöneticimize okulunun adını ve bu kodu ver. Okulunu ve adresini açıp seni müdür
    yapar; okul portalların arasında görünür.", öğretmende "Bu kodu okulunun müdürüne ver. Müdür kodu girince seni okula
    öğretmen olarak ekler; okul portalların arasında görünür.";
  - kartta `KisiKodu.kutu` (4'erli tireli kod + "Kopyala" → "Kod kopyalandı."), "Kod bir kez kullanılır; seni ekleyince
    yenilenir. Kodu yalnızca vermek istediğin kişiye ver." ve "Yeni kod üret" (`HAYALET`): `POST /api/kisilik/kod` → alt mesaj
    "Yeni kod üretildi; eskisi artık geçmez." ve `yukle()` (kod yeniden istenir); hatada düğme serbest ve iletisi alt mesajda;
  - müdür kipinde ek kart "Yöneticiye ulaş": `GET /api/site` → `iletisimCiz(yer, site.iletisim)`; hata olursa
    `iletisimCiz(yer, null)`.
- `iletisimCiz(yer, il)` (iç) — e-posta da telefon da boşsa "İletişim bilgisi sitenin alt bilgisinde duruyor."; e-posta varsa
  dokunulabilir satır (simge `ik_posta`, alt yazı "E-posta yaz") → `mailto:<adres>?subject=Okulumu Eğitim Evi'ne açtırmak
  istiyorum`; telefon varsa satır (`ik_telefon`, "Ara") → `tel:` (rakamlar ve `+` dışındaki her şey atılır).
- `ac(Uri u)` (iç) — `Intent.ACTION_VIEW`; açacak uygulama yoksa "Bunu açacak uygulama yok.".

## Kimle konuşur?

- Aynı paketten çağırdıkları:
  - [Arayuz.md](Arayuz.md) — `sayfaGovdesi`, `baslik`, `altBaslik`, `yazi`, `soluk`, `kart`, `dikey`, `ekle`, `ikon`,
    `tiklananSatir`, `alan`, `dugme`, `mesgul`; `Tema.java` — `zemin`, `renk`, `dp`, `KOD`; renkler `ana`, `ana_acik`, `soluk`;
    simgeler `ik_veli`, `ik_ogretmen`, `ik_okul`, `ik_posta`, `ik_telefon`.
  - `KisiKodu.java` — `ORNEK`, `UZUNLUK` (16), `sade`, `kutuyaBagla`, `kutu`.
  - `Sayfa.java` (`Sayfa`, `Sayfa.Veri`: `adres`, `ciz`, `yukle`); [Ag.md](Ag.md) — `get`, `post`.
  - `Oturum.java` — `tazele`; [AnaEkran.md](AnaEkran.md) — `git`, `bildir`, `yenidenKur`, `startActivity`.
- Sunucu uçları (site deposunda):
  - **`POST /api/kisilik/cocuk { code }`** (`sunucu/bolumler/kisilik.md`; iş `sunucu/bolumler/veli.md` `cocukBagla`'da) —
    yalnız yetişkin hesabı (öğrenci/servisçiye 403 "Bu işlem yetişkin hesabıyla yapılır…"). Kod büyük/küçük harf duyarlı,
    boşluk ve tireler silinir. Hesap başına dakikada 5 deneme ve IP başına saatte 30 yanlış kod (429); kod bulunamazsa, kendini
    ya da zaten ekli çocuğu eklemeye kalkınca 400. Rolsüz hesap burada veli olur; öğrenciye bildirim gider; öğrencinin veli kodu
    kullanılınca YENİLENMEZ (anne ve baba aynı kodla ekler). Cevap: güncel portal listesi + `message`.
  - **`GET /api/me`** (`sunucu/bolumler/kayit.md`) — `{ user, children, kapaliOzellikler, kvkkGuncel, kvkkSurum, …,
    portallar?, hesapAktif? }`.
  - **`GET /api/kisilikler`** (`kisilik.md`) — `{ roller, cocuklar, hesap, aktif, kisiKodu }`; `kisiKodu` tiresiz ham kod. Yan
    etkisiz (kod yazmaz).
  - **`POST /api/kisilik/kod`** (`kisilik.md`) — hesap başına saatte 10 (429 "Kodu çok sık yeniledin. Biraz sonra dene.");
    yeni kod üretir → `{ kisiKodu, message }`.
  - **`GET /api/site`** (`sunucu/site.md`; herkes) — `{ sayilar, iletisim: { eposta, telefon }, … }`; iletişim bilgisi yönetim
    panelindeki site ayarlarından gelir.
  - Kapılar (`sunucu/api.md`): `kisilik`, `kisilikler` rolsüz yetişkine açık; aydınlatma onayı eskiyse 403 `kvkkGerek` →
    [AnaEkran.md](AnaEkran.md) onay ekranını açar.
  - Kodun öbür ucu sitededir: müdür öğretmeni kişi koduyla ekler (`sunucu/bolumler/hesaplar.md`), yönetici okulu açıp müdürü
    atar (`sunucu/bolumler/yonetici-okul.md`).
- Onu açanlar (grep): [AyarlarSayfasi.md](AyarlarSayfasi.md) ("Ekle" satırı), `PortalSecici.java` ("+ Ekle"),
  [AnaSayfa.md](AnaSayfa.md) (rolsüz yetişkinin "+ Ekle" düğmesi).
- Android: `Intent.ACTION_VIEW` (`mailto:`, `tel:`), `Uri`, `InputType`.

## Nasıl çalışır (adım adım)?

### Veli: çocuğu ekle

```
Ekle → "Veli" → Veli sayfası
   kutuya "Ab3#kQx9+mPt7?zR" yaz ya da "Veli kodu: Ab3#-kQx9-+mPt-7?zR" yapıştır → kutu: Ab3#-kQx9-+mPt-7?zR
   "Çocuğumu ekle" → sade → 16 karakter mi? ── hayır ─► "Veli kodu 16 karakterdir."
                                            └ evet ─► POST /api/kisilik/cocuk { code }
        400/429 → kutunun altında sunucunun iletisi
        200     → GET /api/me → Oturum.tazele → "<ad> hesabına eklendi." → yenidenKur (sekmeler baştan, Ana sayfa)
```

(Kodlar `KisiKodu.ORNEK`'in biçimidir; gerçek bir kod değildir.)

### Öğretmen / Müdür: kişi kodunu ver

```
Ekle → "Öğretmen" (Kod(false))  ya da  "Müdür" (Kod(true))
   GET /api/kisilikler → kisiKodu → [ABCD-EFGH-…] [Kopyala]
   "Yeni kod üret" → POST /api/kisilik/kod → "Yeni kod üretildi; eskisi artık geçmez." → GET /api/kisilikler (yeniden çiz)
   müdür: GET /api/site → iletisim → [e-posta yaz] [ara]
sonra (sitede): müdür kodu girer → kişi o okulda öğretmen olur ; yönetici okulu açar → kişi müdür olur
   → uygulamada portal listesinde görünür (portallar penceresi, ana sayfa)
```

## Dikkat!

- **Başarıdan sonra her şey baştan kurulur.** `yenidenKur()` bütün sekmeleri ve yığınları sıfırlar: Ekle sayfası ve onu açan
  sayfa kapanır, kişi Ana sayfa'ya döner. Rolsüz yetişkin artık veli olduğu için ana sayfası da değişir.
- **`/api/me` hata verirse başarı söylenmez.** Çocuk sunucuda eklendiği hâlde "…hesabına eklendi." çıkmaz, sekmeler eski hesap
  bilgisiyle yeniden kurulur; portallar ancak bir sonraki tazelemede (giriş, portal değişimi) görünür (kod okumasına göre).
- **İstemci yalnız uzunluğa bakar.** Kodun biçimi (ilk karakter harf; büyük harf, küçük harf, rakam ve işaretin her biri)
  burada denetlenmez; biçimsiz ama 16 karakterlik bir kod da sunucuya gider. Biçimi sunucu denetler: `cocukBagla` kodu
  `kisiKoduSade` (site deposunda `sunucu/ortak.js`) ile sadeleştirir, desene uymayan kod boş sayılır ve öğrenci hiç aranmadan
  "Bu koda sahip bir öğrenci bulunamadı…" (400) döner. Bu deneme yine hem hesabın dakikada 5 denemesine hem bağlantının saatte
  30 yanlış kod sınırına sayılır. Büyük/küçük harf önemlidir: harfi ters yazılmış kod reddedilir.
- **`ogretmenKodu` yedeği ölü.** Sunucu bugün yalnız `kisiKodu` döndürüyor: alanın eski adı `ogretmenKodu` site deposunun
  `0acca75 commit 516`'sında (2026-09-27, kişi kodu) `kisiKodu` oldu. `ogretmenKodu` bugün sitenin sunucu ve ön yüz kodunda hiç
  geçmiyor; tek geçtiği yer `testler/test-kisi-kodu.js`, o da cevapta bu alanın OLMADIĞINI denetliyor. Yedek bu yüzden hiç
  devreye girmez.
- **Kod boşsa kutu boş görünür.** Hesabın kişi kodu yoksa (`kisiKodu` boş) kutu boş çizilir; "Yeni kod üret" bir kod verir.
  Bugün hesap e-posta onayıyla açılırken kod üretiliyor, boş kod yalnız eski hesaplarda beklenir.
- **"Yeni kod üret" sunucunun döndürdüğü kodu kullanmaz:** sayfayı `GET /api/kisilikler` ile yeniden ister (iki istek). Alt
  mesaj uygulamanın kendi cümlesidir ("…artık geçmez."), sunucununki "…artık çalışmaz." — anlam aynı.
- **Her çizimde `/api/site` yeniden istenir** (müdür kipinde; "Yeni kod üret"ten sonra da). İletişim bilgisi site ayarında boşsa
  "İletişim bilgisi sitenin alt bilgisinde duruyor." yazar; uygulamada alt bilgi olmadığı için kişi siteyi açmalı.
- **`secenek` kartında iki dokunma hedefi.** Hem kart hem içindeki satır tıklanabilir; ikisi aynı işi yapar. Başlığı 18 sp
  yapmak için `Arayuz.satir`'ın iç düzenine (`getChildAt(1)` → `getChildAt(0)`) dayanılıyor ([Arayuz.md](Arayuz.md)).
- **Kutunun klavye türü bilerek "görünür parola".** Klavye harfleri birleştirmeden tek tek gönderir; biçimleyici bu yüzden
  doğru çalışır (`KisiKodu.java` yorumu). Eski `Arayuz.alan` bu türü de noktalıyordu; `cad4bc3 commit 10` düzeltti.
- **Tek kullanımlık kişi kodu, çok kullanımlık veli kodu.** Yetişkinin kişi kodu kullanılınca yenilenir (müdür bir kez ekler);
  öğrencinin veli kodu ise yenilenmez, iki veli aynı kodla ekleyebilir. Ekrandaki metinler bu farkı anlatır.

## Testleri

- Android deposunda otomatik test yok; derleme ve lint (`./gradlew --offline assembleDebug lintDebug`) ana oturumda.
- Sunucu sözleşmesini site deposundaki testler korur:
  - `testler/test-kisi-kodu.js` — eski 10 haneli, 15 karakterlik ve harf durumu değişmiş kodun reddi; ekrandaki tireli biçimi
    yapıştıran ve boşluklu yazan iki velinin aynı koda eklemesi; veli kodunun kullanınca yenilenmemesi; velinin `/api/me`'sinde
    veli portalı; "Yeni kod üret" ile yeni kişi kodu ve eskisinin çalışmaması.
  - `testler/test-yetiskin.js` — `GET /api/kisilikler` (öğrenciye 403); öğretmen rolündeyken `kisilik/cocuk` ile eklenen
    çocuğun yetişkin hesabına bağlanması ve sonra iki portalın (öğretmen, veli) görünmesi.
  - `testler/test-site-ayarlari.js` — iletişim bilgisinin site ayarından gelmesi.
- Elle (öykünücü):
  - Yeni bir yetişkin hesabıyla gir → Ana sayfa "+ Ekle" → "Veli" → bir öğrencinin Ayarlar'ındaki veli kodunu (öğrencinin
    uygulamasında "Kopyala" ile) yapıştır → kutu tireli biçimde dolar → "Çocuğumu ekle" → "<ad> hesabına eklendi." ve Ana
    sayfa.
  - 15 karakter yaz → "Veli kodu 16 karakterdir."; bir harfin büyük/küçüğünü değiştir → sunucunun "bulunamadı" iletisi.
  - "Öğretmen" → "Kişi kodun"; "Yeni kod üret" → kod değişir; "Müdür" → "Yöneticiye ulaş" kartında site ayarındaki e-posta
    ve telefon; e-postaya dokun → e-posta uygulaması konu satırıyla açılır.

## Son durum

- `git log`: 2 commit (Android deposu). Son değişiklik `cad4bc3 commit 10` (2026-09-27, kişi kodu 16 karakter): "Veli kodu
  (15 karakter)" kutusu "(16 karakter)" oldu, ipucu `KisiKodu.ORNEK`'e geçti, kutuya `KisiKodu.kutuyaBagla` (kendiliğinden tire,
  yapıştırılan metinden kod ayıklama) bağlandı, alt not "boşluklar önemli değil" yerine "tireler kendiliğinden gelir" oldu,
  uzunluk denetimi 15'ten `KisiKodu.UZUNLUK`'a (16) çıktı. Aynı commit `Arayuz.alan`'ın görünür parola kutusunu noktalamasını
  düzeltti ve `KisiKodu`'yu 15 karakter / 5'erli boşluklu gösterimden ("Ab3#k Qx9+m Pt7?z") 16 karakter / 4'erli tireli
  gösterime geçirip kutu biçimleyicisini ekledi; ondan önce `sade` yalnız boşlukları atıyordu.
- Dosyanın ilk hâli `e96c5f2 commit 6` (2026-09-26): seçim sayfası, `Veli` ve `Kod` (öğretmen/müdür, yöneticiye ulaş).
- Bilinen açıklar (kod değiştirilmedi): `/api/me` hatasında başarı iletisinin kaybolması, ölü `ogretmenKodu` yedeği.
- Planlı işlerden bu dosyaya dokunması beklenenler:
  - "Çalışan olarak ekleme" — "Öğretmen" seçeneği **"Çalışan"** olacak: "Kişi kodunu okulunun müdürüne ver; seni okula çalışan
    olarak ekler. Görevini (öğretmen, müdür yardımcısı...) müdür verir." Tanım Android'de `EkleSayfasi`'nı açıkça anıyor.
  - "Tek kişi tek hesap + portallar öğrencide de" — öğrenci de ana hesap + kurum portalları modeline geçiyor; "+ Ekle"nin kimlere
    açık olduğu ve servisçinin de portal olması bu sayfayı etkileyebilir.
  - "Android yerel uygulama" — müdürün "Öğretmenler: Kodla ekle (kişi kodu)" ekranı kodun öbür ucunu uygulamaya getirecek.
  - "Çok dil" — sabit metinler.

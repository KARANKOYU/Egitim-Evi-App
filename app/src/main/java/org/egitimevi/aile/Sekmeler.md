# app/src/main/java/org/egitimevi/aile/Sekmeler.java

Alt çubuktaki sekmelerin listesi (bugün herkese aynı üçü: Ana sayfa, Bildirimler, Ayarlar) ve Bildirimler listesindeki bir
bildirime dokununca hangi ekranın açılacağı (bugün yalnız `#/profil` → Ayarlar).

## Bu dosya ne yapar?

Sitede herkesin sol menüsü farklıdır: öğrenci ödevlerini, veli çocuklarının devamsızlığını, müdür okulun düzenini görür
(`public/js/parcalar/06-menu.js`, site deposu). Uygulamada bunun karşılığı ekranın altındaki sekmelerdir ve "kime hangi
sekmeler" kararı bu dosyadadır. Sınıfın yorumu da bunu söyler: "Rol ekranları eklendikçe burası genişler (sitedeki
06-menu.js karşılığı)."

İkinci iş bildirim bağlantılarıdır. Sunucunun yazdığı bildirimlerin çoğunun bir bağlantısı vardır (`#/odevler`, `#/servis`,
`#/profil` …; sitenin sayfa adresleri); bazılarının yoktur (ör. "Şifren okul yönetimi tarafından değiştirildi.",
`sunucu/bolumler/hesaplar.js`; `depo.genel.bildir` bağlantı verilmezse boş metin yazar). Bildirimler listesinde bağlantısı
olan bir satıra dokununca o bağlantının uygulamada hangi ekranı açacağına `baglantiAc` karar verir; bağlantısız satır
dokunulamaz ([BildirimlerSayfasi.md](BildirimlerSayfasi.md)).

Bugün iki iş de başlangıç hâlinde: rol ekranları henüz yazılmadığı için herkes aynı üç sekmeyi görür, bağlantılardan da
yalnız `#/profil` bir ekran açar.

## İçinde neler var?

### `icin(e)` — kişinin sekmeleri

Bir `List<AnaEkran.Sekme>` döner; her sekme bir ad, alt çubuktaki simge ve kök sayfayı üreten bir `Supplier<Sayfa>`:

| Sıra | Ad | Simge | Kök sayfa |
|---|---|---|---|
| 0 | "Ana sayfa" | `R.drawable.ik_ev` | [AnaSayfa.md](AnaSayfa.md) (`AnaSayfa::new`) |
| 1 | "Bildirimler" | `R.drawable.ik_bildirim` | [BildirimlerSayfasi.md](BildirimlerSayfasi.md) (`BildirimlerSayfasi::new`) |
| 2 | "Ayarlar" | `R.drawable.ik_ayar` | [AyarlarSayfasi.md](AyarlarSayfasi.md) (`AyarlarSayfasi::new`) |

`e` (ekran) parametresi bugün kullanılmıyor; rol ekranları gelince kişinin rolüne ([Oturum.md](Oturum.md) `kisi`, `rol`)
ve okulun kapattığı bölümlere (`ozellikAcik`) bakmak için orada. İlk sekme her zaman "ana" sekmedir: [AnaEkran.md](AnaEkran.md)
açılışta onu gösterir, geri tuşu öteki sekmelerden ona döner. Sekme sayısı 2'den azsa alt çubuk gizlenir.

### `baglantiAc(e, baglanti)` — bildirim bağlantısını ekrana çevirir

1. `null` ise hiçbir şey yapmaz.
2. Sayfa adını çıkarır: baştan son `#/`'ye kadar olan kısım atılır (`^.*#/`), sonra `?` ve sonrası (`\?.*$`) atılır.
   Böylece hem listenin ham bağlantısı (`#/profil`) hem telefona giden tam adres (`/school/<kısa-ad>/?k=<kimlik>#/servis?c=…`)
   aynı sayfa adına iner: `profil`, `servis`.
3. Sayfa adına göre:
   - `profil` → `e.git(new AyarlarSayfasi())` (o anki sekmenin yığınına Ayarlar).
   - başka her şey → hiçbir şey (yorum: "bildirim zaten okundu").

## Kimle konuşur?

- Çağırdıkları: [AnaEkran.md](AnaEkran.md) — `AnaEkran.Sekme` (yapıcı: ad, simge, kök), `git`; sayfalar
  [AnaSayfa.md](AnaSayfa.md), [BildirimlerSayfasi.md](BildirimlerSayfasi.md), [AyarlarSayfasi.md](AyarlarSayfasi.md);
  `R.drawable.ik_ev`, `ik_bildirim`, `ik_ayar`. Android: yalnız `java.util.List`/`ArrayList`.
- Onu çağıranlar (grep):
  - [AnaEkran.md](AnaEkran.md) `uygulamayiKur` — `Sekmeler.icin(this)`: açılışta, girişten, portal değişiminden, çocuk
    eklendikten sonra (`yenidenKur`); her sekmeye boş bir yığın açılır.
  - [BildirimlerSayfasi.md](BildirimlerSayfasi.md) — bağlantısı boş olmayan satıra dokununca `baglantiAc(e, link)`.
  - Telefon bildirimine (bildirim çubuğu) dokunmak `baglantiAc`'ı ÇAĞIRMAZ: [AnaEkran.md](AnaEkran.md) `bildirimdenAc` yalnız
    Bildirimler listesini açar.
- Sunucu: kendisi istek atmaz. Bağlantılar sunucudan gelir: listede `GET /api/notifications` satırının `link`'i
  (`sunucu/bolumler/kayit.md`; ham `#/…`), telefon bildiriminde `GET /api/cihaz/bildirimler`'in `baglanti`'sı
  (`sunucu/bolumler/cihaz.md`; `push.bildirimAdresi` ile `/school/<kısa-ad>/?k=<alıcı>#/…`). `#/profil` bağlantısını bugün
  okulun öğrenci şifrelerini toplu yenilemesi yazıyor ("Giriş bilgilerin okul yönetimi tarafından yenilendi. Şifreni Ayarlar
  sayfasından değiştirebilirsin.", `sunucu/bolumler/okul.md`).

## Nasıl çalışır (adım adım)?

```
AnaEkran.uygulamayiKur
   └─► Sekmeler.icin(ekran) = [Ana sayfa | Bildirimler | Ayarlar]
          her sekme için boş yığın; aktif = 0; kök = AnaSayfa::new.get() gösterilir
alt çubukta sekmeye dokun ─► AnaEkran.sekmeSec(i) ─► yığın boşsa kok.get() ile kök sayfa kurulur

Bildirimler listesi: satıra dokun (link = "#/profil")
   └─► baglantiAc: "#/profil" → "profil" → git(new AyarlarSayfasi())
       link = "#/odevler"   → "odevler" → (tanınmıyor) hiçbir şey
```

## Dikkat!

- **Herkes aynı üç sekmeyi görüyor.** Öğrenci, veli, öğretmen, müdür, servisçi ve rolsüz yetişkin için liste aynı; rolün
  kendi ekranları (ödevler, yoklama, servis…) uygulamada yok. Ana sayfadaki şerit de bunu söylüyor ("Bu sürümde rolüne özel
  ekranlar hazırlanıyor.", [AnaSayfa.md](AnaSayfa.md)). Uygulamada olmayan işler için Ayarlar'da "Siteyi aç" var.
- **Bildirimlerin çoğu dokununca hiçbir şey açmaz.** Sunucunun bugün yazdığı bağlantılar (grep): `#/odevler`, `#/servis`,
  `#/cocuklarim`, `#/`, `#/ana`, `#/programim`, `#/etutlerim`, `#/etutler`, `#/sinavlarim`, `#/profil`, `#/okul-ogrenciler`,
  `#/ogretmenler`, `#/mesajlar`, `#/hatirlaticilar`, `#/devamsizligim`, `#/anketler`, `#/aile`. Velilere giden kopyalarda
  bağlantıyı `sunucu/veri/depo/genel.js` `veliBaglantisi` veli sayfasına çevirir (ör. `#/veli-odevler?c=…`,
  `#/veli-devamsizlik?c=…`, `#/veli-ilerleyis?c=…`, `#/servis?c=…`; tabloda olmayanlar `#/cocuklarim?c=…`). Bunlardan
  yalnız `profil` tanınıyor; öteki satırlar dokunulabilir görünür (sağda ok) ama bir şey olmaz, kişiye bir şey de
  söylenmez.
- **`profil` yeni bir Ayarlar sayfası iter.** Ayarlar sekmesine geçmez; dokunulan satır hangi sekmenin yığınındaysa
  (Bildirimler sekmesi ya da üstteki zille Bildirimler'in açıldığı sekme) oraya ayrı bir Ayarlar sayfası konur. Geri ile
  kapanır.
- **`?k=` (hangi portal) yok sayılır.** Telefon bildiriminin tam adresinde bildirimin hangi okul rolüne/portala ait olduğu
  `?k=<kimlik>` ile yazar; sitede bu, gerekirse önce o portala geçmek için kullanılır (`sunucu/bolumler/kisilik.md`'deki
  `gec` yorumu). `baglantiAc` sorgu kısmını atıyor: rol ekranları gelince "öğretmen olarak A okulundayken B okulundaki
  müdürlüğüne gelen bildirime dokunmak" doğru portala geçmeli.
- **Bağlantı güvenilmez girdidir.** Bugün yalnız sabit bir karşılaştırma var, zararsız. Ama telefon bildirimi `Intent` ile
  gelir ve [AnaEkran.md](AnaEkran.md) dışa açık bir Activity'dir; bağlantıya göre ekran açan kod (özellikle `?c=` gibi kimlik
  taşıyan kısımlar) eklenirken içerik doğrulanmalı.
- **Yorumdaki örnek adres eski biçimde.** Yorum `"/okul/?k=...#/servis?c=..."` diyor; sunucunun kurduğu adres
  `/school/<kısa-ad>/?k=...#/...` (`push.bildirimAdresi`). Kural ikisinde de aynı sonucu verir.
- Sekme listesi her `uygulamayiKur`'da baştan alınır; `icin` yan etkisiz olmalı (ağa gitmemeli), çünkü açılışta ve her portal
  değişiminde ana iş parçacığında çağrılır.

## Testleri

- Android deposunda otomatik test yok; derleme ve lint (`./gradlew --offline assembleDebug lintDebug`) ana oturumda.
- Bağlantı biçimini site deposundaki testler korur: `testler/test-servis-yoklama.js` (telefon bildiriminin `?k=` bağlantısı),
  `testler/test-bildirim.js` (bildirim listesi).
- Elle (öykünücü, deneme paketi):
  - Herhangi bir hesapla gir → altta "Ana sayfa", "Bildirimler", "Ayarlar"; seçili sekmeye yeniden dokun → o sekmenin başına
    döner.
  - Okulda müdürle bir sınıfın öğrenci şifrelerini toplu yenile; o sınıftan bir öğrenciyle uygulamaya dağıtılan şifreyle gir
    (toplu yenileme `sifreDegismeli`'yi açtığı için önce [SifreSayfasi.md](SifreSayfasi.md)'nin zorunlu ekranı gelir; kendi
    şifreni koy) → Bildirimler → "Giriş bilgilerin okul yönetimi tarafından yenilendi…" satırına dokun → Ayarlar açılmalı;
    geri → Bildirimler.
  - Öğretmenle ödev ver; öğrenciyle Bildirimler → ödev satırına dokun → hiçbir şey olmamalı (bugünkü davranış).

## Son durum

- `git log` (Android deposu): 1 commit. Dosya `e96c5f2 commit 6` (2026-09-26) ile geldi: siteyi içinde açan WebView'den yerel
  iskelete geçerken alt sekmeler ve bildirim bağlantısı için ilk hâl (üç ortak sekme, yalnız `profil`). O günden beri
  değişmedi.
- Bilinen açıklar (kod değiştirilmedi): rol sekmelerinin olmaması, bağlantıların çoğunun bir şey açmaması, `?k=`'nin yok
  sayılması, telefon bildiriminin bu dosyayı hiç kullanmaması.
- Planlı işlerden bu dosyaya dokunması beklenenler:
  - "Android yerel uygulama" (iş 10): asıl genişleme burada. Tanım rol rol sekmeleri sayıyor — öğrenci: Ana sayfa · Ödevler ·
    Program · Servis · Diğer; veli: (çocuk seçici) Ana sayfa · Ödevler · Servis · Mesajlar · Diğer; öğretmen: Ana sayfa ·
    Program + Yoklama · Ödevler · Mesajlar · Diğer; müdür: Ana sayfa · Mesajlar ve duyuru · Takvim · Diğer; servisçi:
    Yoklama · Harita · Mesajlar · Diğer — ve "bildirim bağlantıları `Sekmeler.baglantiAc` ile doğru ekrana". Ayrıca giriş
    yapmadan da açılan sol menü.
  - "Android geri tuşu" (iş 33): sekmenin kökünde geri önceki sekmeye (sekme geçmişi), geçmiş yoksa ana sekmeye; ana
    sekmenin kökünde hiçbir şey yapmamak — ilk sekmenin "ana" sayılması bu dosyadaki sıraya dayanıyor.
  - "Kulüpler kaldırılacak" (iş 30): uygulama tanımındaki "Diğer" listelerinde Kulüpler hâlâ geçiyor; kulüpler
    kaldırılacağı için rol sekmelerine eklenmemeli.

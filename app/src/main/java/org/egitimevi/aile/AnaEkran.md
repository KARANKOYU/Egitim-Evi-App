# app/src/main/java/org/egitimevi/aile/AnaEkran.java

Uygulamanın tek ekranı (başlatıcıdaki "Eğitim Evi"): üstte başlık çubuğu, ortada o anki sayfa, altta role göre sekmeler;
her sekmenin kendi sayfa yığını, geri tuşu, oturum akışı (giriş → aydınlatma onayı → zorunlu şifre → sekmeler), genel
hataların karşılanması, alttaki kısa mesaj, çıkış, telefonun uygulama anahtarını alması ve zil rozeti.

## Bu dosya ne yapar?

Uygulama "tek Activity" düzeniyle yazıldı: ekranlar ayrı Android `Activity`'leri değil, `Sayfa` nesneleridir (`Sayfa.java`)
ve hepsi bu Activity'nin ortasındaki alana sırayla konur. Böylece geçişler hızlı ve yumuşak olur, her sekmenin geçmişi
uygulamanın kendi elinde durur, dış kütüphaneye (AndroidX) gerek kalmaz. Bu dosya o düzenin sahibidir: sayfaları kurar,
gösterir, yığına koyar, geri tuşunda çıkarır.

Ekranın iki kipi var:

- **Giriş kipi** (`girisKipi = true`): tek bir yığın, üst ve alt çubuk gizli. Giriş, iki adımlı kod, hesap açma, şifremi
  unuttum, aydınlatma metni onayı ve "kendi şifreni belirle" ekranları burada gösterilir.
- **Uygulama kipi**: altta sekmeler (bugün "Ana sayfa", "Bildirimler", "Ayarlar"; `Sekmeler.java`), her sekmenin ayrı
  yığını, üstte başlık çubuğu.

Hangi kipte başlanacağına oturumun durumu karar verir (`akisiBaslat`). Ayrıca bütün ekranların ortak ihtiyaçları buradadır:
[Ag.md](Ag.md) her hatayı önce `genelHata`'ya sorar, ekranlar kısa mesajı `bildir` ile gösterir, giriş ve portal değişimi
`oturumAc` ile biter, çıkış `cikis` ile yapılır.

Kim görür: uygulamaya giren herkes — öğrenci, veli, öğretmen, müdür, servisçi, yönetici ve henüz okula bağlı olmayan
yetişkin hesabı. Bugün rollere özel ekran yok; herkes aynı üç sekmeyi görür ([AnaSayfa.md](AnaSayfa.md)).

Dosyanın geçmişi ilginçtir: Eğitim Evi Aile 1.0.x'te bu sınıf çocuğun telefonunun tek ekranıydı; tek uygulamaya geçişte o
ekran [AileEkrani.md](AileEkrani.md)'ye taşındı, `AnaEkran` önce siteyi içinde açan bir WebView oldu, hemen ardından da
bugünkü kendi çizdiği (yerel) iskelete dönüştü (aşağıda "Son durum").

## İçinde neler var?

### Sabitler, türler, alanlar

- `BAGLANTI` = `"baglanti"` — telefon bildirimine dokununca açılan `Intent`'in ek alanı. `Bildirimler.java` bildirimi
  kurarken sunucunun verdiği bağlantıyı buraya koyar; bu ekran yalnız alanın VAR olup olmadığına bakar (aşağıda).
- `onde` (statik, `volatile`) — ekran önde mi (`onResume` ile `onPause` arası `true`). `Bildirimler.yokla` buna bakar:
  uygulama öndeyken bildirim çubuğuna bildirim yazmaz.
- `Sekme` — alt çubuktaki bir sekme: `ad` ("Ana sayfa"), `simge` (`R.drawable.ik_ev`), `kok` (sekmenin kök sayfasını
  üreten `Supplier<Sayfa>`, ör. `AnaSayfa::new`). Listeyi `Sekmeler.icin(this)` verir.
- İç durum: `sekmeler`, sekme başına sayfa yığınları `yiginlar`, giriş kipinin yığını `girisYigini`, `girisKipi` (başta
  `true`), seçili sekme `aktif`; görünüm parçaları `ustCubuk`, `altCubuk`, `icerik`, `baslikYazi`, `rozet`, `mesaj`,
  `geriDugme`, `hesapDugme`, `yenileDugme`, `bildirimDugme`; kısa mesajın zamanlayıcısı (`ana` işleyicisi, `MESAJ` jetonu).

### Yaşam döngüsü

- `onCreate` — iskeleti kurar (`iskeletKur`), akışı başlatır (`akisiBaslat`), bildirimden açıldıysa Bildirimler'i açar
  (`bildirimdenAc`). Android 13 ve üstünde geri hareketini `getOnBackInvokedDispatcher()` ile `geri()`'ye bağlar
  (`PRIORITY_DEFAULT`; manifestte `android:enableOnBackInvokedCallback="true"`).
- `onResume` — `onde = true`; uygulama kipindeyse zil rozetini tazeler.
- `onPause` — `onde = false`.
- `onNewIntent(i)` — ekran zaten açıkken bildirime dokunulursa (`singleTask`): `setIntent(i)` ve `bildirimdenAc(i)`.
- `onBackPressed` — Android 12 ve altında geri tuşu → `geri()`.

### İskelet (`iskeletKur`, `cubukBosluklari`)

```
kök FrameLayout (zemin rengi)
 ├─ dikey düzen
 │   ├─ üst çubuk (en az 60 dp, 6 dp iç boşluk)
 │   │    [Geri ←] ya da [avatar]   Başlık (20 sp serif, tek satır, sonu "…")   [Yenile] [Zil + rozet]
 │   ├─ içerik FrameLayout (kalan yükseklik) ← sayfanın görünümü buraya
 │   └─ alt çubuk (kart rengi, 8 dp gölge): sekmeler eşit genişlikte
 └─ kısa mesaj kutusu (altta, kenarlardan 16 dp, alttan 96 dp; temanın tersi renkler: zemini `yazi`, yazısı `zemin`
    rengi — açık temada koyu kutu, koyu temada açık kutu)
```

- Geri düğmesi `Arayuz.simgeDugme(ik_geri, "Geri")`; avatar düğmesi 44 dp, içinde 36 dp `Arayuz.avatar` (açıklaması
  "Hesabım ve portallarım"); "Yenile" (`ust_yenile`) üstteki sayfanın `yenile()`'sini çağırır; zil (`ik_bildirim`)
  `BildirimlerSayfasi`'nı o anki sekmenin yığınına koyar. Rozet 11 sp kalın, ana renk hap, 99'dan fazlası "99+".
- `cubukBosluklari(ic)` — durum çubuğu, gezinme çubuğu ve klavyenin kapladığı üst/alt boşluk (Android 11+ `Insets`, daha
  eskisinde eski yöntem); dikey düzene üstten ve alttan bu kadar iç boşluk verilir ki içerik çubukların ve klavyenin
  altında kalmasın (manifestte `adjustResize`). Kısa mesaj kutusu bu boşluğun dışında, kökte durur (alttan sabit 96 dp).

### Akış (oturuma göre nereden başlanır)

- `akisiBaslat()` — sırayla: oturum yoksa (`Oturum.acik`) → `girisGoster()`; aydınlatma onayı eski (`Oturum.kvkkGuncel`)
  → `KvkkSayfasi`; kişinin `sifreDegismeli`'si varsa → `SifreSayfasi(true)` (zorunlu şifre); hiçbiri değilse
  `uygulamayiKur()`. `KvkkSayfasi` onaydan sonra, `SifreSayfasi` zorunlu şifreden sonra bunu yeniden çağırır.
- `girisGoster()` — `tekSayfa(new GirisSayfasi())`.
- `tekSayfa(s)` — giriş kipine geçer, giriş yığınını yalnız `s` yapar ve gösterir.
- `oturumAc(cevap)` — sunucunun oturum cevabını (`token`, `user`, `children`, `portallar`…) `Oturum.yaz` ile saklar ve
  `akisiBaslat()`. Girişte (`GirisSayfasi`, `KodSayfasi`) ve portal değişiminde (`PortalSecici`) çağrılır.
- `uygulamayiKur()` (iç) — uygulama kipine geçer: sekmeleri `Sekmeler.icin(this)`'den alır, her birine boş yığın açar,
  ilk sekmenin kök sayfasını gösterir, alt çubuğu ve avatarı çizer, zil rozetini tazeler, uygulama anahtarı yoksa alır
  (`cihazKaydet`) ve bildirim yoklamasını kurar (`Bildirimler.zamanla`).
- `yenidenKur()` — hesap bilgisi değişti (ör. çocuk eklendi): `uygulamayiKur()`'u yeniden çalıştırır; bütün sekmeler
  baştan kurulur. `EkleSayfasi` çağırır.

### Gezinme

- `git(s)` — sayfayı o anki yığının üstüne koyar ve ileri geçişle gösterir.
- `kapat()` — yığında birden çok sayfa varsa üsttekini çıkarır, alttakini geri geçişle gösterir ve onun `gorundu()`'sünü
  çağırır (ör. `AyarlarSayfasi` o an kendini yeniler). Kök sayfayı kapatmaz.
- `geri()` — geri tuşu/hareketi ve üst çubuktaki "Geri" düğmesi. Sıra: (1) üstteki sayfanın `geriBas()`'ı `true` dönerse
  dur (bugün yalnız `KodSayfasi` geçersiz kılıyor, o da sayaçlarını durdurup `false` döner); (2) yığında birden çok sayfa
  varsa `kapat()`; (3) uygulama kipinde ilk sekmede değilsek ilk sekmeye geç; (4) yoksa `finish()` — uygulama kapanır.
- `sekmeSec(i)` — sekmeye geçer. Zaten seçili sekmeye dokunulursa o sekmenin yığını köküne kadar boşaltılır ("başa dön").
  Öteki sekmelerin yığını korunur; ilk açılışta kök sayfası kurulur. Giriş kipinde ya da geçersiz numarada hiçbir şey
  yapmaz.
- `ust()` — üstteki sayfa (yoksa `null`).
- `sayfayiYenile(s)` — sayfanın görünümünü unutur; üstteyse hemen yeniden kurar. `Sayfa.yenile()`'nin varsayılanı.
- `goster(s, ileri)` (iç) — sayfaya bu ekranı bağlar (`s.e`), görünümü yoksa `olustur()` ile kurar, başka kaptan söker,
  içerik alanına koyar; 220 ms'lik kayarak belirme (ileri 28 dp sağdan, geri soldan). Sonra çubukları ayarlar: giriş
  kipinde ya da `cubuksuz()` sayfada üst ve alt çubuk gizli; tek sekme varsa alt çubuk gizli; başlık `baslik()`; yığın
  derinse "Geri", değilse avatar; "Yenile" yalnız `yenilenir()` sayfada; zil Bildirimler sayfasında gizli; pencere başlığı
  da sayfanın başlığı.
- `altCubukCiz()` (iç) — her sekme: 22 dp simge (seçiliyse ana renk ve açık ana renk hap içinde, değilse soluk), altında
  12 sp ad; ekran okuyucu için "Ana sayfa, seçili".
- `hesapDugmesiCiz()` (iç) — sol üstte kişinin baş harfli avatarı (rengi kişinin kimliğinden).
- `hesapMenusu()` (iç) — avatara dokununca: yetişkin hesabı (`yetiskin`) ya da ona bağlı okul rolü (`rolSatiri`) ise
  `PortalSecici.goster` (portallar penceresi), değilse (ör. öğrenci, servisçi) `AyarlarSayfasi`.

### Genel hatalar ve kısa mesaj

- `genelHata(h)` — [Ag.md](Ag.md) her hatada önce bunu çağırır; `true` dönerse ekran hiçbir şey yapmaz:
  - 401 ve oturum açık → `oturumuBitir()` + "Oturumunun süresi doldu. Yeniden giriş yap.";
  - gövdede `kvkkGerek` → oturumdaki onay bilgisini "eski" yapar (`Oturum.tazele`) ve `KvkkSayfasi`;
  - gövdede `sifreDegismeli` → `SifreSayfasi(true)`;
  - öteki her şey `false` (ekran kendisi karşılar).
- `bildir(metin)` — altta 3,8 saniye görünen kısa mesaj (160 ms'de belirir, 200 ms'de kaybolur); boş metinde bir şey
  yapmaz; yeni mesaj eskisinin zamanlayıcısını sıfırlar; ekran okuyucuya da duyurulur.

### Oturum, uygulama anahtarı, bildirimler

- `cikis()` — "Çıkış yap": arka planda (`Ag.arkada`) önce uygulama anahtarını sunucuda siler (`POST /api/cihaz/sil`,
  `X-Cihaz` başlığıyla), sonra oturumu kapatır (`POST /api/logout`); ikisinin de hatası yok sayılır ve cevapları
  BEKLENMEZ — telefon hemen `oturumuBitir()` ile giriş ekranına döner. `AyarlarSayfasi`, `KvkkSayfasi` ve zorunlu
  `SifreSayfasi`'ndaki "Çıkış yap" düğmeleri çağırır.
- `oturumuBitir()` (iç) — servisçinin sefer servisini durdurur (`SeferServisi.durdur`), bildirim yoklamasını iptal eder
  (`Bildirimler.iptal`), uygulama anahtarını, imleci ve seferi unutur (`UygulamaAyar.cik`), oturumu siler
  (`Oturum.kapat`), sekmeleri temizler, rozeti gizler, giriş ekranını açar.
- `cihazKaydet()` (iç) — telefonda uygulama anahtarı yoksa `POST /api/cihaz { ad: "<üretici> <model>", platform: "android",
  surum: <uygulama sürümü> }`. Cevaptaki `cihazAnahtari` 64 küçük onaltılık karakterse saklar (`UygulamaAyar.anahtarYaz`),
  yoklamayı kurar ve bildirim izni ister. Hata sessizce yok sayılır (yorumu: sunucu bu ucu bilmiyorsa telefon bildirimi
  olmaz, uygulama yine çalışır).
- `bildirimIzniIste()` (iç) — bildirim kanalını kurar (`Bildirimler.kanalKur`); Android 13+ ve izin yoksa
  `POST_NOTIFICATIONS` iznini ister (istek kodu 13).
- `rozetTazele()` — uygulama kipindeysek `GET /api/notifications` → `unread` sayısı rozete; 0 ise rozet gizli; zilin
  ekran okuyucu açıklaması "Bildirimler, 3 okunmamış". Hata sessiz.
- `bildirimdenAc(i)` (iç) — `Intent`'te `BAGLANTI` alanı varsa ve uygulama kipindeysek `BildirimlerSayfasi`'nı üste koyar.

## Kimle konuşur?

- Çağırdıkları (aynı paket):
  - [Arayuz.md](Arayuz.md) — `dikey`, `yatay`, `simgeDugme`, `yazi`, `avatar`, `ikon`, `ekle`; `Tema.java` — `renk`, `dp`,
    `zemin`, `BASLIK`, `KALIN`, `ORTA`, `R_KUCUK`.
  - `Sayfa.java` — `olustur`, `gorundu`, `geriBas`, `cubuksuz`, `yenilenir`, `yenile`, `baslik`; sayfanın `e` ve `gorunum`
    alanlarını bu dosya doldurur/okur.
  - `Sekmeler.java` — `icin(this)` (sekme listesi).
  - Sayfalar: `GirisSayfasi`, `KvkkSayfasi`, `SifreSayfasi(true)`, `BildirimlerSayfasi`, `AyarlarSayfasi`;
    `PortalSecici.goster`.
  - `Oturum.java` — `acik`, `kvkkGuncel`, `kisi`, `yaz`, `tazele`, `kapat`, `anahtar`; `Ayarlar.sunucu`;
    `UygulamaAyar.java` — `anahtar`, `anahtarVar`, `anahtarYaz`, `cik`.
  - `Bildirimler.java` — `zamanla`, `iptal`, `kanalKur`; `SeferServisi.durdur`.
  - [Ag.md](Ag.md) — `post` (`/api/cihaz`), `get` (`/api/notifications`), `arkada` (çıkış); [Api.md](Api.md) — `uygulama`
    (`/api/cihaz/sil`), `oturumla` (`/api/logout`).
- Sunucu uçları (belgeleri site deposunda):
  - `POST /api/cihaz` — `sunucu/bolumler/cihaz.md`: oturumla anahtar; cevap `{ cihazAnahtari, cihazId }` (anahtar yalnız
    bir kez gösterilir); sahip başına saatte 20; altıncı anahtar en eskisini siler.
  - `POST /api/cihaz/sil` (`X-Cihaz` başlığıyla) — `sunucu/bolumler/cihaz.md`: telefon kendi anahtarını kaldırır.
  - `GET /api/notifications` — `sunucu/bolumler/kayit.md`: `{ notifications (son 100), unread, surum }`; burada yalnız
    `unread` kullanılır.
  - `POST /api/logout` — `sunucu/bolumler/kayit.md`: Authorization'daki oturumu siler.
- Android: `Activity`, `Handler`/`Looper`, `WindowInsets`, `OnBackInvokedDispatcher` (API 33+), `PackageManager`
  (sürüm adı), `Build` (üretici, model), çalışma zamanı izni `POST_NOTIFICATIONS`.
- Manifest: `AnaEkran` başlatıcı Activity'si (`MAIN`/`LAUNCHER`), `exported="true"`, `launchMode="singleTask"`,
  `configChanges="orientation|screenSize|screenLayout|keyboardHidden|uiMode"`, `windowSoftInputMode="adjustResize"`;
  uygulama teması `@style/Tema`.
- Onu çağıranlar (grep):
  - [Ag.md](Ag.md) — `runOnUiThread`, `isFinishing`, `isDestroyed`, `genelHata`, `bildir`.
  - `Sayfa.java` — `sayfayiYenile`; sayfalar kendi aralarında `e.git`, `e.kapat`, `e.bildir` ile gezinir ve konuşur.
  - `GirisSayfasi`, `KodSayfasi`, `PortalSecici` — `oturumAc`; `KvkkSayfasi`, `SifreSayfasi` — `akisiBaslat`, `cikis`;
    `AyarlarSayfasi` — `cikis`; `EkleSayfasi` — `yenidenKur`; `BildirimlerSayfasi` — `rozetTazele`;
    `KayitSayfasi`, `KodSayfasi`, `SifremiUnuttumSayfasi`, `SifreSayfasi` (zorunlu değilken) — `kapat`;
    [AnaSayfa.md](AnaSayfa.md) — `git(new EkleSayfasi())`.
  - `Sekmeler.java` — `AnaEkran.Sekme`; `Bildirimler.java` — `onde`, `BAGLANTI` (bildirime dokununca bu ekranı açan
    `PendingIntent`); `SeferServisi.java` — "Sefer sürüyor" ön plan bildirimine dokununca bu ekranı açan `PendingIntent`
    (`BAGLANTI` taşımaz).

## Nasıl çalışır (adım adım)?

### Açılış

```
onCreate → iskeletKur → akisiBaslat
   oturum yok?            → [giriş kipi] GirisSayfasi ── giriş ──► (gerekirse KodSayfasi) ──► oturumAc(cevap)
   onay eski?             → [giriş kipi] KvkkSayfasi  ── onay ──► akisiBaslat
   sifreDegismeli?        → [giriş kipi] SifreSayfasi(true) ── kaydet ──► akisiBaslat
   hepsi tamam            → uygulamayiKur:
                               sekmeler = [Ana sayfa | Bildirimler | Ayarlar]
                               Ana sayfa kökü gösterilir; avatar; rozetTazele (GET /api/notifications)
                               anahtar yoksa POST /api/cihaz → anahtar → Bildirimler.zamanla + bildirim izni
         → bildirimdenAc(getIntent())   (bildirimden açıldıysa Bildirimler sayfası üste)
```

### Gezinme ve geri

```
Ayarlar sekmesi: [AyarlarSayfasi] ─ "Şifre değiştir" ─► git(SifreSayfasi) → yığın [Ayarlar, Şifre]
geri: Şifre.geriBas()? hayır → yığın > 1 → kapat → Ayarlar görünür, gorundu() → yenilenir
geri: yığın 1, aktif = 2 ≠ 0 → sekmeSec(0) → Ana sayfa
geri: yığın 1, aktif = 0     → finish()  (uygulama kapanır)
```

### Oturum düşmesi ve çıkış

```
herhangi bir Ag isteği ─► 401 ─► genelHata ─► oturumuBitir ─► GirisSayfasi + "Oturumunun süresi doldu…"
"Çıkış yap" ─► cikis ─► (arka plan) POST /api/cihaz/sil [X-Cihaz] ; POST /api/logout [Bearer]
                    └─► (hemen) oturumuBitir: sefer dur, yoklama iptal, anahtar + oturum silinir, giriş ekranı
```

## Dikkat!

- **Ana sekmenin kökünde geri uygulamayı kapatır** (`finish()`). Kullanıcı bunu istemiyor: planlı "Android geri tuşu"
  işi kökte hiçbir şey yapmamayı, önce açık pencereyi/ayrıntıyı kapatmayı ve sekme geçmişini istiyor (aşağıda "Son
  durum"). Giriş ekranında da geri bugün uygulamayı kapatır.
- **Bildirime dokunmak yalnız Bildirimler listesini açar.** `bildirimdenAc` `BAGLANTI`'nın içeriğine bakmaz; ilgili
  ekranı açacak `Sekmeler.baglantiAc` burada çağrılmıyor. Sayfa o anki sekmenin (açılışta "Ana sayfa") yığınına itilir,
  "Bildirimler" sekmesine geçilmez; zaten Bildirimler açıkken yeni bir bildirime dokunulursa ikinci bir Bildirimler
  sayfası üste konur.
- **Uygulama öndeyken gelen bildirim görünmez.** `Bildirimler.yokla`, `onde` iken bildirim çubuğuna yazmaz (yorumu site
  WebView döneminden: "site kendi bildirimlerini gösterir") ve imleci ilerletir; bu ekran ise zil rozetini yalnız
  açılışta, `onResume`'da ve bildirimler okununca tazeler — dönemsel yoklama yok. Yani uygulama açıkken gelen bildirim
  ne çubukta ne rozette görünür; ancak uygulama arkaya gidip gelince ya da Bildirimler sayfası yenilenince fark edilir
  (kod okumasına göre; telefonda denenmedi). Ayrıca rozet için her seferinde bütün liste (son 100 bildirim) çekiliyor;
  sunucunun "değişmediyse liste gönderme" (`surum`) özelliği kullanılmıyor.
- **Uygulama anahtarının sunucuda silindiği anlaşılmaz.** `cihazKaydet` yalnız telefonda anahtar var mı diye bakar. Şifre
  değişince (sunucu hesabın bütün anahtarlarını siler) ya da aydınlatma metni güncellenince (anahtar silinir, 403
  `anahtarGecersiz`) yeni anahtar ancak `Bildirimler.yokla` 401/403 alıp telefondakini sildikten SONRA ve
  `uygulamayiKur` yeniden çalışınca (açılış, giriş, portal değişimi, çocuk ekleme) alınır. Arada telefon bildirimi gelmez.
- **401'de anahtar sunucuda kalır.** `oturumuBitir` anahtarı yalnız telefondan siler; sunucuya "sil" demez (bunu yalnız
  `cikis` yapar). Kalan anahtar, hesap başına 5 anahtar sınırında en eski olarak düşene kadar sunucuda durur.
- **Çıkış cevabı beklemez.** İnternet yokken "Çıkış yap"a basılırsa telefon çıkar ama sunucudaki oturum (uygulama
  oturumu 30 gün) ve uygulama anahtarı açık kalır; ikisi de telefondan silindiği için kullanılmaz.
- **Çıkış, çocuğun telefonu bağlantısına dokunmaz.** `oturumuBitir` `Ayarlar`'ı (Aile anahtarı) silmez:
  öğrenci uygulamadan çıksa da konum ve ekran süresi paylaşımı sürer; kaldırmak için [AileEkrani.md](AileEkrani.md)'deki
  "Bu telefonun bağlantısını kaldır" gerekir. İki anahtarın ayrı olması bilinçli; ama kullanıcı "çıktım, paylaşım da
  durdu" sanabilir.
- **Durum kaydedilmez.** `onSaveInstanceState` yok. Döndürme, klavye ve tema değişimi `configChanges` ile Activity'yi
  yeniden kurdurmaz; ama başka bir yapılandırma değişikliği (dil, yazı boyutu, ekran yoğunluğu) ya da Android'in süreci
  öldürmesi ekranı baştan kurar: ilk sekmeye dönülür, yarım formlar kaybolur.
- **Canlı tema değişimi:** `uiMode` `configChanges`'te ama `onConfigurationChanged` yazılmamış; telefon koyu temaya
  geçince var olan çubuklar ve sayfalar eski renklerinde kalır (bkz. [Arayuz.md](Arayuz.md)).
- **`kvkkGerek`'te `Oturum.tazele` iki alanı daha sıfırlar.** `Oturum` her tazelemede `kisilikSec` ve `cocuk`'u koşulsuz
  yazar; burada yalnız `{ kvkkGuncel: false }` verildiği için ikisi de boşalır. Bugün görünür bir etkisi yok:
  `kisilikSec`'i uygulamada okuyan yer yok; `cocuk` yalnız `Oturum.seciliCocuk`'un yedeği, portallar penceresindeki
  "Buradasın" ise önce saklı `portallar` listesindeki `aktif` bayrağına bakar ve o liste bu tazelemede silinmez. Velinin
  çocuğa göre çalışan ekranları `seciliCocuk`/`cocuk`'a dayanırsa bu sıfırlama önem kazanır (kod okumasına göre).
- **Bildirim izninin sonucu dinlenmez.** İzin hem burada (`cihazKaydet` sonrası) hem `AyarlarSayfasi`'nda aynı istek
  koduyla (13) istenir ama `onRequestPermissionsResult` yok; Ayarlar'daki "Telefon bildirimleri" satırı ancak o sayfa
  yeniden kurulunca (üstündeki bir sayfa kapatılıp `gorundu()` çalışınca ya da sekmeler baştan kurulunca) güncellenir —
  sekme değiştirip dönmek yetmez.
- **Dışarıdan gelen `Intent`.** Activity dışa açık (`exported`, başlatıcı); başka bir uygulama `baglanti` ekli bir
  `Intent`'le onu açtırabilir. Bugün tek etkisi Bildirimler sayfasının açılması (bağlantı okunmadığı için zararsız).
  Bağlantıya göre ekran açan kod eklenince, dışarıdan gelen bağlantının güvenilmez olduğunu unutma.
- **`gorundu()` yalnız `kapat`'ta çağrılır.** `git` ile açılan yeni sayfada ve `sekmeSec` ile dönülen sekmede çağrılmaz;
  "sekmeye dönünce tazelen" isteyen sayfa bunu bilmeli.
- `SeferServisi.durdur` çıkışta çağrılıyor ama bugün `SeferServisi.baslat`'ın çağıranı yok (WebView dönemindeki site
  köprüsü `commit 6`'da kalktı, servisçi ekranı henüz yazılmadı).
- Manifestin başındaki yorum hâlâ "Ana ekran site (WebView)" diyor; depodaki `README.md` de WebView dönemini (artık
  olmayan `Kopru.java` dahil) anlatıyor. İkisi de eskidi.

## Testleri

- Android deposunda otomatik test yok; derleme ve lint (`./gradlew --offline assembleDebug lintDebug`) ana oturumda.
- Sunucu tarafında bu ekranın kullandığı sözleşmeyi site deposundaki testler korur: `testler/test-servis-yoklama.js`
  (oturumla `POST /api/cihaz` 64 haneli anahtar; başlıklı `/api/cihaz/sil`; hesap başına 5 anahtar; şifre değişince
  anahtarlar ve öbür oturumlar düşer; uygulama oturumu 30 gün, çıkışta kapanır), `testler/test-bildirim.js`
  (`/api/notifications` liste, `surum`, okununca `unread` 0), `testler/test-yonetim.js` (403 `kvkkGerek`, 403
  `sifreDegismeli`).
- Elle (öykünücü, deneme paketi):
  - İlk açılış → giriş ekranı, çubuklar gizli. Sunucuda "şifresini değiştirmeli" işaretli bir hesapla gir (bugün: okulun
    T.C. no'yu varsayılan şifre yaparak açtığı, toplu giriş bilgisi dağıttığı ya da yetkilinin "değiştirsin" diyerek ya da
    T.C. no'ya döndürerek şifresini sıfırladığı hesap)
    → "kendi şifreni belirle" ekranı; kaydedince sekmeler.
  - Sekmeler arasında gez; Ayarlar'da "Şifre değiştir"e gir, geri → Ayarlar; geri → Ana sayfa; geri → uygulama kapanır.
  - Seçili sekmeye yeniden dokun → o sekmenin başına döner.
  - Girişten sonra ilk yoklamanın geçmesini bekle (ilk yoklama eski bildirimleri getirmez, yalnız imleci alır); sonra
    siteden bir bildirim üret (ör. öğretmenle ödev ver), uygulama arkadayken bir sonraki yoklamayı bekle (15 dakikada bir;
    okulun servis saatlerinde dakikada bir), bildirim çubuğundan dokun → Bildirimler sayfası.
  - Ayarlar → "Çıkış yap" → giriş ekranı; telefon bildirimleri durmalı.

## Son durum

- `git log`: 5 commit (Android deposu). Son değişiklik `e96c5f2 commit 6` (2026-09-26): dosya baştan yazıldı — siteyi
  içinde açan WebView ve site köprüsü (`Kopru`), dosya seçme/indirme, sayfa konumu izni ve deneme paketinin "sunucu
  adresi sor" penceresi kalktı; yerine bugünkü iskelet geldi: üst çubuk (geri/avatar, başlık, yenile, zil + rozet), alt
  sekmeler ve sekme başına yığın, giriş kipi, `akisiBaslat` (giriş → onay → zorunlu şifre), `genelHata`, `bildir`,
  `cikis`, `cihazKaydet`, `rozetTazele`, Android 13+ geri hareketi.
- Ondan önce `f70aeca commit 5` (2026-09-26, tek uygulamaya geçiş): çocuğun telefonu ekranı [AileEkrani.md](AileEkrani.md)'ye
  kopyalandı; `AnaEkran` sitenin WebView'i oldu (yalnız kendi sunucusu içeride, köprü, bildirim yoklaması, servisçi
  seferi). Daha önceki üç commit, bu sınıf henüz çocuğun telefonunun ekranıyken: `3875db7 commit 4` (Android 15'te
  çubuk renkleri, pil ayarının uygulama ayar sayfasından açılması, `tr-TR` tarih biçimi), `9e9d5af commit 2` (durum
  çubuğu/klavye boşlukları, açık zeminde koyu simgeler, şifre gizleme), `34b45f1 commit 1` (ilk hâli, Eğitim Evi Aile).
- Bilinen açıklar (kod değiştirilmedi): kökte geri ile çıkış, bildirim bağlantısının kullanılmaması, öndeyken gelen
  bildirimin görünmemesi, sunucuda silinen anahtarın geç yenilenmesi, durumun kaydedilmemesi, canlı tema değişimi.
- Planlı işlerden bu dosyaya dokunması beklenenler:
  - "Android geri tuşu" (Linux'ta yapılacak): her basışta tek adım — önce açık olanı kapat, kaydedilmemiş yazıyı sor,
    önceki sayfa, sekme geçmişi, ana sekmenin kökünde HİÇBİR ŞEY YAPMA; çıkıştan sonra geri hesaba sokmasın; giriş
    ekranında geri bir şey yapmasın. `geri()` ve `kapat()` buna göre değişecek.
  - "Üst şerit sadeleştirme": uygulamada ← → ⌂ düğmeleri, doğrulama ekranlarında sol üstte ← (vazgeç) — üst çubuk burada.
  - "Android yerel uygulama" (bütün roller): rol sekmeleri (`Sekmeler`), bildirim bağlantısının doğru ekranı açması,
    giriş yapmadan da açılan sol menü, açılış ekranında "Hesaba gir →" ve "Doğrulayıcı", "Yeni sürüm var — Güncelle"
    şeridi.
  - "Sistem" işindeki bakım modu (sunucu 503 `bakim` derse şerit) ve "Çok dil" (dil seçimi, sağdan sola düzen).

# app/src/main/java/org/egitimevi/aile/Bildirimler.java

Telefon bildirimlerinin beyni: Firebase olmadan sunucuya uygulama anahtarıyla kendisi sorar (15 dakikada bir, okulun servis
saatlerinde yaklaşık dakikada bir), yeni bildirimleri "Bildirimler" kanalından bildirim çubuğuna yazar, anahtar geçersizse
unutup durur.

## Bu dosya ne yapar?

Sitede kişi telefon bildirimlerini açarsa bildirimler bildirim çubuğuna Web Push ile gelir. Uygulama ise Firebase (Google'ın
bildirim servisi) kullanmaz; bu yüzden "yeni bir şey var mı?" diye sunucuya **kendisi sorar** (yoklama). Sınıf yorumundaki
kural:

- normalde **15 dakikada bir** — Android'in izin verdiği en sık düzenli iş;
- okulun **servis saatlerinde dakikada bir** — "Zeynep 07:42'de servise bindi." gibi bildirimler gecikmesin;
- sunucu yalnız **son görülenden sonraki** bildirimleri verir (imleç); ilk sorguda eskiler gelmez;
- uygulama açıkken (ekran öndeyken) bildirim çubuğuna yazılmaz.

Sorarken oturum anahtarı değil, girişten sonra alınan **uygulama anahtarı** kullanılır (`X-Cihaz` başlığı; `UygulamaAyar.java`
"uygulama" dosyasında durur). Bu anahtar hesaba giriş vermez; yalnız bildirim yoklamaya ve servisçinin sefer konumuna yarar
([Api.md](Api.md)). Anahtarı [AnaEkran.md](AnaEkran.md)'nin `cihazKaydet`'i alır ve hemen ardından bu sınıfın `zamanla`'sını
çağırır.

İşin "ne zaman"ını Android'in iş zamanlayıcısı (`JobScheduler`) yürütür; zamanı gelen işi [BildirimIsi.md](BildirimIsi.md)
çalıştırır, o da buradaki `yokla`'yı çağırır.

## İçinde neler var?

### Sabitler

- `KANAL` = `"bildirimler"` — Android bildirim kanalının kimliği (paket içi).
- `IS_DUZENLI` = 101, `IS_HIZLI` = 102 (iç) — iş zamanlayıcısındaki iki işin numarası.
- `ONBES_DK` (iç) — 15 dakika (milisaniye).
- `TURKIYE` (iç) — `Europe/Istanbul` saat dilimi; servis saatleri Türkiye saatiyle karşılaştırılır.
- Yapıcı gizli; sınıf yalnız statik işlevlerden oluşur.

### Zamanlama

- `zamanla(Context c)` (dışa açık) — `zamanla(c, false)`.
- `zamanla(Context c, boolean zincir)` (paket içi):
  1. `JobScheduler` alınamazsa hiçbir şey yapmaz.
  2. Telefonda uygulama anahtarı yoksa (`UygulamaAyar.anahtarVar`) bütün işleri iptal eder ve döner.
  3. **Düzenli iş (101)** bekleyen yoksa kurulur: `setPeriodic(15 dk)`, `setRequiredNetworkType(NETWORK_TYPE_ANY)` (ağ varken),
     `setPersisted(true)` (telefon kapanıp açılınca da kalır).
  4. **Hızlı iş (102)**: şu an servis saatindeysek (`servisSaatinde(UygulamaAyar.servisSaatleri(c))`) ve `zincir` doğruysa ya da
     bekleyen hızlı iş yoksa kurulur: `setMinimumLatency(60 sn)`, `setOverrideDeadline(3 dk)`, ağ şartı
     (`NETWORK_TYPE_ANY`); kalıcı değil. Android belgelerine göre son süre (`setOverrideDeadline`) dolunca iş öteki şartlar
     sağlanmasa da çalışır: hızlı iş ağ yokken de başlayabilir, o zaman `yokla` ağ hatasını yok sayar ve zincir sürer (telefonda
     denenmedi). Aynı numarayla `schedule` bekleyen işin yerine geçer.
     `zincir = true` yalnız [BildirimIsi.md](BildirimIsi.md)'den gelir: her yoklamanın sonunda bir sonraki kurulur.
- `iptal(Context c)` (dışa açık) — iki işi de iptal eder.
- `servisSaatinde(String json)` (paket içi) — saklanan servis saatleri metni (`{"sabahBas":"07:00","sabahBit":"09:20",
  "aksamBas":"16:30","aksamBit":"19:00"}` gibi) ile şu anki Türkiye saatini karşılaştırır. Sabah ve akşam aralıklarının her biri
  için: başlangıç ve bitiş okunabiliyor ve bitiş başlangıçtan sonraysa, **başlangıçtan 10 dakika önce** ile **bitişten 60
  dakika sonra** arası "servis saati" sayılır. Metin boşsa ya da bozuksa `false`.
- `dakika(String s)` (iç) — `"SS:DD"` → gün içindeki dakika; biçime (`^[0-2]\d:[0-5]\d$`) uymazsa -1.

### Yoklama

- `yokla(Context c)` (paket içi; ağ işi, arka iş parçacığında çağrılır):
  1. Uygulama anahtarı yoksa döner.
  2. Yol: `/api/cihaz/bildirimler`, imleç varsa `?son=<imleç>` (`URLEncoder` ile kodlanır).
  3. `Api.uygulama(Ayarlar.sunucu(c), yol, null, anahtar)` — `X-Cihaz` başlıklı GET ([Api.md](Api.md)).
  4. Cevaptaki `bildirimler` dizisi varsa, imleç doluysa (ilk yoklama değilse) ve uygulama önde değilse
     (`AnaEkran.onde == false`) ilk 20 bildirim `goster` ile çubuğa yazılır.
  5. İmleç (`imlec`, gelmezse eskisi) ve servis saatleri (`servisSaatleri` nesnesinin metni, gelmezse boş) birlikte saklanır
     (`UygulamaAyar.imlecYaz`). Bildirim gösterilmese de imleç ilerler.
  6. Sunucu 401 ya da 403 derse (anahtar tanınmadı ya da silindi): `UygulamaAyar.cik(c)` (anahtar, imleç, servis saatleri ve
     sefer unutulur) ve `iptal(c)`. Öteki hatalar (ağ yok, 429, 5xx) yok sayılır: bir sonraki işte yeniden denenir.
- `kodla(String s)` (iç) — imleci adrese uygun kodlar (`URLEncoder`, UTF-8).

### Gösterme

- `kanalKur(Context c)` (paket içi) — "Bildirimler" kanalı yoksa kurar: kimlik `bildirimler`, ad "Bildirimler", önem
  `IMPORTANCE_DEFAULT`, açıklama "Ödev, mesaj, servis ve okul bildirimleri.". [AnaEkran.md](AnaEkran.md) bildirim izni
  istemeden önce ve `goster` her bildirimden önce çağırır.
- `goster(Context c, JSONObject b)` (iç):
  - `metin` (kırpılmış) boşsa hiçbir şey yapmaz.
  - Dokununca açılacak `Intent`: [AnaEkran.md](AnaEkran.md), ek alan `AnaEkran.BAGLANTI` = bildirimin `baglanti`'sı, bayraklar
    `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_SINGLE_TOP`.
  - Bildirim numarası: bildirimin `id`'sinin (yoksa metnin) `hashCode()`'u. Aynı numara `PendingIntent`'in istek kodu da olur
    (`FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT`), böylece her bildirimin kendi bağlantısı kalır.
  - Bildirim: küçük simge `R.drawable.bildirim_simge`, başlık "Eğitim Evi", metin bildirimin metni (uzunsa açılınca tamamı:
    `BigTextStyle`), dokununca kapanır (`setAutoCancel`).
  - `notify(no, bildirim)`; izin yoksa çıkan `SecurityException` yutulur.

## Kimle konuşur?

- Aynı paketten çağırdıkları:
  - `UygulamaAyar.java` — `anahtar`, `anahtarVar`, `imlec`, `imlecYaz`, `servisSaatleri`, `cik`.
  - [Ayarlar.md](Ayarlar.md) — `sunucu` (uygulamanın sunucu adresi).
  - [Api.md](Api.md) — `uygulama(...)` ve `Api.Hata` (`durum`).
  - [AnaEkran.md](AnaEkran.md) — `onde` (ekran önde mi), `BAGLANTI` (Intent ek alanı); bildirime dokununca açılan Activity.
  - [BildirimIsi.md](BildirimIsi.md) — işlerin çalıştırdığı servis (`ComponentName`).
- Onu çağıranlar (grep):
  - [AnaEkran.md](AnaEkran.md) — `zamanla` (`uygulamayiKur` ve anahtar alınınca `cihazKaydet`), `iptal` (`oturumuBitir`:
    çıkış ve oturum düşmesi), `kanalKur` (`bildirimIzniIste`).
  - [BildirimIsi.md](BildirimIsi.md) — `yokla`, `zamanla(c, true)`.
  - [BaslatmaAlici.md](BaslatmaAlici.md) — `zamanla` (telefon açılınca, uygulama güncellenince).
- Sunucu ucu (site deposunda `sunucu/bolumler/cihaz.md`):
  - **`GET /api/cihaz/bildirimler[?son=<imleç>]`** (`X-Cihaz: <64 onaltılık>`) →
    `{ bildirimler: [{ id, metin, baglanti, zaman }], imlec, servisSaatleri }`.
    - Alıcılar: anahtarın sahibi ve (yetişkin hesabıysa) bütün okul rolü satırları; yalnız OKUNMAMIŞ bildirimler.
    - İlk çağrıda (ya da imleç bozuksa) eski bildirim gelmez, yalnız güncel imleç döner.
    - İmleç varsa ondan sonraki okunmamışların en yeni 20'si, eskiden yeniye; imleç en yeniye ilerler.
    - `baglanti` okul rolünde `/school/<kısa-ad>/?k=<rol satırı>#/sayfa` biçimindedir (`sunucu/push.md` `bildirimAdresi`).
    - `servisSaatleri`: yalnız hesabın servisle ilgisi varsa (servisçi, servisli öğrenci ya da velisi) dolu; birden çok okulda
      en geniş aralıklar (`sunucu/yardimci/servis-pencere.js` `saatZarfi`); "Servis" bölümü kapalı ya da onaylı olmayan okul
      sayılmaz (tek okulda bu durumda `null`).
    - Hatalar: biçimsiz ya da tanınmayan anahtar 401 "Uygulama anahtarı tanınmadı. Yeniden giriş yap."; hesap kullanılamıyorsa
      ya da aydınlatma metni onayı eskiyse anahtar sunucuda SİLİNİR ve 403 `{ anahtarGecersiz: true }`; anahtar başına saatte
      240'tan sık istekte 429.
- Android: `JobScheduler`, `JobInfo`, `ComponentName`, `NotificationManager`, `NotificationChannel`, `Notification.Builder`
  (`BigTextStyle`), `PendingIntent`, `Intent`; Java: `Calendar`, `TimeZone`, `URLEncoder`, `org.json`. İzinler (manifest):
  `INTERNET`, `POST_NOTIFICATIONS` (Android 13+'te çalışma anında istenir), `RECEIVE_BOOT_COMPLETED` (`setPersisted(true)`
  bunu ister), `ACCESS_NETWORK_STATE` (Android belgelerine göre Android 14'ü hedefleyen uygulamada ağ şartlı iş kurmak için
  gerekir; uygulama `targetSdk 36`. Manifestten kalkarsa `schedule` hata verir).

## Nasıl çalışır (adım adım)?

### Kurulum

```
Giriş ─► AnaEkran.uygulamayiKur ─► cihazKaydet: POST /api/cihaz (Bearer) ─► { cihazAnahtari }
        ─► UygulamaAyar.anahtarYaz (imleç sıfırlanır) ─► Bildirimler.zamanla ─► bildirim izni istenir
zamanla:
   anahtar yok                    → iptal (101, 102)
   101 bekleyen yok               → 15 dk'lık düzenli iş (ağ varken, kalıcı)
   servis saatindeysek            → 102: en erken 1 dk, en geç 3 dk sonra (ilk yoklamadan önce servis saatleri bilinmez)
```

### Bir yoklama

```
BildirimIsi ─► yokla(c)
   anahtar = UygulamaAyar.anahtar   (boşsa dur)
   imleç boş  → GET /api/cihaz/bildirimler                 → { bildirimler: [], imlec, servisSaatleri }
   imleç dolu → GET /api/cihaz/bildirimler?son=<imleç>     → { bildirimler: [en çok 20], imlec, servisSaatleri }
        ilk yoklama değil ve uygulama arkada → her biri için goster: "Eğitim Evi" / "Yeni ödev: …"
   UygulamaAyar.imlecYaz(imleç, servisSaatleri)            (öndeyken de ilerler)
   401 / 403 → UygulamaAyar.cik + iptal                    (telefon bir daha sormaz)
   ağ yok, 429, 5xx → hiçbir şey; sonraki işte yeniden
```

### Servis saati penceresi

```
okulun sabah aralığı 07:00–09:20  →  hızlı yoklama 06:50 … 10:20   (10 dk önce, 60 dk sonra)
okulun akşam aralığı 16:30–19:00  →  hızlı yoklama 16:20 … 20:00
öteki saatlerde                    →  yalnız 15 dakikalık düzenli iş
```

### Dokunma

```
bildirim çubuğunda dokun ─► AnaEkran (Intent'te BAGLANTI var) ─► bildirimdenAc ─► BildirimlerSayfasi üste konur
```

## Dikkat!

- **Uygulama öndeyken gelen bildirim çubuğa hiç düşmez.** `yokla` öndeyken göstermez ama imleci ilerletir; o bildirimler sonra
  da gelmez. Yorumun gerekçesi ("site kendi bildirimlerini gösterir") WebView döneminden kaldı; bugünkü yerel ekran zil rozetini
  yalnız açılışta, `onResume`'da ve bildirimler okununca tazeler ([AnaEkran.md](AnaEkran.md)). Sonuç: uygulama açıkken gelen
  bildirim ne çubukta ne rozette görünür, ancak "Bildirimler" sayfası yenilenince fark edilir (kod okumasına göre).
- **20'den fazlası telefona gelmez.** Sunucu en yeni 20'yi verir, imleç en yeniye geçer; aradakiler yalnız sitede ve
  [BildirimlerSayfasi.md](BildirimlerSayfasi.md)'de görünür. İstemci de en çok 20 gösterir.
- **"Dakikada bir" kabaca 1–3 dakikadır.** Hızlı iş en erken 60 sn, en geç 3 dk sonra çalışır ve bir sonraki ancak o bitince
  kurulur; Android'in pil kuralları (Doze, uygulama bekleme grupları) her iki işi de geciktirebilir. 15 dakikalık iş de tam
  15 dakikada bir değil, Android'in uygun gördüğü anda çalışır (Android'in genel davranışı; telefonda ölçülmedi).
- **Hızlı zincir servis saatinin başında kendiliğinden başlamaz.** 102 yalnız `zamanla` çağrıldığında ve o an servis
  saatindeysek kurulur; uygulama açılmazsa zinciri ilk düzenli iş başlatır (en geç ~15 dk). 10 dakikalık ön pay bunu kısmen
  karşılar. Servis saatleri de ilk yoklamaya kadar bilinmez (giriş anında hızlı iş kurulmaz).
- **Yorumdaki "10 dk pay" yalnız başlangıcı anlatır.** Bitişe eklenen 60 dakika sunucudaki sefer uzatmasıyla aynıdır (site
  deposunda `sunucu/yardimci/servis-pencere.js` `UZATMA_DK = 60`: aralık bitince yoldaki sefer 60 dakika daha sürebilir).
- **Gün ayrımı yok.** Hafta sonu ve tatilde de servis saatlerinde sık yoklar (sunucunun aralıkları da her gün geçerli). Saat
  hep `Europe/Istanbul`'dur; telefonun saat dilimi farklıysa da doğru çalışır.
- **Bildirime dokunmak yalnız listeyi açar.** `baglanti` `Intent`'e konur ama [AnaEkran.md](AnaEkran.md) yalnız alanın varlığına
  bakar ve Bildirimler sayfasını açar; bildirimin ait olduğu ekran açılmaz.
- **Portal uyuşmazlığı.** Anahtar yetişkinin ANA hesabına aittir: telefon kişinin bütün okul rollerine gelen bildirimleri alır.
  Ama Bildirimler sayfası yalnız o an açık oturumun (portalın) bildirimlerini gösterir (`GET /api/notifications`); başka bir
  portala ait bildirime dokunan kişi onu listede bulamaz. Bağlantıdaki `?k=` (sitede portal değiştiren parça) uygulamada
  kullanılmıyor (kod okumasına göre). Sunucu buna hazır: `POST /api/kisilik/gec { tur: 'rol', id: <k> }` rol satırına geçirir,
  `k` yetişkin hesabının kendisiyse hesaba geçirir (site deposunda `sunucu/bolumler/kisilik.md`); bağlantıları açacak iş bunu
  kullanabilir.
- **Oturum bitse de yoklama sürebilir.** Uygulama anahtarı oturumdan bağımsızdır: uygulama oturumunun süresi (30 gün) sunucuda
  dolsa da anahtar geçerli kaldıkça bildirimler gelmeye devam eder. Telefon oturumun düştüğünü ancak uygulama açılıp bir istek
  401 alınca öğrenir (`oturumuBitir` → `iptal`). Şifre değişince ve çıkışta anahtar sunucuda silindiği için yoklama 401 alıp
  durur (kod okumasına göre).
- **Anahtar silinince yenisi geç alınır.** 401/403'te telefondaki anahtar unutulur; yenisini [AnaEkran.md](AnaEkran.md) ancak
  `uygulamayiKur` yeniden çalışınca alır (açılış, giriş, portal değişimi, çocuk ekleme, uygulamada aydınlatma onayı ya da
  zorunlu şifre değişimi). Arada telefon bildirimi gelmez. Aydınlatma metni güncellenince sunucunun 403 iletisi bunu söyler:
  "Aydınlatma metni güncellendi. Uygulamada onayladıktan sonra bildirimler yeniden gelir." — onay ekranı ancak uygulama
  açılınca çıkar.
- **İzin yoksa bildirimler kaybolur.** Android 13+'te bildirim izni verilmemişse `notify` gösteremez ama imleç yine ilerler;
  izin sonradan verilse de kaçanlar gelmez.
- **Çubuktaki saat yoklama anıdır.** `zaman` alanı kullanılmıyor (`setWhen` yok): bildirim çubukta gösterildiği anın saatiyle
  durur. Başlık hep "Eğitim Evi"; bildirimler gruplanmaz, 20 bildirim 20 ayrı satır olur.
- **Bildirim numarası `hashCode`.** İki farklı kimliğin aynı sayıya düşme olasılığı çok küçük ama sıfır değil; düşerse yeni
  bildirim eskisinin yerine geçer.
- **Kanal bir kez kurulur.** `kanalKur` kanal varsa hiç dokunmaz; koddaki ad ya da açıklama değişikliği kurulu telefonlara
  yansımaz. Önem (ses, öne çıkma) ise Android'in kuralı gereği kurulduktan sonra uygulama tarafından değiştirilemez, kişinin
  telefondaki kanal ayarı geçerlidir; önemi değiştirmek yeni bir kanal kimliği ister.
- **Sınıf yorumu eskidi:** "Uygulama açıkken bildirim çubuğuna yazılmaz: site kendi bildirimlerini gösterir." cümlesi siteyi
  içinde açan WebView döneminden kaldı.

## Testleri

- Android deposunda otomatik test yok; derleme ve lint (`./gradlew --offline assembleDebug lintDebug`) ana oturumda.
- Sunucu sözleşmesini site deposundaki testler korur:
  - `testler/test-servis-yoklama.js` "9) CİHAZ ANAHTARI" — oturumla anahtar; anahtar `/api/me`'ye giriş vermez; bilinmeyen
    ya da biçimsiz anahtar 401; ilk yoklamada eski bildirim yok; yeni bildirim (metin, `?k=` bağlantısı, zaman); aynı
    bildirim ikinci kez gelmez; okunmuş bildirim gitmez ama imleç ilerler; en çok 20; bozuk imleç; okul rolüne gelen bildirim;
    servis saatleri; şifre değişince ve hesap silinince iptal. "10) UYGULAMA OTURUMU (30 GÜN)" — uygulama oturumunun süresi.
  - `testler/test-ozellikler.js` — okulda Servis kapalıyken yoklama sürer ama `servisSaatleri` `null`.
  - `testler/test-servis-pencere.js` (sunucusuz) — `saatZarfi` (birden çok okulda en geniş aralık, boş listede `null`).
- Elle (öykünücü, deneme sunucusu):
  - Girişten sonra `adb shell dumpsys jobscheduler` → `org.egitimevi.aile` için 101; okulun servis saatini (müdürle sitede)
    şimdiki saati kapsayacak biçimde ayarla, bir yoklama bekle → 102 da görünür.
  - İlk yoklamayı bekle (eski bildirim gelmez), uygulamayı arkaya al, siteden bildirim üret (ör. öğretmenle ödev ver), bir
    sonraki yoklamada bildirim çubuğunda "Eğitim Evi" ve metin; dokun → Bildirimler sayfası. İşi beklemeden çalıştırmak için
    [BildirimIsi.md](BildirimIsi.md)'deki komut.
  - Siteden aynı hesabın şifresini değiştir → sonraki yoklamada 401; `dumpsys jobscheduler`'da işler kalkar.

## Son durum

- `git log`: 1 commit. Dosya `f70aeca commit 5` (2026-09-26, tek uygulamaya geçiş) ile geldi ve o günden beri değişmedi. O
  commit'te ana ekran siteyi içinde açan bir WebView'di; anahtarı site köprüsü (`Kopru.java`) alıp `zamanla`'yı çağırıyordu.
  `e96c5f2 commit 6` köprüyü kaldırdı, anahtar alma ve `zamanla` [AnaEkran.md](AnaEkran.md)'ye (`cihazKaydet`,
  `uygulamayiKur`) geçti; bu dosya aynı kaldı.
- Bilinen açıklar (kod değiştirilmedi): öndeyken gelen bildirimin kaybolması, bağlantının kullanılmaması, portal uyuşmazlığı,
  oturum bitince de yoklamanın sürmesi, eskimiş sınıf yorumu.
- Planlı işlerden bu dosyaya dokunması beklenenler:
  - "Android yerel uygulama" — bildirim bağlantıları `Sekmeler.baglantiAc` ile doğru ekranı açacak (bu dosyanın `Intent`'e
    koyduğu `baglanti` o zaman kullanılır); rol ekranları.
  - "Optimizasyon + saklama süreleri" — bildirimler 90 gün sonra silinecek; mantık denetimi 11 zamanı önemli bildirimlerin
    telefona ANINDA gitmesini, yoklamanın yalnız yedek kalmasını öneriyor (uygulamada Firebase yok; Android tarafının nasıl
    olacağı tanımda yazmıyor).
  - "Mesaj ayarları …" işindeki bildirim türü (`tur` sütunu) sunucuda eklenince yoklama cevabına da türü taşımak bildirimleri
    kanallara ayırmayı mümkün kılar (tanımda yok; öneri).

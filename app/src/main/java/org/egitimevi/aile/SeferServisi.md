# app/src/main/java/org/egitimevi/aile/SeferServisi.java

Servisçinin seferi sürerken telefonun konumunu birkaç saniyede bir (araç dururken 20 saniyede bir) sunucuya gönderen ön
plan servisi: "Sefer sürüyor" bildirimiyle ekran kapalıyken ve uygulama arkadayken de çalışır, sunucu seferi kapatınca kendini
durdurur — ama bugün onu başlatan bir ekran yok.

## Bu dosya ne yapar?

Okul servisinin sabah ve akşam seferinde veliler aracın nerede olduğunu haritada görür; araç eve 500 m ve 100 m kala
"yaklaşıyor" bildirimi gider (`sunucu/bolumler/okul-hayati.md`, site deposu). Bunun için servisçinin telefonu sefer boyunca
konumunu göndermelidir. Sitede bu iş tarayıcıda, Yoklama sayfası açıkken yapılır (`public/js/parcalar/19e-servis-konum.js`;
ekran kararmasın diye ekran kilidi tutulur, çünkü tarayıcı arka planda konum vermez). Telefon uygulamasında aynı işi bu
servis yapar: Android'in **ön plan servisi** olduğu için ekran kapansa da, servisçi başka uygulamaya geçse de sürer. Android,
arka planda konum kullanan servisin bildirim çubuğunda görünmesini şart koşar; o yüzden sefer boyunca "Sefer sürüyor —
Servisin konumu servisteki öğrencilerin velilerine gönderiliyor." bildirimi durur.

Konum oturum anahtarıyla değil, girişten sonra alınan **uygulama anahtarıyla** gider ([UygulamaAyar.md](UygulamaAyar.md),
`X-Cihaz` başlığı). Bu anahtar hesaba giriş vermez; yalnız bildirim yoklamaya ve sefer konumu göndermeye yarar.

**Bugünkü durum:** servis hazır ama hiç çalışmaz. `baslat`'ı çağıran yok (grep). Siteyi içinde açan WebView döneminde
(`commit 5`) sitedeki "Seferi başlat" köprüyle (`Kopru.seferBasladi`) `AnaEkran.seferBaslat`'ı çağırıyor, o da konum izni
isteyip `baslat`'ı açıyordu. `commit 6`'da WebView ve köprü kalktı; uygulamanın kendi servisçi ekranı henüz yazılmadı.
`durdur` ise çıkışta hâlâ çağrılıyor. Sınıfın başındaki yorum ("site köprüyle (Kopru.seferBasladi) bunu açar") bu yüzden
eskidi.

Kim için: yalnız servisçi (sunucu başka role 403 verir).

## İçinde neler var?

### Sabitler

| Ad | Değer | Anlamı | Sitedeki karşılığı |
|---|---|---|---|
| `KANAL` | `"sefer"` | bildirim kanalının kimliği (kanal adı "Servis seferi", önemi düşük: ses ve titreşim yok) | — |
| `BILDIRIM_NO` | `2` | ön plan bildiriminin numarası | — |
| `EN_AZ_ARALIK` | 5000 ms | iki gönderim arası en az süre; Android'den de bu sıklıkta konum istenir | `SEFER_GONDER_MS` |
| `NABIZ` | 20000 ms | araç dursa da son konum bu aralıkla yeniden gider | `SEFER_NABIZ_MS` |
| `EN_AZ_MESAFE` | 30 m | son gönderilen yerden bu kadar uzaklaşınca beklemeden gönder | 30 m (`mesafeMetre(...) > 30`) |

### Alanlar

- `ana` — ana iş parçacığının `Handler`'ı (nabız ve `stopSelf` buradan). `arka` — tek iş parçacıklı `ExecutorService`
  (ağ istekleri sırayla burada).
- `lm` — `LocationManager` (ilk `onStartCommand`'da alınır). `son` — en son gelen konum; `sonGiden` — en son gönderilen konum;
  `sonGonderim` — son gönderim anı (ms).
- `nabiz` — 20 saniyede bir kendini yeniden kuran iş: `son` varsa ve son gönderimden beri 20 sn geçtiyse `son`'u yeniden
  gönderir.

### Dışa açık işlevler

- `baslat(c, seferId)` — `UygulamaAyar.seferYaz(c, seferId)`, sonra `startForegroundService`. Bugün çağıranı yok.
- `durdur(c)` — `UygulamaAyar.seferYaz(c, "")`, sonra `stopService`. Çağıran: [AnaEkran.md](AnaEkran.md) `oturumuBitir`
  (çıkış ve oturum düşmesi).

### Servisin yaşam döngüsü

- `onStartCommand(i, f, id)`:
  1. Ön plan bildirimini kurar (`bildirim()`) ve hemen `startForeground` — Android 10 (API 29) ve üstünde türü
     `FOREGROUND_SERVICE_TYPE_LOCATION` belirtilerek.
  2. Saklı sefer yoksa, uygulama anahtarı yoksa ya da konum izni (`konumIzni`) yoksa `stopSelf` ve `START_NOT_STICKY`.
  3. İlk çalışmada (`lm == null`): `LocationManager`'ı alır; GPS açıksa GPS'ten, ağ konumu açıksa ağdan 5 sn / 10 m
     sınırıyla ana iş parçacığında konum ister. `SecurityException` (izin o arada geri alındı) → `stopSelf`. Sonra nabzı
     20 sn sonrasına kurar.
  4. Her durumda `START_NOT_STICKY` döner: süreç öldürülürse Android servisi kendiliğinden yeniden başlatmaz.
- `konumIzni()` (iç) — `ACCESS_FINE_LOCATION` ya da `ACCESS_COARSE_LOCATION` verilmiş mi.
- `onLocationChanged(l)`:
  - Ağdan gelen kaba konum, son 15 sn içinde gelmiş bir GPS konumunu ezmez (kod yorumu); o durumda yok sayılır.
  - `son = l`. Gönderim şartı: son gönderimden beri en az 5 sn geçmiş VE (araç son gönderilen yerden 30 m'den fazla
    uzaklaşmış YA DA son gönderimden beri 20 sn geçmiş). İlk konum her zaman gider.
- `onStatusChanged`, `onProviderEnabled`, `onProviderDisabled` — boş (yorum: eski Android sürümleri bu üçünü çağırır,
  yenilerinde varsayılanları var).
- `onDestroy()` — nabzı iptal eder, konum dinlemeyi bırakır, `arka`'yı kapatır (`shutdown`).
- `onBind` → `null` (bağlanılan servis değil).

### İç işler

- `gonder(l)` — `sonGonderim` ve `sonGiden`'i hemen günceller; saklı sefer, uygulama anahtarı ve sunucu adresini (bu an)
  okur. Sefer ya da anahtar boşsa `stopSelf`. Değilse `arka`'da `POST /api/cihaz/servis-konum` gövdesi `{ seferId, enlem,
  boylam, dogruluk }` (doğruluk metre, yuvarlanmış) — [Api.md](Api.md) `uygulama` ile, `X-Cihaz` başlığıyla. Sonuç:
  - `Api.Hata` 409 (sefer bitti ya da başka servisçiye geçti / servis saati bitti), 401 / 403 (anahtar geçersiz, servisçi
    değil, servis kapalı), 404 (sefer yok) → saklı sefer silinir (`seferYaz("")`) ve servis durur;
  - başka her hata (400, 429, 5xx, internet yok) yok sayılır; bir sonraki konumda yeniden denenir.
- `bildirim()` — "sefer" kanalı yoksa kurar (açıklaması "Sefer sürerken servisin konumu velilere gönderilirken görünür.");
  bildirim: simge `bildirim_simge`, başlık "Sefer sürüyor", metin "Servisin konumu servisteki öğrencilerin velilerine
  gönderiliyor.", kalıcı (`setOngoing`), dokununca [AnaEkran.md](AnaEkran.md)'yi öne getiren `PendingIntent`
  (`FLAG_ACTIVITY_SINGLE_TOP`, `FLAG_IMMUTABLE`; `BAGLANTI` taşımaz).

## Kimle konuşur?

- Çağırdıkları (aynı paket): [UygulamaAyar.md](UygulamaAyar.md) — `seferYaz`, `sefer`, `anahtarVar`, `anahtar`;
  [Ayarlar.md](Ayarlar.md) — `sunucu`; [Api.md](Api.md) — `uygulama`, `Api.Hata.durum`; [AnaEkran.md](AnaEkran.md) — bildirim
  dokunuşunun hedefi; `R.drawable.bildirim_simge`.
- Android: `Service`, `startForegroundService`/`startForeground`, `ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION`,
  `LocationManager` (`GPS_PROVIDER`, `NETWORK_PROVIDER`, `requestLocationUpdates`, `removeUpdates`), `LocationListener`,
  `Location.distanceTo`, `NotificationChannel`/`NotificationManager`/`Notification.Builder`, `PendingIntent`, `Handler`/`Looper`,
  `ExecutorService`.
- Manifest: `<service android:name=".SeferServisi" android:exported="false" android:foregroundServiceType="location" />`;
  izinler `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_LOCATION`,
  `POST_NOTIFICATIONS`, `INTERNET`. (`ACCESS_BACKGROUND_LOCATION` çocuğun telefonu içindir, [IzlemeServisi.md](IzlemeServisi.md).)
- Onu çağıranlar ve etkileyenler (grep):
  - [AnaEkran.md](AnaEkran.md) — `oturumuBitir` → `durdur`.
  - [BaslatmaAlici.md](BaslatmaAlici.md) — telefon açılınca ya da uygulama güncellenince saklı seferi siler (servisi yeniden
    başlatmaz).
  - [Bildirimler.md](Bildirimler.md) — yoklama 401/403 alınca `UygulamaAyar.cik` saklı seferi de siler; servis bir sonraki
    gönderimde bunu görüp durur.
  - `baslat`: yok.
- Sunucu (belgeleri site deposunda):
  - `POST /api/cihaz/servis-konum` → `sunucu/bolumler/cihaz.md`: biçimsiz/tanınmayan anahtar 401; anahtar başına dakikada 60
    (429); hesap onaysızsa ya da aydınlatma onayı eskiyse anahtar silinir, 403 `anahtarGecersiz`; servisçi değilse 403
    "Konumu servisçi gönderir"; okulda Servis kapalıysa 403 `ozellikKapali: 'servis'`. İşi `okulHayati.seferKonumuYaz` yapar
    (`sunucu/bolumler/okul-hayati.md`): konum anlaşılmazsa 400 "Konum anlaşılmadı"; sefer yoksa 404 "Sefer bulunamadı";
    bitmiş ya da başkasınınsa 409 "Sefer bitmiş ya da sana ait değil"; servis saati (ve 60 dakikalık uzatması) bittiyse sefer
    kapanır, 409 "Servis saati bitti; sefer kapandı."; başarıda son konum yazılır ve doğruluk 150 m'den iyiyse yaklaşma
    bildirimleri denenir.
  - Seferi açan uç uygulamada değil sitede: `POST /api/servis/sefer-basla` (`okul-hayati.md`). Süren seferi telefona söyleyen
    `GET /api/cihaz/ayar` (`acikSefer`) var ama uygulama bugün çağırmıyor.

## Nasıl çalışır (adım adım)?

```
(gelecekteki servisçi ekranı) konum izni var mı? ── evet ─► SeferServisi.baslat(ekran, seferId)
     UygulamaAyar.sefer = seferId ─► startForegroundService
onStartCommand ─► startForeground("Sefer sürüyor")
     sefer / uygulama anahtarı / konum izni eksik? ─► stopSelf
     GPS açık → GPS dinle ; ağ konumu açık → ağı dinle   (5 sn, 10 m)   ; 20 sn'lik nabız
onLocationChanged(l):
     ağ konumu ve 15 sn içinde GPS gelmiş? ─► yok say
     son = l
     ≥ 5 sn geçti VE (30 m'den fazla gitti YA DA ≥ 20 sn geçti) ─► gonder(l)
nabız (20 sn'de bir): son var ve ≥ 20 sn gönderilmedi ─► gonder(son)     (araç dururken)
gonder ─► [arka] POST /api/cihaz/servis-konum  X-Cihaz: <uygulama anahtarı>
              200            ─► devam
              409/401/403/404 ─► sefer silinir ─► stopSelf ─► onDestroy (dinleme ve nabız biter)
              başka / ağ yok  ─► yok say, sonraki konumda yeniden
çıkış (AnaEkran.oturumuBitir) ─► durdur ─► sefer silinir ─► stopService
```

## Dikkat!

- **Bugün hiçbir yerden başlatılmıyor.** Servisçi uygulamaya girse bile konumu gitmez; sitede Yoklama sayfasını açık tutması
  gerekir (KILAVUZ da "hazırlanan telefon uygulaması … gönderecek" diyor). [BaslatmaAlici.md](BaslatmaAlici.md)'deki sefer
  temizliği de bu yüzden bugün boşa çalışıyor.
- **İzin denetimi `startForeground`'dan SONRA.** `onStartCommand` önce `startForeground`'u konum türüyle çağırıyor, izni sonra
  denetliyor. Uygulama Android 14 ve üstünü hedefliyor (`targetSdk 36`); Android'in kuralına göre bu sürümlerde konum
  izni olmadan konum türlü ön plan servisi başlatmak `SecurityException` fırlatır, yani izinsiz bir `baslat` uygulamayı
  çökertebilir (kod okumasına ve Android belgesine göre; telefonda denenmedi). Eski düzende `AnaEkran.seferBaslat` önce izni
  istiyordu; yeni servisçi ekranı da `baslat`'tan önce izni almalı. Ayrıca Android 12'den beri ön plan servisi uygulama
  arkadayken başlatılamaz: `baslat` bir dokunuşla, uygulama öndeyken çağrılmalı.
- **Konum kapalıysa sessizce boş çalışır.** Servis başlarken GPS de ağ konumu da kapalıysa hiçbir şey dinlenmez; bildirim
  "Sefer sürüyor" der ama tek konum gitmez. Sonradan konumu açmak da işe yaramaz (`onProviderEnabled` boş, dinleme yeniden
  kurulmaz). Başlarken yalnız biri açıksa (ör. GPS kapalı) sonradan açılan öbürü de dinlenmez. Site aynı durumda "Konum
  alınamıyor (GPS kapalı ya da sinyal yok)." gösteriyor; burada servisçiye hiçbir şey söylenmez.
- **Hatalar görünmez.** Servisin ekranla bağı yok: 400, 429, 5xx ve internet kesintisi yok sayılır; sefer bitince (409)
  servis sessizce durur, bildirim kalkar. Servisçi konumunun gidip gitmediğini telefondan anlayamaz.
- **Yavaş ağda istekler birikir.** Her uygun konumda `arka` kuyruğuna yeni bir istek eklenir; bir istek bağlantı ve okuma
  süreleriyle (15 + 20 sn) uzun sürebildiği için kötü bağlantıda kuyruk büyür ve eski konumlar sonradan sırayla gider.
  Sitede "gönderiliyor" bayrağı aynı anda tek istek bırakıyor; burada böyle bir denetim yok (kod okumasına göre).
- **Süreç ölürse sefer konumu durur.** `START_NOT_STICKY`: Android süreci öldürürse servis yeniden başlamaz; telefon yeniden
  açılırsa [BaslatmaAlici.md](BaslatmaAlici.md) saklı seferi siler (sınıf yorumu: "Yarıda kalmış sefer sürdürülmez (servisçi
  yeniden başlatır)"). Sunucu 45 dakika konum gelmeyen seferi kendisi kapatır (KILAVUZ).
- **Bildirimi kapatma ya da "Durdur" düğmesi yok.** Bildirim kalıcıdır (`setOngoing`); servis yalnız sunucu reddedince,
  çıkışta, anahtar geçersizleşince ya da süreç ölünce durur. Android 13 ve üstünde bildirim izni verilmemişse bildirim
  çubukta görünmeyebilir (Android ön plan servisini yine çalıştırır).
- **Kaba konum yaklaşma bildirimi tetiklemez.** Yalnız ağ konumu varsa (GPS kapalı ya da kapalı alanda) doğruluk çoğu zaman
  150 m'den kötüdür; sunucu konumu yazar ama yaklaşma bildirimi göndermez.
- **Değerler gönderim anında okunur.** Sefer kimliği, anahtar ve sunucu adresi her gönderimde yeniden okunur: `baslat`
  yeni bir seferle yeniden çağrılırsa servis yeniden kurulmadan yeni sefere gönderir. `durdur` ya da `UygulamaAyar.cik`
  saklı seferi silince bir sonraki gönderim servisi durdurur; `onDestroy` `shutdown` kullandığı için kuyrukta bekleyen
  istekler yine de gider (sefer bittiyse sunucu onlara da 409 verir; zararsız).
- Konum yalnız uygulama anahtarıyla gider: anahtar alınamamışsa (girişten sonra `POST /api/cihaz` başarısızsa,
  [AnaEkran.md](AnaEkran.md) `cihazKaydet`) servis hemen durur.

## Testleri

- Android deposunda otomatik test yok; derleme ve lint (`./gradlew --offline assembleDebug lintDebug`) ana oturumda.
- Sunucu sözleşmesini site deposundaki testler korur:
  - `testler/test-servis-yoklama.js` — uygulama anahtarıyla sefer konumu gönderilip haritada görünmesi; `GET /api/cihaz/ayar`'da
    servisçi ve açık sefer; veli anahtarı 403, başkasının seferi 409, olmayan sefer 404, bozuk konum 400; anahtarla konumun
    dakikada 60 ile sınırlı olması; servis saati uzatmasında konumun sürmesi.
  - `testler/test-ozellikler.js` — okulda Servis kapalıyken `servis-konum` 403 `ozellikKapali`.
  - `testler/yetki-denetimi.js` — uçların yetki taraması.
- Elle: bugün uygulamada seferi başlatan bir düğme olmadığı ve servis dışa kapalı (`exported="false"`) olduğu için telefonda
  denenemez. Servisçi ekranı yazılınca: öykünücüde konum izni ver, uygulamanın servisçi ekranından seferi başlat,
  `adb emu geo fix <boylam> <enlem>` ile konumu değiştir → velinin haritasında araç ilerlemeli; uygulamayı arkaya al, ekranı
  kapat → konum gelmeye devam etmeli; aynı servisçiyle sitede "Okula vardık" → bir sonraki gönderimde 409, bildirim
  kalkmalı.

## Son durum

- `git log` (Android deposu): 1 commit. Dosya `f70aeca commit 5` (2026-09-26, tek uygulamaya geçiş) ile geldi: uygulama
  siteyi içinde açarken sitenin "Seferi başlat"ı köprüyle bu servisi açıyordu; konum uygulama anahtarıyla
  `/api/cihaz/servis-konum`'a gidiyor, 409/401/403/404'te servis duruyordu. Dosya o günden beri değişmedi.
- Hemen ardından `e96c5f2 commit 6` (2026-09-26) bu dosyaya dokunmadan onu çağıranı kaldırdı: `Kopru.java` silindi,
  `AnaEkran`'daki `seferBaslat` (konum izni isteyip `baslat`'ı çağıran) ve izin sonucu işleyicisi yeni iskeletle gitti. O
  günden beri `baslat`'ın çağıranı yok.
- Bilinen açıklar (kod değiştirilmedi): başlatanın olmaması, izin denetiminin `startForeground`'dan sonra gelmesi, konum
  kapalıyken sessiz boş çalışma, hataların görünmemesi, yavaş ağda kuyruk birikmesi, eskimiş sınıf yorumu.
- Planlı işlerden bu dosyaya dokunması beklenenler:
  - "Android yerel uygulama" (iş 10): servisçinin Yoklama ekranı (sabah Bindi/Binmedi, Seferi başlat/Okula vardık; akşam
    Geldi/Gelmedi → Başlat → İndi) "SeferServisi ile arka plan konumu" — `baslat` oradan, konum izni alındıktan sonra
    çağrılacak; uygulama yeniden açılınca süren seferi `GET /api/cihaz/ayar` ile bulup sürdürmek de düşünülmeli (öneri).
  - "KVKK ve onay metinleri TAM denetimi" (iş 18): her veri ve kimin gördüğü aydınlatma metniyle karşılaştırılacak;
    servisçinin konumunun velilere gitmesi de bu işin bakacağı verilerden.

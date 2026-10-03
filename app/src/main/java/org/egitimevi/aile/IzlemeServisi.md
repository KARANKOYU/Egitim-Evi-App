# app/src/main/java/org/egitimevi/aile/IzlemeServisi.java

Çocuğun telefonunun (Eğitim Evi Aile) arka plan servisi: bildirim çubuğunda görünerek sürekli çalışır, velinin seçtiği
aralıkla konum alıp kuyruğa koyar ve gönderir, 15 dakikada bir ekran süresini yollar, 30 dakikada bir ayarları tazeler;
sunucu telefonu artık tanımazsa bağlantıyı unutup kendini durdurur.

## Bu dosya ne yapar?

Öğrenci [AileEkrani.md](AileEkrani.md)'de kendi hesabı ve açık onayıyla telefonunu velisine bağlayınca telefona bir **Aile
anahtarı** kalır. Bundan sonraki her şeyi bu servis yapar: telefonun konumunu alır, internet yoksa biriktirir, varsa
gönderir; hangi uygulamada kaç dakika geçtiğini okuyup gönderir; velinin sitede değiştirdiği ayarları (gönderme sıklığı,
konum/ekran süresi açık mı) arada bir sorar. Veli bunları sitedeki "Çocuğumun telefonu" sayfasında görür; okul görmez;
sunucu 7 gün sonra siler. Servis hiçbir uygulamayı kapatmaz ya da kilitlemez.

Android, arka planda sürekli konum alan bir uygulamanın bunu gizlice yapmasına izin vermez: servis bir **ön plan servisi**
olmalı ve bildirim çubuğunda durmalıdır. Bu yüzden paylaşım sürdükçe çubukta "Eğitim Evi Aile — Konumun ve ekran süren
velinle paylaşılıyor." yazar; dokununca Aile ekranı açılır. Öğrenci için bu, paylaşımın sürdüğünü her an görmenin de yolu.

Servis öğrencinin oturumuyla değil, yalnız konum ve süre göndermeye yarayan Aile anahtarıyla (`X-Aile-Cihaz` başlığı)
konuşur; bu anahtar öğrencinin hesabına giriş vermez. Uygulamanın asıl ekranından ([AnaEkran.md](AnaEkran.md)) çıkış yapmak
bu servisi durdurmaz: paylaşımı durdurmanın yolu Aile ekranındaki "Bu telefonun bağlantısını kaldır" ya da velinin sitede
telefonu kaldırması.

Dosya Eğitim Evi Aile 1.0.x'ten (uygulamanın yalnız çocuğun telefonu olduğu dönem) gelir; kurulu eski uygulamalar da aynı
uçlarla çalıştığı için sunucuyla konuşma biçimi o günden beri aynıdır.

## İçinde neler var?

### Sabitler ve alanlar

- `KANAL` — bildirim kanalı `"izleme"`: adı "Aile paylaşımı", önemi `IMPORTANCE_LOW` (ses ve titreşim yok), açıklaması
  "Konum ve ekran süresi velinle paylaşılırken görünür.". `BILDIRIM_NO` — 7 (servisçinin sefer bildirimi 2;
  `SeferServisi.java`). `DAKIKA` — 60 000 ms.
- `is` — `"aile-izleme"` adlı `HandlerThread`; `el` — onun `Handler`'ı: tur, ağ istekleri ve gönderim burada, ana iş
  parçacığında değil, çalışır. `lm` — `LocationManager`.
- `istenenDk` — şu an kurulu konum aralığı (dakika). `-1`: konum dinlenmiyor; `-2`: "bir sonraki turda yeniden kur"
  işareti (ayar tazelendi ya da bir konum sağlayıcısı açıldı).
- `sonKullanim`, `sonAyar` — son ekran süresi gönderiminin ve son ayar tazelemesinin anı. Yalnız bellekte: servis her
  yeniden kurulduğunda 0'dan başlar, yani ilk turda ikisi de hemen yapılır.

### Başlatma ve durdurma (dışa açık)

- `baslat(c)` — telefon bağlı değilse (`Ayarlar.bagli` yanlış, yani Aile anahtarı yok) hiçbir şey yapmaz; bağlıysa
  `startForegroundService`. Konum iznine BAKMAZ; o denetim servisin içindedir (aşağıda "Dikkat!").
- `durdur(c)` — `stopService`.

### Yaşam döngüsü (dışa açık, Android çağırır)

- `onCreate` — `"aile-izleme"` iş parçacığını başlatır, `el`'i ve `lm`'yi kurar.
- `onStartCommand` — bağlı değilse ya da konum izni (hassas YA DA yaklaşık; `konumIzniVar`) yoksa `stopSelf()` ve
  `START_NOT_STICKY`. Varsa: ön plan bildirimini kurar ve ön plana geçer (Android 10+ `FOREGROUND_SERVICE_TYPE_LOCATION`
  türüyle, öncesinde türsüz), `el`'deki bekleyen her işi siler, hemen bir `tur` koyar ve `START_STICKY` döner (sistem
  servisi öldürürse yeniden başlatsın). Servis zaten çalışırken yeniden `baslat` edilirse de bu yol izlenir: bekleyen tur
  silinir, hemen yeni bir tur atılır.
- `onLocationChanged(l)` — yeni konum (ana iş parçacığında gelir; konum dinleyicisi ana döngüye bağlıdır):
  1. Süzgeç: `son` = son kabul edilen konumun zamanı (`Ayarlar.sonKonum`, telefonda kalıcı), `aralik` = `max(1, istenenDk)`
     dakika. Konumun zamanı `son`'dan aralığın %80'inden az ileriyse atılır. Böylece GPS ve ağ sağlayıcısının aynı aralıkta
     verdiği iki konumdan yalnız ilk gelen tutulur; %80 payı sağlayıcının biraz erken gelmesine izin verir.
  2. JSON kurulur: `enlem`, `boylam`, `dogruluk` (metre, yuvarlanmış; konumda doğruluk yoksa `null` — `JSONObject.put`
     `null` değerde anahtarı hiç koymaz), `zaman` (`Location.getTime()`, Unix ms), `ag` (o anki ağ türü: `"wifi"`,
     `"mobil"` ya da `""`), `pil` (yüzde).
  3. `Kuyruk.ekle` ([Kuyruk.md](Kuyruk.md)) ve `Ayarlar.konumAlindi(zaman)` (Aile ekranındaki "Son konum").
  4. `el.post(konumlariGonder)` — internet varsa konum hemen gider. Bir hata olursa iletisi `Ayarlar.hata`'ya yazılır.
- `onDestroy` — konum dinlemesini bırakır (hatası yutulur), `el`'deki işleri siler, iş parçacığını `quitSafely` ile kapatır.
- `onBind` — `null` (bu servise bağlanılmaz, yalnız başlatılır).
- `onProviderEnabled` — `istenenDk = -2`: bir sonraki turda dinleme baştan kurulsun. `onProviderDisabled` ve eskimiş
  `onStatusChanged` boş. (Yorum "Eski Android sürümleri için boş gövdeler" diyor ama `onProviderEnabled` boş değil.)

### Tur ve iç işler

- `tur()` — dakikada bir, `el` üzerinde, sırasıyla:
  1. `konumIsteginiAyarla()`;
  2. son ayar tazelemesinden 30 dakika geçtiyse `ayarlariTazele()`;
  3. veli ekran süresini kapatmadıysa (`Ayarlar.kullanimAcik`) ve son gönderimden 15 dakika geçtiyse `kullanimGonder()`;
  4. `konumlariGonder()`.
  Herhangi bir hata `Ayarlar.hata`'ya yazılır; ne olursa olsun tur bir dakika sonrasına yeniden kurulur.
- `konumIzniVar()` — `ACCESS_FINE_LOCATION` ya da `ACCESS_COARSE_LOCATION` verilmiş mi. "Her zaman" iznine
  (`ACCESS_BACKGROUND_LOCATION`) bakmaz.
- `agTuru()` — etkin ağ yoksa ya da ağda internet yeteneği yoksa `""`; Wi-Fi ya da Ethernet ise `"wifi"`; başka her şey
  (mobil veri vb.) `"mobil"`.
- `konumIsteginiAyarla()` — veli konumu kapattıysa (`Ayarlar.konumAcik` yanlış) ya da izin yoksa dinlemeyi bırakır
  (`istenenDk = -1`). Değilse aralığı seçer: Wi-Fi'de `Ayarlar.wifiAraligi` (varsayılan 5 dk), mobil veride YA DA internet
  yokken `Ayarlar.mobilAraligi` (varsayılan 15 dk). Aralık kurulu olanla aynıysa dokunmaz. Değiştiyse eski dinlemeyi bırakır
  ve o an AÇIK olan sağlayıcılara (`GPS_PROVIDER`, `NETWORK_PROVIDER`) `requestLocationUpdates(sağlayıcı, aralık, 0 metre,
  this, ana döngü)` ile yeniden abone olur. `SecurityException` gelirse (izin o arada geri alındı) `istenenDk = -1`; sonraki
  turda yeniden denenir.
- `pil()` — `BatteryManager.BATTERY_PROPERTY_CAPACITY` (yüzde); `BatteryManager` alınamazsa -1. Özelliği desteklemeyen
  telefonda Android `Integer.MIN_VALUE` döndürür; sunucu 0–100 dışını boş sayar.
- `konumlariGonder()` — internet yoksa ya da kuyruk boşsa çıkar. En çok 10 kez: `Kuyruk.bastan(500)` →
  `POST /api/aile/cihaz/konum { konumlar: [...] }` → başarıysa `Kuyruk.sil(gönderilen sayısı)` ve
  `Ayarlar.gonderildi(şimdi)` (Aile ekranındaki "Son gönderim"; "Son sorun" silinir). Yani bir seferde en çok 5000 konum,
  kuyruğun tamamı. `Api.Hata` → `baglantiKoptuMu`; başka hata (internet koptu, zaman aşımı) → yalnız `Ayarlar.hata`, kuyruk
  bekler.
- `kullanimGonder()` — internet yoksa ya da kullanım erişimi verilmemişse (`Kullanim.izinVar`) çıkar; `sonKullanim`
  değişmediği için her turda yeniden bakar. Varsa `POST /api/aile/cihaz/kullanim { gunler: Kullanim.sonGunler(this, 2) }`
  (dün ve bugün; [Kullanim.md](Kullanim.md)) → `sonKullanim` ve `Ayarlar.gonderildi`.
- `ayarlariTazele()` — internet yoksa çıkar. `GET /api/aile/cihaz/ayar` → `{ ayar: { wifiDk, mobilDk, konumAcik,
  kullanimAcik } }` → `Ayarlar.ayariYaz` (gelmeyen alanlar 5, 15, açık, açık; aralıklar en az 1'e yuvarlanır) → `sonAyar`,
  `istenenDk = -2` (aralık değişmiş olabilir; bir sonraki turda yeniden kurulur).
- `baglantiKoptuMu(h)` — iletiyi `Ayarlar.hata`'ya yazar; durum 401 ya da 403 ise sunucu bu telefonu artık tanımıyor
  demektir (veli ya da öğrenci bağlantıyı kaldırdı, öğrencinin hesabı silindi, dördüncü telefon bağlandı): `Ayarlar.cik`
  (anahtar ve ayarlar silinir, sunucu adresi kalır), `Kuyruk.temizle`, `stopSelf`.
- `bildirim()` — kanal yoksa kurar; dokununca `AileEkrani`'ni açan `PendingIntent` (`FLAG_IMMUTABLE`); küçük simge
  `R.drawable.bildirim_simge`, başlık "Eğitim Evi Aile", metin "Konumun ve ekran süren velinle paylaşılıyor.",
  `setOngoing(true)` (kaydırılıp kapatılamaz).

## Kimle konuşur?

- Çağırdıkları (aynı paket):
  - [Ayarlar.md](Ayarlar.md) — `bagli`, `sunucu`, `cihazAnahtari`, `konumAcik`, `kullanimAcik`, `wifiAraligi`,
    `mobilAraligi`, `sonKonum`, `konumAlindi`, `gonderildi`, `ayariYaz`, `hata`, `cik`.
  - [Kuyruk.md](Kuyruk.md) — `ekle`, `boyut`, `bastan`, `sil`, `temizle`.
  - [Kullanim.md](Kullanim.md) — `izinVar`, `sonGunler`.
  - [Api.md](Api.md) — `Api.get(sunucu, yol, anahtar)` ve `Api.post(sunucu, yol, govde, null, anahtar)`: son argüman Aile
    anahtarıdır ve `X-Aile-Cihaz` başlığına konur; oturum (Bearer) gönderilmez. Hatalar `Api.Hata` (`durum`, ileti).
    Ekranların istek kapısı [Ag.md](Ag.md)'yi kullanmaz (ekran yok, ana iş parçacığı yok).
  - [AileEkrani.md](AileEkrani.md) — yalnız bildirime dokununca açılan ekran olarak.
- Onu başlatan ve durduranlar (grep):
  - [AileEkrani.md](AileEkrani.md) — `onResume`'da (bağlıysa ve konum izni varsa) ve konum izni verilince `baslat`;
    "Bu telefonun bağlantısını kaldır"da `durdur` (ardından `Kuyruk.temizle`, `Ayarlar.cik`).
  - [BaslatmaAlici.md](BaslatmaAlici.md) — telefon açılınca (`BOOT_COMPLETED`) ve uygulama güncellenince
    (`MY_PACKAGE_REPLACED`) `baslat`.
  - Manifest: `.IzlemeServisi`, `exported="false"` (yalnız uygulamanın kendisi başlatır), `foregroundServiceType="location"`.
- Sunucu uçları (site deposunda `sunucu/bolumler/aile.md`). `sunucu/api.js` bu yolları oturum kapılarından ÖNCE
  `cihazUclari`'na verir: aydınlatma onayı ya da zorunlu şifre burada sorulmaz.

  | Uç | Ne zaman | Cevap ve kurallar |
  |---|---|---|
  | `GET /api/aile/cihaz/ayar` | 30 dakikada bir (servis her başlayınca hemen) | `{ ayar: { wifiDk, mobilDk, konumAcik, kullanimAcik } }`; veli aralıkları 1, 5, 10, 15, 30, 60 dakikadan seçer |
  | `POST /api/aile/cihaz/konum` | her kabul edilen konumdan hemen sonra; kuyrukta bekleyen varsa her turda | ilk 500 konum işlenir; geçersiz enlem/boylam, 7 günden eski ya da 5 dakikadan ileri zamanlı konum atılır; aynı öğrencide aynı zaman damgası bir kez yazılır; veli konumu kapattıysa `{ alinan: 0, kapali: true }`, değilse `{ alinan }` |
  | `POST /api/aile/cihaz/kullanim` | 15 dakikada bir (servis her başlayınca hemen) | ayrıntı [Kullanim.md](Kullanim.md); `{ alinan }` ya da veli kapattıysa `{ kapali: true }`; ardından velinin sınırları denetlenir |

  Üçünde ortak: anahtar 64 onaltılık hane değilse ya da tanınmıyorsa 401 "Cihaz tanınmadı. Uygulamadan yeniden bağlan.";
  öğrenci silinmiş, öğrenci değil ya da onaylı değilse cihaz kaydı silinir ve 401 "Hesap artık kullanılamıyor."; cihaz
  başına saatte 240 istek (429 "Çok sık istek geldi. Biraz sonra dene."); her istek cihazın "son görülme" zamanını yazar
  (veli sayfasında görünür). Bugün bu uçlar 403 döndürmüyor; kod 403'ü de "bağlantı koptu" sayar.
- Android: `Service` (ön plan, `location` türü), `LocationManager` (`GPS_PROVIDER`, `NETWORK_PROVIDER`), `LocationListener`,
  `ConnectivityManager` / `NetworkCapabilities`, `BatteryManager`, `NotificationManager` / `NotificationChannel` /
  `Notification.Builder`, `PendingIntent`, `HandlerThread` / `Handler`.
- İzinler ([AndroidManifest.xml](../../../../AndroidManifest.xml)): `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`,
  `ACCESS_BACKGROUND_LOCATION` ("her zaman"; Aile ekranı ayrı satırla ister), `FOREGROUND_SERVICE`,
  `FOREGROUND_SERVICE_LOCATION`, `POST_NOTIFICATIONS`, `ACCESS_NETWORK_STATE`, `INTERNET`; ekran süresi için
  `PACKAGE_USAGE_STATS` (öğrenci telefonun ayarından açar).
- Veriler: gönderilen konum (doğruluk, ağ türü ve pil düzeyiyle) ve uygulama süreleri yalnız öğrenciye bağlı velilere
  görünür, okul görmez, sunucuda 7 gün sonra silinir — site deposundaki aydınlatma metninin (`public/kvkk/kvkk.html`)
  "Eğitim Evi Aile" satırları bunu anlatır.
- Rol: yalnız öğrencinin telefonunda çalışır (bağlamayı sunucu yalnız öğrenciye yaptırır).

## Nasıl çalışır (adım adım)?

```
AileEkrani (bağlandı, konum izni verildi) / BaslatmaAlici (telefon açıldı)
  └─ IzlemeServisi.baslat ─ bağlı mı? ─ hayır → hiçbir şey
                                      └ evet → startForegroundService
onStartCommand: izin yok → stopSelf | izin var → ön plan bildirimi "Eğitim Evi Aile", el.post(tur)

tur (her dakika, "aile-izleme" iş parçacığı)
  1 konumIsteginiAyarla: konum açık + izin var?
       ağ: wifi → wifiAraligi (5)   mobil ya da internetsiz → mobilAraligi (15)
       aralık değişti → removeUpdates + requestLocationUpdates(GPS, AĞ; aralık, 0 m)
  2 30 dk geçti → GET  /api/aile/cihaz/ayar      → Ayarlar.ayariYaz, istenenDk = -2
  3 15 dk geçti → POST /api/aile/cihaz/kullanim  { gunler: [dün, bugün] }
  4 kuyruk dolu → POST /api/aile/cihaz/konum     { konumlar: ilk 500 } → Kuyruk.sil  (en çok 10 kez)
  └ 1 dk sonra yeniden

onLocationChanged (ana iş parçacığı)
  zaman - sonKonum < 0,8 × aralık → at
  { enlem, boylam, dogruluk, zaman, ag, pil } → Kuyruk.ekle → Ayarlar.konumAlindi → el.post(konumlariGonder)

sunucu 401/403 (bağlantı kaldırıldı, hesap silindi) → Ayarlar.cik + Kuyruk.temizle + stopSelf
                                                       (bildirim kalkar; Aile ekranı yeniden giriş formunu gösterir)
```

## Dikkat!

- **İzinsiz başlatma ön plan kuralına takılabilir.** `baslat` izne bakmadan `startForegroundService` çağırır; izin yoksa
  `onStartCommand` ön plana HİÇ geçmeden `stopSelf` der. Android 8 ve üstünde `startForegroundService` ile başlatılan bir
  servisin `startForeground`'u çağırmadan durması, sistemin uygulamayı "Context.startForegroundService() did not then call
  Service.startForeground()" hatasıyla kapatmasına yol açabilir. Aile ekranı izni denetleyip öyle başlatıyor, ama
  [BaslatmaAlici.md](BaslatmaAlici.md) telefon açılışında yalnız "bağlı mı"ya bakıyor: öğrenci konum iznini sonradan
  geri aldıysa telefon her açılışında bu yol izlenir. Android'in bilinen davranışına göre yazıldı; telefonda denenmedi.
  Düzeltme önerisi: `baslat` da `konumIzniVar`'a baksın ya da `onStartCommand` önce `startForeground` deyip sonra dursun.
- **Açılışta konum servisi ve "her zaman" izni.** Servis yalnız "uygulamayı kullanırken" iznine bakıyor. Yeni Android
  sürümleri arka plandan (telefon açılışı gibi) başlayan konum servisine ek koşul koyuyor (ör. 14 ve üstünde konum izninin
  servis başlarken bulunması; "her zaman" izni yoksa arka planda başlayan servisin konuma erişememesi). Öğrenci "Her
  zaman izin ver"i seçmediyse açılışta servis konum alamayabilir ya da `startForeground` hata verebilir — o hata burada
  yakalanmıyor. Android belgelerine dayanan uyarı; telefonda doğrulanmadı. Açılış yolu [BaslatmaAlici.md](BaslatmaAlici.md).
- **Ekran kapalıyken dakikalık tur aksayabilir.** Servis `WakeLock` tutmuyor ve `Handler.postDelayed`'in saati
  (`SystemClock.uptimeMillis`) telefon derin uykudayken ilerlemez (Android belgesi). Konum güncellemeleri telefonu
  uyandırdıkça konum yine gönderilir; ama 15 dakikalık ekran süresi ve 30 dakikalık ayar tazelemesi gecikebilir. Aile
  ekranındaki "Pil > Kısıtlamasız" ipucu bu yüzden önemli. Telefonda ölçülmedi.
- **"Daha doğrusu tutulur" yorumu kodla uyuşmuyor.** `onLocationChanged`'in açıklaması aynı aralıkta iki sağlayıcıdan
  gelen konumdan daha doğru olanın tutulduğunu söylüyor; kod doğruluğu karşılaştırmıyor, ilk geleni tutup ikincisini
  atıyor. Ağ sağlayıcısının konumu GPS'inkinden önce gelirse kaba konum kalır, GPS'inki atılır (kod okumasına göre;
  hangisinin önce geldiği telefonda ölçülmedi). Düzeltme önerisi: aralık içinde daha küçük `dogruluk`'lu konum gelirse
  kuyruktaki sonuncunun yerine koymak.
- **Kuyruğa yazmak ana iş parçacığında.** Konum dinleyicisi ana döngüye bağlı; `Kuyruk.ekle` her konumda bütün dosyayı
  okuyup yeniden yazar ([Kuyruk.md](Kuyruk.md)). Aynı süreçte uygulamanın ekranı açıksa bu iş o iş parçacığını kısa süre
  meşgul eder (kuyruk büyükse daha uzun).
- **Gönderilen parçayı silmek tek işlem değil.** `bastan(500)` ile alınan parça gönderilirken ana iş parçacığı yeni konum
  ekleyebilir; kuyruk 5000'de doluysa ya da baştaki konumlar o arada 7 günü geçtiyse ekleme BAŞTAN konum atar ve sonraki
  `sil(n)` gönderilmemiş birkaç konumu da siler. Nadir; ayrıntısı [Kuyruk.md](Kuyruk.md)'de.
- **Bağlantı koptuğunda neden de gider.** `baglantiKoptuMu` önce iletiyi `Ayarlar.hata`'ya yazar, hemen ardından
  `Ayarlar.cik` dosyanın her şeyini (son sorun dahil) siler. Öğrenci Aile ekranını açınca yalnız giriş formunu görür, neden
  koptuğunu ("Cihaz tanınmadı…", "Hesap artık kullanılamıyor.") görmez; telefona bir bildirim de gitmez, yalnız çubuktaki
  "Eğitim Evi Aile" bildirimi kalkar.
- **Velinin ayar değişikliği yaklaşık yarım saatte gelir.** Veli konumu kapatınca telefon bunu bir sonraki ayar
  tazelemesinde (son tazelemeden 30 dakika sonraki ilk turda) öğrenir; o arada alınan konumlar gönderilir ve sunucu
  onları yazmadan atar (`kapali: true`), telefon yine kuyruktan siler — zararsız. Açınca da konum aynı gecikmeyle
  başlar; aralık değişikliği de öyle (turda `konumIsteginiAyarla` tazelemeden ÖNCE çalıştığı için yeni aralık bir tur,
  yani bir dakika sonra kurulur). Telefon internetsizken ayar hiç tazelenmez.
- **Sonradan açılan sağlayıcı geç dinlenir.** `konumIsteginiAyarla` yalnız o an açık sağlayıcılara abone olur; aralık aynı
  kaldıkça yeniden kurmaz. Telefonun konumu kapalıyken servis başlarsa ya da GPS sonradan açılırsa, `onProviderEnabled`
  yalnız abone olunan sağlayıcı için gelir; yeni sağlayıcı ancak aralık yeniden kurulunca (ağ türü değişip aralık
  değişince ya da ayar tazelenince, en geç 30 dakika ve internet varsa) dinlenmeye başlar. Kod okumasına göre.
- **Bildirim izni yoksa paylaşım çubukta görünmez.** Android 13 ve üstünde `POST_NOTIFICATIONS` verilmemişse ön plan
  servisi yine çalışır ama bildirimi çubukta gösterilmez (Android'in kuralı). Öğrenci paylaşımın sürdüğünü ancak Aile
  ekranından görür. Aile ekranı bu izni ayrı bir satırla istiyor; şeffaflık için önemli.
- **"Son sorun" ham iletidir.** Yakalanan her hatanın iletisi olduğu gibi `Ayarlar.hata`'ya yazılır; ağ hatalarında bu
  çoğu zaman Android'in İngilizce cümlesidir ([Ayarlar.md](Ayarlar.md), [AileEkrani.md](AileEkrani.md)).
- **Her başlatmada hemen bir tur.** Aile ekranı her açılışında (`onResume`) `baslat` çağırır; `onStartCommand` bekleyen turu
  silip hemen yenisini atar. Servis yeni kurulduysa `sonAyar` ve `sonKullanim` 0 olduğundan ayar ve ekran süresi de hemen
  istenir/gönderilir; servis zaten çalışıyorsa 15/30 dakika kuralı sürer.
- **İstek sayısı sunucu sınırının altında.** En sık ayarda (Wi-Fi'de 1 dakika) saatte en çok ~75 konum isteği (%80
  süzgeciyle), 4 ekran süresi ve 2 ayar isteği: cihaz başına 240'lık saatlik sınırın altında. 429 gelirse "Son sorun"
  olarak görünür, konumlar kuyrukta bekler.
- **`istenenDk` iki iş parçacığından değişir.** Ana iş parçacığı (`onLocationChanged` okur, `onProviderEnabled` yazar) ve
  servis iş parçacığı (`tur`) aynı alanı `volatile` olmadan kullanır; en kötü durumda değişiklik bir tur gecikir.
- **Eski kurulumlarla uyum.** Uç adları, gövde alanları (`konumlar`, `gunler`, `enlem`, `boylam`, `dogruluk`, `zaman`,
  `ag`, `pil`) ve `X-Aile-Cihaz` başlığı Eğitim Evi Aile 1.0.x'te de aynı; sunucuda ya da burada değiştirilirse kurulu eski
  telefonlar susar.
- Toplanan verinin listesi (konum, doğruluk, ağ türü, pil, uygulama süreleri) aydınlatma metniyle birebir; buraya yeni bir
  alan eklenirse aynı işte `public/kvkk/kvkk.html` ve sürümü güncellenmeli (site deposundaki KVKK kuralı).

## Testleri

- Android deposunda otomatik test yok; derleme ve lint (`./gradlew --offline assembleDebug lintDebug`) ana oturumda.
- Sunucu sözleşmesini site deposundaki testler korur:
  - `testler/test-aile.js` — anahtarsız, yanlış ve biçimsiz anahtar 401; ayar okunuyor (`konumAcik`); konum süzgeci
    (geçersiz enlem, 7 günden eski, gelecek, bozuk zaman atılır), "aynı zaman damgası iki kez yazılmaz" (yeniden
    göndermeyi zararsız kılan kural), "bir istekte en fazla 500 konum işlenir" (servisin parça boyuyla aynı sayı);
    kullanım süzgeci; veli yeni aralık seçince "telefon yeni aralığı alıyor (Wi-Fi 1, mobil 30 dk)"; veli konumu kapatınca
    `alinan: 0, kapali: true`; veli ya da öğrenci bağlantıyı kaldırınca anahtar 401 (servisin kendini durdurduğu yol).
  - `testler/test-servis-yoklama.js` — uygulamanın `X-Cihaz` anahtarı Aile uçlarında 401 (iki anahtar karışmaz).
- Elle (öykünücü, deneme paketi, deneme sunucusu; [AileEkrani.md](AileEkrani.md)'deki gibi bağlanmış öğrenci telefonu):
  - Bağlanıp konum iznini ver → bildirim çubuğunda "Eğitim Evi Aile — Konumun ve ekran süren velinle paylaşılıyor."
    görünmeli; `adb shell dumpsys activity services org.egitimevi.aile` servisi ön planda göstermeli.
  - Öykünücüde `adb emu geo fix <boylam> <enlem>` ile konum ver → Aile ekranında "Son konum" ve "Son gönderim" dolmalı;
    velinin sitedeki "Çocuğumun telefonu" sayfasında konum görünmeli. Aralık başına bir konum kabul edilir.
  - İnterneti kes (`adb shell svc wifi disable`, `adb shell svc data disable`) → konum ver → "Gönderilmeyi bekleyen konum"
    artmalı → interneti aç → en geç bir turda gönderilmeli.
  - Veli sitede aralığı değiştirsin ya da konumu kapatsın → yaklaşık yarım saat içinde telefona yansımalı.
  - Veli sitede telefonu kaldırsın → bir sonraki istekte servis durmalı, bildirim kalkmalı, Aile ekranı giriş formunu
    göstermeli.
  - Konum iznini telefonun ayarından geri al, `adb reboot` → uygulamanın açılışta kapanıp kapanmadığına bak (yukarıdaki
    ilk "Dikkat!" maddesi).

## Son durum

- `git log` (Android deposu): 3 commit, hepsi 2026-09-26.
  - `f70aeca commit 5` (tek uygulamaya geçiş): bildirime dokununca açılan ekran `AnaEkran` yerine `AileEkrani` oldu (çocuğun
    telefonu ekranı o commit'te `AileEkrani` adını aldı; `AnaEkran` artık uygulamanın asıl ekranı). Tek satırlık değişiklik.
  - `3875db7 commit 4` (Aile 1.0.2): uygulamanın en düşük Android sürümü zaten 8 (minSdk 26) olduğu için gereksiz sürüm
    denetimleri kalktı: `baslat` doğrudan `startForegroundService` (eskiden 8 öncesine `startService`), kanal kurulumu ve
    `Notification.Builder(this, KANAL)` koşulsuz.
  - `34b45f1 commit 1`: dosyanın ilk hâli (Eğitim Evi Aile, sürüm 1.0; `commit 4` sürümü 1.0.2 yaptı).
- Bilinen açıklar (kod değiştirilmedi): izinsiz başlatmada ön plan kuralı; açılışta "her zaman" izni koşulu; ekran
  kapalıyken turun aksaması; "daha doğrusu tutulur" yorumunun karşılıksızlığı; kuyruk işinin ana iş parçacığında olması;
  bağlantı koptuğunda nedenin silinmesi; sonradan açılan sağlayıcının geç dinlenmesi.
- Planlı işlerden bu dosyaya dokunması beklenenler: "Android yerel uygulama" (tanım çocuğun telefonu kısmının — bu servis,
  `Kuyruk`, `Kullanim`, `AileEkrani` — eski 1.0.x kurulumlarla uyumlu kalmasını istiyor; velinin "Çocuğumun telefonu"
  ekranı uygulamaya ayrıca gelecek; tanımın "uygulamanın kendini güncellemesi" eki gelince her güncellemeden sonra bu
  servis `BaslatmaAlici`'nın `MY_PACKAGE_REPLACED` yoluyla yeniden başlayacak — bunu tanım yazmıyor, bugünkü
  koddan çıkan sonuç); "KVKK ve onay metinleri TAM denetimi" (telefondan
  dışarı giden konum, pil, ağ türü ve uygulama süreleri gözden geçirilecek); "Çok dil" (bildirim başlığı, metni ve kanal
  adı sabit Türkçe).

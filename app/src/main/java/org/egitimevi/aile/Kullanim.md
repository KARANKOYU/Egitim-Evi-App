# app/src/main/java/org/egitimevi/aile/Kullanim.java

Ekran süresi: Android'in kullanım olaylarından her gün için hangi uygulamanın önde kaç dakika açık kaldığını hesaplar
(ana ekran, sistem arayüzü ve Eğitim Evi'nin kendisi sayılmaz) ve sunucuya gidecek gün listesini kurar.

## Bu dosya ne yapar?

Veli, Eğitim Evi Aile'yle bağlanmış çocuğunun telefonunda hangi uygulamada kaç dakika geçtiğini sitedeki "Çocuğumun
telefonu" sayfasında görür; isterse günlük toplam ya da uygulama başına sınır koyar ve sınır aşılınca kendisine bildirim
gelir (hiçbir uygulama kapatılmaz, kilitlenmez). Bu dakikaları telefonda hesaplayan dosya bu.

Android'de başka uygulamaların kullanımını okumak özel bir izin ister: **Kullanım erişimi** (`PACKAGE_USAGE_STATS`). Bu
izin normal izin penceresiyle verilemez; öğrenci telefonun Ayarlar > Kullanım erişimi sayfasından Eğitim Evi'ni açar
([AileEkrani.md](AileEkrani.md)'deki "Ekran süresi (kullanım erişimi)" satırının düğmesi oraya götürür). `izinVar` bu
iznin verilip verilmediğini söyler.

Hesap Android'in hazır günlük özetiyle değil, olaylarla yapılır: bir uygulamanın ekranı öne geldi (`ACTIVITY_RESUMED`) ile
arkaya gitti (`ACTIVITY_PAUSED` ya da `ACTIVITY_STOPPED`) arasındaki süreler toplanır. Gün, telefonun saat dilimine göre
gece yarısında başlar.

Sınıf `final`, örneği yok. [IzlemeServisi.md](IzlemeServisi.md) 15 dakikada bir `sonGunler(c, 2)` (dün ve bugün) çağırıp
sonucu gönderir.

## İçinde neler var?

### Dışa açık

- `izinVar(c)` — `AppOpsManager` ile `OPSTR_GET_USAGE_STATS` işlemine bu uygulama için bakar: Android 10+
  `unsafeCheckOpNoThrow`, öncesinde (eskimiş, `@SuppressWarnings("deprecation")`) `checkOpNoThrow`. Yalnız
  `MODE_ALLOWED` "izin var" sayılır.
- `sonGunler(c, gunSayisi)` → `JSONArray`, eskiden yeniye günler:

  ```
  [ { "gun": "2026-10-02", "uygulamalar": [ { "paket": "com.google.android.youtube", "ad": "YouTube", "dakika": 95 }, ... ] },
    { "gun": "2026-10-03", "uygulamalar": [ ... ] } ]
  ```

  - İzin yoksa boş dizi.
  - Her gün için pencere: başlangıç telefonun yerel saatiyle o günün 00:00'ı; bitiş başlangıç + 24 saat ya da şimdi
    (hangisi önceyse). Bugünün penceresi "şimdi"de biter.
  - Pencerenin süreleri `onPlandaKalma` ile hesaplanır; atlanacak paketler çıkarılır; süre dakikaya yuvarlanır
    (`Math.round`) ve 1 dakikanın altında kalan (yani 30 saniyeden kısa) uygulama yazılmaz.
  - `gun` biçimi `yyyy-MM-dd` (`Locale.ROOT`); `ad` uygulamanın telefonda görünen adı.
  - Uygulamalar belirli bir sırada değildir (`HashMap`); sıralamayı sunucu/ekran yapar.
  - `JSONException` fırlatabileceğini bildirir (pratikte `org.json` bu değerlerde fırlatmaz).
  - Not: yorumundaki `{ gunler: [...] }` sarmalayıcısını bu işlev değil, `IzlemeServisi.kullanimGonder` koyar.

### İç

- `onPlandaKalma(usm, bas, bit)` — `UsageStatsManager.queryEvents(bas, bit)` olaylarını sırayla gezer; paket başına bir
  "açılış anı" tutar. `ACTIVITY_RESUMED` → açılış yazılır (varsa üstüne). `ACTIVITY_PAUSED` ya da `ACTIVITY_STOPPED` →
  açılış varsa ve olay ondan sonraysa aradaki süre o paketin toplamına eklenir, açılış silinir. Pencere bittiğinde hâlâ
  açık görünen paketin süresi `bit`'e kadar sayılır. Dönüş: paket → milisaniye.
- `getOr(harita, anahtar)` — yoksa 0.
- `atlanacaklar(c, pm)` — sayılmayan paketler: bu uygulamanın kendisi, `com.android.systemui` (bildirim çubuğu, kilit
  ekranı), `android` (sistem) ve `ACTION_MAIN` + `CATEGORY_HOME` niyetine cevap veren her uygulama (ana ekran/başlatıcı).
- `ad(pm, paket)` — `getApplicationLabel`; uygulama bulunamazsa (`NameNotFoundException`) paket adının kendisi.

## Kimle konuşur?

- Çağırdıkları: Android `AppOpsManager`, `UsageStatsManager` (`queryEvents`) ve `UsageEvents.Event`, `PackageManager`
  (`queryIntentActivities`, `getApplicationInfo`, `getApplicationLabel`), `Process.myUid()`, `Calendar`,
  `SimpleDateFormat`.
- Onu çağıranlar (grep):
  - [IzlemeServisi.md](IzlemeServisi.md) — `izinVar` (göndermeden önce) ve `sonGunler(this, 2)`.
  - [AileEkrani.md](AileEkrani.md) — `izinVar` (izin satırı "✓ Ekran süresi (kullanım erişimi)" ya da "— kapalı").
- Sunucu (dolaylı; site deposunda `sunucu/bolumler/aile.md`): `POST /api/aile/cihaz/kullanim { gunler }`, `X-Aile-Cihaz`
  anahtarıyla. Sunucu ilk 8 günü alır; gün Türkiye saatine göre son 7 gün ile bugün arasında olmalı; paket adı
  `^[A-Za-z0-9._-]{1,200}$`; dakika 0–1440 tam sayı; günde en çok 300 uygulama; aynı günde tekrar eden paket atılır;
  uygulama adı denetim ve yön karakterlerinden arındırılıp 100 harfe kısaltılır (boşsa paket adı). Her (öğrenci, gün,
  paket) satırı her gönderimde ÜZERİNE yazılır — bu yüzden bugünün süresini 15 dakikada bir baştan göndermek doğru
  sonucu verir. Veli ekran süresini kapattıysa `{ kapali: true }`. Yazınca o günün süreleri velinin sınırlarıyla
  karşılaştırılır; aşılan sınır için veliye günde bir kez bildirim gider.
- Manifest ([AndroidManifest.xml](../../../../AndroidManifest.xml)): `PACKAGE_USAGE_STATS`
  (`tools:ignore="ProtectedPermissions"`), uygulama adlarını okuyabilmek için `<queries>` içinde `MAIN` + `LAUNCHER`
  niyeti.
- Aydınlatma metni (site deposunda `public/kvkk/kvkk.html`), "Eğitim Evi Aile: uygulama kullanım süreleri": her gün hangi
  uygulamanın önde kaç dakika açık kaldığı (uygulamanın adı ve paket adı); yalnız bağlı veliler görür; 7 gün sonra silinir.

## Nasıl çalışır (adım adım)?

```
IzlemeServisi turu (15 dakikada bir; internet ve kullanım erişimi varsa)
  Kullanim.sonGunler(c, 2)
    atla = { org.egitimevi.aile, com.android.systemui, android, ana ekran uygulamaları }
    g = 1  dün:   [dün 00:00, bugün 00:00)  ─► onPlandaKalma
    g = 0  bugün: [bugün 00:00, şimdi)      ─► onPlandaKalma
             YouTube   RESUMED 10:00 ── PAUSED 10:40         → +40 dk
             Instagram RESUMED 11:00 ── (hâlâ önde; şimdi 11:12) → +12 dk
    dakika = round(ms / 60 000); 1'den küçükse yazılmaz
  ─► { gunler: [dün, bugün] } ─► POST /api/aile/cihaz/kullanim
       sunucu: (öğrenci, gün, paket) satırını üzerine yazar ─► sınır aşıldıysa veliye bildirim
```

## Dikkat!

- **Aynı uygulamada ekrandan ekrana geçiş süreyi düşürebilir (Android 10+).** Sayaç paket başına TEK açılış tutuyor. Bir
  uygulamanın içinde A ekranından B ekranına geçerken Android'in yaşam döngüsünde önce A durur gibi olur (`PAUSED`), sonra
  B öne gelir (`RESUMED`), en son A tamamen durur (`STOPPED`). Üçüncü olay paketin sayacını B açıldıktan hemen sonra
  kapatır; B'de geçen süre, B kapanınca açılış bulunmadığı için hiç sayılmaz. Tek ekranlı uygulamalarda sorun yok; çok
  ekranlı uygulamalarda ekran süresi eksik çıkabilir. Kod okumasına ve Android yaşam döngüsüne göre; telefonda denenmedi.
  Düzeltme önerisi: açılışı paket + ekran sınıfıyla (`Event.getClassName`) tutmak ya da `ACTIVITY_STOPPED`'i yok saymak
  (`PAUSED` zaten ondan önce gelir). Android 8–9'da `ACTIVITY_STOPPED` olayı hiç yok; orada bu sorun da yok.
- **Gece yarısını geçen kullanım.** 23:50'de açılıp 00:30'da kapanan uygulamanın dünkü 10 dakikası dünün penceresinde
  ("hâlâ açık", gün sonuna kadar) sayılır; bugünkü 30 dakikası ise sayılmaz, çünkü bugünün penceresinde yalnız kapanış
  olayı var, açılış yok. Uygulama gece yarısından beri hiç kapanmadıysa bugünkü süresi kapanıp yeniden açılana kadar hiç
  görünmez.
- **Yalnız dün ve bugün gönderilir.** Sınıfın yorumu "son 7 günün her günü" diyor ama servis `sonGunler(c, 2)` çağırır.
  Telefon bir günden uzun internetsiz kalırsa (ya da servis çalışmadıysa) daha eski günlerin süreleri hiç gönderilmez;
  telefon ilk bağlandığında da veli yalnız dünü ve bugünü görür. Sunucu 8 güne kadar kabul ediyor; sayıyı artırmak
  sunucuda değişiklik gerektirmez (yalnız her gönderimin işi büyür).
- **Gün telefona göre, sunucunun süzgeci Türkiye'ye göre.** Telefon başka bir saat dilimindeyse günler kayar; Türkiye'den
  ileri bir saat diliminde gece yarısından sonra gönderilen "bugün" sunucuda henüz gelmemiş bir gün sayılıp atılır. Gün
  uzunluğu da sabit 24 saat (yaz saati uygulanan ülkelerde geçiş günü bir saat kayar; Türkiye'de yaz saati yok).
- **Ana ekranın ayıklanması Android 11+'da paket görünürlüğüne takılabilir.** `atlanacaklar` başlatıcıları
  `CATEGORY_HOME` sorgusuyla bulur; ama manifestteki `<queries>` yalnız `LAUNCHER` niyetini açıyor. Android 11 ve üstünde
  uygulama yalnız görebildiği paketleri sorgu sonucunda alır: uygulama çekmecesinde simgesi olmayan bir başlatıcı (çoğu
  telefonun kendi ana ekranı) bulunamayıp velinin listesine "uygulama" olarak girebilir. Aynı sebeple simgesi olmayan bir
  uygulamanın adı yerine paket adı görünür. Android belgelerine göre; telefonda denenmedi. Düzeltme önerisi:
  `<queries>`'e `MAIN` + `HOME` niyeti eklemek.
- **Sabit adları yeni, değerleri eski.** `ACTIVITY_RESUMED` ve `ACTIVITY_PAUSED` Android 10'da gelen adlar; değerleri (1, 2)
  eski `MOVE_TO_FOREGROUND` / `MOVE_TO_BACKGROUND` ile aynı ve derlemede koda gömülür, bu yüzden uygulamanın desteklediği
  Android 8–9'da da çalışır.
- **İçerik okunmaz.** Yalnız hangi uygulamanın önde kaldığı ve ne kadar; ekran görüntüsü, mesaj, tarama geçmişi yok. Bu,
  aydınlatma metniyle birebir; buraya yeni bir alan eklenirse metin ve sürümü aynı işte güncellenmeli (site deposundaki
  KVKK kuralı).
- `sonGunler` her çağrıda iki günün bütün olaylarını baştan okur; 15 dakikada bir, küçük veriyle sorun görünmüyor.

## Testleri

- Android deposunda otomatik test yok; derleme ve lint (`./gradlew --offline assembleDebug lintDebug`) ana oturumda.
- Sunucu tarafını site deposundaki `testler/test-aile.js` korur: kullanım gövdesinin süzülmesi (bozuk paket adı, 1440 üstü
  dakika ve eski gün atılır; geçerli 3 satır alınır), velinin özetinde bugünün sürelerinin büyükten küçüğe sıralanması ve
  8 günlük toplam çizelgesi, uygulama adının düz metin saklanması, toplam ve uygulama sınırı aşılınca veliye günde bir kez
  bildirim, sınırı geçmeyen uygulamaya bildirim gitmemesi.
- Elle (öykünücü ya da telefon, Aile'ye bağlı):
  1. Kullanım erişimini ver: telefonun Ayarlar > Kullanım erişimi sayfasından ya da
     `adb shell appops set org.egitimevi.aile GET_USAGE_STATS allow`. Aile ekranında satır "✓" olmalı.
  2. Birkaç uygulamayı birkaç dakika kullan. Servis ekran süresini 15 dakikada bir gönderir; servis yeni başladıysa ilk
     turda hemen gönderir.
  3. Velinin sitedeki "Çocuğumun telefonu" sayfasında süreleri telefonun kendi ekran süresi ekranıyla (Dijital Denge)
     karşılaştır; ana ekran ve Eğitim Evi listede olmamalı.
  4. Çok ekranlı bir uygulamada (ör. telefonun Ayarlar'ında alt sayfalar arasında gezinerek) yukarıdaki eksik sayma
     şüphesini dene.

## Son durum

- `git log` (Android deposu): 2 commit. Son değişiklik `3875db7 commit 4` (2026-09-26, Aile 1.0.2): `izinVar`'a yalnız
  `@SuppressWarnings("deprecation")` eklendi (Android 10 öncesindeki eskimiş `checkOpNoThrow` çağrısının uyarısı). Dosyanın
  ilk hâli `34b45f1 commit 1` (2026-09-26, Eğitim Evi Aile, sürüm 1.0).
- Bilinen açıklar (kod değiştirilmedi): aynı uygulamada ekran geçişinde eksik sayma şüphesi; gece yarısını geçen
  kullanımın bugünkü kısmının sayılmaması; yalnız iki günün gönderilmesi (yorum yedi diyor); günün telefonun saat dilimine
  göre olması; Android 11+'da başlatıcı sorgusunun `<queries>`'te olmaması.
- Planlı işlerden bu dosyaya dokunması beklenenler: "Android yerel uygulama" (tanım velinin "Çocuğumun telefonu (aile
  özet, harita, ekran süresi grafiği, sınırlar)" ekranını uygulamaya da getiriyor; çocuğun telefonu kısmı eski 1.0.x
  kurulumlarla uyumlu kalmalı, yani bu dosyanın gönderdiği biçim değişmemeli); "KVKK ve onay metinleri TAM denetimi"
  (uygulama adları ve süreler dışarı giden veri olarak gözden geçirilecek).

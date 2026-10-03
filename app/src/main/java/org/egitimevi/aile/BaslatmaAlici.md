# app/src/main/java/org/egitimevi/aile/BaslatmaAlici.java

Telefon yeniden açılınca ya da uygulama güncellenince Android'in çağırdığı yayın alıcısı: çocuğun telefonu servisini yeniden
başlatır, yarım kalmış servis seferini unutur ve bildirim yoklamasını yeniden kurar.

## Bu dosya ne yapar?

Telefon kapanıp açıldığında ya da uygulamanın yeni sürümü kurulduğunda uygulamanın çalışan servisleri durmuş olur. Kişi
uygulamayı açmadan bunların geri gelmesi için Android'in iki sistem yayını dinlenir: `BOOT_COMPLETED` (telefon açıldı) ve
`MY_PACKAGE_REPLACED` (bu uygulama güncellendi). Bu sınıf o yayınlarda sırayla üç şey yapar:

1. **Çocuğun telefonu servisi** — `IzlemeServisi.baslat(c)`. Telefon velisine bağlı değilse ([Ayarlar.md](Ayarlar.md)
   `bagli`) hiçbir şey olmaz; bağlıysa ön plan servisi başlar ve konum ile ekran süresi göndermeye devam eder (servis konum
   izni yoksa kendini durdurur).
2. **Yarım kalmış sefer unutulur** — `UygulamaAyar.seferYaz(c, "")`. Sınıf yorumu: "Yarıda kalmış sefer sürdürülmez
   (servisçi yeniden başlatır)." Telefon kapanınca ya da uygulama güncellenirken servisçinin sefer konumu (`SeferServisi.java`)
   durmuştur; açılışta kendiliğinden yeniden göndermeye başlamasın diye kayıtlı sefer silinir.
3. **Bildirim yoklaması** — `Bildirimler.zamanla(c)`. Telefonda uygulama anahtarı varsa 15 dakikalık düzenli iş (yoksa)
   kurulur, okulun servis saatindeysek bir dakika sonrasına hızlı yoklama da eklenir; anahtar yoksa işler iptal edilir
   ([Bildirimler.md](Bildirimler.md)).

## İçinde neler var?

- `onReceive(Context c, Intent i)` — tek işlev. Gelen `Intent`'in eylemi `Intent.ACTION_BOOT_COMPLETED`
  (`android.intent.action.BOOT_COMPLETED`) ya da `Intent.ACTION_MY_PACKAGE_REPLACED`
  (`android.intent.action.MY_PACKAGE_REPLACED`) ise yukarıdaki üç adımı bu sırayla yapar; başka bir eylemde hiçbir şey
  yapmaz.
- Manifest kaydı (`AndroidManifest.xml`):

  ```
  <receiver android:name=".BaslatmaAlici" android:exported="true">
      <intent-filter>
          <action android:name="android.intent.action.BOOT_COMPLETED" />
          <action android:name="android.intent.action.MY_PACKAGE_REPLACED" />
      </intent-filter>
  </receiver>
  ```

  ve izin `android.permission.RECEIVE_BOOT_COMPLETED`.

## Kimle konuşur?

- Çağırdıkları (aynı paket):
  - `IzlemeServisi.java` — `baslat(c)`: `Ayarlar.bagli` ise `startForegroundService`; servis `onStartCommand`'da konum izni
    yoksa durur ([AileEkrani.md](AileEkrani.md) arka planı).
  - `UygulamaAyar.java` — `seferYaz(c, "")` ("uygulama" ayar dosyasındaki `sefer`).
  - [Bildirimler.md](Bildirimler.md) — `zamanla(c)`.
- Onu çağıran: yalnız Android (kodda hiçbir yerden çağrılmıyor; grep). Manifestteki `intent-filter` sayesinde sistem yayınları
  gelir.
- Sunucuyla doğrudan konuşmaz. Başlattığı işler konuşur: `IzlemeServisi` Aile uçlarıyla (`X-Aile-Cihaz`; site deposunda
  `sunucu/bolumler/aile.md`), bildirim işi `GET /api/cihaz/bildirimler` ile (`X-Cihaz`; `sunucu/bolumler/cihaz.md`).
- Android: `BroadcastReceiver`, `Intent` eylemleri; izin `RECEIVE_BOOT_COMPLETED` (bildirimin 15 dakikalık işinin
  `setPersisted(true)` ile telefon açılışından sonra da kalması için de gereken izin).

## Nasıl çalışır (adım adım)?

```
telefon açıldı (ilk kilit açılışından sonra)  ya da  uygulama güncellendi
   Android ─► BaslatmaAlici.onReceive(c, BOOT_COMPLETED | MY_PACKAGE_REPLACED)
      1. IzlemeServisi.baslat(c)
            Aile bağlı değil  → hiçbir şey
            bağlı             → startForegroundService → "Eğitim Evi Aile" ön plan bildirimi, dakikalık tur
                                (konum izni yoksa servis kendini durdurur)
      2. UygulamaAyar.seferYaz(c, "")            → açık sefer unutulur
      3. Bildirimler.zamanla(c)
            uygulama anahtarı yok → işler iptal
            var                   → iş 101 (15 dk, kalıcı) yoksa kurulur
                                    servis saatindeysek → iş 102 (1–3 dk sonra)
```

Android 7'den beri ekran kilidi olan telefonda `BOOT_COMPLETED` kişi kilidi ilk kez açtıktan sonra gelir; uygulama
"doğrudan açılış" (Direct Boot) desteklemediği için daha önce çalışmaz (Android'in genel davranışı).

## Dikkat!

- **`exported="true"` gerekli ve güvenli.** Sistem yayınları başka bir süreçten geldiği için alıcı dışa açık olmalı. İki
  eylem de Android'in korumalı yayınlarıdır: başka bir uygulama bunları gönderemez. Bu alıcıya başka eylemle açık bir
  `Intent` gelirse kod hiçbir şey yapmaz.
- **Sefer kuralı bugün boşa çalışıyor.** `SeferServisi.baslat`'ın bugün çağıranı yok (WebView dönemindeki site köprüsü
  `e96c5f2 commit 6`'da kalktı, servisçi ekranı henüz yazılmadı; [AnaEkran.md](AnaEkran.md)); kayıtlı sefer zaten hep boş.
  Servisçinin yoklama ekranı yazılınca kural anlam kazanır: telefon kapanıp açılan servisçi seferi ekrandan yeniden başlatmalı.
- **Açılışta konum servisi ve yeni Android sürümleri.** Android, arka plandan başlatılan konum türündeki ön plan servislerine
  sürüm sürüm yeni koşullar getirdi (ör. 14 ve üstünde konum izninin servis başlarken bulunması; "her zaman" konum izni
  olmadan arka plandan başlayan servisin konuma erişememesi). `IzlemeServisi` yalnız "uygulama kullanılırken" konum
  iznine bakıyor; öğrenci "Her zaman izin ver"i seçmediyse açılışta servisin konum alamaması ya da başlarken hata vermesi
  olası. Android belgelerine dayanan bir uyarıdır; telefonda doğrulanmadı ([AileEkrani.md](AileEkrani.md) "her zaman"
  iznini ayrı bir satırla istiyor).
- **Zorla durdurulan ya da hiç açılmamış uygulama yayın almaz.** Android'in genel kuralı: yeni kurulan ya da Ayarlar'dan
  "Durmaya zorla" denen uygulama, kişi onu bir kez açana kadar bu yayınları almaz ve zorla durdurma zamanlanmış işleri de
  siler. O durumda bildirim yoklaması ancak uygulama açılınca ([AnaEkran.md](AnaEkran.md) `uygulamayiKur` →
  `Bildirimler.zamanla`) geri gelir.
- **Düzenli iş zaten kalıcı.** 15 dakikalık iş `setPersisted(true)` ile kurulduğu için Android onu telefon açılışında kendisi de
  geri kurar; bu alıcı yine de çağırır, çünkü servis saatindeki hızlı iş kalıcı değildir ve anahtar arada silinmişse işlerin
  iptal edilmesi gerekir.
- `onReceive` ana iş parçacığında çalışır; burada yalnız kısa işler var (servis başlatma, bir ayar yazma, iş kurma). Buraya
  ağ isteği koyma.

## Testleri

- Android deposunda otomatik test yok; derleme ve lint (`./gradlew --offline assembleDebug lintDebug`) ana oturumda.
- Elle (öykünücü; deneme paketi):
  - Girişten sonra `adb reboot`; telefon açılıp kilit açılınca `adb shell dumpsys jobscheduler` çıktısında
    `org.egitimevi.aile` için 101 numaralı iş görünmeli; okulun servis saatindeysek 102 de.
  - Öğrenci hesabıyla telefonu Aile'ye bağla ("Her zaman" konum izniyle), `adb reboot` → bildirim çubuğunda "Eğitim Evi Aile
    — Konumun ve ekran süren velinle paylaşılıyor." yeniden görünmeli; velinin sitedeki sayfasında yeni konumlar gelmeli.
  - Uygulamanın yeni bir derlemesini `adb install -r` ile kur (bu `MY_PACKAGE_REPLACED` yayınını tetikler) → aynı sonuçlar.
- Sunucu tarafında bu dosyanın başlattığı işlerin sözleşmesini `testler/test-aile.js` ve `testler/test-servis-yoklama.js`
  korur (site deposu).

## Son durum

- `git log`: 2 commit (Android deposu). Son değişiklik `f70aeca commit 5` (2026-09-26, tek uygulamaya geçiş): uygulama anahtarı
  ve bildirim yoklaması geldiği için `UygulamaAyar.seferYaz(c, "")` ve `Bildirimler.zamanla(c)` eklendi, yorum "Telefon açılınca
  ya da uygulama güncellenince servis yeniden başlar." cümlesinden bugünkü üç maddelik hâline genişledi.
- Dosyanın ilk hâli `34b45f1 commit 1` (2026-09-26, Eğitim Evi Aile 1.0.x): yalnız `IzlemeServisi.baslat(c)`.
- Bilinen açıklar (kod değiştirilmedi): sefer kuralının bugün etkisiz olması; açılışta konum servisinin yeni Android
  sürümlerindeki davranışının telefonda denenmemiş olması.
- Planlı işlerden bu dosyaya dokunması beklenenler: "Android yerel uygulama" — servisçinin yoklama ekranı ve `SeferServisi` ile
  arka plan konumu (sefer kuralı o zaman çalışır); "uygulamanın kendini güncellemesi" eki — kurulan her yeni sürüm
  `MY_PACKAGE_REPLACED` ile bu alıcıyı çalıştırır, güncellemeden sonra servislerin ve yoklamanın geri gelmesi buna dayanır.

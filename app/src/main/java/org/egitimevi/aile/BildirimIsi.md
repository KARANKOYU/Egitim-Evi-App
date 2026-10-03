# app/src/main/java/org/egitimevi/aile/BildirimIsi.java

Android'in zamanladığı bildirim yoklama işini çalıştıran `JobService`: ayrı bir iş parçacığında `Bildirimler.yokla`'yı
çağırır, bitince işi kapatır ve servis saatindeysek bir dakika sonrasına yeni bir yoklama kurar.

## Bu dosya ne yapar?

Uygulama Firebase kullanmadığı için telefon bildirimlerini sunucuya kendisi sorar ([Bildirimler.md](Bildirimler.md)).
"Ne zaman sorulacağı"nı Android'in iş zamanlayıcısı (`JobScheduler`) belirler: uygulama kapalıyken, telefon kilitliyken de
uygun anda işi çalıştırır. Zamanlayıcı doğrudan bir işlevi değil, manifestte kayıtlı bir `JobService`'i çalıştırır; bu sınıf o
servistir. İçinde yoklamanın kendisi yoktur, yalnız iki şey yapar:

1. İşi ana iş parçacığından alıp `"bildirim-yoklama"` adlı yeni bir iş parçacığında `Bildirimler.yokla`'yı çağırır (ağ isteği
   ana iş parçacığında yapılamaz).
2. Yoklama bitince Android'e "iş bitti" der (`jobFinished`) ve `Bildirimler.zamanla(c, true)` ile — okulun servis saatindeysek —
   bir dakika sonrasına yeni bir tek seferlik iş kurar. Böylece servis saatlerinde yoklama kendi kendini besleyen bir zincir olur.

`Bildirimler.zamanla` iki iş kurar ve ikisi de bu servisi çalıştırır: 101 numaralı düzenli iş (15 dakikada bir) ve 102
numaralı hızlı iş (servis saatinde, 1–3 dakika sonra). Bu sınıf hangisi olduğuna bakmaz; ikisinde de aynı şeyi yapar.

## İçinde neler var?

- `onStartJob(JobParameters p)` — Android işi başlatınca ana iş parçacığında çağrılır. Yeni bir iş parçacığı açar:
  `try { Bildirimler.yokla(getApplicationContext()); } finally { jobFinished(p, false); Bildirimler.zamanla(getApplicationContext(), true); }`.
  Hemen `true` döner: "iş sürüyor, bitince ben haber vereceğim".
  - `jobFinished(p, false)` — yeniden deneme istenmez (düzenli iş zaten kendi sırasıyla yeniden çalışır).
  - `zamanla(..., true)` — "zincir" kipi: bekleyen bir hızlı iş olsa bile yenisi kurulur (aynı numaralı iş eskisinin yerine
    geçer). Servis saati dışındaysak hızlı iş kurulmaz, zincir kendiliğinden biter.
- `onStopJob(JobParameters p)` — Android işi yarıda keserse (ör. ağ gitti) çağrılır; `true` döner: "bu işi yeniden zamanla".
  Çalışan iş parçacığını durdurmaz.
- Manifest kaydı (`AndroidManifest.xml`): `<service android:name=".BildirimIsi" android:exported="false"
  android:permission="android.permission.BIND_JOB_SERVICE" />` — başka uygulamalar başlatamaz; yalnız sistemin iş
  zamanlayıcısı bağlanabilir.

## Kimle konuşur?

- Çağırdıkları: [Bildirimler.md](Bildirimler.md) — `yokla(c)` ve `zamanla(c, true)`. Android: `JobService`,
  `JobParameters`, `Thread`.
- Onu çağıran: Android'in `JobScheduler`'ı. İşleri `Bildirimler.zamanla` kurar (`new ComponentName(c, BildirimIsi.class)`);
  kodda başka hiçbir yer bu sınıfı anmıyor (grep).
- Sunucu: dolaylı olarak `GET /api/cihaz/bildirimler[?son=<imleç>]` (`X-Cihaz` başlığıyla; site deposunda
  `sunucu/bolumler/cihaz.md`) — isteği `Bildirimler.yokla` [Api.md](Api.md)'nin `uygulama(...)` işleviyle atar.

## Nasıl çalışır (adım adım)?

```
JobScheduler: iş 101 (her ~15 dk, ağ varken)  ya da  iş 102 (servis saatinde, en erken 1 en geç 3 dk sonra)
   └─► BildirimIsi.onStartJob(p)              [ana iş parçacığı] → return true
          new Thread("bildirim-yoklama"):
             Bildirimler.yokla(uygulama bağlamı)
                 anahtar yoksa → hiçbir şey
                 GET /api/cihaz/bildirimler?son=<imleç>  → yeni bildirimler çubuğa, imleç ve servis saatleri saklanır
             finally:
                 jobFinished(p, false)
                 Bildirimler.zamanla(…, zincir = true)
                     servis saatinde → iş 102 yeniden: +1 dk … +3 dk
                     değilse        → yalnız 101 kalır (zaten kuruluysa dokunulmaz)
```

Servis saatlerinde bir sabah şöyle görünür (örnek: okulun sabah aralığı 07:00–09:20, hızlı yoklama penceresi 06:50–10:20;
[Bildirimler.md](Bildirimler.md)): 06:50'den sonraki ilk düzenli iş zinciri başlatır, sonra her yoklama bir sonrakini 1–3
dakika sonraya kurar; 10:20'den sonra çalışan yoklama yenisini kurmaz, zincir kendiliğinden durur, yalnız 15 dakikalık iş
kalır.

## Dikkat!

- **Zincir ancak bir iş çalışınca başlar.** Hızlı iş yalnız `zamanla` çağrıldığında ve o an servis saatindeysek kurulur. Servis
  saati başladığında uygulama açılmazsa zinciri bir sonraki düzenli iş başlatır (en geç ~15 dakika); `Bildirimler`'deki
  10 dakikalık ön pay bunu kısmen karşılar.
- **`onStopJob` iş parçacığını durdurmaz.** Android işi keserse iş parçacığı sürer, sonunda `jobFinished` boşa çağrılır ve
  zincir yine kurulur; `true` dönüldüğü için Android de işi ayrıca yeniden zamanlar. Nadiren aynı anda iki yoklama olabilir;
  ikisi de aynı imleci kullanır, aynı bildirim aynı numarayla gösterildiği için bildirim çubuğunda çift görünmez (kod
  okumasına göre).
- **Yakalanmayan hata uygulamayı kapatır.** `Bildirimler.yokla` yalnız ağ ve sunucu hatalarını (`IOException`, `Api.Hata`)
  yakalar. Başka bir hata (`RuntimeException`) çıkarsa `finally` yine çalışır (iş kapanır, zincir kurulur) ama hata iş
  parçacığından dışarı çıkar ve Android'de yakalanmamış hata süreci kapatır. Bugün bilinen bir kaynağı yok; yeni kod eklerken
  akılda tut.
- **Uygulama bağlamı kullanılır.** `getApplicationContext()`: servis nesnesi yok edilse de bağlam geçerli kalır.
- **Ağ şartı zamanlayıcıda.** İşler `NETWORK_TYPE_ANY` ile kurulduğu için düzenli iş ağ yokken başlamaz; bu sınıf ağ
  denetimi yapmaz. Hızlı işte ise son süre de var (`setOverrideDeadline(3 dk)`): Android belgelerine göre son süre dolunca iş
  ağ şartı sağlanmasa da çalışır. O zaman `yokla` ağ hatasını yok sayar, `finally` zinciri yine kurar; servis saatinde ağ
  yokken de yaklaşık 3 dakikada bir boş deneme olur (telefonda denenmedi).
- **Düzenli iş de zinciri yeniler.** 101 servis saatinde çalışınca `zamanla(…, true)` bekleyen 102'yi yenisiyle değiştirir
  (aynı numara eskisinin yerine geçer); o hızlı yoklama 1–3 dakika daha kayabilir. Zararsız, yalnız sıklığı biraz düşürür.
- **Pil kuralları.** Android'in pil tasarrufu (Doze, uygulama bekleme grupları) işleri geciktirebilir; "dakikada bir" ve
  "15 dakikada bir" en iyi durumdur (Android'in genel davranışı; telefonda ölçülmedi).

## Testleri

- Android deposunda otomatik test yok; derleme ve lint (`./gradlew --offline assembleDebug lintDebug`) ana oturumda.
- Elle (öykünücü):
  - Girişten sonra `adb shell dumpsys jobscheduler` → `org.egitimevi.aile` için 101 numaralı iş (ve servis saatindeysek 102).
  - İşi beklemeden çalıştırmak için Android'in iş zamanlayıcısı komutu: `adb shell cmd jobscheduler run -f org.egitimevi.aile 101`
    (Android'in kendi aracı; bu depoda denenmedi). Girişten sonra komutu bir kez çalıştır: ilk yoklama eski bildirimleri
    getirmez, yalnız imleci alır. Sonra uygulama arkadayken siteden yeni bir bildirim üret (ör. öğretmenle ödev ver) ve komutu
    yeniden çalıştır → bildirim çubuğunda "Eğitim Evi" bildirimi.
- Sunucu tarafını site deposundaki `testler/test-servis-yoklama.js` "9) CİHAZ ANAHTARI" bölümü korur (ayrıntı
  [Bildirimler.md](Bildirimler.md)).

## Son durum

- `git log`: 1 commit. Dosya `f70aeca commit 5` (2026-09-26, tek uygulamaya geçiş) ile geldi: uygulama anahtarıyla bildirim
  yoklaması ve servis saatlerinde bir dakikalık zincir o commit'te yazıldı; o günden beri değişmedi.
- Bilinen açıklar (kod değiştirilmedi): `onStopJob`'un iş parçacığını durdurmaması; servis saatinin başında zincirin bir
  sonraki düzenli işi beklemesi.
- Planlı işler: "Android yerel uygulama" bildirim bağlantılarının doğru ekranı açmasını istiyor (bu dosyayı değil
  `Bildirimler`'i ve `AnaEkran`'ı etkiler). "Optimizasyon + saklama süreleri" tanımındaki mantık denetimi 11 zamanı önemli
  bildirimlerin (servis yaklaştı, dersine gelmedi) telefona ANINDA gitmesini, yoklamanın yalnız yedek olmasını öneriyor; uygulama
  Firebase kullanmadığı için Android tarafında nasıl yapılacağı tanımda yazmıyor.

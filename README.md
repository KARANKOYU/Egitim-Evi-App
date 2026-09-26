# Eğitim Evi telefon uygulaması

[Eğitim Evi](https://github.com/KARANKOYU/Egitim-Evi) okul portalının Android uygulaması.

## Ne işe yarar

Çocuğun telefonunda çalışır. Velinin seçtiği sıklıkla telefonun **konumunu** ve **hangi
uygulamanın ne kadar kullanıldığını** Eğitim Evi sunucusuna gönderir. Veli bunları sitede
**Çocuğumun telefonu** sayfasında görür.

- Konum Wi-Fi'de ve mobil veride ayrı sıklıkla alınır (1–60 dakika). İnternet yokken alınan
  konumlar telefonda birikir, bağlanılan ilk anda gönderilir.
- Ekran süresi uygulama uygulama, gün gün sayılır. Veli ortak ya da uygulama başına günlük
  sınır koyar; sınır geçilince veliye bildirim gider. Uygulama hiçbir uygulamayı kapatmaz.
- Veriyi yalnızca öğrenciye bağlı veliler görür; okul görmez. Sunucu 7 günden eskisini siler.
- Bağlıyken bildirim çubuğunda her zaman "Konumun ve ekran süren velinle paylaşılıyor" yazar.

## Nasıl çalışır

Telefon öğrenci hesabıyla bir kez bağlanır; öğrenci paylaşımı kendisi onaylar. Sunucu
telefona yalnızca konum ve süre göndermeye yarayan bir **cihaz anahtarı** verir; öğrencinin
oturumu telefonda kalmaz. Anahtar hesaba giriş vermez, sunucuda yalnızca özeti tutulur.
Veli ya da öğrenci bağlantıyı kaldırınca anahtar geçersiz olur ve uygulama durur.

| Dosya | Görevi |
|---|---|
| `AnaEkran.java` | Bağlanma, açık onay, izinler ve durum (tek ekran, kodla kurulan arayüz) |
| `IzlemeServisi.java` | Ön plan servisi: ağ türüne göre konum aralığı, gönderim, ayar tazeleme |
| `Kullanim.java` | Android kullanım istatistiklerinden günlük uygulama süreleri |
| `Kuyruk.java` | Gönderilmeyi bekleyen konumlar (en fazla 5000; 7 günden eskisi atılır) |
| `Api.java` | Sunucuyla JSON; internette yalnızca https, http yalnızca yerel ağdaki deneme sunucusu |
| `BaslatmaAlici.java` | Telefon açılınca servisi yeniden başlatır |

İstenen izinler: konum (arka planda da), kullanım erişimi, bildirim, pil kısıtlamasından muafiyet.
Dış kütüphane yoktur; yalnızca Android'in kendi arayüzleri kullanılır. En düşük Android 8.0.
Sunucu tarafı Eğitim Evi deposunda `sunucu/bolumler/aile.js`.

## Derleme

Android SDK (platform 36) ve JDK 17 ya da üstü gerekir.

```
./gradlew assembleDebug      # deneme paketi: app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease    # imzalı paket:  app/build/outputs/apk/release/app-release.apk
./gradlew bundleRelease      # Play Store:    app/build/outputs/bundle/release/app-release.aab
```

Yayın imzası deponun dışındaki `../Egitim-Evi-App-imza/imza.properties` dosyasından okunur
(`storeFile`, `storePassword`, `keyAlias`, `keyPassword`). Dosya yoksa yayın paketi imzasız çıkar.

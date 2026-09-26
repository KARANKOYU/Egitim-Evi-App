# Eğitim Evi Aile

[Eğitim Evi](https://github.com/KARANKOYU/Egitim-Evi) okul portalının isteğe bağlı Android uygulaması.
Çocuğun telefonuna kurulur; velinin seçtiği aralıkla telefonun **konumunu** ve **hangi uygulamanın
ne kadar kullanıldığını** Eğitim Evi'ne gönderir. Veli bunları sitede **Çocuğumun telefonu**
sayfasında görür.

- Uygulama **hiçbir uygulamayı kapatmaz ya da kilitlemez.** Veli günlük toplam (ortak) ya da
  uygulama başına sınır koyar; sınır geçilince veliye bildirim gelir.
- Yalnızca öğrenciye bağlı veliler görür. **Okul (müdür, öğretmen) görmez.**
- Konumlar ve süreler **7 gün sonra silinir.**
- Telefon bildirim çubuğunda her zaman "Konumun ve ekran süren velinle paylaşılıyor" yazar.

## Kurulum

1. [Releases](https://github.com/KARANKOYU/Egitim-Evi-App/releases) sayfasından APK'yı çocuğun
   telefonuna indir ve kur (bilinmeyen kaynaklardan kuruluma izin vermen gerekebilir).
2. Uygulamada okulun adresini ve **çocuğun öğrenci hesabını** yaz. Paylaşımı çocuk kendisi onaylar.
3. İzinleri sırayla ver:
   - **Konum → Her zaman izin ver** (uygulama kapalıyken de konum alınsın)
   - **Kullanım erişimi** (ekran süresi)
   - **Bildirimler**
   - **Arka planda çalışma** (pil kısıtlaması olmasın; yoksa telefon uygulamayı durdurabilir)
4. Veli sitede **Çocuğumun telefonu** sayfasından gönderme sıklığını seçer: Wi-Fi'de ve mobil
   veride ayrı ayrı 1, 5, 10, 15, 30 ya da 60 dakikada bir.

İnternet yokken alınan konumlar telefonda bekler, bağlandığı ilk anda gönderilir.

## Nasıl çalışır

| Dosya | Görevi |
|---|---|
| `AnaEkran.java` | Giriş, açık onay, izinler ve durum (tek ekran, kodla kurulan arayüz) |
| `IzlemeServisi.java` | Ön plan servisi: konum isteği (Wi-Fi/mobil aralığı), gönderim, ayar tazeleme |
| `Kullanim.java` | Android kullanım istatistiklerinden günlük uygulama süreleri |
| `Kuyruk.java` | Gönderilmeyi bekleyen konumlar (en fazla 5000, 7 günden eskisi atılır) |
| `Api.java` | Sunucuyla JSON; internette yalnızca https, http yalnızca yerel ağdaki deneme sunucusu |
| `BaslatmaAlici.java` | Telefon açılınca servisi yeniden başlatır |

Giriş yapılınca sunucu telefona yalnızca konum ve süre göndermeye yarayan bir **cihaz anahtarı**
verir; öğrencinin oturumu telefonda kalmaz. Anahtar hesaba giriş vermez, sunucuda yalnızca özeti
tutulur. Sunucu uçları Eğitim Evi deposunda `sunucu/bolumler/aile.js`.

Dış kütüphane yoktur; yalnızca Android'in kendi arayüzleri kullanılır. En düşük Android 8.0.
iPhone sürümü yok: Apple öteki uygulamaların kullanım süresini okumaya yalnızca kendi özel izniyle
olanak veriyor.

## Derleme

Android SDK (platform 36) ve JDK 17+ gerekir.

```
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.

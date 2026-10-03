# Eğitim Evi telefon uygulaması

[Eğitim Evi](https://github.com/KARANKOYU/Egitim-Evi) okul portalının Android uygulaması.

## Ne işe yarar

Uygulamanın içinde Eğitim Evi sitesi açılır: müdür, öğretmen, veli, öğrenci ve servisçi aynı
sayfaları kullanır, her şeyi sitedeki hesabıyla yapar. Telefona özgü işler uygulamada kalır:

- **Bildirimler:** ödev, mesaj, servis ve okul bildirimleri bildirim çubuğuna düşer. Uygulama
  15 dakikada bir, okulun servis saatlerinde dakikada bir yeni bildirim olup olmadığına bakar.
- **Servis seferi:** servisçi seferi başlatınca aracın konumu, ekran kapalıyken de birkaç
  saniyede bir velilere gönderilir. Sefer bitince konum gönderimi durur.
- **Çocuğun telefonu:** öğrenci isterse telefonunun konumunu ve hangi uygulamayı ne kadar
  kullandığını velisiyle paylaşır (Ayarlar → Bu telefonu velimle paylaş). Veli bunları sitede
  **Çocuğumun telefonu** sayfasında görür; okul görmez; veriler 7 gün sonra silinir. Uygulama
  hiçbir uygulamayı kapatmaz ya da kilitlemez.
- Dosya seçme ve indirme, haritada "Evimi işaretle" için konum izni.

## Nasıl çalışır

Oturum sitenin kendisinde kalır (girişi site yapar). Girişten sonra site, uygulamaya yalnızca
bildirim yoklamaya ve sefer konumu göndermeye yarayan bir **uygulama anahtarı** verir; anahtar
hesaba giriş vermez, sunucuda yalnızca özeti tutulur, çıkış yapınca geçersiz olur. Çocuğun
telefonu için ayrı bir anahtar vardır; o da yalnızca konum ve kullanım süresi gönderir.

Uygulama yalnızca kendi sunucusunun sayfalarını içeride açar; harita, GitHub, telefon ve e-posta
bağlantıları telefonun kendi uygulamalarında açılır. Site ile uygulama arasındaki köprü
(`window.EgitimEviUygulama`) yalnızca kendi sunucusunun sayfasında çalışır.

| Dosya | Görevi |
|---|---|
| `AnaEkran.java` | Site (WebView): yalnız kendi sunucusu içeride, dosya seçme ve indirme, konum izni, geri tuşu |
| `Kopru.java` | Siteyle köprü: uygulama anahtarı, sefer başla/bitir, çocuğun telefonu ekranı, dosya kaydetme |
| `Bildirimler.java`, `BildirimIsi.java` | Bildirim yoklama (15 dk; servis saatlerinde 1 dk) ve bildirim çubuğu |
| `SeferServisi.java` | Servisçinin seferi sürerken konum gönderen ön plan servisi |
| `AileEkrani.java` | Çocuğun telefonu: bağlanma, açık onay, izinler ve durum |
| `IzlemeServisi.java` | Çocuğun telefonu: ağ türüne göre konum aralığı, kullanım süresi, gönderim |
| `Kullanim.java` | Android kullanım istatistiklerinden günlük uygulama süreleri |
| `Kuyruk.java` | Gönderilmeyi bekleyen konumlar (en fazla 5000; 7 günden eskisi atılır) |
| `Api.java` | Sunucuyla JSON; yalnızca https (http yalnızca deneme paketinde, yerel ağdaki sunucuya) |
| `Ayarlar.java`, `UygulamaAyar.java` | Telefonda kalan ayarlar ve anahtarlar |
| `BaslatmaAlici.java` | Telefon açılınca çocuğun telefonu servisini ve bildirim yoklamasını yeniden kurar |

İstenen izinler: bildirim; konum (servisçide sefer için, "Evimi işaretle" için; çocuğun
telefonunda arka planda da); çocuğun telefonunda kullanım erişimi ve pil ayarında
"Kısıtlamasız". Uygulamanın verisi buluta yedeklenmez, yeni telefona taşınmaz.
Dış kütüphane yoktur; yalnızca Android'in kendi arayüzleri kullanılır. En düşük Android 8.0.

## Derleme

Android SDK (platform 36) ve JDK 17 ya da üstü gerekir.

```
./gradlew assembleDebug      # deneme paketi: app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease    # imzalı paket:  app/build/outputs/apk/release/app-release.apk
./gradlew bundleRelease      # Play Store:    app/build/outputs/bundle/release/app-release.aab
```

Deneme paketi ilk açılışta sunucu adresini sorar (öykünücüde `http://10.0.2.2:3200`); yayın
paketi `https://egitimevi.org`'a bağlanır. Yayın imzası deponun dışındaki
`../Egitim-Evi-App-imza/imza.properties` dosyasından okunur (`storeFile`, `storePassword`,
`keyAlias`, `keyPassword`). Dosya yoksa yayın paketi imzasız çıkar.

## Telif ve kullanım

© 2026 Eğitim Evi yapımcıları ([katkıda bulunanlar](https://github.com/KARANKOYU/Egitim-Evi/blob/main/CONTRIBUTING.md)). **Tüm hakları saklıdır.**

Bu depodaki kod, tasarım ve belgeler yalnız incelenebilsin diye herkese açıktır; bu bir açık kaynak lisansı DEĞİLDİR. Yazılı izin
olmadan kopyalanamaz, değiştirilemez, dağıtılamaz, başka bir sunucuda çalıştırılamaz ve bu kodla başka bir site ya da uygulama
yayınlanamaz. "Eğitim Evi" adı ve logosu da izinsiz kullanılamaz. GitHub'ın kuralları gereği herkese açık bir depo GitHub içinde
görüntülenebilir ve çatallanabilir (fork); bu, kodu kullanma izni vermez. İzin için proje sahibine GitHub'dan yaz
([@KARANKOYU](https://github.com/KARANKOYU)).

Copyright © 2026 the Eğitim Evi authors. All rights reserved. This repository is public for viewing only and is not open source:
no permission is granted to copy, modify, distribute, host or deploy this code, in whole or in part, without written permission.

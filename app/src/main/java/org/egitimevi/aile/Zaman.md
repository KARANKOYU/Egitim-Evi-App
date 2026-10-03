# app/src/main/java/org/egitimevi/aile/Zaman.java

Tarih ve saatin Türkiye saatiyle, Türkçe yazımı: bildirim zamanı ("az önce", "5 dk önce", "14:32", "Dün 09:10", "12 Eylül",
"12 Eylül 2025"), liste gün başlığı ("Bugün", "Dün", "12 Eylül, Cuma"), gün adlı tarih, bugünün tarihi ve saate göre selam.

## Bu dosya ne yapar?

Sunucu zamanları hep ISO metni olarak, UTC'de verir (`"2026-09-26T17:23:37.000Z"`; `timestamptz` sütunları
`toISOString()` ile çevrilir, `sunucu/veri/baglanti.md`, site deposu). Ekranda ise kişinin okuyacağı biçim gerekir: yakın
zamanlar göreli, uzaklar tarihle, ay ve gün adları Türkçe. Bu dosya o çeviriyi yapar.

Hesapların hepsi **Türkiye saatiyle** (`Europe/Istanbul`) yapılır, telefonun saat dilimiyle değil: okul Türkiye'de,
bildirimin "Dün" mü "Bugün" mü olduğu okulun gününe göre söylenir. Ay ve gün adları `tr-TR` diliyle yazılır.

Bugün kullananlar: ana sayfanın tarihi ve selamı ([AnaSayfa.md](AnaSayfa.md)) ile Bildirimler listesinin gün başlıkları ve
satır zamanları ([BildirimlerSayfasi.md](BildirimlerSayfasi.md)).

Sınıf bir araç sınıfıdır: `final`, kurucusu gizli, her şey `static`. Yalnız Java'nın `java.time` paketi (Android 8.0 / API
26'dan beri telefonda var; uygulamanın en düşük sürümü de 26).

## İçinde neler var?

### Sabitler

- `TURKIYE` (dışa açık) — `ZoneId.of("Europe/Istanbul")`.
- `TR` (iç) — `Locale.forLanguageTag("tr-TR")`.
- Biçimler (iç, `DateTimeFormatter`, Türkçe): `SAAT` "HH:mm" → "14:32"; `GUN_AY` "d MMMM" → "12 Eylül"; `GUN_AY_YIL`
  "d MMMM yyyy" → "12 Eylül 2025"; `GUN_AY_HAFTA` "d MMMM, EEEE" → "12 Eylül, Cuma".

### İşlevler

- `oku(iso)` — ISO anı (`Instant.parse`) Türkiye saatinde bir `ZonedDateTime`'a çevirir; `null`, boş ya da okunamayan metinde
  `null`. Öteki işlevlerin ortak girişi.
- `goreli(iso)` — bildirim ve mesaj zamanı (şimdiki ana göre, Türkiye saatiyle):

  | Durum | Sonuç |
  |---|---|
  | okunamadı | `""` |
  | 1 dakikadan az önce (gelecekteki zaman da) | "az önce" |
  | 60 dakikadan az önce | "5 dk önce" |
  | aynı takvim günü | "14:32" |
  | bir önceki takvim günü | "Dün 09:10" |
  | bu yıl | "12 Eylül" |
  | önceki yıllar | "12 Eylül 2025" |

  Dakika farkı süreyle (`Duration`), gün farkı takvim günüyle (`ChronoUnit.DAYS` iki tarihin günleri arasında) ölçülür:
  gece 23:50'deki bildirim 00:10'da "20 dk önce", 22:00'deki bildirim ertesi gün 00:30'da "Dün 22:00" görünür.
- `gunBasligi(iso)` — liste bölüm başlığı: aynı gün "Bugün", bir önceki gün "Dün", yoksa "12 Eylül, Cuma" (yıl YOK);
  okunamazsa `""`.
- `gun(tarih)` — saatsiz tarih ("2026-09-30", `LocalDate.parse`) → "30 Eylül 2026, Çarşamba" (sitedeki `tarihGun`'un
  karşılığı); okunamazsa metni olduğu gibi (`null`'da `""`) döner. Bugün çağıranı yok.
- `bugun()` — bugünün tarihi, Türkiye'de: "26 Eylül, Cumartesi" (ana sayfanın küçük başlığı).
- `selam()` — Türkiye saatine göre: 06:00'dan önce "İyi geceler", 12:00'den önce "Günaydın", 18:00'den önce "İyi günler",
  sonra "İyi akşamlar".

## Kimle konuşur?

- Android/Java: `java.time` (`Instant`, `ZonedDateTime`, `LocalDate`, `LocalTime`, `ZoneId`, `Duration`, `ChronoUnit`,
  `DateTimeFormatter`), `java.util.Locale`. Başka sınıfa bağımlılığı yok, ağa gitmez.
- Onu kullananlar (grep):
  - [AnaSayfa.md](AnaSayfa.md) — `bugun()` (bölüm etiketi), `selam()` (büyük başlık: "Günaydın, Elif").
  - [BildirimlerSayfasi.md](BildirimlerSayfasi.md) — `gunBasligi(createdAt)` (gün kartlarının başlığı), `goreli(createdAt)`
    (satırın alt yazısı).
  - `oku` ve `TURKIYE` dışa açık ama yalnız bu dosyanın içinde kullanılıyor; `gun` hiç kullanılmıyor.
  - Benzer ama ayrı bir saat dilimi [Bildirimler.md](Bildirimler.md)'de var: servis saatini hesaplamak için
    `TimeZone.getTimeZone("Europe/Istanbul")` (eski `Calendar` arayüzüyle).
- Girdiler sunucudan gelir: bildirimin `createdAt`'ı (`GET /api/notifications`, `sunucu/bolumler/kayit.md`).
- Sitedeki karşılıkları (`public/js/parcalar/02-ikonlar.js`, site deposu): `tarihGun` ("4 Eylül 2026, Cuma") → `gun`;
  `tarihSaat` ise "26.09.2026 17:23" biçiminde yazar, `goreli`'nin sitede birebir eşi yok. En yakını Çocuğumun telefonu
  sayfasındaki `aileOnce` (`public/js/parcalar/27b-aile.js`: "az önce", "3 dakika önce", "bugün 21:40", sonra tarih); ama
  "dk" yerine "dakika" yazar ve "Dün" demez.

## Nasıl çalışır (adım adım)?

```
BildirimlerSayfasi: b.createdAt = "2026-10-03T06:05:00.000Z"
   gunBasligi ─► oku: Instant.parse ─► atZone(Europe/Istanbul) = 3 Ekim 09:05
                 bugün (TR) 3 Ekim ─► gün farkı 0 ─► "Bugün"
   goreli     ─► şimdi 09:40 (TR) ─► 35 dk ─► "35 dk önce"
               ─► şimdi 14:00     ─► 295 dk, gün farkı 0 ─► "09:05"
               ─► ertesi gün      ─► gün farkı 1 ─► "Dün 09:05"
AnaSayfa: bugun() ─► "3 Ekim, Cumartesi" ; selam() saat 9 ─► "Günaydın"
```

## Dikkat!

- **Telefon başka saat dilimindeyse de Türkiye saati yazar.** Yurt dışındaki bir velinin telefonunda "14:32" Türkiye'deki
  saattir; selam da Türkiye saatine göredir (bilinçli: okulun saati). Site ise tarayıcının kendi saat dilimini kullanır
  (`new Date(...)`); aynı bildirim sitede ve uygulamada farklı saatle görünebilir.
- **Telefonun saati yanlışsa göreli zamanlar da yanlış.** "Şimdi" telefonun saatinden alınır. Telefon geri kalmışsa yeni
  gelen bildirim "gelecekte" kalır: `goreli` "az önce" der (eksi dakika), `gunBasligi` ise ertesi günün tarihini yazabilir.
- **Yalnız `Z` ile biten ISO anı güvenle okunur.** `Instant.parse` `"…Z"` biçimini bekler; sunucu hep öyle verir. Saat farkı
  eklenmiş (`+03:00`) ya da saatsiz bir metin Android sürümüne göre okunamayabilir; o zaman `oku` `null`, ekranda boş metin
  çıkar, hata fırlamaz. Saatsiz tarih ("2026-09-30") için `gun` kullan.
- **`gunBasligi`'nda yıl yok.** Bir yıldan eski bir bildirim "12 Eylül, Cuma" başlığıyla bu yılınkiyle karışabilir.
  Bildirimler sayfası son 100 bildirimi gösterdiği için nadir; sıradaki saklama işi (bildirimler 90 gün) bunu tümden ortadan
  kaldırır.
- **Takvim günü ile süre farkı ayrı.** "Dün" kararı takvim gününe göre olduğu için gece yarısını geçen kısa aralıklar
  "dk önce" (60 dakikadan azsa) ya da "Dün HH:mm" olur; bu bilinçli.
- Ay ve gün adları cihazın `java.time` dil verisinden gelir; `tr-TR` her Android'de var ("Eylül", "Cumartesi").
- `DateTimeFormatter` nesneleri değişmez ve iş parçacıkları arasında güvenle paylaşılır; sabit tutulmaları doğru.

## Testleri

- Android deposunda otomatik test yok; derleme ve lint (`./gradlew --offline assembleDebug lintDebug`) ana oturumda.
  Saf Java olduğu için ileride JVM'de çalışan birim testleri en kolay buraya yazılır (uygulama tanımı doğrulayıcı için JVM
  testlerinden söz ediyor).
- Elle (öykünücü, deneme paketi):
  - Ana sayfada bugünün tarihi ("3 Ekim, Cumartesi" gibi) ve saate uygun selam.
  - Siteden kişiye bir bildirim üret → Bildirimler'de "BUGÜN" başlığı ve "az önce"; birkaç dakika sonra "Yenile" → "N dk
    önce".
  - Öykünücünün saat dilimini başka bir ülkeye çevir → saatler yine Türkiye saatiyle yazılmalı.

## Son durum

- `git log` (Android deposu): 1 commit. Dosya `e96c5f2 commit 6` (2026-09-26) ile geldi: yerel uygulamanın ilk ekranları (ana
  sayfa, bildirimler) için sitedeki tarih yardımcılarının karşılığı olarak eklendi. O günden beri değişmedi.
- Bilinen açıklar (kod değiştirilmedi): `gun` ve dışa açık `oku`'nun dışarıdan kullanılmaması, `gunBasligi`'nda yıl olmaması.
- Planlı işlerden bu dosyaya dokunması beklenenler:
  - "Android yerel uygulama" (iş 10): ödev son teslimi, ders programı, sınav, takvim, mesaj ekranları gelecek; `gun` ve yeni
    biçimler (ör. sitedeki "4 Eylül 2026, Cuma · 12:00") burada toplanmalı.
  - "Mesaj ayarları …" işinin eki (bildirim paneli sekmeler hâlinde): tanım tarihi "25 Eylül 2026 Cuma 15:57" biçiminde
    istiyor ([BildirimlerSayfasi.md](BildirimlerSayfasi.md)); `goreli`'nin yerini alabilir.
  - "Çok dil" (iş 22): "az önce", "dk önce", "Dün", "Bugün", selamlar ve `tr-TR` sabit dili çeviri kataloğuna ve seçili dile
    bağlanacak.
  - "Optimizasyon + saklama süreleri" (iş 7): bildirimler 90 gün sonra silinecek.

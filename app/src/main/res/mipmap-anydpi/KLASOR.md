# app/src/main/res/mipmap-anydpi/KLASOR.md

Başlatıcı simgesi: tek dosya `ic_launcher.xml`, üç katmanlı bir uyarlanabilir simge (zemin `simge_arka`, ön yüz `simge_on`,
tek renkli `simge_tek`).

## Bu dosya ne yapar?

Telefonun ana ekranında ve uygulama listesinde görünen "Eğitim Evi" simgesi buradan gelir. Manifest onu
`android:icon="@mipmap/ic_launcher"` diye gösterir ([../../KLASOR.md](../../KLASOR.md)).

Android 8.0'dan beri simgeler "uyarlanabilir"dir (adaptive icon): uygulama zemini ve ön yüzü ayrı katmanlar olarak verir,
telefon onları kendi maskesiyle (daire, yuvarlak kare, damla…) kırpar ve hareket ettirir. Android 13'ten beri üçüncü bir
tek renkli katman da verilebilir; kişi "temalı simgeler"i açarsa telefon onu duvar kâğıdının rengiyle boyar.

Klasörün adındaki `anydpi` "her ekran yoğunluğu için bu tek dosya" demektir: katmanlar vektör olduğu için ayrı ayrı PNG'ye
(`mipmap-hdpi`, `mipmap-xxhdpi` …) gerek yok. Uygulama zaten Android 8.0'dan (`minSdk 26`) başladığı için uyarlanabilir
simgeyi desteklemeyen eski telefonlara yedek PNG de gerekmiyor.

## İçinde neler var?

### `ic_launcher.xml`

```
<adaptive-icon>
    <background android:drawable="@drawable/simge_arka" />
    <foreground android:drawable="@drawable/simge_on" />
    <monochrome android:drawable="@drawable/simge_tek" />
</adaptive-icon>
```

| Katman | Çizim | Ne |
|---|---|---|
| `<background>` | `simge_arka` | 108×108 dp, yukarıdan aşağı koyulaşan kırmızı (`#D62839` → `#A51D2C`) |
| `<foreground>` | `simge_on` | Bacalı beyaz ev, aralık kapıdan sarı ışık; ev ortadaki 60 dp'de, telefonun daire maskesi (66 dp) evi kesmez |
| `<monochrome>` | `simge_tek` | Aynı evin tek renkli hâli; telefon yalnız saydamlığı kullanır |

Çizimlerin ayrıntısı ve nasıl güncelleneceği: [../drawable/KLASOR.md](../drawable/KLASOR.md).

Yuvarlak simge için ayrı bir dosya (`ic_launcher_round`) ve manifestte `android:roundIcon` yok; uyarlanabilir simge her
maskeye kendisi uyduğu için gerekmiyor.

## Kimle konuşur?

- Manifest: `android:icon="@mipmap/ic_launcher"` ([../../KLASOR.md](../../KLASOR.md)).
- Çizimler: [../drawable/KLASOR.md](../drawable/KLASOR.md)'deki `simge_arka.xml`, `simge_on.xml`, `simge_tek.xml`.
- Telefonun başlatıcısı ve ayarlar uygulaması bu simgeyi gösterir. Uygulamanın kendi kodu bu dosyayı kullanmaz; giriş
  ekranındaki marka ayrı bir çizimdir (`simge_marka`), bildirimlerin küçük simgesi de ayrı (`bildirim_simge`).

## Nasıl çalışır (adım adım)?

```
başlatıcı simgeyi çizer ─► @mipmap/ic_launcher ─► mipmap-anydpi/ic_launcher.xml
   temalı simgeler kapalı ─► simge_arka (kırmızı) + simge_on (beyaz ev) ─► telefonun maskesiyle kırpılır
   temalı simgeler açık (Android 13+) ─► simge_tek ─► telefonun tema rengiyle boyanır
```

## Dikkat!

- **Ön yüzde güvenli alan.** Uyarlanabilir simgenin 108 dp'sinin yalnız ortadaki 66 dp'lik dairesi her maskede görünür;
  `simge_on`'daki ev bu yüzden ortadaki 60 dp'ye sığdırıldı. Çizimi değiştirirken evi büyütme.
- **Tek renkli katman ayrı çizim.** `commit 4`'te tek renkli katman olarak o günkü çizgi ev `simge_on` verilmişti.
  `commit 11`'de ön yüz sarı ışıklı, dolu bir eve dönüşünce tek renkli katman için ayrı bir çizim, `simge_tek` yazıldı
  (yorumu: kapı kanadı dolu, kanatla kasa arasındaki aralık boş, zemine ışık düşüyor). Ön yüz değişirse `simge_tek` de
  elle değişmeli.
- Klasörün eski adı `mipmap-anydpi-v26`'ydı; `minSdk` 26 olduğu için `-v26` eki bir şey değiştirmiyordu, `commit 4`'te
  kaldırıldı.

## Testleri

- Otomatik test yok; lint kırık bir `@drawable` göndermesini derlemede yakalar.
- Elle: deneme paketini kur, ana ekranda kırmızı zemin üstünde beyaz evi gör; telefonun simge biçimini değiştir (daire,
  kare), ev kesilmemeli; Android 13+'da "temalı simgeler"i aç, tek renkli ev gelmeli.
- Bu belge yazılırken telefonda denenmedi.

## Son durum

- Dosyanın geçmişi: `34b45f1 commit 1` (2026-09-26, `mipmap-anydpi-v26/ic_launcher.xml`: yalnız zemin ve ön yüz),
  `3875db7 commit 4` (klasör `mipmap-anydpi` oldu; `<monochrome android:drawable="@drawable/simge_on" />` eklendi),
  `cf2af61 commit 11` (2026-09-27, logo: tek renkli katman `simge_tek`'e geçti).
- Açık iş yok.
- Planlı işlerde bu dosyaya dokunan bir madde yok; logo değişirse yalnız `drawable/`'daki katman çizimleri değişir.

# app/src/main/res/drawable/KLASOR.md

Uygulamanın bütün çizimleri, 56 vektör XML: sitenin çizgi simgelerinden araçla üretilen 50 `ik_*.xml`, üst çubuğun "Yenile"
simgesi `ust_yenile.xml`, başlatıcı simgesinin üç katmanı (`simge_arka.xml`, `simge_on.xml`, `simge_tek.xml`), giriş
ekranındaki marka `simge_marka.xml` ve bildirim çubuğu simgesi `bildirim_simge.xml`.

## Bu dosya ne yapar?

Uygulamada hiç PNG ya da JPEG yok: her simge Android'in vektör çizimi (`<vector>`) olarak yazılı, her ekran yoğunluğunda
keskin görünür ve tek dosyadır. Bu belge klasördeki her dosyanın ne olduğunu, nereden geldiğini (araç mı üretti, elle mi
yazıldı), uygulamada nerede kullanıldığını ve nasıl güncelleneceğini anlatır.

Üç tür dosya var:

1. **Sitenin simgeleri (`ik_*.xml`, 50 dosya).** Eğitim Evi sitesinin arayüzünde emoji yok; onun yerine 24×24'lük çizgi
   simgeler var (site deposunda `public/js/parcalar/02-ikonlar.js`, `IKONLAR`). [../../../../../araclar/simgeleri-uret.md](../../../../../araclar/simgeleri-uret.md)
   bu simgeleri birebir Android çizimine çevirir; böylece uygulama ve site aynı simgeleri kullanır. Dosyaların içinde renk
   siyah yazılıdır; asıl rengi kod verir (`ImageView` boyama, aşağıda).
2. **Elle yazılan arayüz simgesi:** `ust_yenile.xml`.
3. **Marka simgeleri (elle yazıldı):** başlatıcı simgesinin katmanları, giriş ekranındaki marka ve bildirim simgesi.
   Ölçüleri sitenin simgesiyle aynı sayılardır (site deposunda `araclar/simge-uret.js` ve `public/simge.svg`); o araç bu
   dosyaları üretmez, site simgesi değişirse bunlar elle güncellenir.

## İçinde neler var?

### Marka ve sistem simgeleri

| Dosya | Ne | Nerede |
|---|---|---|
| `simge_arka.xml` | Başlatıcı simgesinin zemini: 108×108 dp, yukarıdan aşağı `#D62839`'dan `#A51D2C`'ye koyulaşan kırmızı (geçiş görünen orta alana, 18..90'a yayılır) | [../mipmap-anydpi/KLASOR.md](../mipmap-anydpi/KLASOR.md) → `ic_launcher.xml` `<background>` |
| `simge_on.xml` | Başlatıcı simgesinin ön yüzü: bacalı beyaz ev, kapısı hafif aralık, içeriden sarı ışık sızıyor ("kapımız herkese açık"); ev ortadaki 60 dp'ye oturur, telefonun daire maskesi (66 dp) evi kesmez | `ic_launcher.xml` `<foreground>` |
| `simge_tek.xml` | Tek renkli başlatıcı simgesi: aynı ev; telefon yalnız saydamlığı kullanıp kendi tema rengiyle boyar (Android 13+ "temalı simgeler") | `ic_launcher.xml` `<monochrome>` |
| `simge_marka.xml` | Giriş ekranının markası: 72 dp, yuvarlak köşeli kırmızı kare içinde ev (sitedeki `public/simge.svg`'nin aynısı); açık ve koyu temada aynı görünür | [GirisSayfasi](../../java/org/egitimevi/aile/GirisSayfasi.md) (`marka()`; ekran okuyucuya gizli, altındaki "Eğitim Evi" yazısı okunur) |
| `bildirim_simge.xml` | Bildirim çubuğu simgesi: beyaz ev silueti (baca, iki pencere, aralık kapı); telefon yalnız saydamlığı kullanır | [Bildirimler](../../java/org/egitimevi/aile/Bildirimler.md) (her telefon bildirimi), [SeferServisi](../../java/org/egitimevi/aile/SeferServisi.md) ("Sefer sürüyor"), [IzlemeServisi](../../java/org/egitimevi/aile/IzlemeServisi.md) (çocuğun telefonu) |
| `ust_yenile.xml` | Sitenin üst çubuğundaki "yenile" simgesi (site deposunda `public/index.html`), 24×24 çizgi; başındaki yorum "elle eklendi" der | [AnaEkran](../../java/org/egitimevi/aile/AnaEkran.md) üst çubuğundaki "Yenile" düğmesi |

### Sitenin simgeleri (`ik_*.xml`)

Hepsi 24×24 dp, 1,8 kalınlığında yuvarlak uçlu çizgi; başlarında `Sitenin "<ad>" simgesi (02-ikonlar.js);
araclar/simgeleri-uret.js üretir, elle değiştirme` yorumu ve lint'in "kullanılmayan kaynak" uyarısını susturan
`tools:ignore="UnusedResources"` var. Dosya adı sitedeki adın küçük harfli, alt çizgili hâlidir (`gozKapali` →
`ik_goz_kapali.xml`). Bugün 50 simgenin 17'si kullanılıyor; öbürleri rol ekranları yazıldıkça kullanılacak.

| Dosya | Sitedeki adı (anlamı) | Uygulamada nerede |
|---|---|---|
| `ik_alev.xml` | `alev` | kullanılmıyor |
| `ik_anahtar.xml` | `anahtar` | kullanılmıyor |
| `ik_anket.xml` | `anket` | kullanılmıyor |
| `ik_ara.xml` | `ara` (arama) | kullanılmıyor |
| `ik_ayar.xml` | `ayar` (ayarlar) | [Sekmeler](../../java/org/egitimevi/aile/Sekmeler.md): alt çubuktaki "Ayarlar" sekmesi |
| `ik_belge.xml` | `belge` | [AyarlarSayfasi](../../java/org/egitimevi/aile/AyarlarSayfasi.md): "Aydınlatma metni" satırı |
| `ik_bildirim.xml` | `bildirim` (zil) | [Sekmeler](../../java/org/egitimevi/aile/Sekmeler.md): "Bildirimler" sekmesi; [AnaEkran](../../java/org/egitimevi/aile/AnaEkran.md): üst çubuktaki zil; [AyarlarSayfasi](../../java/org/egitimevi/aile/AyarlarSayfasi.md): "Telefon bildirimleri"; [BildirimlerSayfasi](../../java/org/egitimevi/aile/BildirimlerSayfasi.md): "Bildirim yok" boş durumu |
| `ik_cikis.xml` | `cikis` (çıkış) | kullanılmıyor |
| `ik_ders.xml` | `ders` | kullanılmıyor |
| `ik_ek.xml` | `ek` (dosya eki) | kullanılmıyor |
| `ik_ekle.xml` | `ekle` | [AyarlarSayfasi](../../java/org/egitimevi/aile/AyarlarSayfasi.md): "Ekle" satırı |
| `ik_ev.xml` | `ev` | [Sekmeler](../../java/org/egitimevi/aile/Sekmeler.md): "Ana sayfa" sekmesi |
| `ik_geri.xml` | `geri` (sola ok) | [AnaEkran](../../java/org/egitimevi/aile/AnaEkran.md): üst çubuktaki "Geri"; [Arayuz](../../java/org/egitimevi/aile/Arayuz.md): dokunulabilir satırın sağındaki ok (180° döndürülerek) |
| `ik_goz.xml` | `goz` (göster) | kullanılmıyor |
| `ik_goz_kapali.xml` | `gozKapali` (gizle) | kullanılmıyor |
| `ik_grafik.xml` | `grafik` | kullanılmıyor |
| `ik_grup.xml` | `grup` (kişiler) | [AnaSayfa](../../java/org/egitimevi/aile/AnaSayfa.md): "Henüz bir portalın yok" boş durumu; [AyarlarSayfasi](../../java/org/egitimevi/aile/AyarlarSayfasi.md): "Portallarım" |
| `ik_harita.xml` | `harita` | kullanılmıyor |
| `ik_hayir.xml` | `hayir` | kullanılmıyor |
| `ik_hedef.xml` | `hedef` | kullanılmıyor |
| `ik_igne.xml` | `igne` (raptiye) | kullanılmıyor |
| `ik_indir.xml` | `indir` | kullanılmıyor |
| `ik_izinli.xml` | `izinli` | kullanılmıyor |
| `ik_kilit.xml` | `kilit` | [AyarlarSayfasi](../../java/org/egitimevi/aile/AyarlarSayfasi.md): "Şifre değiştir" |
| `ik_konum.xml` | `konum` | [AyarlarSayfasi](../../java/org/egitimevi/aile/AyarlarSayfasi.md): öğrencinin "Bu telefonu velimle paylaş" satırı |
| `ik_kulup.xml` | `kulup` (kulüp) | kullanılmıyor |
| `ik_kutu.xml` | `kutu` | kullanılmıyor |
| `ik_mudur.xml` | `mudur` (müdür) | [PortalSecici](../../java/org/egitimevi/aile/PortalSecici.md): "Müdür · okul" satırı |
| `ik_muzik.xml` | `muzik` | kullanılmıyor |
| `ik_odev.xml` | `odev` (ödev) | kullanılmıyor |
| `ik_ogrenci.xml` | `ogrenci` (öğrenci) | kullanılmıyor |
| `ik_ogretmen.xml` | `ogretmen` (öğretmen) | [EkleSayfasi](../../java/org/egitimevi/aile/EkleSayfasi.md): "Öğretmen" seçeneği; [PortalSecici](../../java/org/egitimevi/aile/PortalSecici.md): "Öğretmen · okul" satırı |
| `ik_okul.xml` | `okul` | [EkleSayfasi](../../java/org/egitimevi/aile/EkleSayfasi.md): "Müdür" seçeneği; [GirisSayfasi](../../java/org/egitimevi/aile/GirisSayfasi.md): okul arama sonuçları; [AyarlarSayfasi](../../java/org/egitimevi/aile/AyarlarSayfasi.md): "Siteyi aç" |
| `ik_onay.xml` | `onay` | kullanılmıyor |
| `ik_oynat.xml` | `oynat` | kullanılmıyor |
| `ik_posta.xml` | `posta` (e-posta) | [KayitSayfasi](../../java/org/egitimevi/aile/KayitSayfasi.md) ve [SifremiUnuttumSayfasi](../../java/org/egitimevi/aile/SifremiUnuttumSayfasi.md): "E-postana bak"; [EkleSayfasi](../../java/org/egitimevi/aile/EkleSayfasi.md): yöneticinin e-posta satırı |
| `ik_profil.xml` | `profil` | kullanılmıyor |
| `ik_resim.xml` | `resim` | kullanılmıyor |
| `ik_saat.xml` | `saat` | kullanılmıyor |
| `ik_servis.xml` | `servis` (okul servisi) | kullanılmıyor |
| `ik_sinav.xml` | `sinav` (sınav) | kullanılmıyor |
| `ik_sinif.xml` | `sinif` (sınıf) | kullanılmıyor |
| `ik_soru.xml` | `soru` | [AyarlarSayfasi](../../java/org/egitimevi/aile/AyarlarSayfasi.md): "Sık sorulan sorular" |
| `ik_takvim.xml` | `takvim` | kullanılmıyor |
| `ik_telefon.xml` | `telefon` | [EkleSayfasi](../../java/org/egitimevi/aile/EkleSayfasi.md): yöneticinin telefon satırı |
| `ik_uyari.xml` | `uyari` (uyarı) | [Arayuz](../../java/org/egitimevi/aile/Arayuz.md): hata kutusu ("Bir sorun oldu" + "Yeniden dene") |
| `ik_veli.xml` | `veli` | [EkleSayfasi](../../java/org/egitimevi/aile/EkleSayfasi.md): "Veli" seçeneği |
| `ik_yemek.xml` | `yemek` | kullanılmıyor |
| `ik_yildiz.xml` | `yildiz` (yıldız) | kullanılmıyor |
| `ik_yukle.xml` | `yukle` (yükle) | kullanılmıyor |

## Kimle konuşur?

- **Kod:** simgeler `R.drawable.<ad>` ile çağrılır. Neredeyse hepsi [Arayuz](../../java/org/egitimevi/aile/Arayuz.md)'un
  `ikon(c, simge, renkId, boyDp)` işlevinden geçer: `ImageView` + `setImageTintList` — yani dosyadaki siyah çizgi ekranda
  istenen renge boyanır (ör. sekmede seçiliyse `ana`, değilse `soluk`; boş durumda `ana`). `simgeDugme` (üst çubuk
  düğmeleri) aynı yoldan `yazi` rengiyle 24 dp çizer. `simge_marka` boyanmadan, kendi renkleriyle konur.
  `bildirim_simge`'yi `Notification.Builder.setSmallIcon` kullanır.
- **Başlatıcı simgesi:** [../mipmap-anydpi/KLASOR.md](../mipmap-anydpi/KLASOR.md)'deki `ic_launcher.xml` üç `simge_*`
  katmanını birleştirir; manifest onu `android:icon` olarak verir.
- **Üreten:** `ik_*.xml`'leri [../../../../../araclar/simgeleri-uret.md](../../../../../araclar/simgeleri-uret.md); kaynağı
  site deposundaki `public/js/parcalar/02-ikonlar.js`. Marka simgelerinin ölçüleri site deposundaki
  `araclar/simge-uret.js`'le aynı (o araç bu dosyaları yazmaz; onun site deposundaki belgesi `araclar/simge-uret.md` de
  `simge_on`, `simge_tek`, `simge_marka` ve `bildirim_simge`'nin aynı sayılarla elle yazıldığını söyler).

## Nasıl çalışır (adım adım)?

Bir sekmenin simgesi ekrana şöyle gelir:

```
Sekmeler.icin: new Sekme("Ana sayfa", R.drawable.ik_ev, ...)
  AnaEkran.altCubukCiz ─► Arayuz.ikon(this, R.drawable.ik_ev, secili ? R.color.ana : R.color.soluk, 22)
     ImageView.setImageResource(ik_ev)       (24×24 vektör, siyah çizgi)
     ImageView.setImageTintList(ana/soluk)   (çizgi boyanır; koyu temada values-night'taki renk)
```

Sitede bir simge değişince ya da yeni simge eklenince:

```
site deposunda 02-ikonlar.js değişti
  ─► Android deposunda: node araclar/simgeleri-uret.js <site deposunun klasörü>
  ─► bütün ik_*.xml yeniden yazılır ─► git --no-pager diff ile neyin değiştiğine bak ─► lintDebug
```

## Dikkat!

- **`ik_*.xml`'leri elle düzenleme.** Araç bir sonraki çalışmasında hepsinin üstüne yazar. Simgeyi değiştirmek
  istiyorsan sitedeki `IKONLAR`'da değiştir, aracı çalıştır.
- **Araç dosya silmez.** Sitede bir simge kaldırılırsa (ör. planlı "kulüpler kaldırılacak" işinde `kulup` giderse)
  `ik_kulup.xml` burada kalır; elle sil. Bugün sitedeki liste ile buradaki 50 dosya birebir aynı (aşağıda "Testleri").
- **Renk dosyada değil, kodda.** Bir `ik_*` simgesini `ImageView`'a boyamadan koyarsan siyah çıkar ve koyu temada
  görünmez; her zaman `Arayuz.ikon` ya da `setImageTintList` kullan.
- **Marka simgeleri sitenin simgesiyle elle eşit tutulur.** Sitede evin çizimi değişirse (site deposunda
  `araclar/simge-uret.js`'teki `EV` sayıları) `simge_on`, `simge_tek`, `simge_marka`, `bildirim_simge` (ve renkler değişirse
  `simge_arka`) burada elle güncellenmeli.
- **Bildirim simgesi tek renktir.** Android bildirim çubuğunda yalnız saydamlığı kullanır; renkli bir çizim koyarsan
  beyaz bir kare görünür. `simge_tek` de aynı kurala uyar.
- **`ust_yenile.xml` aracın dışında.** Adı `ik_` ile başlamadığı için araç onu ezmez; sitedeki karşılığı değişirse elle
  güncellenir.
- Sitede quiz ekranının çalışırken eklediği `yukari`/`asagi` okları `IKONLAR`'da yazılı değil; araç onları üretmez. Uygulamaya
  gerekirse ya sitenin listesine eklenmeli ya da burada elle yazılmalı.

## Testleri

- Otomatik test yok. Lint (`./gradlew --offline lintDebug`) bozuk bir vektörü derlemede yakalar; kullanılmayan simgeler
  `tools:ignore="UnusedResources"` sayesinde uyarı vermez.
- Bu belge yazılırken aracın dönüşümü ayrı bir betikle, hiçbir dosyaya yazmadan yeniden hesaplandı: sitenin bugünkü
  `IKONLAR` listesindeki 50 simgenin her biri buradaki `ik_*.xml` ile bayt bayt aynı çıktı (araç çalıştırılmadı).
- Elle: deneme paketinde alt çubuğun simgelerini açık ve koyu temada gör; seçili sekme kırmızı (`ana`), ötekiler gri olmalı.
  Başlatıcıda simgenin kırmızı zeminli ev olduğunu, Android 13+'da "temalı simgeler" açıkken tek renkli evin geldiğini gör.

## Son durum

- Klasörün geçmişi: `34b45f1 commit 1` (2026-09-26: `bildirim_simge`, `simge_arka`, `simge_on` ilk hâlleri), `e96c5f2
  commit 6` (2026-09-26), `cf2af61 commit 11` (2026-09-27).
- Son değişiklikler, farklarına göre:
  - `cf2af61 commit 11` (logo): `simge_arka` düz kırmızı bir şekilden (`<shape>`) kırmızı geçişli vektöre döndü;
    `simge_on` çizgi evden (beyaz çizgi, 2 kat büyütülmüş) bacalı, aralık kapılı, sarı ışıklı dolu eve döndü;
    `bildirim_simge` çizgiden dolu siluete geçti; yeni `simge_tek` (tek renkli katman) ve `simge_marka` (giriş ekranı)
    eklendi. Aynı commit'te giriş ekranı kırmızı kare içindeki `ik_okul` yerine `simge_marka`'yı kullanmaya başladı.
  - `e96c5f2 commit 6` (yerel uygulama çekirdeği): 50 `ik_*.xml` araçla üretildi, `ust_yenile.xml` elle eklendi.
- Açık iş yok.
- Planlı işlerden bu klasörü etkileyecekler: "Android yerel uygulama (bütün roller)" — rol ekranları (ödev, sınav, servis,
  takvim…) kullanılmayan simgeleri kullanmaya başlayacak; yeni simge gerekirse önce sitedeki listeye eklenip araçla
  üretilir. "Kulüpler kaldırılacak" — sitedeki `kulup` simgesi giderse `ik_kulup.xml` elle silinmeli.

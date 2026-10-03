# app/src/main/res/values-night/KLASOR.md

Koyu temanın kaynakları: telefon koyu temadayken Android'in `values/` yerine seçtiği `renkler.xml` (aynı 35 ad, koyu değerler)
ve `temalar.xml` (`Tema`'nın koyu hâli).

## Bu dosya ne yapar?

Kodda hiçbir yerde "koyu tema mı?" diye sorulmaz. Ekranlar renkleri hep adıyla ister (`R.color.ana`); telefonun ayarı koyu
temadaysa Android `-night` ekli bu klasördeki aynı adlı kaynağı verir. Böylece uygulama telefonun temasını kendiliğinden
izler. Açık tema ve renklerin uygulamada ne için kullanıldığı [../values/KLASOR.md](../values/KLASOR.md)'de; bu belge yalnız
koyu değerleri ve sitenin koyu temasıyla farkları anlatır.

## İçinde neler var?

### `renkler.xml`

Başındaki yorum: "Sitenin koyu teması (00-temel.css, prefers-color-scheme: dark)." Adlar `values/renkler.xml` ile birebir
aynı (bu belge yazılırken karşılaştırıldı); `tools:ignore="UnusedResources"` burada da var.

| Ad | Koyu değer | Sitenin koyu temasıyla |
|---|---|---|
| `ana` | `#FF5C6C` | aynı |
| `ana_koyu` | `#FF8F9A` | aynı (koyu temada "koyu" ton daha açıktır: koyu zeminde okunsun) |
| `ana_acik` | `#35191E` | aynı |
| `ana_cizgi` | `#5C2830` | aynı |
| `ustune_yazi` | `#15181D` | aynı (açık kırmızı düğmenin üstünde koyu yazı) |
| `ikinci`, `ikinci_acik` | `#2CC6D4`, `#0F2E33` | aynı |
| `vurgu`, `vurgu_acik` | `#FFC44D`, `#3A2F14` | aynı |
| `zemin` | `#15181D` | aynı |
| `kart` | `#1D2127` | aynı |
| `kart_ust` | `#252A31` | aynı |
| `yazi` | `#EEF0F3` | aynı |
| `soluk` | `#9AA1AB` | aynı |
| `cizgi`, `cizgi_koyu` | `#2C323A`, `#3A414B` | aynı |
| `yesil`, `yesil_zemin`, `yesil_yazi` | `#4ADE80`, `#14301F`, `#86EFAC` | aynı |
| `kirmizi`, `kirmizi_zemin`, `kirmizi_yazi` | `#F87171`, `#3A1A1A`, `#FCA5A5` | aynı |
| `turuncu`, `turuncu_zemin`, `turuncu_yazi` | `#FB923C`, `#3A2414`, `#FDBA74` | aynı |
| `mavi`, `mavi_zemin`, `mavi_yazi` | `#60A5FA`, `#172036`, `#93C5FD` | aynı |
| `gri_zemin` | `#262A31` | **farklı**: sitede `#2a2324` |
| `bordo` | `#B91C1C` | **farklı**: sitede koyu temada da `#7f1d1d` |
| `harita_okul`, `harita_ev`, `harita_servis`, `harita_ben` | `#60A5FA`, `#4ADE80`, `#FF5C6C`, `#2DD4BF` | **farklı**: sitede harita renkleri iki temada aynıdır (harita döşemesi hep açık renkli olduğu için) |
| `perde` | `#B3000000` | **farklı**: burada %70 siyah, sitede `rgba(0, 0, 0, .62)` |

Farklı olanların hepsi bugün uygulamada ya hiç kullanılmıyor (`harita_*`, `perde`) ya da yalnız henüz hiçbir ekranın
kullanmadığı etiket türlerinde geçiyor (`gri_zemin`, `bordo`); yani ekranda bir fark görünmez. Neden farklı yazıldıkları
kodda ya da commit'te söylenmiyor.

### `temalar.xml`

```
<style name="Tema" parent="@android:style/Theme.Material.NoActionBar">
```

Android'in koyu Material teması. Öğeler `values/temalar.xml` ile aynı adları ve aynı renk adlarını taşır (renkler bu
klasörden koyu gelir); tek fark `android:windowLightStatusBar` = `false`: koyu zeminde durum çubuğunun simgeleri açık olur.

## Kimle konuşur?

- Android kaynak seçimi: telefonun gece kipi açıkken (`uiMode` = night) bu klasör `values/`'in önüne geçer.
- Kullananlar `values/` ile aynı: [Tema](../../java/org/egitimevi/aile/Tema.md) (`Tema.renk`),
  [Arayuz](../../java/org/egitimevi/aile/Arayuz.md) ve ekranlar ([../values/KLASOR.md](../values/KLASOR.md) listesi);
  manifest `@style/Tema`.
- Kullanmayan: [AileEkrani](../../java/org/egitimevi/aile/AileEkrani.md) kendi sabit açık renkleriyle çizer; telefon koyu
  temadayken de açık görünür.
- Kaynak: site deposunda `public/css/parcalar/00-temel.css`'in koyu bölümü.

## Nasıl çalışır (adım adım)?

```
Ayarlar > Ekran > Koyu tema açık
  uygulama açılır ─► Tema (values-night/temalar.xml): Theme.Material.NoActionBar, açık durum çubuğu simgeleri
  Arayuz.kart(...) ─► Tema.renk(c, R.color.kart) ─► #1D2127
```

Uygulama açıkken tema değişirse ekran yeniden kurulmaz (manifestte `AnaEkran` `uiMode` değişimini kendisi karşılıyor):
yeni açılan sayfalar koyu, var olanlar eski renkte kalır. Uygulamayı kapatıp açmak hepsini düzeltir.

## Dikkat!

- **Her yeni renk iki dosyaya birden.** Burada olmayan bir ad koyu temada açık değerini alır (çoğu zaman okunmaz).
- **Sitenin koyu temasıyla dört fark var** (yukarıdaki tablo). Harita ya da açılır pencere ekranları yazılırken bu
  değerler sitedekilerle karşılaştırılıp bilerek seçilmeli.
- **`ana_koyu` koyu temada daha açık bir tondur.** Adı "koyu" ama işi "zeminle karşıtlık"; yeni bir yerde kullanırken iki
  temada da bak.

## Testleri

- Otomatik test yok; lint eksik ya da bozuk kaynağı yakalar.
- Elle: telefonu koyu temaya al, uygulamayı kapatıp aç; zemin `#15181D`, kartlar `#1D2127`, birincil düğme açık kırmızı
  (`#FF5C6C`) üstünde koyu yazı olmalı; "Çocuğun telefonu" ekranı açık kalır (bilinen durum).
- Bu belge yazılırken değerler sitenin `00-temel.css`'iyle tek tek karşılaştırıldı; uygulama çalıştırılmadı.

## Son durum

- Klasör tek commit'le geldi: `e96c5f2 commit 6` (2026-09-26, yerel uygulama çekirdeği) — iki dosya da o günden beri
  değişmedi.
- Açık iş: sitenin koyu temasıyla dört farkın (yukarıda) gözden geçirilmesi; kod değiştirilmedi.
- Planlı işlerden bu klasörü etkileyecekler: "Android yerel uygulama (bütün roller)" — harita ve açılır pencere ekranları
  `harita_*` ve `perde` renklerini kullanmaya başlayınca koyu değerleri önem kazanır; tanım her ekranın açık ve koyu temada
  denenmesini istiyor.

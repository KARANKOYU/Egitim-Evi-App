# app/src/main/java/org/egitimevi/aile/Arayuz.java

Uygulamanın arayüz takımı: yazı, başlık, kart, düğme, form alanı, liste satırı, avatar, renkli etiket, yükleniyor, boş durum,
hata kutusu ve uyarı şeridi gibi bütün ekranların kurulduğu hazır parçalar — sitedeki kalıpların Android karşılığı, dış
kütüphanesiz.

## Bu dosya ne yapar?

Uygulamada XML yerleşim dosyası yok; her ekran görünümünü Java koduyla kurar. Herkes kendi `TextView`'ini, kendi kenar
boşluğunu, kendi rengini seçseydi ekranlar birbirine benzemezdi ve koyu tema bir yerde bozulurdu. Bu dosya bunun önüne
geçer: bir ekran "başlık koy, altına kart, kartın içine etiketli bir kutu ve birincil düğme" der, ölçüler, yazı tipleri,
köşe yarıçapları ve renkler hep aynı gelir. Renkler `Tema.renk(...)` üzerinden `res/values/renkler.xml` ve
`res/values-night/renkler.xml`'den okunur; telefon koyu temadaysa koyu karşılıkları gelir.

Parçalar sitedeki görünüşün kopyasıdır: kart, düğmeler, form kutusu ve hata yazısı, "boş kutu", "yükleniyor", uyarı
şeridi (sitede `.msg`, `public/css/parcalar/02-form.css`), avatar (sitede `avatar()`, `public/js/parcalar/02-ikonlar.js`).
Köşe yarıçapları `Tema.java`'daki `R_ORTA` (22 dp, kart) ve `R_KUCUK` (14 dp, düğme ve kutu); bunlar da sitedeki `--r` ve
`--r-kucuk`'un karşılığı.

Uygulama tanımının tasarım kuralı açık: ekranlar YALNIZ `Arayuz`/`Tema` parçalarıyla kurulur, sabit renk kodu yazılmaz.
Bu kuralın tek istisnası 1.0.x'ten kalan [AileEkrani.md](AileEkrani.md)'dir (kendi küçük yardımcıları ve sabit renkleri var).

## İçinde neler var?

### Türler

- `Dugme` — `BIRINCIL` (ana renk zemin, üstüne yazı rengi), `IKINCIL` (açık ana renk zemin, koyu ana renk yazı),
  `HAYALET` (saydam zemin, ana renk yazı), `TEHLIKE` (kırmızı zemin, kırmızı yazı).
- `Etiket` — `ANA`, `YESIL`, `KIRMIZI`, `TURUNCU`, `MAVI`, `GRI`, `BORDO`. Hem `etiket(...)` hapının hem `serit(...)`'in
  renk çiftini seçer (iç işlev `etiketRenkleri`: zemin + yazı rengi; `GRI` yazısı `soluk`, `BORDO` yazısı `ustune_yazi`,
  `ANA` ve bilinmeyen `ana_acik` + `ana_koyu`). Uygulama tanımındaki karşılıklar: Yaptı yeşil, Geç yaptı mavi, Eksik
  turuncu, Yapmadı kırmızı, Gelmedi (izinsiz) bordo.

### Yazı

- `yazi(c, metin, boySp, renkId)` — temel `TextView`: boy sp cinsinden, renk kaynak kimliğiyle, `Tema.GOVDE` (sans-serif),
  satır aralığı 1,25. Öbür bütün yazı parçaları bunu çağırır.
- `baslik(c, metin)` — sayfa başlığı: 27 sp, `Tema.BASLIK` (serif kalın), satır aralığı 1,1 (sitedeki büyük başlık).
- `altBaslik(c, metin)` — kart ya da bölüm başlığı: 17 sp, kalın.
- `bolumEtiketi(c, metin)` — "BUGÜN", "PORTALLARIN" gibi küçük bölüm etiketi: metni Türkçe kurallarla büyük harfe çevirir
  (`tr-TR`: "bildirimler" → "BİLDİRİMLER", noktalı İ), 12 sp, soluk, kalın, harf aralığı 0,08.
- `metin(c, metin)` — 15 sp düz metin. Bugün çağıran yok.
- `soluk(c, metin)` — 13 sp soluk renkli açıklama.

### Yerleşim

- `dikey(c)` / `yatay(c)` — dikey ya da yatay `LinearLayout` (yatayda içerik dikeyde ortalı).
- `ekle(ust, gorunum, ustBoslukDp)` — görünümü tam genişlikte, içeriği kadar yükseklikte ve üstünde verilen kadar boşlukla
  dikey bir kaba ekler. Ekranlardaki "kartlar arası 12–18 dp" ritmi bununla kurulur.
- `sayfaGovdesi(c, kaydirma)` — kaydırılabilir sayfa: içi dolduran (`fillViewport`) bir `ScrollView` ve içinde kenarları
  18 dp, üstü 8, altı 28 dp boşluklu dikey gövde. GÖVDEYİ döner; `ScrollView`'u verilen tek elemanlı dizinin ilk gözüne
  yazar. Sayfa içeriği gövdeye ekler ama `olustur()`'dan `kaydirma[0]`'ı döner (bkz. [AnaSayfa.md](AnaSayfa.md)).
- `kart(c)` — dikey kart: `kart` renkli zemin, 22 dp köşe, 1 dp `cizgi` renkli çerçeve, 16 dp iç boşluk, 1 dp gölge.
  Liste kartlarında ekranlar iç boşluğu `(0, 4, 0, 4)` yapar ki satırlar kenara dayansın.
- `ayirici(c)` — 1 dp (en az 1 piksel) yatay çizgi.

### Düğmeler ve simgeler

- `dugme(c, metin, tur)` — `TextView` tabanlı düğme: ortalı, 16 sp kalın, en az 50 dp yükseklik, 18×12 dp iç boşluk,
  türe göre zemin/yazı rengi, 14 dp köşe ve dokununca dalgalanan zemin (`Tema.dalgali`). Tıklanabilir ve odaklanabilir.
- `mesgul(dugme, mesgul)` — istek sürerken düğmeyi kapatır ve soluklaştırır (saydamlık 0,55); iki kez basılmasın diye.
  Yazıyı değiştirmez ("Giriş yapılıyor..." gibi yazıyı ekran kendisi koyar).
- `simgeDugme(c, simge, aciklama)` — 44 dp yuvarlak simge düğmesi: 24 dp simge (`yazi` renginde), dokununca daire
  biçiminde dalgalanır (dalga rengi `ana_cizgi`), `contentDescription` ekran okuyucu için. Yalnız [AnaEkran.md](AnaEkran.md)'nin üst çubuğu kullanır
  (Geri, Yenile, Bildirimler).
- `ikon(c, simge, renkId, boyDp)` — `res/drawable/ik_*.xml` simgesini verilen renge boyanmış `ImageView` olarak verir
  (boyut `LinearLayout` yerleşim ölçüsüyle gelir).

### Form

- `Alan` — etiketli giriş alanının üç parçası: `kok` (bütün alan; sayfaya bu eklenir), `kutu` (`EditText`), `hata` (kutunun
  altındaki kırmızı yazı). İşlevleri:
  - `deger()` — kutudaki metin, baştaki/sondaki boşluklar kırpılmış.
  - `hamDeger()` — kırpılmamış metin (şifreler için; boşluk şifrenin parçası olabilir).
  - `hataGoster(metin)` — hata yazısını koyar ve gösterir, kutunun çerçevesini kırmızı yapar; `null`/boş verilirse ikisini
    de kaldırır.
- `alan(c, etiket, ipucu, tur)` — 14 sp orta kalınlıkta etiket, altında kutu, altında (gizli) hata yazısı. `tur` Android
  `InputType` değeridir; `TYPE_TEXT_FLAG_MULTI_LINE` yoksa kutu tek satırlıdır. Kutu: 16 sp, en az 50 dp, `kart_ust` zemin;
  çerçeve duruma göre — hata varken 2 dp kırmızı, odaktayken 2 dp ana renk, yoksa 1 dp `cizgi_koyu`. Noktalarla gizleme
  YALNIZ türün varyasyon kısmı (`tur & TYPE_MASK_VARIATION`) tam olarak `TYPE_TEXT_VARIATION_PASSWORD` iken yapılır
  (aşağıda "Dikkat!").

### Liste

- `satir(c, sol, baslik, alt, sag)` — liste satırı: en az 64 dp; solda simge ya da avatar (sağına 14 dp boşluk), ortada
  16 sp başlık ve varsa soluk alt yazı (kalan genişliği kaplar), sağda etiket ya da başka bir görünüm (soluna 10 dp boşluk).
  `sol` ve `sag` `null` olabilir. Çocuk sırası: `[sol] [orta] [sag]`.
- `tiklananSatir(c, sol, baslik, alt, sag, tik)` — dokunulabilir satır: dalgalanan zemin, tıklama dinleyicisi; `sag`
  verilmezse sağa bakan ok koyar (`ik_geri` simgesini 180° döndürerek, 18 dp, soluk).
- `avatar(c, ad, anahtar, boyDp)` — yuvarlak, renkli zemin üstünde baş harfler ("Elif Kaya" → "EK"; `Tema.basHarfler`).
  Renk `Tema.avatarRengi(anahtar)`'dan (anahtar yoksa addan): aynı kişi her yerde aynı renkte. Yazı boyu daire çapının
  yaklaşık %36'sı (`boyDp * 0.36` sp). Ekran okuyucuya kapalı (adı zaten yanında yazar).
- `etiket(c, metin, tur)` — durum hapı: 12 sp kalın, tam yuvarlak köşe, türün renk çifti ("Buradasın" yeşil, "Onay
  bekliyor" turuncu).

### Durumlar

- `yukleniyor(c)` — ortada ana renkli dönen çember (40 dp), en az 220 dp yükseklik, "Yükleniyor" açıklaması.
- `bosDurum(c, simge, baslik, aciklama, dugme)` — ortalı boş durum: açık ana renk 68 dp daire içinde 30 dp simge, başlık,
  varsa soluk açıklama, varsa düğme (sitedeki "boş kutu"nun karşılığı).
- `hataKutusu(c, mesaj, yeniden)` — `bosDurum`'un hata hâli: uyarı simgesi (`ik_uyari`), "Bir sorun oldu", `mesaj` ve
  `yeniden` verilmişse "Yeniden dene" (`IKINCIL`) düğmesi.
- `serit(c, metin, tur)` — uyarı/bilgi şeridi: 14 sp, türün renk çiftiyle 14 dp köşeli kutu (sitedeki `.msg`).

## Kimle konuşur?

- Çağırdıkları: `Tema.java` (`renk`, `dp`, `zemin`, `dalgali`, `avatarRengi`, `basHarfler`, yazı tipleri `GOVDE`, `ORTA`,
  `KALIN`, `BASLIK`, yarıçaplar `R_ORTA`, `R_KUCUK`); renk kaynakları `R.color.*` (`ana`, `ana_acik`, `ana_koyu`,
  `ana_cizgi`, `ustune_yazi`, `kart`, `kart_ust`, `yazi`, `soluk`, `cizgi`, `cizgi_koyu`, `kirmizi`, `kirmizi_zemin`,
  `kirmizi_yazi`, `yesil_zemin`, `yesil_yazi`, `turuncu_zemin`, `turuncu_yazi`, `mavi_zemin`, `mavi_yazi`, `gri_zemin`,
  `bordo`); simgeler `R.drawable.ik_geri`, `ik_uyari`. Android: `TextView`, `EditText`, `ImageView`, `LinearLayout`,
  `FrameLayout`, `ScrollView`, `ProgressBar`, `GradientDrawable`, `RippleDrawable`, `StateListDrawable`, `InputType`,
  `PasswordTransformationMethod`.
- Onu çağıranlar (grep): uygulamanın yeni çekirdeğindeki bütün ekranlar — [AnaEkran.md](AnaEkran.md) (üst ve alt çubuk,
  avatar, kısa mesaj), [AnaSayfa.md](AnaSayfa.md), `AyarlarSayfasi`, `BildirimlerSayfasi`, `DogrulamaSorusu`,
  `EkleSayfasi`, `GirisSayfasi`, `KayitSayfasi`, `KisiKodu`, `KodSayfasi`, `KvkkSayfasi`, `PortalSecici`, `Sayfa`
  (`Sayfa.Veri`: `yukleniyor`, `hataKutusu`), `SifreSayfasi`, `SifremiUnuttumSayfasi`. Kullanmayan tek ekran
  [AileEkrani.md](AileEkrani.md).
  - Bugün kullanılan türler: `Dugme.BIRINCIL`, `IKINCIL`, `HAYALET`, `TEHLIKE` (yalnız Ayarlar'daki "Çıkış yap");
    `Etiket.MAVI` (ana sayfa şeridi, girişte seçilen okul), `YESIL` ("Buradasın"), `TURUNCU` ("Onay bekliyor").
    `Etiket.ANA`, `KIRMIZI`, `GRI`, `BORDO` ve `metin(...)` rol ekranları için hazırda bekliyor.
- Sunucuyla konuşmaz; ağ yok.

## Nasıl çalışır (adım adım)?

Tipik bir sayfa şöyle kurulur (örnek, `GirisSayfasi`'nın sadeleştirilmiş hâli):

```
ScrollView[] s = new ScrollView[1];
LinearLayout g = Arayuz.sayfaGovdesi(e, s);              // kaydırılan gövde (18 dp kenar)
LinearLayout kart = Arayuz.kart(e);                       // beyaz/koyu kart
kart.addView(Arayuz.altBaslik(e, "Giriş yap"));
Arayuz.Alan sifre = Arayuz.alan(e, "Şifre", "", TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_PASSWORD);
Arayuz.ekle(kart, sifre.kok, 12);                         // üstünde 12 dp boşluk
TextView gir = Arayuz.dugme(e, "Giriş yap", Arayuz.Dugme.BIRINCIL);
Arayuz.ekle(kart, gir, 18);
Arayuz.ekle(g, kart, 24);
return s[0];                                              // ScrollView döner, gövde değil

gönderirken:  Arayuz.mesgul(gir, true) → istek → Arayuz.mesgul(gir, false)
hata gelince: sifre.hataGoster(h.getMessage())  → kırmızı çerçeve + altta yazı
```

Form kutusunun çerçevesi bir durum listesidir: önce "hata" (`activated`), sonra "odak" (`focused`), sonra varsayılan.
`hataGoster` kutuyu `activated` yapar; bu yüzden hatalı kutu odaktayken de kırmızı kalır, hata silinince odak rengine döner.

## Dikkat!

- **Renkler kurulurken bir kez okunur.** Her parça rengini oluşturulduğu anda `Tema.renk(...)` ile alır. `AnaEkran`
  manifestte `uiMode` değişikliğini kendisi karşılıyor (`configChanges`) ama `onConfigurationChanged` yazılmamış; telefon
  uygulama açıkken koyu temaya geçerse var olan görünümler eski renklerinde kalır, yalnız yeniden kurulan sayfalar yeni
  renkleri alır (kod okumasına göre; telefonda denenmedi).
- **`satir`'ın çocuk sırasına başka dosyalar dayanıyor.** `BildirimlerSayfasi` (başlığı kalınlaştırmak için) ve
  `EkleSayfasi`'nın seçenek kartları (başlığı 18 sp yapmak için) `getChildAt(1)` (orta) → `getChildAt(0)` (başlık) yolunu
  kullanıyor — ikisinde de solda her zaman bir görünüm olduğu için; `tiklananSatir` da sağdaki oku "son çocuk" diye
  döndürüyor. `satir`'ın düzenini değiştirirsen bunları da güncelle.
- **Sağdan sola diller düşünülmemiş.** Boşluklar `leftMargin`/`rightMargin` ile veriliyor (başlangıç/bitiş değil) ve ok
  `ik_geri`'nin 180° döndürülmüşü; manifestte `supportsRtl="true"` olsa da Arapça gibi bir dilde satırlar ve ok ters
  durur.
- **Şifre gizleme kuralı.** `InputType` bir bit alanıdır: görünür parola türü (`TYPE_TEXT_VARIATION_VISIBLE_PASSWORD`,
  0x90; bugün `EkleSayfasi`'ndaki 16 karakterlik "Veli kodu" kutusu kullanıyor) da parola bitini (0x80) taşır. Eski
  `(tur & PASSWORD) != 0` denetimi bu yüzden veli kodunu da noktalarla gizliyordu; `cad4bc3 commit 10` denetimi
  `(tur & TYPE_MASK_VARIATION) == PASSWORD` yaptı.
  [AileEkrani.md](AileEkrani.md)'nin kendi `alan`'ında eski denetim duruyor (oradaki kutular etkilenmiyor).
- **Sıra önemli:** `setSingleLine(...)` metin dönüştürmeyi sıfırlar; bu yüzden gizleme (`setTransformationMethod`) en
  sonda yapılıyor. Kutu kurulumunu değiştirirken bu sırayı bozma.
- **`dugme` bir `TextView`.** Tıklanır ve odaklanır, ama sınıfı `Button` olmadığı için ekran okuyucu onu büyük olasılıkla
  "düğme" diye duyurmaz (kod okumasına göre; TalkBack'le denenmedi).
- **`sayfaGovdesi` gövdeyi döner, `ScrollView`'u değil.** Sayfa `olustur()`'dan yanlışlıkla gövdeyi dönerse
  [AnaEkran.md](AnaEkran.md)'nin `goster`'i onu `ScrollView`'dan söküp (`ScrollView` bir `FrameLayout`'tur) doğrudan içerik
  alanına koyar: hata çıkmaz ama sayfa kaydırılamaz, uzun içerik ekranın altında kesilir. Her zaman `kaydirma[0]`'ı dön.
- **Sabit renkler yalnız temadan bağımsız yerlerde:** dalgalanma maskeleri beyaz (`0xFFFFFFFF`, yalnız biçim için,
  görünmez), `HAYALET` zemini ve simge düğmesinin zemini saydam, avatar yazısı beyaz ve avatar renkleri `Tema`'daki sabit
  sekizli (iki temada aynı). Yeni parça yazarken renk kodu
  yazma; `renkler.xml`'e (iki dosyaya da) renk ekle.
- `mesgul` yalnız kapatır ve soluklaştırır; düğmenin yazısını geri koymak çağıranın işi.

## Testleri

- Android deposunda otomatik test yok; derleme ve lint (`./gradlew --offline assembleDebug lintDebug`) ana oturumda.
  Uygulama tanımının "İnceleme" aşaması bütün ekranların öykünücüde açık ve koyu temada, telefon ve tablet genişliğinde
  gözle denetlenmesini istiyor; ekran görüntüleri site deposunun `ekran-goruntuleri/uygulama/` klasörüne.
- Elle: giriş ekranında kutuları boş bırakıp "Giriş yap"a bas → kimlik kutusu kırmızı çerçeve ve altında "Kullanıcı adını
  ya da e-posta adresini yaz."; kutuya dokun → çerçeve kırmızı kalır; yazıp yeniden dene → hata kalkar. Yetişkin
  hesabıyla "+ Ekle" → "Veli" → "Veli kodunu gir" ekranındaki "Veli kodu (16 karakter)" kutusuna yazılanlar noktalanmadan
  görünmeli (commit 10'un düzelttiği şey); şifre kutuları noktalı kalmalı. Telefonu koyu temaya alıp uygulamayı yeniden
  aç → kartlar, kutular, etiketler koyu renklerde.

## Son durum

- `git log`: 2 commit (Android deposu). Son değişiklik `cad4bc3 commit 10` (2026-09-27): `alan`'daki şifre gizleme denetimi
  düzeltildi — yalnız gerçek parola türü gizleniyor, görünür parola türündeki veli kodu kutusu artık noktalanmıyor (yanına
  nedenini anlatan yorum eklendi; aynı commit `EkleSayfasi` ve `KisiKodu`'yu da değiştirdi). Dosyanın ilk hâli `e96c5f2 commit 6` (2026-09-26): yerel uygulamanın çekirdeğiyle
  birlikte bütün takım (yazı, yerleşim, kart, düğmeler, form, liste, avatar, etiket, durumlar, şerit).
- Bilinen açıklar (kod değiştirilmedi): canlı tema değişiminde eski renkler, sağdan sola düzen, `satir` düzenine dışarıdan
  dayanılması, `metin(...)`'in kullanılmaması.
- Planlı işlerden bu dosyaya dokunması beklenenler: "Android yerel uygulama" (bütün roller — rol ekranlarının durum
  etiketleri, harita, tarih-saat seçici, dosya yükleme ilerleme çubuğu gibi yeni parçalar buraya eklenecek); "Android geri
  tuşu" (tanım, pencere, menü ve alttan açılan sayfalar için ortak bir "açık katmanlar" düzeni öneriyor); "Üst şerit
  sadeleştirme" (uygulamada ← → ⌂ düğmeleri); "Çok dil" (sağdan sola diller için başlangıç/bitiş yerleşimi).

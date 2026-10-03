# app/src/main/java/org/egitimevi/aile/AileEkrani.java

"Çocuğun telefonu" ekranı (Eğitim Evi Aile): öğrencinin telefonunu kendi hesabı ve açık onayıyla velisine bağlar, bağlıyken
gereken izinleri (konum — her zaman, kullanım erişimi, bildirim, pil) ve son gönderim durumunu gösterir, bağlantıyı
kaldırır.

## Bu dosya ne yapar?

Öğrenci isterse telefonunun konumunu ve hangi uygulamayı ne kadar kullandığını velisiyle paylaşabilir. Veli bunları
sitedeki "Çocuğumun telefonu" sayfasında görür; okul (müdür, öğretmen) görmez; veriler 7 gün sonra silinir; hiçbir
uygulama kapatılmaz ya da kilitlenmez. Bu ekran o paylaşımın telefondaki yüzüdür. İşin arka plandaki kısmını (konumu
almak, kuyruğa koymak, göndermek, ekran süresini okumak) `IzlemeServisi.java`, `Kuyruk.java` ve `Kullanim.java` yapar; bu
ekran yalnız bağlar, izinleri toplar, durumu gösterir ve bağlantıyı kaldırır.

Bu ekran uygulamanın ana ekranından ([AnaEkran.md](AnaEkran.md)) AYRI bir Android `Activity`'sidir ve ondan bağımsız
çalışır: kendi giriş formu, kendi anahtarı, kendi ayar dosyası (`Ayarlar.java`, "aile") vardır. İki yerden açılır:

- Öğrencinin Ayarlar sayfasındaki "Bu telefonu velimle paylaş" satırı (`AyarlarSayfasi`; yalnız rolü `student` olana
  görünür; alt yazısı bağlıyken "Bağlı: konum ve ekran süresi velinle paylaşılıyor").
- Paylaşım sürerken bildirim çubuğunda duran "Eğitim Evi Aile — Konumun ve ekran süren velinle paylaşılıyor." bildirimi
  (`IzlemeServisi`'nin ön plan bildirimi).

İki hâli var:

1. **Bağlı değilken:** paylaşımın ne olduğunu anlatan metin, sunucu adresi, okul adresi, kullanıcı adı, şifre, doğrulama
   sorusu, açık onay kutusu ve "Giriş yap ve bu telefonu bağla". Öğrenci kendi hesabıyla girer; sunucu bu telefona yalnız
   konum ve süre göndermeye yarayan bir **Aile anahtarı** verir; öğrencinin oturumu hemen kapatılır, telefonda yalnız o
   anahtar kalır.
2. **Bağlıyken:** bağlı hesabın adı, velinin seçtiği gönderme sıklığı, izin satırları (her biri "✓ … " ya da "• … — kapalı"
   ve düğme), durum (son konum, son gönderim, bekleyen konum, son sorun) ve "Bu telefonun bağlantısını kaldır".

Dosya Eğitim Evi Aile 1.0.x'te uygulamanın tek ekranıydı (`AnaEkran` adıyla); tek uygulamaya geçişte bu ada taşındı. Bu
yüzden uygulamanın yeni arayüz takımını ([Arayuz.md](Arayuz.md)) kullanmaz, kendi küçük yardımcıları ve sabit renkleri
vardır. Kullandığı sunucu uçları da 1.0.x'teki hâliyle duruyor; kurulu eski uygulamalar çalışmaya devam etsin diye.

## İçinde neler var?

### Sabitler ve alanlar

- Renkler (sabit): `ANA` `#D62839` (başlık, uyarılar, birincil düğme), `YAZI` `#23191A`, `SOLUK` `#6D5F60`; zemin
  `#FFF9F5`, ikincil düğme `#F1E6E4`, izin satırı beyaz ve `#E7DCDA` çerçeveli, verilmiş izin yeşil `#2F7D32`.
- İzin isteği kodları: `IZIN_KONUM` = 1, `IZIN_ARKA` = 2 (arka planda konum), `IZIN_BILDIRIM` = 3.
- `arka` — tek iş parçacıklı yürütücü (ağ işleri sırayla); `ana` — ana iş parçacığı işleyicisi; `govde` — sayfanın dikey
  gövdesi; `sorunNo` — o anki doğrulama sorusunun kimliği (`/api/challenge`'ın `id`'si).

### Yaşam döngüsü

- `onCreate` — `ScrollView` + dikey gövde (20 dp kenar); Android 15 öncesinde durum ve gezinme çubuğunu zemin rengine
  boyar (15'ten itibaren çubuklar saydam); açık zeminde koyu çubuk simgeleri ister (Android 11+ `WindowInsetsController`,
  öncesinde eski bayrak); çubukların ve klavyenin kapladığı yeri gövdenin iç boşluğuna ekler.
- `onResume` — ekranı baştan çizer (`ciz`); bağlıysa ve konum izni varsa `IzlemeServisi.baslat` (izin sayfasından dönünce
  servis kendiliğinden kalksın diye).
- `onRequestPermissionsResult(kod, …)` — konum izni (`IZIN_KONUM`) verildiyse servisi başlatır; her sonuçta ekranı yeniden
  çizer (izin satırları güncellensin).
- `onDestroy` — `arka.shutdownNow()`.

### Çizim

- `ciz()` — gövdeyi boşaltır, "Çocuğun telefonu" başlığını koyar; `Ayarlar.bagli` ise `bagliEkran()`, değilse
  `girisEkrani()`.

### Bağlanma (bağlı değilken)

- `girisEkrani()` — kutular: "Sunucu adresi" (dolu gelir: `Ayarlar.sunucu`, varsayılan `https://egitimevi.org`), "Okulunun
  adresi (egitimevi.org/...)", "Kullanıcı adı", "Şifre" (gizli), doğrulama sorusu yazısı, "Cevap" (yalnız rakam), "Başka
  soru" (yeni soru ister), onay kutusu "Konumumun ve ekran süremin velimle paylaşılacağını okudum, kabul ediyorum.", ileti
  satırı ve "Giriş yap ve bu telefonu bağla". Düğmeye basınca sırayla:
  1. Sunucu adresi kurala uymuyorsa `Api.adresSorunu`'nun cümlesi ([Api.md](Api.md));
  2. onay işaretli değilse "Devam etmek için paylaşımı kabul etmelisin.";
  3. kullanıcı adı ya da şifre boşsa "Kullanıcı adını ve şifreni yaz.";
  4. düğme kapanır, "Bağlanıyor...", arka planda `baglan(...)`; `null` dönerse ekran "bağlı" hâliyle yeniden çizilir ve
     konum izni istenir; bir ileti dönerse ileti gösterilir, düğme açılır, YENİ soru alınır; beklenmedik bir hata olursa
     "Bağlanılamadı: <hatanın iletisi>" (soru yenilenmez).
- `soruGetir(adres, soruYazisi)` — adres kurala uymuyorsa "Önce sunucu adresini doğru yaz."; değilse arka planda
  `GET /api/challenge` → `sorunNo = id`, yazı "Doğrulama: <soru>"; ulaşılamazsa "Sunucuya ulaşılamadı: <ileti>". Ekran
  açılırken, "Başka soru"da ve başarısız denemeden sonra çağrılır.
- `baglan(adres, okul, kadi, sifre, cevap)` (arka iş parçacığında; `null` = başarı, metin = gösterilecek hata):
  1. `POST /api/login` gövdesi `{ email: <kullanıcı adı>, password, challengeId: sorunNo, challengeAnswer: <sayı ya da 0>,
     okul?: <küçük harfle okul adresi> }` — kimlik hâlâ ESKİ alan adıyla, `email` içinde gider. Sunucu hatası → iletisi.
  2. Cevapta `twoFactor` varsa (iki adımlı giriş öğrenci dışındaki e-postalı hesaplara sorulur) → "Bu uygulamaya öğrenci
     hesabıyla girilir."; `token` ya da `user` yoksa → "Giriş yapılamadı.".
  3. `user.role` `student` değilse oturumu hemen kapatır (`/api/logout`) → "Bu uygulamaya öğrenci hesabıyla girilir (veli,
     telefonun sahibi olan çocuğun hesabıyla bağlar)."
  4. `POST /api/aile/cihaz` (öğrencinin oturumuyla, Bearer) gövdesi `{ ad: "<üretici> <model>", platform: "android", surum:
     <Android sürümü>, onay: true }` → cevaptaki `cihazAnahtari`, sunucu adresi ve öğrencinin adı (`ogrenci.ad`, yoksa
     girişteki `fullName`) `Ayarlar.baglan` ile saklanır; `ayar` geldiyse `Ayarlar.ayariYaz(wifiDk, mobilDk, konumAcik,
     kullanimAcik)` (gelmeyen alanlar 5, 15, açık, açık). Sunucu hatası → iletisi.
  5. Her durumda (`finally`) öğrencinin oturumu kapatılır: telefonda öğrencinin oturumu KALMAZ, yalnız Aile anahtarı kalır.
- `cikis(adres, oturum)` — `POST /api/logout` (Bearer); hatası yok sayılır.

### Bağlıyken

- `bagliEkran()`:
  - "Bağlı hesap: <öğrencinin adı>" ve "Konumun ve ekran süren velinle paylaşılıyor. Velin gönderme sıklığını seçer: Wi-Fi'deyken
    N dakikada bir, mobil veride M dakikada bir. İnternet yokken konumlar telefonda bekler, bağlanınca gönderilir."
    (`Ayarlar.wifiAraligi`, `Ayarlar.mobilAraligi`).
  - **İzinler** (her satır: verilmişse yeşil "✓ …", değilse "• … — kapalı" ve düğme):
    - "Konum" → hassas ve yaklaşık konum izni (`IZIN_KONUM`).
    - Android 10+: "Konum: her zaman (uygulama kapalıyken de)" → `ACCESS_BACKGROUND_LOCATION` (`IZIN_ARKA`); düğmenin
      yazısı Android 11+'da `Ayarlar'da "Her zaman izin ver"i seç`, Android 10'da `Her zaman izin ver`. Önce konum izni
      yoksa sayfanın altına "Önce konum iznini ver." yazılır.
    - "Ekran süresi (kullanım erişimi)" → telefonun "Kullanım erişimi" ayar sayfası (`Kullanim.izinVar` denetler).
    - Android 13+: "Bildirimler" → `POST_NOTIFICATIONS` (`IZIN_BILDIRIM`).
    - "Arka planda çalışma (pil kısıtlaması yok)" → uygulamanın ayar sayfası (açılamazsa pil iyileştirme listesi);
      kapalıysa altında "Açılan sayfada Pil (Uygulama pil kullanımı) > Kısıtlamasız'ı seç.".
  - **Durum**: "Son konum: 3 Ekim 14:05" ya da "henüz yok" (`Ayarlar.sonKonum`), "Son gönderim: …" (`Ayarlar.sonGonderim`),
    bekleyen varsa "Gönderilmeyi bekleyen konum: N" (`Kuyruk.boyut`), sorun varsa kırmızı "Son sorun: …"
    (`Ayarlar.sonHata`).
  - "Bu telefonun bağlantısını kaldır" → düğme kapanır; arka planda `POST /api/aile/cihaz/sil` (`X-Aile-Cihaz` başlığıyla,
    hatası yok sayılır); sonra servis durur (`IzlemeServisi.durdur`), kuyruk silinir (`Kuyruk.temizle`), ayarlar silinir
    (`Ayarlar.cik` — sunucu adresi kalır), ekran "bağlı değil" hâline döner.
- `konumIzni()` — hassas YA DA yaklaşık konum izni var mı. `arkaKonumIzni()` — Android 10 öncesinde her zaman doğru, sonra
  `ACCESS_BACKGROUND_LOCATION`.
- `izinIste()` — bağlanınca yalnız konum iznini kendiliğinden ister (yoksa). Öbür izinler satırlardaki düğmelerle verilir.

### Küçük arayüz yardımcıları (iç)

`dp`, `bosluk` (tam genişlik + üst boşluk), `baslik` (26 sp serif kalın, `ANA`), `altBaslik` (18 sp kalın, 22 dp üst
boşluk), `yazi` (gövdeye ekleyip döner), `alan` (etiket + tek satırlı `EditText`; şifre türünde noktalı), `dugme` (yuvarlak
köşeli `Button`; birincil `ANA` zemin beyaz yazı, ikincil açık zemin), `izinSatiri` (ad, durum, gerekiyorsa düğme).

## Kimle konuşur?

- Çağırdıkları (aynı paket):
  - [Api.md](Api.md) — `adresSorunu`, `get` (`/api/challenge`), `post` (`/api/login`, `/api/aile/cihaz`, `/api/logout`,
    `/api/aile/cihaz/sil`), `Api.Hata`. [Ag.md](Ag.md)'yi KULLANMAZ: kendi arka plan yürütücüsü var ve oturum düşmesi,
    aydınlatma onayı gibi genel durumları [AnaEkran.md](AnaEkran.md) karşılamaz.
  - `Ayarlar.java` — `sunucu`, `bagli`, `baglan`, `ayariYaz`, `ogrenciAdi`, `wifiAraligi`, `mobilAraligi`, `sonKonum`,
    `sonGonderim`, `sonHata`, `cihazAnahtari`, `cik`.
  - `IzlemeServisi.java` — `baslat`, `durdur`; `Kuyruk.java` — `boyut`, `temizle`; `Kullanim.java` — `izinVar`.
- Sunucu uçları (belgeleri site deposunda):
  - `GET /api/challenge`, `POST /api/login`, `POST /api/logout` — `sunucu/bolumler/kayit.md`. Girişte kimliğin eski
    `email` alanıyla gelmesi bilerek destekleniyor (belge: "girişteki `body.email` yedeği kaldırılmamalı"); okul verilirse
    kullanıcı adı o okulun içinde aranır.
  - `POST /api/aile/cihaz` (öğrencinin oturumuyla) ve `POST /api/aile/cihaz/sil` (`X-Aile-Cihaz`) —
    `sunucu/bolumler/aile.md`: yalnız öğrenci bağlayabilir (değilse 403 "Telefonu çocuğun kendi öğrenci hesabıyla bağla."),
    `onay` tam `true` olmalı (değilse 400), öğrenci başına saatte 10 bağlama, öğrenci başına en çok 3 telefon (dördüncüsü en
    eskisini uyarısız siler), her bağlama ve kaldırma velilere bildirilir. Cevap `{ cihazAnahtari, cihazId, ogrenci: { ad },
    ayar }`.
  - Arka planda `IzlemeServisi`'nin kullandıkları: `GET /api/aile/cihaz/ayar`, `POST /api/aile/cihaz/konum`,
    `POST /api/aile/cihaz/kullanim` (aynı belge).
- Android: `Activity`, çalışma zamanı izinleri (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`,
  `ACCESS_BACKGROUND_LOCATION`, `POST_NOTIFICATIONS`), `Settings.ACTION_USAGE_ACCESS_SETTINGS`,
  `Settings.ACTION_APPLICATION_DETAILS_SETTINGS`, `Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS`, `PowerManager`
  (`isIgnoringBatteryOptimizations`), `WindowInsets`/`WindowInsetsController`, `SimpleDateFormat` (`tr-TR`).
- Manifest: `.AileEkrani` `exported="false"` (yalnız uygulamanın kendisi açabilir), `windowSoftInputMode="adjustResize"`;
  izinler `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `ACCESS_BACKGROUND_LOCATION`, `FOREGROUND_SERVICE`,
  `FOREGROUND_SERVICE_LOCATION`, `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`, `PACKAGE_USAGE_STATS` (kullanıcı ayar
  sayfasından açar) ve uygulama adlarını okumak için `<queries>`.
- Onu açanlar (grep): `AyarlarSayfasi.java` (`new Intent(e, AileEkrani.class)`), `IzlemeServisi.java` (ön plan
  bildiriminin `PendingIntent`'i). Telefon açılınca `BaslatmaAlici` servisi yeniden başlatır (bu ekranı açmaz).
- Rol: bağlamayı yalnız öğrenci yapabilir (ekranın kendisi uygulamada yalnız öğrencinin Ayarlar'ında görünür). Verileri
  site tarafında yalnız öğrenciye bağlı onaylı veliler görür.

## Nasıl çalışır (adım adım)?

```
Ayarlar → "Bu telefonu velimle paylaş" → AileEkrani.onResume → ciz → (bağlı değil) girisEkrani
   açılışta: GET /api/challenge → { id, soru: "3 + 4 = ?" } → "Doğrulama: 3 + 4 = ?" (sorunNo = id)
   öğrenci: kullanıcı adı, şifre, (okul), cevap, [x] onay → "Giriş yap ve bu telefonu bağla"
     arka:  POST /api/login {email, password, challengeId, challengeAnswer, okul?}
              twoFactor?            → "Bu uygulamaya öğrenci hesabıyla girilir."
              role != student       → logout + aynı uyarı (velinin notuyla)
            POST /api/aile/cihaz [Bearer] {ad, platform, surum, onay:true}
              → Ayarlar.baglan(adres, cihazAnahtari, ad) ; Ayarlar.ayariYaz(...)
            finally: POST /api/logout [Bearer]          ← öğrencinin oturumu telefonda kalmaz
     ana:   ciz() → bagliEkran ; izinIste() → konum izni penceresi
   izin verildi → onRequestPermissionsResult → IzlemeServisi.baslat (ön plan bildirimi: "Eğitim Evi Aile")
   (arka planda IzlemeServisi: dakikada bir tur → konum kuyruğa → X-Aile-Cihaz ile /konum, /kullanim, /ayar)

"Bu telefonun bağlantısını kaldır" → POST /api/aile/cihaz/sil [X-Aile-Cihaz]
   → IzlemeServisi.durdur, Kuyruk.temizle, Ayarlar.cik → girisEkrani
```

## Dikkat!

- **Buradaki sunucu adresi uygulamanın da adresi olur.** Bağlanınca `Ayarlar.baglan` kutudaki adresi "aile" ayar
  dosyasına yazar; uygulamanın bütün ekranları da adresi aynı yerden (`Ayarlar.sunucu`) okur ([Ag.md](Ag.md)). Başka bir
  sunucuya bağlanan öğrencinin ana uygulaması da oraya gitmeye başlar; bağlantı kaldırılınca adres yine kalır (kod
  okumasına göre; telefonda denenmedi).
- **Kimlik eski alan adıyla gider.** Giriş gövdesinde kullanıcı adı `email` alanında; sunucu bunu bilerek kabul ediyor.
  Sunucuda girişteki `body.email` yedeği kaldırılırsa bu ekran ve kurulu 1.0.x uygulamalar bağlanamaz.
- **Yanlış hesapla denemenin yan etkisi.** Velinin ya da öğretmenin e-postalı hesabıyla (doğru şifreyle) denenirse sunucu
  önce iki adımlı giriş kodunu o adrese GÖNDERİR, ekran sonra "öğrenci hesabıyla girilir" der. E-postası olmayan öğrenci dışı bir hesapla
  (ör. okulun açtığı servisçi) denenirse oturum açılır ve hemen kapatılır.
- **Aydınlatma onayı ve zorunlu şifre burada karşılanmaz.** Öğrencinin onayı eskiyse ya da şifresini okul verdiyse
  `/api/aile/cihaz` 403 döner ve ekran yalnız sunucunun iletisini gösterir ("Aydınlatma metni güncellendi. Devam etmek
  için okuyup onaylaman gerekiyor." / "Sana verilen şifreyle girdin. Devam etmeden önce kendi şifreni belirle."). Öğrenci
  önce uygulamaya (ya da siteye) girip bunları bitirmeli.
- **Okul adresi kutusu olduğu gibi gider.** Yazılan metin yalnız küçük harfe çevrilip `okul` olarak gönderilir; sunucu onu
  okulun kısa adresi olarak arar. Kutunun etiketi "(egitimevi.org/...)" dese de başına `egitimevi.org/` yazılırsa
  büyük olasılıkla "Bu okul adresi bulunamadı. Ana sayfadan okulunu seç." döner; yalnız kısa ad yazılmalı (kod okumasına
  göre). Kullanıcı adı birden çok okulda varsa sunucunun "Önce okulunu seç" iletisi çıkar; ana uygulamadaki gibi okul arama
  listesi burada yok.
- **Ham hata iletileri görünebilir.** Çok uzun bir sayı cevap olarak yazılırsa (`Integer.parseInt` taşar) ya da sunucu
  cevabında `cihazAnahtari` yoksa (`getString`) Java'nın İngilizce iletisi "Bağlanılamadı: …" diye ekrana çıkar. Ağ
  kopmasında da (`Api.Hata` olmayan `IOException`) aynı yoldan Android'in İngilizce iletisi gelir. Bağlıyken "Son sorun:"
  satırı da `IzlemeServisi`'nin yakaladığı hatanın iletisini olduğu gibi yazar (`Ayarlar.hata(e.getMessage())`); ağ
  hatalarında bu çoğu zaman İngilizce bir cümledir (kod okumasına göre).
- **Sunucu kutusunun klavye türü eksik.** Tür `TYPE_TEXT_VARIATION_URI` tek başına veriliyor, `TYPE_CLASS_TEXT` olmadan;
  sınıf bitleri boş kaldığı için klavye sınırlı kipte açılabilir (kod okumasına göre; telefonda denenmedi). Şifre gizleme
  denetimi de eski `(tur & PASSWORD) != 0` biçiminde ([Arayuz.md](Arayuz.md)'de düzeltilen); buradaki kutularda zararsız.
- **Koyu tema yok, arayüz takımı yok.** Renkler sabit (1.0.x mirası); telefon koyu temadayken de bu ekran açık renklidir.
  Uygulama tanımının "yalnız `Arayuz`/`Tema`, sabit renk yok" kuralına uymayan tek ekran.
- **Döndürmede yazılanlar gider.** Manifestte `configChanges` yok: telefon döndürülünce Activity baştan kurulur, kutular
  boş gelir. Döndürme anında süren bir bağlama arka planda bitse bile sonucu eski (yok edilmiş) ekrana gider; yeni ekran
  ancak bir sonraki `onResume`'da "bağlı" görünür (kod okumasına göre).
- **İzinler kendiliğinden sırayla istenmez.** `izinIste`'nin yorumu ("Bağlanınca izinler sırayla istenir: önce konum.") ve
  site deposundaki `belge/KILAVUZ.md` ("İzinler (uygulama sırayla ister)") öyle diyor ama `izinIste` yalnız konum iznini
  ister; "her zaman" konum, kullanım erişimi, bildirim ve pil için öğrencinin satırlardaki düğmelere tek tek
  basması gerekir. "Önce konum iznini ver." her basışta sayfanın sonuna yeni bir satır olarak eklenir (ekran yeniden
  çizilene kadar birikir).
- **Bağlantı kaldırmada sunucunun cevabına bakılmaz.** İstek arka planda gider ve biter (cevap ya da hata gelir), telefondaki
  silme ondan SONRA yapılır; ama sonuç ne olursa olsun yapılır. İnternet yokken basılırsa telefon her şeyi siler, sunucudaki
  cihaz kaydı ise kalır (veli sayfasında görünmeye devam eder, veli kaldırana ya da üç telefon sınırında düşene kadar).
- **Ana uygulamadan çıkmak paylaşımı durdurmaz** ([AnaEkran.md](AnaEkran.md)): iki ayrı anahtar ve iki ayrı ayar dosyası
  var. Paylaşımı durdurmanın yolu bu ekrandaki "Bu telefonun bağlantısını kaldır" (ya da velinin sitede kaldırması).
- **Dördüncü telefon.** Öğrenci dördüncü bir telefonu bağlarsa sunucu en eski cihazı uyarısız siler; o telefonun servisi
  401 alınca bağlantıyı kendisi unutur ve bu ekran yeniden giriş formunu gösterir.
- Durumdaki saatler telefonun saat dilimiyle yazılır (`SimpleDateFormat`), uygulamanın geri kalanı gibi Türkiye saatiyle
  (`Zaman`) değil.
- Ekrandaki paylaşım metni ("okulun görmez", "7 gün sonra silinir") sunucunun kurallarıyla aynı; bu kurallar değişirse
  metin ve aydınlatma metni birlikte güncellenmeli (KVKK kuralı).

## Testleri

- Android deposunda otomatik test yok; derleme ve lint (`./gradlew --offline assembleDebug lintDebug`) ana oturumda.
- Bu ekranın dayandığı sunucu sözleşmesini site deposundaki testler korur:
  - `testler/test-aile.js` — onaysız bağlama 400; veli kendi hesabıyla bağlayamaz (403); öğrenci onayla bağlar, 64
    haneli anahtar ve ayar gelir; veliye "telefonunu bağladı" bildirimi; anahtar oturum yerine geçmez, uygulama uçlarında
    geçmez; anahtarsız/yanlış/biçimsiz anahtar 401; konum, kullanım, ayar; veli ve öğrenci (`/api/aile/cihaz/sil`)
    bağlantıyı kaldırabilir, kaldırılan anahtar 401.
  - `testler/test-servis-yoklama.js` — uygulamanın `X-Cihaz` anahtarı Aile uçlarında 401 (iki anahtar karışmaz).
  - `testler/test-cakisma.js` ve `testler/test-admin-gizli.js` — `POST /api/login`'i eski `email` alanıyla (ve
    `okul`'la) çağırır; yedek alan bu sayede korunur. `testler/guvenlik-test.js` — doğrulama sorusu (`id`, `soru`; cevap
    istemciye sızmıyor, aynı soru iki kez kullanılamıyor).
- Elle (öykünücü, deneme paketi, deneme sunucusu `http://10.0.2.2:3200`):
  - Öğrenci hesabıyla uygulamaya gir → Ayarlar → "Bu telefonu velimle paylaş" → sunucu adresini yaz, kullanıcı adı ve
    şifre, onayı işaretle → "Giriş yap ve bu telefonu bağla" → "Bağlı hesap: …" ve konum izni penceresi.
  - Onayı işaretlemeden dene → "Devam etmek için paylaşımı kabul etmelisin."; veli hesabıyla dene → "Bu uygulamaya
    öğrenci hesabıyla girilir…".
  - İzin satırlarını tek tek aç; "Son konum" ve "Son gönderim" dolmalı; velinin sitedeki "Çocuğumun telefonu" sayfasında
    konum görünmeli.
  - "Bu telefonun bağlantısını kaldır" → giriş formu; bildirim çubuğundaki "Eğitim Evi Aile" bildirimi kalkmalı.

## Son durum

- `git log` (bu adla): 1 commit. `f70aeca commit 5` (2026-09-26, tek uygulamaya geçiş): çocuğun telefonu ekranı eski
  `AnaEkran.java`'dan bu dosyaya kopyalandı (git bunu %97 benzerlikle kopya görüyor); değişen yalnız sınıf adı, sınıf
  yorumu ("uygulamanın ana ekranı site; bu ekran öğrencinin Ayarlar'ındaki 'Bu telefonu velimle paylaş' düğmesiyle
  açılır") ve başlık ("Eğitim Evi Aile" → "Çocuğun telefonu"). O günden beri değişmedi. (Yorumdaki "ana ekran site"
  ifadesi `commit 6`'dan beri eski: ana ekran artık yerel.)
- İçeriğin önceki geçmişi `AnaEkran.java` adıyla: `3875db7 commit 4` (2026-09-26: Android 15'te çubuk renklerinin
  boyanmaması, pil izni için doğrudan "muaf tut" penceresi yerine uygulama ayar sayfası — Play Store kuralı — ve "Pil >
  Kısıtlamasız" ipucu, `tr-TR` tarih biçimi), `9e9d5af commit 2` (2026-09-26: çubuk ve klavye boşlukları, açık zeminde
  koyu çubuk simgeleri, şifre kutusunun gizlenmesi ve tek satır ayarının bunu bozmaması), `34b45f1 commit 1` (2026-09-26:
  Eğitim Evi Aile 1.0.x'in ilk hâli).
- Bilinen açıklar (kod değiştirilmedi): okul adresi kutusunun olduğu gibi gitmesi, ham Java iletileri, sunucu kutusunun
  klavye türü, döndürmede kaybolan durum, koyu temasızlık, sunucu adresinin ana uygulamayı da etkilemesi, yalnız konum
  izninin kendiliğinden istenmesi (yorum ve KILAVUZ "sırayla" diyor), "Önce konum iznini ver." satırlarının birikmesi,
  bağlantı kaldırmada sunucu cevabına bakılmaması, durum saatlerinin telefonun saat dilimiyle yazılması.
- Planlı işlerden bu dosyaya dokunması beklenenler: "Android yerel uygulama" (tanım bu ekranın ve uçlarının eski 1.0.x
  kurulumlarla uyumlu kalmasını istiyor; velinin "Çocuğumun telefonu" ekranı ayrı yazılacak); "KVKK ve onay metinleri TAM
  denetimi" (bu ekrandaki paylaşım ve onay metni de dışarı giden veri olarak gözden geçirilecek); "Tek kişi tek hesap +
  portallar öğrencide de" (öğrencinin girişi ve oturumu değişirse buradaki `role === "student"` denetimi ve `okul` kutusu
  etkilenebilir); "Güvenlik denetimi" (tanımdaki onaylı değişiklik: okulun verdiği HER şifre ilk girişte değişecek,
  öğrenci hesapları dahil, ve tanım "Android işine not düş" diyor — o zaman şifresini hiç değiştirmemiş öğrenci burada
  daha sık 403 `sifreDegismeli` görür; yukarıdaki "Aydınlatma onayı ve zorunlu şifre burada karşılanmaz" maddesi);
  "Çok dil" (ekrandaki sabit Türkçe metinler).

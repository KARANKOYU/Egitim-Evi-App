# app/src/main/java/org/egitimevi/aile/GirisSayfasi.java

Uygulamanın giriş ekranı: e-posta ya da kullanıcı adı ve şifre, öğrenci ve servisçi için okul arama, gerekirse doğrulama sorusu;
`POST /api/login` (30 günlük uygulama oturumu) ve iki adımlı girişte kod ekranına geçiş; ayrıca "Şifremi unuttum" ve "Hesap aç".

## Bu dosya ne yapar?

Oturum yokken uygulama bu ekranla açılır ([AnaEkran.md](AnaEkran.md) `akisiBaslat` → `girisGoster`). Üst ve alt çubuk
gizlidir (`cubuksuz`, giriş kipi). Sınıf yorumundaki kurallar:

- Veli, öğretmen ve müdür **e-posta ya da kullanıcı adıyla** girer.
- Öğrenci ve servisçi **okulunu seçip kullanıcı adıyla** girer: aynı kullanıcı adı başka okulda da olabilir. (Kullanıcı adı tek
  bir okulda varsa okul seçmeden de girilir; sunucu birden çok okulda bulursa "önce okulunu seç" der ve ekran okul kutusunu
  açar.)
- Hatalı denemeden sonra **doğrulama sorusu** çıkar ([DogrulamaSorusu.md](DogrulamaSorusu.md)).
- E-postası olan hesaba (öğrenci dışında) ikinci adımda **6 haneli kod** gider; o adım `KodSayfasi.java`'dır.

Giriş gövdesinde her zaman `uygulama: true` gider: sunucu bu oturumu tarayıcıdaki 7 gün yerine **30 gün** geçerli açar.

Kim görür: herkes, girişten önce. Giriş başarılı olunca [AnaEkran.md](AnaEkran.md) `oturumAc` ile aydınlatma onayı, zorunlu
şifre ya da rolün sekmeleri gelir.

## İçinde neler var?

### Sayfa sözleşmesi

- `baslik()` → "Giriş" (pencere başlığı; çubuk gizli olduğu için ekranda görünmez).
- `cubuksuz()` → `true`.
- `olustur()` → ekranın tamamı (aşağıda).

### Ekran (yukarıdan aşağı)

1. **Marka** (`marka()`): 72 dp uygulama simgesi (`R.drawable.simge_marka`; başlatıcı simgesi ve sitedeki simgeyle aynı çizim:
   bacalı ev, aralık kapı; ekran okuyucu atlar, altındaki yazı okunur), "Eğitim Evi" (32 sp, ortalı), "Ödev, not, servis ve
   okuldan haberler tek yerde.".
2. **"Giriş yap" kartı**:
   - "E-posta ya da kullanıcı adı" (`kimlik`; ipucu "ornek@eposta.com"; e-posta klavyesi);
   - "Şifre" (`sifre`; noktalı) ve altında "Şifreyi göster"/"Şifreyi gizle" (`sifreGoster`); klavyedeki "Bitti" tuşu
     `gir()`'i çağırır;
   - "Öğrenci ya da servisçiysen okulunu seç" (sola dayalı `HAYALET` düğme) → okul kutusu açılır, düğme gizlenir;
   - okul kutusu (`okulKutusuKur`): seçili okul şeridi (mavi; "<okul adı>  ·  değiştir", başta gizli), "Okulun" arama kutusu
     ("Okulunun adını yaz"), sonuç listesi;
   - doğrulama sorusu (başta gizli);
   - "Giriş yap" (birincil düğme) → `gir()`;
   - "Şifremi unuttum" → `SifremiUnuttumSayfasi`.
3. **"Hesabın yok mu?" kartı**: "Veli, öğretmen ve müdür kendi hesabını açar. Öğrenci ve servisçi hesabını okul açar; kullanıcı
   adını ve ilk şifreni okulundan al." ve "Hesap aç" (`IKINCIL`) → `KayitSayfasi`.
4. En altta ortalı not: "Bilgilerin yalnızca Eğitim Evi sunucusunda tutulur; reklam ya da satış için kimseyle paylaşılmaz."

### İşlevler

- `sifreGoster(Arayuz.Alan a)` (paket içi, statik) — bir şifre kutusunun altına "Şifreyi göster" yazısı ekler; dokununca kutu
  noktalı ↔ açık olur, imleç sona gider, yazı "Şifreyi gizle"/"Şifreyi göster" olur. `KayitSayfasi` ve `SifreSayfasi` de
  kullanır.
- `okulKutusuKur()` (iç) — okul kutusunu kurar. Seçili okul şeridine dokunmak seçimi kaldırır (`okulKisaAd` boşalır, şerit
  gizlenir, arama kutusu yeniden görünür). Arama kutusuna her yazışta 350 ms bekleyip `okulAra()` çağrılır (yazmayı bırakınca bir
  kez; `aramaIsi`).
- `okulKutusunuAc(boolean ac)` (iç) — kutuyu açar/kapatır; açarken "okulunu seç" düğmesini gizler ve arama kutusuna odaklanır.
- `okulAra()` (iç) — kutudaki yazı (kırpılmış) 2 harften kısaysa sonuçları temizler. Değilse `GET /api/okul-adres/ara?q=…`:
  sonuç yoksa "Bu adla bir okul bulunamadı."; varsa ilk 6 okul satır olarak (okul simgesi, okulun adı, "ilçe / il"). Satıra
  dokununca `okulKisaAd` = okulun kısa adı, şeritte "<ad>  ·  değiştir", arama kutusu ve sonuçlar gizlenir. Arama hatası
  sessizce yok sayılır.
- `gir()` (iç):
  1. Kutuların hata yazıları silinir. Kimlik boşsa "Kullanıcı adını ya da e-posta adresini yaz.", şifre boşsa "Şifreni yaz."
     (ilgili kutunun altında) ve dur.
  2. Gövde: `{ kimlik: <kırpılmış>, password: <olduğu gibi>, uygulama: true }`; okul seçiliyse `okul: <kısa ad>`; doğrulama
     sorusu görünürse `challengeId`, `challengeAnswer` ([DogrulamaSorusu.md](DogrulamaSorusu.md) `ekle`).
  3. Düğme meşgul, yazısı "Giriş yapılıyor..."; `POST /api/login`.
  4. Başarı (düğme eski hâline döner):
     - `twoFactor` → `e.git(new KodSayfasi(challengeId, mesaj))` (ör. uydurma `ornek@eposta.com` için "Giriş kodu
       o***k@eposta.com adresine gönderildi.");
     - `token` varsa → `e.oturumAc(cevap)`;
     - ikisi de yoksa hiçbir şey olmaz.
  5. Hata (düğme eski hâline döner; sunucunun hata gövdesi `h.govde`):
     - `okulSec` → okul kutusu açılır;
     - `soruGerekli` → doğrulama sorusu gösterilir ve yeni soru istenir;
     - ileti `alan`'a göre yazılır: `sifre` → şifre kutusunun altına, `bot` → sorunun cevap kutusunun altına, `kimlik` → kimlik
       kutusunun altına; başka her durumda (kilit, onay bekleyen hesap, ağ yok…) alttaki kısa mesajla.

## Kimle konuşur?

- Aynı paketten çağırdıkları:
  - [Arayuz.md](Arayuz.md) — `sayfaGovdesi`, `kart`, `altBaslik`, `baslik`, `yazi`, `soluk`, `dikey`, `alan`, `dugme`, `mesgul`,
    `serit` (`MAVI`), `tiklananSatir`, `ikon`, `ekle`; `Tema.java` — `dp`, `ORTA`; `R.drawable.simge_marka`, `ik_okul`.
  - [DogrulamaSorusu.md](DogrulamaSorusu.md) — `goster`, `gorunur`, `ekle`, `getir`, `hata`.
  - [Ag.md](Ag.md) — `get` (okul arama), `post` (giriş). Oturum yokken istekler `Authorization` başlığı olmadan gider.
  - Sayfalar: `KodSayfasi`, `SifremiUnuttumSayfasi`, `KayitSayfasi`; [AnaEkran.md](AnaEkran.md) — `git`, `oturumAc`, `bildir`.
  - Android: `TextWatcher` (yazı değişimi), `EditorInfo.IME_ACTION_DONE`, `PasswordTransformationMethod`,
    `HideReturnsTransformationMethod`, `URLEncoder`.
- Sunucu uçları (site deposunda `sunucu/bolumler/kayit.md`):
  - **`GET /api/okul-adres/ara?q=`** (herkes) — IP başına dakikada 3000 istek; sadeleştirilmiş sorgu 2 harften kısaysa boş liste;
    yoksa yalnız onaylı ve adresi olan okullarda bulanık arama (ad, il, ilçe, kısa ad; Türkçe harf ve yazım hatası fark etmez),
    en çok 20: `{ yakin, duzeltme, okullar: [{ ad, il, ilce, kisaAd, vurgu }] }`. Uygulama yalnız `okullar`'ın ilk 6'sını
    kullanır.
  - **`POST /api/login`** (herkes) — gövde `{ kimlik (ya da eski ad email), password, okul?, challengeId?, challengeAnswer?,
    uygulama? }`. Cevaplar:
    - `400 { alan: 'kimlik' }` boş kimlik ya da "Bu okul adresi bulunamadı. Ana sayfadan okulunu seç."; `400 { alan: 'sifre' }`
      boş şifre;
    - `400 { alan: 'kimlik', okulSec: true }` "Bu kullanıcı adı birden çok okulda var. Önce okulunu seç, sonra giriş yap.";
    - `400 { alan: 'bot', soruGerekli: true }` "Doğrulama sorusunun cevabı yanlış." / "Devam etmek için doğrulama sorusunu
      cevapla.";
    - `401 { alan: 'kimlik', hesapYok: true, soruGerekli: true }` hesap yok; `401 { alan: 'sifre', kalanHak, soruGerekli:
      true }` "Şifre yanlış." (+ "N deneme hakkın kaldı." ya da "Giriş 15 dakika kilitlendi.");
    - `429` bağlantı engeli ya da `{ kilitli: true }` hesap kilidi ("… sonra tekrar dene ya da "Şifremi unuttum" ile yeni şifre
      al.");
    - `403` kapatılmış hesap ya da `{ bekliyor: true }` onay bekleyen hesap;
    - öğrenci ya da e-postasız hesap: doğrudan oturum (`token`, `user`, `children`, `portallar`…); öteki hesaplar:
      `{ twoFactor: true, challengeId, maskeliEposta, yontem, mesaj }` → ikinci adım `POST /api/login/dogrula` (`KodSayfasi`).
    - Okul seçiliyse kullanıcı adı önce o okulun hesaplarında, bulunamazsa 11 haneli T.C. kimlik no olarak aranır; okulda
      yoksa okuldan bağımsız yetişkin hesabı alınır, okul hesabının şifresi tutmazsa aynı adlı yetişkin hesabında da denenir.
      E-postayla girişte okul önemsizdir (`sunucu/veri/depo/kullanicilar.js` `girisKimligiyle`, `sunucu/bolumler/kayit.js`).
    - `uygulama: true` → oturum 30 gün geçerli (tarayıcıda 7 gün); bayrak kod doğrulama adımına da taşınır.
- Onu açan: [AnaEkran.md](AnaEkran.md) — `girisGoster()` (`tekSayfa(new GirisSayfasi())`): uygulama oturumsuz açılınca,
  çıkışta ve oturum düşünce (401). `sifreGoster`'i `KayitSayfasi.java` ve `SifreSayfasi.java` da çağırır (grep).

## Nasıl çalışır (adım adım)?

### Veli, öğretmen, müdür (e-postalı yetişkin hesabı)

```
kimlik: ornek@eposta.com  şifre: ••••••  → "Giriş yap"
   POST /api/login { kimlik, password, uygulama: true }
      ─► { twoFactor: true, challengeId, mesaj: "Giriş kodu … adresine gönderildi." }
      ─► e.git(KodSayfasi(challengeId, mesaj)) → 6 haneli kod → POST /api/login/dogrula → oturumAc
```

### Öğrenci (okul seçerek)

```
"Öğrenci ya da servisçiysen okulunu seç" → "Okulun" kutusu
   "atatürk ortao" yaz → 350 ms → GET /api/okul-adres/ara?q=atatürk%20ortao
       [okul] Atatürk Ortaokulu          Çankaya / Ankara
       [okul] Atatürk Ortaokulu          Seyhan / Adana     … (en çok 6)
   satıra dokun → şerit "Atatürk Ortaokulu  ·  değiştir", okulKisaAd = "<kısa ad>"
kullanıcı adı + şifre → POST /api/login { kimlik, password, uygulama: true, okul: "<kısa ad>" }
   ─► { token, user, … } → e.oturumAc(cevap) → (onay / zorunlu şifre) → sekmeler
```

(Okul adları örnektir.)

### Hatalar

```
boş kutu                         → kutunun altında "…yaz." (istek gitmez)
400 okulSec                      → okul kutusu açılır + kimlik kutusunda ileti
401 alan=sifre, soruGerekli      → şifre kutusunda "Şifre yanlış. 3 deneme hakkın kaldı." + doğrulama sorusu belirir
400 alan=bot                     → sorunun altında "Doğrulama sorusunun cevabı yanlış." + yeni soru
429 kilitli / 403 / ağ yok       → altta 3,8 sn görünen kısa mesaj
```

## Dikkat!

- **Okul aramasında yarış.** Her arama ayrı istektir ve cevaplar sırayla gelmek zorunda değildir; eski bir aramanın cevabı yeni
  olanınkinden sonra gelirse liste eski sonuçlarla kalır. Okul seçildikten sonra gelen geç bir cevap da sonuç listesini şeridin
  altında yeniden doldurur (seçim bozulmaz; kod okumasına göre).
- **Arama hatası sessiz.** İnternet yoksa ya da sunucu 429 derse liste boş kalır, ileti çıkmaz.
- **Seçim kaldırılınca arama yenilenmez.** Şeride dokunup seçimi kaldırınca arama kutusu eski yazısıyla geri gelir ama sonuçlar
  ancak yazı değişince yeniden aranır.
- **Okul kutusu kapanmaz.** `okulKutusunuAc(false)`'ı çağıran yok; veli kutuyu yanlışlıkla açarsa boş bırakması yeter (okul
  seçilmedikçe gövdeye `okul` girmez).
- **Seçili okul her denemede gider.** Okul seçiliyken e-postayla giriş de çalışır (sunucu e-postada okula bakmaz) ama okulun kısa
  adı yine denetlenir.
- **Önemli iletiler kısa mesajla geçip gidiyor.** Hesap kilidi ("… sonra tekrar dene ya da "Şifremi unuttum" ile yeni şifre
  al."), onay bekleyen ya da kapatılmış hesap iletileri `alan` taşımadığı için 3,8 saniyelik alt mesajla görünür.
- **Ne kod ne oturum gelirse sessizlik.** Başarılı ama ikisi de olmayan bir cevapta (ör. JSON olmayan bir 200 cevabı; [Api.md](Api.md))
  düğme eski hâline döner, ileti çıkmaz.
- **401 burada "oturum düştü" sayılmaz.** Telefonda oturum olmadığı için [Ag.md](Ag.md) "Şifre yanlış." ve "hesap yok" 401'lerini
  ekranın kendisine verir; genel karşılayıcı devreye girmez.
- **Kimlik kırpılır, şifre kırpılmaz.** Şifredeki baştaki/sondaki boşluklar şifrenin parçası sayılır.
- **Soru ilk seferde iki kez istenir** ve eski soru hatası ekranda kalabilir ([DogrulamaSorusu.md](DogrulamaSorusu.md)).
- **Geri tuşu uygulamayı kapatır.** Giriş ekranı giriş yığınının köküdür; [AnaEkran.md](AnaEkran.md) `geri()` burada
  `finish()` yapar. Planlı "Android geri tuşu" işi girişte geri tuşunun hiçbir şey yapmamasını istiyor.
- **Alttaki gizlilik notu bir söz.** "Bilgilerin yalnızca Eğitim Evi sunucusunda tutulur; reklam ya da satış için kimseyle
  paylaşılmaz." cümlesi aydınlatma metniyle tutarlı kalmalı; metin değişirse (KVKK kuralı) burası da gözden geçirilmeli.
- Arama sonucu yalnız ilk 6 okulu gösterir (sunucu 20'ye kadar döner); "Bunu mu demek istedin?" gibi `yakin`/`duzeltme`
  bilgileri kullanılmıyor.

## Testleri

- Android deposunda otomatik test yok; derleme ve lint (`./gradlew --offline assembleDebug lintDebug`) ana oturumda.
- Sunucu sözleşmesini site deposundaki testler korur:
  - `testler/test-giris-kayit.js` — giriş hatalarının `alan` bayrakları ve iletileri (boş kimlik → `alan: 'kimlik'`; olmayan
    e-posta ya da kullanıcı adı → `hesapYok`; kalan hak 4, 3…; beşinci yanlışta kilit, kilitliyken doğru şifre de 429 `kilitli`).
    Aynı dosyadaki "yanlış doğrulama → `alan: 'bot'`" denetimi KAYIT formu içindir, girişin değil.
  - `testler/guvenlik-test.js` — girişteki doğrulama sorusu: temiz kişi sorusuz girer; hatalı denemeden sonra cevap
    `soruGerekli: true` taşır, sorusuz giriş reddedilir, yanlış cevap reddedilir, doğru cevapla giriş sürer; ayrıca kaba kuvvet
    kilidi.
  - `testler/test-cakisma.js` ve `testler/test-yonetim.js` — aynı kullanıcı adı birden çok okulda → `okulSec`; okul verilince doğru
    hesap; `test-yonetim.js` ayrıca olmayan okul adresinin reddi, okul seçiliyken T.C. no ile giriş ve `okul-adres/ara`
    (büyük/küçük harf ve yazım farkıyla arama).
  - `testler/test-servis-yoklama.js` "10) UYGULAMA OTURUMU (30 GÜN)" — `uygulama: true` girişte oturum 30 gün, kod doğrulama
    adımında da.
  - `testler/test-okul-agi.js` — bir okulun yüzlerce öğrencisinin tek IP'den `okul-adres`, `login` istekleri (her onuncusu önce
    yanlış şifre yazar, soruyu çözüp girer).
- Elle (öykünücü, deneme paketi, deneme sunucusu `http://10.0.2.2:3200`; hesaplar `testler/seed.js`'teki deneme hesapları):
  - Kutuları boş bırakıp "Giriş yap" → kimlik kutusunun altında "Kullanıcı adını ya da e-posta adresini yaz.".
  - Bir öğrenciyle okul seçmeden kullanıcı adıyla gir → tek okulda varsa doğrudan girer; "okulunu seç"ten okulu ara, seç, gir.
  - Bir öğretmenin e-postasıyla gir → "Giriş kodu" ekranı; kod deneme sunucusunda e-posta yerine sunucu günlüğüne yazılır.
  - Şifreyi bir kez yanlış yaz → şifre kutusunda "Şifre yanlış." ve formda doğrulama sorusu belirir (sunucu o hesaba o
    bağlantıdan bir hatadan sonra soru ister); cevapsız yeniden dene → sorunun altında "Doğrulama sorusunun cevabı yanlış." ve
    yeni soru; doğru şifre + doğru cevapla gir.
  - Uçak kipinde "Giriş yap" → altta "İnternet bağlantısı yok ya da Eğitim Evi'ne ulaşılamadı.".

## Son durum

- `git log`: 3 commit (Android deposu). Son değişiklik `cf2af61 commit 11` (2026-09-27, logo): markadaki kırmızı yuvarlak köşeli
  kare içindeki okul simgesi yerine başlatıcı simgesiyle aynı çizim (`R.drawable.simge_marka`) geldi; görüntü ekran okuyucudan
  gizlendi (yorumu: altındaki "Eğitim Evi" yazısı okunur). Aynı commit `simge_marka.xml`'i ekledi ve başlatıcı/bildirim
  simgelerini yeniledi.
- Ondan önce `822d445 commit 7` (2026-09-26): giriş gövdesine `uygulama: true` eklendi (sunucunun 30 günlük uygulama oturumu).
  Dosyanın ilk hâli `e96c5f2 commit 6` (2026-09-26): bugünkü form, okul arama, doğrulama sorusu, `sifreGoster`.
- Bilinen açıklar (kod değiştirilmedi): okul aramasındaki yarış ve sessiz hata, kapanmayan okul kutusu, önemli iletilerin kısa
  mesajla geçmesi, girişte geri tuşunun uygulamayı kapatması.
- Planlı işlerden bu dosyaya dokunması beklenenler:
  - "Android yerel uygulama" — açılış ekranında "Giriş yap / Kayıt ol" yerine "Hesaba gir →" ve "Doğrulayıcı" düğmeleri; giriş
    yapmadan da açılan sol menü (ana sayfa, yorumlar, SSS, KVKK… ve altta "Hesaba gir →").
  - "Tek kişi tek hesap + portallar öğrencide de" — kullanıcı adı site genelinde TEK olacak; giriş "kullanıcı adı / e-posta /
    T.C." ile, "Okul seç" ise o okulun sayfasına götüren aranabilir liste olacak. Bugünkü "okulunu seç → `okul` gövdeye" düzeni
    ve `okulSec` cevabı bu işle değişir.
  - "Android geri tuşu" — giriş ekranında geri hiçbir şey yapmamalı.
  - "Sistem" (yöneticiye ve desteğe ZORUNLU doğrulama uygulaması, yeni cihaz uyarısı) ve "Özel roller" (ortak bilgisayarda giriş)
    — iki adımlı girişin ikinci adımı ve giriş cevapları değişebilir.
  - "Çok dil" — sabit metinler ve sunucu iletilerinin dili.

# app/src/main/java/org/egitimevi/aile/KayitSayfasi.java

"Hesap aç" sayfası: veli, öğretmen ve müdürün aynı yetişkin hesabını açtığı form (ad, kullanıcı adı, e-posta, telefon,
şifre, isteğe bağlı T.C. no, aydınlatma onayı, doğrulama sorusu); hesap e-postadaki bağlantıya 24 saat içinde
tıklanınca açılır. Ayrıca site sayfalarını tarayıcıda açan `siteAc` yardımcısı burada.

## Bu dosya ne yapar?

Eğitim Evi'nde kendisi kaydolan tek tür hesap **yetişkin hesabıdır**: veli, öğretmen ve müdür aynı formu doldurur. Kayıt
olan kişinin henüz rolü yoktur; rolleri girişten sonra "+ Ekle" ile gelir — çocuğunun veli kodunu giren veli olur, kişi
kodunu okulunun müdürüne veren öğretmen olarak eklenir, kişi kodunu sistem yöneticisine veren okulunu açtırıp müdür olur
([EkleSayfasi.md](EkleSayfasi.md)). Öğrenci ve servisçi kaydolmaz; hesaplarını okul açar.

Form gönderilince hesap HEMEN açılmaz: sunucu bilgileri bekletir ve e-posta adresine bir onay bağlantısı yollar. Kişi
24 saat içinde bağlantıya tıklayınca hesap açılır (böylece kimse sahibi olmadığı bir adresle hesap açamaz). Sayfa başarıda
formu kaldırıp "E-postana bak" kartını gösterir; kişi bağlantıyı (tarayıcıda) açtıktan sonra uygulamaya dönüp giriş
yapar.

Sayfa giriş akışının parçasıdır: [GirisSayfasi.md](GirisSayfasi.md)'deki "Hesabın yok mu? → Hesap aç" ile açılır, üst ve
alt çubuk görünmez. Hata hangi kutudaysa (sunucu `alan` ile söyler) ileti o kutunun altına kırmızı yazılır.

## İçinde neler var?

### Alanlar

`ad`, `kullanici`, `eposta`, `sifre`, `telefon`, `tc` ([Arayuz.md](Arayuz.md)'deki `Alan`), `onay` (onay kutusu), `soru`
([DogrulamaSorusu.md](DogrulamaSorusu.md)), `gonder` ("Hesabımı aç" düğmesi), `govde` (sayfanın dikey gövdesi; başarıda
boşaltılır).

### Dışa açık

- `baslik()` — "Hesap aç"; `cubuksuz()` — `true`.
- `olustur()` — sayfa, yukarıdan aşağı:
  - "← Girişe dön" (hayalet düğme) → `e.kapat()`.
  - Başlık "Hesap aç" ve açıklama: "Herkes aynı hesabı açar: veli, öğretmen ya da müdür. Girişten sonra sağ üstteki + Ekle
    ile çocuğunu eklersin, okuluna öğretmen olarak katılırsın ya da okulunu açtırırsın." (bu cümledeki yer için
    "Dikkat!"e bak).
  - Kart içinde kutular (etiket — ipucu — klavye türü):
    | Kutu | İpucu | Tür |
    |---|---|---|
    | "Adın ve soyadın" | Ayşe Yılmaz | kişi adı, her sözcük büyük harfle başlar |
    | "Kullanıcı adı" | ayse.yilmaz | metin, öneri yok |
    | "E-posta" | ornek@eposta.com | e-posta |
    | "Telefon (ülke koduyla)" | +90 532 123 45 67 | telefon; kutu `+90 ` yazılı gelir |
    | "Şifre" | — | gizli; altında "Şifreyi göster" (`GirisSayfasi.sifreGoster`) ve soluk kural yazısı "En az 8 karakter; büyük harf, küçük harf, rakam ve özel karakter (! ? . * gibi)." |
    | "T.C. kimlik no (isteğe bağlı)" | — | yalnız rakam |
  - Onay kutusu: "Aydınlatma metnini okudum; kişisel verilerimin bu metne göre işlenmesini kabul ediyorum." ve altında
    "Aydınlatma metnini oku" (hayalet düğme) → `KvkkSayfasi.metniAc` (metin tarayıcıda açılır; [KvkkSayfasi.md](KvkkSayfasi.md)).
  - Doğrulama sorusu (`soru.goster(true)`: görünür ve hemen bir soru ister — "Doğrulama: 4 + 7 = ?", "Cevap", "Başka
    soru").
  - "Hesabımı aç" (birincil).

### İç

- `gonder()`:
  1. Bütün kutuların hatalarını siler.
  2. Onay işaretli değilse alt mesaj "Devam etmek için aydınlatma metnini onayla." ve durur. Başka hiçbir alanı telefonda
     denetlemez; kuralların hepsi sunucuda.
  3. Gövde: `{ fullName, username, email, phone, password, tc, kvkkOnay: true, challengeId, challengeAnswer }` — metinler
     kırpılmış (`deger()`), şifre olduğu gibi (`hamDeger()`); doğrulama cevabı 1–3 rakamsa sayı, değilse -1
     ([DogrulamaSorusu.md](DogrulamaSorusu.md)).
  4. Düğme meşgul; `POST /api/register`.
  5. Başarı → gövde boşaltılır; yerine posta simgeli boş durum kartı: başlık "E-postana bak", açıklama sunucunun
     `message`'ı ("a***e@… adresine bir bağlantı gönderdik. Hesabını açmak için 24 saat içinde ona tıkla. Posta gelmediyse
     gereksiz klasörüne bak."; gelmezse benzer yedek cümle) ve "Girişe dön" (birincil) → `e.kapat()`.
  6. Hata → düğme açılır; sunucu cevabındaki `alan`'a göre ileti: `ad` → ad kutusu, `kullaniciAdi` → kullanıcı adı,
     `email` → e-posta, `telefon` → telefon, `sifre` → şifre, `tc` → T.C. kutusu; `bot` → doğrulama cevabının altına ve
     YENİ soru istenir (`soru.getir()`, cevap kutusu boşalır); başka her şey (alan yoksa ya da `kvkk`) → alt mesaj.
- `siteAc(e, yol)` (paket içi, `static`) — `Ayarlar.sunucu(e) + yol` adresini telefonun tarayıcısında açar
  (`Intent.ACTION_VIEW`); açacak uygulama yoksa alt mesaj "Bağlantıyı açacak tarayıcı yok.". Kayıtla ilgisi yok; uygulamanın
  bütün "sitede aç" bağlantıları bunu kullanır (aşağıda).

## Kimle konuşur?

- Çağırdıkları:
  - [Ag.md](Ag.md) — `Ag.post(e, "/api/register", …)` (oturumsuz istek).
  - [DogrulamaSorusu.md](DogrulamaSorusu.md) — `goster(true)`, `ekle(gövde)`, `hata`, `getir` (`GET /api/challenge`).
  - [GirisSayfasi.md](GirisSayfasi.md) — `sifreGoster` (şifre kutusunun altındaki "Şifreyi göster/gizle").
  - [KvkkSayfasi.md](KvkkSayfasi.md) — `metniAc` (aydınlatma metni).
  - [Arayuz.md](Arayuz.md) — `sayfaGovdesi`, `dugme`, `baslik`, `yazi`, `kart`, `alan`, `soluk`, `ekle`, `mesgul`,
    `bosDurum`; `Tema.java` — `dp`, `renk`; [AnaEkran.md](AnaEkran.md) — `kapat`, `bildir`, `startActivity`;
    [Ayarlar.md](Ayarlar.md) — `sunucu` (`siteAc` için).
- Onu kullananlar (grep):
  - [GirisSayfasi.md](GirisSayfasi.md) — "Hesap aç" → `e.git(new KayitSayfasi())`.
  - `siteAc`: [KvkkSayfasi.md](KvkkSayfasi.md) (`/kvkk/kvkk.html`), [AyarlarSayfasi.md](AyarlarSayfasi.md) ("Siteyi aç" →
    `/`, "Sık sorulan sorular" → `/sss/sss.html`).
- Sunucu uçları (site deposunda `sunucu/bolumler/kayit.md`):
  - `POST /api/register` — önce sınırlar: bir bağlantıdan saatte 100 deneme (429 "Çok fazla kayıt denemesi yapıldı. Bir
    saat sonra tekrar dene.") ve saatte en çok 60 hesap (429). Sonra sırayla: doğrulama sorusu (`alan: bot`; yanlış
    cevap soruyu harcamaz), ad ve soyad en az iki sözcük (`ad`), e-posta biçimi / kayıtlı mı / alan adı posta alıyor mu
    (`email`), kullanıcı adı 3–30, harfle başlar, Türkçe harf yok, site genelinde tek (`kullaniciAdi`), şifre yetişkin
    kuralı (`sifre`), telefon ülke koduyla (`telefon`; Türkiye numarası 10 hane), T.C. yazıldıysa algoritma ve tekillik
    (`tc`; çakışmada soru HARCANIR ve cevapta `yeniSoru: true`; aynı bağlantıdan saatte 10 çakışma), `kvkkOnay` tam `true`
    (`kvkk`), aynı adrese saatte en çok 3 posta (`email`). Başarı: `{ onayGerekli: true, eposta: <maskeli>, message }`;
    soru harcanır. Bekleyen kayıt aydınlatma metninin o günkü sürümünü saklar.
  - Bağlantı tıklanınca site `POST /api/eposta-onay` ile hesabı açar (o sırada kullanıcı adı, e-posta ya da T.C. başkasınca
    alındıysa söyler); bu adım uygulamada değil, tarayıcıda olur.
- Rol: henüz hesabı olmayan herkes (veli, öğretmen, müdür adayı).

## Nasıl çalışır (adım adım)?

```
GirisSayfasi "Hesap aç" ─► KayitSayfasi.olustur ─► DogrulamaSorusu: GET /api/challenge → "Doğrulama: 4 + 7 = ?"
kişi formu doldurur, onay kutusunu işaretler ─► "Hesabımı aç"
  onay yok? ─► "Devam etmek için aydınlatma metnini onayla."
  POST /api/register { fullName, username, email, phone, password, tc, kvkkOnay: true, challengeId, challengeAnswer }
    200 ─► [posta simgesi] "E-postana bak" + sunucunun cümlesi + "Girişe dön"
    400 { alan: "kullaniciAdi", error: "Bu kullanıcı adı alınmış. Başka bir ad dene." } ─► kutunun altında kırmızı
    400 { alan: "bot" } ─► cevabın altında ileti + yeni soru
    429 ─► alt mesaj
e-posta ─► bağlantı (tarayıcıda) ─► hesap açılır ─► uygulamaya dön, giriş yap
```

## Dikkat!

- **T.C. çakışmasında yeni soru istenmiyor.** Yazılan T.C. no başka bir hesapta kayıtlıysa sunucu, numara denemesi
  yapılamasın diye doğrulama sorusunu HARCAR ve cevaba `yeniSoru: true` koyar. Sitedeki form (`05-giris.js`) bunu görünce
  yeni soru ister; bu sayfa yalnız `alan`'a bakıp iletiyi T.C. kutusuna yazar, soruyu yenilemez. Kişi T.C.'yi düzeltip
  (ya da silip) yeniden gönderince önce "Doğrulama sorusunun cevabı yanlış." alır, ancak o zaman yeni soru gelir: iki kez
  göndermek zorunda kalır. (Site deposundaki `testler/test-giris-kayit.js` sunucunun bu davranışını deniyor: "T.C.
  çakışmasında soru harcanıyor", "harcanan soruyla ikinci deneme olmuyor".) Düzeltme önerisi: `h.govde.optBoolean
  ("yeniSoru")` ise `soru.getir()`.
- **T.C. "isteğe bağlı" yazıyor, aydınlatma metni "zorunlu" diyor.** Site deposundaki aydınlatma metni (sürüm 1.15'ten
  beri) T.C. kimlik numarasının bütün hesaplarda zorunlu olduğunu söylüyor; ama hem sunucu hem bu form (ve sitedeki form)
  bugün numarayı isteğe bağlı alıyor. Kod değişikliği planlı (aşağıda "Son durum"). Kutu ayrıca 11 haneyle sınırlı değil
  (sitedeki kutuda `maxlength="11"` var); fazla hane sunucuda "11 haneli olmalı…" iletisiyle döner.
- **Kullanım koşulları onayda yok.** Sitedeki kayıt formunun onay kutusu "…işlenmesini kabul ediyorum. Kullanım
  koşullarını kabul ediyorum." der ve iki metne de bağlantı verir; buradaki kutu yalnız aydınlatma metnini anar, kullanım
  koşullarına (`/kosullar/kosullar.html`) bağlantı da yok. Sunucu yalnız `kvkkOnay`'a bakıyor, yani hesap yine açılır;
  ama uygulamadan kaydolan kişi kullanım koşullarını ne görmüş ne de kabul etmiş olur. "KVKK ve onay metinleri TAM
  denetimi"nde ele alınmalı (kod değiştirilmedi).
- **"Sağ üstteki + Ekle" uygulamada yanlış yer.** Açıklama cümlesi sitedeki düzeni anlatıyor. Uygulamada "+ Ekle", portalı
  olmayan yeni hesabın ana sayfasında büyük bir düğme ([AnaSayfa.md](AnaSayfa.md)) ve sol üstteki avatarın açtığı
  portal penceresinde, "Portalların" listesinin altında (`PortalSecici.java`); Ayarlar'da da "Portallarım"ın altında
  "Ekle" satırı var.
- **Telefon kutusu `+90 ` ile gelir.** Kişi dokunmazsa sunucu "Telefon numarasını ülke koduyla yaz (ör. +90 532 123 45
  67)" der (yalnız `+90` geçerli bir numara değil). Sunucu `0532…` gibi ülke kodsuz Türkiye numarasını da kabul eder.
- **Başka denetim yok.** Telefonda yalnız onay kutusuna bakılır; boş ad, kısa şifre vb. sunucuya gider ve sunucunun
  cevabıyla kutuya yazılır. İletiler bu yüzden sitedekiyle aynıdır.
- **Hesap e-postayla açılır, uygulamayla değil.** Onay bağlantısı sitenin adresidir; uygulamanın bağlantı açma kaydı yok,
  bu yüzden tarayıcıda açılır. Kişi sonra uygulamaya dönüp giriş yapar; başarı kartındaki "Girişe dön" bunun için.
- **Adres alanı yok.** Sitedeki formda isteğe bağlı "Adres" var; burada gönderilmiyor (sunucu boş sayar).
- **`siteAc` aile ayar dosyasındaki adrese gider.** Adres `Ayarlar.sunucu`'dan (varsayılan `https://egitimevi.org`);
  telefon Aile'de başka bir sunucuya bağlandıysa site bağlantıları da oraya gider ([Ag.md](Ag.md) "Dikkat!"). Yardımcının
  bu sınıfta durması tarihsel; kayıtla ilgisi yok.
- Örnek ad ("Ayşe Yılmaz", "ayse.yilmaz", "ornek@eposta.com") ipucu metnidir, gerçek kişi değil.

## Testleri

- Android deposunda otomatik test yok; derleme ve lint (`./gradlew --offline assembleDebug lintDebug`) ana oturumda.
- Sunucu sözleşmesini site deposundaki testler korur:
  - `testler/test-giris-kayit.js` — "KAYIT HATALARI ALANIYLA": bu sayfanın baktığı her `alan` (`ad`, `kullaniciAdi`,
    `email`, `sifre`, `telefon`, `tc`, `kvkk`, `bot`); doğrulama sorusunun yalnız hesap açılınca harcanması; T.C.
    çakışmasında `yeniSoru: true` ve sorunun harcanması; kayıtta rol gönderilse de hesabın rolsüz açılması.
  - `testler/guvenlik-test.js` — aynı doğrulama sorusunun ikinci kayıtta kullanılamaması.
  - `testler/test-cakisma.js` — kayıt ve e-posta onayı yolunda aynı e-posta, kullanıcı adı ve T.C. (büyük/küçük harf,
    görünmez karakter, tam genişlikli harf farkıyla bile) ikinci hesap açtırmaz; aynı anda gelen kayıtlarda kaybeden açık
    bir ileti ve doğru `alan` alır.
  - `testler/test-yetiskin.js` — yetişkin hesabının kaydolması, iki adımlı girişi ve kişi koduyla okul rolü alması.
- Elle (öykünücü, deneme paketi; uygulamanın sunucu adresi deneme sunucusuna çevrilmiş olmalı, bkz. [Ag.md](Ag.md)):
  - "Hesap aç" → onay kutusu boşken "Hesabımı aç" → "Devam etmek için aydınlatma metnini onayla.".
  - Tek sözcüklü ad → ad kutusunda "Adını ve soyadını birlikte yaz."; Türkçe harfli kullanıcı adı → kullanıcı adı
    kutusunda ileti; yanlış doğrulama cevabı → cevabın altında ileti ve yeni soru.
  - Kayıtlı bir T.C. yaz → T.C. kutusunda ileti; T.C.'yi silip yeniden gönder → önce "Doğrulama sorusunun cevabı yanlış."
    (yukarıdaki ilk madde).
  - Doğru form → "E-postana bak"; sunucu penceresindeki (ya da e-postadaki) bağlantıyı tarayıcıda aç → hesap açılmalı →
    "Girişe dön" → giriş.
  - "Aydınlatma metnini oku" → tarayıcıda `/kvkk/kvkk.html`.

## Son durum

- `git log` (Android deposu): 1 commit. Dosya `e96c5f2 commit 6` (2026-09-26, yerel uygulamanın çekirdeği: WebView'den
  kendi çizdiği sayfalara geçiş) ile geldi; o günden beri değişmedi.
- Bilinen açıklar (kod değiştirilmedi): T.C. çakışmasında `yeniSoru`'nun karşılanmaması; onayda kullanım koşullarının
  olmaması; "sağ üstteki + Ekle" cümlesi; T.C. kutusunun 11 haneyle sınırlı olmaması.
- Planlı işlerden bu dosyaya dokunması beklenenler:
  - "T.C. KİMLİK NO BÜTÜN HESAPLARDA ZORUNLU" (Linux'ta yapılacak): tanım bu dosyadaki "(isteğe bağlı)" kutusunu adıyla
    anıyor — alan zorunlu olacak, not metni aydınlatma metnine uyacak ("Hesapların doğru eşleşmesi için gerekli; okulla
    paylaşılmaz"), sunucuyla aynı algoritma ve iletilerle Java'da denetim ve birim testi; yazarken canlı denetim.
  - "KVKK ve onay metinleri TAM denetimi": onay kutusu metni ve kullanım koşulları.
  - "Android yerel uygulama"nın doğrulayıcı ve sol menü ekleri: açılış ekranı "Hesaba gir →" / "Doğrulayıcı" ve giriş
    yapmadan açılan sol menü; kayda giden yol değişebilir.
  - "Çok dil": sayfadaki sabit Türkçe metinler (hukuki metinler Türkçe kalır).

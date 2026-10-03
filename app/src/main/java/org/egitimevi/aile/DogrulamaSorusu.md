# app/src/main/java/org/egitimevi/aile/DogrulamaSorusu.java

Formlara gömülen "doğrulama sorusu" parçası ("Doğrulama: 4 + 7 = ?", "Başka soru", rakamla cevap kutusu): soruyu
`GET /api/challenge`'tan alır ve gönderilecek gövdeye `challengeId` ile `challengeAnswer`'ı ekler.

## Bu dosya ne yapar?

Sitedeki kayıt, şifremi unuttum ve (hatalı denemeden sonra) giriş formlarında küçük bir toplama sorusu vardır: basit
otomatik araçları (botları) elemek için. Sunucu her soruya bir kimlik verir; form gönderilirken kimlik ve cevap birlikte gider.
Bu sınıf aynı parçayı uygulamanın formları için hazırlar. Bir sayfa (`Sayfa`) değildir; sayfaların kendi formlarına koyduğu bir
bileşendir (paket içi, `final`).

Üç sayfa kullanır:

| Sayfa | Ne zaman görünür |
|---|---|
| `KayitSayfasi.java` (Hesap aç) | her zaman (`goster(true)`) |
| `SifremiUnuttumSayfasi.java` | her zaman (`goster(true)`) |
| [GirisSayfasi.md](GirisSayfasi.md) | başta gizli; sunucu `soruGerekli` derse görünür |

Girişte soru normal kullanıcıya hiç çıkmaz: sunucu yalnız o hesaba o bağlantıdan bir hatalı deneme olduysa, hesaba her
yerden 3 hata geldiyse ya da bağlantıdan 15 dakikada 50 hata geldiyse ister (site deposunda `sunucu/bolumler/kayit.md`,
`belge/KILAVUZ.md` "Akıllı doğrulama sorusu"). Soru basit araçları eler; asıl koruma hesap kilitleri ve bağlantının hata
sınırıdır.

"Çocuğun telefonu" ekranı ([AileEkrani.md](AileEkrani.md)) bu sınıfı KULLANMAZ; kendi soru kodu vardır.

## İçinde neler var?

### Alanlar

- `kok` (paket içi, `final`) — parçanın kök görünümü (`LinearLayout`, dikey); sayfa bunu formuna ekler.
- `e` — ekran ([AnaEkran.md](AnaEkran.md)); `soru` — soru yazısı; `cevap` — `Arayuz.Alan` (etiket + kutu + hata yazısı).
- `id` — o an gösterilen sorunun kimliği; başta boş metin.

### Yapıcı

- `DogrulamaSorusu(AnaEkran e)` — görünümü kurar, istek ATMAZ:
  - üstte yatay satır: soru yazısı "Doğrulama sorusu yükleniyor..." (15 sp, `Tema.ORTA`, kalan genişlik) ve sağda "Başka soru"
    düğmesi (`HAYALET`, en az 40 dp; dokununca `getir()`);
  - altında 6 dp boşlukla "Cevap" kutusu: ipucu "Sayıyla yaz", klavye yalnız rakam (`InputType.TYPE_CLASS_NUMBER`).

### İşlevler

- `getir()` — cevap kutusunu boşaltır, `GET /api/challenge` ister. Başarıda `id` = cevabın `id`'si, yazı "Doğrulama: " +
  cevabın `soru`'su (ör. "Doğrulama: 4 + 7 = ?"). Hatada yazı "Soru alınamadı: <ileti>" olur; `id` DEĞİŞMEZ.
- `goster(boolean g)` — parçayı gösterir ya da gizler (`VISIBLE`/`GONE`); gösterilirken henüz soru yoksa (`id` boş) `getir()`.
- `gorunur()` — parça görünür mü (giriş sayfası gövdeye soruyu yalnız görünürken ekler).
- `hata(String s)` — cevap kutusunun altına kırmızı hata yazısı ve kırmızı çerçeve (`Arayuz.Alan.hataGoster`).
- `ekle(JSONObject g)` — gönderilecek gövdeye `challengeId` (= `id`) ve `challengeAnswer` ekler: cevap 1–3 rakamsa sayı
  olarak, değilse (boş, 4+ hane) `-1`. `JSONException` fırlatabilir (çağıranlar `try` içinde çağırır).

## Kimle konuşur?

- Aynı paketten çağırdıkları: [Ag.md](Ag.md) — `get`; [Arayuz.md](Arayuz.md) — `dikey`, `yatay`, `yazi`, `dugme`
  (`HAYALET`), `alan`, `ekle`; `Tema.java` — `ORTA`, `dp`; `R.color.yazi`.
- Sunucu (site deposunda):
  - **`GET /api/challenge`** (`sunucu/bolumler/kayit.md`; herkes, oturumsuz) — IP başına 10 dakikada 1500 istek (aşılırsa 429
    "Çok fazla istek. Biraz bekle."); cevap `{ id, soru }`. Soruyu `sunucu/guvenlik.md`'deki `botSoruUret` üretir: iki küçük
    sayının toplamı ("a + b = ?"; cevap 5 ile 18 arası), kimlik 24 onaltılık hane, **5 dakika** geçerli; cevabın kendisi
    istemciye gitmez.
  - Cevabı denetleyen uçlar (`botCevapDogru`): `POST /api/login` (yanlış ya da eksikse `400 { alan: 'bot', soruGerekli: true }`
    "Doğrulama sorusunun cevabı yanlış." ya da hiç soru gönderilmediyse "Devam etmek için doğrulama sorusunu cevapla."; doğru
    cevap soruyu harcar), `POST /api/register` (`alan: 'bot'`; soru yalnız hesap açılınca harcanır, başka alan hatalıysa aynı soru
    geçerli kalır), `POST /api/sifre-unuttum` (soru yanlışsa 400). Her soru tek kullanımlıktır.
- Onu kullananlar (grep): [GirisSayfasi.md](GirisSayfasi.md), `KayitSayfasi.java`, `SifremiUnuttumSayfasi.java` — üçü de
  `new DogrulamaSorusu(e)`, `goster`, `ekle`, `hata` ve `getir` çağırır; `gorunur`'u yalnız giriş sayfası kullanır.

## Nasıl çalışır (adım adım)?

```
Hesap aç / Şifremi unuttum:  new DogrulamaSorusu(e) → goster(true) → id boş → getir()
Giriş:                       new DogrulamaSorusu(e) → goster(false)          (gizli, istek yok)
                             POST /api/login → { soruGerekli: true } → goster(true) + getir() → soru görünür

getir():  cevap kutusu boşalır
          GET /api/challenge → { id: "…24 onaltılık…", soru: "4 + 7 = ?" } → id saklanır, "Doğrulama: 4 + 7 = ?"
          ulaşılamadı        → "Soru alınamadı: İnternet bağlantısı yok ya da Eğitim Evi'ne ulaşılamadı."

Gönder:   sayfa → soru.ekle(govde) → { …, challengeId: id, challengeAnswer: 11 }      ("" ya da "1234" → -1)
Sunucu "yanlış" derse: sayfa → soru.hata(ileti) ve yeni soru için soru.getir()
   (giriş: soruGerekli gelince ; hesap aç: alan "bot" ise ; şifremi unuttum: iletide "Doğrulama" geçerse)
```

## Dikkat!

- **Soru tek kullanımlık ve 5 dakika.** Doğru cevap girişte ve şifremi unuttumda soruyu harcar; süresi dolan soru da "yanlış"
  sayılır. Üç sayfa da sunucu yanlış deyince yeni soru ister, ama formu 5 dakikadan uzun açık bırakan kişi önce bir "yanlış"
  uyarısı görür, sonra yeni soruyu cevaplar.
- **Girişte ilk seferde iki istek.** [GirisSayfasi.md](GirisSayfasi.md) `soruGerekli`'de `goster(true)` ve hemen ardından
  `getir()` çağırır; ilk seferde `id` boş olduğu için `goster` da `getir()`'i çağırır → iki `GET /api/challenge`. Cevaplardan
  sonra geleni ekranda kalır; her cevap `id` ile yazıyı birlikte yazdığı için ekrandaki soruyla gönderilen kimlik tutarlıdır.
  Sunucuda boşa bir soru üretilir (5 dakikada düşer). Zararsız ama gereksiz (kod okumasına göre).
- **Eski hata yazısı kalır.** `getir()` cevap kutusunu boşaltır ama altındaki hata yazısını silmez; sayfalar da gönderirken
  sorunun hatasını temizlemez. "Doğrulama sorusunun cevabı yanlış." yeni soru geldikten sonra da, başka bir gönderim onu
  değiştirene kadar görünür kalır (doğru cevapla gönderilip başka bir alan hatalı olsa bile).
- **Soru alınamazsa eski kimlik kalır.** Yazı "Soru alınamadı: …" olur ama `id` önceki sorunun kimliğidir; kişi "Başka soru"ya
  basmadan gönderirse cevap kutusu boş olduğu için `-1` gider ve sunucu "yanlış" der.
- **`-1` hiçbir zaman doğru değildir.** Sunucunun toplamları 5–18 arasıdır; boş ya da çok uzun cevap bu yüzden kesin yanlış
  sayılır (sayfanın ayrıca "cevabı yaz" denetimi yok).
- **İki ayrı kopya.** [AileEkrani.md](AileEkrani.md) aynı soruyu kendi kodu ve kendi arka plan yürütücüsüyle alır; soru düzeni
  değişirse iki yeri de güncelle.
- **Ekran okuyucu.** "Başka soru" `Arayuz.dugme` ile kurulmuş bir `TextView`'dur; ekran okuyucu onu "düğme" diye duyurmayabilir
  ([Arayuz.md](Arayuz.md)).
- **Okul ağı.** Bütün okul tek IP'den gelebildiği için sunucunun soru sınırı geniştir (10 dakikada 1500); bu parça kendisi
  hiçbir sınır koymaz.

## Testleri

- Android deposunda otomatik test yok; derleme ve lint (`./gradlew --offline assembleDebug lintDebug`) ana oturumda.
- Sunucu sözleşmesini site deposundaki testler korur:
  - `testler/guvenlik-test.js` — soru üretiliyor (`id`, `soru`); cevap istemciye sızmıyor; cevapsız ve yanlış cevaplı kayıt
    reddediliyor; doğru cevapla geçiyor; aynı soru ikinci kez kullanılamıyor. Girişte: temiz kişi sorusuz giriyor; hatalı
    denemeden sonra cevap `soruGerekli: true` taşıyor; sorusuz ve yanlış cevaplı giriş reddediliyor; doğru cevapla giriş sürüyor.
  - `testler/test-giris-kayit.js` — kayıtta yanlış doğrulama → `alan: 'bot'`; başka alan hatalıyken soru harcanmıyor (aynı
    soruyla düzeltilmiş kayıt geçiyor); hesap açılınca soru harcanıyor, harcanan soruyla ikinci deneme olmuyor.
  - `testler/test-okul-agi.js` — tek IP'den gelen çok sayıda soru isteği.
- Elle (öykünücü):
  - Giriş ekranında "Hesap aç" → formun altında "Doğrulama: a + b = ?"; "Başka soru" → yeni soru, kutu boşalır; yanlış cevapla
    gönder → cevabın altında kırmızı ileti ve yeni soru.
  - Giriş ekranında bir hesabın şifresini bilerek yanlış yaz → "Şifre yanlış." ve form içinde doğrulama sorusu belirir; doğru
    şifre + doğru cevapla giriş yapılır.
  - Uçak kipinde "Başka soru" → "Soru alınamadı: …".

## Son durum

- `git log`: 1 commit. Dosya `e96c5f2 commit 6` (2026-09-26) ile geldi: yerel uygulamanın giriş, hesap açma ve şifremi unuttum
  ekranları yazılırken ortak parça olarak eklendi; o günden beri değişmedi.
- Bilinen açıklar (kod değiştirilmedi): girişteki çift istek, eski hata yazısının kalması, soru alınamayınca eski kimliğin
  gönderilmesi.
- Planlı işlerden bu dosyaya dokunması beklenenler: "Android yerel uygulama"daki açılış ekranı ("Hesaba gir →" ve
  "Doğrulayıcı") giriş akışını değiştirir ama bu parçayı adıyla anmıyor; "Çok dil" — "Doğrulama:", "Başka soru", "Sayıyla yaz"
  gibi sabit metinler. "Sistem" işindeki doğrulama uygulaması (TOTP) iki adımlı kodun yerine geçecek, bu toplama sorusunun değil.

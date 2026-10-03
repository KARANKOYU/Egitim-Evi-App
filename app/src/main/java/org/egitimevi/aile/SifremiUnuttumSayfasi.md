# app/src/main/java/org/egitimevi/aile/SifremiUnuttumSayfasi.java

"Şifremi unuttum" ekranı: kişi hesabının e-posta adresini ve doğrulama sorusunun cevabını yazar, sunucu (adres kayıtlıysa)
bir saatlik şifre yenileme bağlantısı gönderir; bağlantı sitede açılır.

## Bu dosya ne yapar?

Giriş ekranındaki "Şifremi unuttum" düğmesi ([GirisSayfasi.md](GirisSayfasi.md)) bu sayfayı açar. Oturum yokken açıldığı
için [AnaEkran.md](AnaEkran.md)'nin giriş kipindedir: giriş sayfasının üstüne itilir, üst ve alt çubuk yok.

Akış sitedekiyle aynı iki adımdan ilkidir:

1. **Bağlantı iste** (bu ekran): e-posta + doğrulama sorusu (ör. "Doğrulama: 4 + 7 = ?") → `POST /api/sifre-unuttum`.
2. **Yeni şifreyi yaz**: e-postadaki bağlantı sitenin şifre yenileme sayfasını açar (`POST /api/sifre-yenile`); bu adım
   uygulamada YOK, telefonun tarayıcısında olur. Kişi sonra uygulamaya dönüp yeni şifresiyle girer.

Kimin işine yarar: e-postası olan hesap — veli, öğretmen, müdür (yetişkin hesabı). Öğrenci ve servisçi hesabını okul açar,
çoğunun e-postası yoktur; ekran onlara "şifreni okul yönetimi yeniler" der.

Sunucu, adres kayıtlı olsun olmasın HEP aynı cevabı verir (yoksa bu ekran "bu e-posta kayıtlı mı?" sorgusuna dönerdi). Bu
yüzden ekran da başarıda hep "E-postana bak" der.

## İçinde neler var?

- `baslik()` → "Şifremi unuttum"; `cubuksuz()` → `true`.
- Alanlar: `eposta` (`Arayuz.Alan`), `soru` ([DogrulamaSorusu.md](DogrulamaSorusu.md)), `gonder` düğmesi, `govde` (sayfanın
  dikey gövdesi; başarıda içi değişir).

### `olustur()` — ekran

- Kenar boşlukları: yanlarda 22, üstte 36, altta 32 dp.
- Sol üstte "← Girişe dön" (hayalet düğme, sola yaslı) → `e.kapat()`.
- Büyük başlık "Şifreni yenile" ve açıklama: "Hesabının e-posta adresini yaz; yeni şifre belirlemen için bir bağlantı
  gönderelim. Öğrenci ve servisçi hesabının şifresini okul yönetimi yeniler."
- Kartın içinde: "E-posta" kutusu (ipucu "ornek@eposta.com", e-posta klavyesi), doğrulama sorusu (`soru.goster(true)`:
  görünür yapılır ve soru hemen sunucudan istenir), "Bağlantı gönder" (birincil düğme).

### `gonder()` — istek

1. E-posta hatası temizlenir. Yazılan adreste `@` yoksa ya da ilk karakterse: "Hesabının e-posta adresini yaz." (e-posta
   kutusunda), istek gitmez.
2. Gövde: `{ email, challengeId, challengeAnswer }` — son ikisini `soru.ekle(g)` koyar (cevap 1–3 haneli bir sayı değilse
   `-1`).
3. Düğme meşgul; `POST /api/sifre-unuttum`.
4. Başarı: gövdenin tamamı silinir, yerine boş durum kutusu konur — zarf simgesi (`ik_posta`), "E-postana bak", sunucunun
   iletisi (yoksa "Bu adresle bir hesap varsa yenileme bağlantısı gönderildi. Bağlantı sitede açılır.") ve "Girişe dön"
   düğmesi (`e.kapat()`).
5. Hata: düğme serbest. İleti "Doğrulama" kelimesini içeriyorsa sorunun altına yazılır ve yeni soru istenir
   (`soru.hata`, `soru.getir` — cevap kutusu da temizlenir); değilse e-posta kutusunun altına yazılır.

## Kimle konuşur?

- Çağırdıkları (aynı paket):
  - [Arayuz.md](Arayuz.md) — `sayfaGovdesi`, `dugme` (`HAYALET`, `BIRINCIL`), `baslik`, `yazi`, `kart`, `alan`, `ekle`,
    `mesgul`, `bosDurum`; [Tema.md](Tema.md) — `dp`; `R.color.soluk`, `R.drawable.ik_posta`.
  - [DogrulamaSorusu.md](DogrulamaSorusu.md) — `goster(true)`, `ekle`, `hata`, `getir`, `kok` (soru `GET /api/challenge` ile gelir).
  - [Ag.md](Ag.md) — `post`; [AnaEkran.md](AnaEkran.md) — `kapat`.
  - [Sayfa.md](Sayfa.md) — atası.
- Android: `InputType` (`TYPE_TEXT_VARIATION_EMAIL_ADDRESS`), `Gravity`, `ScrollView`, `LinearLayout`, `TextView`.
- Onu açan (grep): [GirisSayfasi.md](GirisSayfasi.md) — "Şifremi unuttum" → `e.git(new SifremiUnuttumSayfasi())`.
- Sunucu (belgesi site deposunda `sunucu/bolumler/kayit.md`; oturum istemez, aydınlatma onayı kapısından da muaf):
  - `GET /api/challenge` → `{ id, soru: "a + b = ?" }`; soru 5 dakika geçerli, doğru cevap tek kullanımlık, cevap istemciye
    hiç gitmez (`sunucu/guvenlik.js`). Bağlantı başına 10 dakikada 1500 soru (429 "Çok fazla istek. Biraz bekle.").
  - `POST /api/sifre-unuttum { email, challengeId, challengeAnswer }`:
    - bağlantı (IP) başına 15 dakikada 20 istek, aşılırsa 429 "Çok fazla istek. 15 dakika sonra tekrar dene.";
    - doğrulama cevabı yanlış ya da sorunun süresi dolmuşsa 400 "Doğrulama sorusunun cevabı yanlış.";
    - adreste `@` yoksa 400 `alan: "email"` ("Sıfırlama bağlantısı e-postaya gider; hesabının e-posta adresini yaz. E-postası
      olmayan hesaplarda şifreyi okul yönetimi yeniler.");
    - öteki her durumda 200 ve hep aynı ileti: "Bu adres kayıtlıysa şifre sıfırlama bağlantısı gönderildi. Gelen kutunu ve
      gereksiz posta klasörünü kontrol et." Adres geçersizse, aynı adrese saatte 3'ten fazla istendiyse ya da hesap yoksa
      e-posta gitmez ama cevap yine budur. Hesap varsa ve onaylıysa `sifirlamaGonder`: o kullanıcının eski bağlantıları düşer,
      yeni bağlantı 1 saat geçerli ve tek kullanımlık.
  - Sonraki adım sitede: `POST /api/sifre-yenile` (yeni şifre; başarıda o hesabın bütün oturumları ve telefon anahtarları
    kapanır).

## Nasıl çalışır (adım adım)?

```
GirisSayfasi "Şifremi unuttum" ─► git(SifremiUnuttumSayfasi)   [giriş kipi, çubuksuz]
   olustur ─► DogrulamaSorusu.goster(true) ─► GET /api/challenge ─► "Doğrulama: 4 + 7 = ?"
kişi e-postayı ve 11'i yazar ─► "Bağlantı gönder"
   '@' yok?  ─► e-posta kutusunda "Hesabının e-posta adresini yaz."
   POST /api/sifre-unuttum { email, challengeId, challengeAnswer: 11 }
      200 ─► gövde: [zarf] "E-postana bak" + sunucunun iletisi + [Girişe dön]
      400 "Doğrulama sorusunun cevabı yanlış." ─► sorunun altında ileti + yeni soru
      429 / başka ─► e-posta kutusunun altında ileti
"← Girişe dön" / "Girişe dön" / geri tuşu ─► AnaEkran.kapat ─► giriş formu (yazdıkların duruyor)
e-postadaki bağlantı ─► telefonun tarayıcısında sitenin şifre yenileme sayfası ─► yeni şifre ─► uygulamada giriş
```

## Dikkat!

- **Doğrulama hatası metinle ayırt ediliyor.** Sunucu bu hatada `alan` göndermiyor; ekran iletinin "Doğrulama" içerip
  içermediğine bakıyor. Sunucudaki metin değişirse (ya da "Çok dil" işiyle çevrilirse) hata e-posta kutusuna düşer ve soru
  yenilenmez; kişi aynı (belki süresi dolmuş) soruyla takılı kalır. Sunucunun bu hataya `alan: "bot"` eklemesi (girişte
  olduğu gibi) daha sağlam olur (öneri).
- **Hep "E-postana bak" der.** Adres kayıtlı değilse, yazım hatası varsa ya da aynı adrese bir saatte 3'ten fazla
  istendiyse e-posta gitmez ama ekran aynıdır. Bu bilinçli (hesap var mı bilgisi sızmasın); ileti "Bu adres kayıtlıysa…"
  diyerek bunu söylüyor.
- **İkinci adım uygulamada değil.** Bağlantı sitede açılır; uygulamada derin bağlantı (bağlantıya dokununca uygulamanın
  açılması) yok. Kişi tarayıcıda şifresini yeniler, sonra uygulamaya dönüp kendisi girer. Yenileme hesabın bütün
  oturumlarını ve telefon anahtarlarını kapatır: başka bir cihazda açık kalan oturum da düşer.
- **Öğrenci ve servisçi yönlendirmesi metinle.** Ekran "okul yönetimi yeniler" diyor; sunucu ise e-postası olan her onaylı
  hesaba (rolüne bakmadan) bağlantı gönderir. E-postası kayıtlı bir öğrenci de bağlantıyı alabilir.
- **İstemci denetimi çok gevşek.** Yalnız `@`'nin varlığına ve ilk karakter olmamasına bakılıyor; "ali@" gibi bir adres de
  sunucuya gider ve sunucu onu sessizce "geçersiz" sayıp aynı başarı iletisini verir.
- **Soru alınamazsa** (ör. sunucuya ulaşılamadı) sorunun yerinde "Soru alınamadı: …" yazar, soru kimliği boş kalır; gönderim
  "Doğrulama sorusunun cevabı yanlış." alır ve yeni soru istenir. "Başka soru" düğmesi de elle yeniler.
- Başarı ekranı formun yerini alır; geri tuşu ya da "Girişe dön" giriş formuna döner, bu sayfa yığından çıkar. Yeniden
  "Şifremi unuttum"a basmak boş bir form ve yeni soru açar.
- Oturum yokken açıldığı için istek `Authorization` başlığı olmadan gider ([Ag.md](Ag.md)); 401 gibi genel durumlar burada
  devreye girmez.

## Testleri

- Android deposunda otomatik test yok; derleme ve lint (`./gradlew --offline assembleDebug lintDebug`) ana oturumda.
- Sunucu sözleşmesini site deposundaki testler korur:
  - `testler/test-sifre.js` — bağlantı isteğinin kabulü, anahtarın üretilmesi, olmayan hesabın aynı cevabı alması, yanlış bot
    cevabının reddi, uydurma anahtarın ve zayıf şifrenin reddi, şifrenin güncellenmesi, eski oturumun kapanması, aynı
    bağlantının ikinci kez çalışmaması, yeni şifreyle giriş.
  - `testler/test-giris-kayit.js` — "Şifremi unuttum yalnızca e-postayla": e-posta yerine kullanıcı adı yazılınca 400
    `alan: "email"`.
  - `testler/guvenlik-test.js` — doğrulama sorusunun üretilmesi, cevabın istemciye sızmaması, aynı sorunun ikinci kez
    kullanılamaması.
- Elle (öykünücü, deneme paketi): giriş ekranı → "Şifremi unuttum" → soru görünmeli; yanlış cevapla gönder → sorunun altında
  "Doğrulama sorusunun cevabı yanlış." ve yeni soru; doğru cevap ve kayıtlı bir test adresi → "E-postana bak". Sunucuda
  e-posta ayarlanmamışsa (ya da gönderilemezse) bağlantı sunucunun konsoluna yazılır (`sifirlamaKonsolaYaz`,
  `sunucu/guvenlik.js`). "← Girişe dön" → giriş formu.

## Son durum

- `git log` (Android deposu): 1 commit. Dosya `e96c5f2 commit 6` (2026-09-26) ile geldi: yerel uygulamanın giriş ekranları
  (giriş, kayıt, kod, şifremi unuttum) yazılırken sitenin "Şifremi unuttum" penceresinin karşılığı olarak eklendi. O günden
  beri değişmedi.
- Bilinen açıklar (kod değiştirilmedi): doğrulama hatasının metinle ayırt edilmesi, ikinci adımın uygulamada olmaması.
- Planlı işlerden bu dosyaya dokunması beklenenler:
  - "Üst şerit sadeleştirme" (iş 29): doğrulama ekranlarında sol üstte "←" (vazgeç) — bu ekranda bugün "← Girişe dön"
    yazılı düğmesi var; ortak üst çubuk düğmesine dönüşebilir.
  - "Android geri tuşu" (iş 33): giriş ekranında geri hiçbir şey yapmayacak; bu sayfadan geri yine girişe döner.
  - "Çok dil" (iş 22): sunucu iletileri istemcinin diliyle gelecek — "Doğrulama" metin denetimi o zaman bozulur (yukarıda).
  - "Android yerel uygulama" (iş 10): sol menü giriş yapmadan da açılacak ("Hesaba gir →"); bu ekran giriş akışında kalır.

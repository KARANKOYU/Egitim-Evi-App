# app/src/main/java/org/egitimevi/aile/SifreSayfasi.java

Şifre değiştirme ekranı, iki hâliyle: Ayarlar'dan açılan sıradan "Şifre değiştir" ve okulun ya da yöneticinin verdiği
şifreyle girmiş kişinin kendi şifresini koymadan geçemediği zorunlu "Kendi şifreni belirle".

## Bu dosya ne yapar?

Bazı hesapların ilk şifresini kişi değil, başkası belirler: okul öğrenciyi T.C. no'yu şifre yaparak açar, giriş bilgisi
kâğıtları dağıtır ya da yetkili bir şifreyi "değiştirsin" diyerek sıfırlar. Bu şifreyi başkaları da bilebilir. Sunucu böyle
bir hesapta `user.sifreDegismeli: true` der ve kişi kendi şifresini koyana kadar şifre değiştirme, çıkış ve birkaç temel uç
dışındaki her isteğe 403 `sifreDegismeli` verir (`sunucu/api.js` `SIFRE_SERBEST`, site deposu). Uygulama da kişiyi bu
ekrana kilitler: **zorunlu** hâl.

Zorunlu hâl iki yoldan açılır ([AnaEkran.md](AnaEkran.md)):
- giriş sonrası akışta (`akisiBaslat`): oturumdaki `user.sifreDegismeli` doğruysa (aydınlatma onayından sonra);
- oturum sürerken herhangi bir istek 403 `sifreDegismeli` alırsa (`genelHata`).

İkisi de `tekSayfa(new SifreSayfasi(true))`: giriş kipinde tek sayfa, üst ve alt çubuk yok, sekmeler yok. Tek çıkış yolu
"Çıkış yap" düğmesi.

**Sıradan** hâl: Ayarlar → "Şifre değiştir" ([AyarlarSayfasi.md](AyarlarSayfasi.md)) `git(new SifreSayfasi(false))` ile o
sekmenin yığınına konur; üst çubukta "Şifre değiştir" başlığı ve geri düğmesi görünür; kaydedince sayfa kapanır.

Kim görür: herkes (öğrenci, veli, öğretmen, müdür, servisçi, rolsüz yetişkin, yönetici). Okul rolündeyken değişen şifre
yetişkin hesabınındır (sunucu öyle yazar).

## İçinde neler var?

- `SifreSayfasi(zorunlu)` — yapıcı; `zorunlu` alanını saklar.
- `baslik()` → "Şifre değiştir". `cubuksuz()` → `zorunlu`.
- Alanlar: `eski`, `yeni`, `tekrar` ([Arayuz.md](Arayuz.md) `Alan`), `kaydet` düğmesi.

### `olustur()` — ekran

- Zorunluysa önce kenar boşlukları büyür (yanlarda 22, üstte 48, altta 32 dp), büyük başlık "Kendi şifreni belirle" ve
  açıklama: "Sana verilen şifreyle girdin. Bu şifreyi başkaları da bilebilir; devam etmeden önce yalnızca senin bildiğin bir
  şifre belirle."
- Bir kartın içinde:
  - eski şifre kutusu — zorunluysa "Sana verilen şifre", değilse "Şu anki şifren" (parola türü, noktalı);
  - "Yeni şifre" kutusu ve altında "Şifreyi göster/gizle" bağlantısı ([GirisSayfasi.md](GirisSayfasi.md) `sifreGoster`);
  - kural ipucu: kişi yetişkin hesabıysa ya da rol satırındaysa (`yetiskin || rolSatiri`) "En az 8 karakter; büyük harf, küçük
    harf, rakam ve özel karakter (! ? . * gibi).", değilse "En az 8 karakter; en az bir harf ve bir rakam.";
  - "Yeni şifre (tekrar)" kutusu;
  - "Şifreyi kaydet" (birincil düğme).
- Zorunluysa kartın altında "Çıkış yap" (hayalet düğme) → `e.cikis()`.

### `kaydet()` — gönderim

1. Üç kutunun hatası temizlenir.
2. Eski şifre boşsa "Şifreni yaz." (eski kutusunda); iki yeni şifre aynı değilse "İki yeni şifre aynı değil." (tekrar
   kutusunda). Başka istemci denetimi yok; kural sunucuda.
3. Düğme meşgul; `POST /api/password { old, new }` (değerler kırpılmadan, `hamDeger`).
4. Başarı: `Oturum.tazele(e, cevap)` (cevap `{ user }`, artık `sifreDegismeli: false`), alt mesaj "Şifren değişti. Öbür
   cihazlardaki oturumlar kapandı."; zorunluysa `e.akisiBaslat()` (sekmeler açılır), değilse `e.kapat()` (Ayarlar'a döner, o
   da `gorundu` ile yenilenir).
5. Hata: düğme serbest; cevabın `alan`'ı `"eski"` ise ileti eski kutusunun altına, değilse yeni kutusunun altına yazılır.

## Kimle konuşur?

- Çağırdıkları (aynı paket):
  - [Arayuz.md](Arayuz.md) — `sayfaGovdesi`, `baslik`, `yazi`, `kart`, `alan`, `soluk`, `dugme` (`BIRINCIL`, `HAYALET`),
    `ekle`, `mesgul`; [Tema.md](Tema.md) — `dp`; `R.color.soluk`.
  - [GirisSayfasi.md](GirisSayfasi.md) — `sifreGoster`.
  - [Oturum.md](Oturum.md) — `kisi` (`yetiskin`, `rolSatiri`), `tazele`.
  - [Ag.md](Ag.md) — `post`; [AnaEkran.md](AnaEkran.md) — `bildir`, `akisiBaslat`, `kapat`, `cikis`.
  - [Sayfa.md](Sayfa.md) — atası (`baslik`, `cubuksuz`, `olustur`).
- Android: `InputType` (`TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_PASSWORD`), `ScrollView`, `LinearLayout`, `TextView`.
- Onu açanlar (grep): [AnaEkran.md](AnaEkran.md) `akisiBaslat` ve `genelHata` (`SifreSayfasi(true)`),
  [AyarlarSayfasi.md](AyarlarSayfasi.md) ("Şifre değiştir" → `SifreSayfasi(false)`).
- Sunucu: `POST /api/password` → `sunucu/bolumler/kayit.md` (site deposu):
  - Oturum yoksa 401 ([AnaEkran.md](AnaEkran.md) giriş ekranına döner). Şifre değişmesi gerekirken de çalışır (`SIFRE_SERBEST`);
    aydınlatma onayı eskiyse 403 `kvkkGerek` (onay ekranı açılır).
  - Hesap başına 15 dakikada 10 deneme (429 "Çok fazla deneme. Biraz bekle.").
  - Eski şifre yanlış → 400 `{ error: "Mevcut şifre yanlış", alan: "eski" }`.
  - Yeni şifre kurala uymuyor → 400 `alan: "yeni"`, ör. "Yeni şifre: şifre en az 8 karakter olmalı", "Yeni şifre: şifrede bir
    büyük harf, bir özel karakter (! ? . * gibi) olmalı". Kural (`sifreSorunu`, `sunucu/ortak.js`): öğrenci ve servisçi
    hesabında en az 8 karakter, harf ve rakam; ötekilerde en az 8 karakter, büyük harf, küçük harf, rakam ve özel karakter.
  - Yeni şifre eskisiyle aynı → 400 "Yeni şifre eskisiyle aynı olamaz." (`yeni`); T.C. no'yu ya da (en az 4 karakterliyse)
    kullanıcı adını içeriyor (büyük/küçük harf ayrımsız) → 400 "Yeni şifre T.C. kimlik numaranı ya da kullanıcı adını
    içermesin." (`yeni`).
  - Başarıda tek işlemde: şifre yazılır, `sifreDegismeli: false`, bu oturum DIŞINDAKİ bütün oturumlar (yetişkin hesabı ve
    okul rolleri) kapanır ve hesabın bütün telefon (uygulama) anahtarları silinir (`depo.oturumlar.hesabinOturumlariniKapat`);
    yöneticinin eski yönetim çerezleri silinir. Cevap `{ user }` (yöneticide ayrıca `yonetimAdresi`).
- Sitedeki karşılıkları: zorunlu ekran `public/js/parcalar/05b-sifre-zorunlu.js`, Ayarlar'daki şifre değiştirme
  `public/js/parcalar/25-tiklama.js` üzerinden (site deposu).

## Nasıl çalışır (adım adım)?

```
Zorunlu:
  giriş ─► AnaEkran.oturumAc ─► akisiBaslat ─► (onay tamam) user.sifreDegismeli ─► tekSayfa(SifreSayfasi(true))
           ya da herhangi bir istek ─► 403 sifreDegismeli ─► genelHata ─► tekSayfa(SifreSayfasi(true))
  "Kendi şifreni belirle"  [Sana verilen şifre] [Yeni şifre + göster] [tekrar]  [Şifreyi kaydet]  [Çıkış yap]
  kaydet ─► POST /api/password { old, new }
       200 ─► Oturum.tazele({ user }) ─► "Şifren değişti…" ─► akisiBaslat ─► sekmeler
       400 alan=eski ─► eski kutusunda "Mevcut şifre yanlış"
       400 alan=yeni / 429 / ağ ─► yeni kutusunda ileti

Sıradan:
  Ayarlar ─► "Şifre değiştir" ─► git(SifreSayfasi(false))  (üst çubuk: ← Şifre değiştir)
  kaydet 200 ─► "Şifren değişti…" ─► kapat ─► Ayarlar (gorundu → yeniden kurulur)
```

## Dikkat!

- **Kural ipucu bazı hesaplarda yanlış.** Ekran güçlü kuralı yalnız `yetiskin || rolSatiri` iken gösteriyor; sunucu ise
  öğrenci ve servisçi DIŞINDAKİ herkese güçlü kuralı uyguluyor (`gucluSifreli`). Yönetici hesabı (`role: admin`, yetişkin
  hesabı sayılmaz) ve ana hesaba bağlı olmayan eski düzen öğretmen/müdür hesapları "en az bir harf ve bir rakam" ipucunu
  görür ama böyle bir şifre sunucuda reddedilir (ileti doğru gelir, yalnız ipucu yanıltır). Doğru şart: rol `student` ya da
  `servisci` değilse güçlü. Site de böyle yapıyor: `public/js/parcalar/05-giris.js` `gucluSifreli(S.user)` role bakar.
- **Zorunlu ekrandan geri tuşu uygulamayı kapatır.** Giriş kipinde yığında tek sayfa var; [AnaEkran.md](AnaEkran.md) `geri`
  `finish()` der. Uygulama yeniden açılınca oturumdaki `sifreDegismeli` hâlâ doğru olduğu için aynı ekran gelir. Kurtuluş
  yalnız şifreyi koymak ya da "Çıkış yap".
- **Telefon bildirimleri bir süre durabilir.** Sunucu başarıda hesabın bütün uygulama anahtarlarını siler, bu telefonunki
  dahil; bu sayfa ise telefondaki anahtarı ([UygulamaAyar.md](UygulamaAyar.md)) silmez, yenisini de istemez. Bildirim
  yoklaması bir sonraki turda 401 alıp anahtarı unutur ve durur ([Bildirimler.md](Bildirimler.md)); yeni anahtar ancak
  sekmeler yeniden kurulduğunda ve o an telefonda anahtar yoksa alınır ([AnaEkran.md](AnaEkran.md) `cihazKaydet`): uygulama
  yeniden açılınca, giriş, portal değişimi ya da çocuk ekleme sonrası. Ayarlar'dan şifre değiştiren kişi bu arada telefon
  bildirimi almaz (kod okumasına göre; telefonda denenmedi). İlk girişteki zorunlu değişiklikte sorun yok: o anda henüz
  anahtar alınmamıştır, `akisiBaslat` sonrası yenisi alınır.
- **Hata yeri yalnız `alan`'a göre.** `"eski"` dışındaki her hata — kural, 429 "Çok fazla deneme. Biraz bekle.", internet
  kesintisi ("İnternet bağlantısı yok ya da Eğitim Evi'ne ulaşılamadı.") — "Yeni şifre" kutusunun altına yazılır; kişi şifresinin
  kurala uymadığını sanabilir.
- **İstemcide kural denetimi yok.** Boş yeni şifre, eskisiyle aynısı ya da T.C.'yi içereni de sunucuya gider; ileti
  sunucudan gelir. Değerler kırpılmaz: sondaki boşluk şifrenin parçasıdır (girişte de öyle).
- **`Oturum.tazele` iki alanı sıfırlar.** Cevapta yalnız `user` olduğu için `kisilikSec` ve `cocuk` boşalır
  ([Oturum.md](Oturum.md)).
- **"Öbür cihazlardaki oturumlar kapandı" iletisi eksik anlatıyor.** Bu oturum dışındaki bütün oturumlar kapanır: öbür
  cihazlardakiler, sitedeki tarayıcı oturumları ve aynı yetişkin hesabının okul rolü oturumları. Ayrıca hesabın BÜTÜN
  telefon (uygulama) anahtarları silinir, bu telefonunki dahil (yukarıdaki madde).
- "Şifreyi göster" yalnız "Yeni şifre" kutusunda var; eski ve tekrar kutuları hep noktalı.
- Sıradan hâlde gövdede başlık yoktur (başlık üst çubukta); kart 4 dp boşlukla başlar. Zorunlu hâlde açıklamayla kart
  arası 18 dp.

## Testleri

- Android deposunda otomatik test yok; derleme ve lint (`./gradlew --offline assembleDebug lintDebug`) ana oturumda.
- Sunucu sözleşmesini site deposundaki testler korur:
  - `testler/test-yonetim.js` — T.C. no ile açılan hesapta `sifreDegismeli: true`, şifre değişmeden 403 `sifreDegismeli`,
    T.C.'yi içeren ve eskisiyle aynı yeni şifrenin reddi, kendi şifresini koyunca `sifreDegismeli: false`.
  - `testler/test-giris-bilgisi.js` — dağıtılan giriş bilgisiyle girişte kendi şifresini koymasının istenmesi.
  - `testler/test-yetiskin.js` — müdür rolündeyken şifrenin değişmesi ve yeni şifrenin yetişkin hesabında geçerli olması.
  - `testler/test-servis-yoklama.js` — şifre değişince uygulama oturumunun ve uygulama anahtarlarının düşmesi.
- Elle (öykünücü, deneme paketi):
  - Müdürle bir öğrenciyi T.C. no'yu şifre yaparak aç; uygulamada o öğrenciyle gir → "Kendi şifreni belirle", çubuklar yok;
    yanlış "Sana verilen şifre" → eski kutusunda "Mevcut şifre yanlış"; "abc" → yeni kutusunda kural iletisi; geçerli şifre →
    "Şifren değişti…" ve sekmeler.
  - Ayarlar → "Şifre değiştir" → iki farklı yeni şifre → "İki yeni şifre aynı değil."; doğru doldur → Ayarlar'a döner. Aynı
    hesap sitede açıksa sayfa yenilenince giriş ekranına düşmeli.

## Son durum

- `git log` (Android deposu): 1 commit. Dosya `e96c5f2 commit 6` (2026-09-26) ile geldi: yerel uygulamanın çekirdeği
  yazılırken sitedeki zorunlu şifre ekranının (`05b-sifre-zorunlu.js`) ve Ayarlar'daki şifre değiştirmenin karşılığı olarak
  eklendi. O günden beri değişmedi.
- Bilinen açıklar (kod değiştirilmedi): kural ipucunun yönetici ve eski düzen hesaplarda yanlış olması, şifre değişince
  telefon bildirimlerinin bir süre durması, hata iletisinin yerleşimi.
- Planlı işlerden bu dosyaya dokunması beklenenler:
  - "Güvenlik denetimi" (iş 3): okulun verdiği HER şifre ilk girişte değişecek (toplu hesap açma, yetkilinin sıfırlaması,
    öğrenci ve servisçi hesapları); tanım "Android işine not düş" diyor — zorunlu hâl çok daha sık açılacak.
  - "Kullanıcı arama + hesap penceresi" (iş 6): yöneticinin ve desteğin verdiği şifrede de ilk girişte değiştirme zorunlu.
  - "Üst şerit sadeleştirme" (iş 29): doğrulama ekranlarında sol üstte "←" (vazgeç); "Android geri tuşu" (iş 33): giriş
    ekranlarında geri tuşunun uygulamayı kapatmaması.
  - "Sistem" (iş 4): yeni cihaz uyarısı ve açık oturumlar listesi — "öbür oturumlar kapandı" iletisi oraya bağlanabilir.
  - "Çok dil" (iş 22): sunucu iletileri ve ekran metinleri.

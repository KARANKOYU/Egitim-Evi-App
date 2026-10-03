# app/src/main/java/org/egitimevi/aile/KvkkSayfasi.java

Aydınlatma metni onayı: kişinin onayı yürürlükteki metne göre değilse uygulamayı açmadan önce gösterilen sayfa — beş
maddelik özet, metnin tamamına (sitede) bağlantı, onay kutusu, "Onayla ve devam et" ve "Çıkış yap". Aydınlatma metnini
tarayıcıda açan `metniAc` da burada.

## Bu dosya ne yapar?

Eğitim Evi kişisel veri işler (ad, e-posta, telefon, öğrencinin okul kayıtları…). KVKK'nın aydınlatma yükümlülüğü gereği
herkes yürürlükteki **aydınlatma metnini** onaylamış olmalı; metin her değiştiğinde sürümü artar ve herkesten YENİDEN onay
istenir. Okulun açtığı hesaplarda (öğrenci, servisçi) ise hiç onay yoktur; bu kişiler metni ilk girişte onaylar. Onay
olmadan sunucu, onay ve çıkış gibi birkaç uç dışındaki her isteği 403 `kvkkGerek` ile reddeder.

Uygulamada bu sayfa iki yoldan açılır:

1. Giriş cevabında (ya da kayıtlı oturumda) `kvkkGuncel` yanlışsa [AnaEkran.md](AnaEkran.md)'nin `akisiBaslat`'ı uygulamayı
   kurmadan önce bu sayfayı tek başına gösterir.
2. Uygulama kullanılırken herhangi bir istek 403 `kvkkGerek` alırsa `AnaEkran.genelHata` oturumda `kvkkGuncel`'i yanlış
   yazıp bu sayfayı açar ([Ag.md](Ag.md)).

Kişi özeti okur, isterse "Metnin tamamını oku" ile metni tarayıcıda açar, kutuyu işaretleyip "Onayla ve devam et"e basar;
sunucu onayı yazar ve akış baştan kurulur (gerekirse zorunlu şifre sayfası, sonra rolün sekmeleri ilk sekmeden; sayfa
kullanım sırasında açıldıysa kişi kaldığı sayfaya değil ana sekmeye döner). Onaylamak istemeyen "Çıkış yap" diyebilir;
uygulama onaysız kullanılamaz.

## İçinde neler var?

### Dışa açık

- `baslik()` — "Aydınlatma metni"; `cubuksuz()` — `true` (üst ve alt çubuk yok).
- `metniAc(e)` (paket içi, `static`) — `KayitSayfasi.siteAc(e, "/kvkk/kvkk.html")`: metnin tamamı sunucunun adresinde,
  telefonun tarayıcısında açılır ([KayitSayfasi.md](KayitSayfasi.md)).
- `olustur()` — sayfa:
  - Bölüm etiketi "KİŞİSEL VERİLERİN KORUNMASI" (`bolumEtiketi` büyük harfe çevirir), başlık "Aydınlatma metni
    güncellendi", açıklama "Devam etmeden önce yeni metni okuyup onaylaman gerekiyor.".
  - Kart: beş madde (kalın başlık + soluk açıklama):
    | Başlık | Metin |
    |---|---|
    | Veri sorumlusu | Bu sistemi kullanan okuldur. Veriler Eğitim Evi'nin sunucusunda durur; her okulun verisi ayrıdır ve yalnız o okulun yetkilileri erişir. |
    | Ne işlenir | Ad, kullanıcı adı, e-posta ve telefon; öğrencide okul kayıtları (ödev, not, devamsızlık, servis). |
    | Kim görür | Herkes yetkisi kadar: öğrenci kendini, veli kendi çocuğunu, öğretmen ders verdiği sınıfları. |
    | Paylaşım | Veriler reklam, analiz ya da satış için hiçbir üçüncü tarafa aktarılmaz. |
    | Haklarım | Bilgi isteme, düzeltme ve silme gibi haklarını okula başvurarak kullanırsın. |
    Kartın sonunda "Metnin tamamını oku" (ikincil düğme) → `metniAc`.
  - Onay kutusu: "Aydınlatma metnini okudum; kişisel verilerimin bu metne göre işlenmesini kabul ediyorum." (15 sp, ana
    renkli işaret).
  - "Onayla ve devam et" (birincil) → `onayla()`; "Çıkış yap" (hayalet) → `e.cikis()`.

### İç

- `onayla()` — kutu işaretli değilse alt mesaj "Devam etmek için onay kutusunu işaretle.". Değilse düğme meşgul,
  `POST /api/kvkk-onay { onay: true }`:
  - başarı → `Oturum.tazele(e, cevap)` (cevaptaki `user` ve `kvkkGuncel: true`, `kvkkSurum` telefondaki oturuma yazılır)
    → `e.akisiBaslat()`: şifresini okul vermiş kişi önce zorunlu şifre sayfasına, öbürleri rolünün sekmelerine gider.
  - hata → düğme açılır, ileti alt mesajda.

## Kimle konuşur?

- Çağırdıkları:
  - [Ag.md](Ag.md) — `Ag.post(e, "/api/kvkk-onay", …)` (oturumla, Bearer).
  - `Oturum.java` — `tazele`; [AnaEkran.md](AnaEkran.md) — `akisiBaslat`, `cikis`, `bildir`.
  - [KayitSayfasi.md](KayitSayfasi.md) — `siteAc` (`metniAc` için).
  - [Arayuz.md](Arayuz.md) — `sayfaGovdesi`, `bolumEtiketi`, `baslik`, `yazi`, `kart`, `dugme`, `ekle`, `mesgul`;
    `Tema.java` — `dp`, `KALIN`, `renk`.
- Onu açanlar (grep):
  - [AnaEkran.md](AnaEkran.md) — `akisiBaslat` (oturum var, `Oturum.kvkkGuncel` yanlış) ve `genelHata` (403 `kvkkGerek`):
    ikisi de `tekSayfa(new KvkkSayfasi())`.
  - `metniAc`'ı kullananlar: [KayitSayfasi.md](KayitSayfasi.md) ("Aydınlatma metnini oku"), [AyarlarSayfasi.md](AyarlarSayfasi.md)
    ("Eğitim Evi" kartındaki "Aydınlatma metni" satırı).
- Sunucu (site deposunda `sunucu/bolumler/kayit.md` ve `sunucu/api.md`):
  - `POST /api/kvkk-onay { onay }` — oturum yoksa 401 "Giriş yapmalısın"; `onay` tam `true` değilse 400 "Devam etmek
    için aydınlatma metnini okuyup onaylaman gerekiyor."; değilse kişiye `{ onay: true, tarih, surum: KVKK_SURUM }` yazılır.
    Onay KİŞİNİNDİR: okul rolündeyken onaylayanın yetişkin hesabına ve bütün okul rollerine de yazılır. Cevap
    `{ user, kvkkGuncel: true, kvkkSurum }`.
  - Kapı: `sunucu/api.js`'teki `KVKK_SERBEST` listesi dışındaki her uç, onayı eski kişiye 403 `{ kvkkGerek: true }` döner
    (`kvkk-onay` ve `logout` listede; zorunlu şifre kapısının `SIFRE_SERBEST` listesinde de). Bugünkü sürüm `KVKK_SURUM`
    (site deposunda `sunucu/bolumler/kayit.js`).
  - Giriş cevabı ve `GET /api/me` `kvkkGuncel` ile `kvkkSurum`'u taşır; `Oturum` bunları saklar.
- Metnin kendisi: site deposunda `public/kvkk/kvkk.html` (11 bölüm: veri sorumlusu, işlenen veriler, amaç, hukuki sebep,
  kimler erişir, yurt dışı, saklama süresi, koruma, haklar, kullanım sırasında yaşanan sorunlar, metnin değişmesi).
- Rol: oturumu olan herkes (yönetici hariç: sunucu onu bu kapıdan muaf tutar).

## Nasıl çalışır (adım adım)?

```
giriş / uygulama açılışı ─► AnaEkran.akisiBaslat
   Oturum.acik? ── hayır ─► giriş
   Oturum.kvkkGuncel? ── hayır ─► tekSayfa(KvkkSayfasi)
uygulama kullanılırken bir istek ─► 403 { kvkkGerek: true } ─► genelHata ─► kvkkGuncel = false ─► tekSayfa(KvkkSayfasi)

KvkkSayfasi
   "Metnin tamamını oku" ─► tarayıcı: <sunucu>/kvkk/kvkk.html
   [x] onay ─► "Onayla ve devam et" ─► POST /api/kvkk-onay { onay: true }
        200 { user, kvkkGuncel: true, kvkkSurum } ─► Oturum.tazele ─► akisiBaslat
              sifreDegismeli? ─► zorunlu şifre sayfası : rolün sekmeleri
   "Çıkış yap" ─► AnaEkran.cikis ─► giriş
```

## Dikkat!

- **Özet elle yazılmış ve eskimiş.** Beş madde koda gömülü sabit metin; aydınlatma metni değişince kendiliğinden
  değişmez. Bugün sitedeki metin (1.15'ten beri) T.C. kimlik numarasının bütün hesaplarda zorunlu olduğunu söylüyor;
  özetin "Ne işlenir" maddesinde ise T.C. no yok — kişi kodu, Eğitim Evi Aile'nin konum ve ekran süresi verisi, servis
  aracının konumu, quiz kayıtları da yok. Özet bir özet, ama en çok tartışılacak verileri anmıyor. Metin her
  değiştiğinde (site deposunda KVKK kuralı: `kvkk.html` + `KVKK_SURUM`) bu maddeler de gözden geçirilmeli; `commit 12`
  "Veri sorumlusu" maddesini 1.14'e böyle uydurmuştu. "KVKK ve onay metinleri TAM denetimi"nde ele alınmalı (kod
  değiştirilmedi).
- **Kullanım koşulları onayda yok.** Sitedeki aynı pencere (`public/js/parcalar/26-baslat.js`) "…bu kapsamda işlenmesini
  kabul ediyorum. Kullanım koşullarını kabul ediyorum." der ve iki metne bağlantı verir; buradaki kutu yalnız aydınlatma
  metnini anar. Sunucu yalnız `onay`'a baktığı için onay yine yazılır.
- **Başlık hep "güncellendi".** Sitedeki pencere, kişinin daha önce hiç onayı yoksa (okulun açtığı hesapla ilk giriş)
  "Aydınlatma metni" başlığını ve "Hoş geldin…" cümlesini, varsa "güncellendi" ile sürüm numarasını gösterir. Bu sayfa
  her durumda "Aydınlatma metni güncellendi" ve "yeni metni" der; ilk kez giren öğrenci için yanıltıcı. Sürüm numarası da
  gösterilmez.
- **Geri tuşu uygulamadan çıkar.** Sayfa tek başına açılır (`tekSayfa`); geri tuşunda [AnaEkran.md](AnaEkran.md)
  yığında başka sayfa bulamayıp `finish()` der. Oturum kalır; uygulama yeniden açılınca bu sayfa yine gelir.
- **Metin tarayıcıda açılır.** `metniAc` sunucunun adresini aile ayar dosyasından okur (`Ayarlar.sunucu`, varsayılan
  `https://egitimevi.org`; adresin nereden değiştiği [Ag.md](Ag.md) "Dikkat!"te). Telefonda tarayıcı yoksa "Bağlantıyı
  açacak tarayıcı yok." çıkar ve kişi metnin tamamını okuyamadan onay vermek zorunda kalır.
- **Kaldığı yer kaybolur.** Sayfa kullanım sırasında (bir isteğin 403 `kvkkGerek`'iyle) açıldıysa onaydan sonra
  `akisiBaslat` → `uygulamayiKur` sekmeleri ve yığınlarını baştan kurar; kişi ilk sekmenin kök sayfasına döner, açık
  olan sayfası ve yazdıkları gider.
- **Onay bütün rollere yazılır.** Okul rolündeyken (ör. öğretmen portalında) onaylamak yetişkin hesabının ve öbür
  rollerinin onayını da günceller; portal değiştirince sayfa yeniden çıkmaz.
- **Oturum düşerse giriş ekranı.** Onay isteği 401 alırsa (oturumun süresi doldu) `genelHata` girişe götürür; giriş
  sonrası sayfa yeniden gelir.
- `onay` gövdede tam `true` gitmeli; sunucu `"evet"` gibi bir değeri reddeder (site deposundaki testte var).

## Testleri

- Android deposunda otomatik test yok; derleme ve lint (`./gradlew --offline assembleDebug lintDebug`) ana oturumda.
- Sunucu sözleşmesini site deposundaki testler korur:
  - `testler/test-yonetim.js` — onayı eski kişinin isteği 403 + `kvkkGerek`; `onay: "evet"` reddedilir; `onay: true` ile
    onaylanır.
  - `testler/test-giris-bilgisi.js`, `testler/test-nakil.js`, `testler/test-siniflarim.js` — okulun açtığı hesapla
    girişte `kvkkGuncel: false` gelince `POST /api/kvkk-onay { onay: true }` ile onaylayıp devam eder (bu sayfanın
    yaptığının aynısı, yardımcı adım olarak).
- Elle (öykünücü, deneme paketi; uygulamanın sunucu adresi deneme sunucusuna çevrilmiş olmalı, bkz. [Ag.md](Ag.md)):
  - Okulun açtığı yeni bir öğrenci hesabıyla gir → bu sayfa açılmalı. Kutuyu işaretlemeden "Onayla ve devam et" → "Devam
    etmek için onay kutusunu işaretle."; işaretleyip onayla → öğrencinin sekmeleri (ya da okul şifresiyle girdiyse
    zorunlu şifre sayfası).
  - Deneme sunucusunda `KVKK_SURUM`'u artır (site deposunda `sunucu/bolumler/kayit.js`), sunucuyu yeniden başlat,
    uygulamada herhangi bir sayfayı yenile → bu sayfa açılmalı.
  - "Metnin tamamını oku" → tarayıcıda `/kvkk/kvkk.html`; "Çıkış yap" → giriş ekranı.

## Son durum

- `git log` (Android deposu): 3 commit.
  - `292486c commit 12` (2026-09-30): "Veri sorumlusu" maddesi aydınlatma metninin 1.14 sürümüne uyduruldu — "Eğitim Evi
    bir yazılımdır; veriler okulun sunucusunda durur." yerine "Veriler Eğitim Evi'nin sunucusunda durur; her okulun verisi
    ayrıdır ve yalnız o okulun yetkilileri erişir.".
  - `d0fc203 commit 8` (2026-09-27): metnin adresi `/kvkk.html`'den `/kvkk/kvkk.html`'ye taşındı (sitenin sayfa
    klasörleri işi; aynı commit'te Ayarlar'daki SSS adresi).
  - `e96c5f2 commit 6` (2026-09-26, yerel uygulamanın çekirdeği): ilk hâli.
- Bilinen açıklar (kod değiştirilmedi): özetin T.C. no ve öbür yeni verileri anmaması; onayda kullanım koşullarının
  olmaması; ilk kez giren için de "güncellendi" denmesi ve sürümün gösterilmemesi; geri tuşunda uygulamadan çıkılması;
  kullanım sırasında açıldıysa onaydan sonra kişinin kaldığı sayfaya dönmemesi.
- Planlı işlerden bu dosyaya dokunması beklenenler:
  - "KVKK ve onay metinleri TAM denetimi": özet maddeleri ve onay kutusu metni.
  - "T.C. KİMLİK NO BÜTÜN HESAPLARDA ZORUNLU": tanım, T.C.'si olmayan eski yetişkin hesaplarına girişten sonra aydınlatma
    metninin yeniden onayıyla AYNI adımda "T.C. kimlik numaranı gir" sorulmasını istiyor — bu sayfa ya da yanına gelecek
    bir adım.
  - "Android geri tuşu / geri hareketi": giriş akışında geri tuşu "hiçbir şey yapma" olacak (bugün uygulamadan çıkıyor).
  - "Çok dil": sayfa metinleri çevrilir; hukuki metinler Türkçe kalır.

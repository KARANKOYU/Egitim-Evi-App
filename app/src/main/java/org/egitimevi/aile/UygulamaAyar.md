# app/src/main/java/org/egitimevi/aile/UygulamaAyar.java

Telefonda "uygulama" ayar dosyasında kalan, oturumdan ayrı küçük bilgiler: bildirim yoklamak ve servisçinin sefer konumunu
göndermek için alınan uygulama anahtarı, son görülen bildirimin imleci, okulun servis saatleri ve süren sefer.

## Bu dosya ne yapar?

Uygulamanın arka planda çalışan iki işi oturum anahtarını kullanmaz:

- **Bildirim yoklaması** ([Bildirimler.md](Bildirimler.md), [BildirimIsi.md](BildirimIsi.md)): Firebase olmadığı için telefon
  sunucuya kendisi sorar — 15 dakikada bir, okulun servis saatlerinde dakikada bir.
- **Servisçinin sefer konumu** ([SeferServisi.md](SeferServisi.md)).

İkisi de girişten sonra sunucudan bir kez alınan **uygulama anahtarıyla** (64 onaltılık karakter, `X-Cihaz` başlığı) gider.
Bu anahtar hesaba giriş VERMEZ: yalnız bu iki işe yarar; sunucuda yalnız özeti tutulur (`sunucu/bolumler/cihaz.md`, site
deposu). Bu sınıf o anahtarı ve iki işin telefonda tutması gereken durumu saklar.

Telefonda üç ayrı ayar dosyası var: "oturum" ([Oturum.md](Oturum.md); hesaba giriş veren oturum anahtarı ve hesap bilgisi),
"uygulama" (bu dosya) ve "aile" ([Ayarlar.md](Ayarlar.md); sunucu adresi ve çocuğun telefonunun ayrı anahtarı). Üçü de
uygulamaya özel (`MODE_PRIVATE`) ve yedeklenmez (manifestteki `allowBackup="false"` ve `res/xml/` yedek kuralları).

Sınıf bir araç sınıfıdır: `final`, kurucusu gizli, her işlev `static`.

## İçinde neler var?

### Ayar dosyasındaki alanlar ("uygulama")

| Alan | İçerik | Yazan | Okuyan |
|---|---|---|---|
| `anahtar` | uygulama anahtarı (sunucunun `cihazAnahtari`'ı) | `anahtarYaz` | `anahtar`, `anahtarVar` |
| `imlec` | son görülen bildirimin imleci (`"<zaman>|<kimlik>"`) | `imlecYaz` (anahtarYaz siler) | `imlec` |
| `servisSaatleri` | okulun servis saatleri, JSON metni (`{ sabahBas, sabahBit, aksamBas, aksamBit }`) ya da `""` | `imlecYaz` | `servisSaatleri` |
| `sefer` | süren seferin kimliği ya da `""` | `seferYaz` | `sefer` |
| `sunucuSecildi` | deneme paketinde sunucu adresi seçildi mi | `sunucuSecildi(c, b)` | `sunucuSecildi(c)`, `cik` |

### İşlevler

- `anahtar(c)` — uygulama anahtarı; yoksa `""`. `anahtarVar(c)` — boş değil mi.
- `anahtarYaz(c, anahtar)` — anahtarı yazar ve imleci SİLER: yeni anahtarla ilk yoklama eski bildirimleri getirmez, yalnız
  güncel imleci alır.
- `imlec(c)`, `servisSaatleri(c)` — yoksa `""`.
- `imlecYaz(c, imlec, saatler)` — her başarılı yoklamadan sonra ikisini birden yazar (`null` → `""`).
- `sefer(c)`, `seferYaz(c, seferId)` — süren seferin kimliği (`null` → `""`; boş = sefer yok).
- `sunucuSecildi(c)` / `sunucuSecildi(c, secildi)` — okuma ve yazma; bugün çağıranı yok (aşağıda).
- `cik(c)` — dosyayı boşaltır (anahtar, imleç, servis saatleri, sefer gider), yalnız `sunucuSecildi` korunur. Yorum: "sunucu
  adresi kalır" — adresin kendisi zaten bu dosyada değil, [Ayarlar.md](Ayarlar.md)'dadır.

## Kimle konuşur?

- Android: `SharedPreferences` ("uygulama", `MODE_PRIVATE`, `apply()`). Başka sınıfa bağımlılığı yok.
- Onu kullananlar (grep):
  - [AnaEkran.md](AnaEkran.md) — `cihazKaydet`: `anahtarVar` (yoksa `POST /api/cihaz`), `anahtarYaz` (cevaptaki
    `cihazAnahtari` 64 küçük onaltılık karakterse); `cikis`: `anahtar` (sunucuda `POST /api/cihaz/sil`); `oturumuBitir`: `cik`.
  - [Bildirimler.md](Bildirimler.md) — `anahtarVar` (yoksa işleri iptal), `servisSaatleri` (hızlı iş kurulsun mu), `anahtar`,
    `imlec`, `imlecYaz`; 401/403'te `cik`.
  - [SeferServisi.md](SeferServisi.md) — `seferYaz` (`baslat`, `durdur`, sunucu reddedince), `sefer`, `anahtarVar`, `anahtar`.
  - [BaslatmaAlici.md](BaslatmaAlici.md) — telefon açılınca / uygulama güncellenince `seferYaz(c, "")`.
  - [AyarlarSayfasi.md](AyarlarSayfasi.md) — `anahtarVar` ("Telefon bildirimleri" satırının yazısı).
- Sunucu (belgesi site deposunda `sunucu/bolumler/cihaz.md`): anahtar `POST /api/cihaz` cevabıyla gelir (yalnız bir kez
  gösterilir); imleç ve servis saatleri `GET /api/cihaz/bildirimler?son=<imleç>` cevabından; sefer kimliği sitenin
  `POST /api/servis/sefer-basla`'sının ürettiği seferdir. Hesap başına en çok 5 anahtar; şifre değişince ya da sıfırlanınca,
  okul öğrenci şifrelerini toplu yenileyince, hesap silinince, aydınlatma onayı eskiyince (ilk anahtarlı istekte) sunucu
  anahtarı siler.

## Nasıl çalışır (adım adım)?

```
giriş ─► AnaEkran.uygulamayiKur ─► cihazKaydet: anahtarVar()? ── evet ─► (bir şey yapma)
                                                └ hayır ─► POST /api/cihaz ─► anahtarYaz(a)  (imleç silinir)
                                                                           ─► Bildirimler.zamanla
yoklama (15 dk / servis saatinde 1 dk):
   anahtar(), imlec() ─► GET /api/cihaz/bildirimler?son=<imleç>  [X-Cihaz]
        200      ─► (ilk değilse ve uygulama önde değilse bildirim göster) ─► imlecYaz(imlec, servisSaatleri)
        401/403  ─► cik()  (anahtar, imleç, sefer gider) ─► işler iptal
sefer (gelecekteki servisçi ekranı): SeferServisi.baslat ─► seferYaz(id) ... sefer biter ─► seferYaz("")
telefon açıldı / güncellendi ─► BaslatmaAlici ─► seferYaz("")
çıkış ─► AnaEkran.cikis: (arka planda) POST /api/cihaz/sil [X-Cihaz: anahtar()] ─► oturumuBitir ─► cik()
```

## Dikkat!

- **Sınıf yorumu WebView döneminden kaldı.** "Uygulamanın (sitenin açıldığı ana ekranın)" ve "Oturum (giriş) sitenin kendi
  deposunda (WebView) kalır" cümleleri eskidi: `commit 6`'dan beri uygulama siteyi açmıyor, oturum [Oturum.md](Oturum.md)'nin
  "oturum" ayar dosyasında. Anahtarın hesaba giriş vermediği bilgisi hâlâ doğru.
- **`sunucuSecildi` artık işe yaramıyor.** WebView döneminde deneme paketi ilk açılışta sunucu adresini soruyor, cevaptan
  sonra bunu `true` yapıyordu; o pencere `e96c5f2 commit 6`'da kalktı. Bugün iki işlevin de çağıranı yok, `cik` yalnız
  değerini koruyor ([Ag.md](Ag.md)'deki sunucu adresi notu).
- **Telefondaki anahtar sunucuda silinmiş olabilir.** Sunucu anahtarı şifre değişince, hesap silinince ya da aydınlatma
  metni güncellenince siler; telefon bunu ancak bir sonraki yoklama 401/403 alınca anlar ve `cik` ile unutur. Yeni anahtar
  da ancak sekmeler yeniden kurulduğunda alınır ([AnaEkran.md](AnaEkran.md) `cihazKaydet` yalnız "telefonda anahtar var
  mı"ya bakar). Arada telefon bildirimi gelmez ([SifreSayfasi.md](SifreSayfasi.md)).
- **`cik` seferi de siler.** Yoklama 401/403 alınca `cik` saklı seferi de boşaltır; çalışan bir [SeferServisi.md](SeferServisi.md)
  bir sonraki gönderimde bunu görüp durur. Anahtar geçersizse konum zaten gidemeyeceği için tutarlı.
- **Anahtar düz metin saklanır.** Uygulamaya özel dosyada, yedek dışı; Keystore'la şifrelenmiyor. Anahtar hesaba giriş
  vermediği için sızarsa en kötü durumda kişinin yeni bildirimleri okunabilir (ve servisçide sefer konumu
  gönderilebilir); sunucuda telefondan ya da oturumla silinebilir.
- **Sefer telefon yeniden açılınca unutulur.** [BaslatmaAlici.md](BaslatmaAlici.md) bilerek siler (servisçi seferi yeniden
  başlatır); uygulama süren seferi sunucudan (`GET /api/cihaz/ayar` `acikSefer`) öğrenmiyor.
- Yazmalar `apply()` ile (arka planda); [Bildirimler.md](Bildirimler.md)'deki yoklama arka iş parçacığında okuyup yazıyor,
  `SharedPreferences` bunu kaldırır.

## Testleri

- Android deposunda otomatik test yok; derleme ve lint (`./gradlew --offline assembleDebug lintDebug`) ana oturumda.
- Sunucu sözleşmesini site deposundaki `testler/test-servis-yoklama.js` (bölüm "Cihaz anahtarı") korur: oturumla 64 haneli
  anahtar, anahtarın `/api/me`'ye giriş vermemesi, ilk yoklamada eski bildirim gelmemesi (imleç), imleçle yalnız yenilerin
  gelmesi, en çok 20, servis saatleri, oturumla ve anahtarla silme, hesap başına 5, şifre değişince ve hesap silinince
  iptal. `testler/test-ozellikler.js` — okulda Servis kapalıyken `servisSaatleri`'nin `null` gelmesi.
- Elle (öykünücü, deneme paketi): giriş yap, bildirim iznini ver → Ayarlar'daki "Telefon bildirimleri" satırı "Açık: 15
  dakikada bir, servis saatlerinde dakikada bir bakılır" der (anahtar alındı demek; anahtar yoksa yalnız "Açık");
  `adb shell run-as org.egitimevi.aile ls shared_prefs` → "uygulama.xml". Çıkış yap → dosyada yalnız `sunucuSecildi`
  kalır. Dosyanın içeriğini bir yere yapıştırma: anahtar taşır.

## Son durum

- `git log` (Android deposu): 1 commit. Dosya `f70aeca commit 5` (2026-09-26, tek uygulamaya geçiş) ile geldi: uygulama siteyi
  içinde açarken site girişten sonra köprüyle anahtarı veriyordu (`Kopru.anahtarKaydet` → `anahtarYaz`); bildirim yoklaması,
  sefer konumu ve deneme paketinin "sunucu adresi sor" penceresi (`sunucuSecildi`) bunu kullanıyordu. Dosya o günden beri
  değişmedi.
- `e96c5f2 commit 6` (2026-09-26) bu dosyaya dokunmadan çevresini değiştirdi: köprü ve WebView kalktı, anahtarı artık
  [AnaEkran.md](AnaEkran.md) `cihazKaydet` kendisi alıyor; "sunucu adresi sor" penceresi kalktığı için `sunucuSecildi` boşta
  kaldı; `SeferServisi.baslat`'ın çağıranı gitti.
- Bilinen açıklar (kod değiştirilmedi): eskimiş sınıf yorumu, kullanılmayan `sunucuSecildi`, sunucuda silinen anahtarın geç
  fark edilmesi.
- Planlı işlerden bu dosyaya dokunması beklenenler:
  - "Android yerel uygulama" (iş 10): servisçi ekranıyla `sefer` gerçekten kullanılacak; "uygulamanın kendini güncellemesi"
    eki "günde en çok bir kez" sürüm denetimi istiyor — son denetim anı gibi küçük bir bilgi buraya gelebilir (öneri).
  - "Çok dil" (iş 22): seçili dil ve indirilen çeviri kataloğu telefonda saklanacak; uygun yer burası ya da yeni bir dosya
    (öneri).
  - "Sistem" (iş 4): yeni cihaz uyarısı ve açık oturumlar; sunucudaki `GET /api/cihaz` listesi (telefonun adı, sürümü, son
    görülme) uygulamada gösterilirse bu anahtarın sahibi olan telefon "bu telefon" diye işaretlenebilir (öneri).

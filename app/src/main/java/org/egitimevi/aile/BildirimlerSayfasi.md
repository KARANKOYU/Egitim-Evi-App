# app/src/main/java/org/egitimevi/aile/BildirimlerSayfasi.java

Uygulamanın "Bildirimler" sayfası (alt çubuktaki sekme ve üst çubuktaki zil): son 100 bildirimi gün gün kartlarda gösterir
(okunmamışlar solda noktalı ve kalın), açılınca hepsini okundu yapar ve zil rozetini tazeler.

## Bu dosya ne yapar?

Sitedeki zil panelinin uygulamadaki karşılığıdır. Ödev, mesaj, servis, okul duyurusu… o an açık oturuma (portala) gelen her
bildirim burada yeniden eskiye sıralanır ve gün başlıklarıyla ("BUGÜN", "DÜN", "12 EYLÜL, CUMA") kartlara bölünür. Sınıf
yorumundaki kurallar:

- okunmamışlar solda ana renkte bir nokta ve kalın yazıyla görünür;
- sayfa açılınca hepsi okundu sayılır (sitedeki gibi);
- bildirime dokununca ilgili ekran açılır (`Sekmeler.baglantiAc`) — bugün yalnız `#/profil` bağlantısı bir ekran açıyor
  (aşağıda "Dikkat!").

Sayfa `Sayfa.Veri`'den türer: "yükleniyor" göstergesi, hata kutusu ("Bir sorun oldu" + ileti + "Yeniden dene") ve üst çubuktaki
"Yenile" düğmesi oradan hazır gelir; bu dosya yalnız hangi adresin isteneceğini ve gelen cevabın nasıl çizileceğini söyler.

Kim görür: giriş yapan herkes (öğrenci, veli, öğretmen, müdür, servisçi, yönetici ve henüz rolü olmayan yetişkin).

## İçinde neler var?

- `baslik()` → "Bildirimler".
- `adres()` → `"/api/notifications"` — `Sayfa.Veri.yukle` bunu [Ag.md](Ag.md) ile ister (oturum anahtarıyla).
- `ciz(JSONObject j)` — cevaptan görünümü kurar:
  1. `notifications` dizisi yoksa ya da boşsa boş durum: zil simgesi (`ik_bildirim`), "Bildirim yok", "Ödev, mesaj, servis ve
     okuldan haberler burada görünür.".
  2. Değilse her bildirim için gün başlığı `Zaman.gunBasligi(createdAt)` ("Bugün", "Dün" ya da "12 Eylül, Cuma"; Türkiye
     saatiyle) hesaplanır. Gün değişince yeni bir bölüm etiketi (büyük harfle; tarih okunamadıysa "Daha eski") ve yeni bir kart
     açılır (ilk bölüm 4 dp, sonrakiler 18 dp boşlukla); aynı gündeki satırlar ince çizgiyle ayrılır.
  3. Cevaptaki `unread` 0'dan büyükse `POST /api/notifications/read` (gövdesiz → `{}`) gönderilir; başarıda
     `e.rozetTazele()` ile zil rozeti güncellenir ([AnaEkran.md](AnaEkran.md)); hata sessizce yok sayılır.
- `satir(JSONObject b)` (iç) — bir bildirim satırı:
  - solda 9 dp yuvarlak nokta: okunmamışsa (`read` `false`) ana renk, okunmuşsa saydam (yer tutar, hizayı korur);
  - başlık bildirimin metni (`text`), 15 sp; okunmamışsa kalın (`Tema.KALIN`), değilse normal (`Tema.GOVDE`);
  - alt yazı göreli zaman `Zaman.goreli(createdAt)`: "az önce", "5 dk önce", "14:32", "Dün 09:10", "12 Eylül", "12 Eylül 2025";
  - `link` doluysa satır dokunulabilir (`Arayuz.tiklananSatir`: dalgalanır, sağda ok) ve dokununca
    `Sekmeler.baglantiAc(e, link)`; boşsa düz satır (`Arayuz.satir`).
- `Sayfa.Veri`'den gelenler: `yenilenir()` = `true` (üst çubukta "Yenile"), `yenile()` = `yukle()` (adresi yeniden ister).

## Kimle konuşur?

- Aynı paketten çağırdıkları:
  - `Sayfa.java` (`Sayfa.Veri`: `olustur`, `yukle`, `kap`); [Ag.md](Ag.md) — `get` (`yukle` içinden), `post`.
  - `Zaman.java` — `gunBasligi`, `goreli`.
  - [Arayuz.md](Arayuz.md) — `bosDurum`, `sayfaGovdesi`, `bolumEtiketi`, `kart`, `ekle`, `ayirici`, `satir`, `tiklananSatir`;
    `Tema.java` — `dp`, `renk`, `KALIN`, `GOVDE`; `R.color.ana`, `R.drawable.ik_bildirim`.
  - `Sekmeler.java` — `baglantiAc`; [AnaEkran.md](AnaEkran.md) — `rozetTazele`.
- Sunucu uçları (site deposunda `sunucu/bolumler/kayit.md`):
  - **`GET /api/notifications`** (giriş gerekir) — `surum` gönderilmediği için her zaman tam liste:
    `{ notifications: [{ id, userId, text, link, read, createdAt }], unread, surum }`; son 100 bildirim, yeniden eskiye
    (alanların adı `sunucu/veri/esleme.js` `bildirim`'den). Liste o an açık oturumun satırına (portala) aittir.
  - **`POST /api/notifications/read`** — kişinin BÜTÜN okunmamış bildirimlerini okundu yapar.
  - Kapılar (`sunucu/api.md`): `notifications` rolsüz yetişkine açık (`ROLSUZ_SERBEST`), ama aydınlatma onayı eskiyse 403
    `kvkkGerek` döner ve onay ekranını [AnaEkran.md](AnaEkran.md)'nin `genelHata`'sı açar; oturum düştüyse 401 → giriş ekranı.
- Onu açanlar (grep):
  - `Sekmeler.java` — `BildirimlerSayfasi::new`, ikinci sekmenin kökü.
  - [AnaEkran.md](AnaEkran.md) — üst çubuktaki zil (`git(new BildirimlerSayfasi())`), telefon bildirimine dokunulunca
    (`bildirimdenAc`); ayrıca `s instanceof BildirimlerSayfasi` iken zil düğmesini gizler.
- Telefon bildirimleriyle ilişkisi: bildirim çubuğundaki bildirimleri [Bildirimler.md](Bildirimler.md) getirir; ona dokununca
  bu sayfa açılır.

## Nasıl çalışır (adım adım)?

```
"Bildirimler" sekmesi / zil / telefon bildirimine dokunma
   └─► olustur() (Sayfa.Veri) → kap + "yükleniyor" → GET /api/notifications
          hata  → "Bir sorun oldu" · ileti · [Yeniden dene]
          boş   → (zil) "Bildirim yok"
          dolu  → BUGÜN
                  ┌──────────────────────────────────────────┐
                  │ ●  Matematik: yeni ödev "Oran orantı"     │  ← kalın: okunmamış
                  │    az önce                            >   │  ← link var: dokunulabilir
                  ├──────────────────────────────────────────┤
                  │    Servis yarın 10 dk geç kalkacak        │  ← okunmuş
                  │    09:12                                  │
                  └──────────────────────────────────────────┘
                  DÜN  [...]   12 EYLÜL, CUMA  [...]
                  unread > 0 → POST /api/notifications/read → rozetTazele (zil rozeti kalkar)
satıra dokun → Sekmeler.baglantiAc: "…#/profil" → AyarlarSayfasi ; öteki bağlantılar → hiçbir şey
"Yenile" → GET yeniden (eski liste cevap gelene kadar ekranda kalır)
```

(Örnek metinler uydurmadır.)

## Dikkat!

- **Satırların çoğu dokunulabilir görünür ama bir şey açmaz.** Sunucunun bildirim bağlantıları `#/odevler`, `#/servis`,
  `#/mesajlar`, `#/devamsizligim` gibi sitenin sayfalarıdır; `Sekmeler.baglantiAc` bugün yalnız `profil`'i tanır (rol ekranları
  henüz yok). Öteki satırlar dalgalanır, sağda ok vardır, ama dokununca hiçbir şey olmaz.
- **Okundu işareti ekranda kalır.** `POST /read` sonrasında liste yeniden çizilmez: noktalar ve kalın yazı ancak "Yenile"de
  ya da sayfa yeniden kurulunca gider. Zil rozeti ise hemen kalkar.
- **Açmak = hepsini okundu yapmak.** Sunucu kişinin bütün okunmamışlarını işaretler: listede görünmeyenleri (100'den eskiler)
  ve GET ile POST arasında yeni gelmiş, ekranda hiç görünmemiş bildirimi de. Okunmuş bildirim telefona da gitmez
  ([Bildirimler.md](Bildirimler.md) yalnız okunmamışları alır).
- **Liste yalnız açık portalın.** Yetişkin hesabında telefon bütün portallara gelen bildirimleri alır, ama bu sayfa o an hangi
  portaldaysan (öğretmen@A, veli…) onun bildirimlerini gösterir; başka portalın bildirimine telefondan dokunan kişi onu burada
  bulamaz (kod okumasına göre).
- **Sürüm kullanılmıyor.** Site `?surum=` göndererek "değişmediyse liste gönderme" der; bu sayfa her açılışta ve her
  yenilemede son 100 bildirimin tamamını ister.
- **"Daha eski" başlığı aslında "tarihi okunamadı" demektir.** Eski tarihli bildirimler kendi tarih başlıklarıyla görünür;
  "Daha eski" yalnız `createdAt` ayrıştırılamazsa çıkar (normalde hiç görünmez).
- **Okundu isteğinin hatası sessiz.** Ağ koparsa rozet eski kalır ve bildirimler sunucuda okunmamış durur; bir sonraki açılışta
  yeniden denenir. (401 ya da `kvkkGerek` gibi genel durumları yine [AnaEkran.md](AnaEkran.md) karşılar.)
- **`gorundu` yok.** Bu sayfanın üstüne açılan bir sayfa (ör. `profil` bağlantısıyla açılan Ayarlar) kapanınca liste
  tazelenmez; sekmeler arası geçişte de.
- **Görünüm `Arayuz.satir`'ın iç düzenine dayanıyor.** Başlığı kalınlaştırmak için `getChildAt(1)` (orta) → `getChildAt(0)`
  (başlık) yolu kullanılıyor; solda hep nokta olduğu için doğru. `satir`'ın düzeni değişirse burası da değişmeli
  ([Arayuz.md](Arayuz.md)).
- Aynı anda iki Bildirimler sayfası olabilir: ikinci sekmenin kökü ve zilin başka bir sekmenin yığınına koyduğu sayfa; ikisi de
  ayrı ayrı yüklenir. Zil, üstteki sayfa Bildirimler iken gizlenir.

## Testleri

- Android deposunda otomatik test yok; derleme ve lint (`./gradlew --offline assembleDebug lintDebug`) ana oturumda.
- Sunucu sözleşmesini site deposundaki testler korur: `testler/test-bildirim.js` (liste, `surum`; `POST /api/notifications/read`
  sonrası sürüm değişir, liste yeniden gelir, `unread` 0), `testler/test-servis-yoklama.js` (okunmuş bildirim telefona gitmez),
  `testler/test-yonetim.js` (onayı eski kişiye 403 `kvkkGerek`), `testler/test-okul-agi.js` (okul ağından aynı anda gelen
  `notifications` istekleri).
- Elle (öykünücü ya da telefon):
  - Siteden kişiye iki bildirim üret (ör. öğretmenle ödev ver, mesaj gönder), uygulamada "Bildirimler" sekmesini aç → "BUGÜN"
    kartında iki kalın, noktalı satır; zil rozeti kalkar; "Yenile" → noktalar gider.
  - Hiç bildirimi olmayan yeni bir hesapla aç → "Bildirim yok".
  - Uçak kipinde "Yenile" → "Bir sorun oldu", "İnternet bağlantısı yok ya da Eğitim Evi'ne ulaşılamadı." ve "Yeniden dene".

## Son durum

- `git log`: 1 commit. Dosya `e96c5f2 commit 6` (2026-09-26) ile geldi: yerel uygulamanın çekirdeği yazılırken sitedeki zil
  panelinin karşılığı olarak eklendi; o günden beri değişmedi.
- Bilinen açıklar (kod değiştirilmedi): bağlantıların çoğunun bir şey açmaması, okundu işaretinin ekranda kalması, GET ile
  POST arasındaki bildirimin görülmeden okundu sayılması, `surum`'un kullanılmaması.
- Planlı işlerden bu sayfaya dokunması beklenenler:
  - "Mesaj ayarları, …" işinin eki (bildirim paneli sekmeler hâlinde): Tümü · Ödev · Sınav · Devamsızlık · Mesaj · Duyuru · Servis
    sekmeleri (okulda kapalı bölümün sekmesi görünmez, sekmede okunmamış sayısı), "Tümünü okundu say", "25 Eylül 2026 Cuma
    15:57" biçiminde tarih; bildirim türü sunucuda yeni `tur` alanıyla gelecek. Tanım "Telefon uygulamasındaki Bildirimler
    sayfası da aynı sekmeleri kullanır" diyor.
  - "Android yerel uygulama" — bildirim bağlantıları `Sekmeler.baglantiAc` ile doğru ekranı açacak (rol ekranları).
  - "Optimizasyon + saklama süreleri" — bildirimler 90 gün sonra silinecek.
  - "Çok dil" — sayfadaki sabit metinler ve tarih biçimleri.

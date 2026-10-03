# app/src/main/java/org/egitimevi/aile/KodSayfasi.java

İki adımlı girişin ikinci adımı: e-postaya gelen 6 haneli kodu sorar, altı hane yazılınca kendiliğinden gönderir, doğruysa
oturumu açar; 60 saniyelik geri sayımla "Yeni kod gönder" sunar.

## Bu dosya ne yapar?

Eğitim Evi'nde öğrenci dışındaki e-postalı her hesap (veli, öğretmen, müdür, yönetici) şifreyi doğru yazınca hemen içeri
alınmaz: sunucu e-posta adresine 6 haneli bir **giriş kodu** gönderir. Kod 5 dakika geçerlidir, bir kez kullanılır; aynı
koda en çok 5 deneme hakkı vardır. Öğrenci ve e-postası olmayan hesap (okulun açtığı servisçi gibi) bu adımı görmez.

[GirisSayfasi.md](GirisSayfasi.md) şifreyi gönderdiğinde sunucu `twoFactor: true` derse bu sayfa açılır; elinde sunucunun
verdiği **giriş oturumu kimliği** (`challengeId`) ve gösterilecek cümle ("Giriş kodu a***e@… adresine gönderildi.") vardır.
Kişi, kodu yazar; altıncı rakamla birlikte istek kendiliğinden gider, düğmeye basmak gerekmez. Kod doğruysa sunucu oturum
anahtarını verir ve [AnaEkran.md](AnaEkran.md) akışı sürdürür (aydınlatma metni onayı, zorunlu şifre ya da rolün
sekmeleri). Kod gelmediyse 60 saniye sonra yeni kod istenebilir.

Sayfa giriş akışının bir parçasıdır: üst ve alt çubuk görünmez, giriş sayfasının üstüne açılır.

## İçinde neler var?

### Alanlar

- `kimlik` — giriş oturumu kimliği (`challengeId`); "Yeni kod gönder"den sonra sunucunun verdiği yenisiyle değişir.
- `bilgi` — giriş cevabındaki `mesaj` (boşsa "Giriş kodu e-posta adresine gönderildi.").
- `kod` — kod kutusu ([Arayuz.md](Arayuz.md)'deki `Alan`); `dogrula` ("Girişi tamamla"), `tekrar` ("Yeni kod gönder").
- `ana` — ana iş parçacığı `Handler`'ı (geri sayım); `bekle` — kalan saniye, 60'tan başlar.

### Dışa açık

- `KodSayfasi(kimlik, bilgi)` — kurucu; [GirisSayfasi.md](GirisSayfasi.md) `new KodSayfasi(j.optString("challengeId"),
  j.optString("mesaj"))` ile açar.
- `baslik()` — "Giriş kodu"; `cubuksuz()` — `true`.
- `olustur()` — sayfa:
  - Sol üstte "← Geri" (hayalet düğme) → `e.kapat()` (giriş sayfasına döner).
  - Başlık "E-postana gelen kodu yaz", altında `bilgi` (soluk).
  - Kart: "6 haneli kod" kutusu — ipucu `000000`, yalnız rakam klavyesi (`TYPE_CLASS_NUMBER`), en çok 6 karakter
    (`LengthFilter(6)`), 26 sp, harf aralığı 0.4, ortalı, eş aralıklı yazı tipi (`Tema.KOD`). Metin izleyicisi: uzunluk 6
    olunca `gonder()`. Altında "Girişi tamamla" (birincil) ve geri sayım düğmesi (hayalet).
  - Kartın altında: "Kod gelmediyse istenmeyen (spam) klasörüne bak. Kod 5 dakika geçerlidir."
  - Geri sayımı başlatır ve kutuya odaklanır.
- `geriBas()` — telefonun geri tuşu/hareketiyle çıkarken geri sayımı durdurur; `false` döner (geri işlemi her zamanki gibi
  sürer: sayfa kapanır). Uygulamada `geriBas`'ı kullanan TEK sayfa bu.

### İç

- `sayac()` — bekleyen geri sayımı siler, sonra saniyede bir: `bekle > 0` iken düğme yazısı "Yeni kod N saniye sonra
  istenebilir" ve düğme meşgul (soluk, basılamaz), `bekle` bir azalır; 0 olunca "Yeni kod gönder" ve düğme açılır.
- `gonder()` — kutudaki değer (`deger()`, kırpılmış) `^\d{6}$` değilse kutunun altında "Kod 6 rakamdır."; değilse hatayı
  siler, "Girişi tamamla"yı meşgul yapar ve `POST /api/login/dogrula { challengeId, code, uygulama: true }`:
  - başarı → geri sayımı durdurur, `e.oturumAc(cevap)` (cevap: `token`, `user`, `children`, portallar, `kvkkGuncel` …).
  - hata → düğme açılır, sunucunun iletisi kutunun altına yazılır, kutu boşaltılır (kişi baştan yazar).
- `tekrarGonder()` — düğmeyi meşgul yapar, `POST /api/login/tekrar { challengeId }`:
  - başarı → `kimlik` yeni `challengeId` olur (yoksa eskisi kalır), alt mesaj sunucunun `mesaj`'ı ("Yeni kod a***e@…
    adresine gönderildi.", yoksa "Yeni kod gönderildi."), `bekle = 60` ve geri sayım yeniden başlar.
  - hata → düğme açılır, ileti alt mesajda.

## Kimle konuşur?

- Çağırdıkları:
  - [Ag.md](Ag.md) — `Ag.post` (oturumsuz istek; Bearer başlığı yok). Bu sayfada oturum henüz açık olmadığı için sunucunun
    401'i "oturum düştü" sayılmaz, sayfanın kendi hata yoluna gelir (tek istisna: aynı kodla giden iki istek; "Dikkat!").
  - [AnaEkran.md](AnaEkran.md) — `oturumAc`, `kapat`, `bildir`; `e` sayfanın ana ekranı.
  - [Arayuz.md](Arayuz.md) — `sayfaGovdesi`, `dugme` (`HAYALET`, `BIRINCIL`), `baslik`, `yazi`, `kart`, `alan`, `ekle`,
    `soluk`, `mesgul`; `Tema.java` — `dp`, `KOD`.
  - `Sayfa.java` — taban sınıf (`baslik`, `olustur`, `cubuksuz`, `geriBas`).
- Onu açan (grep): [GirisSayfasi.md](GirisSayfasi.md) — `POST /api/login` cevabında `twoFactor` gelince
  `e.git(new KodSayfasi(...))`.
- Sunucu uçları (site deposunda `sunucu/bolumler/kayit.md`; kodları tutan `sunucu/guvenlik.js`):
  - `POST /api/login/dogrula { challengeId, code, uygulama }` — kod 5 dakika geçerli, bir kez kullanılır, her koda en çok
    5 deneme (biçimi bozuk kod da bir deneme sayılır). Hatalar 401 ile: "Giriş oturumu bulunamadı, tekrar giriş yap",
    "Kodun süresi doldu, tekrar giriş yap", "Kod 6 haneli olmalı", "Kod hatalı. Kalan hakkın: N" (beşinci yanlışta
    "Kalan hakkın: 0"); altıncı denemede "Çok fazla hatalı kod denemesi. Baştan giriş yap" (kod o anda silinir).
    Aynı bağlantıdan 5 dakikada 25 yanlış kod → 429 "Çok fazla yanlış kod denendi. Biraz bekleyip tekrar dene.". Başarı:
    girişle aynı oturum cevabı; `uygulama: true` (girişte ya da burada) oturumu 30 gün geçerli yapar (tarayıcıda 7 gün).
  - `POST /api/login/tekrar { challengeId }` — son gönderimden 60 saniye geçmediyse 429 "N saniye sonra yeni kod
    isteyebilirsin."; kayıt yoksa 400 "Giriş oturumu bulunamadı, baştan giriş yap". Başarıda eski kod geçersiz olur, yeni
    kod ve YENİ `challengeId` gelir (`{ challengeId, yontem, mesaj }`); "uygulama" işareti yeni koda taşınır.
  - Deneme sunucusunda e-posta ayarlı değilse ya da adres `.test` gibi teslim edilmeyen bir uzantıdaysa kod e-postaya değil
    sunucu penceresine yazılır; o zaman `bilgi` "Kod sunucu penceresine (siyah ekran) yazıldı: …" olur.
- Rol: öğrenci dışındaki e-postalı herkes (veli, öğretmen, müdür, yönetici).

## Nasıl çalışır (adım adım)?

```
GirisSayfasi: POST /api/login { kimlik, password, uygulama: true }
   → { twoFactor: true, challengeId: "9f…", mesaj: "Giriş kodu a***e@… adresine gönderildi." }
   → e.git(new KodSayfasi("9f…", mesaj))
KodSayfasi.olustur → geri sayım: "Yeni kod 60 saniye sonra istenebilir" … "Yeni kod gönder"
kişi yazar: 4 8 2 0 1 7  ── 6. rakam ──► gonder()
   POST /api/login/dogrula { challengeId: "9f…", code: "482017", uygulama: true }
     200 → e.oturumAc(cevap) → AnaEkran.akisiBaslat → (onay? şifre? sekmeler)
     401 → kutunun altında "Kod hatalı. Kalan hakkın: 4", kutu boşalır
"Yeni kod gönder" (60 sn sonra) → POST /api/login/tekrar { challengeId: "9f…" }
     → { challengeId: "c3…", mesaj } → kimlik = "c3…", geri sayım baştan
```

## Dikkat!

- **Haklar bitince sayfa kendiliğinden dönmez.** Beş yanlıştan sonra ("Kalan hakkın: 0") bir sonraki denemede sunucu kodu
  siler ve "Çok fazla hatalı kod denemesi. Baştan giriş yap" der; kodun 5 dakikası dolunca "Kodun süresi doldu…", sunucu
  yeniden başlayınca (kodlar yalnız bellekte tutulur) "Giriş oturumu bulunamadı…" gelir. Sayfa iletiyi gösterir ama
  kişiyi giriş sayfasına götürmez; kod silindikten sonra "Yeni kod gönder" de "Giriş oturumu bulunamadı, baştan giriş
  yap" alır. Kişi "← Geri" ile dönüp yeniden giriş yapmalı.
- **Kendiliğinden gönderme meşguliyete bakmaz.** Altıncı rakam her yazıldığında `gonder()` çalışır; "Girişi tamamla"
  meşgulken de. İstek sürerken bir rakam silinip yeniden yazılırsa aynı kodla ikinci bir istek gider; biri başarılı
  olur, öbürü 401 "Giriş oturumu bulunamadı…" alır. 401 önce gelirse ileti kutunun altında görünür, kutu boşalır,
  ardından başarı gelip giriş tamamlanır. Ama başarı önce gelirse oturum açılmış olur; [Ag.md](Ag.md) ikinci isteğin
  401'ini önce [AnaEkran.md](AnaEkran.md)'nin `genelHata`'sına sorar ve oturum açıkken gelen 401 "oturum düştü"
  sayılır: kişi "Oturumunun süresi doldu. Yeniden giriş yap." iletisiyle girişe atılır. Nadir (kişinin istek sürerken
  rakam silip yeniden yazması gerekir); kod okumasına göre, telefonda denenmedi. Düzeltme önerisi: `gonder()` başta
  `dogrula` meşgulse hiçbir şey yapmasın.
- **"← Geri" düğmesi geri sayımı durdurmaz.** Sayfadaki düğme `e.kapat()` çağırır, `geriBas()` yalnız telefonun geri
  tuşunda çalışır. Düğmeyle çıkılırsa geri sayım en çok 60 saniye daha görünmeyen bir düğmeyi günceller; zararsız.
- **Yalnız 6 rakam.** Kutu rakam klavyesi ve 6 karakter sınırıyla kurulu; `gonder` de `^\d{6}$` ister. Planlı doğrulama
  uygulaması (TOTP) 6 rakam olduğu için uyar, ama tek kullanımlık kurtarma kodları gelirse bu kutu onları kabul etmez
  (aşağıda "Son durum").
- **429'da geri sayım yeniden kurulmaz.** "Yeni kod gönder" sunucudan "N saniye sonra…" alırsa ileti alt mesajda görünür,
  düğme hemen açılır; geri sayım sunucunun kalan süresine göre yeniden başlamaz.
- **Hata kutuyu boşaltır.** Her hatada kod kutusu silinir; kişi kodun tamamını yeniden yazar (yanlış yazdığı rakamı
  görmez).
- **Uygulama oturumu 30 gün.** Hem girişte hem burada `uygulama: true` gider; sunucu bunu kodun kaydına da yazdığı için
  yalnız birinde gitmesi yeterdi. Bu, telefonda oturumun tarayıcıdaki 7 gün yerine 30 gün sürmesini sağlar (aydınlatma
  metninin saklama bölümü bunu söyler: giriş oturumu tarayıcıda 7 gün, telefon uygulamasında 30 gün).
- Başlık "E-postana gelen kodu yaz" sabit; kodun sunucu penceresine yazıldığı deneme ortamında da aynı başlık görünür
  (altındaki `bilgi` doğrusunu söyler).

## Testleri

- Android deposunda otomatik test yok; derleme ve lint (`./gradlew --offline assembleDebug lintDebug`) ana oturumda.
- Sunucu sözleşmesini site deposundaki testler korur:
  - `testler/guvenlik-test.js` — yanlış kod reddi, doğru kodla giriş, aynı kodun ikinci kez kullanılamaması (401),
    olmayan giriş oturumu.
  - `testler/test-admin-gizli.js`, `testler/test-cakisma.js`, `testler/test-servis-yoklama.js` — iki adımlı girişi
    `POST /api/login/dogrula` ile tamamlar; `test-servis-yoklama.js` uygulama oturumunun 30 gün sürdüğünü de dener.
  - `POST /api/login/tekrar`'ı (60 saniye kuralı, yeni `challengeId`) doğrudan deneyen bir test yok (grep).
- Elle (öykünücü, deneme paketi; uygulamanın sunucu adresi deneme sunucusuna çevrilmiş olmalı — uygulamada bunun için
  bir ekran yok, bkz. [Ag.md](Ag.md) "Dikkat!"): e-postalı bir veli/öğretmen hesabıyla gir → sunucu penceresindeki kodu
  (`.test` gibi deneme adreslerine e-posta gitmez) yaz → altıncı rakamda kendiliğinden girmeli. Yanlış kod
  → "Kod hatalı. Kalan hakkın: 4" ve boş kutu. Geri sayım bitince "Yeni kod gönder" → yeni kod; eskisi artık geçmemeli.
  Beş yanlıştan sonra ("Kalan hakkın: 0") altıncı denemede "Çok fazla hatalı kod denemesi. Baştan giriş yap".

## Son durum

- `git log` (Android deposu): 2 commit.
  - `822d445 commit 7` (2026-09-26): doğrulama gövdesine `uygulama: true` eklendi (aynı commit'te
    [GirisSayfasi.md](GirisSayfasi.md)'deki giriş gövdesine de) — uygulamadan açılan oturum 30 gün geçerli olsun diye.
  - `e96c5f2 commit 6` (2026-09-26, yerel uygulamanın çekirdeği): ilk hâli.
- Bilinen açıklar (kod değiştirilmedi): kod silindikten/süresi dolduktan sonra sayfanın kişiyi girişe döndürmemesi;
  kendiliğinden göndermenin meşguliyete bakmaması (nadir sırada yeni açılan oturumdan atılma); "← Geri" düğmesinin geri
  sayımı durdurmaması.
- Planlı işlerden bu dosyaya dokunması beklenenler:
  - "Android geri tuşu / geri hareketi" (Android yerel uygulama tanımının eki; Linux'ta yapılacak): tanım bugünkü
    `geriBas` kullanımının yalnız bu sayfada olduğunu not ediyor; her sayfa kendi açılır parçaları için `geriBas` yazacak
    ve ortak bir "açık katmanlar" yığını önerilecek.
  - "Sistem" işindeki doğrulama uygulaması (TOTP, 6 hane): iş listesine göre yöneticide (ve destekte) zorunlu, öbür
    hesaplarda (yetişkinler; sonradan alınan bir kararla öğrenci de) isteğe bağlı açılabilecek; açık olan hesapta ikinci
    adım e-posta kodu YERİNE uygulama kodu ister ve tek kullanımlık kurtarma kodunu da kabul eder. O zaman bu sayfanın
    başlığı/metni ve yalnız 6 rakam kabul eden kutusu değişmeli.
  - "Üst şerit sadeleştirme": doğrulama ekranlarında sol üstte "←" (vazgeç) — buradaki "← Geri" ile birleşebilir.
  - "Çok dil": sayfadaki sabit Türkçe metinler; sunucu iletileri istemcinin diliyle gelecek.

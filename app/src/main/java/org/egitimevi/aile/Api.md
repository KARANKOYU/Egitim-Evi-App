# app/src/main/java/org/egitimevi/aile/Api.java

Uygulamanın sunucuyla konuştuğu en alt kat: tek bir `HttpURLConnection` isteği kurar, JSON gönderip alır, isteğin hangi
kimlikle (oturum, uygulama anahtarı ya da Aile anahtarı) gideceğini başlıkla seçer, adresin https olmasını şart koşar ve
her sorunu `Api.Hata`'ya çevirir.

## Bu dosya ne yapar?

Telefondaki her ağ isteği sonunda buradaki `istek`'ten geçer. Dosya kasıtlı olarak "aptal"dır: iş parçacığı açmaz, ekran
bilmez, oturumu kendisi okumaz. Ona sunucu adresini, yolu, gövdeyi ve kimliği sen verirsin; o da cevabın JSON'unu döner ya
da `Api.Hata` fırlatır. İsteği arka plana atmak, cevabı ekrana taşımak ve "oturum düştü" gibi genel durumları karşılamak bir
üst katın, [Ag.md](Ag.md)'nin işidir.

Uygulamada sunucuya üç ayrı kimlikle gidilir ve bu dosya hangisinin hangi başlıkla gideceğini bilir:

1. **Oturum** (`Authorization: Bearer <anahtar>`) — giriş yapan kişinin bütün ekranları. Girişte sunucunun verdiği oturum
   anahtarı `Oturum.java`'da saklanır; [Ag.md](Ag.md) her istekte onu buraya verir.
2. **Uygulama anahtarı** (`X-Cihaz: <64 onaltılık>`) — girişten sonra telefonun aldığı, yalnız bildirim yoklamaya,
   servisçinin sefer konumunu göndermeye ve kendini silmeye yarayan anahtar (sunucuda bir de `GET /api/cihaz/ayar` var;
   uygulama bugün onu çağırmıyor). Hesaba giriş vermez. `UygulamaAyar.java`'da durur.
3. **Aile anahtarı** (`X-Aile-Cihaz: <64 onaltılık>`) — "Çocuğun telefonu" bağlanınca alınan, yalnız konum ve ekran süresi
   göndermeye yarayan anahtar ([AileEkrani.md](AileEkrani.md)). `Ayarlar.java`'da durur.

Sunucu bu iki anahtarı birbirinden ayırır: uygulama anahtarı Aile uçlarında, Aile anahtarı uygulama uçlarında 401 alır.

Bir de güvenlik kuralı taşır: internetteki bir sunucuya yalnız `https://` ile gidilir; `http://` yalnız evdeki deneme
sunucusu (yerel ağ adresi) içindir. Bu dosya ilk sürümde (Eğitim Evi Aile 1.0.x) yalnız Aile anahtarını biliyordu; tek
uygulama "Eğitim Evi"ye geçişte uygulama anahtarı ve oturumlu istek eklendi.

## İçinde neler var?

### Sabitler

- `CIHAZ_BASLIGI` = `"X-Aile-Cihaz"` — çocuğun telefonunun (Aile) anahtar başlığı. Adı 1.0.x'ten kalma: o zaman tek cihaz
  anahtarı buydu. `get`, `post` ve `oturumla` başlık adı olarak bunu kullanır (oturumla'da anahtar `null` olduğu için başlık
  hiç eklenmez).
- `UYGULAMA_BASLIGI` = `"X-Cihaz"` — uygulamanın anahtar başlığı; yalnız `uygulama(...)` kullanır.

### `Api.Hata` (sunucunun ya da adresin sorunu)

`IOException`'dan türer; böylece çağıran tek bir `catch (IOException)` ile ağ kopmasını da sunucu hatasını da yakalayabilir.
Ayrı ayrı yakalamak istersen önce `Api.Hata`'yı yakala (alt sınıf).

- `durum` — HTTP durum kodu (400, 401, 403, 409, 429, 500…). `0`: istek sunucuya hiç gitmedi (adres geçersiz ya da http
  kuralına takıldı). [Ag.md](Ag.md) ağ kopmasını da `durum = 0` olan bir `Hata`'ya çevirir.
- `govde` — sunucunun hata cevabının TAMAMI (`JSONObject`); hiç gövde yoksa boş nesne. Bugün okunan bayraklar: `alan`
  (hangi form kutusu kırmızı olsun: `kimlik`, `sifre`, `bot` — `GirisSayfasi`; `eski` — `SifreSayfasi`; `KayitSayfasi`'nın
  alanları), `okulSec` ve `soruGerekli` (`GirisSayfasi`), `kvkkGerek` ve `sifreDegismeli` ([AnaEkran.md](AnaEkran.md)
  `genelHata`). Sunucunun koyduğu ama bugün okunmayanlar: `rolsuz`, `kilitli`, `kalanHak`, `anahtarGecersiz` gibi.
- İleti (`getMessage()`) — sunucunun `error` alanı; o yoksa `"Sunucu hatası (502)"` gibi kodlu genel cümle.
- Yapıcılar: `Hata(durum, mesaj)` (gövde boş nesne) ve `Hata(durum, mesaj, govde)`.

### `adresSorunu(adres)` — sunucu adresi kurala uyuyor mu?

Sorun yoksa `null`, varsa gösterilecek Türkçe cümle döner:

| Adres | Sonuç |
|---|---|
| `https://…` (her ana makine) | `null` (geçer) |
| `http://localhost…`, `http://10.…`, `http://192.168.…`, `http://172.16.…`–`http://172.31.…` | `null` (yerel ağ, geçer) |
| `http://` + başka her ana makine | "İnternetteki sunucuya yalnızca https:// ile bağlanılır." |
| `ftp://…` gibi başka bir şema | "Adres https:// ile başlamalı." |
| Ayrıştırılamayan metin (boş, şemasız…) | "Adres geçersiz." |

`istek` her istekten önce bunu çağırır; [AileEkrani.md](AileEkrani.md) de giriş düğmesine basılınca ve doğrulama sorusunu
istemeden önce kutudaki adresi bununla denetler.

### İstek işlevleri (hepsi BEKLETİR — ana iş parçacığında çağırma)

| İşlev | Yöntem | Kimlik başlığı | Bugün kim, hangi uçla kullanıyor |
|---|---|---|---|
| `get(sunucu, yol, cihaz)` | GET | `cihaz` boş değilse `X-Aile-Cihaz` | [AileEkrani.md](AileEkrani.md): `GET /api/challenge` (anahtarsız); `IzlemeServisi`: `GET /api/aile/cihaz/ayar` |
| `post(sunucu, yol, govde, oturum, cihaz)` | POST | `oturum` varsa Bearer, `cihaz` varsa `X-Aile-Cihaz` | [AileEkrani.md](AileEkrani.md): `POST /api/login`, `/api/aile/cihaz` (Bearer), `/api/logout` (Bearer), `/api/aile/cihaz/sil` (Aile anahtarı); `IzlemeServisi`: `/api/aile/cihaz/konum`, `/api/aile/cihaz/kullanim` |
| `oturumla(sunucu, yol, yontem, govde, oturum)` | verilen `yontem` | Bearer | [Ag.md](Ag.md) (uygulamanın bütün ekranları), [AnaEkran.md](AnaEkran.md) `cikis` → `POST /api/logout` |
| `uygulama(sunucu, yol, govde, anahtar)` | `govde` `null` ise GET, değilse POST | `X-Cihaz` | `Bildirimler`: `GET /api/cihaz/bildirimler?son=<imleç>`; `SeferServisi`: `POST /api/cihaz/servis-konum`; [AnaEkran.md](AnaEkran.md) `cikis` → `POST /api/cihaz/sil` |

Dördü de başarıda cevabın JSON'unu (`JSONObject`) döner; hata durumunda `Api.Hata`, ağ sorununda düz `IOException`
fırlatır.

### İç işlevler

- `istek(sunucu, yol, yontem, govde, oturum, baslik, cihaz)` — asıl iş (aşağıda adım adım).
- `oku(akis)` — akışı 8 KB'lık parçalarla sonuna kadar okur, UTF-8 metin döner; akışı kapatır.

## Kimle konuşur?

- Android/Java: `java.net.HttpURLConnection` ve `URL` (dış kütüphane yok; AndroidX de yok), `org.json.JSONObject`,
  `StandardCharsets.UTF_8`.
- İzin ve ağ ayarı: `AndroidManifest.xml`'deki `INTERNET` izni; `android:networkSecurityConfig="@xml/ag_guvenligi"`. Yayın
  paketinde `res/xml/ag_guvenligi.xml` şifresiz trafiği tümden kapatır (`cleartextTrafficPermitted="false"`); yalnız deneme
  paketinde `src/debug/res/xml/ag_guvenligi.xml` açar.
- Onu çağıranlar (grep):
  - [Ag.md](Ag.md) — `oturumla` (bütün ekran istekleri bundan geçer).
  - [AnaEkran.md](AnaEkran.md) — `uygulama` ve `oturumla` (çıkış); ayrıca `genelHata(Api.Hata)` bu sınıfın hatasını alır.
  - [AileEkrani.md](AileEkrani.md) — `adresSorunu`, `get`, `post`.
  - `IzlemeServisi.java` — `get`, `post` (Aile anahtarıyla konum, kullanım, ayar); 401/403'te bağlantıyı unutur.
  - `Bildirimler.java` — `uygulama` (bildirim yoklama); 401/403'te uygulama anahtarını unutur.
  - `SeferServisi.java` — `uygulama` (servisçinin sefer konumu).
- Sunucu tarafı (site deposundaki belgeler):
  - `sunucu/bolumler/kayit.md` — `POST /api/login`, `/api/logout`, `GET /api/challenge`, `GET /api/notifications` ve öteki
    oturumlu giriş/hesap uçları.
  - `sunucu/bolumler/cihaz.md` — `X-Cihaz` uçları (`/api/cihaz/bildirimler`, `/api/cihaz/ayar`, `/api/cihaz/servis-konum`,
    başlıklı `/api/cihaz/sil`) ve oturumla anahtar alma `POST /api/cihaz`.
  - `sunucu/bolumler/aile.md` — `X-Aile-Cihaz` uçları (`/api/aile/cihaz/ayar|konum|kullanim|sil`) ve öğrencinin oturumla
    bağlaması `POST /api/aile/cihaz`.
  - `sunucu/api.md` — anahtarlı uçların oturum kapılarından (aydınlatma, şifre, rolsüz) ÖNCE ayrıldığı yer.
  - `sunucu/http.md` — hata cevabının `{ error: ileti }` biçimi (`bad`); buradaki `optString("error")` buna dayanır.

## Nasıl çalışır (adım adım)?

```
çağıran (arka iş parçacığında): Api.oturumla(sunucu, "/api/notifications", "GET", null, oturumAnahtari)
  istek(...)
   1. adresSorunu(sunucu) ── sorun var ─► throw Hata(0, "İnternetteki sunucuya yalnızca https:// ile bağlanılır.")
   2. URL = sunucu (sondaki "/"lar silinir) + yol            → https://egitimevi.org/api/notifications
   3. yöntem; bağlanma 15 sn, okuma 20 sn zaman aşımı; Accept: application/json
   4. oturum varsa Authorization: Bearer …;  anahtar varsa <baslik>: …  (X-Cihaz ya da X-Aile-Cihaz)
   5. gövde varsa: UTF-8 JSON, Content-Type: application/json; charset=utf-8, sabit uzunlukla gönder
   6. durum = getResponseCode()   (ağ yoksa bağlanırken — 5'te ya da burada — IOException fırlar → çağırana gider)
   7. durum >= 400 ? hata akışı : normal akış  →  oku() → disconnect()
   8. metin boşsa {} ; JSON değilse {} ; değilse JSONObject
   9. durum >= 400 ─► throw Hata(durum, j.error || "Sunucu hatası (durum)", j)
      değilse     ─► return j
```

## Dikkat!

- **Bekletir.** Her işlev ağ cevabını bekler; ana iş parçacığında çağırırsan Android `NetworkOnMainThreadException`
  fırlatır. Ekranlar [Ag.md](Ag.md)'yi kullanmalı; servisler zaten kendi iş parçacıklarında çağırıyor.
- **http kuralı iki katlı.** `adresSorunu` yerel ağ adresinde http'ye izin verir, ama yayın paketinin ağ ayarı
  (`cleartextTrafficPermitted="false"`) şifresiz trafiği tümden kapatır. Yani yayın paketinde `http://192.168.1.20:3000`
  gibi bir adres `adresSorunu`'ndan geçer ama bağlantı Android'de düşer (`IOException`): [Ag.md](Ag.md) bunu "İnternet
  bağlantısı yok ya da Eğitim Evi'ne ulaşılamadı." diye gösterir. http yalnız deneme paketinde gerçekten çalışır (kod
  okumasına göre; telefonda denenmedi).
- **Yerel ağ denetimi metin üstünde.** `10.` ve `192.168.` yalnız başlangıç olarak aranır; `10.ornek.com` gibi bir alan adı da
  "yerel" sayılır ve deneme paketinde internetteki o makineye şifresiz gidilebilir. Tersine `127.0.0.1` ve IPv6 adresleri
  http'de reddedilir (yalnız `localhost` yazısı kabul). Yalnız deneme paketini etkiler; kod değiştirilmedi.
- **JSON olmayan başarılı cevap sessizce `{}` olur.** 2xx/3xx bir cevabın gövdesi JSON değilse hata fırlatılmaz, boş
  nesne döner. Örnekler: sunucunun önündeki bir vekil sunucunun 200 ile döndüğü HTML bakım sayfası, Aile ekranının kutusuna
  yanlışlıkla yazılmış ve her yola 200 ile HTML dönen başka bir sitenin adresi, ya da yerel http deneme sunucusunun başka
  bir adrese yönlendirmesi (`HttpURLConnection` şema değiştiren yönlendirmeyi izlemez, 3xx'i başarı sayar). (Otel Wi-Fi'si
  gibi araya giren ağlar https bağlantısında sertifikayı geçemez; o durumda `IOException` olur, `{}` değil.) Çağıran
  "başarılı ama boş" bir cevap görür: ör. giriş ekranında ne oturum ne iki adımlı kod gelir, düğme eski hâline döner,
  ileti çıkmaz; bildirim sayfası "Bildirim yok" der. 4xx/5xx'te JSON yoksa "Sunucu hatası (502)" gibi genel ileti çıkar.
  (Kod okumasına göre; telefonda denenmedi.)
- **`durum = 0`** "sunucuya ulaşılmadı" demektir (adres kuralı ya da [Ag.md](Ag.md)'de ağ kopması). `Bildirimler` ve
  `IzlemeServisi` yalnız 401/403'ü "anahtarım geçersiz" sayıp anahtarı unutur; `SeferServisi` 401/403'ün yanında 404
  (sefer yok) ve 409'da (sefer bitti ya da başka servisçiye geçti) da seferi bırakıp durur. Öteki her durumda (0 dahil)
  bir sonraki turda yeniden denenir.
- **Başlık adları kafa karıştırabilir:** `CIHAZ_BASLIGI` Aile anahtarıdır, uygulamanın anahtarı `UYGULAMA_BASLIGI`'dır
  (1.0.x'ten kalma ad). Yeni bir anahtarlı uç yazarken hangisini kullandığına dikkat et; yanlış başlık 401 alır.
- **`oturumla`'nın yorumu "govde null ise GET" der** ama yöntemi `yontem` parametresi belirler. `post(...)`'a `null` gövde
  verirsen gövdesiz bir POST gider; bugün kimse vermiyor ([Ag.md](Ag.md) `post` boş gövdeyi `{}` yapar).
- **Yeniden deneme, önbellek ya da dil başlığı yok; kullanıcı aracısını (User-Agent) kod koymaz** (Android'in varsayılanı
  gider). Zaman aşımı bağlanmada 15, okumada 20 saniye; okuma zaman aşımı her tek okuma için ayrı işlediğinden yavaş akan
  bir cevapta istek yarım dakikayı da aşabilir.
- **Bütün cevap belleğe okunur.** JSON için doğru; dosya indirmek ya da yüklemek için uygun değil (bugün uygulamada dosya
  işi yok).
- Hata yolunda (`getResponseCode` ya da gövde yazarken `IOException`) `disconnect()` çağrılmaz (`finally` yok); bağlantı
  çöp toplayıcıya kalır. Bugün görünür bir etkisi bilinmiyor.
- Sunucu adresini bu dosya seçmez: çağıran verir. Uygulamanın ekranları için adres `Ayarlar.sunucu(...)`'dur (ayrıntısı ve
  Aile bağlamasıyla ilişkisi [Ag.md](Ag.md)'de).

## Testleri

- Android deposunda otomatik test yok (`app/src/test` ya da `androidTest` klasörü yok). Derleme ve lint denetimi
  (`./gradlew --offline assembleDebug lintDebug`) Android işinin her aşamasında ana oturumda yapılır.
- Bu dosyanın dayandığı sunucu sözleşmesini site deposundaki testler korur:
  - `testler/test-servis-yoklama.js` — oturumla `POST /api/cihaz` 64 haneli anahtar verir; anahtar `/api/me`'ye giriş
    vermez (ne Bearer ne `X-Cihaz` ile); tanınmayan ya da biçimsiz `X-Cihaz` 401; başlıklı `POST /api/cihaz/sil` anahtarı
    kaldırır; hesap başına en çok 5 anahtar; şifre değişince anahtarlar düşer; uygulama anahtarı Aile uçlarında 401;
    `uygulama: true` girişte oturum 30 gün, çıkışta kapanır.
  - `testler/test-aile.js` — öğrenci onayla bağlar, veli kendi hesabıyla bağlayamaz (403), onaysız 400; Aile anahtarı
    oturum yerine geçmez ve uygulama uçlarında 401; anahtarsız/yanlış/biçimsiz anahtar 401; konum, kullanım, ayar uçları;
    öğrencinin uygulamadan kaldırması (`/api/aile/cihaz/sil`).
  - `testler/girdi-denetimi.js` ve `testler/yetki-denetimi.js` — `/api/cihaz` uçları bozuk girdi ve rol tablosunda.
- Elle (deneme paketi, öykünücü): "Çocuğun telefonu" ekranındaki sunucu kutusuna `http://ornek.com` yazıp "Giriş yap ve bu
  telefonu bağla"ya bas → "İnternetteki sunucuya yalnızca https:// ile bağlanılır."; `ftp://ornek.com` → "Adres https://
  ile başlamalı."; anlamsız bir metin → "Adres geçersiz." (bu denetim onay kutusundan da önce yapılır). Öykünücüden
  bilgisayardaki deneme sunucusu `http://10.0.2.2:3200`'dür (yalnız deneme paketinde çalışır).

## Son durum

- `git log`: 3 commit (Android deposu). Son değişiklik `e96c5f2 commit 6` (2026-09-26, yerel uygulamanın çekirdeği):
  `Api.Hata`'ya sunucunun hata gövdesinin tamamı (`govde`) ve üç argümanlı yapıcı eklendi; hata fırlatılırken gövde
  veriliyor; uygulamanın bütün ekranları için Bearer'lı `oturumla(...)` geldi.
- Ondan önce `f70aeca commit 5` (2026-09-26, tek uygulamaya geçiş): uygulama anahtarı başlığı `UYGULAMA_BASLIGI`
  (`X-Cihaz`) ve onu kullanan `uygulama(...)` eklendi; `istek` başlık adını parametre olarak almaya başladı (önce hep
  `X-Aile-Cihaz` koyuyordu); `CIHAZ_BASLIGI`'na "çocuğun telefonu" açıklaması yazıldı. Dosyanın ilk hâli `34b45f1 commit 1`
  (2026-09-26, Eğitim Evi Aile 1.0.x): `adresSorunu`, `get`, `post`, `istek`, `oku` ve yalnız `X-Aile-Cihaz`.
- Bilinen açıklar (kod değiştirilmedi): yerel ağ denetiminin metin üstünde olması, JSON olmayan başarılı cevabın `{}`
  sayılması, hata yolunda `disconnect` olmaması.
- Planlı işlerden bu dosyaya dokunması beklenenler: "Android yerel uygulama" (bütün roller — tanım dosya yüklemeyi akışla
  POST ve `X-Dosya-Adi` başlığıyla, indirmeyi bilet adresi ve `DownloadManager` ile istiyor; bugünkü `istek` yalnız JSON
  taşıyor, yeni bir yükleme yolu gerekecek; uygulamanın kendini güncellemesi de sürüm bilgisini GitHub sürümlerinden ya
  da tanımdaki yeni `GET /api/uygulama/surum` ucundan okuyacak); "Çok dil" (tanım, sunucu hata iletilerinin istemcinin
  diliyle gelmesini istiyor — dil bilgisinin isteklerle gitmesi gerekecek, büyük olasılıkla buradan). "Güvenlik denetimi"
  tanımı uygulama oturumunun sınırlarını denetim alanına koyuyor; bu dosyadaki metin üstü yerel ağ kuralının da o
  denetimde gözden geçirilmesi önerilir (tanımda adı geçmiyor).

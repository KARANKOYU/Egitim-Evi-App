# app/src/main/java/org/egitimevi/aile/Kuyruk.java

Çocuğun telefonunda gönderilmeyi bekleyen konumların kuyruğu: uygulamanın özel klasöründe tek bir JSON dosyası
(`kuyruk.json`), en çok 5000 konum, 7 günden eskisi atılır; baştan sırayla okunur, gönderilen kadarı baştan silinir.

## Bu dosya ne yapar?

Eğitim Evi Aile'de (çocuğun telefonu) konum, internet olsa da olmasa da alınır. İnternet yokken (metroda, kırsalda, mobil
veri bitince) alınan konum kaybolmasın diye her konum önce bu kuyruğa yazılır. [IzlemeServisi.md](IzlemeServisi.md)
internet varken kuyruğun başından 500'erli parçalar alıp sunucuya gönderir ve gönderilen kadarını baştan siler. Böylece
veli, telefonun internetsiz geçen saatlerindeki konumlarını da sonradan görür.

Kuyruk telefonda kalıcıdır: uygulama kapansa, telefon yeniden başlasa da dosya durur. Sınırsız büyümemesi için iki kural
var: en çok 5000 konum (fazlası en eskiden atılır) ve 7 günden eski konum tutulmaz. 7 gün sunucunun kuralıyla aynı: sunucu
7 günden eski konumu kabul etmez, kabul ettiklerini de 7 gün sonra siler.

Sınıf `final`, örneği yok; dışa açık bütün işlevler `static synchronized`: aynı anda yalnız biri dosyaya dokunur. Bu
gerekli, çünkü konum ana iş parçacığından eklenir, gönderilen ise servisin kendi iş parçacığından silinir.

## İçinde neler var?

### Sabitler (iç)

- `DOSYA` — `"kuyruk.json"`, uygulamanın özel klasöründe (`Context.getFilesDir()`). Yazarken önce `kuyruk.json.yeni`.
- `EN_FAZLA` — 5000 konum.
- `YEDI_GUN` — 7 gün (milisaniye).

### Kuyruktaki bir konum

Kuyruk, `IzlemeServisi.onLocationChanged`'in kurduğu JSON nesnesini olduğu gibi saklar; alanların içine bakmaz, yalnız
`zaman`'ı okur:

```
{ "enlem": 39.92, "boylam": 32.85, "dogruluk": 18, "zaman": 1790000000000, "ag": "wifi", "pil": 80 }
```

`zaman` konumun alındığı an (Unix ms, `Location.getTime()`); `dogruluk` metre (konumda doğruluk yoksa alan hiç yok);
`ag` `"wifi"`, `"mobil"` ya da `""` (internet yokken alınmış); `pil` yüzde. Sunucuya giden biçim de tam bu.

### İşlevler

- `oku(c)` — dosyanın tamamını okur, `JSONArray` döner. Dosya yoksa, okunamazsa ya da JSON bozuksa BOŞ dizi döner, hata
  fırlatmaz. Dışa açık ama bugün yalnız bu sınıfın içinden çağrılıyor.
- `ekle(c, konum)` — okur, sona ekler, sonra süzer: dizinin son 5000 ögesini alır (baştaki fazlası atılır) ve bunlardan
  `zaman`'ı "şimdi − 7 gün"e eşit ya da daha yeni olanları tutar (`zaman`'ı olmayan da atılır). Dosyayı baştan yazar.
- `boyut(c)` — kaç konum bekliyor (dosyanın tamamını okuyup sayar).
- `bastan(c, n)` — ilk `n` konum, SİLMEDEN (yeni bir dizi).
- `sil(c, n)` — ilk `n` konumu siler (gönderim başarılı olunca).
- `temizle(c)` — dosyayı siler.
- `yaz(c, dizi)` (iç) — önce `kuyruk.json.yeni`'ye yazar, sonra `renameTo` ile asıl dosyanın yerine koyar: yazma yarıda
  kesilirse asıl dosya bozulmaz. Yazma hatasında sessizce vazgeçer; `renameTo`'nun sonucu da denetlenmez.
- `dosya(c)` (iç) — `getFilesDir()/kuyruk.json`.

## Kimle konuşur?

- Çağırdıkları: yalnız Android ve Java: `Context.getFilesDir()`, `FileInputStream` / `FileOutputStream`, `org.json`.
  Ağa ya da sunucuya kendisi gitmez.
- Onu çağıranlar (grep):
  - [IzlemeServisi.md](IzlemeServisi.md) — `ekle` (kabul edilen her konumda), `boyut`, `bastan(…, 500)`, `sil`
    (gönderimden sonra), `temizle` (sunucu telefonu tanımayınca, 401/403).
  - [AileEkrani.md](AileEkrani.md) — `boyut` ("Gönderilmeyi bekleyen konum: N"), `temizle` ("Bu telefonun bağlantısını
    kaldır").
- Sunucu (dolaylı; site deposunda `sunucu/bolumler/aile.md`): parçalar `POST /api/aile/cihaz/konum { konumlar }`'a gider.
  Bir istekte ilk 500 konum işlenir; geçersiz enlem/boylam, 7 günden eski ya da 5 dakikadan ileri zamanlı konum atılır;
  aynı öğrencide aynı zaman damgası ikinci kez yazılmaz (veritabanında `ON CONFLICT (ogrenci_id, zaman) DO NOTHING`).
- Yedek ve gizlilik: dosya uygulamanın özel klasöründe, başka uygulama okuyamaz. Manifestte `allowBackup="false"`;
  [veri_aktarimi.xml](../../../../res/xml/veri_aktarimi.xml) (Android 12+) ve [yedek_yok.xml](../../../../res/xml/yedek_yok.xml)
  (11 ve altı) uygulamanın bütün dosyalarını buluta yedeklemeden ve yeni telefona aktarmadan çıkarır.

## Nasıl çalışır (adım adım)?

```
yeni konum (ana iş parçacığı) ─► Kuyruk.ekle
    oku ─► [..., yeni] ─► son 5000 ─► zaman >= şimdi − 7 gün ─► yaz (.yeni ─► rename)

servis turu ya da konumdan hemen sonra ("aile-izleme" iş parçacığı)
    internet var ve boyut > 0 ?
      parca = bastan(500) ─► POST /api/aile/cihaz/konum ─► başarı ─► sil(parca.length)
      (en çok 10 parça: bir seferde 5000 konum, kuyruğun tamamı)
      hata ─► kuyruk olduğu gibi kalır, sonraki turda yeniden

sunucu 401/403 ya da "Bu telefonun bağlantısını kaldır" ─► temizle (dosya silinir)
```

Sayılarla: internet yokken servis seyrek aralığı (mobil veri aralığı, varsayılan 15 dakika) kullanır; bu ayarla 7 gün en
çok ~672 konum eder ve 7 gün kuralı 5000 sınırından önce devreye girer. 5000 sınırına ancak veli mobil aralığı 1
dakikaya indirdiyse ve telefon yaklaşık 3,5 gün internetsiz kaldıysa varılır.

## Dikkat!

- **Her ekleme dosyanın tamamını okuyup yeniden yazar.** `ekle`, `sil` ve `boyut` her çağrıda bütün dosyayı okur; `ekle`
  ve `sil` baştan yazar. Konum başına yaklaşık 100 bayt ile dolu bir kuyruk yüzlerce KB eder (ölçülmedi). `ekle` konum
  geldiğinde ana iş parçacığında çalışır (servisin konum dinleyicisi ana döngüye bağlı); `boyut` servis turunda birkaç kez
  çağrılır. Kuyruk küçükken fark edilmez; büyük kuyrukta her konum biraz iş demek.
- **Bozuk dosya sessizce boş sayılır.** JSON okunamazsa `oku` boş döner; bir sonraki `ekle` dosyanın üstüne yalnız yeni
  konumu yazar ve bekleyen konumlar uyarısız gider.
- **Yazma hatası sessiz.** Disk doluysa `ekle`'deki konum kaybolur; `sil`'deki silme olmaz, aynı konumlar yeniden
  gönderilir. Sunucu aynı zaman damgasını ikinci kez yazmadığı için yeniden gönderme zararsızdır.
- **`bastan` ile `sil` tek işlem değil.** Servis parçayı alıp sunucuya gönderirken (istek sürerken kilit bırakılmıştır)
  ana iş parçacığı yeni konum ekleyebilir. Ekleme yalnız sona yapılıyorsa sorun yok: `sil(n)` yine baştaki gönderilmiş
  n konumu siler. Ama kuyruk 5000'de doluysa ya da baştaki konumlar o arada 7 günü geçtiyse `ekle` BAŞTAN konum atar;
  o zaman `sil(n)` gönderilmemiş birkaç konumu da siler. Yalnız kuyruk dolu ya da çok eskiyken olur ve birkaç konum
  kaybettirir (kod okumasına göre). Düzeltme önerisi: silmeyi sayıyla değil, gönderilen son konumun `zaman`'ına kadar yapmak.
- **7 gün süzgeci yalnız eklemede.** Hiç yeni konum gelmezse (veli konumu kapattı, izin geri alındı) kuyrukta 7 günü
  geçmiş konum kalabilir ve gönderilir; sunucu onları atar (`alinan`'a girmez), telefon yine kuyruktan siler. Zararsız.
- **Kuyruk telefona aittir.** Bağlantı kaldırılınca ya da sunucu cihazı tanımayınca `temizle` çağrılır; telefon yeniden
  bağlansa (başka bir öğrenci hesabıyla bile) eski konumlar gönderilmez. Tek dar aralık: `temizle` ile servisin kapanması
  (`onDestroy`, konum dinlemesini bırakan yer) arasında gelen bir konum, `onLocationChanged` bağlı olup olmadığına
  bakmadığı için dosyayı yeniden açabilir; telefon 7 gün içinde yeniden bağlanırsa bu tek konum yeni bağlantıyla gider.
  Kod okumasına göre; telefonda denenmedi.
- **Konum kişisel veridir.** Telefonda en çok 7 gün, özel klasörde, yedeğe girmeden durur — aydınlatma metnindeki
  "Eğitim Evi Aile: telefonun konumu" satırının telefondaki karşılığı. Süre ya da biçim değişirse metin aynı işte
  güncellenmeli (site deposundaki KVKK kuralı).

## Testleri

- Android deposunda otomatik test yok; derleme ve lint (`./gradlew --offline assembleDebug lintDebug`) ana oturumda.
- Sunucu tarafını site deposundaki `testler/test-aile.js` korur: konum süzgeci (geçersiz enlem, 7 günden eski, gelecek
  zamanlı, bozuk zaman atılır; 2 geçerli konum alınır), "aynı zaman damgası iki kez yazılmaz" (bu kuyruğun yeniden
  göndermesini zararsız kılan kural), "bir istekte en fazla 500 konum işlenir" (servisin parça boyuyla aynı sayı), veli
  konumu kapatınca `alinan: 0, kapali: true`.
- Elle (öykünücü, deneme paketi, Aile'ye bağlı öğrenci telefonu):
  1. İnterneti kes: `adb shell svc wifi disable` ve `adb shell svc data disable`.
  2. Birkaç aralık boyunca konum ver (öykünücüde `adb emu geo fix <boylam> <enlem>`; aralık başına bir konum kabul edilir)
     → Aile ekranında "Gönderilmeyi bekleyen konum: N" artmalı.
  3. Dosyayı gör: `adb shell run-as org.egitimevi.aile cat files/kuyruk.json` (yalnız hata ayıklanabilir deneme
     paketinde çalışır).
  4. İnterneti aç → en geç bir turda sayı kaybolmalı, "Son gönderim" güncellenmeli, velinin sitedeki sayfasında konumlar
     görünmeli.
  5. "Bu telefonun bağlantısını kaldır" → `kuyruk.json` silinmiş olmalı.

## Son durum

- `git log` (Android deposu): 1 commit. Dosya `34b45f1 commit 1` (2026-09-26, Eğitim Evi Aile'nin ilk hâli, sürüm 1.0)
  ile geldi; o günden beri değişmedi.
- Bilinen açıklar (kod değiştirilmedi): her eklemede bütün dosyanın yeniden yazılması ve bunun ana iş parçacığında
  olması; bozuk dosyanın sessizce boş sayılması; `bastan`/`sil` arasındaki yarışta dolu kuyrukta birkaç konumun
  kaybolabilmesi; bağlantı kaldırılırken servis kapanmadan gelen bir konumun kuyrukta kalabilmesi.
- Planlı işlerden bu dosyaya dokunması beklenenler: "Android yerel uygulama" (tanım çocuğun telefonu kısmının — bu kuyruk
  dahil — eski 1.0.x kurulumlarla uyumlu kalmasını istiyor; konum biçimi ve 500'lük parça sunucuyla sözleşme); "KVKK ve
  onay metinleri TAM denetimi" (telefonda bekleyen konum da saklanan veri olarak gözden geçirilecek). Sunucuda Aile
  verisinin saklama süresi değişirse buradaki `YEDI_GUN` de birlikte değişmeli.
